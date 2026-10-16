/*******************************************************************************
 * Copyright (c) 2026 Sergii Zolotarov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.projectmodel.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.testplugin.BuildSystemTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.Test;

/**
 * Tests that the value of a macro (symbol) is what the compiler is to see, and is quoted as a single
 * shell word when stored in the "Defined symbols" option of a tool, as it is passed to the compiler
 * through a shell by the generated makefiles.
 */
public class MacroEntriesWithQuotesTest extends BaseTestCase5 {
	private static final String PROJ_NAME = "MacroEntriesWithQuotesTest";

	@Test
	public void testMacroEntriesWithQuotes() throws Exception {
		IProject project = BuildSystemTestHelper.createProject(PROJ_NAME, null, "cdt.managedbuild.target.gnu30.exe");
		ResourceHelper.addResourceCreated(project);
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();

		ICProjectDescription des = mngr.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		ICLanguageSetting ls = cfgDes.getRootFolderDescription().getLanguageSettingForFile("a.c");

		List<ICLanguageSettingEntry> list = new ArrayList<>();
		List<String> expectedValues = new ArrayList<>();
		addMacro(list, expectedValues, "NO_SPACE", "NoSpace", "NO_SPACE=NoSpace");
		addMacro(list, expectedValues, "WITH_SPACE", "With Space", "WITH_SPACE=\"With Space\"");
		addMacro(list, expectedValues, "QUOTED_NO_SPACE", "\"QuotedNoSpace\"", "QUOTED_NO_SPACE=\\\"QuotedNoSpace\\\"");
		addMacro(list, expectedValues, "QUOTED_WITH_SPACE", "\"Quoted With Space\"",
				"QUOTED_WITH_SPACE=\"\\\"Quoted With Space\\\"\"");
		addMacro(list, expectedValues, "MULTIPLE_QUOTED", "\"Multiple Quoted\" \"With Space\"",
				"MULTIPLE_QUOTED=\"\\\"Multiple Quoted\\\" \\\"With Space\\\"\"");
		addMacro(list, expectedValues, "VARIABLE", "${VARIABLE}", "VARIABLE=\"${VARIABLE}\"");
		addMacro(list, expectedValues, "QUOTED_VARIABLE", "\"${VARIABLE}\"", "QUOTED_VARIABLE=\"\\\"${VARIABLE}\\\"\"");
		addMacro(list, expectedValues, "BACKSLASH", "C:\\dir", "BACKSLASH=\"C:\\dir\"");
		addMacro(list, expectedValues, "TRAILING_BACKSLASH", "C:\\dir\\", "TRAILING_BACKSLASH=\"C:\\dir\\\\\"");
		addMacro(list, expectedValues, "SINGLE_QUOTE", "'c'", "SINGLE_QUOTE=\"'c'\"");
		addMacro(list, expectedValues, "MAX(a,b)", "((a)>(b)?(a):(b))", "\"MAX(a,b)=((a)>(b)?(a):(b))\"");
		addMacro(list, expectedValues, "NO_VALUE", "", "NO_VALUE");
		int added = list.size();
		list.addAll(ls.getSettingEntriesList(ICSettingEntry.MACRO));
		ls.setSettingEntries(ICSettingEntry.MACRO, list);

		assertEquals(expectedValues, getDefinedSymbols(cfg).subList(0, added));
		assertEquals(list, ls.getSettingEntriesList(ICSettingEntry.MACRO));

		mngr.setProjectDescription(project, des);

		// Reload the project to read the entries from the saved options
		project.delete(false, true, new NullProgressMonitor());
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJ_NAME);
		assertNull(mngr.getProjectDescription(project), "project description is not null for removed project");

		project = BuildSystemTestHelper.createProject(PROJ_NAME);
		des = mngr.getProjectDescription(project);
		assertNotNull(des, "project description is null for re-created project");
		assertTrue(des.isValid(), "des should be valid for re-created project");

		cfgDes = des.getConfigurations()[0];
		cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		ls = cfgDes.getRootFolderDescription().getLanguageSettingForFile("a.c");

