package sample.p5;

import processing.core.PApplet;
import processing.video.*;

public class GettingStartedCaptureDS extends PApplet {
	private static final long serialVersionUID = 1L;
	public static void main(String[] args) {
		new GettingStartedCaptureDS();
	}

	CaptureDS cam;

	public void setup() {
	  size(640, 480);

	  // If no device is specified, will just use the default.
	  cam = new CaptureDS(this, 640, 480);

	  // To use another device (i.e. if the default device causes an error),
	  // list all available capture devices to the console to find your camera.
	  // String[] devices = CaptureDS.list();
	  // println(devices);

	  // Change devices[0] to the proper index for your camera.
	  // cam = new CaptureDS(this, width, height, devices[0]);
	  // Change "Qcam Pro for Notebooks" to the proper name of your camera.
	  // cam = new CaptureDS(this, width, height, "Qcam Pro for Notebooks");

	  // Opens the settings page for this capture device.
	  //camera.settings();
	}


	public void draw() {
	  if (cam.available() == true) {
	    cam.read();
	    image(cam, (width - cam.width)/2, (height - cam.height)/2);
	    // The following does the same, and is faster when just drawing the image
	    // without any additional resizing, transformations, or tint.
	    // set((width - cam.width)/2, (height - cam.height)/2, cam);
	  }
	}
}
