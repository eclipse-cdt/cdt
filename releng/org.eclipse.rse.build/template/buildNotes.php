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
<!--
<li>The <a href="http://download.eclipse.org/dsdp/tm/updates/">TM Update Site</a> now uses <b>Signed Jarfiles</b>.</li>
<li>An <b>Experimental RSE EFS Provider</b> is now available as a
  download, or from the <a href="http://download.eclipse.org/dsdp/tm/updates/">Update Site</a>.</li>
-->
<li>Fixed <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=164306">bug 164306</a> - FTP console shows plaintext passwords</li>
<li>Use 
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&chfieldfrom=2006-11-14&chfieldto=2006-12-16&chfield=resolution&cmdtype=doit">
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&target_milestone=1.0.1&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&cmdtype=doit"> -->
  this query</a> to show the list of bugs fixed since the last release,
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/R-1.0-200611121600/index.php">
  RSE 1.0</a>
  [<a href="http://download.eclipse.org/dsdp/tm/downloads/drops/R-1.0-200611121600/buildNotes.php">build notes</a>].</li>
<li>For details on checkins, see the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/index.html">
  RSE CVS changelog</a>, and the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/core/index.html">
  TM Core CVS changelog</a>.</li>
<li>For other questions, please check the
  <a href="http://wiki.eclipse.org/index.php/TM_and_RSE_FAQ">TM and RSE FAQ</a>
  as well as the
  <a href="http://wiki.eclipse.org/index.php/RSE_1.0_Known_Issues_and_Workarounds">
  RSE 1.0 Known Issues and Workarounds</a>.</li>
</ul>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Getting Started</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<p>The RSE User Documentation now has a
<a href="http://dsdp.eclipse.org/help/latest/index.jsp?topic=/org.eclipse.rse.doc.user/gettingstarted/g_start.html">
Tutorial</a> that guides you through installation, first steps,
connection setup and important tasks.</p>
<p>
If you want to know more about future directions of the Target Management
Project, developer documents, architecture or how to get involved,<br/>
the online
<a href="http://www.eclipse.org/dsdp/tm/tutorial/index.php">Getting Started page</a>
as well as the
<a href="http://wiki.eclipse.org/index.php/TM_and_RSE_FAQ">TM and RSE FAQ</a>
are the best places for you to get started.
</p>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">API Status</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<p>As per the Target Management 
<a href="http://www.eclipse.org/dsdp/tm/development/plan.php#M4">plan</a>,
we reached API Freeze for RSE 1.0.</p>
<p>In fact we have reviewed and documented all relevant APIs, and they
have proven useful in earlier proprietary versions of RSE.<br/>
Yet, due to a lack of public feedback so far we still want to 
<b>declare the APIs provisional for now</b>.</p>
<p><b>We are committed to not introducing any incompatible
API changes on the RSE 1.0 maintenance stream (1.0.x).</b><br/>
But we reserve the right to change any API for the next 
RSE major release in a not backward compatible way.<br/>
All such API changes will be voted on
by committers on the <a href="http://dev.eclipse.org/mailman/listinfo/dsdp-tm-dev">
dsdp-tm-dev</a> developer mailing list, and documented in a migration guide
for future releases.</p>
<p>Currently, we see the following areas for potential API changes:
<ul>
  <li>Classes and Interfaces that are not meant for public use will be
   moved to packages tagged as <tt>internal</tt>. This will apply 
   particularly to the "implementation" plugins for the ssh, ftp and
   local subsystems (these do not define any new APIs anyways).</li>
  <li>The <tt>IConnectorService</tt> interface may be slightly modified
   in order to allow for better UI / Non-UI separation.</li>
  <li>Some more RSE Model classes may be moved from the UI plugin to the 
   non-UI core plugin.</li>
</ul>
If you want to start programming against RSE APIs now, best let us know
about your endeavours and keep yourself up-to-date. Stay in contact with
the <a href="http://dev.eclipse.org/mailman/listinfo/dsdp-tm-dev">
dsdp-tm-dev</a> developer mailing list, and give feedback to make the 
APIs better.
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#808080"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">API Changes since RSE 1.0 - newest changest first</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<ul>
<li>[<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=161777">bug 161777</a>]:
  <b>Moved</b> <b>HostShellAdapter</b> and <b>HostShellOutputStream</b> from remotecdt into 
  org.eclipse.rse.services.shells, thus making them API.</li>
</li>
</ul>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Known Problems and Workarounds</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<!--
The following critical or major bugs are currently known.
We'll strive to fix these as soon as possible.
<ul>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=162883">bug 162883</a> - maj - [shell updating] No prompt on Solaris local shell</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=162993">bug 162993</a> - maj - ssh connection gets confused</li>
</ul>
-->
<p>No major or critical bugs are known at the time of release.
Use 
<a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&component=RSE&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&bug_severity=blocker&bug_severity=critical&bug_severity=major&cmdtype=doit">this query</a>
for an up-to-date list of major or critical bugs.</p>

<p>The 
<a href="http://wiki.eclipse.org/index.php/RSE_1.0_Known_Issues_and_Workarounds">
RSE 1.0 Known Issues and Workarounds</a> Wiki page gives an up-to-date list
of the most frequent and obvious problems, and describes workarounds for them.<br/>
If you have other questions regarding RSE, please check the
<a href="http://wiki.eclipse.org/index.php/TM_and_RSE_FAQ">TM and RSE FAQ</a>
</p>

<p>Click 
<a href="https://bugs.eclipse.org/bugs/report.cgi?x_axis_field=bug_severity&y_axis_field=op_sys&z_axis_field=&query_format=report-table&classification=DSDP&product=Target+Management&component=RSE&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&format=table&action=wrap">here</a>
for a complete up-to-date bugzilla status report, or
<a href="https://bugs.eclipse.org/bugs/report.cgi?x_axis_field=bug_severity&y_axis_field=op_sys&z_axis_field=&query_format=report-table&classification=DSDP&product=Target+Management&component=RSE&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&format=table&action=wrap">here</a>
for a report on bugs fixed so far.
</p>
</td></tr></tbody></table>

</body>
</html>
