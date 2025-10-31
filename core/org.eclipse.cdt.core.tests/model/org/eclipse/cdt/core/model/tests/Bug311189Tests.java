/*******************************************************************************
 * Copyright (c) 2010, 2012 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp) - initial API and implementation
 *     Wind River Systems - Bug 348569
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.internal.core.model.SourceEntry;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression test for Bug 311189.
 * When a Team Project Set is imported, replacing an existing CDT Project
 * we must ensure that we don't corrupt the set of includes in the incoming
 * project's metadata
 */
public class Bug311189Tests extends BaseTestCase5 {

	private IProject project;

	@BeforeEach
	protected void createProject() throws Exception {
		project = ResourceHelper.createCDTProjectWithConfig("bug311189");
	}

	/**
	 * If a source folder is deleted and re-created in a separate Job, ensure that we don't
	 * delete the source folder from the C Model.
	 */
	@Test
	public void testPathSettingLost() throws Exception {
		IFolder srcFolder = project.getFolder("src");
		final IPathEntry sourceEntry = new SourceEntry(srcFolder.getFullPath(), new IPath[0]);

		// create a source folder and set it as a source entry
		srcFolder.create(true, true, null);
		CoreModel.setRawPathEntries(CoreModel.getDefault().create(project), new IPathEntry[] { sourceEntry }, null);
		IPathEntry[] rawEntries = CoreModel.getPathEntryStore(project).getRawPathEntries();
		assertTrue(Arrays.asList(rawEntries).contains(sourceEntry), "Path entry unset!");

		try {
			// None-batched resource change, though we do hold a scheduling rule
			// on the full project. While team operations are batched, doing this
			// is
			Job.getJobManager().beginRule(project, null);
			// Delete the source folder, and re-recreate it
			srcFolder.delete(true, null);
			srcFolder.create(true, true, null);
		} finally {
			Job.getJobManager().endRule(project);
		}

		// Path entry update should now be running...
		// Tick a workspace job through the workspace so we get when it's finished
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
			}
		}, null);

		IPathEntryStore store = CoreModel.getPathEntryStore(project);
		rawEntries = store.getRawPathEntries();
		assertTrue(Arrays.asList(rawEntries).contains(sourceEntry), "Path entry gone!");
	}

}
