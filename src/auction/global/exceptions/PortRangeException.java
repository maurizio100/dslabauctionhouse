package auction.global.exceptions;

public class PortRangeException extends Exception {

	private String errMessage;
	
	public PortRangeException( String errMessage ){
		this.errMessage = errMessage;
	}
	
	@Override
	public String getMessage(){
		return errMessage;
	}
	
}

