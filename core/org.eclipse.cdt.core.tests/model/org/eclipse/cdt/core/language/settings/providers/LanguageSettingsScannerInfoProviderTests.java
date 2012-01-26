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
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsScannerInfoProvider;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Test cases testing LanguageSettingsProvider functionality
 */
public class LanguageSettingsScannerInfoProviderTests extends BaseTestCase {
	private static final IFile FAKE_FILE = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/project/path0"));
	private static final String PROVIDER_ID = "test.provider.id";
	private static final String PROVIDER_ID_2 = "test.provider.id.2";
	private static final String PROVIDER_NAME = "test.provider.name";

	// constants for getProjectDescription()
	private static final boolean READ_ONLY = false;
	private static final boolean WRITEABLE = true;

	private class MockProvider extends LanguageSettingsBaseProvider implements ILanguageSettingsProvider {
		private final List<ICLanguageSettingEntry> entries;

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
	public LanguageSettingsScannerInfoProviderTests(String name) {
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
		return new TestSuite(LanguageSettingsScannerInfoProviderTests.class);
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
	 * Sets build working directory for DefaultSettingConfiguration being tested.
	 */
	private void setBuilderCWD(IProject project, IPath buildCWD) throws CoreException {
		CProjectDescriptionManager manager = CProjectDescriptionManager.getInstance();
		{
			ICProjectDescription prjDescription = manager.getProjectDescription(project, WRITEABLE);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
			assertNotNull(cfgDescription);

			cfgDescription.getBuildSetting().setBuilderCWD(buildCWD);
			manager.setProjectDescription(project, prjDescription);
			// doublecheck builderCWD
			IPath actualBuildCWD = cfgDescription.getBuildSetting().getBuilderCWD();
			assertEquals(buildCWD, actualBuildCWD);
		}
		{
			// triplecheck builderCWD for different project/configuration descriptions
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, READ_ONLY);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
			assertNotNull(cfgDescription);

		}
	}

	/**
	 * Test cases when some objects are null.
	 */
	public void testNulls() throws Exception {
		{
			// Handle project==null
			IResource root = ResourcesPlugin.getWorkspace().getRoot();
			assertNull(root.getProject());

			LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
			ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(root);
			assertEquals(0, info.getIncludePaths().length);
			assertEquals(0, info.getDefinedSymbols().size());
			assertEquals(0, info.getIncludeFiles().length);
			assertEquals(0, info.getMacroFiles().length);
			assertEquals(0, info.getLocalIncludePath().length);
		}

		{
			// Handle prjDescription==null
			IProject project = FAKE_FILE.getProject();
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, READ_ONLY);
			assertNull(prjDescription);

			LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
			ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(FAKE_FILE);
			assertEquals(0, info.getIncludePaths().length);
			assertEquals(0, info.getDefinedSymbols().size());
			assertEquals(0, info.getIncludeFiles().length);
			assertEquals(0, info.getMacroFiles().length);
			assertEquals(0, info.getLocalIncludePath().length);
		}

		{
			// Handle language==null
			LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
			IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
			IFile file = ResourceHelper.createFile(project, "file");

			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, READ_ONLY);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
			assertNotNull(cfgDescription);
			ILanguage language = LanguageManager.getInstance().getLanguageForFile(file, cfgDescription);
			assertNull(language);

