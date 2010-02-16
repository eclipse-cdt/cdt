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
import org.eclipse.core.resources.IProject;

/**
 * Class for testing exported settings and project references
 */
public class CConfigurationDescriptionExportSettings extends BaseTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ResourceHelper.cleanUp();
	}

	public static TestSuite suite() {
		return suite(CConfigurationDescriptionExportSettings.class, "_");
	}

	/**
	 * This tests for simple reference propagation.
	 * It used to live in the Managedbuild testsuite in ProjectModelTests.java
	 * but is moved here as it doesn't test any managedbuilder specific functionality
	 */
	public void testReferences() throws Exception {
		final String projectName4 = "test4";
		final String projectName5 = "test5";
		CoreModel coreModel = CoreModel.getDefault();

		IProject project4 = ResourceHelper.createCDTProjectWithConfig(projectName4);
		IProject project5 = ResourceHelper.createCDTProjectWithConfig(projectName5);

		ICProjectDescription des4 = coreModel.getProjectDescription(project4);
		ICProjectDescription des5 = coreModel.getProjectDescription(project5);
		ICConfigurationDescription dess[] = des5.getConfigurations();

		ICLanguageSettingEntry entries[] = new ICLanguageSettingEntry[]{
				new CMacroEntry("a", "b", 0),
				new CMacroEntry("c", "d", 0),
				new CIncludePathEntry("a/b/c", 0),
				new CIncludePathEntry("d/e/f", 0),
		};
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
		CoreModel coreModel = CoreModel.getDefault();
		final IProject libProj = ResourceHelper.createCDTProjectWithConfig("libProj");
		final IProject mainProj = ResourceHelper.createCDTProjectWithConfig("mainProj");

		// Settings to set on the lib project
		final ICLanguageSettingEntry entries[] = new ICLanguageSettingEntry[]{
				new CMacroEntry("a", "b", 0),
				new CMacroEntry("c", "d", 0),
				new CIncludePathEntry("a/b/c", 0),
				new CIncludePathEntry("d/e/f", 0),
		};

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
		ICConfigurationDescription cfgMain = coreModel.getProjectDescription(mainProj).getActiveConfiguration();
		ICConfigurationDescription cfgLib = coreModel.getProjectDescription(libProj).getActiveConfiguration();

		checkEquivContents(cfgLib.getExternalSettings()[0].getEntries(), entries);
		for (ICLanguageSettingEntry e : entries) {
			assertTrue(cfgMain.getRootFolderDescription().getLanguageSettingForFile("a.c").
				getSettingEntriesList(e.getKind()).contains(e));
		}
	}

	private void checkEquivContents(Object[] a1, Object[] a2) {
		if(a1 == null){
			assertTrue(a2 == null);
			return;
		}
		assertTrue(a2 != null);
		assertEquals(a1.length, a2.length);

		Set s1 = new HashSet(Arrays.asList(a1));
		Set s2 = new HashSet(Arrays.asList(a2));
		assertEquals(a1.length, s1.size());
		assertEquals(s1, s2);
	}

}
