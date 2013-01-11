package auction.server.interfaces;

import auction.global.interfaces.IAuctionCommandReceiver;

public interface ICommandReceiverServer
extends IAuctionCommandReceiver {

	public void close();
	public void reconnect();
	public void signedBid();
	
}
