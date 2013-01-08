package auction.client.interfaces;

import auction.global.interfaces.IAuctionCommandReceiver;

public interface ICommandReceiverClient
extends IAuctionCommandReceiver {

	public void rejectLogin();
	public void ok();
	public void rejectGroupBid();
	public void notifyConfirmed();
	public void overbid();
	public void endAuction();
	
	
}
