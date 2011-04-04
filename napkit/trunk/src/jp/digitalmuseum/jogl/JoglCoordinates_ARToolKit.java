package jp.digitalmuseum.jogl;

import jp.digitalmuseum.jogl.JoglModelBase.Point;

/**
 * <pre>OpenGL default coordinates:
 *
 *       Y
 *       |
 *       |
 *       +------X
 *     ／
 *   ／
 * Z
 *
 *ARToolKit coordinates:
 *       Z
 *       |     Y
 *       |   ／
 *        | ／
 *        +------X</pre>
 *
 * @author Jun KATO, kei
 */
public class JoglCoordinates_ARToolKit extends JoglCoordinates {

	Point convert(Point point) {
		point.set(point.getX(), point.getZ()*-1, point.getY());
		return point;
	}
}
