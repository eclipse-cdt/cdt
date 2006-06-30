<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="http://www.eclipse.org/default_style.css" type="text/css">
<title>Build Notes for RSE @buildId@</title>
</head>

<body>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" width="80%">
		<p><b><font class=indextop>Build Notes for RSE @buildId@</font></b><br>
		@dateLong@ </p>
		</td>
	</tr>
</table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">New and Noteworthy</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<ul>
<li>RSE ssh feature now supports connections via Proxy.
  <ul>
    <li>Re-uses ssh Preferences from Team &gt; CVS &gt; SSH2 Connection Method, and Team &gt; CVS &gt; Proxy Settings.</li>
    <li>Ssh private key authentication is supported, but RSE requires entering a dummy password
      (<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=142471">bug 142471</a>).
      As an alternative, passwords can also be maintained by RSE.</li>
  </ul>
</li> 
<li>Documentation is now available. Note that this documentation partially still
  refers to the older IBM RSE product and thus contains lots of outdated 
  references.</li>
<li>The New Connection Wizard is now completely replaceable for contributed
  system types. As a side effect of this, the ordering of subsystems for a
  connection may change (<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=149280">bug 149280</a>).
<li>Use <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target%20Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&bugidtype=include&chfieldfrom=2006-05-19&chfieldto=2006-06-30&chfield=resolution">
  this query</a> to show the list of bugs fixed for this build.</li>
<li>Look <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/index.html">
  here</a> for the CVS changelog.</li>
</ul>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Getting Started</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<p>Download RSE SDK, and either
<ul><li>Extract it into your installation of Eclipse 3.2, or</li>
    <li>Extract it into an empty directory and link it as product extension via Help &gt; Install/Update, or</li>
    <li>(If you want to write code for RSE) extract it into an empty directory, and from an Eclipse PDE Workspace 
        choose Import &gt; Plug-in development &gt; Plug-ins and Fragemtns. Select the RSE directory and import
        everything.</li>
</ul>
Start Eclipse Workbench, and choose <b>Window &gt; Open Perspective &gt; Other &gt; Remote System Explorer</b>.</p>

<p>Even without an actual connection to a remote system, you can start experimenting with the RSE UI on the 
local host, which is shown by default:
<ul>
  <li>Browse the Filesystem, choose contextmenu &gt; show in Table, and observe the Properties view</li>
  <li>Create a new Filter to show specific resources in the file system only
  <li>Launch an RSE Shell (Shells node &gt; Launch)</li>  
</ul>
For operations on an actual remote system, you can either
<ul>
  <li>use the "<b>SSH Only</b>" system type (<b>New > Other > Remote Systems Explorer > Connection</b>), or</li>
  <li>start a dstore server daemon on the remote system and use any of the other connection types.</li>
</ul>    
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Installing the Dstore server</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<p>
RSE is a framework that supports plugging in many different communication protocols.
By default, the dstore, FTP and ssh protocol plug-ins are provided, with dstore being 
the most richest in features.</p>
<p>
Dstore requirs a server to run on the remote system. There are several methods to 
get a server launched for a particular user, the most easy one to set up is the 
daemon method. To start a dstore launcher daemon,
<ul>
<li>On Windows:<ul>
  <li>Extract the rseserver-*-windows.zip package and cd to it.</li>
  <li>Run <b>setup.bat</b>, then run <b>daemon.bat</b>.</li>
  </ul></li>
<li>On Linux or AIX or other Unix:<ul>
  <li>Extract the appropriate rseserver-*.tar package.</li>
  <li>Become <b>root</b> and cd to the package directory.</li>
  <li>Make sure that a <b>Sun or IBM JRE 1.4</b> or higher is in the PATH. The gcj-based java installation
      that comes with many Linux distributions will not do! You can download a Sun JRE from
      <a href="http://java.sun.com">http://java.sun.com</a>.
  <li>Run <b>perl daemon.pl</b>.
  </ul></li>
</ul>
<p>
<b>Note:</b> In its default configuration for testing, the dstore daemon accepts <b>unencrypted 
passwords</b> from the RSE client. For production use, SSL can be enabled in order to encrypt
connections, or the RSE server can be launched differently (e.g. through ssh).</p>
<p>
When no root access is available on the remote system (typically UNIX), normal
users can start a dstore server for themselves only, instead of a daemon:
<ul>
<li>On the remote system, run <b>perl server.pl [portname]</b></li>
<li>On the RSE client, create the dstore connection</li>
<li>After creating the connection, select it and choose Properties
<ul><li>On <b>Server Launcher Settings</b>, choose <b>Connect to Running Server</b></li>
    <li>On the <b>Subsystem</b> page, enter the port number you used for starting the server</li>
</ul></li>
<li>When connecting, enter just anything for username and password (these will be ignored).</li>
<li>The server.pl script has more options, e.g. for using the first available
  port instead of a well-known one, or for restricting access to a single 
  user ID. Since all dstore communication will be on the single TCP port,
  this port can also be forwarded through an ssh tunnel if desired.</li>
</ul>

</ul>

</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Using remote connections</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<ul>
  <li>In the RSE Perspective, Remote Systems View, press the <b>New Connection</b> button.<ul>
    <li>Note: In the Preferences, you can enable displaying available new connection types in the RSE tree.</li></ul></li>
  <li>Select the desired system type<ul>
    <li>Coose system type "SSH Only" for ssh servers, or any other for dstore.</li></ul></li>
  <li>Enter an IP address for a remote system running an ssh server or dstore server.
    A connection name will be suggested automatically, but can be changed.<ul>
    <li>You can also run a dstore server on the local machine for testing. In this case,
        type "localhost" as address.</li>
    <li>You can press Finish right away, the wizard defaults are usually fine.</li></ul></li>
  <li>Fill in the username / password dialog.<ul>
    <li>Note: For ssh, if you have private keys, the password here is just a dummy.
        Enter anything and save it. You can setup ssh private key authentication through
        the <b>Team &gt; CVS &gt; SSH2 Connection Method</b> Preference page.</li></ul></li>
  <li><b>Browse remote files</b>, or open remote shells.<ul>
    <li>You can <b>drag and drop</b> files between local and remote file systems, between editors and any view.
        Files are transferred as needed.</li>
    <li>On dstore, you can browse into remote archives (*.zip, *.tar) without having to transfer the entire contents. This works thanks
        to "miners" on the remote side. Custom miners can be plugged into the dstore server.<br>
        Note: Some tar formats currently fail to work. See <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=139207">bug 139207</a>.</li>
    <li>On dstore, when you list directories in a remote shell, the <b>shell output is parsed</b> to identify
        files, folders and even line numbers from compiler error messages. These items can also be
        dragged and dropped, or double clicked to position an editor on them.</li></ul></li>
  <li>On dstore, you can choose <b>Search &gt; Remote...</b>.<ul>
    <li>The dstore miners support searching a remote file system
      without having to transfer any data.</li></ul></li> 
  <li>On dstore, when the remote system is Linux, AIX or Other Unix:<ul>
    <li>Browse remote <b>Processes</b>.</li>
    <li>Select "My Processes" and choose context menu &gt; <b>Monitor</b>.
    <li>Enable polling, choose a short wait time. See processes appear and vanish as you perform commands in a remote shell.</li></ul></li> 

</ul>
</td></tr></tbody></table>

<!--
</td></tr></tbody></table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr><td colspan="2">&nbsp;</td></tr>
</table>
-->

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Known Problems and Workarounds</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
The following M3 <a href="http://www.eclipse.org/dsdp/tm/development/plan.php">original plan</a> deliverables did 
not make it into this build:
<ul>
<li>User Actions, and Import/Export were deferred. 
  A new <a href="http://www.eclipse.org/dsdp/tm/development/plan.php">plan</a> will be published.</li>
<li>JUnit tests did not make it into the build due to pending IP review.
  They are available from Bugzilla 
  <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=149080">bug 149080</a> instead.</li>
<li>Examples are not yet available as downloadable package. A <b>CDT Launch Integration Example</b>
  and a sample custom subsystem called <b>Daytime Example</b> are available from the
  <a href="http://www.eclipse.org/dsdp/tm/development/index.php">RSE CVS Repository</a> instead.</li>
</ul>
The following critical or major bugs are currently known. Since
the goal of this milestone was "functional complete" for soliciting 
user and API feedback, we still gave a go for this milestone.
We'll strive to fix these as soon as possible.
<ul>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=149186">bug 149186</a> - maj - downloading in background can truncate the remote file<br/>
      -- In order to avoid this bug, do not put any data transfer into background.</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=143462">bug 143462</a> - maj - [updating] Dirty remote editors do not get notified</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=143292">bug 143292</a> - maj - [mac] Move Resource dialog causes hang/crash</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=139207">bug 139207</a> - maj - Browsing into some remote tar archives fails, and may crash the dstore server<br/>
      -- This problem was only observed with invalid tar archives.</li>
</ul>
Click 
<a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&component=RSE&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&bug_severity=blocker&bug_severity=critical&bug_severity=major&cmdtype=doit">here</a>
for an up-to-date list of major or critical bugs, or
<a href="https://bugs.eclipse.org/bugs/report.cgi?x_axis_field=bug_severity&y_axis_field=op_sys&z_axis_field=&query_format=report-table&classification=DSDP&product=Target+Management&component=RSE&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&format=table&action=wrap">here</a>
for a complete up-to-date bugzilla status report, or
<a href="https://bugs.eclipse.org/bugs/report.cgi?x_axis_field=bug_severity&y_axis_field=op_sys&z_axis_field=&query_format=report-table&classification=DSDP&product=Target+Management&component=RSE&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&format=table&action=wrap">here</a>
for a report on bugs fixed so far.
<!--
<a href="https://bugs.eclipse.org/bugs/report.cgi?x_axis_field=bug_severity&y_axis_field=op_sys&z_axis_field=bug_status&query_format=report-table&short_desc_type=allwordssubstr&short_desc=&classification=DSDP&product=Target+Management&component=RSE&format=table&action=wrap">here</a>
for a complete up-to-date bugzilla status report. 
-->
</td></tr></tbody></table>

</body>
</html>
