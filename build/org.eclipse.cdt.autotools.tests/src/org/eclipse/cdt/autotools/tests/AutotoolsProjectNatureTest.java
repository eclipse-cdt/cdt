/*******************************************************************************
 * Copyright (c) 2008, 2015 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;

public class AutotoolsProjectNatureTest {
    @Before
    public void setUp() throws CoreException {
        if (!ProjectTools.setup())
        	fail("could not perform basic project workspace setup");
     }
	@Test
	public void testAutotoolsProjectNature() throws Exception {
		IProject testProject = ProjectTools.createProject("testProject");
		if(testProject == null) {
            fail("Unable to create test project");
        }
		assertTrue(testProject.hasNature(AutotoolsNewProjectNature.AUTOTOOLS_NATURE_ID));
		testProject.delete(true, false, ProjectTools.getMonitor());
	}
}
