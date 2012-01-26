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
import java.util.List;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.CModelMock;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases testing serialization of LanguageSettingsProviders.
 */
public class LanguageSettingsSerializableProviderTests extends BaseTestCase {
	// Arbitrary sample parameters used by the test
	private static final String CFG_ID = "test.configuration.id";
	private static final ICConfigurationDescription MOCK_CFG = new CModelMock.DummyCConfigurationDescription(CFG_ID);
	private static final IResource MOCK_RC = ResourcesPlugin.getWorkspace().getRoot();
	private static final String LANG_ID = "test.lang.id";
	private static final String LANG_ID_1 = "test.lang.id.1";
	private static final String LANG_ID_2 = "test.lang.id.2";
	private static final String PROVIDER_NULL = "test.provider.null.id";
	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String PROVIDER_NAME_NULL = "test.provider.null.name";
	private static final String PROVIDER_NAME_1 = "test.provider.1.name";
	private static final String PROVIDER_NAME_2 = "test.provider.2.name";
	private static final String ATTR_PARAMETER = "parameter";
	private static final String VALUE_PARAMETER = "custom.parameter";
	private static final String ELEM_TEST = "test";
	private static final String ATTR_PROPERTY = "custom-property";
	private static final String ATTR_PROPERTY_BOOL = "custom-property-bool";
	private static final String VALUE_PROPERTY = "custom.property";

