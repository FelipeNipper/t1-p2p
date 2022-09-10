package client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
public class clientApp {

    public static void main(String[] args){
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
