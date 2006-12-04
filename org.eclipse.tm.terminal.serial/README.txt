Important note:

In order to compile and run this plugin, comm has to be installed into 
the java installation. Once comm.jar is installed in your JRE, you'll
need to quit and re-start Eclipse PDE in order to recompute the classpath.

Linux or Solaris:
-----------------
* Get comm3.0_u1 or later from Sun at 
  http://www.sun.com/download/products.xml?id=43208d3d
* Follow the installation instructions. As per 3.0_u1 on Linux, you need to
  - cp jar/comm.jar $JRE/lib/ext
  - cp doc/javax.comm.properties $JRE/lib
  - cp lib/*.so $JRE/lib/i386

Windows:
--------
* Get comm.jar 2.0.3 ZIP archive from Sun at
  http://www.sun.com/download/products.xml?id=43208d3d
  - Extract comm.jar into %JRE%\lib\ext
* Get rxtx-2.0-7pre1-i386-pc-mingw32.zip from
  http://users.frii.com/jarvi/rxtx/download.html
  - Extract RXTXcomm.jar into %JRE%\lib\ext
  - Extract rxtx*.dll into %JRE%\bin
* Create new text file 
	%JRE%\lib\javax.comm.properties
  with the following line as contents:
	driver=gnu.io.RXTXCommDriver

Other platforms (MaxOS X etc):
------------------------------
* Get comm.jar 2.0.3 as explained for Windows
* Get rxtx-2.0-7pre1.tar.gz sources for your platform from
  http://users.frii.com/jarvi/rxtx/download.html
* Follow instructions to compile and install. You'll need a 
  javax.comm.properties file as explained for Windows.
  