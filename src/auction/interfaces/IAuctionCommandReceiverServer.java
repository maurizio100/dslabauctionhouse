package auction.interfaces;

public interface IAuctionCommandReceiverServer extends IAuctionCommandReceiverClient{

	public void overbid();
	public void endAuction();
	
}
