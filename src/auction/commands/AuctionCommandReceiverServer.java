package auction.commands;

public interface AuctionCommandReceiverServer extends AuctionCommandReceiverClient{

	public void overbid();
	public void endAuction();
	
}
