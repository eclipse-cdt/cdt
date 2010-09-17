/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 * Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *                                Save build output (bug 294106)
 * Andrew Gvozdev (Quoin Inc)   - Saving build output implemented in different way (bug 306222)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.util.ListComparator;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildDescription;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildStateManager;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IBuildModelBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IConfigurationBuildState;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IProjectBuildState;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.StepBuilder;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator2;
import org.eclipse.cdt.newmake.core.IMakeBuilderInfo;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

public class CommonBuilder extends ACBuilder {

	public final static String BUILDER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".genmakebuilder"; //$NON-NLS-1$
	private static final String BUILD_FINISHED = "ManagedMakeBuilder.message.finished";	//$NON-NLS-1$
	private static final String CONSOLE_HEADER = "ManagedMakeBuilder.message.console.header";	//$NON-NLS-1$
	private static final String ERROR_HEADER = "GeneratedmakefileBuilder error [";	//$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator");	//$NON-NLS-1$
	private static final String TRACE_FOOTER = "]: ";	//$NON-NLS-1$
	private static final String TRACE_HEADER = "GeneratedmakefileBuilder trace [";	//$NON-NLS-1$
	private static final String TYPE_CLEAN = "ManagedMakeBuilder.type.clean";	//$NON-NLS-1$
	private static final String TYPE_INC = "ManagedMakeBuider.type.incremental";	//$NON-NLS-1$
	public static boolean VERBOSE = false;

	private static CfgBuildSet fBuildSet = new CfgBuildSet();

	private boolean fBuildErrOccured;

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

	private static class CfgBuildSet {
		Map<IProject, Set<String>> fMap = new HashMap<IProject, Set<String>>();

		public Set<String> getCfgIdSet(IProject project, boolean create){
			Set<String> set = fMap.get(project);
			if(set == null && create){
				set = new HashSet<String>();
				fMap.put(project, set);
			}
			return set;
		}

		public void start(CommonBuilder bld){
			checkClean(bld);
		}

		private boolean checkClean(CommonBuilder bld){
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject wproject : projects) {
				if(bld.hasBeenBuilt(wproject)){
					if(VERBOSE)
						outputTrace(null, "checking clean: the project " + wproject.getName() +" was built, no clean needed"); //$NON-NLS-1$ //$NON-NLS-2$

					return false;
				}
			}

			if(VERBOSE)
				outputTrace(null, "checking clean: no projects were built.. cleanning"); //$NON-NLS-1$

