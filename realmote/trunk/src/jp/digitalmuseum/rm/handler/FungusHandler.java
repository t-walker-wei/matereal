package jp.digitalmuseum.rm.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class FungusHandler implements HttpHandler {
	private String rootPath;

	public FungusHandler(String rootPath) {
		this.rootPath = rootPath;
		if (!this.rootPath.endsWith("/")) {
			this.rootPath = this.rootPath + "/";
		}
	}

	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();

		byte[] data = null;
		if (path.equals("/fungus/ping")) {
			exchange.sendResponseHeaders(200, 0);
			exchange.close();
			return;
		} else if (path.startsWith("/fungus/")
				&& !path.contains("../")) {
			path = path.substring("/fungus/".length());
			data = loadFile(new File(rootPath + path));
		}
		
		if (data == null) {
			exchange.sendResponseHeaders(404, 0);
			exchange.close();
			return;
		}

		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", getContentType(path));
		exchange.sendResponseHeaders(200, 0);
		OutputStream responseBody = exchange.getResponseBody();
		responseBody.write(data);
		responseBody.close();
	}

	private String getContentType(String path) {
		path = path.toLowerCase();
		if (path.endsWith(".jpeg") ||
				path.endsWith(".jpg")) {
			return "image/jpeg";
		}
		return "text/plain";
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
