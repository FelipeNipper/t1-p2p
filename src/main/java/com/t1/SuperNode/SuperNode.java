package com.t1.SuperNode;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import com.t1.ConsoleColors;

import java.rmi.Naming;

public class SuperNode extends UnicastRemoteObject implements SuperNodeInterface {

	// mapa de ip+port que devolve um mapa de recursos (hash, nome)
	protected ConcurrentHashMap<String, ConcurrentHashMap<Integer, String>> myResources;

	// mapa de ip+port que devolve quanto tempo o nodo esta sem mandar KeepAlive
	protected ConcurrentHashMap<String, Integer> nodesTimeout;

	// name <ip, hash>
	protected ConcurrentHashMap<String, ConcurrentHashMap<String, String>> nodesResponses;

	protected SuperNodeInterface nextSuperNode;

	protected String myAddr;

	protected int port;

	protected double minHash;

	protected double maxHash;

	protected String nextAddr;

	public SuperNode(String myAddr, int port, String nextAddr, double minHash, double maxHash) throws RemoteException {
		myResources = new ConcurrentHashMap<>();
		nodesTimeout = new ConcurrentHashMap<>();
		nodesResponses = new ConcurrentHashMap<>();
		this.myAddr = myAddr;
		this.port = port;
		this.nextAddr = nextAddr;
		this.minHash = minHash;
		this.maxHash = maxHash;
		new Thread(() -> {
			while (true) {
				KeepAliveController();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void connectNext() throws RemoteException {
		boolean connected = false;
		while (!connected) {
			try {
				Thread.sleep(1000);
				String nextSuperNodeRoute = "rmi://" + nextAddr + ":" + port + "/SuperNode";
				System.out.println("Next super node route -> " + nextSuperNodeRoute);
				nextSuperNode = (SuperNodeInterface) Naming.lookup(nextSuperNodeRoute);

				System.out.println(
						"\n" + ConsoleColors.GREEN_BOLD + "Conectando " + myAddr + ":" + port + " -> " + nextAddr
								+ ":" + port + ConsoleColors.RESET);
				connected = true;
				System.out.println();
			} catch (Exception e) {
				System.out
						.println(ConsoleColors.RED_BOLD + "Falha ao conectar " + myAddr + ":" + port + " -> " + nextAddr
								+ ":" + port + ConsoleColors.RESET);
			}
		}
	}

	/*
	 * Registra o Node em um determinado SuperNode
	 * Passando a porta que ele vai estar e um
	 * HashMap<hash do nome do arquivo, nome do arquivo>
	 */
	public String register(String ip, int port, ConcurrentHashMap<Integer, String> resources) throws RemoteException {
		String node = ip + ":" + port;
		resources.forEach((hash, fileName) -> {
			try {
				ConcurrentHashMap<Integer, String> specificResource = new ConcurrentHashMap<>();
				specificResource.put(hash, fileName);
				if (minHash <= hash && hash < maxHash) {
					System.out.println("\tGuardando hash " + hash + " no super node de ip " + myAddr);
					if (myResources.containsKey(node)) {
						myResources.get(node).put(hash, fileName);
					} else {
						myResources.put(node, specificResource);
					}
				} else {
					nextSuperNode.findRightSuperNodeToStoreHash(node, specificResource, hash, fileName);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return node;
	}

	public void findRightSuperNodeToStoreHash(String node, ConcurrentHashMap<Integer, String> specificResource,
			int hash, String fileName)
			throws RemoteException {
		if (minHash <= hash && hash < maxHash) {
			System.out.println("\tGuardando hash " + hash + " no super node de ip " + myAddr);
			if (myResources.containsKey(node)) {
				myResources.get(node).put(hash, fileName);
			} else {
				myResources.put(node, specificResource);
			}
		} else {
			nextSuperNode.findRightSuperNodeToStoreHash(node, specificResource, hash, fileName);
		}
	}

	// Faz o find
	public String findHandler(String fileName) throws RemoteException {
		System.out.println("\t" + ConsoleColors.BLUE + "PROCURANDO POR " + fileName + ConsoleColors.RESET);
		ConcurrentHashMap<String, String> founded = findResource(fileName);
		return founded.toString();
	}

	public String allHandler() throws RemoteException {
		System.out.println("\t" + ConsoleColors.BLUE + "PROCURANDO POR TODOS" + ConsoleColors.RESET);
		ConcurrentHashMap<String, String> founded = findResource("");
		return founded.toString();
	}

	public ConcurrentHashMap<String, String> findResource(String resourceName) throws RemoteException {
		// ip + hash que tem o mesmo nome de arquivo
		ConcurrentHashMap<String, String> resourceNodes = new ConcurrentHashMap<>();
		resourceNodes = findResourceInRing(resourceName);
		return findResourceInThisNode(resourceName, resourceNodes);
	}

	public ConcurrentHashMap<String, String> findResourceInRing(String resourceName) {
		ConcurrentHashMap<String, String> response = new ConcurrentHashMap<>();
		try {
			System.out.println("Buscando no proximo");
			nextSuperNode.sendResourceForNextNode(resourceName, myAddr, response);
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

	public void sendResourceForNextNode(String resourceName, String addr, ConcurrentHashMap<String, String> response)
			throws RemoteException {
		if (!addr.equalsIgnoreCase(myAddr)) {
			response = findResourceInThisNode(resourceName, response);
			response.put("0.0.0.0", myAddr);
			nextSuperNode.sendResourceForNextNode(resourceName, addr, response);
		} else {
			// TODO: adicionar na response
			nodesResponses.put(resourceName, response);
		}
	}

	// devolve um <ip, hash>
	public ConcurrentHashMap<String, String> findResourceInThisNode(String resourceName,
			ConcurrentHashMap<String, String> resourceNodes) {
		myResources.forEach((addr, resources) -> {
			resources.forEach((hash, name) -> {
				if (name.contains(resourceName)) {
					if (resourceNodes.containsKey(addr)) {
						// Mais de um file
						resourceNodes.put(addr, resourceNodes.get(addr) + "," + hash);
					} else {
						// Novo
						resourceNodes.put(addr, "" + hash);
					}
				} else if (resourceName.equalsIgnoreCase("")) {
					if (resourceNodes.containsKey(addr)) {
						// Mais de um file
						resourceNodes.put(addr, resourceNodes.get(addr) + "," + hash);
					} else {
						// Novo
						resourceNodes.put(addr, "" + hash);
					}
				}
			});
		});
		return resourceNodes;
	}

	// devolve um <ip, hash>
	public ConcurrentHashMap<String, String> searchInResponse(String resourceName) {
		ConcurrentHashMap<String, String> response = new ConcurrentHashMap<>();

		System.out.println(nodesResponses.size());
		nodesResponses.forEach((name, resources) -> {
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
				nodesResponses.remove(name);
			} else if (resourceName.equalsIgnoreCase("")) {
				resources.forEach((addr, hash) -> {
					if (response.containsKey(addr)) {
						// Mais de um file
						response.put(addr, response.get(addr) + "," + hash);
					} else {
						// Novo
						response.put(addr, "" + hash);
					}
				});
			}
		});
		return response;
	}

	public void KeepAliveController() {
		ConcurrentHashMap<String, Integer> nodesTimeoutAux = new ConcurrentHashMap<>();

		nodesTimeout.forEach((key, value) -> {
			value--;
			if (value == 0) {
				System.out.println(key + ".......... disconnected");
				disconnect(key);
			} else {
				nodesTimeoutAux.put(key, value);
			}
		});
		nodesTimeout = nodesTimeoutAux;
	}

	public void disconnect(String id) {
		myResources.remove(id);
	}

	public void KeepAlive(String id) throws RemoteException {
		nodesTimeout.put(id, 3);
		System.out.println(id + ":.......... still here");
	}
}
