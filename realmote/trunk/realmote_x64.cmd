rem -remote COM:COM7
rem -roomba btspp://00066600D69A
rem -fungus C:\Users\arc\Pictures\

java -Dfile.encoding=UTF-8 -Djava.library.path=.\lib\x64\ -classpath .\lib\connector-1.0.4.jar;.\lib\bluecove-2.1.1-SNAPSHOT.jar;.\lib\x64\RXTXcomm.jar;.\lib\phybots-core-1.0.0.jar;.\bin jp.digitalmuseum.rm.RealmoteMain %*