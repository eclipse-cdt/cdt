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
<li>TM @buildId@ <b>requires Eclipse 3.3 later for the SSH component</b>.
  Other components may work with earlier Eclipse versions, but these have not been tested.
  Platform Runtime is the minimum requirement for core RSE and Terminal.
  Discovery needs EMF, and the RemoteCDT integration needs CDT.</li>
<li>Important Bug Fixes, Enhancements and API changes:<ul>
<li>On FTP, <b>delete now works recursively</b> like for the other IFileService implementations
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=196351">196351</a>].</li>
<li>API: New <b>listMultiple()</b>, <b>getFiles()</b>, <b>uploadMulti()</b>, <b>downloadMulti()</b> API has been 
  added to <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/files/IFileService.html"><tt>IFileService</tt></a>.
  This allows for optimized file service implementations which avoid
  unnecessary client/server round trips. Currently, only dstore makes use of the new API; API changes have
  been made in a backward compatible manner, as long as implementations derive from 
  <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/files/AbstractFileService.html"><tt>AbstractFileService</tt></a>
  rather than implementing <tt>IFileService</tt> directly
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=207178">207178</a>]
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=162195">162195</a>].</li>
<li>API: The <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/extension-points/org_eclipse_rse_ui_mountPathMappers.html"><b>mountPathMappers</b></a>
  extension point has been generalized to support cache mappings that take additional connection properties 
  like the port or user name into account. To facilitate this,
  <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/files/ui/resources/ISystemMountPathMapper.html"><tt>ISystemMountPathMapper</a>#getWorkspaceMappingFor()</tt>
  has been changed to accept an additional parameter of type 
  <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/subsystems/files/core/subsystems/IRemoteFileSubSystem.html"><tt>IRemoteFileSubSystem</tt></a>.
  This will help fixing issues like 
  <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=193858">bug 193858</a> in the future
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=195285">195285</a>].</li>
<li>API: Listeners can now subscribe to events for completed file uploads and downloads
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=207100">207100</a>].</li>
<li>API: Added an <tt><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/ui/view/ISystemViewElementAdapter.html">ISystemViewElementAdapter</a>#exists()</tt>
  method in order to avoid queries on the dispatch thread in the future
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=208803">208803</a>].</li>
</ul></li>
<li>All bugs from the TM 2.0.1 and 2.0.2 maintenance releases were merged into this milestone.
  At least 40 additional bugs were fixed: Use 
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&chfieldfrom=2007-09-29&chfieldto=2007-11-14&chfield=resolution&cmdtype=doit"> -->
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&chfieldfrom=2007-09-29&chfieldto=2007-11-14&chfield=resolution&cmdtype=doit&negate0=1&field0-0-0=target_milestone&type0-0-0=substring&value0-0-0=2.0.&field0-0-1=target_milestone&type0-0-1=substring&value0-0-1=3.0+M4"> -->
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&target_milestone=3.0+M3&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&cmdtype=doit">
  this query</a> to show the list of bugs fixed since the last milestone,
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/R-2.0.2-200711131300/index.php">
  TM 2.0.2</a>
  [<a href="http://download.eclipse.org/dsdp/tm/downloads/drops/R-2.0.2-200711131300/buildNotes.php">build notes</a>].</li>
<li>For details on checkins, see the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/index.html">
  RSE CVS changelog</a>, and the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/core/index.html">
  TM Core CVS changelog</a>.</li>
<li>For other questions, please check the
  <a href="http://wiki.eclipse.org/index.php/TM_and_RSE_FAQ">TM and RSE FAQ</a>
  as well as the
  <a href="http://wiki.eclipse.org/index.php/TM_3.0_Known_Issues_and_Workarounds">
  TM 3.0 Known Issues and Workarounds</a>.</li>
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
<p>For the upcoming TM 3.0 release, some API changes will be inevitable,
especially in order to support improved componentization and UI/Non-UI splitting.
Although we completed a great deal of API cleanup for TM 2.0, we decided
to still mark all API as <i>provisional</i> since we expect more work to do.
If anyhow possible, we will avoid breaking API changes after TM 2.0, but please 
be prepared for future changes, and especially take care of API marked as 
<b>@deprecated</b> in the Javadoc.
Such API is prime candidate to be removed in the future. All
API changes will be voted by committers on the 
<a href="https://dev.eclipse.org/mailman/listinfo/dsdp-tm-dev">
dsdp-tm-dev</a> developer mailing list, and documented in a migration guide
for future releases. Early migration information can also be found right
in the bug reports. Look for those that are tagged [api][breaking].</p>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#808080"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">API Changes since TM 2.0 - newest changest first</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
The following lists those API changes that are not backward compatible and require
user attention. A short hint on what needs to change is given directly in the list.
More information can be found in the associated bugzilla items.

