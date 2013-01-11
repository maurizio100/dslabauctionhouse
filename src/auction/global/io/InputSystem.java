package auction.global.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import auction.global.config.IOUnitConfig;

public class InputSystem extends Thread{

	private IOUnit ioUnit;
	private BufferedReader in;

	public InputSystem(IOUnit ioUnit) {
		this.ioUnit = ioUnit;
		in = new BufferedReader(new InputStreamReader( IOUnitConfig.INPUTSTREAM ));
		this.start(); 
	}

	public void run(){
		String inputString = null;
		try{
			while( true ){

				if( (inputString = in.readLine()) != null ){
					sendMessage(inputString);
				}
				if( inputString == null ){ throw new IOException(); }
			}			
		} catch( IOException e ) {	}
	}

	private void sendMessage(String message){
		ioUnit.sendMessage(message);	
	}

	public void closeInput() {
		try{
			in.close(); 
			this.sendMessage(IOUnitConfig.INPUTUNITSHUTDOWNINFO); 
		}catch( IOException e ){}
	}

	public String getInput() {
		try {
			return in.readLine();
		} catch (IOException e) { this.sendMessage(IOUnitConfig.INPUTERROR); }
		
		return null;
	}	
}
