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
 package org.eclipse.cdt.build.core.scannerconfig.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.GCCBuiltinSpecsDetector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GCCBuiltinSpecsDetectorTest extends TestCase {
	private static final String PROVIDER_ID = "provider.id";
	private static final String PROVIDER_NAME = "provider name";
	private static final String LANGUAGE_ID = "language.test.id";
	private static final String LANGUAGE_ID_C = "org.eclipse.cdt.core.gcc";
	private static final String LANGUAGE_ID_CPP = "org.eclipse.cdt.core.g++";
	private static final String CUSTOM_PARAMETER = "customParameter";
	private static final String ELEM_TEST = "test";
	
	// those attributes must match that in AbstractBuiltinSpecsDetector
	private static final String ATTR_CONSOLE = "console"; //$NON-NLS-1$
	private static final String ATTR_RUN_ONCE = "run-once"; //$NON-NLS-1$

	private class MockBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
		@Override
		protected String getToolchainId() {
			return null;
		}
		@Override
		protected List<String> parseForOptions(String line) {
			return null;
		}
		@Override
		protected AbstractOptionParser[] getOptionParsers() {
			return null;
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

	public void testAbstractBuiltinSpecsDetector_GettersSetters() throws Exception {
		// define mock detector
		MockBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector();
		
		detector.configureProvider(PROVIDER_ID, PROVIDER_NAME, null, null, null);
		assertEquals(PROVIDER_ID, detector.getId());
		assertEquals(PROVIDER_NAME, detector.getName());
		assertEquals(null, detector.getLanguageScope());
		assertEquals(null, detector.getSettingEntries(null, null, null));
		assertEquals(null, detector.getCustomParameter());

		List<String> languages = new ArrayList<String>();
		languages.add(LANGUAGE_ID);
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		ICLanguageSettingEntry entry = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		entries.add(entry);

		detector.configureProvider(PROVIDER_ID, PROVIDER_NAME, languages, entries, CUSTOM_PARAMETER);
		assertEquals(PROVIDER_ID, detector.getId());
		assertEquals(PROVIDER_NAME, detector.getName());
		assertEquals(languages, detector.getLanguageScope());
		assertEquals(entries, detector.getSettingEntries(null, null, null));
		assertEquals(CUSTOM_PARAMETER, detector.getCustomParameter());
		
		assertEquals(true, detector.isRunOnce());
		detector.setRunOnce(false);
		assertEquals(false, detector.isRunOnce());
	}
	
	public void testAbstractBuiltinSpecsDetector_CloneAndEquals() throws Exception {
		// define mock detector
		class MockDetectorCloneable extends MockBuiltinSpecsDetector implements Cloneable {
			@Override
			public MockDetectorCloneable clone() throws CloneNotSupportedException {
				return (MockDetectorCloneable) super.clone();
			}
			@Override
			public MockDetectorCloneable cloneShallow() throws CloneNotSupportedException {
				return (MockDetectorCloneable) super.cloneShallow();
			}
		}
		
		// create instance to compare to
		MockDetectorCloneable detector = new MockDetectorCloneable();
		
		List<String> languages = new ArrayList<String>();
		languages.add(LANGUAGE_ID);
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		ICLanguageSettingEntry entry = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		entries.add(entry);

		// check clone after initialization
		MockDetectorCloneable clone0 = detector.clone();
		assertTrue(detector.equals(clone0));
		
		// configure provider
		detector.configureProvider(PROVIDER_ID, PROVIDER_NAME, languages, entries, CUSTOM_PARAMETER);
		assertEquals(true, detector.isRunOnce());
		detector.setRunOnce(false);
		assertFalse(detector.equals(clone0));

		// check another clone after configuring
		{
			MockDetectorCloneable clone = detector.clone();
			assertTrue(detector.equals(clone));
		}
		
		// check custom parameter
		{
			MockDetectorCloneable clone = detector.clone();
			clone.setCustomParameter("changed");
			assertFalse(detector.equals(clone));
		}
		
		// check language scope
		{
			MockDetectorCloneable clone = detector.clone();
			clone.setLanguageScope(null);
			assertFalse(detector.equals(clone));
		}
		
		// check 'run once' flag
		{
			MockDetectorCloneable clone = detector.clone();
			boolean runOnce = clone.isRunOnce();
			clone.setRunOnce( ! runOnce );
			assertFalse(detector.equals(clone));
		}
		
		// check console flag
		{
			MockDetectorCloneable clone = detector.clone();
			boolean isConsoleEnabled = clone.isConsoleEnabled();
			clone.setConsoleEnabled( ! isConsoleEnabled );
			assertFalse(detector.equals(clone));
		}
		
		// check entries
		{
			MockDetectorCloneable clone = detector.clone();
			clone.setSettingEntries(null, null, null, null);
			assertFalse(detector.equals(clone));
		}
		
		// check cloneShallow()
		{
			MockDetectorCloneable detector2 = detector.clone();
			MockDetectorCloneable clone = detector2.cloneShallow();
			assertFalse(detector2.equals(clone));
			detector2.setSettingEntries(null, null, null, null);
			assertTrue(detector2.equals(clone));
		}
			
	}
	
	/**
	 */
	public void testAbstractBuiltinSpecsDetector_Serialize() throws Exception {
		{
			// create empty XML
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			
			// load it to new provider
			MockBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector();
			detector.load(rootElement);
			assertEquals(true, detector.isRunOnce());
			assertEquals(false, detector.isConsoleEnabled());
		}
		
		Element elementProvider;
		{
			// define mock detector
			MockBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector();
			assertEquals(true, detector.isRunOnce());
			assertEquals(false, detector.isConsoleEnabled());
			
			// redefine the settings
			detector.setRunOnce(false);
			assertEquals(false, detector.isRunOnce());
			detector.setConsoleEnabled(true);
			assertEquals(true, detector.isConsoleEnabled());
			
			// serialize in XML
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = detector.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);
			
			assertTrue(xmlString.contains(ATTR_RUN_ONCE));
			assertTrue(xmlString.contains(ATTR_CONSOLE));
		}
		{
			// create another instance of the provider
			MockBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector();
			assertEquals(true, detector.isRunOnce());
			assertEquals(false, detector.isConsoleEnabled());
			
			// load element
			detector.load(elementProvider);
			assertEquals(false, detector.isRunOnce());
			assertEquals(true, detector.isConsoleEnabled());
		}
	}


	
	public void testAbstractBuiltinSpecsDetector_Nulls() throws Exception {
		{
			// test AbstractBuiltinSpecsDetector.run(...);
			MockBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector();
			detector.run((IProject)null, null, null, null, null);
			// Do not test with (ICConfigurationDescription)null as it is not allowed
		}
		
		{
			// test AbstractBuiltinSpecsDetector.processLine(...) flow
			MockBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector();
			detector.startup(null);
			detector.processLine(null);
			detector.shutdown();
		}
		
	}

	public void testAbstractBuiltinSpecsDetector_RunConfiguration() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector() {
			@Override
			protected boolean runProgram(String command, String[] env, IPath workingDirectory, IProgressMonitor monitor,
					OutputStream consoleOut, OutputStream consoleErr) throws CoreException, IOException {
				printLine(consoleOut, "#define MACRO VALUE");
				consoleOut.close();
				consoleErr.close();
				return true;
			}
		};
		
		detector.setLanguageScope(new ArrayList<String>() {{add(LANGUAGE_ID);}});
		
		detector.run(cfgDescription, LANGUAGE_ID, null, null, null);
		assertFalse(detector.isEmpty());
		
		List<ICLanguageSettingEntry> noentries = detector.getSettingEntries(null, null, null);
		assertNull(noentries);
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(cfgDescription, null, LANGUAGE_ID);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}
	
	public void testAbstractBuiltinSpecsDetector_RunProject() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector() {
			@Override
			protected boolean runProgram(String command, String[] env, IPath workingDirectory, IProgressMonitor monitor,
					OutputStream consoleOut, OutputStream consoleErr) throws CoreException, IOException {
				printLine(consoleOut, "#define MACRO VALUE");
				consoleOut.close();
				consoleErr.close();
				return true;
			}
		};
		
		detector.setLanguageScope(new ArrayList<String>() {{add(LANGUAGE_ID);}});
		
		detector.run(project, LANGUAGE_ID, null, null, null);
		assertFalse(detector.isEmpty());
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, LANGUAGE_ID);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}
	
	public void testAbstractBuiltinSpecsDetector_RunOnce() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		// Define mock detector which collects number of entries equal to count
		AbstractBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector() {
			int count=0;
			@Override
			public boolean processLine(String line) {
				count++;
				for (int i=0;i<count;i++) {
					detectedSettingEntries.add(new CMacroEntry("MACRO", ""+count, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY));
				}
				return true;
			}
			
			@Override
			protected boolean runProgram(String command, String[] env, IPath workingDirectory, IProgressMonitor monitor,
					OutputStream consoleOut, OutputStream consoleErr) throws CoreException, IOException {
				printLine(consoleOut, "dummy line");
				consoleOut.close();
				consoleErr.close();
				return true;
			}

		};

		// set to run with each build
		detector.setRunOnce(false);
		assertEquals(false, detector.isRunOnce());

		// run first time
		detector.run(project, LANGUAGE_ID_C, null, null, null);
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, LANGUAGE_ID_C);
			assertEquals(1, entries.size());
		}

		// run second time
		detector.run(project, LANGUAGE_ID_C, null, null, null);
		{
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, LANGUAGE_ID_C);
			assertEquals(2, entries.size());
		}
		
		// set to run once
		detector.setRunOnce(true);
		assertEquals(true, detector.isRunOnce());
		assertFalse(detector.isEmpty());

		detector.run(project, LANGUAGE_ID_C, null, null, null);
		{
			// should not collect when provider is not empty
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, LANGUAGE_ID_C);
			assertEquals(2, entries.size());
		}
		
		detector.run(cfgDescription, LANGUAGE_ID_C, null, null, null);
		{
			// should not collect when provider is not empty
			List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, LANGUAGE_ID_C);
			assertEquals(2, entries.size());
		}
	}

	public void testAbstractBuiltinSpecsDetector_GroupSettings() throws Exception {
		// define benchmarks
		final CIncludePathEntry includePath_1 = new CIncludePathEntry("/include/path_1", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CIncludePathEntry includePath_2 = new CIncludePathEntry("/include/path_2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CIncludeFileEntry includeFile_1 = new CIncludeFileEntry(new Path("/include.file1"), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CIncludeFileEntry includeFile_2 = new CIncludeFileEntry(new Path("/include.file2"), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CMacroEntry macro_1 = new CMacroEntry("MACRO_1", "", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CMacroEntry macro_2 = new CMacroEntry("MACRO_2", "", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY |ICSettingEntry.UNDEFINED);
		final CMacroFileEntry macroFile_1 = new CMacroFileEntry(new Path("/macro.file1"), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CMacroFileEntry macroFile_2 = new CMacroFileEntry(new Path("/macro.file2"), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CLibraryPathEntry libraryPath_1 = new CLibraryPathEntry(new Path("/lib/path_1"), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CLibraryPathEntry libraryPath_2 = new CLibraryPathEntry(new Path("/lib/path_2"), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CLibraryFileEntry libraryFile_1 = new CLibraryFileEntry("lib_1.a", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		final CLibraryFileEntry libraryFile_2 = new CLibraryFileEntry("lib_2.a", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		
		// Define mock detector adding unorganized entries
		AbstractBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector() {
			@Override
			public boolean processLine(String line) {
				detectedSettingEntries.add(libraryFile_1);
				detectedSettingEntries.add(libraryPath_1);
				detectedSettingEntries.add(macroFile_1);
				detectedSettingEntries.add(macro_1);
				detectedSettingEntries.add(includeFile_1);
				detectedSettingEntries.add(includePath_1);
				
				detectedSettingEntries.add(includePath_2);
				detectedSettingEntries.add(includeFile_2);
				detectedSettingEntries.add(macro_2);
				detectedSettingEntries.add(macroFile_2);
				detectedSettingEntries.add(libraryPath_2);
				detectedSettingEntries.add(libraryFile_2);
				return true;
			}
		};
		
		// run specs detector
		detector.startup(null);
		detector.processLine("");
		detector.shutdown();
		
		
		// compare benchmarks, expected well-sorted
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		
		int i=0;
		assertEquals(includePath_1, entries.get(i++));
		assertEquals(includePath_2, entries.get(i++));
		assertEquals(includeFile_1, entries.get(i++));
		assertEquals(includeFile_2, entries.get(i++));
		assertEquals(macro_1, entries.get(i++));
		assertEquals(macro_2, entries.get(i++));
		assertEquals(macroFile_1, entries.get(i++));
		assertEquals(macroFile_2, entries.get(i++));
		assertEquals(libraryPath_1, entries.get(i++));
		assertEquals(libraryPath_2, entries.get(i++));
		assertEquals(libraryFile_1, entries.get(i++));
		assertEquals(libraryFile_2, entries.get(i++));
		
		assertEquals(12, entries.size());
	}
	
	public void testGCCBuiltinSpecsDetector_Macro_NoValue() throws Exception {
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.processLine("#define MACRO");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", null, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_ResolvedCommand() throws Exception {
		class MockGCCBuiltinSpecsDetector extends GCCBuiltinSpecsDetector {
			@Override
			public String resolveCommand(String languageId) throws CoreException {
				return super.resolveCommand(languageId);
			}
		}
		{
			MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
			detector.setLanguageScope(new ArrayList<String>() {{add(LANGUAGE_ID_C);}});
			detector.setCustomParameter("${COMMAND} -E -P -v -dD ${INPUTS}");

			String resolvedCommand = detector.resolveCommand(LANGUAGE_ID_C);
			assertTrue(resolvedCommand.startsWith("gcc -E -P -v -dD "));
			assertTrue(resolvedCommand.endsWith("spec.c"));
			detector.shutdown();
		}
		{
			MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
			detector.setLanguageScope(new ArrayList<String>() {{add(LANGUAGE_ID_C);}});
			detector.setCustomParameter("${COMMAND} -E -P -v -dD file.${EXT}");

			String resolvedCommand = detector.resolveCommand(LANGUAGE_ID_C);
			assertTrue(resolvedCommand.startsWith("gcc -E -P -v -dD "));
			assertTrue(resolvedCommand.endsWith("file.c"));
			detector.shutdown();
		}
	}
	
	public void testGCCBuiltinSpecsDetector_Macro_NoArgs() throws Exception {
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.processLine("#define MACRO VALUE");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_Macro_Const() throws Exception {
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.processLine("#define MACRO (3)");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "(3)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_Macro_EmptyArgList() throws Exception {
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.processLine("#define MACRO() VALUE");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO()", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_Macro_ParamUnused() throws Exception {
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.processLine("#define MACRO(X) VALUE");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO(X)", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_Macro_ParamSpace() throws Exception {
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.processLine("#define MACRO(P1, P2) VALUE(P1, P2)");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_Macro_ArgsNoValue() throws Exception {
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.processLine("#define MACRO(P1, P2) ");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO(P1, P2)", null, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testGCCBuiltinSpecsDetector_Includes() throws Exception {
		// Create model project and folders to test
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProject(projectName);
		IPath tmpPath = ResourceHelper.createTemporaryFolder();
		ResourceHelper.createFolder(project, "/misplaced/include1");
		ResourceHelper.createFolder(project, "/local/include");
		ResourceHelper.createFolder(project, "/usr/include");
		ResourceHelper.createFolder(project, "/usr/include2");
		ResourceHelper.createFolder(project, "/misplaced/include2");
		ResourceHelper.createFolder(project, "/System/Library/Frameworks");
		ResourceHelper.createFolder(project, "/Library/Frameworks");
		ResourceHelper.createFolder(project, "/misplaced/include3");
		String loc = tmpPath.toString();

		GCCBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		detector.startup(null);

		detector.processLine(" "+loc+"/misplaced/include1");
		detector.processLine("#include \"...\" search starts here:");
		detector.processLine(" "+loc+"/local/include");
		detector.processLine("#include <...> search starts here:");
		detector.processLine(" "+loc+"/usr/include");
		detector.processLine(" "+loc+"/usr/include/../include2");
		detector.processLine(" "+loc+"/missing/folder");
		detector.processLine(" "+loc+"/Library/Frameworks (framework directory)");
		detector.processLine("End of search list.");
		detector.processLine(" "+loc+"/misplaced/include2");
		detector.processLine("Framework search starts here:");
		detector.processLine(" "+loc+"/System/Library/Frameworks");
		detector.processLine("End of search list.");
		detector.processLine(" "+loc+"/misplaced/include3");
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CIncludePathEntry(loc+"/local/include", ICSettingEntry.LOCAL | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(0));
		assertEquals(new CIncludePathEntry(loc+"/usr/include", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(1));
		assertEquals(new CIncludePathEntry(loc+"/usr/include2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(2));
		assertEquals(new CIncludePathEntry(loc+"/missing/folder", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(3));
		assertEquals(new CIncludePathEntry(loc+"/Library/Frameworks", ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(4));
		assertEquals(new CIncludePathEntry(loc+"/System/Library/Frameworks", ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
				entries.get(5));
		assertEquals(6, entries.size());
	}
	
	public void testGCCBuiltinSpecsDetector_Includes_SymbolicLinkUp() throws Exception {
		// do not test on systems where symbolic links are not supported
		if (!ResourceHelper.isSymbolicLinkSupported())
			return;

		// Create model project and folders to test
		String projectName = getName();
		@SuppressWarnings("unused")
		IProject project = ResourceHelper.createCDTProject(projectName);
		// create link on the filesystem
		IPath dir1 = ResourceHelper.createTemporaryFolder();
		IPath dir2 = dir1.removeLastSegments(1);
		IPath linkPath = dir1.append("linked");
		ResourceHelper.createSymbolicLink(linkPath, dir2);
		
		AbstractBuiltinSpecsDetector detector = new GCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.processLine("#include <...> search starts here:");
		detector.processLine(" "+linkPath.toString()+"/..");
		detector.processLine("End of search list.");
		detector.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		CIncludePathEntry expected = new CIncludePathEntry(dir2.removeLastSegments(1), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
		assertEquals(1, entries.size());
	}

}
