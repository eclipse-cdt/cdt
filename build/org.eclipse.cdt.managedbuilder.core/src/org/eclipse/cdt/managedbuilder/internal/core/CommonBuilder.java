/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.util.ListComparator;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
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
import org.eclipse.cdt.managedbuilder.internal.buildmodel.DescriptionBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IBuildModelBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.ParallelBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.StepBuilder;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator2;
import org.eclipse.cdt.newmake.core.IMakeBuilderInfo;
import org.eclipse.cdt.newmake.internal.core.MakeMessages;
import org.eclipse.cdt.newmake.internal.core.StreamMonitor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

public class CommonBuilder extends ACBuilder {

	public final static String BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".genmakebuilder"; //$NON-NLS-1$
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
	private static final String TYPE_INC = "ManagedMakeBuider.type.incremental";	//$NON-NLS-1$
	private static final String WARNING_UNSUPPORTED_CONFIGURATION = "ManagedMakeBuilder.warning.unsupported.configuration";	//$NON-NLS-1$
	private static final String BUILD_CANCELLED = "ManagedMakeBuilder.message.cancelled";	//$NON-NLS-1$
	private static final String BUILD_FINISHED_WITH_ERRS = "ManagedMakeBuilder.message.finished.with.errs";	//$NON-NLS-1$
	private static final String BUILD_FAILED_ERR = "ManagedMakeBuilder.message.internal.builder.error";	//$NON-NLS-1$
	private static final String BUILD_STOPPED_ERR = "ManagedMakeBuilder.message.stopped.error";	//$NON-NLS-1$
	private static final String INTERNAL_BUILDER_HEADER_NOTE = "ManagedMakeBuilder.message.internal.builder.header.note";	//$NON-NLS-1$
	private static final String TYPE_REBUILD = "ManagedMakeBuider.type.rebuild";	//$NON-NLS-1$
	private static final String INTERNAL_BUILDER = "ManagedMakeBuilder.message.internal.builder";	//$NON-NLS-1$
	public static boolean VERBOSE = false;


	public CommonBuilder() {
	}
	
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

	public class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		private String buildGoalName;
		private IProject project; 
		private IConfiguration cfg;
		private IConfiguration allConfigs[];
		private IPath buildPaths[];
		private boolean incrBuildNeeded = false;
		private boolean fullBuildNeeded = false;
		private List reservedNames;
		
		/**
		 * 
		 */
		public ResourceDeltaVisitor(IConfiguration cfg, IConfiguration allConfigs[]) {
			this.cfg = cfg;
			this.project = cfg.getOwner().getProject();
			this.allConfigs = allConfigs;
			buildPaths = new IPath[allConfigs.length];
			for(int i = 0; i < buildPaths.length; i++){
				buildPaths[i] = ManagedBuildManager.getBuildFullPath(allConfigs[i], allConfigs[i].getBuilder());
			}
			String ext = cfg.getArtifactExtension();
			//try to resolve build macros in the build artifact extension
			try{
				ext = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						ext,
						"", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION,
						cfg);
			} catch (BuildMacroException e){
			}
			
			String name = cfg.getArtifactName();
			//try to resolve build macros in the build artifact name
			try{
				String resolved = ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(
						name,
						"", //$NON-NLS-1$
						" ", //$NON-NLS-1$
						IBuildMacroProvider.CONTEXT_CONFIGURATION,
						cfg);
				if((resolved = resolved.trim()).length() > 0)
					name = resolved;
			} catch (BuildMacroException e){
			}

