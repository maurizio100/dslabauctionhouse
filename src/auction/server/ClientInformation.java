package auction.server;

import java.util.ArrayList;

public class ClientInformation {

	private boolean loggedIn = false;
	private ArrayList<String> notificationQueue = null;
	private String host;	
	private int udpPort = -1;
	
	public ClientInformation(String host, int udpPort){
		this.host = host;
		this.udpPort = udpPort;
		notificationQueue = new ArrayList<String>();
		this.toggleLoginstate();
		
	}

	public void toggleLoginstate(){
		loggedIn = !loggedIn;
	}
	
	public void queueNotification(String notification){
		notificationQueue.add(notification);
	}
	
	public String[] getNotifications(){
		String[] notifications = new String[notificationQueue.size()];
		int i = 0;
		
		for( String notification : notificationQueue){
			notifications[i] = notification;
			i++;
		}
		
		notificationQueue.removeAll(notificationQueue);
		return notifications;
	}
	
	public boolean hasNotification(){
		return notificationQueue.size() > 0;
	}
	
}
