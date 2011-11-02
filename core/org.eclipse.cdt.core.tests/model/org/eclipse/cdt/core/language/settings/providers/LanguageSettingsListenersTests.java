/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.language.settings.providers.ILanguageSettingsChangeEvent;
import org.eclipse.cdt.internal.core.language.settings.providers.ILanguageSettingsChangeListener;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.core.resources.IProject;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsListenersTests extends TestCase {
	// Must match provider id defined as extension point 
	private static final String EXTENSION_REGISTERER_PROVIDER_ID = "org.eclipse.cdt.core.tests.language.settings.listener.registerer.provider";
	private static final String EXTENSION_EDITABLE_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.editable.language.settings.provider";
	
	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_NAME_1 = "test.provider.1.name";
	private static final String PROVIDER_CUSTOM_GLOBAL = "test.provider.custom.global.id";
	private static final String PROVIDER_CUSTOM_GLOBAL_NAME = "test.provider.custom.global.name";
	
	private static final CMacroEntry SAMPLE_LSE = new CMacroEntry("MACRO", "value",0);
	private class MockLanguageSettingsChangeListener implements ILanguageSettingsChangeListener {
		private int count = 0;
		private ILanguageSettingsChangeEvent lastEvent = null;
		
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
	}

	@Override
	protected void tearDown() throws Exception {
		LanguageSettingsProvidersSerializer.unregisterLanguageSettingsChangeListener(mockLseListener);
		LanguageSettingsManager.setWorkspaceProviders(null);
		ResourceHelper.cleanUp();
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
	 */
	public void testListenerRegisterer_OneOwnedByCfg() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());

		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];

			// create a provider
			ILanguageSettingsProvider mockProvider = new MockListenerRegisterer(PROVIDER_1, PROVIDER_NAME_1);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
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
			CoreModel.getDefault().getProjectDescription(project);
			assertEquals(1, MockListenerRegisterer.getCount(PROVIDER_1));
			// and delete
			project.delete(true, null);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_1));
		}

	}

	/**
	 */
	public void testListenerRegisterer_TwoOwnedByCfgs() throws Exception {
		IProject project = ResourceHelper.createCDTProject(this.getName(), null, new String[] {
			"org.eclipse.cdt.core.tests.configuration.id.1",
			"org.eclipse.cdt.core.tests.configuration.id.2",
		});
		
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			
			{
				// create a provider 1
				ILanguageSettingsProvider mockProvider = new MockListenerRegisterer(PROVIDER_1, PROVIDER_NAME_1);
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(mockProvider);
				cfgDescription1.setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = cfgDescription1.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			{
				// create a provider 2
				ILanguageSettingsProvider mockProvider = new MockListenerRegisterer(PROVIDER_1, PROVIDER_NAME_1);
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(mockProvider);
				cfgDescription2.setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = cfgDescription2.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
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
			CoreModel.getDefault().getProjectDescription(project);
			assertEquals(2, MockListenerRegisterer.getCount(PROVIDER_1));
			// and delete
			project.delete(true, null);
			assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_1));
		}
		
	}
	
	/**
	 */
	public void testListenerRegisterer_OneGlobal() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			// add global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
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
			CoreModel.getDefault().getProjectDescription(project);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
			// and delete
			project.delete(true, null);
			assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		
	}
	
	/**
	 */
	public void testListenerRegisterer_TwoGlobal() throws Exception {
		IProject project = ResourceHelper.createCDTProject(this.getName(), null, new String[] {
			"org.eclipse.cdt.core.tests.configuration.id.1",
			"org.eclipse.cdt.core.tests.configuration.id.2",
		});
		
		{
			// retrieve global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			
			{
				// add global provider to configuration 1
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(workspaceProvider);
				cfgDescription1.setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = cfgDescription1.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			{
				// add global provider to configuration 2
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(workspaceProvider);
				cfgDescription2.setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = cfgDescription2.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
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
			CoreModel.getDefault().getProjectDescription(project);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
			// and delete
			project.delete(true, null);
			assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
	}
	
	/**
	 */
	public void testListenerRegisterer_TwoGlobalMinusOne() throws Exception {
		IProject project = ResourceHelper.createCDTProject(this.getName(), null, new String[] {
			"org.eclipse.cdt.core.tests.configuration.id.1",
			"org.eclipse.cdt.core.tests.configuration.id.2",
		});
		
		{
			// retrieve workspace provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			
			{
				// add global provider to configuration 1
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(workspaceProvider);
				cfgDescription1.setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = cfgDescription1.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			{
				// add global provider to configuration 2
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(workspaceProvider);
				cfgDescription2.setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = cfgDescription2.getLanguageSettingProviders();
				assertEquals(1, storedProviders.size());
			}
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// retrieve workspace provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(2, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
			ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
			
			{
				// remove global provider from configuration 1
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				cfgDescription1.setLanguageSettingProviders(providers);
				List<ILanguageSettingsProvider> storedProviders = cfgDescription1.getLanguageSettingProviders();
				assertEquals(0, storedProviders.size());
			}
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
		{
			// close the project
			project.close(null);
			assertEquals(0, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}
	}
	
	/**
	 */
	public void testListenerRegisterer_GlobalProviderTwoProjects() throws Exception {
		// create project 1
		IProject project_1 = ResourceHelper.createCDTProjectWithConfig(this.getName() + ".1");
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project_1);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			// add global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project_1, writableProjDescription);
			assertEquals(1, MockListenerRegisterer.getCount(EXTENSION_REGISTERER_PROVIDER_ID));
		}

		// create project 2
		IProject project_2 = ResourceHelper.createCDTProjectWithConfig(this.getName() + ".2");
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project_2);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			// add global provider
			ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_REGISTERER_PROVIDER_ID);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project_2, writableProjDescription);
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
	 */
	public void testListenerRegisterer_GlobalProviderNotInUse() throws Exception {
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
	 */
	public void testListenerRegisterer_GlobalProviderAddRemoveOutsideTheProject() throws Exception {
		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(PROVIDER_CUSTOM_GLOBAL);
		
		// the global custom provider has not been added yet
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(workspaceProvider);
		assertNull(rawProvider);
		assertEquals(0, MockListenerRegisterer.getCount(PROVIDER_CUSTOM_GLOBAL));
		
		// prepare project
		List<ILanguageSettingsProvider> workspaceProvidersOriginal = LanguageSettingsManager.getWorkspaceProviders();
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			// add global provider
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
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
	 */
	public void testNotification_cfgProvider_AddEmptyProvider() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
		}
		
		// register mock listener to inspect the notifications
		LanguageSettingsProvidersSerializer.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());
		
		// Add empty provider
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			// create a provider and add to cfgDescription
			ILanguageSettingsProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
		}
		
		// No notifications expected
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());
	}
	
	/**
	 */
	public void testNotification_cfgProvider_AddNonEmptyProvider() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
		}
		
		// register mock listener to inspect the notifications
		LanguageSettingsProvidersSerializer.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());
		
		// Add non-empty provider
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			String cfgDescriptionId = cfgDescription.getId();
			
			// create a provider and add entries
			MockLanguageSettingsEditableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1);
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			mockProvider.setSettingEntries(cfgDescription, project, null, entries);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);

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
	 */
	public void testNotification_cfgProvider_SerializeEntries() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		
		// add the mock provider
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			// create a provider and add to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1));
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
		}
		
		// register mock listener to inspect the notifications
		LanguageSettingsProvidersSerializer.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());

		// Change the provider's entries
		{
			// get project descriptions
			ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription[] cfgDescriptions = prjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			String cfgDescriptionId = cfgDescription.getId();
			
			// Add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			assertTrue(providers.get(0) instanceof MockLanguageSettingsEditableProvider);
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);
			mockProvider.setSettingEntries(cfgDescription, project, null, entries);
			assertEquals(0, mockLseListener.getCount());
			assertEquals(null, mockLseListener.getLastEvent());
			
			// Serialize settings
			LanguageSettingsProvidersSerializer.serializeLanguageSettings(prjDescription);
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
	 */
	public void testNotification_cfgProvider_SerializeEntriesConcurrent() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		
		// add the mock provider
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			// create a provider and add to cfgDescription
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1));
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
		}
		
		// register mock listener to inspect the notifications
		LanguageSettingsProvidersSerializer.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());
		
		// Change the provider's entries concurrently
		
		// get project descriptions
		ICProjectDescription prjDescription_1 = CoreModel.getDefault().getProjectDescription(project, false);
		assertNotNull(prjDescription_1);
		ICProjectDescription prjDescription_2 = CoreModel.getDefault().getProjectDescription(project, false);
		assertNotNull(prjDescription_2);
		{
			ICConfigurationDescription[] cfgDescriptions = prjDescription_1.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
			String cfgDescriptionId = cfgDescriptionWritable.getId();
			
			// Add entries
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			List<ILanguageSettingsProvider> providers = cfgDescriptionWritable.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			assertTrue(providers.get(0) instanceof MockLanguageSettingsEditableProvider);
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);
			mockProvider.setSettingEntries(cfgDescriptionWritable, project, null, entries);
			
			// reset count
			mockLseListener.resetCount();
			assertEquals(0, mockLseListener.getCount());
			assertNull(mockLseListener.getLastEvent());
			
			// Serialize settings
			LanguageSettingsProvidersSerializer.serializeLanguageSettings(prjDescription_1);
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
			ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
			String cfgDescriptionId = cfgDescriptionWritable.getId();
			
			// Add same entries
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(SAMPLE_LSE);
			List<ILanguageSettingsProvider> providers = cfgDescriptionWritable.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			assertTrue(providers.get(0) instanceof MockLanguageSettingsEditableProvider);
			MockLanguageSettingsEditableProvider mockProvider = (MockLanguageSettingsEditableProvider) providers.get(0);
			mockProvider.setSettingEntries(cfgDescriptionWritable, project, null, entries);
			
			// reset count
			mockLseListener.resetCount();
			assertEquals(0, mockLseListener.getCount());
			assertNull(mockLseListener.getLastEvent());
			
			// Serialize settings
			LanguageSettingsProvidersSerializer.serializeLanguageSettings(prjDescription_2);
			// inspect event
			assertEquals(0, mockLseListener.getCount());
			ILanguageSettingsChangeEvent event = mockLseListener.getLastEvent();
			assertNull(event);
		}
	}
	
	/**
	 */
	public void testNotification_globalProvider_AddEmptyProvider() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];

			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
		}
		
		// register mock listener to inspect the notifications
		LanguageSettingsProvidersSerializer.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());
		
		// Add empty global provider
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
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
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
		}
		
		// No notifications expected
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());
	}
	
	/**
	 */
	public void testNotification_globalProvider_AddNonEmptyProvider() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		// First clear default providers
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
			// clear providers
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
		}
		
		// register mock listener to inspect the notifications
		LanguageSettingsProvidersSerializer.registerLanguageSettingsChangeListener(mockLseListener);
		assertEquals(0, mockLseListener.getCount());
		assertEquals(null, mockLseListener.getLastEvent());
		
		// Add non-empty provider
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
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
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);

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
	 */
	public void testNotification_globalProvider_SerializeEntries() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		
		// register mock listener to inspect the notifications
		LanguageSettingsProvidersSerializer.registerLanguageSettingsChangeListener(mockLseListener);
		
		// Add empty global provider
		{
			// get project descriptions
			ICProjectDescription writableProjDescription = CoreModel.getDefault().getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			
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
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());
			
			// write to project description
			CoreModel.getDefault().setProjectDescription(project, writableProjDescription);
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
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsWorkspace();

			// get cfgDescriptionId
			ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, false);
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
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsWorkspace();
			
			// get cfgDescriptionId
			ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project, false);
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

