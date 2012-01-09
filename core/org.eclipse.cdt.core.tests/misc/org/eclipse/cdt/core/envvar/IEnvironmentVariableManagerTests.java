/*******************************************************************************
 * Copyright (c) 2009, 2010 Broadcom Corp. and others.
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
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
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

		assertFalse(descs[0].isModified());
		assertFalse(descs[1].isModified());

		// Try setting an environment variable
		final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR");
		contribEnv.addVariable(var, prjDesc.getConfigurationById(id1));
		assertTrue(prjDesc.getConfigurationById(id1).isModified());
		assertFalse(prjDesc.getConfigurationById(id2).isModified());

		// Check that the variable exists on the config1 & not in config2:
		IEnvironmentVariable var2 = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var2, var);
		var2 = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id2), true);
		assertNull(var2);

		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Close and reopen, variables should still exist
		project.close(null);
		project.open(null);
		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		var2 = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var2, var);
		var2 = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id2), true);
		assertNull(var2);
		assertFalse(prjDesc.getConfigurationById(id1).isModified());
		assertFalse(prjDesc.getConfigurationById(id2).isModified());
	}

	/**
	 * Tests that we can load the environment stored as a single (old-style) long XML string
	 * Also tests that an an external change to the settings file is correctly picked up.
	 * @throws Exception
	 */
	public void testOldStyleLoad() throws Exception {
		final IProject project = ResourceHelper.createCDTProjectWithConfig("envProjectOldStyleLoad");

		// Add another, derived configuration
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
		final String id1 = desc.getId(); // Config 1's ID
		final String id2 = "712427638";  // Config 2's ID
		prjDesc.createConfiguration(id2, "config2", desc);
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager.getContributedEnvironment();
		final IEnvironmentVariable varOrig = new EnvironmentVariable("FOO", "ZOO");
		contribEnv.addVariable(varOrig, prjDesc.getConfigurationById(id2));
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		final String env = "#Mon Nov 16 21:47:46 GMT 2009\n" +
			"eclipse.preferences.version=1\n" +
			"environment/project/712427638=<?xml version\\=\"1.0\" encoding\\=\"UTF-8\" standalone\\=\"no\"?>\\n" +
			"<environment append\\=\"true\" appendContributed\\=\"true\">\\n" +
			"<variable delimiter\\=\"\\:\" name\\=\"FOO1\" operation\\=\"replace\" value\\=\"BAR1\"/>\\n" +
			"<variable delimiter\\=\"\\:\" name\\=\"FOO2\" operation\\=\"replace\" value\\=\"BAR2\"/>\\n" +
			"<variable delimiter\\=\"\\;\" name\\=\"FOO\" operation\\=\"append\" value\\=\"BAR\"/>\\n</environment>\n";
		project.getFile(".settings/org.eclipse.cdt.core.prefs").setContents(new ByteArrayInputStream(env.getBytes("UTF-8")), true, false, null);

		final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR", IEnvironmentVariable.ENVVAR_APPEND, ";");
		final IEnvironmentVariable var1 = new EnvironmentVariable("FOO1", "BAR1", ":");
		final IEnvironmentVariable var2 = new EnvironmentVariable("FOO2", "BAR2", ":");

		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		assertEquals(var, envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id2), true));
		assertEquals(var1, envManager.getVariable(var1.getName(), prjDesc.getConfigurationById(id2), true));
		assertEquals(var2, envManager.getVariable(var2.getName(), prjDesc.getConfigurationById(id2), true));
	}

	/**
	 * Tests we can load an old-style preferences while an incompatible scheduling rule is held.
	 * @throws Exception
	 */
	public void testOldStyleLoadConflictingSchedulingRule() throws Exception {
		final IProject project = ResourceHelper.createCDTProjectWithConfig("incompatibleSchedRule");

		// Add another, derived configuration
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
		final String id1 = desc.getId(); // Config 1's ID
		final String id2 = "712427638";  // Config 2's ID
		prjDesc.createConfiguration(id2, "config2", desc);
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager.getContributedEnvironment();
		final IEnvironmentVariable varOrig = new EnvironmentVariable("FOO", "ZOO");
		contribEnv.addVariable(varOrig, prjDesc.getConfigurationById(id2));
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		final String env = "#Mon Nov 16 21:47:46 GMT 2009\n" +
		"eclipse.preferences.version=1\n" +
		"environment/project/712427638=<?xml version\\=\"1.0\" encoding\\=\"UTF-8\" standalone\\=\"no\"?>\\n" +
		"<environment append\\=\"true\" appendContributed\\=\"true\">\\n" +
		"<variable delimiter\\=\"\\:\" name\\=\"FOO1\" operation\\=\"replace\" value\\=\"BAR1\"/>\\n" +
		"<variable delimiter\\=\"\\:\" name\\=\"FOO2\" operation\\=\"replace\" value\\=\"BAR2\"/>\\n" +
		"<variable delimiter\\=\"\\;\" name\\=\"FOO\" operation\\=\"append\" value\\=\"BAR\"/>\\n</environment>\n";
		project.getFile(".settings/org.eclipse.cdt.core.prefs").setContents(new ByteArrayInputStream(env.getBytes("UTF-8")), true, false, null);

		ISchedulingRule incompatibleRule = new ISchedulingRule() {
			@Override
			public boolean isConflicting(ISchedulingRule rule) {
				return rule == this || rule instanceof IResource;
			}
			@Override
			public boolean contains(ISchedulingRule rule) {
				return rule == this;
			}
		};
		try {
			Job.getJobManager().beginRule(incompatibleRule, new NullProgressMonitor());
			final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR", IEnvironmentVariable.ENVVAR_APPEND, ";");
			final IEnvironmentVariable var1 = new EnvironmentVariable("FOO1", "BAR1", ":");
			final IEnvironmentVariable var2 = new EnvironmentVariable("FOO2", "BAR2", ":");
	
			prjDesc = CoreModel.getDefault().getProjectDescription(project);
			assertEquals(var, envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id2), true));
			assertEquals(var1, envManager.getVariable(var1.getName(), prjDesc.getConfigurationById(id2), true));
			assertEquals(var2, envManager.getVariable(var2.getName(), prjDesc.getConfigurationById(id2), true));
		} finally {
			Job.getJobManager().endRule(incompatibleRule);
		}

		// Change back to FOO => ZOO , close and re-open the project and check its still there
		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		contribEnv.addVariable(varOrig, prjDesc.getConfigurationById(id2));
		CoreModel.getDefault().setProjectDescription(project, prjDesc);
		project.close(null);
		project.open(null);

		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		assertEquals(varOrig, envManager.getVariable(varOrig.getName(), prjDesc.getConfigurationById(id2), true));
	}

	/**
	 * Test that an ovewrite of new style preferences is loaded correctly
	 * @throws Exception
	 */
	public void testNewStyleOverwrite() throws Exception {
		final IProject project = ResourceHelper.createCDTProjectWithConfig("envProjectNewStyleLoad");

		// Add another, derived configuration
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
		final String id1 = desc.getId(); // Config 1's ID
		final String id2 = "712427638";  // Config 2's ID
		prjDesc.createConfiguration(id2, "config2", desc);
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager.getContributedEnvironment();
		// Variable which will be overwritten
		final IEnvironmentVariable varOrig = new EnvironmentVariable("FOO", "ZOO");
		contribEnv.addVariable(varOrig, prjDesc.getConfigurationById(id2));
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		final String env = "environment/project/712427638/FOO/delimiter=;\n" +
						   "environment/project/712427638/FOO/operation=append\n" +
						   "environment/project/712427638/FOO/value=BAR\n" +
						   "environment/project/712427638/FOO1/delimiter=\\:\n" +
						   "environment/project/712427638/FOO1/operation=replace\n" +
						   "environment/project/712427638/FOO1/value=BAR1\n" +
						   "environment/project/712427638/FOO2/delimiter=\\:\n" +
						   "environment/project/712427638/FOO2/operation=replace\n" +
						   "environment/project/712427638/FOO2/value=BAR2\n" +
						   "environment/project/712427638/append=true\n" +
						   "environment/project/712427638/appendContributed=true\n";
		project.getFile(".settings/org.eclipse.cdt.core.prefs").setContents(new ByteArrayInputStream(env.getBytes("UTF-8")), true, false, null);

		final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR", IEnvironmentVariable.ENVVAR_APPEND, ";");
		final IEnvironmentVariable var1 = new EnvironmentVariable("FOO1", "BAR1", ":");
		final IEnvironmentVariable var2 = new EnvironmentVariable("FOO2", "BAR2", ":");

		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		assertEquals(var, envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id2), true));
		assertEquals(var1, envManager.getVariable(var1.getName(), prjDesc.getConfigurationById(id2), true));
		assertEquals(var2, envManager.getVariable(var2.getName(), prjDesc.getConfigurationById(id2), true));
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
	 * tests the get / set append persisting
	 */
	public void testGetSetAppend() throws Exception {
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
		assertEquals(2, descs.length);

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager.getContributedEnvironment();

		assertFalse(descs[0].isModified());
		assertFalse(descs[1].isModified());

		// Set append & append contributed on the 2 configs respectively
		final boolean append = contribEnv.appendEnvironment(prjDesc.getConfigurationById(id2));
		contribEnv.setAppendEnvironment(!append, prjDesc.getConfigurationById(id2));
		assertEquals(!append, contribEnv.appendEnvironment(prjDesc.getConfigurationById(id2)));
		assertFalse(prjDesc.getConfigurationById(id1).isModified());
		assertTrue(prjDesc.getConfigurationById(id2).isModified());

		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Close and reopen, variables should still exist
		project.close(null);
		project.open(null);
		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		assertEquals(!append, contribEnv.appendEnvironment(prjDesc.getConfigurationById(id2)));
		assertFalse(prjDesc.getConfigurationById(id1).isModified());
		assertFalse(prjDesc.getConfigurationById(id2).isModified());
	}

	/**
	 * Tests file system change of the settings file
	 */
	public void testSettingsOverwrite() throws Exception {
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

		assertFalse(descs[0].isModified());
		assertFalse(descs[1].isModified());

		// Try setting an environment variable
		final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR");
		contribEnv.addVariable(var, prjDesc.getConfigurationById(id1));
		assertTrue(prjDesc.getConfigurationById(id1).isModified());
		assertFalse(prjDesc.getConfigurationById(id2).isModified());
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Backup the settings file
		project.getFile(".settings/org.eclipse.cdt.core.prefs.bak").create(
				project.getFile(".settings/org.eclipse.cdt.core.prefs").getContents(), true, null);

		// Change the environment variable
		final IEnvironmentVariable var2 = new EnvironmentVariable("FOO", "BOO");
		contribEnv.addVariable(var2, prjDesc.getConfigurationById(id1));
		assertEquals(var2, envManager.getVariable(var2.getName(), prjDesc.getConfigurationById(id1), true));
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Replace the settings with it's backup
		project.getFile(".settings/org.eclipse.cdt.core.prefs").setContents(
				project.getFile(".settings/org.eclipse.cdt.core.prefs.bak").getContents(), true, false, null);
		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		assertEquals(var, envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true));
	}

	/**
	 * Tests file system change of the settings file without recreating the project description
	 */
	public void testSettingsOverwriteBug295436() throws Exception {
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

		assertFalse(descs[0].isModified());
		assertFalse(descs[1].isModified());

		// Try setting an environment variable
		final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR");
		contribEnv.addVariable(var, prjDesc.getConfigurationById(id1));
		assertTrue(prjDesc.getConfigurationById(id1).isModified());
		assertFalse(prjDesc.getConfigurationById(id2).isModified());
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// Backup the settings file
		project.getFile(".settings/org.eclipse.cdt.core.prefs.bak").create(
				project.getFile(".settings/org.eclipse.cdt.core.prefs").getContents(), true, null);

		// Change the environment variable
		final IEnvironmentVariable var2 = new EnvironmentVariable("FOO", "BOO");
		contribEnv.addVariable(var2, prjDesc.getConfigurationById(id1));
		assertEquals(var2, envManager.getVariable(var2.getName(), prjDesc.getConfigurationById(id1), true));
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		// clean desc should be updated when the preference file is overwritten
		final ICProjectDescription cleanDesc = CoreModel.getDefault().getProjectDescription(project);
		assertEquals(contribEnv.getVariable(var.getName(), cleanDesc.getConfigurationById(id1)).getValue(), var2.getValue());

		// Replace the settings with it's backup
		project.getFile(".settings/org.eclipse.cdt.core.prefs").setContents(
				project.getFile(".settings/org.eclipse.cdt.core.prefs.bak").getContents(), true, false, null);
		// check that cleanDesc has been updated
		assertEquals(contribEnv.getVariable(var.getName(), cleanDesc.getConfigurationById(id1)).getValue(), var.getValue());
		assertEquals(var, envManager.getVariable(var.getName(), cleanDesc.getConfigurationById(id1), true));
	}

	/**
	 * Test that on deleting and recreating the project variables haven't persisted
	 * @throws Exception
	 */
	public void testBrokenCaching() throws Exception {
		final IProject project = ResourceHelper.createCDTProjectWithConfig("envProject");

		// Add another, derived configuration
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription desc = prjDesc.getActiveConfiguration();
		final String id1 = desc.getId();

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager.getContributedEnvironment();
		// At the moment 0 variables are set
		assertEquals(0, contribEnv.getVariables(desc).length);

		// Try setting an environment variable
		final IEnvironmentVariable var = new EnvironmentVariable("FOO", "BAR");
		contribEnv.addVariable(var, prjDesc.getConfigurationById(id1));
		assertEquals(1, contribEnv.getVariables(desc).length);
		// Check that the variable exists on config1
		IEnvironmentVariable readVar = envManager.getVariable(var.getName(), prjDesc.getConfigurationById(id1), true);
		assertEquals(var, readVar);

		// Save the project description
		CoreModel.getDefault().setProjectDescription(project, prjDesc);

		ResourceHelper.cleanUp();
		assertFalse(project.exists());
		ResourceHelper.createCDTProjectWithConfig("envProject");
		assertTrue(project.exists());
		// Fetch the current configuration
		prjDesc = CoreModel.getDefault().getProjectDescription(project);
		desc = prjDesc.getActiveConfiguration();
		assertEquals(0, contribEnv.getVariables(desc).length);
	}

	/**
	 * This bug checks for an environment load race during project open / import.
	 *
	 * This occurs because environment is stored using platform Preferences (persisted in
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

	/**
	 * This test checks if an environment variable is only processed as a list
	 * if it matches a certain pattern. ([^:]+:)+[^:]* (; on Windows)
	 * 
	 * If a variable is a list, it is split into a String array depending on the
	 * delimiter given. At some point, this array is used to build a new
	 * String representing the list, separated by the delimiter. This should
	 * only happen when the variable matches the pattern. For example, if a
	 * variable has a value of ':' without the quotes, it shouldn't processed as
	 * a list even if it contains a delimiter because if it was, it would give
	 * an empty string when built since there are no items in the list.
	 * 
	 * @throws Exception
	 */
	public void testBug284843() throws Exception{
		final IProject project = ResourceHelper.createCDTProjectWithConfig("envProject");
		ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription(project);
		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IContributedEnvironment contribEnv = envManager.getContributedEnvironment();
		ICConfigurationDescription confDesc = prjDesc.getActiveConfiguration();
		String delimiter = System.getProperty("path.separator");

		// Create the test variables
		IEnvironmentVariable varDelim = new EnvironmentVariable("DELIM",
				delimiter);
		String varListValue = "value1" + delimiter + "value2" + delimiter + "value3";
		IEnvironmentVariable varList = new EnvironmentVariable("LIST", varListValue);
		IEnvironmentVariable varListDelim = new EnvironmentVariable("LISTDELIM", 
				"value1" + delimiter);
		IEnvironmentVariable varListDelims = new EnvironmentVariable("LISTDELIMS", 
				varListValue + delimiter);
		String varInvalidListValue = delimiter + "value1" + delimiter + "value2" + delimiter + delimiter + "value3" + delimiter;
		IEnvironmentVariable varInvalidList = new EnvironmentVariable("INVALIDLIST", varInvalidListValue);
		
		// Add the variables to the contributed environment
		contribEnv.addVariable(varDelim, confDesc);
		contribEnv.addVariable(varList, confDesc);
		contribEnv.addVariable(varListDelim, confDesc);
		contribEnv.addVariable(varListDelims, confDesc);
		contribEnv.addVariable(varInvalidList, confDesc);
		
		// Get the processed variables
		varDelim = envManager.getVariable(varDelim.getName(), confDesc, true);
		varList = envManager.getVariable(varList.getName(), confDesc, true);
		varListDelim = envManager.getVariable(varListDelim.getName(), confDesc, true);
		varListDelims = envManager.getVariable(varListDelims.getName(), confDesc, true);
		varInvalidList = envManager.getVariable(varInvalidList.getName(), confDesc, true);
		
		// Should keep the same value, not a list
		assertEquals(delimiter, varDelim.getValue());
		
		// Should keep the same value, processed as a list
		assertEquals(varListValue, varList.getValue());
		
		// The delimiter will be trimmed, processed as a list
		assertEquals("value1", varListDelim.getValue());
		
		// The last delimiter will be trimmed, processed as a list
		assertEquals(varListValue, varListDelims.getValue()); 
		
		// Should keep the same value, not a list
		assertEquals(varInvalidListValue,varInvalidList.getValue()); 
	}

}
