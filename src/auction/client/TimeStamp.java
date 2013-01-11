package auction.client;

public class TimeStamp {

	private long timeStamp = 0;
	private String signature = null;
	private String clientName = null;
	
	public TimeStamp( long timeStamp, String signature, String clientName ){
		this.timeStamp = timeStamp;
		this.signature = signature;
		this.clientName = clientName;
	}
	
	public String toString(){
		return clientName + ":" + timeStamp + ":" + signature;
	}
	
}
