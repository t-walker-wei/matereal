package jp.digitalmuseum.rm.handler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.phybots.entity.RemoteStation;
import com.phybots.entity.RemoteStation.RemoteStationCore;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RemoteStationHandler implements HttpHandler {
	private RemoteStationCore remoteStationCore;
	private ArrayList<RemoteCommand> commands;
	private int maxId = 0;

	public RemoteStationHandler(RemoteStation remoteStation) {
		remoteStationCore = remoteStation.requestResource(RemoteStationCore.class, null);
		commands = new ArrayList<RemoteCommand>();
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		System.out.println(path);

		boolean result;
		if (path.equals("/remote/ping")) {
			exchange.sendResponseHeaders(200, 0);
			exchange.close();
			return;
		} else if (path.equals("/remote/list")) {
			list(exchange);
			return;
		} else if (path.equals("/remote/record")) {
			record(exchange);
			return;
		} else if (path.startsWith("/remote/play/")) {
			result = play(exchange, path.substring("/remote/play/".length()));
		} else if (path.startsWith("/remote/set/")) {
			result = set(exchange, path.substring("/remote/set/".length()));
		} else if (path.startsWith("/remote/delete/")) {
			result = delete(exchange, path.substring("/remote/delete/".length()));
		} else {
			result = false;
		}

		if (result) {
			return;
		}
		exchange.sendResponseHeaders(404, 0);
		exchange.close();
	}

	public void save(String fileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeInt(maxId);
        oos.writeObject(commands);
        oos.close();
        fos.close();
	}

	@SuppressWarnings("unchecked")
	public boolean load(String fileName) throws IOException, ClassNotFoundException {
		try {
			FileInputStream fis = new FileInputStream(fileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
			maxId = ois.readInt();
			commands = (ArrayList<RemoteCommand>) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			return false;
		}
		return true;
	}

	private RemoteCommand getCommand(String idString) {
		int id;
		try {
			id = Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			return null;
		}
		RemoteCommand command = null;
		for (RemoteCommand c : commands) {
			if (c.id == id) {
				command = c;
				break;
			}
		}
		return command;
	}

	private void list(HttpExchange exchange) throws IOException {

		// Construct the response body.
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int id = 0; id < commands.size(); id ++) {
			if (id != 0) {
				sb.append(",");
			}
			RemoteCommand command = commands.get(id);
			sb.append(String.format("{\"id\":%d,\"name\":\"%s\"}", command.id, command.name));
		}
		sb.append("]");
		byte[] body = sb.toString().getBytes();

		// Send the response.
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, body.length);
		OutputStream responseBody = exchange.getResponseBody();
		responseBody.write(body);
		responseBody.close();
		exchange.close();
	}

	private void record(HttpExchange exchange) throws IOException {
		System.out.println("---Receiving a command.");

		// Receive command.
		remoteStationCore.blinkLED(3);
		byte[] data = remoteStationCore.receiveCommand();
		if (data == null) {
			System.out.println("No data was received.");
			remoteStationCore.blinkLED(5);
			exchange.sendResponseHeaders(500, 0);
			exchange.close();
			return;
		}

		// Register the command.
		RemoteCommand command = new RemoteCommand();
		command.id = maxId ++;
		command.name = String.format("Command no.%d", command.id + 1);
		command.data = data;
		commands.add(commands.size(), command);

		// Send the response.
		sendResponse(exchange, command);

		System.out.println("Received command:");
		command.print();
		remoteStationCore.blinkLED(3);
	}

	private boolean play(HttpExchange exchange, String idString) throws IOException {
		System.out.println("---Sending a command.");

		// Get the specified command data.
		RemoteCommand command = getCommand(idString);
		if (command == null) {
			remoteStationCore.blinkLED(5);
			return false;
		}

		// Send the command data to the RemoteStation.
		boolean result = true;
		for (int i = 0; i < 4; i ++) {
			if ((command.mask >> i & 1) == 0) {
				continue;
			}
			boolean r = remoteStationCore.sendCommand(command.data, RemoteStation.PORT1 + i);
			result &= r;
			System.out.print("The command is sent to port ");
			System.out.print(i + 1);
			System.out.print(": ");
			System.out.println(r ? "OK" : "NG");
		}

		// Send the response.
		exchange.sendResponseHeaders(result ? 200 : 500, 0);
		exchange.close();
		remoteStationCore.blinkLED(result ? 3 : 5);
		return true;
	}

	private boolean set(HttpExchange exchange, String idString) throws IOException {
		System.out.println("---Setting the name of the command.");

		// Get the specified command data.
		RemoteCommand command = getCommand(idString);
		if (command == null) {
			remoteStationCore.blinkLED(5);
			return false;
		}

		// Set the name of the command.
		@SuppressWarnings("unchecked")
		Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");
		if (params.containsKey("name")) {
			command.name = params.get("name").toString();			
			sendResponse(exchange, command);
			System.out.println(String.format("The name is set to \"%s\"", command.name));
		} else {
			exchange.sendResponseHeaders(500, 0);
			exchange.close();
		}
		return true;
	}

	private boolean delete(HttpExchange exchange, String idString) throws IOException {
		System.out.println("---Deleting the command.");

		// Parse the id string.
		int id;
		try {
			id = Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			remoteStationCore.blinkLED(5);
			return false;
		}

		// Remove the specified command.
		Iterator<RemoteCommand> it = commands.iterator();
		while (it.hasNext()) {
			RemoteCommand c = it.next();
			if (c.id == id) {
				it.remove();
				System.out.println(String.format("Command ID %d is removed.", id));
				exchange.sendResponseHeaders(200, 0);
				exchange.close();
				return true;
			}
		}

		// Return false if the command is not found.
		remoteStationCore.blinkLED(5);
		return false;
	}

	private void sendResponse(HttpExchange exchange, RemoteCommand command) throws IOException {
		byte[] body =
			String.format("{\"id\":%d,\"name\":\"%s\"}", command.id, command.name).getBytes();

		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, body.length);
		OutputStream responseBody = exchange.getResponseBody();
		responseBody.write(body);
		responseBody.close();
		exchange.close();
	}

	public static class RemoteCommand implements Serializable {
		private static final long serialVersionUID = -5749209411053231400L;
		public int id;
		public String name;
		public byte[] data;
		public int mask = 0xf;

		public void print() {
			int idx = 0;
			for (byte b : data) {
				int i = b & 0xff;
				System.out.print(String.format("%02x", i));
				if (idx % 16 == 15) {
					System.out.println();
				} else if (idx % 8 == 7) {
					System.out.print(" ");
				}
				idx ++;
			}
		}
	}
}
