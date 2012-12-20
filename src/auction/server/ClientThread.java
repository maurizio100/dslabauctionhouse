package auction.server;

import java.net.InetAddress;

public interface ClientThread extends FeedbackObserver{

	public void exit();
	public String getClientName();
	public int getUdpPort();
	public InetAddress getHost();
	
	public void setLogin(String name, int preferredUDPPort);
	public void setLogout();
	
	public boolean isLoggedIn();
}
