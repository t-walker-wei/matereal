package jp.digitalmuseum.rm;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.phybots.Phybots;
import com.phybots.entity.RemoteStation;
import com.phybots.entity.Roomba;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jp.digitalmuseum.connector.RXTXConnector;
import jp.digitalmuseum.rm.handler.DefaultFileHandler;
import jp.digitalmuseum.rm.handler.RemoteStationHandler;
import jp.digitalmuseum.rm.handler.RoombaHandler;

/**
 * Control RemoteStation.
 *
 * @author Jun KATO
 */
public class RealmoteMain implements HttpHandler {

	public static final int NUM_BACKLOG = 5;
	public static final String NAME_CONFIGFILE = "rss.dat";
	private HttpServer server;
	private DefaultFileHandler defaultFileHandler;
	private RemoteStationHandler remoteStationHandler;
	private RoombaHandler roombaHandler;

	public static void main(String[] args) throws IOException {
		new RealmoteMain();
	}

	public RealmoteMain() throws IOException {

		ParameterFilter filter = new ParameterFilter();

		defaultFileHandler = new DefaultFileHandler("public");

		RXTXConnector connector = new RXTXConnector("COM:/dev/tty.usbserial-00002480");
		connector.connect(115200,
				RXTXConnector.DATABITS_8,
				RXTXConnector.STOPBITS_1,
				RXTXConnector.PARITY_NONE);
		RemoteStation remoteStation = new RemoteStation(connector);
		remoteStationHandler = new RemoteStationHandler(remoteStation);
		try {
			remoteStationHandler.load(NAME_CONFIGFILE);
		} catch (Exception e) {
			System.out.println("Realmote version was updated.");
		}

		Roomba roomba = new Roomba("btspp://00066600D69A");
		roombaHandler = new RoombaHandler(roomba);

		server = HttpServer.create(new InetSocketAddress(8000), NUM_BACKLOG);
		server.createContext("/", defaultFileHandler);
		server.createContext("/remote", remoteStationHandler).getFilters().add(filter);
		server.createContext("/roomba", roombaHandler).getFilters().add(filter);
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
			remoteStationHandler.save(NAME_CONFIGFILE);
			exchange.sendResponseHeaders(200, 0);
			exchange.close();
			Phybots.getInstance().dispose();
			this.server.stop(0);
			return;
		} else if (path.equals("/system/reload")) {
			remoteStationHandler.save(NAME_CONFIGFILE);
			this.defaultFileHandler.loadFiles();
			exchange.sendResponseHeaders(200, 0);
			exchange.close();
			return;
		}

		exchange.sendResponseHeaders(404, 0);
		exchange.close();
	}
}
