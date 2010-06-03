/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class BackwardCompatibilityTests extends BaseTestCase {
	private static final String PROJ_NAME_PREFIX = "BackwardCompatibilityTests_";
	ICProject p1, p2, p3;
	
	public static TestSuite suite() {
		return suite(BackwardCompatibilityTests.class, "_");
	}
	
	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if(p1 != null){
				p1.getProject().delete(true, null);
				p1 = null;
			}
			if(p2 != null){
				p2.getProject().delete(true, null);
				p2 = null;
			}
			if(p3 != null){
				p3.getProject().delete(true, null);
				p3 = null;
			}
		} catch (CoreException e){
		}
	}

	public void testPathEntriesForNewStyle() throws Exception {
		p1 = CProjectHelper.createNewStileCProject(PROJ_NAME_PREFIX + "a", TestUserAndDiscoveredEntriesCfgDataProvider.PROVIDER_ID, IPDOMManager.ID_NO_INDEXER);
		IProject project = p1.getProject();
		
		IPathEntry[] entries = CoreModel.getRawPathEntries(p1);
		IPathEntry[] resolvedentries = CoreModel.getResolvedPathEntries(p1);
		IPathEntry[] expectedRawEntries = new IPathEntry[]{
			CoreModel.newContainerEntry(new Path("org.eclipse.cdt.core.CFG_BASED_CONTAINER")),
			CoreModel.newSourceEntry(project.getFullPath()),
			CoreModel.newOutputEntry(project.getFullPath()),
		};
		checkEntriesMatch(expectedRawEntries, entries);
		
		IPathEntry[] expectedResolvedEntries = new IPathEntry[]{
			CoreModel.newSourceEntry(project.getFullPath()),
			CoreModel.newOutputEntry(project.getFullPath()),
			CoreModel.newMacroEntry(project.getFullPath(), "a", "b"),
			CoreModel.newMacroEntry(project.getFullPath(), "c", ""),
			CoreModel.newIncludeEntry(project.getFullPath(), null, project.getLocation().append("a/b/c")),
			CoreModel.newIncludeEntry(project.getFullPath(), null, new Path("/d/e/f")),
			CoreModel.newIncludeEntry(project.getFullPath(), project.getFullPath().makeRelative(), new Path("g/h/i")),
			CoreModel.newIncludeEntry(project.getFullPath(), new Path("j"), new Path("k/l")),
		};
		checkEntriesMatch(expectedResolvedEntries, resolvedentries);

		IPathEntry[] newEntries = new IPathEntry[entries.length + 1];
		System.arraycopy(entries, 0, newEntries, 0, entries.length);
		newEntries[entries.length] = CoreModel.newIncludeEntry(new Path("d"), null, new Path("/C/d/e"), true, new Path[]{new Path("a"), new Path("b")}, false);

		IPathEntry[] newExpectedRawEntries = new IPathEntry[entries.length + 1];
		System.arraycopy(entries, 0, newExpectedRawEntries, 0, entries.length);
		newExpectedRawEntries[entries.length] = CoreModel.newIncludeEntry(project.getFullPath().append("d"), null, new Path("/C/d/e"), true, new Path[]{new Path("a"), new Path("b")}, false);

		CoreModel.setRawPathEntries(p1, newEntries, null);
		
		entries = CoreModel.getRawPathEntries(p1);
		checkEntriesMatch(entries, newExpectedRawEntries);
		
		IPathEntry[] newExpectedResolved = new IPathEntry[resolvedentries.length + 1];
		System.arraycopy(resolvedentries, 0, newExpectedResolved, 0, resolvedentries.length);
		newExpectedResolved[resolvedentries.length] = CoreModel.newIncludeEntry(project.getFullPath().append("d"), null, new Path("/C/d/e"), true, new Path[]{new Path("a"), new Path("b")}, false);
		resolvedentries = CoreModel.getResolvedPathEntries(p1);
		checkEntriesMatch(resolvedentries, newExpectedResolved);
		
		entries = concatEntries(entries, new IPathEntry[]{
		       CoreModel.newSourceEntry(project.getFullPath().append("test_src")),
		       CoreModel.newOutputEntry(project.getFullPath().append("test_out")),});
		
		newExpectedRawEntries = concatEntries(newExpectedRawEntries, new IPathEntry[]{
		       CoreModel.newSourceEntry(project.getFullPath().append("test_src")),
		       CoreModel.newOutputEntry(project.getFullPath().append("test_out")),});
		
		for(int i = 0; i < newExpectedRawEntries.length; i++){
			IPathEntry entry = newExpectedRawEntries[i];
			if(entry.getEntryKind() == IPathEntry.CDT_SOURCE && entry.getPath().equals(project.getFullPath())){
				newExpectedRawEntries[i] = CoreModel.newSourceEntry(project.getFullPath(), new Path[]{new Path("test_src")});
			}
//			if(entry.getEntryKind() == IPathEntry.CDT_OUTPUT && entry.getPath().equals(project.getFullPath())){
//				newExpectedRawEntries[i] = CoreModel.newOutputEntry(project.getFullPath(), new Path[]{new Path("test_out")});
//			}
		}
		
		newExpectedResolved = concatEntries(newExpectedResolved, new IPathEntry[]{
			       CoreModel.newSourceEntry(project.getFullPath().append("test_src")),
			       CoreModel.newOutputEntry(project.getFullPath().append("test_out")),});
			
		for(int i = 0; i < newExpectedResolved.length; i++){
			IPathEntry entry = newExpectedResolved[i];
			if(entry.getEntryKind() == IPathEntry.CDT_SOURCE && entry.getPath().equals(project.getFullPath())){
				newExpectedResolved[i] = CoreModel.newSourceEntry(project.getFullPath(), new Path[]{new Path("test_src")});
			}
//			if(entry.getEntryKind() == IPathEntry.CDT_OUTPUT && entry.getPath().equals(project.getFullPath())){
//				newExpectedResolved[i] = CoreModel.newOutputEntry(project.getFullPath(), new Path[]{new Path("test_out")});
//			}
		}
		
		CoreModel.setRawPathEntries(p1, entries, null);
		
		entries = CoreModel.getRawPathEntries(p1);
		resolvedentries = CoreModel.getResolvedPathEntries(p1);
		
		checkEntriesMatch(newExpectedRawEntries, entries);
		checkEntriesMatch(newExpectedResolved, resolvedentries);
		
		CoreModel.setRawPathEntries(p1, expectedRawEntries, null);
		entries = CoreModel.getRawPathEntries(p1);
		resolvedentries = CoreModel.getResolvedPathEntries(p1);
		
		checkEntriesMatch(expectedRawEntries, entries);
		checkEntriesMatch(expectedResolvedEntries, resolvedentries);
		
		//check to see that setting the same entries do not give errors
		CoreModel.setRawPathEntries(p1, expectedRawEntries, null);
		entries = CoreModel.getRawPathEntries(p1);
		resolvedentries = CoreModel.getResolvedPathEntries(p1);
		
		checkEntriesMatch(expectedRawEntries, entries);
		checkEntriesMatch(expectedResolvedEntries, resolvedentries);
	}

	public void testCPathEntriesForOldStyle() throws Exception {
		p2 = CProjectHelper.createCCProject(PROJ_NAME_PREFIX + "b", null, IPDOMManager.ID_NO_INDEXER);
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		IProject project = p2.getProject();
		ICProjectDescription des = mngr.getProjectDescription(project, false);
		assertNotNull(des);
		assertEquals(1, des.getConfigurations().length);
		assertFalse(mngr.isNewStyleProject(des));
		assertFalse(mngr.isNewStyleProject(project));
		
		IPathEntry[] entries = CoreModel.getRawPathEntries(p2);
		entries = concatEntries(entries, new IPathEntry[]{
			       CoreModel.newSourceEntry(project.getFullPath().append("test_src")),
			       CoreModel.newOutputEntry(project.getFullPath().append("test_out")),});

		CoreModel.setRawPathEntries(p2, entries, null);
		
		ICSourceEntry[] expectedSourceEntries = new ICSourceEntry[]{
				new CSourceEntry(project.getFullPath(), new IPath[] {new Path("test_src")}, ICSettingEntry.RESOLVED),
				new CSourceEntry(project.getFullPath().append("test_src"), null, ICSettingEntry.RESOLVED),
		};

		ICOutputEntry[] expectedOutputEntries = new ICOutputEntry[]{
				new COutputEntry(project.getFullPath(), null, ICSettingEntry.RESOLVED | ICSettingEntry.VALUE_WORKSPACE_PATH),
				new COutputEntry(project.getFullPath().append("test_out"), null, ICSettingEntry.RESOLVED | ICSettingEntry.VALUE_WORKSPACE_PATH),
		};

		des = mngr.getProjectDescription(project, false);
		ICConfigurationDescription cfg = des.getDefaultSettingConfiguration();
		ICSourceEntry[] sEntries = cfg.getSourceEntries();
		ICOutputEntry[] oEntries = cfg.getBuildSetting().getOutputDirectories();
		
		checkCEntriesMatch(expectedSourceEntries, sEntries);
		checkCEntriesMatch(expectedOutputEntries, oEntries);

		des = mngr.getProjectDescription(project, true);
		cfg = des.getDefaultSettingConfiguration();
		sEntries = cfg.getSourceEntries();
		oEntries = cfg.getBuildSetting().getOutputDirectories();
		
		checkCEntriesMatch(expectedSourceEntries, sEntries);
		checkCEntriesMatch(expectedOutputEntries, oEntries);
	}
	
	public void testICDescriptorGetProjectData() throws Exception {
		p3 = CProjectHelper.createCCProject(PROJ_NAME_PREFIX + "c", null, IPDOMManager.ID_NO_INDEXER);
		IProject proj = p3.getProject();
		
		doTestRm(proj);
		doTestRm(proj);
		doTestRm(proj);
		doTestRm(proj);
		doTestRm(proj);
	}
	
	private void doTestRm(IProject proj) throws CoreException{
		final String DATA_ID = "testICDescriptorGetProjectData";
        ICDescriptor dr = CCorePlugin.getDefault().getCProjectDescription(proj, false);
        dr.removeProjectStorageElement(DATA_ID);
        dr.saveProjectData();
	}
	
	public static IPathEntry[] concatEntries(IPathEntry[] entries1, IPathEntry[] entries2){
		List list = new ArrayList(entries1.length + entries2.length);
		list.addAll(Arrays.asList(entries1));
		list.addAll(Arrays.asList(entries2));
		return (IPathEntry[])list.toArray(new IPathEntry[list.size()]);
	}
	
	public static void checkCEntriesMatch(ICSettingEntry[] e1, ICSettingEntry[] e2){
		if(e1.length != e2.length)
			fail("entries num do not match");
		
		Set set = new HashSet(Arrays.asList(e1));
		set.removeAll(Arrays.asList(e2));
		if(set.size() != 0)
			fail("entries do not match");
	}
	
	public static void checkEntriesMatch(IPathEntry[] e1, IPathEntry[] e2){
		if(e1.length != e2.length)
			fail("entries arrays have different length \ne1: " + dumpArray(e1) +"\ne2:" + dumpArray(e2) + "\n");
		
		for(int i = 0; i < e1.length; i++){
			IPathEntry entry = e1[i];
			boolean found = false;
			for(int k = 0; k < e2.length; k++){
				IPathEntry entry2 = e2[k];
				if(entry.equals(entry2)){
					found = true;
					break;
				}
			}
			if(!found)
				fail("unable to find entry " + entry.toString() + "\nin array \n" + dumpArray(e2) + "\n");
		}
	}
	
	public static String dumpArray(Object array[]){
		if(array == null)
			return "null";
		
		StringBuffer buf = new StringBuffer();
		buf.append('[');
		for(int i = 0; i < array.length; i++){
			if(i != 0){
				buf.append(",\n");
			}
			buf.append(array[i].toString());
		}
		buf.append(']');
		return buf.toString();
	}
	
}
