package com.t1.Node;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.t1.ConsoleColors;
import com.t1.SuperNode.SuperNodeInterface;

public class Node extends Thread {
    protected String ip;
    protected int port;
    protected String superNodeIp;
    protected int superNodePort;
    protected String user_id;
    protected String dirPath;
    protected int downloadNumber;
    protected SuperNodeInterface server;
    protected SocketListener socketListener;

    public Node(String ip, int port, String superNodeIp, int superNodePort,
            ConcurrentHashMap<Integer, String> resources,
            String dirPath)
            throws IOException {
        // vamos verificar se o servidor está funcionando para nos registrarmos
        try {
            this.ip = ip;
            this.port = port;
            this.superNodeIp = superNodeIp;
            this.superNodePort = superNodePort;
            this.dirPath = dirPath;
            this.socketListener = new SocketListener(port, dirPath, resources);
            String superNodeRoute = "rmi://" + superNodeIp + ":" + superNodePort + "/SuperNode";
            System.out.println("Super node route -> " + superNodeRoute);
            this.server = (SuperNodeInterface) Naming.lookup(superNodeRoute);
            System.out.println("\n" + ConsoleColors.GREEN_BOLD + "Conectando " + ip + ":" + port + " no super nodo "
                    + superNodeIp + ":" + superNodePort + ConsoleColors.RESET + "\n");
            String user_id = this.server.register(port, resources);
            socketListener.start();
            new KeepAlive(server, user_id).start();
        } catch (Exception e) {
            System.out.println("Connection failed with server: " + e.getMessage());
        }
        this.downloadNumber = 0;
    }

    // Só deve realizar o comando find primeiro para saber qual o
    // ip, porta e hash - nome do arquivo em hash
    // para depois usar o comando download
    public void run() {
        String command;
        String response = "";
        Scanner in = new Scanner(System.in);
        // aqui sera a comunicacao entre o client e server
        // se o client mandar exit para o server, ele ira desconectar
        while (!response.equalsIgnoreCase("exit")) {
            System.out.println(
                    "\nComandos para o servidor:\n\tfind <resource name>\n\tdownload <nodeIp>:<nodePort> <archive hash>"
                            + ConsoleColors.YELLOW + "\n*Falta ajustar entrada do usuario" + ConsoleColors.RESET);
            // command = in.nextLine();
            // if (dirPath.contains("Node1Dir")) {
            command = in.nextLine();
            // } else {
            // command = "";
            // }

            if (command.contains("download")) {
                // lançar thread para download
                // logica de conexão direta com sockets
                // espera-se que o usuario faça o comando [download ip hash]
                downloadFile(command.split(" ")[1], Integer.parseInt(command.split(" ")[2]));

            } else {
                // manda para o servidor o comando
                try {
                    response = server.commandHandler(command);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                }
            }
            System.out.println(response + "\n\n\n");
        }
    }

    private void downloadFile(String requestIp, int hashCode) {
        new Thread(() -> {
            try {
                // initialize socket do send a request with hashcode of the file we will
                // download
                // socket port will be +1 because the SocketListener already have located this
                // port
                DatagramSocket socketToRequest = new DatagramSocket(this.port + 2);
                System.out.println("abri um socket datagram com a port: " + (this.port + 2) +
                        " para fazer o request do arquivo");
                InetAddress ipToRequest = InetAddress.getByName(requestIp.split(":")[0]);
                int portToRequest = Integer.parseInt(requestIp.split(":")[1]);
                byte[] contents = new byte[10000];
                contents = (hashCode + "").getBytes();
                DatagramPacket packet = new DatagramPacket(contents, contents.length, ipToRequest, portToRequest);
                // sending request and closing socket request
                System.out.println("enviando request");
                socketToRequest.send(packet);
                socketToRequest.close();

                // opening the socket will save the file
                // socket port will be +2 because the socketToRequest have already located this
                // port+1
                Socket socket = new Socket(ipToRequest, portToRequest + 1);
                System.out.println("abrindo o socket para estabelecer conexão com o socket usando a porta: "
                        + (portToRequest + 1));
                FileOutputStream fos = new FileOutputStream(this.dirPath + "/_" + this.downloadNumber + "_.txt");
                this.downloadNumber++;
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                InputStream is = socket.getInputStream();
                System.out.println("abri inputstream");
                // No of bytes read in one read() call
                int bytesRead = 0;
                while ((bytesRead = is.read(contents)) != -1) {
                    bos.write(contents, 0, bytesRead);
                }
                bos.flush();
                socket.close();
                System.out.println("File saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
