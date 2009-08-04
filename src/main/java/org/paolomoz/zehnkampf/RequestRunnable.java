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
