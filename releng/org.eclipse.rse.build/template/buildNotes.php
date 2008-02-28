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
<li>API: Several <b>SystemMessages and Shared Resource Strings</b> have been moved
  from RSEUIPlugin to non-UI
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=216252">216252</a>].</li>
</ul></li>
<li>At least ?? bugs were fixed: Use 
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&chfieldfrom=2008-02-19&chfieldto=2008-04-07&chfield=resolution&cmdtype=doit&negate0=1&field0-0-0=target_milestone&type0-0-0=substring&value0-0-0=2.0.&field0-0-1=target_milestone&type0-0-1=regexp&value0-0-1=3.0%20M%5B345%5D">
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&target_milestone=3.0+M6&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&cmdtype=doit"> -->
  this query</a> to show the list of bugs fixed since the last milestone,
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-3.0M5-200802181400/">
  TM 3.0M5</a>
  [<a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-3.0M5-200802181400/buildNotes.php">build notes</a>].</li>
<li>For details on checkins, see
  <a href="http://www.eclipse.org/dsdp/tm/searchcvs.php">TM SearchCVS</a>, the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/index.html">
  RSE CVS changelog</a>, and the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/core/index.html">
  TM Core CVS changelog</a>.</li>
<li>For other questions, please check the
  <a href="http://wiki.eclipse.org/TM_and_RSE_FAQ">TM and RSE FAQ</a>
  as well as the
  <a href="http://wiki.eclipse.org/TM_3.0_Known_Issues_and_Workarounds">
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
<li>TM @buildId@ Breaking API Changes [<a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&product=Target+Management&target_milestone=3.0+M6&resolution=FIXED&keywords_type=allwords&keywords=api&cmdtype=doit">query</a>]
<ul>
<li><b>SystemFileTransferModeRegistry</b> has been moved to internal class. <code>ISystemFileTransferModeRegistry</code> can now
    be accessed by calling new API <code>RemoteFileUtility.getSystemFileTransferModeRegistry()</code>
    instead
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=220020">220020</a>].</li>
<li>Several <b>SystemMessages and Shared Resource Strings</b> have been moved to different packages in order
    to allow better integration with other Eclipse projects and better UI/Non-UI splitting. A list of related
    breaking API changes is attached to bug
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=216252">216252</a>].</li>
<li>Some deprecated or not correctly working methods have been removed but should not have
    been used by any clients anyways
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=219975">219975</a>]
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=220041">220041</a>].</li>
</ul></li>
<li>TM 3.0M5 Breaking API Changes [<a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&product=Target+Management&target_milestone=3.0+M5&resolution=FIXED&keywords_type=allwords&keywords=api&cmdtype=doit">query</a>]
<ul>
<li><b>Removed</b> <tt>IServiceSubSystem</tt> and related types in order to simplify the code, and allow better lazy initialization.
    <tt>ISubSystem.getServiceType()</tt> is now used to know whether a given subsystem
    is based on a service or not. Implementers of IServiceSubSystem need to implement ISubSystem now; code that tested for <tt>instanceof IServiceSubSystem</tt>
    needs to use the dynamic check now
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=217556">217556</a>].</li>
<li><b>Removed</b> <tt>ISystemProfile#createHost(IRSESystemType, String, String, String)</tt>. Deprecated some other methods related
    to filter or host creation, in order to support lazy initialization of filter pools.
    Replacement methods are mentioned in the deprecation text
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=197036">197036</a>].</li>
<li>Made the <b>TerminalConnectorId mandatory</b> in terminal connector plugin.xml,
    because it is essentially API allowing to talk to a given connector
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=199285">199285</a>].</li>
<li><b>Removed</b> deprecated <tt>ISystemViewInputProvider#getShell()</tt> 
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=218524">218524</a>].</li>
<li><b>Moved</b> some methods from <tt>ISystemRegistry</tt> into <tt>ISystemRegistryUI</tt>
    in order to facilitate moving SystemRegistry implementation to non-UI. Making this change
    also required <b>adding an SWT dependency for rse.core</b>, which we hope to get rid
    again later. The SystemRegistry logfile can now be found in the <tt>rse.core</tt> plugin rather
    than the <tt>rse.ui</tt> plugin
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=215820">215820</a>].</li>
<li><b>IRSESystemType.isEnabled()</b> has been added instead of <tt>RSESystemTypeAdapter.isEnabled()</tt>,
    in order to provide enablement info to non-UI plugins as well. The adapter method has been made 
    final in order to warn extenders that they need to move their code to non-UI.
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=218655">218655</a>].</li>
</ul></li>
<li>TM 3.0M4 Breaking API Changes
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
</ul></li>
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
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=218387">bug 218387</a> - maj - [efs] Eclipse hangs on startup of a Workspace with a large efs-shared file system on a slow connection</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=208185">bug 208185</a> - maj - [terminal][serial] terminal can hang the UI when text is entered while the backend side is not reading characters</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=198395">bug 198395</a> - maj - [dstore] Can connect to DStore with expired password</li>
</ul>
<!--
<p>No major or critical bugs are known at the time of release.
-->
Use 
<a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&bug_severity=blocker&bug_severity=critical&bug_severity=major&cmdtype=doit">this query</a>
for an up-to-date list of major or critical bugs.</p>

<p>The 
<a href="http://wiki.eclipse.org/TM_3.0_Known_Issues_and_Workarounds">
TM 3.0 Known Issues and Workarounds</a> Wiki page gives an up-to-date list
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
