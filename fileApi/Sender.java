package fileApi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import fileApi.Socket.NetPackage;
import fileApi.Socket.Serializer;

public class Sender {
	// REVER
	private int receiverPort; // = 9876;
	private int senderPort; // = 9878;
	private DatagramSocket senderSocket;
	private DatagramPacket receivedPacket;
	private InetAddress IPAddress;
	// ---------------

	// Tempo de timeout (segundos)
	private int timeoutSeconds = 20;
	// Estrutura do arquivo
	private FileStruct fileStruct;
	// Manipulador de arquivos
	private FileHandler fs;
	// Quantidade de pedaços para serem enviados antes de confirmação
	private int batchSize = 5;
	// N pedaços do arquivo
	private int fileChunks = 0;
	// Pedaço atual sendo enviado
	private int seq = 0;
	// Serializador de arquivos
	private Serializer serializer = new Serializer();

	public Sender(String receiverHost, int receiverPort, int senderPort) {
		System.out.println("Realizando configuracoes");

		this.receiverPort = receiverPort;
		this.senderPort = senderPort;

		// Configura socket para o sender
		try {
			senderSocket = new DatagramSocket(this.senderPort);
			senderSocket.setSoTimeout(timeoutSeconds * 1000);
		} catch (SocketException e) {
			System.out.println("Erro! porta ja em uso");
		}

		// Declara o pacote a ser recebido
		byte[] receiveData = new byte[1024];
		receivedPacket = new DatagramPacket(receiveData, receiveData.length);

		// Seta ip do receiver
		try {
			IPAddress = InetAddress.getByName(receiverHost);
			System.out.println("Conectado ao receiver com sucesso");
		} catch (UnknownHostException e) {
			System.out.println("erro durante obtencao do ip do receiver");
			e.printStackTrace();
		}
	}

	public void sendFile(FileStruct _fileStruct) {
		fileStruct = _fileStruct;
		System.out.println("Iniciando processo de envio de arquivo " + fileStruct.getName() + "...");
		System.out.print("Quebrando o arquivo");

		// Quebra o arquivo em pedaços
		breakFile();

		// Fluxo de envio de mensagens e confirmacoes de entrega, pedaco a pedaco
		sendFilePieces();
		System.err.println("enviou tudo");

		// Finalizando conexao
		sendFinalMessage();
		System.out.println("enviou mensagem final");
		senderSocket.close();
	}

	// Quebra o arquivo a ser enviado em pedaços para transferencia UDP
	private void breakFile() {
		fs = new FileHandler(fileStruct);
		try {
			fileChunks = fs.split() - 1;
			System.out.println("Arquivo quebrado em " + fileChunks + " com sucesso");
		} catch (IOException e) {
			System.out.println("Ocorreu um erro ao manipular o arquivo");
		}
	}

	// Envio dos pedacos do arquivo
	private void sendFilePieces() {
		while (seq < fileChunks) {
			System.out.println("/////////////////////");
			System.out.println("Enviando: ");
			// Define o array de mensagens a enviar
			ArrayList<NetPackage> messages = getMessage();
			for (NetPackage netPackage : messages) {
				sendMessage(netPackage);
			}
			// Recebe confirmaco
			boolean status = receiveACK();
			if (!status) {
				break;
			}
		}
	}

	// Logica de obtencao dos pedacos do arquivo a serem enviados
	private ArrayList<NetPackage> getMessage() {
		ArrayList<NetPackage> messages = new ArrayList<NetPackage>();
		for (int i = 0; i < batchSize; i++) {
			seq++;
			if (seq > fileChunks) {
				break;
			}
			NetPackage np = new NetPackage();
			np.createObj(seq, fs.getFile(seq));
			messages.add(np);
		}
		return messages;
	}

	// Envio de mensagens
	private void sendMessage(NetPackage netPackage) {
		try {
			byte[] sendData = serializer.serialize(netPackage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, this.IPAddress, this.receiverPort);
			senderSocket.send(sendPacket);

		} catch (IOException e1) {
			System.out.println("Ocorreu um erro ao enviar a mensagem");
			e1.printStackTrace();
		}
	}

	// Confirmacao do cliente de que recebeu corretamente os arquivos
	private boolean receiveACK() {
		if (seq >= fileChunks) {
			return true;
		}
		try {
			senderSocket.receive(receivedPacket);
			String receivedACK = new String(receivedPacket.getData());
			receivedACK = receivedACK.replaceAll("[^\\d.]", "");
			int testACK = Integer.parseInt(receivedACK);
			System.out.println("ACK recebido: " + testACK);
			return true;
		} catch (Exception e) {
			System.out.println("Timeout!!!!!!!!!!! Encerrando conexao");
			return false;
		}
	}

	// Envio da mensagem de condico de parada
	private void sendFinalMessage() {
		byte[] sendData = new byte[1024];
		String message = "DONE";
		sendData = message.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, this.IPAddress, this.receiverPort);
		try {
			senderSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("erro durante envio do pacote ao receiver");
			e.printStackTrace();
		}
	}
}
