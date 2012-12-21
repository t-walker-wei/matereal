napkit - a simple wrapper for NyARToolkit
Copyright (C) 2009-2012 Jun KATO

version 1.1.1
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

NyARToolkit Application Toolkit, or simply "napkit", is a small
wrapper library for NyARToolkit.
It uses NyARToolkit 2.5.2 for marker detection,
and allows to detect markers in a very smart way.

"napkit" is distributed under GNU GPLv3.
Please read LICENSE.txt for the detail.
You can get the source code by visiting its official site.

This toolkit works well with "Phybots"
and its video capturing package (jp.digitalmuseum.capture).
Please see http://phybots.com/ for details.

NyARToolkit is developed by R.Iizuka.
It is a Java implementation of ARToolKit, which was originally
developed by Hirokazu Kato.
Note that full version of NyARToolkit is NOT bundled in this
library. "napkit" uses only a part of it. For clarity, a list
of eliminated packages and classes is shown below:

- src.utils/**.*
- lib/NyARToolkit-2.5.2/
	jp/nyatla/nyartoolkit/detector/
	jp/nyatla/nyartoolkit/sample/
	jp/nyatla/nyartoolkit/nyidmarker/
	jp/nyatla/nyartoolkit/nyidmarker/data/
	jp/nyatla/nyartoolkit/processor/
	jp/nyatla/nyartoolkit/utils/

The official site of NyARToolkit may also be helpful for you.
http://nyatla.jp/nyartoolkit/wiki/index.php?FrontPage.en


- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
http://phybots.com/
arc (at) digitalmuseum.jp