package auction.global.config;

import java.io.InputStream;
import java.io.OutputStream;


public class IOUnitConfig {

	private IOUnitConfig(){}
	
	private static final String INFOINPUTUNIT = Components.INPUTSYSTEM + " " + GlobalConfig.INFOSTRING;
	private static final String ERRORINPUTUNIT = Components.INPUTSYSTEM + " " + GlobalConfig.ERRORSTRING;

	public static final InputStream INPUTSTREAM = System.in;
	public static final OutputStream OUTPUTSTREAM = System.out;

	
	public static final String INPUTUNITSHUTDOWNINFO = INFOINPUTUNIT + " Local InputStream shut down. ";	
	public static final String INPUTERROR = ERRORINPUTUNIT + " InputSystem Couldn't get the input!"; ;
	public static final String OUTPUTSALUTATION = "Good Bye!";
	
}
