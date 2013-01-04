package auction.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import auction.communication.CommandReceiver;
import auction.communication.ExitObserver;
import auction.communication.MessageReceiver;

public class ClientManager implements ClientOperator, ExitObserver{

	private CommandReceiver commandMessenger = null;
	private ExecutorService executorService = null;	
	private MessageReceiver localMessenger = null;

	private HashMap<String, ClientThread> loggedInClients = null;
	private HashMap<String, ArrayList<String>> queuedNotifications = null;
	private ArrayList<ClientThread> allClients = null;

	private ServerUDPPort serverUDPPort = null;

	public ClientManager(CommandReceiver commandController, MessageReceiver rcv) {
		commandMessenger = commandController;
		executorService = Executors.newCachedThreadPool();
		localMessenger = rcv;
		serverUDPPort = new ServerUDPPort();

		loggedInClients = new HashMap<String,ClientThread>();
		queuedNotifications = new HashMap<String,ArrayList<String>>();
		allClients = new ArrayList<ClientThread>();
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

	private void deleteNotifications(ClientThread thread){
		ArrayList<String> notifications = queuedNotifications.get(thread.getClientName());
		notifications.removeAll(notifications);
	}

	private void sendToLocalMessenger( String message ){
		localMessenger.receiveMessage(message);
	}

	@Override
	public void loginClient(String clientName, int udpPort, ClientThread thread) {
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
	public void logoffClient(ClientThread thread) {
		thread.setLogout();
	}

	@Override
	public void sendNotification(String notification, String receiver) {
		if( loggedInClients.containsKey(receiver) ){
			ClientThread thread = loggedInClients.get(receiver);


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
	public String getNotifications(ClientThread thread) {
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
	public void shutDownClient(ClientThread thread) {
		thread.exit();
		allClients.remove(thread);
	}

	@Override
	public void exit() {
		this.sendToLocalMessenger("Shutting down Client Manager!");
		for( ClientThread t : allClients){
			t.exit();
		}
		executorService.shutdown();
	}

	@Override
	public void performConfirmNotification(ClientThread groupBidder) {
		//TODO implement performConfirmNotification
	}

	@Override
	public void sendGroupBidNotification(GroupBid gb) {
		ClientThread groupBidder = gb.getGroupBidder();
		
		for( ClientThread ct : loggedInClients.values() ){
			if( ct != groupBidder ){
				ct.receiveFeedback(groupBidder.getClientName() + " has started the following group bid and needs two confirmations\n" + gb);
			}
		}
		
	}
}
