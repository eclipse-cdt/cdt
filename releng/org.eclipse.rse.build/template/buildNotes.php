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
<li>TM @buildId@ <b>requires Eclipse 3.3RC3 or later for the SSH component</b>.
  Other components should work with Eclipse 3.3M6 or later.
  Platform Runtime is the minimum requirement for core RSE and Terminal.
  Discovery needs EMF, and the Remotecdt integration needs CDT.</li>
<li>If you use the dstore server, the version number has been updated. You should use 
  the new server with this build.</li>
<!--
<li><b>Split the Terminal Telnet connector from the core Terminal widget</b>.
  This allows embedding a terminal widget in RCPs without having the unnecessary
  code for the Telnet connector
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=174162">174162</a>].</li>
<li><b>Apache Commons.Net and ORO</b> are now distributed as verbatim compies
  from the Orbit project, so they will not be changed any more.</li>
-->
<li>At least 2 API changes were made and 25 bugs were resolved: use 
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&chfieldfrom=2007-06-06&chfieldto=2007-06-14&chfield=resolution&cmdtype=doit"> -->
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&target_milestone=2.0+RC3&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&cmdtype=doit">
  this query</a> to show the list of bugs fixed since the last milestone,
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-2.0RC2-200706051715/index.php">
  TM 2.0RC2</a>
  [<a href="http://download.eclipse.org/dsdp/tm/downloads/drops/S-2.0RC2-200706051715/buildNotes.php">build notes</a>].</li>
<li>For details on checkins, see the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/index.html">
  RSE CVS changelog</a>, and the
  <a href="http://download.eclipse.org/dsdp/tm/downloads/drops/N-changelog/core/index.html">
  TM Core CVS changelog</a>.</li>
<li>For other questions, please check the
  <a href="http://wiki.eclipse.org/index.php/TM_and_RSE_FAQ">TM and RSE FAQ</a>
  as well as the
  <a href="http://wiki.eclipse.org/index.php/TM_2.0_Known_Issues_and_Workarounds">
  TM 2.0 Known Issues and Workarounds</a>.</li>
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
<p>An important part of the <a href="http://www.eclipse.org/dsdp/tm/development/tm_project_plan_2_0.html">Target Management 2.0 Project Plan</a>
is to harden the APIs which were provisional by RSE 1.0. Naturally, this requires
API changes, and especially moving lots of classes which we cannot guarantee to 
support in the future into internal packages.</p> 
<p>As of TM 2.0 M6, most of this work has been completed, and the list of breaking
API changes is found below with migration info. But although we had planned for
API freeze with M6, there are still few more cleanup changes that we would like
to take the opportunity and bring into TM 2.0. Most of these will be made shortly
after M6, or they will be introduced in a backward compatible manner.
At any rate, we will avoid breaking API changes after M7, or the earliest 
possible integration build up to M7. But please be prepared for future changes,
and especially take care of API marked as <b>@deprecated</b> in the Javadoc.
Such API is prime candidate to be removed for TM 2.0.</p>
<p><b>Use <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bapi%5D&classification=DSDP&product=Target+Management&target_milestone=2.0&target_milestone=2.0+M7&target_milestone=2.0+RC1&target_milestone=2.0+RC2&target_milestone=2.0+RC3&cmdtype=doit">
this query</a> to show the list of API changes planned or done after M6</b>. All
such API changes are voted by committers on the 
<a href="https://dev.eclipse.org/mailman/listinfo/dsdp-tm-dev">
dsdp-tm-dev</a> developer mailing list, and documented in a migration guide
for future releases. Early migration information can also be found right
in the bug reports. Look for those that are tagged [api][breaking].</p>
</td></tr></tbody></table>

<table border="0" cellspacing="5" cellpadding="2" width="100%">
	<tr>
		<td align="LEFT" valign="TOP" colspan="3" bgcolor="#808080"><b>
		<font face="Arial,Helvetica" color="#FFFFFF">API Changes since RSE 1.0.1 - newest changest first</font></b></td>
	</tr>
</table>
<table><tbody><tr><td>
The following lists those API changes that are not backward compatible and require
user attention. A short hint on what needs to change is given directly in the list.
More information can be found in the associated bugzilla items.

