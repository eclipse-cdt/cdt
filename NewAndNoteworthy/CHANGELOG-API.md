# Eclipse CDT Deprecated and Breaking API changes and removals

Deprecated API can be marked for deletion without requiring a major version increment.
See the [policy](../POLICY.md) for the details.
This section describes API removals that occurred in past releases, and upcoming removals in future releases.

# API Changes

## API Changes in CDT 11.0

- [org.eclipse.cdt.ui.newui.AbstractPage reduced visibility of many fields](#newUIAbstractPage)
- [org.eclipse.cdt.dsf.gdb.breakpoints.Messages is no longer API](#org.eclipse.cdt.dsf.gdb.breakpoints.Messages)
- [Removal of deprecated CommandLauncher.execute() method](#executeCommandLauncher)
- [Removal of deprecated CBuildConfiguration.watchProcess() methods](#watchProcessCBuildConfig)
- [Rework of API to determine GDB command line in org.eclipse.cdt.dsf.gdb](#gdbBackendDebuggerCommandLine)
- [Removal of Qt plug-ins and features](#qt-plugins)
- [Removal of constructor org.eclipse.cdt.utils.coff.CodeViewReader(RandomAccessFile, int, boolean)](#CodeViewReader-constructor-removal)
- [Removal of 32-bit Binary parsers with 64-bit replacements](#32bitbinaryparsers)

## API Changes in CDT 10.5.0

- [Package org.eclipse.cdt.cmake.is.core.participant has been renamed to org.eclipse.cdt.jsoncdb.core.participant.](#jsoncdb)

## API Changes in CDT 10.0.

- [Eclipse CDT requires Java 11 as a minimum to build and run.](#java11)
- [Some deprecated methods and classes have been removed.](#deprecatedRemovals)
- [Previously exported packages that were not API are now correctly marked as such.](#internals)
- [Activators removed for all bundles where possible.](#activators)
- [NewClassCreationWizardPage breaking changes.](#NewClassCreationWizardPage)
- [Arduino plug-ins and features removed.](#arduino)
- [Remove LRParser, XLC and UPC.](#oldparsers)
- [Remove org.eclipse.cdt.utils.Platform.](#cdtutilsPlatform)
- [DSF and DSF-GDB API Changes.](#dsf)
- [Partial removal of CDT 3.X project support.](#oldStyleProjects)
- [Removal of CDT Core Options API.](#optionsAPI)
- [TM Terminal has major changes to support new color and preference functionality.](#terminal)
- [Environment Variables are always case sensitive in CDT.](#casesensitive)
- [Environment variables no longer support \\${ to avoid expanding.](#escaping)
- [The binary parsers are now implement Autocloseable](#autocloseable)
- [ICPPASTDeductionGuide and ICPPASTParameterListOwner removed in CDT 10.0.1](#deductionremovedin10.0.0)

# Planned API Changes

The items below can be removed after the given date or on a major release, whichever is sooner.
The details and discussion on the removal happens in the GitHub issue (or Bugzilla) associated with the entry below.
See the [policy](../POLICY.md) for the details.

## Planned Removals after June 2022 or on a major version of Eclipe CDT.

- [32-bit Binary parsers with 64-bit replacements](#binaryparsers)
- [BaudRate enum in org.eclipse.cdt.serial](#baudrate)

## Planned Removals after June 2023 or on a major version of Eclipe CDT.

- [Add ITool parameter to ManagedCommandLineGenerator.toManagedCommandLineInfo](#ManagedCommandLineGenerator.toManagedCommandLineInfo)
- [Removed unneded boolean from function](#GnuMakefileGenerator.addRuleForTool)
- [Changed methods from static to non-static](#GnuMakefileGenerator.addDefaultHeader)

## Planned Removals after December 2023 or on a major version of Eclipe CDT.

- [GnuMakefileGenerator is no longer part of API](#GnuMakefileGeneratorAPI)
- [The Spawner signal constants are nolonger API](#Spawner.signals)

## Planned Removals after March 2024 or on a major version of Eclipe CDT.

- [java.util.regex.Matcher use in JSONCDB API will be removed](#ArgletsMatcher)

## Planned Removals after June 2024 or on a major version of Eclipe CDT.

- [java.util.regex.Matcher use in JSONCDB API will be removed](#ArgletsMatcher2)
- [Misnamed class BuiltinDetctionArgsGeneric will be removed](#BuiltinDetctionArgsGeneric)

## API Changes prior to CDT 10.0 / 2020-09.

Prior to CDT 10 release API changes were published as part of the [New and Noteworthy](README.md) entries for
that release.
From CDT 10 forward breaking API changes are included in this document.

---

# Details

Below is the detailed descriptions of API changes and mitigation efforts API consumers need to take.

## API Changes in CDT 11.0.

### <span id="newUIAbstractPage">org.eclipse.cdt.ui.newui.AbstractPage reduced visibility of many fields</span>

The following fields have been be removed from the API of
org.eclipse.cdt.ui.newui.AbstractPage as they were never intended to be
accessible by the design. As far as the current CDT developers know, no
one was using this API.

- org.eclipse.cdt.ui.newui.AbstractPage.noContentOnPage
- org.eclipse.cdt.ui.newui.AbstractPage.displayedConfig
- org.eclipse.cdt.ui.newui.AbstractPage.internalElement
- org.eclipse.cdt.ui.newui.AbstractPage.isProject
- org.eclipse.cdt.ui.newui.AbstractPage.isFolder
- org.eclipse.cdt.ui.newui.AbstractPage.isFile
- org.eclipse.cdt.ui.newui.AbstractPage.folder
- org.eclipse.cdt.ui.newui.AbstractPage.itabs
- org.eclipse.cdt.ui.newui.AbstractPage.currentTab

In addition, the following inner class has been removed from the API.

- org.eclipse.cdt.ui.newui.AbstractPage.InternalTab

See [Bug 579666](https://bugs.eclipse.org/bugs/show_bug.cgi?id=579666).

### <span id="org.eclipse.cdt.dsf.gdb.breakpoints.Messages">org.eclipse.cdt.dsf.gdb.breakpoints.Messages is no longer API</span>

org.eclipse.cdt.dsf.gdb.breakpoints.Messages should never have been API, Messages classes generally should not be.

See https://github.com/eclipse-cdt/cdt/pull/90

### <span id="executeCommandLauncher">Removal of deprecated CommandLauncher.execute() method</span>

The following method is removed because it does not implement the ICommandLauncher
interface. It has been deprecated since 2009:

- org.eclipse.cdt.core.CommandLauncher.execute(IPath, String[], String[], IPath)

Clients should instead use the equivilent method from the ICommandLauncher interface.
See [Bug 268615](https://bugs.eclipse.org/bugs/show_bug.cgi?id=268615).

### <span id="watchProcessCBuildConfig">Removal of deprecated CBuildConfiguration.watchProcess() methods</span>

The following methods are removed because their use prevents cancellation of
in-progress builds of core-build projects:

- org.eclipse.cdt.core.build.CBuildConfiguration.watchProcess(Process, IConsoleParser[], IConsole)
- org.eclipse.cdt.core.build.CBuildConfiguration.watchProcess(Process, IConsole)
- org.eclipse.cdt.core.build.CBuildConfiguration.watchProcess(Process, IConsoleParser[])

Clients should instead use the methods of the same name that take a progress monitor object.
See [Bug 580314](https://bugs.eclipse.org/bugs/show_bug.cgi?id=580314).

### <span id="gdbBackendDebuggerCommandLine">Rework of API to determine GDB command line in org.eclipse.cdt.dsf.gdb</span>

To support presentation of the GDB command line within the process
property page, a public method getDebuggerCommandLineArray() has been
added to the org.eclipse.cdt.dsf.gdb.service.IGDBBackend interface and
the following redundant protected methods have been removed:

- org.eclipse.cdt.dsf.gdb.service.GDBBackend.getDebuggerCommandLine()
- org.eclipse.cdt.dsf.gdb.service.GDBBackend.getGDBCommandLineArray()

Extenders that previously overrode the above protected methods should override
org.eclipse.cdt.dsf.gdb.service.IGDBBackend.getDebuggerCommandLineArray()
instead.

See [Bug 572944](https://bugs.eclipse.org/bugs/show_bug.cgi?id=572944)
and https://github.com/eclipse-cdt/cdt/pull/112.

### <span id="qt-plugins">Removal of Qt plug-ins and features</span>

For a while now the Qt plug-ins have had at least some issues.
They rely on the Nashorn script engine which was removed in Java 15.
The plug-ins have been removed in CDT 11.
The following bundles and all their related API has been removed:

- org.eclipse.cdt.qt.ui
- org.eclipse.cdt.qt.core

See https://github.com/eclipse-cdt/cdt/issues/123

### <span id="CodeViewReader-constructor-removal">Removal of constructor org.eclipse.cdt.utils.coff.CodeViewReader(RandomAccessFile, int, boolean)</span>

Same instance of RandomAccessFile was shared between multiple objects which
causes problems in closing it properly. A new constructor is introduced which
accepts filename and opens a RandomAccessFile.

See https://github.com/eclipse-cdt/cdt/pull/132

### <span id="32bitbinaryparsers">Removal of 32-bit Binary parsers with 64-bit replacements</span>

The following binary parser classes have been removed, mostly due to these versions not supporting 64-bit variants of the binary files.
The new 64-bit parsers support both 32 and 64 bit files and can be identified by the same name class followed by `64`.

- org.eclipse.cdt.utils.coff.Coff
- org.eclipse.cdt.utils.coff.PE
- org.eclipse.cdt.utils.coff.PEArchive
- org.eclipse.cdt.utils.coff.parser.CygwinPEBinaryArchive
- org.eclipse.cdt.utils.coff.parser.CygwinPEBinaryExecutable
- org.eclipse.cdt.utils.coff.parser.CygwinPEBinaryObject
- org.eclipse.cdt.utils.coff.parser.CygwinPEBinaryShared
- org.eclipse.cdt.utils.coff.parser.CygwinPEParser
- org.eclipse.cdt.utils.coff.parser.CygwinSymbol
- org.eclipse.cdt.utils.coff.parser.PEBinaryArchive
- org.eclipse.cdt.utils.coff.parser.PEBinaryExecutable
- org.eclipse.cdt.utils.coff.parser.PEBinaryObject
- org.eclipse.cdt.utils.coff.parser.PEBinaryShared
- org.eclipse.cdt.utils.coff.parser.PEParser
- org.eclipse.cdt.utils.macho.MachO
- org.eclipse.cdt.utils.macho.MachOHelper
- org.eclipse.cdt.utils.macho.parser.MachOBinaryArchive
- org.eclipse.cdt.utils.macho.parser.MachOBinaryExecutable
- org.eclipse.cdt.utils.macho.parser.MachOBinaryObject
- org.eclipse.cdt.utils.macho.parser.MachOBinaryShared
- org.eclipse.cdt.utils.macho.parser.MachOParser

In addition the following methods have been removed due to there existing a 64-bit compatible version.

- org.eclipse.cdt.utils.debug.dwarf.Dwarf.Dwarf(PE), use Dwarf(PE64) constructor instead
- org.eclipse.cdt.utils.debug.dwarf.Dwarf.init(PE), use init(PE64) method instead
- org.eclipse.cdt.utils.debug.dwarf.DwarfReader.DwarfReader(PE), use DwarfReader(PE64) constructor instead
- org.eclipse.cdt.utils.debug.dwarf.DwarfReader.init(PE), use init(PE64) method instead
- org.eclipse.cdt.utils.debug.stabs.Stabs.init(PE), use init(PE64) method instead

See https://github.com/eclipse-cdt/cdt/pull/135

---

## API Changes in CDT 10.5.0.

### <span id="jsoncdb">Package org.eclipse.cdt.cmake.is.core.participant has been renamed to org.eclipse.cdt.jsoncdb.core.participant.</span>

Affects CMake build, indexer support.
Package org.eclipse.cdt.cmake.is.core.participant was inadvertently not marked as experimental.
Third-party compiler vendors that provide a plugin teaching Eclipse CDT their compiler specific command-line options will have to update the import statements in their plugin's code.
See [Bug 564349](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564349).

---

## API Changes in CDT 10.0.

### <span id="java11">Eclipse CDT requires Java 11 as a minimum to build and run.</span>

Java 11 or greater is required for CDT. This means that the BREE for all
CDT bundles is now Java 11. See
[Bug 562494](https://bugs.eclipse.org/bugs/show_bug.cgi?id=562494).

The previous workaround for single-sourcing Java 8 and Java 11 has been
removed. This means the org.eclipse.tools.templates.freemarker.java11
bundle fragment has been removed. See
[Bug 563494](https://bugs.eclipse.org/bugs/show_bug.cgi?id=563494).

### <span id="deprecatedRemovals">Some deprecated methods and classes have been removed.</span>

Across the CDT code base some previously deprecated code has been
removed.

- EnvironmentReader.getRawEnvVars has been removed. Use
  EnvironmentReader.getEnvVars instead. See
  [Bug 564123](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564123).
- GDBJtagStartupTab.createRunOptionGroup has been removed. Override
  GDBJtagStartupTab.createRunGroup instead. See
  [Bug 566462](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566462).
- IGDBJtagConstants.ATTR_JTAG_DEVICE has been removed. Use
  IGDBJtagConstants.ATTR_JTAG_DEVICE_ID to persist device ID rather
  than device name. See
  [Bug 566462](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566462).
- IGDBJtagConstants.DEFAULT_JTAG_DEVICE has been removed. Use a
  local String if necessary. See
  [Bug 566462](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566462).
- IGDBJtagConstants.DEFAULT_JTAG_DEVICE_ID has been removed. Use a
  local String if necessary. See
  [Bug 566462](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566462).
- IGDBJtagDevice.getDefaultIpAddress has been removed. Implement
  IGDBJtagConnection and specify default_connection in JTagDevice
  extension XML instead. See
  [Bug 566462](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566462).
- IGDBJtagDevice.getDefaultPortNumber has been removed. Implement
  IGDBJtagConnection and specify default_connection in JTagDevice
  extension XML instead. See
  [Bug 566462](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566462).
- IGDBJtagDevice.doRemote has been removed. Implement
  IGDBJtagConnection.doRemote instead. See
  [Bug 566462](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566462).
- PTY constructor PTY(boolean console) has been removed. Use PTY(Mode
  mode) instead.
- org.eclipse.cdt.core.browser.PathUtil has been removed. Use
  org.eclipse.cdt.utils.PathUtil instead. See
  [Bug 564123](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564123).
- org.eclipse.cdt.launch.ui.CMainTab has been removed. This was part
  of the long since removed CDI and has no use. See
  [Bug 566530](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566530).
- org.eclipse.cdt.launch.ui.CMainCoreTab has been removed. This was
  part of the long since removed CDI and has no use. See
  [Bug 566530](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566530).
- org.eclipse.cdt.launch.ui.CMainAttachTab has been removed. This was
  part of the long since removed CDI and has no use. See
  [Bug 566530](https://bugs.eclipse.org/bugs/show_bug.cgi?id=566530).

### <span id="internals">Previously exported packages that were not API are now correctly marked as such.</span>

A review of the previously exported API of CDT was performed. A lot of
what was previously marked as public API has now been marked as
internal. This means the API has either had `x-internal` or `x-friends`
added to the `Export-Package` declarations.

The following bundles have had all their packages exported as internal
See [Bug 561389](https://bugs.eclipse.org/bugs/show_bug.cgi?id=561389):

- org.eclipse.cdt.cmake.ui
- org.eclipse.cdt.autotools.core
- org.eclipse.cdt.core.autotools.core
- org.eclipse.cdt.core.autotools.ui
- org.eclipse.cdt.autotools.tests
- org.eclipse.cdt.autotools.ui.tests
- org.eclipse.cdt.build.crossgcc
- org.eclipse.cdt.cmake.ui
- org.eclipse.cdt.build.gcc.core
- org.eclipse.cdt.build.gcc.ui
- org.eclipse.cdt.codan.checkers.ui.tests
- org.eclipse.tools.templates.ui

The following bundles had additional changes made to be able to mark
packages as internal:

<!-- end list -->

### <span id="activators">Activators removed for all bundles where possible.</span>

Where possible bundles have had their Activators removed. Some of these
activators were API and are no longer available. See
[Bug 561635](https://bugs.eclipse.org/bugs/show_bug.cgi?id=561635) for
workarounds where items that used to be in Activators are still needed,
such as logging and plug-ids.

The following Activators that were API have been removed.

- org.eclipse.tools.templates.ui

### <span id="NewClassCreationWizardPage">NewClassCreationWizardPage breaking changes.</span>

The NewClassCreationWizardPage has some API breaking changes due to how
some protected fields, especially `ALL_FIELDS` was used. While this
breaks binary compatibility, no code changes should be needed in
extenders, just compiling against the new version. See
[Bug 510789](https://bugs.eclipse.org/bugs/show_bug.cgi?id=510789) and
[Bug 561770](https://bugs.eclipse.org/bugs/show_bug.cgi?id=561770).

### <span id="arduino">Arduino plug-ins and features removed.</span>

The CDT Arduino plug-ins and features have been removed. Arduino can
still be used with CDT with manual maintenance of packages, etc. Or a
third-party add-on such as [Sloeber](https://eclipse.baeyens.it/) can be
used as a replacement.

The bundles and features that have been removed are:

- org.eclipse.cdt.arduino-feature
- org.eclipse.cdt.arduino.core.tests
- org.eclipse.cdt.arduino.core
- org.eclipse.cdt.arduino.ui

See [Bug 562498](https://bugs.eclipse.org/bugs/show_bug.cgi?id=562498).

### <span id="oldparsers">Remove LRParser, XLC and UPC.</span>

The LR, UPC and XLC support have been removed from CDT. The bundles have
not been maintained for a while and were not functional anymore.

The bundles and features that have been removed are:

- org.eclipse.cdt.core.lrparser.feature
- org.eclipse.cdt.core.lrparser.sdk.branding
- org.eclipse.cdt.core.lrparser.sdk.feature
- org.eclipse.cdt.core.lrparser.tests
- org.eclipse.cdt.core.lrparser
- org.eclipse.cdt.bupc-feature
- org.eclipse.cdt.core.parser.upc.feature
- org.eclipse.cdt.core.parser.upc.sdk.branding
- org.eclipse.cdt.core.parser.upc.sdk.feature
- org.eclipse.cdt.core.parser.upc.tests
- org.eclipse.cdt.core.parser.upc
- org.eclipse.cdt.managedbuilder.bupc.ui
- org.eclipse.cdt.core.lrparser.xlc.tests
- org.eclipse.cdt.core.lrparser.xlc
- org.eclipse.cdt.errorparsers.xlc.tests
- org.eclipse.cdt.errorparsers.xlc
- org.eclipse.cdt.make.xlc.core
- org.eclipse.cdt.managedbuilder.xlc.core
- org.eclipse.cdt.managedbuilder.xlc.ui
- org.eclipse.cdt.managedbuilder.xlupc.ui
- org.eclipse.cdt.xlc.feature
- org.eclipse.cdt.xlc.sdk-feature
- org.eclipse.cdt.xlc.sdk.branding

See [Bug 562498](https://bugs.eclipse.org/bugs/show_bug.cgi?id=562498).

### <span id="cdtutilsPlatform">Remove org.eclipse.cdt.utils.Platform.</span>

Class org.eclipse.cdt.utils.Platform has been removed. Use
org.eclipse.core.runtime.Platform instead.

See [Bug 564123](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564123).

### <span id="dsf">DSF and DSF-GDB API Changes</span>

DSF and DSF-GDB have had some small API changes, but they are still
breaking changes and are listed here:

- org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate.getCLILabel(ILaunchConfiguration,
  String) has been removed. Use
  org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate.getCLILabel(GdbLaunch,
  ILaunchConfiguration, String) instead. See
  [Bug 564553](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564553).

### <span id="oldStyleProjects">Partial removal of CDT 3.X project support</span>

CDT 3.X projects have been deprecated since CDT 4.0. Some classes
supporting this old version have been removed:

- org.eclipse.cdt.make.ui.wizards.ConvertToMakeProjectWizard
- org.eclipse.cdt.make.ui.wizards.ConvertToMakeProjectWizardPage
- org.eclipse.cdt.make.ui.wizards.MakeProjectWizardOptionPage
- org.eclipse.cdt.make.ui.wizards.NewMakeCCProjectWizard
- org.eclipse.cdt.make.ui.wizards.NewMakeCProjectWizard
- org.eclipse.cdt.make.ui.wizards.NewMakeProjectWizard

See [Bug 564949](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564949).

### <span id="optionsAPI">Removal of CDT Core Options API</span>

The CDT Core Options API and implementation has been removed as now it
is a part of Eclipse Equinox 4.16 Preferences API, the removed packages
are:

- org.eclipse.cdt.core.options
- org.eclipse.cdt.internal.core.options

See [Bug 565154](https://bugs.eclipse.org/bugs/show_bug.cgi?id=565154).

### <span id="terminal">TM Terminal has major changes to support new color and preference functionality.</span>

The TM Terminal's control (org.eclipse.tm.terminal.control) bundle has a
major new version to support numerous API changes to support features
such as new colors, preference sharing and some other code clean-up.

- org.eclipse.tm.terminal.model.Style removed and is mostly replaced
  by org.eclipse.tm.terminal.model.TerminalStyle. To handle new API
  and color handling, the entire Style class was replaced with
  TerminalStyle that operates in a similar way. Because some methods
  in TerminalStyle have similar signatures to those in removed Style
  class a new class was introduced to prevent accidental use of
  incorrect API. See
  [Bug 562495](https://bugs.eclipse.org/bugs/show_bug.cgi?id=549697).
- org.eclipse.tm.terminal.model.StyleColor removed. Replaced by a new
  way of representing standard color in the terminal using the new
  enum org.eclipse.tm.terminal.model.TerminalColor.. See
  [Bug 562495](https://bugs.eclipse.org/bugs/show_bug.cgi?id=549697).
- org.eclipse.tm.terminal.model.ITerminalTextData method taking or
  returning Style now use TerminalStyle. See
  [Bug 562495](https://bugs.eclipse.org/bugs/show_bug.cgi?id=549697).
- org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly method
  taking or returning Style now use TerminalStyle. See
  [Bug 562495](https://bugs.eclipse.org/bugs/show_bug.cgi?id=549697).
- org.eclipse.tm.terminal.model.LineSegment method taking or returning
  Style now use TerminalStyle. See
  [Bug 562495](https://bugs.eclipse.org/bugs/show_bug.cgi?id=549697).

### <span id="casesensitive">Environment Variables are always case sensitive in CDT.</span>

The handling of environment and build variables in CDT has changed to
being case sensitive. This means some API changes to remove methods such
as isVariableCaseSensitive (see full list below). The PATH and dome
other special environment variables in CDT are always uppercased, such
as Path. See
[Bug 564123](https://bugs.eclipse.org/bugs/show_bug.cgi?id=564123).

- org.eclipse.cdt.managedbuilder.core.IBuilder.isVariableCaseSensitive()
  removed.
- org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider.isVariableCaseSensitive()
  removed.
- org.eclipse.cdt.core.envvar.IEnvironmentVariableManager.isVariableCaseSensitive()
  removed.

### <span id="escaping">Environment variables no longer support \\${ to avoid expanding.</span>

This change is not literally an api change but will impact your code if
you used \\${ to not expand environment variables. It may impact your
code if you import environment variables and had to workaround
path\\${childPath} resolving to path${childPath}. See
[Bug 560330](https://bugs.eclipse.org/bugs/show_bug.cgi?id=560330).

- org.eclipse.cdt.managedbuilder.core.IBuilder.isVariableCaseSensitive()
  removed.
- org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider.isVariableCaseSensitive()
  removed.
- org.eclipse.cdt.core.envvar.IEnvironmentVariableManager.isVariableCaseSensitive()
  removed.

### <span id="autocloseable">The binary parsers are now implement AutoCloseable</span>

The binary parsers part of CDT core now implement the AutoCloseable
interface and can be used in try-with-resources blocks. See list below
for all the classes that are now AutoCloseable. See
[Bug 553674](https://bugs.eclipse.org/bugs/show_bug.cgi?id=553674).

- org.eclipse.cdt.utils.coff.Exe
- org.eclipse.cdt.utils.coff.PE
- org.eclipse.cdt.utils.coff.PE64
- org.eclipse.cdt.utils.coff.PEArchive
- org.eclipse.cdt.utils.elf.Elf
- org.eclipse.cdt.utils.elf.ElfHelper
- org.eclipse.cdt.utils.AR
- org.eclipse.cdt.utils.elf.AR
- org.eclipse.cdt.utils.som.AR
- org.eclipse.cdt.utils.xcoff.AR
- org.eclipse.cdt.utils.macho.AR

### <span id="deductionremovedin10.0.0">ICPPASTDeductionGuide and ICPPASTParameterListOwner removed in CDT 10.0.1</span>

Classes org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeductionGuide and
org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterListOwner which were
added for CDT 10.0.0 have been removed in CDT 10.0.1. See
[Bug 567261](https://bugs.eclipse.org/bugs/show_bug.cgi?id=567261).

---

# Future Deletions

The items below can be removed after the given date or on a major release, whichever is sooner.
The details and discussion on the removal happens in the GitHub issue (or Bugzilla) associated with the entry below.
See the [policy](../POLICY.md) for the details.

## API Removals after June 2022

### <span id="baudrate">BaudRate enum in org.eclipse.cdt.serial</span>

The BaudRate enum in org.eclipse.cdt.serial package will be removed. The
following APIs will be removed, listed with their replacement.

- org.eclipse.cdt.serial.BaudRate, use
  org.eclipse.cdt.serial.StandardBaudRates to obtain typical baud rate
  values
- org.eclipse.cdt.serial.SerialPort.setBaudRate(BaudRate), use
  org.eclipse.cdt.serial.SerialPort.setBaudRateValue(int) instead
- org.eclipse.cdt.serial.SerialPort.getBaudRate(), use
  org.eclipse.cdt.serial.SerialPort.getBaudRateValue() instead
- org.eclipse.tm.terminal.connector.cdtserial.connector.SerialSettings.getBaudRate(),
  use
  org.eclipse.tm.terminal.connector.cdtserial.connector.SerialSettings.getBaudRateValue()
  instead
- org.eclipse.tm.terminal.connector.cdtserial.connector.SerialSettings.setBaudRate(BaudRate),
  use
  org.eclipse.tm.terminal.connector.cdtserial.connector.SerialSettings.setBaudRateValue(int)
  instead

See [Bug 563108](https://bugs.eclipse.org/bugs/show_bug.cgi?id=563108).

## API Removals after June 2023

### <span id="ManagedCommandLineGenerator.toManagedCommandLineInfo">Add ITool parameter to ManagedCommandLineGenerator.toManagedCommandLineInfo</span>

To allow extenders to know the context of a generated command line, the
ITool instance was added to
ManagedCommandLineGenerator.toManagedCommandLineInfo method and the
method without the ITool parameter will be removed:

- org.eclipse.cdt.managedbuilder.core.ManagedCommandLineGenerator.toManagedCommandLineInfo(String,
  String, String, String\[\], String, String, String, String\[\])

Extenders that override the above protected method should override
org.eclipse.cdt.managedbuilder.core.ManagedCommandLineGenerator.toManagedCommandLineInfo(ITool,
String, String, String, String\[\], String, String, String, String\[\])
instead.

See [Bug 573254](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573254).

### <span id="GnuMakefileGenerator.addRuleForTool">Removed unneded boolean from function</span>

The implementation for how post-build steps are generated was changed.
The "bEmitPostBuildStepCall"-parameter is thus no longer needed.

- org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator.addRuleForTool(ITool,
  StringBuffer, boolean, String, String, List\<String\>,
  Vector\<String\>, boolean)

Extenders that override the above protected method should override
org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator.addRuleForTool(ITool,
StringBuffer, boolean, String, String, List\<String\>, Vector\<String\>)
instead.

See [Bug 573502](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573502).

### <span id="GnuMakefileGenerator.addDefaultHeader">Changed methods from static to non-static</span>

The implementation for generating the header in the make resources was
changed. The following APIs will be removed, listed with their
(non-static) replacement.

- org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator.populateDummyTargets(IConfiguration,
  IFile, boolean), use
  org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator.generateDummyTargets(IConfiguration,
  IFile, boolean) instead.
- org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator.populateDummyTargets(IResourceInfo,
  IFile, boolean), use
  org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator.generateDummyTargets(IResourceInfo,
  IFile, boolean) instead.
- org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator.addDefaultHeader(),
  use
  org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator.addGenericHeader()
  instead.

See [Bug 573722](https://bugs.eclipse.org/bugs/show_bug.cgi?id=573722).

## API Removals after December 2023

### <span id="GnuMakefileGeneratorAPI">GnuMakefileGenerator is no longer part of API</span>

The following classes will be removed from the API.

- org.eclipse.cdt.managedbuilder.makegen.gnu.GnuMakefileGenerator
- org.eclipse.cdt.managedbuilder.makegen.gnu.IManagedBuildGnuToolInfo
- org.eclipse.cdt.managedbuilder.makegen.gnu.ManagedBuildGnuToolInfo

See [Bug 505882](https://bugs.eclipse.org/bugs/show_bug.cgi?id=505882).

### <span id="Spawner.signals">The Spawner signal constants are nolonger API</span>

The following constants will be removed from the Spawner API.

- NOOP
- HUP
- KILL
- TERM
- INT
- CTRLC

## API Removals after March 2024

### <span id="ArgletsMatcher">java.util.regex.Matcher use in JSONCDB API will be removed</span>

The following fields will be removed from the API as it is not thread
safe. Use the patten instead and call matcher(input) to obtain a
matcher.

- org.eclipse.cdt.jsoncdb.core.participant.Arglets.NameOptionMatcher.matcher
- org.eclipse.cdt.jsoncdb.core.participant.DefaultToolDetectionParticipant.toolNameMatchersExt
- org.eclipse.cdt.jsoncdb.core.participant.DefaultToolDetectionParticipant.toolNameMatchersExtBackslash

See [Bug 578683](https://bugs.eclipse.org/bugs/show_bug.cgi?id=578683).

## API Removals after June 2024

### <span id="ArgletsMatcher2">java.util.regex.Matcher use in JSONCDB API will be removed</span>

The following method will be removed from the API as it encourages
non-safe constructs of reusing Matchers instead of Patterns.

- org.eclipse.cdt.jsoncdb.core.participant.Arglets.BuiltinDetectionArgsGeneric.processArgument(IArgumentCollector,
  String, Matcher\[\]) - use
  org.eclipse.cdt.jsoncdb.core.participant.Arglets.BuiltinDetectionArgsGeneric.processArgument(IArgumentCollector,
  String, Pattern\[\]) instead

See [Bug 579982](https://bugs.eclipse.org/bugs/show_bug.cgi?id=579982).

### <span id="BuiltinDetctionArgsGeneric">Misnamed class BuiltinDetctionArgsGeneric will be removed</span>

The class BuiltinDetctionArgsGeneric will be removed. Use the correctly
spelled BuiltinDetectionArgsGeneric instead.

- org.eclipse.cdt.jsoncdb.core.participant.Arglets.BuiltinDetctionArgsGeneric
