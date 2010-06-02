/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildIOType;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.buildmodel.IStepVisitor;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IAdditionalInput;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator2;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCalculator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGeneratorType;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo;
import org.eclipse.cdt.managedbuilder.pdomdepgen.PDOMDependencyGenerator;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

public class BuildDescription implements IBuildDescription {
	private final static String DOT = ".";	//$NON-NLS-1$
	private final static String WILDCARD = "%";	//$NON-NLS-1$
	private final static String VAR_USER_OBJS = "USER_OBJS"; //$NON-NLS-1$
	private final static String VAR_LIBS = "LIBS"; //$NON-NLS-1$


	private Configuration fCfg;
	private IResourceDelta fDelta;
	private IConfigurationBuildState fBuildState;

	private Map<ITool, BuildStep> fToolToMultiStepMap = new HashMap<ITool, BuildStep>();
	private BuildStep fOrderedMultiActions[];

//	private Map<IPath, BuildResource> fLocationToRcMap = new HashMap<IPath, BuildResource>();
	private Map<URI, BuildResource> fLocationToRcMap = new HashMap<URI, BuildResource>();

	private Map<String, Set<BuildIOType>> fVarToAddlInSetMap = new HashMap<String, Set<BuildIOType>>();

	private List<BuildStep> fStepList = new ArrayList<BuildStep>();

	private BuildStep fTargetStep;

	private IManagedBuilderMakefileGenerator fMakeGen;
	private IProject fProject;
	private IManagedBuildInfo fInfo;
	private IPath fTopBuildDirFullPath;
	private IPath fGeneratedPaths[];
	private int fFlags;

	private BuildStep fInputStep;
	private BuildStep fOutputStep;

	private Map<String, ToolOrderEstimation> fToolOrderMap = new HashMap<String, ToolOrderEstimation>();
	private Set<ITool> fToolInProcesSet = new HashSet<ITool>();
	private ITool fOrderedTools[];

	private ICSourceEntry[] fSourceEntries;

//	private Map fExtToToolAndTypeListMap = new HashMap();

	private Map<String, String> fEnvironment;

	private PDOMDependencyGenerator fPdomDepGen;

	private PathSettingsContainer fToolInfos;

	private BuildStep fCleanStep;

	private class ToolInfoHolder {
		Map<String, List<ToolAndType>> fExtToToolAndTypeListMap;
		Map<String, BuildGroup> fInTypeToGroupMap = new HashMap<String, BuildGroup>();
	}

	class ToolAndType{
		ITool fTool;
		IInputType fType;
		String fExt;

		ToolAndType(ITool tool, IInputType inputType, String ext){
			fTool = tool;
			fType = inputType;
			fExt = ext;
		}
	}

	private class RcVisitor implements IResourceProxyVisitor,
										IResourceDeltaVisitor{
		private boolean fPostProcessMode;

		RcVisitor(){
			setMode(false);
		}

		public void setMode(boolean postProcess){
			fPostProcessMode = postProcess;
		}

		public boolean visit(IResourceProxy proxy) throws CoreException {
			try {
				if(proxy.getType() == IResource.FILE){
					doVisitFile(proxy.requestResource());
					return false;
				}

				return !isGenerated(proxy.requestFullPath());
			} catch (CoreException e) {
				throw e;
			} catch (Exception e) {
				String msg = e.getLocalizedMessage();
				if(msg == null)
					msg = ""; //$NON-NLS-1$

				throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), msg, e));
			}
		}

		protected boolean postProcessVisit(IResourceDelta delta){
			IResource rc = delta.getResource();
			if(rc.getType() == IResource.FILE){
				IPath rcLocation = calcResourceLocation(rc);
				BuildResource bRc = (BuildResource)getBuildResource(rcLocation);
				if(bRc != null){
					if(bRc.getProducerIOType() != null
							&& bRc.getProducerIOType().getStep() == fInputStep){
						if(delta.getKind() == IResourceDelta.REMOVED){
							if(checkFlags(BuildDescriptionManager.REMOVED)){
								bRc.setRemoved(true);
							}
						} else	{
							if(checkFlags(BuildDescriptionManager.REBUILD)){
								bRc.setRebuildState(true);
							}
						}
					} else {
						if(checkFlags(BuildDescriptionManager.REBUILD)){
							bRc.setRebuildState(true);
							IBuildIOType type = bRc.getProducerIOType();
							if(type != null){
								((BuildStep)type.getStep()).setRebuildState(true);
							}
						}
					}
				}
				return false;
			}
			return true;
		}

		public boolean removedCalcVisit(IResourceDelta delta) throws CoreException {
			IResource rc = delta.getResource();
			if(rc.getType() == IResource.FILE){
				if(!isGenerated(rc.getFullPath())){
					//this is a project source, check the removed state
					if(delta.getKind() == IResourceDelta.REMOVED
							&& checkFlags(BuildDescriptionManager.REMOVED)){
						IPath rcLocation = calcResourceLocation(rc);
						BuildResource bRc = (BuildResource)getBuildResource(rcLocation);

						if(bRc == null){
							doVisitFile(rc);
						}
					}
				}
				return false;
			}

			return true;//!isGenerated(rc.getFullPath());
		}


		public boolean visit(IResourceDelta delta) throws CoreException {
			if(fPostProcessMode)
				return postProcessVisit(delta);
			return removedCalcVisit(delta);
		}

		private void doVisitFile(IResource res) throws CoreException{
			BuildResource rc = createResource(res);

			composeOutputs(fInputStep, null, rc);

		}

	}

	protected IPath calcResourceLocation(IResource rc){

	//return rc.getFullPath();
		IPath rcLocation = rc.getLocation();
		if(rcLocation == null){
			IPath fullPath = rc.getFullPath();
			rcLocation = calcLocationForFullPath(fullPath);
//			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//			IProject proj = root.getProject(fullPath.segment(0));
//			rcLocation = proj.getLocation();
//			if(rcLocation != null){
//				rcLocation = rcLocation.append(fullPath.removeFirstSegments(1));
//			} else {
//				rcLocation = root.getLocation().append(fullPath);
//			}
		}
		return rcLocation;
	}

