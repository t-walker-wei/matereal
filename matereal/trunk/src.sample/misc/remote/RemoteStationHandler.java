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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import jp.digitalmuseum.mr.entity.RemoteStation;
import jp.digitalmuseum.mr.entity.RemoteStation.RemoteStationCore;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RemoteStationHandler implements HttpHandler {
	private RemoteStationCore rsCore;
	private ArrayList<RemoteCommand> commands;
	private int maxId = 0;

	public RemoteStationHandler(RemoteStation rs) {
		rsCore = rs.requestResource(RemoteStationCore.class, null);
		commands = new ArrayList<RemoteCommand>();
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		System.out.println(path);

		boolean result;
		if (path.equals("/remote/list")) {
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
		exchange.sendResponseHeaders(200, 0);
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
			sb.append(String.format("{\"id\":%d,\"name\":\"%s\"}", id, command.name));
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
		rsCore.blinkLED(3);
		byte[] data = rsCore.receiveCommand();
		if (data == null) {
			System.out.println("No data received.");
			rsCore.blinkLED(5);
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
		rsCore.blinkLED(3);
	}

	private boolean play(HttpExchange exchange, String idString) throws IOException {
		System.out.println("---Sending a command.");

		// Get the specified command data.
		RemoteCommand command = getCommand(idString);
		if (command == null) {
			rsCore.blinkLED(5);
			return false;
		}

		// Send the command data to the RemoteStation.
		boolean result = true;
		for (int i = 0; i < 4; i ++) {
			boolean r = rsCore.sendCommand(command.data, RemoteStation.PORT1 + i);
			result &= r;
			System.out.print("The command sent to port ");
			System.out.print(i + 1);
			System.out.print(": ");
			System.out.println(r ? "OK" : "NG");
		}

		// Send the response.
		exchange.sendResponseHeaders(result ? 200 : 500, 0);
		exchange.close();
		rsCore.blinkLED(result ? 3 : 5);
		return true;
	}

	private boolean set(HttpExchange exchange, String idString) throws IOException {
		System.out.println("---Setting the name of the command.");

		// Get the specified command data.
		RemoteCommand command = getCommand(idString);
		if (command == null) {
			rsCore.blinkLED(5);
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
			rsCore.blinkLED(5);
			return false;
		}

		// Remove the specified command.
		Iterator<RemoteCommand> it = commands.iterator();
		while (it.hasNext()) {
			RemoteCommand c = it.next();
			if (c.id == id) {
				it.remove();
				return true;
			}
		}

		// Return false if the command is not found.
		rsCore.blinkLED(5);
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
}