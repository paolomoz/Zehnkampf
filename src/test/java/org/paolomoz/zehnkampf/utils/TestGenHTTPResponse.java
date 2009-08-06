package org.paolomoz.zehnkampf.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;

public class TestGenHTTPResponse extends TestCase {
	
	GenHTTPResponse genHTTPResp;
	String entityString = "test";
	
	public void setUp() {
		genHTTPResp = new GenHTTPResponse();
	}
	
	public void testGetRequestLineParams() throws IOException {
		String requestLine = "GET /images/one.gif HTTP/1.1";
		InputStream is = getInputStream(requestLine);
		
		String[] requestLineParams = genHTTPResp.getRequestLineParams(is);
		
		assertEquals("GET", requestLineParams[genHTTPResp.METHOD_REQUEST_PARAM]);
		assertEquals("/images/one.gif", requestLineParams[genHTTPResp.URI_REQUEST_PARAM]);
		assertEquals("HTTP/1.1", requestLineParams[genHTTPResp.PROTOCOL_REQUEST_PARAM]);
	}
	
	public void testWriteResponse() throws IOException {
		HttpEntity entity = new StringEntity(entityString);
		
		HttpResponse response = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "OK");
		response.setHeader("Content-length", new Integer(entityString.length()).toString());
		response.setEntity(entity);
		
		assertEquals(1, response.getAllHeaders().length);
		assertEquals("HTTP/1.1 200 OK", response.getStatusLine().toString());
		
		OutputStream out = new ByteArrayOutputStream();
		genHTTPResp.writeResponse(response, out);
		
		assertEquals("test", out.toString());
	}

	private InputStream getInputStream(String requestLine) {
		InputStream is = null;
		is = new ByteArrayInputStream(requestLine.getBytes());
		return is;
	}

}
