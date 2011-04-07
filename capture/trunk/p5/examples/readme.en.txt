CaptureDS library howto guide
                                                              2011.04.06
                                                   arc@dmz (c) 2010-2011
────────────────────────────────────


* How to use:

1. Get DirectShow Java Wrapper

 Download latest version of dsj (version 0_8_62 as of 04/06/11) from
 http://www.humatic.de/htools/dsj/download.htm
 and unzip the archive.

2. Use DirectShow Java Wrapper through CaptureDS library

 Move (or copy) dsj.dll and dsj.jar unzipped from dsj and CaptureDS.jar
 from CaptureDS to "code" directory inside the root directory of sketch
 in which you want to use the library.

 Then, you can use feature of CaptureDS by writing

	import processing.video.*;

 or

	import processing.video.CaptureDS;

 at the beginning of your sketch code.

3. Check if CaptureDS correctly works

 Copy dsj.dll, dsj.jar, CaptureDS.jar to "code" folder inside
 GettingStartedCaptureDS directory and open GettingStartedCaptureDS.pde.


* Reference:

http://digitalmuseum.jp/software/nui/processing/CaptureDS/reference/


* License:

CaptureDS.jar is part of "capture" project, which allows capturing
images from webcams on Java and is distributed under MPL 1.1/GPL 2.0/
LGPL 2.1 license.
Please refer to the distribution of "capture" project for further detail.
http://mr.digitalmuseum.jp/

Please note that the user of CaptureDS is also bound to the license of
DirectShow Java Wrapper since CaptureDS.jar depends on dsj.jar.


────────────────────────────────────
arc@dmz

mail:  arc@digitalmuseum.jp
web:   http://digitalmuseum.jp/