/*******************************************************************************
 * Copyright (c) 2019, 2020 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.language.settings.providers.tests;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
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
import org.eclipse.core.runtime.NullProgressMonitor;
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
	private IFolder fFolderAllExcluded;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			joinLanguageSettingsJobs();
		} catch (Exception e) {
			// ignore
		}
		super.tearDown();
	}

	private void createTestProject() throws Exception {
		createTestProject(true, true, true, true, true, false);
	}

	private void createTestProject(boolean useAbsoluteSourcePath, boolean haveCommandDir, boolean validCommandDir,
			boolean haveCommandLine, boolean validCommandLine) throws Exception {
		createTestProject(useAbsoluteSourcePath, haveCommandDir, validCommandDir, haveCommandLine, validCommandLine,
				false);
	}

	private void createTestProject(boolean useAbsoluteSourcePath, boolean haveCommandDir, boolean validCommandDir,
			boolean haveCommandLine, boolean validCommandLine, boolean haveCommandArguments) throws Exception {
		fProject = ResourceHelper.createCDTProjectWithConfig(getName());
		fFolder = ResourceHelper.createFolder(fProject, "folder");
		fFolderAllExcluded = ResourceHelper.createFolder(fProject, "folder-all-excluded");
		IFolder subfolderAllExcluded = fFolderAllExcluded.getFolder("subfolder-all-excluded");
		subfolderAllExcluded.create(true, true, new NullProgressMonitor());

		fSourceFile = fFolder.getFile("test.cpp");
		fSourceFile2 = fProject.getFile("test.cpp");
		fOutsideCdbSourceFile = fFolder.getFile("outside.cpp");
		List<IFile> files = Arrays.asList(fSourceFile, fSourceFile2, fOutsideCdbSourceFile,
				fFolderAllExcluded.getFile("file1.cpp"), fFolderAllExcluded.getFile("file2.cpp"),
				subfolderAllExcluded.getFile("file3.cpp"));

		files.forEach(file -> {
			try {
				if (file.exists()) {
					file.delete(true, null);
				}
				ResourceHelper.createFile(file, "//comment " + file.getLocation());
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		});

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
		if (haveCommandArguments) {
			command.arguments = new String[] { "g++", "-I" + fFolder.getLocation().toOSString(), "-DFOO=2",
					sourceFilePath };
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

		if (haveCommandArguments) {
			command2.arguments = new String[] { "g++", "-I" + fFolder.getLocation().toOSString(), "-DFOO=3",
					sourceFilePath2 };
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

	private void joinLanguageSettingsJobs() throws InterruptedException {
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

	private ICConfigurationDescription getConfigurationDescription(IProject project, String configId,
			boolean writable) {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();
		// project description
		ICProjectDescription projectDescription = mngr.getProjectDescription(project, writable);
		assertNotNull(projectDescription);
		return projectDescription.getConfigurationById(configId);
	}

	private void addLanguageSettingsProvider(ICConfigurationDescription cfgDescription,
			ILanguageSettingsProvider provider) {
		List<ILanguageSettingsProvider> providers = new ArrayList<>(
				((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders());
		providers.add(provider);
		((ILanguageSettingsProvidersKeeper) cfgDescription).setLanguageSettingProviders(providers);
	}

	private CompilationDatabaseParser createCompilationDatabaseParser() {
		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		return parser;
	}

	private CompilationDatabaseParser getCompilationDatabaseParser() throws CoreException {
		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, false);
		return getCompilationDatabaseParser(cfgDescription);
	}

	private CompilationDatabaseParser getCompilationDatabaseParser(ICConfigurationDescription cfgDescription)
			throws CoreException {
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
		assertExpectedEntries(parser, getConfigurationDescription(fProject, false).getId());
	}

	private void assertExpectedEntries(CompilationDatabaseParser parser, String configId) {
		assertFalse(parser.isEmpty());
		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, configId, false);
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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertExpectedEntries(parser);

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_WithExclusions() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());
		parser.setExcludeFiles(true);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

		assertExpectedEntries(parser);

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		ICSourceEntry[] sourceEntries = resCfgDescription.getSourceEntries();
		assertTrue(CDataUtil.isExcluded(tu.getPath(), sourceEntries));
		assertTrue(CDataUtil.isExcluded(fFolderAllExcluded.getFullPath(), sourceEntries));
		assertFalse(CDataUtil.isExcluded(fFolder.getFullPath(), sourceEntries));
		assertFalse(CDataUtil.isExcluded(fProject.getFullPath(), sourceEntries));
		assertFalse(CDataUtil.isExcluded(fSourceFile.getFullPath(), sourceEntries));
		assertFalse(CDataUtil.isExcluded(fSourceFile2.getFullPath(), sourceEntries));
	}

	public void testParseCDB_NoBuildCommandParser() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());
		parser.setExcludeFiles(true);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);
		addLanguageSettingsProvider(cfgDescription, parser);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT + "foo");
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());
		parser.setExcludeFiles(true);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);
		addLanguageSettingsProvider(cfgDescription, parser);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(new Path("/testParseCDB_NonExistantCDB").toOSString());
		parser.setExcludeFiles(true);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);
		addLanguageSettingsProvider(cfgDescription, parser);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty("");
		parser.setExcludeFiles(true);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);
		addLanguageSettingsProvider(cfgDescription, parser);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getParent().getLocation().toOSString());
		parser.setExcludeFiles(true);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);
		addLanguageSettingsProvider(cfgDescription, parser);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		CompilationDatabaseParser resultParser = getCompilationDatabaseParser();
		assertTrue(resultParser.isEmpty());
		List<ICLanguageSettingEntry> entries = resultParser.getSettingEntries(resCfgDescription, fSourceFile,
				GPPLanguage.ID);
		assertTrue(entries == null);
		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_ProjectRelativeCDBPath() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(new Path("${ProjDirPath}")
				.append(fCdbFile.getFullPath().makeRelativeTo(cProject.getProject().getFullPath())).toOSString());
		parser.setExcludeFiles(true);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

		assertExpectedEntries(parser);

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
	}

	public void testParseCDB_WorkspaceVarCDBPath() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);

		CompilationDatabaseParser parser = (CompilationDatabaseParser) LanguageSettingsManager
				.getExtensionProviderCopy(COMPILATION_DATABASE_PARSER_EXT, true);
		assertTrue(parser.isEmpty());
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(
				new Path("${WorkspaceDirPath}")
						.append(fCdbFile.getFullPath()
								.makeRelativeTo(cProject.getProject().getWorkspace().getRoot().getFullPath()))
						.toOSString());
		parser.setExcludeFiles(true);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

		assertExpectedEntries(parser);

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
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
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());
		parser.setExcludeFiles(true);

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);
		addLanguageSettingsProvider(cfgDescription, parser);

		assertThrows(CoreException.class, () -> parser.processCompileCommandsFile(null, cfgDescription));

		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		assertFalse(parser.isEmpty());
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

		ICConfigurationDescription resCfgDescription = getConfigurationDescription(fProject, false);
		assertNull(parser.getSettingEntries(resCfgDescription, fSourceFile, GPPLanguage.ID));
		assertNull(parser.getSettingEntries(resCfgDescription, fSourceFile2, GPPLanguage.ID));

		assertFalse(CDataUtil.isExcluded(tu.getPath(), resCfgDescription.getSourceEntries()));
	}

	public void testParseCDB_CommandArguments() throws Exception {
		createTestProject(true, true, true, false, false, true);

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		parser.processCompileCommandsFile(null, cfgDescription);
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();

		assertExpectedEntries(parser);
	}

	public void testClear() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

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

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

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

	/*
	 * This test not only checks that the language settings are updated when the CDB file timestamp changes,
	 * it also tests the scenario where the parser is called with a read-only configuration description,
	 * which typically happens when the configuration gets first loaded. Testing the read-only case on
	 * its own is difficult because there is currently no way to clear the entries without reloading them when
	 * the project description is written so running the parser again after that with the read-only config
	 * would not yield any language settings differences. Updating the time-stamp allows us to see a
	 * difference before/after even running on a read-only config.
	 */
	public void testParseCDB_UpdateWithModifiedCDB_ReadonlyConfig() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser parser = createCompilationDatabaseParser();
		parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);
		addLanguageSettingsProvider(cfgDescription, parser);
		CoreModel.getDefault().setProjectDescription(cfgDescription.getProjectDescription().getProject(),
				cfgDescription.getProjectDescription());
		joinLanguageSettingsJobs();
		parser = getCompilationDatabaseParser();
		assertFalse(parser.isEmpty());

		cfgDescription = getConfigurationDescription(fProject, false);
		assertExpectedEntries(parser);

		assertFalse(CDataUtil.isExcluded(tu.getPath(), cfgDescription.getSourceEntries()));

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

		parser = getCompilationDatabaseParser(cfgDescription);
		parser.setExcludeFiles(true);
		// Intentionally using a read-only cfgDescription to test this case.
		parser.processCompileCommandsFile(null, cfgDescription);
		joinLanguageSettingsJobs();

		cfgDescription = getConfigurationDescription(fProject, false);
		parser = getCompilationDatabaseParser(cfgDescription);

		assertTrue(CDataUtil.isExcluded(tu.getPath(), cfgDescription.getSourceEntries()));

		assertFalse(parser.isEmpty());
		List<ICLanguageSettingEntry> entries = parser.getSettingEntries(cfgDescription, fSourceFile, GPPLanguage.ID);

		CIncludePathEntry expected = new CIncludePathEntry("/${ProjName}/folder",
				CIncludePathEntry.VALUE_WORKSPACE_PATH);
		CIncludePathEntry entry = (CIncludePathEntry) entries.get(0);
		assertEquals(expected.getName(), entry.getName());
		assertEquals(expected.getValue(), entry.getValue());
		assertEquals(expected.getKind(), entry.getKind());
		assertEquals(expected.getFlags(), entry.getFlags());
		assertEquals(expected, entry);

		assertEquals(new CMacroEntry("FOO", "200", 0), entries.get(1));

		entries = parser.getSettingEntries(cfgDescription, fSourceFile2, GPPLanguage.ID);
		assertNull(entries);
	}

	public void testParseCDB_ReloadActiveConfigOnly() throws Exception {
		createTestProject();

		ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(fProject);
		ICElement ce = CCorePlugin.getDefault().getCoreModel().create(fOutsideCdbSourceFile.getFullPath());
		ITranslationUnit tu = (ITranslationUnit) ce;
		assertFalse(
				CDataUtil.isExcluded(tu.getPath(), getConfigurationDescription(fProject, false).getSourceEntries()));

		CompilationDatabaseParser config1Parser = createCompilationDatabaseParser();
		config1Parser.setBuildParserId(GCC_BUILD_COMMAND_PARSER_EXT);
		config1Parser.setCompilationDataBasePathProperty(fCdbFile.getLocation().toOSString());

		ICConfigurationDescription cfgDescription = getConfigurationDescription(fProject, true);

		addLanguageSettingsProvider(cfgDescription, config1Parser);
		final String config1_id = cfgDescription.getId();

		config1Parser.processCompileCommandsFile(null, cfgDescription);
		ICProjectDescription projectDescription = cfgDescription.getProjectDescription();
		CoreModel.getDefault().setProjectDescription(fProject, projectDescription);
		joinLanguageSettingsJobs();

		assertExpectedEntries(config1Parser);

		// Add a second config with same language settings provider configuration
		final String config2_id = "test.config2";
		ICConfigurationDescription cfgDescription2 = projectDescription.createConfiguration(config2_id,
				config2_id + " Name", cfgDescription);

		CompilationDatabaseParser config2Parser = getCompilationDatabaseParser(cfgDescription2);
		config2Parser.processCompileCommandsFile(null, cfgDescription2);

		CoreModel.getDefault().setProjectDescription(fProject, projectDescription);
		joinLanguageSettingsJobs();

		assertExpectedEntries(config2Parser, config2_id);

		// Touch the CDB to allow a reload.
		while (fCdbFile.getLocalTimeStamp() / 1000 == System.currentTimeMillis() / 1000) {
			// In case the system doesn't support milliseconds granularity.
			Thread.sleep(5);
		}
		fCdbFile.setLocalTimeStamp(System.currentTimeMillis());

		String oldTimeStampConfig1 = config1Parser.getProperty(ATTR_CDB_MODIFIED_TIME);
		String oldTimeStampConfig2 = config2Parser.getProperty(ATTR_CDB_MODIFIED_TIME);
		cfgDescription = getConfigurationDescription(fProject, config1_id, true);
		ICConfigurationDescription defaultCfgDescription = cfgDescription.getProjectDescription()
				.getDefaultSettingConfiguration();
		assertEquals(defaultCfgDescription, cfgDescription);
		cfgDescription2 = getConfigurationDescription(fProject, config2_id, true);
		assertNotEquals(defaultCfgDescription, cfgDescription2);

		// Each language settings provider register to their respective config description when a project description is reloaded, this simulates that.
		config1Parser.processCompileCommandsFile(null, cfgDescription);
		joinLanguageSettingsJobs();
		config2Parser.processCompileCommandsFile(null, cfgDescription2);
		joinLanguageSettingsJobs();

		assertNotEquals(oldTimeStampConfig1, config1Parser.getProperty(ATTR_CDB_MODIFIED_TIME));
		assertEquals(oldTimeStampConfig2, config2Parser.getProperty(ATTR_CDB_MODIFIED_TIME));
	}
}
