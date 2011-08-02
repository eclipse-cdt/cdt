/*******************************************************************************
 * Copyright (c) 2009 Andrew Gvozdev and others.
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
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.testplugin.CModelMock;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsPersistenceProjectTests extends TestCase {
	// Should match id of extension point defined in plugin.xml
	private static final String EXTENSION_PROVIDER_ID = "org.eclipse.cdt.core.tests.language.settings.base.provider.subclass";
	private static final String EXTENSION_PROVIDER_NAME = "Test Plugin Base Provider Subclass";
	private static final String EXTENSION_SERIALIZABLE_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.serializable.language.settings.provider";

	private static final String CFG_ID = "test.configuration.id.0";
	private static final String CFG_ID_2 = "test.configuration.id.2";
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String PROVIDER_NAME_2 = "test.provider.2.name";
	private static final String PROVIDER_ID_WSP = "test.provider.workspace.id";
	private static final String PROVIDER_NAME_WSP = "test.provider.workspace.name";
	private static final String CUSTOM_PARAMETER = "custom parameter";
	private static final String ELEM_TEST = "test";

	private static CoreModel coreModel = CoreModel.getDefault();

	class MockConfigurationDescription extends CModelMock.DummyCConfigurationDescription {
		List<ILanguageSettingsProvider> providers;
		public MockConfigurationDescription(String id) {
			super(id);
		}

		@Override
		public void setLanguageSettingProviders(List<ILanguageSettingsProvider> providers) {
			this.providers = new ArrayList<ILanguageSettingsProvider>(providers);
		}

		@Override
		public List<ILanguageSettingsProvider> getLanguageSettingProviders() {
			return providers;
		}
	}
	class MockProjectDescription extends CModelMock.DummyCProjectDescription {
		ICConfigurationDescription[] cfgDescriptions;

		public MockProjectDescription(ICConfigurationDescription[] cfgDescriptions) {
			this.cfgDescriptions = cfgDescriptions;
		}

		public MockProjectDescription(ICConfigurationDescription cfgDescription) {
			this.cfgDescriptions = new ICConfigurationDescription[] { cfgDescription };
		}

		@Override
		public ICConfigurationDescription[] getConfigurations() {
			return cfgDescriptions;

		}

		@Override
		public ICConfigurationDescription getConfigurationById(String id) {
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription.getId().equals(id))
					return cfgDescription;
			}
			return null;
		}
	}

	private class MockProvider extends LanguageSettingsSerializable {
		public MockProvider(String id, String name) {
			super(id, name);
		}
	}


	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsPersistenceProjectTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		LanguageSettingsManager.setWorkspaceProviders(null);
		ResourceHelper.cleanUp();
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsPersistenceProjectTests.class);
	}

	/**
	 * main function of the class.
	 *
	 * @param args - arguments
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		assertNotNull(cfgDescriptions);
		return cfgDescriptions;
	}

	private ICConfigurationDescription getFirstConfigurationDescription(IProject project) {
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertNotNull(cfgDescription);

		return cfgDescription;
	}

	/**
	 */
	public void testWorkspacePersistence_ModifiedExtensionProvider() throws Exception {
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// get the raw extension provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			LanguageSettingsSerializable extProvider = (LanguageSettingsSerializable) LanguageSettingsManager.getRawProvider(provider);
			assertNotNull(extProvider);
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, extProvider.getId());
			
			// add entries
			extProvider.setSettingEntries(null, null, null, entries);
			List<ICLanguageSettingEntry> actual = extProvider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			// serialize language settings of workspace providers
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsWorkspace();
			
			// clear the provider
			extProvider.setSettingEntries(null, null, null, null);
		}

		{
			// doublecheck it's clean
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertNull(actual);
		}
		{
			// re-load and check language settings of the provider
			LanguageSettingsProvidersSerializer.loadLanguageSettingsWorkspace();

			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 */
	public void testWorkspacePersistence_GlobalProvider() throws Exception {
		{
			// get the raw extension provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			LanguageSettingsSerializable rawProvider = (LanguageSettingsSerializable) LanguageSettingsManager.getRawProvider(provider);
			assertNotNull(rawProvider);
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, rawProvider.getId());

			// customize provider
			rawProvider.setCustomParameter(CUSTOM_PARAMETER);
			assertEquals(CUSTOM_PARAMETER, rawProvider.getCustomParameter());
		}
		{
			// save workspace provider (as opposed to raw provider)
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			providers.add(provider);
			LanguageSettingsManager.setWorkspaceProviders(providers);
		}
		{
			// check that it has not cleared
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			LanguageSettingsSerializable rawProvider = (LanguageSettingsSerializable) LanguageSettingsManager.getRawProvider(provider);
			assertEquals(CUSTOM_PARAMETER, rawProvider.getCustomParameter());
		}
	}
	
	/**
	 */
	public void testWorkspacePersistence_ShadowedExtensionProvider() throws Exception {
		{
			// get the raw extension provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			// confirm its type and name
			assertTrue(rawProvider instanceof LanguageSettingsBaseProvider);
			assertEquals(EXTENSION_PROVIDER_ID, rawProvider.getId());
			assertEquals(EXTENSION_PROVIDER_NAME, rawProvider.getName());
		}
		{
			// replace extension provider
			ILanguageSettingsProvider provider = new MockLanguageSettingsSerializableProvider(EXTENSION_PROVIDER_ID, PROVIDER_NAME_0);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(provider);
			// note that this will also serialize workspace providers
			LanguageSettingsManager.setWorkspaceProviders(providers);
		}
		{
			// doublecheck it's in the list
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(rawProvider instanceof MockLanguageSettingsSerializableProvider);
			assertEquals(EXTENSION_PROVIDER_ID, rawProvider.getId());
			assertEquals(PROVIDER_NAME_0, rawProvider.getName());
		}
		
		{
			// re-load to check serialization
			LanguageSettingsProvidersSerializer.loadLanguageSettingsWorkspace();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(rawProvider instanceof MockLanguageSettingsSerializableProvider);
			assertEquals(EXTENSION_PROVIDER_ID, rawProvider.getId());
			assertEquals(PROVIDER_NAME_0, rawProvider.getName());
		}
		
		{
			// reset workspace providers, that will also serialize
			LanguageSettingsManager.setWorkspaceProviders(null);
		}
		{
			// doublecheck original one is in the list
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(rawProvider instanceof LanguageSettingsBaseProvider);
			assertEquals(EXTENSION_PROVIDER_ID, rawProvider.getId());
			assertEquals(EXTENSION_PROVIDER_NAME, rawProvider.getName());
		}
		{
			// re-load to check serialization
			LanguageSettingsProvidersSerializer.loadLanguageSettingsWorkspace();
			
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(rawProvider instanceof LanguageSettingsBaseProvider);
			assertEquals(EXTENSION_PROVIDER_ID, rawProvider.getId());
			assertEquals(EXTENSION_PROVIDER_NAME, rawProvider.getName());
		}
	}
	
	/**
	 */
	public void testProjectPersistence_SerializableProviderDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			LanguageSettingsSerializable serializableProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			serializableProvider.setSettingEntries(null, null, null, entries);

			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(serializableProvider);
			cfgDescription.setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof LanguageSettingsSerializable);

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 */
	public void testProjectPersistence_TwoConfigurationsDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> entries2 = new ArrayList<ICLanguageSettingEntry>();
		entries2.add(new CIncludePathEntry("path2", 0));

		{
			// create a project description with 2 configuration descriptions
			MockProjectDescription mockPrjDescription = new MockProjectDescription(
					new MockConfigurationDescription[] {
							new MockConfigurationDescription(CFG_ID),
							new MockConfigurationDescription(CFG_ID_2),
						});
			{
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				assertNotNull(cfgDescriptions);
				assertEquals(2, cfgDescriptions.length);
				{
					// populate configuration 1 with provider
					ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
					assertNotNull(cfgDescription1);
					assertEquals(CFG_ID, cfgDescription1.getId());
					LanguageSettingsSerializable provider1 = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
					provider1.setSettingEntries(null, null, null, entries);
					ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
					providers.add(provider1);
					cfgDescription1.setLanguageSettingProviders(providers);
				}
				{
					// populate configuration 2 with provider
					ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
					assertNotNull(cfgDescription2);
					assertEquals(CFG_ID_2, cfgDescription2.getId());
					LanguageSettingsSerializable provider2 = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
					provider2.setSettingEntries(null, null, null, entries2);
					ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
					providers.add(provider2);
					cfgDescription2.setLanguageSettingProviders(providers);
				}
			}

			{
				// doublecheck both configuration descriptions
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				assertNotNull(cfgDescriptions);
				assertEquals(2, cfgDescriptions.length);
				{
					// doublecheck configuration 1
					ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
					assertNotNull(cfgDescription1);
					List<ILanguageSettingsProvider> providers = cfgDescription1.getLanguageSettingProviders();
					assertNotNull(providers);
					assertEquals(1, providers.size());
					ILanguageSettingsProvider provider = providers.get(0);
					assertNotNull(provider);
					List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
					assertEquals(entries.get(0), actual.get(0));
					assertEquals(entries.size(), actual.size());
				}
				{
					// doublecheck configuration 2
					ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
					assertNotNull(cfgDescription2);
					List<ILanguageSettingsProvider> providers = cfgDescription2.getLanguageSettingProviders();
					assertNotNull(providers);
					assertEquals(1, providers.size());
					ILanguageSettingsProvider provider = providers.get(0);
					assertNotNull(provider);
					List<ICLanguageSettingEntry> actual2 = provider.getSettingEntries(null, null, null);
					assertEquals(entries2.get(0), actual2.get(0));
					assertEquals(entries2.size(), actual2.size());
				}
			}

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-create a project description and re-load language settings for each configuration
			MockProjectDescription mockPrjDescription = new MockProjectDescription(
					new MockConfigurationDescription[] {
							new MockConfigurationDescription(CFG_ID),
							new MockConfigurationDescription(CFG_ID_2),
						});
			// load
			LanguageSettingsProvidersSerializer.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(2, cfgDescriptions.length);
			{
				// check configuration 1
				ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
				assertNotNull(cfgDescription1);
				List<ILanguageSettingsProvider> providers = cfgDescription1.getLanguageSettingProviders();
				assertNotNull(providers);
				assertEquals(1, providers.size());
				ILanguageSettingsProvider provider = providers.get(0);
				assertNotNull(provider);
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
				assertEquals(entries.get(0), actual.get(0));
				assertEquals(entries.size(), actual.size());
			}
			{
				// check configuration 2
				ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
				assertNotNull(cfgDescription2);
				List<ILanguageSettingsProvider> providers = cfgDescription2.getLanguageSettingProviders();
				assertNotNull(providers);
				assertEquals(1, providers.size());
				ILanguageSettingsProvider provider = providers.get(0);
				assertNotNull(provider);
				List<ICLanguageSettingEntry> actual2 = provider.getSettingEntries(null, null, null);
				assertEquals(entries2.get(0), actual2.get(0));
				assertEquals(entries2.size(), actual2.size());
			}
		}
	}

	/**
	 */
	public void testProjectPersistence_SubclassedSerializableProviderDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			LanguageSettingsSerializable serializableProvider = new MockLanguageSettingsSerializableProvider(PROVIDER_0, PROVIDER_NAME_0);
			serializableProvider.setSettingEntries(null, null, null, entries);

			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(serializableProvider);
			cfgDescription.setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof MockLanguageSettingsSerializableProvider);

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 */
	public void testProjectPersistence_ReferenceExtensionProviderDOM() throws Exception {
		Element rootElement = null;

		// provider of other type (not LanguageSettingsSerializable) defined as an extension
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_PROVIDER_ID);

		{
			// create cfg description
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// populate with provider defined as extension
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerExt);
			cfgDescription.setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// and check the newly loaded provider which should be workspace provider
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertNotNull(provider);
			assertTrue(LanguageSettingsManager.isWorkspaceProvider(provider));
		}
	}

	/**
	 */
	public void testProjectPersistence_OverrideExtensionProviderDOM() throws Exception {
		Element rootElement = null;

		// provider set on workspace level overriding an extension
		String idExt = EXTENSION_PROVIDER_ID;
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(idExt);
		assertNotNull(providerExt);
		{
			// create cfg description
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// populate with provider overriding the extension (must be SerializableLanguageSettingsProvider or a class from another extension)
			ILanguageSettingsProvider providerOverride = new MockLanguageSettingsSerializableProvider(idExt, PROVIDER_NAME_0);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerOverride);
			cfgDescription.setLanguageSettingProviders(providers);


			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettings(rootElement, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			// check the newly loaded provider
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertNotNull(provider);
			assertTrue(provider instanceof MockLanguageSettingsSerializableProvider);
			assertEquals(idExt, provider.getId());
			assertEquals(PROVIDER_NAME_0, provider.getName());
		}
	}


	/**
	 */
	public void testProjectPersistence_MixedProvidersDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> entries_31 = new ArrayList<ICLanguageSettingEntry>();
		entries_31.add(new CIncludePathEntry("path0", 0));

		List<ICLanguageSettingEntry> entries_32 = new ArrayList<ICLanguageSettingEntry>();
		entries_32.add(new CIncludePathEntry("path2", 0));

		ILanguageSettingsProvider providerExt;
		ILanguageSettingsProvider providerWsp;
		{
			// Define providers a bunch
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			{
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				ICConfigurationDescription cfgDescription = cfgDescriptions[0];
				assertNotNull(cfgDescription);

				// 1. Provider reference to extension from plugin.xml
				providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_PROVIDER_ID);

				// 2. TODO Provider reference to provider defined in the project

				// 3. Providers defined in a configuration
				// 3.1
				LanguageSettingsSerializable mockProvider1 = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
				mockProvider1.setSettingEntries(null, null, null, entries_31);
				// 3.2
				LanguageSettingsSerializable mockProvider2 = new MockLanguageSettingsSerializableProvider(PROVIDER_2, PROVIDER_NAME_2);
				mockProvider2.setSettingEntries(null, null, null, entries_32);

				ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(providerExt);
				providers.add(mockProvider1);
				providers.add(mockProvider2);
				cfgDescription.setLanguageSettingProviders(providers);
			}

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettings(rootElement, mockPrjDescription);
			XmlUtil.toString(doc);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettings(rootElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];

			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertNotNull(providers);
			// 1. Provider reference to extension from plugin.xml
			ILanguageSettingsProvider provider0 = providers.get(0);
			assertTrue(LanguageSettingsManager.isWorkspaceProvider(provider0));

			// 2. TODO Provider reference to provider defined in the project

			// 3. Providers defined in a configuration
			// 3.1
			{
				ILanguageSettingsProvider provider1 = providers.get(1);
				assertTrue(provider1 instanceof LanguageSettingsSerializable);
				List<ICLanguageSettingEntry> actual = provider1.getSettingEntries(null, null, null);
				assertEquals(entries_31.get(0), actual.get(0));
				assertEquals(entries_31.size(), actual.size());
			}
			// 3.2
			{
				ILanguageSettingsProvider provider2 = providers.get(2);
				assertTrue(provider2 instanceof MockLanguageSettingsSerializableProvider);
				List<ICLanguageSettingEntry> actual = provider2.getSettingEntries(null, null, null);
				assertEquals(entries_32.get(0), actual.get(0));
				assertEquals(entries_32.size(), actual.size());
			}
			assertEquals(3, providers.size());
		}
	}

	/**
	 */
	public void testProjectPersistence_RealProject() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		String xmlStorageFileLocation;
		String xmlOutOfTheWay;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// get project descriptions
			ICProjectDescription writableProjDescription = coreModel.getProjectDescription(project);
			assertNotNull(writableProjDescription);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];

			// create a provider
			LanguageSettingsSerializable mockProvider = new LanguageSettingsSerializable(PROVIDER_0, PROVIDER_NAME_0);
			mockProvider.setSettingEntries(cfgDescription, null, null, entries);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			coreModel.setProjectDescription(project, writableProjDescription);
			IFile xmlStorageFile = project.getFile(".settings/language.settings.xml");
			assertTrue(xmlStorageFile.exists());
			xmlStorageFileLocation = xmlStorageFile.getLocation().toOSString();
		}
		{
			coreModel.getProjectDescription(project);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof LanguageSettingsSerializable);
			assertEquals(PROVIDER_0, provider.getId());
			assertEquals(PROVIDER_NAME_0, provider.getName());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(cfgDescription, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
		{
			// Move storage out of the way
			java.io.File xmlFile = new java.io.File(xmlStorageFileLocation);
			xmlOutOfTheWay = xmlStorageFileLocation+".out-of-the-way";
			java.io.File xmlFileOut = new java.io.File(xmlOutOfTheWay);
			xmlFile.renameTo(xmlFileOut);
			assertFalse(xmlFile.exists());
			assertTrue(xmlFileOut.exists());
		}

		{
			// clear configuration
			ICProjectDescription writableProjDescription = coreModel.getProjectDescription(project);
			ICConfigurationDescription[] cfgDescriptions = writableProjDescription.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);

			cfgDescription.setLanguageSettingProviders(new ArrayList<ILanguageSettingsProvider>());
			coreModel.setProjectDescription(project, writableProjDescription);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// re-check if it really took it
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// close the project
			project.close(null);
		}
		{
			// open to double-check the data is not kept in some other kind of cache
			project.open(null);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(0, providers.size());
			// and close
			project.close(null);
		}

		{
			// Move storage back
			java.io.File xmlFile = new java.io.File(xmlStorageFileLocation);
			xmlFile.delete();
			assertFalse("File "+xmlFile+ " still exist", xmlFile.exists());
			java.io.File xmlFileOut = new java.io.File(xmlOutOfTheWay);
			xmlFileOut.renameTo(xmlFile);
			assertTrue("File "+xmlFile+ " does not exist", xmlFile.exists());
			assertFalse("File "+xmlFileOut+ " still exist", xmlFileOut.exists());
		}

		{
			// Remove project from internal cache
			CProjectDescriptionManager.getInstance().projectClosedRemove(project);
			// open project and check if providers are loaded
			project.open(null);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider loadedProvider = providers.get(0);
			assertTrue(loadedProvider instanceof LanguageSettingsSerializable);
			assertEquals(PROVIDER_0, loadedProvider.getId());
			assertEquals(PROVIDER_NAME_0, loadedProvider.getName());

			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(cfgDescription, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}


}
