Important note:
---------------
This README is for terminal.serial version 0.9.100 and later, corresponding
to RSE downloads after 2.0M4. Instructions for previous versions (using
Sun javacomm / javax.comm package instead of gnu.io) are still available from
http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.tm.core/terminal/org.eclipse.tm.terminal.serial/README.txt?root=DSDP_Project&view=markup&pathrev=R1_0_1  


Prerequisites:
--------------
In order to compile and run this plugin, RXTX has to be installed.
There are two options: Either installing RXTX as an Eclipse plugin,
or installing RXTX as a JVM extension. For installation as plugin,
you can download a ZIP archive or use the Update Manager.
In either case, once RXTX is installed, you'll need to quit and 
re-start Eclipse PDE in order to recompute the classpath.


Option A.1: Installation as an Eclipse Plugin via Update Manager:
-----------------------------------------------------------------
* In Eclipse, choose Help > Software Updates > Find and Install
  - Search for New Features to Install, Next
  - New Remote Site: 
       Name = RXTX
       URL  = http://rxtx.qbang.org/eclipse/
  - Finish, select proper version, Install All
 

Option A.2: Installation as an Eclipse Plugin via Download:
-----------------------------------------------------------
* Download RXTX SDK or Runtime ZIP from
     http://rxtx.qbang.org/eclipse/downloads/
  and extract it into your Eclipse installation.


Option B: Installation as a JVM Extension:
------------------------------------------
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
1.0.1 - Added options for installing RXTX as an Eclipse Plugin
0.9.100 - switched from Sun javax.comm to rxtx gnu.io for serial support
0.9.0   - first version