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
package org.eclipse.cdt.managedbuilder.internal.language.settings.providers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ICListenerAgent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

import com.google.gson.Gson;
import com.ibm.icu.text.MessageFormat;

/**
 * This language settings provider takes a compile_commands.json file as input (aka, Compilation Database or CDB) and parses the commands
 * with a chosen build command parser. The command parser can be any implementation of AbstractBuildCommandParser like GCCBuildCommandParser,
 * MSVCBuildCommandParser, etc.
 *
 * The file json file is re-parsed at startup through {@link #registerListener(ICConfigurationDescription)} but only if the timestamp changed.
 * It it also parsed when the options are modified in the UI through {@link #processCompileCommandsFile(IProgressMonitor, ICConfigurationDescription)}
 */
public class CompilationDatabaseParser extends LanguageSettingsSerializableProvider
		implements ICListenerAgent, ILanguageSettingsEditableProvider {

	public static final String JOB_FAMILY_COMPILATION_DATABASE_PARSER = "org.eclipse.cdt.managedbuilder.internal.language.settings.providers.CompilationDatabaseParser"; //$NON-NLS-1$

	private static final String ATTR_CDB_PATH = "cdb-path"; //$NON-NLS-1$
	private static final String ATTR_BUILD_PARSER_ID = "build-parser-id"; //$NON-NLS-1$
	private static final String ATTR_CDB_MODIFIED_TIME = "cdb-modified-time"; //$NON-NLS-1$
	private static final String ATTR_EXCLUDE_FILES = "exclude-files"; //$NON-NLS-1$

	public IPath getCompilationDataBasePath() {
		return Path.fromOSString(getProperty(ATTR_CDB_PATH));
	}

	public void setCompilationDataBasePath(IPath compilationDataBasePath) {
		setProperty(ATTR_CDB_PATH, compilationDataBasePath.toOSString());
	}

	public void setExcludeFiles(boolean selection) {
		setPropertyBool(ATTR_EXCLUDE_FILES, selection);
	}

	public boolean getExcludeFiles() {
		return getPropertyBool(ATTR_EXCLUDE_FILES);
	}

	public void setBuildParserId(String parserId) {
		setProperty(ATTR_BUILD_PARSER_ID, parserId);
	}

	public String getBuildParserId() {
		return getProperty(ATTR_BUILD_PARSER_ID);
	}

	public Long getCDBModifiedTime(String cdbPath) throws IOException {
		FileTime lastModifiedTime = Files.getLastModifiedTime(Paths.get(cdbPath));
		return lastModifiedTime.toMillis();
	}

	// Wanted to use this as a base to also count the number of translation unit
	// for the progress monitor but it's too slow so only use it to exclude for now.
	private abstract class SourceFilesVisitor implements ICElementVisitor {
		@Override
		public boolean visit(ICElement element) throws CoreException {
			int elementType = element.getElementType();
			if (elementType != ICElement.C_UNIT) {
				return elementType == ICElement.C_CCONTAINER || elementType == ICElement.C_PROJECT;
			}

			ITranslationUnit tu = (ITranslationUnit) element;
			if (tu.isSourceUnit()) {
				handleTranslationUnit(tu);
			}
			return false;
		}

		abstract void handleTranslationUnit(ITranslationUnit tu) throws CoreException;
	}

	private final class ExcludeSourceFilesVisitor extends SourceFilesVisitor {
		private final ICConfigurationDescription cfgDescription;
		ICSourceEntry[] entries = null;
		private final IProgressMonitor monitor;
		private final int sourceFilesCount;
		private int nbChecked = 0;

		//Note: monitor already has ticks allocated for number of source files (not considering exclusions though)
		private ExcludeSourceFilesVisitor(IProgressMonitor monitor, int sourceFilesCount,
				ICConfigurationDescription cfgDescription) {
			this.monitor = monitor;
			this.sourceFilesCount = sourceFilesCount;
			this.cfgDescription = cfgDescription;
		}

		public ICSourceEntry[] getSourceEntries() {
			return entries;
		}

		@Override
		void handleTranslationUnit(ITranslationUnit tu) throws CoreException {
			boolean isExcluded = CDataUtil.isExcluded(tu.getPath(), cfgDescription.getSourceEntries());
			if (!isExcluded) {
				List<ICLanguageSettingEntry> list = getSettingEntries(cfgDescription, tu.getResource(),
						tu.getLanguage().getId());
				if (list == null) {
					if (entries == null) {
						entries = cfgDescription.getSourceEntries();
					}
					entries = CDataUtil.setExcluded(tu.getResource().getFullPath(), false, true, entries);
				}
			}
			monitor.worked(1);
			if (nbChecked % 100 == 0) {
				monitor.subTask(String.format(Messages.CompilationDatabaseParser_ProgressExcludingFiles, nbChecked,
						sourceFilesCount));
			}
			nbChecked++;
		}
	}

	private static class CDBWorkingDirectoryTracker implements IWorkingDirectoryTracker {
		URI currentDirectory = null;

		@Override
		public URI getWorkingDirectoryURI() {
			return currentDirectory;
		}

		public void setCurrentDirectory(URI currentDirectory) {
			this.currentDirectory = currentDirectory;
		}
	}

	@Override
	public void registerListener(ICConfigurationDescription cfgDescription) {
		unregisterListener();
		try {
			processCompileCommandsFile(null, cfgDescription);
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
		}
	}

	@Override
	public void unregisterListener() {
	}

	/**
	 * Processes the compilation database based on the attributes previously set.
	 * Parses the commands and sets the language setting entries. If cfgDescription is a writable configuration, it is assumed that the called will call
	 * CoreModel#setProjectDescription. Otherwise if cfgDescription is read-only, the method will restart itself with a writable configuration description and call CoreModel#setProjectDescription.
	 */
	public boolean processCompileCommandsFile(IProgressMonitor monitor, ICConfigurationDescription cfgDescription)
			throws CoreException {
		if (cfgDescription.isReadOnly()) {
			scheduleOnWritableCfgDescription(cfgDescription);
			return false;
		}

		if (getCompilationDataBasePath().isEmpty()) {
			throw new CoreException(new Status(Status.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
					Messages.CompilationDatabaseParser_CDBNotConfigured));
		}

		if (!Files.exists(Paths.get(getCompilationDataBasePath().toOSString()))) {
			throw new CoreException(new Status(Status.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, MessageFormat.format(
					Messages.CompilationDatabaseParser_CDBNotFound, getCompilationDataBasePath().toOSString())));
		}

		try {
			if (!getProperty(ATTR_CDB_MODIFIED_TIME).isEmpty() && getProperty(ATTR_CDB_MODIFIED_TIME)
					.equals(getCDBModifiedTime(getCompilationDataBasePath().toOSString()).toString())) {
				return false;
			}
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
					Messages.CompilationDatabaseParser_ErrorProcessingCompilationDatabase, e));
		}

		if (getBuildParserId().isEmpty()) {
			throw new CoreException(new Status(Status.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
					MessageFormat.format(Messages.CompilationDatabaseParser_BuildCommandParserNotConfigured,
							getCompilationDataBasePath().toOSString())));
		}

		if (!isEmpty()) {
			clear();
		}
		String cdbPath = getCompilationDataBasePath().toOSString();
		Long cdbModifiedTime;
		try {
			cdbModifiedTime = getCDBModifiedTime(cdbPath);
		} catch (Exception e) {
			//setProperty(ATTR_CDB_MODIFIED_TIME, Long.toString(0L));
			throw new CoreException(new Status(Status.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
					Messages.CompilationDatabaseParser_ErrorProcessingCompilationDatabase, e));
		}

		int totalTicks = getExcludeFiles() ? 100 : 60;
		SubMonitor subMonitor = SubMonitor.convert(monitor, totalTicks);
		subMonitor.subTask(Messages.CompilationDatabaseParser_ProgressParsingJSONFile);
		subMonitor.split(5);

		CompileCommand[] compileCommands = null;
		try (FileReader reader = new FileReader(cdbPath)) {
			Gson gson = new Gson();
			compileCommands = gson.fromJson(reader, CompileCommand[].class);
		} catch (Exception e) {
			//setProperty(ATTR_CDB_MODIFIED_TIME, Long.toString(0L));
			throw new CoreException(new Status(Status.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
					Messages.CompilationDatabaseParser_ErrorProcessingCompilationDatabase, e));
		}

		AbstractBuildCommandParser outputParser;
		try {
			outputParser = getBuildCommandParser(cfgDescription, getBuildParserId());
		} catch (Exception e) {
			//setProperty(ATTR_CDB_MODIFIED_TIME, Long.toString(0L));
			throw new CoreException(new Status(Status.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
					Messages.CompilationDatabaseParser_ErrorProcessingCompilationDatabase, e));
		}

		CDBWorkingDirectoryTracker workingDirectoryTracker = new CDBWorkingDirectoryTracker();

		SubMonitor parseCmdsMonitor = SubMonitor.convert(subMonitor.split(50), compileCommands.length);
		outputParser.startup(cfgDescription, workingDirectoryTracker);
		for (int i = 0; i < compileCommands.length; i++) {
			CompileCommand c = compileCommands[i];
			// Don't spam the progress view too much
			if (i % 100 == 0) {
				parseCmdsMonitor.subTask(String.format(Messages.CompilationDatabaseParser_ProgressParsingBuildCommands,
						i, compileCommands.length));
			}
			String dir = c.getDirectory();
			workingDirectoryTracker.setCurrentDirectory(null);
			if (dir != null) {
				File file = new File(dir);
				if (file.exists()) {
					workingDirectoryTracker.setCurrentDirectory(file.toURI());
				}
			}

			outputParser.processLine(c.getCommand());
			parseCmdsMonitor.worked(1);
		}
		LanguageSettingsStorage storage = outputParser.copyStorage();
		SubMonitor entriesMonitor = SubMonitor.convert(subMonitor.split(5), storage.getLanguages().size());
		entriesMonitor.subTask(Messages.CompilationDatabaseParser_ProgressApplyingEntries);
		for (String language : storage.getLanguages()) {
			SubMonitor langMonitor = entriesMonitor.split(1);
			Set<String> resourcePaths = storage.getResourcePaths(language);
			SubMonitor langEntriesMonitor = SubMonitor.convert(langMonitor, resourcePaths.size());
			for (String resourcePath : resourcePaths) {
				IFile file = cfgDescription.getProjectDescription().getProject().getFile(new Path(resourcePath));
				if (file.exists()) {
					List<ICLanguageSettingEntry> settingEntries = storage.getSettingEntries(resourcePath, language);
					setSettingEntries(cfgDescription, file, language, settingEntries);
				}
				langEntriesMonitor.worked(1);
			}
		}

		if (getExcludeFiles()) {
			excludeFiles(cfgDescription, subMonitor);
		}

		setProperty(ATTR_CDB_MODIFIED_TIME, cdbModifiedTime.toString());
		touchProjectDes(cfgDescription.getProjectDescription());
		return true;
	}

	private void scheduleOnWritableCfgDescription(ICConfigurationDescription cfgDescription) {
		WorkspaceJob job = new WorkspaceJob(Messages.CompilationDatabaseParser_Job) {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				// If the config description we have been given is read-only, we need to get a writable one instead in order to be able to set source entries (exclusions).
				// The tricky thing is that in that situation, the CompilationDatabaseParser instance (this) came from the read-only project description so anything that is
				// saved that is not stored in the project description (i.e. calls to setProperties) will be saved to the wrong instance so when we call setProjectDescription, our changes will be ignored.
				// So instead, restart the whole thing with the corresponding CompilationDatabaseParser instance in the writable config.
				IProject project = cfgDescription.getProjectDescription().getProject();
				ICProjectDescription projectDescription = CCorePlugin.getDefault().getCoreModel()
						.getProjectDescription(project.getProject(), true);
				ICConfigurationDescription writableCfg = projectDescription
						.getConfigurationById(cfgDescription.getId());
				if (writableCfg instanceof ILanguageSettingsProvidersKeeper) {
					return Status.CANCEL_STATUS;
				}

				CompilationDatabaseParser parser = null;
				List<ILanguageSettingsProvider> settingProviders = ((ILanguageSettingsProvidersKeeper) writableCfg)
						.getLanguageSettingProviders();
				for (ILanguageSettingsProvider languageSettingsProvider : settingProviders) {
					if (languageSettingsProvider.getId().equals(CompilationDatabaseParser.this.getId())
							&& languageSettingsProvider instanceof CompilationDatabaseParser) {
						parser = (CompilationDatabaseParser) languageSettingsProvider;
						break;
					}
				}

				if (parser == null) {
					// Seems very unlikely to get here. This should mean that the provider was disabled before the job ran.
					return Status.CANCEL_STATUS;
				}

				try {
					if (parser.processCompileCommandsFile(monitor, writableCfg)) {
						CoreModel.getDefault().setProjectDescription(project.getProject(), projectDescription);
					}
				} catch (CoreException e) {
					// If we are running this in a WorkspaceJob it's because the CfgDescription was read-only so we are probably loading the project.
					// We don't want to pop-up jarring error dialogs on start-up. Ideally, CDT would have problem markers for project setup issues.
					ManagedBuilderCorePlugin.log(e);
				}

				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return family == JOB_FAMILY_COMPILATION_DATABASE_PARSER;
			}
		};

		// Using root rule because of call to setProjectDescription above
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
	}

	private void excludeFiles(ICConfigurationDescription cfgDescription, SubMonitor subMonitor) throws CoreException {
		ICProject cProject = CCorePlugin.getDefault().getCoreModel()
				.create(cfgDescription.getProjectDescription().getProject());
		// Getting a approximation of the number of source files we will have to visit based on file names.
		// Much faster than going through the CElements. Then do the real work and report progress.
		// It's possible that the approximation will be pretty wrong if there are a lot of already excluded files
		// then we won't visit them in the ExcludeSourceFilesVisitor and the progress monitor won't be ticked for those.
		int sourceFilesCount[] = new int[1];
		cProject.getProject().accept(new IResourceProxyVisitor() {
			@Override
			public boolean visit(IResourceProxy proxy) throws CoreException {
				if (CoreModel.isValidSourceUnitName(cProject.getProject(), proxy.getName()))
					sourceFilesCount[0]++;
				return true;
			}
		}, IResource.DEPTH_INFINITE, IResource.NONE);
		SubMonitor sourceMonitor = SubMonitor.convert(subMonitor.split(35), sourceFilesCount[0]);

		ExcludeSourceFilesVisitor sourceFileVisitor = new ExcludeSourceFilesVisitor(sourceMonitor, sourceFilesCount[0],
				cfgDescription);
		cProject.accept(sourceFileVisitor);
		ICSourceEntry[] sourceEntries = sourceFileVisitor.getSourceEntries();

		subMonitor.split(5);
		if (sourceEntries != null) {
			cfgDescription.setSourceEntries(sourceEntries);
		}
	}

	private void touchProjectDes(ICProjectDescription desc) {
		// Make sure the project description is marked as modified so that language settings serialization kicks in.
		// We need to let the setProjectDescription do the serialization because we cannot do it on a writable description
		// and we need a writable description because we need to call setSourceEntries!
		final QualifiedName TOUCH_PROPERTY = new QualifiedName(CCorePlugin.PLUGIN_ID, "touch-project"); //$NON-NLS-1$
		desc.setSessionProperty(TOUCH_PROPERTY, ""); //$NON-NLS-1$
		desc.setSessionProperty(TOUCH_PROPERTY, null);
	}

	private AbstractBuildCommandParser getBuildCommandParser(ICConfigurationDescription cfgDesc, String id)
			throws CloneNotSupportedException {
		ICConfigurationDescription configurationDescription = cfgDesc;
		if (configurationDescription instanceof ILanguageSettingsProvidersKeeper) {
			List<ILanguageSettingsProvider> settingProviders = ((ILanguageSettingsProvidersKeeper) configurationDescription)
					.getLanguageSettingProviders();
			for (ILanguageSettingsProvider languageSettingsProvider : settingProviders) {
				if (languageSettingsProvider instanceof AbstractBuildCommandParser
						&& languageSettingsProvider instanceof ILanguageSettingsEditableProvider) {
					AbstractBuildCommandParser buildParser = (AbstractBuildCommandParser) languageSettingsProvider;
					if (buildParser.getId().equals(id))
						return (AbstractBuildCommandParser) ((ILanguageSettingsEditableProvider) buildParser).clone();
				}
			}
		}

		throw new IllegalArgumentException(MessageFormat
				.format(Messages.CompilationDatabaseParser_BuildCommandParserNotFound, id, cfgDesc.getName()));
	}

	@Override
	public boolean isEmpty() {
		// treat provider that has been executed as not empty
		// to let "Clear" button to restart the provider
		return getProperty(ATTR_CDB_MODIFIED_TIME).isEmpty() && super.isEmpty();
	}

	@Override
	public void clear() {
		super.clear();
		setProperty(ATTR_CDB_MODIFIED_TIME, null);
	}

	@Override
	public CompilationDatabaseParser cloneShallow() throws CloneNotSupportedException {
		CompilationDatabaseParser clone = (CompilationDatabaseParser) super.cloneShallow();
		clone.setProperty(ATTR_CDB_MODIFIED_TIME, null);
		return clone;
	}

	@Override
	public CompilationDatabaseParser clone() throws CloneNotSupportedException {
		return (CompilationDatabaseParser) super.clone();
	}
}
