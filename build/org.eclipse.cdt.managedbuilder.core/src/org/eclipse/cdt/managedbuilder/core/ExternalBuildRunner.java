/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.newmake.internal.core.StreamMonitor;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author dschaefer
 * @since 8.0
 */
public class ExternalBuildRunner implements IBuildRunner {

	private static final String TYPE_CLEAN = "ManagedMakeBuilder.type.clean";	//$NON-NLS-1$
	private static final String TYPE_INC = "ManagedMakeBuider.type.incremental";	//$NON-NLS-1$
	private static final String CONSOLE_HEADER = "ManagedMakeBuilder.message.console.header";	//$NON-NLS-1$
	private static final String WARNING_UNSUPPORTED_CONFIGURATION = "ManagedMakeBuilder.warning.unsupported.configuration";	//$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String PATH = "PATH"; //$NON-NLS-1$

	public boolean invokeBuild(int kind, IProject project, IConfiguration configuration,
			IBuilder builder, IConsole console, IMarkerGenerator markerGenerator,
			IncrementalProjectBuilder projectBuilder, IProgressMonitor monitor) throws CoreException {
		return invokeExternalBuild(kind, project, configuration, builder, console,
				markerGenerator, projectBuilder, monitor);
	}

	protected boolean invokeExternalBuild(int kind, IProject project, IConfiguration configuration,
			IBuilder builder, IConsole console, IMarkerGenerator markerGenerator,
			IncrementalProjectBuilder projectBuilder, IProgressMonitor monitor) throws CoreException {
		boolean isClean = false;

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(ManagedMakeMessages.getResourceString("MakeBuilder.Invoking_Make_Builder") + project.getName(), 100); //$NON-NLS-1$

		try {
			IPath buildCommand = builder.getBuildCommand();
			if (buildCommand != null) {
				OutputStream cos = console.getOutputStream();
				StringBuffer buf = new StringBuffer();

				String[] consoleHeader = new String[3];
				switch (kind) {
					case IncrementalProjectBuilder.FULL_BUILD:
					case IncrementalProjectBuilder.INCREMENTAL_BUILD:
					case IncrementalProjectBuilder.AUTO_BUILD:
						consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
						break;
					case IncrementalProjectBuilder.CLEAN_BUILD:
						consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_CLEAN);
						break;
				}

				consoleHeader[1] = configuration.getName();
				consoleHeader[2] = project.getName();
				buf.append(NEWLINE);
				buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader)).append(NEWLINE);
				buf.append(NEWLINE);

				if(!configuration.isSupported()){
					String unsupportedToolchainMsg = ManagedMakeMessages.getFormattedString(WARNING_UNSUPPORTED_CONFIGURATION,
							new String[] { configuration.getName(), configuration.getToolChain().getName() });
					buf.append(unsupportedToolchainMsg).append(NEWLINE);
					buf.append(NEWLINE);
				}
				cos.write(buf.toString().getBytes());
				cos.flush();

				// remove all markers for this project
				IWorkspace workspace = project.getWorkspace();
				IMarker[] markers = project.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
				if (markers != null)
					workspace.deleteMarkers(markers);

				IPath workingDirectory = ManagedBuildManager.getBuildLocation(configuration, builder);
				URI workingDirectoryURI = ManagedBuildManager.getBuildLocationURI(configuration, builder);

				String[] targets = getTargets(kind, builder);
				if (targets.length != 0 && targets[targets.length - 1].equals(builder.getCleanBuildTarget()))
					isClean = true;

				String errMsg = null;
				ICommandLauncher launcher = builder.getCommandLauncher();
				launcher.setProject(project);
				// Print the command for visual interaction.
				launcher.showCommand(true);

				// Set the environment
				Map<String, String> envMap = getEnvironment(builder);
				String[] env = getEnvStrings(envMap);
				String[] buildArguments = targets;

				String[] newArgs = CommandLineUtil.argumentsToArray(builder.getBuildArguments());
				buildArguments = new String[targets.length + newArgs.length];
				System.arraycopy(newArgs, 0, buildArguments, 0, newArgs.length);
				System.arraycopy(targets, 0, buildArguments, newArgs.length, targets.length);

