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
<li>APIs and String Constants have been cleaned up from old artifacts.
  RSE <b>Documentation</b> has been adjusted to latest refactorings. You'll get the
  documentation as part of the installation, or you can browse it online at
  <a href="http://dsdp.eclipse.org/help/latest/">http://dsdp.eclipse.org/help/latest/</a>.
  Note that the online version is updated every night to hold the latest updates.</li>
<li>The ssh command shell now supports <b>RSE pattern matching</b> in the output.
  Output parsers are installed for make, ls, pwd, cd, ps and many popular compilers.
  If you run such a command, it's output will be parsed by the RSE shell
  and annotations will be added. Double clicking these partially works (Bugs 
  <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=153270">153270</a>,
  <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=153272">153272</a>).</li>
<li>The ssh command shell also supports <b>content assist</b> for directory names 
  and file names in the input box. To use it, press Ctrl+Space. After selecting
  a completion, the command will be sent immediately (Bug
  <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=153271">153271</a>).</li>
<li>RSE now provides an <b>update site</b> at
  <a href="http://download.eclipse.org/dsdp/tm/updates/">http://download.eclipse.org/dsdp/tm/updates/</a>.
  We encourage users to update frequently, since we expect
  RSE quality to improve more and more as we are approaching our 1.0 release.</li>
<li>Numerous bugs have been fixed, and we consider RSE safe now for 
  all kinds of data transfer, even if it's done in multiple background sessions
  (except <b>FTP</b>, which will be enhanced as soon as the Jakarta Commons Net library
  passes EMO legal review).</li>
<li>Use <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target%20Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&bugidtype=include&chfieldfrom=2006-06-30&chfieldto=2006-08-18&chfield=resolution">
  this query</a> to show the list of bugs fixed since the last milestone,
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-1.0M3-200606300720/index.php">
  RSE 1.0M3</a>.</li>
<li>Look <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/index.html">
  here</a> for the CVS changelog.</li>
  <!--
<li>New since I20060811-1342:<ul>
    <li><a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target%20Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&bugidtype=include&chfieldfrom=2006-08-11&chfieldto=2006-08-17&chfield=resolution">
    bugs fixed</a></li>
    </ul></li>
  -->
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
Getting Started Tutorial</a> that guides you through installation, first steps,
connection setup and important tasks.</p>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">API Freeze</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<p>As per the Target Management 
<a href="http://www.eclipse.org/dsdp/tm/development/plan.php#M4">plan</a>,
we reached API Freeze for RSE M4.</p>
<p>In fact we have reviewed and documented all relevant APIs, but just like most
Eclipse projects, we'll still reserve the right to make API improvements when
committers vote on them. Votes will be held publicly, such that everyone will
be informed in case the APIs should change.</p>
<p>Currently, we see the following areas for potential API changes:
<ul>
  <li>Classes and Interfaces that are not meant for public use will be
   moved to packages tagged as <tt>internal</tt>. This will apply 
   particularly to the "implementation" plugins for the ssh, ftp and
   local subsystems (these do not define any new APIs anyways).</li>
  <li>The <tt>IConnectorService</tt> interface may be slightly modified
   in order to allow for better UI / Non-UI separation.</li>
  <li>Some RSE Model classes may be moved from the UI plugin to the 
   non-UI core plugin.</li>
</ul>
If you want to start programming against RSE APIs now, best let us know
about your endeavours and keep yourself up-to-date.
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Known Problems and Workarounds</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
The following M4 <a href="http://www.eclipse.org/dsdp/tm/development/plan.php#M4">plan</a>
deliverables did not make it into this build:
<ul>
<li>User Actions, and Import/Export were deferred with M3 already. 
  A new plan has been published with M3 already.</li>
<li>JUnit tests did not make it into the build due to pending IP legal review.
  They are available from Bugzilla 
  <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=149080">bug 149080</a>
  instead. Due to the missing Unit Test Framework, automated tests could also
  not yet be added to this build.</li>
<li>The <b>CDT Launch Integration Example</b> is not yet available as a
  download. It is available from the 
  <a href="http://www.eclipse.org/dsdp/tm/development/index.php">
  RSE CVS Repository</a> instead.</li>
<li>Jakarta Commons Net is not yet available for <b>FTP and Telnet</b> due to pending legal
  review. We are confident to get these completed in August though.<ul>
  <li>As a consequence, FTP connections are still not quite reliable.</li>
  </ul></li>
</ul>
The following critical or major bugs are currently known.
We'll strive to fix these as soon as possible.
<ul>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=150949">bug 150949</a> - maj - RSE gets unusable when full logging is enabled</li>
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
</td></tr></tbody></table>

</body>
</html>
