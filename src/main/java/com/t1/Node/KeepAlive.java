package com.t1.Node;

import java.io.*;
import java.rmi.RemoteException;

import com.t1.SuperNode.SuperNodeInterface;

public class KeepAlive extends Thread {
    private SuperNodeInterface superNode;
    private String id;

    public KeepAlive(SuperNodeInterface superNode, String id) throws IOException {
        this.superNode = superNode;
        this.id = id;
    }

    public void run() {
        while (true) {
            try {
                superNode.KeepAlive(id);
                Thread.sleep(10000);
            } catch (RemoteException e1) {
                System.out.println("Error on sending KeepAlive");
            } catch (InterruptedException e) {
                System.out.println("Error on thread sleep Heart Beat");
            }
        }
    }
}
