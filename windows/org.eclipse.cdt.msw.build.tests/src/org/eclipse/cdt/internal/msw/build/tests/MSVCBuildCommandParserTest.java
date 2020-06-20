/*******************************************************************************
 * Copyright (c) 2009, 2019 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *     Marc-Andre Laperle - Adapted to MSVC
 *******************************************************************************/
package org.eclipse.cdt.internal.msw.build.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
import org.eclipse.cdt.msw.build.core.MSVCBuildCommandParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases to test build command parsers.
 */
public class MSVCBuildCommandParserTests {
	// ID of the parser taken from the extension point
	private static final String MSVC_BUILD_COMMAND_PARSER_EXT = "org.eclipse.cdt.msw.build.core.MSVCBuildCommandParser"; //$NON-NLS-1$
	private static final String PROJECT_NAME = "MSVCBuildCommandParserTest";

	@Before
	public void setUp() {
		//We can only run this on Windows because of Windows-style paths, i.e. C:\
		assumeTrue(Platform.getOS().equals(Platform.OS_WIN32));
	}

	@After
	public void tearDown() throws Exception {
		try {
			Job.getJobManager().join(AbstractBuildCommandParser.JOB_FAMILY_BUILD_COMMAND_PARSER, null);
			Job.getJobManager().join(LanguageSettingsProvidersSerializer.JOB_FAMILY_SERIALIZE_LANGUAGE_SETTINGS_PROJECT,
					null);
			Job.getJobManager()
					.join(LanguageSettingsProvidersSerializer.JOB_FAMILY_SERIALIZE_LANGUAGE_SETTINGS_WORKSPACE, null);
		} catch (Exception e) {
			// ignore
		}
		ResourceHelper.cleanUp(PROJECT_NAME);
	}

