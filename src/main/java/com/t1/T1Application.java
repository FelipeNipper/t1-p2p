package com.t1;

import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Scanner;

import com.t1.Node.Node;
import com.t1.SuperNode.SuperNode;
import com.t1.SuperNode.SuperNodeInterface;

public class T1Application {

	private static String superNodeIp;

	// private static String nextSuperNodeIpPort;
	private static String nextSuperNodeIp;

	private static int superNodePort;

	private static int nodePort;

	private static boolean hasToken;

	public static void main(String[] args) throws InterruptedException {
		// SuperNode
		if (System.getenv("type").equalsIgnoreCase("SuperNode")) {
			superNodeIp = System.getenv("ip");
			superNodePort = Integer.parseInt(System.getenv("port"));
			nextSuperNodeIp = System.getenv("nextIp");
			hasToken = System.getenv("hasToken").equalsIgnoreCase("1") ? true : false;

			SuperNodeCreate();
		} else {
			nodePort = Integer.parseInt(System.getenv("port"));
			superNodeIp = System.getenv("superNodeIp");
			superNodePort = Integer.parseInt(System.getenv("superNodePort"));
			NodeCreate();
		}
	}

	public static void SuperNodeCreate() {
		System.out.println("\n" + ConsoleColors.GREEN_BOLD + " -----------> Criando SuperNode <----------"
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
			SuperNodeInterface superNode = new SuperNode(superNodeIp, superNodePort, nextSuperNodeIp, hasToken);
			Naming.rebind(server, superNode);
			superNode.connectNext();
			System.out.println("P2P SuperNode is ready.");
		} catch (Exception e) {
			System.out.println(ConsoleColors.RED + "P2P SuperNode failed: " + e + ConsoleColors.RESET);
		}
	}

	public static void NodeCreate() throws InterruptedException {
		System.out
				.println("\n" + ConsoleColors.GREEN_BOLD + "-----------> Criando node com os seus arquivos <----------"
						+ ConsoleColors.RESET + "\n");
		Scanner in = new Scanner(System.in);

		System.out.println(
				"\nDigite o path para um diretorio de arquivos: \nEx - src/main/java/com/t1/<nome do diretório>"
						+ ConsoleColors.YELLOW + " \n*Falta ajustar entrada do usuario" + ConsoleColors.RESET);
		// String dirPath = in.next();
		String dirPath = System.getenv("dir");

		String allPath = "src/main/java/com/t1/" + dirPath;
		FileTerminal.InputTempFile(allPath);
		HashMap<Integer, String> resources = readPath(allPath);
		try {
			Thread.sleep(10000);
			System.out.println("WAIT");
			new Node(nodePort, superNodeIp, superNodePort, resources, dirPath).start();
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
