package auction.interfaces;

import auction.client.interfaces.IAuctionCommandReceiverClient;

public interface IAuctionCommandReceiverServer extends IAuctionCommandReceiverClient{

	public void overbid();
	public void endAuction();
	
}
