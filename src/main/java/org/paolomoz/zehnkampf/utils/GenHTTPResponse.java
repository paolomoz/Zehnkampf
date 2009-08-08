/**
 * Copyright 2009 Paolo Mottadelli  <paolo.moz@gmail.com>
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;

/**
 * This class processes the Socket Input/OutputStream providing the correct
 * response from the server
 * 
 * @author paolomoz
 * 
 */
public class GenHTTPResponse {

	Logger logger = Logger.getLogger("HttpServer");
	String requestLine;
	String docRootPath;
	String[] requestLineParams = new String[3];
	int METHOD_REQUEST_PARAM = 0;
	int URI_REQUEST_PARAM = 1;
	int PROTOCOL_REQUEST_PARAM = 2;
	BufferedReader reader = null;

	static final String HTML_START = "<html><title>Zehnkampf Server</title><body>";
	static final String HTML_END = "</body></html>";

	public GenHTTPResponse(String DocRootPath) {
		this.docRootPath = DocRootPath;
	}

	public void generateResponse(InputStream in, OutputStream out)
			throws IOException {
		requestLineParams = getRequestLineParams(in);
		
		if (requestLineParams[METHOD_REQUEST_PARAM].equals("POST")) {
			PostMethodUtil util = new PostMethodUtil(reader, docRootPath
					+ requestLineParams[URI_REQUEST_PARAM]);
			util.upload();
		}
		
		File requestedFile = new File(docRootPath + requestLineParams[URI_REQUEST_PARAM]);
		BufferedOutputStream buffOut = new BufferedOutputStream(out);
		HttpResponse response = setResponse(requestedFile);
		writeResponse(response, buffOut);
		buffOut.flush();
	}
	
	public HttpResponse setResponse(File requestedFile)
			throws FileNotFoundException, UnsupportedEncodingException {
		HttpResponse response;
		if (requestLineParams[METHOD_REQUEST_PARAM].equals("GET")) {
			if (requestedFile.exists()) {
				response = setOK(requestedFile);
			} else {
				response = setNotFound();
			}
		} else if (requestLineParams[METHOD_REQUEST_PARAM].equals("POST")) {
			response = setCreated(requestedFile);
		} else {
			logger.severe("Method not supported (yet)");
			response = null;
		}
		return response;
	}

	public HttpResponse setCreated(File requestedFile)
			throws UnsupportedEncodingException {
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_CREATED, "Created");
		HttpEntity entity = getDirectoryEntity(requestedFile);
		response.setEntity(entity);
		return response;
	}
	
	public HttpEntity getDirectoryEntity(File requestedFile) throws UnsupportedEncodingException {
		String content = getDirectoryPageHtml(requestedFile);
		HttpEntity entity = new StringEntity(content);
		return entity;
	}

	public HttpResponse setNotFound() throws UnsupportedEncodingException {
		HttpResponse response;
		response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_NOT_FOUND, "Not Found");
		String entityBody = "<html>"
				+ "<head><title>404 - Not Found</title></head>"
				+ "<body>Not Found</body></html>";
		HttpEntity entity = new StringEntity(entityBody);
		response.setEntity(entity);
		return response;
	}

	public HttpResponse setOK(File requestedFile)
			throws FileNotFoundException, UnsupportedEncodingException {
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_OK, "OK");
		HttpEntity entity = null;
		if (requestedFile.isDirectory()) {
			entity = getDirectoryEntity(requestedFile);
		} else {
			int fileLen = (int) requestedFile.length();
			BufferedInputStream fileIn = new BufferedInputStream(
					new FileInputStream(requestedFile));
			
			// Detect the content mimetype
			String mimeTypeName = new MimetypesFileTypeMap().getContentType(requestedFile);

			response.setHeader("Content-Type", mimeTypeName);
			response.setHeader("Content-length", new Integer(fileLen)
					.toString());
			entity = new InputStreamEntity(fileIn, fileLen);
		}
		response.setEntity(entity);
		return response;
	}

	public void writeResponse(HttpResponse response, OutputStream buffOut)
			throws IOException {
		
		String statusLine = response.getStatusLine().toString();
		String[] headers = new String[response.getAllHeaders().length];
		int i = 0;
		for (HeaderIterator iterator = response.headerIterator(); iterator.hasNext();) {
			Header item = iterator.nextHeader();
			headers[i++] = item.getName() + ": " + item.getValue();
		}
		HttpEntity entity = response.getEntity();
		
		byte[] headerBytes = createResponseBytes(statusLine, headers, entity);

		buffOut.write(headerBytes);
	}

	public String[] getRequestLineParams(InputStream in) throws IOException {
		String[] params = new String[3];
		reader = new BufferedReader(new InputStreamReader(in));
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
	
	private byte[] createResponseBytes(String statusLine, String[] headers,
			HttpEntity entity) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));

		// Write the first line of the response, followed by
		// the RFC-specified line termination sequence.
		writer.write(statusLine + "\r\n");

		// Iterate all the speficied headers and write each of them
		for (int i = 0; i < headers.length; i++) {
			writer.write(headers[i] + "\r\n");
		}

		// A blank line is required after the header.
		writer.write("\r\n");
		writer.flush();

		// Write the response entity
		if (entity != null) {
			entity.writeTo(baos);
		}

		byte[] data = baos.toByteArray();
		writer.close();

		return data;
	}

	private String getDirectoryPageHtml(File requestedFile) {
	String HTML_FORM = "<form action=\"" + requestLineParams[URI_REQUEST_PARAM] + "\" enctype=\"multipart/form-data\""
		+ "method=\"post\">Enter the name of the File <input name=\"file\" type=\"file\"><br><input value=\"Upload\" type=\"submit\"></form>"
		+ "Upload only text files.";
		StringBuffer sb = new StringBuffer();
		sb
				.append(HTML_START + HTML_FORM);
		sb.append("<ul>");
		for (int i = 0; i < requestedFile.listFiles().length; i++) {
			File item = requestedFile.listFiles()[i];
			String itemPath = item.getPath().substring(docRootPath.length());
			sb.append("<li><a href=\""
					+ itemPath
					+ "\">" + item.getName() + "</a></li>");
		}
		sb.append("</ul>");
		sb.append(HTML_END);
		String content = sb.toString();
		return content;
	}


}
