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
<li>API: A new <b><a href="http://dsdp.eclipse.org/help/latest/index.jsp?topic=/org.eclipse.rse.doc.isv/reference/extension-points/org_eclipse_rse_services_codePageConverters.html">
  codePageConverters</a></b> extension point has been added to support services
  that need to perform special additional conversions on encodings
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=209704">209704</a>].</li>
<li><b>Archive Handling</b> has been improved to run outside the dispatch thread on the
  Local and DStore subsystems. Several API changes were made to support a cancelable
  progress monitor with the archive operations
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=160775">160775</a>].</li>
<li>The <b>File Transfer Mode Preference Page</b> has been improved for better usability.
  Transfer mode preferences are now decoupled from Eclipse Platform Editor File Types
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=203114">203114</a>];
  Upload and Download do honor the preference such that in text mode, encoding conversions
  are performed to ensure that remote text files are always locally stored in the local
  default encoding. This allows for external tools to easily perform operations on remote
  files which are in uncommon encodings
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=210812">210812</a>].</li>
<li>API: The <b><a href="http://dsdp.eclipse.org/help/latest/index.jsp?topic=/org.eclipse.rse.doc.isv/reference/extension-points/org_eclipse_rse_subsystems_files_core_remoteFileTypes.html">
  remoteFileTypes</a></b> extension point has been added to specify the default file transfer mode
  (binary/ascii) for a specific file type
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=208951">208951</a>].</li>
<li>Remote file encoding now defaults to the parent folder encoding
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=209660">209660</a>].</li>
<li>API: The <b>Select Input Dialog</b> now supports setting a viewer filter such that unwanted kinds of items
  can not e selected [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=187543">187543</a>].</li>
<li>API: The new <b>uploadMulti()</b>, <b>downloadMulti()</b> API has been renamed in  
  <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/files/IFileService.html"><tt>IFileService</tt></a>.
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=162195">162195</a>]. 
  Also, the new multi-API is now being leveraged in <tt>UniversalFileTransferUtility</tt>
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=209375">209375</a>].</li>
</ul></li>
<li>At least 37 bugs were fixed: Use 
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&chfieldfrom=2007-11-06&chfieldto=2008-01-07&chfield=resolution&cmdtype=doit"> -->
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&chfieldfrom=2007-11-06&chfieldto=2008-01-07&chfield=resolution&cmdtype=doit&negate0=1&field0-0-0=target_milestone&type0-0-0=substring&value0-0-0=2.0.&field0-0-1=target_milestone&type0-0-1=substring&value0-0-1=3.0+M3">
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&target_milestone=3.0+M4&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&cmdtype=doit"> -->
  this query</a> to show the list of bugs fixed since the last milestone,
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-3.0M3-200711141025/index.php">
  TM 3.0M3</a>
  [<a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-3.0M3-200711141025/buildNotes.php">build notes</a>].</li>
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
<li><b><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/core/subsystems/IRemoteObjectResolver.html#getObjectWithAbsoluteName(java.lang.String,%20org.eclipse.core.runtime.IProgressMonitor)">
    IRemoteObjectResolver.getObjectWithAbsoluteName()</a></b>
    now takes an additional <tt>IProgressMonitor</tt> parameter, in order to support cancellation of deferred queries.
    The old method has been deprecated and will be removed for 3.0. Especially custom <b>Subsystem</b> implementations will need to 
    be changed to implement the new method instead of the old one
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=211472">211472</a>].</li>
<li><b><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/files/IFileService.html#getOutputStream(java.lang.String,%20java.lang.String,%20boolean,%20int,%20org.eclipse.core.runtime.IProgressMonitor)">
    IFileService.getOutputStream()</a></b>
    now takes an additional <tt>int options</tt> parameter, in order to support opening streams which append
    to existing files. This was required in order to properly fulfill the EFS APIs. The corresponding old
    method has been deprecated and will be removed for 3.0. Custom File Service implementations should be
    changed to implement the new method instead of the old one
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=208778">208778</a>].</li>
<li><b>Removed</b> the now obsolete IFileService.list(...) methods in favor of the new API
    from <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=207178">bug 207178</a>.
    Clients of IRemoteFileSubSystem and IFileService need to be changed, though the change
    is simple and compiler will mark error positions
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=209552">209552</a>].</li>
<li><b>Archive Handler API</b> has been changed to support background operation and cancellation.
    To facilitate this, an additional ISystemOperationMonitor interface was added as last parameter
    of most method calls in 
    <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/clientserver/archiveutils/ISystemArchiveHandler.html"><tt>ISystemArchiveHandler</tt></a>. In addition to that,
    <tt><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/clientserver/archiveutils/VirtualChild.html">VirtualChild</a>#getExtractedFile()</tt> was also changed
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=160775">160775</a>].</li>
<li><b>Removed</b> obsolete method
    <tt><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/ui/RSESystemTypeAdapter.html">RSESystemTypeAdapter</a>#acceptContextMenuActionContribution()</tt>
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=199032">199032</a>].</li>
</ul>
<li>TM 3.0M3 Breaking API Changes
<ul>
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
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=208185">bug 208185</a> - maj - [terminal][serial] terminal can hang the UI when text is entered while the backend side is not reading characters</li>
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
