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
<li>The <b>systemTypes</b> extension point now allows specifying the "icon", "iconLive" 
  and "enableOffline" properties either as a Property, or as an attribute. This change
  allows for better integration with PDE. It is backward compatible with the format used
  in RSE 1.0M4.</li>
<li>RSE now provides an <b>update site</b> at
  <a href="http://download.eclipse.org/dsdp/tm/updates/">http://download.eclipse.org/dsdp/tm/updates/</a>.
  We encourage users to update frequently, since we expect
  RSE quality to improve more and more as we are approaching our 1.0 release.</li>
<li>Persistent storage of connection and filter data has been streamlined to
  use <b>fewer files and directories</b>, resulting in simplification of team support
  and improved perfomance.</li>
<li>Windows dstore daemon can now be started by simply double clicking on daemon.bat
  (<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=142952">Bug 142952</a>)</li>
<li>Numerous bugs have been fixed, and we consider RSE safe now for 
  all kinds of data transfer, even if it's done in multiple background sessions
  (except <b>FTP</b>, which will be enhanced as soon as the Jakarta Commons Net library
  passes EMO legal review).</li>
<li>Use <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target%20Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&bugidtype=include&chfieldfrom=2006-08-18&chfieldto=2006-09-27&chfield=resolution">
  this query</a> to show the list of bugs fixed since the last milestone,
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-1.0M4-200608182355/index.php">
  RSE 1.0M4</a>.</li>
<li>Look <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/index.html">
  here</a> for the CVS changelog.</li>
</ul>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#808080"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">API Changes since RSE 1.0 M4</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<ul>
<li><b>Renamed</b> the <b>org.eclipse.rse.ui.subsystemConfiguration</b> extension point 
  to <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/extension-points/org_eclipse_rse_ui_subsystemConfigurations.html">
  subsystemConfigurations</a> 
  in order to better match the standard naming scheme used by the Platform.</li>
<li><b>Renamed</b> the <b>org.eclipse.rse.ui.newConnectionWizardDelegate</b> extension point
  to <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/extension-points/org_eclipse_rse_ui_newConnectionWizardDelegates.html">
  newConnectionWizardDelegates</a> 
  in order to better match the standard naming scheme used by the Platform.</li>
<li><b>Removed</b> the <b>org.eclipse.rse.ui.rseConfigDefaults</b> extension point.
  Use Java Properties instead, as described in the 
  <a href="http://dsdp.eclipse.org/help/latest/index.jsp?topic=/org.eclipse.rse.doc.isv/reference/misc/runtime-options.html">
  runtime-options documentation</a>.</li>
<li><b>Removed</b> the <b>org.eclipse.rse.ui.passwordPersistence</b> extension point.
  The same functionality is achieved by using the data known from the
  IConnectorService implementations of a systemType (supportsUserId(), supportsPassword()).
  As long as a subsystem supports user id and password, the password can also be persisted.
  Persistence is always done case sensitive; Persistence cannot be disabled for system types.</li>
<li><b>Moved</b> several <b>RSE Model Objects and Interfaces</b> from org.eclipse.rse.ui to core:
  <ul>
    <li>(UI) <code>org.eclipse.rse.filters</code> --&gt; <code>org.eclipse.rse.core.filters</code></li>
    <li>(UI) <code>org.eclipse.rse.model</code> --&gt; <code>org.eclipse.rse.core.model</code></li>
    <li>(UI) <code>org.eclipse.rse.references</code> --&gt; <code>org.eclipse.rse.core.references</code></li>
    <li>(UI) <code>org.eclipse.rse.subsystems.servicesubsystem</code> --&gt; <code>org.eclipse.rse.core.subsystems</code></li>
  </ul> 
  Client code can be adapted to the new locations easily by invoking "Organize Imports" except for
  the following additional changes that need to be made:
  <ul>
    <li><b>Event handling methods</b> for <code>ISystemResourceChangeEvent</code>, 
      <code>ISystemPreferenceChangeEvent</code>,
      <code>ISystemModelChangeEvent</code>,
      <code>ISystemRemoteChangeEvent</code> have been removed from 
      <b>ISystemRegistry</b>, such that they are available only in the 
      <b>SystemRegistry</b> implementation. This applies to the fireEvent(),
      postEvent() and corresponding add...Listener() methods. The simplest
      fix in user code is to get the SystemRegistry from RSEUIPlugin 
      instead of SystemRegistry as described below.</li>
    <li>Use <code>RSEUIPlugin.getTheSystemRegistry()</code> instead of <code>SystemRegistry.getSystemRegistry()</code></li>
  </ul>
  Note that wherever possible, client code should only refer to the model object
  interfaces in <code>org.eclipse.rse.core.*</code> and not use the actual 
  implementations which still reside in the UI plugin (these will be moved
  to core eventually, too).
</li>
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
  <li>Some more RSE Model classes may be moved from the UI plugin to the 
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
The following M5 <a href="http://www.eclipse.org/dsdp/tm/development/plan.php#M5">plan</a>
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
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=143462">bug 143462</a> - maj - [updating] Dirty remote editors do not get notified</li>
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
