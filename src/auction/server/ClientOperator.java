package auction.server;

public interface ClientOperator {

	public void loginClient(String clientName, int udpPort, ClientThread thread);
	public void logoffClient(ClientThread thread);
	public void sendNotification(String notification, String receiver);
	public String getNotifications(ClientThread thread);
	public void shutDownClient( ClientThread thread );
	public void performConfirmNotification(ClientThread groupBidder);
	public void sendGroupBidNotification(GroupBid gb);
	
}
