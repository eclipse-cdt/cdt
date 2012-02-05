/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev and others.
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
import java.util.Arrays;
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.testplugin.CModelMock;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases testing LanguageSettingsProvider functionality related to persistence.
 */
public class LanguageSettingsPersistenceProjectTests extends BaseTestCase {
	// These should match extension points defined in plugin.xml
	private static final String EXTENSION_BASE_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_BASE_PROVIDER_ID;
	private static final String EXTENSION_BASE_PROVIDER_NAME = LanguageSettingsExtensionsTests.EXTENSION_BASE_PROVIDER_NAME;
	private static final String EXTENSION_SERIALIZABLE_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_SERIALIZABLE_PROVIDER_ID;
	private static final String EXTENSION_EDITABLE_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_EDITABLE_PROVIDER_ID;
	private static final ICLanguageSettingEntry EXTENSION_SERIALIZABLE_PROVIDER_ENTRY = LanguageSettingsExtensionsTests.EXTENSION_SERIALIZABLE_PROVIDER_ENTRY;

	// Constants from LanguageSettingsProvidersSerializer
	public static final String LANGUAGE_SETTINGS_PROJECT_XML = ".settings/language.settings.xml";
	public static final String LANGUAGE_SETTINGS_WORKSPACE_XML = "language.settings.xml";

	// Arbitrary sample parameters used by the test
	private static final String CFG_ID = "test.configuration.id.0";
	private static final String CFG_ID_2 = "test.configuration.id.2";
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String PROVIDER_NAME_1 = "test.provider.1.name";
	private static final String PROVIDER_NAME_2 = "test.provider.2.name";
	private static final String ATTR_PARAMETER = "parameter";
	private static final String CUSTOM_PARAMETER = "custom parameter";
	private static final String ELEM_TEST = "test";
	private static final String ELEM_PROVIDER = "provider "; // keep space for more reliable comparison
	private static final String ELEM_PROVIDER_REFERENCE = "provider-reference";

	/**
	 * Mock configuration description.
	 */
	class MockConfigurationDescription extends CModelMock.DummyCConfigurationDescription implements ILanguageSettingsProvidersKeeper {
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
		@Override
		public void setDefaultLanguageSettingsProvidersIds(String[] ids) {
		}
		@Override
		public String[] getDefaultLanguageSettingsProvidersIds() {
			return null;
		}
	}

	/**
	 * Mock project description.
	 */
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

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsPersistenceProjectTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		LanguageSettingsManager.setWorkspaceProviders(null);
		super.tearDown(); // includes ResourceHelper cleanup
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

