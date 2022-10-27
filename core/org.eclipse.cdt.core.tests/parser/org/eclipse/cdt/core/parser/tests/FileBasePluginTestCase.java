/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.TestCase;

/**
 * @author dsteffle
 */
public abstract class FileBasePluginTestCase extends TestCase {
	static NullProgressMonitor monitor;
	static IWorkspace workspace;
	static IProject project;
	static int numProjects;
	static Class className;
	static ICProject cPrj;
	private Class className2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		monitor = new NullProgressMonitor();

		workspace = ResourcesPlugin.getWorkspace();

		cPrj = CProjectHelper.createCCProject("ParserTestProject", "bin", IPDOMManager.ID_NO_INDEXER); //$NON-NLS-1$ //$NON-NLS-2$
		project = cPrj.getProject();
		assertNotNull(project);
	}

	@Override
	protected void tearDown() throws Exception {
		if (project == null || !project.exists())
			return;

		project.delete(true, false, monitor);
		BaseTestCase5.assertWorkspaceIsEmpty();
	}

	protected IFolder importFolder(String folderName) throws Exception {
		IFolder folder = project.getProject().getFolder(folderName);

		// Create file input stream
		if (!folder.exists())
			folder.create(false, false, monitor);

		return folder;
	}

	public IFile importFile(String fileName, String contents) throws Exception {
		// Obtain file handle
		IFile file = project.getProject().getFile(fileName);

		InputStream stream = new ByteArrayInputStream(contents.getBytes());
		// Create file input stream
		if (file.exists()) {
			file.setContents(stream, false, false, monitor);
		} else {
			file.create(stream, false, monitor);
		}

		return file;
	}
}
