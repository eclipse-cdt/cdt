/*******************************************************************************
 * Copyright (c) 2008, 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     Marc-Andre Laperle - Fix failing test on Windows
 *******************************************************************************/
package org.eclipse.cdt.autotools.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class AutotoolsProjectTest0 extends TestCase {
    
	private IProject testProject;
	
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        if (!ProjectTools.setup())
        	fail("could not perform basic project workspace setup");
		testProject = ProjectTools.createProject("testProject0");
		if (testProject == null) {
            fail("Unable to create test project");
        }
		testProject.open(new NullProgressMonitor());
    }
	
    /**
     * Test sample project which has a hello world program. The top-level 
     * configure is found in the top-level directory.  The hello world source
     * is found in sample/hello.c.
     * @throws Exception
     */
	public void testAutotoolsProject0() throws Exception {
		Path p = new Path("zip/project1.zip");
		ProjectTools.addSourceContainerWithImport(testProject, null, p, null, true);
		assertTrue(testProject.hasNature(AutotoolsNewProjectNature.AUTOTOOLS_NATURE_ID));
		org.eclipse.core.runtime.Path x = new org.eclipse.core.runtime.Path("ChangeLog");
		assertTrue(testProject.exists(x));
		x = new org.eclipse.core.runtime.Path("configure");
		ProjectTools.markExecutable(testProject, "configure");
		ProjectTools.markExecutable(testProject, "config.guess");
		ProjectTools.markExecutable(testProject, "config.sub");
		ProjectTools.markExecutable(testProject, "missing");
		ProjectTools.markExecutable(testProject, "mkinstalldirs");
		ProjectTools.markExecutable(testProject, "install-sh");
		assertTrue(ProjectTools.build());
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
	
	protected void tearDown() throws Exception {
		testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		try {
			testProject.delete(true, true, null);
		} catch (Exception e) {
			//FIXME: Why does a ResourceException occur when deleting the project??
		}
		super.tearDown();
	}
}
