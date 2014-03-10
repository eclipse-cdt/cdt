Readme for RSE DataStore Example
------------------------------

The DataStore Example shows how to use the DataStore communication layer
to interact with an agent on a server machine.  This example includes an
RSE service layer, subsystem layer and some UI pieces in order to demonstrate
different types of remote tasks.  The example is only meant as an educational
 guide to developers that use or extend the DataStore services.
 
__Requirements:__
The DataStore example has been tested with with RSE 3.1 

__Installation:__
You need an Eclipse PDE Workspace with RSE.
Then, choose File > Import > Existing Projects > Archive File,
to import the example archive.

__Server Installation:___
The DStore Server Runtime needs to be installed on the remote machines
that you wish to connect to.
To enable to server portion of the example: 
1) The "miners" source folder needs to be exported to a jar file (i.e. myminer.jar)
2) The exported jar needs to be put in the DStore Server Runtime installation 
directory on the server.  
3) The server and daemon scripts need to be updated to include this jar in their 
respective classpaths.

__Usage:__
The dstore service must be enabled on the remote system (see below).  The subsystem
configuration for the sample subsystem in this plugin is enabled for Linux and
Unix connections but could be enabled for any other system that supports DStore.



