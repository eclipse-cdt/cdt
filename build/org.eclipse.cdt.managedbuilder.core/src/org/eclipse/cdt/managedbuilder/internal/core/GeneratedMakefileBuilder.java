package org.eclipse.cdt.managedbuilder.internal.core;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * This is the incremental builder associated with a managed build project. It dynamically 
 * decides the makefile generator it wants to use for a specific target.
 * 
 * @since 1.2 
 */
public class GeneratedMakefileBuilder extends ACBuilder {
	
	/**
	 * @since 1.2
	 */
	public class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		private IManagedBuildInfo buildInfo;
		private boolean buildNeeded = true;
		private List reservedNames;
		
		/**
		 * 
		 */
		public ResourceDeltaVisitor(IManagedBuildInfo info) {
			buildInfo = info;
			reservedNames = Arrays.asList(new String[]{".cdtbuild", ".cdtproject", ".project"});	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		/**
		 * @param changedResource
		 * @return
		 */
		private boolean isGeneratedResource(IResource resource) {
			// Is this a generated directory ...
			IPath path = resource.getProjectRelativePath();
			String[] configNames = buildInfo.getConfigurationNames();
			for (int i = 0; i < configNames.length; i++) {
				String name = configNames[i];
				IPath root = new Path(name);
				// It is if it is a root of the resource pathname
				if (root.isPrefixOf(path)) return true;
			}
			return false;
		}

		/**
		 * @param resource
		 * @return
		 */
		private boolean isProjectFile(IResource resource) {
			return reservedNames.contains(resource.getName()); 
		}

		public boolean shouldBuild() {
			return buildNeeded;
		}
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			// If the project has changed, then a build is needed and we can stop
			if (resource != null && resource.getProject() == getProject()) {
				IResourceDelta[] kids = delta.getAffectedChildren();
				for (int index = kids.length - 1; index >= 0; --index) {
					IResource changedResource = kids[index].getResource();
					String ext = changedResource.getFileExtension();
					// There are some things we don't care about
					if (resource.isDerived()) {
						buildNeeded = false;
					} else if (isGeneratedResource(changedResource)) {
						buildNeeded = false;
					} else if (buildInfo.buildsFileType(ext) || buildInfo.isHeaderFile(ext)) {
						// We do care and there is no point checking anythings else
						buildNeeded = true;
						break;
					} else if (isProjectFile(changedResource)) {
						buildNeeded = false;
					} else {
						buildNeeded = true;
					}
				}
				return false;
			}

			return true;
		}
	}

	// String constants
	private static final String MESSAGE = "ManagedMakeBuilder.message";	//$NON-NLS-1$
	private static final String BUILD_ERROR = MESSAGE + ".error";	//$NON-NLS-1$
	private static final String BUILD_FINISHED = MESSAGE + ".finished";	//$NON-NLS-1$
	private static final String CONSOLE_HEADER = MESSAGE + ".console.header";	//$NON-NLS-1$
	private static final String ERROR_HEADER = "GeneratedmakefileBuilder error [";	//$NON-NLS-1$
	private static final String MAKE = MESSAGE + ".make";	//$NON-NLS-1$
	private static final String MARKERS = MESSAGE + ".creating.markers";	//$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator");	//$NON-NLS-1$
	private static final String NOTHING_BUILT = MESSAGE + ".no.build";	//$NON-NLS-1$
	private static final String REFRESH = MESSAGE + ".updating";	//$NON-NLS-1$
	private static final String REFRESH_ERROR = BUILD_ERROR + ".refresh";	//$NON-NLS-1$
	private static final String TRACE_FOOTER = "]: ";	//$NON-NLS-1$
	private static final String TRACE_HEADER = "GeneratedmakefileBuilder trace [";	//$NON-NLS-1$
	private static final String TYPE_CLEAN = "ManagedMakeBuilder.type.clean";	//$NON-NLS-1$
	private static final String TYPE_FULL = "ManagedMakeBuilder.type.full";	//$NON-NLS-1$
	private static final String TYPE_INC = "ManagedMakeBuider.type.incremental";	//$NON-NLS-1$
	public static boolean VERBOSE = false;
	
	// Local variables
	protected IManagedBuilderMakefileGenerator generator;
	protected IProject[] referencedProjects;
	protected List resourcesToBuild;
	protected List ruleList;

	public static void outputTrace(String resourceName, String message) {
		if (VERBOSE) {
			System.out.println(TRACE_HEADER + resourceName + TRACE_FOOTER + message + NEWLINE);
		}
	}

	public static void outputError(String resourceName, String message) {
		if (VERBOSE) {
			System.err.println(ERROR_HEADER + resourceName + TRACE_FOOTER + message + NEWLINE);
		}
	}

	/**
	 * Zero-argument constructor needed to fulfill the contract of an
	 * incremental builder.
	 */
	public GeneratedMakefileBuilder() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		// We should always tell the build system what projects we reference
		referencedProjects = getProject().getReferencedProjects();
		checkCancel(monitor);
		
		// Get the build information
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		if (info == null) {
			outputError(getProject().getName(), "Build information was not found");	//$NON-NLS-1$
			return referencedProjects;
		}

		// So let's figure out why we got called
		if (kind == CLEAN_BUILD) {
			outputTrace(getProject().getName(), "Clean build requested");	//$NON-NLS-1$
			cleanBuild(monitor, info);
		}
		else if (kind == FULL_BUILD || info.needsRebuild()) {
			outputTrace(getProject().getName(), "Full build needed/requested");	//$NON-NLS-1$
			fullBuild(monitor, info);
		}
		else if (kind == AUTO_BUILD && info.needsRebuild()) {
			outputTrace(getProject().getName(), "Autobuild requested, full build needed");	//$NON-NLS-1$
			fullBuild(monitor, info);
		}
		else {
			// Create a delta visitor to make sure we should be rebuilding
			ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(info);
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				outputTrace(getProject().getName(), "Incremental build requested, full build needed");	//$NON-NLS-1$
				fullBuild(monitor, info);
			}
			else {
				delta.accept(visitor);
				if (visitor.shouldBuild()) {
					outputTrace(getProject().getName(), "Incremental build requested");	//$NON-NLS-1$
					incrementalBuild(delta, info, monitor);
				}
			}
		}
		
		// Scrub the build info the project
		info.setRebuildState(false);
		
		// Ask build mechanism to compute deltas for project dependencies next time
		return referencedProjects;
	}


	/**
	 * Check whether the build has been canceled. Cancellation requests 
	 * propagated to the caller by throwing <code>OperationCanceledException</code>.
	 * 
	 * @see org.eclipse.core.runtime.OperationCanceledException#OperationCanceledException()
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	/**
	 * @param monitor
	 * @param info
	 */
	protected void cleanBuild(IProgressMonitor monitor, IManagedBuildInfo info) {
		// Make sure that there is a top level directory and a set of makefiles
		String targetID = info.getDefaultTarget().getParent().getId();
		generator = ManagedBuildManager.getMakefileGenerator(targetID);
		IPath buildDir = new Path(info.getConfigurationName());
		IPath makefilePath = buildDir.append(generator.getMakefileName());		
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		IFile makefile = root.getFileForLocation(makefilePath);
		
		if (buildDir != null && makefile != null && makefile.exists()) {		
			// invoke make with the clean argument
			String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.starting", getProject().getName());	//$NON-NLS-1$
			monitor.subTask(statusMsg);
			checkCancel(monitor);
			invokeMake(CLEAN_BUILD, buildDir, info, monitor);
		}
	}

	/**
	 * @param monitor
	 */
	protected void fullBuild(IProgressMonitor monitor, IManagedBuildInfo info) throws CoreException {
		// Always need one of these bad boys
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		// Regenerate the makefiles for this project
		String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.rebuild.makefiles", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		checkCancel(monitor);
		String targetID = info.getDefaultTarget().getParent().getId();
		generator = ManagedBuildManager.getMakefileGenerator(targetID);
		generator.initialize(getProject(), info, monitor);
		try {
			generator.regenerateMakefiles();
		} catch (CoreException e) {
			// Throw the exception back to the builder
			throw e;
		}
		monitor.worked(1);

		// Now call make
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.starting", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		checkCancel(monitor);
		IPath topBuildDir = generator.getBuildWorkingDir();
		if (topBuildDir != null) {
			invokeMake(FULL_BUILD, topBuildDir, info, monitor);
		} else {
			statusMsg = ManagedMakeMessages.getFormattedString(NOTHING_BUILT, getProject().getName());	//$NON-NLS-1$
			monitor.subTask(statusMsg);
			return;
		}
		monitor.worked(1);
		
		// Now regenerate the dependencies
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.regen.deps", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		checkCancel(monitor);
		try {
			generator.regenerateDependencies(false);
		} catch (CoreException e) {
			// Throw the exception back to the builder
			throw e;
		}
		monitor.worked(1);

		// Say bye bye
		statusMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
	}

	/**
	 * @param toolId
	 * @return
	 */
	public IManagedDependencyGenerator getDependencyCalculator(String toolId) {
		try {
			IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(ManagedBuilderCorePlugin.getUniqueIdentifier(), ManagedBuilderCorePlugin.DEP_CALC_ID);
			if (extension != null) {
				// There could be many of these
				IExtension[] extensions = extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						IConfigurationElement element = configElements[j];
						if (element.getName().equals(ITool.TOOL_ELEMENT_NAME)) { 
							if (element.getAttribute(ITool.ID).equals(toolId)) {
								if (element.getAttribute(ManagedBuilderCorePlugin.DEP_CALC_ID) != null) {
									return (IManagedDependencyGenerator) element.createExecutableExtension(ManagedBuilderCorePlugin.DEP_CALC_ID);
								}
							}
						}
					}
				}
			}
		} 
		catch (CoreException e) {
			// Probably not defined
		}
		return null;
	}
	/* (non-javadoc)
	 * Answers an array of strings with the proper make targets
	 * 
	 * @param fullBuild
	 * @return
	 */
	protected String[] getMakeTargets(int buildType) {
		List args = new ArrayList();
		switch (buildType) {
			case CLEAN_BUILD:
				args.add("clean");	//$NON-NLS-1$
				break;
			case FULL_BUILD:
				args.add("clean");	//$NON-NLS-1$
			case INCREMENTAL_BUILD:
				args.add("all");	//$NON-NLS-1$
				break;
		}
		return (String[])args.toArray(new String[args.size()]);
	}
	
	/**
	 * @return
	 */
	protected List getResourcesToBuild() {
		if (resourcesToBuild == null) {
			resourcesToBuild = new ArrayList();
		}
		return resourcesToBuild;
	}

	/* (non-javadoc)
	 * Answers the list of build rules that have been assembled. If there are none, 
	 * answers an empty list, never <code>null</code>
	 * 
	 * @return
	 */
	protected List getRuleList() {
		if (ruleList == null) {
			ruleList = new ArrayList();
		}
		return ruleList;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.ACBuilder#getWorkingDirectory()
	 */
	public IPath getWorkingDirectory() {
		return getProject().getLocation();
	}

	/* (non-Javadoc)
	 * @param delta
	 * @param info
	 * @param monitor
	 * @throws CoreException
	 */
	protected void incrementalBuild(IResourceDelta delta, IManagedBuildInfo info, IProgressMonitor monitor) throws CoreException {
		// Need to report status to the user
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		// Ask the makefile generator to generate any makefiles needed to build delta
		String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.update.makefiles", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		checkCancel(monitor);
		String targetID = info.getDefaultTarget().getParent().getId();
		generator = ManagedBuildManager.getMakefileGenerator(targetID);
		generator.initialize(getProject(), info, monitor);
		try {
			generator.generateMakefiles(delta);
		} catch (CoreException e) {
			// Throw the exception back to the builder
			ManagedBuilderCorePlugin.log(e);
			throw e;
		}
		monitor.worked(1);

		// Run the build
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.starting", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		checkCancel(monitor);
		IPath buildDir = new Path(info.getConfigurationName());
		if (buildDir != null) {
			invokeMake(INCREMENTAL_BUILD, buildDir, info, monitor);
		} else {
			statusMsg = ManagedMakeMessages.getFormattedString(NOTHING_BUILT, getProject().getName());	//$NON-NLS-1$
			monitor.subTask(statusMsg);
			return;
		}
		monitor.worked(1);
		
		// Generate the dependencies for all changes
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.updating.deps", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		checkCancel(monitor);
		try {
			generator.generateDependencies();
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
			throw e;
		}
		monitor.worked(1);

		// Say bye bye
		statusMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
}

	/* (non-Javadoc)
	 * @param fullBuild
	 * @param buildDir
	 * @param info
	 * @param monitor
	 */
	protected void invokeMake(int buildType, IPath buildDir, IManagedBuildInfo info, IProgressMonitor monitor) {
		// Get the project and make sure there's a monitor to cancel the build
		IProject currentProject = getProject();
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			// Figure out the working directory for the build and make sure there is a makefile there 
			IPath workingDirectory = getWorkingDirectory().append(buildDir);
			IWorkspace workspace = currentProject.getWorkspace();
			if (workspace == null) {
				return;
			}
			IWorkspaceRoot root = workspace.getRoot();
			if (root == null) {
				return;
			}
			IPath makefile = workingDirectory.addTrailingSeparator().append(generator.getMakefileName());
			if (root.getFileForLocation(makefile) == null) {
				return; 
			}

			// Flag to the user that make is about to be called
			IPath makeCommand = new Path(info.getMakeCommand()); 
			if (makeCommand != null) {
				String[] msgs = new String[2];
				msgs[0] = makeCommand.toString();
				msgs[1] = currentProject.getName();
				monitor.beginTask(ManagedMakeMessages.getFormattedString(MAKE, msgs), IProgressMonitor.UNKNOWN);

				// Get a build console for the project
				StringBuffer buf = new StringBuffer();
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(currentProject);
				ConsoleOutputStream consoleOutStream = console.getOutputStream();
				String[] consoleHeader = new String[3];
				switch (buildType) {
					case FULL_BUILD:
						consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_FULL);
						break;
					case INCREMENTAL_BUILD:
						consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
						break;
					case CLEAN_BUILD:
						consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_CLEAN);
						break;
				}
						
				consoleHeader[1] = info.getConfigurationName();
				consoleHeader[2] = currentProject.getName();
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader));
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
				
				// Remove all markers for this project
				removeAllMarkers(currentProject);
				for (int i = 0; i < referencedProjects.length; i++) {
					IProject project = referencedProjects[i];
					removeAllMarkers(project);
				} 

				// Get the arguments to be passed to make from build model
				ArrayList makeArgs = new ArrayList();
				String arg = info.getMakeArguments();
				if (arg.length() > 0) {
					String[] args = arg.split("\\s"); //$NON-NLS-1$
					for (int i = 0; i < args.length; ++i) {
						makeArgs.add(args[i]);
					}
				}
				makeArgs.addAll(Arrays.asList(getMakeTargets(buildType)));
				String[] makeTargets = (String[]) makeArgs.toArray(new String[makeArgs.size()]);
			
				// Get a launcher for the make command
				String errMsg = null;
				CommandLauncher launcher = new CommandLauncher();
				launcher.showCommand(true);
	
				// Set the environmennt, some scripts may need the CWD var to be set.
				Properties props = launcher.getEnvironment();
				props.put("CWD", workingDirectory.toOSString());	//$NON-NLS-1$
				props.put("PWD", workingDirectory.toOSString());	//$NON-NLS-1$
				String[] env = null;
				ArrayList envList = new ArrayList();
				Enumeration names = props.propertyNames();
				if (names != null) {
					while (names.hasMoreElements()) {
						String key = (String) names.nextElement();
						envList.add(key + "=" + props.getProperty(key)); //$NON-NLS-1$
					}
					env = (String[]) envList.toArray(new String[envList.size()]);
				}
			
				// Hook up an error parser manager
				String[] errorParsers = info.getDefaultTarget().getErrorParserList(); 
				ErrorParserManager epm = new ErrorParserManager(getProject(), workingDirectory, this, errorParsers);
				epm.setOutputStream(consoleOutStream);
				OutputStream stdout = epm.getOutputStream();
				OutputStream stderr = epm.getOutputStream();
			
				// Launch make
				Process proc = launcher.execute(makeCommand, makeTargets, env, workingDirectory);
				if (proc != null) {
					try {
						// Close the input of the process since we will never write to it
						proc.getOutputStream().close();
					} catch (IOException e) {
					}
				
					if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) { 
						errMsg = launcher.getErrorMessage();
					}
				
					// Force a resync of the projects without allowing the user to cancel.
					// This is probably unkind, but short of this there is no way to insure
					// the UI is up-to-date with the build results
					monitor.subTask(ManagedMakeMessages.getResourceString(REFRESH));
					try {
						currentProject.refreshLocal(IResource.DEPTH_INFINITE, null);
						for (int j = 0; j < referencedProjects.length; ++j) {
							IProject project = referencedProjects[j];
							project.refreshLocal(IResource.DEPTH_INFINITE, null);
						}
					} catch (CoreException e) {
						monitor.subTask(ManagedMakeMessages.getResourceString(REFRESH_ERROR));
					}
				} else {
					errMsg = launcher.getErrorMessage();
				}
				
				// Report either the success or failure of our mission
				buf = new StringBuffer();
				if (errMsg != null && errMsg.length() > 0) {
					String errorDesc = ManagedMakeMessages.getResourceString(BUILD_ERROR);
					buf.append(errorDesc);
					buf.append(System.getProperty("line.separator", "\n"));  //$NON-NLS-1$//$NON-NLS-2$
					buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					// Report a successful build
					String successMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, currentProject.getName());
					buf.append(successMsg);
					buf.append(System.getProperty("line.separator", "\n"));  //$NON-NLS-1$//$NON-NLS-2$
				}

				// Write message on the console
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
				stdout.close();
				stderr.close();				

				monitor.subTask(ManagedMakeMessages.getResourceString(MARKERS));
				epm.reportProblems();
			}
		} catch (Exception e) {
			ManagedBuilderCorePlugin.log(e);
			forgetLastBuiltState();
		} finally {
			monitor.done();
		}
	}

	private void removeAllMarkers(IProject project) {
		if (project == null || !project.exists()) return;

		// Clear out the problem markers
		IWorkspace workspace = project.getWorkspace();
		IMarker[] markers;
		try {
			markers = project.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
			return;
		}
		if (markers != null) {
			try {
				workspace.deleteMarkers(markers);
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
	}
}
