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
 package org.eclipse.cdt.build.core.scannerconfig.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.GCCBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.GCCBuiltinSpecsDetectorCygwin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class GCCBuiltinSpecsDetectorTest extends TestCase {
	private static final String LANGUAGE_ID_C = GCCLanguage.ID;
	
	class MockGCCBuiltinSpecsDetector extends GCCBuiltinSpecsDetector {
		@Override
		public void startupForLanguage(String languageId) throws CoreException {
			super.startupForLanguage(languageId);
		}
		@Override
		public void shutdownForLanguage() {
			super.shutdownForLanguage();
		}
	}

	class MockGCCBuiltinSpecsDetectorCygwin extends GCCBuiltinSpecsDetectorCygwin {
		@Override
		public void startupForLanguage(String languageId) throws CoreException {
			super.startupForLanguage(languageId);
		}
		@Override
		public void shutdownForLanguage() {
			super.shutdownForLanguage();
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

	public void testGCCBuiltinSpecsDetector_ResolvedCommand() throws Exception {
		class MockGCCBuiltinSpecsDetectorLocal extends GCCBuiltinSpecsDetector {
			@Override
			public String resolveCommand(String languageId) throws CoreException {
				return super.resolveCommand(languageId);
			}
		}
		{
			MockGCCBuiltinSpecsDetectorLocal detector = new MockGCCBuiltinSpecsDetectorLocal();
			detector.setLanguageScope(new ArrayList<String>() {{add(LANGUAGE_ID_C);}});
			detector.setCustomParameter("${COMMAND} -E -P -v -dD ${INPUTS}");

			String resolvedCommand = detector.resolveCommand(LANGUAGE_ID_C);
			assertTrue(resolvedCommand.startsWith("gcc -E -P -v -dD "));
			assertTrue(resolvedCommand.endsWith("spec.c"));
		}
		{
			MockGCCBuiltinSpecsDetectorLocal detector = new MockGCCBuiltinSpecsDetectorLocal();
			detector.setLanguageScope(new ArrayList<String>() {{add(LANGUAGE_ID_C);}});
			detector.setCustomParameter("${COMMAND} -E -P -v -dD file.${EXT}");

			String resolvedCommand = detector.resolveCommand(LANGUAGE_ID_C);
			assertTrue(resolvedCommand.startsWith("gcc -E -P -v -dD "));
			assertTrue(resolvedCommand.endsWith("file.c"));
		}
	}
	
	public void testGCCBuiltinSpecsDetector_Macro_NoValue() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO", null);
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO", null, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	public void testGCCBuiltinSpecsDetector_Macro_Simple() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO VALUE", null);
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	public void testGCCBuiltinSpecsDetector_Macro_Const() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO (3)", null);
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO", "(3)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	public void testGCCBuiltinSpecsDetector_Macro_WhiteSpaces() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#define \t MACRO_1 VALUE", null);
		detector.processLine("#define MACRO_2 \t VALUE", null);
		detector.processLine("#define MACRO_3 VALUE \t", null);
		detector.shutdownForLanguage();
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		int index = 0;
		assertEquals(new CMacroEntry("MACRO_1", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CMacroEntry("MACRO_2", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CMacroEntry("MACRO_3", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(index, entries.size());
	}

	public void testGCCBuiltinSpecsDetector_Macro_EmptyArgList() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO() VALUE", null);
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO()", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	public void testGCCBuiltinSpecsDetector_Macro_ParamUnused() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO(X) VALUE", null);
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO(X)", "VALUE", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	public void testGCCBuiltinSpecsDetector_Macro_ParamSpace() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO(P1, P2) VALUE(P1, P2)", null);
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	public void testGCCBuiltinSpecsDetector_Macro_ArgsNoValue() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#define MACRO(P1, P2) ", null);
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CMacroEntry("MACRO(P1, P2)", null, ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	public void testGCCBuiltinSpecsDetector_Macro_Args_WhiteSpaces() throws Exception {
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#define \t MACRO_1(P1, P2) VALUE(P1, P2)", null);
		detector.processLine("#define MACRO_2(P1, P2) \t VALUE(P1, P2)", null);
		detector.processLine("#define MACRO_3(P1, P2) VALUE(P1, P2) \t", null);
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		int index = 0;
		assertEquals(new CMacroEntry("MACRO_1(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CMacroEntry("MACRO_2(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CMacroEntry("MACRO_3(P1, P2)", "VALUE(P1, P2)", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(index, entries.size());
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

		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		detector.startup(null);
		detector.startupForLanguage(null);

		detector.processLine(" "+loc+"/misplaced/include1", null);
		detector.processLine("#include \"...\" search starts here:", null);
		detector.processLine(" "+loc+"/local/include", null);
		detector.processLine("#include <...> search starts here:", null);
		detector.processLine(" "+loc+"/usr/include", null);
		detector.processLine(" "+loc+"/usr/include/../include2", null);
		detector.processLine(" "+loc+"/missing/folder", null);
		detector.processLine(" "+loc+"/Library/Frameworks (framework directory)", null);
		detector.processLine("End of search list.", null);
		detector.processLine(" "+loc+"/misplaced/include2", null);
		detector.processLine("Framework search starts here:", null);
		detector.processLine(" "+loc+"/System/Library/Frameworks", null);
		detector.processLine("End of framework search list.", null);
		detector.processLine(" "+loc+"/misplaced/include3", null);
		detector.shutdownForLanguage();
		detector.shutdown();

		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		int index = 0;
		assertEquals(new CIncludePathEntry(loc+"/local/include", ICSettingEntry.LOCAL | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc+"/usr/include", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc+"/usr/include2", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc+"/missing/folder", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc+"/Library/Frameworks", ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc+"/System/Library/Frameworks", ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(index, entries.size());
	}
	
	public void testGCCBuiltinSpecsDetector_Includes_WhiteSpaces() throws Exception {
		String loc = ResourceHelper.createTemporaryFolder().toString();
		
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		detector.startup(null);
		detector.startupForLanguage(null);
		
		detector.processLine("#include \"...\" search starts here:", null);
		detector.processLine(" \t "+loc+"/local/include", null);
		detector.processLine("#include <...> search starts here:", null);
		detector.processLine(loc+"/usr/include", null);
		detector.processLine(" "+loc+"/Library/Frameworks \t (framework directory)", null);
		detector.processLine("End of search list.", null);
		detector.processLine("Framework search starts here:", null);
		detector.processLine(" "+loc+"/System/Library/Frameworks \t ", null);
		detector.processLine("End of framework search list.", null);
		detector.shutdownForLanguage();
		detector.shutdown();
		
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		int index = 0;
		assertEquals(new CIncludePathEntry(loc+"/local/include", ICSettingEntry.LOCAL | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc+"/usr/include", ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc+"/Library/Frameworks", ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(new CIncludePathEntry(loc+"/System/Library/Frameworks", ICSettingEntry.FRAMEWORKS_MAC | ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(index++));
		assertEquals(index, entries.size());
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
		
		MockGCCBuiltinSpecsDetector detector = new MockGCCBuiltinSpecsDetector();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#include <...> search starts here:", null);
		detector.processLine(" "+linkPath.toString()+"/..", null);
		detector.processLine("End of search list.", null);
		detector.shutdownForLanguage();
		detector.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CIncludePathEntry(dir2.removeLastSegments(1), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}
	
	public void testGCCBuiltinSpecsDetector_Cygwin_NoProject() throws Exception {
		String windowsLocation;
		String cygwinLocation = "/usr/include";
		try {
			windowsLocation = ResourceHelper.cygwinToWindowsPath(cygwinLocation);
		} catch (UnsupportedOperationException e) {
			// Skip the test if Cygwin is not available.
			return;
		}
		assertTrue("windowsLocation=["+windowsLocation+"]", new Path(windowsLocation).getDevice()!=null);

		MockGCCBuiltinSpecsDetectorCygwin detector = new MockGCCBuiltinSpecsDetectorCygwin();
		
		detector.startup(null);
		detector.startupForLanguage(null);
		detector.processLine("#include <...> search starts here:", null);
		detector.processLine(" /usr/include", null);
		detector.processLine("End of search list.", null);
		detector.shutdownForLanguage();
		detector.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CIncludePathEntry(new Path(windowsLocation), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

	public void testGCCBuiltinSpecsDetector_Cygwin_Configuration() throws Exception {
		String windowsLocation;
		String cygwinLocation = "/usr/include";
		try {
			windowsLocation = ResourceHelper.cygwinToWindowsPath(cygwinLocation);
		} catch (UnsupportedOperationException e) {
			// Skip the test if Cygwin is not available.
			return;
		}
		assertTrue("windowsLocation=["+windowsLocation+"]", new Path(windowsLocation).getDevice()!=null);
		
		// Create model project and folders to test
		String projectName = getName();
		IProject project = ResourceHelper.createCDTProjectWithConfig(projectName);
		ICConfigurationDescription[] cfgDescriptions = getConfigurationDescriptions(project);
		ICConfigurationDescription cfgDescription = cfgDescriptions[0];
		
		MockGCCBuiltinSpecsDetectorCygwin detector = new MockGCCBuiltinSpecsDetectorCygwin();
		
		detector.startup(cfgDescription);
		detector.startupForLanguage(null);
		detector.processLine("#include <...> search starts here:", null);
		detector.processLine(" /usr/include", null);
		detector.processLine("End of search list.", null);
		detector.shutdownForLanguage();
		detector.shutdown();
		
		// check populated entries
		List<ICLanguageSettingEntry> entries = detector.getSettingEntries(null, null, null);
		assertEquals(new CIncludePathEntry(new Path(windowsLocation), ICSettingEntry.BUILTIN | ICSettingEntry.READONLY), entries.get(0));
		assertEquals(1, entries.size());
	}

}
