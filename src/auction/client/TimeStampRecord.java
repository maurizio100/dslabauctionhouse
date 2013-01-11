package auction.client;

import java.util.ArrayList;

public class TimeStampRecord {

	
	private ArrayList<TimeStamp> timestamps = null;
	
	public TimeStampRecord(){
		timestamps = new ArrayList<TimeStamp>();
	}
	
	public boolean addTimeStamp(long timestamp, String signature, String clientName){
		if( timestamps.size() == 2 ){
			return false;
		}else{
			timestamps.add(new TimeStamp(timestamp, signature, clientName));
			return true;
		}
	}
	
	public ArrayList<TimeStamp> getTimestampList(){
		return timestamps;
	}
}
