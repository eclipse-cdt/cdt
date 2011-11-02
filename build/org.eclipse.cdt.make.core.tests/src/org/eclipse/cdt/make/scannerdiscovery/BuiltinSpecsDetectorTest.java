/*******************************************************************************
 * Copyright (c) 2010, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
 package org.eclipse.cdt.make.scannerdiscovery;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.ErrorParserManager;
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
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BuiltinSpecsDetectorTest extends BaseTestCase {
	private static final String PROVIDER_ID = "provider.id";
	private static final String PROVIDER_NAME = "provider name";
	private static final String LANGUAGE_ID = "language.test.id";
	private static final String CUSTOM_PARAMETER = "customParameter";
	private static final String ELEM_TEST = "test";
	
	// those attributes must match that in AbstractBuiltinSpecsDetector
	private static final String ATTR_CONSOLE = "console"; //$NON-NLS-1$

	private class MockBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
		@Override
		protected void startupForLanguage(String languageId) throws CoreException {
			super.startupForLanguage(languageId);
		}
		@Override
		protected void shutdownForLanguage() {
			super.shutdownForLanguage();
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

	private class MockBuiltinSpecsDetectorExecutedFlag extends AbstractBuiltinSpecsDetector {
		@Override
		protected List<String> parseForOptions(String line) {
			return null;
		}
		@Override
		protected AbstractOptionParser[] getOptionParsers() {
			return null;
		}
		@Override
		protected void execute() {
			super.execute();
		}
		protected boolean isExecuted() {
			return isExecuted;
		}
	}
	
	private class MockConsoleBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
		@SuppressWarnings("nls")
		private final AbstractOptionParser[] optionParsers = {
			new MacroOptionParser("#define (\\S*) *(\\S*)", "$1", "$2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY),
		};
		@Override
		protected boolean runProgram(String command, String[] env, IPath workingDirectory, IProgressMonitor monitor,
				OutputStream consoleOut, OutputStream consoleErr) throws CoreException, IOException {
			printLine(consoleOut, "#define MACRO VALUE");
			consoleOut.close();
			consoleErr.close();
			return true;
		}
		@Override
		protected IStatus runForEachLanguage(ICConfigurationDescription cfgDescription, IPath workingDirectory,
				String[] env, IProgressMonitor monitor) {
			return super.runForEachLanguage(cfgDescription, workingDirectory, env, monitor);
		}
		@Override
		protected List<String> parseForOptions(final String line) {
			return new ArrayList<String>() {{ add(line); }};
		}
		@Override
		protected AbstractOptionParser[] getOptionParsers() {
			return optionParsers;
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		Job.getJobManager().join(AbstractBuiltinSpecsDetector.JOB_FAMILY_BUILTIN_SPECS_DETECTOR, null);
		super.tearDown();
	}

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

	public void testAbstractBuiltinSpecsDetector_GettersSetters() throws Exception {
		// define mock detector
		MockBuiltinSpecsDetectorExecutedFlag detector = new MockBuiltinSpecsDetectorExecutedFlag();
		
		detector.configureProvider(PROVIDER_ID, PROVIDER_NAME, null, null, null);
		assertEquals(PROVIDER_ID, detector.getId());
		assertEquals(PROVIDER_NAME, detector.getName());
		assertEquals(null, detector.getLanguageScope());
		assertEquals(null, detector.getSettingEntries(null, null, null));
		assertEquals(null, detector.getCustomParameter());
		assertEquals(false, detector.isExecuted());

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
		assertEquals(false, detector.isExecuted());
		
		detector.execute();
		assertEquals(true, detector.isExecuted());
	}
	
	public void testAbstractBuiltinSpecsDetector_CloneAndEquals() throws Exception {
		// define mock detector
		class MockDetectorCloneable extends MockBuiltinSpecsDetectorExecutedFlag implements Cloneable {
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
		assertEquals(false, detector.isConsoleEnabled());
		detector.setConsoleEnabled(true);
		detector.execute();
		assertEquals(true, detector.isExecuted());
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
		
		// check console flag
		{
			MockDetectorCloneable clone = detector.clone();
			boolean isConsoleEnabled = clone.isConsoleEnabled();
			clone.setConsoleEnabled( ! isConsoleEnabled );
			assertFalse(detector.equals(clone));
		}
		
		// check isExecuted flag
		{
			MockDetectorCloneable clone = detector.clone();
			assertEquals(true, clone.isExecuted());
			clone.clear();
			assertEquals(false, clone.isExecuted());
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
			assertEquals(false, clone.isExecuted());
			assertFalse(detector2.equals(clone));

			detector2.setSettingEntries(null, null, null, null);
			assertFalse(detector2.equals(clone));
			
			clone.execute();
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
			MockBuiltinSpecsDetectorExecutedFlag detector = new MockBuiltinSpecsDetectorExecutedFlag();
			detector.load(rootElement);
			assertEquals(false, detector.isConsoleEnabled());
		}
		
		Element elementProvider;
		{
			// define mock detector
			MockBuiltinSpecsDetectorExecutedFlag detector = new MockBuiltinSpecsDetectorExecutedFlag();
			assertEquals(false, detector.isConsoleEnabled());
			
			// redefine the settings
			detector.setConsoleEnabled(true);
			assertEquals(true, detector.isConsoleEnabled());
			
			// serialize in XML
			Document doc = XmlUtil.newDocument();
			Element rootElement = XmlUtil.appendElement(doc, ELEM_TEST);
			elementProvider = detector.serialize(rootElement);
			String xmlString = XmlUtil.toString(doc);
			
			assertTrue(xmlString.contains(ATTR_CONSOLE));
		}
		{
			// create another instance of the provider
			MockBuiltinSpecsDetectorExecutedFlag detector = new MockBuiltinSpecsDetectorExecutedFlag();
			assertEquals(false, detector.isConsoleEnabled());
			
			// load element
			detector.load(elementProvider);
			assertEquals(true, detector.isConsoleEnabled());
		}
	}

	public void testAbstractBuiltinSpecsDetector_Nulls() throws Exception {
		{
			// test AbstractBuiltinSpecsDetector.processLine(...) flow
			MockBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector();
			detector.startup(null);
			detector.startupForLanguage(null);
			detector.processLine(null, null);
			detector.shutdownForLanguage();
			detector.shutdown();
		}
		{
			// test AbstractBuiltinSpecsDetector.processLine(...) flow
			MockConsoleBuiltinSpecsDetector detector = new MockConsoleBuiltinSpecsDetector();
			detector.runForEachLanguage(null, null, null, null);
		}
	}

	public void testAbstractBuiltinSpecsDetector_RunConfiguration() throws Exception {
		// Create model project and accompanied descriptions
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		MockConsoleBuiltinSpecsDetector detector = new MockConsoleBuiltinSpecsDetector();
		detector.setLanguageScope(new ArrayList<String>() {{add(LANGUAGE_ID);}});
		
		detector.runForEachLanguage(cfgDescription, null, null, null);
		assertFalse(detector.isEmpty());
		
		List<ICLanguageSettingEntry> noentries = detector.getSettingEntries(null, null, null);
		assertNull(noentries);
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(cfgDescription, null, LANGUAGE_ID);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
	}

	public void testAbstractBuiltinSpecsDetector_RunGlobal() throws Exception {
		MockConsoleBuiltinSpecsDetector detector = new MockConsoleBuiltinSpecsDetector();
		detector.setLanguageScope(new ArrayList<String>() {{add(LANGUAGE_ID);}});
		
		detector.runForEachLanguage(null, null, null, null);
		assertFalse(detector.isEmpty());
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, LANGUAGE_ID);
		ICLanguageSettingEntry expected = new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY);
		assertEquals(expected, entries.get(0));
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
		MockBuiltinSpecsDetector detector = new MockBuiltinSpecsDetector() {
			@Override
			public boolean processLine(String line, ErrorParserManager epm) {
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
		detector.startupForLanguage(null);
		detector.processLine("", null);
		detector.shutdownForLanguage();
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
}
