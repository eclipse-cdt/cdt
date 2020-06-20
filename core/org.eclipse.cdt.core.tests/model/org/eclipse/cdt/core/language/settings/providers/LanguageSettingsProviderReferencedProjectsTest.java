/*******************************************************************************
 * Copyright (c) 2013 Andrew Gvozdev and others.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.language.settings.providers.ReferencedProjectsLanguageSettingsProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import junit.framework.TestSuite;

/**
 * Test cases testing ReferencedProjectsLanguageSettingsProvider functionality
 */
public class LanguageSettingsProviderReferencedProjectsTests extends BaseTestCase {
	/**
	 * Constructor.
	 * @param name - name of the test.
	 */
	public LanguageSettingsProviderReferencedProjectsTests(String name) {
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
		return new TestSuite(LanguageSettingsProviderReferencedProjectsTests.class);
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
	 * Helper method to fetch configuration descriptions.
	 */
	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project, false);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		return cfgDescriptions;
	}

	/**
	 * Helper method to set reference project.
	 */
	private void setReference(IProject project, final IProject projectReferenced) throws CoreException {
		{
			CoreModel coreModel = CoreModel.getDefault();
			ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
			// project description
			ICProjectDescription projectDescription = mngr.getProjectDescription(project);
			assertNotNull(projectDescription);
			assertEquals(1, projectDescription.getConfigurations().length);
			// configuration description
			ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];

			final ICConfigurationDescription cfgDescriptionReferenced = getConfigurationDescriptions(
					projectReferenced)[0];
			cfgDescription.setReferenceInfo(new HashMap<String, String>() {
				{
					put(projectReferenced.getName(), cfgDescriptionReferenced.getId());
				}
			});
			coreModel.setProjectDescription(project, projectDescription);
		}

		{
			// doublecheck that it's set as expected
			ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			Map<String, String> refs = cfgDescription.getReferenceInfo();
			assertEquals(1, refs.size());
			Set<String> referencedProjectsNames = new LinkedHashSet<>(refs.keySet());
			assertEquals(projectReferenced.getName(), referencedProjectsNames.toArray()[0]);
		}

	}

	/**
	 * Test that null arguments don't crash the provider.
	 */
	public void testNulls() throws Exception {
		ILanguageSettingsProvider provider = LanguageSettingsManager
				.getWorkspaceProvider(ReferencedProjectsLanguageSettingsProvider.ID);
		assertNotNull(provider);
		List<ICLanguageSettingEntry> entries = provider.getSettingEntries(null, null, null);
		assertEquals(null, entries);
	}

	/**
	 * Test main functionality of ReferencedProjectsLanguageSettingsProvider.
	 */
	public void testReferencedProjectProvider() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		IProject nonReferencedProject = ResourceHelper.createCDTProjectWithConfig(projectName + "-non-referenced");
		IProject referencedProject = ResourceHelper.createCDTProjectWithConfig(projectName + "-referenced");
		setReference(project, referencedProject);

		// get cfgDescription
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		{
			// double-check that provider for referenced projects is set in the configuration
			ILanguageSettingsProvider refProjectsProvider = LanguageSettingsManager
					.getWorkspaceProvider(ReferencedProjectsLanguageSettingsProvider.ID);
			assertNotNull(refProjectsProvider);
			List<ILanguageSettingsProvider> providers = ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders();
			assertTrue(providers.contains(refProjectsProvider));
		}

		// Check that no setting entries are set initially
		{
			List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesByKind(cfgDescription,
					project, null, ICSettingEntry.ALL);
			assertEquals(0, entries.size());
		}

		// Add an entry into a non-referenced project
		CIncludePathEntry nonRefEntry = CDataUtil.createCIncludePathEntry("non-referenced-exported",
				ICSettingEntry.EXPORTED);
		{
			ICConfigurationDescription[] nonRefCfgDescriptions = getConfigurationDescriptions(nonReferencedProject);
			ICConfigurationDescription nonRefCfgDescription = nonRefCfgDescriptions[0];
			List<ILanguageSettingsProvider> providersNonRef = ((ILanguageSettingsProvidersKeeper) nonRefCfgDescription)
					.getLanguageSettingProviders();
			// get user provider which is the first one
			ILanguageSettingsProvider userProviderNonRef = providersNonRef.get(0);
			assertEquals(ScannerDiscoveryLegacySupport.USER_LANGUAGE_SETTINGS_PROVIDER_ID, userProviderNonRef.getId());
			assertTrue(userProviderNonRef instanceof LanguageSettingsGenericProvider);
			// add sample entries
			ArrayList<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(nonRefEntry);
			((LanguageSettingsGenericProvider) userProviderNonRef).setSettingEntries(null, null, null, entries);
		}
		// Confirm that that does not add entries to the main project
		{
			List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesByKind(cfgDescription,
					project, null, ICSettingEntry.ALL);
			assertEquals(0, entries.size());
		}

		CIncludePathEntry refEntry = CDataUtil.createCIncludePathEntry("referenced-exported", ICSettingEntry.EXPORTED);
		// Add entries into a referenced project
		{
			ICConfigurationDescription[] refCfgDescriptions = getConfigurationDescriptions(referencedProject);
			ICConfigurationDescription refCfgDescription = refCfgDescriptions[0];
			List<ILanguageSettingsProvider> providersRef = ((ILanguageSettingsProvidersKeeper) refCfgDescription)
					.getLanguageSettingProviders();
			// get user provider which is the first one
			ILanguageSettingsProvider userProviderRef = providersRef.get(0);
			assertEquals(ScannerDiscoveryLegacySupport.USER_LANGUAGE_SETTINGS_PROVIDER_ID, userProviderRef.getId());
			assertTrue(userProviderRef instanceof LanguageSettingsGenericProvider);
			// add sample entries
			ArrayList<ICLanguageSettingEntry> entries = new ArrayList<>();
			CIncludePathEntry refEntryNotExported = CDataUtil.createCIncludePathEntry("referenced-not-exported", 0);
			entries.add(refEntry);
			entries.add(refEntryNotExported);
			((LanguageSettingsGenericProvider) userProviderRef).setSettingEntries(null, null, null, entries);
			List<ICLanguageSettingEntry> entriesActual = LanguageSettingsManager
					.getSettingEntriesByKind(refCfgDescription, project, null, ICSettingEntry.ALL);
			assertEquals(entries, entriesActual);
		}
		// Check that the new entries from referenced project made it to the main project
		{
			List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesByKind(cfgDescription,
					project, null, ICSettingEntry.ALL);
			assertEquals(CDataUtil.createCIncludePathEntry(refEntry.getName(), 0), entries.get(0));
			assertEquals(1, entries.size());
		}
	}

	/**
	 * Test case when projects reference each other recursively.
	 */
	public void testRecursiveReferences() throws Exception {
		// Create model projects that reference each other
		String projectName = getName();
		IProject projectA = ResourceHelper.createCDTProjectWithConfig(projectName + "-A");
		IProject projectB = ResourceHelper.createCDTProjectWithConfig(projectName + "-B");
		setReference(projectA, projectB);
		setReference(projectB, projectA);

		{
			// get cfgDescriptions to work with
			ICConfigurationDescription[] cfgDescriptionsA = getConfigurationDescriptions(projectA);
			ICConfigurationDescription cfgDescriptionA = cfgDescriptionsA[0];
			ICConfigurationDescription[] cfgDescriptionsB = getConfigurationDescriptions(projectB);
			ICConfigurationDescription cfgDescriptionB = cfgDescriptionsB[0];
			// double-check that provider for referenced projects is set in the configurations
			ILanguageSettingsProvider refProjectsProvider = LanguageSettingsManager
					.getWorkspaceProvider(ReferencedProjectsLanguageSettingsProvider.ID);
			assertNotNull(refProjectsProvider);
			List<ILanguageSettingsProvider> providersA = ((ILanguageSettingsProvidersKeeper) cfgDescriptionA)
					.getLanguageSettingProviders();
			assertTrue(providersA.contains(refProjectsProvider));
			List<ILanguageSettingsProvider> providersB = ((ILanguageSettingsProvidersKeeper) cfgDescriptionB)
					.getLanguageSettingProviders();
			assertTrue(providersB.contains(refProjectsProvider));

			// Check that no setting entries are set initially
			List<ICLanguageSettingEntry> entriesA = LanguageSettingsManager.getSettingEntriesByKind(cfgDescriptionA,
					projectA, null, ICSettingEntry.ALL);
			assertEquals(0, entriesA.size());
			List<ICLanguageSettingEntry> entriesB = LanguageSettingsManager.getSettingEntriesByKind(cfgDescriptionA,
					projectB, null, ICSettingEntry.ALL);
			assertEquals(0, entriesB.size());
		}

		CIncludePathEntry entryExportedA = CDataUtil.createCIncludePathEntry("referenced-exported-A",
				ICSettingEntry.EXPORTED);
		CIncludePathEntry entryNotExportedA = CDataUtil.createCIncludePathEntry("referenced-not-exported-A", 0);
		// Add entries into a project A
		{
			ICConfigurationDescription[] refCfgDescriptions = getConfigurationDescriptions(projectA);
			ICConfigurationDescription refCfgDescription = refCfgDescriptions[0];
			List<ILanguageSettingsProvider> providersRef = ((ILanguageSettingsProvidersKeeper) refCfgDescription)
					.getLanguageSettingProviders();
			// get user provider which is the first one
			ILanguageSettingsProvider userProviderRef = providersRef.get(0);
			assertEquals(ScannerDiscoveryLegacySupport.USER_LANGUAGE_SETTINGS_PROVIDER_ID, userProviderRef.getId());
			assertTrue(userProviderRef instanceof LanguageSettingsGenericProvider);
			// add sample entries
			ArrayList<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(entryExportedA);
			entries.add(entryNotExportedA);
			((LanguageSettingsGenericProvider) userProviderRef).setSettingEntries(null, null, null, entries);
		}

		CIncludePathEntry entryExportedB = CDataUtil.createCIncludePathEntry("referenced-exported-B",
				ICSettingEntry.EXPORTED);
		CIncludePathEntry entryNotExportedB = CDataUtil.createCIncludePathEntry("referenced-not-exported-B", 0);
		// Add entries into a project B
		{
			ICConfigurationDescription[] refCfgDescriptions = getConfigurationDescriptions(projectB);
			ICConfigurationDescription refCfgDescription = refCfgDescriptions[0];
			List<ILanguageSettingsProvider> providersRef = ((ILanguageSettingsProvidersKeeper) refCfgDescription)
					.getLanguageSettingProviders();
			// get user provider which is the first one
			ILanguageSettingsProvider userProviderRef = providersRef.get(0);
			assertEquals(ScannerDiscoveryLegacySupport.USER_LANGUAGE_SETTINGS_PROVIDER_ID, userProviderRef.getId());
			assertTrue(userProviderRef instanceof LanguageSettingsGenericProvider);
			// add sample entries
			ArrayList<ICLanguageSettingEntry> entries = new ArrayList<>();
			entries.add(entryExportedB);
			entries.add(entryNotExportedB);
			((LanguageSettingsGenericProvider) userProviderRef).setSettingEntries(null, null, null, entries);
		}

		// Check that the new entries from projectB made it to projectA
		{
			ICConfigurationDescription[] cfgDescriptionsA = getConfigurationDescriptions(projectA);
			ICConfigurationDescription cfgDescriptionA = cfgDescriptionsA[0];
			List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesByKind(cfgDescriptionA,
					projectA, null, ICSettingEntry.ALL);
			assertEquals(entryExportedA, entries.get(0));
			assertEquals(entryNotExportedA, entries.get(1));
			assertEquals(CDataUtil.createCIncludePathEntry(entryExportedB.getName(), 0), entries.get(2));
			assertEquals(3, entries.size());
		}
		// Check that the new entries from projectA made it to projectB
		{
			ICConfigurationDescription[] cfgDescriptionsB = getConfigurationDescriptions(projectB);
			ICConfigurationDescription cfgDescriptionB = cfgDescriptionsB[0];
			List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesByKind(cfgDescriptionB,
					projectB, null, ICSettingEntry.ALL);
			assertEquals(entryExportedB, entries.get(0));
			assertEquals(entryNotExportedB, entries.get(1));
			assertEquals(CDataUtil.createCIncludePathEntry(entryExportedA.getName(), 0), entries.get(2));
			assertEquals(3, entries.size());
		}

		// Hopefully it gets here without stack overflow
	}

}
