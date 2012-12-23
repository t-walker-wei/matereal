package jp.digitalmuseum.rm.handler;

import java.io.IOException;

import com.phybots.entity.Roomba.RoombaCore;
import com.phybots.entity.Roomba;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RoombaHandler implements HttpHandler {
	private Roomba roomba;

	public RoombaHandler(Roomba roomba) {
		this.roomba = roomba;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		System.out.println(path);

		boolean result;
		if (path.equals("/roomba/ping")) {
			exchange.sendResponseHeaders(200, 0);
			exchange.close();
			return;
		} else if (path.equals("/roomba/list")) {
			// list(exchange);
			return;
		} else if (path.equals("/roomba/status")) {
			// status(exchange);
			return;
		} else if (path.equals("/roomba/clean")) {
			result = clean(exchange);
		} else if (path.equals("/roomba/cancel")) {
			result = cancel(exchange);
		} else if (path.startsWith("/roomba/delete/")) {
			// result = delete(exchange, path.substring("/roomba/delete/".length()));
			result = false;
		} else {
			result = false;
		}

		if (result) {
			return;
		}
		exchange.sendResponseHeaders(404, 0);
		exchange.close();
	}

	private boolean clean(HttpExchange exchange) throws IOException {

		Roomba.RooTooth.wakeUp(roomba);

		RoombaCore core = roomba.requestResource(RoombaCore.class, this);
		core.clean();
		roomba.freeResource(core, this);

		exchange.sendResponseHeaders(200, 0);
		exchange.close();
		return true;
	}

	private boolean cancel(HttpExchange exchange) throws IOException {

		RoombaCore core = roomba.requestResource(RoombaCore.class, this);
		core.safe();
		core.power();
		roomba.freeResource(core, this);

		exchange.sendResponseHeaders(200, 0);
		exchange.close();
		return true;
	}

	/*
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, body.length);
		OutputStream responseBody = exchange.getResponseBody();
		responseBody.write(body);
		responseBody.close();
		exchange.close();
	*/
}
