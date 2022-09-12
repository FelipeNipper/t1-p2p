package com.t1.SuperNode;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface SuperNodeInterface extends Remote {
    public String register(int port, HashMap<Integer, String> resources) throws RemoteException;

    public String commandHandler(String command) throws RemoteException;

    public HashMap<String, String> findResource(String resourceName) throws RemoteException;

    public void KeepAlive(String id) throws RemoteException;

    public void KeepAliveController() throws RemoteException;

    public void disconnect(String id) throws RemoteException;

    public void sendResourceForNextNode(String resourceName, String addr, HashMap<String, String> response)
            throws RemoteException;

    public void sendToken() throws RemoteException;
}