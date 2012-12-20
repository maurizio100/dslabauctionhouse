package auction.commands;

public interface AuctionCommandReceiverClient {

	public void createAuction();
	public void bidForAuction();
	public void list();
	public void exit();

	
}
