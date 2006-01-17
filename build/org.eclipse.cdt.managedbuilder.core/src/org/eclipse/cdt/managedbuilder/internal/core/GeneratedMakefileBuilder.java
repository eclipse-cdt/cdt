/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.managedbuilder.core.IConfiguration; 
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
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
		private String buildGoalName;
		private IManagedBuildInfo buildInfo;
		private boolean incrBuildNeeded = false;
		private boolean fullBuildNeeded = false;
		private List reservedNames;
		
		/**
		 * 
		 */
		public ResourceDeltaVisitor(IManagedBuildInfo info) {
			buildInfo = info;
			String ext = buildInfo.getBuildArtifactExtension();
			//try to resolve build macros in the build artifact extension
			try{
				ext = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						ext,
						"", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION,
						info.getDefaultConfiguration());
			} catch (BuildMacroException e){
			}
			
			String name = buildInfo.getBuildArtifactName();
			//try to resolve build macros in the build artifact name
			try{
				String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						name,
						"", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION,
						info.getDefaultConfiguration());
				if((resolved = resolved.trim()).length() > 0)
					name = resolved;
			} catch (BuildMacroException e){
			}

			if (ext.length() > 0) {
				buildGoalName = buildInfo.getOutputPrefix(ext) + name + IManagedBuilderMakefileGenerator.DOT + ext;
			} else {
				buildGoalName = name;
			}
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

		public boolean shouldBuildIncr() {
			return incrBuildNeeded;
		}
		
		public boolean shouldBuildFull() {
			return fullBuildNeeded;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			// If the project has changed, then a build is needed and we can stop
			if (resource != null && resource.getProject() == getProject()) {
				IResourceDelta[] kids = delta.getAffectedChildren();
				for (int index = kids.length - 1; index >= 0; --index) {
					IResource changedResource = kids[index].getResource();
					if (changedResource instanceof IFolder) {
						return true;
					} else {
						String name = changedResource.getName();						
						if ((!name.equals(buildGoalName) &&
							// TODO:  Also need to check for secondary outputs
							(changedResource.isDerived() || 
							(isProjectFile(changedResource)) ||
							(isGeneratedResource(changedResource))))) {
						     // The resource that changed has attributes which make it uninteresting, 
							 // so don't do anything
						    ;							
					    } 
						else {
							//  TODO:  Should we do extra checks here to determine if a build is really needed,
							//         or do you just do exclusion checks like above?
							//         We could check for:
							//         o  The build goal name
							//         o  A secondary output
							//         o  An input file to a tool:
							//            o  Has an extension of a source file used by a tool
							//            o  Has an extension of a header file used by a tool
							//            o  Has the name of an input file specified in an InputType via:
							//               o  An Option
							//               o  An AdditionalInput
							//
							//if (resourceName.equals(buildGoalName) || 
							//	(buildInfo.buildsFileType(ext) || buildInfo.isHeaderFile(ext))) {
							
								// We need to do an incremental build, at least
								incrBuildNeeded = true;
								if (kids[index].getKind() == IResourceDelta.REMOVED) {
									// If a meaningful resource was removed, then force a full build
									// This is required because an incremental build will trigger make to
									// do nothing for a missing source, since the state after the file 
									// removal is uptodate, as far as make is concerned
									// A full build will clean, and ultimately trigger a relink without
									// the object generated from the deleted source, which is what we want
									fullBuildNeeded = true;
									// There is no point in checking anything else since we have
									// decided to do a full build anyway
									break;
								}
								
							//}
						} 
					}
				}
				return false;
			}
			return true;
		}
	}

	// String constants
	private static final String BUILD_ERROR = "ManagedMakeBuilder.message.error";	//$NON-NLS-1$
	private static final String BUILD_FINISHED = "ManagedMakeBuilder.message.finished";	//$NON-NLS-1$
	private static final String CONSOLE_HEADER = "ManagedMakeBuilder.message.console.header";	//$NON-NLS-1$
	private static final String ERROR_HEADER = "GeneratedmakefileBuilder error [";	//$NON-NLS-1$
	private static final String MAKE = "ManagedMakeBuilder.message.make";	//$NON-NLS-1$
	private static final String MARKERS = "ManagedMakeBuilder.message.creating.markers";	//$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator");	//$NON-NLS-1$
	private static final String NOTHING_BUILT = "ManagedMakeBuilder.message.no.build";	//$NON-NLS-1$
	private static final String REFRESH = "ManagedMakeBuilder.message.updating";	//$NON-NLS-1$
	private static final String REFRESH_ERROR = BUILD_ERROR + ".refresh";	//$NON-NLS-1$
	private static final String TRACE_FOOTER = "]: ";	//$NON-NLS-1$
	private static final String TRACE_HEADER = "GeneratedmakefileBuilder trace [";	//$NON-NLS-1$
	private static final String TYPE_CLEAN = "ManagedMakeBuilder.type.clean";	//$NON-NLS-1$
	private static final String TYPE_FULL = "ManagedMakeBuilder.type.full";	//$NON-NLS-1$
	private static final String TYPE_INC = "ManagedMakeBuider.type.incremental";	//$NON-NLS-1$
	private static final String WARNING_UNSUPPORTED_CONFIGURATION = "ManagedMakeBuilder.warning.unsupported.configuration";	//$NON-NLS-1$
	public static boolean VERBOSE = false;
	
	// Local variables
	protected Vector generationProblems;
	protected IProject[] referencedProjects;
	protected List resourcesToBuild;
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

	/**
	 * @param epm
	 */
	private void addBuilderMarkers(ErrorParserManager epm) {
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		Iterator iter = getGenerationProblems().iterator();
		while (iter.hasNext()) {
			IStatus stat = (IStatus)iter.next();
			IResource location = root.findMember(stat.getMessage());
			if (stat.getCode() == IManagedBuilderMakefileGenerator.SPACES_IN_PATH) {
				epm.generateMarker(location, -1, ManagedMakeMessages.getResourceString("MakefileGenerator.error.spaces"), IMarkerGenerator.SEVERITY_WARNING, null);	//$NON-NLS-1$
			}
		}
	}

	/* (non-javadoc)
	 * Emits a message to the console indicating that there were no source files to build
	 * @param buildType
	 * @param status
	 * @param configName
	 */
	private void emitNoSourceMessage(int buildType, IStatus status, String configName) throws CoreException {
		try {
			StringBuffer buf = new StringBuffer();
			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(getProject());
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			// Report a successful clean
			String[] consoleHeader = new String[3];
			if (buildType == FULL_BUILD) {
			    consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_FULL);
			} else if (buildType == INCREMENTAL_BUILD) {
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
			} else {
				consoleHeader[0] = new String();
				outputError(getProject().getName(), "The given build type is not supported in this context");	//$NON-NLS-1$
			}			
			consoleHeader[1] = configName;
			consoleHeader[2] = getProject().getName();
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader));
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(status.getMessage());
			buf.append(System.getProperty("line.separator", "\n"));  //$NON-NLS-1$//$NON-NLS-2$
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			consoleOutStream.close();
		} catch (CoreException e) {
			// Throw the exception back to the builder
			throw e;
		} catch (IOException io) {	//  Ignore console failures...
		}		
	}

	/**
	 * 
	 * This method has been created so that subclasses can override how the builder obtains its
	 * build info.  The default implementation retrieves the info from the build manager.
	 * 
	 * @return An IManagedBuildInfo object representing the build info.
	 */
	protected IManagedBuildInfo getBuildInfo() {
		return ManagedBuildManager.getBuildInfo(getProject());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		// We should always tell the build system what projects we reference
		referencedProjects = getProject().getReferencedProjects();

		// Get the build information
		IManagedBuildInfo info = getBuildInfo();
		if (info == null) {
			outputError(getProject().getName(), "Build information was not found");	//$NON-NLS-1$
			return referencedProjects;
		}
		if (!info.isValid()) {
			outputError(getProject().getName(), "Build information is not valid");	//$NON-NLS-1$
			return referencedProjects;
		}
		
		// Create a makefile generator for the build
		IManagedBuilderMakefileGenerator generator = ManagedBuildManager.getBuildfileGenerator(info.getDefaultConfiguration());
		generator.initialize(getProject(), info, monitor);

		// So let's figure out why we got called
		if (kind == FULL_BUILD || info.needsRebuild()) {
			outputTrace(getProject().getName(), "Full build needed/requested");	//$NON-NLS-1$
			fullBuild(info, generator, monitor);
		}
		else if (kind == AUTO_BUILD && info.needsRebuild()) {
			outputTrace(getProject().getName(), "Autobuild requested, full build needed");	//$NON-NLS-1$
			fullBuild(info, generator, monitor);
		}
		else {
			// Create a delta visitor to make sure we should be rebuilding
			ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(info);
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				outputTrace(getProject().getName(), "Incremental build requested, full build needed");	//$NON-NLS-1$
				fullBuild(info, generator, monitor);
			}
			else {
				delta.accept(visitor);
				if (visitor.shouldBuildFull()) {
					outputTrace(getProject().getName(), "Incremental build requested, full build needed");	//$NON-NLS-1$
					fullBuild(info, generator, monitor);
				} else if (visitor.shouldBuildIncr()) {
					outputTrace(getProject().getName(), "Incremental build requested");	//$NON-NLS-1$
					incrementalBuild(delta, info, generator, monitor);
				}
				else if (referencedProjects != null) {
					//  Also check to see is any of the dependent projects changed
					for (int i=0; i<referencedProjects.length; i++) {
						IProject ref = referencedProjects[i];
						IResourceDelta refDelta = getDelta(ref);
						if (refDelta == null) {
							outputTrace(getProject().getName(), "Incremental build because of changed referenced project");	//$NON-NLS-1$
							incrementalBuild(delta, info, generator, monitor);
							//  Should only build this project once, for this delta
							break;
						} else {
							int refKind = refDelta.getKind(); 
							if (refKind != IResourceDelta.NO_CHANGE) {
								int refFlags = refDelta.getFlags();
								if (!(refKind == IResourceDelta.CHANGED &&
									  refFlags == IResourceDelta.OPEN)) {
									outputTrace(getProject().getName(), "Incremental build because of changed referenced project");	//$NON-NLS-1$
									incrementalBuild(delta, info, generator, monitor);
									//  Should only build this project once, for this delta
									break;
								}
							}
						}						
					}
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
			outputTrace(getProject().getName(), "Build cancelled");	//$NON-NLS-1$
			forgetLastBuiltState();
			throw new OperationCanceledException();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void clean(IProgressMonitor monitor) throws CoreException {
		referencedProjects = getProject().getReferencedProjects();
		outputTrace(getProject().getName(), "Clean build requested");	//$NON-NLS-1$
		IManagedBuildInfo info = getBuildInfo();
		if (info == null) {
			outputError(getProject().getName(), "Build information was not found");	//$NON-NLS-1$
			return;
		}
		if (!info.isValid()) {
			outputError(getProject().getName(), "Build information is not valid");	//$NON-NLS-1$
			return;
		}
		IPath buildDirPath = getProject().getLocation().append(info.getConfigurationName());
		IWorkspace workspace = CCorePlugin.getWorkspace();
		IContainer buildDir = workspace.getRoot().getContainerForLocation(buildDirPath);
		if (buildDir == null || !buildDir.isAccessible()){
			outputError(buildDir.getName(), "Could not delete the build directory");	//$NON-NLS-1$
			return;
		}
		String status;		
		try {
			// try the brute force approach first
			status = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.clean.deleting.output", buildDir.getName());	//$NON-NLS-1$
			monitor.subTask(status);
			workspace.delete(new IResource[]{buildDir}, true, monitor);
			StringBuffer buf = new StringBuffer();
			// write to the console
			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(getProject());
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];
			consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_CLEAN);
			consoleHeader[1] = info.getConfigurationName();
			consoleHeader[2] = getProject().getName();
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader));
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			buf = new StringBuffer();
			// Report a successful clean
			String successMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, getProject().getName());
			buf.append(successMsg);
			buf.append(System.getProperty("line.separator", "\n"));  //$NON-NLS-1$//$NON-NLS-2$
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			consoleOutStream.close();
		} catch (CoreException e) {
			// Create a makefile generator for the build
			status = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.clean.build.clean", buildDir.getName());	//$NON-NLS-1$
			monitor.subTask(status);
			IManagedBuilderMakefileGenerator generator = ManagedBuildManager.getBuildfileGenerator(info.getDefaultConfiguration());
			generator.initialize(getProject(), info, monitor);
			cleanBuild(info, generator, monitor);
		}  catch (IOException io) {}	//  Ignore console failures...		
	}
	
	/* (non-Javadoc)
	 * @param info
	 * @param generator
	 * @param monitor
	 */
	protected void cleanBuild(IManagedBuildInfo info, IManagedBuilderMakefileGenerator generator, IProgressMonitor monitor) {
		// Make sure that there is a top level directory and a set of makefiles
		IPath buildDir = generator.getBuildWorkingDir();
		if (buildDir == null) {
			buildDir = new Path(info.getConfigurationName());
		}
		IPath makefilePath = getProject().getLocation().append(buildDir.append(generator.getMakefileName()));		
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		IFile makefile = root.getFileForLocation(makefilePath);
		
		if (buildDir != null && makefile != null && makefile.isAccessible()) {		
			// invoke make with the clean argument
			String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.starting", getProject().getName());	//$NON-NLS-1$
			monitor.subTask(statusMsg);
			checkCancel(monitor);
			invokeMake(CLEAN_BUILD, buildDir, info, generator, monitor);
		}
	}

	/* (non-Javadoc)
	 * @param info
	 * @param generator
	 * @param monitor
	 */
	protected void fullBuild(IManagedBuildInfo info, IManagedBuilderMakefileGenerator generator, IProgressMonitor monitor) throws CoreException {
		// Always need one of these bad boys
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		checkCancel(monitor);
		//If the previous builder invocation was cancelled, generated files might be corrupted
		//in case one or more of the generated makefiles (e.g. dep files) are corrupted, 
		//the builder invocation might fail because of the possible syntax errors, so e.g. "make clean" will not work 
		//we need to explicitly clean the generated directories
		clean(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
		
		// Regenerate the makefiles for this project
		checkCancel(monitor);
		String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.rebuild.makefiles", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		//generator = ManagedBuildManager.getBuildfileGenerator(info.getDefaultConfiguration());
		generator.initialize(getProject(), info, monitor);
		MultiStatus result = generator.regenerateMakefiles();
		if (result.getCode() == IStatus.WARNING || result.getCode() == IStatus.INFO) {
			IStatus[] kids = result.getChildren();
			for (int index = 0; index < kids.length; ++index) {
				// One possibility is that there is nothing to build
				IStatus status = kids[index]; 
				if (status.getCode() == IManagedBuilderMakefileGenerator.NO_SOURCE_FOLDERS) {
					// Inform the user, via the console, that there is nothing to build
					// either because there are no buildable sources files or all potentially
					// buildable files have been excluded from build
					try { 
						emitNoSourceMessage(FULL_BUILD, status, info.getConfigurationName());
					} catch (CoreException e) {
						// Throw the exception back to the builder
					    throw e;
					}					
					// Dude, we're done
					return;
				} else {
					// Stick this in the list of stuff to warn the user about
					getGenerationProblems().add(status);		
				}				
			}
		}
		
		// Now call make
		checkCancel(monitor);
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.starting", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		IPath topBuildDir = generator.getBuildWorkingDir();
		if (topBuildDir != null) {
			invokeMake(FULL_BUILD, topBuildDir, info, generator, monitor);
		} else {
			statusMsg = ManagedMakeMessages.getFormattedString(NOTHING_BUILT, getProject().getName());	//$NON-NLS-1$
			monitor.subTask(statusMsg);
			return;
		}
		
		// Now regenerate the dependencies
		checkCancel(monitor);
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.regen.deps", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		try {
			generator.regenerateDependencies(false);
		} catch (CoreException e) {
			// Throw the exception back to the builder
			throw e;
		}

		//  Build finished message
		statusMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
	}
	
	/* (non-Javadoc)
	 * 
	 * @return
	 */
	private Vector getGenerationProblems() {
		if (generationProblems == null) {
			generationProblems = new Vector();
		}
		return generationProblems;
	}
	
	/* (non-javadoc)
	 * Answers an array of strings with the proper make targets
        * for a build with no custom prebuild/postbuild steps 
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
	protected void incrementalBuild(IResourceDelta delta, IManagedBuildInfo info, IManagedBuilderMakefileGenerator generator, IProgressMonitor monitor) throws CoreException {
		// Need to report status to the user
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		// Ask the makefile generator to generate any makefiles needed to build delta
		checkCancel(monitor);
		String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.update.makefiles", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		MultiStatus result = generator.generateMakefiles(delta);
		if (result.getCode() == IStatus.WARNING || result.getCode() == IStatus.INFO) {
			IStatus[] kids = result.getChildren();
			for (int index = 0; index < kids.length; ++index) {
				// One possibility is that there is nothing to build
				IStatus status = kids[index]; 
				if (status.getCode() == IManagedBuilderMakefileGenerator.NO_SOURCE_FOLDERS) {
					// Inform the user, via the console, that there is nothing to build
					// either because there are no buildable sources files or all potentially
					// buildable files have been excluded from build
					try { 
						emitNoSourceMessage(INCREMENTAL_BUILD, status, info.getConfigurationName());
					} catch (CoreException e) {
						// Throw the exception back to the builder
					    throw e;
					}					
					// Dude, we're done
					return;
				} else {
					// Stick this in the list of stuff to warn the user about
					getGenerationProblems().add(status);		
				}				
			}
		}

		// Run the build
		checkCancel(monitor);
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.starting", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		IPath buildDir = generator.getBuildWorkingDir();
		if (buildDir != null) {
			invokeMake(INCREMENTAL_BUILD, buildDir, info, generator, monitor);
		} else {
			statusMsg = ManagedMakeMessages.getFormattedString(NOTHING_BUILT, getProject().getName());	//$NON-NLS-1$
			monitor.subTask(statusMsg);
			return;
		}
		
		// Generate the dependencies for all changes
		checkCancel(monitor);
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.updating.deps", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		try {
			generator.generateDependencies();
		} catch (CoreException e) {
			throw e;
		}
		
		// Build finished message
		statusMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
}

	/* (non-Javadoc)
     * @param buildType 
	 * @param buildDir
	 * @param info
     * @param generator 
	 * @param monitor
	 */
	protected void invokeMake(int buildType, IPath buildDir, IManagedBuildInfo info, IManagedBuilderMakefileGenerator generator, IProgressMonitor monitor) {
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
			IPath makefile = workingDirectory.append(generator.getMakefileName());
			if (root.getFileForLocation(makefile) == null) {
				return; 
			}

			// Flag to the user that make is about to be called
			String makeCmd = info.getBuildCommand();
			//try to resolve the build macros in the builder command
			try{
				String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						makeCmd,
						"", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION,
						info.getDefaultConfiguration());
				if((resolved = resolved.trim()).length() > 0)
					makeCmd = resolved;
			} catch (BuildMacroException e){
			}

			IPath makeCommand = new Path(makeCmd); 
			if (makeCommand != null) {
				String[] msgs = new String[2];
				msgs[0] = makeCommand.toString();
				msgs[1] = currentProject.getName();
				monitor.subTask(ManagedMakeMessages.getFormattedString(MAKE, msgs));

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
				
				IConfiguration cfg = info.getDefaultConfiguration();
				if(!cfg.isSupported()){
					buf.append(ManagedMakeMessages.getFormattedString(WARNING_UNSUPPORTED_CONFIGURATION,new String[] {cfg.getName(),cfg.getToolChain().getName()}));
					buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
					buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				}
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
				
				// Remove all markers for this project
				removeAllMarkers(currentProject);
			
				// Get a launcher for the make command
				String errMsg = null;
				CommandLauncher launcher = new CommandLauncher();
				launcher.showCommand(true);
	
				// Set the environmennt
				IBuildEnvironmentVariable variables[] = ManagedBuildManager.getEnvironmentVariableProvider().getVariables(cfg,true,true);
				String[] env = null;
				ArrayList envList = new ArrayList();
				if (variables != null) {
					for(int i = 0; i < variables.length; i++){
						envList.add(variables[i].getName() + "=" + variables[i].getValue());	//$NON-NLS-1$
					}
					env = (String[]) envList.toArray(new String[envList.size()]);
				}
			
				// Hook up an error parser manager
				String[] errorParsers = info.getDefaultConfiguration().getErrorParserList(); 
				ErrorParserManager epm = new ErrorParserManager(getProject(), workingDirectory, this, errorParsers);
				epm.setOutputStream(consoleOutStream);
				// This variable is necessary to ensure that the EPM stream stay open
				// until we explicitly close it. See bug#123302.
				OutputStream epmOutputStream = epm.getOutputStream();
			
                // Get the arguments to be passed to make from build model 
				ArrayList makeArgs = new ArrayList();
				String arg = info.getBuildArguments();
				if (arg.length() > 0) {
					String[] args = arg.split("\\s"); //$NON-NLS-1$ 
					for (int i = 0; i < args.length; ++i) {
						makeArgs.add(args[i]);
					}
				}

				String[] makeTargets;
				String prebuildStep = info.getPrebuildStep();
				//try to resolve the build macros in the prebuildStep
				try{
					prebuildStep = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
							prebuildStep,
							"", //$NON-NLS-1$
							" ", //$NON-NLS-1$
							IBuildMacroProvider.CONTEXT_CONFIGURATION,
							cfg);
				} catch (BuildMacroException e){
				}
				boolean prebuildStepPresent = (prebuildStep.length() > 0);
				Process proc = null;
				boolean isuptodate = false;

				if (prebuildStepPresent) {
					ArrayList premakeArgs = (ArrayList) makeArgs.clone();
					String[] premakeTargets;
					switch (buildType) {
					case INCREMENTAL_BUILD: {
						// For an incremental build with a prebuild step: 
						// Check the status of the main build with "make -q main-build" 
						// If up to date: 
						// then: don't invoke the prebuild step, which should be run only if 
						//       something needs to be built in the main build 
						// else: invoke the prebuild step and the main build step 
						premakeArgs.add("-q"); //$NON-NLS-1$ 
						premakeArgs.add("main-build"); //$NON-NLS-1$ 
						premakeTargets = (String[]) premakeArgs.toArray(new String[premakeArgs.size()]);
						proc = launcher.execute(makeCommand, premakeTargets, env, workingDirectory);
						if (proc != null) {
							try {
								// Close the input of the process since we will never write to it
								proc.getOutputStream().close();
							} catch (IOException e) {
							}
							if (launcher.waitAndRead(epm.getOutputStream(), epm.getOutputStream(),
									new SubProgressMonitor(monitor,
											IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
								errMsg = launcher.getErrorMessage();
							}
						} else {
							errMsg = launcher.getErrorMessage();
						}

						if ((errMsg != null && errMsg.length() > 0) || proc == null) {
							// Can't tell if the build is needed, so assume it is, and let any errors be triggered
							// when the "real" build is invoked below 
							makeArgs.add("pre-build"); //$NON-NLS-1$ 
							makeArgs.add("main-build"); //$NON-NLS-1$ 
						} else {
							// The "make -q" command launch was successful 
							if (proc.exitValue() == 0) {
								// If the status value returned from "make -q" is 0, then the build state is up-to-date
								isuptodate = true;
								// Report that the build was up to date, and thus nothing needs to be built
								String uptodateMsg = ManagedMakeMessages
										.getFormattedString(NOTHING_BUILT, currentProject.getName());
								buf = new StringBuffer();
								buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
								buf.append(uptodateMsg);
								buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
								// Write message on the console 
								consoleOutStream.write(buf.toString().getBytes());
								consoleOutStream.flush();
								epmOutputStream.close();
								consoleOutStream.close();
							} else {
								// The status value was other than 0, so press on with the build process
								makeArgs.add("pre-build"); //$NON-NLS-1$                        
								makeArgs.add("main-build"); //$NON-NLS-1$ 
							}
						}
						break;
					}
					case FULL_BUILD: {
						makeArgs.add("clean"); //$NON-NLS-1$        
						makeArgs.add("pre-build"); //$NON-NLS-1$ 
						makeArgs.add("main-build"); //$NON-NLS-1$                           
						break;
					}
					case CLEAN_BUILD: {
						makeArgs.add("clean"); //$NON-NLS-1$                                                            
						break;
					}
					}

				} else {
					// No prebuild step 
					// 
					makeArgs.addAll(Arrays.asList(getMakeTargets(buildType)));
				}

				makeTargets = (String[]) makeArgs.toArray(new String[makeArgs.size()]);

				// Launch make - main invocation 
				if (!isuptodate) {
					proc = launcher.execute(makeCommand, makeTargets, env,
								workingDirectory);
					if (proc != null) {
						try {
							// Close the input of the process since we will never write to it
							proc.getOutputStream().close();
						} catch (IOException e) {
						}

						if (launcher.waitAndRead(epm.getOutputStream(), epm.getOutputStream(),
								new SubProgressMonitor(monitor,
										IProgressMonitor.UNKNOWN)) != CommandLauncher.OK) {
							errMsg = launcher.getErrorMessage();
						}

						// Force a resync of the projects without allowing the user to cancel. 
						// This is probably unkind, but short of this there is no way to insure 
						// the UI is up-to-date with the build results 
						monitor.subTask(ManagedMakeMessages
								.getResourceString(REFRESH));
						try {
							currentProject.refreshLocal(
									IResource.DEPTH_INFINITE, null);
						} catch (CoreException e) {
							monitor.subTask(ManagedMakeMessages
									.getResourceString(REFRESH_ERROR));
						}
					} else {
						errMsg = launcher.getErrorMessage();
					}

					// Report either the success or failure of our mission 
					buf = new StringBuffer();
					if (errMsg != null && errMsg.length() > 0) {
						String errorDesc = ManagedMakeMessages
								.getResourceString(BUILD_ERROR);
						buf.append(errorDesc);
						buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
						buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ 
					} else {
						// Report a successful build 
						String successMsg = ManagedMakeMessages
								.getFormattedString(BUILD_FINISHED,
										currentProject.getName());
						buf.append(successMsg);
						buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
					}

					// Write message on the console 
					consoleOutStream.write(buf.toString().getBytes());
					consoleOutStream.flush();
					epmOutputStream.close();

					// Generate any error markers that the build has discovered 
					monitor.subTask(ManagedMakeMessages
							.getResourceString(MARKERS));
					addBuilderMarkers(epm);
					epm.reportProblems();
					consoleOutStream.close();
				}
			}                               
		} catch (Exception e) {
			forgetLastBuiltState();
		} finally {
			getGenerationProblems().clear();
		}
	}

	/* (non-Javadoc)
	 * Removes the IMarkers for the project specified in the argument if the
	 * project exists, and is open. 
	 * 
	 * @param project
	 */
	private void removeAllMarkers(IProject project) {
		if (project == null || !project.isAccessible()) return;

		// Clear out the problem markers
		IWorkspace workspace = project.getWorkspace();
		IMarker[] markers;
		try {
			markers = project.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			// Handled just about every case in the sanity check
			return;
		}
		if (markers != null) {
			try {
				workspace.deleteMarkers(markers);
			} catch (CoreException e) {
				// The only situation that might cause this is some sort of resource change event
				return;
			}
		}
	}
}
