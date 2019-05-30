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
package org.eclipse.cdt.compilationdatabase.internal.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.language.settings.providers.ICListenerAgent;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializableProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.msw.build.core.MSVCBuildCommandParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.w3c.dom.Element;

import com.google.gson.Gson;

public class CompilationDatabaseParser extends LanguageSettingsSerializableProvider
		implements ICListenerAgent, ILanguageSettingsEditableProvider {

	public static final String JOB_FAMILY_COMPILATION_DATABASE_PARSER = "org.eclipse.cdt.compilationdatabase.internal.core.CompilationDatabaseParser"; //$NON-NLS-1$

	private static final String ATTR_CDB_PATH = "cdb-path"; //$NON-NLS-1$
	private boolean isExecuted = false;

	public IPath getCompilationDataBasePath() {
		return Path.fromOSString(getProperty(ATTR_CDB_PATH));
	}

	public void setCompilationDataBasePath(IPath compilationDataBasePath) {
		setProperty(ATTR_CDB_PATH, compilationDataBasePath.toOSString());
	}

	private Map<String, CompileCommand> fCompileCommands = new HashMap<>();

	@Override
	public void registerListener(ICConfigurationDescription cfgDescription) {
		unregisterListener();
		fCompileCommands = new HashMap<>();
		try {
			processCompileCommandsFile(null, cfgDescription);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static class CDBWorkingDirectoryTracker implements IWorkingDirectoryTracker {
		URI currentDirectory = null;

		@Override
		public URI getWorkingDirectoryURI() {
			// TODO Auto-generated method stub
			return currentDirectory;
		}

		public void setCurrentDirectory(URI currentDirectory) {
			this.currentDirectory = currentDirectory;
		}
	}

	private void processCompileCommandsFile(IProgressMonitor monitor, ICConfigurationDescription cfgDescription)
			throws CoreException {
		String cdbPath = getCompilationDataBasePath().toOSString();
		if (isExecuted || cdbPath.isEmpty())
			return;
		isExecuted = true;
		if (Files.exists(Paths.get(cdbPath))) {
			WorkspaceJob job = new WorkspaceJob("Discover compiler Compilation Database language settings") { //$NON-NLS-1$
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					//isExecuted = false;
					if (!isEmpty()) {
						clear();
						serializeLanguageSettings(cfgDescription);
					}
					try (FileReader reader = new FileReader(cdbPath)) {
						Gson gson = new Gson();
						CompileCommand[] compileCommands = gson.fromJson(reader, CompileCommand[].class);
						//TODO: Use value set with setBuildParserId
						//GCCBuildCommandParser outputParser = new GCCBuildCommandParser();
						MSVCBuildCommandParser outputParser = new MSVCBuildCommandParser();
						CDBWorkingDirectoryTracker workingDirectoryTracker = new CDBWorkingDirectoryTracker();

						try {
							outputParser.startup(cfgDescription, null);
							for (CompileCommand c : compileCommands) {
								String dir = c.getDirectory();
								workingDirectoryTracker.setCurrentDirectory(null);
								if (dir != null) {
									File file = new File(dir);
									if (file.exists()) {
										workingDirectoryTracker.setCurrentDirectory(file.toURI());
									}
								}

								outputParser.processLine(c.getCommand());
							}
							LanguageSettingsStorage storage = outputParser.copyStorage();
							for (String language : storage.getLanguages()) {
								for (String resourcePath : storage.getResourcePaths(language)) {
									IFile file = cfgDescription.getProjectDescription().getProject()
											.getFile(new Path(resourcePath));
									if (!file.exists()) {
										continue;
									}
									List<ICLanguageSettingEntry> settingEntries = storage
											.getSettingEntries(resourcePath, language);
									setSettingEntries(cfgDescription, file, language, settingEntries);
								}
							}
						} finally {
							isExecuted = true;
							outputParser.shutdown();
						}
					} catch (IOException e) {
						e.printStackTrace();
						// TODO:
						//				throw new CoreException(Activator.errorStatus(
						//						String.format(Messages.CMakeBuildConfiguration_ProcCompCmds, project.getName()), e));
					}

					return Status.OK_STATUS;
				}

				@Override
				public boolean belongsTo(Object family) {
					return family == JOB_FAMILY_COMPILATION_DATABASE_PARSER;
				}
			};

			ISchedulingRule rule = null;
			if (cfgDescription != null) {
				ICProjectDescription prjDescription = cfgDescription.getProjectDescription();
				if (prjDescription != null) {
					rule = prjDescription.getProject();
				}
			}
			if (rule == null) {
				rule = ResourcesPlugin.getWorkspace().getRoot();
			}
			job.setRule(rule);
			job.schedule();
		}
	}

	@Override
	public void unregisterListener() {
		// TODO Auto-generated method stub
		fCompileCommands.clear();
		//		isExecuted = false;
	}

	@Override
	public void loadEntries(Element providerNode) {
		super.loadEntries(providerNode);
		if (!isEmpty()) {
			isExecuted = true;
		}
	}

	@Override
	public boolean isEmpty() {
		// treat provider that has been executed as not empty
		// to let "Clear" button to restart the provider
		return !isExecuted && super.isEmpty();
	}

	@Override
	public void clear() {
		super.clear();
		isExecuted = false;
	}

	@Override
	public CompilationDatabaseParser cloneShallow() throws CloneNotSupportedException {
		CompilationDatabaseParser clone = (CompilationDatabaseParser) super.cloneShallow();
		clone.isExecuted = false;
		return clone;
	}

	@Override
	public CompilationDatabaseParser clone() throws CloneNotSupportedException {
		return (CompilationDatabaseParser) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isExecuted ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompilationDatabaseParser other = (CompilationDatabaseParser) obj;
		if (isExecuted != other.isExecuted)
			return false;
		return true;
	}

	public void setBuildParserId(String parserId) {
		// TODO Auto-generated method stub

	}
}
