<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="http://www.eclipse.org/default_style.css" type="text/css">
<title>TM @buildTypeLong@ Build: @buildId@</title>
</head>

<body>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" width="80%">
		<p><b><font class=indextop>TM @buildTypeLong@ Build: @buildId@</font></b><br>
		@dateLong@ </p>
		<p>These downloads are provided under the
		<a href="http://www.eclipse.org/legal/epl/notice.php">Eclipse.org Software 
		User Agreement</a>.</p>
		
		<p><font size="+1"><strong>
<!-- -->
		  <a href="buildNotes.php">New and Noteworthy / Build Notes</a>
<!-- -->
<!-- 
		  <p><a href="http://www.eclipse.org/tm/development/relnotes/3.3/tm-news-3.3.html">New and Noteworthy</a></p>
		  <p><a href="http://www.eclipse.org/tm/development/relnotes/3.3/readme_tm_3.3.html">TM 3.3 Readme / Release Notes</a></p>
		  <p><a href="buildNotes.php">Build Notes</a></p>
-->
		</strong></font></p>
		
		To view the map file entries for this build, click
		<a href="directory.txt">here</a>.<br/>
		To view the compile logs for this build, click 
		<a href="compilelog.txt">here</a>.<br/>
		For getting started, consider the <a href="http://www.eclipse.org/tm/tutorial/index.php">TM Tutorials</a>
		and the <a href="http://wiki.eclipse.org/TM_and_RSE_FAQ">TM and RSE FAQ</a>.
		</p>
<!--
		<p>To view the build notes for this build click <a
 href="buildNotes.php">here</a>.<br>
		To view the test results for this build, click 
		<a href="testResults.php">here</a>.<br>
-->
		</td>
<!--
		<td width="28%"><img src="http://www.eclipse.org/images/Idea.jpg" height=86 width=120></td>
-->
	</tr>
</table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Requirements</font></b></td>
	</tr>
</table>
<table>
  <tbody>
    <tr><td><b>TM @buildId@ requires 
    	<a href="http://download.eclipse.org/eclipse/downloads/">
         Eclipse 3.4</a> or later</b>.
         <b>RSE Import/Export as well as FTP and Telnet</b> require <b>Java 1.5</b>
         or higher, the rest of RSE works with Java 1.4.
    </b></td></tr>
    <tr><td>
      For core RSE and TM-Terminal, the Eclipse <b>Platform Runtime Binary</b> is sufficient.
      Of course you can also use the popular <b>SDK</b> or any one of the downloadable <b>Packages</b>, all of which include the Eclipse Platform. 
    </td></tr> 
  </tbody>
</table>
</table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr><td colspan="2">&nbsp;</td></tr>
</table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0">
		<font face="Arial,Helvetica" color="#FFFFFF"><b>All-in-one</b> (includes runtime and documentation for <b>dstore, ftp, local, ssh, and telnet</b>)</font></td>
	</tr>
	<tr><td align="LEFT">
	For tool writers, <b>the RSE SDK</b> also includes the runtime as listed, plus programmer documentation and source.
	</td></tr>
</table>
<table border="0" cellspacing="2" cellpadding="0" width="100%">
	<tr>
		<td align="RIGHT" valign="TOP" width="7%">
		<div align="left">
			<b>Status</b></div>
		</td>
		<td width="30%"><b>Platform</b></td>
		<td width="63%"><b>Download</b></td>
	</tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
        <td>All</td>
        <td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/RSE-runtime-@buildId@.zip">RSE-runtime-@buildId@.zip</a>
           <i>(Recommended for most users)</i></td>
    </tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
		<td>All</td>
		<td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/RSE-SDK-@buildId@.zip">RSE-SDK-@buildId@.zip</a></td>
	</tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
		<td>All</td>
		<td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/TM-repo-@buildId@.zip">TM-repo-@buildId@.zip</a>
		    (Downloadable p2 repository including all features)</td>
	</tr>
</table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr><td colspan="2">&nbsp;</td></tr>
</table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#808080"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Stand-alone Offerings, Integrations and Add-ons</font></b>
		</td>
	</tr>
</table>
<table border="0" cellspacing="2" cellpadding="0" width="100%">
	<tr>
		<td align="RIGHT" valign="TOP" width="7%">
		   <div align="left"><b>Status</b></div>
		</td>
		<td width="7%"><b>Platform</b></td>
		<td width="23%"><b>Download</b></td>
		<td width="63%"><b>Notes</b></td>
	</tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
        <td>All</td>
        <td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/TM-terminal-@buildId@.zip">TM-terminal-@buildId@.zip</a></td>
        <td>
			A stand-alone ANSI / vt102 terminal emulator widget and view (with minor 
			  <a href="http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.tm.core/terminal/org.eclipse.tm.terminal/README.txt?revision=1.4&root=Tools_Project&view=markup">
			limitations</a>). Includes pluggable connectors 
			for serial line (through RXTX, see the 
			  <a href="http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.tm.core/terminal/org.eclipse.tm.terminal.serial/README.txt?revision=1.5&root=Tools_Project&view=markup">
			installation notes</a>), ssh and telnet. Includes Source.
        </td>
    </tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
		<td>All</td>
		<td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/RSE-useractions-@buildId@.zip">RSE-useractions-@buildId@.zip</a></td>
        <td><small>&nbsp;</small><br/>
        	Additional Framework for user-defined actions and compile commands.
        	Includes Source.
        </td>
    </tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
		<td>All</td>
		<td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/RSE-examples-@buildId@.zip">RSE-examples-@buildId@.zip</a></td>
        <td><small>&nbsp;</small><br/>
        	Tutorial code and example projects for developing against RSE SDK.
        	Includes Source.
        </td>
    </tr>