	/**
	 * Get read-only configuration descriptions.
	 */
	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		// project description
		ICProjectDescription projectDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		assertNotNull(cfgDescriptions);
		return cfgDescriptions;
	}

	/**
	 * Get first read-only configuration description.
	 */
	private ICConfigurationDescription getFirstConfigurationDescription(IProject project) {
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);

		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertNotNull(cfgDescription);

		return cfgDescription;
	}

	/**
	 * Persist and reload when no customized providers are defined in the workspace.
	 */
	public void testWorkspacePersistence_NoProviders() throws Exception {
		// serialize language settings of user defined providers (on workspace level)
		LanguageSettingsProvidersSerializer.serializeLanguageSettingsWorkspace();
		LanguageSettingsProvidersSerializer.loadLanguageSettingsWorkspace();

		// test passes if no exception was thrown
	}

	/**
	 * Make sure providers in configuration cannot be modified accidentally outside of API.
	 */
	public void testProjectDescription_PreventBackDoorAccess() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());

		// get project descriptions
		ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
		assertNotNull(prjDescriptionWritable);
		ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
		assertEquals(1, cfgDescriptions.length);
		ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
		assertNotNull(cfgDescriptionWritable);
		assertTrue(cfgDescriptionWritable instanceof ILanguageSettingsProvidersKeeper);

		List<ILanguageSettingsProvider> originalProviders = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).getLanguageSettingProviders();
		int originalSize = originalProviders.size();

		// create new provider list
		LanguageSettingsSerializableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_0, PROVIDER_NAME_0);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(originalProviders);
		providers.add(mockProvider);
		assertTrue(originalSize != providers.size());

		// changing providers shouldn't affect the original list
		((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(providers);
		assertEquals(originalSize, originalProviders.size());
	}

	/**
	 * Test assigning providers to read-only vs. writable configuration descriptions.
	 */
	public void testProjectDescription_ReadWriteDescription() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());

		{
			// get read-only description
			ICProjectDescription prjDescriptionReadOnly = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertNotNull(prjDescriptionReadOnly);
			ICConfigurationDescription cfgDescriptionReadOnly = prjDescriptionReadOnly.getDefaultSettingConfiguration();
			assertNotNull(cfgDescriptionReadOnly);
			assertTrue(cfgDescriptionReadOnly instanceof ILanguageSettingsProvidersKeeper);

			// try to write to it providers
			try {
				List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				((ILanguageSettingsProvidersKeeper) cfgDescriptionReadOnly).setLanguageSettingProviders(providers);
				fail("WriteAccessException was expected but it was not throw.");
			} catch (WriteAccessException e) {
				// exception is expected
			}

			// try to write to it default providers ids
			try {
				((ILanguageSettingsProvidersKeeper) cfgDescriptionReadOnly).setDefaultLanguageSettingsProvidersIds(new String[] { PROVIDER_0 });
				fail("WriteAccessException was expected but it was not throw.");
			} catch (WriteAccessException e) {
				// exception is expected
			}
		}

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
			assertNotNull(cfgDescriptionWritable);
			assertTrue(cfgDescriptionWritable instanceof ILanguageSettingsProvidersKeeper);

			// create a provider and write to cfgDescription
			LanguageSettingsSerializableProvider mockProvider = new MockLanguageSettingsEditableProvider(PROVIDER_1, PROVIDER_NAME_1);
			LanguageSettingsManager.setStoringEntriesInProjectArea(mockProvider, true);
			mockProvider.setSettingEntries(cfgDescriptionWritable, null, null, entries);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to cfgDescription default providers ids
			((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setDefaultLanguageSettingsProvidersIds(new String[] { PROVIDER_0 });

			// apply new project description to the project model
			CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescriptionWritable);
		}
		{
			// get read-only project descriptions
			ICProjectDescription prjDescriptionReadOnly = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertNotNull(prjDescriptionReadOnly);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionReadOnly.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescriptionReadOnly = cfgDescriptions[0];
			assertNotNull(cfgDescriptionReadOnly);
			assertTrue(cfgDescriptionReadOnly instanceof ILanguageSettingsProvidersKeeper);

			// double-check providers
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescriptionReadOnly).getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider loadedProvider = providers.get(0);
			assertTrue(loadedProvider instanceof MockLanguageSettingsEditableProvider);
			assertEquals(PROVIDER_1, loadedProvider.getId());
			assertEquals(PROVIDER_NAME_1, loadedProvider.getName());
			// double-check provider's setting entries
			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(cfgDescriptionReadOnly, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			// double-check default providers ids
			String[] actualDefaultProvidersIds = ((ILanguageSettingsProvidersKeeper) cfgDescriptionReadOnly).getDefaultLanguageSettingsProvidersIds();
			assertTrue(Arrays.equals(new String[] { PROVIDER_0 }, actualDefaultProvidersIds));
		}
		{
			// get writable project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
			assertNotNull(cfgDescriptionWritable);
			assertTrue(cfgDescriptionWritable instanceof ILanguageSettingsProvidersKeeper);

			// check providers
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider loadedProvider = providers.get(0);
			assertTrue(loadedProvider instanceof MockLanguageSettingsEditableProvider);
			assertEquals(PROVIDER_1, loadedProvider.getId());
			assertEquals(PROVIDER_NAME_1, loadedProvider.getName());
			// check provider's setting entries
			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(cfgDescriptionWritable, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			// check default providers ids
			String[] actualDefaultProvidersIds = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).getDefaultLanguageSettingsProvidersIds();
			assertTrue(Arrays.equals(new String[] { PROVIDER_0 }, actualDefaultProvidersIds));
		}
	}

	/**
	 * Persist and reload a customized provider defined in the workspace.
	 */
	public void testWorkspacePersistence_ModifiedExtensionProvider() throws Exception {
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// get the raw extension provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			LanguageSettingsSerializableProvider extProvider = (LanguageSettingsSerializableProvider) LanguageSettingsManager.getRawProvider(provider);
			assertNotNull(extProvider);
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, extProvider.getId());

			// add entries
			extProvider.setSettingEntries(null, null, null, entries);
			List<ICLanguageSettingEntry> actual = extProvider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			// serialize language settings of workspace providers
			LanguageSettingsManager.serializeLanguageSettingsWorkspace();

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
	 * Check persistence of unmodified extension provider in the workspace.
	 */
	public void testWorkspacePersistence_UnmodifiedExtensionProvider() throws Exception {
		List<ICLanguageSettingEntry> extensionEntries = new ArrayList<ICLanguageSettingEntry>();
		extensionEntries.add(EXTENSION_SERIALIZABLE_PROVIDER_ENTRY);
		{
			// test initial state of the extension provider
			ILanguageSettingsProvider extProvider = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_SERIALIZABLE_PROVIDER_ID, true);
			assertNull(extProvider);
		}
		{
			// get the workspace provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			// check that entries match that of extension provider
			assertEquals(extensionEntries, provider.getSettingEntries(null, null, null));
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(LanguageSettingsManager.isEqualExtensionProvider(rawProvider, true));

			// serialize language settings of workspace providers
			LanguageSettingsManager.serializeLanguageSettingsWorkspace();
		}
		{
			// re-load
			LanguageSettingsProvidersSerializer.loadLanguageSettingsWorkspace();

			// ensure the workspace provider still matches extension
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, provider.getId());
			assertEquals(extensionEntries, provider.getSettingEntries(null, null, null));
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(LanguageSettingsManager.isEqualExtensionProvider(rawProvider, true));

			// replace entries
			assertTrue(rawProvider instanceof LanguageSettingsSerializableProvider);
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(new CIncludePathEntry("path0", 0));
			((LanguageSettingsSerializableProvider)rawProvider).setSettingEntries(null, null, null, entries);

			// check that the extension provider is not affected
			assertTrue(!LanguageSettingsManager.isEqualExtensionProvider(rawProvider, true));
		}
	}

	/**
	 * Test persistence of global providers in the workspace.
	 */
	public void testWorkspacePersistence_GlobalProvider() throws Exception {
		{
			// get the raw extension provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			LanguageSettingsSerializableProvider rawProvider = (LanguageSettingsSerializableProvider) LanguageSettingsManager.getRawProvider(provider);
			assertNotNull(rawProvider);
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, rawProvider.getId());

			// customize provider
			rawProvider.setProperty(ATTR_PARAMETER, CUSTOM_PARAMETER);
			assertEquals(CUSTOM_PARAMETER, rawProvider.getProperty(ATTR_PARAMETER));
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
			LanguageSettingsSerializableProvider rawProvider = (LanguageSettingsSerializableProvider) LanguageSettingsManager.getRawProvider(provider);
			assertEquals(CUSTOM_PARAMETER, rawProvider.getProperty(ATTR_PARAMETER));
		}
	}

	/**
	 * Test persistence of global providers with ID matching an extension provider in the workspace.
	 */
	public void testWorkspacePersistence_ShadowedExtensionProvider() throws Exception {
		{
			// get the raw extension provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			// confirm its type and name
			assertTrue(rawProvider instanceof LanguageSettingsBaseProvider);
			assertEquals(EXTENSION_BASE_PROVIDER_ID, rawProvider.getId());
			assertEquals(EXTENSION_BASE_PROVIDER_NAME, rawProvider.getName());
		}
		{
			// replace extension provider
			ILanguageSettingsProvider provider = new MockLanguageSettingsSerializableProvider(EXTENSION_BASE_PROVIDER_ID, PROVIDER_NAME_0);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(provider);
			// note that this will also serialize workspace providers
			LanguageSettingsManager.setWorkspaceProviders(providers);
		}
		{
			// doublecheck it's in the list
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(rawProvider instanceof MockLanguageSettingsSerializableProvider);
			assertEquals(EXTENSION_BASE_PROVIDER_ID, rawProvider.getId());
			assertEquals(PROVIDER_NAME_0, rawProvider.getName());
		}

		{
			// re-load to check serialization
			LanguageSettingsProvidersSerializer.loadLanguageSettingsWorkspace();

			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(rawProvider instanceof MockLanguageSettingsSerializableProvider);
			assertEquals(EXTENSION_BASE_PROVIDER_ID, rawProvider.getId());
			assertEquals(PROVIDER_NAME_0, rawProvider.getName());
		}

		{
			// reset workspace providers, that will also serialize
			LanguageSettingsManager.setWorkspaceProviders(null);
		}
		{
			// doublecheck original one is in the list
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(rawProvider instanceof LanguageSettingsBaseProvider);
			assertEquals(EXTENSION_BASE_PROVIDER_ID, rawProvider.getId());
			assertEquals(EXTENSION_BASE_PROVIDER_NAME, rawProvider.getName());
		}
		{
			// re-load to check serialization
			LanguageSettingsProvidersSerializer.loadLanguageSettingsWorkspace();

			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			assertTrue(rawProvider instanceof LanguageSettingsBaseProvider);
			assertEquals(EXTENSION_BASE_PROVIDER_ID, rawProvider.getId());
			assertEquals(EXTENSION_BASE_PROVIDER_NAME, rawProvider.getName());
		}
	}

	/**
	 * Test serialization of providers to project storage.
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
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			LanguageSettingsSerializableProvider serializableProvider = new LanguageSettingsSerializableProvider(PROVIDER_0, PROVIDER_NAME_0);
			serializableProvider.setSettingEntries(null, null, null, entries);
			LanguageSettingsManager.setStoringEntriesInProjectArea(serializableProvider, true);

			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(serializableProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(rootElement, null, mockPrjDescription);
			assertTrue(XmlUtil.toString(doc).contains(PROVIDER_0));
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(rootElement, null, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof LanguageSettingsSerializableProvider);

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 * Test User language settings provider defined as extension in cdt.ui.
	 */
	public void testProjectPersistence_UserProviderDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			ILanguageSettingsProvider provider = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID, false);
			assertTrue(provider instanceof MockLanguageSettingsEditableProvider);
			MockLanguageSettingsEditableProvider serializableProvider = (MockLanguageSettingsEditableProvider) provider;
			serializableProvider.setSettingEntries(null, null, null, entries);
			LanguageSettingsManager.setStoringEntriesInProjectArea(serializableProvider, true);

			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(serializableProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(rootElement, null, mockPrjDescription);
			assertTrue(XmlUtil.toString(doc).contains(EXTENSION_EDITABLE_PROVIDER_ID));
			assertTrue(XmlUtil.toString(doc).contains(MockLanguageSettingsEditableProvider.class.getName()));
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(rootElement, null, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof MockLanguageSettingsEditableProvider);

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 * Test serialization of providers to project storage where the project has multiple configurations.
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
					assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);

					assertEquals(CFG_ID, cfgDescription1.getId());
					LanguageSettingsSerializableProvider provider1 = new LanguageSettingsSerializableProvider(PROVIDER_0, PROVIDER_NAME_0);
					LanguageSettingsManager.setStoringEntriesInProjectArea(provider1, true);
					provider1.setSettingEntries(null, null, null, entries);
					ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
					providers.add(provider1);
					((ILanguageSettingsProvidersKeeper) cfgDescription1).setLanguageSettingProviders(providers);
				}
				{
					// populate configuration 2 with provider
					ICConfigurationDescription cfgDescription2 = cfgDescriptions[1];
					assertNotNull(cfgDescription2);
					assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);

					assertEquals(CFG_ID_2, cfgDescription2.getId());
					LanguageSettingsSerializableProvider provider2 = new LanguageSettingsSerializableProvider(PROVIDER_0, PROVIDER_NAME_0);
					LanguageSettingsManager.setStoringEntriesInProjectArea(provider2, true);
					provider2.setSettingEntries(null, null, null, entries2);
					ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
					providers.add(provider2);
					((ILanguageSettingsProvidersKeeper) cfgDescription2).setLanguageSettingProviders(providers);
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
					assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);
					List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription1).getLanguageSettingProviders();

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
					assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);

					List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription2).getLanguageSettingProviders();
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
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(rootElement, null, mockPrjDescription);
		}
		{
			// re-create a project description and re-load language settings for each configuration
			MockProjectDescription mockPrjDescription = new MockProjectDescription(
					new MockConfigurationDescription[] {
							new MockConfigurationDescription(CFG_ID),
							new MockConfigurationDescription(CFG_ID_2),
						});
			// load
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(rootElement, null, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(2, cfgDescriptions.length);
			{
				// check configuration 1
				ICConfigurationDescription cfgDescription1 = cfgDescriptions[0];
				assertNotNull(cfgDescription1);
				assertTrue(cfgDescription1 instanceof ILanguageSettingsProvidersKeeper);

				List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription1).getLanguageSettingProviders();
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
				assertTrue(cfgDescription2 instanceof ILanguageSettingsProvidersKeeper);
				List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription2).getLanguageSettingProviders();

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
	 * Test serialization of providers subclassing {@link LanguageSettingsSerializableProvider}.
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
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			LanguageSettingsSerializableProvider serializableProvider = new MockLanguageSettingsSerializableProvider(PROVIDER_0, PROVIDER_NAME_0);
			serializableProvider.setSettingEntries(null, null, null, entries);
			LanguageSettingsManager.setStoringEntriesInProjectArea(serializableProvider, true);

			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(serializableProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(rootElement, null, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(rootElement, null, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	 * Serialization of providers exactly equal extension providers.
	 */
	public void testProjectPersistence_ReferenceExtensionProviderDOM() throws Exception {
		Element rootElement = null;

		// provider of other type (not LanguageSettingsSerializableProvider) defined as an extension
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);

		{
			// create cfg description
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// populate with provider defined as extension
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerExt);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(rootElement, null, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(rootElement, null, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// and check the newly loaded provider which should be workspace provider
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertNotNull(provider);
			assertTrue(LanguageSettingsManager.isWorkspaceProvider(provider));
		}
	}

	/**
	 * Test serialization of providers overriding/shadowing extension providers.
	 */
	public void testProjectPersistence_OverrideExtensionProviderDOM() throws Exception {
		Element rootElement = null;

		// provider set on workspace level overriding an extension
		String idExt = EXTENSION_BASE_PROVIDER_ID;
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(idExt);
		assertNotNull(providerExt);
		{
			// create cfg description
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// populate with provider overriding the extension (must be SerializableLanguageSettingsProvider or a class from another extension)
			MockLanguageSettingsSerializableProvider providerOverride = new MockLanguageSettingsSerializableProvider(idExt, PROVIDER_NAME_0);
			LanguageSettingsManager.setStoringEntriesInProjectArea(providerOverride, true);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerOverride);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);


			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(rootElement, null, mockPrjDescription);
		}
		{
			// re-load
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(rootElement, null, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			// check the newly loaded provider
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
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
	 * Test serialization flavors in one storage.
	 */
	public void testProjectPersistence_MixedProvidersDOM() throws Exception {
		Element rootElement = null;

		List<ICLanguageSettingEntry> entries_31 = new ArrayList<ICLanguageSettingEntry>();
		entries_31.add(new CIncludePathEntry("path0", 0));

		List<ICLanguageSettingEntry> entries_32 = new ArrayList<ICLanguageSettingEntry>();
		entries_32.add(new CIncludePathEntry("path2", 0));

		ILanguageSettingsProvider providerExt;
		{
			// Define providers a bunch
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			{
				ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
				ICConfigurationDescription cfgDescription = cfgDescriptions[0];
				assertNotNull(cfgDescription);
				assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

				// 1. Provider reference to extension from plugin.xml
				providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);

				// 2. Providers defined in a configuration
				// 2.1
				LanguageSettingsSerializableProvider mockProvider1 = new LanguageSettingsSerializableProvider(PROVIDER_0, PROVIDER_NAME_0);
				LanguageSettingsManager.setStoringEntriesInProjectArea(mockProvider1, true);
				mockProvider1.setSettingEntries(null, null, null, entries_31);
				// 2.2
				LanguageSettingsSerializableProvider mockProvider2 = new MockLanguageSettingsSerializableProvider(PROVIDER_2, PROVIDER_NAME_2);
				LanguageSettingsManager.setStoringEntriesInProjectArea(mockProvider2, true);
				mockProvider2.setSettingEntries(null, null, null, entries_32);

				ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
				providers.add(providerExt);
				providers.add(mockProvider1);
				providers.add(mockProvider2);
				((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
			}

			// prepare DOM storage
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(rootElement, null, mockPrjDescription);
			XmlUtil.toString(doc);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(rootElement, null, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertNotNull(providers);
			// 1. Provider reference to extension from plugin.xml
			ILanguageSettingsProvider provider0 = providers.get(0);
			assertTrue(LanguageSettingsManager.isWorkspaceProvider(provider0));

			// 2. Providers defined in a configuration
			// 2.1
			{
				ILanguageSettingsProvider provider1 = providers.get(1);
				assertTrue(provider1 instanceof LanguageSettingsSerializableProvider);
				List<ICLanguageSettingEntry> actual = provider1.getSettingEntries(null, null, null);
				assertEquals(entries_31.get(0), actual.get(0));
				assertEquals(entries_31.size(), actual.size());
			}
			// 2.2
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
	 * Test serialization of real project.
	 */
	public void testProjectPersistence_RealProject() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		IFile xmlStorageFilePrj = project.getFile(LANGUAGE_SETTINGS_PROJECT_XML);
		String xmlPrjOutOfTheWay;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
			assertNotNull(cfgDescriptionWritable);
			assertTrue(cfgDescriptionWritable instanceof ILanguageSettingsProvidersKeeper);

			// create a provider
			LanguageSettingsSerializableProvider mockProvider = new LanguageSettingsSerializableProvider(PROVIDER_0, PROVIDER_NAME_0);
			LanguageSettingsManager.setStoringEntriesInProjectArea(mockProvider, true);
			mockProvider.setSettingEntries(cfgDescriptionWritable, null, null, entries);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescriptionWritable);
			assertTrue(xmlStorageFilePrj.exists());
		}
		{
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof LanguageSettingsSerializableProvider);
			assertEquals(PROVIDER_0, provider.getId());
			assertEquals(PROVIDER_NAME_0, provider.getName());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(cfgDescription, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
		{
			// Move storage out of the way
			String xmlStorageFileLocation = xmlStorageFilePrj.getLocation().toOSString();
			java.io.File xmlFile = new java.io.File(xmlStorageFileLocation);
			xmlPrjOutOfTheWay = xmlStorageFileLocation+".out-of-the-way";
			java.io.File xmlFileOut = new java.io.File(xmlPrjOutOfTheWay);
			xmlFile.renameTo(xmlFileOut);
			assertFalse(xmlFile.exists());
			assertTrue(xmlFileOut.exists());
		}
		{
			// Should not pollute workspace area with file with no meaningful data
			String xmlStorageFileWspLocation = getStoreLocationInWorkspaceArea(project.getName()+'.'+LANGUAGE_SETTINGS_WORKSPACE_XML);
			java.io.File xmlStorageFileWsp = new java.io.File(xmlStorageFileWspLocation);
			assertFalse(xmlStorageFileWsp.exists());
		}

		{
			// clear configuration
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
			assertNotNull(cfgDescriptionWritable);
			assertTrue(cfgDescriptionWritable instanceof ILanguageSettingsProvidersKeeper);

			((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(new ArrayList<ILanguageSettingsProvider>());
			CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescriptionWritable);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// re-check if it really took it
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// close the project
			project.close(null);
		}
		{
			// open to double-check the data is not kept in some other kind of cache
			project.open(null);

			// check that list of providers is empty
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(0, providers.size());

			// Move storage back
			String xmlStorageFileLocation = xmlStorageFilePrj.getLocation().toOSString();
			java.io.File xmlFile = new java.io.File(xmlStorageFileLocation);
			xmlFile.delete();
			assertFalse("File "+xmlFile+ " still exist", xmlFile.exists());
			java.io.File xmlFileOut = new java.io.File(xmlPrjOutOfTheWay);
			xmlFileOut.renameTo(xmlFile);
			assertTrue("File "+xmlFile+ " does not exist", xmlFile.exists());
			assertFalse("File "+xmlFileOut+ " still exist", xmlFileOut.exists());

			// Refresh storage in workspace
			xmlStorageFilePrj.refreshLocal(IResource.DEPTH_ZERO, null);
			assertTrue("File "+xmlStorageFilePrj+ " does not exist", xmlStorageFilePrj.exists());

			// and close
			project.close(null);
		}

		{
			// Remove project from internal cache
			CProjectDescriptionManager.getInstance().projectClosedRemove(project);
			// open project and check if providers are loaded
			project.open(null);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider loadedProvider = providers.get(0);
			assertTrue(loadedProvider instanceof LanguageSettingsSerializableProvider);
			assertEquals(PROVIDER_0, loadedProvider.getId());
			assertEquals(PROVIDER_NAME_0, loadedProvider.getName());

			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(cfgDescription, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 * Test case when the storage is split between project and workspace area.
	 */
	public void testProjectPersistence_SplitStorageDOM() throws Exception {
		Element prjStorageElement = null;
		Element wspStorageElement = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			LanguageSettingsSerializableProvider serializableProvider = new LanguageSettingsSerializableProvider(PROVIDER_0, PROVIDER_NAME_0);
			serializableProvider.setSettingEntries(null, null, null, entries);
			// do not store entries inside project
			LanguageSettingsManager.setStoringEntriesInProjectArea(serializableProvider, false);

			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(serializableProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

			// prepare DOM storage
			Document prjDoc = XmlUtil.newDocument();
			prjStorageElement = XmlUtil.appendElement(prjDoc, ELEM_TEST);
			Document wspDoc = XmlUtil.newDocument();
			wspStorageElement = XmlUtil.appendElement(wspDoc, ELEM_TEST);
			// serialize language settings to the DOM
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(prjStorageElement, wspStorageElement, mockPrjDescription);
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(prjStorageElement, wspStorageElement, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof LanguageSettingsSerializableProvider);

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 * TODO: refactor with ErrorParserManager
	 *
	 * @param store - name of the store
	 * @return location of the store in the plug-in state area
	 */
	public static String getStoreLocationInWorkspaceArea(String store) {
		IPath location = CCorePlugin.getDefault().getStateLocation().append(store);
		return location.toString();
	}

	/**
	 * Test split storage in a real project.
	 */
	public void testProjectPersistence_RealProjectSplitStorage() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		IFile xmlStorageFilePrj;
		String xmlPrjOutOfTheWay;
		String xmlStorageFileWspLocation;
		String xmlWspOutOfTheWay;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		{
			// get project descriptions
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			assertNotNull(prjDescriptionWritable);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
			assertNotNull(cfgDescriptionWritable);
			assertTrue(cfgDescriptionWritable instanceof ILanguageSettingsProvidersKeeper);

			// create a provider
			LanguageSettingsSerializableProvider mockProvider = new LanguageSettingsSerializableProvider(PROVIDER_0, PROVIDER_NAME_0);
			LanguageSettingsManager.setStoringEntriesInProjectArea(mockProvider, false);
			mockProvider.setSettingEntries(cfgDescriptionWritable, null, null, entries);
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(mockProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(providers);
			List<ILanguageSettingsProvider> storedProviders = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).getLanguageSettingProviders();
			assertEquals(1, storedProviders.size());

			// write to project description
			CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescriptionWritable);
			xmlStorageFilePrj = project.getFile(LANGUAGE_SETTINGS_PROJECT_XML);
			assertTrue(xmlStorageFilePrj.exists());
			xmlStorageFileWspLocation = getStoreLocationInWorkspaceArea(project.getName()+'.'+LANGUAGE_SETTINGS_WORKSPACE_XML);
			java.io.File xmlStorageFileWsp = new java.io.File(xmlStorageFileWspLocation);
			assertTrue(xmlStorageFileWsp.exists());
		}
		{
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertTrue(provider instanceof LanguageSettingsSerializableProvider);
			assertEquals(PROVIDER_0, provider.getId());
			assertEquals(PROVIDER_NAME_0, provider.getName());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(cfgDescription, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
		{
			// Move storages out of the way
			// project storage
			String xmlStorageFilePrjLocation = xmlStorageFilePrj.getLocation().toOSString();
			java.io.File xmlFile = new java.io.File(xmlStorageFilePrjLocation);
			xmlPrjOutOfTheWay = xmlStorageFilePrjLocation+".out-of-the-way";
			java.io.File xmlFileOut = new java.io.File(xmlPrjOutOfTheWay);
			xmlFile.renameTo(xmlFileOut);
			assertFalse(xmlFile.exists());
			assertTrue(xmlFileOut.exists());

			// workspace storage
			java.io.File xmlStorageFileWsp = new java.io.File(xmlStorageFileWspLocation);
			assertTrue(xmlStorageFileWsp.exists());
			xmlWspOutOfTheWay = xmlStorageFileWspLocation+".out-of-the-way";
			java.io.File xmlWspFileOut = new java.io.File(xmlWspOutOfTheWay);
			boolean result = xmlStorageFileWsp.renameTo(xmlWspFileOut);
			assertTrue(result);
			assertFalse(xmlStorageFileWsp.exists());
			assertTrue(xmlWspFileOut.exists());
		}

		{
			// clear configuration
			ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
			ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
			assertNotNull(cfgDescriptionWritable);
			assertTrue(cfgDescriptionWritable instanceof ILanguageSettingsProvidersKeeper);

			((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(new ArrayList<ILanguageSettingsProvider>());
			CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescriptionWritable);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// re-check if it really took it
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// close the project
			project.close(null);
		}
		{
			// open to double-check the data is not kept in some other kind of cache
			project.open(null);

			// check that list of providers is empty
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(0, providers.size());

			// Move project storage back
			project.open(null);
			String xmlStorageFilePrjLocation = xmlStorageFilePrj.getLocation().toOSString();
			java.io.File xmlFile = new java.io.File(xmlStorageFilePrjLocation);
			xmlFile.delete();
			assertFalse("File "+xmlFile+ " still exist", xmlFile.exists());
			java.io.File xmlFileOut = new java.io.File(xmlPrjOutOfTheWay);
			xmlFileOut.renameTo(xmlFile);
			assertTrue("File "+xmlFile+ " does not exist", xmlFile.exists());
			assertFalse("File "+xmlFileOut+ " still exist", xmlFileOut.exists());

			// Refresh storage in workspace
			xmlStorageFilePrj.refreshLocal(IResource.DEPTH_ZERO, null);
			assertTrue("File "+xmlStorageFilePrj+ " does not exist", xmlStorageFilePrj.exists());

			// and close
			project.close(null);
		}

		{
			// Move workspace storage back
			java.io.File xmlWspFile = new java.io.File(xmlStorageFileWspLocation);
			xmlWspFile.delete();
			assertFalse("File "+xmlWspFile+ " still exist", xmlWspFile.exists());
			java.io.File xmlWspFileOut = new java.io.File(xmlWspOutOfTheWay);
			xmlWspFileOut.renameTo(xmlWspFile);
			assertTrue("File "+xmlWspFile+ " does not exist", xmlWspFile.exists());
			assertFalse("File "+xmlWspFileOut+ " still exist", xmlWspFileOut.exists());
		}

		{
			// Remove project from internal cache
			CProjectDescriptionManager.getInstance().projectClosedRemove(project);
			// open project and check if providers are loaded
			project.open(null);
			ICConfigurationDescription cfgDescription = getFirstConfigurationDescription(project);
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(1, providers.size());
			ILanguageSettingsProvider loadedProvider = providers.get(0);
			assertTrue(loadedProvider instanceof LanguageSettingsSerializableProvider);
			assertEquals(PROVIDER_0, loadedProvider.getId());
			assertEquals(PROVIDER_NAME_0, loadedProvider.getName());

			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(cfgDescription, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 * Test serialization of providers referring to global shared instance.
	 */
	public void testProjectPersistence_ProviderExtensionReferenceDOM() throws Exception {
		Document doc = XmlUtil.newDocument();
		Element storageElement = XmlUtil.appendElement(doc, ELEM_TEST);

		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);
		assertNotNull(providerExt);

		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);


			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerExt);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

			// prepare DOM storage
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(storageElement, null, mockPrjDescription);

			String xml = XmlUtil.toString(doc);
			assertTrue(xml.contains(ELEM_PROVIDER_REFERENCE));
			assertTrue(xml.contains(EXTENSION_BASE_PROVIDER_ID));
			assertTrue(xml.contains(LanguageSettingsProvidersSerializer.ATTR_ID));
			assertFalse(xml.contains(LanguageSettingsProvidersSerializer.ATTR_CLASS));
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(storageElement, null, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertEquals(providerExt, provider);
		}
	}

	/**
	 * Walk the scenario when a provider is cloned to a configuration from extension.
	 */
	public void testProjectPersistence_ProviderExtensionCopyDOM() throws Exception {
		Document doc = XmlUtil.newDocument();
		Element storageElement = XmlUtil.appendElement(doc, ELEM_TEST);

		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID, true);
		assertNotNull(providerExt);

		{
			// create a provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);


			ArrayList<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerExt);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

			// prepare DOM storage
			LanguageSettingsProvidersSerializer.serializeLanguageSettingsInternal(storageElement, null, mockPrjDescription);

			String xml = XmlUtil.toString(doc);
			assertTrue(xml.contains(ELEM_PROVIDER));
			assertTrue(xml.contains(EXTENSION_EDITABLE_PROVIDER_ID));
			assertTrue(xml.contains(LanguageSettingsProvidersSerializer.ATTR_ID));
			assertFalse(xml.contains(LanguageSettingsProvidersSerializer.ATTR_CLASS));
		}
		{
			// re-load and check language settings of the newly loaded provider
			MockProjectDescription mockPrjDescription = new MockProjectDescription(new MockConfigurationDescription(CFG_ID));
			LanguageSettingsProvidersSerializer.loadLanguageSettingsInternal(storageElement, null, mockPrjDescription);

			ICConfigurationDescription[] cfgDescriptions = mockPrjDescription.getConfigurations();
			assertNotNull(cfgDescriptions);
			assertEquals(1, cfgDescriptions.length);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			assertNotNull(cfgDescription);
			assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertNotNull(providers);
			assertEquals(1, providers.size());
			ILanguageSettingsProvider provider = providers.get(0);
			assertEquals(providerExt, provider);
		}
	}

	/**
	 * Test that default settings do not cause the files to appear in the project or file-system.
	 */
	public void testProjectPersistence_Defaults() throws Exception {
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		IFile xmlStorageFilePrj = project.getFile(LANGUAGE_SETTINGS_PROJECT_XML);
		assertFalse(xmlStorageFilePrj.exists());

		String xmlPrjWspStorageFileLocation = getStoreLocationInWorkspaceArea(project.getName()+'.'+LANGUAGE_SETTINGS_WORKSPACE_XML);
		java.io.File xmlStorageFilePrjWsp = new java.io.File(xmlPrjWspStorageFileLocation);
		assertFalse(xmlStorageFilePrjWsp.exists());
	}

	/**
	 * Test serialization of global providers exactly equal extension in workspace area.
	 */
	public void testWorkspacePersistence_ProviderExtensionCopy() throws Exception {
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// get extension provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			LanguageSettingsSerializableProvider rawProvider = (LanguageSettingsSerializableProvider) LanguageSettingsManager.getRawProvider(provider);
			assertNotNull(rawProvider);
			assertEquals(EXTENSION_EDITABLE_PROVIDER_ID, rawProvider.getId());

			// add non-modified provider to the list
			providers.add(provider);
		}
		{
			// get another extension provider
			ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			LanguageSettingsSerializableProvider rawProvider = (LanguageSettingsSerializableProvider) LanguageSettingsManager.getRawProvider(provider);
			assertNotNull(rawProvider);
			assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, rawProvider.getId());
			// modify it and add it to the list
			rawProvider.setSettingEntries(null, null, null, entries);
			providers.add(rawProvider);

			// set and serialize language settings of workspace providers
			LanguageSettingsManager.setWorkspaceProviders(providers);
			LanguageSettingsManager.serializeLanguageSettingsWorkspace();
		}
		{
			String xmlStorageFileWspLocation = getStoreLocationInWorkspaceArea(LANGUAGE_SETTINGS_WORKSPACE_XML);
			String xml = ResourceHelper.getContents(xmlStorageFileWspLocation);
			// provider matching extension is not saved (extensions added automatically during loading providers)
			assertFalse(xml.contains(EXTENSION_EDITABLE_PROVIDER_ID));
			// provider that differs is saved
			assertTrue(xml.contains(EXTENSION_SERIALIZABLE_PROVIDER_ID));
		}
	}

	/**
	 * Test that default settings do not cause the file to appear on the file-system.
	 */
	public void testWorkspacePersistence_Defaults() throws Exception {
		// reset and serialize workspace providers
		LanguageSettingsManager.setWorkspaceProviders(null);
		LanguageSettingsManager.serializeLanguageSettingsWorkspace();

		// check that XML file is not created
		String xmlStorageFileWspLocation = getStoreLocationInWorkspaceArea(LANGUAGE_SETTINGS_WORKSPACE_XML);
		java.io.File xmlStorageFileWsp = new java.io.File(xmlStorageFileWspLocation);
		assertFalse(xmlStorageFileWsp.exists());
	}

}
