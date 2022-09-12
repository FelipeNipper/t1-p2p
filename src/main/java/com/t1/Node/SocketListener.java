package com.t1.Node;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class SocketListener extends Thread {
    private HashMap<Integer, String> resources;
    private int port;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] resource;
    private String dirPath;

    public SocketListener(int port, String dir, HashMap<Integer, String> resources) {
        this.dirPath = dir;
        this.resources = resources;
        this.port = port;
        this.resource = new byte[1024];
        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
        }

    }

    @Override
    public void run() {
        System.out.println("*---------- aguardando conexao com outros peers ----------*");
        while (true) {
            try {
                // espera receber o packet
                packet = new DatagramPacket(resource, resource.length);
                socket.setSoTimeout(500);
                socket.receive(packet);
                System.out.print("*--------------------*\nRecebi uma solicitacao de download");

                // processa o que foi recebido
                String content = new String(packet.getData(), 0, packet.getLength());
                InetAddress addr = packet.getAddress();
                // getPort()+1 because the socket will send is socketToRequest
                // and the socket will receive have port + 2
                int responsePort = packet.getPort() + 1;
                // content = hash
                int fileHash = Integer.parseInt(content);
                if (resources.containsKey(fileHash)) {
                    // @toDo metodo tcp para realizar transferencia de arquivos
                    // pode-se fazer o metodo virar uma thread para não travar o listener
                    sendContent(addr, responsePort, fileHash);
                } else {
                    System.out.println("erro, arquivo com hash: " + fileHash + " nao encontrado");
                    System.out.println("encerrando conexao");
                    // @toDo metodo para finalizar a conexao com o peer
                    System.out.println("*---------- conexao encerrada ----------*");
                }

            } catch (Exception e) {
            }
        }
    }

    public boolean sendContent(InetAddress addr, int port, int fileHash) {
        try {
            // Initialize Sockets
            ServerSocket ssock = new ServerSocket(this.port + 1);
            System.out.println("abri o server socket com a porta" + (this.port + 1) +
                    "\n aguardando a abertura de conexão pelo lado do client");
            Socket socket = ssock.accept();
            System.out.println("conexão aceita");
            // The InetAddress specification
            InetAddress IA = InetAddress.getByName("localhost");

            // Specify the file
            File file = new File(this.dirPath + "/" + this.resources.get(fileHash));
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            // Get socket's output stream
            OutputStream os = socket.getOutputStream();
            // Read File Contents into contents array
            byte[] contents;
            long fileLength = file.length();
            long current = 0;
            long start = System.nanoTime();
            while (current != fileLength) {
                int size = 10000;
                if (fileLength - current >= size)
                    current += size;
                else {
                    size = (int) (fileLength - current);
                    current = fileLength;
                }
                contents = new byte[size];
                bis.read(contents, 0, size);
                os.write(contents);
                System.out.print("Sending file ... " + (current * 100) / fileLength + "% complete!");
            }
            os.flush();
            // File transfer done. Close the socket connection!
            socket.close();
            ssock.close();
            System.out.println("File sent succesfully!");
        } catch (UnknownHostException e) {
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    private boolean sendFile(InetAddress addr, int port, int fileHash) {
        // logica do tcp para a transferencia
        return true;
    }
}
