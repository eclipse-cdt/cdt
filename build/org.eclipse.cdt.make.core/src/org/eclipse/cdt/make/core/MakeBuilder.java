/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *                                    Save build output  (bug 294106)
 *     Andrew Gvozdev (Quoin Inc)   - Saving build output implemented in different way (bug 306222)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.language.settings.providers.ICBuildOutputParser;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.IWorkingDirectoryTracker;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.BuildRunnerHelper;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MakeBuilder extends ACBuilder {
	public final static String BUILDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".makeBuilder"; //$NON-NLS-1$
	private static final int PROGRESS_MONITOR_SCALE = 100;
	private static final int TICKS_STREAM_PROGRESS_MONITOR = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_DELETE_MARKERS = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_EXECUTE_COMMAND = 1 * PROGRESS_MONITOR_SCALE;

	private BuildRunnerHelper buildRunnerHelper = null;

	public MakeBuilder() {
	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	@Override
	protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map args0, IProgressMonitor monitor) throws CoreException {
		@SuppressWarnings("unchecked")
		Map<String, String> args = args0;
		if (DEBUG_EVENTS)
			printEvent(kind, args);

		boolean bPerformBuild = true;
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(args, MakeBuilder.BUILDER_ID);
		if (!shouldBuild(kind, info)) {
			return new IProject[0];
		}
		if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
			IResourceDelta delta = getDelta(getProject());
			if (delta != null) {
				IResource res = delta.getResource();
				if (res != null) {
					bPerformBuild = res.getProject().equals(getProject());
				}
			} else {
				bPerformBuild = false;
			}
		}
		if (bPerformBuild) {
			boolean isClean = invokeMake(kind, info, monitor);
			if (isClean) {
				forgetLastBuiltState();
			}
		}
		checkCancel(monitor);
		return getProject().getReferencedProjects();
	}


	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		if (DEBUG_EVENTS)
			printEvent(IncrementalProjectBuilder.CLEAN_BUILD, null);

		final IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(getProject(), BUILDER_ID);
		if (shouldBuild(CLEAN_BUILD, info)) {
			IResourceRuleFactory ruleFactory= ResourcesPlugin.getWorkspace().getRuleFactory();
			final ISchedulingRule rule = ruleFactory.modifyRule(getProject());
			Job backgroundJob = new Job("Standard Make Builder"){  //$NON-NLS-1$
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

							@Override
							public void run(IProgressMonitor monitor) {
								invokeMake(CLEAN_BUILD, info, monitor);
							}
						}, rule, IWorkspace.AVOID_UPDATE, monitor);
					} catch (CoreException e) {
						return e.getStatus();
					}
					IStatus returnStatus = Status.OK_STATUS;
					return returnStatus;
				}


			};

			backgroundJob.setRule(rule);
			backgroundJob.schedule();
		}
	}

	/**
	 * Run build command.
	 *
	 * @param kind - kind of build, constant defined by {@link IncrementalProjectBuilder}
	 * @param info - builder info
	 * @param monitor - progress monitor in the initial state where {@link IProgressMonitor#beginTask(String, int)}
	 *    has not been called yet.
	 * @return {@code true} if the build purpose is to clean, {@code false} otherwise.
	 */
	protected boolean invokeMake(int kind, IMakeBuilderInfo info, IProgressMonitor monitor) {
		boolean isClean = false;
		IProject project = getProject();
		buildRunnerHelper = new BuildRunnerHelper(project);

		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask(MakeMessages.getString("MakeBuilder.Invoking_Make_Builder") + project.getName(), //$NON-NLS-1$
					TICKS_STREAM_PROGRESS_MONITOR + TICKS_DELETE_MARKERS + TICKS_EXECUTE_COMMAND);

			IPath buildCommand = info.getBuildCommand();
			if (buildCommand != null) {
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(project);

				// Prepare launch parameters for BuildRunnerHelper
				ICommandLauncher launcher = new CommandLauncher();

				String[] targets = getTargets(kind, info);
				if (targets.length != 0 && targets[targets.length - 1].equals(info.getCleanBuildTarget()))
					isClean = true;
				boolean isOnlyClean = isClean && (targets.length == 1);

				String[] args = getCommandArguments(info, targets);

				URI workingDirectoryURI = MakeBuilderUtil.getBuildDirectoryURI(project, info);

				HashMap<String, String> envMap = getEnvironment(launcher, info);
				String[] envp = BuildRunnerHelper.envMapToEnvp(envMap);

				String[] errorParsers = info.getErrorParsers();
				ErrorParserManager epm = new ErrorParserManager(getProject(), workingDirectoryURI, this, errorParsers);

				List<IConsoleParser> parsers = new ArrayList<IConsoleParser>();
				if (!isOnlyClean) {
					ICProjectDescription prjDescription = CoreModel.getDefault().getProjectDescription(project);
					if (prjDescription != null) {
						ICConfigurationDescription cfgDescription = prjDescription.getActiveConfiguration();
						collectLanguageSettingsConsoleParsers(cfgDescription, epm, parsers);
					}
					if (ScannerDiscoveryLegacySupport.isLegacyScannerDiscoveryOn(project)) {
						IScannerInfoConsoleParser parserSD = ScannerInfoConsoleParserFactory.getScannerInfoConsoleParser(project, workingDirectoryURI, this);
						parsers.add(parserSD);
					}
				}

				buildRunnerHelper.setLaunchParameters(launcher, buildCommand, args, workingDirectoryURI, envp);
				buildRunnerHelper.prepareStreams(epm, parsers, console, new SubProgressMonitor(monitor, TICKS_STREAM_PROGRESS_MONITOR, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

				buildRunnerHelper.removeOldMarkers(project, new SubProgressMonitor(monitor, TICKS_DELETE_MARKERS, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

				buildRunnerHelper.greeting(kind);
				int state = buildRunnerHelper.build(new SubProgressMonitor(monitor, TICKS_EXECUTE_COMMAND, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
				buildRunnerHelper.close();
				buildRunnerHelper.goodbye();

				if (state != ICommandLauncher.ILLEGAL_COMMAND) {
					refreshProject(project);
				}
			} else {
				String msg = MakeMessages.getFormattedString("MakeBuilder.message.undefined.build.command", project.getName()); //$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, msg, new Exception()));
			}
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
		return isClean;
	}

	private HashMap<String, String> getEnvironment(ICommandLauncher launcher, IMakeBuilderInfo info)
			throws CoreException {
		HashMap<String, String> envMap = new HashMap<String, String>();
		if (info.appendEnvironment()) {
			@SuppressWarnings({"unchecked", "rawtypes"})
			Map<String, String> env = (Map)launcher.getEnvironment();
			envMap.putAll(env);
		}
		envMap.putAll(info.getExpandedEnvironment());
		return envMap;
	}

	private String[] getCommandArguments(IMakeBuilderInfo info, String[] targets) {
		String[] args = targets;
		if (info.isDefaultBuildCmd()) {
			if (!info.isStopOnError()) {
				args = new String[targets.length + 1];
				args[0] = "-k"; //$NON-NLS-1$
				System.arraycopy(targets, 0, args, 1, targets.length);
			}
		} else {
			String argsStr = info.getBuildArguments();
			if (argsStr != null && !argsStr.equals("")) { //$NON-NLS-1$
				String[] newArgs = makeArray(argsStr);
				args = new String[targets.length + newArgs.length];
				System.arraycopy(newArgs, 0, args, 0, newArgs.length);
				System.arraycopy(targets, 0, args, newArgs.length, targets.length);
			}
		}
		return args;
	}

	/**
	 * Refresh project. Can be overridden to not call actual refresh or to do something else.
	 * Method is called after build is complete.
	 *
	 * @since 6.0
	 */
	protected void refreshProject(IProject project) {
		if (buildRunnerHelper != null) {
			buildRunnerHelper.refreshProject(null, null);
		}
	}


	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}

	protected boolean shouldBuild(int kind, IMakeBuilderInfo info) {
		switch (kind) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				return info.isAutoBuildEnable();
			case IncrementalProjectBuilder.INCREMENTAL_BUILD : // now treated as the same!
			case IncrementalProjectBuilder.FULL_BUILD :
				return info.isFullBuildEnabled() | info.isIncrementalBuildEnabled() ;
			case IncrementalProjectBuilder.CLEAN_BUILD :
				return info.isCleanBuildEnabled();
		}
		return true;
	}

	protected String[] getTargets(int kind, IMakeBuilderInfo info) {
		String targets = ""; //$NON-NLS-1$
		switch (kind) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				targets = info.getAutoBuildTarget();
				break;
			case IncrementalProjectBuilder.INCREMENTAL_BUILD : // now treated as the same!
			case IncrementalProjectBuilder.FULL_BUILD :
				targets = info.getIncrementalBuildTarget();
				break;
			case IncrementalProjectBuilder.CLEAN_BUILD :
				targets = info.getCleanBuildTarget();
				break;
		}
		return makeArray(targets);
	}

	// Turn the string into an array.
	private String[] makeArray(String string) {
		return CommandLineUtil.argumentsToArray(string);
	}
	
	private static void collectLanguageSettingsConsoleParsers(ICConfigurationDescription cfgDescription, IWorkingDirectoryTracker cwdTracker, List<IConsoleParser> parsers) {
		if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
			List<ILanguageSettingsProvider> lsProviders = ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders();
			for (ILanguageSettingsProvider lsProvider : lsProviders) {
				ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(lsProvider);
				if (rawProvider instanceof ICBuildOutputParser) {
					ICBuildOutputParser consoleParser = (ICBuildOutputParser) rawProvider;
					try {
						consoleParser.startup(cfgDescription, cwdTracker);
						parsers.add(consoleParser);
					} catch (CoreException e) {
						MakeCorePlugin.log(new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID,
								"Language Settings Provider failed to start up", e)); //$NON-NLS-1$
					}
				}
			}
		}

	}


}
