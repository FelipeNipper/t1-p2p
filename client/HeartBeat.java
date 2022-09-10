package client;
import java.io.*;
import java.rmi.RemoteException;

import server.ServerInterface;

public class HeartBeat extends Thread {
    private ServerInterface server;
    private String id;

	public HeartBeat(ServerInterface server, String id) throws IOException {
		this.server = server;
        this.id = id;
	}

	public void run() {
		while (true) {
			try {
                server.heartBeat(id);
                Thread.sleep(10000);
            } catch (RemoteException e1) {
                System.out.println("Error on sending heartBeat");
            }
			catch(InterruptedException e) {
                System.out.println("Error on thread sleep Heart Beat");
            }
		}
	}
}
