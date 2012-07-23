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

import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.autotools.core.AutotoolsOptionConstants;
import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.autotools.core.IAutotoolsOption;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

// This test verifies an autogen.sh project that builds configure, but
// does not run it.
public class UpdateConfigureTest extends TestCase {
    
	private IProject testProject;
	
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        if (!ProjectTools.setup())
        	fail("could not perform basic project workspace setup");
		testProject = ProjectTools.createProject("testProject2");
		if (testProject == null) {
            fail("Unable to create test project");
        }
		testProject.open(new NullProgressMonitor());
    }
	
    /**
     * Test getting and updating configuration options for an Autotools Project. The top-level 
     * contains autogen.sh which will build configure, but not run it.
     * @throws Exception
     */
	public void testGetConfigureOptions() throws Exception {
		Path p = new Path("zip/project2.zip");
		ProjectTools.addSourceContainerWithImport(testProject, "src", p, null);
		assertTrue(testProject.hasNature(ManagedCProjectNature.MNG_NATURE_ID));
		ProjectTools.setConfigDir(testProject, "src");
		ProjectTools.markExecutable(testProject, "src/autogen.sh");
		assertTrue(ProjectTools.build());
		ICConfigurationDescription cfgDes = CoreModel.getDefault().getProjectDescription(testProject).getActiveConfiguration();
		IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		assertTrue(cfg.getName().equals("Build [GNU]"));
		Map<String, IAutotoolsOption> opts = AutotoolsPlugin.getDefault().getAutotoolCfgOptions(testProject, cfg.getId());
		IAutotoolsOption configdir = opts.get(AutotoolsOptionConstants.OPT_CONFIGDIR);
		assertTrue(configdir.equals("src"));
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
