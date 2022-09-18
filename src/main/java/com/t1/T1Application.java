package com.t1;

import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.ConcurrentHashMap;

import com.t1.Node.Node;
import com.t1.SuperNode.SuperNode;
import com.t1.SuperNode.SuperNodeInterface;

public class T1Application {

	private static String superNodeIp;

	private static String nextSuperNodeIp;

	private static int superNodePort;

	private static String nodeIp;

	private static String position;

	private static int nodePort;

	private static double minHash = Integer.MIN_VALUE;

	private static double rangeHash;

	private static double superNodeSpace;

	private static double totalHash = 4294967296.0;

	public static void main(String[] args) throws InterruptedException, IOException {

		// SuperNode
		if (System.getenv("type").equalsIgnoreCase("SuperNode")) {
			int numSuperNode = 4;
			rangeHash = totalHash / numSuperNode;
			superNodeSpace = (int) (rangeHash *
					Integer.parseInt(System.getenv("position"))) + minHash;

			superNodeIp = System.getenv("ip");
			superNodePort = Integer.parseInt(System.getenv("port"));
			nextSuperNodeIp = System.getenv("nextIp");
			position = System.getenv("position");

			SuperNodeCreate();
		} else {
			nodeIp = System.getenv("ip");
			nodePort = Integer.parseInt(System.getenv("port"));
			superNodeIp = System.getenv("superNodeIp");
			superNodePort = Integer.parseInt(System.getenv("superNodePort"));
			NodeCreate();
		}

	}

	public static void SuperNodeCreate() {
		System.out.println("\n" + ConsoleColors.GREEN_BOLD +
				" -----------> Criando SuperNode <----------"
				+ ConsoleColors.RESET + "\n");
		try {
			// Inicializando Servidor RMI
			System.setProperty("java.rmi.server.hostname", superNodeIp);
			LocateRegistry.createRegistry(superNodePort);
			System.out.println("RMI registry ready.");
		} catch (Exception e) {
		}
		try {
			// Registrando Rotas
			String server = "rmi://" + superNodeIp + ":" + superNodePort + "/SuperNode";
			SuperNodeInterface superNode = new SuperNode(superNodeIp, superNodePort,
					nextSuperNodeIp, superNodeSpace, superNodeSpace + rangeHash);
			Naming.rebind(server, superNode);
			superNode.connectNext();
			System.out.println("P2P SuperNode is ready.");
		} catch (Exception e) {
			System.out.println(ConsoleColors.RED + "P2P SuperNode failed: " + e +
					ConsoleColors.RESET);
		}
	}

	public static void NodeCreate() throws InterruptedException, IOException {
		System.out
				.println("\n" + ConsoleColors.GREEN_BOLD +
						"-----------> Criando node com os seus arquivos <----------"
						+ ConsoleColors.RESET + "\n");

		String dirPath = "src/main/java/com/t1/Resources/" + System.getenv("dir");
		String terminalPath = "src/main/java/com/t1/Terminal/" +
				System.getenv("terminal") + ".txt";

		ConcurrentHashMap<Integer, String> resources = readPath(dirPath);
		try {
			System.out.println("WAIT SUPER NODES CONNECT");
			Thread.sleep(5000);
			new Node(nodeIp, nodePort, superNodeIp, superNodePort, resources, dirPath,
					terminalPath).start();
		} catch (IOException e) {

		}
	}

	/*
	 * Passa por todos os files do diretório recebido
	 * HashMap com o <hash do nome, nome do arquivo>
	 */
	public static ConcurrentHashMap<Integer, String> readPath(String path) throws IOException {
		ConcurrentHashMap<Integer, String> resources = new ConcurrentHashMap<>();
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				// int hash = calculateHash(listOfFiles[i].getName());
				int hash = FileTerminal.hashFile(listOfFiles[i]);
				resources.put(hash, listOfFiles[i].getName());
			}
		}
		return resources;
	}

	public static int calculateHash(String name) {
		return name.hashCode();
	}
}

// ver range
// hash do conteudo do arquivo - ver
// download