/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.projectmodel.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.junit.jupiter.api.Test;

/**
 * Creates a project in a loop and checks that it is created with appropriate number
 * of build configurations
 *
 */
public class CProjectDescriptionSerializationTests extends BaseTestCase5 {

	/**
	 * This test is intended to test serialization of C++ project
	 * @throws Exception
	 */
	@Test
	public void testTooManyConfigurations() throws Exception {
		String projectName = "testTooManyConfigurations";
		String pluginProjectTypeId = "cdt.managedbuild.target.gnu.cygwin.exe";

		CoreModel coreModel = CoreModel.getDefault();

		{
			// Create model project and accompanied descriptions
			IProject project = BuildSystemTestHelper.createProject(projectName);
			ICProjectDescription des = coreModel.createProjectDescription(project, false);
			assertNotNull(des, "createDescription returned null!");

			{
				ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
				IProjectType type = ManagedBuildManager.getProjectType(pluginProjectTypeId);
				assertNotNull(type, "project type not found");

				ManagedProject mProj = new ManagedProject(project, type);
				info.setManagedProject(mProj);

				IConfiguration cfgs[] = type.getConfigurations();
				assertNotNull(cfgs, "configurations not found");
				assertTrue(cfgs.length > 0, "no configurations found in the project type");

				for (IConfiguration configuration : cfgs) {
					String id = ManagedBuildManager.calculateChildId(configuration.getId(), null);
					Configuration config = new Configuration(mProj, (Configuration) configuration, id, false, true,
							false);
					CConfigurationData data = config.getConfigurationData();
					assertNotNull(data, "data is null for created configuration");
					ICConfigurationDescription cfgDes = des
							.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
				}
				assertEquals((Object) 2, (Object) des.getConfigurations().length);
			}

			// Persist the project
			coreModel.setProjectDescription(project, des);
			ResourceHelper.joinIndexerBeforeCleanup(getName());
			project.close(null);
		}

		{
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();

			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);

			// Trying to induce an anomaly
			for (int i = 0; i < 144; i++) {
				// Open project
				IProject project = root.getProject(projectName);
				project.open(null);
				assertEquals(true, project.isOpen());

				// Check project description
				ICProjectDescription des = coreModel.getProjectDescription(project);
				assertEquals((Object) 2, (Object) des.getConfigurations().length);

				IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
				// once in a while managedProject.getConfigurations() can return null
				// inside buildInfo.getConfigurationNames() which results in NPE
				String[] configurationNames = buildInfo.getConfigurationNames();
				// this Assert triggers as well on occasion
				assertNotNull(configurationNames, "buildInfo.getConfigurationNames() returned null");

				IConfiguration configurations[] = buildInfo.getManagedProject().getConfigurations();
				// this condition is not supposed to be true
				// since the project is supposed to have exactly 2 configurations
				if (configurations.length != 2) {
					String message = i + "-th round: Invalid number (not 2) of configurations loaded. ";
					for (IConfiguration configuration : configurations) {
						message = message + "[" + configuration.getName() + "], ";
					}
					assertEquals((Object) 2, (Object) configurations.length, message);
				}

				ResourceHelper.joinIndexerBeforeCleanup(getName());
				project.close(null);
			}
		}
	}

	/**
	 * This test is intended to check persistentProperties after a project is created.
	 * @throws Exception
	 */
	@Test
	public void testPersistentProperties() throws Exception {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();

		String pluginProjectTypeId = "cdt.managedbuild.target.gnu.cygwin.exe";
		final String projectName = "testPersistentProperties";

		{
			// Create model project and accompanied descriptions
			IProject project = BuildSystemTestHelper.createProject(projectName);
			ICProjectDescription des = coreModel.createProjectDescription(project, false);
			des.setConfigurationRelations(ICProjectDescription.CONFIGS_INDEPENDENT);
			assertNotNull(des, "createDescription returned null!");

			{
				ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
				IProjectType type = ManagedBuildManager.getProjectType(pluginProjectTypeId);
				assertNotNull(type, "project type not found");

				ManagedProject mProj = new ManagedProject(project, type);
				info.setManagedProject(mProj);

				IConfiguration cfgs[] = type.getConfigurations();
				assertNotNull(cfgs, "configurations not found");
				assertTrue(cfgs.length > 0, "no configurations found in the project type");

				for (IConfiguration configuration : cfgs) {
					String id = ManagedBuildManager.calculateChildId(configuration.getId(), null);
					Configuration config = new Configuration(mProj, (Configuration) configuration, id, false, true,
							false);
					CConfigurationData data = config.getConfigurationData();
					assertNotNull(data, "data is null for created configuration");
					ICConfigurationDescription cfgDes = des
							.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
				}
				assertEquals((Object) 2, (Object) des.getConfigurations().length);
			}

			coreModel.setProjectDescription(project, des);
			assertEquals(project, des.getProject());

			QualifiedName pdomName = new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomName");
			QualifiedName activeCfg = new QualifiedName(CCorePlugin.PLUGIN_ID, "activeConfiguration");
			QualifiedName settingCfg = new QualifiedName(CCorePlugin.PLUGIN_ID, "settingConfiguration");
			QualifiedName discoveredScannerConfigFileName = new QualifiedName(MakeCorePlugin.PLUGIN_ID,
					"discoveredScannerConfigFileName");

			// pdomName is set by indexer setup, which may still be postponed or not even
			// scheduled yet, so we can't join the job. Just wait for the property to appear.
			// (The other properties were set synchronously in setProjectDescription().)
			for (int i = 0; i < 100 && !project.getPersistentProperties().containsKey(pdomName); i++) {
				Thread.sleep(100);
			}

			assertTrue(project.getPersistentProperties().containsKey(pdomName), "pdomName");
			assertTrue(project.getPersistentProperties().containsKey(activeCfg), "activeCfg");
			assertTrue(project.getPersistentProperties().containsKey(discoveredScannerConfigFileName),
					"discoveredScannerConfigFileName");
			assertTrue(project.getPersistentProperties().containsKey(settingCfg), "settingCfg");

			ResourceHelper.joinIndexerBeforeCleanup(getName());
			project.close(null);
		}
	}

	@Test
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
		assertNotNull(initialProjectDescription, "createDescription returned null!");
		assertEquals((Object) 2, (Object) initialProjectDescription.getConfigurations().length);

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
			ICFolderDescription parentDescription = (ICFolderDescription) cfgDescription
					.getResourceDescription(folderPath, false);
			assertEquals("/", parentDescription.toString());
			ICFolderDescription folderDescription = cfgDescription.createFolderDescription(folderPath,
					parentDescription);
			assertEquals(folderPath, folderDescription.getPath());
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
		ResourceHelper.joinIndexerBeforeCleanup(getName());
		project.close(null);
	}

}
