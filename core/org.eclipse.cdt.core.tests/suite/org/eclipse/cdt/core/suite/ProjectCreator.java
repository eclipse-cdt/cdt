/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.suite;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.eclipse.cdt.testplugin.CTestPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * This is a utility class that properly creates a project for testing
 * core features. Projects are created from a template stored in the
 * resources/zips directory. These templates are created using the CDT
 * and have the proper natures and build settings set up.
 * 
 * @author Doug Schaefer
 */
public class ProjectCreator extends TestCase {

	private static final byte[] buffer = new byte[512];
	private static final IProgressMonitor monitor = new NullProgressMonitor();

	public static IProject createProject(IPath zipPath,	String projectName) throws Exception {
		ZipFile zipFile = new ZipFile(CTestPlugin.getDefault().getFileInPlugin(zipPath));
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath rootPath = root.getLocation();
		String zipProjectName = null;

		Enumeration entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry)entries.nextElement();
			if (!entry.isDirectory()) {
				IPath entryPath = rootPath.append(entry.getName());
				IPath entryDir = entryPath.removeLastSegments(1);
				entryDir.toFile().mkdirs();
				InputStream in = zipFile.getInputStream(entry);
				OutputStream out = new FileOutputStream(entryPath.toFile());
				for (int n = in.read(buffer); n >= 0; n = in.read(buffer))
					out.write(buffer, 0, n);
				in.close();
				out.close();
				
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

	public void test() throws Exception {
		IProject project = createProject(
				new Path("resources/zips/CPPManaged.zip"),
				"TestProject");
	}
	
}
