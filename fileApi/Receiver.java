package fileApi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import fileApi.Socket.NetPackage;
import fileApi.Socket.Serializer;

public class Receiver {
    // REVER
    private int receiverPort;// = 9876;
    private int senderPort;// = 9878;
    private DatagramSocket receiverSocket;
    private InetAddress IPAddress;
    // ---------------

    // Manipulador de arquivos
    private FileHandler fs;
    // Serializador de arquivos
    private Serializer serializer = new Serializer();
    // Quantidade de pedaços para serem enviados antes de confirmação
    private int batchSize = 5;
    // Tempo de timeout (segundos)
    private int timeoutSeconds = 40;

    public Receiver(String senderHost, int senderPort, int receiverPort, String fileName) {
        System.out.println("Realizando configuracoes");

        this.senderPort = senderPort;
        this.receiverPort = receiverPort;

        // Configura manipulador de arquivos
        fs = new FileHandler(new FileStruct(null, fileName));

        // Configura socket para o receiver
        try {
            receiverSocket = new DatagramSocket(this.receiverPort);
            receiverSocket.setSoTimeout(timeoutSeconds * 1000);
        } catch (SocketException e1) {
            System.out.println("Ocorreu um erro ao setar o datagram");
        }

        // Seta ip do sender
        try {
            IPAddress = InetAddress.getByName(senderHost);
        } catch (Exception e) {
            System.out.println("Ocorreu um erro ao obter o ip de destino");
        }
    }

    // Fluxo de recebimento de mensagens e confirmacoes de entrega
    public void receiveFile() {
        System.out.println("Recebendo: ");

        boolean stop = false;
        while (!stop) {
            System.out.println("/////////////////////");
            // Recebe N mensagens, de acordo com slow start
            for (int i = 0; i < batchSize; i++) {
                // Recebimento de mensagem
                boolean status = receiveMessage();
                if (!status) {
                    stop = true;
                    break;
                }
            }
            // Envia uma confirmacao
            sendMessage();
        }
        receiverSocket.close();

        // Monta o arquivo
        System.out.println("Montando arquivo");
        try {
            fs.mergeFiles();
            System.out.println("Arquivo final pronto");
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao mergear os arquivos");
            e.printStackTrace();
        }
    }

    // Recebimento de mensagens
    private boolean receiveMessage() {
        try {
            // Recebe uma mensagem
            byte[] buffer = new byte[1024 * 4];
            receiverSocket.receive(new DatagramPacket(buffer, buffer.length));
            String message = new String(buffer);

            // Verifica se e uma condicao de parada
            if (message.contains("DONE")) {
                return false;
            }

            // Se nao, e um novo arquivo
            NetPackage np = (NetPackage) serializer.deserialize(buffer);
            fs.saveFile(np.getSeq(), np.getFileArray());
            return true;
        } catch (Exception e) {
            System.out.println("--------------");
            System.out.println("Ocorreu um erro ao receber a mensagem");
            return false;
        }
    }

    // Envio de mensagem
    // Confirma que recebeu os arquivos
    private void sendMessage() {
        byte[] sendData = new byte[1024];
        System.out.println("enviando ACK:" + (100 + batchSize));
        String newACK = Integer.toString(100 + batchSize);
        sendData = newACK.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, this.IPAddress, this.senderPort);
        try {
            receiverSocket.send(sendPacket);
        } catch (IOException e) {
            System.out.println("erro durante envio do pacote ao sender");
            e.printStackTrace();
        }
    }
}
