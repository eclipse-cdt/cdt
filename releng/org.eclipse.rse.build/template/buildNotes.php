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
<li>TM @buildId@ <b>requires Eclipse 3.3 or later for the SSH component</b>.
  Other components may work with earlier Eclipse versions, but these have not been tested.
  Platform Runtime is the minimum requirement for core RSE and Terminal.
  Discovery needs EMF, and the RemoteCDT integration needs CDT.<br>
  <b>Building</b> the RSE SSH service requires <b>Eclipse 3.4M6</b> or later for the fix
  of <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=224799">bug 224799</a>;
  the fix also requires 3.4M6 at runtime, but the code contains a backward 
  compatibility fallback to also run on Eclipse 3.3 if that particular fix
  is not required.</li>
<li>Important Bug Fixes, Enhancements and API changes:<ul>
<li>The <b>RSE User Actions</b> framework is available as a new downloadable package
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=187395">187395</a>].</li>
<li>The <b>RAPI Library and Windows CE Subsystem</b> is available as a new downloadable
  package in Incubation status. Thanks to Radoslav Gerganov for contributing this new
  functionality
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=214887">214887</a>].</li>
<li><b>Tgz and tar.gz files</b> are now supported by the DSore and Local archive handlers.
  Thanks to Johnson Ma for contributing this new functionality
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=195402">195402</a>].</li>
<li>The optional <b>terminal input line is now resizeable</b>
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=196447">196447</a>].</li>
<li>Performance: <b>Fewer plugins are now activated</b> when RSE starts up, because the
  code that loads UI Adapters for the core services being used now loads those
  adapters more lazily. For some extenders of RSE, this might mean that they need
  to manually provide for loading their adapters when needed. For details, see the
  final comments on bug
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=218304">218304</a>].</li>
<li>API: <b>Streamed remote shell and Terminal access</b> is now supported by API. 
  <code>IAdaptable</code> is used to convert the old API into the new one. This
  will enable RSE Terminal integrations, and provide better performance and 
  consumability. At the same time, the RSE Services was cleaned up and made 
  implement <code>IAdaptable</code> in general
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=170910">170910</a>]
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=226262">226262</a>].</li>
<li>API: <b>RSE Early Startup and initialization</b> behavior was improved. The
  <code><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/extension-points/org_eclipse_rse_core_modelInitializers.html">org.eclipse.rse.core.modelInitializers</a></code>
  extension point was added for clients to register code that needs to be executed
  when the RSE Model is initialized. For clients, new API was added to
  <code><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/core/RSECorePlugin.html">RSECorePlugin</a></code>
  in order to
  <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/core/RSECorePlugin.html#isInitComplete(int)">query</a>
  when initialization is complete, or to 
  <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/core/RSECorePlugin.html#waitForInitCompletion(int)">wait</a>
  until initialization completes a given phase, or to get
  <a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/core/RSECorePlugin.html#addInitListener(org.eclipse.rse.core.IRSEInitListener)">notified</a>
  when initialization passes a given phase
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=197167">197167</a>].</li>
<li>API: <b>RSE SystemMessages</b> can now be constructed more easily with the new
  <code><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/clientserver/messages/SimpleSystemMessage.html">SimpleSystemMessage</a></code> API.
  Contents of these messages typically comes from
  standard Eclipse NLS property files, rather than the RSE-specific monolithic
  systemmessages.xml file. At the same time, messages have been refactored into
  non-UI plugins where possible, or the correct feature-specific plugins. This 
  accounts for better modularity and Platform integration;
  but it also means breaking API changes where clients had re-used RSE messages
  for themselves. Such re-use is now no longer supported  
  [<a href="https://bugs.eclipse.org/bugs/buglist.cgi?quicksearch=211067,216252,220309">211067,216252,220309</a>].</li>