			fMap.clear();
			return true;
		}
	}
	private static class CfgBuildInfo {
		private final IProject fProject;
		private final IManagedBuildInfo fBuildInfo;
		private final IConfiguration fCfg;
		private final IBuilder fBuilder;
		private IConsole fConsole;
		CfgBuildInfo(IBuilder builder, boolean isForegound){
			this.fBuilder = builder;
			this.fCfg = builder.getParent().getParent();
			this.fProject = this.fCfg.getOwner().getProject();
			this.fBuildInfo = ManagedBuildManager.getBuildInfo(this.fProject);
		}

		public IProject getProject(){
			return fProject;
		}

		public IConsole getConsole(){
			if(fConsole == null){
				fConsole = CCorePlugin.getDefault().getConsole();
				fConsole.start(fProject);
			}
			return fConsole;
		}

//		public boolean isForeground(){
//			return fIsForeground;
//		}

		public IBuilder getBuilder(){
			return fBuilder;
		}

		public IConfiguration getConfiguration(){
			return fCfg;
		}

		public IManagedBuildInfo getBuildInfo(){
			return fBuildInfo;
		}
	}


	public class ResourceDeltaVisitor implements IResourceDeltaVisitor {
		private String buildGoalName;
		private final IProject project;
		private final IPath buildPaths[];
		private boolean incrBuildNeeded = false;
		private boolean fullBuildNeeded = false;
		private final List<String> reservedNames;

		public ResourceDeltaVisitor(IConfiguration cfg, IConfiguration allConfigs[]) {
			this.project = cfg.getOwner().getProject();
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

		private boolean isGeneratedResource(IResource resource) {
			// Is this a generated directory ...
			IPath path = resource.getFullPath();
			for (IPath buildPath : buildPaths) {
				if(buildPath != null && buildPath.isPrefixOf(path)){
					return true;
				}
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
		IPath buildFullPaths[];
//		IConfiguration buildConfigs[];
		Configuration otherConfigs[];
		int resourceChangeState;

		private static final IPath[] ignoreList = {
			new Path(".cdtproject"), //$NON-NLS-1$
			new Path(".cproject"), //$NON-NLS-1$
			new Path(".cdtbuild"), //$NON-NLS-1$
			new Path(".settings"), //$NON-NLS-1$
		};

		OtherConfigVerifier(IBuilder builders[], IConfiguration allCfgs[]){
			Set<IConfiguration> buildCfgSet = new HashSet<IConfiguration>();
			for (IBuilder builder : builders) {
				buildCfgSet.add(builder.getParent().getParent());
			}
			@SuppressWarnings("unchecked")
			List<Configuration> othersList = ListComparator.getAdded(allCfgs, buildCfgSet.toArray());
			if(othersList != null)
				otherConfigs = othersList.toArray(new Configuration[othersList.size()]);
			else
				otherConfigs = new Configuration[0];

			List<IPath> list = new ArrayList<IPath>(builders.length);
//			buildFullPaths = new IPath[builders.length];
			for (IBuilder builder : builders) {
				IPath path = ManagedBuildManager.getBuildFullPath(builder.getParent().getParent(), builder);
				if(path != null)
					list.add(path);
//				buildFullPaths[i] = ManagedBuildManager.getBuildFullPath(builders[i].getParent().getParent(), builders[i]);
			}
			buildFullPaths = list.toArray(new IPath[list.size()]);

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
			for (IPath buildFullPath : buildFullPaths) {
				if(buildFullPath.isPrefixOf(rc.getFullPath()))
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
			for (Configuration otherConfig : otherConfigs) {
				otherConfig.addResourceChangeState(resourceChangeState);
			}
		}

		private boolean isResourceValuable(IResource rc){
			IPath path = rc.getProjectRelativePath();
			for (IPath ignoredPath : ignoreList) {
				if(ignoredPath.equals(path))
					return false;
			}
			return true;
		}
	}

	protected boolean isCdtProjectCreated(IProject project){
		ICProjectDescription des = CoreModel.getDefault().getProjectDescription(project, false);
		return des != null && !des.isCdtProjectCreating();
	}

	private class MyBoolean {
	    private boolean value;

	    public MyBoolean (boolean value) {
	        this.value = value;
	    }

	    public boolean getValue() {
	        return value;
	    }

	    public void setValue(boolean value) {
	        this.value = value;
	    }

	}
	/**
	 * @see IncrementalProjectBuilder#build
	 */
	@Override
	protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map argsMap, IProgressMonitor monitor) throws CoreException {
		@SuppressWarnings("unchecked")
		Map<String, String> args = argsMap;
		if (DEBUG_EVENTS)
			printEvent(kind, args);

		fBuildSet.start(this);

		IProject project = getProject();

		if(!isCdtProjectCreated(project))
			return project.getReferencedProjects();

		if(VERBOSE)
			outputTrace(project.getName(), ">>build requested, type = " + kind); //$NON-NLS-1$

		IProject[] projects = null;
		if (needAllConfigBuild()) {
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
			IConfiguration defCfg = info.getDefaultConfiguration();
			for (IConfiguration cfg : cfgs) {
				info.setDefaultConfiguration(cfg);
				IBuilder builders[] = ManagedBuilderCorePlugin.createBuilders(project, args);
				projects = build(kind, project, builders, true, monitor, new MyBoolean(false));
			}
			info.setDefaultConfiguration(defCfg);
		} else {
			IBuilder builders[] = ManagedBuilderCorePlugin.createBuilders(project, args);
			projects = build(kind, project, builders, true, monitor, new MyBoolean(false));
		}

		if(VERBOSE)
			outputTrace(project.getName(), "<<done build requested, type = " + kind); //$NON-NLS-1$

		return projects;
	}

	protected IProject[] build(int kind, IProject project, IBuilder[] builders, boolean isForeground, IProgressMonitor monitor) throws CoreException{
		return build(kind, project, builders, isForeground, monitor, new MyBoolean(false));
	}

	private IProject[] build(int kind, IProject project, IBuilder[] builders, boolean isForeground, IProgressMonitor monitor, MyBoolean isBuild) throws CoreException{
		if(!isCdtProjectCreated(project))
			return project.getReferencedProjects();

		int num = builders.length;
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IConfiguration activeCfg = info.getDefaultConfiguration();
		IProject[] refProjects = project.getReferencedProjects();
		if(num != 0){
			MultiStatus status = checkBuilders(builders, activeCfg);
			if(status.getSeverity() != IStatus.OK)
				throw new CoreException(status);

			IConfiguration rcfgs[] = getReferencedConfigs(builders);

			monitor.beginTask("", num + rcfgs.length); //$NON-NLS-1$

			if(rcfgs.length != 0){
				Set<IProject> set = buildReferencedConfigs(rcfgs, new SubProgressMonitor(monitor, 1), isBuild);// = getProjectsSet(cfgs);
				if(set.size() != 0){
					set.addAll(Arrays.asList(refProjects));
					refProjects = set.toArray(new IProject[set.size()]);
				}
			}

			for(int i = 0; i < num; i++){
			    //bug 219337
			    if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD ) {
    			    if (buildConfigResourceChanges()) { //only build projects with project resource changes
    			        IResourceDelta delta = getDelta(project);
                        if (delta != null && delta.getAffectedChildren().length > 0) { //project resource has changed within Eclipse, need to build this configuration
                            isBuild.setValue(true);
                            build(kind, new CfgBuildInfo(builders[i], isForeground), new SubProgressMonitor(monitor, 1));
                        }
                        else if (isBuild.getValue()) { //one of its dependencies have rebuilt, need to rebuild this configuration
                            build(kind, new CfgBuildInfo(builders[i], isForeground), new SubProgressMonitor(monitor, 1));
                        }
    			    } else { //the default behaviour: 'make' is invoked on all configurations and incremental build is handled by 'make'
    			        build(kind, new CfgBuildInfo(builders[i], isForeground), new SubProgressMonitor(monitor, 1));
    			    }
			    } else { //FULL_BUILD or CLEAN
			        build(kind, new CfgBuildInfo(builders[i], isForeground), new SubProgressMonitor(monitor, 1));
			    }
			}
		}

		if(isForeground)
			updateOtherConfigs(info, builders, kind);

		monitor.done();
		return refProjects;
	}

	private Set<IProject> buildReferencedConfigs(IConfiguration[] cfgs, IProgressMonitor monitor, MyBoolean refConfigChanged){
		Set<IProject> projSet = getProjectsSet(cfgs);
		cfgs = filterConfigsToBuild(cfgs);
		MyBoolean nextConfigChanged = new MyBoolean(false);

		if(cfgs.length != 0){
			monitor.beginTask(ManagedMakeMessages.getResourceString("CommonBuilder.22"), cfgs.length); //$NON-NLS-1$
			for (IConfiguration cfg : cfgs) {
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
				nextConfigChanged.setValue(false);
				try {
					IBuilder builder = cfg.getEditableBuilder();
//					CfgBuildInfo bInfo = new CfgBuildInfo(builder, false);

					if(VERBOSE)
						outputTrace(cfg.getOwner().getProject().getName(), ">>>>building reference cfg " + cfg.getName()); //$NON-NLS-1$

					IProject[] projs = build(INCREMENTAL_BUILD, cfg.getOwner().getProject(), new IBuilder[]{builder}, false, subMonitor, nextConfigChanged);

					if(VERBOSE)
						outputTrace(cfg.getOwner().getProject().getName(), "<<<<done building reference cfg " + cfg.getName()); //$NON-NLS-1$

					projSet.addAll(Arrays.asList(projs));
				} catch (CoreException e){
					ManagedBuilderCorePlugin.log(e);
				} finally {
					subMonitor.done();
				}
				refConfigChanged.setValue(refConfigChanged.getValue() || nextConfigChanged.getValue());
			}
		} else {
			monitor.done();
		}

		return projSet;
	}

	private IConfiguration[] filterConfigsToBuild(IConfiguration[] cfgs){
		List<IConfiguration> cfgList = new ArrayList<IConfiguration>(cfgs.length);
		for (IConfiguration cfg : cfgs) {
			IProject project = cfg.getOwner().getProject();
			Set<String> set = fBuildSet.getCfgIdSet(project, true);
			if(set.add(cfg.getId())){
				if(VERBOSE){
					outputTrace(cfg.getOwner().getProject().getName(), "set: adding cfg " + cfg.getName() + " ( id=" + cfg.getId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					outputTrace(cfg.getOwner().getProject().getName(), "filtering regs: adding cfg " + cfg.getName() + " ( id=" + cfg.getId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}

				cfgList.add(cfg);
			} else if(VERBOSE)
				outputTrace(cfg.getOwner().getProject().getName(), "filtering regs: excluding cfg " + cfg.getName() + " ( id=" + cfg.getId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		}
		return cfgList.toArray(new IConfiguration[cfgList.size()]);
	}



	@Override
	protected void startupOnInitialize() {
		super.startupOnInitialize();

	}

	private IConfiguration[] getReferencedConfigs(IBuilder[] builders){
		Set<IConfiguration> set = new HashSet<IConfiguration>();
		for (IBuilder builder : builders) {
			IConfiguration cfg = builder.getParent().getParent();
			IConfiguration refs[] = ManagedBuildManager.getReferencedConfigurations(cfg);
			for (IConfiguration ref : refs) {
				set.add(ref);
			}
		}
		return set.toArray(new Configuration[set.size()]);
	}

	private Set<IProject> getProjectsSet(IConfiguration[] cfgs){
		if(cfgs.length == 0)
			return new HashSet<IProject>(0);

		Set<IProject> set = new HashSet<IProject>();
		for (IConfiguration cfg : cfgs) {
			set.add(cfg.getOwner().getProject());
		}

		return set;
	}

	protected MultiStatus checkBuilders(IBuilder builders[], IConfiguration activeCfg){
		MultiStatus status = null;
		for (IBuilder builder : builders) {
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
							ManagedMakeMessages.getResourceString("CommonBuilder.1"), //$NON-NLS-1$
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
							ManagedMakeMessages.getResourceString("CommonBuilder.2"), //$NON-NLS-1$
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

	private void updateOtherConfigs(IManagedBuildInfo info, IBuilder builders[], int buildKind){
		IConfiguration allCfgs[] = info.getManagedProject().getConfigurations();
		new OtherConfigVerifier(builders, allCfgs).updateOtherConfigs(buildKind == FULL_BUILD ? null : getDelta(info.getManagedProject().getOwner().getProject()));
	}

	protected class BuildStatus {
		private final boolean fManagedBuildOn;
		private boolean fRebuild;
		private boolean fBuild = true;
		private final List<String> fConsoleMessages = new ArrayList<String>();
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

		public List<String> getConsoleMessagesList(){
			return fConsoleMessages;
		}

		public IManagedBuilderMakefileGenerator getMakeGen(){
			return fMakeGen;
		}

		public void setMakeGen(IManagedBuilderMakefileGenerator makeGen){
			fMakeGen = makeGen;
		}
	}

	protected void build(int kind, CfgBuildInfo bInfo, IProgressMonitor monitor) throws CoreException{
		if(VERBOSE)
			outputTrace(bInfo.getProject().getName(), "building cfg " + bInfo.getConfiguration().getName() + " with builder " + bInfo.getBuilder().getName()); //$NON-NLS-1$ //$NON-NLS-2$
		IBuilder builder = bInfo.getBuilder();
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
			IConfiguration cfg = bInfo.getConfiguration();

			if(!builder.isCustomBuilder()){
				Set<String> set = fBuildSet.getCfgIdSet(bInfo.getProject(), true);
				if(VERBOSE)
					outputTrace(bInfo.getProject().getName(), "set: adding cfg " + cfg.getName() + " ( id=" + cfg.getId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				set.add(cfg.getId());
			}

			if(status.isManagedBuildOn()){
				status = performPrebuildGeneration(kind, bInfo, status, monitor);
			}

			if(status.isBuild()){
				try {
				boolean isClean = builder.getBuildRunner().invokeBuild(
						kind,
						bInfo.getProject(),
						bInfo.getConfiguration(),
						builder,
						bInfo.getConsole(),
						this,
						this,
						monitor);
					if (isClean) {
						forgetLastBuiltState();
						cfg.setRebuildState(true);
					} else {
						if(status.isManagedBuildOn()){
							performPostbuildGeneration(kind, bInfo, status, monitor);
						}
						cfg.setRebuildState(false);
					}
				} catch(CoreException e){
					cfg.setRebuildState(true);
					throw e;
				}

				PropertyManager.getInstance().serialize(cfg);
			} else if(status.getConsoleMessagesList().size() != 0) {
				emitMessage(bInfo, concatMessages(status.getConsoleMessagesList()));
			}
		}
		checkCancel(monitor);
	}



	private String concatMessages(List<String> msgs){
		int size = msgs.size();
		if(size == 0){
			return ""; //$NON-NLS-1$
		} else if(size == 1){
			return msgs.get(0);
		}

		StringBuffer buf = new StringBuffer();
		buf.append(msgs.get(0));
		for(int i = 1; i < size; i++){
			buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append(msgs.get(i));
		}
		return buf.toString();
	}

	/* (non-javadoc)
	 * Emits a message to the console indicating that there were no source files to build
	 * @param buildType
	 * @param status
	 * @param configName
	 */
	private String createNoSourceMessage(int buildType, IStatus status, CfgBuildInfo bInfo) throws CoreException {
		StringBuffer buf = new StringBuffer();
		String[] consoleHeader = new String[3];
		String configName = bInfo.getConfiguration().getName();
		String projName = bInfo.getProject().getName();
		if (buildType == FULL_BUILD || buildType == INCREMENTAL_BUILD) {
			consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
		} else {
			consoleHeader[0] = new String();
			outputError(projName, "The given build type is not supported in this context");	//$NON-NLS-1$
		}
		consoleHeader[1] = configName;
		consoleHeader[2] = projName;
		buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
		buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader));
		buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
		buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
		buf.append(status.getMessage());
		buf.append(System.getProperty("line.separator", "\n"));  //$NON-NLS-1$//$NON-NLS-2$
		return buf.toString();
	}

	private void emitMessage(CfgBuildInfo info, String msg) throws CoreException {
		try {
			IConsole console = info.getConsole();
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

	protected BuildStatus performPostbuildGeneration(int kind, CfgBuildInfo bInfo, BuildStatus buildStatus, IProgressMonitor monitor) throws CoreException{
		IBuilder builder = bInfo.getBuilder();
		if(builder.isInternalBuilder())
			return buildStatus;

		if(buildStatus.isRebuild()){
			buildStatus.getMakeGen().regenerateDependencies(false);
		} else {
			buildStatus.getMakeGen().generateDependencies();
		}

		return buildStatus;
	}

	protected BuildStatus performPrebuildGeneration(int kind, CfgBuildInfo bInfo, BuildStatus buildStatus, IProgressMonitor monitor) throws CoreException{
		IBuilder builder = bInfo.getBuilder();
		if(builder.isInternalBuilder())
			return buildStatus;

		buildStatus = performCleanning(kind, bInfo, buildStatus, monitor);
		IManagedBuilderMakefileGenerator generator = builder.getBuildFileGenerator();
		if(generator != null){
			initializeGenerator(generator, kind, bInfo, monitor);
			buildStatus.setMakeGen(generator);

			MultiStatus result = performMakefileGeneration(bInfo, generator, buildStatus, monitor);
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
						buildStatus.getConsoleMessagesList().add(createNoSourceMessage(kind, status, bInfo));
						buildStatus.cancelBuild();
//						break;

					} else {
						// Stick this in the list of stuff to warn the user about

				//TODO:		messages.add(status);
					}
				}
			} else if (result.getCode() == IStatus.ERROR){
				StringBuffer buf = new StringBuffer();
				buf.append(ManagedMakeMessages.getString("CommonBuilder.23")).append(NEWLINE); //$NON-NLS-1$
				String message = result.getMessage();
				if(message != null && message.length() != 0){
					buf.append(message).append(NEWLINE);
				}

				buf.append(ManagedMakeMessages.getString("CommonBuilder.24")).append(NEWLINE); //$NON-NLS-1$
				message = buf.toString();
				buildStatus.getConsoleMessagesList().add(message);
				buildStatus.cancelBuild();
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

	protected BuildStatus performCleanning(int kind, CfgBuildInfo bInfo, BuildStatus status, IProgressMonitor monitor) throws CoreException{
		IConfiguration cfg = bInfo.getConfiguration();
		IProject curProject = bInfo.getProject();
//		IBuilder builder = bInfo.getBuilder();

		boolean makefileRegenerationNeeded = false;
		//perform necessary cleaning and build type calculation
		if(cfg.needsFullRebuild()){
			//configuration rebuild state is set to true,
			//full rebuild is needed in any case
			//clean first, then make a full build
			outputTrace(curProject.getName(), "config rebuild state is set to true, making a full rebuild");	//$NON-NLS-1$
			clean(bInfo, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
			makefileRegenerationNeeded = true;
		} else {
			makefileRegenerationNeeded = cfg.needsRebuild();
			IBuildDescription des = null;

			IResourceDelta delta = kind == FULL_BUILD ? null : getDelta(curProject);
			if(delta == null)
				makefileRegenerationNeeded = true;
			if(cfg.needsRebuild() || delta != null){
				//use a build desacription model to calculate the resources to be cleaned
				//only in case there are some changes to the project sources or build information
				try{
					int flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.DEPFILES | BuildDescriptionManager.DEPS;
					if(delta != null)
						flags |= BuildDescriptionManager.REMOVED;

					outputTrace(curProject.getName(), "using a build description..");	//$NON-NLS-1$

					des = BuildDescriptionManager.createBuildDescription(cfg, getDelta(curProject), flags);

					BuildDescriptionManager.cleanGeneratedRebuildResources(des);
				} catch (Throwable e){
					//TODO: log error
					outputError(curProject.getName(), "error occured while build description calculation: " + e.getLocalizedMessage());	//$NON-NLS-1$
					//in case an error occured, make it behave in the old stile:
					if(cfg.needsRebuild()){
						//make a full clean if an info needs a rebuild
						clean(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
						makefileRegenerationNeeded = true;
					}
					else if(delta != null && !makefileRegenerationNeeded){
						// Create a delta visitor to detect the build type
						ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(cfg, bInfo.getBuildInfo().getManagedProject().getConfigurations());
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

	protected MultiStatus performMakefileGeneration(CfgBuildInfo bInfo, IManagedBuilderMakefileGenerator generator, BuildStatus buildStatus, IProgressMonitor monitor) throws CoreException {
		// Need to report status to the user
		IProject curProject = bInfo.getProject();
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// Ask the makefile generator to generate any makefiles needed to build delta
		checkCancel(monitor);
		String statusMsg = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.update.makefiles", curProject.getName());	//$NON-NLS-1$
		monitor.subTask(statusMsg);

		MultiStatus result;
		if(buildStatus.isRebuild()){
			result = generator.regenerateMakefiles();
		} else {
			result = generator.generateMakefiles(getDelta(curProject));
		}

		return result;
	}

//	private MultiStatus createMultiStatus(int severity){
//		return new MultiStatus(
//				ManagedBuilderCorePlugin.getUniqueIdentifier(),
//				severity,
//				new String(),
//				null);
//	}


	protected void initializeGenerator(IManagedBuilderMakefileGenerator generator, int kind, CfgBuildInfo bInfo, IProgressMonitor monitor){
		if(generator instanceof IManagedBuilderMakefileGenerator2){
			IManagedBuilderMakefileGenerator2 gen2 = (IManagedBuilderMakefileGenerator2)generator;
			gen2.initialize(kind, bInfo.getConfiguration(), bInfo.getBuilder(), monitor);
		} else {
			generator.initialize(bInfo.getProject(), bInfo.getBuildInfo(), monitor);
		}

	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		if (DEBUG_EVENTS)
			printEvent(IncrementalProjectBuilder.CLEAN_BUILD, null);

		IProject curProject = getProject();

		if(!isCdtProjectCreated(curProject))
			return;

		IBuilder[] builders = ManagedBuilderCorePlugin.createBuilders(curProject, null);
		for (IBuilder builder : builders) {
			CfgBuildInfo bInfo = new CfgBuildInfo(builder, true);
			clean(bInfo, monitor);
		}
	}

	@Override
	public void addMarker(IResource file, int lineNumber, String errorDesc,
			int severity, String errorVar) {
		super.addMarker(file, lineNumber, errorDesc, severity, errorVar);
		if(severity == IStatus.ERROR)
			fBuildErrOccured = true;
	}

	@Override
	public void addMarker(ProblemMarkerInfo problemMarkerInfo) {
		super.addMarker(problemMarkerInfo);
		if(problemMarkerInfo.severity == IStatus.ERROR)
			fBuildErrOccured = true;
	}

	protected void clean(CfgBuildInfo bInfo, IProgressMonitor monitor) throws CoreException{
		if (shouldBuild(CLEAN_BUILD, bInfo.getBuilder())) {
			BuildStateManager bsMngr = BuildStateManager.getInstance();
			IProject project = bInfo.getProject();
			IConfiguration cfg = bInfo.getConfiguration();
			IProjectBuildState pbs = bsMngr.getProjectBuildState(project);
			IConfigurationBuildState cbs = pbs.getConfigurationBuildState(cfg.getId(), false);
			if(cbs != null){
				pbs.removeConfigurationBuildState(cfg.getId());
				bsMngr.setProjectBuildState(project, pbs);
			}

			if(!cfg.getEditableBuilder().isManagedBuildOn()){
				performExternalClean(bInfo, false, monitor);
			} else {
				boolean programmatically = true;
				IPath path = ManagedBuildManager.getBuildFullPath(cfg, bInfo.getBuilder());
				IResource rc = path != null ? ResourcesPlugin.getWorkspace().getRoot().findMember(path) : null;

				if(path == null || (rc != null && rc.getType() != IResource.FILE)){
					if(!cfg.getEditableBuilder().isInternalBuilder()){
						fBuildErrOccured = false;
						try {
							performExternalClean(bInfo, false, monitor);
						} catch (CoreException e) {
							fBuildErrOccured = true;
						}
						if(!fBuildErrOccured)
							programmatically = false;
					}

					if(programmatically){
						try {
							cleanWithInternalBuilder(bInfo, monitor);
						} catch (CoreException e) {
							cleanProgrammatically(bInfo, monitor);
						}
					}
				}
			}
		}

	}

	protected void performExternalClean(final CfgBuildInfo bInfo, boolean separateJob, IProgressMonitor monitor) throws CoreException {
		IResourceRuleFactory ruleFactory= ResourcesPlugin.getWorkspace().getRuleFactory();
		final ISchedulingRule rule = ruleFactory.modifyRule(bInfo.getProject());

		if(separateJob){
			Job backgroundJob = new Job("CDT Common Builder"){  //$NON-NLS-1$
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

							public void run(IProgressMonitor monitor) throws CoreException {
								bInfo.fBuilder.getBuildRunner().invokeBuild(
										CLEAN_BUILD,
										bInfo.getProject(),
										bInfo.getConfiguration(),
										bInfo.getBuilder(),
										bInfo.getConsole(),
										CommonBuilder.this,
										CommonBuilder.this,
										monitor);
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
			bInfo.fBuilder.getBuildRunner().invokeBuild(
					CLEAN_BUILD,
					bInfo.getProject(),
					bInfo.getConfiguration(),
					bInfo.getBuilder(),
					bInfo.getConsole(),
					this,
					this,
					monitor);
		}

	}

	protected boolean shouldCleanProgrammatically(CfgBuildInfo bInfo){
		if(!bInfo.getBuilder().isManagedBuildOn())
			return false;
		return true;
//		IConfiguration cfg = builder.getParent().getParent();
//		IPath path = ManagedBuildManager.getBuildFullPath(cfg, builder);
//		if(path == null)
//			return false;
//
//		return cfg.getOwner().getProject().getFullPath().isPrefixOf(path);
	}

	protected void cleanWithInternalBuilder(CfgBuildInfo bInfo, IProgressMonitor monitor) throws CoreException {
//		referencedProjects = getProject().getReferencedProjects();
		IProject curProject = bInfo.getProject();
		outputTrace(curProject.getName(), "Clean build with Internal Builder requested");	//$NON-NLS-1$
		IConfiguration cfg = bInfo.getConfiguration();
		int flags = BuildDescriptionManager.DEPFILES;
		BuildDescription des = (BuildDescription)BuildDescriptionManager.createBuildDescription(cfg, null, null, flags);

		IBuildStep cleanStep = des.getCleanStep();

		StepBuilder sBuilder = new StepBuilder(cleanStep, null, null);

		try {
			// try the brute force approach first
			StringBuffer buf = new StringBuffer();
			// write to the console
//
//			IConsole console = CCorePlugin.getDefault().getConsole();
//			console.start(getProject());
			IConsole console = bInfo.getConsole();
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];
			consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_CLEAN);
			consoleHeader[1] = cfg.getName();
			consoleHeader[2] = curProject.getName();
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader));
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			buf = new StringBuffer();
			int result = sBuilder.build(consoleOutStream, consoleOutStream, monitor);
			//Throw a core exception indicating that the clean command failed
			if(result == IBuildModelBuilder.STATUS_ERROR_LAUNCH)
			{
				try {
					consoleOutStream.close();
				} catch (IOException e) {
				}
				Status status = new Status(IStatus.INFO, ManagedBuilderCorePlugin.getUniqueIdentifier(),
						"Failed to exec delete command"); //$NON-NLS-1$
				throw new CoreException(status);
			}
			// Report a successful clean
			String successMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, curProject.getName());
			buf.append(successMsg);
			buf.append(System.getProperty("line.separator", "\n"));  //$NON-NLS-1$//$NON-NLS-2$
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			consoleOutStream.close();
			curProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		}  catch (IOException io) {}	//  Ignore console failures...

	}

	protected void cleanProgrammatically(CfgBuildInfo bInfo, IProgressMonitor monitor) throws CoreException {
//		referencedProjects = getProject().getReferencedProjects();
		IProject curProject = bInfo.getProject();
		outputTrace(curProject.getName(), "Clean build requested");	//$NON-NLS-1$
		IBuilder builder = bInfo.getBuilder();
		IConfiguration cfg = bInfo.getConfiguration();
		IPath buildPath = ManagedBuildManager.getBuildFullPath(cfg, builder);
		if(buildPath == null){
			throw new CoreException(new Status(IStatus.ERROR,
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					ManagedMakeMessages.getResourceString("CommonBuilder.0"))); //$NON-NLS-1$
		}

		IPath projectFullPath = curProject.getFullPath();
		if(!projectFullPath.isPrefixOf(buildPath)){
			throw new CoreException(new Status(IStatus.ERROR,
					ManagedBuilderCorePlugin.getUniqueIdentifier(),
					ManagedMakeMessages.getResourceString("CommonBuilder.16"))); //$NON-NLS-1$
		}

		IWorkspace workspace = CCorePlugin.getWorkspace();
		IResource rc = workspace.getRoot().findMember(buildPath);
		if(rc != null){
			if(rc.getType() != IResource.FOLDER){
				throw new CoreException(new Status(IStatus.ERROR,
						ManagedBuilderCorePlugin.getUniqueIdentifier(),
						ManagedMakeMessages.getResourceString("CommonBuilder.12"))); //$NON-NLS-1$
			}

			IFolder buildDir = (IFolder)rc;
			if (!buildDir.isAccessible()){
				outputError(buildDir.getName(), "Could not delete the build directory");	//$NON-NLS-1$
				throw new CoreException(new Status(IStatus.ERROR,
						ManagedBuilderCorePlugin.getUniqueIdentifier(),
						ManagedMakeMessages.getResourceString("CommonBuilder.13"))); //$NON-NLS-1$
			}
		String status;
		try {
			// try the brute force approach first
			status = ManagedMakeMessages.getFormattedString("ManagedMakeBuilder.message.clean.deleting.output", buildDir.getName());	//$NON-NLS-1$
			monitor.subTask(status);
			workspace.delete(new IResource[]{buildDir}, true, monitor);
			StringBuffer buf = new StringBuffer();
			// write to the console
//
//			IConsole console = CCorePlugin.getDefault().getConsole();
//			console.start(getProject());
			IConsole console = bInfo.getConsole();
			ConsoleOutputStream consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];
			consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_CLEAN);
			consoleHeader[1] = cfg.getName();
			consoleHeader[2] = curProject.getName();
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader));
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			buf = new StringBuffer();
			// Report a successful clean
			String successMsg = ManagedMakeMessages.getFormattedString(BUILD_FINISHED, curProject.getName());
			buf.append(successMsg);
			buf.append(System.getProperty("line.separator", "\n"));  //$NON-NLS-1$//$NON-NLS-2$
			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();
			consoleOutStream.close();
		}  catch (IOException io) {}	//  Ignore console failures...
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

	/**
	 * Only lock the workspace is this is a ManagedBuild, or this project references others.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ISchedulingRule getRule(int trigger, Map args) {
		IResource WR_rule = ResourcesPlugin.getWorkspace().getRoot();
		if (needAllConfigBuild() || !isCdtProjectCreated(getProject()))
			return WR_rule;

		// Get the builders to run
		IBuilder builders[] = ManagedBuilderCorePlugin.createBuilders(getProject(), args);
		// Be pessimistic if we referenced other configs
		if (getReferencedConfigs(builders).length > 0)
			return WR_rule;
		// If any builder isManaged => pessimistic
		for (IBuilder builder : builders) {
			if (builder.isManagedBuildOn())
				return WR_rule;
		}

		// Success!
		return null;
	}
}