</table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr><td colspan="2">&nbsp;</td></tr>
</table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#808080"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Add-ons in Incubation</font></b>
		</td>
	</tr>
	<tr>
	    <td>Add-ons in 
	    <a href="http://www.eclipse.org/projects/what-is-incubation.php">
	    Incubation</a> state provide new features, but have 
	    not yet hardened their APIs through public review such that there could be 
	    a promise to keep them stable over releases. These add-ons have a 0.x version
	    number, and are provided for early adopters. Note that these features may 
	    already be very mature in terms of features provided, but not yet in terms
	    of the APIs provided.</td>
	</tr>
</table>
<table border="0" cellspacing="2" cellpadding="0" width="100%">
	<tr>
		<td align="RIGHT" valign="TOP" width="7%">
		   <div align="left"><b>Status</b></div>
		</td>
		<td width="7%"><b>Platform</b></td>
		<td width="23%"><b>Download</b></td>
		<td width="63%"><b>Notes</b></td>
	</tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
        <td>All <img src="egg.gif" width=20 height=20></td>
        <td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/TM-terminal-local-incubation-@buildId@.zip">TM-terminal-local-incubation-@buildId@.zip</a></td>
        <td>
			A local terminal (console) integrated into Eclipse. Works on Linux, Solaris and Mac OS X, but currently <i>not on Windows</i>. Requires CDT 7.0 (Eclipse Helios / June 2010) or higher. Includes Source.
        </td>
    </tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
        <td>All <img src="egg.gif" width=20 height=20></td>
        <td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/RSE-scp-incubation-@buildId@.zip">RSE-scp-incubation-@buildId@.zip</a></td>
        <td><small>&nbsp;</small><br/>
			A File Subsystem for SSH connections that do not provide the Sftp service,
			such as embedded targets running Dropbear. Allows choosing between scp or sftp
			connection when using the "SSH Only" System Type.
			Includes Source.
        </td>
    </tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
        <td>All <img src="egg.gif" width=20 height=20></td>
        <td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/RSE-wince-incubation-@buildId@.zip">RSE-wince-incubation-@buildId@.zip</a></td>
        <td><small>&nbsp;</small><br/>
			RSE Services for accessing Windows CE devices via Microsoft RAPI2.
			Provides a File subsystem for transparent remote file access.
			Requires Microsoft RAPI libraries installed.
			Includes Source.
        </td>
    </tr>
</table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr><td colspan="2">&nbsp;</td></tr>
</table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">DStore Server Runtime</font></b></td>
	</tr>
</table>
<table border="0" cellspacing="2" cellpadding="0" width="100%">
	<tr>
		<td align="RIGHT" valign="TOP" width="7%">
			<div align="left"><b>Status</b></div></td>
		<td width="30%"><b>Platform</b></td>
		<td width="63%"><b>Download</b></td></tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
        <td>Windows</td>
        <td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/rseserver-@buildId@-windows.zip">rseserver-@buildId@-windows.zip</a></td></tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
        <td>Linux</td>
        <td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/rseserver-@buildId@-linux.tar">rseserver-@buildId@-linux.tar</a></td></tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
        <td>Other Unix</td>
        <td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/rseserver-@buildId@-unix.tar">rseserver-@buildId@-unix.tar</a></td></tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
        <td>Mac OS X (<i>Experimental</i>)</td>
        <td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/rseserver-@buildId@-macosx.tar">rseserver-@buildId@-macosx.tar</a></td></tr>
</table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr><td colspan="2">&nbsp;</td></tr>
</table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr><td colspan="2">&nbsp;</td></tr>
</table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#808080"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">JUnit Plugin Tests</font></b></td>
	</tr>
	<tr>
	    <td>The RSE test suite requires the JUnit plug-in, which is included in the 
        <a href="http://download.eclipse.org/eclipse/downloads/">
        Eclipse SDK</a> or available as<br/>
        <a href="http://download.eclipse.org/eclipse/downloads/">
        eclipse-test-framework download</a> for users of the Eclipse Platform Runtime Binary. 
	    </td>
	</tr>
</table>



<table border="0" cellspacing="2" cellpadding="0" width="100%">
	<tr>
		<td align="RIGHT" valign="TOP" width="7%">
		<div align="left">
			<b>Status</b></div>
		</td>
		<td width="30%"><b>Platform</b></td>
		<td width="63%"><b>Download</b></td>
	</tr>
	<tr><td><div align=left><img src = "OK.gif" width=19 height=23></div></td>
		<td>All</td>
		<td><a href="http://www.eclipse.org/downloads/download.php?file=/tm/downloads/drops/@dropDir@/RSE-tests-@buildId@.zip">RSE-tests-@buildId@.zip</a></td>
	</tr>
</table>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr><td colspan="2">&nbsp;</td></tr>
</table>

<p>&nbsp;</p>
</body>
</html>
