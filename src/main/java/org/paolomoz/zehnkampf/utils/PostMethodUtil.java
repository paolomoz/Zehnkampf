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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * This class provides methods nedded to process POST requests
 * @author paolomoz
 *
 */

public class PostMethodUtil {

	Logger logger = Logger.getLogger("HttpServer");
	BufferedReader reader;
	String targetPath;

	public PostMethodUtil(BufferedReader reader, String targetPath) {
		this.reader = reader;
		this.targetPath = targetPath;
	}

	public void upload() throws IOException {
		do {
			String currentLine = reader.readLine();

			if (currentLine.indexOf("Content-Type: multipart/form-data") != -1) {
				String boundary = currentLine.split("boundary=")[1];
				// The POST boundary
				while (true) {
					currentLine = reader.readLine();
					if (currentLine.indexOf("Content-Length:") != -1) {
						String contentLength = currentLine.split(" ")[1];
						logger.info("Content Length = " + contentLength);
						break;
					}
				}

				String filename = null;
				while (true) {
					currentLine = reader.readLine();
					if (currentLine.indexOf("--" + boundary) != -1) {
						filename = reader.readLine().split("filename=")[1]
								.replaceAll("\"", "");
						String[] filelist = filename.split("\\"
								+ System.getProperty("file.separator"));
						filename = filelist[filelist.length - 1];
						break;
					}
				}

				String fileContentType = reader.readLine().split(" ")[1];
				System.out.println("File content type = " + fileContentType);

				reader.readLine();
				PrintWriter fout = new PrintWriter(targetPath + "/" + filename);
				String prevLine = reader.readLine();
				currentLine = reader.readLine();

				// Here we upload the actual file contents
				while (true) {
					if (currentLine.equals("--" + boundary + "--")) {
						fout.print(prevLine);
						break;
					} else {
						fout.println(prevLine);
					}
					prevLine = currentLine;
					currentLine = reader.readLine();
				}
				fout.close();
			}

		} while (reader.ready());
	}

}
