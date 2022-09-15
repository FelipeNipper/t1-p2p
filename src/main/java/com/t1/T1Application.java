package com.t1;

import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.t1.Node.Node;
import com.t1.SuperNode.SuperNode;
import com.t1.SuperNode.SuperNodeInterface;

public class T1Application {

	private static String superNodeIp;

	private static String nextSuperNodeIp;

	private static int superNodePort;

	private static String nodeIp;

	private static int nodePort;

	private static boolean hasToken;

	public static void main(String[] args) throws InterruptedException, IOException {
		testeee();
		/*
		 * // SuperNode
		 * if (System.getenv("type").equalsIgnoreCase("SuperNode")) {
		 * superNodeIp = System.getenv("ip");
		 * superNodePort = Integer.parseInt(System.getenv("port"));
		 * nextSuperNodeIp = System.getenv("nextIp");
		 * hasToken = System.getenv("hasToken").equalsIgnoreCase("1") ? true : false;
		 * 
		 * SuperNodeCreate();
		 * } else {
		 * nodeIp = System.getenv("ip");
		 * nodePort = Integer.parseInt(System.getenv("port"));
		 * superNodeIp = System.getenv("superNodeIp");
		 * superNodePort = Integer.parseInt(System.getenv("superNodePort"));
		 * NodeCreate();
		 * }
		 */
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
			// SuperNodeInterface superNode = new SuperNode(superNodeIp, superNodePort,
			// 		nextSuperNodeIp, hasToken);
			Naming.rebind(server, new SuperNode(superNodeIp, superNodePort,
					nextSuperNodeIp, hasToken));
			// superNode.connectNext();
			System.out.println("P2P SuperNode is ready.");
		} catch (Exception e) {
			System.out.println(ConsoleColors.RED + "P2P SuperNode failed: " + e +
					ConsoleColors.RESET);
		}
	}

	public static void NodeCreate() throws InterruptedException {
		System.out
				.println("\n" + ConsoleColors.GREEN_BOLD +
						"-----------> Criando node com os seus arquivos <----------"
						+ ConsoleColors.RESET + "\n");

		// String dir = System.getenv("dir");
		String dirPath = "src/main/java/com/t1/Resources/" + System.getenv("dir");
		String terminalPath = "src/main/java/com/t1/Terminal/" +
				System.getenv("terminal") + ".txt";

		ConcurrentHashMap<Integer, String> resources = readPath(dirPath);
		try {
			System.out.println("WAIT");
			Thread.sleep(2000);
			new Node(nodeIp, nodePort, superNodeIp, superNodePort, resources, dirPath,
					terminalPath).start();
		} catch (IOException e) {

		}
	}

	/*
	 * Passa por todos os files do diretório recebido
	 * HashMap com o <hash do nome, nome do arquivo>
	 */
	public static ConcurrentHashMap<Integer, String> readPath(String path) {
		ConcurrentHashMap<Integer, String> resources = new ConcurrentHashMap<>();
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				int hash = calculateHash(listOfFiles[i].getName());
				resources.put(hash, listOfFiles[i].getName());
				System.out.println("File " + listOfFiles[i].getName() + " -> " + hash);
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}

		return resources;
	}

	public static int calculateHash(String name) {
		return name.hashCode();
	}

	// }

	/*
	 * String command = null;
	 * System.out.println(
	 * "Comandos para o SUPER NODO:  \n[ find <nome do arquivo> ]\n[ download <ip>:<port> <hash> ]\n[ all ]\n[ exit ]"
	 * );
	 * // command = in.nextLine();
	 * while (command == null || command.equalsIgnoreCase("")) {
	 * command = FileTerminal.inputFile("src/main/java/com/t1/Terminal/Node1.txt");
	 * }
	 * System.out.println("sad " + command);
	 * }
	 */

	public static void testeee() throws IOException {
		String command = "";
		String terminalPath = "src/main/java/com/t1/Terminal/Node1.txt";
		while (true) {
			System.out.println(
					"Comandos para o SUPER NODO:  \n[ find <nome do arquivo> ]\n[ download <ip>:<port> <hash> ]\n[ all ]\n[ exit ]");
			// command = in.nextLine();
			while (command == null || command.equalsIgnoreCase("")) {
				command = FileTerminal.inputFile("src/main/java/com/t1/Terminal/Node1.txt");
			}
			System.out.println("sad " + command);
		}
		/*
		 * 
		 * 
		 * while (true) {
		 * try {
		 * System.out.println(
		 * "Comandos para o SUPER NODO:  \n\tfind <nome do arquivo> \n\tdownload <ip>:<port> <hash> \n\tall \n\texit"
		 * );
		 * 
		 * while (command == null || command.equalsIgnoreCase("")) {
		 * command = FileTerminal.inputFile(terminalPath);
		 * }
		 * // FileTerminal.cleanFile(terminalPath);
		 * // System.out.println("command -> " + command);
		 * String[] exec = command.split(" ");
		 * switch (exec[0]) {
		 * case "find":
		 * // System.out.println("Response => " + mySuperNode.findHandler(exec[1]));
		 * System.out.println("find");
		 * break;
		 * case "download":
		 * System.out.println("download");
		 * 
		 * // downloadFile(exec[1], Integer.parseInt(exec[2]));
		 * break;
		 * case "all":
		 * System.out.println("all");
		 * 
		 * // mySuperNode.getAllHash()
		 * break;
		 * case "exit":
		 * System.out.println("exit");
		 * 
		 * // fazer desconectar
		 * return;
		 * default:
		 * System.out.println(ConsoleColors.RED + "Comando inválido: " + command +
		 * ConsoleColors.RESET);
		 * }
		 * } catch (Exception e) {
		 * System.out.println(e.getMessage());
		 * return;
		 * }
		 * }
		 */
	}
}