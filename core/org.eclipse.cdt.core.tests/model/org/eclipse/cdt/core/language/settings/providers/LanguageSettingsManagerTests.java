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

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.CModelMock;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescription;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing {@link LanguageSettingsManager} utility methods.
 */
public class LanguageSettingsManagerTests extends BaseTestCase {
	// Those should match ids of plugin extensions defined in plugin.xml
	private static final String EXTENSION_BASE_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_BASE_PROVIDER_ID;
	private static final String EXTENSION_SERIALIZABLE_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_SERIALIZABLE_PROVIDER_ID;
	private static final String EXTENSION_SERIALIZABLE_PROVIDER_NAME = LanguageSettingsExtensionsTests.EXTENSION_SERIALIZABLE_PROVIDER_NAME;
	private static final String EXTENSION_EDITABLE_PROVIDER_ID = LanguageSettingsExtensionsTests.EXTENSION_EDITABLE_PROVIDER_ID;

	// Arbitrary sample parameters used by the test case
	private static final String PROVIDER_0 = "test.provider.0.id";
	private static final String PROVIDER_1 = "test.provider.1.id";
	private static final String PROVIDER_2 = "test.provider.2.id";
	private static final String PROVIDER_NAME_0 = "test.provider.0.name";
	private static final String PROVIDER_NAME_1 = "test.provider.1.name";
	private static final String PROVIDER_NAME_2 = "test.provider.2.name";
	private static final String CFG_ID = "test.configuration.id";
	private static final String LANG_ID = "test.lang.id";
	private static final IFile FILE_0 = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));

	/**
	 * Mock configuration description.
	 */
	class MockConfigurationDescription extends CModelMock.DummyCConfigurationDescription implements ILanguageSettingsProvidersKeeper {
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		String[] defaultProvidersIds = null;
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
			defaultProvidersIds = ids;
		}
		@Override
		public String[] getDefaultLanguageSettingsProvidersIds() {
			return defaultProvidersIds;
		}
	}

	/**
	 * Mock language sttings provider.
	 */
	private class MockProvider extends AbstractExecutableExtensionBase implements ILanguageSettingsProvider {
		private List<ICLanguageSettingEntry> entries;
		public MockProvider(String id, String name, List<ICLanguageSettingEntry> entries) {
			super(id, name);
			this.entries = entries;
		}
		@Override
		public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
			return entries;
		}
	}

	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsManagerTests(String name) {
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
		return new TestSuite(LanguageSettingsManagerTests.class);
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
	 * Test ILanguageSettingsProvidersKeeper API (getters and setters).
	 */
	public void testConfigurationDescription_Providers() throws Exception {
		// mock configuration description
		MockConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// set providers
		ILanguageSettingsProvider provider1 = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null);
		ILanguageSettingsProvider provider2 = new MockProvider(PROVIDER_2, PROVIDER_NAME_2, null);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider1);
		providers.add(provider2);
		cfgDescription.setLanguageSettingProviders(providers);

		// get providers
		List<ILanguageSettingsProvider> actual = cfgDescription.getLanguageSettingProviders();
		assertEquals(provider1, actual.get(0));
		assertEquals(provider2, actual.get(1));
		assertEquals(providers.size(), actual.size());
		assertNotSame(actual, providers);

		// set default providers
		String[] defaultProviders = { PROVIDER_0, PROVIDER_1 };
		cfgDescription.setDefaultLanguageSettingsProvidersIds(defaultProviders);

		// get default providers
		assertEquals(defaultProviders, cfgDescription.getDefaultLanguageSettingsProvidersIds());
	}

	/**
	 * Test to ensure uniqueness of ids for providers kept in configuration description.
	 */
	public void testConfigurationDescription_ProvidersUniqueId() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);

		ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
		ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
		assertTrue(cfgDescriptionWritable instanceof CConfigurationDescription);

		// attempt to add duplicate providers
		MockProvider dupe1 = new MockProvider(PROVIDER_0, PROVIDER_NAME_1, null);
		MockProvider dupe2 = new MockProvider(PROVIDER_0, PROVIDER_NAME_2, null);

		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(dupe1);
		providers.add(dupe2);

		try {
			((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(providers);
			fail("cfgDescription.setLanguageSettingProviders() should not accept duplicate providers");
		} catch (Exception e) {
			// Exception is welcome here
		}
	}

	/**
	 * Test various cases of ill-defined providers.
	 */
	public void testRudeProviders() throws Exception {
		// mock configuration description
		MockConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);
		// set impolite provider returning null by getSettingEntries()
		ILanguageSettingsProvider providerNull = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null);
		{
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerNull);
			cfgDescription.setLanguageSettingProviders(providers);
		}

		// use provider returning null, no exception should be recorded
		{
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(providerNull, cfgDescription, FILE_0, LANG_ID);
			assertNotNull(actual);
			assertEquals(0, actual.size());
		}
		{
			List<ICLanguageSettingEntry> actual = LanguageSettingsProvidersSerializer
				.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, 0);
			assertNotNull(actual);
			assertEquals(0, actual.size());
		}

		// set impolite provider returning null in getSettingEntries() array
		ILanguageSettingsProvider providerNull_2 = new MockProvider(PROVIDER_2, PROVIDER_NAME_2,
			new ArrayList<ICLanguageSettingEntry>() {
				{ // init via static initializer
					add(null);
				}
			});
		{
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(providerNull);
			cfgDescription.setLanguageSettingProviders(providers);
		}

		// use provider returning null as item in array
		{
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(providerNull_2, cfgDescription, FILE_0, LANG_ID);
			assertNotNull(actual);
			assertEquals(1, actual.size());
		}
		{
			List<ICLanguageSettingEntry> actual = LanguageSettingsProvidersSerializer
				.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, 0);
			assertNotNull(actual);
			assertEquals(0, actual.size());
		}

		// use careless provider causing an exception
		{
			setExpectedNumberOfLoggedNonOKStatusObjects(1);

			ILanguageSettingsProvider providerNPE = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null) {
				@Override
				public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
					throw new NullPointerException("Can you handle me?");
				}
			};
			try {
				List<ICLanguageSettingEntry> actual = LanguageSettingsManager
						.getSettingEntriesUpResourceTree(providerNPE, null, null, LANG_ID);
				assertNotNull(actual);
				assertEquals(0, actual.size());
			} catch (Throwable e) {
				fail("Exceptions are expected to be swallowed (after logging) but got " + e);
			}
		}
	}

	/**
	 * Test assigning and retrieving providers from a configuration.
	 */
	public void testProvider_Basic() throws Exception {
		final MockConfigurationDescription modelCfgDescription = new MockConfigurationDescription(CFG_ID);

		final List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));

		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		// define provider returning entries when configuration id matches and null otherwise
		ILanguageSettingsProvider providerYes = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, null)  {
			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (cfgDescription.getId().equals(modelCfgDescription.getId())) {
					return entries;
				}
				return null;
			}

		};
		providers.add(providerYes);
		// define provider returning entries when configuration id does NOT match and null otherwise
		ILanguageSettingsProvider providerNo = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, null)  {
			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (cfgDescription!= null && !cfgDescription.getId().equals(modelCfgDescription.getId())) {
					return entries;
				}
				return null;
			}

		};
		providers.add(providerNo);
		modelCfgDescription.setLanguageSettingProviders(providers);

		{
			// retrieve the entries with provider returning the given list
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(providerYes, modelCfgDescription, FILE_0, LANG_ID);
			assertEquals(entries.get(0), actual.get(0));
			assertEquals(entries.size(), actual.size());
		}

		{
			// retrieve the entries with provider returning empty list
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(providerNo, modelCfgDescription, FILE_0, LANG_ID);
			assertEquals(0, actual.size());
		}
	}

	/**
	 * Test regular functionality with a few providers.
	 */
	public void testProvider_Regular() throws Exception {
		MockConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// create couple of providers
		List<ICLanguageSettingEntry> entries1 = new ArrayList<ICLanguageSettingEntry>();
		entries1.add(new CIncludePathEntry("value1", 1));
		entries1.add(new CIncludePathEntry("value2", 2));

		List<ICLanguageSettingEntry> entries2 = new ArrayList<ICLanguageSettingEntry>();
		entries2.add(new CIncludePathEntry("value1", 1));
		entries2.add(new CIncludePathEntry("value2", 2));
		entries2.add(new CIncludePathEntry("value3", 2));

		ILanguageSettingsProvider provider1 = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, entries1);
		ILanguageSettingsProvider provider2 = new MockProvider(PROVIDER_2, PROVIDER_NAME_2, entries2);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider1);
		providers.add(provider2);
		cfgDescription.setLanguageSettingProviders(providers);

		{
			// retrieve the entries for provider-1
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(provider1, cfgDescription, FILE_0, LANG_ID);
			assertNotSame(entries1, actual);

			ICLanguageSettingEntry[] entriesArray = entries1.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] actualArray = actual.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<entries1.size();i++) {
				assertEquals("i="+i, entriesArray[i], actualArray[i]);
			}
			assertEquals(entries1.size(), actual.size());
		}

		{
			// retrieve the entries for provider-2
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(provider2, cfgDescription, FILE_0, LANG_ID);
			assertNotSame(entries2, actual);

			ICLanguageSettingEntry[] entriesArray = entries2.toArray(new ICLanguageSettingEntry[0]);
			ICLanguageSettingEntry[] actualArray = actual.toArray(new ICLanguageSettingEntry[0]);
			for (int i=0;i<entries2.size();i++) {
				assertEquals("i="+i, entriesArray[i], actualArray[i]);
			}
			assertEquals(entries2.size(), actual.size());
		}
	}

	/**
	 * Test getting entries from resource hierarchy.
	 */
	public void testProvider_ParentFolder() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
		ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();

		ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
		assertTrue(cfgDescriptionWritable instanceof CConfigurationDescription);

		final IFolder parentFolder = ResourceHelper.createFolder(project, "/ParentFolder/");
		assertNotNull(parentFolder);
		final IFile emptySettingsPath = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/empty");
		assertNotNull(emptySettingsPath);

		// store the entries in parent folder
		final List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, null)  {
			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (rc!=null && rc.equals(parentFolder)) {
					return entries;
				}
				if (rc!=null && rc.equals(emptySettingsPath)) {
					return new ArrayList<ICLanguageSettingEntry>(0);
				}
				return null;
			}

		};
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(providers);

		{
			// retrieve entries for a derived resource (in a subfolder)
			IFile derived = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/resource");
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(provider, cfgDescriptionWritable, derived, LANG_ID);
			// taken from parent folder
			assertEquals(entries.get(0),actual.get(0));
			assertEquals(entries.size(), actual.size());
		}

		{
			// retrieve entries for not related resource
			IFile notRelated = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/AnotherFolder/Subfolder/resource"));
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(provider, cfgDescriptionWritable, notRelated, LANG_ID);
			assertEquals(0, actual.size());
		}

		{
			// test distinction between no settings and empty settings
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
				.getSettingEntriesUpResourceTree(provider, cfgDescriptionWritable, emptySettingsPath, LANG_ID);
			// NOT taken from parent folder
			assertEquals(0, actual.size());
		}
	}

	/**
	 * Test getting entries from resource hierarchy up to default entries.
	 */
	public void testProvider_DefaultEntries() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);
		ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();

		ICConfigurationDescription cfgDescriptionWritable = cfgDescriptions[0];
		assertTrue(cfgDescriptionWritable instanceof CConfigurationDescription);

		final IFolder parentFolder = ResourceHelper.createFolder(project, "/ParentFolder/");
		assertNotNull(parentFolder);
		final IFile emptySettingsPath = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/empty");
		assertNotNull(emptySettingsPath);

		// store the entries as default entries
		final List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, null)  {
			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (cfgDescription==null && rc==null) {
					return entries;
				}
				return null;
			}

		};
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescriptionWritable).setLanguageSettingProviders(providers);

		{
			// retrieve entries for a resource
			IFile derived = ResourceHelper.createFile(project, "/ParentFolder/Subfolder/resource");
			List<ICLanguageSettingEntry> actual = LanguageSettingsManager
					.getSettingEntriesUpResourceTree(provider, cfgDescriptionWritable, derived, LANG_ID);
			// default entries given
			assertEquals(entries.get(0),actual.get(0));
			assertEquals(entries.size(), actual.size());
		}
	}

	/**
	 * Test ability to get entries by kind.
	 */
	public void testEntriesByKind_Regular() throws Exception {
		MockConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// contribute the entries
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path0", 0));
		entries.add(new CMacroEntry("MACRO0", "value0",0));
		entries.add(new CIncludePathEntry("path1", 0));
		entries.add(new CMacroEntry("MACRO1", "value1",0));
		entries.add(new CIncludePathEntry("path2", 0));

		ILanguageSettingsProvider provider0 = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider0);
		cfgDescription.setLanguageSettingProviders(providers);

		// retrieve entries by kind
		List<ICLanguageSettingEntry> includes = LanguageSettingsProvidersSerializer
			.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
		assertEquals(new CIncludePathEntry("path0", 0),includes.get(0));
		assertEquals(new CIncludePathEntry("path1", 0),includes.get(1));
		assertEquals(new CIncludePathEntry("path2", 0),includes.get(2));
		assertEquals(3, includes.size());

		List<ICLanguageSettingEntry> macros = LanguageSettingsProvidersSerializer
			.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.MACRO);
		assertEquals(new CMacroEntry("MACRO0", "value0",0), macros.get(0));
		assertEquals(new CMacroEntry("MACRO1", "value1",0), macros.get(1));
		assertEquals(2, macros.size());
	}

	/**
	 * Test how conflicting entries are resolved.
	 */
	public void testEntriesByKind_ConflictingEntries() throws Exception {
		MockConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// contribute the entries
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path", ICSettingEntry.BUILTIN));
		entries.add(new CIncludePathEntry("path", ICSettingEntry.UNDEFINED));
		entries.add(new CIncludePathEntry("path", 0));

		ILanguageSettingsProvider provider0 = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider0);
		cfgDescription.setLanguageSettingProviders(providers);

		// retrieve entries by kind, only first entry should be returned
		List<ICLanguageSettingEntry> includes = LanguageSettingsProvidersSerializer.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
		assertEquals(1, includes.size());
		assertEquals(entries.get(0),includes.get(0));
	}

	/**
	 * Check handling of {@link ICSettingEntry#UNDEFINED} flag.
	 */
	public void testEntriesByKind_Undefined() throws Exception {
		MockConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// contribute the entries
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(new CIncludePathEntry("path", ICSettingEntry.UNDEFINED));
		entries.add(new CIncludePathEntry("path", 0));

		ILanguageSettingsProvider provider0 = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider0);
		cfgDescription.setLanguageSettingProviders(providers);

		// retrieve entries by kind, no entries should be returned
		List<ICLanguageSettingEntry> includes = LanguageSettingsProvidersSerializer
			.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
		assertEquals(0, includes.size());
	}

	/**
	 * Check handling of local vs. system entries, see {@link ICSettingEntry#LOCAL} flag.
	 */
	public void testEntriesByKind_LocalAndSystem() throws Exception {
		MockConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// contribute the entries
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		CIncludePathEntry localIncludeEntry = new CIncludePathEntry("path-local", ICSettingEntry.LOCAL);
		CIncludePathEntry systemIncludeEntry = new CIncludePathEntry("path-system", 0);
		entries.add(localIncludeEntry);
		entries.add(systemIncludeEntry);

		ILanguageSettingsProvider provider0 = new MockProvider(PROVIDER_0, PROVIDER_NAME_0, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider0);
		cfgDescription.setLanguageSettingProviders(providers);

		{
			// retrieve local entries
			List<ICLanguageSettingEntry> includes = LanguageSettingsProvidersSerializer
				.getLocalSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
			assertEquals(localIncludeEntry, includes.get(0));
			assertEquals(1, includes.size());
		}

		{
			// retrieve system entries
			List<ICLanguageSettingEntry> includes = LanguageSettingsProvidersSerializer
				.getSystemSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
			assertEquals(systemIncludeEntry, includes.get(0));
			assertEquals(1, includes.size());
		}

		{
			// retrieve both local and system
			List<ICLanguageSettingEntry> includes = LanguageSettingsProvidersSerializer
				.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
			assertEquals(entries.get(0), includes.get(0));
			assertEquals(entries.get(1), includes.get(1));
			assertEquals(2, includes.size());
		}
	}

	/**
	 * Test conflicting entries contributed by different providers.
	 */
	public void testEntriesByKind_ConflictingProviders() throws Exception {
		MockConfigurationDescription cfgDescription = new MockConfigurationDescription(CFG_ID);

		// contribute the entries
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();

		// contribute the higher ranked entries
		List<ICLanguageSettingEntry> entriesHigh = new ArrayList<ICLanguageSettingEntry>();
		entriesHigh.add(new CIncludePathEntry("path0", ICSettingEntry.RESOLVED));
		entriesHigh.add(new CIncludePathEntry("path1", 0));
		entriesHigh.add(new CIncludePathEntry("path2", ICSettingEntry.UNDEFINED));
		ILanguageSettingsProvider highRankProvider = new MockProvider(PROVIDER_2, PROVIDER_NAME_2, entriesHigh);
		providers.add(highRankProvider);

		// contribute the lower ranked entries
		List<ICLanguageSettingEntry> entriesLow = new ArrayList<ICLanguageSettingEntry>();
		entriesLow.add(new CIncludePathEntry("path0", ICSettingEntry.BUILTIN));
		entriesLow.add(new CIncludePathEntry("path1", ICSettingEntry.UNDEFINED));
		entriesLow.add(new CIncludePathEntry("path2", 0));
		entriesLow.add(new CIncludePathEntry("path3", 0));
		ILanguageSettingsProvider lowRankProvider = new MockProvider(PROVIDER_1, PROVIDER_NAME_1, entriesLow);
		providers.add(lowRankProvider);

		cfgDescription.setLanguageSettingProviders(providers);

		// retrieve entries by kind
		List<ICLanguageSettingEntry> includes = LanguageSettingsProvidersSerializer
			.getSettingEntriesByKind(cfgDescription, FILE_0, LANG_ID, ICSettingEntry.INCLUDE_PATH);
		// path0 is taken from higher priority provider
		assertEquals(entriesHigh.get(0),includes.get(0));
		// path1 disablement by lower priority provider is ignored
		assertEquals(entriesHigh.get(1),includes.get(1));
		// path2 is removed because of DISABLED flag of high priority provider
		// path3 gets there from low priority provider
		assertEquals(entriesLow.get(3),includes.get(2));
		assertEquals(3, includes.size());
	}

	/**
	 * Test ability to serialize providers for a configuration.
	 */
	public void testConfigurationDescription_SerializeProviders() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICProjectDescription prjDescriptionWritable = CProjectDescriptionManager.getInstance().getProjectDescription(project, true);

		ICConfigurationDescription[] cfgDescriptions = prjDescriptionWritable.getConfigurations();
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		assertTrue(cfgDescription instanceof CConfigurationDescription);

		ILanguageSettingsProvider workspaceProvider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_BASE_PROVIDER_ID);
		assertNotNull(workspaceProvider);
		{
			// ensure no test provider is set yet
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(0, providers.size());
		}
		{
			// set test provider
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			providers.add(workspaceProvider);
			((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
		}
		{
			// check that test provider got there
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			assertEquals(workspaceProvider, providers.get(0));
		}

		{
			// serialize
			CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescriptionWritable);
			// close and reopen the project
			project.close(null);
			project.open(null);
		}

		{
			// check that test provider got loaded
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			ICConfigurationDescription[] loadedCfgDescriptions = prjDescription.getConfigurations();
			ICConfigurationDescription loadedCfgDescription = loadedCfgDescriptions[0];
			assertTrue(cfgDescription instanceof CConfigurationDescription);

			List<ILanguageSettingsProvider> loadedProviders = ((ILanguageSettingsProvidersKeeper) loadedCfgDescription).getLanguageSettingProviders();
			assertTrue(LanguageSettingsManager.isWorkspaceProvider(loadedProviders.get(0)));
		}

	}

	/**
	 * Test a workspace provider basics.
	 */
	public void testWorkspaceProvider_Basic() throws Exception {
		// get workspace provider
		ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
		assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, provider.getId());
		assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_NAME, provider.getName());

		// get raw provider
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
		assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_ID, rawProvider.getId());
		assertEquals(EXTENSION_SERIALIZABLE_PROVIDER_NAME, rawProvider.getName());
		assertTrue(rawProvider instanceof LanguageSettingsSerializableProvider);
		// assert they are not the same object
		assertNotSame(provider, rawProvider);

		{
			// make sure entries are the same
			List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);
			assertEquals(1, entries.size()); // defined in the extension
			List<ICLanguageSettingEntry> rawEntries = rawProvider.getSettingEntries(null, null, null);
			assertEquals(entries, rawEntries);
		}

		{
			// set new entries to the raw provider
			List<ICLanguageSettingEntry> newEntries = new ArrayList<ICLanguageSettingEntry>();
			newEntries.add(new CIncludePathEntry("path0", 0));
			newEntries.add(new CIncludePathEntry("path1", 0));
			((LanguageSettingsSerializableProvider)rawProvider).setSettingEntries(null, null, null, newEntries);

			// check that the workspace provider gets them too
			List<ICLanguageSettingEntry> newRawEntries = rawProvider.getSettingEntries(null, null, null);
			assertEquals(newEntries, newRawEntries);
			assertEquals(2, newEntries.size());
		}
	}

	/**
	 * Test workspace providers equality.
	 */
	public void testWorkspaceProvider_Equals() throws Exception {
		ILanguageSettingsProvider providerA = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
		ILanguageSettingsProvider providerB = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
		assertEquals(providerA, providerB);
	}

	/**
	 * Test ability to replace underlying raw provider.
	 */
	public void testWorkspaceProvider_ReplaceRawProvider() throws Exception {
		// get sample workspace provider
		ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_EDITABLE_PROVIDER_ID);
		{
			// check on its entries (1 predefined entry via extension point)
			List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);
			assertEquals(1, entries.size()); // defined in the extension
		}

		// define new entries for the raw provider
		List<ICLanguageSettingEntry> newEntries = new ArrayList<ICLanguageSettingEntry>();
		newEntries.add(new CIncludePathEntry("path0", 0));
		newEntries.add(new CIncludePathEntry("path1", 0));
		newEntries.add(new CIncludePathEntry("path2", 0));

		{
			// replace raw provider
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
			LanguageSettingsSerializableProvider newRawProvider = new LanguageSettingsSerializableProvider(EXTENSION_EDITABLE_PROVIDER_ID, PROVIDER_NAME_0);
			newRawProvider.setSettingEntries(null, null, null, newEntries);
			providers.add(newRawProvider);
			LanguageSettingsManager.setWorkspaceProviders(providers);
		}

		{
			// check that provider provides the new entries
			List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);
			assertEquals(newEntries.size(), entries.size());
			assertEquals(newEntries, entries);
		}
	}

	/**
	 * Test ability to be called with workspace provider as well (NOOP).
	 */
	public void testWorkspaceProvider_ReplaceWithWorkspaceProvider() throws Exception {
		// get sample workspace provider
		ILanguageSettingsProvider provider = LanguageSettingsManager.getWorkspaceProvider(EXTENSION_SERIALIZABLE_PROVIDER_ID);
		ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
		assertNotSame(provider, rawProvider);

		// attempt to "replace" with workspace provider (which is a wrapper around raw provider), should be NOOP
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		LanguageSettingsManager.setWorkspaceProviders(providers);
		ILanguageSettingsProvider newRawProvider = LanguageSettingsManager.getRawProvider(provider);
		assertSame(rawProvider, newRawProvider);

		// check for no side effect
		assertSame(provider, providers.get(0));
	}

}
