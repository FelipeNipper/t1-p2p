package rmi;

import fileApi.FileStruct;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class Resource extends UnicastRemoteObject implements ResourceInterface {

	private List<Peer> peers;

	public Resource(List<Peer> peers) throws RemoteException {
		this.peers = peers;
	}

	public String registerPeer(String host, int port, List<FileStruct> files) throws RemoteException {
		Peer peer = new Peer(host, port, files);
		this.peers.add(peer);

		return "Peer Added!";
	}

	public List<Peer> listPeers() throws RemoteException {
		return this.peers;
	}

	public String keepAlive(String host, int port) throws RemoteException {
		Peer peer = findPeer(host, port);
		if (peer != null) {
			peer.setLastPing(System.nanoTime());
			return "Peer continua vivo!";
		} else {
			return "Peer não encontrado!";
		}
	}

	public Peer findPeerByFileName(String fileName) throws RemoteException {
		for (Peer p : this.peers) {
			for (FileStruct fs : p.getFiles()) {
				if(fs.getName().toUpperCase().contains(fileName.toUpperCase())) {
					return p;
				}
			}
		}
		return null;
	}

	public String askForFile(String receiverHost, int receiverPort, String senderHost, int senderPort, String file) throws RemoteException {
		FileRequest fileRequest = new FileRequest(receiverHost, receiverPort, file);

		Peer sender = findPeer(senderHost, senderPort);
		if (sender != null) {
			sender.setFileRequest(fileRequest);
			return "Requisito de arquivo criado";
		} else {
			return "Peer não encontrado";
		}
	}

	public FileRequest checkForFileRequests(String host, int port) throws RemoteException {
		Peer peer = findPeer(host, port);
		return peer.getFileRequest();
	}

	private Peer findPeer(String host, int port) {
		for (Peer p : this.peers) {
			if(host.equalsIgnoreCase(p.getHost()) && port == p.getPort()) {
				return p;
			}
		}
		return null;
	}

}

