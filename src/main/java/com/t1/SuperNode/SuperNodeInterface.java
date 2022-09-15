package com.t1.SuperNode;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public interface SuperNodeInterface extends Remote {

    public void connectNext() throws RemoteException;

    public String register(String ip, int port, ConcurrentHashMap<Integer, String> resources) throws RemoteException;

    public String findHandler(String fileName) throws RemoteException;

    public ConcurrentHashMap<String, String> findResource(String resourceName) throws RemoteException;

    public void KeepAlive(String id) throws RemoteException;

    public void KeepAliveController() throws RemoteException;

    public void disconnect(String id) throws RemoteException;

    public void sendResourceForNextNode(String resourceName, String addr, ConcurrentHashMap<String, String> response)
            throws RemoteException;

    public void sendToken() throws RemoteException;
}