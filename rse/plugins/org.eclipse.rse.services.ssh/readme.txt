Readme for RSE ssh service
--------------------------

The RSE ssh plugin allows to connect the RSE Remote Command View to 
a remote host through the secure shell (ssh) protocol.
This plugin is meant as a proof-of-concept. The code does not have
product quality yet, and there are lots of open issues (marked as
TODO in the code). But it is functional for setting up an ssh shell
connection.

__Requirements:__
The ssh service plugin has been tested with RSE M2 candidate
(CVS HEAD as of May 09, 2006) and Eclipse 3.2 RC3.
The Eclipse Platform Team / CVS feature is needed for the 
com.jcraft.jsh plugin and the org.eclipse.team.cvs.ui plugin.

__Installation:__
You need an Eclipse PDE Workspace with RSE.
Then, choose File > Import > Existing Projects > Archive File,
to import the ssh service archive.

__Usage:__
* Start RSE, create a new system of type "SSH Only".
* If you store your ssh private keys in a non-standard place, use
  Window > Preferences > Team > CVS > SSh2 Connection Method > General
  to set the ssh home directory, and private key types to be used.
* Select the "Shells" node and choose Contextmenu > Launch Shell.
* Enter your username on the remote system. For the password, just
  enter anything (this is not checked, since ssh has its own method
  of acquiring password information).
* When asked to accept remote host authenticity, press OK.
* Enter the correct password for ssh on the remote system (this is only
  necessary if you are not using a private key).

__Known Limitations:__
* Symbolic Links are not resolved (readlink not supported by jsch-0.1.28)
* Ssh passwords can not be stored (no key ring; use private keys instead)
* Ssh timeouts are not observed (no automatic reconnect)
  - after auto-logout, shell just doesnt react to input any more
* Password and passphrase internal handling has not been checked for
  security against malicious reading from other Eclipse plugins.

__Known Issues:__
* A dummy password must be entered on initial connect (can be saved)
* After some time, the connection may freeze and need to be
  disconnected.
* Command service should be provided in addition to the remote shell service.
* Extremely long remote command output may lead to an Exception
  due to memory exhaustion (ArrayIndexOutOfBoundsException)
* "Break" can not be sent to the remote system in order to cancel
  long-running jobs on the remote side. 
* Moving files with a space in the name doesn't currently work. 
  Renaming them works though.
* Copy&paste, or drag&drop of remote files remotely does'nt currently work.
* The plugin currently uses some "internal" classes from the 
  org.eclipse.team.cvs.ui plugin. This needs to be cleaned up.
* For other internal coding issues, see TODO items in the code.

__Changelog:__
v0.2:
* Re-use Team/CVS/ssh2 preferences for ssh2 home and private keys specification
  Allows to do the ssh login without password if private/public key are set up.
* Key management from Team/CVS/ssh2 Preferences can also be used
* Add sftp files subsystem
* Fix status after disconnect operation
* Update about.html
