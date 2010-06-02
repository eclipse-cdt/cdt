/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.projectmodel.tests;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.testplugin.BuildSystemTestHelper;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
/**
 * Creates a project in a loop and checks that it is created with appropriate number
 * of build configurations
 *
 */
public class CProjectDescriptionSerializationTests extends TestCase {
	/**
	 * @return Test
	 */
	public static Test suite() {
		return new TestSuite(CProjectDescriptionSerializationTests.class);
	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}

	/**
	 * This test is intended to test serialization of C++ project
	 * @throws Exception
	 */
	public void testTooManyConfigurations() throws Exception {
		String projectName = "testTooManyConfigurations";
		String pluginProjectTypeId = "cdt.managedbuild.target.gnu.cygwin.exe";

		CoreModel coreModel = CoreModel.getDefault();
		
		{
			// Create model project and accompanied descriptions
			IProject project = BuildSystemTestHelper.createProject(projectName);
			ICProjectDescription des = coreModel.createProjectDescription(project, false);
			Assert.assertNotNull("createDescription returned null!", des);

			{
				ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
				IProjectType type = ManagedBuildManager.getProjectType(pluginProjectTypeId);
				Assert.assertNotNull("project type not found", type);

				ManagedProject mProj = new ManagedProject(project, type);
				info.setManagedProject(mProj);

				IConfiguration cfgs[] = type.getConfigurations();
				Assert.assertNotNull("configurations not found", cfgs);
				Assert.assertTrue("no configurations found in the project type",cfgs.length>0);

				for (IConfiguration configuration : cfgs) {
					String id = ManagedBuildManager.calculateChildId(configuration.getId(), null);
					Configuration config = new Configuration(mProj, (Configuration)configuration, id, false, true, false);
					CConfigurationData data = config.getConfigurationData();
					Assert.assertNotNull("data is null for created configuration", data);
					ICConfigurationDescription cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
				}
				Assert.assertEquals(2, des.getConfigurations().length);
			}

			// Persist the project
			coreModel.setProjectDescription(project, des);
			project.close(null);
		}


		{
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();

			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);

			// Trying to induce an anomaly
			for (int i=0;i<144;i++)
			{
				// Open project
				IProject project = root.getProject(projectName);
				project.open(null);
				Assert.assertEquals(true, project.isOpen());

				// Check project description
				ICProjectDescription des = coreModel.getProjectDescription(project);
				Assert.assertEquals(2, des.getConfigurations().length);

				IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
				// once in a while managedProject.getConfigurations() can return null
				// inside buildInfo.getConfigurationNames() which results in NPE
				String[] configurationNames = buildInfo.getConfigurationNames();
				// this Assert triggers as well on occasion
				Assert.assertNotNull("buildInfo.getConfigurationNames() returned null", configurationNames);

				IConfiguration configurations[] = buildInfo.getManagedProject().getConfigurations();
				// this condition is not supposed to be true
				// since the project is supposed to have exactly 2 configurations
				if (configurations.length != 2) {
					String message = i + "-th round: Invalid number (not 2) of configurations loaded. ";
					for (IConfiguration configuration : configurations) {
						message = message + "["+configuration.getName()+"], ";
					}
					Assert.assertEquals(message, 2, configurations.length);
				}

				project.close(null);
			}
		}
	}
	
	/**
	 * This test is intended to check persistentProperties after a project is created.
	 * @throws Exception
	 */
	public void testPersistentProperties() throws Exception {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		
		String pluginProjectTypeId = "cdt.managedbuild.target.gnu.cygwin.exe";
		final String projectName = "testPersistentProperties";
		
		{
			// Create model project and accompanied descriptions
			IProject project = BuildSystemTestHelper.createProject(projectName);
			ICProjectDescription des = coreModel.createProjectDescription(project, false);
			Assert.assertNotNull("createDescription returned null!", des);
			
			{
				ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
				IProjectType type = ManagedBuildManager.getProjectType(pluginProjectTypeId);
				Assert.assertNotNull("project type not found", type);
				
				ManagedProject mProj = new ManagedProject(project, type);
				info.setManagedProject(mProj);
				
				IConfiguration cfgs[] = type.getConfigurations();
				Assert.assertNotNull("configurations not found", cfgs);
				Assert.assertTrue("no configurations found in the project type",cfgs.length>0);
				
				for (IConfiguration configuration : cfgs) {
					String id = ManagedBuildManager.calculateChildId(configuration.getId(), null);
					Configuration config = new Configuration(mProj, (Configuration)configuration, id, false, true, false);
					CConfigurationData data = config.getConfigurationData();
					Assert.assertNotNull("data is null for created configuration", data);
					ICConfigurationDescription cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
				}
				Assert.assertEquals(2, des.getConfigurations().length);
			}
			
			coreModel.setProjectDescription(project, des);
			Assert.assertEquals(project, des.getProject());
			
			Thread.sleep(1000);  // let scanner discovery participate
			try {
				QualifiedName pdomName = new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomName");
				QualifiedName activeCfg = new QualifiedName(CCorePlugin.PLUGIN_ID, "activeConfiguration");
				QualifiedName settingCfg = new QualifiedName(CCorePlugin.PLUGIN_ID, "settingConfiguration");
				QualifiedName discoveredScannerConfigFileName = new QualifiedName(MakeCorePlugin.PLUGIN_ID, "discoveredScannerConfigFileName");
				
				assertTrue("pdomName", project.getPersistentProperties().containsKey(pdomName));
				assertTrue("activeCfg", project.getPersistentProperties().containsKey(activeCfg));
				assertTrue("discoveredScannerConfigFileName", project.getPersistentProperties().containsKey(discoveredScannerConfigFileName));
				assertTrue("settingCfg", project.getPersistentProperties().containsKey(settingCfg));
			} catch (CoreException e) {
				Assert.fail(e.getMessage());
			}
				
			project.close(null);
		}
	}

	public void testResetDefaultSetings_Bug298590() throws Exception {
		String pluginProjectTypeId = "cdt.managedbuild.target.gnu.cygwin.exe";

		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		
		String projectName = getName();
		String folderName = "Folder";
		
		// Create a managed project and a folder
		IProject project = ManagedBuildTestHelper.createProject(projectName, pluginProjectTypeId);
		IFolder folder = ManagedBuildTestHelper.createFolder(project, folderName);
		IPath folderPath = folder.getProjectRelativePath();
		
		// Initial project description after creating the project
		ICProjectDescription initialProjectDescription = mngr.getProjectDescription(project);
		assertNotNull("createDescription returned null!", initialProjectDescription);
		assertEquals(2, initialProjectDescription.getConfigurations().length);
		
		{
			// No folder description initially as it does not have any custom settings
			ICConfigurationDescription cfgDescription = initialProjectDescription.getConfigurations()[0];
			ICResourceDescription rcDescription = cfgDescription.getResourceDescription(folderPath, true);
			assertNull(rcDescription);
		}

		// Properties window: get project description: prjd
		ICProjectDescription propertyProjectDescription = CoreModel.getDefault().getProjectDescription(project);
		{
			// Still no folder description
			ICConfigurationDescription cfgDescription = propertyProjectDescription.getConfigurations()[0];
			ICResourceDescription rcDescription = cfgDescription.getResourceDescription(folderPath, true);
			assertNull(rcDescription);

			// getResDesc() creates default folder description
			ICFolderDescription parentDescription = (ICFolderDescription)cfgDescription.getResourceDescription(folderPath, false);
			assertEquals("/", parentDescription.toString());
			ICFolderDescription folderDescription = cfgDescription.createFolderDescription(folderPath, parentDescription);
			assertEquals(folderPath,folderDescription.getPath());
		}
		
		// OK button, persist the property project description prjd.
		coreModel.setProjectDescription(project, propertyProjectDescription);
		
		{
			// Folder description should be null as no custom settings were defined
			ICProjectDescription prjDescription = mngr.getProjectDescription(project);
			ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
			ICResourceDescription rcDescription = cfgDescription.getResourceDescription(folderPath, true);
			assertNull(rcDescription);
		}

		// Close project
		project.close(null);
	}

}
