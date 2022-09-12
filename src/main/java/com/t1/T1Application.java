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

	private static int superNodePort;

	public static void main(String[] args) throws IOException {
		// SuperNode
		// Node -> java 0 <superNodeIp>
		if (args[0] == "0") {
			superNodePort = 9000;
			superNodeIp = args[1];
			SuperNodeCreate();
		}
		// Node -> java <superNodeIp>
		else {
			superNodeIp = args[0];
		}
	}

	public static void SuperNodeCreate() {
		try {
			// superNodeIp = args[1];
			System.setProperty("java.rmi.server.hostname", superNodeIp);
			LocateRegistry.createRegistry(superNodePort);
			System.out.println("RMI registry ready.");
		} catch (Exception e) {
		}
		try {
			// String superNodeIp = "0.0.0.0";
			String server = "rmi://" + superNodeIp + ":" + superNodePort + "/SuperNode";
			// String nextSuperNodeIp = "0.0.0.1";
			// Naming.rebind(server, new SuperNode(superNodeIp, nextSuperNodeIp, true));
			Naming.rebind(server, new SuperNode("", "", true));// myAddr, nextAddr, hasToken));
			System.out.println("p2p SuperNode is ready.");
		} catch (Exception e) {
			System.out.println("p2p SuperNode failed: " + e);
		}
	}

	public static void NodeCreate() {
		Scanner in = new Scanner(System.in);

		System.out.println("Digite uma porta para disponibilizar para outros peers: ");
		int port = in.nextInt();
		// validar se a porta esta disponivel e pedir outra para o usuario

		System.out.println("\nDigite o path para um diretorio de arquivos: \nEx - src/main/java/com/t1/dir");
		String dirPath = in.next();

		// readPath("src/main/java/com/t1/dir");
		HashMap<Integer, String> resources = readPath(dirPath);
		try {
			// String superNodeIp = "0.0.0.0";;
			new Node(superNodeIp, port, resources, dirPath).start();
		} catch (IOException e) {

		}
	}

	public static HashMap<Integer, String> readPath(String path) {
		HashMap<Integer, String> resources = new HashMap<>();
		File folder = new File(path);
		// pega todos os files que tem no diretorio
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				// adiciona no hashmap <hash do nome, name>
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
