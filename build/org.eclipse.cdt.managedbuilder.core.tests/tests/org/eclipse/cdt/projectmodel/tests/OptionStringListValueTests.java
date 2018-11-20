/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.managedbuilder.core.BuildException;
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
		ResourceHelper.cleanUp(getName());
	}

	public void testCfgDesEntries() throws Exception {
		String projName = PROJ_NAME_PREFIX + "1";
		IProject project = BuildSystemTestHelper.createProject(projName, null, "cdt.managedbuild.target.gnu30.exe");
		ResourceHelper.addResourceCreated(project);
		CoreModel model = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = model.getProjectDescriptionManager();

		ICProjectDescription des = mngr.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);

		ICFolderDescription fDes = cfgDes.getRootFolderDescription();
		IFolderInfo fInfo = cfg.getRootFolderInfo();

		ICLanguageSetting ls = fDes.getLanguageSettingForFile("a.c");
		List<ICLanguageSettingEntry> list = new ArrayList<>();
		list.add(new CIncludePathEntry("a", 0));
		list.add(new CIncludePathEntry("b", 0));
		list.addAll(ls.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH));
		ls.setSettingEntries(ICSettingEntry.INCLUDE_PATH, list);

		List<ICLanguageSettingEntry> returned = ls.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);
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
		ResourceHelper.addResourceCreated(project);
		CoreModel model = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = model.getProjectDescriptionManager();

		ICProjectDescription des = mngr.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);

		ICFolderDescription fDes = cfgDes.getRootFolderDescription();

		ICLanguageSetting ls = fDes.getLanguageSettingForFile("a.c");
		List<ICLanguageSettingEntry> list = new ArrayList<>();
		list.add(new CLibraryFileEntry("usr_a", 0, new Path("ap"), new Path("arp"), new Path("apx")));
		list.add(new CLibraryFileEntry("usr_b", 0, new Path("bp"), null, null));
		list.add(new CLibraryFileEntry("usr_c", 0, new Path("cp"), new Path("crp"), null));
		list.add(new CLibraryFileEntry("usr_d", 0, new Path("dp"), null, new Path("dpx")));
		list.addAll(ls.getSettingEntriesList(ICSettingEntry.LIBRARY_FILE));
		ls.setSettingEntries(ICSettingEntry.LIBRARY_FILE, list);

		ICLanguageSettingEntry[] resolved = ls.getResolvedSettingEntries(ICSettingEntry.LIBRARY_FILE);
		assertEquals(list.size(), resolved.length);
		for (int i = 0; i < resolved.length; i++) {
			ICLibraryFileEntry other = (ICLibraryFileEntry) list.get(i);
			ICLibraryFileEntry r = (ICLibraryFileEntry) resolved[i];
			assertEquals(other.getName(), r.getName());
			assertEquals(other.getSourceAttachmentPath(), r.getSourceAttachmentPath());
			assertEquals(other.getSourceAttachmentRootPath(), r.getSourceAttachmentRootPath());
			assertEquals(other.getSourceAttachmentPrefixMapping(), r.getSourceAttachmentPrefixMapping());
		}

		List<ICLanguageSettingEntry> returned = ls.getSettingEntriesList(ICSettingEntry.LIBRARY_FILE);
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

	private void checkEntriesMatch(List<ICLanguageSettingEntry> list1, List<ICLanguageSettingEntry> list2) {
		Set<ICLanguageSettingEntry> set1 = new LinkedHashSet<>(list1);
		set1.removeAll(list2);
		Set<ICLanguageSettingEntry> set2 = new LinkedHashSet<>(list2);
		set2.removeAll(list1);
		if (set1.size() != 0 || set2.size() != 0) {
			fail("entries diff");
		}
	}

	private static String[] toValues(OptionStringValue[] ves) {
		String[] values = new String[ves.length];
		for (int i = 0; i < ves.length; i++) {
			values[i] = ves[i].getValue();
		}
		return values;
	}

	private static void checkOptionValues(IOption option) throws Exception {
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) option.getValue();
		String values[] = list.toArray(new String[list.size()]);
		String[] values2 = option.getBasicStringListValue();
		OptionStringValue[] values3 = option.getBasicStringListValueElements();
		assertTrue(Arrays.equals(values, values2));
		assertTrue(Arrays.equals(values, toValues(values3)));
	}

	public void testOptions() throws Exception {
		String projName = PROJ_NAME_PREFIX + "3";
		IProject project = BuildSystemTestHelper.createProject(projName, null, "lv.tests.ptype");
		ResourceHelper.addResourceCreated(project);
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
		String expectedBIs[] = new String[] { "lf_c" };
		assertTrue(Arrays.equals(expectedBIs, builtins));

		checkOptionValues(option);

		List<Object> list = new ArrayList<>();
		list.add("usr_1");
		list.add("usr_2");
		list.addAll(Arrays.asList(option.getBasicStringListValue()));
		String[] updated = list.toArray(new String[0]);
		option = ManagedBuildManager.setOption(fInfo, tool, option, updated);

		assertTrue(Arrays.equals(updated, option.getBasicStringListValue()));
		checkOptionValues(option);

		list = new ArrayList<>();
		list.add(new OptionStringValue("usr_3", false, "ap", "arp", "apx"));
		list.add(new OptionStringValue("usr_4", false, null, null, null));
		list.add(new OptionStringValue("usr_5", false, "cp", null, null));
		list.add(new OptionStringValue("usr_6", false, "dp", null, "dpx"));
		list.add(new OptionStringValue("usr_6", false, null, null, "epx"));
		list.addAll(Arrays.asList(option.getBasicStringListValueElements()));

		OptionStringValue updatedves[] = list.toArray(new OptionStringValue[0]);
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

	public void testSetToEmptyList_bug531106() throws Exception {
		String projName = PROJ_NAME_PREFIX + "_bug531106";
		IProject project = BuildSystemTestHelper.createProject(projName, null, "bug531106.tests.ptype");
		ResourceHelper.addResourceCreated(project);
		CoreModel model = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = model.getProjectDescriptionManager();

		ICProjectDescription des = mngr.getProjectDescription(project);
		ICConfigurationDescription cfgDes = des.getConfigurations()[0];
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);

		ICFolderDescription fDes = cfgDes.getRootFolderDescription();
		IFolderInfo fInfo = cfg.getRootFolderInfo();

		ITool tool = fInfo.getToolsBySuperClassId("bug531106.tests.tool")[0];
		testSetToEmptyList_VerifyValueCount(fInfo, tool, 1);

		//Test clearing
		IOption slOption = tool.getOptionBySuperClassId("bug531106.tests.option.stringList");
		ManagedBuildManager.setOption(fInfo, tool, slOption, new OptionStringValue[0]);
		IOption incPathOption = tool.getOptionBySuperClassId("bug531106.tests.option.incPath");
		ManagedBuildManager.setOption(fInfo, tool, incPathOption, new OptionStringValue[0]);
		IOption symbolsOption = tool.getOptionBySuperClassId("bug531106.tests.option.symbols");
		ManagedBuildManager.setOption(fInfo, tool, symbolsOption, new OptionStringValue[0]);
		IOption libsOption = tool.getOptionBySuperClassId("bug531106.tests.option.libs");
		ManagedBuildManager.setOption(fInfo, tool, libsOption, new OptionStringValue[0]);
		IOption userObjsOption = tool.getOptionBySuperClassId("bug531106.tests.option.userObjs");
		ManagedBuildManager.setOption(fInfo, tool, userObjsOption, new OptionStringValue[0]);
		IOption symFilesOption = tool.getOptionBySuperClassId("bug531106.tests.option.symFiles");
		ManagedBuildManager.setOption(fInfo, tool, symFilesOption, new OptionStringValue[0]);
		IOption incFilesOption = tool.getOptionBySuperClassId("bug531106.tests.option.incFiles");
		ManagedBuildManager.setOption(fInfo, tool, incFilesOption, new OptionStringValue[0]);
		IOption libPathsOption = tool.getOptionBySuperClassId("bug531106.tests.option.libPaths");
		ManagedBuildManager.setOption(fInfo, tool, libPathsOption, new OptionStringValue[0]);
		IOption libFilesOption = tool.getOptionBySuperClassId("bug531106.tests.option.libFiles");
		ManagedBuildManager.setOption(fInfo, tool, libFilesOption, new OptionStringValue[0]);

		testSetToEmptyList_VerifyValueCount(fInfo, tool, 0);

		mngr.setProjectDescription(project, des);
		ManagedBuildManager.saveBuildInfo(project, true);

		//Close & re-open project
		project.close(new NullProgressMonitor());
		project.open(new NullProgressMonitor());

		//Reload config
		des = mngr.getProjectDescription(project);
		cfgDes = des.getConfigurations()[0];
		cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);

		fDes = cfgDes.getRootFolderDescription();
		fInfo = cfg.getRootFolderInfo();

		tool = fInfo.getToolsBySuperClassId("bug531106.tests.tool")[0];
		testSetToEmptyList_VerifyValueCount(fInfo, tool, 0);
	}

	private void testSetToEmptyList_VerifyValueCount(IFolderInfo fInfo, ITool tool, int count) throws BuildException {
		IOption slOption = tool.getOptionBySuperClassId("bug531106.tests.option.stringList");
		assertEquals(count, slOption.getBasicStringListValueElements().length);
		IOption incPathOption = tool.getOptionBySuperClassId("bug531106.tests.option.incPath");
		assertEquals(count, incPathOption.getBasicStringListValueElements().length);
		IOption symbolsOption = tool.getOptionBySuperClassId("bug531106.tests.option.symbols");
		assertEquals(count, symbolsOption.getBasicStringListValueElements().length);
		IOption libsOption = tool.getOptionBySuperClassId("bug531106.tests.option.libs");
		assertEquals(count, libsOption.getBasicStringListValueElements().length);
		IOption userObjsOption = tool.getOptionBySuperClassId("bug531106.tests.option.userObjs");
		assertEquals(count, userObjsOption.getBasicStringListValueElements().length);
		IOption symFilesOption = tool.getOptionBySuperClassId("bug531106.tests.option.symFiles");
		assertEquals(count, symFilesOption.getBasicStringListValueElements().length);
		IOption incFilesOption = tool.getOptionBySuperClassId("bug531106.tests.option.incFiles");
		assertEquals(count, incFilesOption.getBasicStringListValueElements().length);
		IOption libPathsOption = tool.getOptionBySuperClassId("bug531106.tests.option.libPaths");
		assertEquals(count, libPathsOption.getBasicStringListValueElements().length);
		IOption libFilesOption = tool.getOptionBySuperClassId("bug531106.tests.option.libFiles");
		assertEquals(count, libFilesOption.getBasicStringListValueElements().length);
	}

}
