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

	public static void main(String[] args) throws IOException {
		// SuperNode
		String[] argsTemp = { "0", "asuhd" };
		if (argsTemp[0] == "0") {
			try {
				System.setProperty("java.rmi.server.hostname", argsTemp[1]);
				LocateRegistry.createRegistry(9000);
				System.out.println("RMI registry ready.");
			} catch (Exception e) {
			}
			try {
				String server = "rmi://" + argsTemp[1] + ":9000/SuperNode";
				Naming.rebind(server, new SuperNode(server));
				System.out.println("p2p SuperNode is ready.");
			} catch (Exception e) {
				System.out.println("p2p SuperNode failed: " + e);
			}
		}
		// Node
		else {
			if (argsTemp.length < 1) {
				System.out.println("Uso: java clientApp <server> \"<message>\" ");
				return;
			} else {
				Scanner in = new Scanner(System.in);

				System.out.println("digite uma porta para disponibilizar para outros peers");
				int port = in.nextInt();
				// validar se a porta esta disponivel e pedir outra para o usuario

				System.out.println("digite o path para um diretorio de arquivos");
				String dirPath = in.next();

				HashMap<Integer, String> resources = readPath(dirPath);
				try {
					new Node(argsTemp[0], port, resources, dirPath).start();
				} catch (IOException e) {
				}
			}
		}

	}

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
