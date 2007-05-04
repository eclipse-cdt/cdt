/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.Arrays;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class ExternalSettingsProviderTests extends BaseTestCase{
	private static final String PROJ_NAME_PREFIX = "espt_";
	ICProject p1, p2;
	
	public static TestSuite suite() {
		return suite(ExternalSettingsProviderTests.class, "_");
	}
	
	protected void setUp() throws Exception {
		p1 = CProjectHelper.createNewStileCProject(PROJ_NAME_PREFIX + "a", IPDOMManager.ID_NO_INDEXER);
		p2 = CProjectHelper.createNewStileCProject(PROJ_NAME_PREFIX + "b", IPDOMManager.ID_NO_INDEXER);
	}
	
	public void testRefs() throws Exception {
		CoreModel model = CoreModel.getDefault();
		IProject project = p1.getProject();
		
		ICProjectDescription des = model.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		ICLanguageSetting ls = cfgDes.getLanguageSettingForFile(new Path("a.c"), true);
		ICLanguageSettingEntry[] entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		assertEquals(0, entries.length);
		ICSourceEntry[] sourceEntries = cfgDes.getSourceEntries();
		ICSourceEntry[] expectedSourceEntries = new ICSourceEntry[]{
			new CSourceEntry(project.getFullPath(), null, ICSettingEntry.RESOLVED)
		};
		assertEquals(1, sourceEntries.length);
		assertTrue(Arrays.equals(expectedSourceEntries, sourceEntries));
		String[] extPIds = new String[]{CTestPlugin.PLUGIN_ID + ".testExtSettingsProvider"};
		cfgDes.setExternalSettingsProviderIds(extPIds);
		assertEquals(extPIds.length, cfgDes.getExternalSettingsProviderIds().length);
		assertTrue(Arrays.equals(extPIds, cfgDes.getExternalSettingsProviderIds()));
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		assertEquals(2, entries.length);
		ICLanguageSettingEntry[] expectedEntries = new ICLanguageSettingEntry[]{
				new CIncludePathEntry("ip_a", 0),
				new CIncludePathEntry("ip_b", 0),
		};
		assertTrue(Arrays.equals(expectedEntries, entries));
		sourceEntries = cfgDes.getSourceEntries();
		assertEquals(2, sourceEntries.length);
		ICSourceEntry[] newExpectedSourceEntries = new ICSourceEntry[]{
				new CSourceEntry(project.getFullPath().append("sp_a"), null, 0),
				new CSourceEntry(project.getFullPath().append("sp_b"), null, 0),
		};
		assertTrue(Arrays.equals(newExpectedSourceEntries, sourceEntries));
		
		ICLanguageSettingEntry[] newEntries = new ICLanguageSettingEntry[3];
		newEntries[0] = expectedEntries[1];
		newEntries[1] = new CIncludePathEntry("added", 0);
		newEntries[2] = expectedEntries[0];
		
		ls.setSettingEntries(ICSettingEntry.INCLUDE_PATH, newEntries);
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		
		assertEquals(3, entries.length);
		assertTrue(Arrays.equals(newEntries, entries));
		
		newEntries = new ICLanguageSettingEntry[1];
		newEntries[0] = expectedEntries[0];
		ls.setSettingEntries(ICSettingEntry.INCLUDE_PATH, newEntries);
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		
		assertEquals(1, entries.length);
		assertTrue(Arrays.equals(newEntries, entries));
		
		newEntries = new ICLanguageSettingEntry[0];
		ls.setSettingEntries(ICSettingEntry.INCLUDE_PATH, newEntries);
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		
		assertEquals(0, entries.length);
		
		ls.setSettingEntries(ICSettingEntry.INCLUDE_PATH, (ICLanguageSettingEntry[])null);
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		assertEquals(2, entries.length);
		assertTrue(Arrays.equals(expectedEntries, entries));
	}
	
	public void testCreateCfg() throws Exception {
		CoreModel model = CoreModel.getDefault();
		IProject project = p2.getProject();
		
		ICProjectDescription des = model.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		ICLanguageSetting ls = cfgDes.getLanguageSettingForFile(new Path("a.c"), true);
		ICLanguageSettingEntry[] entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		assertEquals(0, entries.length);
		ICSourceEntry[] sourceEntries = cfgDes.getSourceEntries();
		ICSourceEntry[] expectedSourceEntries = new ICSourceEntry[]{
			new CSourceEntry(project.getFullPath(), null, ICSettingEntry.RESOLVED)
		};
		assertEquals(1, sourceEntries.length);
		assertTrue(Arrays.equals(expectedSourceEntries, sourceEntries));
		String[] extPIds = new String[]{CTestPlugin.PLUGIN_ID + ".testExtSettingsProvider"};
		cfgDes.setExternalSettingsProviderIds(extPIds);
		
		model.setProjectDescription(project, des);
		
		des = model.getProjectDescription(project, false);
		cfgDes = des.getConfigurations()[0];
		assertEquals(extPIds.length, cfgDes.getExternalSettingsProviderIds().length);
		assertTrue(Arrays.equals(extPIds, cfgDes.getExternalSettingsProviderIds()));
		
		des = model.getProjectDescription(project);
		cfgDes = des.getConfigurations()[0];
		assertEquals(extPIds.length, cfgDes.getExternalSettingsProviderIds().length);
		assertTrue(Arrays.equals(extPIds, cfgDes.getExternalSettingsProviderIds()));
		
		String newCfgId = CDataUtil.genId(null);
		ICConfigurationDescription cfgDes2 = des.createConfiguration(newCfgId, "cfg2", cfgDes);
		assertEquals(extPIds.length, cfgDes2.getExternalSettingsProviderIds().length);
		assertTrue(Arrays.equals(extPIds, cfgDes2.getExternalSettingsProviderIds()));
	}
	
	protected void tearDown() throws Exception {
		try {
			p1.getProject().delete(true, null);
		} catch (CoreException e){
		}
		try {
			p2.getProject().delete(true, null);
		} catch (CoreException e){
		}

	}
}
