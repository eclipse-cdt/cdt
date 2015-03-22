/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.tests;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import junit.framework.TestCase;

public class AutotoolsEnvironmentVarTest extends TestCase {

	private IProject testProject;

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
	 * Test that a sample project contains the expected environment variables.
	 * For example the verbose environment variable (V=1) necessary for proper
	 * GCC output parsing.
	 *
	 * @throws Exception
	 */
	public void testAutotoolsEnvironmentVar() throws Exception {

		Path p = new Path("zip/project1.zip");
		ProjectTools.addSourceContainerWithImport(testProject, null, p, null, true);
		assertTrue(testProject.hasNature(AutotoolsNewProjectNature.AUTOTOOLS_NATURE_ID));
		ICConfigurationDescription cfgDes = CoreModel.getDefault().getProjectDescription(testProject)
				.getActiveConfiguration();
		IEnvironmentVariable[] variables = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cfgDes,
				false);
		Map<String, IEnvironmentVariable> environmentVariables = new HashMap<>();
		for (IEnvironmentVariable var : variables) {
			environmentVariables.put(var.getName(), var);
		}

		IEnvironmentVariable verboseEnvironmentVariable = environmentVariables.get("V");
		assertNotNull(verboseEnvironmentVariable);
		assertEquals("1", verboseEnvironmentVariable.getValue());
	}

	protected void tearDown() throws Exception {
		testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		try {
			testProject.delete(true, true, null);
		} catch (Exception e) {
			// FIXME: Why does a ResourceException occur when deleting the
			// project??
		}
		super.tearDown();
	}
}

