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

package org.eclipse.cdt.make.core.language.settings.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
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
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsLogger;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
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
 *
 * @since 7.2
 */
public abstract class AbstractBuiltinSpecsDetector extends AbstractLanguageSettingsOutputScanner implements ICListenerAgent {
	public static final String JOB_FAMILY_BUILTIN_SPECS_DETECTOR = "org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector";

	private static final int MONITOR_SCALE = 100;
	private static final int TICKS_CLEAN_MARKERS = 1 * MONITOR_SCALE;
	private static final int TICKS_RUN_FOR_ONE_LANGUAGE = 10 * MONITOR_SCALE;
	private static final int TICKS_SERIALIZATION = 1 * MONITOR_SCALE;
	private static final int TICKS_OUTPUT_PARSING = 1 * MONITOR_SCALE;
	private static final int TICKS_REMOVE_MARKERS = 1 * MONITOR_SCALE;
	private static final int TICKS_EXECUTE_COMMAND = 1 * MONITOR_SCALE;

	private static final String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String PLUGIN_CDT_MAKE_UI_ID = "org.eclipse.cdt.make.ui"; //$NON-NLS-1$
	private static final String GMAKE_ERROR_PARSER_ID = "org.eclipse.cdt.core.GmakeErrorParser"; //$NON-NLS-1$
	private static final String ATTR_PARAMETER = "parameter"; //$NON-NLS-1$
	private static final String ATTR_CONSOLE = "console"; //$NON-NLS-1$

	protected static final String COMPILER_MACRO = "${COMMAND}"; //$NON-NLS-1$
	protected static final String SPEC_FILE_MACRO = "${INPUTS}"; //$NON-NLS-1$
	protected static final String SPEC_EXT_MACRO = "${EXT}"; //$NON-NLS-1$
	protected static final String SPEC_FILE_BASE = "spec"; //$NON-NLS-1$

	private SDMarkerGenerator markerGenerator = new SDMarkerGenerator();

	private String currentCommandResolved = null;
	protected List<ICLanguageSettingEntry> detectedSettingEntries = null;

	protected boolean isExecuted = false;
	protected int collected = 0;

	private boolean isConsoleEnabled = false;
	protected java.io.File specFile = null;
	protected boolean preserveSpecFile = false;

	protected URI mappedRootURI = null;
	protected URI buildDirURI = null;

	private BuildRunnerHelper buildRunnerHelper;

	private class SDMarkerGenerator implements IMarkerGenerator {
		// Scanner discovery markers are defined in org.eclipse.cdt.managedbuilder.core plugin.xml
		protected static final String SCANNER_DISCOVERY_PROBLEM_MARKER = "org.eclipse.cdt.managedbuilder.core.scanner.discovery.problem";
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
			// we have to add the marker in the job or we can deadlock other
			// threads that are responding to a resource delta by doing something
			// that accesses the project description
			Job markerJob = new Job("Adding Scanner Discovery markers") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					// Try to find matching markers and don't put in duplicates
					try {
						IMarker[] cur = problemMarkerInfo.file.findMarkers(SDMarkerGenerator.SCANNER_DISCOVERY_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
						if ((cur != null) && (cur.length > 0)) {
							for (int i = 0; i < cur.length; i++) {
								int sev = ((Integer) cur[i].getAttribute(IMarker.SEVERITY)).intValue();
								String mesg = (String) cur[i].getAttribute(IMarker.MESSAGE);
								if (sev == problemMarkerInfo.severity && mesg.equals(problemMarkerInfo.description)) {
									return Status.OK_STATUS;
								}
							}
						}
					} catch (CoreException e) {
						return new Status(Status.ERROR, MakeCorePlugin.getUniqueIdentifier(), "Error removing markers.", e);
					}

					try {
						IMarker marker = problemMarkerInfo.file.createMarker(SDMarkerGenerator.SCANNER_DISCOVERY_PROBLEM_MARKER);
						marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
						marker.setAttribute(IMarker.SEVERITY, problemMarkerInfo.severity);
						marker.setAttribute(SDMarkerGenerator.ATTR_PROVIDER, providerId);

						if (problemMarkerInfo.file instanceof IWorkspaceRoot) {
							marker.setAttribute(IMarker.LOCATION, "Preferences, C++/Build/Settings/Discovery, [" + providerName + "] options");
						} else {
							marker.setAttribute(IMarker.LOCATION, "Project Properties, C++ Preprocessor Include.../Providers, [" + providerName + "] options");
						}
					} catch (CoreException e) {
						return new Status(Status.ERROR, MakeCorePlugin.getUniqueIdentifier(), "Error adding markers.", e);
					}

					return Status.OK_STATUS;
				}
			};

