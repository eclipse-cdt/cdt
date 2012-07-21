/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.language.settings.providers.tests;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeSettings;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases to test build command parsers.
 */
public class GCCBuildCommandParserTest extends BaseTestCase {
	// ID of the parser taken from the extension point
	private static final String GCC_BUILD_COMMAND_PARSER_EXT = "org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser"; //$NON-NLS-1$

	private static final String PROVIDER_ID = "provider.id";
	private static final String PROVIDER_NAME = "provider name";
	private static final String LANGUAGE_ID = "language.test.id";
	private static final String CUSTOM_PARAMETER = "customParameter";
	private static final String CUSTOM_PARAMETER_2 = "customParameter2";
	private static final String ELEM_TEST = "test";
	private static final String LANG_CPP = GPPLanguage.ID;

	// those attributes must match that in AbstractBuildCommandParser
	private static final String ATTR_PARAMETER = "parameter"; //$NON-NLS-1$
	private static final String ATTR_KEEP_RELATIVE_PATHS = "keep-relative-paths"; //$NON-NLS-1$

	/**
	 * Mock build command parser.
	 */
	private class MockBuildCommandParser extends AbstractBuildCommandParser  implements Cloneable {
		public MockBuildCommandParser() {
			setId("GCCBuildCommandParserTest.MockBuildCommandParser");
		}
		@Override
		protected AbstractOptionParser[] getOptionParsers() {
			return new AbstractOptionParser[] {};
		}
		@Override
		public MockBuildCommandParser cloneShallow() throws CloneNotSupportedException {
			return (MockBuildCommandParser) super.cloneShallow();
		}
		@Override
		public MockBuildCommandParser clone() throws CloneNotSupportedException {
			return (MockBuildCommandParser) super.clone();
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			Job.getJobManager().join(AbstractBuildCommandParser.JOB_FAMILY_BUILD_COMMAND_PARSER, null);
			Job.getJobManager().join(LanguageSettingsProvidersSerializer.JOB_FAMILY_SERIALIZE_LANGUAGE_SETTINGS_PROJECT, null);
			Job.getJobManager().join(LanguageSettingsProvidersSerializer.JOB_FAMILY_SERIALIZE_LANGUAGE_SETTINGS_WORKSPACE, null);
		} catch (Exception e) {
			// ignore
		}
		super.tearDown();
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
	 * Sets build working directory for DefaultSettingConfiguration being tested.
	 */
	private void setBuilderCWD(IProject project, IPath buildCWD) throws CoreException {
		CProjectDescriptionManager manager = CProjectDescriptionManager.getInstance();
		{
			ICProjectDescription prjDescription = manager.getProjectDescription(project, true);
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
			ICProjectDescription prjDescription = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
			assertNotNull(cfgDescription);

		}
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

			final ICConfigurationDescription cfgDescriptionReferenced = getConfigurationDescriptions(projectReferenced)[0];
			cfgDescription.setReferenceInfo(new HashMap<String, String>() {{ put(projectReferenced.getName(), cfgDescriptionReferenced.getId()); }});
			coreModel.setProjectDescription(project, projectDescription);
		}

		{
			// doublecheck that it's set as expected
			ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
			ICConfigurationDescription cfgDescription = cfgDescriptions[0];
			Map<String,String> refs = cfgDescription.getReferenceInfo();
			assertEquals(1, refs.size());
			Set<String> referencedProjectsNames = new LinkedHashSet<String>(refs.keySet());
			assertEquals(projectReferenced.getName(), referencedProjectsNames.toArray()[0]);
		}

	}


	/**
	 * Test getters and setters.
	 */
	public void testAbstractBuildCommandParser_GettersSetters() throws Exception {
		{
			// provider configured with null parameters
			MockBuildCommandParser provider = new MockBuildCommandParser();
			provider.configureProvider(PROVIDER_ID, PROVIDER_NAME, null, null, null);

			assertEquals(PROVIDER_ID, provider.getId());
			assertEquals(PROVIDER_NAME, provider.getName());
			assertEquals(null, provider.getLanguageScope());
			assertEquals(null, provider.getSettingEntries(null, null, null));
			assertEquals("", provider.getCompilerPattern());
			assertEquals(AbstractBuildCommandParser.ResourceScope.FILE, provider.getResourceScope());
		}

		{
			// provider configured with non-null parameters
			MockBuildCommandParser provider = new MockBuildCommandParser();
			List<String> languages = new ArrayList<String>();
			languages.add(LANGUAGE_ID);
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(ATTR_PARAMETER, CUSTOM_PARAMETER);
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			ICLanguageSettingEntry entry = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
			entries.add(entry);

			provider.configureProvider(PROVIDER_ID, PROVIDER_NAME, languages, entries, properties);
			assertEquals(PROVIDER_ID, provider.getId());
			assertEquals(PROVIDER_NAME, provider.getName());
			assertEquals(languages, provider.getLanguageScope());
			assertEquals(entries, provider.getSettingEntries(null, null, null));
			assertEquals(CUSTOM_PARAMETER, provider.getCompilerPattern());

			// setters
			provider.setCompilerPattern(CUSTOM_PARAMETER_2);
			assertEquals(CUSTOM_PARAMETER_2, provider.getCompilerPattern());
			provider.setResourceScope(AbstractBuildCommandParser.ResourceScope.PROJECT);
			assertEquals(AbstractBuildCommandParser.ResourceScope.PROJECT, provider.getResourceScope());
		}
	}

