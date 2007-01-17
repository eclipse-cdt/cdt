Important note:

In order to compile and run this plugin, RXTX has to be installed into 
the jvm. Once RXTXcomm.jar is installed in your JRE, you'll need to 
quit and re-start Eclipse PDE in order to recompute the classpath.

Installation:
-------------
* Get RXTX binaries from
  ftp://ftp.qbang.org/pub/rxtx/rxtx-2.1-7-bins-r2.zip
* Copy RXTXcomm.jar into $JRE/lib/ext
* Copy the native libs for your Platform (*.so, *.jnilib, *.dll)
  into the respective native lib folder of your RSE 
* More installation instructions are at
  http://rxtx.qbang.org/wiki/index.php/Main_Page
* More downloads for other platforms (currently about 30)
  are available from the "ToyBox" link on
  http://users.frii.com/jarvi/rxtx/download.html

Changelog:
----------
0.9.100 - switched from Sun javax.comm to rxtx gnu.io for serial support
0.9.0   - first version