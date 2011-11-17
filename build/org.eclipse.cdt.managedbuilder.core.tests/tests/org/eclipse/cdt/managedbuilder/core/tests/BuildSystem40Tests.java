/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.testplugin.BuildSystemTestHelper;
import org.eclipse.cdt.managedbuilder.testplugin.CTestPlugin;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class BuildSystem40Tests  extends TestCase {
	private IPath resourcesLocation = new Path(CTestPlugin.getFileInPlugin(new Path("resources/test40Projects/")).getAbsolutePath());

	public static Test suite() {
		TestSuite suite = new TestSuite(BuildSystem40Tests.class);

		return suite;
	}

	private String platformDependentPath(String cfgFolder, String winPath) {
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			return cfgFolder + '/' + winPath;
		}
		return winPath;
	}

	public void test40() throws Exception{
		String[] makefiles = {
				 "makefile",
				 "objects.mk",
				 "sources.mk",
				 "d1/subdir.mk",
				 "d1/d2/subdir.mk",
				 "d1/d2/d3/subdir.mk",
				 "d1_1/subdir.mk",
				 "d1_1/d2_1/subdir.mk",
				 "dir1/dd/excluded_c/subdir.mk",
				 "dir1/dd/excluded_c/asd/subdir.mk",
				 "dir1/dd/ff/subdir.mk",
			};
		IProject[] projects = createProjects("test_40", null, null, true);
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription des = mngr.getProjectDescription(projects[0]);
		ICConfigurationDescription cfgDes = des.getConfigurationByName("dbg 2");
		assertNotNull(cfgDes);
		des.setActiveConfiguration(cfgDes);
		mngr.setProjectDescription(projects[0], des);
		buildProjects(projects, makefiles);

		des = mngr.getProjectDescription(projects[0]);
		cfgDes = des.getConfigurationByName("Test 4.0 ConfigName.Dbg");
		assertNotNull(cfgDes);
		des.setActiveConfiguration(cfgDes);
		mngr.setProjectDescription(projects[0], des);
		buildProjects(projects, makefiles);

		des = mngr.getProjectDescription(projects[0]);
		cfgDes = des.getConfigurationByName("dbg 3");
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);

		ICFolderDescription foDes = cfgDes.getRootFolderDescription();
		ICLanguageSetting ls = foDes.getLanguageSettingForFile("foo.cpp");
		IFolderInfo foInfo = cfg.getRootFolderInfo();
		Tool tool = (Tool)foInfo.getToolFromInputExtension("cpp");
		IOption option = tool.getOptionsOfType(IOption.INCLUDE_PATH)[0];
		OptionStringValue[] value = option.getBasicStringListValueElements();
		ICLanguageSettingEntry[] entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		ICLanguageSettingEntry[] expectedEntries = new ICLanguageSettingEntry[] {
				new CIncludePathEntry("dbg 3/rel/path", 0),
				new CIncludePathEntry("proj/rel/path", 0),
				new CIncludePathEntry("/abs/path", 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "c:/abs/path"), 0),
				new CIncludePathEntry("/test_40/dir1/dir2/dir3", ICSettingEntry.VALUE_WORKSPACE_PATH/* | ICSettingEntry.RESOLVED*/),
				new CIncludePathEntry("/test_40", ICSettingEntry.VALUE_WORKSPACE_PATH/* | ICSettingEntry.RESOLVED*/),
				new CIncludePathEntry(platformDependentPath("dbg 3", "D:\\docs\\incs"), 0),
		};

		assertTrue(Arrays.equals(entries, expectedEntries));

		ls.setSettingEntries(ICSettingEntry.INCLUDE_PATH, entries);

		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
		assertTrue(Arrays.equals(entries, expectedEntries));

		option = tool.getOptionsOfType(IOption.INCLUDE_PATH)[0];
		OptionStringValue[] modifiedValue = option.getBasicStringListValueElements();
		assertTrue(Arrays.equals(modifiedValue, value));

		{
			List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
			list.addAll(Arrays.asList(entries));
			list.add(new CIncludePathEntry(platformDependentPath("dbg 3", "E:\\tmp\\w"), 0));
			entries = list.toArray(new ICLanguageSettingEntry[0]);
			ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, entries);
			expectedEntries = entries;
			entries = ls.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);
			assertTrue(Arrays.equals(entries, expectedEntries));
		}

		{
			ArrayList<OptionStringValue> list = new ArrayList<OptionStringValue>();
			list.addAll(Arrays.asList(value));
			list.add(new OptionStringValue("\"E:\\tmp\\w\""));
			value = list.toArray(new OptionStringValue[0]);
		}

		option = tool.getOptionsOfType(IOption.INCLUDE_PATH)[0];
		modifiedValue = option.getBasicStringListValueElements();

		assertTrue(Arrays.equals(value, modifiedValue));

		foDes = (ICFolderDescription)cfgDes.getResourceDescription(new Path("d1/d2"), true);
		foInfo = (IFolderInfo)cfg.getResourceInfo(new Path("d1/d2"), true);

		ls = foDes.getLanguageSettingForFile("foo.cpp");
		tool = (Tool)foInfo.getToolFromInputExtension("cpp");
		option = tool.getOptionsOfType(IOption.INCLUDE_PATH)[0];

		expectedEntries = new ICLanguageSettingEntry[] {
				new CIncludePathEntry("dbg 3/d2_rel/path", 0),
				new CIncludePathEntry("d2_proj/rel/path", 0),
				new CIncludePathEntry("/d2_abs/path", 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "c:/d2_abs/path"), 0),
				new CIncludePathEntry("dbg 3/d1_rel/path", 0),
				new CIncludePathEntry("d1_proj/rel/path", 0),
				new CIncludePathEntry("/d1_abs/path", 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "c:/d1_abs/path"), 0),
				new CIncludePathEntry("dbg 3/rel/path", 0),
				new CIncludePathEntry("proj/rel/path", 0),
				new CIncludePathEntry("/abs/path", 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "c:/abs/path"), 0),
				new CIncludePathEntry("/test_40/dir1/dir2/dir3", ICSettingEntry.VALUE_WORKSPACE_PATH/* | ICSettingEntry.RESOLVED*/),
				new CIncludePathEntry("/test_40", ICSettingEntry.VALUE_WORKSPACE_PATH/* | ICSettingEntry.RESOLVED*/),
				new CIncludePathEntry(platformDependentPath("dbg 3", "D:\\docs\\incs"), 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "E:\\tmp\\w"), 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "D:\\d1_docs\\incs"), 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "D:\\d2_docs\\incs"), 0),
		};

		entries = ls.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);

		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		OptionStringValue[] expectedValue = new OptionStringValue[] {
				new OptionStringValue("d2_rel/path"),
				new OptionStringValue("../d2_proj/rel/path"),
				new OptionStringValue("/d2_abs/path"),
				new OptionStringValue("c:/d2_abs/path"),
				new OptionStringValue("${IncludeDefaults}"),
				new OptionStringValue("\"D:\\d2_docs\\incs\""),
		};

		value = option.getBasicStringListValueElements();
		BuildSystemTestHelper.checkDiff(expectedValue, value);

		ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, entries);
		value = option.getBasicStringListValueElements();
		entries = ls.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);
		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		{
			ArrayList<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>(Arrays.asList(entries));
			list.remove(6); //new CIncludePathEntry("/d1_abs/path", 0),
			expectedEntries = list.toArray(new ICLanguageSettingEntry[0]);
			ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, list);
		}

		option = tool.getOptionsOfType(IOption.INCLUDE_PATH)[0];

		expectedValue = new OptionStringValue[] {
				new OptionStringValue("d2_rel/path"),
				new OptionStringValue("../d2_proj/rel/path"),
				new OptionStringValue("/d2_abs/path"),
				new OptionStringValue("c:/d2_abs/path"),
				new OptionStringValue("d1_rel/path"),
				new OptionStringValue("../d1_proj/rel/path"),
//removed				new OptionStringValue("/d1_abs/path"),
				new OptionStringValue("c:/d1_abs/path"),
				new OptionStringValue("rel/path"),
				new OptionStringValue("../proj/rel/path"),
				new OptionStringValue("/abs/path"),
				new OptionStringValue("c:/abs/path"),
				new OptionStringValue("\"${workspace_loc:/test_40/dir1/dir2/dir3}\""),
				new OptionStringValue("\"${workspace_loc:/test_40}\""),
				new OptionStringValue("\"D:\\docs\\incs\""),
				new OptionStringValue("\"E:\\tmp\\w\""),
				new OptionStringValue("\"D:\\d1_docs\\incs\""),
				new OptionStringValue("\"D:\\d2_docs\\incs\""),
		};

		value = option.getBasicStringListValueElements();
		BuildSystemTestHelper.checkDiff(expectedValue, value);

		entries = ls.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		foDes = cfgDes.getRootFolderDescription();
		foInfo = cfg.getRootFolderInfo();
		ls = foDes.getLanguageSettingForFile("foo.cpp");
		tool = (Tool)foInfo.getToolFromInputExtension("cpp");
		option = tool.getOptionsOfType(IOption.INCLUDE_PATH)[0];

		{
			ArrayList<OptionStringValue> list = new ArrayList<OptionStringValue>(Arrays.asList(option.getBasicStringListValueElements()));
			assertTrue(list.remove(new OptionStringValue("${IncludeDefaults}")));
			list.add(0, new OptionStringValue("${IncludeDefaults}"));
			expectedValue = list.toArray(new OptionStringValue[0]);
			option = foInfo.setOption(tool, option, list.toArray(new OptionStringValue[0]));
			value = option.getBasicStringListValueElements();
		}

		expectedEntries = new ICLanguageSettingEntry[] {
				new CIncludePathEntry("dbg 3/rel/path", 0),
				new CIncludePathEntry("proj/rel/path", 0),
				new CIncludePathEntry("/abs/path", 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "c:/abs/path"), 0),
				new CIncludePathEntry("/test_40/dir1/dir2/dir3", ICSettingEntry.VALUE_WORKSPACE_PATH/* | ICSettingEntry.RESOLVED*/),
				new CIncludePathEntry("/test_40", ICSettingEntry.VALUE_WORKSPACE_PATH/* | ICSettingEntry.RESOLVED*/),
				new CIncludePathEntry(platformDependentPath("dbg 3", "D:\\docs\\incs"), 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "E:\\tmp\\w"), 0),
		};

		entries = ls.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, entries);

		assertTrue(option == tool.getOptionsOfType(IOption.INCLUDE_PATH)[0]);
		value = option.getBasicStringListValueElements();

		entries = ls.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);


		{
			ArrayList<OptionStringValue> list = new ArrayList<OptionStringValue>(Arrays.asList(option.getBasicStringListValueElements()));
			assertTrue(list.remove(new OptionStringValue("${IncludeDefaults}")));
			list.add(list.size(), new OptionStringValue("${IncludeDefaults}"));
			expectedValue = list.toArray(new OptionStringValue[0]);
			option = foInfo.setOption(tool, option, list.toArray(new OptionStringValue[0]));
			value = option.getBasicStringListValueElements();
		}

		expectedEntries = new ICLanguageSettingEntry[] {
				new CIncludePathEntry("dbg 3/rel/path", 0),
				new CIncludePathEntry("proj/rel/path", 0),
				new CIncludePathEntry("/abs/path", 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "c:/abs/path"), 0),
				new CIncludePathEntry("/test_40/dir1/dir2/dir3", ICSettingEntry.VALUE_WORKSPACE_PATH/* | ICSettingEntry.RESOLVED*/),
				new CIncludePathEntry("/test_40", ICSettingEntry.VALUE_WORKSPACE_PATH/* | ICSettingEntry.RESOLVED*/),
				new CIncludePathEntry(platformDependentPath("dbg 3", "D:\\docs\\incs"), 0),
				new CIncludePathEntry(platformDependentPath("dbg 3", "E:\\tmp\\w"), 0),
		};

		entries = ls.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, entries);

		assertTrue(option == tool.getOptionsOfType(IOption.INCLUDE_PATH)[0]);
		value = option.getBasicStringListValueElements();

		entries = ls.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);


		//deletion is performed in case if no fail occured
		for(int i = 0; i < projects.length; i++){
			try {
				projects[i].delete(true, null);
				assertNull(mngr.getProjectDescription(projects[i]));
				assertNull(mngr.getProjectDescription(projects[i], false));

				assertNull(ManagedBuildManager.getBuildInfo(projects[i]));
			} catch (Exception e) {
				System.err.println("Exception deleting a project " + projects[i].getName());
				System.err.println(e);
			}
		}
	}

	public void test40_pathconverter() throws Exception {
		IProject[] projects = createProjects("test_40_pathconverter", null, null, true);
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();

		ICProjectDescription des = mngr.getProjectDescription(projects[0]);
		ICConfigurationDescription cfgDes = des.getConfigurationByName("Test 4.0 ConfigName.Dbg");
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);

		ICFolderDescription foDes = cfgDes.getRootFolderDescription();
		ICLanguageSetting ls = foDes.getLanguageSettingForFile("foo.cpp");
		IFolderInfo foInfo = cfg.getRootFolderInfo();
		Tool tool = (Tool)foInfo.getToolFromInputExtension("cpp");
		IOption option = tool.getOptionsOfType(IOption.INCLUDE_PATH)[0];
		OptionStringValue[] value = option.getBasicStringListValueElements();
		ICLanguageSettingEntry[] entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);

		OptionStringValue[] expectedValue = new OptionStringValue[] {
				new OptionStringValue("../rel"),
				new OptionStringValue("/abs"),
		};

		ICLanguageSettingEntry[] expectedEntries = new ICLanguageSettingEntry[] {
				new CIncludePathEntry("rel", 0),
				new CIncludePathEntry("/test/abs", 0),
		};

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, entries);
		assertTrue(option == tool.getOptionsOfType(IOption.INCLUDE_PATH)[0]);
		value = option.getBasicStringListValueElements();
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		ArrayList<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
		list.addAll(Arrays.asList(entries));
		list.add(new CIncludePathEntry("/test/another/abs", 0));
		expectedEntries = list.toArray(new ICLanguageSettingEntry[0]);

		expectedValue = new OptionStringValue[] {
				new OptionStringValue("../rel"),
				new OptionStringValue("/abs"),
				new OptionStringValue("/another/abs"),
		};

		ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, list);

		assertTrue(option == tool.getOptionsOfType(IOption.INCLUDE_PATH)[0]);

		value = option.getBasicStringListValueElements();
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);


		//testing one-way converter
		ls = foDes.getLanguageSettingForFile("foo.c");
		tool = (Tool)foInfo.getToolFromInputExtension("c");
		option = tool.getOptionsOfType(IOption.INCLUDE_PATH)[0];
		value = option.getBasicStringListValueElements();
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);

		expectedValue = new OptionStringValue[] {
				new OptionStringValue("../rel"),
				new OptionStringValue("/abs"),
		};

		expectedEntries = new ICLanguageSettingEntry[] {
				new CIncludePathEntry("rel", 0),
				new CIncludePathEntry("/test/abs", 0),
		};

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, entries);
		assertTrue(option == tool.getOptionsOfType(IOption.INCLUDE_PATH)[0]);
		value = option.getBasicStringListValueElements();
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		list = new ArrayList<ICLanguageSettingEntry>();
		list.addAll(Arrays.asList(entries));
		list.add(new CIncludePathEntry("/another/abs", 0));

		expectedEntries = new ICLanguageSettingEntry[] {
				new CIncludePathEntry("rel", 0),
				new CIncludePathEntry("/test/abs", 0),
				new CIncludePathEntry("/test/another/abs", 0),
		};

		expectedValue = new OptionStringValue[] {
				new OptionStringValue("../rel"),
				new OptionStringValue("/abs"),
				new OptionStringValue("/another/abs"),
		};

		ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, list);

		assertTrue(option == tool.getOptionsOfType(IOption.INCLUDE_PATH)[0]);

		value = option.getBasicStringListValueElements();
		entries = ls.getSettingEntries(ICSettingEntry.INCLUDE_PATH);

		BuildSystemTestHelper.checkDiff(expectedValue, value);
		BuildSystemTestHelper.checkDiff(expectedEntries, entries);

		//deletion is performed in case if no fail occured
		for(int i = 0; i < projects.length; i++){
			projects[i].delete(true, null);
			assertNull(mngr.getProjectDescription(projects[i]));
			assertNull(mngr.getProjectDescription(projects[i], false));

			assertNull(ManagedBuildManager.getBuildInfo(projects[i]));
		}
	}

	private void buildProjects(IProject projects[], String[] files) {
		buildProjectsWorker(projects, files, true);
	}

	private void buildProjectsWorker(IProject projects[], String[] files, boolean compareBenchmark) {
		if(projects == null || projects.length == 0)
			return;

		for (int i = 0; i < projects.length; i++){
			IProject curProject = projects[i];

			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(curProject);

			//check whether the managed build info is converted
			boolean isCompatible = true;//UpdateManagedProjectManager.isCompatibleProject(info);
			//assertTrue(isCompatible);

			if (isCompatible){
				// Build the project in order to generate the makefiles
				try{
					curProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD,null);
				}
				catch(CoreException e){
					fail(e.getStatus().getMessage());
				}
				catch(OperationCanceledException e){
					fail("the project \"" + curProject.getName() + "\" build was cancelled, exception message: " + e.getMessage());
				}

				//compare the generated makefiles to their benchmarks
				if (files != null && files.length > 0) {
					if (i == 0) {
						String configName = info.getDefaultConfiguration().getName();
						IPath buildDir = Path.fromOSString(configName);
						if (compareBenchmark){
							IPath benchmarkLocationBase = resourcesLocation.append(curProject.getName());
							IPath buildLocationBase = curProject.getLocation();
							IPath[] paths = new IPath[files.length];
							for (int ii=0;ii<files.length;ii++) {
								paths[ii] = buildDir.append(files[ii]);
							}
							ManagedBuildTestHelper.compareBenchmarks(curProject, buildLocationBase, paths, benchmarkLocationBase);
						}
					}
				}
			}
		}
	}

	private IProject[] createProjects(String projName, IPath location, String projectTypeId, boolean containsZip) {
		IProject projects[] = createProject(projName, location, projectTypeId, containsZip);
		return projects;
	}

	private IProject[] createProject(String projName, IPath location, String projectTypeId, boolean containsZip){
		ArrayList<IProject> projectList = null;
		if (containsZip) {
			File testDir = CTestPlugin.getFileInPlugin(new Path("resources/test40Projects/" + projName));
			if(testDir == null) {
				fail("Test project directory " + projName + " is missing.");
				return null;
			}

			File projectZips[] = testDir.listFiles(new FileFilter(){
				@Override
				public boolean accept(File pathname){
					if(pathname.isDirectory())
						return false;
					return true;
				}
			});

			projectList = new ArrayList<IProject>(projectZips.length);
			for(int i = 0; i < projectZips.length; i++){
				try{
					String projectName = projectZips[i].getName();
					if(!projectName.endsWith(".zip"))
						continue;

					projectName = projectName.substring(0,projectName.length()-".zip".length());
					if(projectName.length() == 0)
						continue;
					IProject project = ManagedBuildTestHelper.createProject(projectName, projectZips[i], location, projectTypeId);
					if(project != null)
						projectList.add(project);
				}
				catch(Exception e){
				}
			}
			if(projectList.size() == 0) {
				fail("No projects found in test project directory " + testDir.getName() + ".  The .zip file may be missing or corrupt.");
				return null;
			}
		} else {
			try{
				IProject project = ManagedBuildTestHelper.createProject(projName, null, location, projectTypeId);
				if(project != null)
					projectList = new ArrayList<IProject>(1);
					projectList.add(project);
			} catch(Exception e){}
		}

		return projectList.toArray(new IProject[projectList.size()]);
	}

}
