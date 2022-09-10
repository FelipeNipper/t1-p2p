package rmi;

import fileApi.FileStruct;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ResourceInterface extends Remote {

	String registerPeer(String host, int port, List<FileStruct> files) throws RemoteException;

	String keepAlive(String host, int port) throws RemoteException;

	List<Peer> listPeers() throws RemoteException;

	Peer findPeerByFileName(String fileName) throws RemoteException;

	String askForFile(String receiverHost, int receiverPort, String senderHost, int senderPort, String file) throws RemoteException;

	FileRequest checkForFileRequests(String host, int port) throws RemoteException;
}