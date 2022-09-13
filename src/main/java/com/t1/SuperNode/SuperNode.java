package com.t1.SuperNode;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Time;
import java.util.HashMap;

import com.t1.ConsoleColors;

import java.rmi.Naming;

public class SuperNode extends UnicastRemoteObject implements SuperNodeInterface {

	// mapa de ip+port que devolve um mapa de recursos (hash, nome)
	protected HashMap<String, HashMap<Integer, String>> myNodes;
	// mapa de ip+port que devolve quanto tempo o peer esta sem mandar KeepAlive
	protected HashMap<String, Integer> peersTimeout;
	// name <ip, hash>
	protected HashMap<String, HashMap<String, String>> peersResponses;

	protected SuperNodeInterface server = null;

	protected String myAddr = null;

	protected int port = 0;

	protected String nextAddrPort = null;

	protected Boolean hasToken = false;

	public SuperNode(String myAddr, int port, String nextAddrPort, Boolean hasToken) throws RemoteException {
		myNodes = new HashMap<>();
		peersTimeout = new HashMap<>();
		peersResponses = new HashMap<>();
		this.myAddr = myAddr;
		this.port = port;
		this.nextAddrPort = nextAddrPort;
		this.hasToken = hasToken;
		System.out.println("\nmyAddr-> " + myAddr);
		System.out.println("port-> " + port);
		System.out.println("nextAddrPort-> " + nextAddrPort);
		System.out.println("hasToken-> " + hasToken);
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

	public void connectNext() throws RemoteException {
		try {
			String serverRoute = "rmi://" + myAddr + ":" + nextAddrPort + "/SuperNode";
			System.out.println("next server route -> " + serverRoute);
			Thread.sleep(10000);
			server = (SuperNodeInterface) Naming.lookup(serverRoute);
			System.out.println("Naming lookup -> " + serverRoute);

			System.out.println("\n" + ConsoleColors.GREEN_BOLD + "Portas do ip " + myAddr + " conectadas " + port
					+ " -> " + nextAddrPort + ConsoleColors.RESET + "\n");
			System.out.println();
		} catch (Exception e) {
			System.out.println("Connection failed with server: " + e.getMessage());
		}
	}

	/*
	 * Registra o Node em um determinado SuperNode
	 * Passando a porta que ele vai estar e um
	 * HashMap<hash do nome do arquivo, nome do arquivo>
	 */
	public String register(int port, HashMap<Integer, String> resources) throws RemoteException {
		System.out.println("Registrando o Node no SuperNode");
		String ip = "";
		String user_id = "";
		try {
			ip = RemoteServer.getClientHost();
			user_id = ip + ":" + port;
			System.out.println(user_id);
			// o SuperNode guarda a porta do node junto com o
			// HashMap <hash do nome do arquivo, nome do arquivo>
			myNodes.put(user_id, resources);
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
		return user_id;
	}

	public String commandHandler(String command) throws RemoteException {
		// find 'nome do arquivo'
		String[] vars = command.split(" ");
		String name = "";
		// vai armazenar o nome de todos os arquivos solicitados?
		for (int i = 1; i < vars.length; i++) {
			// ! aqui ele nao vai armazenar colado? se é pedido 'find teste test', a string
			// name vai ser testetest
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

	public HashMap<String, String> findResource(String resourceName) throws RemoteException {
		// ip + hash que tem o mesmo nome de arquivo
		HashMap<String, String> resourcePeers = new HashMap<>();
		// O SuperNode só procura quando ele tem o token?
		// TODO: Buscar no anel
		while (hasToken != true) {
			continue;
		}
		resourcePeers = findResourceInRing(resourceName);
		return findResourceInThisNode(resourceName, resourcePeers);
	}

	public HashMap<String, String> findResourceInRing(String resourceName) {
		HashMap<String, String> response = new HashMap<>();
		try {
			// buguei - como vai passar para o proximo
			// magica do rmi?
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

	// devolve um <ip, hash>
	public HashMap<String, String> findResourceInThisNode(String resourceName, HashMap<String, String> resourcePeers) {

		myNodes.forEach((addr, resources) -> {
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
		myNodes.remove(id);
	}

	public void KeepAlive(String id) throws RemoteException {
		peersTimeout.put(id, 3);
		System.out.println(id + ":.......... still here");
	}

	public void sendToken() throws RemoteException {
		this.hasToken = true;

	}
}
