package server;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Server extends UnicastRemoteObject implements ServerInterface{

	// mapa de ip+port que devolve um mapa de recursos (hash, nome)
	protected HashMap<String, HashMap<Integer, String>> peersResources;
	// mapa de ip+port que devolve quanto tempo o peer esta sem mandar heartbeat
	protected HashMap<String, Integer> peersTimeout;
	
	public Server() throws RemoteException{
		peersResources = new HashMap<>();
		peersTimeout = new HashMap<>();
		new Thread(() -> {
			while (true) {
				heartBeatController();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public String commandHandler(String command) throws RemoteException{
		// find 'nome do arquivo'
		String[] vars = command.split(" ");
		String name = "";
		for (int i = 1; i < vars.length; i++) {
			name += vars[i];
		}
		switch (vars[0]) {
			case "find":
				HashMap<String,String> founded = findResource(name);
				return founded.toString();
			default:
				return "invalid command";
		}
	}

	public HashMap<String, String> findResource(String resourceName){
		HashMap<String, String> resourcePeers = new HashMap<>();
		peersResources.forEach((addr, resources) -> {
			resources.forEach((hash,name) -> {
				if (name.contains(resourceName)){
					if(resourcePeers.containsKey(addr)){
						//Achou local
						resourcePeers.put(addr, resourcePeers.get(addr) + ", (" + name + ", " + hash + ")");
					}else{
						//TODO: Buscar no anel
						// sendRequestForFileInRing(resourceName)	
						// else retorna erro
						resourcePeers.put(addr, "(" + name + ", " + hash + ")");
					}
				}
			});
		});
		return resourcePeers;
	}

	// Outra thread
	// Escuta o token ring e :
	// - Receber um request de file -> findResource

	public String register(int port, HashMap<Integer, String> resources) throws RemoteException{
		String ip = "";
		String user_id = "";
		try {
			ip = RemoteServer.getClientHost();
			user_id = ip + ":" + port;
			peersResources.put(user_id, resources);
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
		}
		return user_id;
	}

	public void heartBeatController(){
		HashMap<String, Integer> peersTimeoutAux = new HashMap<>();

		peersTimeout.forEach((key, value) -> {
			value--;
			if (value == 0){
				System.out.println(key + ".......... disconnected");
				disconnect(key);
			}else {
				peersTimeoutAux.put(key, value);
			}
		});
		peersTimeout = peersTimeoutAux;
	}

	public void disconnect(String id){
		peersResources.remove(id);
	}
	public void heartBeat(String id) throws RemoteException{
		peersTimeout.put(id, 3);
		System.out.println(id + ":.......... still here");
	}
}