			// AG FIXME - temporarily ignore the entry in the log
			setExpectedNumberOfLoggedNonOKStatusObjects(1);

			ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
			assertEquals(0, info.getIncludePaths().length);
			assertEquals(0, info.getDefinedSymbols().size());
			assertEquals(0, info.getIncludeFiles().length);
			assertEquals(0, info.getMacroFiles().length);
			assertEquals(0, info.getLocalIncludePath().length);
		}
	}

	/**
	 * Test empty scanner info.
	 */
	public void testEmpty() throws Exception {
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		IFile file = ResourceHelper.createFile(project, "file.c");

		// confirm that language==null
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, READ_ONLY);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		ILanguage language = LanguageManager.getInstance().getLanguageForFile(file, cfgDescription);
		assertNotNull(language);

		// test that the info is empty
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		assertEquals(0, info.getIncludePaths().length);
		assertEquals(0, info.getDefinedSymbols().size());
		assertEquals(0, info.getIncludeFiles().length);
		assertEquals(0, info.getMacroFiles().length);
		assertEquals(0, info.getLocalIncludePath().length);
	}

	/**
	 * Test regular cases.
	 */
	public void testRegular() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// sample file
		IFile file = ResourceHelper.createFile(project, "file.c");

		// sanity test of language
		ILanguage language = LanguageManager.getInstance().getLanguageForFile(file, cfgDescription);
		assertNotNull(language);

		// contribute the entries
		IFolder includeFolder = ResourceHelper.createFolder(project, "/include-path");
		IFolder includeLocalFolder = ResourceHelper.createFolder(project, "/local-include-path");
		IFile macroFile = ResourceHelper.createFile(project, "macro-file");
		IFile includeFile = ResourceHelper.createFile(project, "include-file");

		CIncludePathEntry includePathEntry = new CIncludePathEntry(includeFolder, 0);
		CIncludePathEntry includeLocalPathEntry = new CIncludePathEntry(includeLocalFolder, ICSettingEntry.LOCAL); // #include "..."
		CMacroEntry macroEntry = new CMacroEntry("MACRO", "value",0);
		CIncludeFileEntry includeFileEntry = new CIncludeFileEntry(includeFile, 0);
		CMacroFileEntry macroFileEntry = new CMacroFileEntry(macroFile, 0);

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(includePathEntry);
		entries.add(includeLocalPathEntry);
		entries.add(macroEntry);
		entries.add(includeFileEntry);
		entries.add(macroFileEntry);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test that the scannerInfoProvider gets the entries
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();
		Map<String, String> actualDefinedSymbols = info.getDefinedSymbols();
		String[] actualIncludeFiles = info.getIncludeFiles();
		String[] actualMacroFiles = info.getMacroFiles();
		String[] actualLocalIncludePath = info.getLocalIncludePath();
		// include paths
		assertEquals(includeFolder.getLocation(), new Path(actualIncludePaths[0]));
		assertEquals(1, actualIncludePaths.length);
		// macros
		assertEquals(macroEntry.getValue(), actualDefinedSymbols.get(macroEntry.getName()));
		assertEquals(1, actualDefinedSymbols.size());
		// include file
		assertEquals(includeFile.getLocation(), new Path(actualIncludeFiles[0]));
		assertEquals(1, actualIncludeFiles.length);
		// macro file
		assertEquals(macroFile.getLocation(), new Path(actualMacroFiles[0]));
		assertEquals(1, actualMacroFiles.length);
		// local include files
		assertEquals(includeLocalFolder.getLocation(), new Path(actualLocalIncludePath[0]));
		assertEquals(1, actualLocalIncludePath.length);
	}

	/**
	 * Test "local" flag (#include "...").
	 */
	public void testLocal() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// sample file
		IFile file = ResourceHelper.createFile(project, "file.c");

		// contribute the entries
		IFolder incFolder = ResourceHelper.createFolder(project, "include");
		IFolder incFolder2 = ResourceHelper.createFolder(project, "include2");
		CIncludePathEntry includePathEntry = new CIncludePathEntry(incFolder, 0);
		CIncludePathEntry includeLocalPathEntry = new CIncludePathEntry(incFolder, ICSettingEntry.LOCAL); // #include "..."
		CIncludePathEntry includeLocalPathEntry2 = new CIncludePathEntry(incFolder2, ICSettingEntry.LOCAL); // #include "..."
		CIncludePathEntry includePathEntry2 = new CIncludePathEntry(incFolder2, 0);

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(includePathEntry);
		entries.add(includeLocalPathEntry);
		// reverse order for incPath2
		entries.add(includeLocalPathEntry2);
		entries.add(includePathEntry2);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test that the scannerInfoProvider gets the entries
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();
		String[] actualLocalIncludePath = info.getLocalIncludePath();
		// include paths
		assertEquals(incFolder.getLocation(), new Path(actualIncludePaths[0]));
		assertEquals(incFolder2.getLocation(), new Path(actualIncludePaths[1]));
		assertEquals(2, actualIncludePaths.length);
		// local include files
		assertEquals(incFolder.getLocation(), new Path(actualLocalIncludePath[0]));
		assertEquals(incFolder2.getLocation(), new Path(actualLocalIncludePath[1]));
		assertEquals(2, actualLocalIncludePath.length);
	}

	/**
	 * Test Mac frameworks.
	 */
	public void testFramework() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// sample file
		IFile file = ResourceHelper.createFile(project, "file.c");

		// contribute the entries
		IFolder frameworkFolder = ResourceHelper.createFolder(project, "Fmwk");
		CIncludePathEntry frameworkPathEntry = new CIncludePathEntry(frameworkFolder, ICSettingEntry.FRAMEWORKS_MAC);

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(frameworkPathEntry);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test that the scannerInfoProvider gets the entries
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();
		// include paths
		assertEquals(frameworkFolder.getLocation().append("/__framework__.framework/Headers/__header__"),
				new Path(actualIncludePaths[0]));
		assertEquals(frameworkFolder.getLocation().append("/__framework__.framework/PrivateHeaders/__header__"),
				new Path(actualIncludePaths[1]));
		assertEquals(2, actualIncludePaths.length);
	}

	/**
	 * Test duplicate entries.
	 */
	public void testDuplicate() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// sample file
		IFile file = ResourceHelper.createFile(project, "file.c");

		// contribute the entries
		IFolder incFolder = ResourceHelper.createFolder(project, "include");
		CIncludePathEntry includePathEntry = new CIncludePathEntry(incFolder, 0);
		CIncludePathEntry includeLocalPathEntry = new CIncludePathEntry(incFolder, ICSettingEntry.LOCAL); // #include "..."
		CIncludePathEntry includePathEntry2 = new CIncludePathEntry(incFolder, 0);
		CIncludePathEntry includeLocalPathEntry2 = new CIncludePathEntry(incFolder, ICSettingEntry.LOCAL); // #include "..."

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(includePathEntry);
		entries.add(includeLocalPathEntry);
		entries.add(includePathEntry2);
		entries.add(includeLocalPathEntry2);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test that the scannerInfoProvider gets the entries
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();
		String[] actualLocalIncludePath = info.getLocalIncludePath();
		// include paths
		assertEquals(incFolder.getLocation(), new Path(actualIncludePaths[0]));
		assertEquals(1, actualIncludePaths.length);
		// local include files
		assertEquals(incFolder.getLocation(), new Path(actualLocalIncludePath[0]));
		assertEquals(1, actualLocalIncludePath.length);
	}

	/**
	 * Test include path managed by eclipse as a workspace path.
	 */
	public void testWorkspacePath() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// create sample file
		IFile file = ResourceHelper.createFile(project, "file.c");
		// eclipse-managed folder in workspace
		IFolder incWorkspace_1 = ResourceHelper.createFolder(project, "include_1");
		IPath incWorkspaceLocation_1 = incWorkspace_1.getLocation();
		IFolder incWorkspace_2 = ResourceHelper.createFolder(project, "include_2");
		IPath incWorkspacePath_2 = incWorkspace_2.getFullPath();
		IPath incWorkspaceLocation_2 = incWorkspace_2.getLocation();
		IFolder incWorkspace_3 = ResourceHelper.createFolder(project, "include_3");
		// "relative" should make no difference for VALUE_WORKSPACE_PATH
		IPath incWorkspaceRelativePath_3 = incWorkspace_3.getFullPath().makeRelative();
		IPath incWorkspaceLocation_3 = incWorkspace_3.getLocation();
		// folder defined by absolute path on the filesystem
		IPath incFilesystem = ResourceHelper.createWorkspaceFolder("includeFilesystem");

		// contribute the entries
		CIncludePathEntry incWorkspaceEntry_1 = new CIncludePathEntry(incWorkspace_1, 0);
		CIncludePathEntry incWorkspaceEntry_2 = new CIncludePathEntry(incWorkspacePath_2, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
		CIncludePathEntry incWorkspaceEntry_3 = new CIncludePathEntry(incWorkspaceRelativePath_3, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
		CIncludePathEntry incFilesystemEntry = new CIncludePathEntry(incFilesystem, 0);

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(incWorkspaceEntry_1);
		entries.add(incWorkspaceEntry_2);
		entries.add(incWorkspaceEntry_3);
		entries.add(incFilesystemEntry);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test the entries received from the scannerInfoProvider
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();

		assertEquals(incWorkspaceLocation_1, new Path(actualIncludePaths[0]));
		assertEquals(incWorkspaceLocation_2, new Path(actualIncludePaths[1]));
		assertEquals(incWorkspaceLocation_3, new Path(actualIncludePaths[2]));
		assertEquals(incFilesystem, new Path(actualIncludePaths[3]));
		assertEquals(4, actualIncludePaths.length);

	}

	/**
	 * Confirm that device letter is prepended on filesystems that support that.
	 */
	public void testFilesystemPathNoDriveLetter() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		// change drive on build working directory
		String buildCwdDevice = project.getLocation().getDevice();

//		// Test manually with a device which is different from project location device (path should exist)
//		IPath buildCWD = new Path("D:/build/path");
//		String buildCwdDevice = buildCWD.getDevice();

		// get project/configuration descriptions
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// create sample file
		IFile file = ResourceHelper.createFile(project, "file.c");

		// contribute the entries
		// no-drive-letter folder defined by absolute path on the filesystem
		IPath incFilesystem = ResourceHelper.createWorkspaceFolder("includeFilesystem").setDevice(null);
		CIncludePathEntry incFilesystemEntry = new CIncludePathEntry(incFilesystem, 0);
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(incFilesystemEntry);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test the entries received from the scannerInfoProvider
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();

		IPath expectedInclude = incFilesystem.setDevice(buildCwdDevice);
		assertEquals(expectedInclude, new Path(actualIncludePaths[0]));
		assertEquals(1, actualIncludePaths.length);
	}

	/**
	 * Test relative paths.
	 */
	public void testRelativePath() throws Exception {
		// create a project
		String prjName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(prjName);
		String relativePath = "include";
		IFolder buildFolder = ResourceHelper.createFolder(project, "buildDir");
		IFolder relativeFolder = ResourceHelper.createFolder(project, "buildDir/"+relativePath);
		IFolder relativeFolderProjName = ResourceHelper.createFolder(project, "buildDir/"+prjName);
		String markedResolved = "-MarkedResolved";
		IFolder relativeFolderProjNameResolved = ResourceHelper.createFolder(project, "buildDir/" + prjName+markedResolved);
		IPath buildCWD=buildFolder.getLocation();
		setBuilderCWD(project, buildCWD);

		// get project/configuration descriptions
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// create sample file
		IFile file = ResourceHelper.createFile(project, "file.c");

		// contribute the entries
		CIncludePathEntry incRelativeEntry = new CIncludePathEntry(new Path(relativePath), 0);
		CIncludePathEntry incProjNameEntry = new CIncludePathEntry(new Path("${ProjName}"), 0);
		CIncludePathEntry incProjNameMarkedResolvedEntry = new CIncludePathEntry(new Path("${ProjName}"+markedResolved), ICSettingEntry.RESOLVED);
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(incRelativeEntry);
		entries.add(incProjNameEntry);
		entries.add(incProjNameMarkedResolvedEntry);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test the entries received from the scannerInfoProvider
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();

		// pair of entries, one from build dir another relative path
		assertEquals(relativeFolder.getLocation(), new Path(actualIncludePaths[0]));
		assertEquals(new Path(relativePath), new Path(actualIncludePaths[1]));

		// pair of entries, one resolved from build dir another expanded relative path
		assertEquals(relativeFolderProjName.getLocation(), new Path(actualIncludePaths[2]));
		assertEquals(new Path(prjName), new Path(actualIncludePaths[3]));

		// if marked RESOLVED only that path stays
		assertEquals(new Path("${ProjName}"+markedResolved), new Path(actualIncludePaths[4]));

		assertEquals(5, actualIncludePaths.length);
	}

	/**
	 * Test relative paths - some combinations of dot paths.
	 */
	public void testRelativePathWithDots() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		// set build CWD
		IFolder buildFolder = ResourceHelper.createFolder(project, "buildDir");
		IPath buildCWD=buildFolder.getLocation();
		setBuilderCWD(project, buildCWD);

		// define a few variations of paths
		String relativePath_dot = ".";
		String relativePath_dot_slash = "./";
		String relativePath_dot_slash_path = "./include";
		IFolder relativeFolder_dot_slash_path = ResourceHelper.createFolder(project, "buildDir/include");
		String relativePath_dotdot = "..";
		String relativePath_dotdot_slash = "../";
		String relativePath_dotdot_slash_path = "../include";
		IFolder relativeFolder_dotdot_slash_path = ResourceHelper.createFolder(project, "include");
		String locationPath_dotdot_path = buildCWD.toString()+"/../include2";
		IFolder incFolder_dotdot_slash_path = ResourceHelper.createFolder(project, "include2"); // "/ProjPath/buildDir/../include2"

		// get project/configuration descriptions
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// create sample file
		IFile file = ResourceHelper.createFile(project, "file.c");

		// contribute the entries
		CIncludePathEntry incRelativeEntry_dot = new CIncludePathEntry(new Path(relativePath_dot), 0);
		CIncludePathEntry incRelativeEntry_dot_slash_path = new CIncludePathEntry(new Path(relativePath_dot_slash_path), 0);
		CIncludePathEntry incRelativeEntry_dotdot = new CIncludePathEntry(new Path(relativePath_dotdot), 0);
		CIncludePathEntry incRelativeEntry_dotdot_slash_path = new CIncludePathEntry(new Path(relativePath_dotdot_slash_path), 0);
		CIncludePathEntry incEntry_dotdot_path = new CIncludePathEntry(locationPath_dotdot_path, 0);
		// use LOCAL flag not to clash with plain dot entries
		CIncludePathEntry incRelativeEntry_dotdot_slash = new CIncludePathEntry(new Path(relativePath_dotdot_slash), ICSettingEntry.LOCAL);
		CIncludePathEntry incRelativeEntry_dot_slash = new CIncludePathEntry(new Path(relativePath_dot_slash), ICSettingEntry.LOCAL);

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(incRelativeEntry_dot);
		entries.add(incRelativeEntry_dot_slash);
		entries.add(incRelativeEntry_dot_slash_path);
		entries.add(incRelativeEntry_dotdot);
		entries.add(incRelativeEntry_dotdot_slash);
		entries.add(incRelativeEntry_dotdot_slash_path);
		entries.add(incEntry_dotdot_path);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test the entries received from the scannerInfoProvider
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();
		String[] actualLocalIncludePaths = info.getLocalIncludePath();

		IPath expectedLocation_dot = buildFolder.getLocation();
		IPath expectedLocation_dot_slash = buildFolder.getLocation();
		IPath expectedLocation_dot_slash_path = relativeFolder_dot_slash_path.getLocation();
		IPath expectedLocation_dotdot = project.getLocation();
		IPath expectedLocation_dotdot_slash = project.getLocation();
		IPath expectedLocation_dotdot_slash_path = relativeFolder_dotdot_slash_path.getLocation();

		assertEquals(expectedLocation_dot, new Path(actualIncludePaths[0]));
		assertEquals(".", actualIncludePaths[1]);
		assertEquals(expectedLocation_dot_slash_path, new Path(actualIncludePaths[2]));
		assertEquals(new Path(relativePath_dot_slash_path), new Path(actualIncludePaths[3]));

		assertEquals(expectedLocation_dotdot, new Path(actualIncludePaths[4]));
		assertEquals("..", actualIncludePaths[5]);
		assertEquals(expectedLocation_dotdot_slash_path, new Path(actualIncludePaths[6]));
		assertEquals(new Path(relativePath_dotdot_slash_path), new Path(actualIncludePaths[7]));
		assertTrue(actualIncludePaths[7].startsWith(".."));
		assertEquals(new Path(locationPath_dotdot_path), new Path(actualIncludePaths[8]));
		assertTrue(actualIncludePaths[8].contains(".."));
		assertEquals(9, actualIncludePaths.length);

		assertEquals(expectedLocation_dot_slash, new Path(actualLocalIncludePaths[0]));
		assertEquals(new Path(relativePath_dot_slash), new Path(actualLocalIncludePaths[1]));
		assertTrue(actualLocalIncludePaths[1].startsWith("."));
		assertEquals(expectedLocation_dotdot_slash, new Path(actualLocalIncludePaths[2]));
		assertEquals(new Path(relativePath_dotdot_slash), new Path(actualLocalIncludePaths[3]));
		assertTrue(actualLocalIncludePaths[3].startsWith(".."));
		assertEquals(4, actualLocalIncludePaths.length);
	}

	/**
	 * Test if build/environment variables are expanded
	 */
	public void testEnvironmentVars() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		IFolder folder = ResourceHelper.createFolder(project, "Folder");
		String envPathStr = "${ProjDirPath}/Folder";

		// get project/configuration descriptions
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// create sample file
		IFile file = ResourceHelper.createFile(project, "file.c");

		// contribute the entries
		CIncludePathEntry incRelativeEntry = new CIncludePathEntry(envPathStr, 0);
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(incRelativeEntry);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test the entries received from the scannerInfoProvider
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();

		IPath expectedLocation = folder.getLocation();
		assertEquals(expectedLocation, new Path(actualIncludePaths[0]));
		assertEquals(1, actualIncludePaths.length);
	}

	/**
	 * Test from parent folder's entries.
	 */
	public void testParentFolder() throws Exception {
		class MockProviderForResource extends LanguageSettingsBaseProvider implements ILanguageSettingsProvider {
			private IResource rc;
			private final List<ICLanguageSettingEntry> entries;

			public MockProviderForResource(IResource rc, List<ICLanguageSettingEntry> entries) {
				this.rc = rc;
				this.entries = entries;
			}

			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (this.rc.equals(rc))
					return entries;
				return null;
			}
		}

		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// sample file
		IFolder parentFolder = ResourceHelper.createFolder(project, "ParentFolder");
		IFile file = ResourceHelper.createFile(project, "ParentFolder/file.c");

		// contribute the entries
		IFolder incFolder = ResourceHelper.createFolder(project, "include");
		CIncludePathEntry includePathEntry = new CIncludePathEntry(incFolder, 0);

		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(includePathEntry);

		// add provider for parent folder
		ILanguageSettingsProvider provider = new MockProviderForResource(parentFolder, entries);
		assertNull(provider.getSettingEntries(cfgDescription, file, null));
		assertEquals(includePathEntry, provider.getSettingEntries(cfgDescription, parentFolder, null).get(0));

		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test that the scannerInfoProvider gets the entries for
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();
		// include paths
		assertEquals(incFolder.getLocation(), new Path(actualIncludePaths[0]));
		assertEquals(1, actualIncludePaths.length);
	}

	/**
	 * Test resolved paths.
	 */
	public void testResolvedPath() throws Exception {
		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		IFolder folder = ResourceHelper.createFolder(project, "Folder");
		String envPathStr = "${ProjDirPath}/Folder";

		// get project/configuration descriptions
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// create sample file
		IFile file = ResourceHelper.createFile(project, "file.c");

		// contribute the entries
		CIncludePathEntry incRelativeEntry = new CIncludePathEntry(envPathStr, ICSettingEntry.RESOLVED);
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		entries.add(incRelativeEntry);

		// add provider to the configuration
		ILanguageSettingsProvider provider = new MockProvider(PROVIDER_ID, PROVIDER_NAME, entries);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test the entries received from the scannerInfoProvider
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(file);
		String[] actualIncludePaths = info.getIncludePaths();

		// test that RESOLVED entries are not modified
		IPath expectedLocation = new Path(envPathStr);
		assertEquals(expectedLocation, new Path(actualIncludePaths[0]));
		assertEquals(1, actualIncludePaths.length);
	}

	/**
	 * Get languages for the folder.
	 */
	private List<String> getLanguages(IFolder folder, ICConfigurationDescription cfgDescription) {
		IPath rcPath = folder.getProjectRelativePath();
		ICFolderDescription rcDes = (ICFolderDescription) cfgDescription.getResourceDescription(rcPath, false);
		ICLanguageSetting[] langSettings = rcDes.getLanguageSettings();
		assertNotNull(langSettings);

		List<String> languageIds = new ArrayList<String>();
		for (ICLanguageSetting ls : langSettings) {
			String langId = ls.getLanguageId();
			if (langId!=null && !languageIds.contains(langId)) {
				languageIds.add(langId);
			}
		}
		return languageIds;
	}

	/**
	 * Test composition of 2 languages.
	 */
	public void testResourceLanguages() throws Exception {
		 class MockProviderLang extends LanguageSettingsBaseProvider implements ILanguageSettingsProvider {
			private final String langId;
			private final List<ICLanguageSettingEntry> entries;

			public MockProviderLang(String id, String name, String langId, List<ICLanguageSettingEntry> entries) {
				super(id, name);
				this.langId = langId;
				this.entries = entries;
			}

			@Override
			public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
				if (langId==null || langId.equals(languageId))
					return entries;
				return new ArrayList<ICLanguageSettingEntry>();
			}
		}

		// create a project
		IProject project = ResourceHelper.createCDTProjectWithConfig(getName());
		IFolder folder = ResourceHelper.createFolder(project, "Folder");

		IFolder incFolderA = ResourceHelper.createFolder(project, "includeA");
		IFolder incFolderB = ResourceHelper.createFolder(project, "includeB");
		IFolder incFolderC = ResourceHelper.createFolder(project, "includeC");

		// get project/configuration descriptions
		ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, WRITEABLE);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
		assertNotNull(cfgDescription);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);

		// find 2 languages applicable to the folder
		List<String> languageIds = getLanguages(folder, cfgDescription);
		assertTrue(languageIds.size() >= 2);
		String langId1 = languageIds.get(0);
		String langId2 = languageIds.get(1);

		// define overlapping entries
		CIncludePathEntry incEntryA = new CIncludePathEntry(incFolderA, 0);
		CIncludePathEntry incEntryB = new CIncludePathEntry(incFolderB, 0);
		CIncludePathEntry incEntryC = new CIncludePathEntry(incFolderC, 0);
		List<ICLanguageSettingEntry> entries1 = new ArrayList<ICLanguageSettingEntry>();
		entries1.add(incEntryA);
		entries1.add(incEntryB);
		List<ICLanguageSettingEntry> entries2 = new ArrayList<ICLanguageSettingEntry>();
		entries2.add(incEntryC);
		entries2.add(incEntryB);

		// add providers to the configuration
		ILanguageSettingsProvider provider1 = new MockProviderLang(PROVIDER_ID, PROVIDER_NAME, langId1, entries1);
		ILanguageSettingsProvider provider2 = new MockProviderLang(PROVIDER_ID_2, PROVIDER_NAME, langId2, entries2);
		List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>();
		providers.add(provider1);
		providers.add(provider2);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);

		// set project description
		CProjectDescriptionManager.getInstance().setProjectDescription(project, prjDescription);

		// test the entries received from the scannerInfoProvider
		LanguageSettingsScannerInfoProvider scannerInfoProvider = new LanguageSettingsScannerInfoProvider();
		ExtendedScannerInfo info = scannerInfoProvider.getScannerInformation(folder);
		String[] actualIncludePaths = info.getIncludePaths();

		// Test that the result is the union of entries
		assertEquals(incFolderA.getLocation(), new Path(actualIncludePaths[0]));
		assertEquals(incFolderB.getLocation(), new Path(actualIncludePaths[1]));
		assertEquals(incFolderC.getLocation(), new Path(actualIncludePaths[2]));
		assertEquals(3, actualIncludePaths.length);
	}

}
