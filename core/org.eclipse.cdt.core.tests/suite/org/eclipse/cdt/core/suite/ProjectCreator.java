/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.suite;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import junit.framework.TestCase;

/**
 * This is a utility class that properly creates a project for testing
 * core features. Projects are created from a template stored in a zip file
 * These templates are created using the CDT and have the proper natures
 * and build settings set up.
 *
 * @author Doug Schaefer
 */
public class ProjectCreator extends TestCase {

	private static final byte[] buffer = new byte[512];
	private static final IProgressMonitor monitor = new NullProgressMonitor();

	public static IProject createProject(IPath zipPath, String projectName) throws Exception {
		try (ZipFile zipFile = new ZipFile(CTestPlugin.getDefault().getFileInPlugin(zipPath))) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			IPath rootPath = root.getLocation();
			String zipProjectName = null;

			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (!entry.isDirectory()) {
					IPath entryPath = rootPath.append(entry.getName());
					IPath entryDir = entryPath.removeLastSegments(1);
					entryDir.toFile().mkdirs();
					try (InputStream in = zipFile.getInputStream(entry);
							OutputStream out = new FileOutputStream(entryPath.toFile())) {
						for (int n = in.read(buffer); n >= 0; n = in.read(buffer)) {
							out.write(buffer, 0, n);
						}
					}

					// Is this the .project file?
					if (".project".equals(entryPath.lastSegment())) {
						IProjectDescription desc = workspace.loadProjectDescription(entryPath);
						zipProjectName = desc.getName();
					}
				}
			}

			IProject project = root.getProject(zipProjectName);
			project.create(monitor);
			project.open(monitor);
			project.move(new Path(projectName), true, monitor);

			return project;
		}
	}

	public static IProject createCManagedProject(String projectName) throws Exception {
		return createProject(new Path("resources/zips/CManaged.zip"), projectName);
	}

	public static IProject createCPPManagedProject(String projectName) throws Exception {
		return createProject(new Path("resources/zips/CPPManaged.zip"), projectName);
	}

	public static IProject createCStandardProject(String projectName) throws Exception {
		return createProject(new Path("resources/zips/CStandard.zip"), projectName);
	}

	public static IProject createCPPStandardProject(String projectName) throws Exception {
		return createProject(new Path("resources/zips/CPPStandard.zip"), projectName);
	}

	public void test() throws Exception {
		IProject project = createCPPManagedProject("TestProject");
		assertNotNull(project);
	}

}
