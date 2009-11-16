/*******************************************************************************
 * Copyright (c) 2009 Broadcom Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    James Blackburn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.envvar;

import java.io.ByteArrayInputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.internal.errorparsers.tests.ResourceHelper;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

public class IEnvironmentVariableManagerTests extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
	}

	public static Test suite() {
        TestSuite suite = new TestSuite(IEnvironmentVariableManagerTests.class);
        return suite;
	}

	/**
	 * Create a project with 2 configurations. Set an environment variable on one of
	 * the configurations. Close and reopen the project. Check persistence
	 * @throws Exception
	 */
	public void testSimpleVar() throws Exception {
		final IProject project = ResourceHelper.createCDTProjectWithConfig("envProject");

		// Add another, derived configuration
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
		final String id1 = desc.getId();  	      // Config 1's ID
		final String id2 = CDataUtil.genId(id1);  // Config 2's ID
		prjDesc.createConfiguration(id2, "config2", desc);
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Get all the configurations
		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] descs = prjDesc.getConfigurations();
		assertTrue(descs.length == 2);

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager.getContributedEnvironment();

		// Try setting an environment variable
		final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR");
		contribEnv.addVariable(var, prjDesc.getConfigurationById(id1));

		// Check that the variable exists on the config1 & not in config2:
		IEnvironmentVariable var2 = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var2, var);
		var2 = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id2), true);
		assertNull(var2);

		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Close and reopen, variables should still exist
		project.close(null);
		project.open(null);
		descs = CoreModel.getDefault().getProjectDescription(project).getConfigurations();
		var2 = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var2, var);
		var2 = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id2), true);
		assertNull(var2);
	}


	public void testNoChangeToOneVariable() throws Exception {
		final IProject project = ResourceHelper.createCDTProjectWithConfig("envProject");

		// Add another, derived configuration
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
		final String id1 = desc.getId();

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager.getContributedEnvironment();

		// Try setting an environment variable
		final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR");
		final IEnvironmentVariable var1 = new EnvironmentVariable("FOO1", "BAR1");
		final IEnvironmentVariable var2 = new EnvironmentVariable("FOO2", "BAR2");		
		contribEnv.addVariable(var, prjDesc.getConfigurationById(id1));
		contribEnv.addVariable(var1, prjDesc.getConfigurationById(id1));
		contribEnv.addVariable(var2, prjDesc.getConfigurationById(id1));

		// Check that the variable exists on config1
		IEnvironmentVariable readVar = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var, readVar);
		readVar = envManager.getVariable(var1.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var1, readVar);
		readVar = envManager.getVariable(var2.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var2, readVar);

		// Save the project description
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Close and open the project
		project.close(null);
		project.open(null);
		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		final IEnvironmentVariable var3 = new EnvironmentVariable("FOO", "BAZ");
		contribEnv.addVariable(var3, prjDesc.getConfigurationById(id1));
		readVar = envManager.getVariable(var3.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var3, readVar);
		readVar = envManager.getVariable(var1.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var1, readVar);
		// Save the project description
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Close and open the project
		project.close(null);
		project.open(null);
		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		
		readVar = envManager.getVariable(var3.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var3, readVar);
		readVar = envManager.getVariable(var1.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var1, readVar);
		readVar = envManager.getVariable(var2.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var2, readVar);
	}
	

	/**
	 * This bug checks for an environment load race during project open / import.
	 *
	 * This occurs because enviornment is stored using platform Preferences (persisted in
	 * the .settings directory) and, when background refresh is enabled this is loaded
	 * asynchronously.
	 *
	 * The model shouldn't cache incorrect variables / values in the project description
	 * @throws Exception
	 */
	public void testBug265282() throws Exception {
		final IProject project = ResourceHelper.createCDTProjectWithConfig("envProject");

		// Add another, derived configuration
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
		final String id1 = desc.getId();

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager.getContributedEnvironment();

		// Try setting an environment variable
		final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR");
		final IEnvironmentVariable var1 = new EnvironmentVariable("FOO1", "BAR1");
		final IEnvironmentVariable var2 = new EnvironmentVariable("FOO2", "BAR2");		
		contribEnv.addVariable(var, prjDesc.getConfigurationById(id1));

		// Check that the variable exists on config1
		IEnvironmentVariable readVar = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var, readVar);

		// Save the project description
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Delete and reimport the project, environment should persist...
		project.close(null);
		project.delete(false, null);

		IJobManager jm = Job.getJobManager();
		ISchedulingRule root = ResourcesPlugin.getWorkspace().getRoot();

		// Var 3 is to overwrite var2
		final IEnvironmentVariable var3 = new EnvironmentVariable("FOO2", "BAR3");
		try {
			// lock the workspace preventing any asynchronous refresh job from detecting new environment
			jm.beginRule(root, null);

			project.create(null);
			project.open(IResource.BACKGROUND_REFRESH, null);

			prjDesc = CoreModel.getDefault().getProjectDescription(project);
			readVar = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
			// At this point readVar will be null -- we've locked the resource tree, so async refresh can't proceed
			assertNull(readVar);

			// Remove one variable
			envManager.getContributedEnvironment().removeVariable(var2.getName(), prjDesc.getConfigurationById(id1));
			// repalce one with another
			envManager.getContributedEnvironment().addVariable(var3, prjDesc.getConfigurationById(id1));
		} finally {
			jm.endRule(root);
		}

		// Make everything up to date
		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		// Environment should now be correct
		readVar = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var, readVar);
		readVar = envManager.getVariable(var1.getName(), prjDesc.getConfigurationById(id1), true);
		assertNull(readVar);
		readVar = envManager.getVariable(var2.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var3, readVar);
		
		// Get project description should only have the persisted envvar
		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		readVar = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var, readVar);
		readVar = envManager.getVariable(var1.getName(), prjDesc.getConfigurationById(id1), true);
		assertNull(readVar);
		readVar = envManager.getVariable(var2.getName(), prjDesc.getConfigurationById(id1), true);
		assertNull(readVar);
	}

}
