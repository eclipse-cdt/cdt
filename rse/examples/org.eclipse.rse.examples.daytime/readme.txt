Readme for RSE Daytime Example
------------------------------

The Daytime Example shows how a new subsystem (daytime) is contributed
to RSE, and how an existing subsystem (ftp) is configured for a system
type. The example is mainly meant for developer's educational use,
it does not have much user value: the Daytime Subsystem retrieves 
the current time of day from a remote host via TCP port 13.

__Requirements:__
The Daytime example has been tested with with RSE M1 candidate
(CVS HEAD as of April 25, 2006) and Eclipse 3.2 RC1.

__Installation:__
You need an Eclipse PDE Workspace with RSE.
Then, choose File > Import > Existing Projects > Archive File,
to import the example archive.

__Usage:__
The daytime service must be enabled on the remote system (see below).
* Start RSE, create a new system of type "FTP-Daytime".
* Select the Daytime Subsystem and choose Contextmenu > Connect.
* Enter any username and password (this is not checked).
* Select the Daytime Subsystem and choose Refresh, or Contextmenu > Monitor.
* Enable polling in the remote monitor, you can see the time advance.

__Programmer's documentation:__
The interesting part of this example is in package 
   org.eclipse.rse.examples.daytime.model
where you see how the daytime node is added to the RSE tree through an
AbstractSystemViewAdapter. The DaytimeService is rather simple, since
queries are fast enough to use a connectionless service.

__Known Issues:__
* When something goes wrong during connect, the error message 
  does not give enough information about the cause of the error.
* Should define a second service, that uses UDP for getting the
  daytime. This would show the advantages of ServiceSubsystem.
  The Tutorial example (developer) is good for showing service-less
  subsystems.
* ConnectorService / ConnectorServiceManager should exist in a 
  simpler default implementation such that not every new service
  or subsystem implements the same over and over again (bug 150928).

__Enabling the Daytime Service on a Remote Host:__
In order for the example to work, the service on TCP port 13 must be 
activated on the host as follows:
* On Linux or other xinetd based UNIX systems, edit /etc/xinetd.d/daytime
  and set "disable=no", then restart (kill -HUP) xinetd
* On Solaris or other inetd based UNIX systmes, edit /etc/inetd.conf
  and make sure the following line is there:
     daytime stream tcp nowait root internal
  the kill -HUP inetd.
* On Windows/Cygwin, with xinetd installed, edit config
  files like described for Linux, then start xinetd.exe
