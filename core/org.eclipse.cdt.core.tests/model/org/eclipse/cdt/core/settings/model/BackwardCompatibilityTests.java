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

import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class BackwardCompatibilityTests extends BaseTestCase {
	private static final String PROJ_NAME_PREFIX = "BackwardCompatibilityTests_";
	ICProject p1;
	
	public static TestSuite suite() {
		return suite(BackwardCompatibilityTests.class, "_");
	}
	
	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
		try {
			if(p1 != null){
				p1.getProject().delete(true, null);
				p1 = null;
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
		assertEquals(expectedRawEntries.length, entries.length);
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
		assertEquals(expectedResolvedEntries.length, resolvedentries.length);
		checkEntriesMatch(expectedResolvedEntries, resolvedentries);

		IPathEntry[] newEntries = new IPathEntry[entries.length + 1];
		System.arraycopy(entries, 0, newEntries, 0, entries.length);
		newEntries[entries.length] = CoreModel.newIncludeEntry(new Path("d"), null, new Path("/C/d/e"), true, new Path[]{new Path("a"), new Path("b")}, false);

		IPathEntry[] newExpectedRawEntries = new IPathEntry[entries.length + 1];
		System.arraycopy(entries, 0, newExpectedRawEntries, 0, entries.length);
		newExpectedRawEntries[entries.length] = CoreModel.newIncludeEntry(project.getFullPath().append("d"), null, new Path("/C/d/e"), true, new Path[]{new Path("a"), new Path("b")}, false);

		CoreModel.setRawPathEntries(p1, newEntries, null);
		
		entries = CoreModel.getRawPathEntries(p1);
		assertEquals(newExpectedRawEntries.length, entries.length);
		checkEntriesMatch(entries, newExpectedRawEntries);
		
		IPathEntry[] newExpectedResolved = new IPathEntry[resolvedentries.length + 1];
		System.arraycopy(resolvedentries, 0, newExpectedResolved, 0, resolvedentries.length);
		newExpectedResolved[resolvedentries.length] = CoreModel.newIncludeEntry(project.getFullPath().append("d"), null, new Path("/C/d/e"), true, new Path[]{new Path("a"), new Path("b")}, false);
		resolvedentries = CoreModel.getResolvedPathEntries(p1);
		checkEntriesMatch(resolvedentries, newExpectedResolved);
	}
	
	private void checkEntriesMatch(IPathEntry[] e1, IPathEntry[] e2){
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
	
	private String dumpArray(Object array[]){
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
