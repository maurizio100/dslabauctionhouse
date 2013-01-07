package auction.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import auction.commands.ICommandReceiver;
import auction.communication.interfaces.IExitObserver;
import auction.communication.interfaces.IMessageReceiver;
import auction.server.interfaces.IClientOperator;
import auction.server.interfaces.IClientThread;

public class ClientManager implements IClientOperator, IExitObserver{

	private ICommandReceiver commandMessenger = null;
	private ExecutorService executorService = null;	
	private IMessageReceiver localMessenger = null;

	private HashMap<String, IClientThread> loggedInClients = null;
	private HashMap<String, ArrayList<String>> queuedNotifications = null;
	private ArrayList<IClientThread> allClients = null;

	private ServerUDPPort serverUDPPort = null;

	public ClientManager(ICommandReceiver commandController, IMessageReceiver rcv) {
		commandMessenger = commandController;
		executorService = Executors.newCachedThreadPool();
		localMessenger = rcv;
		serverUDPPort = new ServerUDPPort();

		loggedInClients = new HashMap<String,IClientThread>();
		queuedNotifications = new HashMap<String,ArrayList<String>>();
		allClients = new ArrayList<IClientThread>();
	}

	public void addNewClient(Socket newClient) {
		try{
			Client c = new Client(newClient, commandMessenger, localMessenger);
			executorService.execute(c);
			allClients.add(c);
		}catch(IOException e){
			localMessenger.receiveMessage("Client couldnt be initialized.");
		}
	}

	private void deleteNotifications(IClientThread thread){
		ArrayList<String> notifications = queuedNotifications.get(thread.getClientName());
		notifications.removeAll(notifications);
	}

	private void sendToLocalMessenger( String message ){
		localMessenger.receiveMessage(message);
	}

	@Override
	public void loginClient(String clientName, int udpPort, IClientThread thread) {
		clientName = clientName.replace("\n", "");
		clientName = clientName.toLowerCase();

		thread.setLogin(clientName, udpPort);
		if( !loggedInClients.containsKey(clientName) ){
			queuedNotifications.put(clientName, new ArrayList<String>());
			loggedInClients.put(clientName, thread);
		}else{
			loggedInClients.put(clientName, thread);
			String notifications = this.getNotifications(thread);
			if(notifications != null){
				deleteNotifications(thread);
				this.sendNotification(notifications, clientName);
			}
		}
	}

	@Override
	public void logoffClient(IClientThread thread) {
		thread.setLogout();
	}

	@Override
	public void sendNotification(String notification, String receiver) {
		if( loggedInClients.containsKey(receiver) ){
			IClientThread thread = loggedInClients.get(receiver);


			try {
				if( thread.isLoggedIn() && receiver.equals(thread.getClientName())){
					int udpPort= thread.getUdpPort();
					InetAddress host = thread.getHost();
					serverUDPPort.sendNotification(udpPort, host, notification);

				}else {
					addNotification(receiver, notification);
				}
			} catch (IOException e) {
				this.sendToLocalMessenger("Couldnt send the UDP package to: " + thread.getHost());
			}
		}
	}

	private void addNotification(String receiver, String notification){
		
		queuedNotifications.get(receiver).add(notification);
	}

	@Override
	public String getNotifications(IClientThread thread) {
		String clientName = thread.getClientName();
		ArrayList<String> notifications = queuedNotifications.get(clientName);
		String notificationBulg = "";

		if(notifications.isEmpty()) return null;

		for(String s : notifications){
			notificationBulg += s + "\n";
		}

		return notificationBulg;
	}

	@Override
	public void shutDownClient(IClientThread thread) {
		thread.exit();
		allClients.remove(thread);
	}

	@Override
	public void exit() {
		this.sendToLocalMessenger("Shutting down Client Manager!");
//		for( IClientThread t : allClients){
//			t.exit();
//		}
		executorService.shutdown();
	}

	@Override
	public void sendGroupBidNotification(GroupBid gb) {
		IClientThread groupBidder = gb.getGroupBidder();
		
		
	}

	@Override
	public void sendFeedback(IClientThread c, String feedback) {
		c.receiveFeedback(feedback);
	}

	@Override
	public Collection<IClientThread> getLoggedInClients() {
		return loggedInClients.values();
	}
}
