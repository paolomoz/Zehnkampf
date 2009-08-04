package org.paolomoz.zehnkampf.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.tika.mime.MimeTypes;

public class GenHTTPResponse {

	Logger logger = Logger.getLogger("HttpServer");
	String requestLine;
	String method;
	String uri;

	public void generateResponse(InputStream in, OutputStream out, File docRoot)
			throws IOException {

		parseRequestLine(in);

		File requestedFile = new File(docRoot, uri);

		BufferedOutputStream buffOut = new BufferedOutputStream(out);

		HttpResponse response;
		if (requestedFile.exists() && !requestedFile.isDirectory()) {
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
		} else {
			response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
					HttpStatus.SC_NOT_FOUND, "Not Found");
		}

		StatusLine statusLine = response.getStatusLine();
		logger.info("Response - " + statusLine.getProtocolVersion().getProtocol() + 
				"/" + statusLine.getProtocolVersion().getMajor() + 
				"." + statusLine.getProtocolVersion().getMinor() +
				" " + statusLine.getStatusCode() + 
				" " + statusLine.getReasonPhrase());
		writeResponse(response, buffOut);

		buffOut.flush();
	}


	private void parseRequestLine(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		requestLine = reader.readLine();
		if ((requestLine == null) || (requestLine.length() < 1)) {
			throw new IOException("Could not read request");
		}
		StringTokenizer st = new StringTokenizer(requestLine);
		try {
			method = st.nextToken();
			uri = st.nextToken();
		} catch (NoSuchElementException x) {
			throw new IOException("Could not parse the request line");
		}
	}
	

	private void writeResponse(HttpResponse response, OutputStream buffOut)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));
		writer.write(response.getStatusLine() + "\r\n");
		writer.write("\r\n");
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			entity.writeTo(buffOut);
		}
	}

}