			markerJob.setRule(problemMarkerInfo.file);
			markerJob.schedule();
		}

		public void deleteMarkers(IResource rc) {
			String providerId = getId();
			try {
				IMarker[] markers = rc.findMarkers(SCANNER_DISCOVERY_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (IMarker marker : markers) {
					if (providerId.equals(marker.getAttribute(ATTR_PROVIDER))) {
						marker.delete();
					}
				}
			} catch (CoreException e) {
				MakeCorePlugin.log(e);
			}
		}

	}

	/**
	 * This ICConsoleParser handles each individual run for one language from
	 * {@link AbstractBuiltinSpecsDetector#runForEachLanguage(ICConfigurationDescription, URI, String[], IProgressMonitor)}
	 *
	 */
	private class ConsoleParserAdapter implements ICBuildOutputParser {
		@Override
		public void startup(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker) throws CoreException {
			// Also see startupForLanguage() in AbstractBuiltinSpecsDetector.runForEachLanguage(...)
			AbstractBuiltinSpecsDetector.this.startup(cfgDescription, cwdTracker);
		}
		@Override
		public boolean processLine(String line) {
			return AbstractBuiltinSpecsDetector.this.processLine(line);
		}
		@Override
		public void shutdown() {
			// not used here, see instead shutdownForLanguage() in AbstractBuiltinSpecsDetector.runForEachLanguage(...)
		}
	}

	/**
	 * The command to run. Some macros could be specified in there:
	 * <ul>
	 * <b>${COMMAND}</b> - compiler command taken from the toolchain.<br>
	 * <b>${INPUTS}</b> - path to spec file which will be placed in workspace area.<br>
	 * <b>${EXT}</b> - file extension calculated from language ID.
	 * </ul>
	 * The parameter could be taken from the extension
	 * in {@code plugin.xml} or from property file.
	 *
	 * @return the command to run.
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

	public void setConsoleEnabled(boolean enable) {
		isConsoleEnabled = enable;
	}

	public boolean isConsoleEnabled() {
		return isConsoleEnabled;
	}

	protected String resolveCommand(String languageId) throws CoreException {
		String cmd = getCommand();
		if (cmd!=null && (cmd.contains(COMPILER_MACRO) || cmd.contains(SPEC_FILE_MACRO) || cmd.contains(SPEC_EXT_MACRO))) {
			if (cmd.contains(COMPILER_MACRO)) {
				String compiler = getCompilerCommand(languageId);
				if (compiler!=null)
					cmd = cmd.replace(COMPILER_MACRO, compiler);
			}
			if (cmd.contains(SPEC_FILE_MACRO)) {
				String specFileName = getSpecFile(languageId);
				if (specFileName!=null)
					cmd = cmd.replace(SPEC_FILE_MACRO, specFileName);
			}
			if (cmd.contains(SPEC_EXT_MACRO)) {
				String specFileExt = getSpecFileExtension(languageId);
				if (specFileExt!=null)
					cmd = cmd.replace(SPEC_EXT_MACRO, specFileExt);
			}
		}
		return cmd;
	}

	/**
	 * TODO
	 */
	@Override
	protected String parseForResourceName(String line) {
		// This works as if workspace-wide
		return null;
	}

	@Override
	protected String determineLanguage() {
		// language id is supposed to be set by run(), just return it
		return currentLanguageId;
	}

	@Override
	protected URI getMappedRootURI(IResource sourceFile, String parsedResourceName) {
		if (mappedRootURI==null) {
			mappedRootURI = super.getMappedRootURI(sourceFile, parsedResourceName);
		}
		return mappedRootURI;
	}

	@Override
	protected URI getBuildDirURI(URI mappedRootURI) {
		if (buildDirURI==null) {
			buildDirURI = super.getBuildDirURI(mappedRootURI);
		}
		return buildDirURI;
	}

	@Override
	public void registerListener(ICConfigurationDescription cfgDescription) {
		// AG FIXME - temporary log to remove before CDT Juno release
		LanguageSettingsLogger.logInfo(getPrefixForLog() + "registerListener [" + System.identityHashCode(this) + "] " + this);

		execute(cfgDescription);
	}

	@Override
	public void unregisterListener() {
		// AG FIXME - temporary log to remove before CDT Juno release
		LanguageSettingsLogger.logInfo(getPrefixForLog() + "unregisterListener [" + System.identityHashCode(this) + "] " + this);
	}

	protected void execute(final ICConfigurationDescription cfgDescription) {
		if (isExecuted) {
			// AG FIXME - temporary log to remove before CDT Juno release
//			LanguageSettingsLogger.logInfo(getPrefixForLog() + "Already executed [" + System.identityHashCode(this) + "] " + this);
			return;
		}
		isExecuted = true;

		Job job = new Job("Discover compiler's built-in language settings") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status;
				try {
					startup(cfgDescription, null);
					status = runForEachLanguage(cfgDescription, null, null, monitor);
				} catch (CoreException e) {
					MakeCorePlugin.log(e);
					status = new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, IStatus.ERROR, "Error running Builtin Specs Detector", e);
				} finally {
					shutdown();
				}

				return status;
			}
			@Override
			public boolean belongsTo(Object family) {
				return family == JOB_FAMILY_BUILTIN_SPECS_DETECTOR;
			}
		};

		IProject ownerProject = null;
		if (currentCfgDescription != null) {
			ICProjectDescription prjDescription = currentCfgDescription.getProjectDescription();
			if (prjDescription != null) {
				ownerProject = prjDescription.getProject();
			}
		}
		ISchedulingRule rule = null;
		if (ownerProject != null) {
			rule = ownerProject.getFile(".settings/language.settings.xml");
		}
		if (rule == null) {
			rule = ResourcesPlugin.getWorkspace().getRoot();
		}
		job.setRule(rule);
		job.schedule();

		// AG FIXME - temporary log to remove before CDT Juno release
		LanguageSettingsLogger.logInfo(getPrefixForLog() + "Execution scheduled [" + System.identityHashCode(this) + "] " + this);
	}

	/**
	 * TODO
	 */
	protected IStatus runForEachLanguage(ICConfigurationDescription cfgDescription, URI workingDirectoryURI, String[] env, IProgressMonitor monitor) {
		MultiStatus status = new MultiStatus(MakeCorePlugin.PLUGIN_ID, IStatus.OK, "Problem running CDT Scanner Discovery provider " + getId(), null);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			boolean isChanged = false;
			mappedRootURI = null;
			buildDirURI = null;

			List<String> languageIds = getLanguageScope();
			if (languageIds != null) {
				monitor.beginTask("CDT Scanner Discovery", TICKS_CLEAN_MARKERS + languageIds.size()*TICKS_RUN_FOR_ONE_LANGUAGE + TICKS_SERIALIZATION);

				IResource markersResource = currentProject != null ? currentProject : ResourcesPlugin.getWorkspace().getRoot();

				monitor.subTask("Clearing markers for " + markersResource.getFullPath());
				markerGenerator.deleteMarkers(markersResource);
				if (monitor.isCanceled())
					throw new OperationCanceledException();

				monitor.worked(TICKS_CLEAN_MARKERS);

				for (String languageId : languageIds) {
					List<ICLanguageSettingEntry> oldEntries = getSettingEntries(cfgDescription, null, languageId);
					try {
						startupForLanguage(languageId);
						runForLanguage(languageId, currentCommandResolved, env, workingDirectoryURI, new SubProgressMonitor(monitor, TICKS_RUN_FOR_ONE_LANGUAGE));

						if (monitor.isCanceled())
							throw new OperationCanceledException();
					} catch (Exception e) {
						IStatus s = new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, IStatus.ERROR, "Error running Builtin Specs Detector", e);
						MakeCorePlugin.log(s);
						status.merge(s);
					} finally {
						shutdownForLanguage();
					}
					if (!isChanged) {
						List<ICLanguageSettingEntry> newEntries = getSettingEntries(cfgDescription, null, languageId);
						isChanged = newEntries != oldEntries;
					}
				}
			}

			monitor.subTask("Serializing results");
			if (isChanged) { // avoids resource and settings change notifications
				IStatus s = serializeLanguageSettings(currentCfgDescription);
				status.merge(s);
			}
			if (monitor.isCanceled())
				throw new OperationCanceledException();

			monitor.worked(TICKS_SERIALIZATION);
		} catch (OperationCanceledException e) {
			if (!status.isOK()) {
				MakeCorePlugin.log(status);
			}
			status.merge(new Status(IStatus.CANCEL, MakeCorePlugin.PLUGIN_ID, IStatus.OK, "Operation cancelled by user", e));
		} finally {
			monitor.done();

			// release resources
			buildDirURI = null;
			mappedRootURI = null;
			shutdown();
			currentCfgDescription = cfgDescription; // current description gets cleared in super.shutdown(), keep it
		}

		return status;
	}

	protected void startupForLanguage(String languageId) throws CoreException {
		currentLanguageId = languageId;

		specFile = null; // can get set in resolveCommand()
		currentCommandResolved = resolveCommand(currentLanguageId);

		detectedSettingEntries = new ArrayList<ICLanguageSettingEntry>();
		collected = 0;
	}

	protected void shutdownForLanguage() {
		if (detectedSettingEntries != null && detectedSettingEntries.size() > 0) {
			collected = detectedSettingEntries.size();

			// AG FIXME - temporary log to remove before CDT Juno release
			LanguageSettingsLogger.logInfo(getPrefixForLog()
					+ getClass().getSimpleName() + " collected " + detectedSettingEntries.size() + " entries" + " for language " + currentLanguageId);

			setSettingEntries(currentCfgDescription, currentResource, currentLanguageId, detectedSettingEntries);
		}
		detectedSettingEntries = null;

		currentCommandResolved = null;
		if (specFile!=null && !preserveSpecFile) {
			specFile.delete();
			specFile = null;
		}

		currentLanguageId = null;
	}

	private void runForLanguage(String languageId, String command, String[] envp, URI workingDirectoryURI, IProgressMonitor monitor) throws CoreException {
		buildRunnerHelper = new BuildRunnerHelper(currentProject);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor.beginTask("Running scanner discovery: " + getName(), TICKS_REMOVE_MARKERS + TICKS_EXECUTE_COMMAND + TICKS_OUTPUT_PARSING);

			IConsole console;
			if (isConsoleEnabled) {
				console = startProviderConsole();
			} else {
				// that looks in extension points registry and won't find the id, this console is not shown
				console = CCorePlugin.getDefault().getConsole(MakeCorePlugin.PLUGIN_ID + ".console.hidden"); //$NON-NLS-1$
			}
			console.start(currentProject);

			IPath program = new Path("");
			String[] args = new String[0];
			String[] cmdArray = CommandLineUtil.argumentsToArray(command);
			if (cmdArray != null && cmdArray.length > 0) {
				program = new Path(cmdArray[0]);
				if (cmdArray.length>1) {
					args = new String[cmdArray.length-1];
					System.arraycopy(cmdArray, 1, args, 0, args.length);
				}
			}

			ICommandLauncher launcher = new CommandLauncher();
			launcher.setProject(currentProject);

			// Using GMAKE_ERROR_PARSER_ID as it can handle generated error messages
			ErrorParserManager epm = new ErrorParserManager(currentProject, markerGenerator, new String[] {GMAKE_ERROR_PARSER_ID});
			ConsoleParserAdapter consoleParser = new ConsoleParserAdapter();
			consoleParser.startup(currentCfgDescription, epm);
			List<IConsoleParser> parsers = new ArrayList<IConsoleParser>();
			parsers.add(consoleParser);

			buildRunnerHelper.setLaunchParameters(launcher, program, args, workingDirectoryURI, envp);
			buildRunnerHelper.prepareStreams(epm, parsers, console, new SubProgressMonitor(monitor, TICKS_OUTPUT_PARSING));

			buildRunnerHelper.removeOldMarkers(currentProject, new SubProgressMonitor(monitor, TICKS_REMOVE_MARKERS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			buildRunnerHelper.greeting("Running scanner discovery: " + getName());

			OutputStream outStream = buildRunnerHelper.getOutputStream();
			OutputStream errStream = buildRunnerHelper.getErrorStream();
			runProgramForLanguage(languageId, command, envp, workingDirectoryURI, outStream, errStream, new SubProgressMonitor(monitor, TICKS_EXECUTE_COMMAND, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			buildRunnerHelper.close();
			buildRunnerHelper.goodbye();

		} catch (Exception e) {
			// AG TODO - better message
			String msg = "Internal error running scanner discovery";
			MakeCorePlugin.log(new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, msg, e)));
		} finally {
			try {
				buildRunnerHelper.close();
			} catch (IOException e) {
				MakeCorePlugin.log(e);
			}
			monitor.done();
		}
	}

	protected int runProgramForLanguage(String languageId, String command, String[] envp, URI workingDirectoryURI, OutputStream consoleOut, OutputStream consoleErr, IProgressMonitor monitor) throws CoreException, IOException {
		return buildRunnerHelper.build(monitor);
	}

	/**
	 * TODO
	 */
	@Override
	protected void setSettingEntries(List<ICLanguageSettingEntry> entries) {
		// Builtin specs detectors collect entries not per line but for the whole output
		if (entries!=null)
			detectedSettingEntries.addAll(entries);
	}

	private IConsole startProviderConsole() {
		String extConsoleId;
		if (currentProject != null) {
			extConsoleId = "org.eclipse.cdt.make.internal.ui.scannerconfig.ScannerDiscoveryConsole";
		} else {
			// FIXME This console is not colored!
			extConsoleId = "org.eclipse.cdt.make.internal.ui.scannerconfig.ScannerDiscoveryGlobalConsole";
		}
		ILanguage ld = LanguageManager.getInstance().getLanguage(currentLanguageId);
		String consoleId = MakeCorePlugin.PLUGIN_ID + '.' + getId() + '.' + currentLanguageId;
		String consoleName = getName() + ", " + ld.getName();
		URL defaultIcon = Platform.getBundle(PLUGIN_CDT_MAKE_UI_ID).getEntry("icons/obj16/inspect_system.gif");

		IConsole console = CCorePlugin.getDefault().getConsole(extConsoleId, consoleId, consoleName, defaultIcon);
		return console;
	}

	protected String getCompilerCommand(String languageId) {
		IStatus status = new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, "Provider "+getId()
				+" unable to find the compiler tool for language " + languageId);
		MakeCorePlugin.log(new CoreException(status));
		return null;
	}

	protected String getSpecFile(String languageId) {
		String specExt = getSpecFileExtension(languageId);
		String ext = ""; //$NON-NLS-1$
		if (specExt != null) {
			ext = '.' + specExt;
		}

		String specFileName = SPEC_FILE_BASE + ext;
		IPath workingLocation = MakeCorePlugin.getWorkingDirectory();
		IPath fileLocation = workingLocation.append(specFileName);

		specFile = new java.io.File(fileLocation.toOSString());
		// will preserve spec file if it was already there otherwise will delete upon finishing
		preserveSpecFile = specFile.exists();
		if (!preserveSpecFile) {
			try {
				specFile.createNewFile();
			} catch (IOException e) {
				MakeCorePlugin.log(e);
			}
		}

		return fileLocation.toString();
	}

	/**
	 * Determine file extension by language id. This implementation retrieves first extension
	 * from the list as there could be multiple extensions associated with the given language.
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
			MakeCorePlugin.log(new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, "Unable to find file extension for language "+languageId));
		}
		return ext;
	}

	protected void printLine(OutputStream stream, String msg) {
		try {
			stream.write((msg + NEWLINE).getBytes());
			stream.flush();
		} catch (IOException e) {
			MakeCorePlugin.log(e);
		}
	}

	@Override
	public Element serializeAttributes(Element parentElement) {
		Element elementProvider = super.serializeAttributes(parentElement);
		elementProvider.setAttribute(ATTR_CONSOLE, Boolean.toString(isConsoleEnabled));
		return elementProvider;
	}

	@Override
	public void loadAttributes(Element providerNode) {
		super.loadAttributes(providerNode);

		String consoleValue = XmlUtil.determineAttributeValue(providerNode, ATTR_CONSOLE);
		if (consoleValue!=null)
			isConsoleEnabled = Boolean.parseBoolean(consoleValue);
	}

	@Override
	public void loadEntries(Element providerNode) {
		super.loadEntries(providerNode);
		// TODO - test case for that or maybe introduce "isExecuted" attribute in XML?
		if (!isEmpty())
			isExecuted = true;
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
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isConsoleEnabled ? 1231 : 1237);
		result = prime * result + (isExecuted ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AbstractBuiltinSpecsDetector))
			return false;
		AbstractBuiltinSpecsDetector other = (AbstractBuiltinSpecsDetector) obj;
		if (isConsoleEnabled != other.isConsoleEnabled)
			return false;
		if (isExecuted != other.isExecuted)
			return false;
		return true;
	}
}