//	private class StepCollector implements IStepVisitor{
//		private Set<IBuildStep> fStepSet = new HashSet<IBuildStep>();
//
//		public int visit(IBuildStep action) throws CoreException {
//			if(DbgUtil.DEBUG){
//				DbgUtil.trace("StepCollector: visiting step " + DbgUtil.stepName(action));	//$NON-NLS-1$
//			}
//			fStepSet.add(action);
//			return VISIT_CONTINUE;
//		}
//
//		public BuildStep[] getSteps(){
//			return (BuildStep[])fStepSet.toArray(new BuildStep[fStepSet.size()]);
//		}
//
//		public Set getStepSet(){
//			return fStepSet;
//		}
//
//		public void clear(){
//			fStepSet.clear();
//		}
//	}

	private class RebuildStateSynchronizer implements IStepVisitor{

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.builddescription.IStepVisitor#visit(org.eclipse.cdt.managedbuilder.builddescription.IBuildStep)
		 */
		public int visit(IBuildStep a) throws CoreException {
			BuildStep action = (BuildStep)a;
			BuildResource rcs[] = (BuildResource[])action.getInputResources();
			boolean rebuild = action.needsRebuild();
			boolean removed = action.isRemoved();

			if(DbgUtil.DEBUG){
				DbgUtil.trace(">>visiting step " + DbgUtil.stepName(a));	//$NON-NLS-1$
			}

			if(!removed){
				BuildIOType args[] = action.getPrimaryTypes(true);
				int j = 0;
				if(args.length > 0){
					for(j = 0; j < args.length; j++){
						BuildResource[] ress = (BuildResource[])args[j].getResources();
						if(ress.length > 0){
							int k = 0;
							for(k = 0; k < ress.length; k++){
								if(!ress[k].isRemoved())
									break;
							}
							if(k != ress.length)
								break;
						}
					}
					if(j == args.length)
						removed = true;
				}

			}

			if(!removed && !rebuild){
				for (BuildResource rc : rcs) {
					if(rc.needsRebuild()){
						if(DbgUtil.DEBUG)
							DbgUtil.trace("resource " + locationToRel(rc.getLocation()).toString() + " needs rebuild");	//$NON-NLS-1$	//$NON-NLS-2$
						rebuild = true;
						break;
					} else if(rc.isRemoved()){
						if(DbgUtil.DEBUG)
							DbgUtil.trace("resource " + locationToRel(rc.getLocation()).toString() + " is removed");	//$NON-NLS-1$ 	//$NON-NLS-2$
						rebuild = true;
						break;
					}
				}
			}

			if(removed){
				if(DbgUtil.DEBUG)
					DbgUtil.trace("action to be removed");	//$NON-NLS-1$

				action.setRemoved();

				for (IBuildResource outRc : action.getOutputResources()) {
					if(DbgUtil.DEBUG)
						DbgUtil.trace("setting remove state for resource " + locationToRel(outRc.getLocation()).toString());	//$NON-NLS-1$

					((BuildResource)outRc).setRemoved(true);
				}

			} else if(rebuild){
				if(DbgUtil.DEBUG)
					DbgUtil.trace("action needs rebuild");	//$NON-NLS-1$

				action.setRebuildState(true);

				for (IBuildResource outRc : action.getOutputResources()) {
					if(DbgUtil.DEBUG)
						DbgUtil.trace("setting rebuild state for resource " + locationToRel(outRc.getLocation()).toString());	//$NON-NLS-1$

					((BuildResource)outRc).setRebuildState(true);
				}
			}

			if(DbgUtil.DEBUG)
				DbgUtil.trace("<<leaving..");	//$NON-NLS-1$

			return VISIT_CONTINUE;
		}
	}

	private class ToolOrderEstimation {
		private ITool fTool;
		private ITool fDeps[];
		private ITool fConsumers[];

		private ToolOrderEstimation(ITool tool){
			fTool = tool;
		}

//		ITool getTool(){
//			return fTool;
//		}

		ITool[] getDeps(){
			if(fDeps == null)
				fDeps = doCalcDeps(fTool);
			return fDeps;
		}

		ITool[] getConsumers(){
			if(fConsumers == null)
				fConsumers = doCalcConsumers(fTool);
			return fConsumers;
		}

//		boolean dependsOn(ITool tool){
//			return indexOf(tool, getDeps()) != -1;
//		}
//
//		boolean hasConsumer(ITool tool){
//			return indexOf(tool, getConsumers()) != -1;
//		}

	}

	protected BuildDescription(){

	}

	public BuildDescription(IConfiguration cfg){
		initBase(cfg, null, null, 0);
	}

	public void synchRebuildState() throws CoreException{
		if(DbgUtil.DEBUG)
			DbgUtil.trace("--->Synch started");	//$NON-NLS-1$

		BuildDescriptionManager.accept(new RebuildStateSynchronizer(), this, true);

		if(fOutputStep.needsRebuild())
			fInputStep.setRebuildState(true);//needed for the pre-build step invocation

		if(DbgUtil.DEBUG)
			DbgUtil.trace("<---Synch stopped");	//$NON-NLS-1$
	}

	private BuildIOType findTypeForExtension(BuildStep step, boolean input, String ext){
		if(ext == null)
			return null;

		BuildIOType types[] = input ? (BuildIOType[])step.getInputIOTypes() : (BuildIOType[])step.getOutputIOTypes();

		for (BuildIOType type : types) {
			for (IBuildResource rc : type.getResources()) {
				String e = rc.getLocation().getFileExtension();
				if(e == null){
					if(ext.length() == 0)
						return type;
				} else {
					if(ext.equals(e))
						return type;
				}
			}
		}
		return null;
	}

	private Map<String, List<ToolAndType>> initToolAndTypeMap(IFolderInfo foInfo){
		Map<String, List<ToolAndType>> extToToolAndTypeListMap = new HashMap<String, List<ToolAndType>>();
		for (ITool tool : foInfo.getFilteredTools()) {
			IInputType types[] = tool.getInputTypes();
			if(types.length != 0){
				for (IInputType type : types) {
					for (String ext : type.getSourceExtensions(tool)) {
						if(tool.buildsFileType(ext)){
							List<ToolAndType> list = extToToolAndTypeListMap.get(ext);
							if(list == null){
								list = new ArrayList<ToolAndType>();
								extToToolAndTypeListMap.put(ext, list);
							}
							list.add(new ToolAndType(tool, type, ext));
						}
					}
				}
			} else {
				for (String ext : tool.getAllInputExtensions()) {
					if(tool.buildsFileType(ext)){
						List<ToolAndType> list = extToToolAndTypeListMap.get(ext);
						if(list == null){
							list = new ArrayList<ToolAndType>();
							extToToolAndTypeListMap.put(ext, list);
						}
						list.add(new ToolAndType(tool, null, ext));
					}
				}
			}
		}
		return extToToolAndTypeListMap;
	}

	ToolAndType getToolAndType(BuildResource rc, boolean checkVar){
		ToolInfoHolder h = getToolInfo(rc);
		return getToolAndType(h, rc, checkVar);
	}

	ToolAndType getToolAndType(ToolInfoHolder h, BuildResource rc, boolean checkVar){
		String locString = rc.getLocation().toString();
		BuildIOType arg = (BuildIOType)rc.getProducerIOType();
		String linkId = (checkVar && arg != null) ? arg.getLinkId() : null;

		for (Entry<String, List<ToolAndType>> entry : h.fExtToToolAndTypeListMap.entrySet()) {
			String ext = entry.getKey();
			if(locString.endsWith("." + ext)){	//$NON-NLS-1$
				List<ToolAndType> list = entry.getValue();
				for (ToolAndType tt : list) {
					if(!checkVar)
						return tt;

					IInputType type = tt.fType;
					if(type == null)
						return tt;

					String var = type.getBuildVariable();
					if(var == null || var.length() == 0)
						return tt;

					if(linkId != null && linkId.equals(var)){
						return tt;
					}
				}
			}
		}

		return null;
	}

	protected boolean isSource(IPath path){
		return !CDataUtil.isExcluded(path, fSourceEntries);
//
//		path = path.makeRelative();
//		for(int i = 0; i < fSourcePaths.length; i++){
//			if(fSourcePaths[i].isPrefixOf(path))
//				return true;
//		}
//		return false;
	}


	private void composeOutputs(BuildStep inputAction, BuildIOType inputActionArg, BuildResource rc) throws CoreException{

		boolean isSource = inputActionArg == null;
		if(isSource){
			if(rc.isProjectResource()
					&& !isSource(rc.getFullPath().removeFirstSegments(1).makeRelative()))
				return;
		} else {
			if(inputAction != null && inputAction == fTargetStep){
				BuildIOType arg = (BuildIOType)rc.getProducerIOType();
				if(arg.isPrimary()){
					BuildIOType oArg = findTypeForExtension(fOutputStep,true,rc.getLocation().getFileExtension());
					if(oArg == null || !arg.isPrimary())
						oArg = fOutputStep.createIOType(true, true, null);
					oArg.addResource(rc);
				}

				return;
			} else {
				for (IOutputType secondaryOutput : fCfg.getToolChain().getSecondaryOutputs()) {
					if(inputActionArg!=null && secondaryOutput==inputActionArg.getIoType()){
						BuildIOType arg = findTypeForExtension(fOutputStep,true,rc.getLocation().getFileExtension());
						if(arg == null || arg.isPrimary()){
							arg = fOutputStep.createIOType(true, false, null);
						}
						arg.addResource(rc);
					}
				}
			}
		}

		IPath location = rc.getLocation();

		IResourceInfo rcInfo = rc.isProjectResource() ?
				fCfg.getResourceInfo(rc.getFullPath().removeFirstSegments(1), false) :
					fCfg.getRootFolderInfo();
		ITool tool = null;
		IInputType inputType = null;
		String ext = null;
		boolean stepRemoved = false;
		if(rcInfo.isExcluded()){
			if(rcInfo.needsRebuild())
				stepRemoved = true;
			else
				return;
		}

		ToolInfoHolder h = null;
		if(rcInfo instanceof IFileInfo){
			IFileInfo fi = (IFileInfo)rcInfo;
			ITool[] tools = fi.getToolsToInvoke();
			if(tools.length > 0 )
			{
			    tool = fi.getToolsToInvoke()[0];
			    String locString = location.toString();
			    for (String e : tool.getAllInputExtensions()) {
			        if(locString.endsWith(e)){
			            inputType = tool.getInputType(e);
			            ext = e;
			        }
			    }
			}
		} else {
			h = getToolInfo(rc);
			ToolAndType tt = getToolAndType(h, rc, true);
			if(tt != null){
				tool = tt.fTool;
				inputType = tt.fType;
				ext = tt.fExt;
			}

		}

		if(ext == null)
			ext = location.getFileExtension();

		if(tool != null) {
			//  Generate the step to build this source file
			IInputType primaryInputType = tool.getPrimaryInputType();
			if ((primaryInputType != null && !primaryInputType.getMultipleOfType()) ||
				(inputType == null && tool != fCfg.calculateTargetTool())){

				BuildStep action = null;
				BuildIOType argument = null;
				BuildGroup group = null;
				if(h != null)
					group = createGroup(h, inputType, ext);

				action = createStep(tool, inputType);//new BuildStep(this, tool, inputType);
				if(stepRemoved)
					action.setRemoved();
				if(group != null)
					group.addAction(action);
				argument = action.createIOType(true, true, inputType);

				argument.addResource(rc);

				if(inputActionArg == null){
					inputActionArg = findTypeForExtension(inputAction,false,rc.getLocation().getFileExtension());
					if(inputActionArg == null && inputAction!=null)
						inputActionArg = inputAction.createIOType(false, false, null);
					if (inputActionArg!=null)
						inputActionArg.addResource(rc);
				}

				calculateInputs(action);

				calculateOutputs(action, argument, rc);

				BuildIOType outputs[] = (BuildIOType[])action.getOutputIOTypes();

				for (BuildIOType output : outputs) {
					BuildResource rcs[] = (BuildResource[])output.getResources();
					for (BuildResource outputRc : rcs) {
						composeOutputs(action, output, outputRc);
					}
				}
			} else {

				if(inputType != null ? inputType.getMultipleOfType() : tool == fCfg.calculateTargetTool()){
					BuildStep step = fToolToMultiStepMap.get(tool);

					if(step != null){
						BuildIOType argument = step.getIOTypeForType(inputType, true);
						if(argument == null)
							argument = step.createIOType(true, true, inputType);

						argument.addResource(rc);

						if(inputActionArg == null){
							inputActionArg = findTypeForExtension(inputAction,false,rc.getLocation().getFileExtension());
							if(inputActionArg == null && inputAction!=null)
								inputActionArg = inputAction.createIOType(false, false, null);
							if (inputActionArg!=null)
								inputActionArg.addResource(rc);
						}
					}
				} else {

				}
			}
		}
	}

	private BuildGroup createGroup(ToolInfoHolder h, IInputType inType, String ext){
		String key = inType != null ?
				inType.getId() : "ext:"+ext;	//$NON-NLS-1$
		BuildGroup group = h.fInTypeToGroupMap.get(key);
		if(group == null){
			group = new BuildGroup();
			h.fInTypeToGroupMap.put(key, group);
		}
		return group;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescription#getInputStep()
	 */
	public IBuildStep getInputStep() {
		return fInputStep;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescription#getOutputStep()
	 */
	public IBuildStep getOutputStep() {
		return fOutputStep;
	}

	public boolean checkFlags(int flags){
		return (fFlags & flags) == flags;
	}

	protected void initBase(IConfiguration cfg, IConfigurationBuildState bs, IResourceDelta delta, int flags){
		fCfg = (Configuration)cfg;
		fDelta = delta;
		fBuildState = bs;
		fProject = cfg.getOwner().getProject();
		fInfo = ManagedBuildManager.getBuildInfo(fProject);
		fFlags = flags;

		fSourceEntries = fCfg.getSourceEntries();
		if(fSourceEntries.length == 0){
			fSourceEntries = new ICSourceEntry[]{new CSourceEntry(Path.EMPTY, null, ICSettingEntry.RESOLVED | ICSettingEntry.VALUE_WORKSPACE_PATH)};
		} else {
			ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(cfg);
			fSourceEntries = CDataUtil.resolveEntries(fSourceEntries, cfgDes);
		}
		fInputStep = createStep(null,null);
		fOutputStep = createStep(null,null);
	}

	protected void initDescription() throws CoreException{
		if(fCfg.needsFullRebuild())
			fInputStep.setRebuildState(true);

		if(fBuildState != null && fBuildState.getState() == IRebuildState.NEED_REBUILD)
			fInputStep.setRebuildState(true);

		initToolInfos();

		initMultiSteps();

		RcVisitor visitor = new RcVisitor();
		fProject.accept(visitor, IResource.NONE);


		if(checkFlags(BuildDescriptionManager.REMOVED)
				&& fDelta != null)
			fDelta.accept(visitor);

		handleMultiSteps();

		visitor.setMode(true);
		if((checkFlags(BuildDescriptionManager.REMOVED)
				|| checkFlags(BuildDescriptionManager.REBUILD))){
			if(fDelta != null)
				fDelta.accept(visitor);
			if(fBuildState != null)
				processBuildState();
		}

		completeLinking();
		synchRebuildState();
		//TODO: trim();
	}

	protected void processBuildState(){
		IPath paths[] = fBuildState.getFullPathsForState(IRebuildState.NEED_REBUILD);
		processBuildState(IRebuildState.NEED_REBUILD, paths);

		paths = fBuildState.getFullPathsForState(IRebuildState.REMOVED);
		processBuildState(IRebuildState.REMOVED, paths);
	}

	protected void processBuildState(int state, IPath fullPaths[]){
		for (IPath fullPath : fullPaths) {
			processBuildState(state, fullPath);
		}
	}

	protected void processBuildState(int state, IPath fullPath){
		BuildResource bRc = (BuildResource)getBuildResourceForFullPath(fullPath);
		if(bRc == null)
			return;

		if(bRc.getProducerIOType() != null
				&& bRc.getProducerIOType().getStep() == fInputStep){
			if(state == IRebuildState.REMOVED){
				if(checkFlags(BuildDescriptionManager.REMOVED)){
					bRc.setRemoved(true);
				}
			} else if (state == IRebuildState.NEED_REBUILD){
				if(checkFlags(BuildDescriptionManager.REBUILD)){
					bRc.setRebuildState(true);
				}
			}
		} else {
			if(state == IRebuildState.NEED_REBUILD
					|| state == IRebuildState.REMOVED
					|| checkFlags(BuildDescriptionManager.REBUILD)){
				bRc.setRebuildState(true);
				IBuildIOType type = bRc.getProducerIOType();
				if(type != null){
					((BuildStep)type.getStep()).setRebuildState(true);
				}
			}
		}
	}

	protected void init(IConfiguration cfg, IConfigurationBuildState bs, IResourceDelta delta, int flags) throws CoreException {
		initBase(cfg, bs, delta, flags);

		initDescription();
	}

	protected void stepRemoved(BuildStep step){
		fStepList.remove(step);

		if(fTargetStep == step){
			fTargetStep = null;
		}
	}

	public BuildResource[][] removeStep(BuildStep step){
		return step.remove();
	}

	public BuildIOType[][] removeResource(BuildResource rc){
		return rc.remove();
	}

	private void handleMultiSteps() throws CoreException{
		for (BuildStep action : fOrderedMultiActions) {
			calculateInputs(action);

			calculateOutputs(action, action.getPrimaryTypes(true)[0], null);

			if(action.getOutputResources().length == 0){
				removeStep(action);
			}
			BuildIOType args[] =  (BuildIOType[])action.getOutputIOTypes();

			for (BuildIOType arg : args) {
				BuildResource rcs[] = (BuildResource[])arg.getResources();
				for (BuildResource rc : rcs) {
					composeOutputs(action, arg, rc);
				}
			}
		}
	}

	private void initMultiSteps(){
		ITool targetTool = fCfg.calculateTargetTool();

		for (ITool tool : fCfg.getFilteredTools()) {
			IInputType type = tool.getPrimaryInputType();
			BuildStep action = null;
			if(type != null ? type.getMultipleOfType() : tool == targetTool){
				action = createStep(tool,type);//new BuildStep(this, tool, type);
				action.createIOType(true, true, type);
				fToolToMultiStepMap.put(tool, action);
			}

		}

		fOrderedMultiActions = new BuildStep[fToolToMultiStepMap.size()];
		int index = 0;
		for (ITool orderedTool : getOrderedTools()) {
			BuildStep action = fToolToMultiStepMap.get(orderedTool);
			if(action != null)
				fOrderedMultiActions[index++] = action;
		}
	}



	private void completeLinking() throws CoreException{
		boolean foundUnused = false;

		do{
			BuildStep steps[] = (BuildStep[])getSteps();
			foundUnused = false;
			for (BuildStep step : steps) {
				if(step == fOutputStep || step == fInputStep)
					continue;

				IBuildResource rcs[] = step.getResources(false);
				int i;
				for(i = 0; i < rcs.length; i++){
					if(rcs[i].getDependentIOTypes().length != 0)
						break;
				}
				if(i == rcs.length){
					if(DbgUtil.DEBUG){
						DbgUtil.trace("unused step found: " + DbgUtil.stepName(step));	//$NON-NLS-1$
					}

					foundUnused = true;
					if(step.needsRebuild()
							&& step.getTool() != null
							&& step.getTool().getCustomBuildStep()){
						if(DbgUtil.DEBUG){
							DbgUtil.trace("unused step is an RCBS needing rebuild, settings input step rebuild state to true");	//$NON-NLS-1$
						}
						fInputStep.setRebuildState(true);
					}
					removeStep(step);
				}
			}
		}while(foundUnused);

		Set<Entry<URI, BuildResource>> set = fLocationToRcMap.entrySet();
		List<BuildResource> list = new ArrayList<BuildResource>();
		for (Entry<URI, BuildResource> entry : set) {
			BuildResource rc = entry.getValue();
			boolean doRemove = false;
			BuildIOType producerArg = (BuildIOType)rc.getProducerIOType();
			if(producerArg == null){
				if(rc.getDependentIOTypes().length == 0)
					doRemove = true;
				else {
					producerArg = findTypeForExtension(fInputStep,false,rc.getLocation().getFileExtension());
					if(producerArg == null)
						producerArg = fInputStep.createIOType(false, false, null);
					producerArg.addResource(rc);
				}
			} else if(producerArg.getStep() == fInputStep
						&& rc.getDependentIOTypes().length == 0) {
					doRemove = true;
			}

			if(doRemove)
				list.add(rc);
		}

		for (BuildResource buildResource : list) {
			BuildIOType[][] types = removeResource(buildResource);

			BuildIOType producer = types[0][0];
			if(producer != null && producer.getResources().length == 0){
				((BuildStep)producer.getStep()).removeIOType(producer);
			}

			BuildIOType deps[] = types[1];
			for (BuildIOType dep : deps) {
				if(dep.getResources().length == 0)
					((BuildStep)dep.getStep()).removeIOType(dep);
			}
		}

	}

	protected void resourceRemoved(BuildResource rc){
		fLocationToRcMap.remove(rc.getLocationURI());
	}

	protected void resourceCreated(BuildResource rc){
		fLocationToRcMap.put(rc.getLocationURI(), rc);
	}

	private IManagedBuilderMakefileGenerator getMakeGenInitialized(){
		if(fMakeGen == null){
			fMakeGen = ManagedBuildManager.getBuildfileGenerator(fCfg);
			if(fMakeGen instanceof IManagedBuilderMakefileGenerator2)
				((IManagedBuilderMakefileGenerator2)fMakeGen).initialize(IncrementalProjectBuilder.FULL_BUILD, fCfg, fCfg.getEditableBuilder(), new NullProgressMonitor());
			else
				fMakeGen.initialize(fProject, fInfo, null);
		}
		return fMakeGen;
	}

	private IPath getTopBuildDirFullPath(){
		if(fTopBuildDirFullPath == null)
			fTopBuildDirFullPath = fProject.getFullPath().append(getMakeGenInitialized().getBuildWorkingDir()).addTrailingSeparator();
		return fTopBuildDirFullPath;
	}

	private IPath getTopBuildDirLocation(){
		IPath projLocation = getProjectLocation();
		return projLocation.append(getTopBuildDirFullPath().removeFirstSegments(1));
	}

	private URI getTopBuildDirLocationURI(){
		return org.eclipse.core.runtime.URIUtil.makeAbsolute(URIUtil.toURI(getTopBuildDirFullPath().removeFirstSegments(1)),
															fProject.getLocationURI());
	}

	private IPath getProjectLocation() {
		return new Path(fProject.getLocationURI().getPath());
	}

	private BuildResource[] addOutputs(IPath paths[], BuildIOType buildArg, IPath outDirPath){
		if(paths != null){
			List<BuildResource> list = new ArrayList<BuildResource>();
			for (IPath path : paths) {
				IPath outFullPath = path;
				IPath outWorkspacePath = path;
				IPath outProjPath;
				IPath projLocation = new Path(fProject.getLocationURI().getPath());

				if(outFullPath.isAbsolute()){
					outProjPath = outFullPath;

					if(projLocation.isPrefixOf(outProjPath)) {
						// absolute location really points to same place the project lives, so it IS a project file
						outProjPath = outProjPath.removeFirstSegments(projLocation.segmentCount());
						outFullPath = projLocation.append(outProjPath.removeFirstSegments(projLocation.segmentCount()));
						outWorkspacePath = fProject.getFullPath().append(outProjPath);
					}
					else {
						// absolute path to somewhere outside the workspace
						outProjPath = null;
						outWorkspacePath = null;
					}
				} else {
					if (outFullPath.segmentCount() == 1) {
						outFullPath = projLocation.append(outDirPath.removeFirstSegments(1).append(outFullPath));
						outProjPath = outDirPath.removeFirstSegments(1).append(outWorkspacePath);
						outWorkspacePath = fProject.getFullPath().append(outProjPath);
					} else {
						outProjPath = fProject.getFullPath().removeFirstSegments(1).append(outDirPath.removeFirstSegments(1).append(outWorkspacePath));

						if(outDirPath.isPrefixOf(outFullPath)) {
							outFullPath.removeFirstSegments(outDirPath.segmentCount());
						}

						outFullPath = projLocation.append(outDirPath.removeFirstSegments(1).append(outFullPath.lastSegment()));
						outWorkspacePath = fProject.getFullPath().append(outProjPath);
					}
				}

				BuildResource outRc = createResource(outWorkspacePath, getURIForFullPath(outFullPath));
				list.add(outRc);
				buildArg.addResource(outRc);

			}
			return list.toArray(new BuildResource[list.size()]);
		}
		return null;
	}

	private URI getURIForFullPath(IPath fullPath) {
		// Basically, assume that we use the same type of URI that the project uses.
		// Create one using the same info, except point the path at the path provided.
		URI projURI = fProject.getLocationURI();

		try {
			IFileStore projStore = EFS.getStore(projURI);

			if(projStore.toLocalFile(EFS.NONE, null) != null) {
				// local file
				return URIUtil.toURI(fullPath);
			}
		} catch (CoreException e1) {
			ManagedBuilderCorePlugin.log(e1);
			return null;
		}

		try {
			URI newURI = new URI(projURI.getScheme(), projURI.getUserInfo(),
					projURI.getHost(), projURI.getPort(), fullPath.toString(), projURI.getQuery(), projURI
							.getFragment());
			return newURI;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private void calculateOutputs(BuildStep action, BuildIOType arg, BuildResource buildRc) throws CoreException {
		BuildResource rcs[] = null;
		ITool tool = action.getTool();

		boolean isMultiAction = action.isMultiAction();

		IPath resPath = null;

		if(!isMultiAction){
			resPath = buildRc.getFullPath();
			if(resPath == null)
				resPath = buildRc.getLocation();
		} else {
			rcs = (BuildResource[])action.getPrimaryTypes(true)[0].getResources();
			if(rcs.length == 0)
				return;
		}

		IPath outDirPath = isMultiAction ?
				getTopBuildDirFullPath() :
					buildRc.getProducerIOType().getStep() == fInputStep ?
							getTopBuildDirFullPath().append(resPath.removeFirstSegments(1).removeLastSegments(1)).addTrailingSeparator() :
								resPath.removeLastSegments(1).addTrailingSeparator();
		IInputType inType = (IInputType)arg.getIoType();
		String linkId = inType != null ? inType.getBuildVariable() : null;
		if(linkId != null && linkId.length() == 0)
			linkId = null;

		IOutputType[] outTypes = tool.getOutputTypes();
		//  1.  If the tool is the build target and this is the primary output,
		//      use artifact name & extension
		if (fTargetStep == action){
			String artifactName = fCfg.getArtifactName();
			try {
				String tmp = ManagedBuildManager.getBuildMacroProvider().resolveValue(artifactName, "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, fCfg);	//$NON-NLS-1$	//$NON-NLS-2$
				if((tmp = tmp.trim()).length() > 0)
					artifactName = tmp;
			} catch (BuildMacroException e){
			}

			String artifactExt = fCfg.getArtifactExtension();
			try {
				String tmp = ManagedBuildManager.getBuildMacroProvider()
								.resolveValue(artifactExt, "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, fCfg);	//$NON-NLS-1$	//$NON-NLS-2$
				if((tmp = tmp.trim()).length() > 0)
					artifactExt = tmp;
			} catch (BuildMacroException e) {
			}

			String artifactPrefix = tool.getOutputPrefix();
			if(artifactPrefix != null && artifactPrefix.length() != 0){
				try {
					String tmp = ManagedBuildManager.getBuildMacroProvider().resolveValue(artifactPrefix, "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, fCfg);	//$NON-NLS-1$	//$NON-NLS-2$
					if((tmp = tmp.trim()).length() > 0)
						artifactPrefix = tmp;
				} catch (BuildMacroException e){
				}
				artifactName = artifactPrefix + artifactName;
			}

			IPath path = new Path(artifactName);
			if(artifactExt != null && artifactExt.length() != 0)
				path = path.addFileExtension(artifactExt);

			IOutputType type = action.getTool().getPrimaryOutputType();
			BuildIOType ioType = action.getIOTypeForType(type, false);
			if(ioType == null)
				ioType = action.createIOType(false, true, type);
			addOutputs(new IPath[]{path}, ioType, outDirPath);
		} else if (outTypes != null && outTypes.length > 0) {
			for (IOutputType type : outTypes) {
				boolean primaryOutput = (type == tool.getPrimaryOutputType());
				String outputPrefix = type.getOutputPrefix();
				String[] pathStrings = null;
				IPath[] paths = null;

                // Resolve any macros in the outputPrefix
                // Note that we cannot use file macros because if we do a clean
                // we need to know the actual name of the file to clean, and
                // cannot use any builder variables such as $@. Hence we use the
                // next best thing, i.e. configuration context.

                // figure out the configuration we're using
                IBuildObject toolParent = tool.getParent();
                IConfiguration config = null;
                // if the parent is a config then we're done
                if (toolParent instanceof IConfiguration)
                    config = (IConfiguration) toolParent;
                else if (toolParent instanceof IToolChain) {
                    // must be a toolchain
                    config = ((IToolChain) toolParent).getParent();
                }

                else if (toolParent instanceof IResourceConfiguration) {
                    config = ((IResourceConfiguration) toolParent).getParent();
                }

                else {
                    // bad
                    throw new AssertionError(
                            "tool parent must be one of configuration, toolchain, or resource configuration");	//$NON-NLS-1$
                }

                if (config != null) {

                    try {
//TODO
                    		outputPrefix = ManagedBuildManager
                            .getBuildMacroProvider()
                            .resolveValue(
                                    outputPrefix,
                                    "", //$NON-NLS-1$
                                    " ", //$NON-NLS-1$
                                    IBuildMacroProvider.CONTEXT_CONFIGURATION,
                                    config);
                    }
                    catch (BuildMacroException e) {
                    }

                }

				boolean multOfType = type.getMultipleOfType();
				IOption option = tool.getOptionBySuperClassId(type.getOptionId());
				IManagedOutputNameProvider nameProvider = type.getNameProvider();
				String[] outputNames = type.getOutputNames();
				BuildIOType buildArg = null;


				//  2.  If an option is specified, use the value of the option

					if (option != null) {
					try {
						int optType = option.getValueType();
						if (optType == IOption.STRING) {
							String val = option.getStringValue();
							if(val != null && val.length() > 0){

								// try to resolve the build macros in the output
								// names
								try {

//TODO
									val = ManagedBuildManager
												.getBuildMacroProvider()
												.resolveValue(
														val,
														"", //$NON-NLS-1$
														" ", //$NON-NLS-1$
														IBuildMacroProvider.CONTEXT_FILE,
														new FileContextData(
																resPath,
																null, option, tool));
								} catch (BuildMacroException e){
								}

								if((val = val.trim()).length() > 0){
									pathStrings = new String[]{outputPrefix + val};
								}
							}
						} else if (
								optType == IOption.STRING_LIST ||
								optType == IOption.LIBRARIES ||
								optType == IOption.OBJECTS ||
								optType == IOption.INCLUDE_FILES ||
								optType == IOption.LIBRARY_PATHS ||
								optType == IOption.LIBRARY_FILES ||
								optType == IOption.MACRO_FILES ||
								optType == IOption.UNDEF_INCLUDE_PATH ||
								optType == IOption.UNDEF_PREPROCESSOR_SYMBOLS ||
								optType == IOption.UNDEF_INCLUDE_FILES ||
								optType == IOption.UNDEF_LIBRARY_PATHS ||
								optType == IOption.UNDEF_LIBRARY_FILES ||
								optType == IOption.UNDEF_MACRO_FILES) {
							List<String> outputList = (List<String>)option.getValue();
							// Add outputPrefix to each if necessary
							if(outputList != null && outputList.size() > 0){
//TODO
							try{
								pathStrings = ManagedBuildManager
								.getBuildMacroProvider()
								.resolveStringListValues(
										outputList.toArray(new String[outputList.size()]),
										"", //$NON-NLS-1$
										" ", //$NON-NLS-1$
										IBuildMacroProvider.CONTEXT_FILE,
										new FileContextData(
												resPath,
												null, option, tool));
							} catch (BuildMacroException e){
							}
							}


							if(pathStrings != null && pathStrings.length > 0 && outputPrefix.length() > 0){
								for (int j=0; j<pathStrings.length; j++) {
									if(pathStrings[j] == null && (pathStrings[j] = pathStrings[j].trim()).length() == 0)
										pathStrings[j] = null;
									else
										pathStrings[j] = outputPrefix + pathStrings[j];
								}
							}

						}
					} catch( BuildException ex ) {}
				} else
				//  3.  If a nameProvider is specified, call it
				if (nameProvider != null) {
					IPath[] inPaths;
					if(buildRc != null){
						inPaths = new Path[1];
						inPaths[0] = buildRc.getLocation();
					} else {
						inPaths = new Path[rcs.length];
						for(int k = 0; k < inPaths.length; k++){
							inPaths[k] = rcs[k].getLocation();
						}
					}
					paths = nameProvider.getOutputNames(tool, inPaths);
				} else
				//  4.  If outputNames is specified, use it
				if (outputNames != null) {
					try{
						pathStrings = ManagedBuildManager
						.getBuildMacroProvider()
						.resolveStringListValues(
								outputNames,
								"", //$NON-NLS-1$
								" ", //$NON-NLS-1$
								IBuildMacroProvider.CONTEXT_FILE,
								new FileContextData(
										resPath,
										null, option, tool));
					} catch (BuildMacroException e){
					}

				} else {
				//  5.  Use the name pattern to generate a transformation macro
				//      so that the source names can be transformed into the target names
				//      using the built-in string substitution functions of <code>make</code>.
					if (multOfType || isMultiAction) {
						// This case is not handled - a nameProvider or outputNames must be specified
						// TODO - report error
					} else {
						String namePattern = type.getNamePattern();
						IPath namePatternPath = null;
						String inExt = resPath.getFileExtension();
						String outExt = tool.getOutputExtension(inExt);
						if (namePattern == null || namePattern.length() == 0) {
							namePattern = /*outDirPath.toOSString() +*/ outputPrefix + IManagedBuilderMakefileGenerator.WILDCARD;
							if (outExt != null && outExt.length() > 0) {
								namePattern += DOT + outExt;
							}
							namePatternPath = Path.fromOSString(namePattern);
						}
						else {
							if (outputPrefix.length() > 0) {
								namePattern = outputPrefix + namePattern;
							}
							namePatternPath = Path.fromOSString(namePattern);
							//  If only a file name is specified, add the relative path of this output directory
							if (namePatternPath.segmentCount() == 1) {
								namePatternPath = Path.fromOSString(/*outDirPath.toOSString() +*/ namePatternPath.toString());
							}
						}

						paths = new IPath[]{resolvePercent(namePatternPath, buildRc.getLocation())};

					}
				}

				if(paths == null && pathStrings != null){
					paths = new IPath[pathStrings.length];
					for(int k = 0; k < pathStrings.length; k++){
						paths[k] = Path.fromOSString(pathStrings[k]);
					}
				}


				if(paths != null){
					buildArg = action.createIOType(false, primaryOutput, type);
					addOutputs(paths, buildArg, outDirPath);
				}

			}
		} else {
			// For support of pre-CDT 3.0 integrations.
			// NOTE WELL:  This only supports the case of a single "target tool"
			//     that consumes exactly all of the object files, $OBJS, produced
			//     by other tools in the build and produces a single output.
			//     In this case, the output file name is the input file name with
			//     the output extension.

				String outPrefix = tool.getOutputPrefix();
				IPath outFullPath = Path.fromOSString(outDirPath.toOSString() + outPrefix + WILDCARD);
				IPath outLocation;
				String inExt = resPath.getFileExtension();
				String outExt = tool.getOutputExtension(inExt);
				outFullPath = resolvePercent(outFullPath.addFileExtension(outExt), buildRc.getLocation());

				outLocation = getProjectLocation().append(outFullPath.removeFirstSegments(1));

				BuildIOType buildArg = action.createIOType(false, true, null);

				BuildResource outRc = createResource(outFullPath, getURIForFullPath(outLocation));
				buildArg.addResource(outRc);
		}

		if(checkFlags(BuildDescriptionManager.DEPFILES)){
			if(buildRc != null){
				IInputType type = action.getInputType();
				String ext = null;
				if(type != null){
					String location = buildRc.getLocation().toOSString();
					for (String srcExt : type.getSourceExtensions(tool)) {
						if(location.endsWith(srcExt)){
							ext = srcExt;
							break;
						}
					}
				}
				if(ext == null)
					ext = buildRc.getLocation().getFileExtension();

				if (ext != null) {
					IManagedDependencyGeneratorType depGenType = tool
							.getDependencyGeneratorForExtension(ext);
					if (depGenType != null) {
						IPath depFiles[] = null;
						if (depGenType instanceof IManagedDependencyGenerator2) {
							IBuildObject context = tool.getParent();
							if (context instanceof IToolChain) {
								context = ((IToolChain) context).getParent();
							}
							IPath path = buildRc.isProjectResource() ? buildRc
									.getFullPath().removeFirstSegments(1)
									: buildRc.getLocation();

							IResource resource = buildRc.isProjectResource() ? fProject
									.findMember(buildRc.getLocation())
									: null;

							IManagedDependencyInfo info = ((IManagedDependencyGenerator2) depGenType)
									.getDependencySourceInfo(path, resource,
											context, tool,
											getDefaultBuildDirLocation());
							if (info instanceof IManagedDependencyCommands) {
								depFiles = ((IManagedDependencyCommands) info)
										.getDependencyFiles();
							}
						} else if (depGenType.getCalculatorType() == IManagedDependencyGeneratorType.TYPE_COMMAND
								&& depGenType instanceof IManagedDependencyGenerator) {
							depFiles = new IPath[1];
							depFiles[0] = new Path(buildRc.getLocation()
									.segment(
											buildRc.getLocation()
													.segmentCount() - 1))
									.removeFileExtension()
									.addFileExtension("d"); //$NON-NLS-1$
						}

						if (depFiles != null) {
							BuildIOType depType = action.createIOType(false,
									false, null);
							addOutputs(depFiles, depType, outDirPath);
						}
					}
				}
			}
		}
	}


	/* (non-Javadoc)
	 * If the path contains a %, returns the path resolved using the resource name
	 *
	 */
	protected IPath resolvePercent(IPath outPath, IPath sourceLocation) {
		//  Get the input file name
		String fileName = sourceLocation.removeFileExtension().lastSegment();
		//  Replace the % with the file name
		String outName = outPath.toOSString().replaceAll("%", fileName); //$NON-NLS-1$
		return Path.fromOSString(outName);
	}


	private IPath locationToRel(IPath location){
		if(getProjectLocation().isPrefixOf(location))
			return location.removeFirstSegments(getProjectLocation().segmentCount()).setDevice(null);
		//TODO
		return location;
	}

	public IBuildResource getBuildResource(IPath location) {
		return getBuildResource(URIUtil.toURI(location));
	}

	private IBuildResource getBuildResource(URI locationURI) {
		return fLocationToRcMap.get(locationURI);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescription#getResources()
	 */
	public IBuildResource[] getResources(){
		return fLocationToRcMap.values().toArray(new IBuildResource[0]);
	}

	public IBuildResource[] getResources(boolean generated){
		List<IBuildResource> list = new ArrayList<IBuildResource>();
		for (IBuildResource rc : getResources()) {
			if(generated == (rc.getProducerStep() != fInputStep))
				list.add(rc);
		}

		return list.toArray(new IBuildResource[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescription#getConfiguration()
	 */
	public IConfiguration getConfiguration() {
		return fCfg;
	}

	public Map<String, String> getEnvironment(){
		if(fEnvironment == null)
			fEnvironment = calculateEnvironment();
		return fEnvironment;
	}

	protected Map<String, String> calculateEnvironment(){
		IBuildEnvironmentVariable variables[] = ManagedBuildManager.getEnvironmentVariableProvider().getVariables(fCfg,true,true);
		Map<String, String> map = new HashMap<String, String>();

		for (IBuildEnvironmentVariable var : variables) {
			map.put(var.getName(), var.getValue());
		}
		return map;
	}

	public IProject getProject() {
		return fProject;
	}

	private void calculateInputs(BuildStep step) throws CoreException {
		// Get the inputs for this tool invocation
		// Note that command inputs that are also dependencies are also added to the command dependencies list

		/* The priorities for determining the names of the inputs of a tool are:
		 *  1.  If an option is specified, use the value of the option.
		 *  2.  If a build variable is specified, use the files that have been added to the build variable as
		 *      the output(s) of other build steps.
		 *  3.  Use the file extensions and the resources in the project
		 */
		ITool tool = step.getTool();
		IInputType[] inTypes = tool.getInputTypes();
		if (inTypes != null && inTypes.length > 0) {
			for (IInputType type : inTypes) {
				boolean primaryInput = type.getPrimaryInput();
				IOption option = tool.getOptionBySuperClassId(type.getOptionId());
				BuildIOType arg = step.getIOTypeForType(type, true);

				//  Option?
				if (option != null) {
					try {
						List<String> inputs = new ArrayList<String>();
						int optType = option.getValueType();
						if (optType == IOption.STRING) {
							inputs.add(option.getStringValue());
						} else if (
								optType == IOption.STRING_LIST ||
								optType == IOption.LIBRARIES ||
								optType == IOption.OBJECTS ||
								optType == IOption.INCLUDE_FILES ||
								optType == IOption.LIBRARY_PATHS ||
								optType == IOption.LIBRARY_FILES ||
								optType == IOption.MACRO_FILES ||
								optType == IOption.UNDEF_INCLUDE_PATH ||
								optType == IOption.UNDEF_PREPROCESSOR_SYMBOLS ||
								optType == IOption.UNDEF_INCLUDE_FILES ||
								optType == IOption.UNDEF_LIBRARY_PATHS ||
								optType == IOption.UNDEF_LIBRARY_FILES ||
								optType == IOption.UNDEF_MACRO_FILES
								) {
							inputs = (List<String>)option.getValue();
						}
						for (int j=0; j<inputs.size(); j++) {
							String inputName = inputs.get(j).trim();


							try {
								String resolved = null;

								resolved = ManagedBuildManager
										.getBuildMacroProvider()
										.resolveValue(
												inputName,
												"", //$NON-NLS-1$
												" ", //$NON-NLS-1$
												IBuildMacroProvider.CONTEXT_OPTION,
												new OptionContextData(
														option,
														tool));

								if ((resolved = resolved.trim()).length() > 0)
									inputName = resolved;
							} catch (BuildMacroException e) {
							}

							if(arg == null)
								arg = step.createIOType(true, primaryInput, type);

							addInput(inputName, arg);
						}
					} catch( BuildException ex ) {
					}

				}

				// Get any additional inputs specified in the manifest file or the project file
				IAdditionalInput[] addlInputs = type.getAdditionalInputs();
				if (addlInputs != null) {
					for (IAdditionalInput addlInput : addlInputs) {
						int kind = addlInput.getKind();
						if (kind == IAdditionalInput.KIND_ADDITIONAL_INPUT ||
							kind == IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY) {
							String[] paths = addlInput.getPaths();
							if (paths != null) {
								for (String path : paths) {
									String strPath = path;

									// Translate the path from project relative to
									// build directory relative
									if (!(strPath.startsWith("$("))) {		//$NON-NLS-1$

										if(arg == null)
											arg = step.createIOType(true, primaryInput, type);

										addInput(strPath, arg);

									} else if (strPath.endsWith(")")){	//$NON-NLS-1$
										String var = strPath.substring(2, strPath.length() - 1);
										if((var = var.trim()).length() != 0){
											if(VAR_USER_OBJS.equals(var)){
												String objs[] = getUserObjs(step);
												if(objs != null && objs.length != 0){
													if(arg == null)
														arg = step.createIOType(true, primaryInput, type);

													for (String userObj : objs) {
														addInput(userObj, arg);
													}
												}
												//TODO
											} else if (VAR_LIBS.equals(var)){
												ITool libTool = fCfg.calculateTargetTool();
												if(libTool == null)
													libTool = step.getTool();

												step.setLibTool(libTool);
											} else {
												if(arg == null)
													arg = step.createIOType(true, primaryInput, type);

												Set<BuildIOType> set = fVarToAddlInSetMap.get(var);
												if(set == null){
													set = new HashSet<BuildIOType>();
													fVarToAddlInSetMap.put(var, set);
												}

												if(set.add(arg)){
													for (BuildResource rc : fLocationToRcMap.values()) {
														BuildIOType t = (BuildIOType)rc.getProducerIOType();
														if(t != null && var.equals(t.getLinkId()))
															arg.addResource(rc);
													}
												}
											}

										}
									}
								}
							}
						}
					}
				}

			}
		} else {
		}

		calculateDeps(step);
	}

	private void calculateDeps(BuildStep step){
		BuildResource rcs[] = (BuildResource[])step.getInputResources();
		Set<IPath> depSet = new HashSet<IPath>();

		for (BuildResource rc : rcs) {
			IManagedDependencyCalculator depCalc = getDependencyCalculator(step, rc);
			if(depCalc != null){
				IPath paths[] = depCalc.getDependencies();
				for (IPath path : paths) {
					depSet.add(path);
				}
			}
		}

		if(depSet.size() > 0){
			BuildIOType ioType = step.createIOType(true, false, null);

			for (IPath path : depSet) {
				addInput(path, ioType);
			}
		}
	}

	protected IManagedDependencyCalculator getDependencyCalculator(BuildStep step, BuildResource bRc){
		if(!checkFlags(BuildDescriptionManager.DEPS))
			return null;

		final ITool tool = step.getTool();
		if(tool == null)
			return null;

		IManagedDependencyCalculator depCalc = null;
		String ext = bRc.getLocation().getFileExtension();
		if(ext == null)
			ext = ""; 	//$NON-NLS-1$
		IManagedDependencyGeneratorType depGenType = tool.getDependencyGeneratorForExtension(ext);
		IManagedDependencyGeneratorType depGen = null;

		if(depGenType != null){
			switch(depGenType.getCalculatorType()){
			case IManagedDependencyGeneratorType.TYPE_NODEPS:
			case IManagedDependencyGeneratorType.TYPE_NODEPENDENCIES:
				//no dependencies
				break;
			case IManagedDependencyGeneratorType.TYPE_INDEXER:
			case IManagedDependencyGeneratorType.TYPE_EXTERNAL:
			case IManagedDependencyGeneratorType.TYPE_CUSTOM:
				depGen = depGenType;
				break;
			case IManagedDependencyGeneratorType.TYPE_COMMAND:
			case IManagedDependencyGeneratorType.TYPE_BUILD_COMMANDS:
			case IManagedDependencyGeneratorType.TYPE_PREBUILD_COMMANDS:
				//TODO: may implement the .d file parsing for deps calculation here
				//break;
			default:
				depGen = getPDOMDependencyGenerator();
				break;
			}
		} else {
			depGen = getPDOMDependencyGenerator();
		}

		if(depGen != null){
			final IResource rc = BuildDescriptionManager.findResourceForBuildResource(bRc);
			IBuildObject bo = tool.getParent();
			if(bo instanceof IToolChain)
				bo = ((IToolChain)bo).getParent();

			if(rc != null){
				if(depGen instanceof IManagedDependencyGenerator2){
					IManagedDependencyInfo srcInfo = ((IManagedDependencyGenerator2)depGen).getDependencySourceInfo(
							rc.getLocation(),
							rc,
							bo,
							tool,
							getTopBuildDirLocation());
					if(srcInfo instanceof IManagedDependencyCalculator)
						depCalc = (IManagedDependencyCalculator)srcInfo;

				} else if (depGen instanceof IManagedDependencyGenerator){
					IResource rcs[] = ((IManagedDependencyGenerator)depGen).findDependencies(rc, fProject);
					if(rcs != null && rcs.length > 0){
						final IPath paths[] = new IPath[rcs.length];
						final IBuildObject bof = bo;
						for(int i = 0; i < paths.length; i++){
							paths[i] = rcs[i].getLocation();
						}
						depCalc = new IManagedDependencyCalculator(){

							public IPath[] getAdditionalTargets() {
								return null;
							}

							public IPath[] getDependencies() {
								return paths;
							}

							public IBuildObject getBuildContext() {
								return bof;
							}

							public IPath getSource() {
								return rc.getLocation();
							}

							public ITool getTool() {
								return tool;
							}

							public IPath getTopBuildDirectory() {
								return getTopBuildDirectory();
							}
						};
					}
				}
			}
		}
		return depCalc;
	}

	protected PDOMDependencyGenerator getPDOMDependencyGenerator(){
		if(fPdomDepGen == null)
			fPdomDepGen = new PDOMDependencyGenerator();
		return fPdomDepGen;
	}

	public String[] getLibs(BuildStep step) {
		Vector<String> libs = new Vector<String>();
		ITool tool = step.getLibTool();

		if(tool != null){
			// Look for the lib option type
			for (IOption option : tool.getOptions()) {
				try {
					if (option.getValueType() == IOption.LIBRARIES) {

						// check to see if the option has an applicability calculator
						IOptionApplicability applicabilitytCalculator = option.getApplicabilityCalculator();

						if (applicabilitytCalculator == null
								|| applicabilitytCalculator.isOptionUsedInCommandLine(fCfg, tool, option)) {
							String command = option.getCommand();
							for (String lib : option.getLibraries()) {
								try {
									String resolved[] = ManagedBuildManager.getBuildMacroProvider().resolveStringListValueToMakefileFormat(
											lib,
											"", //$NON-NLS-1$
											" ", //$NON-NLS-1$
											IBuildMacroProvider.CONTEXT_OPTION,
											new OptionContextData(option, tool));
									if(resolved != null && resolved.length > 0){
										for (String string : resolved) {
											if(string.length() > 0)
												libs.add(command + string);
										}
									}
								} catch (BuildMacroException e) {
									// TODO: report error
									continue;
								}

							}
						}
					}
				} catch (BuildException e) {
					// TODO: report error
					continue;
				}
			}
		}
		return libs.toArray(new String[libs.size()]);
	}

	public String[] getUserObjs(BuildStep step) {
		Vector<String> objs = new Vector<String>();
		ITool tool = fCfg.calculateTargetTool();
		if(tool == null)
			tool = step.getTool();

		if(tool != null){
				// Look for the user object option type
				for (IOption option : tool.getOptions()) {
					try {
						if (option.getValueType() == IOption.OBJECTS) {
							String unresolved[] = option.getUserObjects();
							if(unresolved != null && unresolved.length > 0){
								for (String unresolvedObj : unresolved) {
									try {
										String resolved[] = ManagedBuildManager.getBuildMacroProvider().resolveStringListValueToMakefileFormat(
												unresolvedObj,
												"", //$NON-NLS-1$
												" ", //$NON-NLS-1$
												IBuildMacroProvider.CONTEXT_OPTION,
												new OptionContextData(option, tool));
										if(resolved != null && resolved.length > 0)
											objs.addAll(Arrays.asList(resolved));
									} catch (BuildMacroException e) {
										// TODO: report error
										continue;
									}
								}
							}
						}
					} catch (BuildException e) {
						// TODO: report error
						continue;
					}
				}
		}
		return objs.toArray(new String[objs.size()]);
	}

	private BuildResource addInput(String path, BuildIOType buildArg){
		if(path.length() > 0){
			if(path.length() >= 2){
				// Unquote path potentially quoted by FileListControl.getNewInputObject()
				if(path.charAt(0) == '"' && path.charAt(path.length() -1) == '"') {
					path = path.substring(1, path.length() -1);
				}
			}
			IPath pPath = Path.fromOSString(path);
			return addInput(pPath, buildArg);
		}
		return null;
	}

	private BuildResource addInput(IPath path, BuildIOType buildArg){
			IPath inFullPath = path;
			IPath inLocation;

			if(inFullPath.isAbsolute()){
				inLocation = inFullPath;
				inFullPath = null;
				IFile files[] = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(inLocation);
				for (IFile file : files) {
					IPath fl = file.getFullPath();
					if(fl.segment(0).equals(fProject.getName())){
						inFullPath = fl;
						break;
					}
				}
				if(inFullPath == null && files.length > 0)
					inFullPath = files[0].getFullPath();
				if(inFullPath == null && getProjectLocation().isPrefixOf(inLocation)){
					inFullPath = fProject.getFullPath().append(inLocation.removeFirstSegments(getProjectLocation().segmentCount()));
				}
			} else {
				inFullPath = fProject.getFullPath().append(inFullPath);

				IResource res = ResourcesPlugin.getWorkspace().getRoot().getFile(inFullPath);//.findMember(inFullPath);
				inLocation = calcResourceLocation(res);
			}


			BuildResource rc = createResource(inFullPath, getURIForFullPath(inLocation));
			buildArg.addResource(rc);

			return rc;
	}


	void typeCreated(BuildIOType arg){
	}

	public BuildResource createResource(String projPath){
		Path path = new Path(projPath);
		return createResource(path);
	}

	public BuildResource createResource(IPath projPath){
		return createResource(fProject.getFullPath().append(projPath), createProjectRelativeURI(projPath));
	}

	private URI createProjectRelativeURI(IPath projPath) {
		URI projURI = fProject.getLocationURI();
		IFileStore projStore = null;
		try {
			projStore = EFS.getStore(projURI);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(projStore == null)
			return null;

		IFileStore childStore = projStore.getFileStore(projPath);
		return childStore.toURI();
	}

	public BuildResource createResource(IResource rc){
		return createResource(rc.getFullPath(), rc.getLocationURI());
	}

	public BuildResource createResource(IPath fullWorkspacePath, URI locationURI){

		BuildResource rc = (BuildResource)getBuildResource(locationURI);

		if(rc == null)
			rc = new BuildResource(this, fullWorkspacePath, locationURI);

		return rc;
	}

	public IResourceDelta getDelta(){
		return fDelta;
	}

	private ITool[] getOrderedTools(){
		if(fOrderedTools == null){
			ITool tools[] = fCfg.getFilteredTools();
			for(int i = 0; i < tools.length; i++){
				for(int j = i; j < tools.length; j++){
					ITool tool = tools[j];
					ToolOrderEstimation order = getToolOrder(tool);
					ITool deps[] = order.getDeps();
					boolean put = deps.length == 0;
					if(!put && deps.length <= i){
						put = true;
						for (ITool dep : deps) {
							if(indexOf(dep, tools, 0, i) == -1){
								put = false;
								break;
							}
						}
					}
					if(put){
						if(i != j){
							ITool tmp = tools[i];
							tools[i] = tools[j];
							tools[j] = tmp;
						}
						break;
					}
				}
			}
			fOrderedTools = tools;
		}
		return fOrderedTools;
	}

//	private int indexOf(Object obj, Object array[]){
//		return indexOf(obj, array, 0, -1);
//	}

	private int indexOf(Object obj, Object array[], int start, int stop){
		if(start < 0)
			start = 0;
		if(stop == -1)
			stop = array.length;

		if(start < stop){
			for(int i = start; i < stop; i++){
				if(obj == array[i])
					return i;
			}
		}
		return -1;
	}

	private ToolOrderEstimation getToolOrder(ITool tool){
		ToolOrderEstimation order = fToolOrderMap.get(tool.getId());
		if(order == null){
			order = new ToolOrderEstimation(tool);
			fToolOrderMap.put(tool.getId(), order);
		}
		return order;
	}

	private ITool[] doCalcDeps(ITool tool){
		if(!fToolInProcesSet.add(tool)){
			//TODO throw error?
			if(DbgUtil.DEBUG)
				DbgUtil.trace("loop dependency for tool" + tool.getName());	//$NON-NLS-1$
			return new ITool[0];
		}

		String exts[] = tool.getAllInputExtensions();

		Set<ITool> set = new HashSet<ITool>();
		for (ITool t : fCfg.getFilteredTools()) {
			if(t == tool)
				continue;

			for (String e : exts) {
				if(t.producesFileType(e)){
					IInputType inType = tool.getInputType(e);
					IOutputType outType = t.getOutputType(e);
					if((inType == null && outType == null)
							|| (inType != null && outType != null
									&& inType.getBuildVariable().equals(outType.getBuildVariable()))){

						set.add(t);
						ToolOrderEstimation est = getToolOrder(t);
						for (ITool dep : est.getDeps()) {
							if(dep != tool)
								set.add(dep);
							else{
								if(DbgUtil.DEBUG)
									DbgUtil.trace("loop dependency for tool" + tool.getName());	//$NON-NLS-1$
								//TODO throw error
							}
						}
					}
				}
			}
		}

		fToolInProcesSet.remove(tool);
		return set.toArray(new ITool[set.size()]);
	}

	private ITool[] doCalcConsumers(ITool tool){
		if(!fToolInProcesSet.add(tool)){
			//TODO throw error?
			if(DbgUtil.DEBUG)
				DbgUtil.trace("loop dependency for tool" + tool.getName());	//$NON-NLS-1$
			return new ITool[0];
		}

		String exts[] = tool.getAllOutputExtensions();

		Set<ITool> set = new HashSet<ITool>();
		for (ITool t : fCfg.getFilteredTools()) {
			if(t == tool)
				continue;

			for (String e : exts) {
				if(t.buildsFileType(e)){
					IOutputType inType = tool.getOutputType(e);
					IInputType outType = t.getInputType(e);
					if((inType == null && outType == null)
							|| (inType != null && outType != null
									&& inType.getBuildVariable().equals(outType.getBuildVariable()))){

						set.add(t);
						ToolOrderEstimation est = getToolOrder(t);
						for (ITool consumer : est.getConsumers()) {
							if(consumer != tool)
								set.add(consumer);
							else{
								if(DbgUtil.DEBUG)
									DbgUtil.trace("loop dependency for tool" + tool.getName());	//$NON-NLS-1$
								//TODO throw error
							}
						}
					}
				}
			}
		}

		fToolInProcesSet.remove(tool);
		return set.toArray(new ITool[set.size()]);
	}

	private IPath[] getGeneratedPaths(){
		if(fGeneratedPaths == null){
			IConfiguration cfgs[] = fCfg.getManagedProject().getConfigurations();
			fGeneratedPaths = new IPath[cfgs.length];
			//TODO: this is a temporary hack for obtaining the top generated dirs
			//for all configurations. We can not use the buildfile generator here
			//since it can only be used for the default configuration
			for(int i = 0; i < cfgs.length; i++){
				fGeneratedPaths[i] = fProject.getFullPath().append(cfgs[i].getName());
			}
		}
		return fGeneratedPaths;
	}

	protected boolean isGenerated(IPath path){
		for (IPath genPath : getGeneratedPaths()) {
			if(genPath.isPrefixOf(path))
				return true;
		}

		return getTopBuildDirFullPath().isPrefixOf(path);
	}

	protected void stepCreated(BuildStep step){
		fStepList.add(step);
		ITool tool = step.getTool();
		if(tool != null
				&& tool == fCfg.calculateTargetTool()
	//			&& (prym == null || step.getInputType() == prym)
				){
			if(fTargetStep != null){
				//TODO: this is an error case, log or perform some special handling
				if(DbgUtil.DEBUG)
					DbgUtil.trace("ERROR: target action already created");	//$NON-NLS-1$
			}
			fTargetStep = step;
		}
	}

	public BuildStep createStep(ITool tool, IInputType type){
		return new BuildStep(this, tool, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescription#getDefaultBuildDirLocation()
	 */
	public IPath getDefaultBuildDirLocation() {
		return getTopBuildDirLocation();
	}

	public URI getDefaultBuildDirLocationURI() {
		return getTopBuildDirLocationURI();
	}

	public IPath getDefaultBuildDirFullPath() {
		return getTopBuildDirFullPath();
	}

	protected void resourceAddedToType(BuildIOType type, BuildResource rc){
		if(!type.isInput()){
			String var = type.getLinkId();
			if(var == null)
				var = new String();

			Set<BuildIOType> set = fVarToAddlInSetMap.get(var);
			if(set != null){
				for (BuildIOType t : set) {
					t.addResource(rc);
				}
			}
		}
	}

	protected void resourceRemovedFromType(BuildIOType type, BuildResource rc){

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescription#getSteps()
	 */
	public IBuildStep[] getSteps() {
		return fStepList.toArray(new BuildStep[fStepList.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription#findBuildResource(org.eclipse.core.resources.IResource)
	 */
	public IBuildResource getBuildResource(IResource resource){
		return getBuildResource(calcResourceLocation(resource));
	}

	public IBuildResource getBuildResourceForFullPath(IPath fullPath){
		IPath location = calcLocationForFullPath(fullPath);
		return getBuildResource(location);
	}

	protected IPath calcLocationForFullPath(IPath fullPath){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject proj = root.getProject(fullPath.segment(0));
		IPath rcLocation = proj.getLocation();
		if(rcLocation != null){
			rcLocation = rcLocation.append(fullPath.removeFirstSegments(1));
		} else {
			rcLocation = root.getLocation().append(fullPath);
		}
		return rcLocation;
	}

	private void initToolInfos(){
		fToolInfos = PathSettingsContainer.createRootContainer();

		for (IResourceInfo rcInfo : fCfg.getResourceInfos()) {
//			if(rcInfo.isExcluded())
//				continue;
			
			ToolInfoHolder h = getToolInfo(rcInfo.getPath(), true);
			if(rcInfo instanceof IFolderInfo){
				IFolderInfo fo = (IFolderInfo)rcInfo;
				h.fExtToToolAndTypeListMap = initToolAndTypeMap(fo);
			}
		}
	}

	private ToolInfoHolder getToolInfo(BuildResource rc){
		IPath path = rc.isProjectResource() ?
				rc.getFullPath().removeFirstSegments(1).makeRelative() :
					Path.EMPTY;
		return getToolInfo(path);
	}

	private ToolInfoHolder getToolInfo(IPath path){
		return getToolInfo(path, false);
	}

	private ToolInfoHolder getToolInfo(IPath path, boolean create){
		PathSettingsContainer child = fToolInfos.getChildContainer(path, create, create);
		ToolInfoHolder h = null;
		if(child != null){
			h = (ToolInfoHolder)child.getValue();
			if(h == null && create){
				h = new ToolInfoHolder();
				child.setValue(h);
			}
		}
		return h;
	}

	public IBuildStep getCleanStep(){
		if(fCleanStep == null){
			fCleanStep = new BuildStep(this, null, null);
		}
		return fCleanStep;
	}

}
