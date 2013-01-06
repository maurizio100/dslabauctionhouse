package auction.interfaces;

import java.net.InetAddress;


public interface IClientThread extends IFeedbackObserver{

	public void exit();
	public String getClientName();
	public int getUdpPort();
	public InetAddress getHost();
	
	public void setLogin(String name, int preferredUDPPort);
	public void setLogout();
	
	public boolean isLoggedIn();
}
