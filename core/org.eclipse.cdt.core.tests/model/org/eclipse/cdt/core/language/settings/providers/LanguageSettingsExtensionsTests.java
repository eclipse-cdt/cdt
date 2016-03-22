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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing LanguageSettingsProvider extensions
 */
public class LanguageSettingsExtensionsTests extends BaseTestCase {
	// These should match corresponding entries defined in plugin.xml
	/*package*/ static final String EXTENSION_BASE_PROVIDER_ID = "org.eclipse.cdt.core.tests.language.settings.base.provider";
	/*package*/ static final String EXTENSION_BASE_PROVIDER_NAME = "Test Plugin Mock Language Settings Base Provider";
	/*package*/ static final String EXTENSION_BASE_PROVIDER_LANG_ID = "org.eclipse.cdt.core.tests.language.id";
	/*package*/ static final String EXTENSION_BASE_PROVIDER_PARAMETER = "custom parameter";
	/*package*/ static final String EXTENSION_BASE_PROVIDER_ATTR_PARAMETER = "parameter";
	/*package*/ static final String EXTENSION_CUSTOM_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.language.settings.provider";
	/*package*/ static final String EXTENSION_CUSTOM_PROVIDER_NAME = "Test Plugin Mock Language Settings Provider";
	/*package*/ static final String EXTENSION_BASE_SUBCLASS_PROVIDER_ID = "org.eclipse.cdt.core.tests.language.settings.base.provider.subclass";
	/*package*/ static final String EXTENSION_BASE_SUBCLASS_PROVIDER_PARAMETER = "custom parameter subclass";
	/*package*/ static final String EXTENSION_SERIALIZABLE_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.serializable.language.settings.provider";
	/*package*/ static final String EXTENSION_SERIALIZABLE_PROVIDER_NAME = "Test Plugin Mock Serializable Language Settings Provider";
	/*package*/ static final String EXTENSION_SERIALIZABLE_PROVIDER_MISSING_PARAMETER = "parameter";
	/*package*/ static final String EXTENSION_EDITABLE_PROVIDER_ID = "org.eclipse.cdt.core.tests.custom.editable.language.settings.provider";
	/*package*/ static final String EXTENSION_EDITABLE_PROVIDER_NAME = "Test Plugin Mock Editable Language Settings Provider";
	/*package*/ static final ICLanguageSettingEntry EXTENSION_SERIALIZABLE_PROVIDER_ENTRY = new CMacroEntry("MACRO", "value", 0);
	/*package*/ static final ICLanguageSettingEntry EXTENSION_EDITABLE_PROVIDER_ENTRY = new CMacroEntry("MACRO", "value", 0);
	/*package*/ static final String EXTENSION_REGISTERER_PROVIDER_ID = "org.eclipse.cdt.core.tests.language.settings.listener.registerer.provider";

