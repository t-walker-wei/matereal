#!/bin/sh

# -remote COM:/dev/tty.usbserial-00002480
# -roomba btspp://00066600D69A
# -fungus /Users/arc/Pictures/

java -Dfile.encoding=UTF-8 -Djava.library.path=./lib/ -classpath ./lib/connector-1.0.4.jar:./lib/bluecove-2.1.1-SNAPSHOT.jar:./lib/RXTXcomm.jar:./lib/phybots-core-1.0.0.jar:./bin jp.digitalmuseum.rm.RealmoteMain $*