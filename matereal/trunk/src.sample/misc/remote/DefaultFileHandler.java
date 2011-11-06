/*
 * PROJECT: matereal at http://mr.digitalmuseum.jp/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is matereal.
 *
 * The Initial Developer of the Original Code is Jun KATO.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun KATO. All Rights Reserved.
 *
 * Contributor(s): Jun KATO
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package misc.remote;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Stack;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class DefaultFileHandler implements HttpHandler {
	private String rootPath;
	private HashMap<String, byte[]> cache;

	public DefaultFileHandler(String rootPath) {
		this.rootPath = rootPath;
		loadFiles();
	}

	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		if (path.endsWith("/")) {
			path += "index.html";
		}

		if (!cache.containsKey(path)) {
			exchange.sendResponseHeaders(404, 0);
			exchange.close();
			return;
		}

		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", getContentType(path));
		exchange.sendResponseHeaders(200, 0);

		OutputStream responseBody = exchange.getResponseBody();
		responseBody.write(cache.get(path));
		responseBody.close();
	}

	private String getContentType(String path) {
		path = path.toLowerCase();
		if (path.endsWith(".html") ||
				path.endsWith(".htm")) {
			return "text/html";
		}
		if (path.endsWith(".js")) {
			return "text/javascript"; // obsolete
		}
		if (path.endsWith(".css")) {
			return "text/css";
		}
		return "text/plain";
	}

	public void loadFiles() {
		cache = new HashMap<String, byte[]>();
		System.out.println("---Loading static files.");

		if (!new File(rootPath).isDirectory()) {
			rootPath = ".";
		}
		Stack<String> dirs = new Stack<String>();
		dirs.push(rootPath);

		while (!dirs.isEmpty()) {
			String dirPath = dirs.pop();
			File dir = new File(dirPath);
			String[] list = dir.list();
			for (String e : list) {
				if (e.startsWith(".")) {
					continue;
				}
				String path = dirPath + "/" + e;
				File f = new File(path);
				if (f.exists()) {
					if (f.isDirectory()) {
						dirs.push(path);
					} else {
						// remove the root path.
						path = path.substring(rootPath.length());
						// add the pair of path & data to the database
						cache.put(path, loadFile(f));
						System.out.println(path);
					}
				}
			}
		}
	}

	private byte[] loadFile(File file) {
		try {
			byte[] data = new byte[(int) file.length()];
			FileInputStream fis;
			fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(data, 0, data.length);
			bis.close();
			return data;
		} catch (IOException e) {
			return null;
		}
	}
}