				QualifiedName qName = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "progressMonitor"); //$NON-NLS-1$
				Integer last = (Integer)project.getSessionProperty(qName);
				if (last == null) {
					last = new Integer(100);
				}
				ErrorParserManager epm = new ErrorParserManager(project, workingDirectoryURI, markerGenerator, builder.getErrorParsers());
				epm.setOutputStream(cos);
				StreamMonitor streamMon = new StreamMonitor(new SubProgressMonitor(monitor, 100), epm, last.intValue());
				OutputStream stdout = streamMon;
				OutputStream stderr = streamMon;

				// Sniff console output for scanner info
				ConsoleOutputSniffer sniffer = createBuildOutputSniffer(stdout, stderr, project, configuration, workingDirectory, markerGenerator, null);
				OutputStream consoleOut = (sniffer == null ? stdout : sniffer.getOutputStream());
				OutputStream consoleErr = (sniffer == null ? stderr : sniffer.getErrorStream());
				Process p = launcher.execute(buildCommand, buildArguments, env, workingDirectory, monitor);
				if (p != null) {
					try {
						// Close the input of the Process explicitly.
						// We will never write to it.
						p.getOutputStream().close();
					} catch (IOException e) {
					}
					// Before launching give visual cues via the monitor
					monitor.subTask(ManagedMakeMessages.getResourceString("MakeBuilder.Invoking_Command") + launcher.getCommandLine()); //$NON-NLS-1$
					if (launcher.waitAndRead(consoleOut, consoleErr, new SubProgressMonitor(monitor, 0))
						!= ICommandLauncher.OK)
						errMsg = launcher.getErrorMessage();
					monitor.subTask(ManagedMakeMessages.getResourceString("MakeBuilder.Updating_project")); //$NON-NLS-1$

					try {
						// Do not allow the cancel of the refresh, since the builder is external
						// to Eclipse, files may have been created/modified and we will be out-of-sync.
						// The caveat is for huge projects, it may take sometimes at every build.
						
						// TODO should only refresh output folders
						project.refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (CoreException e) {
					}
				} else {
					buf = new StringBuffer(launcher.getCommandLine()).append(NEWLINE);
					errMsg = launcher.getErrorMessage();
				}
				project.setSessionProperty(qName, !monitor.isCanceled() && !isClean ? new Integer(streamMon.getWorkDone()) : null);

				if (errMsg != null) {
					// Launching failed, trying to figure out possible cause
					String errorPrefix = ManagedMakeMessages.getResourceString("ManagedMakeBuilder.error.prefix"); //$NON-NLS-1$
					String buildCommandStr = buildCommand.toString();
					String envPath = envMap.get(PATH);
					if (envPath==null) {
						envPath = System.getenv(PATH);
					}
					if (PathUtil.findProgramLocation(buildCommandStr, envPath)==null) {
						buf.append(errMsg).append(NEWLINE);
						errMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.program.not.in.path", buildCommandStr); //$NON-NLS-1$
						buf.append(errorPrefix).append(errMsg).append(NEWLINE);
						buf.append(NEWLINE);
						buf.append(PATH+"=["+envPath+"]").append(NEWLINE); //$NON-NLS-1$//$NON-NLS-2$
					} else {
						buf.append(errorPrefix).append(errMsg).append(NEWLINE);
					}
					consoleErr.write(buf.toString().getBytes());
					consoleErr.flush();
				}
				
				buf = new StringBuffer(NEWLINE);
				buf.append(ManagedMakeMessages.getResourceString("ManagedMakeBuilder.message.build.finished")).append(NEWLINE); //$NON-NLS-1$
				consoleOut.write(buf.toString().getBytes());

				stdout.close();
				stderr.close();

				monitor.subTask(ManagedMakeMessages.getResourceString("MakeBuilder.Creating_Markers")); //$NON-NLS-1$
				consoleOut.close();
				consoleErr.close();
				cos.close();
			}
		} catch (Exception e) {
			ManagedBuilderCorePlugin.log(e);
			throw new CoreException(new Status(IStatus.ERROR,
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					e.getLocalizedMessage(),
					e));
		} finally {
			monitor.done();
		}
		return (isClean);
	}

	protected String[] getTargets(int kind, IBuilder builder) {
		String targetsArray[] = null;

		if(kind != IncrementalProjectBuilder.CLEAN_BUILD && !builder.isCustomBuilder() && builder.isManagedBuildOn()){
			IConfiguration cfg = builder.getParent().getParent();
			String preBuildStep = cfg.getPrebuildStep();
			try {
				preBuildStep = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						preBuildStep,
						"", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION,
						cfg);
			} catch (BuildMacroException e) {
			}

			if(preBuildStep != null && preBuildStep.length() != 0){
				targetsArray = new String[]{"pre-build", "main-build"}; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		if(targetsArray == null){
			String targets = ""; //$NON-NLS-1$
			switch (kind) {
				case IncrementalProjectBuilder.AUTO_BUILD :
					targets = builder.getAutoBuildTarget();
					break;
				case IncrementalProjectBuilder.INCREMENTAL_BUILD : // now treated as the same!
				case IncrementalProjectBuilder.FULL_BUILD :
					targets = builder.getIncrementalBuildTarget();
					break;
				case IncrementalProjectBuilder.CLEAN_BUILD :
					targets = builder.getCleanBuildTarget();
					break;
			}

			targetsArray = CommandLineUtil.argumentsToArray(targets);
		}

		return targetsArray;
	}

	protected Map<String, String> getEnvironment(IBuilder builder) throws CoreException {
		Map<String, String> envMap = new HashMap<String, String>();
		if (builder.appendEnvironment()) {
			ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(builder.getParent().getParent());
			IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IEnvironmentVariable[] vars = mngr.getVariables(cfgDes, true);
			for (IEnvironmentVariable var : vars) {
				envMap.put(var.getName(), var.getValue());
			}
		}
		
		// Add variables from build info
		Map<String, String> builderEnv = builder.getExpandedEnvironment();
		if (builderEnv != null)
			envMap.putAll(builderEnv);
		
		return envMap;
	}
	
	protected static String[] getEnvStrings(Map<String, String> env) {
		// Convert into env strings
		List<String> strings= new ArrayList<String>(env.size());
		for (Entry<String, String> entry : env.entrySet()) {
			StringBuffer buffer= new StringBuffer(entry.getKey());
			buffer.append('=').append(entry.getValue());
			strings.add(buffer.toString());
		}
		
		return strings.toArray(new String[strings.size()]);
	}
	
	private ConsoleOutputSniffer createBuildOutputSniffer(OutputStream outputStream,
			OutputStream errorStream,
			IProject project,
			IConfiguration cfg,
			IPath workingDirectory,
			IMarkerGenerator markerGenerator,
			IScannerInfoCollector collector){
		ICfgScannerConfigBuilderInfo2Set container = CfgScannerConfigProfileManager.getCfgScannerConfigBuildInfo(cfg);
		Map<CfgInfoContext, IScannerConfigBuilderInfo2> map = container.getInfoMap();
		List<IScannerInfoConsoleParser> clParserList = new ArrayList<IScannerInfoConsoleParser>();

		if(container.isPerRcTypeDiscovery()){
			for (IResourceInfo rcInfo : cfg.getResourceInfos()) {
				ITool tools[];
				if(rcInfo instanceof IFileInfo){
					tools = ((IFileInfo)rcInfo).getToolsToInvoke();
				} else {
					tools = ((IFolderInfo)rcInfo).getFilteredTools();
				}
				for (ITool tool : tools) {
					IInputType[] types = tool.getInputTypes();

					if(types.length != 0){
						for (IInputType type : types) {
							CfgInfoContext c = new CfgInfoContext(rcInfo, tool, type);
							contributeToConsoleParserList(project, map, c, workingDirectory, markerGenerator, collector, clParserList);
						}
					} else {
						CfgInfoContext c = new CfgInfoContext(rcInfo, tool, null);
						contributeToConsoleParserList(project, map, c, workingDirectory, markerGenerator, collector, clParserList);
					}
				}
			}
		}

		if(clParserList.size() == 0){
			contributeToConsoleParserList(project, map, new CfgInfoContext(cfg), workingDirectory, markerGenerator, collector, clParserList);
		}

		if(clParserList.size() != 0){
			return new ConsoleOutputSniffer(outputStream, errorStream,
					clParserList.toArray(new IScannerInfoConsoleParser[clParserList.size()]));
		}

		return null;
	}

	private boolean contributeToConsoleParserList(
			IProject project,
			Map<CfgInfoContext, IScannerConfigBuilderInfo2> map,
			CfgInfoContext context,
			IPath workingDirectory,
			IMarkerGenerator markerGenerator,
			IScannerInfoCollector collector,
			List<IScannerInfoConsoleParser> parserList){
		IScannerConfigBuilderInfo2 info = map.get(context);
		InfoContext ic = context.toInfoContext();
		boolean added = false;
		if (info != null &&
				info.isAutoDiscoveryEnabled() &&
				info.isBuildOutputParserEnabled()) {

			String id = info.getSelectedProfileId();
			ScannerConfigProfile profile = ScannerConfigProfileManager.getInstance().getSCProfileConfiguration(id);
			if(profile.getBuildOutputProviderElement() != null){
				// get the make builder console parser
				SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
						getSCProfileInstance(project, ic, id);

				IScannerInfoConsoleParser clParser = profileInstance.createBuildOutputParser();
                if (collector == null) {
                    collector = profileInstance.getScannerInfoCollector();
                }
                if(clParser != null){
					clParser.startup(project, workingDirectory, collector,
                            info.isProblemReportingEnabled() ? markerGenerator : null);
					parserList.add(clParser);
					added = true;
                }

			}
		}

		return added;
	}

}
