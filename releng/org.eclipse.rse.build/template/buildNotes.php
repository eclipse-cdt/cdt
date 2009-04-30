<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="stylesheet" href="http://www.eclipse.org/default_style.css" type="text/css">
<title>Build Notes for TM @buildId@</title>
</head>

<body>
<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" width="80%">
		<p><b><font class=indextop>Build Notes for TM @buildId@</font></b><br>
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
<li>TM @buildId@ <b>requires Eclipse 3.4 or later</b>, although parts of it will likely run
  with earlier versions. <b>Import/Export, Telnet and FTP require Java 1.5</b>, the rest of
  RSE runs on Java 1.4.
  Platform Runtime is the minimum requirement for core RSE and Terminal.
  Discovery needs EMF.</li>
<li>Important Bug Fixes, Enhancements and API changes:<ul>
  <li>For <b>IService.initService()</b> there was an API specification update 
    (see below).</li>
  <li>The <b>Remote CDT Launcher Plug-in</b> has been moved into the CDT
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=267065">267065</a>].</li>
  <li><b>RSE FTP and Telnet</b> now ship with Apache Commons Net version 2.0.
    As a prerequisite of Commons Net 2.0, Java5 is now required for these connection types.
    Besides bug fixes in Commons Net, one new feature due to the change is that RSE now 
    supports Telnet window size notifications in the RSE Terminal.
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=267473">267473</a>]
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=267474">267474</a>].</li>
  </ul></li>