	/**
	 * Test clone() and equals().
	 */
	public void testAbstractBuildCommandParser_CloneAndEquals() throws Exception {
		// create instance to compare to
		MockBuildCommandParser parser = new MockBuildCommandParser();
		assertEquals(true, parser.isResolvingPaths());

		// check clone after initialization
		MockBuildCommandParser clone0 = parser.clone();
		assertTrue(parser.equals(clone0));

		// configure provider
		parser.setResolvingPaths(false);
		assertFalse(parser.equals(clone0));
		parser.setResourceScope(AbstractBuildCommandParser.ResourceScope.PROJECT);
		assertEquals(AbstractBuildCommandParser.ResourceScope.PROJECT, parser.getResourceScope());
		parser.setResourceScope(AbstractBuildCommandParser.ResourceScope.FOLDER);
		assertEquals(AbstractBuildCommandParser.ResourceScope.FOLDER, parser.getResourceScope());
		parser.setResourceScope(AbstractBuildCommandParser.ResourceScope.FILE);
		assertEquals(AbstractBuildCommandParser.ResourceScope.FILE, parser.getResourceScope());

		// check another clone after changing settings
		{
			parser.setResolvingPaths(false);
			assertFalse(parser.equals(clone0));
			parser.setResourceScope(AbstractBuildCommandParser.ResourceScope.PROJECT);
			assertEquals(AbstractBuildCommandParser.ResourceScope.PROJECT, parser.getResourceScope());
			MockBuildCommandParser clone = parser.clone();
			assertTrue(parser.equals(clone));
			assertEquals(parser.isResolvingPaths(), clone.isResolvingPaths());
			assertEquals(parser.getResourceScope(), clone.getResourceScope());
		}

		// check 'expand relative paths' flag
		{
			MockBuildCommandParser clone = parser.clone();
			boolean expandRelativePaths = clone.isResolvingPaths();
			clone.setResolvingPaths( ! expandRelativePaths );
			assertFalse(parser.equals(clone));
		}

		// check resource scope
		{
			parser.setResourceScope(AbstractBuildCommandParser.ResourceScope.PROJECT);
			assertEquals(AbstractBuildCommandParser.ResourceScope.PROJECT, parser.getResourceScope());
			MockBuildCommandParser clone = parser.clone();
			assertEquals(AbstractBuildCommandParser.ResourceScope.PROJECT, clone.getResourceScope());
			clone.setResourceScope(AbstractBuildCommandParser.ResourceScope.FOLDER);
			assertFalse(parser.equals(clone));
		}

		// check cloneShallow()
		{
			MockBuildCommandParser parser2 = parser.clone();
			MockBuildCommandParser clone = parser2.cloneShallow();
			assertTrue(parser2.equals(clone));
		}

	}

	/**
	 * Test basic serialization functionality.
	 */
	public void testAbstractBuildCommandParser_SerializeDOM() throws Exception {
		{
			// create empty XML
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);

			// load it to new provider
			MockBuildCommandParser parser = new MockBuildCommandParser();
			parser.load(rootElement);
			assertEquals(true, parser.isResolvingPaths());
		}

		Element elementProvider;
		{
			// define mock parser
			MockBuildCommandParser parser = new MockBuildCommandParser();
			assertEquals(true, parser.isResolvingPaths());

			// redefine the settings
			parser.setResolvingPaths(false);
			assertEquals(false, parser.isResolvingPaths());

			// serialize in XML
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = parser.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);

