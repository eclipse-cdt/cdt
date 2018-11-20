/*******************************************************************************
 * Copyright (c) 2009, 2016 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *     Liviu Ionescu - Bug 413678: trigger discovery after command line change
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.language.settings.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncherManager;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ICListenerAgent;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.BuildRunnerHelper;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.envvar.IEnvironmentChangeEvent;
import org.eclipse.cdt.utils.envvar.IEnvironmentChangeListener;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Element;

/**
 * Abstract parser capable to execute compiler command printing built-in compiler
 * specs and parse built-in language settings out of it.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class interface is not stable yet as
 * it is not currently (CDT 8.1, Juno) clear how it may need to be used in future.
 * There is no guarantee that this API will work or that it will remain the same.
 * Please do not use this API without consulting with the CDT team.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 *
 * @since 8.1
 */
public abstract class AbstractBuiltinSpecsDetector extends AbstractLanguageSettingsOutputScanner
		implements ICListenerAgent, IEnvironmentChangeListener {

	public static final String JOB_FAMILY_BUILTIN_SPECS_DETECTOR = "org.eclipse.cdt.managedbuilder.AbstractBuiltinSpecsDetector"; //$NON-NLS-1$

	protected static final String COMPILER_MACRO = "${COMMAND}"; //$NON-NLS-1$
	/** @since 8.3 */
	protected static final String FLAGS_MACRO = "${FLAGS}"; //$NON-NLS-1$
	protected static final String SPEC_FILE_MACRO = "${INPUTS}"; //$NON-NLS-1$
	protected static final String SPEC_EXT_MACRO = "${EXT}"; //$NON-NLS-1$
	protected static final String SPEC_FILE_BASE = "spec"; //$NON-NLS-1$

	private static final String CDT_MANAGEDBUILDER_UI_PLUGIN_ID = "org.eclipse.cdt.managedbuilder.ui"; //$NON-NLS-1$
	private static final String SCANNER_DISCOVERY_CONSOLE = "org.eclipse.cdt.managedbuilder.ScannerDiscoveryConsole"; //$NON-NLS-1$
	private static final String SCANNER_DISCOVERY_GLOBAL_CONSOLE = "org.eclipse.cdt.managedbuilder.ScannerDiscoveryGlobalConsole"; //$NON-NLS-1$
	private static final String DEFAULT_CONSOLE_ICON = "icons/obj16/inspect_sys.gif"; //$NON-NLS-1$
	private static final String GMAKE_ERROR_PARSER_ID = "org.eclipse.cdt.core.GmakeErrorParser"; //$NON-NLS-1$

	private static final String ATTR_PARAMETER = "parameter"; //$NON-NLS-1$
	private static final String ATTR_CONSOLE = "console"; //$NON-NLS-1$
	private static final String ATTR_ENV_HASH = "env-hash"; //$NON-NLS-1$

	private static final String ENV_LANGUAGE = "LANGUAGE"; //$NON-NLS-1$
	private static final String ENV_LC_ALL = "LC_ALL"; //$NON-NLS-1$
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	private static final int MONITOR_SCALE = 100;
	private static final int TICKS_REMOVE_MARKERS = 1 * MONITOR_SCALE;
	private static final int TICKS_RUN_FOR_ONE_LANGUAGE = 10 * MONITOR_SCALE;
	private static final int TICKS_SERIALIZATION = 1 * MONITOR_SCALE;
	private static final int TICKS_OUTPUT_PARSING = 1 * MONITOR_SCALE;
	private static final int TICKS_EXECUTE_COMMAND = 1 * MONITOR_SCALE;

	protected URI mappedRootURI = null;
	protected URI buildDirURI = null;
	protected java.io.File specFile = null;
	protected boolean preserveSpecFile = false;
	/** @since 8.2 */
	protected IEnvironmentVariableManager envMngr = null;
	/** @since 8.2 */
	protected volatile Map<String, String> environmentMap = null;

	protected List<ICLanguageSettingEntry> detectedSettingEntries = null;
	protected int collected = 0;
	protected volatile boolean isExecuted = false;

	private static final int HASH_NOT_INITIALIZED = -1;
	private long envPathHash = HASH_NOT_INITIALIZED;

	private BuildRunnerHelper buildRunnerHelper;
	private SDMarkerGenerator markerGenerator = new SDMarkerGenerator();
	private boolean isConsoleEnabled = false;
	private String currentCommandResolved = null;

	private class SDMarkerGenerator implements IMarkerGenerator {
		// Reuse scanner discovery markers defined in org.eclipse.cdt.managedbuilder.core plugin.xml
		protected static final String SCANNER_DISCOVERY_PROBLEM_MARKER = "org.eclipse.cdt.managedbuilder.core.scanner.discovery.problem"; //$NON-NLS-1$
		protected static final String ATTR_PROVIDER = "provider"; //$NON-NLS-1$

		@Override
		public void addMarker(IResource rc, int lineNumber, String errorDesc, int severity, String errorVar) {
			ProblemMarkerInfo info = new ProblemMarkerInfo(rc, lineNumber, errorDesc, severity, errorVar);
			addMarker(info);
		}

		@Override
		public void addMarker(final ProblemMarkerInfo problemMarkerInfo) {
			final String providerName = getName();
			final String providerId = getId();
			// Add markers in a job to avoid deadlocks
			Job markerJob = new Job(
					ManagedMakeMessages.getResourceString("AbstractBuiltinSpecsDetector.AddScannerDiscoveryMarkers")) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					// Avoid duplicates as different languages can generate identical errors
					try {
						IMarker[] markers = problemMarkerInfo.file.findMarkers(
								SDMarkerGenerator.SCANNER_DISCOVERY_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
						for (IMarker marker : markers) {
							int sev = ((Integer) marker.getAttribute(IMarker.SEVERITY)).intValue();
							if (sev == problemMarkerInfo.severity) {
								String msg = (String) marker.getAttribute(IMarker.MESSAGE);
								if (msg != null && msg.equals(problemMarkerInfo.description)) {
									return Status.OK_STATUS;
								}
							}
						}
					} catch (CoreException e) {
						return new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "Error checking markers.", //$NON-NLS-1$
								e);
					}

					try {
						IMarker marker = problemMarkerInfo.file
								.createMarker(SDMarkerGenerator.SCANNER_DISCOVERY_PROBLEM_MARKER);
						marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
						marker.setAttribute(IMarker.SEVERITY, problemMarkerInfo.severity);
						marker.setAttribute(SDMarkerGenerator.ATTR_PROVIDER, providerId);

						if (problemMarkerInfo.file instanceof IWorkspaceRoot) {
							String msgPreferences = ManagedMakeMessages.getFormattedString(
									"AbstractBuiltinSpecsDetector.ScannerDiscoveryMarkerLocationPreferences", //$NON-NLS-1$
									providerName);
							marker.setAttribute(IMarker.LOCATION, msgPreferences);
						} else {
							String msgProperties = ManagedMakeMessages.getFormattedString(
									"AbstractBuiltinSpecsDetector.ScannerDiscoveryMarkerLocationProperties", //$NON-NLS-1$
									providerName);
							marker.setAttribute(IMarker.LOCATION, msgProperties);
						}
					} catch (CoreException e) {
						return new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "Error adding markers.", //$NON-NLS-1$
								e);
					}

					return Status.OK_STATUS;
				}
			};

			markerJob.setRule(problemMarkerInfo.file);
			markerJob.schedule();
		}

		/**
		 * Delete markers previously set by this provider for the resource.
		 *
		 * @param rc - resource to check markers.
		 */
		public void deleteMarkers(IResource rc) {
			if (!rc.isAccessible()) {
				return; // resource might be read-only or project might be closed
			}
			String providerId = getId();
			try {
				IMarker[] markers = rc.findMarkers(SCANNER_DISCOVERY_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (IMarker marker : markers) {
					if (providerId.equals(marker.getAttribute(ATTR_PROVIDER))) {
						marker.delete();
					}
				}
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(
						new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "Error deleting markers.", e)); //$NON-NLS-1$
			}
		}

	}

	/**
	 * Internal ICConsoleParser to handle individual run for one language.
	 */
	private class ConsoleParserAdapter implements ICBuildOutputParser {
		@Override
		public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker)
				throws CoreException {
			AbstractBuiltinSpecsDetector.this.cwdTracker = cwdTracker;
		}

		@Override
		public boolean processLine(String line) {
			return AbstractBuiltinSpecsDetector.this.processLine(line);
		}

		@Override
		public void shutdown() {
			AbstractBuiltinSpecsDetector.this.cwdTracker = null;
		}
	}

	/**
	 * Compiler command without arguments. This value is used to replace macro ${COMMAND}.
	 * In particular, this method is implemented in {@link ToolchainBuiltinSpecsDetector}
	 * which retrieves the command from tool-chain.
	 *
	 * @param languageId - language ID.
	 * @return compiler command without arguments, i.e. compiler program.
	 */
	protected abstract String getCompilerCommand(String languageId);

	/**
	 * The command to run. Some macros could be specified in there:
	 * <ul>
	 * <b>${COMMAND}</b> - compiler command without arguments (compiler program).
	 *    Normally would come from the tool-chain.<br>
	 * <b>${INPUTS}</b> - path to spec file which will be placed in workspace area.<br>
	 * <b>${EXT}</b> - file extension calculated from language ID.
	 * </ul>
	 * The parameter could be taken from the extension
	 * in {@code plugin.xml} or from property file.
	 *
	 * @return the command to run or empty string if command is not defined.
	 */
	public String getCommand() {
		return getProperty(ATTR_PARAMETER);
	}

	/**
	 * Set custom command for the provider. See {@link #getCommand()}.
	 * @param command - value of custom command to set.
	 */
	public void setCommand(String command) {
		setProperty(ATTR_PARAMETER, command);
	}

	/**
	 * @return {@code true} if console output is enabled for this provider, {@code false} otherwise.
	 */
	public boolean isConsoleEnabled() {
		return isConsoleEnabled;
	}

	/**
	 * Enable or disable console output for this provider.
	 *
	 * @param enable - {@code true} to enable console output or {@code false} to disable.
	 */
	public void setConsoleEnabled(boolean enable) {
		isConsoleEnabled = enable;
	}

	/**
	 * Expand macros specified in the compiler command. See {@link #getCommand()} for
	 * the recognized list of macros.
	 *
	 * @param languageId - language ID.
	 * @return - resolved command to run.
	 * @throws CoreException if something goes wrong.
	 */
	protected String resolveCommand(String languageId) throws CoreException {
		String cmd = getCommand();
		if (cmd != null) {
			if (cmd.contains(COMPILER_MACRO)) {
				String compiler = getCompilerCommand(languageId);
				if (compiler != null)
					cmd = cmd.replace(COMPILER_MACRO, compiler);
			}
			if (cmd.contains(FLAGS_MACRO)) {
				String flags = getToolOptions(languageId);
				if (flags != null)
					cmd = cmd.replace(FLAGS_MACRO, flags);
			}
			if (cmd.contains(SPEC_FILE_MACRO)) {
				String specFileName = getSpecFile(languageId);
				if (specFileName != null)
					cmd = cmd.replace(SPEC_FILE_MACRO, specFileName);
			}
			if (cmd.contains(SPEC_EXT_MACRO)) {
				String specFileExt = getSpecFileExtension(languageId);
				if (specFileExt != null)
					cmd = cmd.replace(SPEC_EXT_MACRO, specFileExt);
			}
			if (cmd.contains("${")) { //$NON-NLS-1$
				ICdtVariableManager varManager = CCorePlugin.getDefault().getCdtVariableManager();
				try {
					cmd = varManager.resolveValue(cmd, "", null, currentCfgDescription); //$NON-NLS-1$
				} catch (Exception e) {
					ManagedBuilderCorePlugin.log(e);
				}
			}
		}
		return cmd;
	}

	@Override
	protected String parseResourceName(String line) {
		// Normally built-in specs detectors are per-language and the result applies for the whole workspace.
		// Returning null works workspace-wide here.
		return null;
	}

	@Override
	protected String determineLanguage() {
		// language id is supposed to be set by run(), just return it
		return currentLanguageId;
	}

	@Override
	protected URI getMappedRootURI(IResource sourceFile, String parsedResourceName) {
		// Do not calculate mappedRootURI for each line
		if (mappedRootURI == null) {
			mappedRootURI = super.getMappedRootURI(sourceFile, parsedResourceName);
		}
		return mappedRootURI;
	}

	@Override
	protected URI getBuildDirURI(URI mappedRootURI) {
		if (buildDirURI == null) {
			// do not use build directory from MBS which could be not-existent when the provider runs
			buildDirURI = currentProject != null ? currentProject.getLocationURI() : null;
		}
		return buildDirURI;
	}

	@Override
	public void registerListener(ICConfigurationDescription cfgDescription) {
		currentCfgDescription = cfgDescription;
		EnvironmentVariableManager.fUserSupplier.registerEnvironmentChangeListener(this);

		execute();
	}

	@Override
	public void unregisterListener() {
		EnvironmentVariableManager.fUserSupplier.unregisterEnvironmentChangeListener(this);
	}

	/** @since 8.2 */
	@Override
	public void handleEvent(IEnvironmentChangeEvent event) {
		// here comes workspace environment change
		execute();
	}

	@Override
	public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker)
			throws CoreException {
		super.startup(cfgDescription, cwdTracker);

		mappedRootURI = null;
		buildDirURI = getBuildDirURI(mappedRootURI);
		environmentMap = createEnvironmentMap(currentCfgDescription);
	}

	@Override
	public void shutdown() {
		mappedRootURI = null;
		buildDirURI = null;

		super.shutdown();
	}

	/**
	 * Calculate hash code for the environment
	 */
	private long calculateEnvHash() {
		String envPathValue = environmentMap.get(ENV_PATH);
		long envHashNew = envPathValue != null ? envPathValue.hashCode() : 0;

		List<String> languageIds = getLanguageScope();
		if (languageIds == null) {
			languageIds = new ArrayList<>(1);
			// "null" language indicates that the provider provides for any language
			languageIds.add(null);
		}
		for (String languageId : languageIds) {
			try {
				String command = resolveCommand(languageId);
				if (command != null) {
					envHashNew = 31 * envHashNew + command.hashCode();
				}

				String[] cmdArray = CommandLineUtil.argumentsToArray(command);
				if (cmdArray != null && cmdArray.length > 0) {
					IPath location = new Path(cmdArray[0]);
					if (!location.isAbsolute()) {
						location = PathUtil.findProgramLocation(cmdArray[0], envPathValue);
					}
					if (location != null) {
						java.io.File file = new java.io.File(location.toString());
						try {
							// handles symbolic links as java.io.File.getCanonicalPath() resolves symlinks on UNIX
							file = file.getCanonicalFile();
						} catch (IOException e) {
							ManagedBuilderCorePlugin.log(e);
						}
						long lastModified = file.lastModified();
						envHashNew = 31 * envHashNew + location.hashCode();
						envHashNew = 31 * envHashNew + lastModified;
					}
				}
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
		return envHashNew;
	}

	/**
	 * This method does 2 related things:
	 * <br>
	 * 1. Validate environment, i.e. check that environment for running the command has not changed.
	 * If environment changed {@link #execute()} will rerun the command even if flag {@link #isExecuted}
	 * suggests that it was run already.
	 * <br>
	 * 2. The relevant environment is cached here so the new one is validated against it at the next call.
	 * {@link #validateEnvironment()} will be called right before running the job to execute the command.
	 *
	 * @since 8.2
	 */
	protected boolean validateEnvironment() {
		long envHashNew = calculateEnvHash();
		if (envHashNew != envPathHash) {
			envPathHash = envHashNew;
			return false;
		}

		return true;
	}

	/**
	 * Execute provider's command which is expected to print built-in compiler options (specs) to build output.
	 * The parser will parse output and generate language settings for corresponding resources.
	 */
	protected void execute() {
		environmentMap = createEnvironmentMap(currentCfgDescription);
		if (validateEnvironment() && isExecuted) {
			return;
		}

		WorkspaceJob job = new WorkspaceJob(
				ManagedMakeMessages.getResourceString("AbstractBuiltinSpecsDetector.DiscoverBuiltInSettingsJobName")) { //$NON-NLS-1$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				isExecuted = false;
				if (!isEmpty()) {
					clear();
					serializeLanguageSettings(currentCfgDescription);
				}
				IStatus status;
				try {
					startup(currentCfgDescription, null);
					status = runForEachLanguage(monitor);
				} catch (CoreException e) {
					ManagedBuilderCorePlugin.log(e);
					status = new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, IStatus.ERROR,
							"Error running Builtin Specs Detector", e); //$NON-NLS-1$
				} finally {
					isExecuted = true;
					shutdown();
				}

				return status;
			}

			@Override
			public boolean belongsTo(Object family) {
				return family == JOB_FAMILY_BUILTIN_SPECS_DETECTOR;
			}
		};

		ISchedulingRule rule = null;
		if (currentCfgDescription != null) {
			ICProjectDescription prjDescription = currentCfgDescription.getProjectDescription();
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

	/**
	 * Run built-in specs command for each language.
	 *
	 * @param monitor - progress monitor in the initial state where {@link IProgressMonitor#beginTask(String, int)}
	 *    has not been called yet.
	 * @return status of operation.
	 */
	protected IStatus runForEachLanguage(IProgressMonitor monitor) {
		MultiStatus status = new MultiStatus(ManagedBuilderCorePlugin.PLUGIN_ID, IStatus.OK,
				"Problem running CDT Scanner Discovery provider " + getId(), null); //$NON-NLS-1$

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			boolean isChanged = false;

			List<String> languageIds = getLanguageScope();

			if (languageIds != null) {
				monitor.beginTask(
						ManagedMakeMessages.getResourceString("AbstractBuiltinSpecsDetector.ScannerDiscoveryTaskTitle"), //$NON-NLS-1$
						TICKS_REMOVE_MARKERS + languageIds.size() * TICKS_RUN_FOR_ONE_LANGUAGE + TICKS_SERIALIZATION);

				IResource markersResource = currentProject != null ? currentProject
						: ResourcesPlugin.getWorkspace().getRoot();

				monitor.subTask(ManagedMakeMessages.getFormattedString("AbstractBuiltinSpecsDetector.ClearingMarkers", //$NON-NLS-1$
						markersResource.getFullPath().toString()));
				markerGenerator.deleteMarkers(markersResource);
				if (monitor.isCanceled())
					throw new OperationCanceledException();

				monitor.worked(TICKS_REMOVE_MARKERS);

				for (String languageId : languageIds) {
					List<ICLanguageSettingEntry> oldEntries = getSettingEntries(currentCfgDescription, null,
							languageId);
					try {
						startupForLanguage(languageId);
						runForLanguage(new SubProgressMonitor(monitor, TICKS_RUN_FOR_ONE_LANGUAGE));
					} catch (Exception e) {
						IStatus s = new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, IStatus.ERROR,
								"Error running Builtin Specs Detector", e); //$NON-NLS-1$
						ManagedBuilderCorePlugin.log(s);
						status.merge(s);
					} finally {
						shutdownForLanguage();
					}
					if (!isChanged) {
						List<ICLanguageSettingEntry> newEntries = getSettingEntries(currentCfgDescription, null,
								languageId);
						isChanged = newEntries != oldEntries;
					}

					if (monitor.isCanceled())
						throw new OperationCanceledException();
				}
			}

			monitor.subTask(ManagedMakeMessages.getResourceString("AbstractBuiltinSpecsDetector.SerializingResults")); //$NON-NLS-1$
			if (isChanged) { // avoids resource and settings change notifications
				IStatus s = serializeLanguageSettings(currentCfgDescription);
				status.merge(s);
			}
			monitor.worked(TICKS_SERIALIZATION);

		} catch (OperationCanceledException e) {
			// user chose to cancel operation, do not threaten them with red error signs
		} catch (Exception e) {
			status.merge(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, IStatus.ERROR,
					"Error running Builtin Specs Detector", e)); //$NON-NLS-1$
			ManagedBuilderCorePlugin.log(status);
		} finally {
			monitor.done();
		}

		return status;
	}

	/**
	 * Initialize provider before running for a language.
	 *
	 * @param languageId - language ID.
	 * @throws CoreException if something goes wrong.
	 */
	protected void startupForLanguage(String languageId) throws CoreException {
		currentLanguageId = languageId;

		specFile = null; // init specFile *before* calling resolveCommand(), can be changed in there
		currentCommandResolved = resolveCommand(currentLanguageId);

		detectedSettingEntries = new ArrayList<>();
		collected = 0;
	}

	/**
	 * Save collected entries and dispose temporary data used during run for the language.
	 */
	protected void shutdownForLanguage() {
		if (detectedSettingEntries != null && detectedSettingEntries.size() > 0) {
			collected = detectedSettingEntries.size();
			setSettingEntries(currentCfgDescription, currentResource, currentLanguageId, detectedSettingEntries);
		}
		detectedSettingEntries = null;

		currentCommandResolved = null;
		if (specFile != null && !preserveSpecFile) {
			specFile.delete();
			specFile = null;
		}

		currentLanguageId = null;
	}

	/**
	 * Run built-in specs command for one language.
	 *
	 * @param monitor - progress monitor in the initial state where {@link IProgressMonitor#beginTask(String, int)}
	 *    has not been called yet.
	 */
	private void runForLanguage(IProgressMonitor monitor) throws CoreException {
		buildRunnerHelper = new BuildRunnerHelper(currentProject);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask(
					ManagedMakeMessages.getFormattedString("AbstractBuiltinSpecsDetector.RunningScannerDiscovery", //$NON-NLS-1$
							getName()),
					TICKS_EXECUTE_COMMAND + TICKS_OUTPUT_PARSING);

			IConsole console;
			if (isConsoleEnabled) {
				console = startProviderConsole();
			} else {
				// that looks in extension points registry and won't find the id, this console is not shown
				console = CCorePlugin.getDefault().getConsole(ManagedBuilderCorePlugin.PLUGIN_ID + ".console.hidden"); //$NON-NLS-1$
			}
			console.start(currentProject);

			ICommandLauncher launcher = CommandLauncherManager.getInstance().getCommandLauncher(currentCfgDescription);
			launcher.setProject(currentProject);

			IPath program = new Path(""); //$NON-NLS-1$
			String[] args = new String[0];
			String[] cmdArray = CommandLineUtil.argumentsToArray(currentCommandResolved);
			if (cmdArray != null && cmdArray.length > 0) {
				program = new Path(cmdArray[0]);
				if (cmdArray.length > 1) {
					args = new String[cmdArray.length - 1];
					System.arraycopy(cmdArray, 1, args, 0, args.length);
				}
			}

			String[] envp = toEnvp(environmentMap);

			// Using GMAKE_ERROR_PARSER_ID as it can handle generated error messages
			ErrorParserManager epm = new ErrorParserManager(currentProject, buildDirURI, markerGenerator,
					new String[] { GMAKE_ERROR_PARSER_ID });
			ConsoleParserAdapter consoleParser = new ConsoleParserAdapter();
			consoleParser.startup(currentCfgDescription, epm);
			List<IConsoleParser> parsers = new ArrayList<>();
			parsers.add(consoleParser);

			buildRunnerHelper.setLaunchParameters(launcher, program, args, buildDirURI, envp);
			buildRunnerHelper.prepareStreams(epm, parsers, console,
					new SubProgressMonitor(monitor, TICKS_OUTPUT_PARSING));

			buildRunnerHelper.greeting(ManagedMakeMessages
					.getFormattedString("AbstractBuiltinSpecsDetector.RunningScannerDiscovery", getName())); //$NON-NLS-1$

			OutputStream outStream = buildRunnerHelper.getOutputStream();
			OutputStream errStream = buildRunnerHelper.getErrorStream();
			runProgramForLanguage(currentLanguageId, currentCommandResolved, envp, buildDirURI, outStream, errStream,
					new SubProgressMonitor(monitor, TICKS_EXECUTE_COMMAND,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			buildRunnerHelper.close();
			buildRunnerHelper.goodbye();

		} catch (Exception e) {
			ManagedBuilderCorePlugin.log(new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
					"Error running Builtin Specs Detector", e))); //$NON-NLS-1$
		} finally {
			try {
				buildRunnerHelper.close();
			} catch (IOException e) {
				ManagedBuilderCorePlugin.log(e);
			}
			monitor.done();
		}
	}

	/**
	 * Returns list of environment variables to be used during execution of provider's command.
	 * Implementers are expected to add their variables to the end of the list.
	 *
	 * @return list of environment variables.
	 * @since 8.2
	 */
	protected List<IEnvironmentVariable> getEnvironmentVariables() {
		if (envMngr == null) {
			envMngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
		}
		List<IEnvironmentVariable> vars = new ArrayList<>(
				Arrays.asList(envMngr.getVariables(currentCfgDescription, true)));

		// On POSIX (Linux, UNIX) systems reset language variables to default (English)
		// with UTF-8 encoding since GNU compilers can handle only UTF-8 characters.
		// Include paths with locale characters will be handled properly regardless
		// of the language as long as the encoding is set to UTF-8.
		// English language is set for parser because it relies on English messages
		// in the output of the 'gcc -v' command.
		vars.add(new EnvironmentVariable(ENV_LANGUAGE, "en")); //$NON-NLS-1$
		vars.add(new EnvironmentVariable(ENV_LC_ALL, "C.UTF-8")); //$NON-NLS-1$

		return vars;
	}

	/**
	 * Create a handy map of environment variables.
	 */
	private Map<String, String> createEnvironmentMap(ICConfigurationDescription cfgDescription) {
		Map<String, String> envMap = new HashMap<>();
		for (IEnvironmentVariable var : getEnvironmentVariables()) {
			String name = var.getName();
			if (!envMngr.isVariableCaseSensitive()) {
				name = name.toUpperCase();
			}
			envMap.put(name, var.getValue());
		}
		return envMap;
	}

	/**
	 * Convert map of environment variables to array in format "var=value".
	 */
	private String[] toEnvp(Map<String, String> environmentMap) {
		Set<String> envp = new HashSet<>();
		for (Entry<String, String> var : environmentMap.entrySet()) {
			envp.add(var.getKey() + '=' + var.getValue());
		}
		return envp.toArray(new String[envp.size()]);
	}

	protected int runProgramForLanguage(String languageId, String command, String[] envp, URI workingDirectoryURI,
			OutputStream consoleOut, OutputStream consoleErr, IProgressMonitor monitor)
			throws CoreException, IOException {
		return buildRunnerHelper.build(monitor);
	}

	@Override
	protected void setSettingEntries(List<? extends ICLanguageSettingEntry> entries) {
		// Built-in specs detectors collect entries not per line but for the whole output
		// so collect them to save later when output finishes
		if (entries != null) {
			for (ICLanguageSettingEntry entry : entries) {
				if (detectedSettingEntries != null && !detectedSettingEntries.contains(entry)) {
					detectedSettingEntries.add(entry);
				}
			}
		}
	}

	/**
	 * Create and start the provider console.
	 * @return CDT console.
	 */
	private IConsole startProviderConsole() {
		IConsole console = null;

		if (isConsoleEnabled && currentLanguageId != null) {
			String extConsoleId;
			if (currentProject != null) {
				extConsoleId = SCANNER_DISCOVERY_CONSOLE;
			} else {
				extConsoleId = SCANNER_DISCOVERY_GLOBAL_CONSOLE;
			}
			ILanguage ld = LanguageManager.getInstance().getLanguage(currentLanguageId);
			if (ld != null) {
				String consoleId = ManagedBuilderCorePlugin.PLUGIN_ID + '.' + getId() + '.' + currentLanguageId;
				String consoleName = getName() + ", " + ld.getName(); //$NON-NLS-1$
				URL defaultIcon = Platform.getBundle(CDT_MANAGEDBUILDER_UI_PLUGIN_ID).getEntry(DEFAULT_CONSOLE_ICON);
				if (defaultIcon == null) {
					@SuppressWarnings("nls")
					String msg = "Unable to find icon " + DEFAULT_CONSOLE_ICON + " in plugin "
							+ CDT_MANAGEDBUILDER_UI_PLUGIN_ID;
					ManagedBuilderCorePlugin.log(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, msg));
				}

				console = CCorePlugin.getDefault().getConsole(extConsoleId, consoleId, consoleName, defaultIcon);
			}
		}

		if (console == null) {
			// that looks in extension points registry and won't find the id, this console is not shown
			console = CCorePlugin.getDefault().getConsole(ManagedBuilderCorePlugin.PLUGIN_ID + ".console.hidden"); //$NON-NLS-1$
		}

		return console;
	}

	/**
	 * Get path to spec file which normally would be placed in workspace area.
	 * This value is used to replace macro ${INPUTS}.
	 *
	 * @param languageId - language ID.
	 * @return full path to the specs file.
	 */
	protected String getSpecFile(String languageId) {
		String specExt = getSpecFileExtension(languageId);
		String ext = ""; //$NON-NLS-1$
		if (specExt != null) {
			ext = '.' + specExt;
		}

		String specFileName = SPEC_FILE_BASE + ext;
		IPath workingLocation = ManagedBuilderCorePlugin.getDefault().getStateLocation();
		IPath fileLocation = workingLocation.append(specFileName);

		specFile = new java.io.File(fileLocation.toOSString());
		// will preserve spec file if it was already there otherwise will delete upon finishing
		preserveSpecFile = specFile.exists();
		if (!preserveSpecFile) {
			try {
				// In the typical case it is sufficient to have an empty file.
				specFile.createNewFile();
			} catch (IOException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}

		return fileLocation.toString();
	}

	/**
	 * Determine file extension by language id. This implementation retrieves first extension
	 * from the list as there could be multiple extensions associated with the given language.
	 * This value is used to replace macro ${EXT}.
	 *
	 * @param languageId - given language ID.
	 * @return file extension associated with the language or {@code null} if not found.
	 */
	protected String getSpecFileExtension(String languageId) {
		String ext = null;
		ILanguageDescriptor langDescriptor = LanguageManager.getInstance().getLanguageDescriptor(languageId);
		if (langDescriptor != null) {
			IContentType[] contentTypes = langDescriptor.getContentTypes();
			if (contentTypes != null && contentTypes.length > 0) {
				String[] fileExtensions = contentTypes[0].getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
				if (fileExtensions != null && fileExtensions.length > 0) {
					ext = fileExtensions[0];
				}
			}
		}

		if (ext == null) {
			ManagedBuilderCorePlugin.log(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
					"Unable to find file extension for language " + languageId)); //$NON-NLS-1$
		}
		return ext;
	}

	/**
	 * Determine additional options to pass to scanner discovery command.
	 * These options are intended to come from the tool-chain.
	 *
	 * @param languageId - language ID.
	 * @return additional options to pass to scanner discovery command.
	 *
	 * @since 8.3
	 */
	protected String getToolOptions(String languageId) {
		return ""; //$NON-NLS-1$
	}

	@Override
	public Element serializeAttributes(Element parentElement) {
		Element elementProvider = super.serializeAttributes(parentElement);
		elementProvider.setAttribute(ATTR_CONSOLE, Boolean.toString(isConsoleEnabled));
		if (envPathHash != HASH_NOT_INITIALIZED) {
			elementProvider.setAttribute(ATTR_ENV_HASH, Long.toString(envPathHash));
		}
		return elementProvider;
	}

	@Override
	public void loadAttributes(Element providerNode) {
		super.loadAttributes(providerNode);

		String consoleValue = XmlUtil.determineAttributeValue(providerNode, ATTR_CONSOLE);
		if (consoleValue != null) {
			isConsoleEnabled = Boolean.parseBoolean(consoleValue);
		}

		envPathHash = HASH_NOT_INITIALIZED;
		String envPathHashStr = XmlUtil.determineAttributeValue(providerNode, ATTR_ENV_HASH);
		if (envPathHashStr != null) {
			try {
				envPathHash = Long.parseLong(envPathHashStr);
			} catch (Exception e) {
				ManagedBuilderCorePlugin.log(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
						"Wrong integer format [" + envPathHashStr + "]", e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
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
	protected AbstractBuiltinSpecsDetector cloneShallow() throws CloneNotSupportedException {
		AbstractBuiltinSpecsDetector clone = (AbstractBuiltinSpecsDetector) super.cloneShallow();
		clone.isExecuted = false;
		clone.envMngr = null;
		clone.environmentMap = null;
		clone.envPathHash = HASH_NOT_INITIALIZED;
		this.properties.remove(ATTR_ENV_HASH);
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isConsoleEnabled ? 1231 : 1237);
		result = prime * result + (isExecuted ? 1231 : 1237);
		result = prime * result + Long.valueOf(envPathHash).hashCode();
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
		AbstractBuiltinSpecsDetector other = (AbstractBuiltinSpecsDetector) obj;
		if (isConsoleEnabled != other.isConsoleEnabled)
			return false;
		if (isExecuted != other.isExecuted)
			return false;
		if (envPathHash != other.envPathHash)
			return false;
		return true;
	}
}
