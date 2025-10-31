/*******************************************************************************
 * Copyright (c) 2002, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *    Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * CPathEntryTest
 */
public class CPathEntryTest extends BaseTestCase5 {

	private ICProject testProject;

	class CElementListener implements IElementChangedListener {

		int count = 0;

		void processDelta(ICElementDelta delta) {
			if (delta == null) {
				return;
			}

			int flags = delta.getFlags();
			int kind = delta.getKind();
			if (kind == ICElementDelta.CHANGED) {
				if ((flags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0) {
					count++;
				}
			}
			ICElementDelta[] affectedChildren = delta.getAffectedChildren();
			for (int i = 0; i < affectedChildren.length; i++) {
				processDelta(affectedChildren[i]);
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
		 */
		@Override
		public void elementChanged(ElementChangedEvent event) {
			processDelta(event.getDelta());
		}

	}

	/**
	 * Sets up the test fixture.
	 *
	 * Called before every test case method.
	 *
	 */
	@BeforeEach
	protected void createProject() throws CoreException {
		testProject = CProjectHelper.createCProject("cpathtest", "none", IPDOMManager.ID_NO_INDEXER);
		if (testProject == null) {
			Assertions.fail("Unable to create project");
		}
	}

	/**
	 * Tears down the test fixture.
	 *
	 * Called after every test case method.
	 */
	@AfterEach
	protected void deleteProject() throws CoreException {
		testProject.getProject().delete(true, null);
	}

	/*******************************************************************************************************************************
	 * Check if the PathEntry's are generated.
	 *
	 * @see CProjectHelper#createCProject
	 */
	@Test
	public void testCPathEntries() throws CoreException {
		IPathEntry[] entries = testProject.getResolvedPathEntries();
		// We always have at least two entries:
		//  1) the default sourceEntry becomes the project
		//  2) the default outputEntry becomes the project
		Assertions.assertTrue(entries.length == 2, "No cpathentries");
		entries = new IPathEntry[3];
		entries[0] = CoreModel.newIncludeEntry(new Path(""), null, new Path("/usr/include"), true);
		entries[1] = CoreModel.newIncludeEntry(new Path("cpaththest/foo.c"), null, new Path("/usr/include"), true);
		entries[2] = CoreModel.newLibraryEntry(new Path(""), null, new Path("/usr/lib/libc.so.1"), null, null, null,
				false);
		testProject.setRawPathEntries(entries, new NullProgressMonitor());
		entries = testProject.getResolvedPathEntries();
		// We always have at least two entries:
		//  1) the default sourceEntry becomes the project
		//  2) the default outputEntry becomes the project
		Assertions.assertTrue(entries.length == (3 + 2), "Expecting 5 pathentries");
		testProject.setRawPathEntries(null, null);
	}

	/*******************************************************************************************************************************
	 * Check if the PathEntry's are generated.
	 *
	 * @see CProjectHelper#createCProject
	 */
	@Test
	public void testCPathEntriesDelta() throws CoreException {
		CProjectHelper.addCContainer(testProject, "foo");
		IPathEntry[] entries = new IPathEntry[3];
		entries[0] = CoreModel.newIncludeEntry(new Path(""), null, new Path("/usr/include"), true);
		entries[1] = CoreModel.newIncludeEntry(new Path("foo"), null, new Path("/usr/include"), true);
		entries[2] = CoreModel.newLibraryEntry(new Path(""), null, new Path("/usr/lib/libc.so.1"), null, null, null,
				false);
		CElementListener listener = new CElementListener();
		CoreModel.getDefault().addElementChangedListener(listener);
		testProject.setRawPathEntries(entries, new NullProgressMonitor());
		entries = testProject.getResolvedPathEntries();
		//CoreModel.getDefault().removeElementChangedListener(listener);
		testProject.setRawPathEntries(null, null);
		Assertions.assertTrue(listener.count > 1, "Expecting 3 pathEntries deltas");
	}

	/**
	 * Check the IPathEntryContainer.
	 */
	@Test
	public void testPathEntryContainer() throws CoreException {
		final IPath containerID = new Path("Testing/Container");
		IContainerEntry containerEntry = CoreModel.newContainerEntry(containerID);
		IPathEntryContainer container = new IPathEntryContainer() {

			@Override
			public IPathEntry[] getPathEntries() {
				IPathEntry[] entries = new IPathEntry[3];
				entries[0] = CoreModel.newIncludeEntry(new Path(""), null, new Path("/usr/include"), true);
				entries[1] = CoreModel.newIncludeEntry(new Path("foo.c"), null, new Path("/usr/include"), true);
				entries[2] = CoreModel.newLibraryEntry(new Path(""), null, new Path("/usr/lib/libc.so.1"), null, null,
						null, true);
				return entries;
			}

			@Override
			public String getDescription() {
				return "Testing container"; //$NON-NLS-1$
			}

			@Override
			public IPath getPath() {
				return containerID;
			}

		};
		CoreModel.setRawPathEntries(testProject, new IPathEntry[] { containerEntry }, new NullProgressMonitor());
		CoreModel.setPathEntryContainer(new ICProject[] { testProject }, container, new NullProgressMonitor());
		IPathEntry[] entries = testProject.getResolvedPathEntries();
		// We always have at least two entries:
		//  1) the default sourceEntry becomes the project
		//  2) the default outputEntry becomes the project
		Assertions.assertTrue(entries.length == (3 + 2), "Expecting 3 pathentries from container");

	}

	@Test
	public void testSetExclusionFilter_Bug197486() throws Exception {
		// get project description
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(testProject.getProject(), true);
		ICConfigurationDescription activeCfg = prjDesc.getActiveConfiguration();
		Assertions.assertNotNull(activeCfg);

		// add filter to source entry
		ICSourceEntry[] entries = activeCfg.getSourceEntries();
		final String sourceEntryName = entries[0].getName();
		final IPath[] exclusionPatterns = new IPath[] { new Path("dummy*"), new Path("dummy2/*") };

		ICSourceEntry entry = new CSourceEntry(sourceEntryName, exclusionPatterns, entries[0].getFlags());
		activeCfg.setSourceEntries(new ICSourceEntry[] { entry });

		// check the modified configuration for the exclusion patterns
		checkExclusionPatterns(sourceEntryName, exclusionPatterns, activeCfg);

		// store the changed configuration
		CoreModel.getDefault().setProjectDescription(testProject.getProject(), prjDesc);

		// check again.
		prjDesc = CoreModel.getDefault().getProjectDescription(testProject.getProject(), false);
		ICConfigurationDescription[] allConfigs = prjDesc.getConfigurations();
		Assertions.assertEquals((long) 1, (long) allConfigs.length);
		checkExclusionPatterns(sourceEntryName, exclusionPatterns, allConfigs[0]);

		activeCfg = prjDesc.getActiveConfiguration();
		checkExclusionPatterns(sourceEntryName, exclusionPatterns, activeCfg);
	}

	private void checkExclusionPatterns(String sourceEntryName, IPath[] exclusionPatterns,
			ICConfigurationDescription cfg) {
		Assertions.assertNotNull(cfg);

		ICSourceEntry[] entries = cfg.getSourceEntries();
		Assertions.assertEquals((long) 1, (long) entries.length);
		Assertions.assertEquals(sourceEntryName, entries[0].getName());
		IPath[] actualExclusionPatterns = entries[0].getExclusionPatterns();
		Assertions.assertEquals((long) exclusionPatterns.length, (long) actualExclusionPatterns.length);
		Assertions.assertEquals(toSet(exclusionPatterns), toSet(actualExclusionPatterns));
	}

	private Set toSet(Object[] array) {
		HashSet set = new HashSet();
		for (int i = 0; i < array.length; i++) {
			set.add(array[i]);
		}
		return set;
	}
}
