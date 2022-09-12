package com.t1.SuperNode;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.rmi.Naming;

public class SuperNode extends UnicastRemoteObject implements SuperNodeInterface {

	// mapa de ip+port que devolve um mapa de recursos (hash, nome)
	protected HashMap<String, HashMap<Integer, String>> peersResources;
	// mapa de ip+port que devolve quanto tempo o peer esta sem mandar KeepAlive
	protected HashMap<String, Integer> peersTimeout;
	// name <ip, hash>
	protected HashMap<String, HashMap<String, String>> peersResponses;

	protected SuperNodeInterface server = null;

	protected Boolean hasToken = false;

	protected String myAddr = null;

	protected String nextAddr = null;

	public SuperNode(String myAddr, String nextAddr, Boolean hasToken) throws RemoteException {
		peersResources = new HashMap<>();
		peersTimeout = new HashMap<>();
		peersResponses = new HashMap<>();
		this.myAddr = myAddr;
		this.hasToken = hasToken;
		this.nextAddr = nextAddr;
		try {
			this.server = (SuperNodeInterface) Naming.lookup("rmi://" + nextAddr + ":9000/SuperNode");
		} catch (Exception e) {
			System.out.println("connection failed with server");
		}
		new Thread(() -> {
			while (true) {
				KeepAliveController();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (hasToken) {
					// passa token
				}
			}
		}).start();
	}

	public String commandHandler(String command) throws RemoteException {
		// find 'nome do arquivo'
		String[] vars = command.split(" ");
		String name = "";
		for (int i = 1; i < vars.length; i++) {
			name += vars[i];
		}
		switch (vars[0]) {
			case "find":
				HashMap<String, String> founded = findResource(name);
				return founded.toString();
			default:
				return "invalid command";
		}
	}

	public HashMap<String, String> findResourceInRing(String resourceName) {
		HashMap<String, String> response = new HashMap<>();
		try {
			this.server.sendResourceForNextNode(resourceName, myAddr, response);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return response;
		}
		while (response.size() == 0) {
			response = searchInResponse(resourceName);
			continue;
		}
		response.remove("0.0.0.0");
		return response;
	}

	public void sendResourceForNextNode(String resourceName, String addr, HashMap<String, String> response)
			throws RemoteException {
		if (!addr.equalsIgnoreCase(myAddr)) {
			response = findResourceInThisNode(resourceName, response);
			response.put("0.0.0.0", myAddr);
			this.server.sendResourceForNextNode(resourceName, addr, response);
		} else {
			// TODO: adicionar na response
			peersResponses.put(resourceName, response);
			if (this.hasToken) {
				this.hasToken = false;
				this.server.sendToken();
			}

		}
	}

	public HashMap<String, String> findResource(String resourceName) throws RemoteException {
		HashMap<String, String> resourcePeers = new HashMap<>();
		// TODO: Buscar no anel
		while (hasToken != true) {
			continue;
		}
		resourcePeers = findResourceInRing(resourceName);
		return findResourceInThisNode(resourceName, resourcePeers);
	}

	// devolve um <ip, hash>
	public HashMap<String, String> findResourceInThisNode(String resourceName, HashMap<String, String> resourcePeers) {

		peersResources.forEach((addr, resources) -> {
			resources.forEach((hash, name) -> {
				if (name.contains(resourceName)) {
					if (resourcePeers.containsKey(addr)) {
						// Mais de um file
						resourcePeers.put(addr, resourcePeers.get(addr) + "," + hash);
					} else {
						// Novo
						resourcePeers.put(addr, "" + hash);
					}
				}
			});
		});
		return resourcePeers;
	}

	// Outra thread
	// Escuta o token ring e :
	// - Receber um request de file -> findResource
	// -

	public String register(int port, HashMap<Integer, String> resources) throws RemoteException {
		String ip = "";
		String user_id = "";
		try {
			ip = RemoteServer.getClientHost();
			user_id = ip + ":" + port;
			peersResources.put(user_id, resources);
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
		return user_id;
	}

	// devolve um <ip, hash>
	public HashMap<String, String> searchInResponse(String resourceName) {
		HashMap<String, String> response = new HashMap<>();
		peersResponses.forEach((name, resources) -> {
			if (name.contains(resourceName)) {
				resources.forEach((addr, hash) -> {
					if (response.containsKey(addr)) {
						// Mais de um file
						response.put(addr, response.get(addr) + "," + hash);
					} else {
						// Novo
						response.put(addr, "" + hash);
					}
				});
				peersResponses.remove(name);
			}

		});
		return response;
	}

	public void KeepAliveController() {
		HashMap<String, Integer> peersTimeoutAux = new HashMap<>();

		peersTimeout.forEach((key, value) -> {
			value--;
			if (value == 0) {
				System.out.println(key + ".......... disconnected");
				disconnect(key);
			} else {
				peersTimeoutAux.put(key, value);
			}
		});
		peersTimeout = peersTimeoutAux;
	}

	public void disconnect(String id) {
		peersResources.remove(id);
	}

	public void KeepAlive(String id) throws RemoteException {
		peersTimeout.put(id, 3);
		System.out.println(id + ":.......... still here");
	}

	public void sendToken() throws RemoteException {
		this.hasToken = true;

	}
}
