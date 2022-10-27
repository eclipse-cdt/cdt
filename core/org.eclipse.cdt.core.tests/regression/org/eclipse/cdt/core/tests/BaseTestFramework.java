/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/*
 * Created on Oct 4, 2004
 */
package org.eclipse.cdt.core.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author aniefer
 */
abstract public class BaseTestFramework extends BaseTestCase {
	protected NullProgressMonitor monitor;
	protected IWorkspace workspace;
	protected IProject project;
	protected ICProject cproject;
	protected FileManager fileManager;
	protected boolean indexDisabled = false;

	public BaseTestFramework() {
		super();
	}

	/**
	 * @param name
	 */
	public BaseTestFramework(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		monitor = new NullProgressMonitor();
		workspace = ResourcesPlugin.getWorkspace();
		cproject = CProjectHelper.createCCProject("RegressionTestProject", "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$
		project = cproject.getProject();
		assertNotNull(project);

		//Create file manager
		fileManager = new FileManager();
	}

	@Override
	protected void tearDown() throws Exception {
		if (project == null || !project.exists())
			return;

		project.delete(true, true, monitor);
		BaseTestCase5.assertWorkspaceIsEmpty();
		super.tearDown();
	}

	protected IFile importFile(String fileName, String contents) throws Exception {
		// Obtain file handle
		IFile file = project.getProject().getFile(fileName);

		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		// Create file input stream
		if (file.exists()) {
			file.setContents(stream, false, false, monitor);
		} else {
			IPath path = file.getLocation();
			path = path.makeRelativeTo(project.getLocation());
			if (path.segmentCount() > 1) {
				path = path.removeLastSegments(1);

				for (int i = path.segmentCount() - 1; i >= 0; i--) {
					IPath currentPath = path.removeLastSegments(i);
					IFolder folder = project.getFolder(currentPath);
					if (!folder.exists()) {
						folder.create(false, true, null);
					}
				}
			}
			file.create(stream, false, monitor);
		}

		fileManager.addFile(file);

		return file;
	}
}
