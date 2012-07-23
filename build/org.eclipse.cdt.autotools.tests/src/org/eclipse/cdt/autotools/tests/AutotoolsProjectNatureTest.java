/*******************************************************************************
 * Copyright (c) 2008 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.tests;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;

public class AutotoolsProjectNatureTest extends TestCase {
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        if (!ProjectTools.setup())
        	fail("could not perform basic project workspace setup");
     }
	
	public void testAutotoolsProjectNature() throws Exception {
		IProject testProject = ProjectTools.createProject("testProject");
		if(testProject == null) {
            fail("Unable to create test project");
        }
		assertTrue(testProject.hasNature(AutotoolsNewProjectNature.AUTOTOOLS_NATURE_ID));
		testProject.delete(true, false, ProjectTools.getMonitor());
	}
}