			assertTrue(xmlString.contains(ATTR_KEEP_RELATIVE_PATHS));
		}
		{
			// create another instance of the provider
			MockBuildCommandParser parser = new MockBuildCommandParser();
			assertEquals(true, parser.isResolvingPaths());

			// load element
			parser.load(elementProvider);
			assertEquals(false, parser.isResolvingPaths());
		}
	}

	/**
	 * Smoke test exercising passing {@code null} to the functions.
	 */
	public void testAbstractBuildCommandParser_Nulls() throws Exception {
		MockBuildCommandParser parser = new MockBuildCommandParser();
		parser.startup(null, null);
		parser.processLine(null);
		parser.shutdown();

		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(null, null, null);
		assertNull(entries);
	}

	/**
	 * Test basic parsing functionality.
	 */
	public void testAbstractBuildCommandParser_Basic() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		final IFile file=ResourceHelper.createFile(project, "file.cpp");

		// create test class
		AbstractBuildCommandParser parser = new MockBuildCommandParser() {
			@Override
			public boolean processLine(String line) {
				// pretending that we parsed the line
				currentResource = file;
				List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
				ICLanguageSettingEntry entry = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN);
				entries.add(entry);
				setSettingEntries(entries);
				return true;
			}
		};
		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -DMACRO=VALUE file.cpp");
		parser.shutdown();

		// sanity check that it does not return same values for all inputs
		List<ICLanguageSettingEntry> noentries = parser.getSettingEntries(null, null, null);
		assertNull(noentries);

		// check populated entries
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CMacroEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN);
		assertEquals(expected, entries.get(0));
	}

	/**
	 * Test parsing of one typical entry.
	 */
	public void testOneEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			CIncludePathEntry expected = new CIncludePathEntry("/path0", 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
	}

	/**
	 * Test possible variations of compiler command.
	 */
	public void testGccFlavors() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file1=ResourceHelper.createFile(project, "file1.cpp");
		IFile file2=ResourceHelper.createFile(project, "file2.cpp");
		IFile file3=ResourceHelper.createFile(project, "file3.cpp");
		IFile file4=ResourceHelper.createFile(project, "file4.cpp");
		IFile file5=ResourceHelper.createFile(project, "file5.cpp");
		IFile file6=ResourceHelper.createFile(project, "file6.cpp");
		IFile file7=ResourceHelper.createFile(project, "file7.cpp");
		IFile file8=ResourceHelper.createFile(project, "file8.cpp");
		IFile file9=ResourceHelper.createFile(project, "file9.cpp");
		IFile file10=ResourceHelper.createFile(project, "file10.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file1.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 file1.cpp");
		parser.processLine("gcc-4.2 -I/path0 file2.cpp");
		parser.processLine("g++ -I/path0 file3.cpp");
		parser.processLine("c++ -I/path0 file4.cpp");
		parser.processLine("\"gcc\" -I/path0 file5.cpp");
		parser.processLine("/absolute/path/gcc -I/path0 file6.cpp");
		parser.processLine(" \"/absolute/path/gcc\" -I/path0 file7.cpp");
		parser.processLine("../relative/path/gcc -I/path0 file8.cpp");
		parser.processLine("clang -I/path0 file9.cpp");
		parser.processLine("clang++ -I/path0 file10.cpp");
		parser.shutdown();

		// check populated entries
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file1, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file2, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file3, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file4, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file5, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file6, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file7, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file8, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file9, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file10, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
	}

	/**
	 * Parse variations of -I options.
	 */
	public void testCIncludePathEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc"
				// regular
				+ " -I/path0 "
				// space after -I
				+ " -I /path1 "
				// unknown option, should be ignored
				+ " -? "
				// double-quoted path with spaces
				+ " -I\"/path with spaces\""
				// single-quoted path with spaces
				+ " -I'/path with spaces2'"
				// second single-quoted and space after -I
				+ " -I '/path with spaces3'"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CIncludePathEntry expected = new CIncludePathEntry("/path0", 0);
		CIncludePathEntry entry = (CIncludePathEntry)entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CIncludePathEntry("/path1", 0), entries.get(1));
		assertEquals(new CIncludePathEntry("/path with spaces", 0), entries.get(2));
		assertEquals(new CIncludePathEntry("/path with spaces2", 0), entries.get(3));
		assertEquals(new CIncludePathEntry("/path with spaces3", 0), entries.get(4));
	}

	/**
	 * Parse Mac Frameworks.
	 */
	public void testCIncludePathEntryFrameworks() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc"
				// framework
				+ " -F/Framework "
				// framework system
				+ " -iframework/framework/system "
				// with spaces
				+ " -F '/Framework with spaces' "
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry("/Framework", ICSettingEntry.FRAMEWORKS_MAC), entries.get(0));
		assertEquals(new CIncludePathEntry("/framework/system", ICSettingEntry.FRAMEWORKS_MAC), entries.get(1));
		assertEquals(new CIncludePathEntry("/Framework with spaces", ICSettingEntry.FRAMEWORKS_MAC), entries.get(2));
	}

	/**
	 * Parse variations of -D options.
	 */
	public void testCMacroEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -DMACRO0"
				+ " -DMACRO1=value"
				+ " -DMACRO2=\"value with spaces\""
				+ " -DMACRO3='value with spaces'"
				+ " -DMACRO4='\"quoted value\"'"
				+ " -D'MACRO5=\"quoted value\"'"
				+ " -DMACRO6=\\\"escape-quoted value\\\""
				+ " -DMACRO7=\"'single-quoted value'\""
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CMacroEntry expected = new CMacroEntry("MACRO0", "", 0);
		CMacroEntry entry = (CMacroEntry)entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CMacroEntry("MACRO1", "value", 0), entries.get(1));
		assertEquals(new CMacroEntry("MACRO2", "value with spaces", 0), entries.get(2));
		assertEquals(new CMacroEntry("MACRO3", "value with spaces", 0), entries.get(3));
		assertEquals(new CMacroEntry("MACRO4", "\"quoted value\"", 0), entries.get(4));
		assertEquals(new CMacroEntry("MACRO5", "\"quoted value\"", 0), entries.get(5));
		assertEquals(new CMacroEntry("MACRO6", "\"escape-quoted value\"", 0), entries.get(6));
		assertEquals(new CMacroEntry("MACRO7", "'single-quoted value'", 0), entries.get(7));
	}

	/**
	 * Parse -U option.
	 */
	public void testCMacroEntry_undef() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -UMACRO"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CMacroEntry("MACRO", null, ICSettingEntry.UNDEFINED), entries.get(0));
	}

	/**
	 * Parse variations of -include options.
	 */
	public void testCIncludeFileEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -include /include.file1"
				+ " -include '/include.file with spaces'"
				+ " -include ../../include.file2"
				+ " -include include.file3"
				+ " -include ../../include-file-with-dashes"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CIncludeFileEntry expected = new CIncludeFileEntry("/include.file1", 0);
		CIncludeFileEntry entry = (CIncludeFileEntry)entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CIncludeFileEntry("/include.file with spaces", 0), entries.get(1));
		assertEquals(new CIncludeFileEntry(project.getLocation().removeLastSegments(2).append("include.file2"), 0), entries.get(2));
		assertEquals(new CIncludeFileEntry(project.getFullPath().append("include.file3"), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(3));
		assertEquals(new CIncludeFileEntry(project.getLocation().removeLastSegments(2).append("include-file-with-dashes"), 0), entries.get(4));
	}

	/**
	 * Parse variations of -macros options.
	 */
	public void testCMacroFileEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -macros macro.file file.cpp");
		parser.processLine("gcc "
				+ " -macros /macro.file"
				+ " -macros '/macro.file with spaces'"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CMacroFileEntry expected = new CMacroFileEntry("/macro.file", 0);
		CMacroFileEntry entry = (CMacroFileEntry)entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CMacroFileEntry("/macro.file with spaces", 0), entries.get(1));

	}

	/**
	 * Parse variations of -L options.
	 */
	public void testCLibraryPathEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -L/path0"
				+ " -L'/path with spaces'"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CLibraryPathEntry expected = new CLibraryPathEntry("/path0", 0);
		CLibraryPathEntry entry = (CLibraryPathEntry)entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CLibraryPathEntry("/path with spaces", 0), entries.get(1));
	}

	/**
	 * Parse variations of -l options.
	 */
	public void testCLibraryFileEntry() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -ldomain file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CLibraryFileEntry expected = new CLibraryFileEntry("libdomain.a", 0);
		CLibraryFileEntry entry = (CLibraryFileEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);
	}

	/**
	 * Parse mixed options in the same command.
	 */
	public void testMixedSettingEntries() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc"
				+ " -I/path0 "
				+ " -DMACRO1=value"
				+ " -v"
				+ " -ldomain"
				+ " -E"
				+ " -I /path1 "
				+ " -DMACRO2=\"value with spaces\""
				+ " -I\"/path with spaces\""
				+ " -o file.exe"
				+ " -L/usr/lib"
				+ " file.cpp"
				+ " -mtune=pentiumpro"
			);
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		assertEquals(new CIncludePathEntry("/path1", 0), entries.get(1));
		assertEquals(new CIncludePathEntry("/path with spaces", 0), entries.get(2));
		assertEquals(new CMacroEntry("MACRO1", "value", 0), entries.get(3));
		assertEquals(new CMacroEntry("MACRO2", "value with spaces", 0), entries.get(4));
		assertEquals(new CLibraryPathEntry("/usr/lib", 0), entries.get(5));
		assertEquals(new CLibraryFileEntry("libdomain.a", 0), entries.get(6));
		assertEquals(7, entries.size());
	}

	/**
	 * Parse command where resource is missing.
	 */
	public void testFileMissing() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 missing.cpp");
		parser.shutdown();

		// check entries
		assertTrue(parser.isEmpty());
	}

	/**
	 * Parsing of absolute path to the file being compiled.
	 */
	public void testFileAbsolutePath() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ "-I/path0 "
				+ "-I. "
				+ file.getLocation().toOSString());
		parser.shutdown();

		// check entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		assertEquals(new CIncludePathEntry(project.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
	}

	/**
	 * Parsing of absolute path to the file being compiled where provider is global.
	 */
	public void testFileAbsolutePath_NoProject() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(null, null);
		parser.processLine("gcc "
				+ "-I/path0 "
				+ "-I. "
				+ file.getLocation().toOSString());
		parser.shutdown();

		// check entries
		assertEquals(new CIncludePathEntry("/path0", 0), parser.getSettingEntries(null, file, languageId).get(0));
		assertEquals(new CIncludePathEntry(file.getParent().getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), parser.getSettingEntries(null, file, languageId).get(1));
	}

	/**
	 * Parsing where the name of the file being compiled contains spaces.
	 */
	public void testFileWithSpaces() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file1=ResourceHelper.createFile(project, "file with spaces 1.cpp");
		IFile file2=ResourceHelper.createFile(project, "file with spaces 2.cpp");
		IFile file3=ResourceHelper.createFile(project, "file with spaces 3.cpp");
		IFile file4=ResourceHelper.createFile(project, "file with spaces 4.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file1.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 'file with spaces 1.cpp'");
		parser.processLine("gcc -I/path0 \"file with spaces 2.cpp\"");
		parser.processLine("gcc -I/path0 'file with spaces 3.cpp'\n");
		parser.processLine("gcc -I/path0 'file with spaces 4.cpp'\r\n");
		parser.shutdown();

		// check populated entries
		// in single quotes
		assertEquals(new CIncludePathEntry("/path0", 0), parser.getSettingEntries(cfgDescription, file1, languageId).get(0));
		// in double quotes
		assertEquals(new CIncludePathEntry("/path0", 0), parser.getSettingEntries(cfgDescription, file2, languageId).get(0));
		// Unix EOL
		assertEquals(new CIncludePathEntry("/path0", 0), parser.getSettingEntries(cfgDescription, file3, languageId).get(0));
		// Windows EOL
		assertEquals(new CIncludePathEntry("/path0", 0), parser.getSettingEntries(cfgDescription, file4, languageId).get(0));
	}

	/**
	 * Resolve disagreement between working directory and path to the file being compiled.
	 */
	public void testFileIgnoreWrongBuildDir() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		IFolder folder1=ResourceHelper.createFolder(project, "Folder1");
		IFile file=ResourceHelper.createFile(project, "Folder1/Folder2/file.cpp");
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);
		// Shift build directory, that could happen if Make Target from folder1 was run
		// Build directory points to /project/Folder1/
		IFolder buildDir = folder1;
		epm.pushDirectoryURI(buildDir.getLocationURI());

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ "-I/path0 "
				+ "-I. "
				// This implies the build working directory is /project/
				+ "Folder1/Folder2/file.cpp");
		parser.shutdown();

		// check entries
		assertEquals(new CIncludePathEntry("/path0", 0), parser.getSettingEntries(cfgDescription, file, languageId).get(0));
		// Information from build output should take precedence over build dir
		assertEquals(new CIncludePathEntry(project.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), parser.getSettingEntries(cfgDescription, file, languageId).get(1));
	}

	/**
	 * Test various ends of lines for the lines being parsed.
	 */
	public void testEndOfLine() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file0=ResourceHelper.createFile(project, "file0.cpp");
		IFile file1=ResourceHelper.createFile(project, "file1.cpp");
		IFile file2=ResourceHelper.createFile(project, "file2.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file0.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 file0.cpp");
		parser.processLine("gcc -I/path0 file1.cpp\n");
		parser.processLine("gcc -I/path0 file2.cpp\r\n");
		parser.shutdown();

		// check populated entries
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file0, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file1, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file2, languageId);
			assertEquals(new CIncludePathEntry("/path0", 0), entries.get(0));
		}
	}

	/**
	 * Test parsing of paths located on a different drive on Windows.
	 */
	public void testPathEntry_DriveLetter() throws Exception {
		// do not test on non-windows systems where drive letters are not supported
		if (! Platform.getOS().equals(Platform.OS_WIN32))
			return;

		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		parser.setResolvingPaths(true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -IX:\\path"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(new Path("X:\\path"), 0), entries.get(0));
	}

	/**
	 * Test various relative paths provided in options with resolving.
	 */
	public void testPathEntry_ExpandRelativePath() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		IFolder folder=ResourceHelper.createFolder(project, "Folder");
		IFolder folderComposite=ResourceHelper.createFolder(project, "Folder-Icomposite");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		parser.setResolvingPaths(true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -IFolder"
				+ " -IFolder-Icomposite" // to test case when "-I" is a part of folder name
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		// check that relative paths are relative to CWD which is the location of the project
		assertEquals(new CIncludePathEntry(project.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry(project.getLocation().removeLastSegments(1), 0), entries.get(1));
		assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(2));
		assertEquals(new CIncludePathEntry(folderComposite.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(3));
	}

	/**
	 * Test various relative paths provided in options without resolving.
	 */
	public void testPathEntry_DoNotExpandRelativePath() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		@SuppressWarnings("unused")
		IFolder folder=ResourceHelper.createFolder(project, "Folder");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser with expandRelativePaths=false
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		parser.setResolvingPaths(false);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -IFolder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(".", 0), entries.get(0));
		assertEquals(new CIncludePathEntry("..", 0), entries.get(1));
		assertEquals(new CIncludePathEntry("Folder", 0), entries.get(2));
	}

	/**
	 * Ensure that duplicate paths are ignored.
	 */
	public void testPathEntry_DuplicatePath() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		IFolder folder=ResourceHelper.createFolder(project, "Folder");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		parser.setResolvingPaths(true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -IFolder"
				+ " -IFolder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(1, entries.size());
	}

	/**
	 * Test that working directory supplied by ErrorParserManager is considered.
	 */
	public void testPathEntry_FollowCWD() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFolder buildDir=ResourceHelper.createFolder(project, "BuildDir");
		IFolder folder=ResourceHelper.createFolder(project, "BuildDir/Folder");
		IFile file=ResourceHelper.createFile(project, "BuildDir/file.cpp");
		@SuppressWarnings("unused")
		IFile fakeFile=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);
		// Set different working directory
		epm.pushDirectoryURI(buildDir.getLocationURI());

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -I../../.."
				+ " -IFolder"
				+ " -IMissingFolder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry(buildDir.getFullPath().removeLastSegments(1), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
		assertEquals(new CIncludePathEntry(buildDir.getLocation().removeLastSegments(3), 0), entries.get(2));
		assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(3));
		assertEquals(new CIncludePathEntry(buildDir.getFullPath().append("MissingFolder"), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(4));
	}

	/**
	 * Determine working directory basing on file being compiled.
	 */
	public void testPathEntry_GuessCWD() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFolder folder=ResourceHelper.createFolder(project, "BuildDir/Folder");
		IFile file=ResourceHelper.createFile(project, "BuildDir/file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -IFolder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
	}

	/**
	 * Test case when build command indicates impossible working directory.
	 */
	public void testPathEntry_NonExistentCWD_Workspace() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFolder buildDir=project.getFolder("Missing/Folder");
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		ErrorParserManager epm = new ErrorParserManager(project, null);
		// define working directory
		epm.pushDirectoryURI(buildDir.getLocationURI());

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -IFolder"
				// indicates non-existing working directory
				+ " ../file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry(buildDir.getFullPath().removeLastSegments(1), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
		assertEquals(new CIncludePathEntry(buildDir.getFullPath().append("Folder"), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(2));
	}

	/**
	 * Test case when build command indicates impossible working directory and
	 * ErrorParserManager indicates non-existing working directory.
	 */
	public void testPathEntry_NonExistentCWD_Filesystem() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		ErrorParserManager epm = new ErrorParserManager(project, null);
		URI uriBuildDir = new URI("file:/non-existing/path");
		// define non-existing working directory
		epm.pushDirectoryURI(uriBuildDir);

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -IFolder"
				// indicates non-existing working directory
				+ " ../file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		IPath buildPath = new Path(uriBuildDir.getPath()).setDevice(project.getLocation().getDevice());
		assertEquals(new CIncludePathEntry(buildPath, 0), entries.get(0));
		assertEquals(new CIncludePathEntry(buildPath.removeLastSegments(1), 0), entries.get(1));
		assertEquals(new CIncludePathEntry(buildPath.append("Folder"), 0), entries.get(2));
	}

	/**
	 * Simulate mapping of a sub-folder in the project to remote URI.
	 */
	public void testPathEntry_MappedRemoteFolder() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFolder buildDir=ResourceHelper.createFolder(project, "Local/BuildDir");
		IFolder folder=ResourceHelper.createFolder(project, "Local/BuildDir/Folder");
		IFolder folder2=ResourceHelper.createFolder(project, "Local/BuildDir/Folder2");
		IFile file=ResourceHelper.createFile(project, "Local/BuildDir/file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		ErrorParserManager epm = new ErrorParserManager(project, null);
		// define working directory as URI pointing outside workspace
		URI uriBuildDir = new URI("file:/BuildDir");
		epm.pushDirectoryURI(uriBuildDir);

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -I."
				+ " -I/BuildDir/Folder"
				+ " -I../BuildDir/Folder2"
				+ " -I/BuildDir/MissingFolder"
				+ " -I../BuildDir/MissingFolder2"
				+ " /BuildDir/file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
		assertEquals(new CIncludePathEntry(folder2.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(2));
		assertEquals(new CIncludePathEntry("/BuildDir/MissingFolder", 0), entries.get(3));
		assertEquals(new CIncludePathEntry(buildDir.getFullPath().append("MissingFolder2"), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(4));
	}

	/**
	 * Test mapping folders heuristics - inside a project.
	 */
	public void testPathEntry_MappedFolderInProject() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "BuildDir/file.cpp");
		IFolder mappedFolder=ResourceHelper.createFolder(project, "Mapped/Folder");
		IFolder folder=ResourceHelper.createFolder(project, "Mapped/Folder/Subfolder");
		@SuppressWarnings("unused")
		IFolder ambiguousFolder1=ResourceHelper.createFolder(project, "One/Ambiguous/Folder");
		@SuppressWarnings("unused")
		IFolder ambiguousFolder2=ResourceHelper.createFolder(project, "Another/Ambiguous/Folder");

		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -I/Folder/Subfolder"
				+ " -I/Mapped/Folder"
				+ " -I/Ambiguous/Folder"
				+ " -I/Missing/Folder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry(mappedFolder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
		assertEquals(new CIncludePathEntry("/Ambiguous/Folder", 0), entries.get(2));
		assertEquals(new CIncludePathEntry("/Missing/Folder", 0), entries.get(3));
	}

	/**
	 * Test mapping folders heuristics - mapping to another project.
	 */
	public void testPathEntry_MappedFolderInAnotherProject() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		// create files and folders
		IFile file=ResourceHelper.createFile(project, "file.cpp");
		// another project
		IProject anotherProject = ResourceHelper.createCDTProjectWithConfig(projectName+"-another");
		IFolder folder=ResourceHelper.createFolder(anotherProject, "Mapped/Folder/Subfolder");
		@SuppressWarnings("unused")
		IFolder ambiguousFolder1=ResourceHelper.createFolder(anotherProject, "One/Ambiguous/Folder");
		@SuppressWarnings("unused")
		IFolder ambiguousFolder2=ResourceHelper.createFolder(anotherProject, "Another/Ambiguous/Folder");


		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -I/Folder/Subfolder"
				+ " -I/Ambiguous/Folder"
				+ " -I/Missing/Folder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry("/Ambiguous/Folder", 0), entries.get(1));
		assertEquals(new CIncludePathEntry("/Missing/Folder", 0), entries.get(2));
	}

	/**
	 * Test mapping folders heuristics - mapping to a referenced project.
	 */
	public void testPathEntry_MappedFolderInReferencedProject() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();

		// create main project
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		IFile file=ResourceHelper.createFile(project, "file.cpp");

		// create another project (non-referenced)
		IProject anotherProject = ResourceHelper.createCDTProjectWithConfig(projectName+"-another");
		@SuppressWarnings("unused")
		IFolder folderInAnotherProject=ResourceHelper.createFolder(anotherProject, "Mapped/Folder/Subfolder");

		// create referenced project
		IProject referencedProject = ResourceHelper.createCDTProjectWithConfig(projectName+"-referenced");
		IFolder folderInReferencedProject=ResourceHelper.createFolder(referencedProject, "Mapped/Folder/Subfolder");
		@SuppressWarnings("unused")
		IFolder ambiguousFolder1=ResourceHelper.createFolder(referencedProject, "One/Ambiguous/Folder");
		@SuppressWarnings("unused")
		IFolder ambiguousFolder2=ResourceHelper.createFolder(referencedProject, "Another/Ambiguous/Folder");

		setReference(project, referencedProject);

		// get cfgDescription and language to work with
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -I/Folder/Subfolder"
				+ " -I/Ambiguous/Folder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(folderInReferencedProject.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry("/Ambiguous/Folder", 0), entries.get(1));
	}

	/**
	 * Test ".." in symbolic links where the symbolic link is present as absolute path.
	 */
	public void testPathEntry_NavigateSymbolicLinkUpAbsolute() throws Exception {
		// do not test on systems where symbolic links are not supported
		if (!ResourceHelper.isSymbolicLinkSupported())
			return;

		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		String languageId = LANG_CPP;
		IFile file=ResourceHelper.createFile(project, "file.cpp");

		// create link on the filesystem
		IPath dir1 = ResourceHelper.createTemporaryFolder();
		IPath dir2 = dir1.removeLastSegments(1);
		IPath linkPath = dir1.append("linked");
		ResourceHelper.createSymbolicLink(linkPath, dir2);

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription, epm);
		// "../" should navigate along filesystem path, not along the link itself
		parser.processLine("gcc -I"+linkPath.toString()+"/.."+" file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CIncludePathEntry expected = new CIncludePathEntry(dir2.removeLastSegments(1), 0);
		assertEquals(expected, entries.get(0));
	}

	/**
	 * Test ".." in symbolic links where the symbolic link is present as relative path.
	 */
	public void testPathEntry_NavigateSymbolicLinkUpRelative() throws Exception {
		// do not test on systems where symbolic links are not supported
		if (!ResourceHelper.isSymbolicLinkSupported())
			return;

		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		String languageId = LANG_CPP;
		IFile file=ResourceHelper.createFile(project, "file.cpp");

		// create link
		IFolder folder = ResourceHelper.createFolder(project, "folder");
		IFolder subfolder = ResourceHelper.createFolder(project, "folder/subfolder");
		IPath linkPath = project.getLocation().append("linked");
		ResourceHelper.createSymbolicLink(linkPath, subfolder.getLocation());

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription, epm);
		// "../" should navigate along filesystem path, not along the link itself
		parser.processLine("gcc -Ilinked/.."+" file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		CIncludePathEntry expected = new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
		assertEquals(expected, entries.get(0));
	}

	/**
	 * Determine working directory from configuration builder settings.
	 */
	public void testPathEntry_BuildDirDefinedByConfiguration_RelativePath() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		// Create resources trying to confuse the parser
		@SuppressWarnings("unused")
		IFile fileInProjectRoot=ResourceHelper.createFile(project, "file.cpp");
		@SuppressWarnings("unused")
		IFolder includeDirInProjectRoot=ResourceHelper.createFolder(project, "include");
		// Create resources meant to be found
		IFolder buildDir=ResourceHelper.createFolder(project, "BuildDir");
		IFile file=ResourceHelper.createFile(project, "BuildDir/file.cpp");
		IFolder includeDir=ResourceHelper.createFolder(project, "BuildDir/include");
		// Change build dir
		setBuilderCWD(project, buildDir.getLocation());

		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -I."
				+ " -Iinclude"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry(includeDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
	}

	/**
	 * Test where working directory from command line disagrees with configuration builder settings.
	 */
	public void testPathEntry_BuildDirDefinedByConfiguration_AbsolutePath() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		// Create resources trying to confuse the parser
		@SuppressWarnings("unused")
		IFile fileInProjectRoot=ResourceHelper.createFile(project, "file.cpp");
		@SuppressWarnings("unused")
		IFolder includeDirInProjectRoot=ResourceHelper.createFolder(project, "include");
		// Create resources meant to be found
		IFolder buildDir=ResourceHelper.createFolder(project, "BuildDir");
		IFile file=ResourceHelper.createFile(project, "BuildDir/file.cpp");
		IFolder includeDir=ResourceHelper.createFolder(project, "BuildDir/include");
		// Change build dir
		setBuilderCWD(project, buildDir.getLocation());

		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc "
				+ " -I."
				+ " -Iinclude"
				+ " " + file.getLocation().toOSString()
			);
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry(includeDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
	}

	/**
	 * Smoke test when non-C files appear in output, should not choke.
	 */
	public void testContentType_None() throws Exception {
		MockBuildCommandParser parser = new MockBuildCommandParser() {
			@Override
			protected String parseResourceName(String line) {
				return "file.wrong-content-type";
			}
		};
		parser.startup(null, null);
		parser.processLine("gcc file.wrong-content-type");
		parser.shutdown();

		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(null, null, null);
		assertNull(entries);
	}

	/**
	 * Test that unsupported language is ignored.
	 */
	public void testContentType_Mismatch() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		ResourceHelper.createFile(project, "file.c");

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		// restrict the parser's language scope to C++ only
		parser.setLanguageScope(new ArrayList<String>() {{add(LANG_CPP);}});

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 file.c");
		parser.shutdown();

		assertTrue(parser.isEmpty());
	}

	/**
	 * Test custom file extensions defined in content type.
	 */
	public void testContentType_FileExtensions() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		String languageId = LANG_CPP;
		// add custom extension to C++ content type
		IContentTypeManager manager = Platform.getContentTypeManager();
		IContentType contentType = manager.findContentTypeFor("file.cpp");
		contentType.addFileSpec("x++", IContentTypeSettings.FILE_EXTENSION_SPEC);

		IFile file=ResourceHelper.createFile(project, "file.x++");
		IContentType contentTypeX = manager.findContentTypeFor("file.x++");
		assertEquals(contentType, contentTypeX);

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 file.x++");
		parser.shutdown();

		// check populated entries
		assertEquals(new CIncludePathEntry("/path0", 0), parser.getSettingEntries(cfgDescription, file, languageId).get(0));

		// cleanup
		contentType.removeFileSpec("x++", IContentTypeSettings.FILE_EXTENSION_SPEC);
	}

	/**
	 * Test filenames appearing in upper-case.
	 */
	public void testUpperCase() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		String languageId = LANG_CPP;

		IFile file=ResourceHelper.createFile(project, "file.cpp");

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc -I/path0 FILE.CPP");
		parser.shutdown();

		// check populated entries
		assertEquals(new CIncludePathEntry("/path0", 0), parser.getSettingEntries(cfgDescription, file, languageId).get(0));
	}

	/**
	 * Test sample output of boost builder utility bjam.
	 */
	public void testBoostBjam() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "libs/python/src/numeric.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);

		// parse line
		parser.startup(cfgDescription, null);
		//    "g++"  -ftemplate-depth-128 -O0 -fno-inline -Wall -g -mthreads  -DBOOST_ALL_NO_LIB=1 -DBOOST_PYTHON_SOURCE -DBOOST_PYTHON_STATIC_LIB  -I"." -I"/Python25/Include" -c -o "bin.v2/libs/python/build/gcc-mingw-3.4.5/debug/link-static/threading-multi/numeric.o" "libs/python/src/numeric.cpp"
		parser.processLine("   \"g++\"" +
				" -ftemplate-depth-128 -O0 -fno-inline -Wall -g -mthreads" +
				" -DBOOST_ALL_NO_LIB=1" +
				" -DBOOST_PYTHON_SOURCE" +
				" -DBOOST_PYTHON_STATIC_LIB" +
				" -I\".\"" +
				" -I\"/Python1025/Include\"" +
				" -c -o \"bin.v2/libs/python/build/gcc-mingw-3.4.5/debug/link-static/threading-multi/numeric.o\"" +
				" libs/python/src/numeric.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(project.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		// "/Python1025/Include" not expected to be there
		assertFalse(new java.io.File("/Python1025/Include").exists());
		assertEquals(new CIncludePathEntry("/Python1025/Include", 0), entries.get(1));
		assertEquals(new CMacroEntry("BOOST_ALL_NO_LIB", "1", 0), entries.get(2));
		assertEquals(new CMacroEntry("BOOST_PYTHON_SOURCE", "", 0), entries.get(3));
		assertEquals(new CMacroEntry("BOOST_PYTHON_STATIC_LIB", "", 0), entries.get(4));
		assertEquals(5, entries.size());
	}

	/**
	 * Test resource file residing on EFS file-system.
	 */
	public void testPathEntry_Efs() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		// create folder structure
		@SuppressWarnings("unused")
		IFolder buildDir=ResourceHelper.createEfsFolder(project, "BuildDir", new URI("mem:/EfsProject/BuildDir"));
		IFolder folder=ResourceHelper.createEfsFolder(project, "BuildDir/Folder", new URI("mem:/EfsProject/BuildDir/Folder"));
		IFile file=ResourceHelper.createEfsFile(project, "BuildDir/file.cpp", new URI("mem:/EfsProject/BuildDir/file.cpp"));
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -IFolder"
				+ " -I/Absolute/Folder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		String device = project.getLocation().getDevice();
		assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		assertEquals(new CIncludePathEntry(new Path("/Absolute/Folder").setDevice(device), 0), entries.get(1));
	}

	/**
	 * Test mapping entries to EFS.
	 */
	public void testPathEntry_EfsMappedFolder() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		// create folder structure
		@SuppressWarnings("unused")
		IFolder buildDir=ResourceHelper.createEfsFolder(project, "BuildDir", new URI("mem:/MappedEfsProject/BuildDir"));
		@SuppressWarnings("unused")
		IFolder folder=ResourceHelper.createEfsFolder(project, "BuildDir/Folder", new URI("mem:/MappedEfsProject/BuildDir/Folder"));
		IFile file=ResourceHelper.createEfsFile(project, "BuildDir/file.cpp", new URI("mem:/MappedEfsProject/BuildDir/file.cpp"));
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription, epm);
		parser.processLine("gcc "
				+ " -I/BeingMappedFrom/Folder" // mapped to local folder in EFSExtensionProvider extension point
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		String device = project.getLocation().getDevice();
		assertEquals(new CIncludePathEntry(new Path("/LocallyMappedTo/Folder").setDevice(device), 0), entries.get(0));
	}

	/**
	 * Test assigning entries on file level.
	 */
	public void testEntriesFileLevel() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFolder folder=ResourceHelper.createFolder(project, "folder");
		IFile file=ResourceHelper.createFile(project, "folder/file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		parser.setResourceScope(AbstractBuildCommandParser.ResourceScope.FILE);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 folder/file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> expected = new ArrayList<ICLanguageSettingEntry>();
		expected.add(new CIncludePathEntry("/path0", 0));
		assertEquals(expected, parser.getSettingEntries(cfgDescription, file, languageId));
		assertEquals(null, parser.getSettingEntries(cfgDescription, folder, languageId));
		assertEquals(null, parser.getSettingEntries(cfgDescription, project, languageId));
	}

	/**
	 * Test assigning entries on folder level.
	 */
	public void testEntriesFolderLevel() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFolder folder=ResourceHelper.createFolder(project, "folder");
		IFile file=ResourceHelper.createFile(project, "folder/file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		parser.setResourceScope(AbstractBuildCommandParser.ResourceScope.FOLDER);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 folder/file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> expected = new ArrayList<ICLanguageSettingEntry>();
		expected.add(new CIncludePathEntry("/path0", 0));
		assertEquals(null, parser.getSettingEntries(cfgDescription, file, languageId));
		assertEquals(expected, parser.getSettingEntries(cfgDescription, folder, languageId));
		assertEquals(null, parser.getSettingEntries(cfgDescription, project, languageId));
	}

	/**
	 * Test assigning entries on project level.
	 */
	public void testEntriesProjectLevel() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFolder folder=ResourceHelper.createFolder(project, "folder");
		IFile file=ResourceHelper.createFile(project, "folder/file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		parser.setResourceScope(AbstractBuildCommandParser.ResourceScope.PROJECT);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 folder/file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> expected = new ArrayList<ICLanguageSettingEntry>();
		expected.add(new CIncludePathEntry("/path0", 0));

		assertEquals(null, parser.getSettingEntries(cfgDescription, file, languageId));
		assertEquals(null, parser.getSettingEntries(cfgDescription, folder, languageId));
		assertEquals(expected, parser.getSettingEntries(cfgDescription, project, languageId));
	}

	/**
	 * Test assigning entries for global provider.
	 */
	public void testEntriesProjectLevelGlobalProvider() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		IFile file=ResourceHelper.createFile(project, "file.cpp");
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		ILanguageSettingsProvider wspProvider = LanguageSettingsManager.getWorkspaceProvider(GCC_BUILD_COMMAND_PARSER_EXT);
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getRawProvider(wspProvider);
		parser.setResourceScope(AbstractBuildCommandParser.ResourceScope.PROJECT);

		// parse line
		parser.startup(cfgDescription, null);
		parser.processLine("gcc -I/path0 file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> expected = new ArrayList<ICLanguageSettingEntry>();
		expected.add(new CIncludePathEntry("/path0", 0));
		assertEquals(expected, parser.getSettingEntries(null, project, languageId));
	}

}
