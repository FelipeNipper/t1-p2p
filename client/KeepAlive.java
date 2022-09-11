package client;

import java.io.*;
import java.rmi.RemoteException;

import server.SuperNodeInterface;

public class KeepAlive extends Thread {
    private SuperNodeInterface server;
    private String id;

    public KeepAlive(SuperNodeInterface server, String id) throws IOException {
        this.server = server;
        this.id = id;
    }

    public void run() {
        while (true) {
            try {
                server.KeepAlive(id);
                Thread.sleep(10000);
            } catch (RemoteException e1) {
                System.out.println("Error on sending KeepAlive");
            } catch (InterruptedException e) {
                System.out.println("Error on thread sleep Heart Beat");
            }
        }
    }
}
