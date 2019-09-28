/*******************************************************************************
 * Copyright (c) 2019 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.language.settings.providers.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.managedbuilder.internal.language.settings.providers.CompilationDatabaseParser;
import org.eclipse.cdt.managedbuilder.internal.language.settings.providers.CompileCommand;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Test cases to test CompilationDatabaseParser (compile_commands.json).
 */
public class CompilationDatabaseParserTest extends BaseTestCase {
	private static final String COMPILATION_DATABASE_PARSER_EXT = "org.eclipse.cdt.managedbuilder.core.CompilationDatabaseParser"; //$NON-NLS-1$
	private static final String GCC_BUILD_COMMAND_PARSER_EXT = "org.eclipse.cdt.managedbuilder.core.GCCBuildCommandParser"; //$NON-NLS-1$

	private static final String ATTR_CDB_PATH = "cdb-path"; //$NON-NLS-1$
	private static final String ATTR_BUILD_PARSER_ID = "build-parser-id"; //$NON-NLS-1$
	private static final String ATTR_CDB_MODIFIED_TIME = "cdb-modified-time"; //$NON-NLS-1$
	private static final String ATTR_EXCLUDE_FILES = "exclude-files"; //$NON-NLS-1$

	private IFile fCdbFile;
	private IFile fSourceFile;
	private IFile fSourceFile2;
	private IFile fOutsideCdbSourceFile;
	private IProject fProject;
	private IFolder fFolder;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			joingLanguageSettingsJobs();
		} catch (Exception e) {
			// ignore
		}
		super.tearDown();
	}

	private void createTestProject() throws Exception {
		createTestProject(true, true, true, true, true);
	}

	private void createTestProject(boolean useAbsoluteSourcePath, boolean haveCommandDir, boolean validCommandDir,
			boolean haveCommandLine, boolean validCommandLine) throws Exception {
		fProject = ResourceHelper.createCDTProjectWithConfig(getName());
		fFolder = ResourceHelper.createFolder(fProject, "folder");

		IFile sourceFile = fFolder.getFile("test.cpp");
		if (sourceFile.exists()) {
			sourceFile.delete(true, null);
		}

		IFile sourceFile2 = fProject.getFile("test.cpp");
		if (sourceFile2.exists()) {
			sourceFile2.delete(true, null);
		}

		fSourceFile = ResourceHelper.createFile(sourceFile, "//comment");
		fSourceFile2 = ResourceHelper.createFile(sourceFile2, "//comment2");

		IFile outsideSourceFile = fFolder.getFile("outside.cpp");
		if (outsideSourceFile.exists()) {
			outsideSourceFile.delete(true, null);
		}
		fOutsideCdbSourceFile = ResourceHelper.createFile(outsideSourceFile, "//comment");

		IFile file = fProject.getFile("compile_commands.json");
		if (file.exists()) {
			file.delete(true, null);
		}

		// Command for proj/folder/test.cpp
		CompileCommand command = new CompileCommand();
		if (haveCommandDir) {
			if (validCommandDir)
				command.directory = fSourceFile.getParent().getLocation().toOSString();
			else
				command.directory = "foo";
		}
		String sourceFilePath = fSourceFile.getLocation().toOSString();
		if (!useAbsoluteSourcePath) {
			sourceFilePath = fSourceFile.getLocation().makeRelativeTo(fSourceFile.getParent().getLocation())
					.toOSString();
		}
		command.file = sourceFilePath;
		if (haveCommandLine) {
			if (validCommandLine)
				command.command = "g++ -I" + fFolder.getLocation().toOSString() + " -DFOO=2 " + sourceFilePath;
			else
				command.command = "foo";
		}

		// Command for proj/test.cpp
		CompileCommand command2 = new CompileCommand();
		if (haveCommandDir) {
			if (validCommandDir)
				command2.directory = fSourceFile2.getParent().getLocation().toOSString();
			else
				command2.directory = "foo";
		}
		String sourceFilePath2 = fSourceFile2.getLocation().toOSString();
		if (!useAbsoluteSourcePath) {
			sourceFilePath2 = fSourceFile2.getLocation().makeRelativeTo(fSourceFile2.getParent().getLocation())
					.toOSString();
		}
		command2.file = sourceFilePath2;
		if (haveCommandLine) {
			if (validCommandLine)
				command2.command = "g++ -I" + fFolder.getLocation().toOSString() + " -DFOO=3 " + sourceFilePath2;
			else
				command2.command = "foo";
		}

		CompileCommand[] commands = new CompileCommand[2];
		commands[0] = command;
		commands[1] = command2;

		String json = new Gson().toJson(commands);
		fCdbFile = ResourceHelper.createFile(file, json);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);
		GCCBuildCommandParser buildCommandParser = (GCCBuildCommandParser) LanguageSettingsManager
				.getExtensionProviderCopy(GCC_BUILD_COMMAND_PARSER_EXT, true);
		assertTrue(cfgDescription instanceof ILanguageSettingsProvidersKeeper);
		List<ILanguageSettingsProvider> providers = new ArrayList<>();
		providers.add(buildCommandParser);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
	}

	private void joingLanguageSettingsJobs() throws InterruptedException {
		Job.getJobManager().join(CompilationDatabaseParser.JOB_FAMILY_COMPILATION_DATABASE_PARSER, null);
		Job.getJobManager().join(AbstractBuildCommandParser.JOB_FAMILY_BUILD_COMMAND_PARSER, null);
		Job.getJobManager().join(LanguageSettingsProvidersSerializer.JOB_FAMILY_SERIALIZE_LANGUAGE_SETTINGS_PROJECT,
				null);
		Job.getJobManager().join(LanguageSettingsProvidersSerializer.JOB_FAMILY_SERIALIZE_LANGUAGE_SETTINGS_WORKSPACE,
				null);
	}

	/**
	 * Helper method to fetch a configuration description.
	 */
	private ICConfigurationDescription getConfigurationDescription(IProject project, boolean writable) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project, writable);
		assertNotNull(projectDescription);
		assertEquals(1, projectDescription.getConfigurations().length);
		// configuration description
		ICConfigurationDescription[] cfgDescriptions = projectDescription.getConfigurations();
		return cfgDescriptions[0];
	}

	private void addLanguageSettingsProvider(ILanguageSettingsProvider provider) throws CoreException {
		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);
		List<ILanguageSettingsProvider> providers = new ArrayList<>(
				((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders());
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
	}

	private CompilationDatabaseParser getCompilationDatabaseParser() throws CoreException {
		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, false);
		List<ILanguageSettingsProvider> settingProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription)
				.getLanguageSettingProviders();
		for (ILanguageSettingsProvider languageSettingsProvider : settingProviders) {
			if (languageSettingsProvider instanceof CompilationDatabaseParser) {
				return (CompilationDatabaseParser) languageSettingsProvider;
			}
		}

		return null;
	}

	private void assertExpectedEntries(CompilationDatabaseParser parser) {
		assertFalse(parser.isEmpty());
		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(resCfgDescription, fSourceFile, GPPLanguage.ID);

		CIncludePathEntry expected = new CIncludePathEntry("/${ProjName}/folder",
				CIncludePathEntry.VALUE_WORKSPACE_PATH);
		CIncludePathEntry entry = (CIncludePathEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CMacroEntry("FOO", "2", 0), entries.get(1));

		entries = parser.getSettingEntries(resCfgDescription, fSourceFile2, GPPLanguage.ID);

		entry = (CIncludePathEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CMacroEntry("FOO", "3", 0), entries.get(1));
	}

	public void testParseCDB_WritableConfigDesc() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertExpectedEntries(parser);

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_ReadonlyConfigDesc() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, false);

		parser.processCompileCommandsFile(null, cfgDescription);
		// processCompileCommandsFile restarts itself in a WorkspaceJob with a writable config desc so we have to wait for the job.
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertExpectedEntries(getCompilationDatabaseParser());

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_WithExclusions() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		parser.setExcludeFiles(true);
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		assertExpectedEntries(parser);

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertTrue(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_ReadonlyConfigDescWithExclusions() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		parser.setExcludeFiles(true);
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, false);

		parser.processCompileCommandsFile(null, cfgDescription);
		// processCompileCommandsFile restarts itself in a WorkspaceJob with a writable config desc so we have to wait for the job.
		joingLanguageSettingsJobs();

		assertExpectedEntries(getCompilationDatabaseParser());

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertTrue(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_NoBuildCommandParser() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		parser.setExcludeFiles(true);
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		CompilationDatabaseParser resultParser = getCompilationDatabaseParser();
		assertTrue(resultParser.isEmpty());
		List<ICLanguageSettingEntry> entries = resultParser.getSettingEntries(resCfgDescription, fSourceFile,
				GPPLanguage.ID);
		assertTrue(entries == null);
		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_InvalidBuildCommandParser() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT + "foo");
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		parser.setExcludeFiles(true);
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		CompilationDatabaseParser resultParser = getCompilationDatabaseParser();
		assertTrue(resultParser.isEmpty());
		List<ICLanguageSettingEntry> entries = resultParser.getSettingEntries(resCfgDescription, fSourceFile,
				GPPLanguage.ID);
		assertTrue(entries == null);
		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_NonExistantCDB() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(new Path("/testParseCDB_NonExistantCDB"));
		parser.setExcludeFiles(true);
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		CompilationDatabaseParser resultParser = getCompilationDatabaseParser();
		assertTrue(resultParser.isEmpty());
		List<ICLanguageSettingEntry> entries = resultParser.getSettingEntries(resCfgDescription, fSourceFile,
				GPPLanguage.ID);
		assertTrue(entries == null);
		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_EmptyCDBPath() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(new Path(""));
		parser.setExcludeFiles(true);
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		CompilationDatabaseParser resultParser = getCompilationDatabaseParser();
		assertTrue(resultParser.isEmpty());
		List<ICLanguageSettingEntry> entries = resultParser.getSettingEntries(resCfgDescription, fSourceFile,
				GPPLanguage.ID);
		assertTrue(entries == null);
		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_DirectoryCDBPath() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getParent().getLocation());
		parser.setExcludeFiles(true);
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		CompilationDatabaseParser resultParser = getCompilationDatabaseParser();
		assertTrue(resultParser.isEmpty());
		List<ICLanguageSettingEntry> entries = resultParser.getSettingEntries(resCfgDescription, fSourceFile,
				GPPLanguage.ID);
		assertTrue(entries == null);
		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_InvalidJson() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		//Make the Json invalid
		String cdbOsString = fCdbFile.getLocation().toOSString();
		Files.write(Paths.get(cdbOsString), new byte[] { 'f', 'o', 'o' });
		try (FileReader reader = new FileReader(cdbOsString)) {
			Gson gson = new Gson();
			CompileCommand[] compileCommands = gson.fromJson(reader, CompileCommand[].class);
			assertTrue("Json should have been invalid and thrown an JsonSyntaxException", false);
		} catch (JsonSyntaxException e) {

		} catch (Exception e) {
			assertTrue("Json should have been invalid and thrown an JsonSyntaxException", false);
		}

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		parser.setExcludeFiles(true);
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));

		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		CompilationDatabaseParser resultParser = getCompilationDatabaseParser();
		assertTrue(resultParser.isEmpty());
		List<ICLanguageSettingEntry> entries = resultParser.getSettingEntries(resCfgDescription, fSourceFile,
				GPPLanguage.ID);
		assertTrue(entries == null);
		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_RelativePaths() throws Exception {
		createTestProject(false, true, true, true, true);

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		assertExpectedEntries(parser);

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_InvalidCommandDir() throws Exception {
		createTestProject(true, true, false, true, true);

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(resCfgDescription, fSourceFile2,
				GPPLanguage.ID);

		// Since the directory could not be used as working dir, both files CDB entries will match
		// the same source file and only the last one will be recorder in language setting entries.
		CIncludePathEntry expected = new CIncludePathEntry("/${ProjName}/folder",
				CIncludePathEntry.VALUE_WORKSPACE_PATH);
		CIncludePathEntry entry = (CIncludePathEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CMacroEntry("FOO", "3", 0), entries.get(1));

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_NoCommandDir() throws Exception {
		createTestProject(true, false, true, true, true);

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(resCfgDescription, fSourceFile2,
				GPPLanguage.ID);

		// Since the directory could not be used as working dir, both files CDB entries will match
		// the same source file and only the last one will be recorder in language setting entries.
		CIncludePathEntry expected = new CIncludePathEntry("/${ProjName}/folder",
				CIncludePathEntry.VALUE_WORKSPACE_PATH);
		CIncludePathEntry entry = (CIncludePathEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CMacroEntry("FOO", "3", 0), entries.get(1));

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_InvalidCommandLine() throws Exception {
		createTestProject(true, true, true, true, false);

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertNull(parser.getSettingEntries(resCfgDescription, fSourceFile, GPPLanguage.ID));
		assertNull(parser.getSettingEntries(resCfgDescription, fSourceFile2, GPPLanguage.ID));

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_NoCommandLine() throws Exception {
		createTestProject(true, true, true, false, true);

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertNull(parser.getSettingEntries(resCfgDescription, fSourceFile, GPPLanguage.ID));
		assertNull(parser.getSettingEntries(resCfgDescription, fSourceFile2, GPPLanguage.ID));

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testClear() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());

		parser.clear();
		assertTrue(parser.isEmpty());
		assertEquals(parser.getProperty(ATTR_CDB_MODIFIED_TIME), "");
	}

	public void testCloneShallow() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());

		CompilationDatabaseParser clonedShallow = parser.cloneShallow();
		assertEquals(clonedShallow.getProperty(ATTR_CDB_PATH), parser.getProperty(ATTR_CDB_PATH));
		assertEquals(clonedShallow.getProperty(ATTR_BUILD_PARSER_ID), parser.getProperty(ATTR_BUILD_PARSER_ID));
		assertEquals(clonedShallow.getProperty(ATTR_EXCLUDE_FILES), parser.getProperty(ATTR_EXCLUDE_FILES));
		assertEquals(clonedShallow.getProperty(ATTR_CDB_MODIFIED_TIME), "");
	}

	public void testClone() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(resCfgDescription, fSourceFile, GPPLanguage.ID);
		assertExpectedEntries(parser);

		CompilationDatabaseParser cloned = parser.clone();
		entries = cloned.getSettingEntries(resCfgDescription, fSourceFile, GPPLanguage.ID);
		assertExpectedEntries(cloned);
		assertEquals(cloned.getProperty(ATTR_CDB_MODIFIED_TIME), parser.getProperty(ATTR_CDB_MODIFIED_TIME));
	}

	public void testParseCDB_testUpdateWithModifiedCDB() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePath(fCdbFile.getLocation());
		addLanguageSettingsProvider(parser);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertExpectedEntries(parser);

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));

		// Modify the CDB, only to contain one file with different macro definition.
		String sourceFilePath = fSourceFile.getLocation().toOSString();
		CompileCommand command = new CompileCommand();
		command.directory = fSourceFile.getParent().getLocation().toOSString();
		command.file = sourceFilePath;
		command.command = "g++ -I" + fFolder.getLocation().toOSString() + " -DFOO=200 " + sourceFilePath;
		CompileCommand[] commands = new CompileCommand[1];
		commands[0] = command;
		String json = new Gson().toJson(commands);
		InputStream inputStream = new ByteArrayInputStream(json.getBytes());
		// Make sure the timestamp is different, in case the code runs fast and
		// in case the system doesn't support milliseconds granularity.
		while (fCdbFile.getLocalTimeStamp() / 1000 == System.currentTimeMillis() / 1000) {
			Thread.sleep(5);
		}
		fCdbFile.setContents(inputStream, IFile.FORCE, null);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joingLanguageSettingsJobs();

		resCfgDescription = getConfigurationDescription(fProject, false);

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));

		assertFalse(parser.isEmpty());
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(resCfgDescription, fSourceFile, GPPLanguage.ID);

		CIncludePathEntry expected = new CIncludePathEntry("/${ProjName}/folder",
				CIncludePathEntry.VALUE_WORKSPACE_PATH);
		CIncludePathEntry entry = (CIncludePathEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CMacroEntry("FOO", "200", 0), entries.get(1));

		entries = parser.getSettingEntries(resCfgDescription, fSourceFile2, GPPLanguage.ID);
		assertNull(entries);
	}
}
