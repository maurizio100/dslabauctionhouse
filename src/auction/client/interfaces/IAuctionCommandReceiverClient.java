package auction.client.interfaces;

public interface IAuctionCommandReceiverClient {

	public void createAuction();
	public void bidForAuction();
	public void list();
	public void exit();
	public void ok();

	public void confirmGroupBid();
	public void rejectGroupBid();
	public void notifyConfirmed();
}
