package auction.server.interfaces;

import java.net.InetAddress;

import auction.communication.interfaces.IFeedbackObserver;


public interface IClientThread extends IFeedbackObserver{

	public void exit();
	public String getClientName();
	public int getUdpPort();
	public InetAddress getHost();
	
	public void setLogin(String name, int preferredUDPPort);
	public void setLogout();
	
	public boolean isLoggedIn();
}
