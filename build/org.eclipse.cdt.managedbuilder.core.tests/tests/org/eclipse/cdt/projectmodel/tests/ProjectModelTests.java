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
package org.eclipse.cdt.projectmodel.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.testplugin.BuildSystemTestHelper;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ProjectModelTests extends TestCase implements IElementChangedListener{
	private boolean isPrint = false;
	private CDefaultModelEventChecker fEventChecker;
	
	public void elementChanged(ElementChangedEvent event) {
		if(fEventChecker != null)
			fEventChecker.checkEvent(event);
	}
	
	private class CDefaultModelEventChecker {

		void checkEvent(ElementChangedEvent event) {
			assertEquals(ElementChangedEvent.POST_CHANGE, event.getType());
			
			ICElementDelta delta = event.getDelta();
			if (isPrint)
				System.out.println(delta.toString());
		}
		
	}
	
	

	public static Test suite() {
		return new TestSuite(ProjectModelTests.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fEventChecker = null;
		CoreModel.getDefault().addElementChangedListener(this);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		fEventChecker = null;
		CoreModel.getDefault().removeElementChangedListener(this);
	}

	private void modify(ICFileDescription fiDes){
		ICLanguageSetting ls = fiDes.getLanguageSetting();
		modify(ls);
	}
	
	private void modify(ICFolderDescription foDes){
		ICLanguageSetting ls = foDes.getLanguageSettingForFile("a.c");
		modify(ls);
	}
	
	private void modify(ICLanguageSetting ls){
		List<ICLanguageSettingEntry> list = ls.getSettingEntriesList(ICSourceEntry.INCLUDE_PATH);
		list.add(new CIncludePathEntry("_modify_", 0));
		ls.setSettingEntries(ICSettingEntry.INCLUDE_PATH, list);
	}

	private void modify(IFileInfo fiInfo){
		CLanguageData lData = fiInfo.getCLanguageDatas()[0];
		modify(lData);
	}
	
	private void modify(CLanguageData lData){
		ICLanguageSettingEntry[] entries = lData.getEntries(ICSourceEntry.INCLUDE_PATH);
		ICLanguageSettingEntry[] updatedEntries = new ICLanguageSettingEntry[entries.length + 1];
		System.arraycopy(entries, 0, updatedEntries, 0, entries.length);
		updatedEntries[entries.length] = new CIncludePathEntry("_modify_", 0);
		lData.setEntries(ICSettingEntry.INCLUDE_PATH, updatedEntries);
	}

	public void testDescription() throws Exception{
		final String projectName = "test1";
		IProject project = createProject(projectName);
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.getProjectDescription(project);
		assertNull("detDescription1 returned not null!", des);
		
		des = coreModel.createProjectDescription(project, true);
		assertNotNull("createDescription returned null!", des);
		
		assertNull("detDescription2 returned not null!", coreModel.getProjectDescription(project));
		
		assertFalse("new des should be not valid", des.isValid());
		
		assertEquals(0, des.getConfigurations().length);
		
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		IProjectType type = ManagedBuildManager.getProjectType("cdt.managedbuild.target.gnu30.exe");
		assertNotNull("project type not found", type);

		ManagedProject mProj = new ManagedProject(project, type);
		info.setManagedProject(mProj);
		
		IConfiguration cfgs[] = type.getConfigurations();
		

		for(int i = 0; i < cfgs.length; i++){
			String id = ManagedBuildManager.calculateChildId(cfgs[i].getId(), null);
			Configuration config = new Configuration(mProj, (Configuration)cfgs[i], id, false, true, false);
			CConfigurationData data = config.getConfigurationData();
			assertNotNull("data is null for created configuration", data);
			des.createConfiguration("org.eclipse.cdt.managedbuilder.core.configurationDataProvider", data);
		}
		coreModel.setProjectDescription(project, des);
		
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = wsp.getRoot(); 
		project.delete(false, true, new NullProgressMonitor());
		
		project = root.getProject(projectName);
		des = coreModel.getProjectDescription(project);
		assertNull("project description is not null for removed project", des);
		
		project = createProject(projectName);
		des = coreModel.getProjectDescription(project);
		assertNotNull("project description is null for re-created project", des);
		assertTrue("des should be valid for re-created project", des.isValid());
		
		ICConfigurationDescription cfgDess[] = des.getConfigurations();
		
		assertEquals(2, cfgDess.length);
		
		ICConfigurationDescription cfgDes = cfgDess[0];
		ICResourceDescription rcDess[] = cfgDes.getResourceDescriptions();
		assertEquals(1, rcDess.length);
		assertEquals(cfgDes.getRootFolderDescription(), rcDess[0]);
		assertFalse(cfgDes.isModified());
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), true));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), false));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), false));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path("ds/sd/sdf/"), false));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), true));
		assertEquals(null, cfgDes.getResourceDescription(new Path("ds/sd/sdf/"), true));
		
		ICFileDescription fd_abc = cfgDes.createFileDescription(new Path("a/b/c.c"), rcDess[0]);
		
		assertTrue(cfgDes.isModified());

		modify(fd_abc);
		
			ICProjectDescription anotherDes = coreModel.getProjectDescription(project);
			assertNotNull("project description is null for re-created project", des);
			assertTrue("des should be valid for re-created project", des.isValid());
			
			ICConfigurationDescription anotherCfgDess[] = anotherDes.getConfigurations();
			
			assertEquals(2, anotherCfgDess.length);
			
			ICConfigurationDescription anotherCfgDes = anotherCfgDess[0];
			ICResourceDescription anotherRcDess[] = anotherCfgDes.getResourceDescriptions();
			assertEquals(1, anotherRcDess.length);
			assertEquals(anotherCfgDes.getRootFolderDescription(), anotherRcDess[0]);
			assertFalse(anotherCfgDes.isModified());
		
		CConfigurationData cfgData = cfgDes.getConfigurationData();
		assertEquals(cfgData, cfgDes.getConfigurationData());
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		IResourceInfo infos[] = cfg.getResourceInfos();
		assertEquals(2, infos.length);

		ICFolderDescription rf = cfgDes.getRootFolderDescription();
		ICResourceDescription nestedFis[] = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FILE);
		ICResourceDescription nestedFos[] = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FOLDER);
		assertEquals(2, cfgDes.getResourceDescriptions().length);
		assertEquals(0, nestedFos.length);
		assertEquals(1, nestedFis.length);
		
		ICFileDescription fd_asd = cfgDes.createFileDescription(new Path("a/s/d.c"), cfgDes.getRootFolderDescription());
		modify(fd_asd);
		assertEquals(3, cfgDes.getResourceDescriptions().length);
		nestedFis = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FILE);
		nestedFos = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FOLDER);
		assertEquals(0, nestedFos.length);
		assertEquals(2, nestedFis.length);
		
		IFileInfo fi = cfg.createFileInfo(new Path("z/x/c.c"));
		modify(fi);
		nestedFis = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FILE);
		nestedFos = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FOLDER);
		assertEquals(0, nestedFos.length);
		assertEquals(3, nestedFis.length);
		assertEquals(4, cfgDes.getResourceDescriptions().length);

		fi = cfg.createFileInfo(new Path("q/w/e.c"));
		modify(fi);
		ICFileDescription fd_qwe = (ICFileDescription)cfgDes.getResourceDescription(new Path("q/w/e.c"), true);
		assertNotNull(fd_qwe);
		assertEquals(5, cfgDes.getResourceDescriptions().length);
		nestedFis = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FILE);
		nestedFos = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FOLDER);
		assertEquals(0, nestedFos.length);
		assertEquals(4, nestedFis.length);
		
		cfgDes.removeResourceDescription(fd_abc);
		nestedFis = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FILE);
		nestedFos = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FOLDER);
		assertEquals(0, nestedFos.length);
		assertEquals(3, nestedFis.length);
		assertEquals(4, cfgDes.getResourceDescriptions().length);

		cfg.removeResourceInfo(new Path("a/s/d.c"));
		assertEquals(3, cfgDes.getResourceDescriptions().length);
		nestedFis = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FILE);
		nestedFos = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FOLDER);
		assertEquals(0, nestedFos.length);
		assertEquals(2, nestedFis.length);
		
		IFileInfo fi_qwe = (IFileInfo)cfg.getResourceInfo(new Path("q/w/e.c"), true);
		assertNotNull(fi_qwe);
		
		ICFileDescription fid_qwe = (ICFileDescription)cfgDes.getResourceDescription(new Path("q/w/e.c"), true);
		assertNotNull(fid_qwe);
		fi_qwe.setPath(new Path("r/t/y.c"));
		assertEquals(fi_qwe, cfg.getResourceInfo(new Path("r/t/y.c"), true));
		assertNull(cfgDes.getResourceDescription(new Path("q/w/e.c"), true));
		ICFileDescription fid_rty = (ICFileDescription)cfgDes.getResourceDescription(new Path("r/t/y.c"), true);
		assertEquals(fid_qwe, fid_rty);
		
		fid_rty.setPath(new Path("f/g/h.c"));
		assertNull(cfg.getResourceInfo(new Path("r/t/y.c"), true));
		IFileInfo fi_fgh = (IFileInfo)cfg.getResourceInfo(new Path("f/g/h.c"), true);
		assertEquals(fi_qwe, fi_fgh);
		
		ICFolderDescription fod_fg1 = cfgDes.createFolderDescription(new Path("f/g/1"), cfgDes.getRootFolderDescription());
		modify(fod_fg1);
		ICFolderDescription fod_fg12 = cfgDes.createFolderDescription(new Path("f/g/1/2"), fod_fg1);
		modify(fod_fg12);
		assertEquals(fod_fg12, fod_fg1.getNestedResourceDescription(new Path("2"), true));

		ICFileDescription fid_fg13 = cfgDes.createFileDescription(new Path("f/g/1/3.c"), fod_fg1);
		modify(fid_fg13);
		assertEquals(fid_fg13, fod_fg1.getNestedResourceDescription(new Path("3.c"), true));
		
		assertEquals(2, fod_fg1.getNestedResourceDescriptions().length);
		assertEquals(1, fod_fg1.getNestedResourceDescriptions(ICSettingBase.SETTING_FILE).length);
		assertEquals(1, fod_fg1.getNestedResourceDescriptions(ICSettingBase.SETTING_FOLDER).length);

		IFolderInfo fo_fg1 = (IFolderInfo)cfg.getResourceInfo(new Path("f/g/1"), true);
		assertNotNull(fo_fg1);
		
		fo_fg1.setPath(new Path("t/y/u"));

		assertEquals(2, fod_fg1.getNestedResourceDescriptions().length);
		assertEquals(1, fod_fg1.getNestedResourceDescriptions(ICSettingBase.SETTING_FILE).length);
		assertEquals(1, fod_fg1.getNestedResourceDescriptions(ICSettingBase.SETTING_FOLDER).length);
		
		assertEquals(fod_fg12, cfgDes.getResourceDescription(new Path("t/y/u/2"), true));
		assertEquals(fid_fg13, cfgDes.getResourceDescription(new Path("t/y/u/3.c"), true));

		ICLanguageSetting settings[] = cfgDes.getRootFolderDescription().getLanguageSettings();
		for(int i = 0; i < settings.length; i++){
			ICLanguageSetting setting = settings[i];
			ICLanguageSettingEntry[] entries = setting.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);
			if (!setting.supportsEntryKind(ICSettingEntry.INCLUDE_PATH))
				assertTrue(entries.length == 0);
			if (isPrint) {
				for(int j = 0; j < entries.length; j++){
					System.out.println(entries[j].getName());
				}
				System.out.println(entries.length);
			}
		}
		coreModel.setProjectDescription(project, des);
		
		project.delete(false, true, new NullProgressMonitor());
		
		project = root.getProject(projectName);
		assertFalse(project.exists());
		assertFalse(project.isOpen());
		des = coreModel.getProjectDescription(project);
		assertFalse(project.exists());
		assertFalse(project.isOpen());
		assertNull("project description is not null for removed project", des);
		
		project = createProject(projectName);
		long time = System.currentTimeMillis();
		des = coreModel.getProjectDescription(project);
		time = System.currentTimeMillis() - time;
		if (isPrint)
			System.out.println("time to load = " + time);
		
		assertNotNull("project description is null for re-created project", des);
		assertTrue("des should be valid for re-created project", des.isValid());
		
		cfgDess = des.getConfigurations();
		cfgDes = cfgDess[0];
		rf = cfgDes.getRootFolderDescription();
		settings = rf.getLanguageSettings();
		ICLanguageSettingEntry updatedEntries[] = new ICLanguageSettingEntry[0];
		for(int i = 0; i < settings.length; i++){
			ICLanguageSetting setting = settings[i];
			ICLanguageSettingEntry[] entries = setting.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);
			if(entries.length > 0){
				ICLanguageSettingEntry updated[] = new ICLanguageSettingEntry[entries.length + 1];
				System.arraycopy(entries, 0, updated, 1, entries.length);
				updated[0] = new CIncludePathEntry("a/b/c", 0);
				setting.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, updated);
				updatedEntries = setting.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);
				assertEquals(updated.length, updatedEntries.length);
				for(int k = 0; k < updated.length; k++){
					assertEquals(updated[i].getValue(), updatedEntries[i].getValue());
				}
			}
		}
		
		fEventChecker = new CDefaultModelEventChecker();
		coreModel.setProjectDescription(project, des);
		fEventChecker = null;
		
		project.delete(false, true, new NullProgressMonitor());
		
		project = root.getProject(projectName);
		assertFalse(project.exists());
		assertFalse(project.isOpen());
		des = coreModel.getProjectDescription(project);
		assertNull("project description is not null for removed project", des);
		
		project = createProject(projectName);
		time = System.currentTimeMillis();
		des = coreModel.getProjectDescription(project);
		time = System.currentTimeMillis() - time;
		if (isPrint)
			System.out.println("time to load = " + time);
		
		assertNotNull("project description is null for re-created project", des);
		assertTrue("des should be valid for re-created project", des.isValid());
		
		cfgDess = des.getConfigurations();
		cfgDes = cfgDess[0];
		rf = cfgDes.getRootFolderDescription();
		settings = rf.getLanguageSettings();

		for(int i = 0; i < settings.length; i++){
			ICLanguageSetting setting = settings[i];
			ICLanguageSettingEntry[] entries = setting.getSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH);
			if (setting.supportsEntryKind(ICSettingEntry.INCLUDE_PATH)) {
				BuildSystemTestHelper.checkDiff(entries, updatedEntries);
				assertEquals(entries.length, updatedEntries.length);
				for(int k = 0; k < entries.length; k++)
					assertEquals(entries[i].getValue(), updatedEntries[i].getValue());
			} else
				assertTrue(entries.length == 0);
		}

		assertEquals(2, cfgDess.length);
		nestedFis = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FILE);
		nestedFos = rf.getNestedResourceDescriptions(ICSettingBase.SETTING_FOLDER);
		assertEquals(2, nestedFos.length);
		assertEquals(3, nestedFis.length);
		
		assertEquals(6, cfgDes.getResourceDescriptions().length);
		
		ManagedBuildTestHelper.createFolder(project, "a/b");
		ICFolderDescription base = (ICFolderDescription)cfgDes.getResourceDescription(new Path("a/b"), false);
		ICFolderDescription abFoDes =  cfgDes.createFolderDescription(new Path("a/b"), base);
		ICLanguageSetting ls = abFoDes.getLanguageSettingForFile("a.c");
		assertNotNull(ls);
		List<ICLanguageSettingEntry> list = ls.getSettingEntriesList(ICLanguageSettingEntry.INCLUDE_PATH);
		list.add(0, new CIncludePathEntry("zzza/b/c", 0));
		ls.setSettingEntries(ICLanguageSettingEntry.INCLUDE_PATH, list);

		if (isPrint)
			System.out.println("setting entries for non-root folder..\n");
		fEventChecker = new CDefaultModelEventChecker();
		coreModel.setProjectDescription(project, des);
		fEventChecker = null;
		
		time = System.currentTimeMillis();
		des = coreModel.getProjectDescription(project);
		time = System.currentTimeMillis() - time;
		if (isPrint)
			System.out.println("time to load = " + time);
		
		assertNotNull("project description is null for re-created project", des);
		assertTrue("des should be valid for re-created project", des.isValid());
		
		cfgDess = des.getConfigurations();
		cfgDes = cfgDess[0];
		rf = cfgDes.getRootFolderDescription();
		
		ManagedBuildTestHelper.createFolder(project, "b/c");
		base = (ICFolderDescription)cfgDes.getResourceDescription(new Path("b/c"), false);
		ICFolderDescription bcFoDes =  cfgDes.createFolderDescription(new Path("b/c"), base);
		ls = bcFoDes.getLanguageSettingForFile("a.c");
		assertNotNull(ls);
		ICLanguageSetting rLS = rf.getLanguageSettingForFile("a.c");
		assertNotNull(rLS);
		ls.getSettingEntriesList(ICLanguageSettingEntry.INCLUDE_PATH);
		rLS.getSettingEntriesList(ICLanguageSettingEntry.INCLUDE_PATH);
		
		if (isPrint)
			System.out.println("default entries for non-root folder..\n");
		fEventChecker = new CDefaultModelEventChecker();
		coreModel.setProjectDescription(project, des);
		fEventChecker = null;

		project.delete(true, true, new NullProgressMonitor());
	}
	
	public void testSourceEntries() throws Exception {
		final String projectName = "test2";
		IProject project = createProject(projectName);
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.getProjectDescription(project);
		assertNull("detDescription1 returned not null!", des);
		
		des = coreModel.createProjectDescription(project, true);
		assertNotNull("createDescription returned null!", des);
		
		assertNull("detDescription2 returned not null!", coreModel.getProjectDescription(project));
		
		assertFalse("new des should be not valid", des.isValid());
		
		assertEquals(0, des.getConfigurations().length);
		
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		IProjectType type = ManagedBuildManager.getProjectType("cdt.managedbuild.target.gnu30.exe");
		assertNotNull("project type not found", type);

		ManagedProject mProj = new ManagedProject(project, type);
		info.setManagedProject(mProj);
		
		IConfiguration cfgs[] = type.getConfigurations();
		

		for(int i = 0; i < cfgs.length; i++){
			String id = ManagedBuildManager.calculateChildId(cfgs[i].getId(), null);
			Configuration config = new Configuration(mProj, (Configuration)cfgs[i], id, false, true);
			CConfigurationData data = config.getConfigurationData();
			assertNotNull("data is null for created configuration", data);
			des.createConfiguration("org.eclipse.cdt.managedbuilder.core.configurationDataProvider", data);
		}
		coreModel.setProjectDescription(project, des);
		
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = wsp.getRoot(); 
		project.delete(false, true, new NullProgressMonitor());
		
		project = root.getProject(projectName);
		des = coreModel.getProjectDescription(project);
		assertNull("project description is not null for removed project", des);
		
		project = createProject(projectName);
		des = coreModel.getProjectDescription(project);
		assertNotNull("project description is null for re-created project", des);
		assertTrue("des should be valid for re-created project", des.isValid());
		
		ICConfigurationDescription cfgDess[] = des.getConfigurations();
		
		assertEquals(2, cfgDess.length);
		
		ICConfigurationDescription cfgDes = cfgDess[0];
		ICResourceDescription rcDess[] = cfgDes.getResourceDescriptions();
		assertEquals(1, rcDess.length);
		assertEquals(cfgDes.getRootFolderDescription(), rcDess[0]);
		assertFalse(cfgDes.isModified());
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), true));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), false));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), false));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path("ds/sd/sdf/"), false));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), true));
		assertEquals(null, cfgDes.getResourceDescription(new Path("ds/sd/sdf/"), true));

		ICSourceEntry s[] = cfgDes.getSourceEntries();
		IPath projPath = cfgDes.getProjectDescription().getProject().getFullPath();
		assertEquals(1, s.length);
		assertEquals(projPath, s[0].getFullPath());
		assertEquals(0, s[0].getExclusionPatterns().length);
		
		ManagedBuildTestHelper.createFolder(project, "a/b");

		ICSourceEntry updatetSEs[] = new ICSourceEntry[2];
		updatetSEs[0] = new CSourceEntry(projPath.append("a"), new Path[]{new Path("b")}, ICSourceEntry.VALUE_WORKSPACE_PATH);
		updatetSEs[1] = s[0];
		
		cfgDes.setSourceEntries(updatetSEs);
		
		s = cfgDes.getSourceEntries();
		updatetSEs[1] = new CSourceEntry(projPath, new Path[]{new Path("a")}, ICSourceEntry.VALUE_WORKSPACE_PATH | ICSourceEntry.RESOLVED);
		checkArrays(updatetSEs, s);
		//assertTrue(Arrays.equals(updatetSEs, s));

		if (isPrint)
			System.out.println("saving updated source entries..\n");
		fEventChecker = new CDefaultModelEventChecker();
		coreModel.setProjectDescription(project, des);
		fEventChecker = null;
		
		des = coreModel.getProjectDescription(project);
		cfgDes = des.getConfigurations()[0];
		checkArrays(cfgDes.getSourceEntries(), s);
		
		project.delete(true, true, new NullProgressMonitor());
	}

	public void testMacroEntries() throws Exception {
		final String projectName = "test3";
		IProject project = createProject(projectName);
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.getProjectDescription(project);
		assertNull("detDescription1 returned not null!", des);
		
		des = coreModel.createProjectDescription(project, true);
		assertNotNull("createDescription returned null!", des);
		
		assertNull("detDescription2 returned not null!", coreModel.getProjectDescription(project));
		
		assertFalse("new des should be not valid", des.isValid());
		
		assertEquals(0, des.getConfigurations().length);
		
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		IProjectType type = ManagedBuildManager.getProjectType("cdt.managedbuild.target.gnu30.exe");
		assertNotNull("project type not found", type);

		ManagedProject mProj = new ManagedProject(project, type);
		info.setManagedProject(mProj);
		
		IConfiguration cfgs[] = type.getConfigurations();
		
		for(int i = 0; i < cfgs.length; i++){
			String id = ManagedBuildManager.calculateChildId(cfgs[i].getId(), null);
			Configuration config = new Configuration(mProj, (Configuration)cfgs[i], id, false, true);
			CConfigurationData data = config.getConfigurationData();
			assertNotNull("data is null for created configuration", data);
			des.createConfiguration("org.eclipse.cdt.managedbuilder.core.configurationDataProvider", data);
		}
		coreModel.setProjectDescription(project, des);

		des = coreModel.getProjectDescription(project);
		assertNotNull("project description is null ", des);
		assertTrue("des should be valid ", des.isValid());
		
		ICConfigurationDescription cfgDess[] = des.getConfigurations();
		
		assertEquals(2, cfgDess.length);
		
		ICConfigurationDescription cfgDes = cfgDess[0];
		ICResourceDescription rcDess[] = cfgDes.getResourceDescriptions();
		assertEquals(1, rcDess.length);
		assertEquals(cfgDes.getRootFolderDescription(), rcDess[0]);
		assertFalse(cfgDes.isModified());
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), true));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), false));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), false));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path("ds/sd/sdf/"), false));
		assertEquals(cfgDes.getRootFolderDescription(), cfgDes.getResourceDescription(new Path(""), true));
		assertEquals(null, cfgDes.getResourceDescription(new Path("ds/sd/sdf/"), true));

		ICFolderDescription rf = cfgDes.getRootFolderDescription();
		ICLanguageSetting setting = rf.getLanguageSettingForFile("a.c");
		ICLanguageSettingEntry entries[] = setting.getSettingEntries(ICLanguageSettingEntry.MACRO);
		
		if (isPrint) {
			for(int i = 0; i < entries.length; i++){
				System.out.println("name = \"" + entries[i].getName() + "\", value = \"" + entries[i].getValue() + "\"");
			}
		}
		
		CMacroEntry entry = new CMacroEntry("a", "b", 0);
		List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
		list.add(entry);
		list.addAll(Arrays.asList(entries));
		
		setting.setSettingEntries(ICLanguageSettingEntry.MACRO, list);
	
		ICLanguageSettingEntry updatedEntries[] = setting.getSettingEntries(ICLanguageSettingEntry.MACRO);
		assertEquals(entries.length + 1, updatedEntries.length);
		
		boolean found = false;
		for(int i = 0; i < updatedEntries.length; i++){
			if(updatedEntries[i].getName().equals("a") 
					&& updatedEntries[i].getValue().equals("b")){
				found = true;
				break;
			}
		}
		
		assertTrue(found);
	}
	
	public void testActiveCfg() throws Exception{
		final String projectName = "test8";
		
		IProject project = createProject(projectName, "cdt.managedbuild.target.gnu30.exe");
		CoreModel coreModel = CoreModel.getDefault();
		
		ICProjectDescription des = coreModel.getProjectDescription(project);
		ICConfigurationDescription cfgs[] = des.getConfigurations();
		String id1 = cfgs[0].getId();
		cfgs[1].getId();
		
		cfgs[0].setActive();
		assertEquals(cfgs[0], des.getActiveConfiguration());
		
		coreModel.setProjectDescription(project, des);
		
		des = coreModel.getProjectDescription(project);
		cfgs = des.getConfigurations();
		assertEquals(id1, des.getActiveConfiguration().getId());
		
		ICConfigurationDescription newActive = null;
		for(int i = 0; i < cfgs.length; i++){
			if(!cfgs[i].getId().equals(id1)){
				newActive = cfgs[i];
				break;
			}
		}
		
		String newActiveId = newActive.getId();
		newActive.setActive();
		assertEquals(newActive, des.getActiveConfiguration());
		
		coreModel.setProjectDescription(project, des);
		
		des = coreModel.getProjectDescription(project);
		assertEquals(newActiveId, des.getActiveConfiguration().getId());
		
		
	}

	private void checkArrays(Object[] a1, Object[] a2){
		if(a1 == null){
			assertTrue(a2 == null);
			return;
		}
		assertTrue(a2 != null);

		assertEquals(a1.length, a2.length);
		
		for(int i = 0; i < a1.length; i++){
			Object o1 = a1[i];
			boolean found = false;
			for(int j = 0; j < a2.length; j++){
				if(o1.equals(a2[j])){
					found = true;
					break;
				}
			}
			
			if(!found){
				fail("a2 array does not contain the \"" + o1 + "\" element");
			}
		}
	}
	
	static public IProject createProject(String name, String projTypeId) throws CoreException{
		IProject project = createProject(name);
		
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.getProjectDescription(project);
		assertNull("detDescription1 returned not null!", des);
		
		des = coreModel.createProjectDescription(project, true);
		assertNotNull("createDescription returned null!", des);
		
		assertNull("detDescription2 returned not null!", coreModel.getProjectDescription(project));
		
		assertFalse("new des should be not valid", des.isValid());
		
		assertEquals(0, des.getConfigurations().length);
		
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		IProjectType type = ManagedBuildManager.getProjectType(projTypeId);
		assertNotNull("project type not found", type);

		ManagedProject mProj = new ManagedProject(project, type);
		info.setManagedProject(mProj);
		
		IConfiguration cfgs[] = type.getConfigurations();
		

		for(int i = 0; i < cfgs.length; i++){
			String id = ManagedBuildManager.calculateChildId(cfgs[i].getId(), null);
			Configuration config = new Configuration(mProj, (Configuration)cfgs[i], id, false, true);
			CConfigurationData data = config.getConfigurationData();
			assertNotNull("data is null for created configuration", data);
			des.createConfiguration("org.eclipse.cdt.managedbuilder.core.configurationDataProvider", data);
		}

		coreModel.setProjectDescription(project, des);

		return project;
	}
	
	static public IProject createProject(String name) throws CoreException{
		return createProject(name, (IPath)null);
	}
	
	static public IProject createProject(
			final String name, 
			final IPath location) throws CoreException{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject newProjectHandle = root.getProject(name);
		IProject project = null;
		
		if (!newProjectHandle.exists()) {
			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			if(location != null)
				description.setLocation(location);
			//description.setLocation(root.getLocation());
			project = CCorePlugin.getDefault().createCDTProject(description, newProjectHandle, new NullProgressMonitor());
		} else {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			project = newProjectHandle;
		}
        
		// Open the project if we have to
		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}
				
		return project;	
	}
}