	/**
	 * Helper method to fetch configuration descriptions.
	 */
	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project, false);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		return cfgDescriptions;
	}

	/**
	 * Test parsing of one typical entry.
	 */
	@Test
	public void testOneEntry() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file = ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl /IC:\\path0 file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			CIncludePathEntry expected = new CIncludePathEntry("C:/path0", 0);
			CIncludePathEntry entry = (CIncludePathEntry) entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
	}

	/**
	 * Test possible variations of compiler command.
	 */
	@Test
	public void testClFlavors() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file1 = ResourceHelper.createFile(project, "file1.cpp");
		IFile file2 = ResourceHelper.createFile(project, "file2.cpp");
		IFile file3 = ResourceHelper.createFile(project, "file3.cpp");
		IFile file4 = ResourceHelper.createFile(project, "file4.cpp");
		IFile file5 = ResourceHelper.createFile(project, "file5.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file1.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl /IC:\\path0 file1.cpp");
		parser.processLine("cl.exe /IC:\\path0 file2.cpp");
		parser.processLine("clang-cl /IC:\\path0 file3.cpp");
		parser.processLine("clang-cl.exe /IC:\\path0 file4.cpp");
		parser.processLine("C:\\absolute\\path\\cl.exe /IC:\\path0 file5.cpp");
		parser.shutdown();

		// check populated entries
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file1, languageId);
			assertEquals(new CIncludePathEntry("C:/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file2, languageId);
			assertEquals(new CIncludePathEntry("C:/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file3, languageId);
			assertEquals(new CIncludePathEntry("C:/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file4, languageId);
			assertEquals(new CIncludePathEntry("C:/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file5, languageId);
			assertEquals(new CIncludePathEntry("C:/path0", 0), entries.get(0));
		}
	}

	/**
	 * Parse variations of /I options.
	 */
	@Test
	public void testCIncludePathEntry() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file = ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl"
				// regular
				+ " /IC:\\path0 "
				// space after /I
				+ " /I C:\\path1 "
				// unknown option, should be ignored
				+ " /? "
				// double-quoted path with spaces
				+ " /I\"C:\\path with spaces\"" + " /I\"C:\\backslash at end\\\\\"" + " /I\"..\\..\\relative\""
				+ " /I\"..\\..\\relative with spaces\"" + " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CIncludePathEntry expected = new CIncludePathEntry("C:/path0", 0);
		CIncludePathEntry entry = (CIncludePathEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CIncludePathEntry("C:/path1", 0), entries.get(1));
		assertEquals(new CIncludePathEntry("C:/path with spaces", 0), entries.get(2));
		assertEquals(new CIncludePathEntry("C:/backslash at end", 0), entries.get(3));
		assertEquals(new CIncludePathEntry(project.getLocation().removeLastSegments(2).append("relative"), 0),
				entries.get(4));
		assertEquals(
				new CIncludePathEntry(project.getLocation().removeLastSegments(2).append("relative with spaces"), 0),
				entries.get(5));
	}

	/**
	 * Parse /imsvc (clang-cl)
	 */
	@Test
	public void testCIncludePathEntry_ClangCLSystemIncludePaths() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file = ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl" + " /imsvcC:\\path0 " + " /imsvc C:\\path1 " + " /imsvc\"C:\\path with spaces\""
				+ " /imsvc\"C:\\backslash at end\\\\\"" + " /imsvc\"..\\..\\relative\""
				+ " /imsvc\"..\\..\\relative with spaces\"" + " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CIncludePathEntry expected = new CIncludePathEntry("C:/path0", 0);
		CIncludePathEntry entry = (CIncludePathEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CIncludePathEntry("C:/path1", 0), entries.get(1));
		assertEquals(new CIncludePathEntry("C:/path with spaces", 0), entries.get(2));
		assertEquals(new CIncludePathEntry("C:/backslash at end", 0), entries.get(3));
		assertEquals(new CIncludePathEntry(project.getLocation().removeLastSegments(2).append("relative"), 0),
				entries.get(4));
		assertEquals(
				new CIncludePathEntry(project.getLocation().removeLastSegments(2).append("relative with spaces"), 0),
				entries.get(5));
	}

	/**
	 * Parse variations of /D options.
	 */
	@Test
	public void testCMacroEntry() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file = ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl " + " /DMACRO0" + " /DMACRO1=value   " + " /DMACRO2=\"value with spaces\"   "
				+ " /DMACRO3='c'   " + " /DMACRO4='\\\"'   " + " /DMACRO6=\\\"quotedvalue\\\"   "
				+ " /DMACRO7=\"'single-quotedvalue'\"   " + " /DMACRO8=\"\\\"escape-quoted value\\\"\"   "
				+ " /D MACRO9=\"\\\"with space\\\"\"   " + " /D\"MACROA=\\\"value\\\"\"   "
				+ " /D\"MACROB=\\\"val\\\"\\\"ue\\\"\"   " + " /D\"MACROC=\\\"\\\\\\\"\"   " + " /D\"MACROD\"   "
				+ " /D\"MACROE=\\\"with space\\\"\"   " + "-DMACROF -DMACROG" + " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CMacroEntry expected = new CMacroEntry("MACRO0", "1", 0);
		CMacroEntry entry = (CMacroEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CMacroEntry("MACRO1", "value", 0), entries.get(1));
		assertEquals(new CMacroEntry("MACRO2", "value with spaces", 0), entries.get(2));
		assertEquals(new CMacroEntry("MACRO3", "'c'", 0), entries.get(3));
		assertEquals(new CMacroEntry("MACRO4", "'\"'", 0), entries.get(4));
		assertEquals(new CMacroEntry("MACRO6", "\"quotedvalue\"", 0), entries.get(5));
		assertEquals(new CMacroEntry("MACRO7", "'single-quotedvalue'", 0), entries.get(6));
		assertEquals(new CMacroEntry("MACRO8", "\"escape-quoted value\"", 0), entries.get(7));
		assertEquals(new CMacroEntry("MACRO9", "\"with space\"", 0), entries.get(8));
		assertEquals(new CMacroEntry("MACROA", "\"value\"", 0), entries.get(9));
		assertEquals(new CMacroEntry("MACROB", "\"val\"\"ue\"", 0), entries.get(10));
		assertEquals(new CMacroEntry("MACROC", "\"\\\"", 0), entries.get(11));
		assertEquals(new CMacroEntry("MACROD", "1", 0), entries.get(12));
		assertEquals(new CMacroEntry("MACROE", "\"with space\"", 0), entries.get(13));
		assertEquals(new CMacroEntry("MACROF", "1", 0), entries.get(14));
		assertEquals(new CMacroEntry("MACROG", "1", 0), entries.get(15));
	}

	/**
	 * Parse /U option.
	 */
	@Test
	public void testCMacroEntry_undef() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file = ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl " + " /UMACRO /U MACRO2 /U\"MACRO3\" " + " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CMacroEntry("MACRO", null, ICSettingEntry.UNDEFINED), entries.get(0));
		assertEquals(new CMacroEntry("MACRO2", null, ICSettingEntry.UNDEFINED), entries.get(1));
		assertEquals(new CMacroEntry("MACRO3", null, ICSettingEntry.UNDEFINED), entries.get(2));
		//		assertEquals(new CMacroEntry("MACRO4", null, ICSettingEntry.UNDEFINED), entries.get(3));
	}

	/**
	 * Parse variations of force include (/FI) options.
	 */
	@Test
	public void testCIncludeFileEntry() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file = ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl " + " /FI C:\\include.file1" + " /FI \"include.file with spaces\""
				+ " /FI \"C:\\include.file with spaces\"" + " /FI ..\\..\\include.file2" + " /FI include.file3"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CIncludeFileEntry expected = new CIncludeFileEntry("C:/include.file1", 0);
		CIncludeFileEntry entry = (CIncludeFileEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(
				new CIncludeFileEntry("/${ProjName}/include.file with spaces", ICSettingEntry.VALUE_WORKSPACE_PATH),
				entries.get(1));
		assertEquals(new CIncludeFileEntry("C:/include.file with spaces", 0), entries.get(2));
		assertEquals(new CIncludeFileEntry(project.getLocation().removeLastSegments(2).append("include.file2"), 0),
				entries.get(3));
		assertEquals(new CIncludeFileEntry("/${ProjName}/include.file3", ICSettingEntry.VALUE_WORKSPACE_PATH),
				entries.get(4));
	}

	/**
	 * Parse command where resource is missing.
	 */
	@Test
	public void testFileMissing() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl /IC:\\path0 missing.cpp");
		parser.shutdown();

		// check entries
		assertTrue(parser.isEmpty());
	}

	/**
	 * Parsing of absolute path to the file being compiled.
	 */
	@Test
	public void testFileAbsolutePath() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		IFile file = ResourceHelper.createFile(project, "file.cpp");
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl " + "/IC:\\path0 " + "/I. " + '"' + file.getLocation().toOSString() + '"');
		parser.shutdown();

		// check entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry("C:/path0", 0), entries.get(0));
		assertEquals(new CIncludePathEntry("/${ProjName}/", ICSettingEntry.VALUE_WORKSPACE_PATH), entries.get(1));
	}

	/**
	 * Parsing where the name of the file being compiled contains spaces.
	 */
	@Test
	public void testFileWithSpaces() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file1 = ResourceHelper.createFile(project, "file with spaces 1.cpp");
		IFile file2 = ResourceHelper.createFile(project, "file with spaces 2.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file1.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl /IC:\\path0 \"file with spaces 1.cpp\"");
		parser.processLine("cl /IC:\\path0 \"" + file2.getLocation().toOSString() + "\"");
		parser.shutdown();

		// check populated entries
		// in double quotes
		assertEquals(new CIncludePathEntry("C:/path0", 0),
				parser.getSettingEntries(cfgDescription, file1, languageId).get(0));
		// Absolute path
		assertEquals(new CIncludePathEntry("C:/path0", 0),
				parser.getSettingEntries(cfgDescription, file2, languageId).get(0));
	}

	/**
	 * Ensure that duplicate paths are ignored.
	 */
	@Test
	public void testPathEntry_DuplicatePath() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(PROJECT_NAME);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file = ResourceHelper.createFile(project, "file.cpp");
		IFolder folder = ResourceHelper.createFolder(project, "Folder");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create MSVCBuildCommandParser
		MSVCBuildCommandParser parser = (MSVCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(MSVC_BUILD_COMMAND_PARSER_EXT, true);
		parser.setResolvingPaths(true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("cl " + " /IFolder" + " /IFolder" + " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry("/${ProjName}/" + folder.getProjectRelativePath(),
				ICSettingEntry.VALUE_WORKSPACE_PATH), entries.get(0));
		assertEquals(1, entries.size());
	}
}
