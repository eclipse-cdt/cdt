/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *  Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.BuildRunnerHelper;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeBuilderUtil;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * New default external scanner info provider of type 'run'
 *
 * @author vhirsl
 */
public class DefaultRunSIProvider implements IExternalScannerInfoProvider {
	private static final String GMAKE_ERROR_PARSER_ID = "org.eclipse.cdt.core.GmakeErrorParser"; //$NON-NLS-1$
	private static final String PREF_CONSOLE_ENABLED = "org.eclipse.cdt.make.core.scanner.discovery.console.enabled"; //$NON-NLS-1$

	private static final int PROGRESS_MONITOR_SCALE = 100;
	private static final int TICKS_STREAM_PROGRESS_MONITOR = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_EXECUTE_PROGRAM = 1 * PROGRESS_MONITOR_SCALE;

	protected IResource resource;
	protected String providerId;
	protected IScannerConfigBuilderInfo2 buildInfo;
	protected IScannerInfoCollector collector;
	// To be initialized by a subclass
	protected IPath fWorkingDirectory;
	protected IPath fCompileCommand;
	protected String[] fCompileArguments;

	private SCMarkerGenerator markerGenerator = new SCMarkerGenerator();

	@Override
	public boolean invokeProvider(IProgressMonitor monitor, IResource resource,
			String providerId, IScannerConfigBuilderInfo2 buildInfo,
			IScannerInfoCollector collector) {
		return invokeProvider(monitor, resource, new InfoContext(resource.getProject()), providerId, buildInfo, collector, null);
	}