<li>API: <b>RSE MOVE and COPY Events</b> now also contain source and destination objects,
  such that listeners can update data associated with moved remote objects
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=224313">224313</a>].</li>
<li>API: Added API to support running the dstore server in multi-threaded mode,
  where many clients can share a single remote process to save resources
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=220126">220126</a>].</li>
<li>API: The TerminalConnectorProxy class was removed, and replaced by
  an <code>IAdaptable</code> mechanism to get a concrete connector instance. This
  allows to programmatically create connections when a concrete connector instance
  is known, and will be further enhanced in the future
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=200541">200541</a>].</li>
<li>API: RSE FTP Listing Parsers can now contribute custom commands to send on
  connect. This enables connecting IBM System/i (OS400) FTP in IFS mode
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=212382">212382</a>].</li>
</ul></li>
<li>At least 100 bugs were fixed: Use 
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&component=Core&component=RSE&component=Terminal&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=WORKSFORME&chfieldfrom=2008-02-19&chfieldto=2008-04-12&chfield=resolution&cmdtype=doit&negate0=1&field0-0-0=target_milestone&type0-0-0=substring&value0-0-0=2.0.&field0-0-1=target_milestone&type0-0-1=regexp&value0-0-1=3.0%20M%5B3457%5D">
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&component=Core&component=RSE&component=Terminal&target_milestone=3.0+M6&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=WORKSFORME&cmdtype=doit"> -->
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
<li><b>RSE UI Adapter Loading</b> has been made more lazy. This means, that contributors
  of RSE subsystems, which provide core services and UI adapters in separate plugins, may
  need to take care of loading their adapters at the right time. RSE does provide for 
  automatica adapter loading when a subsystem gets connected, but any adapter functionality
  that's needed before that time needs to be provided by the client. For details, see the
  final comments on bug
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=218304">218304</a>].</li>
<li>Several <b>SystemMessages and Shared Resource Strings</b> have been moved to different packages in order
    to allow better integration with other Eclipse projects and better UI/Non-UI splitting.
    New <code><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/clientserver/messages/SimpleSystemMessage.html">SimpleSystemMessage</a></code> class has been added to create System Messages out 
    of standard Eclipse NLS Strings. A list of related breaking API changes is attached to bugs
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=216252">216252</a>]
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=220309">220309</a>].</li>
<li><b>Adaptable Services</b>: All RSE Services must now extend <code><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/services/package-summary.html">AbstractService</a></code>
  rather than implementing the Service interface directly, in order to make the Service
  adaptable
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=226262">226262</a>].</li>
<li><b>ISystemNewConnectionWizardPage</b> was moved from Core to non-UI, and replaced
  by a non-UI base class named <code><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/core/model/ISubSystemConfigurator.html">ISubSystemConfigurator</a></code> in non-UI.
  Contributed Wizard Pages should use the new API in order to support configuring 
  subsystems without bringing in unnecessary UI dependencies
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=168976">168976</a>].</li>
<li><b>SystemFileTransferModeRegistry</b> has been moved to internal class. <code>ISystemFileTransferModeRegistry</code> can now
    be accessed by calling new API <code><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/api/org/eclipse/rse/subsystems/files/core/model/RemoteFileUtility.html#getSystemFileTransferModeRegistry()">RemoteFileUtility.getSystemFileTransferModeRegistry()</a></code>
    instead
    [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=220020">220020</a>].</li>
<li>Some deprecated or not correctly working methods have been removed but should not have
    been used by any clients anyways
    [<a href="https://bugs.eclipse.org/bugs/buglist.cgi?quicksearch=219975,221138,220041,223126">219975,220041,223126</a>].</li>
<li>Some less relevant breaking API changes, mostly for cleaning up API, have been made.
    See the bug reports if you find that your code doesn't compile any more against RSE
    3.0M6 and you find that not even an "organize imports" operation helps:
    [<a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bbreaking%5D&product=Target+Management&component=Core&component=RSE&component=Terminal&resolution=FIXED&chfieldfrom=2008-02-19&chfieldto=2008-04-12&chfield=resolution&chfieldvalue=&cmdtype=doit&negate0=1&field0-0-0=target_milestone&type0-0-0=substring&value0-0-0=2.0.&field0-0-1=target_milestone&type0-0-1=regexp&value0-0-1=3.0+M%5B3457%5D&field0-0-2=bug_id&type0-0-2=anyexact&value0-0-2=168976%2C220020%2C216252%2C220309%2C219975%2C220041%2C223126%2C218304%2C221138%2C226262">query bugzilla</a>].</li>
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
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=197027">bug 197027</a> - maj - [persistence] Can lose data if close Eclipse before saving profile completes</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=219934">bug 219934</a> - maj - [regression][dnd] Cannot Copy & Paste / Drag&Drop remote to Resource Navigator</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=222380">bug 222380</a> - maj - [persistence][migration][team] Subsystem association is lost when creating connection with an installation that does not have subsystem impl</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=226727">bug 226727</a> - maj - Remote search results in ConcurrentModificationException</li>
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=225573">bug 225573</a> - maj - [dstore] client not falling back to single operation when missing batch descriptors (due to old server)</li>
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
