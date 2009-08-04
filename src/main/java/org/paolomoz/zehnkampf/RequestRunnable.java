package org.paolomoz.zehnkampf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;
import org.paolomoz.zehnkampf.utils.GenHTTPResponse;

/**
 * An implementation of Runnable designed to run Sockets
 * @author paolomoz
 *
 */
class RequestRunnable implements Runnable {

	Logger logger = Logger.getLogger("HttpServer");
	private Socket s;
	GenHTTPResponse respGen = new GenHTTPResponse();
	File docRoot;

	RequestRunnable(Socket s, File docRoot) {
		this.s = s;
		this.docRoot = docRoot;
	}

	public void run() {
		InputStream in = null;
		OutputStream out = null;

		while (s != null) {
			try {
				in = s.getInputStream();
				out = s.getOutputStream();
				respGen.generateResponse(in, out, docRoot);
				out.flush();
			} catch (IOException iox) {
				logger.severe("I/O error while processing request\n");
				iox.printStackTrace();
			} finally {
				closeStreams(in, out);
				if (s != null) {
					try {
						s.close();
					} catch (IOException iox) {
						iox.printStackTrace();
					} finally {
						s = null;
					}
				}
			}
		}
	}

	private void closeStreams(InputStream in, OutputStream out) {
		try {
			in.close();
			out.close();
		} catch (Exception e) {
			logger.warning(e.getStackTrace().toString());
		} finally {
			in = null;
			out = null;
		}
	}
	

}
