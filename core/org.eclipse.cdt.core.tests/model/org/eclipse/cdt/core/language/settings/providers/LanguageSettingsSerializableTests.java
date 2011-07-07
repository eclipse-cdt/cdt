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

import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.testplugin.CModelMock;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsSerializableTests extends TestCase {
	// Should match id of extension point defined in plugin.xml
	private static final String EXTENSION_SERIALIZABLE_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.serializable.language.settings.provider";

	private static final String CFG_ID = "test.configuration.id";
	private static final String CFG_ID_1 = "test.configuration.id.1";
	private static final String CFG_ID_2 = "test.configuration.id.2";
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
	private static final String CUSTOM_PARAMETER = "custom.parameter";

	private static final String ELEM_TEST = "test";

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsSerializableTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsSerializableTests.class);
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
	public void testProvider() throws Exception {
		// benchmark data
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<String> languages = new ArrayList<String>();
		languages.add(LANG_ID);

		// create a provider
		LanguageSettingsSerializable mockProvider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
		// test isEmpty()
		assertTrue(mockProvider.isEmpty());
		
		// test setters and getters
		mockProvider.setId(PROVIDER_2);
		assertEquals(PROVIDER_2, mockProvider.getId());
		mockProvider.setName(PROVIDER_NAME_2);
		assertEquals(PROVIDER_NAME_2, mockProvider.getName());
		mockProvider.setCustomParameter(CUSTOM_PARAMETER);
		assertEquals(CUSTOM_PARAMETER, mockProvider.getCustomParameter());
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
	 */
	public void testNoProviders() throws Exception {
		// serialize language settings of user defined providers (on workspace level)
		LanguageSettingsProvidersSerializer.serializeLanguageSettingsWorkspace();
		LanguageSettingsProvidersSerializer.loadLanguageSettingsWorkspace();

		// test passes if no exception was thrown
	}

	/**
	 */
	public void testEmptyProvider() throws Exception {
		Element elementProvider;
		{
			// create null provider
			LanguageSettingsSerializable providerNull = new LanguageSettingsSerializable(PROVIDER_NULL, PROVIDER_NAME_NULL);
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_NULL, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertNull(actual);
		}
	}

	/**
	 */
	public void testCustomParameter() throws Exception {
		Element elementProvider;
		{
			// create provider with custom parameter
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setCustomParameter(CUSTOM_PARAMETER);
			assertEquals(CUSTOM_PARAMETER, provider.getCustomParameter());

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);
			assertTrue(xmlString.contains(CUSTOM_PARAMETER));
		}
		{
			// re-load and check custom parameter of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(CUSTOM_PARAMETER, provider.getCustomParameter());
		}
	}

	/**
	 */
	public void testLanguages() throws Exception {
		List<String> expectedLanguageIds = new ArrayList<String>();
		expectedLanguageIds.add(LANG_ID);
		expectedLanguageIds.add(LANG_ID_1);

		Element elementProvider;
		{
			// create provider with custom language scope
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			List<String> actualIds = provider.getLanguageScope();
			assertEquals(expectedLanguageIds.get(0), actualIds.get(0));
			assertEquals(expectedLanguageIds.get(1), actualIds.get(1));
			assertEquals(expectedLanguageIds.size(), actualIds.size());
		}
	}
	
	/**
	 */
	public void testLanguageScope() throws Exception {
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
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
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
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
	 */
	public void testNullConfiguration() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, MOCK_RC, LANG_ID, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "configuration" element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<configuration")); // LanguageSettingsSerializable.ELEM_CONFIGURATION;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, LANG_ID);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 */
	public void testNullLanguage() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(MOCK_CFG, MOCK_RC, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "language" element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<language")); // LanguageSettingsSerializable.ELEM_LANGUAGE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(MOCK_CFG, MOCK_RC, null);
			assertEquals(entries.get(0), actual.get(0));
		}
	}


	/**
	 */
	public void testNullLanguageScope() throws Exception {
		// define benchmark entries
		List<ICLanguageSettingEntry> entriesNullLanguage = new ArrayList<ICLanguageSettingEntry>();
		entriesNullLanguage.add(new CIncludePathEntry("path_null", 0));
		List<ICLanguageSettingEntry> entriesLanguage = new ArrayList<ICLanguageSettingEntry>();
		entriesLanguage.add(new CIncludePathEntry("path", 0));
		
		Element elementProvider;

		{
			// create a provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
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
			assertTrue(xmlString.contains("<language")); // LanguageSettingsSerializable.ELEM_LANGUAGE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
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
	 */
	public void testNullResource() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(MOCK_CFG, null, LANG_ID, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that "resource" element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<resource")); // LanguageSettingsSerializable.ELEM_RESOURCE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(MOCK_CFG, null, LANG_ID);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 */
	public void testNullConfigurationLanguage() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, MOCK_RC, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<configuration")); // LanguageSettingsSerializable.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains("<language")); // LanguageSettingsSerializable.ELEM_LANGUAGE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, MOCK_RC, null);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 */
	public void testNullConfigurationResource() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, LANG_ID, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<configuration")); // LanguageSettingsSerializable.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains("<resource")); // LanguageSettingsSerializable.ELEM_RESOURCE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, LANG_ID);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 */
	public void testNullLanguageResource() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(MOCK_CFG, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<language")); // LanguageSettingsSerializable.ELEM_LANGUAGE;
			assertFalse(xmlString.contains("<resource")); // LanguageSettingsSerializable.ELEM_RESOURCE;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(MOCK_CFG, null, null);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 */
	public void testNullConfigurationLanguageResourceFlag() throws Exception {
		// provider/configuration/language/resource/settingEntry
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		int flag = 0;
		entries.add(new CIncludePathEntry("path0", flag));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);

			// verify that element is collapsed and not saved in XML
			String xmlString = XmlUtil.toString(doc);
			assertFalse(xmlString.contains("<configuration")); // LanguageSettingsSerializable.ELEM_CONFIGURATION;
			assertFalse(xmlString.contains("<language")); // LanguageSettingsSerializable.ELEM_LANGUAGE;
			assertFalse(xmlString.contains("<resource")); // LanguageSettingsSerializable.ELEM_RESOURCE;
			assertFalse(xmlString.contains("<flag")); // LanguageSettingsSerializable.ELEM_FLAG;
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
		}
	}

	/**
	 */
	public void testCIncludePathEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
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
	 */
	public void testCIncludeFileEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludeFileEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
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
	 */
	public void testCMacroEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CMacroEntry("MACRO0", "value0",1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
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
	 */
	public void testCMacroFileEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CMacroFileEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
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
	 */
	public void testCLibraryPathEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CLibraryPathEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
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
	 */
	public void testCLibraryFileEntry() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CLibraryFileEntry("name", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
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
	 */
	public void testMixedSettingEntries() throws Exception {
		Element elementProvider;
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CMacroEntry("MACRO0", "value0",1));
		entries.add(new CIncludePathEntry("path0", 1));
		entries.add(new CIncludePathEntry("path1", 1));
		{
			// create a provider and serialize its settings
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			provider.setSettingEntries(null, null, null, entries);

			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = provider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable provider = new LanguageSettingsSerializable(elementProvider);
			assertEquals(PROVIDER_1, provider.getId());

			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.get(1), actual.get(1));
			assertEquals(entries.get(2), actual.get(2));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 */
	public void testLanguageAndNull() throws Exception {
		Element elementProvider = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> entries2 = new ArrayList<ICLanguageSettingEntry>();
		entries2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			LanguageSettingsSerializable mockProvider = null;
			mockProvider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			mockProvider.setSettingEntries(null, null, null, entries);
			mockProvider.setSettingEntries(null, null, LANG_ID, entries2);

			// serialize language settings to DOM
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = mockProvider.serialize(rootElement);
		}
		{
			// re-load and check language settings of the newly loaded provider
			LanguageSettingsSerializable loadedProvider = new LanguageSettingsSerializable(elementProvider);

			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(null, null, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			List<ICLanguageSettingEntry> actual2 = loadedProvider.getSettingEntries(null, null, LANG_ID);
			assertEquals(entries2.get(0), actual2.get(0));
			assertEquals(entries2.size(), actual2.size());
		}
	}

	/**
	 */
	public void testTwoLanguages() throws Exception {
		Element elementProvider = null;

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<ICLanguageSettingEntry> entries2 = new ArrayList<ICLanguageSettingEntry>();
		entries2.add(new CIncludePathEntry("path2", 0));

		{
			// create a provider
			LanguageSettingsSerializable mockProvider = null;
			mockProvider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
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
			LanguageSettingsSerializable loadedProvider = new LanguageSettingsSerializable(elementProvider);

			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(null, null, LANG_ID_1);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			List<ICLanguageSettingEntry> actual2 = loadedProvider.getSettingEntries(null, null, LANG_ID_2);
			assertEquals(entries2.get(0), actual2.get(0));
			assertEquals(entries2.size(), actual2.size());
		}
	}

	/**
	 */
	public void testTwoResources() throws Exception {
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
			LanguageSettingsSerializable mockProvider = null;
			mockProvider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
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
			LanguageSettingsSerializable loadedProvider = new LanguageSettingsSerializable(elementProvider);

			List<ICLanguageSettingEntry> actual = loadedProvider.getSettingEntries(null, rc1, null);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());

			List<ICLanguageSettingEntry> actual2 = loadedProvider.getSettingEntries(null, rc2, null);
			assertEquals(entries2.get(0), actual2.get(0));
			assertEquals(entries2.size(), actual2.size());
		}
	}

	/**
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
		LanguageSettingsSerializable provider = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);

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
	 */
	public void testEquals() throws Exception {
		List<ICLanguageSettingEntry> sampleEntries_1 = new ArrayList<ICLanguageSettingEntry>();
		sampleEntries_1.add(new CMacroEntry("MACRO0", "value0",1));
		sampleEntries_1.add(new CIncludePathEntry("path0", 1));
		sampleEntries_1.add(new CIncludePathEntry("path1", 1));

		List<ICLanguageSettingEntry> sampleEntries_2 = new ArrayList<ICLanguageSettingEntry>();
		sampleEntries_2.add(new CIncludePathEntry("path0", 1));

		List<String> sampleLanguages = new ArrayList<String>();
		sampleLanguages.add(LANG_ID);

		// create a model provider
		LanguageSettingsSerializable provider1 = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
		provider1.setLanguageScope(sampleLanguages);
		provider1.setCustomParameter(CUSTOM_PARAMETER);
		provider1.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, sampleEntries_1);
		provider1.setSettingEntries(null, null, LANG_ID, sampleEntries_2);

		{
			// create another provider with the same data
			LanguageSettingsSerializable provider2 = new LanguageSettingsSerializable(PROVIDER_1, PROVIDER_NAME_1);
			assertFalse(provider1.equals(provider2));
			assertFalse(provider1.hashCode()==provider2.hashCode());

			provider2.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, sampleEntries_1);
			assertFalse(provider1.equals(provider2));
			assertFalse(provider1.hashCode()==provider2.hashCode());

			provider2.setSettingEntries(null, null, LANG_ID, sampleEntries_2);
			assertFalse(provider1.equals(provider2));
			assertFalse(provider1.hashCode()==provider2.hashCode());

			provider2.setLanguageScope(sampleLanguages);
			assertFalse(provider1.equals(provider2));
			assertFalse(provider1.hashCode()==provider2.hashCode());

			provider2.setCustomParameter(CUSTOM_PARAMETER);
			assertTrue(provider1.equals(provider2));
			assertTrue(provider1.hashCode()==provider2.hashCode());

			// check different ID
			provider2.setId(PROVIDER_2);
			assertFalse(provider1.equals(provider2));
			assertFalse(provider1.hashCode()==provider2.hashCode());
		}

		{
			// check that subclasses are not equal
			LanguageSettingsSerializable providerSub1 = new LanguageSettingsSerializable() {};
			LanguageSettingsSerializable providerSub2 = new LanguageSettingsSerializable() {};
			assertFalse(providerSub1.equals(providerSub2));
			assertFalse(providerSub1.hashCode()==providerSub2.hashCode());
		}
	}

	/**
	 */
	public void testClone() throws Exception {
		// define sample data
		List<ICLanguageSettingEntry> sampleEntries_1 = new ArrayList<ICLanguageSettingEntry>();
		sampleEntries_1.add(new CMacroEntry("MACRO0", "value0",1));
		sampleEntries_1.add(new CIncludePathEntry("path0", 1));
		sampleEntries_1.add(new CIncludePathEntry("path1", 1));

		List<ICLanguageSettingEntry> sampleEntries_2 = new ArrayList<ICLanguageSettingEntry>();
		sampleEntries_2.add(new CIncludePathEntry("path0", 1));

		List<String> sampleLanguages = new ArrayList<String>();
		sampleLanguages.add(LANG_ID);

		// create a model provider
		class LanguageSettingsSerializableMock extends LanguageSettingsSerializable implements Cloneable {
			public LanguageSettingsSerializableMock(String id, String name) {
				super(id, name);
			}
			@Override
			public LanguageSettingsSerializableMock clone() throws CloneNotSupportedException {
				return (LanguageSettingsSerializableMock) super.clone();
			}

		}
		LanguageSettingsSerializableMock provider1 = new LanguageSettingsSerializableMock(PROVIDER_1, PROVIDER_NAME_1);
		provider1.setLanguageScope(sampleLanguages);
		provider1.setCustomParameter(CUSTOM_PARAMETER);
		provider1.setSettingEntries(MOCK_CFG, MOCK_RC, LANG_ID, sampleEntries_1);
		provider1.setSettingEntries(null, null, LANG_ID, sampleEntries_2);

		// clone provider
		LanguageSettingsSerializableMock providerClone = provider1.clone();
		assertNotSame(provider1, providerClone);
		assertTrue(provider1.equals(providerClone));
		assertTrue(provider1.getClass()==providerClone.getClass());
		assertEquals(provider1.getCustomParameter(), providerClone.getCustomParameter());
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
	 */
	public void testCloneShallow() throws Exception {
		// define sample data
		List<String> sampleLanguages = new ArrayList<String>();
		sampleLanguages.add(LANG_ID);
		
		// create a model provider
		class LanguageSettingsSerializableMock extends LanguageSettingsSerializable implements Cloneable {
			public LanguageSettingsSerializableMock(String id, String name) {
				super(id, name);
			}
			@Override
			public LanguageSettingsSerializableMock cloneShallow() throws CloneNotSupportedException {
				return (LanguageSettingsSerializableMock) super.cloneShallow();
			}
			
		}
		LanguageSettingsSerializableMock provider1 = new LanguageSettingsSerializableMock(PROVIDER_1, PROVIDER_NAME_1);
		provider1.setLanguageScope(sampleLanguages);
		provider1.setCustomParameter(CUSTOM_PARAMETER);
		
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path", 1));
		provider1.setSettingEntries(null, null, null, entries);
		
		// clone provider
		LanguageSettingsSerializableMock providerClone = provider1.cloneShallow();
		assertNotSame(provider1, providerClone);
		assertFalse(provider1.equals(providerClone));
		assertTrue(provider1.getClass()==providerClone.getClass());
		assertEquals(provider1.getCustomParameter(), providerClone.getCustomParameter());
		assertEquals(provider1.getLanguageScope().get(0), providerClone.getLanguageScope().get(0));
		
		List<ICLanguageSettingEntry> actual = providerClone.getSettingEntries(null, null, null);
		assertNull(actual);
	}
}


