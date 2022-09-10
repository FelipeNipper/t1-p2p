package superNode;

import rmi.Peer;
import rmi.Resource;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SuperNode extends Thread {

	protected String host;
	protected int port;

	private List<Peer> peers;

	public SuperNode(String[] args) throws IOException {
		host = args[0];
		port = Integer.parseInt(args[1]);
		this.peers = new ArrayList<>();
	}

	public void run() {
		// Inicializando Servidor RMI
		try {
			System.setProperty("java.rmi.server.hostname", host);
			LocateRegistry.createRegistry(port);
			System.out.println("RMI registry ready.");
		} catch (RemoteException e) {
			System.out.println("RMI registry already running.");
		}

		// Registrando Rotas
		try {
			String server = "rmi://" + host + ":" + port + "/Resource";
			Naming.rebind(server, new Resource(peers));
			System.out.println("p2p Server is ready.");
		} catch (Exception e) {
			System.out.println("p2p Server failed: " + e);
		}

		long lastRun = System.nanoTime();

		// Looping de controle de recursos
		while (true) {
			if (this.cycleCheck(lastRun)) {
				// System.out.println("passou 10 segundos!");
				System.out.println("Peers: " + this.listAllPeers());
				lastRun = System.nanoTime();
				killDeadPeers();
			}

		}
	}

	private boolean cycleCheck(long lastRun) {
		long now = System.nanoTime();
		long elapsedTime = now - lastRun;
		long convertedTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);

		return convertedTime > 10;
	}

	private void killDeadPeers() {
		if (this.peers != null && !this.peers.isEmpty()) {
			List<Peer> peersToRemove = new ArrayList<>();
			for (Peer p : this.peers) {
				if (p.isPeerDead()) {
					peersToRemove.add(p);
					System.out.println("Peer killed '" + p.toString());
				}
			}
			this.peers.removeAll(peersToRemove);
		}
	}

	private String listAllPeers() {
		String response = "";
		for (Peer p : this.peers) {
			response += p.toString() + "; ";
		}
		return response;
	}
}
