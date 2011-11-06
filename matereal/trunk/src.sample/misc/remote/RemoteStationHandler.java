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

import java.io.IOException;
import java.util.ArrayList;

import jp.digitalmuseum.mr.entity.RemoteStation;
import jp.digitalmuseum.mr.entity.RemoteStation.RemoteStationCore;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RemoteStationHandler implements HttpHandler {
	private RemoteStationCore rsCore;
	private ArrayList<RemoteCommand> commands;

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
		} else {
			result = false;
		}

		if (result) {
			return;
		}
		exchange.sendResponseHeaders(200, 0);
		exchange.close();
	}

	private void list(HttpExchange exchange) throws IOException {

		// Send the response.
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "application/json");
		exchange.getResponseBody().write("[".getBytes());
		for (int id = 0; id < commands.size(); id ++) {
			if (id != 0) {
				exchange.getResponseBody().write(",".getBytes());
			}
			RemoteCommand command = commands.get(id);
			exchange.getResponseBody().write(
					String.format("{id:%d,name:\"%s\"}", id, command.name).getBytes());
		}
		exchange.getResponseBody().write("]".getBytes());
		exchange.getResponseBody().close();
		exchange.close();
	}

	private void record(HttpExchange exchange) throws IOException {
		System.out.println("---Receiving a command.");

		// Receive command.
		rsCore.blinkLED(3);
		byte[] data = rsCore.receiveCommand();
		if (data == null) {
			rsCore.blinkLED(5);
			exchange.sendResponseHeaders(500, 0);
			exchange.close();
			return;
		}

		// Register the command.
		int id = commands.size();
		RemoteCommand command = new RemoteCommand();
		command.name = String.format("Command no.%d", id + 1);
		command.data = data;
		commands.add(id, command);

		// Send the response.
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "application/json");
		exchange.getResponseBody().write(
				String.format("{id:%d,name:\"%s\"}", id, command.name).getBytes());
		exchange.getResponseBody().close();
		exchange.close();

		System.out.println("Received command:");
		command.print();
		rsCore.blinkLED(3);
	}

	private boolean play(HttpExchange exchange, String idString) throws IOException {
		System.out.println("---Sending a command.");

		// Get the specified command data.
		int id;
		try {
			id = Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			id = -1;
		}
		if (id < 0 || id >= commands.size()) {
			rsCore.blinkLED(5);
			return false;
		}

		// Send the command data to the RemoteStation.
		RemoteCommand command = commands.get(id);
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
}