	@Override
	public boolean invokeProvider(IProgressMonitor monitor,
			IResource resource,
			InfoContext context,
			String providerId,
			IScannerConfigBuilderInfo2 buildInfo,
			IScannerInfoCollector collector,
			Properties env) {
		// initialize fields
		this.resource = resource;
		this.providerId = providerId;
		this.buildInfo = buildInfo;
		this.collector = collector;

		IProject project = resource.getProject();
		BuildRunnerHelper buildRunnerHelper = new BuildRunnerHelper(project);

		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask(MakeMessages.getString("ExternalScannerInfoProvider.Reading_Specs"), //$NON-NLS-1$
					TICKS_STREAM_PROGRESS_MONITOR + TICKS_EXECUTE_PROGRAM);

			// call a subclass to initialize protected fields
			if (!initialize()) {
				return false;
			}

			ILanguage language = context.getLanguage();
			IConsole console;
			if (language!=null && isConsoleEnabled()) {
				String consoleId = MakeCorePlugin.PLUGIN_ID + '.' + providerId + '.' + language.getId();
				String consoleName =  MakeMessages.getFormattedString("ExternalScannerInfoProvider.Console_Name", language.getName()); //$NON-NLS-1$
				console = CCorePlugin.getDefault().getBuildConsole(consoleId, consoleName, null);
			} else {
				// that looks in extension points registry and won't find the id
				console = CCorePlugin.getDefault().getConsole(MakeCorePlugin.PLUGIN_ID + ".console.hidden"); //$NON-NLS-1$
			}
			console.start(project);

			ICommandLauncher launcher = new CommandLauncher();
			launcher.setProject(project);

			String[] comandLineOptions = getCommandLineOptions();
			IPath program = getCommandToLaunch();
			URI workingDirectoryURI = MakeBuilderUtil.getBuildDirectoryURI(project, MakeBuilder.BUILDER_ID);
			String[] envp = setEnvironment(launcher, env);

			ErrorParserManager epm = new ErrorParserManager(project, markerGenerator, new String[] {GMAKE_ERROR_PARSER_ID});

			List<IConsoleParser> parsers = new ArrayList<IConsoleParser>();
			IConsoleParser parser = ScannerInfoConsoleParserFactory.getESIConsoleParser(project, context, providerId, buildInfo, collector, markerGenerator);
			if (parser != null) {
				parsers.add(parser);
			}

			buildRunnerHelper.setLaunchParameters(launcher, program, comandLineOptions, workingDirectoryURI, envp );
			buildRunnerHelper.prepareStreams(epm, parsers, console, new SubProgressMonitor(monitor, TICKS_STREAM_PROGRESS_MONITOR, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			buildRunnerHelper.greeting(MakeMessages.getFormattedString("ExternalScannerInfoProvider.Greeting", project.getName())); //$NON-NLS-1$
			buildRunnerHelper.build(new SubProgressMonitor(monitor, TICKS_EXECUTE_PROGRAM, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			buildRunnerHelper.close();
			buildRunnerHelper.goodbye();

		} catch (Exception e) {
			MakeCorePlugin.log(e);
		} finally {
			try {
				buildRunnerHelper.close();
			} catch (IOException e) {
				MakeCorePlugin.log(e);
			}
			monitor.done();
		}
		return true;
	}

	protected IPath getCommandToLaunch() {
		return fCompileCommand;
	}

	protected String[] getCommandLineOptions() {
		// add additional arguments
		// subclass can change default behavior
		return prepareArguments(
				buildInfo.isUseDefaultProviderCommand(providerId));
	}

	/**
	 * Initialization of protected fields.
	 * Subclasses are most likely to override default implementation.
	 */
	protected boolean initialize() {

		IProject currProject = resource.getProject();
		//fWorkingDirectory = resource.getProject().getLocation();
		URI workingDirURI = MakeBuilderUtil.getBuildDirectoryURI(currProject, MakeBuilder.BUILDER_ID);
		String pathString = EFSExtensionManager.getDefault().getPathFromURI(workingDirURI);
		if(pathString != null) {
			fWorkingDirectory = new Path(pathString);
		}

		else {
			// blow up
			throw new IllegalStateException();
		}

		fCompileCommand = new Path(buildInfo.getProviderRunCommand(providerId));
		fCompileArguments = ScannerConfigUtil.tokenizeStringWithQuotes(buildInfo.getProviderRunArguments(providerId), "\"");//$NON-NLS-1$
		return (fCompileCommand != null);
	}

	/**
	 * Add additional arguments. For example: tso - target specific options
	 * Base class implementation returns compileArguments.
	 * Subclasses are most likely to override default implementation.
	 */
	protected String[] prepareArguments(boolean isDefaultCommand) {
		return fCompileArguments;
	}

	private Properties getEnvMap(ICommandLauncher launcher, Properties initialEnv) {
		// Set the environmennt, some scripts may need the CWD var to be set.
		Properties props = initialEnv != null ? initialEnv : launcher.getEnvironment();

		if (fWorkingDirectory != null) {
			props.put("CWD", fWorkingDirectory.toOSString()); //$NON-NLS-1$
			props.put("PWD", fWorkingDirectory.toOSString()); //$NON-NLS-1$
		}
		// On POSIX (Linux, UNIX) systems reset LANG variable to English with
		// UTF-8 encoding since GNU compilers can handle only UTF-8 characters.
		// Include paths with locale characters will be handled properly regardless
		// of the language as long as the encoding is set to UTF-8.
		// English language is chosen because parser relies on English messages
		// in the output of the 'gcc -v' command.
		props.put("LANGUAGE", "en");        // override for GNU gettext   //$NON-NLS-1$ //$NON-NLS-2$
		props.put("LC_ALL", "en_US.UTF-8"); // for other parts of the system libraries    //$NON-NLS-1$ //$NON-NLS-2$
		return props;
	}

	protected String[] setEnvironment(ICommandLauncher launcher, Properties initialEnv) {
		Properties props = getEnvMap(launcher, initialEnv);
		String[] env = null;
		ArrayList<String> envList = new ArrayList<String>();
		Enumeration<?> names = props.propertyNames();
		if (names != null) {
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				envList.add(key + "=" + props.getProperty(key)); //$NON-NLS-1$
			}
			env = envList.toArray(new String[envList.size()]);
		}
		return env;
	}


	/**
	 * Set preference to stream output of scanner discovery to a console.
	 */
	public static void setConsoleEnabled(boolean value) {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(MakeCorePlugin.PLUGIN_ID);
		node.putBoolean(PREF_CONSOLE_ENABLED, value);
		try {
			node.flush();
		} catch (BackingStoreException e) {
			MakeCorePlugin.log(e);
		}
	}

	/**
	 * Check preference to stream output of scanner discovery to a console.
	 *
	 * @return boolean preference value
	 */
	public static boolean isConsoleEnabled() {
		boolean value = InstanceScope.INSTANCE.getNode(MakeCorePlugin.PLUGIN_ID)
				.getBoolean(PREF_CONSOLE_ENABLED, false);
		return value;
	}

}