	// This value must match that of LanguageSettingsProvidersSerializer.ATTR_STORE_ENTRIES_WITH_PROJECT
	private static final String ATTR_STORE_ENTRIES_WITH_PROJECT = "store-entries-with-project";

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsSerializableProviderTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown(); // includes ResourceHelper cleanup
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsSerializableProviderTests.class);
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
	 * Test basic methods, getters and setters.
	 */
	public void testProvider_SettersGetters() throws Exception {
		// benchmark data
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<String> languages = new ArrayList<String>();
		languages.add(LANG_ID);

		// create a provider
		LanguageSettingsSerializableProvider mockProvider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
		// test isEmpty()
		assertTrue(mockProvider.isEmpty());

		// test setters and getters
		mockProvider.setId(PROVIDER_2);
		assertEquals(PROVIDER_2, mockProvider.getId());
		mockProvider.setName(PROVIDER_NAME_2);
		assertEquals(PROVIDER_NAME_2, mockProvider.getName());
		mockProvider.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);
		assertEquals(VALUE_PARAMETER, mockProvider.getProperty(ATTR_PARAMETER));

		mockProvider.setLanguageScope(languages);
		assertEquals(languages, mockProvider.getLanguageScope());
		mockProvider.setLanguageScope(null);
		assertEquals(null, mockProvider.getLanguageScope());

		mockProvider.setSettingEntries(null, MOCK_RC, LANG_ID, entries);
		List<ICLanguageSettingEntry> actual = mockProvider.getSettingEntries(null, MOCK_RC, LANG_ID);
		assertEquals(entries.get(0), actual.get(0));
		assertEquals(entries.size(), actual.size());
		assertFalse(mockProvider.isEmpty());

		// test clear()
		mockProvider.clear();
		assertTrue(mockProvider.isEmpty());
	}

	/**
	 * Test property defining whether to store entries in project or workspace area.
	 */
	public void testProvider_SetStoringEntriesInProjectArea() throws Exception {
		// create a provider
		LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);

		assertEquals(false, LanguageSettingsManager.isStoringEntriesInProjectArea(provider));
		LanguageSettingsManager.setStoringEntriesInProjectArea(provider, true);
		assertEquals(true, LanguageSettingsManager.isStoringEntriesInProjectArea(provider));
		LanguageSettingsManager.setStoringEntriesInProjectArea(provider, false);
		assertEquals(false, LanguageSettingsManager.isStoringEntriesInProjectArea(provider));
	}

	/**
	 * Check basic serialization.
	 */
	public void testProvider_RegularDOM() throws Exception {
		Element elementProvider;
		{
			// create customized provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			LanguageSettingsManager.setStoringEntriesInProjectArea(provider, true);
			provider.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);

			List<String> languageScope = new ArrayList<String>();
			languageScope.add(LANG_ID);
			provider.setLanguageScope(languageScope);

			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(new CIncludePathEntry("path0", 1));
			provider.setSettingEntries(null, null, null, entries);

			// serialize
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
			// check XML
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains(PROVIDER_1));
			assertTrue(xmlString.contains(PROVIDER_NAME_1));
			assertTrue(xmlString.contains(VALUE_PARAMETER));
			assertTrue(xmlString.contains(LANG_ID));
			assertTrue(xmlString.contains("path0"));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			assertEquals(true, LanguageSettingsManager.isStoringEntriesInProjectArea(provider));
			assertEquals(VALUE_PARAMETER, provider.getProperty(ATTR_PARAMETER));
			assertNotNull(provider.getLanguageScope());
			assertTrue(provider.getLanguageScope().size()>0);
			assertEquals(LANG_ID, provider.getLanguageScope().get(0));

			List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);
			assertNotNull(entries);
			assertTrue(entries.size()>0);
			assertEquals(new CIncludePathEntry("path0", 1), entries.get(0));
		}
	}

	/**
	 * Test serialization of properties of the provider.
	 */
	public void testProvider_serializeAttributesDOM() throws Exception {
		Element elementProvider;
		{
			// create customized provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			LanguageSettingsManager.setStoringEntriesInProjectArea(provider, true);
			provider.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);

			List<String> languageScope = new ArrayList<String>();
			languageScope.add(LANG_ID);
			provider.setLanguageScope(languageScope);

			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(new CIncludePathEntry("path0", 1));
			provider.setSettingEntries(null, null, null, entries);

			// serialize
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serializeAttributes(rootElement);
			// check XML
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains(PROVIDER_1));
			assertTrue(xmlString.contains(PROVIDER_NAME_1));
			assertTrue(xmlString.contains(VALUE_PARAMETER));
			assertTrue(xmlString.contains(LANG_ID));
			// no entries
			assertFalse(xmlString.contains("path0"));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider();
			provider.loadAttributes(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			assertEquals(true, LanguageSettingsManager.isStoringEntriesInProjectArea(provider));
			assertEquals(VALUE_PARAMETER, provider.getProperty(ATTR_PARAMETER));
			assertNotNull(provider.getLanguageScope());
			assertTrue(provider.getLanguageScope().size()>0);
			assertEquals(LANG_ID, provider.getLanguageScope().get(0));
			// no entries should be loaded
			List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);
			assertNull(entries);
		}
	}

	/**
	 * Test serialization of entries.
	 */
	public void testProvider_serializeEntriesDOM() throws Exception {
		Element rootElement;
		{
			// create customized provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			LanguageSettingsManager.setStoringEntriesInProjectArea(provider, true);
			provider.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);

			List<String> languageScope = new ArrayList<String>();
			languageScope.add(LANG_ID);
			provider.setLanguageScope(languageScope);

			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			entries.add(new CIncludePathEntry("path0", 1));
			provider.setSettingEntries(null, null, null, entries);

			// serialize
			Document doc = XmlUtil.newDocument();
			rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			provider.serializeEntries(rootElement);
			// check XML
			String xmlString = XmlUtil.toString(doc);
			// no attributes
			assertFalse(xmlString.contains(PROVIDER_1));
			assertFalse(xmlString.contains(PROVIDER_NAME_1));
			assertFalse(xmlString.contains(VALUE_PARAMETER));
			assertFalse(xmlString.contains(LANG_ID));
			// entries should be present
			assertTrue(xmlString.contains("path0"));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_2, PROVIDER_NAME_2);
			provider.loadEntries(rootElement);
			assertEquals(PROVIDER_2, provider.getId());
			assertEquals(PROVIDER_NAME_2, provider.getName());
			// no attributes should be loaded
			assertFalse(PROVIDER_1.equals(provider.getId()));
			assertFalse(PROVIDER_NAME_1.equals(provider.getName()));
			assertFalse(true==LanguageSettingsManager.isStoringEntriesInProjectArea(provider));
			assertFalse(VALUE_PARAMETER.equals(provider.getProperty(ATTR_PARAMETER)));
			assertNull(provider.getLanguageScope());
			// entries should be loaded
			List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);
			assertNotNull(entries);
			assertTrue(entries.size()>0);
			assertEquals(new CIncludePathEntry("path0", 1), entries.get(0));
		}
	}

	/**
	 * Test serialization of empty provider.
	 */
	public void testProvider_EmptyDOM() throws Exception {
		Element elementProvider;
		{
			// create null provider
			LanguageSettingsSerializableProvider providerNull = new LanguageSettingsSerializableProvider(PROVIDER_NULL, PROVIDER_NAME_NULL);
			assertNull(providerNull.getSettingEntries(null, null, null));
			// set and get null entries
			providerNull.setSettingEntries(null, null, null, null);
			assertNull(providerNull.getSettingEntries(null, null, null));

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = providerNull.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains(PROVIDER_NULL));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_NULL, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertNull(actual);
		}
	}

	/**
	 * Test serialization of custom parameter.
	 */
	public void testCustomParameterDOM() throws Exception {
		Element elementProvider;
		{
			// create provider with custom parameter
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);
			assertEquals(VALUE_PARAMETER, provider.getProperty(ATTR_PARAMETER));

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains(VALUE_PARAMETER));
		}
		{
			// re-load and check custom parameter of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(VALUE_PARAMETER, provider.getProperty(ATTR_PARAMETER));
		}
	}

	/**
	 * Test serialization to project area storage.
	 */
	public void testStoreEntriesWithProjectDOM() throws Exception {
		Element elementProvider;
		{
			// create provider storing entries in project area
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			assertEquals(false, LanguageSettingsManager.isStoringEntriesInProjectArea(provider));
			LanguageSettingsManager.setStoringEntriesInProjectArea(provider, true);
			assertEquals(true, LanguageSettingsManager.isStoringEntriesInProjectArea(provider));

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains(ATTR_STORE_ENTRIES_WITH_PROJECT));
			assertTrue(xmlString.contains(ATTR_STORE_ENTRIES_WITH_PROJECT+"=\"true\""));
		}
		{
			// re-load and check storing mode of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(true, LanguageSettingsManager.isStoringEntriesInProjectArea(provider));
		}
	}

	/**
	 * Test serialization of language scope.
	 */
	public void testLanguagesDOM() throws Exception {
		List<String> expectedLanguageIds = new ArrayList<String>();
		expectedLanguageIds.add(LANG_ID);
		expectedLanguageIds.add(LANG_ID_1);

		Element elementProvider;
		{
			// create provider with custom language scope
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setLanguageScope(expectedLanguageIds);
			List<String> actualIds = provider.getLanguageScope();
			assertEquals(LANG_ID, actualIds.get(0));
			assertEquals(LANG_ID_1, actualIds.get(1));
			assertEquals(2, actualIds.size());

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains(LANG_ID));
			assertTrue(xmlString.contains(LANG_ID_1));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			List<String> actualIds = provider.getLanguageScope();
			assertEquals(expectedLanguageIds.get(0), actualIds.get(0));
			assertEquals(expectedLanguageIds.get(1), actualIds.get(1));
			assertEquals(expectedLanguageIds.size(), actualIds.size());
		}
	}

	/**
	 * Edge cases for language scope.
	 */
	public void testLanguageScopeDOM() throws Exception {
		// benchmark entries
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		// define the scope
		List<String> expectedLanguageIds = new ArrayList<String>();
		expectedLanguageIds.add(LANG_ID);
		expectedLanguageIds.add(LANG_ID_1);

		Element elementProvider;
		{
			// create provider with no scope by default
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			// set entries for the whole language scope (now langId=null)
			provider.setSettingEntries(null, null, null, entries);
			{
				// doublecheck for language scope itself
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
				assertEquals(entries, actual);
			}
			{
				// doublecheck for any language
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, LANG_ID_2);
				assertEquals(entries, actual);
			}

			// set the scope
			provider.setLanguageScope(expectedLanguageIds);
			List<String> actualIds = provider.getLanguageScope();
			assertEquals(LANG_ID, actualIds.get(0));
			assertEquals(LANG_ID_1, actualIds.get(1));
			assertEquals(2, actualIds.size());

			{
				// check for language scope itself
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
				assertEquals(entries, actual);
			}
			{
				// check for language in the language scope
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, LANG_ID);
				assertEquals(entries, actual);
			}
			{
				// check for language not in scope
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, LANG_ID_2);
				assertNull(actual);
			}

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains(LANG_ID));
			assertTrue(xmlString.contains(LANG_ID_1));
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			List<String> actualIds = provider.getLanguageScope();
			assertEquals(expectedLanguageIds.get(0), actualIds.get(0));
			assertEquals(expectedLanguageIds.get(1), actualIds.get(1));
			assertEquals(expectedLanguageIds.size(), actualIds.size());

			{
				// check for language scope itself
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
				assertEquals(entries, actual);
			}
			{
				// check for language in the language scope
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, LANG_ID);
				assertEquals(entries, actual);
			}
			{
				// check for language not in scope
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, LANG_ID_2);
				assertNull(actual);
			}
		}
	}

	/**
	 * Test serialization of entries when configuration description is null.
	 */
	public void testNullConfigurationDOM() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, MOCK_RC, LANG_ID, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "configuration" element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<configuration")); // LanguageSettingsSerializableProvider.ELEM_CONFIGURATION;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, LANG_ID);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 * Test serialization of entries when language is null.
	 */
	public void testNullLanguageDOM() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(MOCK_CFG, MOCK_RC, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "language" element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<language")); // LanguageSettingsStorage.ELEM_LANGUAGE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(MOCK_CFG, MOCK_RC, null);
			assertEquals(entries.get(0), actual.get(0));
		}
	}


	/**
	 * Test serialization of entries when language scope is null.
	 */
	public void testNullLanguageScopeDOM() throws Exception {
		// define benchmark entries
		List<ICLanguageSettingEntry> entriesNullLanguage = new ArrayList<ICLanguageSettingEntry>();
		entriesNullLanguage.add(new CIncludePathEntry("path_null", 0));
		List<ICLanguageSettingEntry> entriesLanguage = new ArrayList<ICLanguageSettingEntry>();
		entriesLanguage.add(new CIncludePathEntry("path", 0));

		Element elementProvider;

		{
			// create a provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			assertEquals(null, provider.getLanguageScope());

			// add null language
			provider.setSettingEntries(null, MOCK_RC, null, entriesNullLanguage);
			assertEquals(null, provider.getLanguageScope());
			{
				// getter by null language
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, null);
				assertEquals(entriesNullLanguage.get(0), actual.get(0));
				assertEquals(entriesNullLanguage.size(), actual.size());
			}
			{
				// getter by any language - should return same entries as null
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, LANG_ID);
				assertEquals(entriesNullLanguage.get(0), actual.get(0));
				assertEquals(entriesNullLanguage.size(), actual.size());
			}

			// add non-null language
			provider.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, entriesLanguage);
			assertNull(provider.getLanguageScope());
			{
				// getter by null language
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, null);
				assertEquals(entriesNullLanguage.get(0), actual.get(0));
				assertEquals(entriesNullLanguage.size(), actual.size());
			}
			{
				// getter by the language
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, LANG_ID);
				assertEquals(entriesLanguage.get(0), actual.get(0));
				assertEquals(entriesLanguage.size(), actual.size());
			}

			// provider/configuration/language/resource/settingEntry
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "language" element is saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains("<language")); // LanguageSettingsStorage.ELEM_LANGUAGE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			{
				// getter by null language
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, null);
				assertEquals(entriesNullLanguage.get(0), actual.get(0));
				assertEquals(entriesNullLanguage.size(), actual.size());
			}
			{
				// getter by the language
				List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, LANG_ID);
				assertEquals(entriesLanguage.get(0), actual.get(0));
				assertEquals(entriesLanguage.size(), actual.size());
			}
		}
	}

	/**
	 * Test serialization of entries when resource is null.
	 */
	public void testNullResourceDOM() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(MOCK_CFG, null, LANG_ID, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "resource" element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<resource")); // LanguageSettingsStorage.ELEM_RESOURCE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(MOCK_CFG, null, LANG_ID);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 * Test serialization of entries when configuration and language are both null.
	 */
	public void testNullConfigurationLanguageDOM() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, MOCK_RC, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<configuration")); // LanguageSettingsSerializableProvider.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains("<language")); // LanguageSettingsStorage.ELEM_LANGUAGE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, null);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 * Test serialization of entries when configuration and resource are both null.
	 */
	public void testNullConfigurationResourceDOM() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, LANG_ID, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<configuration")); // LanguageSettingsSerializableProvider.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains("<resource")); // LanguageSettingsStorage.ELEM_RESOURCE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, LANG_ID);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 * Test serialization of entries when language and resource are both null.
	 */
	public void testNullLanguageResourceDOM() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(MOCK_CFG, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<language")); // LanguageSettingsStorage.ELEM_LANGUAGE;
			assertFalse(xmlString.contains("<resource")); // LanguageSettingsStorage.ELEM_RESOURCE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(MOCK_CFG, null, null);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 * Test serialization of entries when configuration, language and resource are all null.
	 */
	public void testNullConfigurationLanguageResourceFlagDOM() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		int flag = 0;
		entries.add(new CIncludePathEntry("path0", flag));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<configuration")); // LanguageSettingsSerializableProvider.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains("<language")); // LanguageSettingsStorage.ELEM_LANGUAGE;
			assertFalse(xmlString.contains("<resource")); // LanguageSettingsStorage.ELEM_RESOURCE;
			assertFalse(xmlString.contains("<flag")); // LanguageSettingsStorage.ELEM_FLAG;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 * Serialization of include path.
	 */
	public void testCIncludePathEntryDOM() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = actual.get(0);
			assertTrue(entry instanceof CIncludePathEntry);

			CIncludePathEntry includePathEntry = (CIncludePathEntry)entry;
			assertEquals(entries.get(0).getName(), includePathEntry.getName());
			assertEquals(entries.get(0).getValue(), includePathEntry.getValue());
			assertEquals(entries.get(0).getKind(), includePathEntry.getKind());
			assertEquals(entries.get(0).getFlags(), includePathEntry.getFlags());
			assertEquals(entries.get(0), includePathEntry);
		}
	}

	/**
	 * Serialization of include file.
	 */
	public void testCIncludeFileEntryDOM() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludeFileEntry("a-path", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = actual.get(0);
			assertTrue(entry instanceof CIncludeFileEntry);
			CIncludeFileEntry includeFileEntry = (CIncludeFileEntry)entry;
			assertEquals(entries.get(0).getName(), includeFileEntry.getName());
			assertEquals(entries.get(0).getValue(), includeFileEntry.getValue());
			assertEquals(entries.get(0).getKind(), includeFileEntry.getKind());
			assertEquals(entries.get(0).getFlags(), includeFileEntry.getFlags());
			assertEquals(entries.get(0), includeFileEntry);
		}
	}

	/**
	 * Serialization of macro.
	 */
	public void testCMacroEntryDOM() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CMacroEntry("MACRO0", "value0",1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = actual.get(0);
			assertTrue(entry instanceof CMacroEntry);
			CMacroEntry macroEntry = (CMacroEntry)entry;
			assertEquals(entries.get(0).getName(), macroEntry.getName());
			assertEquals(entries.get(0).getValue(), macroEntry.getValue());
			assertEquals(entries.get(0).getKind(), macroEntry.getKind());
			assertEquals(entries.get(0).getFlags(), macroEntry.getFlags());
			assertEquals(entries.get(0), macroEntry);
		}
	}

	/**
	 * Serialization of macro file.
	 */
	public void testCMacroFileEntryDOM() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CMacroFileEntry("a-path", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = actual.get(0);
			assertTrue(entry instanceof CMacroFileEntry);
			CMacroFileEntry macroFileEntry = (CMacroFileEntry)entry;
			assertEquals(entries.get(0).getName(), macroFileEntry.getName());
			assertEquals(entries.get(0).getValue(), macroFileEntry.getValue());
			assertEquals(entries.get(0).getKind(), macroFileEntry.getKind());
			assertEquals(entries.get(0).getFlags(), macroFileEntry.getFlags());
			assertEquals(entries.get(0), macroFileEntry);
		}
	}

	/**
	 * Serialization of library path.
	 */
	public void testCLibraryPathEntryDOM() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CLibraryPathEntry("a-path", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = actual.get(0);
			assertTrue(entry instanceof CLibraryPathEntry);
			CLibraryPathEntry libraryPathEntry = (CLibraryPathEntry)entry;
			assertEquals(entries.get(0).getName(), libraryPathEntry.getName());
			assertEquals(entries.get(0).getValue(), libraryPathEntry.getValue());
			assertEquals(entries.get(0).getKind(), libraryPathEntry.getKind());
			assertEquals(entries.get(0).getFlags(), libraryPathEntry.getFlags());
			assertEquals(entries.get(0), libraryPathEntry);
		}
	}

	/**
	 * Serialization of library file.
	 */
	public void testCLibraryFileEntryDOM() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CLibraryFileEntry("a-path", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			ICLanguageSettingEntry entry = actual.get(0);
			assertTrue(entry instanceof CLibraryFileEntry);
			CLibraryFileEntry libraryFileEntry = (CLibraryFileEntry)entry;
			assertEquals(entries.get(0).getName(), libraryFileEntry.getName());
			assertEquals(entries.get(0).getValue(), libraryFileEntry.getValue());
			assertEquals(entries.get(0).getKind(), libraryFileEntry.getKind());
			assertEquals(entries.get(0).getFlags(), libraryFileEntry.getFlags());
			assertEquals(entries.get(0), libraryFileEntry);
		}
	}

	/**
	 * Serialization of entries of different types.
	 */
	public void testMixedSettingEntriesDOM() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 1));
		entries.add(new CIncludePathEntry("path1", 1));
		entries.add(new CMacroEntry("MACRO0", "value0",1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.get(1), actual.get(1));
			assertEquals(entries.get(2), actual.get(2));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 * Serialization of entries for default and specific languages together.
	 */
	public void testLanguageAndNullDOM() throws Exception {
		Element elementProvider = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> entries2 = new ArrayList<ICLanguageSettingEntry>();
		entries2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			LanguageSettingsSerializableProvider mockProvider = null;
			mockProvider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			mockProvider.setSettingEntries(null, null, null, entries);
			mockProvider.setSettingEntries(null, null, LANG_ID, entries2);

			// serialize language settings to DOM
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = mockProvider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider loadedProvider = new LanguageSettingsSerializableProvider(elementProvider);

			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			List<ICLanguageSettingEntry> actual2 = loadedProvider.getSettingEntries(null, null, LANG_ID);
			assertEquals(entries2.get(0), actual2.get(0));
			assertEquals(entries2.size(), actual2.size());
		}
	}

	/**
	 * Serialization of entries for 2 languages.
	 */
	public void testTwoLanguagesDOM() throws Exception {
		Element elementProvider = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> entries2 = new ArrayList<ICLanguageSettingEntry>();
		entries2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			LanguageSettingsSerializableProvider mockProvider = null;
			mockProvider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			mockProvider.setSettingEntries(null, null, LANG_ID_1, entries);
			mockProvider.setSettingEntries(null, null, LANG_ID_2, entries2);

			// serialize language settings to DOM
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = mockProvider.serialize(rootElement);
			String xml = XmlUtil.toString(elementProvider.getOwnerDocument());
//			fail(xml); // for debugging
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider loadedProvider = new LanguageSettingsSerializableProvider(elementProvider);

			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(null, null, LANG_ID_1);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			List<ICLanguageSettingEntry> actual2 = loadedProvider.getSettingEntries(null, null, LANG_ID_2);
			assertEquals(entries2.get(0), actual2.get(0));
			assertEquals(entries2.size(), actual2.size());
		}
	}

	/**
	 * Serialization of entries for different resources.
	 */
	public void testTwoResourcesDOM() throws Exception {
		// Create resources
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());
		IFile rc1 = ResourceHelper.createFile(project, "rc1");
		assertNotNull(rc1);
		IFile rc2 = ResourceHelper.createFile(project, "rc2");
		assertNotNull(rc2);
		assertFalse(rc1.getFullPath().equals(rc2.getFullPath()));

		Element elementProvider = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> entries2 = new ArrayList<ICLanguageSettingEntry>();
		entries2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			LanguageSettingsSerializableProvider mockProvider = null;
			mockProvider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
			mockProvider.setSettingEntries(null, rc1, null, entries);
			mockProvider.setSettingEntries(null, rc2, null, entries2);

			// serialize language settings to DOM
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = mockProvider.serialize(rootElement);
			String xml = XmlUtil.toString(elementProvider.getOwnerDocument());
