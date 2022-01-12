/*******************************************************************************
 * Copyright (c) 2010, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.language.settings.providers.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.Cygwin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.language.settings.providers.GCCBuiltinSpecsDetectorCygwin;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Test cases to test GCC built-in specs detector.
 */
public class GCCBuiltinSpecsDetectorTest extends BaseTestCase {
	private static final String LANGUAGE_ID_C = GCCLanguage.ID;
	private static final String SAMPLE_COMMAND = "NEW_COMMAND";
	private static final String PROJECT_TYPE_EXECUTABLE_GNU = "cdt.managedbuild.target.gnu.exe";

	/**
	 * Mock GCCBuiltinSpecsDetector to gain access to protected methods.
	 */
	class MockGCCBuiltinSpecsDetector extends GCCBuiltinSpecsDetector {
		@Override
		public void startupForLanguage(String languageId) throws CoreException {
			super.startupForLanguage(languageId);
		}

		@Override
		public void shutdownForLanguage() {
			super.shutdownForLanguage();
		}
	}

	/**
	 * Mock GCCBuiltinSpecsDetectorCygwin to gain access to protected methods.
	 */
	class MockGCCBuiltinSpecsDetectorCygwin extends GCCBuiltinSpecsDetectorCygwin {
		@Override
		public void startupForLanguage(String languageId) throws CoreException {
			super.startupForLanguage(languageId);
		}

		@Override
		public void shutdownForLanguage() {
			super.shutdownForLanguage();
		}
	}

	class MockGCCBuiltinSpecsDetectorCommandResolver extends GCCBuiltinSpecsDetector {
		@Override
		public String resolveCommand(String languageId) throws CoreException {
			return super.resolveCommand(languageId);
		}
	}

	class MockLspToolchainBuiltinSpecsDetectorCommandResolver extends MockGCCBuiltinSpecsDetectorCommandResolver {
		// ID must match the tool-chain definition in org.eclipse.cdt.managedbuilder.core.buildDefinitions extension point
		private static final String MOCK_TOOLCHAIN_ID = "cdt.managedbuilder.lsp.tests.toolchain";

		@Override
		public String getToolchainId() {
			return MOCK_TOOLCHAIN_ID;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Helper method to fetch configuration descriptions.
	 */
	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		return cfgDescriptions;
	}

	/**
	 * Test expansion of variables in build command.
	 */
	public void testGCCBuiltinSpecsDetector_ResolvedCommand() throws Exception {
		{
			// check ${COMMAND} and ${INPUTS}
			MockGCCBuiltinSpecsDetectorCommandResolver detector = new MockGCCBuiltinSpecsDetectorCommandResolver();
			detector.setLanguageScope(new ArrayList<String>() {
				{
					add(LANGUAGE_ID_C);
				}
			});
			detector.setCommand("${COMMAND} -E -P -v -dD ${INPUTS}");

			String resolvedCommand = detector.resolveCommand(LANGUAGE_ID_C);
			assertTrue(resolvedCommand.startsWith("gcc -E -P -v -dD "));
			assertTrue(resolvedCommand.endsWith("spec.c"));
		}
		{
			// check ${EXT}
			MockGCCBuiltinSpecsDetectorCommandResolver detector = new MockGCCBuiltinSpecsDetectorCommandResolver();
			detector.setLanguageScope(new ArrayList<String>() {
				{
					add(LANGUAGE_ID_C);
				}
			});
			detector.setCommand("${COMMAND} -E -P -v -dD file.${EXT}");

			String resolvedCommand = detector.resolveCommand(LANGUAGE_ID_C);
			assertTrue(resolvedCommand.startsWith("gcc -E -P -v -dD "));
			assertTrue(resolvedCommand.endsWith("file.c"));
		}
		{
			// check expansion of environment variables
			MockGCCBuiltinSpecsDetectorCommandResolver detector = new MockGCCBuiltinSpecsDetectorCommandResolver();
			detector.setLanguageScope(new ArrayList<String>() {
				{
					add(LANGUAGE_ID_C);
				}
			});
			String command = "cmd --env1=${CWD} --env2=${OS}";
			detector.setCommand(command);
			String resolvedCommand = detector.resolveCommand(LANGUAGE_ID_C);

			ICdtVariableManager varManager = CCorePlugin.getDefault().getCdtVariableManager();
			String expected = varManager.resolveValue(command, "", null, null);
			// confirm that "expected" expanded
			assertFalse(command.equals(expected));
			assertEquals(expected, resolvedCommand);
		}
		{
			// check expansion of eclipse and MBS variables
			MockGCCBuiltinSpecsDetectorCommandResolver detector = new MockGCCBuiltinSpecsDetectorCommandResolver();
			detector.setLanguageScope(new ArrayList<String>() {
				{
					add(LANGUAGE_ID_C);
				}
			});
			String command = "cmd --eclipse-var=${workspace_loc} --mbs-var=${WorkspaceDirPath}";
			detector.setCommand(command);
			String resolvedCommand = detector.resolveCommand(LANGUAGE_ID_C);

			ICdtVariableManager varManager = CCorePlugin.getDefault().getCdtVariableManager();
			String expected = varManager.resolveValue(command, "", null, null);
			// confirm that "expected" expanded
			assertFalse(command.equals(expected));
			assertEquals(expected, resolvedCommand);
		}
	}

