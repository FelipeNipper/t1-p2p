package server;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class NodeMain {
	public static void main(String[] args) throws IOException {
		if (args[0] == 0) {
			try {
				System.setProperty("java.rmi.server.hostname", args[1]);
				LocateRegistry.createRegistry(9000);
				System.out.println("RMI registry ready.");
			} catch (Exception e) {
			}
			try {
				String server = "rmi://" + args[1] + ":9000/SuperNode";
				Naming.rebind(server, new SuperNode());
				System.out.println("p2p SuperNode is ready.");
			} catch (Exception e) {
				System.out.println("p2p SuperNode failed: " + e);
			}
		} else {
			if (args.length < 1) {
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
					new Client(args[0], port, resources, dirPath).start();
				} catch (IOException e) {}
			}
		}
		
	}

    public static HashMap<Integer, String> readPath(String path){
        HashMap<Integer, String> resources = new HashMap<>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                resources.put(calculateHash(listOfFiles[i].getName()),listOfFiles[i].getName());
                System.out.println("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                    System.out.println("Directory " + listOfFiles[i].getName());
                }
        }

        return resources;
    }

    public static int calculateHash(String name){
        return name.hashCode();
    }
}