package com.t1;

import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Scanner;

import com.t1.Node.Node;
import com.t1.SuperNode.SuperNode;

public class T1Application {

	private static String superNodeIp;

	private static String nextSuperNodeIpPort;

	private static int superNodePort;

	private static boolean hasToken;

	public static void main(String[] args) throws IOException {
		superNodePort = Integer.parseInt(System.getenv("port"));
		// SuperNode
		System.out.println("----->" + System.getenv("type"));
		if (System.getenv("type").equalsIgnoreCase("SuperNode")) {
			superNodePort = 9000;
			superNodeIp = System.getenv("ip");
			nextSuperNodeIpPort = System.getenv("nextIpPort");
			hasToken = System.getenv("hasToken").equalsIgnoreCase("1") ? true : false;
			SuperNodeCreate();
		} else {
			superNodeIp = args[0];
			NodeCreate();
		}
	}

	public static void SuperNodeCreate() {
		System.out.println("\n" + ConsoleColors.GREEN_BOLD + "Criando SuperNode" + ConsoleColors.RESET + "\n");
		try {
			System.setProperty("java.rmi.server.hostname", superNodeIp);
			LocateRegistry.createRegistry(superNodePort);
			System.out.println("RMI registry ready.");
		} catch (Exception e) {
		}
		try {
			String server = "rmi://" + superNodeIp + ":" + superNodePort + "/SuperNode" + System.getenv("numSuperNode");
			// System.out.println(
			// "Connection failed with server: Connection refused to host: 127.0.0.1; nested
			// exception is: java.net.ConnectException: Connection refused");
			Naming.rebind(server, new SuperNode(superNodeIp, superNodePort, nextSuperNodeIpPort, hasToken));
			System.out.println("p2p SuperNode is ready.");
		} catch (Exception e) {
			System.out.println(ConsoleColors.RED + "p2p SuperNode failed: " + e + ConsoleColors.RESET);
		}
	}

	public static void NodeCreate() {
		System.out
				.println("\n" + ConsoleColors.GREEN_BOLD + "-----------> Criando node com os seus arquivos<----------\t"
						+ ConsoleColors.RESET + "\n");
		Scanner in = new Scanner(System.in);

		System.out.println("Digite uma porta para disponibilizar para outros peers: ");
		int port = in.nextInt();
		// validar se a porta esta disponivel e pedir outra para o usuario

		System.out.println(
				"\nDigite o path para um diretorio de arquivos: \nEx - src/main/java/com/t1/<nome do diretório>");
		String dirPath = in.next();

		String allPath = "src/main/java/com/t1/" + dirPath;
		HashMap<Integer, String> resources = readPath(allPath);
		try {
			new Node(superNodeIp, port, resources, dirPath).start();
		} catch (IOException e) {

		}
	}

	/*
	 * Passa por todos os files do diretório recebido
	 * HashMap com o <hash do nome, nome do arquivo>
	 */
	public static HashMap<Integer, String> readPath(String path) {
		HashMap<Integer, String> resources = new HashMap<>();
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				resources.put(calculateHash(listOfFiles[i].getName()), listOfFiles[i].getName());
				System.out.println("File " + listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}

		return resources;
	}

	public static int calculateHash(String name) {
		return name.hashCode();
	}
}
