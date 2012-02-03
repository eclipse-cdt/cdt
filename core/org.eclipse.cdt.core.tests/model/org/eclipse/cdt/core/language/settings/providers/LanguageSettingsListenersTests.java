/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IProject;

/**
 * Test cases to cover {@link ILanguageSettingsChangeListener} capabilities.
 */
public class LanguageSettingsListenersTests extends BaseTestCase {
	// These should match corresponding entries defined in plugin.xml
	private static final String EXTENSION_REGISTERER_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_REGISTERER_PROVIDER_ID;
	private static final String EXTENSION_EDITABLE_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_EDITABLE_PROVIDER_ID;

	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_NAME_1 = "test.provider.1.name";
	private static final String PROVIDER_CUSTOM_GLOBAL = "test.provider.custom.global.id";
	private static final String PROVIDER_CUSTOM_GLOBAL_NAME = "test.provider.custom.global.name";

	private static final CMacroEntry SAMPLE_LSE = new CMacroEntry("MACRO", "value",0);

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

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsListenersTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		LanguageSettingsManager.unregisterLanguageSettingsChangeListener(mockLseListener);
		LanguageSettingsManager.setWorkspaceProviders(null);
		super.tearDown(); // includes ResourceHelper cleanup
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsListenersTests.class);
	}

	/**
	 * main function of the class.
	 *
	 * @param args - arguments
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	/**
	 * Check that global provider does not get unnecessarily registered on start.
	 */
	public void testListenerRegisterer_CheckExtensionProvider() throws Exception {
		// check if extension provider exists
		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(workspaceProvider);
		assertNotNull(rawProvider);
		// global listeners providers get registered only lazily
		assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
	}

	/**
	 * Test events triggered for non-shared configuration owned provider.
	 */
	public void testListenerRegisterer_OneOwnedByCfg() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);

		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider
			ILanguageSettingsProvider mockProvider = new MockListenerRegisterer(PROVIDER_1, PROVIDER_NAME_1);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	public void testListenerRegisterer_TwoOwnedByCfgs() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProject(this.getName(), null, new String[] {
			"org.eclipse.cdt.core.tests.configuration.id.1",
			"org.eclipse.cdt.core.tests.configuration.id.2",
		});
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);

		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
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
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(mockProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription1).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription1).getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			{
				// create a provider 2
				ILanguageSettingsProvider mockProvider = new MockListenerRegisterer(PROVIDER_1, PROVIDER_NAME_1);
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(mockProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription2).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription2).getLanguageSettingProviders();
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
	public void testListenerRegisterer_OneGlobal() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);

		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	public void testListenerRegisterer_TwoGlobal() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProject(this.getName(), null, new String[] {
			"org.eclipse.cdt.core.tests.configuration.id.1",
			"org.eclipse.cdt.core.tests.configuration.id.2",
		});
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);

		{
			// retrieve global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);
			assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);

			{
				// add global provider to configuration 1
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(workspaceProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription1).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription1).getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			{
				// add global provider to configuration 2
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(workspaceProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription2).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription2).getLanguageSettingProviders();
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
	public void testListenerRegisterer_TwoGlobalMinusOne() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProject(this.getName(), null, new String[] {
			"org.eclipse.cdt.core.tests.configuration.id.1",
			"org.eclipse.cdt.core.tests.configuration.id.2",
		});
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);

		{
			// retrieve workspace provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);
			assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);

			{
				// add global provider to configuration 1
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(workspaceProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription1).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription1).getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			{
				// add global provider to configuration 2
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(workspaceProvider);
				((ILanguageSettingsProvidersKeeper) cfgDescription2).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription2).getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// retrieve workspace provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);
			assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);

			{
				// remove global provider from configuration 1
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				((ILanguageSettingsProvidersKeeper) cfgDescription1).setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription1).getLanguageSettingProviders();
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
	public void testListenerRegisterer_GlobalProviderTwoProjects() throws Exception {
		// create project 1
		IProject project_1 = ResourceHelper.createCDTProjectWithConfig(this.getName() + ".1");
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project_1, true);
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project_1, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project_1, prjDescriptionWritable);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}

		// create project 2
		IProject project_2 = ResourceHelper.createCDTProjectWithConfig(this.getName() + ".2");
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project_2, true);
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project_2, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	public void testListenerRegisterer_GlobalProviderNotInUse() throws Exception {
		// create project
		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(PROVIDER_CUSTOM_GLOBAL);

		// the global custom provider has not been added yet
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(workspaceProvider);
		assertNull(rawProvider);
		assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));

		List<ILanguageSettingsProvider> workspaceProvidersOriginal = LanguageSettingsManager.getWorkspaceProviders();
		{
			// add global provider which is not engaged
			ILanguageSettingsProvider provider = new MockListenerRegisterer(PROVIDER_CUSTOM_GLOBAL, PROVIDER_CUSTOM_GLOBAL_NAME);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(workspaceProvidersOriginal);
			providers.add(provider);
			LanguageSettingsManager.setWorkspaceProviders(providers);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}
		{
			// remove global provider and restore original list
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(workspaceProvidersOriginal);
			LanguageSettingsManager.setWorkspaceProviders(providers);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}
	}

	/**
	 * Test events triggered for shared global provider replacing another one in global list.
	 */
	public void testListenerRegisterer_GlobalProviderAddRemoveOutsideTheProject() throws Exception {
		// create project
		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(PROVIDER_CUSTOM_GLOBAL);

		// the global custom provider has not been added yet
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(workspaceProvider);
		assertNull(rawProvider);
		assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));

		// prepare project
		List<ILanguageSettingsProvider> workspaceProvidersOriginal = LanguageSettingsManager.getWorkspaceProviders();
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// add global provider
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
			// the global custom provider has not been added yet
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}

		{
			// add global provider
			ILanguageSettingsProvider provider = new MockListenerRegisterer(PROVIDER_CUSTOM_GLOBAL, PROVIDER_CUSTOM_GLOBAL_NAME);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(provider);
			LanguageSettingsManager.setWorkspaceProviders(providers);
			assertEquals(1, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}
		{
			// remove global provider
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(workspaceProvidersOriginal);
			LanguageSettingsManager.setWorkspaceProviders(providers);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}

		{
			// close project
			project.close(null);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		}
	}

	/**
	 * Test events triggered when empty provider added and the resulting list of entries does not change.
	 */
	public void testNotification_cfgProvider_AddEmptyProvider() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider and add to cfgDescription
			ILanguageSettingsProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	public void testNotification_cfgProvider_AddNonEmptyProvider() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			String cfgDescriptionId = cfgDescription.getId();
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider and add entries
			MockLanguageSettingsEditableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1);
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			mockProvider.setSettingEntries(cfgDescription, project, null, entries);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	public void testNotification_cfgProvider_SerializeEntries() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);

		// add the mock provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider and add to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1));
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			String cfgDescriptionId = cfgDescription.getId();

			// Add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	public void testNotification_cfgProvider_SerializeEntriesConcurrent() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);

		// add the mock provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// create a provider and add to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1));
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
		ICProjectDescription prjDescription_1 = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		assertNotNull(prjDescription_1);
		ICProjectDescription prjDescription_2 = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		assertNotNull(prjDescription_2);
		{
			ICConfigurationDescription[] cfgDescriptions = prjDescription_1.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			String cfgDescriptionId = cfgDescription.getId();

			// Add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	public void testNotification_globalProvider_AddEmptyProvider() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			// clear it
			((MockLanguageSettingsEditableProvider) rawProvider).clear();
			assertEquals(null, wspProvider.getSettingEntries(cfgDescription, project, null));
			// add the provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(wspProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	public void testNotification_globalProvider_AddNonEmptyProvider() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			String cfgDescriptionId = cfgDescription.getId();

			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			((MockLanguageSettingsEditableProvider) rawProvider).clear();
			// add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			((MockLanguageSettingsEditableProvider) rawProvider).setSettingEntries(cfgDescription, project, null, entries);
			assertEquals(SAMPLE_LSE, wspProvider.getSettingEntries(cfgDescription, project, null).get(0));
			// add the provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(wspProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	public void testNotification_globalProvider_SerializeEntries() throws Exception {
		// create project
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(project, true);

		// register mock listener to inspect the notifications
		LanguageSettingsManager.registerLanguageSettingsChangeListener(mockLseListener);

		// Add empty global provider
		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			// clear it
			((MockLanguageSettingsEditableProvider) rawProvider).clear();
			assertEquals(null, wspProvider.getSettingEntries(cfgDescription, project, null));
			// add the provider to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(wspProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, prjDescriptionWritable);
		}

		// Change the provider's entries
		{
			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			assertNotNull(wspProvider);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(wspProvider);
			assertTrue(rawProvider instanceof MockLanguageSettingsEditableProvider);
			((MockLanguageSettingsEditableProvider) rawProvider).clear();
			// add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			((MockLanguageSettingsEditableProvider) rawProvider).setSettingEntries(null, project, null, entries);
			assertEquals(SAMPLE_LSE, wspProvider.getSettingEntries(null, project, null).get(0));

			// reset count
			mockLseListener.resetCount();
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());

			// Serialize settings
			LanguageSettingsManager.serializeLanguageSettingsWorkspace();

			// get cfgDescriptionId
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
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
		// Change the provider's entries back (bug was found for this case)
		{
			// retrieve a global provider
			ILanguageSettingsProvider wspProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
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
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
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
}

