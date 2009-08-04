package org.paolomoz.zehnkampf;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.paolomoz.zehnkampf.utils.ServerArguments;


public class HttpServer {
	
	static Logger logger = Logger.getLogger("HttpServer");
	
	public static void main(String[] args) throws IOException {
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
