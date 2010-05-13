/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/

package org.eclipse.cdt.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Class for testing exported settings and project references.
 * This functionality is provided by the CfgExportSettingContainerFactory which plugins
 * into the CExternalSettingsManager
 */
public class CConfigurationDescriptionExportSettings extends BaseTestCase {

	CoreModel coreModel = CoreModel.getDefault();

	public static TestSuite suite() {
		return suite(CConfigurationDescriptionExportSettings.class, "_");
	}

	// Setting entries 1
	final ICLanguageSettingEntry entries[] = new ICLanguageSettingEntry[]{
			new CMacroEntry("a", "b", 0),
			new CMacroEntry("c", "d", 0),
			new CIncludePathEntry("a/b/c", 0),
			new CIncludePathEntry("d/e/f", 0),
	};

	// Setting entries 2
	final ICLanguageSettingEntry entries2[] = new ICLanguageSettingEntry[]{
			new CMacroEntry("a2", "b2", 0),
			new CMacroEntry("c2", "d2", 0),
			new CIncludePathEntry("a/b/c/2", 0),
			new CIncludePathEntry("d/e/f/2", 0),
	};

	/**
	 * This tests for simple reference propagation.
	 * It used to live in the Managedbuild testsuite in ProjectModelTests.java
	 * but is moved here as it doesn't test any managedbuilder specific functionality
	 */
	public void testReferences() throws Exception {
		final String projectName4 = "test4";
		final String projectName5 = "test5";

		IProject project4 = ResourceHelper.createCDTProjectWithConfig(projectName4);
		IProject project5 = ResourceHelper.createCDTProjectWithConfig(projectName5);

		ICProjectDescription des4 = coreModel.getProjectDescription(project4);
		ICProjectDescription des5 = coreModel.getProjectDescription(project5);
		ICConfigurationDescription dess[] = des5.getConfigurations();

		dess[0].createExternalSetting(null, null, null, entries);
		dess[0].setActive();

		ICExternalSetting extSettings[] = dess[0].getExternalSettings();
		assertEquals(extSettings.length, 1);

		checkEquivContents(extSettings[0].getEntries(), entries);
		List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>(Arrays.asList(entries));
		list.remove(3);
		list.remove(2);
		checkEquivContents(extSettings[0].getEntries(ICSettingEntry.MACRO), list.toArray());
		list = new ArrayList<ICLanguageSettingEntry>(Arrays.asList(entries));
		list.remove(0);
		list.remove(0);
		checkEquivContents(extSettings[0].getEntries(ICSettingEntry.INCLUDE_PATH), list.toArray());
		coreModel.setProjectDescription(project5, des5);

		extSettings = coreModel.getProjectDescription(project5).getActiveConfiguration().getExternalSettings();
		assertEquals(extSettings.length, 1);

		checkEquivContents(extSettings[0].getEntries(), entries);
		list = new ArrayList<ICLanguageSettingEntry>(Arrays.asList(entries));
		list.remove(3);
		list.remove(2);
		checkEquivContents(extSettings[0].getEntries(ICSettingEntry.MACRO), list.toArray());
		list = new ArrayList<ICLanguageSettingEntry>(Arrays.asList(entries));
		list.remove(0);
		list.remove(0);
		checkEquivContents(extSettings[0].getEntries(ICSettingEntry.INCLUDE_PATH), list.toArray());

		dess = des4.getConfigurations();
		ICLanguageSetting ls = dess[0].getRootFolderDescription().getLanguageSettingForFile("a.c");
		ICLanguageSettingEntry macros[] = ls.getSettingEntries(ICSettingEntry.MACRO);
		ICLanguageSettingEntry includes[] = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		assertFalse(Arrays.asList(macros).contains(entries[0]));
		assertFalse(Arrays.asList(macros).contains(entries[1]));
		assertFalse(Arrays.asList(includes).contains(entries[2]));
		assertFalse(Arrays.asList(includes).contains(entries[3]));
		Map<String, String> map = new HashMap<String, String>();
		map.put(projectName5, "");
		dess[0].setReferenceInfo(map);
		ICLanguageSettingEntry updatedMacros[] = ls.getSettingEntries(ICSettingEntry.MACRO);
		ICLanguageSettingEntry udatedIncludes[] = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		assertTrue(Arrays.asList(updatedMacros).contains(entries[0]));
		assertTrue(Arrays.asList(updatedMacros).contains(entries[1]));
		assertTrue(Arrays.asList(udatedIncludes).contains(entries[2]));
		assertTrue(Arrays.asList(udatedIncludes).contains(entries[3]));
	}

