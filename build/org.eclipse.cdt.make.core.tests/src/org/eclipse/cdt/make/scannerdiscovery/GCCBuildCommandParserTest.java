/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.scannerdiscovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.cdt.core.ErrorParserManager;
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
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuildCommandParser;
import org.eclipse.cdt.make.core.scannerconfig.GCCBuildCommandParser;
import org.eclipse.cdt.make.core.scannerconfig.ILanguageSettingsBuildOutputScanner;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GCCBuildCommandParserTest extends TestCase {
	// ID of the parser taken from the extension point
	private static final String GCC_BUILD_COMMAND_PARSER_EXT = "org.eclipse.cdt.make.core.build.command.parser.gcc"; //$NON-NLS-1$
	
	private static final String ELEM_TEST = "test";
	private static final String LANG_CPP = "org.eclipse.cdt.core.g++";

	// those attributes must match that in AbstractBuiltinSpecsDetector
	private static final String ATTR_EXPAND_RELATIVE_PATHS = "expand-relative-paths"; //$NON-NLS-1$

	private class MockBuildCommandParser extends AbstractBuildCommandParser  implements Cloneable {
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
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
	}

	private ICConfigurationDescription[] getConfigurationDescriptions(IProject project) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project);
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

		// check another clone after configuring
		{
			MockBuildCommandParser clone = parser.clone();
			assertTrue(parser.equals(clone));
		}

		// check 'expand relative paths' flag
		{
			MockBuildCommandParser clone = parser.clone();
			boolean expandRelativePaths = clone.isResolvingPaths();
			clone.setResolvingPaths( ! expandRelativePaths );
			assertFalse(parser.equals(clone));
		}

		// check cloneShallow()
		{
			MockBuildCommandParser parser2 = parser.clone();
			MockBuildCommandParser clone = parser2.cloneShallow();
			assertTrue(parser2.equals(clone));
		}

	}

	public void testAbstractBuildCommandParser_Serialize() throws Exception {
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

			assertTrue(xmlString.contains(ATTR_EXPAND_RELATIVE_PATHS));
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

	public void testAbstractBuildCommandParser_Nulls() throws Exception {
		MockBuildCommandParser parser = new MockBuildCommandParser();
		parser.startup(null);
		parser.processLine(null);
		parser.shutdown();

		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(null, null, null);
		assertNull(entries);
	}

	public void testAbstractBuildCommandParser_Basic() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		final IFile file=ResourceHelper.createFile(project, "file.cpp");

		// create test class
		ILanguageSettingsBuildOutputScanner parser = new MockBuildCommandParser() {
			@Override
			public boolean processLine(String line, ErrorParserManager epm) {
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
		parser.startup(cfgDescription);
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

//	public void testGCCBuildCommandParser_Nulls() throws Exception {
//		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
//		parser.startup(null);
//		parser.processLine(null);
//		parser.shutdown();
//
//		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(null, null, null);
//		assertNull(entries);
//	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			IPath path = new Path("/path0").setDevice(project.getLocation().getDevice());
			CIncludePathEntry expected = new CIncludePathEntry(path, 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
	}

	/**
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
		ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(file1.getProjectRelativePath(), true);
		String languageId = ls.getLanguageId();

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 file1.cpp");
		parser.processLine("gcc-4.2 -I/path0 file2.cpp");
		parser.processLine("g++ -I/path0 file3.cpp");
		parser.processLine("c++ -I/path0 file4.cpp");
		parser.processLine("\"gcc\" -I/path0 file5.cpp");
		parser.shutdown();

		// check populated entries
		IPath path0 = new Path("/path0").setDevice(project.getLocation().getDevice());
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file1, languageId);
			assertEquals(new CIncludePathEntry(path0, 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file2, languageId);
			assertEquals(new CIncludePathEntry(path0, 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file3, languageId);
			assertEquals(new CIncludePathEntry(path0, 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file4, languageId);
			assertEquals(new CIncludePathEntry(path0, 0), entries.get(0));
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file5, languageId);
			assertEquals(new CIncludePathEntry(path0, 0), entries.get(0));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
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
		{
			IPath path = new Path("/path0").setDevice(project.getLocation().getDevice());
			CIncludePathEntry expected = new CIncludePathEntry(path, 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
		{
			IPath path = new Path("/path1").setDevice(project.getLocation().getDevice());
			CIncludePathEntry expected = new CIncludePathEntry(path, 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(1);
			assertEquals(expected, entry);
		}
		{
			IPath path = new Path("/path with spaces").setDevice(project.getLocation().getDevice());
			CIncludePathEntry expected = new CIncludePathEntry(path, 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(2);
			assertEquals(expected, entry);
		}
		{
			IPath path = new Path("/path with spaces2").setDevice(project.getLocation().getDevice());
			CIncludePathEntry expected = new CIncludePathEntry(path, 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(3);
			assertEquals(expected, entry);
		}
		{
			IPath path = new Path("/path with spaces3").setDevice(project.getLocation().getDevice());
			CIncludePathEntry expected = new CIncludePathEntry(path, 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(4);
			assertEquals(expected, entry);
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
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
		{
			CMacroEntry expected = new CMacroEntry("MACRO0", "", 0);
			CMacroEntry entry = (CMacroEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO1", "value", 0);
			CMacroEntry entry = (CMacroEntry)entries.get(1);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO2", "value with spaces", 0);
			CMacroEntry entry = (CMacroEntry)entries.get(2);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO3", "value with spaces", 0);
			CMacroEntry entry = (CMacroEntry)entries.get(3);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO4", "\"quoted value\"", 0);
			CMacroEntry entry = (CMacroEntry)entries.get(4);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO5", "\"quoted value\"", 0);
			CMacroEntry entry = (CMacroEntry)entries.get(5);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO6", "\"escape-quoted value\"", 0);
			CMacroEntry entry = (CMacroEntry)entries.get(6);
			assertEquals(expected, entry);
		}
		{
			CMacroEntry expected = new CMacroEntry("MACRO7", "'single-quoted value'", 0);
			CMacroEntry entry = (CMacroEntry)entries.get(7);
			assertEquals(expected, entry);
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -UMACRO"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CMacroEntry("MACRO", null, ICSettingEntry.UNDEFINED), entries.get(0));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
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
		{
			IPath incFile = new Path("/include.file1").setDevice(project.getLocation().getDevice());
			CIncludeFileEntry expected = new CIncludeFileEntry(incFile, 0);
			CIncludeFileEntry entry = (CIncludeFileEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}

		{
			IPath incFile = new Path("/include.file with spaces").setDevice(project.getLocation().getDevice());
			assertEquals(new CIncludeFileEntry(incFile, 0), entries.get(1));
		}
		{
			assertEquals(new CIncludeFileEntry(project.getLocation().removeLastSegments(2).append("include.file2"), 0), entries.get(2));
			assertEquals(new CIncludeFileEntry(project.getFullPath().append("include.file3"), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(3));
			assertEquals(new CIncludeFileEntry(project.getLocation().removeLastSegments(2).append("include-file-with-dashes"), 0), entries.get(4));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc -macros macro.file file.cpp");
		parser.processLine("gcc "
				+ " -macros /macro.file"
				+ " -macros '/macro.file with spaces'"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			IPath path = new Path("/macro.file").setDevice(project.getLocation().getDevice());
			CMacroFileEntry expected = new CMacroFileEntry(path, 0);
			CMacroFileEntry entry = (CMacroFileEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
		{
			IPath path = new Path("/macro.file with spaces").setDevice(project.getLocation().getDevice());
			CMacroFileEntry expected = new CMacroFileEntry(path, 0);
			CMacroFileEntry entry = (CMacroFileEntry)entries.get(1);
			assertEquals(expected, entry);
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -L/path0"
				+ " -L'/path with spaces'"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			IPath path = new Path("/path0").setDevice(project.getLocation().getDevice());
			CLibraryPathEntry expected = new CLibraryPathEntry(path, 0);
			CLibraryPathEntry entry = (CLibraryPathEntry)entries.get(0);
			assertEquals(expected.getName(), entry.getName());
			assertEquals(expected.getValue(), entry.getValue());
			assertEquals(expected.getKind(), entry.getKind());
			assertEquals(expected.getFlags(), entry.getFlags());
			assertEquals(expected, entry);
		}
		{
			IPath path = new Path("/path with spaces").setDevice(project.getLocation().getDevice());
			CLibraryPathEntry expected = new CLibraryPathEntry(path, 0);
			CLibraryPathEntry entry = (CLibraryPathEntry)entries.get(1);
			assertEquals(expected, entry);
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
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
//		+ " -I/path0 "
		{
			IPath path = new Path("/path0").setDevice(project.getLocation().getDevice());
			CIncludePathEntry expected = new CIncludePathEntry(path, 0);
			assertEquals(expected, entries.get(0));
		}
//		+ " -DMACRO1=value"
		{
			CMacroEntry expected = new CMacroEntry("MACRO1", "value", 0);
			assertEquals(expected, entries.get(1));
		}
//		+ " -ldomain"
		{
			CLibraryFileEntry expected = new CLibraryFileEntry("libdomain.a", 0);
			assertEquals(expected, entries.get(2));
		}
//		+ " -I /path1 "
		{
			IPath path = new Path("/path1").setDevice(project.getLocation().getDevice());
			CIncludePathEntry expected = new CIncludePathEntry(path, 0);
			assertEquals(expected, entries.get(3));
		}
//		+ " -DMACRO2=\"value with spaces\""
		{
			CMacroEntry expected = new CMacroEntry("MACRO2", "value with spaces", 0);
			assertEquals(expected, entries.get(4));
		}
//		+ " -I\"/path with spaces\""
		{
			IPath path = new Path("/path with spaces").setDevice(project.getLocation().getDevice());
			CIncludePathEntry expected = new CIncludePathEntry(path, 0);
			assertEquals(expected, entries.get(5));
		}
//		+ " -L/usr/lib"
		{
			IPath path = new Path("/usr/lib").setDevice(project.getLocation().getDevice());
			CLibraryPathEntry expected = new CLibraryPathEntry(path, 0);
			assertEquals(expected, entries.get(6));
		}

		assertEquals(7, entries.size());
	}

	/**
	 */
	public void testFileMissing() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];

		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 missing.cpp");
		parser.shutdown();
		
		// check entries
		assertTrue(parser.isEmpty());
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		
		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ "-I/path0 "
				+ "-I. "
				+ file.getLocation().toOSString());
		parser.shutdown();
		
		// check entries
		IPath path0 = new Path("/path0").setDevice(project.getLocation().getDevice());
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
			assertEquals(new CIncludePathEntry(path0, 0), entries.get(0));
			assertEquals(new CIncludePathEntry(project.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
		}
	}
	
	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		
		// parse line
		parser.startup(null);
		parser.processLine("gcc "
				+ "-I/path0 "
				+ "-I. "
				+ file.getLocation().toOSString());
		parser.shutdown();
		
		// check entries
		IPath path0 = new Path("/path0").setDevice(project.getLocation().getDevice());
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(null, file, languageId);
			assertEquals(new CIncludePathEntry(path0, 0), entries.get(0));
			assertEquals(new CIncludePathEntry(file.getParent().getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
		}
	}
	
	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 'file with spaces 1.cpp'");
		parser.processLine("gcc -I/path0 \"file with spaces 2.cpp\"");
		parser.processLine("gcc -I/path0 'file with spaces 3.cpp'\n");
		parser.processLine("gcc -I/path0 'file with spaces 4.cpp'\r\n");
		parser.shutdown();

		// check populated entries
		IPath path0 = new Path("/path0").setDevice(project.getLocation().getDevice());
		{
			// in single quotes
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file1, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			assertEquals(expected, entries.get(0));
		}
		{
			// in double quotes
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file2, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			assertEquals(expected, entries.get(0));
		}
		{
			// Unix EOL
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file3, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			assertEquals(expected, entries.get(0));
		}
		{
			// Windows EOL
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file4, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			assertEquals(expected, entries.get(0));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
	
		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 file0.cpp");
		parser.processLine("gcc -I/path0 file1.cpp\n");
		parser.processLine("gcc -I/path0 file2.cpp\r\n");
		parser.shutdown();
	
		// check populated entries
		IPath path0 = new Path("/path0").setDevice(project.getLocation().getDevice());
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file0, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(0);
			assertEquals(expected, entry);
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file1, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(0);
			assertEquals(expected, entry);
		}
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file2, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			CIncludePathEntry entry = (CIncludePathEntry)entries.get(0);
			assertEquals(expected, entry);
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setResolvingPaths(true);
		
		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -IC:\\path"
				+ " file.cpp");
		parser.shutdown();
		
		// check populated entries
		IPath path0 = new Path("C:\\path").setDevice(project.getLocation().getDevice());
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			assertEquals(expected, entries.get(0));
		}
	}
	
	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setResolvingPaths(true);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -IFolder"
				+ " -IFolder-Icomposite" // to test case when "-I" is a part of folder name
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			// check that relative paths are relative to CWD which is the location of the project
			assertEquals(new CIncludePathEntry(project.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
			assertEquals(new CIncludePathEntry(project.getLocation().removeLastSegments(1), 0), entries.get(1));
			assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(2));
			assertEquals(new CIncludePathEntry(folderComposite.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(3));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setResolvingPaths(false);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -IFolder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(".", 0), entries.get(0));
			assertEquals(new CIncludePathEntry("..", 0), entries.get(1));
			assertEquals(new CIncludePathEntry("Folder", 0), entries.get(2));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setResolvingPaths(true);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -IFolder"
				+ " -IFolder"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
			assertEquals(1, entries.size());
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		ErrorParserManager epm = new ErrorParserManager(project, null);
		epm.pushDirectoryURI(buildDir.getLocationURI());
		
		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -I../../.."
				+ " -IFolder"
				+ " -IMissingFolder"
				+ " file.cpp",
				epm);
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
			assertEquals(new CIncludePathEntry(buildDir.getFullPath().removeLastSegments(1), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
			assertEquals(new CIncludePathEntry(buildDir.getLocation().removeLastSegments(3), 0), entries.get(2));
			assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(3));
			assertEquals(new CIncludePathEntry(buildDir.getFullPath().append("MissingFolder"), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(4));
		}
	}
	
	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -IFolder"
				+ " file.cpp",
			epm);
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		ErrorParserManager epm = new ErrorParserManager(project, null);
		epm.pushDirectoryURI(buildDir.getLocationURI());

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -IFolder"
				+ " ../file.cpp",
				epm);
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
			assertEquals(new CIncludePathEntry(buildDir.getFullPath().removeLastSegments(1), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
			assertEquals(new CIncludePathEntry(buildDir.getFullPath().append("Folder"), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(2));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		ErrorParserManager epm = new ErrorParserManager(project, null);
		URI uriBuildDir = new URI("file:/non-existing/path");
		epm.pushDirectoryURI(uriBuildDir);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I."
				+ " -I.."
				+ " -IFolder"
				+ " ../file.cpp",
				epm);
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			IPath buildPath = new Path(uriBuildDir.getPath()).setDevice(project.getLocation().getDevice());
			assertEquals(new CIncludePathEntry(buildPath, 0), entries.get(0));
			assertEquals(new CIncludePathEntry(buildPath.removeLastSegments(1), 0), entries.get(1));
			assertEquals(new CIncludePathEntry(buildPath.append("Folder"), 0), entries.get(2));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		ErrorParserManager epm = new ErrorParserManager(project, null);
		URI uriBuildDir = new URI("file:/BuildDir");
		epm.pushDirectoryURI(uriBuildDir);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I."
				+ " -I/BuildDir/Folder"
				+ " -I../BuildDir/Folder2"
				+ " -I/BuildDir/MissingFolder"
				+ " -I../BuildDir/MissingFolder2"
				+ " /BuildDir/file.cpp",
				epm);
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
			assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
			assertEquals(new CIncludePathEntry(folder2.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(2));
			assertEquals(new CIncludePathEntry(buildDir.getFullPath().append("MissingFolder"), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(3));
			assertEquals(new CIncludePathEntry(buildDir.getFullPath().append("MissingFolder2"), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(4));
		}
	}
	
	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		ErrorParserManager epm = new ErrorParserManager(project, null);
		
		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I/Folder/Subfolder"
				+ " -I/Mapped/Folder"
				+ " -I/Ambiguous/Folder"
				+ " -I/Missing/Folder"
				+ " file.cpp",
				epm);
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
			assertEquals(new CIncludePathEntry(mappedFolder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
		}
		{
			IPath path = new Path("/Ambiguous/Folder").setDevice(file.getLocation().getDevice());
			assertEquals(new CIncludePathEntry(path, 0), entries.get(2));
		}
		{
			IPath path = new Path("/Missing/Folder").setDevice(file.getLocation().getDevice());
			assertEquals(new CIncludePathEntry(path, 0), entries.get(3));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		ErrorParserManager epm = new ErrorParserManager(project, null);
		
		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I/Folder/Subfolder"
				+ " -I/Ambiguous/Folder"
				+ " -I/Missing/Folder"
				+ " file.cpp",
				epm);
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
		}
		{
			IPath path = new Path("/Ambiguous/Folder").setDevice(file.getLocation().getDevice());
			assertEquals(new CIncludePathEntry(path, 0), entries.get(1));
		}
		{
			IPath path = new Path("/Missing/Folder").setDevice(file.getLocation().getDevice());
			assertEquals(new CIncludePathEntry(path, 0), entries.get(2));
		}
	}
	
	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		ErrorParserManager epm = new ErrorParserManager(project, null);
		
		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I/Folder/Subfolder"
				+ " -I/Ambiguous/Folder"
				+ " file.cpp",
				epm);
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(folderInReferencedProject.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));

			IPath path = new Path("/Ambiguous/Folder").setDevice(file.getLocation().getDevice());
			assertEquals(new CIncludePathEntry(path, 0), entries.get(1));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription);
		// "../" should navigate along filesystem path, not along the link itself
		parser.processLine("gcc -I"+linkPath.toString()+"/.."+" file.cpp", epm);
		parser.shutdown();

		// check populated entries
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(dir2.removeLastSegments(1), 0);
			assertEquals(expected, entries.get(0));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription);
		// "../" should navigate along filesystem path, not along the link itself
		parser.processLine("gcc -Ilinked/.."+" file.cpp", epm);
		parser.shutdown();

		// check populated entries
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
			assertEquals(expected, entries.get(0));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I."
				+ " -Iinclude"
				+ " file.cpp");
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
			assertEquals(new CIncludePathEntry(includeDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		
		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I."
				+ " -Iinclude"
				+ " " + file.getLocation().toOSString()
			);
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			assertEquals(new CIncludePathEntry(buildDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
			assertEquals(new CIncludePathEntry(includeDir.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(1));
		}
		
	}
	
	public void testContentType_None() throws Exception {
		MockBuildCommandParser parser = new MockBuildCommandParser() {
			@Override
			protected String parseForResourceName(String line) {
				return "file.wrong-content-type";
			}
		};
		parser.startup(null);
		parser.processLine("gcc file.wrong-content-type");
		parser.shutdown();
		
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(null, null, null);
		assertNull(entries);
	}
	
	/**
	 */
	public void testContentType_Mismatch() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		ResourceHelper.createFile(project, "file.c");
		
		// create GCCBuildCommandParser
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		// restrict the parser's language scope to C++ only
		parser.setLanguageScope(new ArrayList<String>() {{add(LANG_CPP);}});

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 file.c");
		parser.shutdown();
		
		assertTrue(parser.isEmpty());
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 file.x++");
		parser.shutdown();

		// check populated entries
		IPath path0 = new Path("/path0").setDevice(project.getLocation().getDevice());
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			assertEquals(expected, entries.get(0));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc -I/path0 FILE.CPP", epm);
		parser.shutdown();

		// check populated entries
		IPath path0 = new Path("/path0").setDevice(project.getLocation().getDevice());
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
			CIncludePathEntry expected = new CIncludePathEntry(path0, 0);
			assertEquals(expected, entries.get(0));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);

		// parse line
		parser.startup(cfgDescription);
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
		{
			List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
			assertEquals(new CMacroEntry("BOOST_ALL_NO_LIB", "1", 0), entries.get(0));
			assertEquals(new CMacroEntry("BOOST_PYTHON_SOURCE", "", 0), entries.get(1));
			assertEquals(new CMacroEntry("BOOST_PYTHON_STATIC_LIB", "", 0), entries.get(2));
			assertEquals(new CIncludePathEntry(project.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(3));
			assertEquals(new CIncludePathEntry(new Path("/Python1025/Include").setDevice(project.getLocation().getDevice()), 0), entries.get(4));
			assertEquals(5, entries.size());
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		ErrorParserManager epm = new ErrorParserManager(project, null);

		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -IFolder"
				+ " -I/Absolute/Folder"
				+ " file.cpp",
			epm);
		parser.shutdown();

		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			String device = project.getLocation().getDevice();
			assertEquals(new CIncludePathEntry(folder.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED), entries.get(0));
			assertEquals(new CIncludePathEntry(new Path("/Absolute/Folder").setDevice(device), 0), entries.get(1));
		}
	}

	/**
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
		GCCBuildCommandParser parser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT);
		ErrorParserManager epm = new ErrorParserManager(project, null);
		
		// parse line
		parser.startup(cfgDescription);
		parser.processLine("gcc "
				+ " -I/BeingMappedFrom/Folder" // mapped to local folder in EFSExtensionProvider extension point
				+ " file.cpp",
				epm);
		parser.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, file, languageId);
		{
			String device = project.getLocation().getDevice();
			assertEquals(new CIncludePathEntry(new Path("/LocallyMappedTo/Folder").setDevice(device), 0), entries.get(0));
		}
	}

}
