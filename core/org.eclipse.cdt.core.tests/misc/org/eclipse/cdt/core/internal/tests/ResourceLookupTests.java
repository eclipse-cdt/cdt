/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.internal.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResourceLookupTests extends BaseTestCase5 {

	private IProject fProject;

	@BeforeEach
	protected void beforeEach() throws Exception {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		fProject = root.getProject("reslookup_" + getName());
	}

	@AfterEach
	protected void afterEach() throws Exception {
		fProject.delete(true, true, new NullProgressMonitor());
	}

	protected IFolder createFolder(IProject project, String filename) throws CoreException {
		IFolder folder = project.getFolder(filename);
		folder.create(true, false, new NullProgressMonitor());
		return folder;
	}

	protected IFile createFile(IProject project, String filename) throws CoreException {
		IFile file = project.getFile(filename);
		file.create(new InputStream() {
			@Override
			public int read() throws IOException {
				return -1;
			}
		}, true, new NullProgressMonitor());
		return file;
	}

	@Test
	public void testNameLookup() throws CoreException {
		IProject[] prjs = new IProject[] { fProject };

		fProject.create(new NullProgressMonitor());
		fProject.open(new NullProgressMonitor());
		createFolder(fProject, "folder1");
		createFolder(fProject, "folder2");
		createFile(fProject, "abc.h");
		createFile(fProject, "folder1/abc.h");
		createFile(fProject, "folder2/abC.h");

		IFile[] files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, false);
		assertEquals(2, files.length);
		files = ResourceLookup.findFilesByName(new Path("bla/../abc.h"), prjs, false);
		assertEquals(2, files.length);
		files = ResourceLookup.findFilesByName(new Path("../abc.h"), prjs, false);
		assertEquals(2, files.length);
		files = ResourceLookup.findFilesByName(new Path("../../abc.h"), prjs, false);
		assertEquals(2, files.length);

		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(3, files.length);

		files = ResourceLookup.findFilesByName(new Path("folder1/abc.h"), prjs, false);
		assertEquals(1, files.length);
		files = ResourceLookup.findFilesByName(new Path("folder1/abC.h"), prjs, false);
		assertEquals(0, files.length);
		files = ResourceLookup.findFilesByName(new Path("fOlder1/abc.h"), prjs, false);
		assertEquals(0, files.length);

		files = ResourceLookup.findFilesByName(new Path("folder1/abc.h"), prjs, true);
		assertEquals(1, files.length);
		files = ResourceLookup.findFilesByName(new Path("folder1/abC.h"), prjs, true);
		assertEquals(1, files.length);
		files = ResourceLookup.findFilesByName(new Path("fOlder1/abc.h"), prjs, true);
		assertEquals(1, files.length);

		files = ResourceLookup.findFilesByName(new Path("bla/../abc.h"), prjs, true);
		assertEquals(3, files.length);
	}

	@Test
	public void testResourceDelta() throws CoreException {
		IProject[] prjs = new IProject[] { fProject };
		fProject.create(new NullProgressMonitor());
		fProject.open(new NullProgressMonitor());

		IFile[] files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(0, files.length);

		IFolder f1 = createFolder(fProject, "folder1");
		createFolder(fProject, "folder2");
		IFile f2 = createFile(fProject, "abc.h");
		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(1, files.length);

		createFile(fProject, "folder1/abc.h");
		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(2, files.length);

		createFile(fProject, "folder2/abC.h");
		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(3, files.length);

		f1.delete(true, new NullProgressMonitor());
		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(2, files.length);

		f2.delete(true, new NullProgressMonitor());
		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(1, files.length);
	}

	@Test
	public void testDeref() throws CoreException {
		IProject[] prjs = new IProject[] { fProject };

		fProject.create(new NullProgressMonitor());
		fProject.open(new NullProgressMonitor());
		createFolder(fProject, "folder1");
		createFolder(fProject, "folder2");
		createFile(fProject, "abc.h");
		IFile[] files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(1, files.length);

		ResourceLookup.unrefNodeMap();
		createFile(fProject, "folder1/abc.h");
		createFile(fProject, "folder2/abC.h");

		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(3, files.length);

		ResourceLookup.unrefNodeMap();
		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(3, files.length);
	}

	@Test
	public void testCollected() throws CoreException {
		IProject[] prjs = new IProject[] { fProject };

		fProject.create(new NullProgressMonitor());
		fProject.open(new NullProgressMonitor());
		createFolder(fProject, "folder1");
		createFolder(fProject, "folder2");
		createFile(fProject, "abc.h");
		IFile[] files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(1, files.length);

		ResourceLookup.simulateNodeMapCollection();
		createFile(fProject, "folder1/abc.h");
		createFile(fProject, "folder2/abC.h");

		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(3, files.length);

		ResourceLookup.simulateNodeMapCollection();
		files = ResourceLookup.findFilesByName(new Path("abc.h"), prjs, true);
		assertEquals(3, files.length);
	}

	@Test
	public void testFindFilesByLocation() throws Exception {
		fProject.create(new NullProgressMonitor());
		fProject.open(new NullProgressMonitor());
		createFolder(fProject, "folder1");
		createFolder(fProject, "folder2");
		IFile file = createFile(fProject, "abc.h");
		createFile(fProject, "folder1/abc.h");
		createFile(fProject, "folder2/abC.h");

		URI uri = file.getLocationURI();
		IPath path = file.getLocation();
		IFile[] files = ResourceLookup.findFilesForLocationURI(uri);
		assertEquals(1, files.length);
		files = ResourceLookup.findFilesForLocation(path);
		assertEquals(1, files.length);

		if (new File("a").equals(new File("A"))) {
			URI upperCase = new URI(uri.getScheme(), uri.getSchemeSpecificPart().toUpperCase(), uri.getFragment());
			IPath upperCasePath = new Path(path.toString().toUpperCase());
			files = ResourceLookup.findFilesForLocationURI(upperCase);
			assertEquals(1, files.length);
			files = ResourceLookup.findFilesForLocation(upperCasePath);
			assertEquals(1, files.length);
		}
	}

	@Test
	public void testLinkedResourceFiles() throws Exception {
		IProject[] prjs = new IProject[] { fProject };

		fProject.create(new NullProgressMonitor());
		fProject.open(new NullProgressMonitor());
		createFolder(fProject, "folder1");
		File f = createTempFile("extern", ".h");
		IPath location = Path.fromOSString(f.getAbsolutePath());
		IFile file1 = fProject.getFile("linked1");
		IFile file2 = fProject.getFile("linked2.h");
		file1.createLink(location, 0, new NullProgressMonitor());
		file2.createLink(location, 0, new NullProgressMonitor());

		IFile[] files = ResourceLookup.findFilesForLocation(location);
		assertEquals(2, files.length);

		files = ResourceLookup.findFilesByName(new Path(location.lastSegment()), prjs, false);
		assertEquals(2, files.length);

		files = ResourceLookup.findFilesByName(new Path("linked2.h"), prjs, false);
		assertEquals(0, files.length);
	}
}
