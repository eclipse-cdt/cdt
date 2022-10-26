/*******************************************************************************
 * Copyright (c) 2011, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases to cover {@link ILanguageSettingsChangeListener} capabilities.
 */
public class LanguageSettingsListenersTests extends BaseTestCase5 {
	// These should match corresponding entries defined in plugin.xml
	private static final String EXTENSION_REGISTERER_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_REGISTERER_PROVIDER_ID;
	private static final String EXTENSION_EDITABLE_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_EDITABLE_PROVIDER_ID;

	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_NAME_1 = "test.provider.1.name";
	private static final String PROVIDER_CUSTOM_GLOBAL = "test.provider.custom.global.id";
	private static final String PROVIDER_CUSTOM_GLOBAL_NAME = "test.provider.custom.global.name";

	private static final CMacroEntry SAMPLE_LSE = new CMacroEntry("MACRO", "value", 0);

	/**
	 * Mock {@link ILanguageSettingsChangeListener}.
	 */
	private class MockLanguageSettingsChangeListener implements ILanguageSettingsChangeListener {
		private int count = 0;
		private ILanguageSettingsChangeEvent lastEvent = null;

		@Override
		public void handleEvent(ILanguageSettingsChangeEvent event) {
			count++;
			lastEvent = event;
		}

		public int getCount() {
			return count;
		}

		public void resetCount() {
			count = 0;
			lastEvent = null;
		}

		public ILanguageSettingsChangeEvent getLastEvent() {
			return lastEvent;
		}
	}

	private MockLanguageSettingsChangeListener mockLseListener = new MockLanguageSettingsChangeListener();

