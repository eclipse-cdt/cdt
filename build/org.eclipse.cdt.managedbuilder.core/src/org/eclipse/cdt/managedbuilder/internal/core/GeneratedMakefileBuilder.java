/*******************************************************************************
 *  Copyright (c) 2002, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.RefreshScopeManager;
import org.eclipse.cdt.internal.core.BuildRunnerHelper;
import org.eclipse.cdt.internal.errorparsers.FixitManager;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildIOType;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.DescriptionBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IBuildModelBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.ParallelBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.StepBuilder;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
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
		private List<String> reservedNames;

		public ResourceDeltaVisitor(IManagedBuildInfo info) {
			buildInfo = info;
			String ext = buildInfo.getBuildArtifactExtension();
			//try to resolve build macros in the build artifact extension
			try {
				ext = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(ext, "", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION, info.getDefaultConfiguration());
			} catch (BuildMacroException e) {
			}

			String name = buildInfo.getBuildArtifactName();
			//try to resolve build macros in the build artifact name
			try {
				String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(name, "", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION, info.getDefaultConfiguration());
				if ((resolved = resolved.trim()).length() > 0)
					name = resolved;
			} catch (BuildMacroException e) {
			}

			if (ext.length() > 0) {
				buildGoalName = buildInfo.getOutputPrefix(ext) + name + IManagedBuilderMakefileGenerator.DOT + ext;
			} else {
				buildGoalName = name;
			}
			reservedNames = Arrays.asList(new String[] { ".cdtbuild", ".cdtproject", ".project" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		private boolean isGeneratedResource(IResource resource) {
			// Is this a generated directory ...
			IPath path = resource.getProjectRelativePath();
			String[] configNames = buildInfo.getConfigurationNames();
			for (int i = 0; i < configNames.length; i++) {
				String name = configNames[i];
				IPath root = new Path(name);
				// It is if it is a root of the resource pathname
				if (root.isPrefixOf(path))
					return true;
			}
			return false;
		}

		private boolean isProjectFile(IResource resource) {
			return reservedNames.contains(resource.getName());
		}

		public boolean shouldBuildIncr() {
			return incrBuildNeeded;
		}

		public boolean shouldBuildFull() {
			return fullBuildNeeded;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			// If the project has changed, then a build is needed and we can stop
			if (resource != null && resource.getProject() == getProject()) {
				switch (resource.getType()) {
				case IResource.FILE:
					String name = resource.getName();
					if ((!name.equals(buildGoalName) &&
					// TODO:  Also need to check for secondary outputs
							(resource.isDerived() || (isProjectFile(resource)) || (isGeneratedResource(resource))))) {
						// The resource that changed has attributes which make it uninteresting,
						// so don't do anything
					} else {
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
						if (delta.getKind() == IResourceDelta.REMOVED) {
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

					return false;
				}
			}
			return true;
		}
	}

	private static class OtherConfigVerifier implements IResourceDeltaVisitor {
		IConfiguration config;
		IConfiguration configs[];
		Configuration otherConfigs[];
		int resourceChangeState;

		private static final IPath[] ignoreList = { new Path(".cdtproject"), //$NON-NLS-1$
				new Path(".cproject"), //$NON-NLS-1$
				new Path(".cdtbuild"), //$NON-NLS-1$
				new Path(".settings"), //$NON-NLS-1$
		};

		OtherConfigVerifier(IConfiguration cfg) {
			config = cfg;
			configs = cfg.getManagedProject().getConfigurations();
			otherConfigs = new Configuration[configs.length - 1];
			int counter = 0;
			for (int i = 0; i < configs.length; i++) {
				if (configs[i] != config)
					otherConfigs[counter++] = (Configuration) configs[i];
			}
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {

			IResource rc = delta.getResource();
			if (rc.getType() == IResource.FILE) {
				if (isResourceValuable(rc))
					resourceChangeState |= delta.getKind();
				return false;
			}
			return !isGeneratedForConfig(rc, config) && isResourceValuable(rc);
		}

		public void updateOtherConfigs(IResourceDelta delta) {
			if (delta == null)
				resourceChangeState = ~0;
			else {
				try {
					delta.accept(this);
				} catch (CoreException e) {
					resourceChangeState = ~0;
				}
			}

			setResourceChangeStateForOtherConfigs();
		}

		private void setResourceChangeStateForOtherConfigs() {
			for (int i = 0; i < otherConfigs.length; i++) {
				otherConfigs[i].addResourceChangeState(resourceChangeState);
			}
		}

		private boolean isGeneratedForConfig(IResource resource, IConfiguration cfg) {
			// Is this a generated directory ...
			IPath path = resource.getProjectRelativePath();
			IPath root = new Path(cfg.getName());
			// It is if it is a root of the resource pathname
			if (root.isPrefixOf(path))
				return true;
			return false;
		}

		private boolean isResourceValuable(IResource rc) {
			IPath path = rc.getProjectRelativePath();
			for (int i = 0; i < ignoreList.length; i++) {
				if (ignoreList[i].equals(path))
					return false;
			}
			return true;
		}
	}

	// String constants
	private static final String BUILD_ERROR = "ManagedMakeBuilder.message.error"; //$NON-NLS-1$
	private static final String BUILD_FINISHED = "ManagedMakeBuilder.message.finished"; //$NON-NLS-1$
	private static final String CONSOLE_HEADER = "ManagedMakeBuilder.message.console.header"; //$NON-NLS-1$
	private static final String ERROR_HEADER = "GeneratedmakefileBuilder error ["; //$NON-NLS-1$
	private static final String MAKE = "ManagedMakeBuilder.message.make"; //$NON-NLS-1$
	private static final String MARKERS = "ManagedMakeBuilder.message.creating.markers"; //$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String NOTHING_BUILT = "ManagedMakeBuilder.message.no.build"; //$NON-NLS-1$
	private static final String REFRESH = "ManagedMakeBuilder.message.updating"; //$NON-NLS-1$
	private static final String REFRESH_ERROR = BUILD_ERROR + ".refresh"; //$NON-NLS-1$
	private static final String TRACE_FOOTER = "]: "; //$NON-NLS-1$
	private static final String TRACE_HEADER = "GeneratedmakefileBuilder trace ["; //$NON-NLS-1$
	private static final String TYPE_CLEAN = "ManagedMakeBuilder.type.clean"; //$NON-NLS-1$
	private static final String TYPE_INC = "ManagedMakeBuider.type.incremental"; //$NON-NLS-1$
	private static final String WARNING_UNSUPPORTED_CONFIGURATION = "ManagedMakeBuilder.warning.unsupported.configuration"; //$NON-NLS-1$
	private static final String BUILD_CANCELLED = "ManagedMakeBuilder.message.cancelled"; //$NON-NLS-1$
	private static final String BUILD_FINISHED_WITH_ERRS = "ManagedMakeBuilder.message.finished.with.errs"; //$NON-NLS-1$
	private static final String BUILD_FAILED_ERR = "ManagedMakeBuilder.message.internal.builder.error"; //$NON-NLS-1$
	private static final String BUILD_STOPPED_ERR = "ManagedMakeBuilder.message.stopped.error"; //$NON-NLS-1$
	private static final String INTERNAL_BUILDER_HEADER_NOTE = "ManagedMakeBuilder.message.internal.builder.header.note"; //$NON-NLS-1$
	private static final String TYPE_REBUILD = "ManagedMakeBuider.type.rebuild"; //$NON-NLS-1$
	private static final String INTERNAL_BUILDER = "ManagedMakeBuilder.message.internal.builder"; //$NON-NLS-1$

	private static final int PROGRESS_MONITOR_SCALE = 100;
	private static final int TICKS_STREAM_PROGRESS_MONITOR = 1 * PROGRESS_MONITOR_SCALE;
	private static final int TICKS_DELETE_MARKERS = 1 * PROGRESS_MONITOR_SCALE;

	public static boolean VERBOSE = false;

	// Local variables
	protected Vector<IStatus> generationProblems;
	protected IProject[] referencedProjects;
	protected List<IResource> resourcesToBuild;
	private IConsole console;

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

	private void addBuilderMarkers(ErrorParserManager epm) {
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		Iterator<IStatus> iter = getGenerationProblems().iterator();
		while (iter.hasNext()) {
			IStatus stat = iter.next();
			IResource location = root.findMember(stat.getMessage());
			if (stat.getCode() == IManagedBuilderMakefileGenerator.SPACES_IN_PATH) {
				epm.generateMarker(location, -1,
						ManagedMakeMessages.getResourceString("MakefileGenerator.error.spaces"), //$NON-NLS-1$
						IMarkerGenerator.SEVERITY_WARNING, null);
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
			StringBuilder buf = new StringBuilder();
			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(getProject());
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			// Report a successful clean
			String[] consoleHeader = new String[3];
			if (buildType == FULL_BUILD || buildType == INCREMENTAL_BUILD) {
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
			} else {
				consoleHeader[0] = ""; //$NON-NLS-1$
				outputError(getProject().getName(), "The given build type is not supported in this context"); //$NON-NLS-1$
			}
			consoleHeader[1] = configName;
			consoleHeader[2] = getProject().getName();
			buf.append(NEWLINE);
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader)).append(NEWLINE);
			buf.append(NEWLINE);
			buf.append(status.getMessage()).append(NEWLINE);
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			consoleOutStream.close();
		} catch (CoreException e) {
			// Throw the exception back to the builder
			throw e;
		} catch (IOException io) { //  Ignore console failures...
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
	@Override
	protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map argsMap, IProgressMonitor monitor)
			throws CoreException {
		@SuppressWarnings("unchecked")
		Map<String, String> args = argsMap;
		if (DEBUG_EVENTS)
			printEvent(kind, args);

		// We should always tell the build system what projects we reference
		referencedProjects = getProject().getReferencedProjects();

		// Get the build information
		IManagedBuildInfo info = getBuildInfo();
		if (info == null) {
			outputError(getProject().getName(), "Build information was not found"); //$NON-NLS-1$
			return referencedProjects;
		}
		if (!info.isValid()) {
			outputError(getProject().getName(), "Build information is not valid"); //$NON-NLS-1$
			return referencedProjects;
		}

		IConfiguration[] cfgs = null;
		if (needAllConfigBuild()) {
			cfgs = info.getManagedProject().getConfigurations();
		} else {
			cfgs = new IConfiguration[] { info.getDefaultConfiguration() };
		}

		SubMonitor progress = SubMonitor.convert(monitor, cfgs.length);
		for (IConfiguration cfg : cfgs) {
			SubMonitor subProgress = progress.split(100);
			updateOtherConfigs(cfg, kind);

			if (((Configuration) cfg).isInternalBuilderEnabled()) {
				invokeInternalBuilder(cfg, kind != FULL_BUILD, ((Configuration) cfg).getInternalBuilderIgnoreErr(),
						progress.split(1));

				// Scrub the build info the project
				info.setRebuildState(false);
				return referencedProjects;
			}

			// Create a makefile generator for the build
			IManagedBuilderMakefileGenerator generator = ManagedBuildManager
					.getBuildfileGenerator(info.getDefaultConfiguration());
			generator.initialize(getProject(), info, subProgress.split(5));

			//perform necessary cleaning and build type calculation
			if (cfg.needsFullRebuild()) {
				//configuration rebuild state is set to true,
				//full rebuild is needed in any case
				//clean first, then make a full build
				outputTrace(getProject().getName(), "config rebuild state is set to true, making a full rebuild"); //$NON-NLS-1$
				clean(subProgress.split(15));
				fullBuild(info, generator, subProgress.split(80));
			} else {
				boolean fullBuildNeeded = info.needsRebuild();
				IBuildDescription des = null;

				IResourceDelta delta = kind == FULL_BUILD ? null : getDelta(getProject());
				if (delta == null)
					fullBuildNeeded = true;
				if (cfg.needsRebuild() || delta != null) {
					//use a build description model to calculate the resources to be cleaned
					//only in case there are some changes to the project sources or build information
					try {
						int flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.DEPFILES
								| BuildDescriptionManager.DEPS;
						if (delta != null)
							flags |= BuildDescriptionManager.REMOVED;

						outputTrace(getProject().getName(), "using a build description.."); //$NON-NLS-1$

						des = BuildDescriptionManager.createBuildDescription(info.getDefaultConfiguration(),
								getDelta(getProject()), flags);

						BuildDescriptionManager.cleanGeneratedRebuildResources(des);
						subProgress.worked(15);
					} catch (Throwable e) {
						//TODO: log error
						outputError(getProject().getName(),
								"error occured while build description calculation: " + e.getLocalizedMessage()); //$NON-NLS-1$
						//in case an error occured, make it behave in the old stile:
						if (info.needsRebuild()) {
							//make a full clean if an info needs a rebuild
							clean(subProgress.split(15));
							fullBuildNeeded = true;
						} else if (delta != null && !fullBuildNeeded) {
							// Create a delta visitor to detect the build type
							ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(info);
							delta.accept(visitor);
							if (visitor.shouldBuildFull()) {
								clean(subProgress.split(15));
								fullBuildNeeded = true;
							}
						}
					}
				}

				if (fullBuildNeeded) {
					outputTrace(getProject().getName(), "performing a full build"); //$NON-NLS-1$
					fullBuild(info, generator, subProgress.split(80));
				} else {
					outputTrace(getProject().getName(), "performing an incremental build"); //$NON-NLS-1$
					incrementalBuild(delta, info, generator, subProgress.split(80));
				}
			}
		}
		/*
		// So let's figure out why we got called
		if (kind == FULL_BUILD) {
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
		 */
		// Scrub the build info the project
		info.setRebuildState(false);
		// Ask build mechanism to compute deltas for project dependencies next time
		return referencedProjects;
	}

	private void updateOtherConfigs(IConfiguration cfg, int buildKind) {
		new OtherConfigVerifier(cfg).updateOtherConfigs(buildKind == FULL_BUILD ? null : getDelta(getProject()));
	}

	/**
	 * Check whether the build has been canceled. Cancellation requests
	 * propagated to the caller by throwing <code>OperationCanceledException</code>.
	 *
	 * @see org.eclipse.core.runtime.OperationCanceledException#OperationCanceledException()
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled()) {
			outputTrace(getProject().getName(), "Build cancelled"); //$NON-NLS-1$
			forgetLastBuiltState();
			throw new OperationCanceledException();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		if (DEBUG_EVENTS)
			printEvent(IncrementalProjectBuilder.CLEAN_BUILD, null);

		referencedProjects = getProject().getReferencedProjects();
		outputTrace(getProject().getName(), "Clean build requested"); //$NON-NLS-1$
		IManagedBuildInfo info = getBuildInfo();
		if (info == null) {
			outputError(getProject().getName(), "Build information was not found"); //$NON-NLS-1$
			return;
		}
		if (!info.isValid()) {
			outputError(getProject().getName(), "Build information is not valid"); //$NON-NLS-1$
			return;
		}
		IPath buildDirPath = getProject().getLocation().append(info.getConfigurationName());
		IWorkspace workspace = CCorePlugin.getWorkspace();
		IContainer buildDir = workspace.getRoot().getContainerForLocation(buildDirPath);
		if (buildDir == null || !buildDir.isAccessible()) {
			outputError(buildDir == null ? "null" : buildDir.getName(), "Could not delete the build directory"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		String status;
		try {
			// try the brute force approach first
			status = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.clean.deleting.output", //$NON-NLS-1$
					buildDir.getName());
			monitor.subTask(status);
			workspace.delete(new IResource[] { buildDir }, true, monitor);
			StringBuilder buf = new StringBuilder();
			// write to the console
			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(getProject());
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];
			consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_CLEAN);
			consoleHeader[1] = info.getConfigurationName();
			consoleHeader[2] = getProject().getName();
			buf.append(NEWLINE);
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader)).append(NEWLINE);
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			buf = new StringBuilder();
			// Report a successful clean
			String successMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, getProject().getName());
			buf.append(successMsg).append(NEWLINE);
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			consoleOutStream.close();
		} catch (CoreException e) {
			// Create a makefile generator for the build
			status = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.clean.build.clean", //$NON-NLS-1$
					buildDir.getName());
			monitor.subTask(status);
			IManagedBuilderMakefileGenerator generator = ManagedBuildManager
					.getBuildfileGenerator(info.getDefaultConfiguration());
			generator.initialize(getProject(), info, monitor);
			cleanBuild(info, generator, monitor);
		} catch (IOException io) {
		} //  Ignore console failures...
	}

	/* (non-Javadoc)
	 * @param info
	 * @param generator
	 * @param monitor
	 */
	protected void cleanBuild(IManagedBuildInfo info, IManagedBuilderMakefileGenerator generator,
			IProgressMonitor monitor) {
		// Make sure that there is a top level directory and a set of makefiles
		IPath buildDir = generator.getBuildWorkingDir();
		if (buildDir == null) {
			buildDir = new Path(info.getConfigurationName());
		}
		IPath makefilePath = getProject().getLocation().append(buildDir.append(generator.getMakefileName()));
		IWorkspaceRoot root = CCorePlugin.getWorkspace().getRoot();
		IFile makefile = root.getFileForLocation(makefilePath);

		if (makefile != null && makefile.isAccessible()) {
			// invoke make with the clean argument
			String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.starting", //$NON-NLS-1$
					getProject().getName());
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
	protected void fullBuild(IManagedBuildInfo info, IManagedBuilderMakefileGenerator generator,
			IProgressMonitor monitor) throws CoreException {
		// Always need one of these bad boys
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		checkCancel(monitor);
		//If the previous builder invocation was cancelled, generated files might be corrupted
		//in case one or more of the generated makefiles (e.g. dep files) are corrupted,
		//the builder invocation might fail because of the possible syntax errors, so e.g. "make clean" will not work
		//we need to explicitly clean the generated directories
		//		clean(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));

		// Regenerate the makefiles for this project
		checkCancel(monitor);
		String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.rebuild.makefiles", //$NON-NLS-1$
				getProject().getName());
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
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.starting", //$NON-NLS-1$
				getProject().getName());
		monitor.subTask(statusMsg);
		IPath topBuildDir = generator.getBuildWorkingDir();
		if (topBuildDir != null) {
			invokeMake(FULL_BUILD, topBuildDir, info, generator, monitor);
		} else {
			statusMsg = ManagedMakeMessages.getFormattedString(NOTHING_BUILT, getProject().getName());
			monitor.subTask(statusMsg);
			return;
		}

		// Now regenerate the dependencies
		checkCancel(monitor);
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.regen.deps", //$NON-NLS-1$
				getProject().getName());
		monitor.subTask(statusMsg);
		try {
			generator.regenerateDependencies(false);
		} catch (CoreException e) {
			// Throw the exception back to the builder
			throw e;
		}

		//  Build finished message
		statusMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, getProject().getName());
		monitor.subTask(statusMsg);
	}

	/* (non-Javadoc)
	 *
	 * @return
	 */
	private Vector<IStatus> getGenerationProblems() {
		if (generationProblems == null) {
			generationProblems = new Vector<>();
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
		List<String> args = new ArrayList<>();
		switch (buildType) {
		case CLEAN_BUILD:
			args.add("clean"); //$NON-NLS-1$
			break;
		case FULL_BUILD:
		case INCREMENTAL_BUILD:
			args.add("all"); //$NON-NLS-1$
			break;
		}
		return args.toArray(new String[args.size()]);
	}

	protected List<IResource> getResourcesToBuild() {
		if (resourcesToBuild == null) {
			resourcesToBuild = new ArrayList<>();
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
	protected void incrementalBuild(IResourceDelta delta, IManagedBuildInfo info,
			IManagedBuilderMakefileGenerator generator, IProgressMonitor monitor) throws CoreException {
		// Need to report status to the user
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// Ask the makefile generator to generate any makefiles needed to build delta
		checkCancel(monitor);
		String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.update.makefiles", //$NON-NLS-1$
				getProject().getName());
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
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.starting", //$NON-NLS-1$
				getProject().getName());
		monitor.subTask(statusMsg);
		IPath buildDir = generator.getBuildWorkingDir();
		if (buildDir != null) {
			invokeMake(INCREMENTAL_BUILD, buildDir, info, generator, monitor);
		} else {
			statusMsg = ManagedMakeMessages.getFormattedString(NOTHING_BUILT, getProject().getName());
			monitor.subTask(statusMsg);
			return;
		}

		// Generate the dependencies for all changes
		checkCancel(monitor);
		statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.updating.deps", //$NON-NLS-1$
				getProject().getName());
		monitor.subTask(statusMsg);
		try {
			generator.generateDependencies();
		} catch (CoreException e) {
			throw e;
		}

		// Build finished message
		statusMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, getProject().getName());
		monitor.subTask(statusMsg);
	}

	/* (non-Javadoc)
	 * @param buildType
	 * @param buildDir
	 * @param info
	 * @param generator
	 * @param monitor
	 */
	protected void invokeMake(int buildType, IPath buildDir, IManagedBuildInfo info,
			IManagedBuilderMakefileGenerator generator, IProgressMonitor monitor) {
		// Get the project and make sure there's a monitor to cancel the build
		IProject project = getProject();
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			// Figure out the working directory for the build and make sure there is a makefile there
			final URI workingDirectoryURI = getProject().getFolder(buildDir).getLocationURI();
			final String pathFromURI = EFSExtensionManager.getDefault().getPathFromURI(workingDirectoryURI);
			if (pathFromURI == null) {
				throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID,
						ManagedMakeMessages.getString("ManagedMakeBuilder.message.error"), null)); //$NON-NLS-1$
			}

			IPath workingDirectory = new Path(pathFromURI);

			IWorkspace workspace = project.getWorkspace();
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
			try {
				String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(makeCmd, "", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION, info.getDefaultConfiguration());
				if ((resolved = resolved.trim()).length() > 0)
					makeCmd = resolved;
			} catch (BuildMacroException e) {
			}

			IPath makeCommand = new Path(makeCmd);
			String[] msgs = new String[2];
			msgs[0] = makeCommand.toString();
			msgs[1] = project.getName();
			monitor.subTask(ManagedMakeMessages.getFormattedString(MAKE, msgs));

			// Get a build console for the project
			StringBuilder buf = new StringBuilder();
			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(project);
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];
			switch (buildType) {
			case FULL_BUILD:
			case INCREMENTAL_BUILD:
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
				break;
			case CLEAN_BUILD:
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_CLEAN);
				break;
			}

			consoleHeader[1] = info.getConfigurationName();
			consoleHeader[2] = project.getName();
			buf.append(NEWLINE);
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader)).append(NEWLINE);
			buf.append(NEWLINE);

			IConfiguration cfg = info.getDefaultConfiguration();
			if (!cfg.isSupported()) {
				String msg = ManagedMakeMessages.getFormattedString(WARNING_UNSUPPORTED_CONFIGURATION,
						new String[] { cfg.getName(), cfg.getToolChain().getName() });
				buf.append(msg).append(NEWLINE);
				buf.append(NEWLINE);
			}
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();

			// Remove all markers for this project
			removeAllMarkers(project);

			// Get a launcher for the make command
			String errMsg = null;
			IBuilder builder = info.getDefaultConfiguration().getBuilder();
			ICommandLauncher launcher = builder.getCommandLauncher();
			launcher.setProject(project);
			launcher.showCommand(true);

			// Set the environmennt
			IBuildEnvironmentVariable variables[] = ManagedBuildManager.getEnvironmentVariableProvider()
					.getVariables(cfg, true, true);
			String[] envp = null;
			ArrayList<String> envList = new ArrayList<>();
			if (variables != null) {
				for (int i = 0; i < variables.length; i++) {
					envList.add(variables[i].getName() + "=" + variables[i].getValue()); //$NON-NLS-1$
				}
				envp = envList.toArray(new String[envList.size()]);
			}

			// Hook up an error parser manager
			String[] errorParsers = info.getDefaultConfiguration().getErrorParserList();
			ErrorParserManager epm = new ErrorParserManager(getProject(), workingDirectoryURI, this, errorParsers);
			epm.setOutputStream(consoleOutStream);
			// This variable is necessary to ensure that the EPM stream stay open
			// until we explicitly close it. See bug#123302.
			OutputStream epmOutputStream = epm.getOutputStream();

			// Get the arguments to be passed to make from build model
			ArrayList<String> makeArgs = new ArrayList<>();
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
			try {
				prebuildStep = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(prebuildStep,
						"", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION, cfg);
			} catch (BuildMacroException e) {
			}
			boolean prebuildStepPresent = (prebuildStep.length() > 0);
			Process proc = null;
			boolean isuptodate = false;

			if (prebuildStepPresent) {
				@SuppressWarnings("unchecked")
				ArrayList<String> premakeArgs = (ArrayList<String>) makeArgs.clone();
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
					premakeTargets = premakeArgs.toArray(new String[premakeArgs.size()]);
					proc = launcher.execute(makeCommand, premakeTargets, envp, workingDirectory, monitor);
					if (proc != null) {
						try {
							// Close the input of the process since we will never write to it
							proc.getOutputStream().close();
						} catch (IOException e) {
						}
						if (launcher.waitAndRead(epm.getOutputStream(), epm.getOutputStream(),
								SubMonitor.convert(monitor, IProgressMonitor.UNKNOWN)) != ICommandLauncher.OK) {
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
							String uptodateMsg = ManagedMakeMessages.getFormattedString(NOTHING_BUILT,
									project.getName());
							buf = new StringBuilder();
							buf.append(NEWLINE);
							buf.append(uptodateMsg).append(NEWLINE);
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
					//						makeArgs.add("clean"); //$NON-NLS-1$
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

			makeTargets = makeArgs.toArray(new String[makeArgs.size()]);

			// Launch make - main invocation
			if (!isuptodate) {
				proc = launcher.execute(makeCommand, makeTargets, envp, workingDirectory, monitor);
				if (proc != null) {
					try {
						// Close the input of the process since we will never write to it
						proc.getOutputStream().close();
					} catch (IOException e) {
					}

					int state = launcher.waitAndRead(epm.getOutputStream(), epm.getOutputStream(),
							SubMonitor.convert(monitor, IProgressMonitor.UNKNOWN));
					if (state != ICommandLauncher.OK) {
						errMsg = launcher.getErrorMessage();

						if (state == ICommandLauncher.COMMAND_CANCELED) {
							//TODO: the better way of handling cancel is needed
							//currently the rebuild state is set to true forcing the full rebuild
							//on the next builder invocation
							info.getDefaultConfiguration().setRebuildState(true);
						}
					}

					// Force a resync of the projects without allowing the user to cancel.
					// This is probably unkind, but short of this there is no way to insure
					// the UI is up-to-date with the build results
					monitor.subTask(ManagedMakeMessages.getResourceString(REFRESH));
					try {
						//currentProject.refreshLocal(IResource.DEPTH_INFINITE, null);

						// use the refresh scope manager to refresh
						RefreshScopeManager refreshManager = RefreshScopeManager.getInstance();
						IWorkspaceRunnable runnable = refreshManager.getRefreshRunnable(project, cfg.getName());
						ResourcesPlugin.getWorkspace().run(runnable, null, IWorkspace.AVOID_UPDATE, null);
					} catch (CoreException e) {
						monitor.subTask(ManagedMakeMessages.getResourceString(REFRESH_ERROR));
					}
				} else {
					errMsg = launcher.getErrorMessage();
				}

				// Report either the success or failure of our mission
				buf = new StringBuilder();
				if (errMsg != null && errMsg.length() > 0) {
					buf.append(errMsg).append(NEWLINE);
				} else {
					// Report a successful build
					String successMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, project.getName());
					buf.append(successMsg).append(NEWLINE);
				}

				// Write message on the console
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
				epmOutputStream.close();

				// Generate any error markers that the build has discovered
				monitor.subTask(ManagedMakeMessages.getResourceString(MARKERS));
				addBuilderMarkers(epm);
				consoleOutStream.close();
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
	@SuppressWarnings("restriction")
	private void removeAllMarkers(IProject project) {
		if (project == null || !project.isAccessible())
			return;

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
				FixitManager.getInstance().deleteMarkers(markers);
			} catch (CoreException e) {
				// The only situation that might cause this is some sort of resource change event
				return;
			}
		}
	}

	/**
	 * called to invoke the MBS Internal Builder for building the given configuration
	 *
	 * @param cfg configuration to be built
	 * @param buildIncrementaly if true, incremental build will be performed,
	 * only files that need rebuild will be built.
	 * If false, full rebuild will be performed
	 * @param resumeOnErr if true, build will continue in case of error while building.
	 * If false the build will stop on the first error
	 * @param monitor monitor
	 */
	protected void invokeInternalBuilder(IConfiguration cfg, boolean buildIncrementaly, boolean resumeOnErr,
			IProgressMonitor monitor) {

		boolean isParallel = ((Configuration) cfg).getParallelDef();

		// Get the project and make sure there's a monitor to cancel the build
		IProject currentProject = cfg.getOwner().getProject();
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		String[] msgs = new String[2];
		msgs[0] = ManagedMakeMessages.getResourceString(INTERNAL_BUILDER);
		msgs[1] = currentProject.getName();

		ConsoleOutputStream consoleOutStream = null;
		IConsole console = null;
		OutputStream epmOutputStream = null;
		try {
			int flags = 0;
			IResourceDelta delta = null;

			if (buildIncrementaly) {
				flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED
						| BuildDescriptionManager.DEPS;
				delta = getDelta(currentProject);
			}

			IBuildDescription des = BuildDescriptionManager.createBuildDescription(cfg, delta, flags);

			DescriptionBuilder builder = null;
			if (!isParallel)
				builder = new DescriptionBuilder(des, buildIncrementaly, resumeOnErr, null);

			// Get a build console for the project
			StringBuilder buf = new StringBuilder();
			console = CCorePlugin.getDefault().getConsole();
			console.start(currentProject);
			consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];
			if (buildIncrementaly)
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
			else
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_REBUILD);

			consoleHeader[1] = cfg.getName();
			consoleHeader[2] = currentProject.getName();
			buf.append(NEWLINE);
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader)).append(NEWLINE);
			buf.append(NEWLINE);

			buf.append(ManagedMakeMessages.getResourceString(INTERNAL_BUILDER_HEADER_NOTE));
			buf.append("\n"); //$NON-NLS-1$

			if (!cfg.isSupported()) {
				String msg = ManagedMakeMessages.getFormattedString(WARNING_UNSUPPORTED_CONFIGURATION,
						new String[] { cfg.getName(), cfg.getToolChain().getName() });
				buf.append(msg).append(NEWLINE);
				buf.append(NEWLINE);
			}
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();

			if (isParallel || builder.getNumCommands() > 0) {
				// Remove all markers for this project
				removeAllMarkers(currentProject);

				// Hook up an error parser manager
				String[] errorParsers = cfg.getErrorParserList();
				ErrorParserManager epm = new ErrorParserManager(getProject(), des.getDefaultBuildDirLocationURI(), this,
						errorParsers);
				epm.setOutputStream(consoleOutStream);
				// This variable is necessary to ensure that the EPM stream stay open
				// until we explicitly close it. See bug#123302.
				epmOutputStream = epm.getOutputStream();

				int status = 0;

				long t1 = System.currentTimeMillis();
				if (isParallel)
					status = ParallelBuilder.build(des, null, null, epmOutputStream, epmOutputStream, monitor,
							resumeOnErr, buildIncrementaly);
				else
					status = builder.build(epmOutputStream, epmOutputStream, monitor);
				long t2 = System.currentTimeMillis();

				// Report either the success or failure of our mission
				buf = new StringBuilder();

				switch (status) {
				case IBuildModelBuilder.STATUS_OK:
					buf.append(ManagedMakeMessages.getFormattedString(BUILD_FINISHED, currentProject.getName()));
					break;
				case IBuildModelBuilder.STATUS_CANCELLED:
					buf.append(ManagedMakeMessages.getResourceString(BUILD_CANCELLED));
					break;
				case IBuildModelBuilder.STATUS_ERROR_BUILD:
					String msg = resumeOnErr ? ManagedMakeMessages.getResourceString(BUILD_FINISHED_WITH_ERRS)
							: ManagedMakeMessages.getResourceString(BUILD_STOPPED_ERR);
					buf.append(msg);
					break;
				case IBuildModelBuilder.STATUS_ERROR_LAUNCH:
				default:
					buf.append(ManagedMakeMessages.getResourceString(BUILD_FAILED_ERR));
					break;
				}
				buf.append(NEWLINE);

				// Report time and number of threads used
				//				buf.append("Time consumed: ");
				//				buf.append(t2 - t1);
				//				buf.append(" ms.  ");
				//				if (isParallel) {
				//					buf.append("Parallel threads used: ");
				//					buf.append(ParallelBuilder.lastThreadsUsed);
				//				}
				//				buf.append("\n");

				// Report time and number of threads used
				buf.append(
						ManagedMakeMessages.getFormattedString("CommonBuilder.6", Integer.toString((int) (t2 - t1)))); //$NON-NLS-1$
				//				buf.append(t2 - t1);
				//				buf.append(" ms.  ");
				if (isParallel) {
					buf.append(ManagedMakeMessages.getFormattedString("CommonBuilder.7", //$NON-NLS-1$
							Integer.toString(ParallelBuilder.lastThreadsUsed)));
					//					buf.append(ParallelBuilder.lastThreadsUsed);
				}
				buf.append(NEWLINE);

				// Write message on the console
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
				epmOutputStream.close();
				epmOutputStream = null;
				// Generate any error markers that the build has discovered
				monitor.subTask(ManagedMakeMessages.getResourceString(MARKERS));
				addBuilderMarkers(epm);
			} else {
				buf = new StringBuilder();
				buf.append(ManagedMakeMessages.getFormattedString(NOTHING_BUILT, getProject().getName()))
						.append(NEWLINE);
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
			}

		} catch (Exception e) {
			if (consoleOutStream != null) {
				StringBuilder buf = new StringBuilder();
				String errorDesc = ManagedMakeMessages.getResourceString(BUILD_ERROR);
				buf.append(errorDesc).append(NEWLINE);
				buf.append("(").append(e.getLocalizedMessage()).append(")").append(NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$

				try {
					consoleOutStream.write(buf.toString().getBytes());
					consoleOutStream.flush();
				} catch (IOException e1) {
				}
			}
			forgetLastBuiltState();
		} finally {
			if (epmOutputStream != null) {
				try {
					epmOutputStream.close();
				} catch (IOException e) {
				}
			}
			if (consoleOutStream != null) {
				try {
					consoleOutStream.close();
				} catch (IOException e) {
				}
			}
			getGenerationProblems().clear();
			monitor.done();
		}
	}

	private Map<IProject, List<IFile>> arrangeFilesByProject(List<IFile> files) {
		Map<IProject, List<IFile>> projectMap = new HashMap<>();
		for (IFile file : files) {
			IProject project = file.getProject();
			List<IFile> filesInProject = projectMap.get(project);
			if (filesInProject == null) {
				filesInProject = new ArrayList<>();
				projectMap.put(project, filesInProject);
			}
			filesInProject.add(file);
		}
		return projectMap;
	}

	/**
	 * Called to invoke the MBS Internal Builder for building the given resources
	 *
	 * @param files - list of files to build.
	 * @param monitor - progress monitor to report progress to user.
	 * @return status of the operation. Can be {@link Status#OK_STATUS} or
	 *    {@link Status#CANCEL_STATUS}.
	 */
	public IStatus invokeInternalBuilder(List<IFile> files, IProgressMonitor monitor) {
		// Make sure there's a monitor to cancel the build
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			Map<IProject, List<IFile>> projectMap = arrangeFilesByProject(files);
			monitor.beginTask("", projectMap.size() * PROGRESS_MONITOR_SCALE); //$NON-NLS-1$

			for (List<IFile> filesInProject : projectMap.values()) {
				IProject project = filesInProject.get(0).getProject();
				setCurrentProject(project);
				monitor.subTask(ManagedMakeMessages.getFormattedString("GeneratedMakefileBuilder.buildingProject", //$NON-NLS-1$
						project.getName()));
				invokeInternalBuilderForOneProject(filesInProject, new SubProgressMonitor(monitor,
						1 * PROGRESS_MONITOR_SCALE, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			}
		} finally {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	private void invokeInternalBuilderForOneProject(List<IFile> files, IProgressMonitor monitor) {
		IProject project = files.get(0).getProject();
		BuildRunnerHelper buildRunnerHelper = new BuildRunnerHelper(project);

		try {
			SubMonitor progress = SubMonitor.convert(monitor,
					ManagedMakeMessages.getFormattedString("GeneratedMakefileBuilder.buildingProject", //$NON-NLS-1$
							project.getName()) + ':',
					TICKS_STREAM_PROGRESS_MONITOR + files.size() * PROGRESS_MONITOR_SCALE);

			// Get a build console for the project
			console = CCorePlugin.getDefault().getConsole();
			console.start(project);

			IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
			IConfiguration configuration = buildInfo.getDefaultConfiguration();

			String cfgName = configuration.getName();
			String toolchainName = configuration.getToolChain().getName();
			boolean isSupported = configuration.isSupported();

			IBuildDescription des = BuildDescriptionManager.createBuildDescription(configuration, null, 0);

			String[] errorParsers = configuration.getErrorParserList();
			ErrorParserManager epm = new ErrorParserManager(project, des.getDefaultBuildDirLocationURI(), this,
					errorParsers);

			buildRunnerHelper.prepareStreams(epm, null, console, progress.split(TICKS_STREAM_PROGRESS_MONITOR));
			OutputStream stdout = buildRunnerHelper.getOutputStream();
			OutputStream stderr = buildRunnerHelper.getErrorStream();

			buildRunnerHelper.greeting(
					ManagedMakeMessages.getResourceString("GeneratedMakefileBuilder.buildingSelectedFiles"), cfgName, //$NON-NLS-1$
					toolchainName, isSupported);
			buildRunnerHelper.printLine(
					ManagedMakeMessages.getResourceString("ManagedMakeBuilder.message.internal.builder.header.note")); //$NON-NLS-1$

			// Build artifacts for each file in the project
			for (IFile file : files) {
				if (progress.isCanceled()) {
					break;
				}
				String filePath = file.getProjectRelativePath().toString();

				try {
					IBuildResource buildResource = des.getBuildResource(file);

					Set<IBuildStep> dependentSteps = new HashSet<>();
					IBuildIOType depTypes[] = buildResource.getDependentIOTypes();
					for (IBuildIOType btype : depTypes) {
						if (btype != null && btype.getStep() != null)
							dependentSteps.add(btype.getStep());
					}

					SubMonitor monitor2 = progress.split(1 * PROGRESS_MONITOR_SCALE);
					try {
						SubMonitor monitor3 = monitor2
								.split(TICKS_DELETE_MARKERS + dependentSteps.size() * PROGRESS_MONITOR_SCALE); //$NON-NLS-1

						// Remove problem markers for the file
						monitor3.subTask(ManagedMakeMessages
								.getFormattedString("GeneratedMakefileBuilder.removingResourceMarkers", filePath)); //$NON-NLS-1$
						buildRunnerHelper.removeOldMarkers(file, monitor3.split(TICKS_DELETE_MARKERS));

						// Build dependent steps
						for (IBuildStep step : dependentSteps) {
							if (monitor3.isCanceled()) {
								break;
							}

							monitor3.subTask(filePath);
							StepBuilder stepBuilder = new StepBuilder(step, null);
							stepBuilder.build(stdout, stderr, monitor3.split(1 * PROGRESS_MONITOR_SCALE));

							monitor3.subTask(ManagedMakeMessages
									.getFormattedString("GeneratedMakefileBuilder.refreshingArtifacts", filePath)); //$NON-NLS-1$
							IBuildIOType[] outputIOTypes = step.getOutputIOTypes();
							for (IBuildIOType type : outputIOTypes) {
								for (IBuildResource outResource : type.getResources()) {
									IFile outFile = project.getFile(outResource.getLocation());
									// Refresh the output resource without allowing the user to cancel.
									outFile.refreshLocal(IResource.DEPTH_INFINITE, null);
								}
							}
						}
					} finally {
						monitor2.done();
					}

				} catch (Exception e) {
					ManagedBuilderCorePlugin.log(new CoreException(
							new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "CDT Build Error", e))); //$NON-NLS-1$
				}

			}
			buildRunnerHelper.close();
			buildRunnerHelper.goodbye();

		} catch (Exception e) {
			ManagedBuilderCorePlugin.log(new CoreException(
					new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "CDT Build Error", e))); //$NON-NLS-1$
			forgetLastBuiltState();
		} finally {
			getGenerationProblems().clear();
			try {
				buildRunnerHelper.close();
			} catch (IOException e) {
				ManagedBuilderCorePlugin.log(new CoreException(
						new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "CDT Build Error", e))); //$NON-NLS-1$
			}
			monitor.done();
		}
	}

	public IStatus cleanFiles(List<IFile> files, IProgressMonitor monitor) {
		// Make sure there's a monitor to cancel the build
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			Map<IProject, List<IFile>> projectMap = arrangeFilesByProject(files);
			monitor.beginTask("", projectMap.size() * PROGRESS_MONITOR_SCALE); //$NON-NLS-1$

			for (List<IFile> filesInProject : projectMap.values()) {
				IProject project = filesInProject.get(0).getProject();
				monitor.subTask(ManagedMakeMessages.getFormattedString("GeneratedMakefileBuilder.cleaningProject", //$NON-NLS-1$
						project.getName()));
				cleanFilesForOneProject(filesInProject, new SubProgressMonitor(monitor, 1 * PROGRESS_MONITOR_SCALE,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			}
		} finally {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	public void cleanFilesForOneProject(List<IFile> files, IProgressMonitor monitor) {
		IProject project = files.get(0).getProject();
		BuildRunnerHelper buildRunnerHelper = new BuildRunnerHelper(project);
		int countDeleted = 0;

		try {
			SubMonitor progress = SubMonitor.convert(monitor,
					ManagedMakeMessages.getFormattedString("GeneratedMakefileBuilder.cleaningProject", //$NON-NLS-1$
							project.getName()) + ':',
					TICKS_STREAM_PROGRESS_MONITOR + files.size() * PROGRESS_MONITOR_SCALE);

			// Get a build console for the project
			console = CCorePlugin.getDefault().getConsole();
			console.start(project);

			IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
			IConfiguration configuration = buildInfo.getDefaultConfiguration();

			String cfgName = configuration.getName();
			String toolchainName = configuration.getToolChain().getName();
			boolean isSupported = configuration.isSupported();

			int flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED;
			IResourceDelta delta = getDelta(project);

			IBuildDescription des = BuildDescriptionManager.createBuildDescription(configuration, delta, flags);

			String[] errorParsers = configuration.getErrorParserList();
			ErrorParserManager epm = new ErrorParserManager(project, des.getDefaultBuildDirLocationURI(), this,
					errorParsers);
			buildRunnerHelper.prepareStreams(epm, null, console, progress.split(TICKS_STREAM_PROGRESS_MONITOR));

			buildRunnerHelper.greeting(
					ManagedMakeMessages.getResourceString("GeneratedMakefileBuilder.cleanSelectedFiles"), cfgName, //$NON-NLS-1$
					toolchainName, isSupported);
			buildRunnerHelper.printLine(
					ManagedMakeMessages.getResourceString("ManagedMakeBuilder.message.internal.builder.header.note")); //$NON-NLS-1$

			for (IFile file : files) {
				if (progress.isCanceled()) {
					break;
				}
				String filePath = file.getProjectRelativePath().toString();

				try {
					IBuildResource buildResource = des.getBuildResource(file);
					if (buildResource != null) {
						Set<IBuildStep> dependentSteps = new HashSet<>();
						IBuildIOType depTypes[] = buildResource.getDependentIOTypes();
						for (IBuildIOType btype : depTypes) {
							if (btype != null && btype.getStep() != null)
								dependentSteps.add(btype.getStep());
						}

						SubMonitor monitor2 = progress.split(1 * PROGRESS_MONITOR_SCALE);
						try {
							SubMonitor monitor3 = monitor2
									.split(TICKS_DELETE_MARKERS + dependentSteps.size() * PROGRESS_MONITOR_SCALE);

							// Remove problem markers for the file
							monitor3.subTask(ManagedMakeMessages
									.getFormattedString("GeneratedMakefileBuilder.removingResourceMarkers", filePath)); //$NON-NLS-1$
							buildRunnerHelper.removeOldMarkers(file, monitor3.split(TICKS_DELETE_MARKERS));

							// iterate through all build steps
							for (IBuildStep step : dependentSteps) {
								if (monitor3.isCanceled()) {
									break;
								}

								monitor3.subTask(filePath);
								// Delete the output resources
								IBuildIOType[] outputIOTypes = step.getOutputIOTypes();

								for (IBuildIOType ioType : outputIOTypes) {
									for (IBuildResource rc : ioType.getResources()) {
										IResource outputFile = project
												.findMember(rc.getFullPath().removeFirstSegments(1)); // strip project name
										if (outputFile != null) {
											outputFile.delete(true, null);
											countDeleted++;
											buildRunnerHelper.printLine(ManagedMakeMessages.getFormattedString(
													"GeneratedMakefileBuilder.fileDeleted", //$NON-NLS-1$
													outputFile.getProjectRelativePath().toString()));
										}
									}
								}

								monitor3.worked(1 * PROGRESS_MONITOR_SCALE);
							}
						} finally {
							monitor2.done();
						}
					}
				} catch (Exception e) {
					ManagedBuilderCorePlugin.log(new CoreException(
							new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "CDT Build Error", e))); //$NON-NLS-1$
				}

			}
			if (countDeleted == 0) {
				buildRunnerHelper
						.printLine(ManagedMakeMessages.getResourceString("GeneratedMakefileBuilder.nothingToClean")); //$NON-NLS-1$
			}
			buildRunnerHelper.close();
			buildRunnerHelper.goodbye();

		} catch (Exception e) {
			ManagedBuilderCorePlugin.log(new CoreException(
					new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "CDT Build Error", e))); //$NON-NLS-1$
		} finally {
			try {
				buildRunnerHelper.close();
			} catch (IOException e) {
				ManagedBuilderCorePlugin.log(new CoreException(
						new Status(IStatus.ERROR, ManagedBuilderCorePlugin.PLUGIN_ID, "CDT Build Error", e))); //$NON-NLS-1$
			}
			monitor.done();
		}
	}
}
