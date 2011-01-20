import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;

import jp.digitalmuseum.mr.Matereal;
import jp.digitalmuseum.mr.service.Camera;

/**
 * Run a camera service to capture images.
 *
 * @author Jun KATO
 */
public class RunCameraAndSaveImage {

	public static void main(String[] args) {

		// Run a camera.
		Camera camera = new Camera();
		camera.start();

		// Save an image captured from the camera.
		try {
			Thread.sleep(3000);
			RenderedImage image = camera.getImage();
			ImageIO.write(image, "JPEG", new File("test.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Matereal.getInstance().dispose();
		}
	}
}