			if (ext.length() > 0) {
				buildGoalName = cfg.getOutputPrefix(ext) + name + IManagedBuilderMakefileGenerator.DOT + ext;
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
			IPath path = resource.getFullPath();
			for (int i = 0; i < buildPaths.length; i++) {
				if(buildPaths[i] != null && buildPaths[i].isPrefixOf(path)){
					return true;
				}
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
			if (resource != null && resource.getProject() == project) {
				switch(resource.getType()){
				case IResource.FILE:
					String name = resource.getName();						
					if ((!name.equals(buildGoalName) &&
						// TODO:  Also need to check for secondary outputs
						(resource.isDerived() || 
						(isProjectFile(resource)) ||
						(isGeneratedResource(resource))))) {
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
		IBuilder builders[];
		IPath buildFullPaths[];
//		IConfiguration buildConfigs[];
		IConfiguration allConfigs[];
		Configuration otherConfigs[];
		int resourceChangeState;
		
		private static final IPath[] ignoreList = {
			new Path(".cdtproject"), //$NON-NLS-1$
			new Path(".cproject"), //$NON-NLS-1$
			new Path(".cdtbuild"), //$NON-NLS-1$
			new Path(".settings"), //$NON-NLS-1$
		};

		OtherConfigVerifier(IBuilder builders[], IConfiguration allCfgs[]){
			this.builders = builders;
			allConfigs = allCfgs;
			Set buildCfgSet = new HashSet();
			for(int i = 0; i < builders.length; i++){
				buildCfgSet.add(builders[i].getParent().getParent());
			}
			List othersList = ListComparator.getAdded(allCfgs, buildCfgSet.toArray());
			if(othersList != null)
				otherConfigs = (Configuration[])othersList.toArray(new Configuration[othersList.size()]);
			else
				otherConfigs = new Configuration[0];
			
			List list = new ArrayList(builders.length);
//			buildFullPaths = new IPath[builders.length];
			for(int i = 0; i < builders.length; i++){
				IPath path = ManagedBuildManager.getBuildFullPath(builders[i].getParent().getParent(), builders[i]);
				if(path != null)
					list.add(path);
//				buildFullPaths[i] = ManagedBuildManager.getBuildFullPath(builders[i].getParent().getParent(), builders[i]);
			}
			buildFullPaths = (IPath[])list.toArray(new IPath[list.size()]);
			
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			
			IResource rc = delta.getResource();
			if(rc.getType() == IResource.FILE){
				if(isResourceValuable(rc))
					resourceChangeState |= delta.getKind();
				return false;
			}
			
			if(!isResourceValuable(rc))
				return false;
			for(int i = 0; i < buildFullPaths.length; i++){
				if(buildFullPaths[i].isPrefixOf(rc.getFullPath()))
					return false;
			}
			return true;
		}
		
		public void updateOtherConfigs(IResourceDelta delta){
			if(delta == null)
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
		
		private void setResourceChangeStateForOtherConfigs(){
			for(int i = 0; i < otherConfigs.length; i++){
				otherConfigs[i].addResourceChangeState(resourceChangeState);
			}
		}
	
		private boolean isResourceValuable(IResource rc){
			IPath path = rc.getProjectRelativePath();
			for(int i = 0; i < ignoreList.length; i++){
				if(ignoreList[i].equals(path))
					return false;
			}
			return true;
		}
	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		IBuilder builders[] = BuilderFactory.createBuilders(getProject(), args);
		int num = builders.length;
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		IConfiguration activeCfg = info.getDefaultConfiguration();
		if(num != 0){
			
			MultiStatus status = checkBuilders(builders, activeCfg);
			if(status.getSeverity() != IStatus.OK)
				throw new CoreException(status);

			IConfiguration cfgs[] = getReferencedConfigs(builders);

			monitor.beginTask("", num + cfgs.length);

			if(cfgs.length != 0)
				ManagedBuildManager.buildConfigurations(cfgs, new SubProgressMonitor(monitor, 1));

			for(int i = 0; i < num; i++){
				build(kind, builders[i], info, new SubProgressMonitor(monitor, 1));
			}
		}
		
		updateOtherConfigs(builders, kind);
		
		monitor.done();
		return getProject().getReferencedProjects();
	}
	
	private IConfiguration[] getReferencedConfigs(IBuilder[] builders){
		Set set = new HashSet();
		for(int i = 0; i < builders.length; i++){
			IConfiguration cfg = builders[i].getParent().getParent();
			IConfiguration refs[] = ManagedBuildManager.getReferencedConfigurations(cfg);
			for(int k = 0; k < refs.length; k++){
				set.add(refs[k]);
			}
		}
		return (IConfiguration[]) set.toArray(new Configuration[set.size()]);
	}
	
	protected MultiStatus checkBuilders(IBuilder builders[], IConfiguration activeCfg){
		MultiStatus status = null;
		for(int i = 0; i < builders.length; i++){
			IBuilder builder = builders[i];
			boolean supportsCustomization = builder.supportsCustomizedBuild();
			boolean isManagedBuildOn = builder.isManagedBuildOn();
			if(isManagedBuildOn && !supportsCustomization){
				if(builder.isCustomBuilder()){
					if(status == null){
						status = new MultiStatus(
								ManagedBuilderCorePlugin.getUniqueIdentifier(),
								IStatus.ERROR,
								new String(),
								null);
					}

					status.add(new Status (
							IStatus.ERROR,
							ManagedBuilderCorePlugin.getUniqueIdentifier(),
							0,
							"customized builder created for builder that does not support customization",
							null));

				}
				else if(builder.getParent().getParent() != activeCfg){
					if(status == null){
						status = new MultiStatus(
								ManagedBuilderCorePlugin.getUniqueIdentifier(),
								IStatus.ERROR,
								new String(),
								null);
					}
					
					status.add(new Status (
							IStatus.ERROR,
							ManagedBuilderCorePlugin.getUniqueIdentifier(),
							0,
							"request for building non active configuration for the builder that does not support this",
							null));

				}
			}
		}
		
		if(status == null){
			status = new MultiStatus(
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					IStatus.OK,
					new String(),
					null);
		}
		
		return status;
	}
	
	private void updateOtherConfigs(IBuilder builders[], int buildKind){
		//IProject project 
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		IConfiguration allCfgs[] = info.getManagedProject().getConfigurations();
		new OtherConfigVerifier(builders, allCfgs).updateOtherConfigs(buildKind == FULL_BUILD ? null : getDelta(getProject()));
	}

	protected class BuildStatus {
		private boolean fManagedBuildOn;
		private boolean fRebuild;
		private boolean fBuild = true;
		private List fConsoleMessages = new ArrayList();
		private IManagedBuilderMakefileGenerator fMakeGen;
		
		public BuildStatus(IBuilder builder){
			fManagedBuildOn = builder.isManagedBuildOn();
		}
		
		public void setRebuild(){
			fRebuild = true;
		}
		
		public boolean isRebuild(){
			return fRebuild;
		}
		
		public boolean isManagedBuildOn(){
			return fManagedBuildOn;
		}
		
		public boolean isBuild(){
			return fBuild;
		}
		
		public void cancelBuild(){
			fBuild = false;
		}
		
		public List getConsoleMessagesList(){
			return fConsoleMessages;
		}
		
		public IManagedBuilderMakefileGenerator getMakeGen(){
			return fMakeGen;
		}
		
		public void setMakeGen(IManagedBuilderMakefileGenerator makeGen){
			fMakeGen = makeGen;
		}
	}
	
	protected void build(int kind, IBuilder builder, IManagedBuildInfo info, IProgressMonitor monitor) throws CoreException{
		BuildStatus status = new BuildStatus(builder);
		if (!shouldBuild(kind, builder)) {
			return;
		}
//		if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
//			IResourceDelta delta = getDelta(getProject());
//			if (delta != null) {
//				IResource res = delta.getResource();
//				if (res != null) {
//					bPerformBuild = res.getProject().equals(getProject());
//				}
//			} else {
//				bPerformBuild = false;
//			}
//		}
		
		if (status.isBuild()) {
//			IManagedBuilderMakefileGenerator makeGen = null;
			if(status.isManagedBuildOn()){
				status = performPrebuildGeneration(kind, builder, info, status, monitor);
			}

			IConfiguration cfg = builder.getParent().getParent();
			
			if(status.isBuild()){
				try {
				boolean isClean = invokeBuilder(kind, builder, monitor);
					if (isClean) {
						forgetLastBuiltState();
						cfg.setRebuildState(true);
					} else {
						if(status.isManagedBuildOn()){
							performPostbuildGeneration(kind, builder, info, status, monitor);
						}
						cfg.setRebuildState(false);
					}
				} catch(CoreException e){
					cfg.setRebuildState(true);
					throw e;
				}
			} else if(status.getConsoleMessagesList().size() != 0) {
				emitMessage(concatMessages(status.getConsoleMessagesList()));
			}
		}
		checkCancel(monitor);
	}
	
	
	
	private String concatMessages(List msgs){
		int size = msgs.size();
		if(size == 0){
			return "";
		} else if(size == 1){
			return (String)msgs.get(0);
		} 
		
		StringBuffer buf = new StringBuffer();
		buf.append(msgs.get(0));
		for(int i = 1; i < size; i++){
			buf.append("\n");
			buf.append((String)msgs.get(i));
		}
		return buf.toString(); 
	}
	
	/* (non-javadoc)
	 * Emits a message to the console indicating that there were no source files to build
	 * @param buildType
	 * @param status
	 * @param configName
	 */
	private String createNoSourceMessage(int buildType, IStatus status, String configName) throws CoreException {
		StringBuffer buf = new StringBuffer();
		String[] consoleHeader = new String[3];
		if (buildType == FULL_BUILD || buildType == INCREMENTAL_BUILD) {
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
		return buf.toString();
	}

	private void emitMessage(String msg) throws CoreException {
		try {
			IConsole console = getConsole();
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			// Report a successful clean
			consoleOutStream.write(msg.getBytes());
			consoleOutStream.flush();
			consoleOutStream.close();
		} catch (CoreException e) {
			// Throw the exception back to the builder
			throw e;
		} catch (IOException io) {	//  Ignore console failures...
			throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(),
					io.getLocalizedMessage(), io));
		}		
	}
	
	private IConsole getConsole(){
		IConsole console = CCorePlugin.getDefault().getConsole();
		console.start(getProject());
		return console;
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
	protected boolean invokeInternalBuilder(int kind, IBuilder builder,
			IProgressMonitor monitor) {
		
		IConfiguration cfg = builder.getParent().getParent();
		boolean isParallel = builder.getParallelizationNum() != 0;
//		boolean buildIncrementaly = true;
		boolean resumeOnErr = !builder.isStopOnError();
		
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
			IResourceDelta delta = getDelta(currentProject);
			
			if(delta != null){
				flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED | BuildDescriptionManager.DEPS;
//				delta = getDelta(currentProject);
			}
			
			boolean buildIncrementaly = delta != null;
			
			IBuildDescription des = BuildDescriptionManager.createBuildDescription(cfg, delta, flags);
	
			DescriptionBuilder dBuilder = null;
			if (!isParallel)
				dBuilder = new DescriptionBuilder(des, buildIncrementaly, resumeOnErr);

			// Get a build console for the project
			StringBuffer buf = new StringBuffer();
			console = CCorePlugin.getDefault().getConsole();
			console.start(currentProject);
			consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];
			if(buildIncrementaly)
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
			else
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_REBUILD);
						
			consoleHeader[1] = cfg.getName();
			consoleHeader[2] = currentProject.getName();
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader));
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			
			buf.append(ManagedMakeMessages.getResourceString(INTERNAL_BUILDER_HEADER_NOTE));
			buf.append("\n"); //$NON-NLS-1$
			
			if(!cfg.isSupported()){
				buf.append(ManagedMakeMessages.getFormattedString(WARNING_UNSUPPORTED_CONFIGURATION,new String[] {cfg.getName(),cfg.getToolChain().getName()}));
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			}
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			
			if(isParallel || dBuilder.getNumCommands() > 0) {
				// Remove all markers for this project
				removeAllMarkers(currentProject);
				
				// Hook up an error parser manager
				String[] errorParsers = builder.getErrorParsers(); 
				ErrorParserManager epm = new ErrorParserManager(getProject(), des.getDefaultBuildDirLocation(), this, errorParsers);
				epm.setOutputStream(consoleOutStream);
				// This variable is necessary to ensure that the EPM stream stay open
				// until we explicitly close it. See bug#123302.
				epmOutputStream = epm.getOutputStream();
				
				int status = 0;
				
				long t1 = System.currentTimeMillis();
				if (isParallel)
					status = ParallelBuilder.build(des, null, null, epmOutputStream, epmOutputStream, monitor, resumeOnErr, buildIncrementaly);
				else
				    status = dBuilder.build(epmOutputStream, epmOutputStream, monitor);
				long t2 = System.currentTimeMillis();
				
				// Report either the success or failure of our mission 
				buf = new StringBuffer();
				
				switch(status){
				case IBuildModelBuilder.STATUS_OK:
					buf.append(ManagedMakeMessages
							.getFormattedString(BUILD_FINISHED,
									currentProject.getName()));
					break;
				case IBuildModelBuilder.STATUS_CANCELLED:
					buf.append(ManagedMakeMessages
							.getResourceString(BUILD_CANCELLED));
					break;
				case IBuildModelBuilder.STATUS_ERROR_BUILD:
					String msg = resumeOnErr ? 
							ManagedMakeMessages.getResourceString(BUILD_FINISHED_WITH_ERRS) :
								ManagedMakeMessages.getResourceString(BUILD_STOPPED_ERR);
					buf.append(msg);
					break;
				case IBuildModelBuilder.STATUS_ERROR_LAUNCH:
				default:
					buf.append(ManagedMakeMessages
							.getResourceString(BUILD_FAILED_ERR));
					break;
				}
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				
				// Report time and number of threads used
				buf.append("Time consumed: ");
				buf.append(t2 - t1);
				buf.append(" ms.  ");
				if (isParallel) {
					buf.append("Parallel threads used: ");
					buf.append(ParallelBuilder.lastThreadsUsed);
				}
				buf.append("\n");
				// Write message on the console 
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
				epmOutputStream.close();
				epmOutputStream = null;
				// Generate any error markers that the build has discovered 
				monitor.subTask(ManagedMakeMessages
						.getResourceString(MARKERS));
//TODO:				addBuilderMarkers(epm);
				epm.reportProblems();
			} else {
				buf = new StringBuffer();
				buf.append(ManagedMakeMessages.getFormattedString(NOTHING_BUILT, getProject().getName()));
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
			}

		} catch (Exception e) {
			if(consoleOutStream != null){
				StringBuffer buf = new StringBuffer();
				String errorDesc = ManagedMakeMessages
							.getResourceString(BUILD_ERROR);
				buf.append(errorDesc);
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append("(").append(e.getLocalizedMessage()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$

				try {
					consoleOutStream.write(buf.toString().getBytes());
					consoleOutStream.flush();
				} catch (IOException e1) {
				}
			}
			forgetLastBuiltState();
		} finally {
			if(epmOutputStream != null){
				try {
					epmOutputStream.close();
				} catch (IOException e) {
				} 
			}
			if(consoleOutStream != null){
				try {
					consoleOutStream.close();
				} catch (IOException e) {
				}
			}
//			getGenerationProblems().clear();
			monitor.done();
		}
		return false;
	}
	
	protected String[] calcEnvironment(IBuilder builder) throws CoreException{
		HashMap envMap = new HashMap();
		if (builder.appendEnvironment()) {
			ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(builder.getParent().getParent());
			IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IEnvironmentVariable[] vars = mngr.getVariables(cfgDes, true);
			for(int i = 0; i < vars.length; i++){
				envMap.put(vars[i].getName(), vars[i].getValue());
			}
		}
		// Add variables from build info
		Map builderEnv = builder.getExpandedEnvironment();
		if(builderEnv != null)
			envMap.putAll(builderEnv);
		Iterator iter = envMap.entrySet().iterator();
		List strings= new ArrayList(envMap.size());
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			StringBuffer buffer= new StringBuffer((String) entry.getKey());
			buffer.append('=').append((String) entry.getValue());
			strings.add(buffer.toString());
		}
		return (String[]) strings.toArray(new String[strings.size()]);
	}
	
	
	/**
	 * Called to invoke the MBS Internal Builder for building the given resources in
	 * the given configuration
	 * 
	 * This method is considered experimental.  Clients implementing this API should expect
	 * possible changes in the API.
	 *  
	 * @param cfg configuration to be built
	 * @param buildIncrementaly if true, incremental build will be performed,
	 * only files that need rebuild will be built.
	 * If false, full rebuild will be performed
	 * @param resumeOnErr if true, build will continue in case of error while building.
	 * If false the build will stop on the first error 
	 * @param monitor Progress monitor.  For every resource built this monitor will consume one unit of work.
	 */
	public void invokeInternalBuilder(IResource[] resourcesToBuild, IConfiguration cfg, 
			boolean buildIncrementaly,
			boolean resumeOnErr,
			boolean initNewConsole,
			boolean printFinishedMessage,
			IProgressMonitor monitor) {
		// Get the project and make sure there's a monitor to cancel the build
		
		IProject currentProject = cfg.getOwner().getProject();
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		try {
			int flags = 0;
			IResourceDelta delta = null;
			
			if(buildIncrementaly){
				flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED | BuildDescriptionManager.DEPS;
				delta = getDelta(currentProject);
			}
			
			
			String[] msgs = new String[2];
			msgs[0] = ManagedMakeMessages.getResourceString(INTERNAL_BUILDER);
			msgs[1] = currentProject.getName();

			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(currentProject);
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			
			StringBuffer buf = new StringBuffer();
			
			if (initNewConsole) {
				if (buildIncrementaly)
					buf.append(ManagedMakeMessages.getResourceString("GeneratedMakefileBuilder.buildSelectedIncremental")); //$NON-NLS-1$
				else
					buf.append(ManagedMakeMessages.getResourceString("GeneratedMakefileBuilder.buildSelectedRebuild")); //$NON-NLS-1$
				

				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$	//$NON-NLS-2$

				buf.append(ManagedMakeMessages
						.getResourceString(INTERNAL_BUILDER_HEADER_NOTE));
				buf.append("\n"); //$NON-NLS-1$
			}
			
			
			if(!cfg.isSupported()){
				buf.append(ManagedMakeMessages.getFormattedString(WARNING_UNSUPPORTED_CONFIGURATION,new String[] {cfg.getName(),cfg.getToolChain().getName()}));
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			}
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
				
			// Remove all markers for this project
			// TODO remove only necessary markers
			removeAllMarkers(currentProject);
			
			IBuildDescription des = BuildDescriptionManager.createBuildDescription(cfg, delta, flags);
			
			// Hook up an error parser manager
			String[] errorParsers = cfg.getErrorParserList(); 
			ErrorParserManager epm = new ErrorParserManager(currentProject, des.getDefaultBuildDirLocation(), this, errorParsers);
			epm.setOutputStream(consoleOutStream);
			// This variable is necessary to ensure that the EPM stream stay open
			// until we explicitly close it. See bug#123302.
			OutputStream epmOutputStream = epm.getOutputStream();
			
			boolean errorsFound = false;
			
		doneBuild: for (int k = 0; k < resourcesToBuild.length; k++) {
				IBuildResource buildResource = des
						.getBuildResource(resourcesToBuild[k]);

//				step collector 
				Set dependentSteps = new HashSet();

//				get dependent IO types
				IBuildIOType depTypes[] = buildResource.getDependentIOTypes();

//				iterate through each type and add the step the type belongs to to the collector
				for(int j = 0; j < depTypes.length; j++){
				IBuildIOType type = depTypes[j];
				if(type != null && type.getStep() != null)
					dependentSteps.add(type.getStep());
				}

				monitor.subTask(ManagedMakeMessages.getResourceString("GeneratedMakefileBuilder.buildingFile") + resourcesToBuild[k].getProjectRelativePath()); //$NON-NLS-1$
				
				// iterate through all build steps
				Iterator stepIter = dependentSteps.iterator();
				
				while(stepIter.hasNext())
				{
					IBuildStep step = (IBuildStep) stepIter.next();
					
					StepBuilder stepBuilder = new StepBuilder(step);
					
					int status = stepBuilder.build(consoleOutStream, epmOutputStream, new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
					
					// Refresh the output resource without allowing the user to cancel. 
					// This is probably unkind, but short of this there is no way to ensure 
					// the UI is up-to-date with the build results 
					IBuildIOType[] outputIOTypes = step.getOutputIOTypes();
					
					for(int j = 0; j < outputIOTypes.length; j++ )
					{
						IBuildResource[] resources = outputIOTypes[j].getResources();
						
						for(int i = 0; i < resources.length; i++)
						{
							IFile file = currentProject.getFile(resources[i].getLocation());
							file.refreshLocal(IResource.DEPTH_INFINITE, null);
						}
					}
					
					// check status
					
					switch (status) {
					case IBuildModelBuilder.STATUS_OK:
						// don't print anything if the step was successful,
						// since the build might not be done as a whole
						break;
					case IBuildModelBuilder.STATUS_CANCELLED:
						buf.append(ManagedMakeMessages
								.getResourceString(BUILD_CANCELLED));
						break doneBuild;
					case IBuildModelBuilder.STATUS_ERROR_BUILD:
						errorsFound = true;
						if (!resumeOnErr) {
							buf.append(ManagedMakeMessages
									.getResourceString(BUILD_STOPPED_ERR));
							break doneBuild;
						}
						break;
					case IBuildModelBuilder.STATUS_ERROR_LAUNCH:
					default:
						buf.append(ManagedMakeMessages
								.getResourceString(BUILD_FAILED_ERR));
						break doneBuild;
					}
				}

				
			}

			// check status
			// Report either the success or failure of our mission
			buf = new StringBuffer();

			
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			
			if (printFinishedMessage) {
				if (errorsFound) {
					buf.append(ManagedMakeMessages
							.getResourceString(BUILD_FAILED_ERR));
				} else {
					buf
							.append(ManagedMakeMessages
									.getResourceString("GeneratedMakefileBuilder.buildResourcesFinished")); //$NON-NLS-1$
				}
			}
			
			// Write message on the console 
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			epmOutputStream.close();

			// Generate any error markers that the build has discovered 
//TODO:			addBuilderMarkers(epm);
			epm.reportProblems();
			consoleOutStream.close();
		} catch (Exception e) {
			StringBuffer buf = new StringBuffer();
			String errorDesc = ManagedMakeMessages
						.getResourceString(BUILD_ERROR);
			buf.append(errorDesc);
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
			buf.append("(").append(e.getLocalizedMessage()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ 

			forgetLastBuiltState();
		} finally {
//			getGenerationProblems().clear();
		}
	}

	protected BuildStatus performPostbuildGeneration(int kind, IBuilder builder, IManagedBuildInfo mInfo, BuildStatus buildStatus, IProgressMonitor monitor) throws CoreException{
		if(builder.isInternalBuilder())
			return buildStatus;

		if(buildStatus.isRebuild()){
			buildStatus.getMakeGen().regenerateDependencies(false);
		} else {
			buildStatus.getMakeGen().generateDependencies();
		}
		
		return buildStatus;
	}

	protected BuildStatus performPrebuildGeneration(int kind, IBuilder builder, IManagedBuildInfo mInfo, BuildStatus buildStatus, IProgressMonitor monitor) throws CoreException{
		if(builder.isInternalBuilder())
			return buildStatus;

		IConfiguration cfg = builder.getParent().getParent();
		buildStatus = performCleanning(kind, cfg, builder, mInfo, buildStatus, monitor);
		IManagedBuilderMakefileGenerator generator = builder.getBuildFileGenerator();
		if(generator != null){
			initializeGenerator(generator, kind, cfg, builder, mInfo, monitor);
			buildStatus.setMakeGen(generator);
	
			MultiStatus result = performMakefileGeneration(generator, buildStatus, monitor);
			if (result.getCode() == IStatus.WARNING || result.getCode() == IStatus.INFO) {
				IStatus[] kids = result.getChildren();
				for (int index = 0; index < kids.length; ++index) {
					// One possibility is that there is nothing to build
					IStatus status = kids[index]; 
//					if(messages == null){
//						messages = new MultiStatus(
//								ManagedBuilderCorePlugin.getUniqueIdentifier(),
//								IStatus.INFO,
//								"",
//								null);
//
//					}
					if (status.getCode() == IManagedBuilderMakefileGenerator.NO_SOURCE_FOLDERS) {
//						performBuild = false;
						buildStatus.getConsoleMessagesList().add(createNoSourceMessage(kind, status, cfg.getName()));
						buildStatus.cancelBuild();
//						break;
						
					} else {
						// Stick this in the list of stuff to warn the user about
						
				//TODO:		messages.add(status);
					}				
				}
			}

			checkCancel(monitor);
			


//			if(result.getSeverity() != IStatus.OK)
//				throw new CoreException(result);
		}	else {
			buildStatus.cancelBuild();
		}
		
//		if(messages == null){
//			messages = createMultiStatus(IStatus.OK);
//		}
		
		return buildStatus;
	}
	
	protected BuildStatus performCleanning(int kind, IConfiguration cfg, IBuilder builder, IManagedBuildInfo mInfo, BuildStatus status, IProgressMonitor monitor) throws CoreException{
		boolean makefileRegenerationNeeded = false;
		//perform necessary cleaning and build type calculation
		if(cfg.needsFullRebuild()){
			//configuration rebuild state is set to true,
			//full rebuild is needed in any case
			//clean first, then make a full build
			outputTrace(getProject().getName(), "config rebuild state is set to true, making a full rebuild");	//$NON-NLS-1$
			clean(builder, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
			makefileRegenerationNeeded = true;
		} else {
			makefileRegenerationNeeded = cfg.needsRebuild();
			IBuildDescription des = null;
			
			IResourceDelta delta = kind == FULL_BUILD ? null : getDelta(getProject());
			if(delta == null)
				makefileRegenerationNeeded = true;
			if(cfg.needsRebuild() || delta != null){
				//use a build desacription model to calculate the resources to be cleaned 
				//only in case there are some changes to the project sources or build information
				try{
					int flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.DEPFILES | BuildDescriptionManager.DEPS;
					if(delta != null)
						flags |= BuildDescriptionManager.REMOVED;

					outputTrace(getProject().getName(), "using a build description..");	//$NON-NLS-1$

					des = BuildDescriptionManager.createBuildDescription(cfg, getDelta(getProject()), flags);
	
					BuildDescriptionManager.cleanGeneratedRebuildResources(des);
				} catch (Throwable e){
					//TODO: log error
					outputError(getProject().getName(), "error occured while build description calculation: " + e.getLocalizedMessage());	//$NON-NLS-1$
					//in case an error occured, make it behave in the old stile:
					if(cfg.needsRebuild()){
						//make a full clean if an info needs a rebuild
						clean(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
						makefileRegenerationNeeded = true;
					}
					else if(delta != null && !makefileRegenerationNeeded){
						// Create a delta visitor to detect the build type
						ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(cfg, mInfo.getManagedProject().getConfigurations());
						delta.accept(visitor);
						if (visitor.shouldBuildFull()) {
							clean(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
							makefileRegenerationNeeded = true;
						}
					}
				}
			}
		}
		
		if(makefileRegenerationNeeded){
			status.setRebuild();
		}
		return status;
	}
	
	protected MultiStatus performMakefileGeneration(IManagedBuilderMakefileGenerator generator, BuildStatus buildStatus, IProgressMonitor monitor) throws CoreException {
		// Need to report status to the user
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		// Ask the makefile generator to generate any makefiles needed to build delta
		checkCancel(monitor);
		String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.update.makefiles", getProject().getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);
		
		MultiStatus result;
		if(buildStatus.isRebuild()){
			result = generator.regenerateMakefiles();
		} else {
			result = generator.generateMakefiles(getDelta(getProject()));
		}
		
		return result;
	}
	
	private MultiStatus createMultiStatus(int severity){
		return new MultiStatus(
				ManagedBuilderCorePlugin.getUniqueIdentifier(),
				severity,
				new String(),
				null);
	}

	
	protected void initializeGenerator(IManagedBuilderMakefileGenerator generator, int kind, IConfiguration cfg, IBuilder builder, IManagedBuildInfo info, IProgressMonitor monitor){
		if(generator instanceof IManagedBuilderMakefileGenerator2){
			IManagedBuilderMakefileGenerator2 gen2 = (IManagedBuilderMakefileGenerator2)generator;
			gen2.initialize(kind, cfg, builder, monitor);
		} else {
			generator.initialize(getProject(), info, monitor);
		}
		
	}
	
	protected void clean(IProgressMonitor monitor) throws CoreException {
		IBuilder[] builders = BuilderFactory.createBuilders(getProject(), null);
		for(int i = 0; i < builders.length; i++){
			IBuilder builder = builders[i];
			clean(builder, monitor);
		}
	}
	
	protected void clean(IBuilder builder, IProgressMonitor monitor) throws CoreException{
		if (shouldBuild(CLEAN_BUILD, builder)) {
			boolean performExternalClean = true;
			if(shouldCleanProgrammatically(builder)){
				try {
					cleanProgrammatically(builder, monitor);
					performExternalClean = false;
				} catch (CoreException e) {
				}
			}
			
			if(performExternalClean){
				performExternalClean(builder, false, monitor);
			}
		}
		
	}
	
	protected void performExternalClean(final IBuilder builder, boolean separateJob, IProgressMonitor monitor) throws CoreException {
		IResourceRuleFactory ruleFactory= ResourcesPlugin.getWorkspace().getRuleFactory();
		final ISchedulingRule rule = ruleFactory.modifyRule(getProject());
		
		if(separateJob){
			Job backgroundJob = new Job("CDT Common Builder"){  //$NON-NLS-1$
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
	
							public void run(IProgressMonitor monitor) throws CoreException {
									invokeMake(CLEAN_BUILD, builder, monitor);
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
		} else {
			invokeMake(CLEAN_BUILD, builder, monitor);
		}
		
	}
	
	protected boolean shouldCleanProgrammatically(IBuilder builder){
		if(!builder.isManagedBuildOn())
			return false;
		return true;
//		IConfiguration cfg = builder.getParent().getParent();
//		IPath path = ManagedBuildManager.getBuildFullPath(cfg, builder);
//		if(path == null)
//			return false;
//		
//		return cfg.getOwner().getProject().getFullPath().isPrefixOf(path);
	}
	
	protected void cleanProgrammatically(IBuilder builder, IProgressMonitor monitor) throws CoreException {
//		referencedProjects = getProject().getReferencedProjects();
		outputTrace(getProject().getName(), "Clean build requested");	//$NON-NLS-1$
		IConfiguration cfg = builder.getParent().getParent();
		IPath buildPath = ManagedBuildManager.getBuildFullPath(cfg, builder);
		if(buildPath == null){
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(), 
					"can not clean programmatically: build workspace path is not specified"));
		}
		
		IPath projectFullPath = getProject().getFullPath();
		if(!projectFullPath.isPrefixOf(buildPath)){
			throw new CoreException(new Status(IStatus.ERROR, 
					ManagedBuilderCorePlugin.getUniqueIdentifier(), 
					"can not clean programmatically: build workspace path is not the project path"));
		}
			
		IWorkspace workspace = CCorePlugin.getWorkspace();
		IResource rc = workspace.getRoot().findMember(buildPath);
		if(rc != null){
			if(rc.getType() != IResource.FOLDER){
				throw new CoreException(new Status(IStatus.ERROR, 
						ManagedBuilderCorePlugin.getUniqueIdentifier(), 
						"can not clean programmatically: build workspace path is not folder"));
			}
			
			IFolder buildDir = (IFolder)rc;
			if (!buildDir.isAccessible()){
				outputError(buildDir.getName(), "Could not delete the build directory");	//$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR, 
						ManagedBuilderCorePlugin.getUniqueIdentifier(), 
						"can not clean programmatically: build folder is not accessible"));
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
			consoleHeader[1] = cfg.getName();
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
		}  catch (IOException io) {}	//  Ignore console failures...		
		}
	}
	
	protected boolean invokeBuilder(int kind, IBuilder builder, IProgressMonitor monitor) throws CoreException {
		if(builder.isInternalBuilder())
			return invokeInternalBuilder(kind, builder, monitor);
		return invokeMake(kind, builder, monitor);
	}

	
	protected boolean invokeMake(int kind, IBuilder builder, IProgressMonitor monitor) throws CoreException {
		boolean isClean = false;
		IProject currProject = getProject();

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeMessages.getString("MakeBuilder.Invoking_Make_Builder") + currProject.getName(), 100); //$NON-NLS-1$

		try {
			IPath buildCommand = builder.getBuildCommand();
			if (buildCommand != null) {
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(currProject);

				OutputStream cos = console.getOutputStream();
				StringBuffer buf = new StringBuffer();

				String[] consoleHeader = new String[3];
				switch (kind) {
					case FULL_BUILD:
					case INCREMENTAL_BUILD:
					case AUTO_BUILD:
						consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
						break;
					case CLEAN_BUILD:
						consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_CLEAN);
						break;
				}
				
				IConfiguration cfg = builder.getParent().getParent();
				consoleHeader[1] = cfg.getName();
				consoleHeader[2] = getProject().getName();
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader));
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				
				if(!cfg.isSupported()){
					buf.append(ManagedMakeMessages.getFormattedString(WARNING_UNSUPPORTED_CONFIGURATION,new String[] {cfg.getName(),cfg.getToolChain().getName()}));
					buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
					buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				}
				cos.write(buf.toString().getBytes());
				cos.flush();

				// remove all markers for this project
				removeAllMarkers(currProject);

				IPath workingDirectory = ManagedBuildManager.getBuildLocation(cfg, builder);

				String[] targets = getTargets(kind, builder);
				if (targets.length != 0 && targets[targets.length - 1].equals(builder.getCleanBuildTarget())) //$NON-NLS-1$
					isClean = true;

				String errMsg = null;
				CommandLauncher launcher = new CommandLauncher();
				// Print the command for visual interaction.
				launcher.showCommand(true);

				// Set the environment
				String[] env = calcEnvironment(builder);
				String[] buildArguments = targets;
				if (builder.isDefaultBuildCmd()) {
//					if (!builder.isStopOnError()) {
//						buildArguments = new String[targets.length + 1];
//						buildArguments[0] = "-k"; //$NON-NLS-1$
//						System.arraycopy(targets, 0, buildArguments, 1, targets.length);
//					}
				} else {
					String args = builder.getBuildArguments();
					if (args != null && !args.equals("")) { //$NON-NLS-1$
						String[] newArgs = makeArray(args);
						buildArguments = new String[targets.length + newArgs.length];
						System.arraycopy(newArgs, 0, buildArguments, 0, newArgs.length);
						System.arraycopy(targets, 0, buildArguments, newArgs.length, targets.length);
					}
				}
//					MakeRecon recon = new MakeRecon(buildCommand, buildArguments, env, workingDirectory, makeMonitor, cos);
//					recon.invokeMakeRecon();
//					cos = recon;
				QualifiedName qName = new QualifiedName(ManagedBuilderCorePlugin.getUniqueIdentifier(), "progressMonitor"); //$NON-NLS-1$
				Integer last = (Integer)getProject().getSessionProperty(qName);
				if (last == null) {
					last = new Integer(100);
				}
				StreamMonitor streamMon = new StreamMonitor(new SubProgressMonitor(monitor, 100), cos, last.intValue());
				ErrorParserManager epm = new ErrorParserManager(getProject(), workingDirectory, this, builder.getErrorParsers());
				epm.setOutputStream(streamMon);
				OutputStream stdout = epm.getOutputStream();
				OutputStream stderr = epm.getOutputStream();
				// Sniff console output for scanner info
				ConsoleOutputSniffer sniffer = ScannerInfoConsoleParserFactory.getMakeBuilderOutputSniffer(
						stdout, stderr, cfg, null, workingDirectory, null, this, null);
				OutputStream consoleOut = (sniffer == null ? stdout : sniffer.getOutputStream());
				OutputStream consoleErr = (sniffer == null ? stderr : sniffer.getErrorStream());
				Process p = launcher.execute(buildCommand, buildArguments, env, workingDirectory);
				if (p != null) {
					try {
						// Close the input of the Process explicitly.
						// We will never write to it.
						p.getOutputStream().close();
					} catch (IOException e) {
					}
					// Before launching give visual cues via the monitor
					monitor.subTask(MakeMessages.getString("MakeBuilder.Invoking_Command") + launcher.getCommandLine()); //$NON-NLS-1$
					if (launcher.waitAndRead(consoleOut, consoleErr, new SubProgressMonitor(monitor, 0))
						!= CommandLauncher.OK)
						errMsg = launcher.getErrorMessage();
					monitor.subTask(MakeMessages.getString("MakeBuilder.Updating_project")); //$NON-NLS-1$

					try {
						// Do not allow the cancel of the refresh, since the builder is external
						// to Eclipse, files may have been created/modified and we will be out-of-sync.
						// The caveat is for hugue projects, it may take sometimes at every build.
						currProject.refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (CoreException e) {
					}
				} else {
					errMsg = launcher.getErrorMessage();
				}
				getProject().setSessionProperty(qName, !monitor.isCanceled() && !isClean ? new Integer(streamMon.getWorkDone()) : null);

				if (errMsg != null) {
					buf = new StringBuffer(buildCommand.toString() + " "); //$NON-NLS-1$
					for (int i = 0; i < buildArguments.length; i++) {
						buf.append(buildArguments[i]);
						buf.append(' ');
					}

					String errorDesc = MakeMessages.getFormattedString("MakeBuilder.buildError", buf.toString()); //$NON-NLS-1$
					buf = new StringBuffer(errorDesc);
					buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
					cos.write(buf.toString().getBytes());
					cos.flush();
				}

				stdout.close();
				stderr.close();

				monitor.subTask(MakeMessages.getString("MakeBuilder.Creating_Markers")); //$NON-NLS-1$
				consoleOut.close();
				consoleErr.close();
				epm.reportProblems();
				cos.close();
			}
		} catch (Exception e) {
			CCorePlugin.log(e);
			throw new CoreException(new Status(IStatus.ERROR,
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					e.getLocalizedMessage(),
					e));
		} finally {
			monitor.done();
		}
		return (isClean);
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

	protected String[] getTargets(int kind, IBuilder builder) {
		String targetsArray[] = null;
		
		if(kind != CLEAN_BUILD && !builder.isCustomBuilder() && builder.isManagedBuildOn()){
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
				targetsArray = new String[]{"pre-build", "main-build"};
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
			
			targetsArray = makeArray(targets);
		}
		
		return targetsArray;
	}

	// Turn the string into an array.
	String[] makeArray(String string) {
		string.trim();
		char[] array = string.toCharArray();
		ArrayList aList = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		boolean inComment = false;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];
			if (array[i] == '"' || array[i] == '\'') {
				if (i > 0 && array[i - 1] == '\\') {
					inComment = false;
				} else {
					inComment = !inComment;
				}
			}
			if (c == ' ' && !inComment) {
				aList.add(buffer.toString());
				buffer = new StringBuffer();
			} else {
				buffer.append(c);
			}
		}
		if (buffer.length() > 0)
			aList.add(buffer.toString());
		return (String[]) aList.toArray(new String[aList.size()]);
	}

	private void removeAllMarkers(IProject currProject) throws CoreException {
		IWorkspace workspace = currProject.getWorkspace();

		// remove all markers
		IMarker[] markers = currProject.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers != null) {
			workspace.deleteMarkers(markers);
		}
	}}