<ul>
<li>TM @buildId@ Breaking API Changes
<ul>
<li><b>ISystemResourceManager</b> - removed two methods getRemoteSystemsProject() 
  and getProfileFolder(String).
  The method getRemoteSystemsProject() was a convenience method that was 
  equivalent to getRemoteSystemsProject(true) and it forced the
  creation of RemoteSystemConnections project in the workspace.
  This was undesirable given the current persistence provider defaults.
  The method getProfileFolder(String) returned a IFolder for a profile under the 
  old persistence provider scheme. The method was obsolete and
  should not be being used at all.
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=191130">191130</a>]
<li><b>The version number of the dstore server was updated from 8.0.0 to 9.0.0</b> - running this build against an
  older dstore server will likely work, but will receive an incompatibility message on connection.
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=188365">188365</a>]
</li> 
</ul></li>
<li>TM 2.0RC2 Breaking API Changes
<ul>
<li><b>Cleaned up ISystemRegistry and ISystemProfileManager</b> - removed several convenience methods 
  that are easily accessible through more common methods. If your code does
  not compile any more because it is missing methods in ISystemRegistry,
  browse RSE looking for alternative methods to do what you need
  [<a href="https://bugs.eclipse.org/bugs/showdependencytree.cgi?id=175680">175680</a>]
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=189749">189749</a>].</li>
<li><b>Removed FileSelectionDialog, SystemFilterTableDialog</b> and related obsolete code.
  For migration, use SystemRemoteFileDialog instead
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=189973">189973</a>]
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=189508">189508</a>].</li>
<li><b>Removed icons, messages, constants and methods referring to User Actions</b>.
  Since user actions are not shipped with TM 2.0, these items would not have been
  functional and have thus been removed. For migration, just remove corresponding
  usage of API in your code 
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186589">186589</a>].</li>
<li><b>Removed showActionBar() and showButtonBar() in ISystemViewInputProvider</b>,
  and <b>moved ISystemViewInputProvider to non-UI</b>. Showing of action and button
  bars is now governed by the dialogs or views using a SystemView component.
  For migration, just "organize imports" and get rid of the deprecated methods in
  ISystemViewInputProvider 
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=189506">189506</a>]
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=190271">190271</a>].</li>
<li><b>Moved the Terminal Telnet connector into a separate plugin</b>
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=174162">174162</a>].</li>
</ul></li>
<li>TM 2.0RC1 Breaking API Changes
<ul>
<li><b>Created API for default encoding of a connection on the IHost level</b>.
  Existing <code>IRemoteCmdSubSystem.getEncoding()</code> is to be replaced by
  <code>IRemoteCmdSubSystem.getHost().getDefaultEncoding(true)</code>.
  For details, see bug 
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=179937">179937</a>].</li> 
<li><b>Moved RSESystemTypeAdapter.createNewHostInstance() to IRSESystemType.createNewHostInstance()</b>.
  This was necessary in order to support moving SystemHostPool from UI to Core,
  which is a blocking prerequisite for future enhanced UI/Non-UI separation.
  Migration: Extenders of RSESystemTypeAdapter will need to change their code
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=176211">176211</a>].</li>
<li><b>ISystemRegistry.fireEvent() now switches to the Display thread automatically.</b>
  This may be relevant for event listeners to know
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=176601">176601</a>].</li>
<li><b>Moved SystemIFileProperties from UI to Core</b>, in preparation of moving
  SystemRegistry into non-UI in the future.
  For migration, just organize imports
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=189130">189130</a>].</li>
<li><b>Moved three methods from ISystemRegistry into ISystemRegistryUI</b>
  in preparation of moving SystemRegistry into non-UI in the future:
  <code>showRSEPerspective(), expandHost(), expandSubSystem()</code> are now
  UI-only methods. Note that for non-UI expand, an event exists in 
  ISystemResourceChangeEvents.EVENT_SELECT_EXPAND. Migration notes: use the 
  methods from their new home. For details, see bug
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=189123">189123</a>].</li>
<li><b>Moved one method 
  from ISubSystemConfigurationAdapter into ISubSystemConfiguration</b>
  in preparation of moving SystemRegistry into non-UI in the future:
  <code>renameSubSystemProfile()</code>. Migration notes: use the method
  from its new home. Override renameSubSystemProfile() in its new home.
  For details, see bug
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=189123">189123</a>].</li>
<li><b>Renamed subsystemFactory to subsystemConfiguration in IActionFilter</b>
  implementation of RSE model objects, which is being used in plugin.xml
  contributed popupMenu and propertyPage extensions  
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=189163">189163</a>].</li>
<li><b>Renamed plugin org.eclipse.rse.eclipse.filesystem</b> to org.eclipse.rse.efs.ui
  and org.eclipse.rse.efs respectively, in preparation of future UI/Non-UI split and 
  making the name better understandable
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=188360">188360</a>]
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186320">186320</a>].</li>
</ul></li>
<li>TM 2.0M7 Breaking API Changes
<ul>
<li><b>Moved some more classes</b>, like the dstore miners, ISubSystemConfigurationAdapter,
  or Terminal API classes (which have been marked provisional).
  These changes can simply be picked up by running "organize imports" on client code
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=180649">180649</a>] 
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186748">186748</a>] 
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186134">186134</a>].</li> 
<li><b>Moved the subsystemConfigurations and keystoreProviders extension points</b>
  to org.eclipse.rse.core in order to better support UI/non-UI separation
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186523">186523</a>] 
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186525">186525</a>].</li> 
<li><b>Removed the remoteSystemsViewPreferencesActions and
  dynamicPopupMenuExtensions extension points</b>
  since the same can now be achieved with standard org.eclipse.ui extension points 
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=185552">185552</a>] 
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=185554">185554</a>].</li> 
<li>Fixed incorrect usage of <b>IRSESystemType.getAdapter()</b>. This change is
  important to <b>consider manually since it does not break the build</b> - 
  see the bug entry for details
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186779">186779</a>].</li> 
<li>Made <b>IRemoteFileSubSystem forward exceptions</b> rather than handling itself
  by opening dialogs
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=183824">183824</a>].</li> 
<li><b>Unify Singleton Access Methods</b> to use getInstance() in most cases
  in order to achieve better API consistency
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=177523">177523</a>].</li> 
<li><b>Added IProgressMonitor parameter</b> to all long-running IRemoteFileSubSystem
  methods. Clients need to pass in a NullProgressMonitor in all these methods now.
  Moved the <b>IProgressMonitor parameter last</b> in all methods for consistency.
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=184322">184322</a>]
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186128">186128</a>].</li>
<li><b>Use IRSESystemType</b> instead of String systemTypeName everywhere -
  this is the prerequisite for allowing system type labels to be translated
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=180688">180688</a>].</li>
<li><b>Move SysteRegistry's Event Interface to non-UI</b> and split ISystemRegistryUI
  from ISystemRegistry - See the bug entries for migration docs
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=168975">168975</a>]
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=186773">186773</a>].</li>
<li><b>Dont implement interfaces just for Constants</b> - See the bug 
  entry for migration docs
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=183165">183165</a>].</li>
</ul></li>
<li>TM 2.0M6a Breaking API Changes
<ul>
<li><b>Making more classes "internal"</b>: Lots of more packages and classes were tagged as
  discouraged access (moved to "internal").
  For migration, just "Organize Imports" and search for alternative ways of doing things
  in case you end up with imports from "internal" packages.
  For details, see [<a href="https://bugs.eclipse.org/bugs/showdependencytree.cgi?id=170922">170922</a>].</li>