	@AfterEach
	protected void afterEachCleanup() throws Exception {
		LanguageSettingsManager.unregisterLanguageSettingsChangeListener(mockLseListener);
		LanguageSettingsManager.setWorkspaceProviders(null);
		try {
			Job.getJobManager().join(LanguageSettingsProvidersSerializer.JOB_FAMILY_SERIALIZE_LANGUAGE_SETTINGS_PROJECT,
					null);
			Job.getJobManager()
					.join(LanguageSettingsProvidersSerializer.JOB_FAMILY_SERIALIZE_LANGUAGE_SETTINGS_WORKSPACE, null);
		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * This method exists because PDE reports accessing
	 * ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled
	 * as an API violation, and having only one call means only one warning in this
	 * file instead of many.
	 */
	private void setLanguageSettingsProvidersFunctionalityEnabled(IProject project) {
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);
	}

	/**
	 * Check that global provider does not get unnecessarily registered on start.
	 */
	@Test
	public void testListenerRegisterer_CheckExtensionProvider() throws Exception {
		// check if extension provider exists
		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
				.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(workspaceProvider);
		assertNotNull(rawProvider);
		// global listeners providers get registered only lazily
		assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
	}

	/**
	 * Test events triggered for non-shared configuration owned provider.
	 */
	@Test
	public void testListenerRegisterer_OneOwnedByCfg() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider
			ILanguageSettingsProvider mockProvider = new MockListenerRegisterer(PROVIDER_1, PROVIDER_NAME_1);
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
			assertEquals(1, MockListenerRegisterer.getCount(PROVIDER_1));
		}
		{
			// close the project
			project.close(null);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_1));
		}
		{
			// reopen the project
			project.open(null);
			// initialize project description
			CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertEquals(1, MockListenerRegisterer.getCount(PROVIDER_1));
			// and delete
			project.delete(true, null);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_1));
		}

	}

	/**
	 * Test events triggered for non-shared configuration owned multiple providers.
	 */
	@Test
	public void testListenerRegisterer_TwoOwnedByCfgs() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProject(this.getName(), null, new String[] {
				"org.eclipse.cdt.core.tests.configuration.id.1", "org.eclipse.cdt.core.tests.configuration.id.2", });
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);
			assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);

			{
				// create a provider 1
				ILanguageSettingsProvider mockProvider = new MockListenerRegisterer(PROVIDER_1, PROVIDER_NAME_1);
				List<ILanguageSettingsProvider> providers = new ArrayList<>();
				providers.add(mockProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription1).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription1)
						.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			{
				// create a provider 2
				ILanguageSettingsProvider mockProvider = new MockListenerRegisterer(PROVIDER_1, PROVIDER_NAME_1);
				List<ILanguageSettingsProvider> providers = new ArrayList<>();
				providers.add(mockProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription2).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription2)
						.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
			assertEquals(2, MockListenerRegisterer.getCount(PROVIDER_1));
		}
		{
			// close the project
			project.close(null);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_1));
		}
		{
			// reopen the project
			project.open(null);
			// initialize project description
			CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertEquals(2, MockListenerRegisterer.getCount(PROVIDER_1));
			// and delete
			project.delete(true, null);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_1));
		}

	}

	/**
	 * Test events triggered for shared provider.
	 */
	@Test
	public void testListenerRegisterer_OneGlobal() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(workspaceProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// close the project
			project.close(null);
			assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// reopen the project
			project.open(null);
			// initialize project description
			CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
			// and delete
			project.delete(true, null);
			assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}

	}

	/**
	 * Test events triggered for multiple shared providers.
	 */
	@Test
	public void testListenerRegisterer_TwoGlobal() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProject(this.getName(), null, new String[] {
				"org.eclipse.cdt.core.tests.configuration.id.1", "org.eclipse.cdt.core.tests.configuration.id.2", });
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		{
			// retrieve global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);
			assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);

			{
				// add global provider to configuration 1
				List<ILanguageSettingsProvider> providers = new ArrayList<>();
				providers.add(workspaceProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription1).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription1)
						.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			{
				// add global provider to configuration 2
				List<ILanguageSettingsProvider> providers = new ArrayList<>();
				providers.add(workspaceProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription2).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription2)
						.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// close the project
			project.close(null);
			assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// reopen the project
			project.open(null);
			// initialize project description
			CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
			// and delete
			project.delete(true, null);
			assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
	}

	/**
	 * Test events triggered for shared provider when the provider removed from the list.
	 */
	@Test
	public void testListenerRegisterer_TwoGlobalMinusOne() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProject(this.getName(), null, new String[] {
				"org.eclipse.cdt.core.tests.configuration.id.1", "org.eclipse.cdt.core.tests.configuration.id.2", });
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		{
			// retrieve workspace provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);
			assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);

			{
				// add global provider to configuration 1
				List<ILanguageSettingsProvider> providers = new ArrayList<>();
				providers.add(workspaceProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription1).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription1)
						.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			{
				// add global provider to configuration 2
				List<ILanguageSettingsProvider> providers = new ArrayList<>();
				providers.add(workspaceProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription2).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription2)
						.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// retrieve workspace provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);
			assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);

			{
				// remove global provider from configuration 1
				List<ILanguageSettingsProvider> providers = new ArrayList<>();
				((ILanguageSettingsProvidersKeeper) cfgDescription1).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription1)
						.getLanguageSettingProviders();
				assertEquals(0, storedProviders.size());
			}
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// close the project
			project.close(null);
			assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
	}

	/**
	 * Test events triggered for shared provider define in multiple projects.
	 */
	@Test
	public void testListenerRegisterer_GlobalProviderTwoProjects() throws Exception {
		// create project 1
		IProject project_1 = ResourceHelper.createCDTProjectWithConfig(this.getName() + ".1");
		setLanguageSettingsProvidersFunctionalityEnabled(project_1);
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project_1, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(workspaceProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project_1, prjDescriptionWritable);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}

		// create project 2
		IProject project_2 = ResourceHelper.createCDTProjectWithConfig(this.getName() + ".2");
		setLanguageSettingsProvidersFunctionalityEnabled(project_2);
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project_2, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(workspaceProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project_2, prjDescriptionWritable);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}

		{
			// close project 1
			project_1.close(null);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// close project 2
			project_2.close(null);
			assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}

	}

	/**
	 * Test events triggered for shared global providers not included in any configuration.
	 */
	@Test
	public void testListenerRegisterer_GlobalProviderNotInUse() throws Exception {
		// create project
		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
				.getWorkspaceProvider(PROVIDER_CUSTOM_GLOBAL);

		// the global custom provider has not been added yet
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(workspaceProvider);
		assertNull(rawProvider);
		assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));

		List<ILanguageSettingsProvider> workspaceProvidersOriginal = LanguageSettingsManager.getWorkspaceProviders();
		{
			// add global provider which is not engaged
			ILanguageSettingsProvider provider = new MockListenerRegisterer(PROVIDER_CUSTOM_GLOBAL,
					PROVIDER_CUSTOM_GLOBAL_NAME);
			List<ILanguageSettingsProvider> providers = new ArrayList<>(workspaceProvidersOriginal);
			providers.add(provider);
			LanguageSettingsManager.setWorkspaceProviders(providers);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}
		{
			// remove global provider and restore original list
			List<ILanguageSettingsProvider> providers = new ArrayList<>(workspaceProvidersOriginal);
			LanguageSettingsManager.setWorkspaceProviders(providers);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}
	}

	/**
	 * Test events triggered for shared global provider replacing another one in global list.
	 */
	@Test
	public void testListenerRegisterer_GlobalProviderAddRemoveOutsideTheProject() throws Exception {
		// create project
		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager
				.getWorkspaceProvider(PROVIDER_CUSTOM_GLOBAL);

		// the global custom provider has not been added yet
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(workspaceProvider);
		assertNull(rawProvider);
		assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));

		// prepare project
		List<ILanguageSettingsProvider> workspaceProvidersOriginal = LanguageSettingsManager.getWorkspaceProviders();
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add global provider
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(workspaceProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
			// the global custom provider has not been added yet
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}

		{
			// add global provider
			ILanguageSettingsProvider provider = new MockListenerRegisterer(PROVIDER_CUSTOM_GLOBAL,
					PROVIDER_CUSTOM_GLOBAL_NAME);
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(provider);
			LanguageSettingsManager.setWorkspaceProviders(providers);
			assertEquals(1, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}
		{
			// remove global provider
			List<ILanguageSettingsProvider> providers = new ArrayList<>(workspaceProvidersOriginal);
			LanguageSettingsManager.setWorkspaceProviders(providers);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}

		{
			// wait until serializing has finished
			Job.getJobManager()
					.join(LanguageSettingsProvidersSerializer.JOB_FAMILY_SERIALIZE_LANGUAGE_SETTINGS_WORKSPACE, null);
			// close project
			project.close(null);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}
	}

	/**
	 * Test events triggered when empty provider added and the resulting list of entries does not change.
	 */
	@Test
	public void testNotification_cfgProvider_AddEmptyProvider() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(0, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());

		// Add empty provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider and add to cfgDescription
			ILanguageSettingsProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1,
					PROVIDER_NAME_1);
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// No notifications expected
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());
	}

	/**
	 * Test events triggered where non-empty provider added.
	 */
	@Test
	public void testNotification_cfgProvider_AddNonEmptyProvider() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(0, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());

		// Add non-empty provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			String cfgDescriptionId = cfgDescription.getId();
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider and add entries
			MockLanguageSettingsEditableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1,
					PROVIDER_NAME_1);
			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			mockProvider.setSettingEntries(cfgDescription, project, null, entries);
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);

			// inspect notifications
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);

			assertEquals(project.getName(), event.getProjectName());
			assertEquals(1, event.getConfigurationDescriptionIds().length);
			assertEquals(cfgDescriptionId, event.getConfigurationDescriptionIds()[0]);
		}
	}

	/**
	 * Test events triggered during serialization.
	 */
	@Test
	public void testNotification_cfgProvider_SerializeEntries() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		// add the mock provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider and add to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1));
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());

		// Change the provider's entries
		{
			// get project descriptions
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			String cfgDescriptionId = cfgDescription.getId();

			// Add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			assertTrue(providers.get(0) instanceof MockLanguageSettingsEditableProvider);
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);
			mockProvider.setSettingEntries(cfgDescription, project, null, entries);
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());

			// Serialize settings
			LanguageSettingsManager.serializeLanguageSettings(prjDescription);
			// inspect event
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);

			assertEquals(project.getName(), event.getProjectName());
			assertEquals(1, event.getConfigurationDescriptionIds().length);
			assertEquals(cfgDescriptionId, event.getConfigurationDescriptionIds()[0]);
		}
	}

	/**
	 * Test events triggered when providers are being added by 2 independent parties in parallel.
	 */
	@Test
	public void testNotification_cfgProvider_SerializeEntriesConcurrent() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		// add the mock provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider and add to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1));
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());

		// Change the provider's entries concurrently

		// get project descriptions
		ICProjectDescription prjDescription_1 = CProjectDescriptionManager.getInstance().getProjectDescription(project,
				false);
		assertNotNull(prjDescription_1);
		ICProjectDescription prjDescription_2 = CProjectDescriptionManager.getInstance().getProjectDescription(project,
				false);
		assertNotNull(prjDescription_2);
		{
			ICConfigurationDescription[] cfgDescriptions = prjDescription_1.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			String cfgDescriptionId = cfgDescription.getId();

			// Add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			assertTrue(providers.get(0) instanceof MockLanguageSettingsEditableProvider);
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);
			mockProvider.setSettingEntries(cfgDescription, project, null, entries);

			// reset count
			mockLseListener.resetCount();
			assertEquals(0, mockLseListener.getCount());
			assertNull(mockLseListener.getLastEvent());

			// Serialize settings
			LanguageSettingsManager.serializeLanguageSettings(prjDescription_1);
			// inspect event
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);

			assertEquals(project.getName(), event.getProjectName());
			assertEquals(1, event.getConfigurationDescriptionIds().length);
			assertEquals(cfgDescriptionId, event.getConfigurationDescriptionIds()[0]);
		}
		{
			ICConfigurationDescription[] cfgDescriptions = prjDescription_2.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);
			String cfgDescriptionId = cfgDescription.getId();

			// Add same entries
			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			assertTrue(providers.get(0) instanceof MockLanguageSettingsEditableProvider);
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);
			mockProvider.setSettingEntries(cfgDescription, project, null, entries);

			// reset count
			mockLseListener.resetCount();
			assertEquals(0, mockLseListener.getCount());
			assertNull(mockLseListener.getLastEvent());

			// Serialize settings
			LanguageSettingsManager.serializeLanguageSettings(prjDescription_2);
			// inspect event
			assertEquals(0, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNull(event);
		}
	}

	/**
	 * Test events triggered during adding global empty provider.
	 */
	@Test
	public void testNotification_globalProvider_AddEmptyProvider() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(0, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());

		// Add empty global provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			// clear it
			((MockLanguageSettingsEditableProvider) rawProvider).clear();
			assertEquals(null, wspProvider.getSettingEntries(cfgDescription, project, null));
			// add the provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(wspProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// No notifications expected
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());
	}

	/**
	 * Test events triggered during adding global non-empty provider.
	 */
	@Test
	public void testNotification_globalProvider_AddNonEmptyProvider() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(0, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());

		// Add non-empty provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			String cfgDescriptionId = cfgDescription.getId();

			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			((MockLanguageSettingsEditableProvider) rawProvider).clear();
			// add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			((MockLanguageSettingsEditableProvider) rawProvider).setSettingEntries(cfgDescription, project, null,
					entries);
			assertEquals(SAMPLE_LSE, wspProvider.getSettingEntries(cfgDescription, project, null).get(0));
			// add the provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(wspProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);

			// inspect notifications
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);

			assertEquals(project.getName(), event.getProjectName());
			assertEquals(1, event.getConfigurationDescriptionIds().length);
			assertEquals(cfgDescriptionId, event.getConfigurationDescriptionIds()[0]);
		}
	}

	/**
	 * Test events triggered during serialization of global shared providers.
	 */
	@Test
	public void testNotification_globalProvider_SerializeEntries() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		// register mock listener to inspect the notifications
		LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);

		// Add empty global provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			// clear it
			((MockLanguageSettingsEditableProvider) rawProvider).clear();
			assertEquals(null, wspProvider.getSettingEntries(cfgDescription, project, null));
			// add the provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			providers.add(wspProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// Change the provider's entries
		{
			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			((MockLanguageSettingsEditableProvider) rawProvider).clear();
			// add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			((MockLanguageSettingsEditableProvider) rawProvider).setSettingEntries(null, project, null, entries);
			assertEquals(SAMPLE_LSE, wspProvider.getSettingEntries(null, project, null).get(0));

			// reset count
			mockLseListener.resetCount();
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = root.getProjects();
			if (projects.length != 1) {
				fail("Unexpected projects exist, only expected '" + project.toString() + "', got these projects: "
						+ Arrays.toString(projects));
			}

			// Serialize settings
			LanguageSettingsManager.serializeLanguageSettingsWorkspace();

			// get cfgDescriptionId
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			String cfgDescriptionId = cfgDescription.getId();

			// inspect event
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);

			assertEquals(project.getName(), event.getProjectName());
			assertEquals(1, event.getConfigurationDescriptionIds().length);
			assertEquals(cfgDescriptionId, event.getConfigurationDescriptionIds()[0]);
		}

		// Clear the provider's entries
		{
			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			// clear the provider again
			((MockLanguageSettingsEditableProvider) rawProvider).clear();

			// reset count
			mockLseListener.resetCount();
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());

			// Serialize settings
			LanguageSettingsManager.serializeLanguageSettingsWorkspace();

			// get cfgDescriptionId
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			String cfgDescriptionId = cfgDescription.getId();

			// inspect event
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);

			assertEquals(project.getName(), event.getProjectName());
			assertEquals(1, event.getConfigurationDescriptionIds().length);
			assertEquals(cfgDescriptionId, event.getConfigurationDescriptionIds()[0]);
		}

		// Change the provider's entries back to original state from extension point
		{

			ILanguageSettingsProvider extensionProviderCopy = LanguageSettingsManager
					.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID, true);
			List<ICLanguageSettingEntry> extEntries = extensionProviderCopy.getSettingEntries(null, null, null);

			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager
					.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			// reset the provider to match extension
			((MockLanguageSettingsEditableProvider) rawProvider).setSettingEntries(null, null, null, extEntries);
			assertTrue(LanguageSettingsManager.isEqualExtensionProvider(rawProvider, true));

			// reset count
			mockLseListener.resetCount();
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());

			// Serialize settings
			LanguageSettingsManager.serializeLanguageSettingsWorkspace();

			// get cfgDescriptionId
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			String cfgDescriptionId = cfgDescription.getId();

			// inspect event
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);

			assertEquals(project.getName(), event.getProjectName());
			assertEquals(1, event.getConfigurationDescriptionIds().length);
			assertEquals(cfgDescriptionId, event.getConfigurationDescriptionIds()[0]);
		}
	}

	/**
	 * Test case when a project is present in the list of resources in delta.
	 */
	@Test
	public void testDelta_AffectedResources_Project() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		// create a mock provider and add to cfgDescription
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add mock provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			MockLanguageSettingsEditableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1,
					PROVIDER_NAME_1);
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		{
			LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());
		}

		// trigger an event on the project
		ICConfigurationDescription cfgDescription;
		{
			// get project descriptions
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);

			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			mockProvider.setSettingEntries(cfgDescription, project, null, entries);
			mockProvider.serializeLanguageSettings(cfgDescription);
		}

		// inspect event
		{
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);
			assertEquals(event.getProjectName(), project.getName());

			Set<IResource> resources = event.getAffectedResources(cfgDescription.getId());
			assertNotNull(resources);
			assertEquals(project, resources.toArray()[0]);
			assertEquals(1, resources.size());
		}
	}

	/**
	 * Test case when a default resource (null) is represented in the list of resources in delta.
	 */
	@Test
	public void testDelta_AffectedResources_DefaultResource() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		// create a mock provider and add to cfgDescription
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add mock provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			MockLanguageSettingsEditableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1,
					PROVIDER_NAME_1);
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		{
			LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());
		}

		// trigger an event on the project
		ICConfigurationDescription cfgDescription;
		{
			// get project descriptions
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);

			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			mockProvider.setSettingEntries(cfgDescription, null, null, entries);
			mockProvider.serializeLanguageSettings(cfgDescription);
		}

		// inspect event
		{
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);
			assertEquals(event.getProjectName(), project.getName());

			Set<IResource> resources = event.getAffectedResources(cfgDescription.getId());
			assertNotNull(resources);
			assertEquals(project, resources.toArray()[0]);
			assertEquals(1, resources.size());
		}
	}

	/**
	 * Test case when a folder is present in the list of resources in delta.
	 */
	@Test
	public void testDelta_AffectedResources_Folder() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		IFolder folder = ResourceHelper.createFolder(project, "Folder");
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		// create a mock provider and add to cfgDescription
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add mock provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			MockLanguageSettingsEditableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1,
					PROVIDER_NAME_1);
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		{
			LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());
		}

		// trigger an event on the project
		ICConfigurationDescription cfgDescription;
		{
			// get project descriptions
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);

			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			mockProvider.setSettingEntries(cfgDescription, folder, null, entries);
			mockProvider.serializeLanguageSettings(cfgDescription);
		}

		// inspect event
		{
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);
			assertEquals(event.getProjectName(), project.getName());

			Set<IResource> resources = event.getAffectedResources(cfgDescription.getId());
			assertNotNull(resources);
			assertEquals(folder, resources.toArray()[0]);
			assertEquals(1, resources.size());
		}
	}

	/**
	 * Test case when a file is present in the list of resources in delta.
	 */
	@Test
	public void testDelta_AffectedResources_File() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		IFile file = ResourceHelper.createFile(project, "file.cpp");
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		// create a mock provider and add to cfgDescription
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add mock provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			MockLanguageSettingsEditableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1,
					PROVIDER_NAME_1);
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		{
			LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());
		}

		// trigger an event on the project
		ICConfigurationDescription cfgDescription;
		{
			// get project descriptions
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);

			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			mockProvider.setSettingEntries(cfgDescription, file, null, entries);
			mockProvider.serializeLanguageSettings(cfgDescription);
		}

		// inspect event
		{
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);
			assertEquals(event.getProjectName(), project.getName());

			Set<IResource> resources = event.getAffectedResources(cfgDescription.getId());
			assertNotNull(resources);
			assertEquals(file, resources.toArray()[0]);
			assertEquals(1, resources.size());
		}
	}

	/**
	 * Test case when a mix of files and folders is present in the list of resources in delta.
	 */
	@Test
	public void testDelta_AffectedResources_Mix() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		IFolder folder = ResourceHelper.createFolder(project, "Folder");
		IFile file1 = ResourceHelper.createFile(project, "file1.cpp");
		IFile file2 = ResourceHelper.createFile(project, "file2.cpp");
		IFile file3 = ResourceHelper.createFile(project, "file3.cpp");
		setLanguageSettingsProvidersFunctionalityEnabled(project);

		// create a mock provider and add to cfgDescription
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add mock provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<>();
			MockLanguageSettingsEditableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1,
					PROVIDER_NAME_1);
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// register mock listener to inspect the notifications
		{
			LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());
		}

		// trigger an event on the project
		ICConfigurationDescription cfgDescription;
		{
			// get project descriptions
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance()
					.getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);

			List<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(SAMPLE_LSE);
			mockProvider.setSettingEntries(cfgDescription, folder, null, entries);
			mockProvider.setSettingEntries(cfgDescription, file1, null, entries);
			mockProvider.setSettingEntries(cfgDescription, file2, null, entries);
			mockProvider.serializeLanguageSettings(cfgDescription);
		}

		// inspect event
		{
			assertEquals(1, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNotNull(event);
			assertEquals(event.getProjectName(), project.getName());

			Set<IResource> resources = event.getAffectedResources(cfgDescription.getId());
			assertNotNull(resources);
			assertTrue(resources.contains(folder));
			assertTrue(resources.contains(file1));
			assertTrue(resources.contains(file2));
			assertFalse(resources.contains(file3));
			assertEquals(3, resources.size());
		}
	}
}
