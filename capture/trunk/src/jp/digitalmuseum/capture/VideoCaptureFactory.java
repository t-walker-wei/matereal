package jp.digitalmuseum.capture;

public interface VideoCaptureFactory {

	/**
	 * Returns a new instance of VideoCapture implementation.
	 *
	 * @return A new instance of VideoCapture implementation.
	 */
	public abstract VideoCapture newInstance();

	/**
	 * Returns a new instance of VideoCapture implementation
	 * with the specified identifier.
	 *
	 * @param identifier Device identifier.
	 * @return VideoCapture object.
	 */
	public abstract VideoCapture newInstance(String identifier);

	/**
	 * Query available devices and return their identifiers
	 * @return Device identifiers in a String array.
	 */
	public abstract String[] queryIdentifiers();
}