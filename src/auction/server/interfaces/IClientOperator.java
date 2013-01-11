package auction.server.interfaces;

import java.util.Collection;

public interface IClientOperator {

	public void loginClient(String clientName, int udpPort, IClientThread thread);
	public void logoffClient(IClientThread thread);
	public void sendNotification(String notification, String receiver);
	public String getNotifications(IClientThread thread);
	public void shutDownClient( IClientThread thread );
	public void sendFeedback(IClientThread c, String feedback);
	public Collection<IClientThread> getLoggedInClients();

	//public void sendGroupBidNotification(GroupBid gb);
}
