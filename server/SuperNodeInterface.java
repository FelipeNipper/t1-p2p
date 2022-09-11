package server;

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
}