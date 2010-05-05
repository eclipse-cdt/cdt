/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.tests;

import java.util.Arrays;

import junit.framework.Test;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.model.SourceEntry;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Regression test for Bug 311189.
 * When a Team Project Set is imported, replacing an existing CDT Project
 * we must ensure that we don't corrupt the set of includes in the incoming
 * project's metadata
 */
public class Bug311189 extends BaseTestCase {

	public static Test suite() {
		return suite(Bug311189.class, "_");
	}

	private IProject project;

	@Override
	protected void setUp() throws Exception {
		project = ResourceHelper.createCDTProjectWithConfig("bug311189");
	}

	/**
	 * If a source folder is deleted and re-created in a separate Job, ensure that we don't
	 * delete the source folder from the C Model.
	 */
	public void testPathSettingLost() throws Exception {
		IFolder srcFolder = project.getFolder("src");
		final IPathEntry sourceEntry = new SourceEntry(srcFolder.getFullPath(), new IPath[0]);

		// create a source folder and set it as a source entry
		srcFolder.create(true, false, null);
		CoreModel.setRawPathEntries(CoreModel.getDefault().create(project), new IPathEntry[] {sourceEntry}, null);
		IPathEntry[] rawEntries = CoreModel.getPathEntryStore(project).getRawPathEntries();
		assertTrue ("Path entry unset!", Arrays.asList(rawEntries).contains(sourceEntry));

		try {
			// None-batched resource change, though we do hold a scheduling rule
			// on the full project. While team operations are batched, doing this
			// is
			Job.getJobManager().beginRule(project, null);
			// Delete the source folder, and re-recreate it
			srcFolder.delete(true, null);
			srcFolder.create(true, false, null);
		} finally {
			Job.getJobManager().endRule(project);
		}

		// Path entry update should now be running...
		// Tick a workspace job through the workspace so we get when it's finished
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
			}
		}, null);

		IPathEntryStore store = CoreModel.getPathEntryStore(project);
		rawEntries = store.getRawPathEntries();
		assertTrue ("Path entry gone!", Arrays.asList(rawEntries).contains(sourceEntry));
	}


}
