import client.p2pPeerClient;
import server.p2pSuperNode;

import java.io.IOException;

public class p2pMain {

	public static void main(String[] args) throws IOException {
		if (args.length == 2) {
			System.out.println("Inicializando Server - host:" + args[0]
					+ " port:" + args[1]);
			new SuperNode(args).start();
		} else if (args.length == 5) {
			System.out.println("Inicializando Peer - host:" + args[0]
					+ " port:" + args[1]
					+ " server-host:" + args[2]
					+ " server-port:" + args[3]
					+ " file:" + args[4]);
			new Node(args).start();
		} else {
			System.out.println("Argumentos inválidos!");
			System.out.println("Uso para Server: java p2pStarter <host> <port>");
			System.out.println("Uso para Peer: java p2pStarter <host> <port> <server_host> <server_port> <file_name>");
			return;
		}
	}
}
