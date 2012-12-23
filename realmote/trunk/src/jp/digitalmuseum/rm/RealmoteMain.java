package jp.digitalmuseum.rm;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jp.digitalmuseum.connector.RXTXConnector;

import com.phybots.Phybots;
import com.phybots.entity.RemoteStation;
import com.phybots.entity.Roomba;

import jp.digitalmuseum.rm.handler.DefaultFileHandler;
import jp.digitalmuseum.rm.handler.FungusHandler;
import jp.digitalmuseum.rm.handler.RemoteStationHandler;
import jp.digitalmuseum.rm.handler.RoombaHandler;

/**
 * Control RemoteStation + α.
 * 
 * @author Jun KATO
 */
public class RealmoteMain implements HttpHandler {

	public static final int NUM_BACKLOG = 5;
	public static final String NAME_CONFIGFILE = "rss.dat";
	private HttpServer server;
	private DefaultFileHandler defaultFileHandler;
	private RemoteStationHandler remoteStationHandler = null;
	private RoombaHandler roombaHandler = null;
	private FungusHandler fungusHandler = null;

	public static void main(String[] args) throws IOException {
		new RealmoteMain(args);
	}

	public RealmoteMain(String[] args) throws IOException {

		ParameterFilter filter = new ParameterFilter();
		server = HttpServer.create(new InetSocketAddress(8000), NUM_BACKLOG);

		// Default file handler
		defaultFileHandler = new DefaultFileHandler("public");

		// RemoteStation handler
		String remoteStationPort = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-remote") && i < args.length - 1) {
				remoteStationPort = args[i + 1];
			}
		}
		if (remoteStationPort != null) {
			RXTXConnector connector = new RXTXConnector(remoteStationPort);
			connector.connect(115200, RXTXConnector.DATABITS_8,
					RXTXConnector.STOPBITS_1, RXTXConnector.PARITY_NONE);
			RemoteStation remoteStation = new RemoteStation(connector);
			remoteStationHandler = new RemoteStationHandler(remoteStation);
			server.createContext("/remote", remoteStationHandler).getFilters()
					.add(filter);
			try {
				remoteStationHandler.load(NAME_CONFIGFILE);
			} catch (Exception e) {
				System.out.println("Realmote version was updated.");
			}
		}

		// Roomba handler
		String roombaPort = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-roomba") && i < args.length - 1) {
				roombaPort = args[i + 1];
			}
		}
		if (roombaPort != null) {
			Roomba roomba = new Roomba(roombaPort);
			roombaHandler = new RoombaHandler(roomba);
			server.createContext("/roomba", roombaHandler).getFilters()
					.add(filter);
		}
		
		// Fungus handler
		String fungusDirectory = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-fungus") && i < args.length - 1) {
				fungusDirectory = args[i + 1];
			}
		}
		if (fungusDirectory != null) {
			fungusHandler = new FungusHandler(fungusDirectory);
			server.createContext("/fungus", fungusHandler).getFilters()
					.add(filter);
		}

		server.createContext("/", defaultFileHandler);
		server.createContext("/system", this);
		server.setExecutor(null);
		server.start();
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		System.out.println(path);

		// Shutdown the server.
		if (path.equals("/system/off")) {
			if (remoteStationHandler != null) {
				remoteStationHandler.save(NAME_CONFIGFILE);
			}
			exchange.sendResponseHeaders(200, 0);
			exchange.close();
			Phybots.getInstance().dispose();
			this.server.stop(0);
			return;

		// Reload the setting.
		} else if (path.equals("/system/reload")) {
			if (remoteStationHandler != null) {
				remoteStationHandler.save(NAME_CONFIGFILE);
			}
			this.defaultFileHandler.loadFiles();
			exchange.sendResponseHeaders(200, 0);
			exchange.close();
			return;
		}

		exchange.sendResponseHeaders(404, 0);
		exchange.close();
	}
}
