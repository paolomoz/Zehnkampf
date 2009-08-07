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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.entity.StringEntity;
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
	String docRootPath;
	String[] requestLineParams = new String[3];
	int METHOD_REQUEST_PARAM = 0;
	int URI_REQUEST_PARAM = 1;
	int PROTOCOL_REQUEST_PARAM = 2;
	BufferedReader reader = null;
	
    static final String HTML_START =
        "<html>" +
        "<title>HTTP POST Server in java</title>" +
        "<body>";

static final String HTML_END =
        "</body>" +
        "</html>";
	
	public GenHTTPResponse(String DocRootPath) {
		this.docRootPath = DocRootPath;
	}

	public void generateResponse(InputStream in, OutputStream out, File docRoot)
			throws IOException {
		requestLineParams = getRequestLineParams(in);
		if (requestLineParams[METHOD_REQUEST_PARAM].equals("POST")) {
			PostMethodUtil util = new PostMethodUtil(reader, docRootPath + requestLineParams[URI_REQUEST_PARAM]);
			util.upload();
		}
		File requestedFile = new File(docRoot, requestLineParams[URI_REQUEST_PARAM]);
		BufferedOutputStream buffOut = new BufferedOutputStream(out);
		HttpResponse response = setResponse(requestedFile);
		writeResponse(response, buffOut);
		buffOut.flush();
	}
	
//	public void processPost(InputStream in) throws IOException {
//		do {
//            String currentLine = reader.readLine();
//            
//            if (currentLine.indexOf("Content-Type: multipart/form-data") != -1) {
//              String boundary = currentLine.split("boundary=")[1];
//              // The POST boundary                                 
//              while (true) {
//                  currentLine = reader.readLine();
//                  if (currentLine.indexOf("Content-Length:") != -1) {
//                      String contentLength = currentLine.split(" ")[1];
//                      logger.info("Content Length = " + contentLength);
//                      break;
//                  }              
//              }
//              
//              String filename = null;
//              while (true) {
//                  currentLine = reader.readLine();
//                  if (currentLine.indexOf("--" + boundary) != -1) {
//                      filename = reader.readLine().split("filename=")[1].replaceAll("\"", "");
//                      String [] filelist = filename.split("\\" + System.getProperty("file.separator"));
//                      filename = filelist[filelist.length - 1];
//                      break;
//                  }              
//              }    
//              
//              String fileContentType = reader.readLine().split(" ")[1];
//              System.out.println("File content type = " + fileContentType);
//
//              reader.readLine();
//              PrintWriter fout = new PrintWriter(docRootPath + requestLineParams[URI_REQUEST_PARAM] + "/" + filename);
//              String prevLine = reader.readLine();
//              currentLine = reader.readLine();        
//              
//              //Here we upload the actual file contents
//              while (true) {
//                  if (currentLine.equals("--" + boundary + "--")) {
//                      fout.print(prevLine);
//                      break;
//                  }
//                  else {
//                      fout.println(prevLine);
//                  }
//                  prevLine = currentLine;              
//                  currentLine = reader.readLine();
//              } 
//              fout.close();           
//            }
//			
//		}while (reader.ready());
//	}


	private HttpResponse setResponse(File requestedFile)
			throws FileNotFoundException, UnsupportedEncodingException {
		HttpResponse response;
		if (requestLineParams[METHOD_REQUEST_PARAM].equals("GET")) {
			if (requestedFile.exists()) {
				response = setOK(requestedFile);
			} else {
				response = setNotFound();
			}
		}
		else if (requestLineParams[METHOD_REQUEST_PARAM].equals("POST")) {
			response = setCreated(requestedFile);
		}
		else {
			logger.severe("Method not supported (yet)");
			response = null;
		}
		return response;
	}
	
	private HttpResponse setCreated(File requestedFile) throws UnsupportedEncodingException {
		return setDirectoryResponse(requestedFile);
	}

	private HttpResponse setDirectoryResponse(File requestedFile)
			throws UnsupportedEncodingException {
		HttpEntity entity;
		HttpResponse response;
		response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_CREATED, "Created");
		StringBuffer sb = new StringBuffer();
		sb.append(HTML_START +
        "<form action=\"" + requestLineParams[URI_REQUEST_PARAM] + "\" enctype=\"multipart/form-data\"" +
        "method=\"post\">" +
        "Enter the name of the File <input name=\"file\" type=\"file\"><br>" +
       "<input value=\"Upload\" type=\"submit\"></form>" +
       "Upload only text files.");
		sb.append("<ul>");
		for (int i = 0 ; i < requestedFile.listFiles().length; i++) {
			File item = requestedFile.listFiles()[i];
			sb.append("<li><a href=\"" + item.getPath().substring(docRootPath.length()+1) + "\">" + item.getName() + "</a></li>");
		}
		sb.append("</ul>");
           sb.append(HTML_END);
           String content = sb.toString();
			response.setHeader("Content-Type", "text/html");
			response.setHeader("Content-length", new Integer(content.length())
					.toString());
		entity = new StringEntity(content);
		response.setEntity(entity);
		logger.info(response.getStatusLine().getStatusCode() + " - "
				+ response.getStatusLine().getReasonPhrase());
		return response;
	}

	private HttpResponse setNotFound() throws UnsupportedEncodingException {
		HttpResponse response;
		response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_NOT_FOUND, "404 - Not Found");
		String entityBody = "<html>" + 
		"<head><title>404 - Not Found</title></head>" +
		"<body>Not Found</body></html>";
		HttpEntity entity = new StringEntity(entityBody);
		response.setEntity(entity);
		logger.info(response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
		return response;
	}


	private HttpResponse setOK(File requestedFile) throws FileNotFoundException, UnsupportedEncodingException {
		HttpResponse response = null;
		HttpEntity entity = null;
		if (requestedFile.isDirectory()) {
			return setDirectoryResponse(requestedFile);
		}
		else {
		response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_OK, "OK");
			int fileLen = (int) requestedFile.length();
			BufferedInputStream fileIn = new BufferedInputStream(
					new FileInputStream(requestedFile));
			MimeTypes mimeTypes = new MimeTypes();
			String mimeType = mimeTypes.getMimeType(requestedFile).getName();

			response.setHeader("Content-Type", mimeType);
			response.setHeader("Content-length", new Integer(fileLen)
					.toString());
			entity = new InputStreamEntity(fileIn, fileLen);
			response.setEntity(entity);
			logger.info(response.getStatusLine().getStatusCode() + " - "
					+ response.getStatusLine().getReasonPhrase());
			return response;
		}
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