	/**
	 * This tests importing projects with references works even if the projects are
	 * imported in opposite order
	 * @throws Exception
	 */
	public void testProjectImport() throws Exception {
		final IProject libProj = ResourceHelper.createCDTProjectWithConfig("libProj");
		final IProject mainProj = ResourceHelper.createCDTProjectWithConfig("mainProj");

		// set the settings on the lib config; reference it from the main config
		{
			ICProjectDescription desLib = coreModel.getProjectDescription(libProj);
			ICProjectDescription desMain = coreModel.getProjectDescription(mainProj);
			ICConfigurationDescription cfgLib = desLib.getActiveConfiguration();
			ICConfigurationDescription cfgMain = desMain.getActiveConfiguration();

			cfgLib.createExternalSetting(null, null, null, entries);
			coreModel.setProjectDescription(libProj, desLib);

			// Main Project references lib project
			cfgMain.setReferenceInfo(new HashMap<String, String>() {{ put(libProj.getName(), ""); }});
			coreModel.setProjectDescription(mainProj, desMain);

			// Referenced settings have been picked up
			for (ICLanguageSettingEntry e : entries) {
				assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
			}
		}

		// Now delete the two projects, import main first
		// then lib and check we're A-Ok
		libProj.delete(false, false, null);
		mainProj.delete(false, false, null);

		// project description obviously no longer eixsts
		assertNull(coreModel.getProjectDescription(mainProj));
		assertNull(coreModel.getProjectDescription(libProj));

		// Re-import the main project first
		mainProj.create(null);
		mainProj.open(null);

		// Now re-open the lib project
		assertFalse(libProj.exists());
		libProj.create(null);
		libProj.open(null);

		// Referenced settings should still exist
		ICConfigurationDescription cfgMain = coreModel.getProjectDescription(mainProj, false).getActiveConfiguration();
		ICConfigurationDescription cfgLib = coreModel.getProjectDescription(libProj, false).getActiveConfiguration();

		checkEquivContents(cfgLib.getExternalSettings()[0].getEntries(), entries);
		for (ICLanguageSettingEntry e : entries) {
			assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
				getSettingEntriesList(e.getKind()).contains(e));
		}
	}

	/**
	 * Tests that updating the exported settings on the active configuration, without updating the set
	 * of references, is correctly picked up in referencing projects.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=312575
	 */
	public void testUpdateExportedSettingsActiveCfg() throws Exception {
		final IProject libProj = ResourceHelper.createCDTProjectWithConfig("libUpdateExtSettings");
		final IProject mainProj = ResourceHelper.createCDTProjectWithConfig("mainProjUpdateExtSettings");

		{
			// set the settings on the lib config; reference it from the main config
			ICProjectDescription desLib = coreModel.getProjectDescription(libProj);
			ICProjectDescription desMain = coreModel.getProjectDescription(mainProj);
			ICConfigurationDescription cfgLib = desLib.getActiveConfiguration();
			ICConfigurationDescription cfgMain = desMain.getActiveConfiguration();

			cfgLib.createExternalSetting(null, null, null, entries);
			coreModel.setProjectDescription(libProj, desLib);

			// Main Project references lib project
			cfgMain.setReferenceInfo(new HashMap<String, String>() {{ put(libProj.getName(), ""); }});
			coreModel.setProjectDescription(mainProj, desMain);

			// Referenced settings have been picked up
			for (ICLanguageSettingEntry e : entries) {
				assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
			}
		}

		{
			// Now apply different language setting entries
			ICProjectDescription desLib = coreModel.getProjectDescription(libProj);
			ICConfigurationDescription cfgLib = desLib.getActiveConfiguration();
			cfgLib.removeExternalSetting(cfgLib.getExternalSettings()[0]);
			cfgLib.createExternalSetting(null, null, null, entries2);
			coreModel.setProjectDescription(libProj, desLib);

			// Check the exported settings is correct
			checkEquivContents(entries2, cfgLib.getExternalSettings()[0].getEntries());
			// Fetch the configuration a-fresh to pick up the settings
			ICConfigurationDescription cfgMain = coreModel.getProjectDescription(mainProj, false).getActiveConfiguration();
			assertTrue(cfgMain.getReferenceInfo().containsKey(libProj.getName()));

			// Referenced settings have changed from entries -> entries2
			for (ICLanguageSettingEntry e : entries)
				assertTrue(!cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
			for (ICLanguageSettingEntry e : entries2)
				assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
		}
	}

	/**
	 * This is the same as testeUpdateExprtedSettingsActiveCfg except we explicitly name the
	 * referenced configuration.
	 * Tests that updating the exported settings on a name configuration, without updating the set
	 * of references, is correctly picked up in referencing projects
	 */
	public void testUpdateExportedSettingsNamedConfig() throws Exception {
		final IProject libProj = ResourceHelper.createCDTProjectWithConfig("libUpdateExpSettingsNamed");
		final IProject mainProj = ResourceHelper.createCDTProjectWithConfig("mainProjUpdateExpSettingsNamed");

		{
			// set the settings on the lib config; reference it from the main config
			ICProjectDescription desLib = coreModel.getProjectDescription(libProj);
			ICProjectDescription desMain = coreModel.getProjectDescription(mainProj);
			final ICConfigurationDescription cfgLib = desLib.getActiveConfiguration();
			ICConfigurationDescription cfgMain = desMain.getActiveConfiguration();

			cfgLib.createExternalSetting(null, null, null, entries);
			coreModel.setProjectDescription(libProj, desLib);

			// Main Project references lib project
			cfgMain.setReferenceInfo(new HashMap<String, String>() {{ put(libProj.getName(), cfgLib.getId()); }});
			coreModel.setProjectDescription(mainProj, desMain);

			// Referenced settings have been picked up
			for (ICLanguageSettingEntry e : entries) {
				assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
			}
		}

		{
			// Now apply different language setting entries
			ICProjectDescription desLib = coreModel.getProjectDescription(libProj);
			ICConfigurationDescription cfgLib = desLib.getActiveConfiguration();
			cfgLib.removeExternalSetting(cfgLib.getExternalSettings()[0]);
			cfgLib.createExternalSetting(null, null, null, entries2);
			coreModel.setProjectDescription(libProj, desLib);

			// Check the exported settings is correct
			checkEquivContents(entries2, cfgLib.getExternalSettings()[0].getEntries());
			// Fetch the configuration a-fresh to pick up the settings
			ICConfigurationDescription cfgMain = coreModel.getProjectDescription(mainProj, false).getActiveConfiguration();
			assertTrue(cfgMain.getReferenceInfo().get(libProj.getName()).equals(cfgLib.getId()));

			// Referenced settings have changed from entries -> entries2
			for (ICLanguageSettingEntry e : entries)
				assertTrue(!cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
			for (ICLanguageSettingEntry e : entries2)
				assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
		}
	}

	/**
	 * Bug 312575 tests that updating a .cproject external (e.g. via a repository update)
	 * causes referencing projects to correctly pick up changes to the project exports.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=312575
	 */
	public void _testExportedSettingsExternalUpdate() throws Exception {
		final IProject libProj = ResourceHelper.createCDTProjectWithConfig("libProj312575");
		final IProject mainProj = ResourceHelper.createCDTProjectWithConfig("mainProj312575");

		// .cproject file and its backup
		IFile libCproject = libProj.getFile(".cproject");
		IFile libCproject_back = libProj.getFile(".cproject_back");

		// set the settings on the lib config; reference it from the main config
		{
			ICProjectDescription desLib = coreModel.getProjectDescription(libProj);
			ICProjectDescription desMain = coreModel.getProjectDescription(mainProj);
			final ICConfigurationDescription cfgLib = desLib.getActiveConfiguration();
			ICConfigurationDescription cfgMain = desMain.getActiveConfiguration();

			cfgLib.createExternalSetting(null, null, null, entries);
			coreModel.setProjectDescription(libProj, desLib);

			// Main Project references lib project
			cfgMain.setReferenceInfo(new HashMap<String, String>() {{ put(libProj.getName(), cfgLib.getId()); }});
			coreModel.setProjectDescription(mainProj, desMain);
			checkEquivContents(entries, cfgLib.getExternalSettings()[0].getEntries());

			// Referenced settings have been picked up
			for (ICLanguageSettingEntry e : entries) {
				assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
			}

			// Backup the .cproject
			libCproject_back.create(libCproject.getContents(), false, null);

			// Now apply different language setting entries
			cfgLib.removeExternalSetting(cfgLib.getExternalSettings()[0]);
			cfgLib.createExternalSetting(null, null, null, entries2);
			coreModel.setProjectDescription(libProj, desLib);
			checkEquivContents(entries2, cfgLib.getExternalSettings()[0].getEntries());
			// Referenced settings have been picked up
			for (ICLanguageSettingEntry e : entries2) {
				assertTrue(coreModel.getProjectDescription(mainProj).getActiveConfiguration().getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
			}
		}

		// Now replace the .cproject with .cproject_back. The exported settings should be picked up in the referenced config
		libCproject.setContents(libCproject_back.getContents(), IResource.NONE, null);

		// Referenced settings should still exist
		ICConfigurationDescription cfgLib = coreModel.getProjectDescription(libProj, false).getActiveConfiguration();
		ICConfigurationDescription cfgMain = coreModel.getProjectDescription(mainProj, false).getActiveConfiguration();

		checkEquivContents(entries, cfgLib.getExternalSettings()[0].getEntries());
		// Referencing project contains entries and not entries2
		for (ICLanguageSettingEntry e : entries) {
			assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
				getSettingEntriesList(e.getKind()).contains(e));
		}
		for (ICLanguageSettingEntry e : entries2) {
			assertTrue(!cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
				getSettingEntriesList(e.getKind()).contains(e));
		}
	}

	/**
	 * Two configurations in two projects reference each other.
	 * Test that the system is happy with this and updates each correctly on
	 * the other's change.
	 */
	public void _testExportedSettingsCyclicExternalUpdate() throws Exception {
		final IProject libProj = ResourceHelper.createCDTProjectWithConfig("libProjCyclic");
		final IProject mainProj = ResourceHelper.createCDTProjectWithConfig("libProjCyclic2");

		// .cproject file and its backup
		final IFile libCproject = libProj.getFile(".cproject");
		final IFile libCproject_back = libProj.getFile(".cproject_back");
		final IFile mainCproject = mainProj.getFile(".cproject");
		final IFile mainCproject_back = mainProj.getFile(".cproject_back");

		// set the settings on the lib config; reference it from the main config & vice versa
		{
			ICProjectDescription desLib = coreModel.getProjectDescription(libProj);
			ICProjectDescription desMain = coreModel.getProjectDescription(mainProj);
			final ICConfigurationDescription cfgLib = desLib.getActiveConfiguration();
			final ICConfigurationDescription cfgMain = desMain.getActiveConfiguration();

			// Lib Exports entries
			cfgLib.createExternalSetting(null, null, null, entries);
			// Main Exports entries2
			cfgMain.createExternalSetting(null, null, null, entries2);

			// Main Project references lib project
			cfgMain.setReferenceInfo(new HashMap<String, String>() {{ put(libProj.getName(), cfgLib.getId()); }});
			cfgLib.setReferenceInfo(new HashMap<String, String>() {{ put(mainProj.getName(), cfgMain.getId()); }});
			coreModel.setProjectDescription(libProj, desLib);
			coreModel.setProjectDescription(mainProj, desMain);

			checkEquivContents(entries, cfgLib.getExternalSettings()[0].getEntries());
			checkEquivContents(entries2, cfgMain.getExternalSettings()[0].getEntries());

			// Backup the .cproject
			libCproject_back.create(libCproject.getContents(), false, null);
			mainCproject_back.create(mainCproject.getContents(), false, null);

			// Now apply different language setting entries
			cfgLib.removeExternalSetting(cfgLib.getExternalSettings()[0]);
			cfgLib.createExternalSetting(null, null, null, entries2);
			coreModel.setProjectDescription(libProj, desLib);
			cfgMain.removeExternalSetting(cfgMain.getExternalSettings()[0]);
			cfgMain.createExternalSetting(null, null, null, entries);
			coreModel.setProjectDescription(mainProj, desMain);

			checkEquivContents(entries2, cfgLib.getExternalSettings()[0].getEntries());
			checkEquivContents(entries, cfgMain.getExternalSettings()[0].getEntries());

		}
		ICConfigurationDescription cfgLib = coreModel.getProjectDescription(libProj).getActiveConfiguration();
		ICConfigurationDescription cfgMain = coreModel.getProjectDescription(mainProj).getActiveConfiguration();

		// Tests the exports are now the right way round
		for (ICLanguageSettingEntry e : entries) {
			assertTrue(!cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
				getSettingEntriesList(e.getKind()).contains(e));
			assertTrue(cfgLib.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
		}
		for (ICLanguageSettingEntry e : entries2) {
			assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
				getSettingEntriesList(e.getKind()).contains(e));
			assertTrue(!cfgLib.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
		}

		// Now replace the .cproject with .cproject_back. The exported settings should be picked up in the referenced config
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				libCproject.setContents(libCproject_back.getContents(), IResource.NONE, null);
				mainCproject.setContents(mainCproject_back.getContents(), IResource.NONE, null);
			}
		}, null);

		// Referenced settings should still exist
		cfgLib = coreModel.getProjectDescription(libProj).getActiveConfiguration();
		cfgMain = coreModel.getProjectDescription(mainProj).getActiveConfiguration();

		checkEquivContents(entries, cfgLib.getExternalSettings()[0].getEntries());
		checkEquivContents(entries2, cfgMain.getExternalSettings()[0].getEntries());
		// Referencing project contains entries and not entries2
		for (ICLanguageSettingEntry e : entries) {
			assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
				getSettingEntriesList(e.getKind()).contains(e));
			assertTrue(!cfgLib.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
		}
		for (ICLanguageSettingEntry e : entries2) {
			assertTrue(!cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
				getSettingEntriesList(e.getKind()).contains(e));
			assertTrue(cfgLib.getRootFolderDescription().getLanguageSettingForFile("a.c").
					getSettingEntriesList(e.getKind()).contains(e));
		}
	}


	private void checkEquivContents(Object[] expected, Object[] actual) {
		if(expected == null){
			assertTrue(actual == null);
			return;
		}
		assertTrue(actual != null);
		assertEquals(expected.length, actual.length);

		Set s1 = new HashSet(Arrays.asList(expected));
		Set s2 = new HashSet(Arrays.asList(actual));
		assertEquals(expected.length, s1.size());
		assertEquals(s1, s2);
	}

}
