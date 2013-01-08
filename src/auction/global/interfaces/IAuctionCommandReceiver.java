package auction.global.interfaces;

public interface IAuctionCommandReceiver {

	public void createAuction();
	public void bidForAuction();
	public void confirmGroupBid();
	
	public void login();
	public void logout();
	public void list();
	public void exit();
	

}
