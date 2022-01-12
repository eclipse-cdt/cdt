/*******************************************************************************
 * Copyright (c) 2008, 2015 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Marc-Andre Laperle - Fix failing test on Windows
 *******************************************************************************/
package org.eclipse.cdt.autotools.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// This test verifies using Autotools with a linked folder.
public class AutotoolsVirtualFolderTest {

	private IProject testProject;

	@Before
	public void setUp() throws Exception {
		if (!ProjectTools.setup())
			fail("could not perform basic project workspace setup");
		testProject = ProjectTools.createProject("testProjectVirtualFolder");
		if (testProject == null) {
			fail("Unable to create test project");
		}
		testProject.open(new NullProgressMonitor());
	}

	/**
	 * Test sample project with a virtual folder that points to configure scripts.
	 * Tests Bug 434275 - Autotools configuration in subfolder not found
	 * @throws Exception
	 */
	@Test
	public void testAutotoolsVirtualFolder() throws Exception {
		Path p = new Path("zip/project2.zip");
		IWorkspaceRoot root = ProjectTools.getWorkspaceRoot();
		IPath rootPath = root.getLocation();
		IPath configPath = rootPath.append("config");
		File configDir = configPath.toFile();
		configDir.deleteOnExit();
		assertTrue(configDir.mkdir());
		ProjectTools.createLinkedFolder(testProject, "src", URIUtil.append(root.getLocationURI(), "config"));
		ProjectTools.addSourceContainerWithImport(testProject, "src", p);
		assertTrue(testProject.hasNature(AutotoolsNewProjectNature.AUTOTOOLS_NATURE_ID));
		assertTrue(exists("src/ChangeLog"));
		ProjectTools.setConfigDir(testProject, "src");
		ProjectTools.markExecutable(testProject, "src/autogen.sh");
		assertFalse(exists("src/configure"));
		assertFalse(exists("src/Makefile.in"));
		assertFalse(exists("src/sample/Makefile.in"));
		assertFalse(exists("src/aclocal.m4"));
		assertTrue(ProjectTools.build());
		assertTrue(exists("src/configure"));
		assertTrue(exists("src/Makefile.in"));
		assertTrue(exists("src/sample/Makefile.in"));
		assertTrue(exists("src/aclocal.m4"));
		assertTrue(exists("config.status"));
		assertTrue(exists("Makefile"));
		String extension = Platform.getOS().equals(Platform.OS_WIN32) ? ".exe" : "";
		assertTrue(exists("sample/a.out" + extension));
		assertTrue(exists("sample/Makefile"));
	}

	private boolean exists(String path) {
		return testProject.exists(new Path(path));
	}

	@After
	public void tearDown() throws Exception {
		testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		try {
			testProject.delete(true, true, null);
		} catch (Exception e) {
			//FIXME: Why does a ResourceException occur when deleting the project??
		}
	}

}
