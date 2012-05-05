/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.language.settings.providers.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsPersistenceProjectTests;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.ConfigurationDataProvider;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Test creation of a new project in respect with language settings providers.
 */
public class LanguageSettingsProvidersMBSTest extends BaseTestCase {
	private static final String MBS_LANGUAGE_SETTINGS_PROVIDER_ID = ScannerDiscoveryLegacySupport.MBS_LANGUAGE_SETTINGS_PROVIDER_ID;
	private static final String USER_LANGUAGE_SETTINGS_PROVIDER_ID = ScannerDiscoveryLegacySupport.USER_LANGUAGE_SETTINGS_PROVIDER_ID;
	private static final String GCC_SPECS_DETECTOR_ID = "org.eclipse.cdt.managedbuilder.core.GCCBuiltinSpecsDetector";
	private static final String PROJECT_TYPE_EXECUTABLE_GNU = "cdt.managedbuild.target.gnu.exe";
	private static final String LANGUAGE_SETTINGS_PROJECT_XML = LanguageSettingsPersistenceProjectTests.LANGUAGE_SETTINGS_PROJECT_XML;
	private static final String LANGUAGE_SETTINGS_WORKSPACE_XML = LanguageSettingsPersistenceProjectTests.LANGUAGE_SETTINGS_WORKSPACE_XML;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		ManagedBuildTestHelper.removeProject(this.getName());
		super.tearDown();
	}

	/**
	 * Imitate a new Project Wizard. New Project Wizards really do these things in CDT.
	 */
	private static IProject imitateNewProjectWizard(String name, String projectTypeId) throws CoreException {
		IProject project = ManagedBuildTestHelper.createProject(name, projectTypeId);
		ManagedBuildTestHelper.addManagedBuildNature(project);

		ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgDescription);
			ConfigurationDataProvider.setDefaultLanguageSettingsProviders(project, cfg, cfgDescription);

			assertTrue(((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders().size() > 0);
		}

		CoreModel.getDefault().setProjectDescription(project, prjDescription);

		return project;
	}

	/**
	 * Test new GNU Executable project.
	 */
	public void testGnuToolchainProviders() throws Exception {
		// create a new project imitating wizard
		IProject project = imitateNewProjectWizard(this.getName(), PROJECT_TYPE_EXECUTABLE_GNU);

		// check that the language settings providers are in place.
		ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, false);
		assertNotNull(prjDescription);
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			{
				ILanguageSettingsProvider provider = providers.get(0);
				String id = provider.getId();
				assertEquals(USER_LANGUAGE_SETTINGS_PROVIDER_ID, id);
				assertEquals(false, LanguageSettingsManager.isPreferShared(id));
				assertEquals(false, LanguageSettingsManager.isWorkspaceProvider(provider));
			}
			{
				ILanguageSettingsProvider provider = providers.get(1);
				String id = provider.getId();
				assertEquals(MBS_LANGUAGE_SETTINGS_PROVIDER_ID, id);
				assertEquals(true, LanguageSettingsManager.isPreferShared(id));
				assertEquals(true, LanguageSettingsManager.isWorkspaceProvider(provider));
			}
			{
				ILanguageSettingsProvider provider = providers.get(2);
				String id = provider.getId();
				assertEquals(GCC_SPECS_DETECTOR_ID, id);
				assertEquals(true, LanguageSettingsManager.isPreferShared(id));
				assertEquals(true, LanguageSettingsManager.isWorkspaceProvider(provider));
			}
			assertEquals(3, providers.size());
		}
	}

	/**
	 * Test that no unnecessary storage file is created for language settings for default set
	 * of language settings providers.
	 */
	public void testProjectPersistence_Defaults() throws Exception {
		// create a new project imitating wizard
		IProject project = imitateNewProjectWizard(this.getName(), PROJECT_TYPE_EXECUTABLE_GNU);

		// double-check that the project contains language settings providers
		ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, false);
		assertNotNull(prjDescription);
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			String[] defaultIds = ((ILanguageSettingsProvidersKeeper) cfgDescription).getDefaultLanguageSettingsProvidersIds();
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(defaultIds.length, providers.size());
			for (int i = 0; i < defaultIds.length; i++) {
				assertEquals(providers.get(i).getId(), defaultIds[i]);
			}
			assertTrue(defaultIds.length > 0);
		}

		// no settings file in project area
		IFile xmlStorageFile = project.getFile(LANGUAGE_SETTINGS_PROJECT_XML);
		assertEquals(false, xmlStorageFile.exists());
		assertEquals(false, xmlStorageFile.getParent().exists()); // .settings folder

		// no settings file in workspace area
		String xmlPrjWspStorageFileLocation = LanguageSettingsPersistenceProjectTests.getStoreLocationInWorkspaceArea(project.getName()+'.'+LANGUAGE_SETTINGS_WORKSPACE_XML);
		java.io.File xmlStorageFilePrjWsp = new java.io.File(xmlPrjWspStorageFileLocation);
		assertEquals(false, xmlStorageFilePrjWsp.exists());
	}

	/**
	 * Test that storage file is created for language settings for empty set of language settings providers.
	 */
	public void testProjectPersistence_NoProviders() throws Exception {
		// create a new project imitating wizard
		IProject project = imitateNewProjectWizard(this.getName(), PROJECT_TYPE_EXECUTABLE_GNU);

		// remove language settings providers from the project
		ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
		for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(new ArrayList<ILanguageSettingsProvider>());
			assertTrue(((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders().size() == 0);
		}

		CoreModel.getDefault().setProjectDescription(project, prjDescription);

		// settings file appears in project area
		IFile xmlStorageFile = project.getFile(LANGUAGE_SETTINGS_PROJECT_XML);
		assertEquals(true, xmlStorageFile.exists());

		// no settings file in workspace area
		String xmlPrjWspStorageFileLocation = LanguageSettingsPersistenceProjectTests.getStoreLocationInWorkspaceArea(project.getName()+'.'+LANGUAGE_SETTINGS_WORKSPACE_XML);
		java.io.File xmlStorageFilePrjWsp = new java.io.File(xmlPrjWspStorageFileLocation);
		assertEquals(false, xmlStorageFilePrjWsp.exists());

	}

}
