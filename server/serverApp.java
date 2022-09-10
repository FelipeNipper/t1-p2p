package server;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class serverApp {
    public static void main (String[] args) throws IOException {
        try {
            System.setProperty("java.rmi.server.hostname", args[0]);
			LocateRegistry.createRegistry(9000);
			System.out.println("RMI registry ready.");
        } catch (Exception e) {
        }
        try {
			String server = "rmi://" + args[0] + ":9000/Server";
			Naming.rebind(server, new Server());
			System.out.println("p2p Server is ready.");
		} catch (Exception e) {
			System.out.println("p2p Server failed: " + e);
		}
	}
}
