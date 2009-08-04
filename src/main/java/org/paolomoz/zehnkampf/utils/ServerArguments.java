package org.paolomoz.zehnkampf.utils;

import java.util.logging.Logger;

/**
 * This class provides some helper methods to validate, 
 * retrieve and manage default values for arguments 
 * provided in the main class
 * @author paolomoz
 *
 */
public class ServerArguments {

	static Logger logger = Logger.getLogger("HttpServer");
	String[] args = new String[3];
	
	public ServerArguments(String args[]) {
		this.args = args;
	}
	
	public int getPort() {
		return getValidArgumentNumber(0, 8000);
	}
	
	public int getTpSize() {
		return getValidArgumentNumber(1, 10);
	}
	
	public String getDocRootPath() {
		if (args.length == 3) {
			return args[2];
		}
		else {
			return "/var/www/";
		}
	}
	
	private int getValidArgumentNumber(int index, int defaultValue) {
		int value = defaultValue;
		if ( args.length == 3 ) {
			value = defaultValue;
			try {
				value = new Integer(args[index]).intValue();
			} catch (NumberFormatException e) {
				usageAndExit("Could not parse one of the arguments.");
			}
		}
		return value;
	}
	
	private static void usageAndExit(String msg) {
		String usageMessage = "Usage: java -jar Zehnkampf.jar <port> <threadPoolSize>\n" +
				"<port> - port to listen on for HTTP requests\n" +
				"<threadPoolSize> - size of threadPool to create";
		logger.severe(msg + "\n" + usageMessage);
		System.exit(0);
	}

}
