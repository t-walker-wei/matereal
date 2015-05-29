"matereal" is a Java toolkit that allows to prototype interactive robot applications. It runs on Windows, Mac OSX and hopefully on Linux(not tested). This project contains three sub-projects, "capture", "connector" and "napkit". **This toolkit was renamed to Phybots and is now distributed at [phybots.com](http://phybots.com/).**

"capture" is a cross-platform library that uses DirectShow on Windows, QuickTime on Mac OSX and Java Media Framework on Linux to capture images with webcams. This library can be used independently without "matereal".

"connector" is a simple wrapper package of java.net, [RXTXlib](http://rxtx.qbang.org/wiki/) and [BlueCove](http://bluecove.org/) for connecting Java VM to external devices through TCP/IP, serial and parallel port and Bluetooth Serial Port Profile.

"napkit" is a wrapper library of [NyARToolkit](http://sourceforge.jp/projects/nyartoolkit) that can be easily used with "capture". Some part of this library is distributed under GPLv3 due to ARToolKit license restriction. This library also extends "matereal" to detect absolute positions of robots and objects with visual markers.

All of these libraries are currently under development phase, and their documentation is going to be available soon.

Please visit [our official site](http://phybots.com/) for more information.