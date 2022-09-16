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
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.t1.ConsoleColors;
import com.t1.FileTerminal;
import com.t1.SuperNode.SuperNodeInterface;

public class Node extends Thread {
    protected String ip;
    protected int port;
    protected String superNodeIp;
    protected int superNodePort;
    protected String node;
    protected String dirPath;
    protected String terminalPath;
    protected int downloadNumber;
    protected SuperNodeInterface mySuperNode;
    protected SocketListener socketListener;

    public Node(String ip, int port, String superNodeIp, int superNodePort,
            ConcurrentHashMap<Integer, String> resources,
            String dirPath, String terminalPath)
            throws IOException {
        try {
            this.ip = ip;
            this.port = port;
            this.superNodeIp = superNodeIp;
            this.superNodePort = superNodePort;
            this.dirPath = dirPath;
            this.terminalPath = terminalPath;
            this.socketListener = new SocketListener(port, dirPath, resources);
            String superNodeRoute = "rmi://" + superNodeIp + ":" + superNodePort + "/SuperNode";
            System.out.println("Super node route -> " + superNodeRoute);
            this.mySuperNode = (SuperNodeInterface) Naming.lookup(superNodeRoute);
            System.out.println("\n" + ConsoleColors.GREEN_BOLD + "Conectando " + ip + ":" + port + " no super nodo "
                    + superNodeIp + ":" + superNodePort + ConsoleColors.RESET + "\n");
            String node = mySuperNode.register(ip, port, resources);
            socketListener.start();
            new KeepAlive(mySuperNode, node).start();
        } catch (Exception e) {
            System.out.println("Connection failed with super node: " + e.getMessage());
        }
        this.downloadNumber = 0;
    }

    public void run() {
        String command = "";

        while (true && !command.equalsIgnoreCase("exit")) {
            try {
                command = "";
                FileTerminal.cleanFile(terminalPath);
                System.out.println(
                        "Comandos para o SUPER NODO: \n[ find <nome do arquivo> ]\n[ download <ip>:<port> <hash> ]\n[ all ]\n[ exit ]");

                while (command == null || command.equalsIgnoreCase("")) {
                    command = FileTerminal.inputFile(terminalPath);
                }
                String[] exec = command.split(" ");
                switch (exec[0]) {
                    case "find":
                        System.out.println("Response -> " + mySuperNode.findHandler(exec[1]));
                        break;
                    case "download":
                        downloadFile(exec[1], Integer.parseInt(exec[2]));
                        break;
                    case "all":
                        System.out.println("Response -> " + mySuperNode.allHandler());
                        break;
                    case "exit":
                        System.out.println("EXIT");
                        return;
                    default:
                        System.out.println(ConsoleColors.RED + "Comando inválido: " + command + ConsoleColors.RESET);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }

    private void downloadFile(String requestIpPort, int hashCode) {
        new Thread(() -> {
            try {
                // initialize socket do send a request with hashcode of the file we will
                // download
                // socket port will be +1 because the SocketListener already have located this
                // port
                DatagramSocket socketToRequest = new DatagramSocket(port + 1);
                System.out.println("Abri um socket datagram com a port -> " + (port + 1) +
                        " para fazer o request do arquivo");

                InetAddress ipToRequest = InetAddress.getByName(requestIpPort.split(":")[0]);
                int portToRequest = Integer.parseInt(requestIpPort.split(":")[1]);
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
                Socket socket = new Socket(ipToRequest, portToRequest + 2);
                System.out.println("abrindo o socket para estabelecer conexão com o socket usando a porta: "
                        + (portToRequest + 1));
                FileOutputStream fos = new FileOutputStream(dirPath + "/_" + downloadNumber + "_.txt");
                downloadNumber++;
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
                bos.close();
                System.out.println("File saved successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
