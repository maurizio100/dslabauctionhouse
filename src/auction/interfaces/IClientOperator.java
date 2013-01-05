package auction.interfaces;

import java.util.ArrayList;

import auction.server.Client;
import auction.server.GroupBid;

public interface IClientOperator {

	public void loginClient(String clientName, int udpPort, IClientThread thread);
	public void logoffClient(IClientThread thread);
	public void sendNotification(String notification, String receiver);
	public String getNotifications(IClientThread thread);
	public void shutDownClient( IClientThread thread );
	public void performConfirmNotification(ArrayList<Client> confirmers);
	public void sendGroupBidNotification(GroupBid gb);
	public void sendFeedback(Client c, String feedback);
	
}