		assertEquals(expectedValues, getDefinedSymbols(cfg).subList(0, added));
		assertEquals(list, ls.getSettingEntriesList(ICSettingEntry.MACRO));
	}

	/**
	 * The values reported in https://github.com/eclipse-cdt/cdt/issues/1521, each set on its own as they
	 * share the name. The combinations with quotes in the name are left out, as a name must be an identifier.
	 */
	@Test
	public void testMacroValuesFromIssue1521() throws Exception {
		String[][] macros = { //
				{ "symbol1", "\"${PATH}\"" }, //
				{ "foo", "bar" }, //
				{ "foo", "\"bar\"" }, //
				{ "foo", "\"bar" }, //
				{ "foo", "bar\"" }, //
		};
		IProject project = BuildSystemTestHelper.createProject(PROJ_NAME, null, "cdt.managedbuild.target.gnu30.exe");
		ResourceHelper.addResourceCreated(project);
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();

		for (String[] macro : macros) {
			String message = "name: " + macro[0] + ", value: " + macro[1];
			ICLanguageSettingEntry entry = new CMacroEntry(macro[0], macro[1], 0);

			ICProjectDescription des = mngr.getProjectDescription(project);
			ICLanguageSetting ls = des.getConfigurations()[0].getRootFolderDescription()
					.getLanguageSettingForFile("a.c");
			ls.setSettingEntries(ICSettingEntry.MACRO, List.of(entry));
			assertEquals(entry, ls.getSettingEntriesList(ICSettingEntry.MACRO).get(0), message);
			mngr.setProjectDescription(project, des);

			// Reload the project to read the entry from the saved option
			project.delete(false, true, new NullProgressMonitor());
			project = BuildSystemTestHelper.createProject(PROJ_NAME);
			des = mngr.getProjectDescription(project);
			assertNotNull(des, "project description is null for re-created project");
			ls = des.getConfigurations()[0].getRootFolderDescription().getLanguageSettingForFile("a.c");
			assertEquals(entry, ls.getSettingEntriesList(ICSettingEntry.MACRO).get(0), message);
		}
	}

	/**
	 * Values entered directly in the option, or stored by previous versions, are read the way the shell
	 * passes them to the compiler, and are kept as they are when the entries are not changed.
	 */
	@Test
	public void testExistingOptionValues() throws Exception {
		IProject project = BuildSystemTestHelper.createProject(PROJ_NAME, null, "cdt.managedbuild.target.gnu30.exe");
		ResourceHelper.addResourceCreated(project);
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();

		ICProjectDescription des = mngr.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);

		List<String> values = List.of("\"SPACE=a b\"", "QUOTED=\"a b\"", "ESCAPED=\\\"str\\\"",
				"\"ESCAPED_SPACE=\\\"a b\\\"\"", "SINGLE_QUOTED='\"Hello World\"'", "\"BACKSLASH=a\\b\"",
				"\"NO_VALUE\"");
		ITool tool = cfg.getRootFolderInfo().getToolFromInputExtension("c");
		ManagedBuildManager.setOption(cfg.getRootFolderInfo(), tool, getDefinedSymbolsOption(cfg),
				values.toArray(new String[0]));

		List<ICLanguageSettingEntry> expected = List.of(new CMacroEntry("SPACE", "a b", 0),
				new CMacroEntry("QUOTED", "a b", 0), new CMacroEntry("ESCAPED", "\"str\"", 0),
				new CMacroEntry("ESCAPED_SPACE", "\"a b\"", 0), new CMacroEntry("SINGLE_QUOTED", "\"Hello World\"", 0),
				new CMacroEntry("BACKSLASH", "a\\b", 0), new CMacroEntry("NO_VALUE", "", 0));
		ICLanguageSetting ls = cfgDes.getRootFolderDescription().getLanguageSettingForFile("a.c");
		List<ICLanguageSettingEntry> entries = ls.getSettingEntriesList(ICSettingEntry.MACRO);
		assertEquals(expected, entries.subList(0, expected.size()));

		ls.setSettingEntries(ICSettingEntry.MACRO, entries);
		assertEquals(values, getDefinedSymbols(cfg).subList(0, values.size()));
	}

	private static void addMacro(List<ICLanguageSettingEntry> list, List<String> expectedValues, String name,
			String value, String expectedValue) {
		list.add(new CMacroEntry(name, value, 0));
		expectedValues.add(expectedValue);
	}

	private static IOption getDefinedSymbolsOption(IConfiguration cfg) throws BuildException {
		ITool tool = cfg.getRootFolderInfo().getToolFromInputExtension("c");
		for (IOption option : tool.getOptions()) {
			if (option.getValueType() == IOption.PREPROCESSOR_SYMBOLS) {
				return option;
			}
		}
		return fail("no defined symbols option");
	}

	private static List<String> getDefinedSymbols(IConfiguration cfg) throws BuildException {
		return Arrays.asList(getDefinedSymbolsOption(cfg).getBasicStringListValue());
	}
}
