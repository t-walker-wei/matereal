package marker;

import jp.digitalmuseum.napkit.NapMarker;

public class MarkerInfo {

	public static NapMarker getRobotMarker() {
		return new NapMarker("markers/4x4_45.patt", 5.5);
	}

	public static NapMarker getEntityMarker() {
		return new NapMarker("markers/4x4_48.patt", 5.5);
	}

}
