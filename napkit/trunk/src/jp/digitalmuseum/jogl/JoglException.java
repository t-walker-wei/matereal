package jp.digitalmuseum.jogl;

public class JoglException extends Exception {
	private static final long serialVersionUID = -5977063783146465466L;
	StringBuffer sb = null;

	public JoglException() {
		super();
	}

	public JoglException(Exception e) {
		super(e);
	}

	public JoglException(String s) {
		super(s);
	}

	public void setMessage(String s) {
		if (sb == null)
			sb = new StringBuffer(this.getMessage() + "\n");
		sb.append(s).append("\n");
	}

	public void setMessage(char[] c) {
		if (sb == null)
			sb = new StringBuffer(this.getMessage() + "\n");
		sb.append(c).append("\n");
	}

	public String getMessage() {
		if (sb == null)
			return super.getMessage();
		return sb.toString();
	}
}
