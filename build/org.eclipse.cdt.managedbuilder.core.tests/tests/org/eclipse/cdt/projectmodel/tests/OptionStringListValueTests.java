/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.projectmodel.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.cdt.managedbuilder.testplugin.BuildSystemTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class OptionStringListValueTests extends TestCase {
	private static final String PROJ_NAME_PREFIX = "OptionStringListValueTests_";

	public static Test suite() {
		return new TestSuite(OptionStringListValueTests.class);
	}
	
	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}
	
	public void testCfgDesEntries() throws Exception {
		String projName = PROJ_NAME_PREFIX + "1";
		IProject project = BuildSystemTestHelper.createProject(projName, null, "cdt.managedbuild.target.gnu30.exe");
		CoreModel model = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = model.getProjectDescriptionManager();
		
		ICProjectDescription des = mngr.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		
		ICFolderDescription fDes = cfgDes.getRootFolderDescription();
		IFolderInfo fInfo = cfg.getRootFolderInfo();
		
		ICLanguageSetting ls = fDes.getLanguageSettingForFile("a.c");
		List list = new ArrayList();
		list.add(new CIncludePathEntry("a", 0));
		list.add(new CIncludePathEntry("b", 0));
		list.addAll(ls.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH));
		ls.setSettingEntries(ICSettingEntry.INCLUDE_PATH, list);
		
		List returned = ls.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);
		assertEquals(list.size(), returned.size());
		assertTrue(Arrays.equals(list.toArray(), returned.toArray()));
		
		mngr.setProjectDescription(project, des);
		
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = wsp.getRoot(); 
		project.delete(false, true, new NullProgressMonitor());
		
		project = root.getProject(projName);
		des = mngr.getProjectDescription(project);
		assertNull("project description is not null for removed project", des);
		
		project = BuildSystemTestHelper.createProject(projName);
		des = mngr.getProjectDescription(project);
		assertNotNull("project description is null for re-created project", des);
		assertTrue("des should be valid for re-created project", des.isValid());
		
		cfgDes = des.getConfigurations()[0];
		fDes = cfgDes.getRootFolderDescription();
		ls = fDes.getLanguageSettingForFile("a.c");
		
		returned = ls.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);
		assertEquals(list.size(), returned.size());
		assertTrue(Arrays.equals(list.toArray(), returned.toArray()));
	}
	
	public void testLibFiles() throws Exception {
		String projName = PROJ_NAME_PREFIX + "2";
		IProject project = BuildSystemTestHelper.createProject(projName, null, "lv.tests.ptype");
		CoreModel model = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = model.getProjectDescriptionManager();
		
		ICProjectDescription des = mngr.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		
		ICFolderDescription fDes = cfgDes.getRootFolderDescription();
		IFolderInfo fInfo = cfg.getRootFolderInfo();
		
		ICLanguageSetting ls = fDes.getLanguageSettingForFile("a.c");
		List list = new ArrayList();
		list.add(new CLibraryFileEntry("usr_a", 0, new Path("ap"), new Path("arp"), new Path("apx")));
		list.add(new CLibraryFileEntry("usr_b", 0, new Path("bp"), null, null));
		list.add(new CLibraryFileEntry("usr_c", 0, new Path("cp"), new Path("crp"), null));
		list.add(new CLibraryFileEntry("usr_d", 0, new Path("dp"), null, new Path("dpx")));
		list.addAll(ls.getSettingEntriesList(ICSettingEntry.LIBRARY_FILE));
		ls.setSettingEntries(ICSettingEntry.LIBRARY_FILE, list);
		
		ICLanguageSettingEntry[] resolved = ls.getResolvedSettingEntries(ICSettingEntry.LIBRARY_FILE);
		assertEquals(list.size(), resolved.length);
		for(int i = 0; i < resolved.length; i++){
			ICLibraryFileEntry other = (ICLibraryFileEntry)list.get(i);
			ICLibraryFileEntry r = (ICLibraryFileEntry)resolved[i];
			assertEquals(other.getName(), r.getName());
			assertEquals(other.getSourceAttachmentPath(), r.getSourceAttachmentPath());
			assertEquals(other.getSourceAttachmentRootPath(), r.getSourceAttachmentRootPath());
			assertEquals(other.getSourceAttachmentPrefixMapping(), r.getSourceAttachmentPrefixMapping());
		}
		
		List returned = ls.getSettingEntriesList(ICSettingEntry.LIBRARY_FILE);
		assertEquals(list.size(), returned.size());
		assertTrue(Arrays.equals(list.toArray(), returned.toArray()));
		
		mngr.setProjectDescription(project, des);
		
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = wsp.getRoot(); 
		project.delete(false, true, new NullProgressMonitor());
		
		project = root.getProject(projName);
		des = mngr.getProjectDescription(project);
		assertNull("project description is not null for removed project", des);
		
		project = BuildSystemTestHelper.createProject(projName);
		des = mngr.getProjectDescription(project);
		assertNotNull("project description is null for re-created project", des);
		assertTrue("des should be valid for re-created project", des.isValid());
		
		cfgDes = des.getConfigurations()[0];
		fDes = cfgDes.getRootFolderDescription();
		ls = fDes.getLanguageSettingForFile("a.c");
		
		returned = ls.getSettingEntriesList(ICSettingEntry.LIBRARY_FILE);
		checkEntriesMatch(list, returned);
		assertEquals(list.size(), returned.size());
		assertTrue(Arrays.equals(list.toArray(), returned.toArray()));
	}
	
	private Set[] diff(List list1, List list2){
		Set set1 = new LinkedHashSet(list1);
		set1.removeAll(list2);
		Set set2 = new LinkedHashSet(list2);
		set2.removeAll(list1);
		if(set1.size() == 0 && set2.size() == 0)
			return null;
		return new Set[]{set1, set2};
	}
	
	private void checkEntriesMatch(List list1, List list2){
		Set[] diff = diff(list1, list2);
		if(diff != null){
			fail("entries diff");
		}
	}
	
	private static String[] toValues(OptionStringValue[] ves){
		String[] values = new String[ves.length];
		for(int i = 0; i < ves.length; i++){
			values[i] = ves[i].getValue();
		}
		return values;
	}
	
	private static void checkOptionValues(IOption option) throws Exception {
		List list = (List)option.getValue();
		String values[] = (String[])list.toArray(new String[list.size()]);
		String[] values2 = option.getBasicStringListValue();
		OptionStringValue[] values3 = option.getBasicStringListValueElements();
		assertTrue(Arrays.equals(values, values2));
		assertTrue(Arrays.equals(values, toValues(values3)));
	}

	public void testOptions() throws Exception {
		String projName = PROJ_NAME_PREFIX + "3";
		IProject project = BuildSystemTestHelper.createProject(projName, null, "lv.tests.ptype");
		CoreModel model = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = model.getProjectDescriptionManager();
		
		ICProjectDescription des = mngr.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		
		ICFolderDescription fDes = cfgDes.getRootFolderDescription();
		IFolderInfo fInfo = cfg.getRootFolderInfo();
		
		ITool tool = fInfo.getToolsBySuperClassId("lv.tests.tool")[0];
		IOption option = tool.getOptionBySuperClassId("lv.tests.libFiles.option");
		
		String[] builtins = option.getBuiltIns();
		assertEquals(1, builtins.length);
		String expectedBIs[] = new String[]{"lf_c"};
		assertTrue(Arrays.equals(expectedBIs, builtins));
		
		checkOptionValues(option);
		
		List list = new ArrayList();
		list.add("usr_1");
		list.add("usr_2");
		list.addAll(Arrays.asList(option.getBasicStringListValue()));
		String[] updated = (String[])list.toArray(new String[0]);
		option = ManagedBuildManager.setOption(fInfo, tool, option, updated);
		
		assertTrue(Arrays.equals(updated, option.getBasicStringListValue()));
		checkOptionValues(option);

		list = new ArrayList();
		list.add(new OptionStringValue("usr_3", false, "ap", "arp", "apx"));
		list.add(new OptionStringValue("usr_4", false, null, null, null));
		list.add(new OptionStringValue("usr_5", false, "cp", null, null));
		list.add(new OptionStringValue("usr_6", false, "dp", null, "dpx"));
		list.add(new OptionStringValue("usr_6", false, null, null, "epx"));
		list.addAll(Arrays.asList(option.getBasicStringListValueElements()));
		
		OptionStringValue updatedves[] = (OptionStringValue[])list.toArray(new OptionStringValue[0]);
		IOption updatedOption = ManagedBuildManager.setOption(fInfo, tool, option, updatedves); 
		assertTrue(option == updatedOption);
		OptionStringValue[] ves = option.getBasicStringListValueElements();
		assertEquals(updatedves.length, ves.length);
		assertTrue(Arrays.equals(updatedves, ves));
		checkOptionValues(option);
		
		mngr.setProjectDescription(project, des);
		
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = wsp.getRoot(); 
		project.delete(false, true, new NullProgressMonitor());
		
		project = root.getProject(projName);
		des = mngr.getProjectDescription(project);
		assertNull("project description is not null for removed project", des);
		
		project = BuildSystemTestHelper.createProject(projName);
		des = mngr.getProjectDescription(project);
		assertNotNull("project description is null for re-created project", des);
		assertTrue("des should be valid for re-created project", des.isValid());
		
		cfgDes = des.getConfigurations()[0];
		fDes = cfgDes.getRootFolderDescription();
		
		cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		
		fInfo = cfg.getRootFolderInfo();
		
		tool = fInfo.getToolsBySuperClassId("lv.tests.tool")[0];
		option = tool.getOptionBySuperClassId("lv.tests.libFiles.option");
		
		ves = option.getBasicStringListValueElements();
		assertTrue(Arrays.equals(updatedves, ves));
		checkOptionValues(option);
	}
	
}