<li>At least 32 bugs were fixed: Use 
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&component=Core&component=RSE&component=Terminal&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=WORKSFORME&chfieldfrom=2009-03-19&chfieldto=2009-05-10&chfield=resolution&cmdtype=doit&negate0=1&field0-0-0=target_milestone&type0-0-0=regexp&value0-0-0=%5B23%5D.0&field0-0-1=target_milestone&type0-0-1=regexp&value0-0-1=3.1%20M%5B23456%5D"> -->
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&component=Core&component=RSE&component=Terminal&target_milestone=3.1+M7&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=WORKSFORME&cmdtype=doit">
  this query</a> to show the list of bugs fixed since
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-3.1M6-200903192015/">
  TM 3.1M6</a>
  [<a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-3.1M6-200903192015/buildNotes.php">build notes</a>].</li>
<li>For details on checkins, see
  <a href="http://dsdp.eclipse.org/dsdp/tm/searchcvs.php">TM SearchCVS</a>, the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/index.html">
  RSE CVS changelog</a>, and the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/core/index.html">
  TM Core CVS changelog</a>.</li>
<li>For other questions, please check the
  <a href="http://wiki.eclipse.org/TM_and_RSE_FAQ">TM and RSE FAQ</a>
  as well as the
  <a href="http://wiki.eclipse.org/DSDP/TM/3.1_Known_Issues_and_Workarounds">
  TM 3.1 Known Issues and Workarounds</a>.</li>
</ul>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Getting Started</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
<p>The RSE User Documentation has a
<a href="http://dsdp.eclipse.org/help/latest/index.jsp?topic=/org.eclipse.rse.doc.user/gettingstarted/g_start.html">
Tutorial</a> that guides you through installation, first steps,
connection setup and important tasks.</p>
<p>
If you want to know more about future directions of the Target Management
Project, developer documents, architecture or how to get involved,
the online
<a href="http://www.eclipse.org/dsdp/tm/tutorial/index.php">Getting Started page</a>
as well as the
<a href="http://wiki.eclipse.org/TM_and_RSE_FAQ">TM and RSE FAQ</a>
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
<p>For the upcoming TM 3.1 release, only backward compatible API changes
are planned, especially in order to support improved componentization
and UI/Non-UI splitting.
In the interest of improving the code base, though, please 
take care of API marked as <b>@deprecated</b> in the Javadoc.
Such API is prime candidate to be removed in the future.
Also, observe the API Tooling tags such as <b>@noextend</b> and 
<b>@noimplement</b>.
</p>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#808080"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">API Specification Updates since TM 3.0</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
The following lists amendments to API specifications that are worth noticing,
and may require changes in client code even though they are binary compatible.
More information can be found in the associated bugzilla items.

<ul>
<li>TM @buildId@ API Specification Updates
<ul><li>Methods <code><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/IService.html#initService(org.eclipse.core.runtime.IProgressMonitor)">IService.initService()</a></code>
  and <code><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/core/subsystems/ISubSystem.html#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)">ISubSystem.initializeSubSystem</a></code>
  can now throw a <code>SystemMessageException</code> on error. As per the 
  <a href="http://wiki.eclipse.org/Evolving_Java-based_APIs_2#Evolving_API_interfaces_-_API_methods">Java API compatibility guide</a>,
  this is not strictly a change breaking binary compatibility, but it does break source compatibility.
  In other words, you'll need to update your sources when you recompile, though old compiled bundles
  will continue to run
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=272882">272882</a>].</li>
</ul></li>
<li>TM 3.1M6 API Specification Updates
<ul><li>None.</li>
</ul></li>
<li>TM 3.1M5 API Specification Updates
<ul>
<li>The <b>TerminalState.OPENED</b> state has been removed. This was provisional "internal" API,
  so this change does not constitute an official API change. Still, in order to properly
  support all update scenarios for consumers of the previous provisional API, the bundle
  <b>version of org.eclipse.tm.terminal has been moved up to 3.0.0</b>
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=262996">262996</a>].</li>
<li>The <b>SshShellService, SshHostShell, TelnetShellService, TelnetHostShell</b> and related 
  classes have been removed and replaced by a generic Terminal-to-HostShell adapter which can 
  re-use the functionality across the Telnet and SSH plugins.
  In order to ensure proper version computing on update, the following plugin versinos were changed:
  <ul>
    <li><b>org.eclipse.rse.services.ssh</b> changed from 2.1.0 to 3.0.0</li>
    <li><b>org.eclipse.rse.services.telnet</b> changed from 1.1.0 to 2.0.0</li>
  </ul>
  These changes do not indicate breaking public API since the removed classes were
  actually never API, but consumers should be aware of the probably unexpected
  major version bump, which was required due to the internal version dependency
  structure
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=261478">261478</a>].</li>
  </li>
</ul>
</li>
</ul>

Use 
  <!-- 
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bapi&classification=DSDP&product=Target+Management&component=Core&component=RSE&component=Terminal&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WORKSFORME&chfieldfrom=2008-06-20&chfieldto=2008-10-03&chfield=resolution&cmdtype=doit">
   -->
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bapi&classification=DSDP&product=Target+Management&component=Core&component=RSE&component=Terminal&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WORKSFORME&target_milestone=3.1+M2&target_milestone=3.1+M3&target_milestone=3.1+M4&target_milestone=3.1+M5&target_milestone=3.1+M6&target_milestone=3.1+M7&cmdtype=doit">
  this query</a> to show the full list of API related updates since TM 3.0
  <!--
  , and
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bapi%5D&classification=DSDP&product=Target+Management&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&cmdtype=doit">
  this query</a> to show the list of additional API changes proposed for TM 3.1
  -->
  .
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#0080C0"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">Known Problems and Workarounds</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
The following critical or major bugs are currently known.
We'll strive to fix these as soon as possible.
<ul>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=268463">bug 268463</a> - maj - [ssh] [sftp] SftpFileService.getFile(...) fails with cryptic jsch exception (4)</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=260796">bug 260796</a> - maj - [ftp] Fetching folder job sometimes runs forever with Outpost firewall</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=248913">bug 248913</a> - maj - [ssh] SSH subsystem loses connection</li> 
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=238156">bug 238156</a> - maj - Export/Import Connection doesn't create default filters for the specified connection</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=226564">bug 226564</a> - maj - [efs] Deadlock while starting dirty workspace
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=222380">bug 222380</a> - maj - [persistence][migration][team] Subsystem association is lost when creating connection with an installation that does not have subsystem impl</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=218387">bug 218387</a> - maj - [efs] Eclipse hangs on startup of a Workspace with a large efs-shared file system on a slow connection</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=208185">bug 208185</a> - maj - [terminal][serial] terminal can hang the UI when text is entered while the backend side is not reading characters</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=198395">bug 198395</a> - maj - [dstore] Can connect to DStore with expired password</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=175300">bug 175300</a> - maj - [performance] processes.shell.linux subsystem is slow over ssh</li>
</ul>
<!--
<p>No major or critical bugs are known at the time of release.
-->
Use 
<a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&bug_severity=blocker&bug_severity=critical&bug_severity=major&cmdtype=doit">this query</a>
for an up-to-date list of major or critical bugs.</p>

<p>The 
<a href="http://wiki.eclipse.org/TM_3.1_Known_Issues_and_Workarounds">
TM 3.1 Known Issues and Workarounds</a> Wiki page gives an up-to-date list
of the most frequent and obvious problems, and describes workarounds for them.<br/>
If you have other questions regarding TM or RSE, please check the
<a href="http://wiki.eclipse.org/TM_and_RSE_FAQ">TM and RSE FAQ</a>
</p>

<p>Click 
<a href="https://bugs.eclipse.org/bugs/report.cgi?x_axis_field=bug_severity&y_axis_field=op_sys&z_axis_field=&query_format=report-table&classification=DSDP&product=Target+Management&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&format=table&action=wrap">here</a>
for a complete up-to-date bugzilla status report, or
<a href="https://bugs.eclipse.org/bugs/report.cgi?x_axis_field=bug_severity&y_axis_field=op_sys&z_axis_field=&query_format=report-table&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&format=table&action=wrap">here</a>
for a report on bugs fixed so far.
</p>
</td></tr></tbody></table>

</body>
</html>
