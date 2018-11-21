/*******************************************************************************
 * Copyright (c) 2005, 2011 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.tests;

import java.io.FileReader;

import org.eclipse.cdt.core.CCorePlugin;
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

import junit.framework.Assert;

public class StandardBuildTestHelper {
	/* (non-Javadoc)
	 * Create a new project named <code>name</code> or return the project in
	 * the workspace of the same name if it exists.
	 *
	 * @param name The name of the project to create or retrieve.
	 * @return
	 * @throws CoreException
	 */
	static public IProject createProject(final String name, final IPath location, final String projectId)
			throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject newProjectHandle = root.getProject(name);
		IProject project = null;

		if (!newProjectHandle.exists()) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			//description.setLocation(root.getLocation());
			project = CCorePlugin.getDefault().createCProject(description, newProjectHandle, new NullProgressMonitor(),
					projectId);
		} else {
			project = newProjectHandle;
		}

		// Open the project if we have to
		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}

		return project;
	}

	/**
	 * Remove the <code>IProject</code> with the name specified in the argument from the
	 * receiver's workspace.
	 *
	 * @param name
	 */
	static public void removeProject(String name) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject project = root.getProject(name);
		if (project.exists()) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					System.gc();
					System.runFinalization();
					project.delete(true, true, null);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			try {
				workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			} catch (CoreException e2) {
				Assert.assertTrue(false);
			}
		}
	}

	static public boolean compareBenchmarks(final IProject project, IPath testDir, IPath[] files) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
		};
		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
		} catch (Exception e) {
			Assert.fail("File " + files[0].lastSegment() + " - project refresh failed.");
		}
		for (int i = 0; i < files.length; i++) {
			IPath testFile = testDir.append(files[i]);
			IPath benchmarkFile = Path.fromOSString("Benchmarks/" + files[i]);
			StringBuffer testBuffer = readContentsStripLineEnds(project, testFile);
			StringBuffer benchmarkBuffer = readContentsStripLineEnds(project, benchmarkFile);
			if (!testBuffer.toString().equals(benchmarkBuffer.toString())) {
				Assert.fail("File " + testFile.lastSegment() + " does not match its benchmark.");
			}
		}
		return true;
	}

	static public boolean verifyFilesDoNotExist(final IProject project, IPath testDir, IPath[] files) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			}
		};
		try {
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, monitor);
		} catch (Exception e) {
			Assert.fail("File " + files[0].lastSegment() + " - project refresh failed.");
		}
		for (int i = 0; i < files.length; i++) {
			IPath testFile = testDir.append(files[i]);
			IPath fullPath = project.getLocation().append(testFile);
			try {
				if (fullPath.toFile().exists()) {
					Assert.fail("File " + testFile.lastSegment() + " unexpectedly found.");
					return false;
				}
			} catch (Exception e) {
				Assert.fail("File " + fullPath.toString() + " could not be referenced.");
			}
		}
		return true;
	}

	static public StringBuffer readContentsStripLineEnds(IProject project, IPath path) {
		StringBuffer buff = new StringBuffer();
		IPath fullPath = project.getLocation().append(path);
		try {
			FileReader input = null;
			try {
				input = new FileReader(fullPath.toFile());
			} catch (Exception e) {
				Assert.fail("File " + fullPath.toString() + " could not be read.");
			}
			//InputStream input = file.getContents(true);   // A different way to read the file...
			int c;
			do {
				c = input.read();
				if (c == -1)
					break;
				if (c != '\r' && c != '\n') {
					buff.append((char) c);
				}
			} while (c != -1);
			input.close();
		} catch (Exception e) {
			Assert.fail("File " + fullPath.toString() + " could not be read.");
		}
		return buff;
	}
}
