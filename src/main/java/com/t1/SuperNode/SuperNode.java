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

	protected HashMap<String, String> peersResponses;

	protected SuperNodeInterface server = null;

	protected Boolean hasToken = false;

	protected String myAddr = null;

	public SuperNode(String myAddr, String nextServerIp, Boolean hasToken) throws RemoteException {
		peersResources = new HashMap<>();
		peersTimeout = new HashMap<>();
		peersResponses = new HashMap<>();
		myAddr = myAddr;
		hasToken = hasToken;
		try {
			this.server = (SuperNodeInterface) Naming.lookup("rmi://" + nextServerIp + ":9000/SuperNode");
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
		// Dispara
		HashMap<String, String> response = new HashMap<>();
		while (response.size() == 0) {
			response = searchInResponse(resourceName);
			continue;
		}
		return response;
	}

	public void sendResourceForNextNode(String resourceName, String addr, HashMap<String, String> response)
			throws RemoteException {
		if (addr = !myAddr) {
			response = findResourceInThisNode(resourceName, response);
			this.server.sendResourceForNextNode(resourceName, addr, response);
		} else {
			// TODO: adicionar na response
			response.forEach((respAddr, content) -> {
				if (name.contains(resourceName)) {
					if (peersResources.containsKey(addr)) {
						// Mais de um file
						peersResources.put(respAddr, peersResources.get(respAddr) + content);
					} else {
						// Novo
						peersResources.put(respAddr, content);
					}
				}
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

	public HashMap<String, String> findResourceInThisNode(String resourceName, HashMap<String, String> resourcePeers) {

		peersResources.forEach((addr, resources) -> {
			resources.forEach((hash, name) -> {
				if (name.contains(resourceName)) {
					if (resourcePeers.containsKey(addr)) {
						// Mais de um file
						resourcePeers.put(addr, resourcePeers.get(addr) + ", (" + name + ", " + hash + ")");
					} else {
						// Novo
						resourcePeers.put(addr, "(" + name + ", " + hash + ")");
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

	public HashMap<String, String> searchInResponse(String resourceName) {
		HashMap<String, String> response = new HashMap<>();
		peersResponses.forEach((addr, resources) -> {
			resources.forEach((hash, name) -> {
				if (name.contains(resourceName)) {
					if (response.containsKey(addr)) {
						// Mais de um file
						response.put(addr, response.get(addr) + ", (" + name + ", " + hash +
								")");
						peersResponses.remove(addr);
					} else {
						// Novo
						response.put(addr, "(" + name + ", " + hash + ")");
						peersResponses.remove(addr);
					}
				}
			});
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
		// TODO Auto-generated method stub

	}
}
