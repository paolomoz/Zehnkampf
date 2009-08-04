/**
 * Copyright 2009 Paolo Mottadelli <paolo.moz@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  
 */
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