<ul>
<li>TM @buildId@ Breaking API Changes
<ul>
<!-- <li>None</li> -->
<li><b>Optimized IFileService for multi-queries</b>. This is not a breaking API change for clients
    that extend <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/files/AbstractFileService.html"><tt>AbstractFileService</tt></a>
    rather than implementing IFileService directly. But the
    now deprecated methods <tt>getFiles()</tt>, <tt>getFolders()</tt> and <tt>getFilesAndFolders()</tt>
    may be removed soon in favor of the new list() API
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=207178">207178</a>].</li>
<li><b>Changed ISubSystem#checkIsConnected()</b> to accept an IProgressMonitor argument
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=207095">207095</a>].</li>
<li><b>Changed the <tt>mountPathMappers</tt> extension point:</b>
  <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/files/ui/resources/ISystemMountPathMapper.html"><tt>ISystemMountPathMapper</a>#getWorkspaceMappingFor()</tt></b>
  has been changed to accept an additional parameter of type 
  <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/subsystems/files/core/subsystems/IRemoteFileSubSystem.html"><tt>IRemoteFileSubSystem</tt></a>
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=195285">195285</a>].</li>
<li><b>Removed some deprecated APIs:</b>
  <ul><li>ISubSystem#connect() API without progress or callback
      [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186363">186363</a>].</li>
      <li>obsolete SystemSelectConnection* classes
      [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=196938">196938</a>].</li>
      <li>obsolete classes ISystemConnectionWizardPropertyPage and SystemSubSystemsPropertiesWizardPage
      [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=197129">197129</a>].</li>
      <li>obsolete methods in IRemoteCmdSubSystem and IRemoteSystemEnvVar
      [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=208813">208813</a>].</li>
  </ul>
    <!-- 
    See the respective bug reports for migration.
    <a href="https://bugs.eclipse.org/bugs/buglist.cgi?quicksearch=186363,196938,208813">186363,196938,208813</a>]
    -->
  </li>
</ul>
</li>
</ul>

Use 
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bapi%5D&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WORKSFORME&chfieldfrom=2007-06-28&chfieldto=2008-07-01&chfield=resolution&cmdtype=doit"> -->
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bapi%5D&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WORKSFORME&target_milestone=3.0+M3&cmdtype=doit">
  this query</a> to show the full list of API changes since TM 2.0, and
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bapi%5D&classification=DSDP&product=Target+Management&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&cmdtype=doit">
  this query</a> to show the list of additional API changes proposed for TM 3.0.
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
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=198143">bug 198143</a> - maj - [dstore][performance] Refresh a big directory takes very long time, and freezes workbench</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=198395">bug 198395</a> - maj - [dstore] Can connect to DStore with expired password</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=203501">bug 203501</a> - maj - NPE in PFMetadataLocation when saving RSEDOM</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=207308">bug 207308</a> - maj - Removing a file type should not delete the platform's file association to editors</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=208778">bug 208778</a> - maj - [efs][api] RSEFileStore#getOutputStream() does not support EFS#APPEND</li>
</ul>
<!--
<p>No major or critical bugs are known at the time of release.
-->
Use 
<a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&bug_severity=blocker&bug_severity=critical&bug_severity=major&cmdtype=doit">this query</a>
for an up-to-date list of major or critical bugs.</p>

<p>The 
<a href="http://wiki.eclipse.org/index.php/TM_3.0_Known_Issues_and_Workarounds">
TM 3.0 Known Issues and Workarounds</a> Wiki page gives an up-to-date list
of the most frequent and obvious problems, and describes workarounds for them.<br/>
If you have other questions regarding TM or RSE, please check the
<a href="http://wiki.eclipse.org/index.php/TM_and_RSE_FAQ">TM and RSE FAQ</a>
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
