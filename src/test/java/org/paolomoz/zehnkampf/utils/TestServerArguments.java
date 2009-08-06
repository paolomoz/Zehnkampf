package org.paolomoz.zehnkampf.utils;

import junit.framework.TestCase;

public class TestServerArguments extends TestCase {
	
	String[] args1 = {"7070", "3", "/some/path"};
	String[] args2 = {"7070", "xx", "/some/path"};
	String[] args3 = {"yyyy", "3", "/some/path"};
	String[] args4 = {"7070", "3"};
	ServerArguments serverArgs;
	int port;
	int tpSize;
	String docRootPath;
	
	public void testGetValidArgumentNumber() throws Exception {
		// Test against args1: regular arguments
		serverArgs = new ServerArguments(args1);
		port = serverArgs.getPort();
		assertEquals(7070, port);
		tpSize = serverArgs.getTpSize();
		assertEquals(3, tpSize);
		docRootPath = serverArgs.getDocRootPath();
		assertEquals("/some/path", docRootPath);
	}
	
	public void  testInvalidThreadPoolSizeNumber() throws Exception {
		// test against args2: invalid thread pool size number
		serverArgs = new ServerArguments(args2);
		port = serverArgs.getPort();
		assertEquals(7070, port);
		try {
			tpSize = serverArgs.getTpSize();
			assertEquals(10, tpSize);
		}
		catch (Exception e) {}
		docRootPath = serverArgs.getDocRootPath();
		assertEquals("/some/path", docRootPath);
	}
	
	public void testInvalidPortNumber() throws Exception {
		// test against args3: invalid port number
		serverArgs = new ServerArguments(args3);
		try {
			port = serverArgs.getPort();
			assertEquals(8000, port);
		}
		catch (Exception e) {}
		tpSize = serverArgs.getTpSize();
		assertEquals(3, tpSize);
		docRootPath = serverArgs.getDocRootPath();
		assertEquals("/some/path", docRootPath);
	}
	
	public void testInvalidArgumentsNumber() throws Exception {
		// test against args4: invalid number of arguments
		serverArgs = new ServerArguments(args4);
		port = serverArgs.getPort();
		assertEquals(8000, port);
		tpSize = serverArgs.getTpSize();
		assertEquals(10, tpSize);
		docRootPath = serverArgs.getDocRootPath();
		assertEquals("/var/www/", docRootPath);
	}

}