	/**
	 * Test expansion of relevant tool options in build command.
	 */
	public void testGCCBuiltinSpecsDetector_ResolvedCommand_Flags() throws Exception {
		// check ${FLAGS}
		MockLspToolchainBuiltinSpecsDetectorCommandResolver detector = new MockLspToolchainBuiltinSpecsDetectorCommandResolver();
		detector.setLanguageScope(new ArrayList<String>() {
			{
				add(LANGUAGE_ID_C);
			}
		});
		detector.setCommand("gcc ${FLAGS}");

		String resolvedCommand = detector.resolveCommand(LANGUAGE_ID_C);
		assertEquals("gcc -bool-option -str-option=str-value -enum-option -list-option1 -list-option2 -tree-option",
				resolvedCommand);
	}

	/**
	 * Test parsing of macro without value.
	 */
	public void testGCCBuiltinSpecsDetector_Macro_NoValue() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO", null, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test parsing of macro with ordinary value.
	 */
	public void testGCCBuiltinSpecsDetector_Macro_Simple() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO VALUE");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test parsing of macro with value in round brackets.
	 */
	public void testGCCBuiltinSpecsDetector_Macro_Const() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO (3)");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO", "(3)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test parsing of macro definition with tabs.
	 */
	public void testGCCBuiltinSpecsDetector_Macro_WhiteSpaces() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#define \t MACRO_1 VALUE");
		detector.processLine("#define MACRO_2 \t VALUE");
		detector.processLine("#define MACRO_3 VALUE \t");
		detector.processLine("#define MACRO_4 VALUE + 1");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		int index = 0;
		assertEquals(new CMacroEntry("MACRO_1", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(new CMacroEntry("MACRO_2", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(new CMacroEntry("MACRO_3", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(new CMacroEntry("MACRO_4", "VALUE + 1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(index, entries.size());
	}

	/**
	 * Test parsing of macro definition with empty argument list.
	 */
	public void testGCCBuiltinSpecsDetector_Macro_EmptyArgList() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO() VALUE");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO()", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test parsing of macro definition with unused parameter.
	 */
	public void testGCCBuiltinSpecsDetector_Macro_ParamUnused() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO(X) VALUE");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO(X)", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test parsing of macro definition with multiple parameters.
	 */
	public void testGCCBuiltinSpecsDetector_Macro_ParamSpace() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO(P1, P2) VALUE(P1, P2)");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(
				new CMacroEntry("MACRO(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test parsing of macro definition with multiple parameters and no value.
	 */
	public void testGCCBuiltinSpecsDetector_Macro_ArgsNoValue() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO(P1, P2) ");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO(P1, P2)", null, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test parsing of macro definition having white spaces in various places.
	 */
	public void testGCCBuiltinSpecsDetector_Macro_Args_WhiteSpaces() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#define \t MACRO_1(P1, P2) VALUE(P1, P2)");
		detector.processLine("#define MACRO_2(P1, P2) \t VALUE(P1, P2)");
		detector.processLine("#define MACRO_3(P1, P2) VALUE(P1, P2) \t");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		int index = 0;
		assertEquals(
				new CMacroEntry("MACRO_1(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(
				new CMacroEntry("MACRO_2(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(
				new CMacroEntry("MACRO_3(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(index, entries.size());
	}

	/**
	 * Test parsing of include directives.
	 */
	public void testGCCBuiltinSpecsDetector_Includes() throws Exception {
		// Create model project and folders to test
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProject(projectName);
		IPath tmpPath = ResourceHelper.createTemporaryFolder();
		ResourceHelper.createFolder(project, "/misplaced/include1");
		ResourceHelper.createFolder(project, "/local/include");
		ResourceHelper.createFolder(project, "/usr/include");
		ResourceHelper.createFolder(project, "/usr/include2");
		ResourceHelper.createFolder(project, "/misplaced/include2");
		ResourceHelper.createFolder(project, "/System/Library/Frameworks");
		ResourceHelper.createFolder(project, "/Library/Frameworks");
		ResourceHelper.createFolder(project, "/misplaced/include3");
		String loc = tmpPath.toString();

		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.startupForLanguage(null);

		detector.processLine(" " + loc + "/misplaced/include1");
		detector.processLine("#undef BEFORE_INCLUDES");
		detector.processLine("#include \"...\" search starts here:");
		detector.processLine("#undef IN_THE_MIDDLE_OF_INCLUDES_0");
		detector.processLine(" " + loc + "/local/include");
		detector.processLine("#include <...> search starts here:");
		detector.processLine(" " + loc + "/usr/include");
		detector.processLine(" " + loc + "/usr/include/../include2");
		detector.processLine("#undef IN_THE_MIDDLE_OF_INCLUDES_1");
		detector.processLine(" " + loc + "/missing/folder");
		detector.processLine("#undef IN_THE_MIDDLE_OF_INCLUDES_2");
		detector.processLine(" " + loc + "/Library/Frameworks (framework directory)");
		detector.processLine("End of search list.");
		detector.processLine(" " + loc + "/misplaced/include2");
		detector.processLine("Framework search starts here:");
		detector.processLine(" " + loc + "/System/Library/Frameworks");
		detector.processLine("End of framework search list.");
		detector.processLine(" " + loc + "/misplaced/include3");
		detector.processLine("#undef AFTER_INCLUDES");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		int index = 0;
		assertEquals(new CIncludePathEntry(loc + "/local/include",
				ICSettingEntry.LOCAL | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc + "/usr/include", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(new CIncludePathEntry(loc + "/usr/include2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(new CIncludePathEntry(loc + "/missing/folder", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(
				new CIncludePathEntry(loc + "/Library/Frameworks",
						ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(
				new CIncludePathEntry(loc + "/System/Library/Frameworks",
						ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(index, entries.size());
	}

	/**
	 * Test parsing of macro definition of include directives having white spaces.
	 */
	public void testGCCBuiltinSpecsDetector_Includes_WhiteSpaces() throws Exception {
		String loc = ResourceHelper.createTemporaryFolder().toString();

		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.startupForLanguage(null);

		detector.processLine("#include \"...\" search starts here:");
		detector.processLine(" \t " + loc + "/local/include");
		detector.processLine("#include <...> search starts here:");
		detector.processLine(loc + "/usr/include");
		detector.processLine(" " + loc + "/Library/Frameworks \t (framework directory)");
		detector.processLine("End of search list.");
		detector.processLine("Framework search starts here:");
		detector.processLine(" " + loc + "/System/Library/Frameworks \t ");
		detector.processLine("End of framework search list.");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		int index = 0;
		assertEquals(new CIncludePathEntry(loc + "/local/include",
				ICSettingEntry.LOCAL | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc + "/usr/include", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(
				new CIncludePathEntry(loc + "/Library/Frameworks",
						ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(
				new CIncludePathEntry(loc + "/System/Library/Frameworks",
						ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(index, entries.size());
	}

	/**
	 * Test parsing of include directives incorporating symbolic links.
	 */
	public void testGCCBuiltinSpecsDetector_Includes_SymbolicLinkUp() throws Exception {
		// do not test on systems where symbolic links are not supported
		if (!ResourceHelper.isSymbolicLinkSupported()) {
			return;
		}

		// Create model project and folders to test
		String projectName = getName();
		@SuppressWarnings("unused")
		IProject project = ResourceHelper.createCDTProject(projectName);
		// create link on the filesystem
		IPath dir1 = ResourceHelper.createTemporaryFolder();
		IPath dir2 = dir1.removeLastSegments(1);
		IPath linkPath = dir1.append("linked");
		ResourceHelper.createSymbolicLink(linkPath, dir2);

		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#include <...> search starts here:");
		detector.processLine(" " + linkPath.toString() + "/..");
		detector.processLine("End of search list.");
		detector.shutdownForLanguage();
		detector.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(
				new CIncludePathEntry(dir2.removeLastSegments(1), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test parsing of include directives included multiple times.
	 */
	public void testGCCBuiltinSpecsDetector_Includes_Duplicates() throws Exception {
		// Create model project and folders to test
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProject(projectName);
		IPath tmpPath = ResourceHelper.createTemporaryFolder();
		ResourceHelper.createFolder(project, "/usr/include");
		String loc = tmpPath.toString();

		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		detector.startup(null, null);
		detector.startupForLanguage(null);

		detector.processLine("#include <...> search starts here:");
		detector.processLine(" " + loc + "/usr/include");
		detector.processLine(" " + loc + "/usr/include");
		detector.processLine(" " + loc + "/usr/include/");
		detector.processLine(" " + loc + "/usr/include/../include");
		detector.processLine("End of search list.");
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		int index = 0;
		assertEquals(new CIncludePathEntry(loc + "/usr/include", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(index++));
		assertEquals(index, entries.size());
	}

	/**
	 * Test parsing of include directives for Cygwin for global provider.
	 */
	public void testGCCBuiltinSpecsDetector_Cygwin_NoProject() throws Exception {
		if (!Cygwin.isAvailable()) {
			// Skip the test if Cygwin is not available.
			return;
		}

		String cygwinLocation = "/usr/include";
		String windowsLocation = ResourceHelper.cygwinToWindowsPath(cygwinLocation);
		assertTrue("windowsLocation=[" + windowsLocation + "]", new Path(windowsLocation).getDevice() != null);

		MockGCCBuiltinSpecsDetectorCygwin detector = new MockGCCBuiltinSpecsDetectorCygwin();

		detector.startup(null, null);
		detector.startupForLanguage(null);
		detector.processLine("#include <...> search starts here:");
		detector.processLine(" /usr/include");
		detector.processLine("End of search list.");
		detector.shutdownForLanguage();
		detector.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CIncludePathEntry(new Path(windowsLocation), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test parsing of include directives for Cygwin for provider running for a configuration.
	 */
	public void testGCCBuiltinSpecsDetector_Cygwin_Configuration() throws Exception {
		if (!Cygwin.isAvailable()) {
			// Skip the test if Cygwin is not available.
			return;
		}

		String cygwinLocation = "/usr/include";
		String windowsLocation = ResourceHelper.cygwinToWindowsPath(cygwinLocation);
		assertTrue("windowsLocation=[" + windowsLocation + "]", new Path(windowsLocation).getDevice() != null);

		// Create model project and folders to test
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		MockGCCBuiltinSpecsDetectorCygwin detector = new MockGCCBuiltinSpecsDetectorCygwin();

		detector.startup(cfgDescription, null);
		detector.startupForLanguage(null);
		detector.processLine("#include <...> search starts here:");
		detector.processLine(" /usr/include");
		detector.processLine("End of search list.");
		detector.shutdownForLanguage();
		detector.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CIncludePathEntry(new Path(windowsLocation), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test expansion of variable ${COMMAND} for case when the command was modified in tool-chain.
	 */
	public void test_GCCBuiltinSpecsDetector_ResolveModifiedCommand() throws Exception {
		// create a new project
		IProject project = ManagedBuildTestHelper.createProject(this.getName(), PROJECT_TYPE_EXECUTABLE_GNU);

		{
			// create a mock specs detector
			MockGCCBuiltinSpecsDetectorCommandResolver detector = new MockGCCBuiltinSpecsDetectorCommandResolver();
			detector.setCommand("${COMMAND}");
			String command = ((AbstractBuiltinSpecsDetector) detector).getCommand();
			assertEquals("${COMMAND}", command);
			assertTrue(!command.equals(SAMPLE_COMMAND));
			// assign the mock specs detector to the first configuration
			ICProjectDescription prjDescriptionWritable = CoreModel.getDefault().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertTrue(cfgDescriptions.length > 0);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(detector);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			// change the default command in all the tools of the toolchain
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDescription);
			assertNotNull(cfg);
			ITool[] tools = cfg.getTools();
			assertTrue(tools.length > 0);
			for (ITool tool : tools) {
				tool.setToolCommand(SAMPLE_COMMAND);
				assertEquals(SAMPLE_COMMAND, tool.getToolCommand());
			}
			// save project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		{
			// double-check that the new command made its way to the tools
			ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertTrue(cfgDescriptions.length > 0);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDescription);
			assertNotNull(cfg);
			ITool[] tools = cfg.getTools();
			for (ITool tool : tools) {
				assertEquals(SAMPLE_COMMAND, tool.getToolCommand());
			}
			// verify that the new command is picked by the provider
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof MockGCCBuiltinSpecsDetectorCommandResolver);
			String command = ((MockGCCBuiltinSpecsDetectorCommandResolver) provider).resolveCommand(LANGUAGE_ID_C);
			assertEquals(SAMPLE_COMMAND, command);
		}
	}

}
