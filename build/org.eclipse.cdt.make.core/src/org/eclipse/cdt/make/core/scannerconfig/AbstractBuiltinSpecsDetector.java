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

package org.eclipse.cdt.make.core.scannerconfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICConsoleParser;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.language.settings.providers.ICListenerRegisterer;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
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

public abstract class AbstractBuiltinSpecsDetector extends AbstractLanguageSettingsOutputScanner
		implements ICListenerRegisterer, IResourceChangeListener {
	private static final int TICKS_STREAM_MONITOR = 100;
	private static final int TICKS_CLEAN_MARKERS = 1;
	private static final int TICKS_RUN_FOR_ONE_LANGUAGE = 10;
	private static final int TICKS_SERIALIZATION = 1;
	private static final int TICKS_SCALE = 100;
	private static final String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String PLUGIN_CDT_MAKE_UI_ID = "org.eclipse.cdt.make.ui"; //$NON-NLS-1$
	private static final String GMAKE_ERROR_PARSER_ID = "org.eclipse.cdt.core.GmakeErrorParser"; //$NON-NLS-1$
	private static final String PATH_ENV = "PATH"; //$NON-NLS-1$
	private static final String ATTR_CONSOLE = "console"; //$NON-NLS-1$

	protected static final String COMPILER_MACRO = "${COMMAND}"; //$NON-NLS-1$
	protected static final String SPEC_FILE_MACRO = "${INPUTS}"; //$NON-NLS-1$
	protected static final String SPEC_EXT_MACRO = "${EXT}"; //$NON-NLS-1$
	protected static final String SPEC_FILE_BASE = "spec"; //$NON-NLS-1$

	private String currentCommandResolved = null;
	protected List<ICLanguageSettingEntry> detectedSettingEntries = null;

	protected boolean isExecuted = false;
	protected int collected = 0;

	private boolean isConsoleEnabled = false;
	protected java.io.File specFile = null;
	protected boolean preserveSpecFile = false;

	protected URI mappedRootURI = null;
	protected URI buildDirURI = null;
	
	private class SDMarkerGenerator implements IMarkerGenerator {
		protected static final String SCANNER_DISCOVERY_PROBLEM_MARKER = MakeCorePlugin.PLUGIN_ID + ".scanner.discovery.problem"; //$NON-NLS-1$
		protected static final String PROVIDER = "provider"; //$NON-NLS-1$

		public void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar) {
			ProblemMarkerInfo info = new ProblemMarkerInfo(file, lineNumber, errorDesc, severity, errorVar);
			addMarker(info);
		}

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
					
					// add new marker
					try {
						IMarker marker = problemMarkerInfo.file.createMarker(SDMarkerGenerator.SCANNER_DISCOVERY_PROBLEM_MARKER);
						marker.setAttribute(IMarker.MESSAGE, problemMarkerInfo.description);
						marker.setAttribute(IMarker.SEVERITY, problemMarkerInfo.severity);
						marker.setAttribute(SDMarkerGenerator.PROVIDER, providerId);
						
						if (problemMarkerInfo.file instanceof IWorkspaceRoot) {
							marker.setAttribute(IMarker.LOCATION, "SD90 Providers, [" + providerName + "] options in Preferences");
						} else {
							marker.setAttribute(IMarker.LOCATION, "SD90 Providers, [" + providerName + "] options in project properties");
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
		
	}
	
	/**
	 * This ICConsoleParser handles each individual run for one language from
	 * {@link AbstractBuiltinSpecsDetector#runForEachLanguage(ICConfigurationDescription, IPath, String[], IProgressMonitor)}
	 *
	 */
	private class ConsoleParser implements ICConsoleParser {
		public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
			// not used here, see instead startupForLanguage() in AbstractBuiltinSpecsDetector.runForEachLanguage(...)
		}
		public boolean processLine(String line) {
			return AbstractBuiltinSpecsDetector.this.processLine(line, errorParserManager);
		}
		public void shutdown() {
			// not used here, see instead shutdownForLanguage() in AbstractBuiltinSpecsDetector.runForEachLanguage(...)
		}
	}

	public void setConsoleEnabled(boolean enable) {
		isConsoleEnabled = enable;
	}

	public boolean isConsoleEnabled() {
		return isConsoleEnabled;
	}

	protected String resolveCommand(String languageId) throws CoreException {
		String cmd = getCustomParameter();
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

	public void registerListener(ICConfigurationDescription cfgDescription) {
		currentCfgDescription = cfgDescription;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD);
		// TODO - remove me
		CCorePlugin.log(new Status(IStatus.INFO,CCorePlugin.PLUGIN_ID,
				getPrefixForLog() + "Added listener [" + System.identityHashCode(this) + "] " + this));
	}

	public void unregisterListener() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		// TODO - remove me
		CCorePlugin.log(new Status(IStatus.INFO,CCorePlugin.PLUGIN_ID,
				getPrefixForLog() + "Removed listener [" + System.identityHashCode(this) + "] " + this));
	}

	private String eventToString(IResourceChangeEvent event) {
		String strType = null;
		IResource rc = null;
		if (event != null) {
			int type = event.getType();
			switch (type) {
			case IResourceChangeEvent.POST_CHANGE: strType = "POST_CHANGE";break;
			case IResourceChangeEvent.PRE_CLOSE: strType = "PRE_CLOSE";break;
			case IResourceChangeEvent.PRE_DELETE: strType = "PRE_DELETE";break;
			case IResourceChangeEvent.PRE_BUILD: strType = "PRE_BUILD";break;
			case IResourceChangeEvent.POST_BUILD: strType = "POST_BUILD";break;
			case IResourceChangeEvent.PRE_REFRESH: strType = "PRE_REFRESH";break;
			default: strType = "unknown";break;
			}
			strType += "=" + Integer.toHexString(type);
			
			IResourceDelta delta = event.getDelta();
			rc = delta!=null ? delta.getResource() : null;
		}
		String result = "Event " + strType + ", " + rc;
		return result;
	}

	public void resourceChanged(IResourceChangeEvent event) {
			System.out.println(eventToString(event));
			
	//		if (event.getType() != IResourceChangeEvent.PRE_BUILD)
	//			return;
	//		
	//		IResourceDelta delta = event.getDelta();
	//		delta.getKind();
	//		delta.getFlags();
			
			execute();
		}

	protected void execute() {
		if (isExecuted) {
//			// TODO - remove me
//			CCorePlugin.log(new Status(IStatus.INFO,CCorePlugin.PLUGIN_ID,
//					getPrefixForLog() + "Already executed [" + System.identityHashCode(this) + "] " + this));
			return;
		}
		isExecuted = true;
		
		Job job = new Job("Discover compiler's built-in language settings") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return runForEachLanguage(currentCfgDescription, null, null, monitor);
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
		
		// TODO - remove me
		CCorePlugin.log(new Status(IStatus.INFO,CCorePlugin.PLUGIN_ID,
				getPrefixForLog() + "Execution scheduled [" + System.identityHashCode(this) + "] " + this));
	
	}

	/**
	 * TODO
	 */
	protected IStatus runForEachLanguage(ICConfigurationDescription cfgDescription, IPath workingDirectory,
			String[] env, IProgressMonitor monitor) {

		try {
			startup(cfgDescription);
		} catch (CoreException e) {
			IStatus status = new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, IStatus.ERROR, "Error preparing to run Builtin Specs Detector", e);
			MakeCorePlugin.log(status);
			return status;
		}

		MultiStatus status = new MultiStatus(MakeCorePlugin.PLUGIN_ID, IStatus.OK, "Problem running CDT Scanner Discovery provider " + getId(), null);
		
		boolean isChanged = false;
		mappedRootURI = null;
		buildDirURI = null;
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			
			List<String> languageIds = getLanguageScope();
			if (languageIds != null) {
				int totalWork = TICKS_CLEAN_MARKERS + languageIds.size()*TICKS_RUN_FOR_ONE_LANGUAGE + TICKS_SERIALIZATION;
				monitor.beginTask("CDT Scanner Discovery", totalWork * TICKS_SCALE);
				
				IResource markersResource = currentProject!= null ? currentProject : ResourcesPlugin.getWorkspace().getRoot();
				
				// clear old markers
				monitor.subTask("Clearing stale markers");
				try {
					IMarker[] cur = markersResource.findMarkers(SDMarkerGenerator.SCANNER_DISCOVERY_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
					for (IMarker marker : cur) {
						if (getId().equals(marker.getAttribute(SDMarkerGenerator.PROVIDER))) {
							marker.delete();
						}
					}
				} catch (CoreException e) {
					MakeCorePlugin.log(e);
				}
				
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				
				monitor.worked(TICKS_CLEAN_MARKERS * TICKS_SCALE);
				
				for (String languageId : languageIds) {
					List<ICLanguageSettingEntry> oldEntries = getSettingEntries(cfgDescription, null, languageId);
					try {
						startupForLanguage(languageId);
						if (monitor.isCanceled())
							throw new OperationCanceledException();
						
						runForLanguage(workingDirectory, env, new SubProgressMonitor(monitor, TICKS_RUN_FOR_ONE_LANGUAGE * TICKS_SCALE));
						if (monitor.isCanceled())
							throw new OperationCanceledException();
					} catch (CoreException e) {
						IStatus s = new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, IStatus.ERROR, "Error running Builtin Specs Detector", e);
						MakeCorePlugin.log(s);
						status.merge(s);
					} finally {
						shutdownForLanguage();
					}
					List<ICLanguageSettingEntry> newEntries = getSettingEntries(cfgDescription, null, languageId);
					isChanged = isChanged || newEntries != oldEntries;
					
				}
			}
	
			monitor.subTask("Serializing results");
			if (isChanged) { // avoids resource and settings change notifications
				try {
					if (currentCfgDescription != null) {
						LanguageSettingsProvidersSerializer.serializeLanguageSettings(currentCfgDescription.getProjectDescription());
					} else {
						LanguageSettingsProvidersSerializer.serializeLanguageSettingsWorkspace();
					}
				} catch (CoreException e) {
					IStatus s = new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, IStatus.ERROR, "Error serializing language settings", e);
					MakeCorePlugin.log(s);
					status.merge(s);
				}
				
				// AG: FIXME - rather send event that ls settings changed
				if (currentCfgDescription != null) {
					ICProject icProject = CoreModel.getDefault().create(currentProject);
					ICElement[] tuSelection = new ICElement[] {icProject};
					try {
						CCorePlugin.getIndexManager().update(tuSelection, IIndexManager.UPDATE_ALL | IIndexManager.UPDATE_EXTERNAL_FILES_FOR_PROJECT);
					} catch (CoreException e) {
						IStatus s = new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, IStatus.ERROR, "Error updating CDT index", e);
						MakeCorePlugin.log(s);
						status.merge(s);
					}
				} else {
					// TODO
				}
			}
			if (monitor.isCanceled())
				throw new OperationCanceledException();
			
			monitor.worked(TICKS_SERIALIZATION * TICKS_SCALE);
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
			
			IStatus status = new Status(IStatus.INFO, MakeCorePlugin.PLUGIN_ID, getPrefixForLog()
					+ getClass().getSimpleName() + " collected " + detectedSettingEntries.size() + " entries" + " for language " + currentLanguageId);
			MakeCorePlugin.log(status);
			
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

	/**
	 * TODO: test case for this function
	 */
	private void runForLanguage(IPath workingDirectory, String[] env, IProgressMonitor monitor) throws CoreException {
		IConsole console;
		if (isConsoleEnabled) {
			console = startProviderConsole();
		} else {
			// that looks in extension points registry and won't find the id, this console is not shown
			console = CCorePlugin.getDefault().getConsole(MakeCorePlugin.PLUGIN_ID + ".console.hidden"); //$NON-NLS-1$
		}
		console.start(currentProject);
		OutputStream cos = console.getOutputStream();

		// Using GMAKE_ERROR_PARSER_ID as it can handle shell error messages
		errorParserManager = new ErrorParserManager(currentProject, new SDMarkerGenerator(), new String[] {GMAKE_ERROR_PARSER_ID});
		errorParserManager.setOutputStream(cos);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		try {
			// StreamMonitor will do monitor.beginTask(...) 
			StreamMonitor streamMon = new StreamMonitor(monitor, errorParserManager, TICKS_STREAM_MONITOR);
			OutputStream stdout = streamMon;
			OutputStream stderr = streamMon;
	
			String msg = "Running scanner discovery: " + getName();
			printLine(stdout, "**** " + msg + " ****" + NEWLINE);
	
			ConsoleParser consoleParser = new ConsoleParser();
			ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(stdout, stderr, new IConsoleParser[] { consoleParser }, errorParserManager);
			OutputStream consoleOut = sniffer.getOutputStream();
			OutputStream consoleErr = sniffer.getErrorStream();
	
			boolean isSuccess = false;
			try {
				isSuccess = runProgram(currentCommandResolved, env, workingDirectory, monitor, consoleOut, consoleErr);
			} catch (Exception e) {
				MakeCorePlugin.log(e);
			}
			if (!isSuccess) {
				try {
					consoleOut.close();
				} catch (IOException e) {
					MakeCorePlugin.log(e);
				}
				try {
					consoleErr.close();
				} catch (IOException e) {
					MakeCorePlugin.log(e);
				}
			}
		} finally {
			// ensure monitor.done() is called in cases when StreamMonitor fails to do that
			monitor.done();
		}
	}

	/**
	 * TODO
	 * Note that progress monitor life cycle is handled elsewhere. This method assumes that
	 * monitor.beginTask(...) has already been called.
	 */
	protected boolean runProgram(String command, String[] env, IPath workingDirectory, IProgressMonitor monitor,
			OutputStream consoleOut, OutputStream consoleErr) throws CoreException, IOException {
		
		if (command==null || command.trim().length()==0) {
			return false;
		}

		String errMsg = null;
		ICommandLauncher launcher = new CommandLauncher();

		launcher.setProject(currentProject);

		// Print the command for visual interaction.
		launcher.showCommand(true);

		String[] cmdArray = CommandLineUtil.argumentsToArray(command);
		IPath program = new Path(cmdArray[0]);
		String[] args = new String[0];
		if (cmdArray.length>1) {
			args = new String[cmdArray.length-1];
			System.arraycopy(cmdArray, 1, args, 0, args.length);
		}

		if (monitor==null) {
			monitor = new NullProgressMonitor();
		}
		Process p = launcher.execute(program, args, env, workingDirectory, monitor);

		if (p != null) {
			// Before launching give visual cues via the monitor
			monitor.subTask("Invoking command " + command);
			if (launcher.waitAndRead(consoleOut, consoleErr, monitor) != ICommandLauncher.OK) {
				errMsg = launcher.getErrorMessage();
			}
		} else {
			errMsg = launcher.getErrorMessage();
		}
		if (errMsg!=null) {
			String errorPrefix = MakeMessages.getString("ExternalScannerInfoProvider.Error_Prefix"); //$NON-NLS-1$

			String msg = MakeMessages.getFormattedString("ExternalScannerInfoProvider.Provider_Error", command);
			printLine(consoleErr, errorPrefix + msg + NEWLINE);

			// Launching failed, trying to figure out possible cause
			String envPath = getEnvVar(env, PATH_ENV);
			if (!program.isAbsolute() && PathUtil.findProgramLocation(program.toString(), envPath) == null) {
				printLine(consoleErr, errMsg);
				msg = MakeMessages.getFormattedString("ExternalScannerInfoProvider.Working_Directory", workingDirectory); //$NON-NLS-1$
				msg = MakeMessages.getFormattedString("ExternalScannerInfoProvider.Program_Not_In_Path", program); //$NON-NLS-1$
				printLine(consoleErr, errorPrefix + msg + NEWLINE);
				printLine(consoleErr, PATH_ENV + "=[" + envPath + "]" + NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				printLine(consoleErr, errorPrefix + errMsg);
				msg = MakeMessages.getFormattedString("ExternalScannerInfoProvider.Working_Directory", workingDirectory); //$NON-NLS-1$
				printLine(consoleErr, PATH_ENV + "=[" + envPath + "]" + NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return false;
		}
		
		printLine(consoleOut, NEWLINE + "**** Collected " + detectedSettingEntries.size() + " entries. ****");
		return true;
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
			extConsoleId = "org.eclipse.cdt.make.internal.ui.scannerconfig.ScannerDiscoveryGlobalConsole";
		}
		ILanguage ld = LanguageManager.getInstance().getLanguage(currentLanguageId);
		String consoleId = MakeCorePlugin.PLUGIN_ID + '.' + getId() + '.' + currentLanguageId;
		String consoleName = getName() + ", " + ld.getName();
		URL defaultIcon = Platform.getBundle(PLUGIN_CDT_MAKE_UI_ID).getEntry("icons/obj16/inspect_system.gif");
		
		IConsole console = CCorePlugin.getDefault().getConsole(extConsoleId, consoleId, consoleName, defaultIcon);
		return console;
	}

	private String getEnvVar(String[] envStrings, String envVar) {
		String envPath = null;
		if (envStrings!=null) {
			String varPrefix = envVar+'=';
			for (String envStr : envStrings) {
				boolean found = false;
				// need to convert "Path" to "PATH" on Windows
				if (Platform.getOS().equals(Platform.OS_WIN32)) {
					found = envStr.substring(0,varPrefix.length()).toUpperCase().startsWith(varPrefix);
				} else {
					found = envStr.startsWith(varPrefix);
				}
				if (found) {
					envPath = envStr.substring(varPrefix.length());
					break;
				}
			}
		} else {
			envPath = System.getenv(envVar);
		}
		return envPath;
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
