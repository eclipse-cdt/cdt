/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.model.tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/*
 * CPathEntryTest
 */
public class CPathEntryTest extends TestCase {
	IWorkspace workspace;
	IWorkspaceRoot root;
	IProject project_c, project_cc;
	NullProgressMonitor monitor;
	String pluginRoot;

	class CElementListener implements IElementChangedListener {

		int count = 0;

		void processDelta(ICElementDelta delta) {
			if (delta == null) {
				return ;
			}

			int flags = delta.getFlags();
			int kind = delta.getKind();
			if (kind == ICElementDelta.CHANGED ) {
				if ((flags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0) {
					count++;
				}
			}
			ICElementDelta[] affectedChildren= delta.getAffectedChildren();
			for (int i= 0; i < affectedChildren.length; i++) {
				processDelta(affectedChildren[i]);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
		 */
		public void elementChanged(ElementChangedEvent event) {
			processDelta(event.getDelta());
		}

	}

	/**
	 * Constructor for CModelTests.
	 * 
	 * @param name
	 */
	public CPathEntryTest(String name) {
		super(name);
	}

	/**
	 * Sets up the test fixture.
	 * 
	 * Called before every test case method.
	 *  
	 */
	protected void setUp() throws CoreException {
		/***************************************************************************************************************************
		 * The test of the tests assume that they have a working workspace and workspace root object to use to create
		 * projects/files in, so we need to get them setup first.
		 */
		IWorkspaceDescription desc;
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
		if (workspace == null)
			fail("Workspace was not setup");
		if (root == null)
			fail("Workspace root was not setup");
		pluginRoot = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
		desc = workspace.getDescription();
		desc.setAutoBuilding(false);
		workspace.setDescription(desc);

	}

	/**
	 * Tears down the test fixture.
	 * 
	 * Called after every test case method.
	 */
	protected void tearDown() {
		// release resources here and clean-up
	}

	public static TestSuite suite() {
		return new TestSuite(CPathEntryTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/*******************************************************************************************************************************
	 * Check if the PathEntry's are generated.
	 * 
	 * @see CProjectHelper#createCProject
	 */
	public void testCPathEntries() throws CoreException {
		ICProject testProject;
		testProject = CProjectHelper.createCProject("cpathtest", "none");
		if (testProject == null) {
			fail("Unable to create project");
		}
		IPathEntry[] entries = testProject.getResolvedPathEntries();
		// We always have at least two entries:
		//  1) the default sourceEntry becomes the project
		//  2) the default outputEntry becomes the project
		assertTrue("No cpathentries", entries.length == 2);
		entries = new IPathEntry[3];
		entries[0] = CoreModel.newIncludeEntry(new Path(""), null, new Path("/usr/include"), true);
		entries[1] = CoreModel.newIncludeEntry(new Path("cpaththest/foo.c"), null, new Path("/usr/include"), true);
		entries[2] = CoreModel.newLibraryEntry(new Path(""), null, new Path("/usr/lib/libc.so.1"), null, null, null, false);
		testProject.setRawPathEntries(entries, new NullProgressMonitor());
		entries = testProject.getResolvedPathEntries();
		// We always have at least two entries:
		//  1) the default sourceEntry becomes the project
		//  2) the default outputEntry becomes the project
		assertTrue("Expecting 5 pathentries", entries.length == (3 + 2));
		testProject.setRawPathEntries(null, null);
	}

	/*******************************************************************************************************************************
	 * Check if the PathEntry's are generated.
	 * 
	 * @see CProjectHelper#createCProject
	 */
	public void testCPathEntriesDelta() throws CoreException {
		ICProject testProject;
		testProject = CProjectHelper.createCProject("cpathtest", "none");
		if (testProject == null) {
			fail("Unable to create project");
		}
		CProjectHelper.addCContainer(testProject, "foo");
		IPathEntry[] entries = new IPathEntry[3];
		entries[0] = CoreModel.newIncludeEntry(new Path(""), null, new Path("/usr/include"), true);
		entries[1] = CoreModel.newIncludeEntry(new Path("foo"), null, new Path("/usr/include"), true);
		entries[2] = CoreModel.newLibraryEntry(new Path(""), null, new Path("/usr/lib/libc.so.1"), null, null, null, false);
		CElementListener listener = new CElementListener();
		CoreModel.getDefault().addElementChangedListener(listener);
		testProject.setRawPathEntries(entries, new NullProgressMonitor());
		entries = testProject.getResolvedPathEntries();
		//CoreModel.getDefault().removeElementChangedListener(listener);
		testProject.setRawPathEntries(null, null);
		assertTrue("Expecting 3 pathEntries deltas", listener.count > 1);
	}

	/**
	 * Check the IPathEntryContainer.
	 */
	public void testPathEntryContainer() throws CoreException {
		ICProject testProject;
		testProject = CProjectHelper.createCProject("cpathtest", "none");
		if (testProject == null) {
			fail("Unable to create project");
		}
		final IPath containerID = new Path("Testing/Container");
		IContainerEntry containerEntry = CoreModel.newContainerEntry(containerID);
		IPathEntryContainer container = new IPathEntryContainer() {

			public IPathEntry[] getPathEntries() {
				IPathEntry[] entries = new IPathEntry[3];
				entries[0] = CoreModel.newIncludeEntry(new Path(""), null, new Path("/usr/include"), true);
				entries[1] = CoreModel.newIncludeEntry(new Path("foo.c"), null, new Path("/usr/include"), true);
				entries[2] = CoreModel.newLibraryEntry(new Path(""), null, new Path("/usr/lib/libc.so.1"), null, null, null, true);
				return entries;
			}

			public String getDescription() {
				return "Testing container"; //$NON-NLS-1$
			}

			public IPath getPath() {
				return containerID;
			}
			
		};
		CoreModel.getDefault().setRawPathEntries(testProject, new IPathEntry[]{containerEntry}, new NullProgressMonitor());
		CoreModel.getDefault().setPathEntryContainer(new ICProject[]{testProject}, container, new NullProgressMonitor());
		IPathEntry[] entries = testProject.getResolvedPathEntries();
		// We always have at least two entries:
		//  1) the default sourceEntry becomes the project
		//  2) the default outputEntry becomes the project
		assertTrue("Expecting 3 pathentries from container", entries.length == (3 + 2));
	}
}