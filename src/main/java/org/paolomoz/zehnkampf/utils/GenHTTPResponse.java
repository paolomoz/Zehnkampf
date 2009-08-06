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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.tika.mime.MimeTypes;

/**
 * This class processes the Socket Input/OutputStream 
 * providing the correct response from the server
 * @author paolomoz
 *
 */
public class GenHTTPResponse {

	Logger logger = Logger.getLogger("HttpServer");
	String requestLine;
	String[] requestLineParams = new String[3];
	int METHOD_REQUEST_PARAM = 0;
	int URI_REQUEST_PARAM = 1;
	int PROTOCOL_REQUEST_PARAM = 2;

	public void generateResponse(InputStream in, OutputStream out, File docRoot)
			throws IOException {
		requestLineParams = getRequestLineParams(in);
		File requestedFile = new File(docRoot, requestLineParams[URI_REQUEST_PARAM]);
		BufferedOutputStream buffOut = new BufferedOutputStream(out);
		HttpResponse response = setResponse(requestedFile);
		writeResponse(response, buffOut);
		buffOut.flush();
	}


	private HttpResponse setResponse(File requestedFile)
			throws FileNotFoundException {
		HttpResponse response;
		if (requestedFile.exists() && !requestedFile.isDirectory()) {
			response = setOK(requestedFile);
		} else {
			response = setNotFound();
		}
		return response;
	}


	private HttpResponse setNotFound() {
		HttpResponse response;
		response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_NOT_FOUND, "Not Found");
		logger.info(response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
		return response;
	}


	private HttpResponse setOK(File requestedFile) throws FileNotFoundException {
		HttpResponse response;
		int fileLen = (int) requestedFile.length();
		BufferedInputStream fileIn = new BufferedInputStream(
				new FileInputStream(requestedFile));
		MimeTypes mimeTypes = new MimeTypes();
		String mimeType = mimeTypes.getMimeType(requestedFile).getName();

		response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_OK, "OK");
		response.setHeader("Content-Type", mimeType);
		response.setHeader("Content-length", new Integer(fileLen)
				.toString());
		HttpEntity entity = new InputStreamEntity(fileIn, fileLen);
		response.setEntity(entity);
		logger.info(response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
		return response;
	}
	
	public String[] getRequestLineParams(InputStream in) throws IOException {
		String[] params = new String[3];
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		requestLine = reader.readLine();
		logger.info("Request: " + requestLine);
		if ((requestLine == null) || (requestLine.length() < 1)) {
			throw new IOException("Could not read request");
		}
		StringTokenizer st = new StringTokenizer(requestLine);
		try {
			params[METHOD_REQUEST_PARAM] = st.nextToken();
			params[URI_REQUEST_PARAM] = st.nextToken();
			params[PROTOCOL_REQUEST_PARAM] = st.nextToken();
		} catch (NoSuchElementException x) {
			throw new IOException("Could not parse the request line");
		}
		return params;
	}


//	public void parseRequestLine(InputStream in) throws IOException {
//		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//		requestLine = reader.readLine();
//		logger.info("Request: " + requestLine);
//		if ((requestLine == null) || (requestLine.length() < 1)) {
//			throw new IOException("Could not read request");
//		}
//		StringTokenizer st = new StringTokenizer(requestLine);
//		try {
//			method = st.nextToken();
//			uri = st.nextToken();
//		} catch (NoSuchElementException x) {
//			throw new IOException("Could not parse the request line");
//		}
//	}
	

	public void writeResponse(HttpResponse response, OutputStream buffOut)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(buffOut));
		
		// write response status line
		writer.write(response.getStatusLine().toString() + "\r\n");
		
		// write response headers
		for (HeaderIterator iterator = response.headerIterator() ; iterator.hasNext() ;) {
			Header item = iterator.nextHeader();
			writer.write(item.getName() + ": " + item.getValue());
		}
		
		// write response separator line
		writer.write("\r\n");
		
		// write response entity
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			entity.writeTo(buffOut);
		}
	}

}
