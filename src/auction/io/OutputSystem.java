package auction.io;

import java.io.PrintWriter;

public class OutputSystem {

	private PrintWriter out = null;
	private String login = "";
	
	public OutputSystem(String welcomeMessage){
		out = new PrintWriter(System.out);
		out.println(welcomeMessage);
		out.print(prompt());
		out.flush();
	}
	
	public void receiveInstruction(String instruction) {
		out.println(instruction);
		out.print(prompt());
		out.flush();
	}

	private String prompt(){
		return login+">";
	}

	public void closeOutput() {
		out.println("Good Bye!");
		out.close();
	}

	public void resetUser() {
		login = "";
		out.print(prompt());
		out.flush();
	}

	public void setUser(String user) {
		login = user;
		out.print(prompt());
		out.flush();	
	}
	
}