<li><b>Refactored IConnectorService</b> and moved to Core for beter UI / Non-UI separation.
  Most notably, the concept of a CredentialsProvider was added.
  That work is not quite complete yet, but the bulk of the work has been done. For detailed
  migration info, see [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=168977">168977</a>].</li>
<li><b>Renamed getAdapter(Object) -&gt; getViewAdapter(Object)</b> on several classes.
  For migration, same renaming may need to be done in client code.
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=175150">175150</a>].</li>
<li><b>Dont implement constant interfaces</b> just to bring constants into namespace.
  As an effect of this, some classes that clients can derive from do not "see" some constants any more. For
  migration, these constants will need to be qualified with the interface that they come from.
  For details, see 
  [<a href="https://bugs.eclipse.org/bugs/showdependencytree.cgi?id=180562">180562</a>].</li>
<li><b>Streams are now part of IFileService and IRemoteFileSubSystem</b>. Supporting Streams was
  important for the EFS integration and is a more modern and flexible way to download and upload.
  Thus Streams are now mandatory, and extenders must ensure that they implement getInputStream()
  and getOutputStrem() properly in their own file services and subsystems.
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=162954">162954</a>] and
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=179850">179850</a>].</li>
<li><b>IHost.getSystemType() now returns an IRSESystemType</b> instead of a String.
  More similar changes will follow, in order to move from system type Strings to
  IDs and translatable / externalizable labels. For migration, client code can
  replace <tt>getSystemType()</tt> by <tt>getSystemType().getName()</tt>.
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=175262">175262</a>].</li>
<li><b>Support Menu Configuration in RSESystemTypeAdapter</b> [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=161195">161195</a>].</li>
<li><b>The RSE Communications Daemon has been removed</b> since it was never quite properly
  supported in Open Source [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=180602">180602</a>].</li>
<li><b>The org.eclipse.rse.logging</b> plugin has been removed, functionality is now
  in the core and UI plugins, respectively
  [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=170920">170920</a>].</li>
</ul></li>
<li>TM 2.0M5 Breaking API Changes
<ul>
<li><b>Modified Extension Point subsystemConfigurations</b> to reference system types by ID rather than
by name. Client code needs to use the <b><i>systemTypeIds</i></b> tag now rather than <samp>systemTypes</samp>
[<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=162081">162081</a>].</li>
<li><b>Removed Extension Points for Popup Menus and Property Pages:</b> The RSE-specific extension points
<samp>org.eclipse.rse.ui.propertyPages</samp> and <samp>org.eclipse.rse.ui.popupMenus</samp> were removed
because newer Eclipse versions have all required functionality in the base extension points already. 
Example code, tutorials and ISV docs were updated and have the required information for how to migrate
to the new extension points. [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=172651">172651</a>].</li>
<li><b>Moved extension point</b> <samp>org.eclipse.rse.ui.archiveHandlers</samp> to 
<samp><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/extension-points/org_eclipse_rse_services_archivehandlers.html">org.eclipse.rse.services.archiveHandlers</a></samp>
[<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=173871">173871</a>].
<li><b>newConnectionWizards:</b> The old <samp>newConnectionWizardDelegates</samp> extension point
 has been replaced by a new <samp><a href="http://dsdp.eclipse.org/help/latest/topic/org.eclipse.rse.doc.isv/reference/extension-points/org_eclipse_rse_ui_newConnectionWizards.html">newConnectionWizards</a></samp>
 extension point for more flexibility [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=173772">173772</a>].</li>
<li><b>Deep Filtering:</b> The SystemView now supports Context Information for queries of multiple filters to the same remote elements.
  That is, the SystemViewElementAdapter.getChildren() method can know the filer context in which a query is made.
  This is necessary in order to properly support "deep filtering" by file types. An API change was required 
  to get this implemented: <b>Existing code needs to change</b> two method signatures in classes deriving from
  AbstractSystemViewAdapter. For details, see [<a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=170627">170627</a>].</li>
<li><b>Making classes internal:</b> Many classes have been made internal, and PDE support for generating
  warnings of discourage access has been enabled. Client code should not have used the classes which are
  now internal anyways; but if you do, just running "organize imports" should make your code compile again.
  For details, see [<a href="https://bugs.eclipse.org/bugs/showdependencytree.cgi?id=170922">170922</a>].</li>
</ul></li>
</ul>

Use 
  <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bapi%5D&classification=DSDP&product=Target+Management&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&chfieldfrom=2006-12-24&chfieldto=2007-07-01&chfield=resolution&cmdtype=doit">
  <!-- <a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&short_desc_type=allwordssubstr&short_desc=%5Bapi%5D&classification=DSDP&product=Target+Management&target_milestone=2.0+M5&target_milestone=2.0+M6&target_milestone=2.0+M7&target_milestone=2.0+RC1&target_milestone=2.0&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&resolution=FIXED&resolution=WONTFIX&resolution=INVALID&resolution=WORKSFORME&cmdtype=doit">  -->
  this query</a> to show the full list of API changes since RSE 1.0.1.
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
  <li><a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=187301">bug 187301</a> - maj - [telnet] Telnet does not allow multiple shells</li>
</ul>
<!--
<p>No major or critical bugs are known at the time of release.
-->
Use 
<a href="https://bugs.eclipse.org/bugs/buglist.cgi?query_format=advanced&classification=DSDP&product=Target+Management&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&bug_severity=blocker&bug_severity=critical&bug_severity=major&cmdtype=doit">this query</a>
for an up-to-date list of major or critical bugs.</p>

<p>The 
<a href="http://wiki.eclipse.org/index.php/TM_2.0_Known_Issues_and_Workarounds">
TM 2.0 Known Issues and Workarounds</a> Wiki page gives an up-to-date list
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
