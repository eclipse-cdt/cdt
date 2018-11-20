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

import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// This test verifies an autogen.sh project that builds configure, but
// does not run it.
public class AutotoolsProjectTest2 {

	private IProject testProject;

	@Before
	public void setUp() throws Exception {
		if (!ProjectTools.setup())
			fail("could not perform basic project workspace setup");
		testProject = ProjectTools.createProject("testProject2");
		if (testProject == null) {
			fail("Unable to create test project");
		}
		testProject.open(new NullProgressMonitor());
	}

	/**
	 * Test sample project which has a hello world program. The top-level
	 * contains autogen.sh which will build configure, but not run it.
	 * The hello world source is found in
	 * src/sample/hello.c so configuration must create multiple
	 * Makefiles.
	 * @throws Exception
	 */
	@Test
	public void testAutotoolsProject2() throws Exception {
		Path p = new Path("zip/project2.zip");
		ProjectTools.addSourceContainerWithImport(testProject, "src", p);
		assertTrue(testProject.hasNature(AutotoolsNewProjectNature.AUTOTOOLS_NATURE_ID));
		org.eclipse.core.runtime.Path x = new org.eclipse.core.runtime.Path("src/ChangeLog");
		assertTrue(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("src/configure");
		ProjectTools.setConfigDir(testProject, "src");
		ProjectTools.markExecutable(testProject, "src/autogen.sh");
		x = new org.eclipse.core.runtime.Path("src/configure");
		assertFalse(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("src/Makefile.in");
		assertFalse(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("src/sample/Makefile.in");
		assertFalse(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("src/aclocal.m4");
		assertFalse(testProject.exists(x));
		assertTrue(ProjectTools.build());
		x = new org.eclipse.core.runtime.Path("src/configure");
		assertTrue(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("src/Makefile.in");
		assertTrue(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("src/sample/Makefile.in");
		assertTrue(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("src/aclocal.m4");
		assertTrue(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("config.status");
		assertTrue(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("Makefile");
		assertTrue(testProject.exists(x));
		String extension = Platform.getOS().equals(Platform.OS_WIN32) ? ".exe" : "";
		x = new org.eclipse.core.runtime.Path("sample/a.out" + extension);
		assertTrue(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("sample/Makefile");
		assertTrue(testProject.exists(x));
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
