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
package org.paolomoz.zehnkampf;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.paolomoz.zehnkampf.utils.ServerArguments;

/**
 * The HttpServer main class
 * @author paolomoz
 *
 */
public class HttpServer {
	
	static Logger logger = Logger.getLogger("HttpServer");
	
	public static void main(String[] args) throws Exception {
		ServerArguments argsHelper = new ServerArguments(args);
		int port = argsHelper.getPort();
		int tpSize = argsHelper.getTpSize();
		String docRootPath = argsHelper.getDocRootPath();
		
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(tpSize, tpSize, 50000L,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

		ServerSocket sock = new ServerSocket(port);
		logger.info("Server ready on port " + port);
		while (true) {
			Socket s = sock.accept();
			logger.info("A connection has been accepted by the Server Socket");
			tpe.execute(new RequestRunnable(s, new File(docRootPath)));
		}
	}

}