//			fail(xml); // for debugging
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializableProvider loadedProvider = new LanguageSettingsSerializableProvider(elementProvider);

			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(null, rc1, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			List<ICLanguageSettingEntry> actual2 = loadedProvider.getSettingEntries(null, rc2, null);
			assertEquals(entries2.get(0), actual2.get(0));
			assertEquals(entries2.size(), actual2.size());
		}
	}

	/**
	 * Serialization of entries for resource hierarchy.
	 */
	public void testParentFolder() throws Exception {
		// Create model project and accompanied descriptions
		IProject project = ResourceHelper.createCDTProjectWithConfig(this.getName());

		// Create resources
		IFolder parentFolder = ResourceHelper.createFolder(project, "/ParentFolder/");
		assertNotNull(parentFolder);
		IFile emptySettingsPath = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/empty");
		assertNotNull(emptySettingsPath);

		// Create provider
		LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);

		// store the entries in parent folder
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		provider.setSettingEntries(null, parentFolder, LANG_ID, entries);
		provider.setSettingEntries(null, emptySettingsPath, LANG_ID, new ArrayList<ICLanguageSettingEntry>());

		{
			// retrieve entries for a parent folder itself
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, parentFolder, LANG_ID);
			assertEquals(entries,actual);
			assertEquals(entries.size(), actual.size());
		}

		{
			// retrieve entries for a derived resource (in a subfolder)
			IFile derived = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/resource");
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, derived, LANG_ID);
			// NOT taken from parent folder
			assertEquals(null,actual);
		}

		{
			// retrieve entries for not related resource
			IFile notRelated = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/AnotherFolder/Subfolder/resource"));
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, notRelated, LANG_ID);
			assertEquals(null,actual);
		}

		{
			// test distinction between no settings and empty settings
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, emptySettingsPath, LANG_ID);
			// NOT taken from parent folder and not null
			assertEquals(0, actual.size());
		}
	}

	/**
	 * Test equals() and hashCode().
	 */
	public void testEquals() throws Exception {
		// create sample entries
		List<ICLanguageSettingEntry> sampleEntries_1 = new ArrayList<ICLanguageSettingEntry>();
		sampleEntries_1.add(new CMacroEntry("MACRO0", "value0",1));
		sampleEntries_1.add(new CIncludePathEntry("path0", 1));
		sampleEntries_1.add(new CIncludePathEntry("path1", 1));

		List<ICLanguageSettingEntry> sampleEntries_2 = new ArrayList<ICLanguageSettingEntry>();
		sampleEntries_2.add(new CIncludePathEntry("path0", 1));

		// create sample languages
		List<String> sampleLanguages = new ArrayList<String>();
		sampleLanguages.add(LANG_ID);

		// create a model provider
		LanguageSettingsSerializableProvider provider1 = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
		provider1.setLanguageScope(sampleLanguages);
		provider1.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);
		assertEquals(false, LanguageSettingsManager.isStoringEntriesInProjectArea(provider1));
		LanguageSettingsManager.setStoringEntriesInProjectArea(provider1, true);
		provider1.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, sampleEntries_1);
		provider1.setSettingEntries(null, null, LANG_ID, sampleEntries_2);

		// create another provider with the same data
		LanguageSettingsSerializableProvider provider2 = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
		{
			provider2.setLanguageScope(sampleLanguages);
			provider2.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);
			LanguageSettingsManager.setStoringEntriesInProjectArea(provider2, true);
			provider2.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, sampleEntries_1);
			provider2.setSettingEntries(null, null, LANG_ID, sampleEntries_2);
			// All set now, so they should be equal
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
		}

		{
			// start with provider with the same data
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
			// replace languages
			List<String> sampleLanguages2 = new ArrayList<String>();
			sampleLanguages2.add(LANG_ID_1);
			provider2.setLanguageScope(sampleLanguages2);
			assertFalse(provider1.hashCode()==provider2.hashCode());
			assertFalse(provider1.equals(provider2));
			// restore provider
			provider2.setLanguageScope(sampleLanguages);
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
		}

		{
			// start with provider with the same data
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
			// replace property
			provider2.setProperty(ATTR_PARAMETER, "changed-parameter");
			// hash is not calculated for properties
			assertFalse(provider1.equals(provider2));
			// restore provider
			provider2.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
		}
		{
			// start with provider with the same data
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
			// replace property
			LanguageSettingsManager.setStoringEntriesInProjectArea(provider2, false);
			// hash is not calculated for properties
			assertFalse(provider1.equals(provider2));
			// restore provider
			LanguageSettingsManager.setStoringEntriesInProjectArea(provider2, true);
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
		}
		{
			// start with provider with the same data
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
			// replace entries
			List<ICLanguageSettingEntry> changedEntries = new ArrayList<ICLanguageSettingEntry>();
			changedEntries.add(new CMacroEntry("MACROX", "valueX",1));
			provider2.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, changedEntries);
			assertFalse(provider1.hashCode()==provider2.hashCode());
			assertFalse(provider1.equals(provider2));
			// restore provider
			provider2.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, sampleEntries_1);
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
		}
		{
			// start with provider with the same data
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
			// replace default entries
			List<ICLanguageSettingEntry> changedEntries = new ArrayList<ICLanguageSettingEntry>();
			changedEntries.add(new CIncludePathEntry("pathX", 1));
			provider2.setSettingEntries(null, null, LANG_ID, changedEntries);
			assertFalse(provider1.hashCode()==provider2.hashCode());
			assertFalse(provider1.equals(provider2));
			// restore provider
			provider2.setSettingEntries(null, null, LANG_ID, sampleEntries_2);
			assertTrue(provider1.hashCode()==provider2.hashCode());
			assertTrue(provider1.equals(provider2));
		}

		{
			// check that subclasses are not equal
			LanguageSettingsSerializableProvider providerSub1 = new LanguageSettingsSerializableProvider() {};
			LanguageSettingsSerializableProvider providerSub2 = new LanguageSettingsSerializableProvider() {};
			assertFalse(providerSub1.hashCode()==providerSub2.hashCode());
			assertFalse(providerSub1.equals(providerSub2));
		}
	}

	/**
	 * Test equality for properties.
	 */
	public void testEquals_DefaultProperties() throws Exception {
		// create model providers
		LanguageSettingsSerializableProvider provider1 = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
		LanguageSettingsSerializableProvider provider2 = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);

		// equality for setProperty(String, String)
		{
			// equality for missing property
			assertTrue(provider1.equals(provider2));
			// equality for default empty value (missing in provider2)
			provider1.setProperty(ATTR_PROPERTY, "");
			assertTrue(provider1.equals(provider2));
			// just for kicks disturb equality
			provider1.setProperty(ATTR_PROPERTY, VALUE_PROPERTY);
			assertFalse(provider1.equals(provider2));
			// equality for default null value (missing in provider2)
			provider1.setProperty(ATTR_PROPERTY, null);
			assertTrue(provider1.equals(provider2));
		}

		// equality for setPropertyBool(String, boolean)
		{
			// equality for missing property
			assertEquals(false, provider1.getPropertyBool(ATTR_PROPERTY_BOOL));
			assertTrue(provider1.equals(provider2));
			// equality for default empty value (missing in provider2)
			provider1.setPropertyBool(ATTR_PROPERTY_BOOL, false);
			assertEquals(false, provider1.getPropertyBool(ATTR_PROPERTY_BOOL));
			assertTrue(provider1.equals(provider2));
			// just for kicks disturb equality
			provider1.setPropertyBool(ATTR_PROPERTY_BOOL, true);
			assertEquals(true, provider1.getPropertyBool(ATTR_PROPERTY_BOOL));
			assertFalse(provider1.equals(provider2));
			// equality for true value in both
			provider2.setPropertyBool(ATTR_PROPERTY_BOOL, true);
			assertEquals(true, provider2.getPropertyBool(ATTR_PROPERTY_BOOL));
			assertTrue(provider1.equals(provider2));
			// switch provider1 back to false
			provider1.setPropertyBool(ATTR_PROPERTY_BOOL, false);
			provider1.setPropertyBool(ATTR_PROPERTY_BOOL, false);
			assertFalse(provider1.equals(provider2));
		}
	}

	/**
	 * Test cloning of provider.
	 */
	public void testClone() throws Exception {
		// define sample data
		List<ICLanguageSettingEntry> sampleEntries_1 = new ArrayList<ICLanguageSettingEntry>();
		sampleEntries_1.add(new CIncludePathEntry("path0", 1));
		sampleEntries_1.add(new CIncludePathEntry("path1", 1));
		sampleEntries_1.add(new CMacroEntry("MACRO0", "value0",1));

		List<ICLanguageSettingEntry> sampleEntries_2 = new ArrayList<ICLanguageSettingEntry>();
		sampleEntries_2.add(new CIncludePathEntry("path0", 1));

		List<String> sampleLanguages = new ArrayList<String>();
		sampleLanguages.add(LANG_ID);

		// create a model provider
		class MockSerializableProvider extends LanguageSettingsSerializableProvider implements Cloneable {
			public MockSerializableProvider(String id, String name) {
				super(id, name);
			}
			@Override
			public MockSerializableProvider clone() throws CloneNotSupportedException {
				return (MockSerializableProvider) super.clone();
			}

		}
		MockSerializableProvider provider1 = new MockSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
		provider1.setLanguageScope(sampleLanguages);
		provider1.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);
		assertEquals(false, LanguageSettingsManager.isStoringEntriesInProjectArea(provider1));
		LanguageSettingsManager.setStoringEntriesInProjectArea(provider1, true);
		provider1.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, sampleEntries_1);
		provider1.setSettingEntries(null, null, LANG_ID, sampleEntries_2);

		// clone provider
		MockSerializableProvider providerClone = provider1.clone();
		assertNotSame(provider1, providerClone);
		assertTrue(provider1.equals(providerClone));
		assertTrue(provider1.getClass()==providerClone.getClass());

		assertEquals(provider1.getProperty(ATTR_PARAMETER), providerClone.getProperty(ATTR_PARAMETER));
		// ensure we did not clone reference
		provider1.setProperty(ATTR_PARAMETER, "");
		assertFalse(provider1.getProperty(ATTR_PARAMETER).equals(providerClone.getProperty(ATTR_PARAMETER)));

		assertEquals(LanguageSettingsManager.isStoringEntriesInProjectArea(provider1), LanguageSettingsManager.isStoringEntriesInProjectArea(providerClone));
		// ensure we did not clone reference
		LanguageSettingsManager.setStoringEntriesInProjectArea(provider1, !LanguageSettingsManager.isStoringEntriesInProjectArea(providerClone));
		assertFalse(LanguageSettingsManager.isStoringEntriesInProjectArea(provider1) == LanguageSettingsManager.isStoringEntriesInProjectArea(providerClone));

		assertEquals(provider1.getLanguageScope().get(0), providerClone.getLanguageScope().get(0));

		List<ICLanguageSettingEntry> actual1 = providerClone.getSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID);
		assertNotSame(sampleEntries_1, actual1);
		assertEquals(sampleEntries_1.get(0), actual1.get(0));
		assertEquals(sampleEntries_1.get(1), actual1.get(1));
		assertEquals(sampleEntries_1.get(2), actual1.get(2));
		assertEquals(sampleEntries_1.size(), actual1.size());

		List<ICLanguageSettingEntry> actual2 = providerClone.getSettingEntries(null, null, LANG_ID);
		assertNotSame(sampleEntries_2, actual2);
		assertEquals(sampleEntries_2.get(0), actual2.get(0));
		assertEquals(sampleEntries_2.size(), actual2.size());
	}

	/**
	 * Test shallow clone.
	 */
	public void testCloneShallow() throws Exception {
		// define sample data
		List<String> sampleLanguages = new ArrayList<String>();
		sampleLanguages.add(LANG_ID);

		// create a model provider
		class MockSerializableProvider extends LanguageSettingsSerializableProvider implements Cloneable {
			public MockSerializableProvider(String id, String name) {
				super(id, name);
			}
			@Override
			public MockSerializableProvider cloneShallow() throws CloneNotSupportedException {
				return (MockSerializableProvider) super.cloneShallow();
			}

		}
		MockSerializableProvider provider1 = new MockSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
		provider1.setLanguageScope(sampleLanguages);
		provider1.setProperty(ATTR_PARAMETER, VALUE_PARAMETER);
		assertEquals(false, LanguageSettingsManager.isStoringEntriesInProjectArea(provider1));
		LanguageSettingsManager.setStoringEntriesInProjectArea(provider1, true);

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path", 1));
		provider1.setSettingEntries(null, null, null, entries);

		// clone provider
		MockSerializableProvider providerClone = provider1.cloneShallow();
		assertNotSame(provider1, providerClone);
		assertFalse(provider1.equals(providerClone));
		assertTrue(provider1.getClass()==providerClone.getClass());
		assertEquals(provider1.getProperty(ATTR_PARAMETER), providerClone.getProperty(ATTR_PARAMETER));
		assertEquals(LanguageSettingsManager.isStoringEntriesInProjectArea(provider1), LanguageSettingsManager.isStoringEntriesInProjectArea(providerClone));
		assertEquals(provider1.getLanguageScope().get(0), providerClone.getLanguageScope().get(0));

		List<ICLanguageSettingEntry> actual = providerClone.getSettingEntries(null, null, null);
		assertNull(actual);
	}

	/**
	 * Verify that entries are sorted by kinds.
	 */
	public void testSort_Kinds() throws Exception {
		// create sample entries
		CIncludePathEntry includePathEntry1 = new CIncludePathEntry("path1", 0);
		CIncludePathEntry includePathEntry2 = new CIncludePathEntry("path2", 0);
		CMacroEntry macroEntry1 = new CMacroEntry("MACRO1", null, 0);
		CMacroEntry macroEntry2 = new CMacroEntry("MACRO2", null, 0);
		CIncludeFileEntry includeFileEntry1 = new CIncludeFileEntry("file1", 0);
		CIncludeFileEntry includeFileEntry2 = new CIncludeFileEntry("file2", 0);
		CMacroFileEntry macroFileEntry1 = new CMacroFileEntry("file1", 0);
		CMacroFileEntry macroFileEntry2 = new CMacroFileEntry("file2", 0);
		CLibraryPathEntry libraryPathEntry1 = new CLibraryPathEntry("lib1", 0);
		CLibraryPathEntry libraryPathEntry2 = new CLibraryPathEntry("lib2", 0);
		CLibraryFileEntry libraryFileEntry1 = new CLibraryFileEntry("file1", 0);
		CLibraryFileEntry libraryFileEntry2 = new CLibraryFileEntry("file2", 0);

		// place entries in unsorted list
		List<ICLanguageSettingEntry> unsortedEntries = new ArrayList<ICLanguageSettingEntry>();
		unsortedEntries.add(macroEntry1);
		unsortedEntries.add(macroFileEntry1);
		unsortedEntries.add(macroEntry2);
		unsortedEntries.add(includePathEntry1);
		unsortedEntries.add(includeFileEntry1);
		unsortedEntries.add(macroFileEntry2);
		unsortedEntries.add(libraryFileEntry1);
		unsortedEntries.add(includeFileEntry2);
		unsortedEntries.add(libraryFileEntry2);
		unsortedEntries.add(libraryPathEntry1);
		unsortedEntries.add(includePathEntry2);
		unsortedEntries.add(libraryPathEntry2);

		// create a provider and set the entries
		LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
		provider.setSettingEntries(null, null, null, unsortedEntries);

		// retrieve and check that language settings got sorted properly
		int i=0;
		List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
		assertEquals(includePathEntry1, actual.get(i++));
		assertEquals(includePathEntry2, actual.get(i++));
		assertEquals(includeFileEntry1, actual.get(i++));
		assertEquals(includeFileEntry2, actual.get(i++));
		assertEquals(macroEntry1, actual.get(i++));
		assertEquals(macroEntry2, actual.get(i++));
		assertEquals(macroFileEntry1, actual.get(i++));
		assertEquals(macroFileEntry2, actual.get(i++));
		assertEquals(libraryPathEntry1, actual.get(i++));
		assertEquals(libraryPathEntry2, actual.get(i++));
		assertEquals(libraryFileEntry1, actual.get(i++));
		assertEquals(libraryFileEntry2, actual.get(i++));

		assertEquals(unsortedEntries.size(), actual.size());
	}

	/**
	 * Check how entries are sorted inside a kind.
	 */
	public void testSort_Entries() throws Exception {
		// create sample entries
		CIncludePathEntry includePathEntry1 = new CIncludePathEntry("path_B", 0);
		CIncludePathEntry includePathEntry2 = new CIncludePathEntry("path_A", 0);
		CMacroEntry macroEntry1 = new CMacroEntry("MACRO_A", null, 0);
		CMacroEntry macroEntry2 = new CMacroEntry("MACRO_B", null, 0);
		CIncludeFileEntry includeFileEntry1 = new CIncludeFileEntry("file_B", 0);
		CIncludeFileEntry includeFileEntry2 = new CIncludeFileEntry("file_A", 0);
		CMacroFileEntry macroFileEntry1 = new CMacroFileEntry("file_B", 0);
		CMacroFileEntry macroFileEntry2 = new CMacroFileEntry("file_A", 0);
		CLibraryPathEntry libraryPathEntry1 = new CLibraryPathEntry("lib_B", 0);
		CLibraryPathEntry libraryPathEntry2 = new CLibraryPathEntry("lib_A", 0);
		CLibraryFileEntry libraryFileEntry1 = new CLibraryFileEntry("file_B", 0);
		CLibraryFileEntry libraryFileEntry2 = new CLibraryFileEntry("file_A", 0);

		// place entries in unsorted list
		List<ICLanguageSettingEntry> unsortedEntries = new ArrayList<ICLanguageSettingEntry>();
		// macros will be sorted by name
		unsortedEntries.add(macroEntry2);
		unsortedEntries.add(macroEntry1);
		// paths are not sorted only grouped by kind
		unsortedEntries.add(macroFileEntry1);
		unsortedEntries.add(macroFileEntry2);
		unsortedEntries.add(includePathEntry1);
		unsortedEntries.add(includePathEntry2);
		unsortedEntries.add(includeFileEntry1);
		unsortedEntries.add(includeFileEntry2);
		unsortedEntries.add(libraryFileEntry1);
		unsortedEntries.add(libraryFileEntry2);
		unsortedEntries.add(libraryPathEntry1);
		unsortedEntries.add(libraryPathEntry2);

		// create a provider and set the entries
		LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
		provider.setSettingEntries(null, null, null, unsortedEntries);

		// retrieve and check that language settings got sorted properly
		int i=0;
		List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
		assertEquals(includePathEntry1, actual.get(i++));
		assertEquals(includePathEntry2, actual.get(i++));
		assertEquals(includeFileEntry1, actual.get(i++));
		assertEquals(includeFileEntry2, actual.get(i++));
		assertEquals(macroEntry1, actual.get(i++));
		assertEquals(macroEntry2, actual.get(i++));
		assertEquals(macroFileEntry1, actual.get(i++));
		assertEquals(macroFileEntry2, actual.get(i++));
		assertEquals(libraryPathEntry1, actual.get(i++));
		assertEquals(libraryPathEntry2, actual.get(i++));
		assertEquals(libraryFileEntry1, actual.get(i++));
		assertEquals(libraryFileEntry2, actual.get(i++));

		assertEquals(unsortedEntries.size(), actual.size());
	}

	/**
	 * Sorting including undefined entries.
	 */
	public void testSort_Undef() throws Exception {
		// create sample entries
		CMacroEntry macroEntry1 = new CMacroEntry("MACRO_1", null, 0);
		CMacroEntry macroEntry2A = new CMacroEntry("MACRO_2", null, ICSettingEntry.UNDEFINED);
		CMacroEntry macroEntry2B = new CMacroEntry("MACRO_2", null, 0);
		CMacroEntry macroEntry2C = new CMacroEntry("MACRO_2", null, ICSettingEntry.BUILTIN);
		CMacroEntry macroEntry3 = new CMacroEntry("MACRO_3", null, 0);

		// place entries in unsorted list
		List<ICLanguageSettingEntry> unsortedEntries = new ArrayList<ICLanguageSettingEntry>();
		// macros will be sorted by name and keep order for the same name
		unsortedEntries.add(macroEntry2A);
		unsortedEntries.add(macroEntry3);
		unsortedEntries.add(macroEntry2B);
		unsortedEntries.add(macroEntry1);
		unsortedEntries.add(macroEntry2C);

		// create a provider and set the entries
		LanguageSettingsSerializableProvider provider = new LanguageSettingsSerializableProvider(PROVIDER_1, PROVIDER_NAME_1);
		provider.setSettingEntries(null, null, null, unsortedEntries);

		// retrieve and check that language settings got sorted properly
		int i=0;
		List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
		assertEquals(macroEntry1, actual.get(i++));
		assertEquals(macroEntry2A, actual.get(i++));
		assertEquals(macroEntry2B, actual.get(i++));
		assertEquals(macroEntry2C, actual.get(i++));
		assertEquals(macroEntry3, actual.get(i++));

		assertEquals(unsortedEntries.size(), actual.size());
	}

}
