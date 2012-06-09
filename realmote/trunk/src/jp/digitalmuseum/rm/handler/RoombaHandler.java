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
import com.phybots.entity.Roomba;
import com.phybots.entity.RemoteStation.RemoteStationCore;
import com.phybots.entity.Roomba.RoombaCore;
import com.sun.net.httpserver.Headers;
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
		if (path.equals("/roomba/list")) {
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
