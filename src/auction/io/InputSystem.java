package auction.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;

import auction.communication.MessageReceiver;

public class InputSystem extends Thread{

	private MessageReceiver ioUnit;
	private BufferedReader in;

	public InputSystem(MessageReceiver rcv) {
		ioUnit = rcv;
		in = new BufferedReader(new InputStreamReader(System.in));
		this.start(); 
	}

	public void run(){
		String inputString = null;
		try{
			while( !this.isInterrupted() ){

				if( (inputString = in.readLine()) != null ){
					sendMessage(inputString);
				}
				if( inputString == null ){ throw new IOException(); }
			}			
		} catch( IOException e ) {	}
	}

	private void sendMessage(String message){
		ioUnit.receiveMessage(message);	
	}

	public void closeInput() {
		this.interrupt();
		try{
			in.close(); 
			sendMessage("Local InputStream shut down."); 
		}catch( IOException e ){}
	}

	public String getInput() {
		try {
			return in.readLine();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}	
}