	// Arbitrary sample parameters used by the test
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String LANG_ID = "test.lang.id";
	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));

	// Constants used in LanguageSettingsSerializableProvider
	private static final String ATTR_ID = LanguageSettingsProvidersSerializer.ATTR_ID;
	private static final String ATTR_NAME = LanguageSettingsProvidersSerializer.ATTR_NAME;
	private static final String ATTR_CLASS = LanguageSettingsProvidersSerializer.ATTR_CLASS;

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsExtensionsTests(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * @return - new TestSuite.
	 */
	public static TestSuite suite() {
		return new TestSuite(LanguageSettingsExtensionsTests.class);
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
	 * Check that regular {@link ICLanguageSettingsProvider} extension defined in plugin.xml is accessible.
	 */
	public void testExtension() throws Exception {
		{
			// test provider defined as an extension
			List<ILanguageSettingsProvider> providers = LanguageSettingsManager.getWorkspaceProviders();
			List<String> ids = new ArrayList<String>();
			for (ILanguageSettingsProvider provider : providers) {
				ids.add(provider.getId());
			}
			assertTrue("extension " + EXTENSION_BASE_PROVIDER_ID + " not found", ids.contains(EXTENSION_BASE_PROVIDER_ID));
		}

		{
			// test provider that is not in the list
			ILanguageSettingsProvider providerExt = LanguageSettingsManager.getExtensionProviderCopy("missing.povider", true);
			assertNull(providerExt);
		}

		// this extension provider is not copyable
		ILanguageSettingsProvider providerExtCopy = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_BASE_PROVIDER_ID, true);
		assertNull(providerExtCopy);

		// test raw workspace provider defined as an extension
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);
		assertTrue(LanguageSettingsManager.isWorkspaceProvider(providerExt));
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(providerExt);
		assertTrue(rawProvider instanceof LanguageSettingsBaseProvider);
		LanguageSettingsBaseProvider provider = (LanguageSettingsBaseProvider)rawProvider;
		assertEquals(EXTENSION_BASE_PROVIDER_ID, provider.getId());
		assertEquals(EXTENSION_BASE_PROVIDER_NAME, provider.getName());
		assertEquals(EXTENSION_BASE_PROVIDER_PARAMETER, provider.getProperty(EXTENSION_BASE_PROVIDER_ATTR_PARAMETER));
		// these attributes are not exposed as properties
		assertEquals("", provider.getProperty(ATTR_ID));
		assertEquals("", provider.getProperty(ATTR_NAME));
		assertEquals("", provider.getProperty(ATTR_CLASS));

		// attempt to get entries for wrong language
		assertNull(provider.getSettingEntries(null, FILE_0, LANG_ID));

		// benchmarks matching extension point definition
		List<ICLanguageSettingEntry> entriesExt = new ArrayList<ICLanguageSettingEntry>();
		entriesExt.add(new CIncludePathEntry("/usr/include/",
				ICSettingEntry.BUILTIN
				| ICSettingEntry.LOCAL
				| ICSettingEntry.RESOLVED
				| ICSettingEntry.VALUE_WORKSPACE_PATH
				| ICSettingEntry.UNDEFINED
		));
		entriesExt.add(new CMacroEntry("TEST_DEFINE", "100", 0));
		entriesExt.add(new CIncludeFileEntry("/include/file.inc", 0));
		entriesExt.add(new CLibraryPathEntry("/usr/lib/", 0));
		entriesExt.add(new CLibraryFileEntry("libdomain.a", 0));
		entriesExt.add(new CMacroFileEntry("/macro/file.mac", 0));

		// retrieve entries from extension point
		List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, EXTENSION_BASE_PROVIDER_LANG_ID);
		for (int i=0;i<entriesExt.size();i++) {
			assertEquals("i="+i, entriesExt.get(i), actual.get(i));
		}
		assertEquals(entriesExt.size(), actual.size());
	}

	/**
	 * Check that subclassed {@link LanguageSettingsBaseProvider} extension defined in plugin.xml is accessible.
	 */
	public void testExtensionBaseProviderSubclass() throws Exception {
		// get test plugin extension provider
		ILanguageSettingsProvider providerExtCopy = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_BASE_SUBCLASS_PROVIDER_ID, true);
		assertNull(providerExtCopy);
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_SUBCLASS_PROVIDER_ID);
		assertTrue(LanguageSettingsManager.isWorkspaceProvider(providerExt));

		// get raw extension provider
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(providerExt);
		assertTrue(rawProvider instanceof MockLanguageSettingsBaseProvider);
		MockLanguageSettingsBaseProvider provider = (MockLanguageSettingsBaseProvider)rawProvider;
		assertEquals(EXTENSION_BASE_SUBCLASS_PROVIDER_ID, provider.getId());
		assertEquals(EXTENSION_BASE_SUBCLASS_PROVIDER_PARAMETER, provider.getCustomParameter());

		// Test for null languages
		assertNull(provider.getLanguageScope());

		// benchmarks matching extension point definition
		List<ICLanguageSettingEntry> entriesExt = new ArrayList<ICLanguageSettingEntry>();
		entriesExt.add(new CIncludePathEntry("/usr/include/", ICSettingEntry.BUILTIN));

		// retrieve entries from extension point
		List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, LANG_ID);
		for (int i=0;i<entriesExt.size();i++) {
			assertEquals("i="+i, entriesExt.get(i), actual.get(i));
		}
		assertEquals(entriesExt.size(), actual.size());
	}

	/**
	 * Make sure extensions contributed through extension point created with proper ID/name.
	 */
	public void testExtensionCustomProvider() throws Exception {
		// get test plugin extension non-default provider
		ILanguageSettingsProvider providerExtCopy = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_CUSTOM_PROVIDER_ID, true);
		assertNull(providerExtCopy);
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_CUSTOM_PROVIDER_ID);
		assertTrue(LanguageSettingsManager.isWorkspaceProvider(providerExt));

		// get raw extension provider
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(providerExt);
		assertTrue(rawProvider instanceof MockLanguageSettingsProvider);

		assertEquals(EXTENSION_CUSTOM_PROVIDER_ID, rawProvider.getId());
		assertEquals(EXTENSION_CUSTOM_PROVIDER_NAME, rawProvider.getName());
	}

	/**
	 * Basic test for {@link LanguageSettingsBaseProvider}.
	 */
	public void testBaseProvider() throws Exception {
		// define benchmarks
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<String> languages = new ArrayList<String>(2);
		languages.add("bogus.language.id");
		languages.add(LANG_ID);

		// create base provider
		LanguageSettingsBaseProvider provider = new LanguageSettingsBaseProvider(PROVIDER_0, PROVIDER_NAME_0, languages, entries);

		{
			// attempt to get entries for wrong language
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, "wrong.lang.id");
			assertNull(actual);
		}

		{
			// retrieve the entries
			List<ICLanguageSettingEntry> actual = provider.getSettingEntries(null, FILE_0, LANG_ID);
			assertEquals(entries.get(0), actual.get(0));
			assertNotSame(entries, actual);
			// retrieve languages
			List<String> actualLanguageIds = provider.getLanguageScope();
			for (String languageId: languages) {
				assertTrue(actualLanguageIds.contains(languageId));
			}
			assertEquals(languages.size(), actualLanguageIds.size());
		}
	}

	/**
	 * Test ability to configure {@link LanguageSettingsBaseProvider}.
	 */
	public void testBaseProviderConfigure() throws Exception {
		// sample entries
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("/usr/include/", 0));
		List<String> languages = new ArrayList<String>();
		languages.add(LANG_ID);
		// create LanguageSettingsBaseProvider
		LanguageSettingsBaseProvider provider1 = new LanguageSettingsBaseProvider();
		LanguageSettingsBaseProvider provider2 = new LanguageSettingsBaseProvider();
		{
			// configure provider1
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("key1", "value1");
			properties.put("key2", null);
			properties.put("key3", "");
			properties.put("key4", "false");
			provider1.configureProvider(PROVIDER_0, PROVIDER_NAME_0, languages, entries, properties);
			assertEquals(PROVIDER_0, provider1.getId());
			assertEquals(PROVIDER_NAME_0, provider1.getName());
			assertEquals(languages, provider1.getLanguageScope());
			assertEquals(entries, provider1.getSettingEntries(null, null, LANG_ID));
			assertEquals("value1", provider1.getProperty("key1"));
			assertEquals("", provider1.getProperty("key2"));
			assertEquals("", provider1.getProperty("key3"));
			assertEquals("false", provider1.getProperty("key4"));
			assertEquals(false, provider1.getPropertyBool("key4"));
			assertEquals("", provider1.getProperty("keyX"));
			assertEquals(false, provider1.getPropertyBool("keyX"));
		}
		{
			// configure provider2
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("key1", "value1");
			provider2.configureProvider(PROVIDER_0, PROVIDER_NAME_0, languages, entries, properties);
			assertEquals(PROVIDER_0, provider2.getId());
			assertEquals(PROVIDER_NAME_0, provider2.getName());
			assertEquals(languages, provider2.getLanguageScope());
			assertEquals(entries, provider2.getSettingEntries(null, null, LANG_ID));
			assertEquals("value1", provider2.getProperty("key1"));
			assertEquals("", provider2.getProperty("keyX"));
			assertEquals(false, provider2.getPropertyBool("keyX"));
		}
		// test equality
		assertTrue(provider1.equals(provider2));
	}

	/**
	 * {@link LanguageSettingsBaseProvider} is not allowed to be configured twice.
	 */
	public void testBaseProviderCantReconfigure() throws Exception {
		// create LanguageSettingsBaseProvider
		LanguageSettingsBaseProvider provider = new LanguageSettingsBaseProvider();
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("/usr/include/", 0));
		// configure it
		provider.configureProvider("test.id", "test.name", null, entries, null);

		try {
			// attempt to configure it twice should fail
			provider.configureProvider("test.id", "test.name", null, entries, null);
			fail("LanguageSettingsBaseProvider is not allowed to be configured twice");
		} catch (UnsupportedOperationException e) {
		}
	}

	/**
	 * Test {@link LanguageSettingsSerializableProvider} defined via extension point.
	 */
	public void testSerializableProvider() throws Exception {
		// get test plugin extension for serializable provider
		ILanguageSettingsProvider providerExtCopy = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_SERIALIZABLE_PROVIDER_ID, true);
		assertNull(providerExtCopy);
		ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
		assertTrue(LanguageSettingsManager.isWorkspaceProvider(providerExt));

		// get raw extension provider
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(providerExt);
		assertTrue(rawProvider instanceof LanguageSettingsSerializableProvider);
		LanguageSettingsSerializableProvider provider = (LanguageSettingsSerializableProvider) rawProvider;

		assertEquals(null, provider.getLanguageScope());
		assertEquals("", provider.getProperty(EXTENSION_SERIALIZABLE_PROVIDER_MISSING_PARAMETER));

		List<ICLanguageSettingEntry> expected = new ArrayList<ICLanguageSettingEntry>();
		expected.add(EXTENSION_EDITABLE_PROVIDER_ENTRY);
		assertEquals(expected, provider.getSettingEntries(null, null, null));
	}

	/**
	 * Test {@link ILanguageSettingsEditableProvider} defined via extension point.
	 */
	public void testEditableProvider() throws Exception {
		// Non-editable providers cannot be copied so they are singletons
		{
			// get test plugin extension for serializable provider
			ILanguageSettingsProvider providerExtCopy = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_SERIALIZABLE_PROVIDER_ID, true);
			assertNull(providerExtCopy);
			ILanguageSettingsProvider providerExt = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
			assertTrue(LanguageSettingsManager.isWorkspaceProvider(providerExt));

			// get raw extension provider
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(providerExt);
			assertTrue(rawProvider instanceof LanguageSettingsSerializableProvider);
			assertTrue(LanguageSettingsManager.isEqualExtensionProvider(rawProvider, true));
			assertEquals(true, LanguageSettingsExtensionManager.isPreferShared(EXTENSION_SERIALIZABLE_PROVIDER_ID));
		}

		// Editable providers are retrieved by copy
		{
			// get extension provider
			ILanguageSettingsProvider providerExt = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID, true);
			assertFalse(LanguageSettingsManager.isWorkspaceProvider(providerExt));
			assertTrue(providerExt instanceof ILanguageSettingsEditableProvider);
			assertTrue(LanguageSettingsManager.isEqualExtensionProvider(providerExt, true));
			assertEquals(LanguageSettingsExtensionManager.isPreferShared(EXTENSION_EDITABLE_PROVIDER_ID), false);

			// test that different copies are not same
			ILanguageSettingsProvider providerExt2 = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID, true);
			assertNotSame(providerExt, providerExt2);
			assertEquals(providerExt, providerExt2);

			// test that workspace provider is not the same as extension provider
			ILanguageSettingsProvider providerWsp = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
			ILanguageSettingsProvider providerWspRaw = LanguageSettingsManager.getRawProvider(providerWsp);
			assertNotSame(providerExt, providerWspRaw);
			assertEquals(providerExt, providerWspRaw);
			assertTrue(LanguageSettingsManager.isEqualExtensionProvider(providerWspRaw, true));
		}

		// Test shallow copy
		{
			ILanguageSettingsProvider provider = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID, true);
			assertNotNull(provider);
			assertTrue(provider instanceof ILanguageSettingsEditableProvider);

			ILanguageSettingsProvider providerShallow = LanguageSettingsManager.getExtensionProviderCopy(EXTENSION_EDITABLE_PROVIDER_ID, false);
			assertNotNull(providerShallow);
			assertTrue(providerShallow instanceof ILanguageSettingsEditableProvider);
			assertFalse(provider.equals(providerShallow));

			assertFalse(LanguageSettingsManager.isEqualExtensionProvider(providerShallow, true));
			assertTrue(LanguageSettingsManager.isEqualExtensionProvider(providerShallow, false));
		}
	}

}
