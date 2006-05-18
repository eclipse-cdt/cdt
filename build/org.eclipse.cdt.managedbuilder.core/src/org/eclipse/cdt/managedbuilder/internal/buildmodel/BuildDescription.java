/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedOutputNameProvider;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IOutputType;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyCommands;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator2;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGeneratorType;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class BuildDescription implements IBuildDescription {
	private final static String DOT = ".";	//$NON-NLS-1$
	private final static String WILDCARD = "%";	//$NON-NLS-1$
	private final static String VAR_USER_OBJS = "USER_OBJS"; //$NON-NLS-1$
	private final static String VAR_LIBS = "LIBS"; //$NON-NLS-1$
	

	private Configuration fCfg;
	private IResourceDelta fDelta;
	
	private Map fInTypeToGroupMap = new HashMap();
	private Map fToolToMultiStepMap = new HashMap();
	private BuildStep fOrderedMultiActions[];

	private Map fLocationToRcMap = new HashMap();
	
	private Map fVarToAddlInSetMap = new HashMap();
	
	private List fStepList = new ArrayList();
	
	private BuildStep fTargetStep;

	private IManagedBuilderMakefileGenerator fMakeGen;
	private IProject fProject;
	private IManagedBuildInfo fInfo;
	private IPath fTopBuildDirFullPath;
	private IPath fGeneratedPaths[];
	private int fFlags;

	private BuildStep fInputStep;
	private BuildStep fOutputStep;
	
	private Map fToolOrderMap = new HashMap();
	private Set fToolInProcesSet = new HashSet();
	private ITool fOrderedTools[];
	
	private Map fExtToToolAndTypeListMap = new HashMap();
	
	private Map fEnvironment;
	
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

			if(proxy.getType() == IResource.FILE){
				doVisitFile(proxy.requestResource());
				return false;
			} 
			
			return !isGenerated(proxy.requestFullPath());
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
		IPath rcLocation = rc.getLocation();
		if(rcLocation == null){
			IPath fullPath = rc.getFullPath();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); 
			IProject proj = root.getProject(fullPath.segment(0));
			rcLocation = proj.getLocation();
			if(rcLocation != null){
				rcLocation = rcLocation.append(fullPath.removeFirstSegments(1));
			} else {
				rcLocation = root.getLocation().append(fullPath);
			}
		}
		return rcLocation;
	}
	
	private class StepCollector implements IStepVisitor{
		private Set fStepSet = new HashSet();

		public int visit(IBuildStep action) throws CoreException {
			if(DbgUtil.DEBUG){
				DbgUtil.trace("StepCollector: visiting step " + DbgUtil.stepName(action));	//$NON-NLS-1$
			}
			fStepSet.add(action);
			return VISIT_CONTINUE;
		}
		
		public BuildStep[] getSteps(){
			return (BuildStep[])fStepSet.toArray(new BuildStep[fStepSet.size()]);
		}
		
		public Set getStepSet(){
			return fStepSet;
		}
		
		public void clear(){
			fStepSet.clear();
		}
	}

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
				for(int i = 0; i < rcs.length; i++){
					if(rcs[i].needsRebuild()){
						if(DbgUtil.DEBUG)
							DbgUtil.trace("resource " + locationToRel(rcs[i].getLocation()).toString() + " needs rebuild");	//$NON-NLS-1$	//$NON-NLS-2$
						rebuild = true;
						break;
					} else if(rcs[i].isRemoved()){
						if(DbgUtil.DEBUG)
							DbgUtil.trace("resource " + locationToRel(rcs[i].getLocation()).toString() + " is removed");	//$NON-NLS-1$ 	//$NON-NLS-2$
						rebuild = true;
						break;
					}
				}
			}
			
			if(removed){
				if(DbgUtil.DEBUG)
					DbgUtil.trace("action to be removed");	//$NON-NLS-1$

				action.setRemoved();
				
				IBuildResource[] outRcs = action.getOutputResources();
				
				for(int i = 0; i < outRcs.length; i++){
					if(DbgUtil.DEBUG)
						DbgUtil.trace("setting remove state for resource " + locationToRel(outRcs[i].getLocation()).toString());	//$NON-NLS-1$
					
					((BuildResource)outRcs[i]).setRemoved(true);
				}
				
			} else if(rebuild){
				if(DbgUtil.DEBUG)
					DbgUtil.trace("action needs rebuild");	//$NON-NLS-1$

				action.setRebuildState(true);
				
				IBuildResource[] outRcs = action.getOutputResources();
				
				for(int i = 0; i < outRcs.length; i++){
					if(DbgUtil.DEBUG)
						DbgUtil.trace("setting rebuild state for resource " + locationToRel(outRcs[i].getLocation()).toString());	//$NON-NLS-1$
					
					((BuildResource)outRcs[i]).setRebuildState(true);
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
		
		ITool getTool(){
			return fTool;
		}
		
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
		
		boolean dependsOn(ITool tool){
			return indexOf(tool, getDeps()) != -1;
		}

		boolean hasConsumer(ITool tool){
			return indexOf(tool, getConsumers()) != -1;
		}

	}
	
	protected BuildDescription(){
		
	}
	
	public BuildDescription(IConfiguration cfg){
		initBase(cfg, null, 0);
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
		
		for(int i = 0; i < types.length; i++){
			IBuildResource rcs[] = types[i].getResources();
			for(int j = 0; j < rcs.length; j++){
				String e = rcs[j].getLocation().getFileExtension();
				if(e == null){
					if(ext.length() == 0)
						return types[i];
				} else {
					if(ext.equals(e))
						return types[i];
				}
			}
		}
		return null;
	}
	
	private void initToolAndTypeMap(){
		ITool tools[] = fCfg.getFilteredTools();
		for(int i = 0; i < tools.length; i++){
			ITool tool = tools[i];
			IInputType types[] = tool.getInputTypes();
			if(types.length != 0){
				for(int j = 0; j < types.length; j++){
					IInputType type = types[j];
					String exts[] = type.getSourceExtensions(tool);
					for(int k = 0; k < exts.length; k++){
						String ext = exts[k];
						if(tool.buildsFileType(ext)){
							List list = (List)fExtToToolAndTypeListMap.get(ext);
							if(list == null){
								list = new ArrayList();
								fExtToToolAndTypeListMap.put(ext, list);
							}
							list.add(new ToolAndType(tool, type, ext));
						}
					}
				}
			} else {
				String exts[] = tool.getAllInputExtensions();
				for(int k = 0; k < exts.length; k++){
					String ext = exts[k];
					if(tool.buildsFileType(ext)){
						List list = (List)fExtToToolAndTypeListMap.get(ext);
						if(list == null){
							list = new ArrayList();
							fExtToToolAndTypeListMap.put(ext, list);
						}
						list.add(new ToolAndType(tool, null, ext));
					}
				}
			}
		}
	}
	
	ToolAndType getToolAndType(BuildResource rc, boolean checkVar){
		String locString = rc.getLocation().toString();
		BuildIOType arg = (BuildIOType)rc.getProducerIOType();
		String linkId = (checkVar && arg != null) ? arg.getLinkId() : null;
		
		for(Iterator iter = fExtToToolAndTypeListMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			String ext = (String)entry.getKey();
			if(locString.endsWith("." + ext)){	//$NON-NLS-1$
				List list = (List)entry.getValue();
				for(Iterator itt = list.iterator(); itt.hasNext();){
					ToolAndType tt = (ToolAndType)itt.next();
					
					if(!checkVar)
						return tt;
					
					IInputType type = tt.fType;
					if(type == null)
						return tt;

					String var = type.getBuildVariable();
					if(var == null || var.length() == 0)
						return tt;
					
					if(linkId == null){
						if(var == null || var.length() == 0)
							return tt; 
					} else if(linkId.equals(var)){
						return tt;
					}
				}
			}
		}
		
		return null;
	}
	
	private void composeOutputs(BuildStep inputAction, BuildIOType inputActionArg, BuildResource rc) throws CoreException{

		boolean isSource = inputActionArg == null;

		if(!isSource){
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
				IOutputType[] secondaryOutputs = fCfg.getToolChain().getSecondaryOutputs();
				for(int i = 0; i < secondaryOutputs.length; i++){
					if(secondaryOutputs[i] == inputActionArg.getIoType()){
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
		
		IResourceConfiguration rcCfg = rc.getFullPath() != null ?
				fCfg.getResourceConfiguration(rc.getFullPath().toString()) :
					null;
		ITool tool = null;
		IInputType inputType = null;
		String ext = null;
		boolean stepRemoved = false;
		if(rcCfg != null){
			if(rcCfg.isExcluded()){
				if(rcCfg.needsRebuild())
					stepRemoved = true;
				else
					return;
			}
				
			tool = rcCfg.getToolsToInvoke()[0]; 
			String exts[] = tool.getAllInputExtensions();
			String locString = location.toString();
			for(int i = 0; i < exts.length; i++){
				String e = exts[i];
				if(locString.endsWith(e)){
					inputType = tool.getInputType(e);
					ext = e;
				}
			}
		}
		else {
			ToolAndType tt = getToolAndType(rc, true);
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
				(inputType == null && tool != fCfg.getToolFromOutputExtension(fCfg.getArtifactExtension()))){
					
				BuildStep action = null;
				BuildIOType argument = null;
				BuildGroup group = null;
				if(rcCfg == null)
					group = createGroup(inputType, ext);

				action = createStep(tool, inputType);//new BuildStep(this, tool, inputType);
				if(stepRemoved)
					action.setRemoved();
				if(group != null)
					group.addAction(action);
				argument = action.createIOType(true, true, inputType);
				
				argument.addResource(rc);
				
				if(inputActionArg == null){
					inputActionArg = findTypeForExtension(inputAction,false,rc.getLocation().getFileExtension());
					if(inputActionArg == null)
						inputActionArg = inputAction.createIOType(false, false, null);
					inputActionArg.addResource(rc);
				}
				
				calculateInputs(action);
				
				calculateOutputs(action, argument, rc);
				
				BuildIOType outputs[] = (BuildIOType[])action.getOutputIOTypes();
				
				for(int i = 0; i < outputs.length; i++){
					BuildResource rcs[] = (BuildResource[])outputs[i].getResources();
					for(int j = 0; j < rcs.length; j++){
						composeOutputs(action, outputs[i], rcs[j]);
					}
				}
			} else {

				if(inputType != null ? inputType.getMultipleOfType() : tool == fCfg.calculateTargetTool()){	
					BuildStep step = (BuildStep)fToolToMultiStepMap.get(tool);

					if(step != null){
						BuildIOType argument = step.getIOTypeForType(inputType, true); 
						if(argument == null)
							argument = step.createIOType(true, true, inputType);
							
						argument.addResource(rc);
		
						if(inputActionArg == null){
							inputActionArg = findTypeForExtension(inputAction,false,rc.getLocation().getFileExtension());;
							if(inputActionArg == null)
								inputActionArg = inputAction.createIOType(false, false, null);
							inputActionArg.addResource(rc);
						}
					}
				} else {

				}
			}
		}
	}
	
	private BuildGroup createGroup(IInputType inType, String ext){
		String key = inType != null ?
				inType.getId() : "ext:"+ext;	//$NON-NLS-1$
		BuildGroup group = (BuildGroup)fInTypeToGroupMap.get(key);
		if(group == null){
			group = new BuildGroup();
			fInTypeToGroupMap.put(key, group);
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
	
	protected void initBase(IConfiguration cfg, IResourceDelta delta, int flags){
		fCfg = (Configuration)cfg;
		fDelta = delta;
		fProject = cfg.getOwner().getProject();
		fInfo = ManagedBuildManager.getBuildInfo(fProject);
		fFlags = flags;
		
		fInputStep = createStep(null,null);
		fOutputStep = createStep(null,null);
	}
	
	protected void initDescription() throws CoreException{
		if(fCfg.needsFullRebuild())
			fInputStep.setRebuildState(true);

		initToolAndTypeMap();
		
		initMultiSteps();

		RcVisitor visitor = new RcVisitor();
		fProject.accept(visitor, IResource.NONE);
		
		
		if(checkFlags(BuildDescriptionManager.REMOVED)
				&& fDelta != null)
			fDelta.accept(visitor);
		
		handleMultiSteps();
		
		visitor.setMode(true);
		if((checkFlags(BuildDescriptionManager.REMOVED) 
				|| checkFlags(BuildDescriptionManager.REBUILD))
				&& fDelta != null)
			fDelta.accept(visitor);
		
		completeLinking();
		synchRebuildState();
		//TODO: trim();
	}

	protected void init(IConfiguration cfg, IResourceDelta delta, int flags) throws CoreException {
		initBase(cfg, delta, flags);
		
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
		for(int i = 0; i < fOrderedMultiActions.length; i++){
			BuildStep action = fOrderedMultiActions[i];
			
			calculateInputs(action);
			
			calculateOutputs(action, action.getPrimaryTypes(true)[0], null);
			
			if(action.getOutputResources().length == 0){
				removeStep(action);
			}
			BuildIOType args[] =  (BuildIOType[])action.getOutputIOTypes();
				
			for(int j = 0; j < args.length; j++){
				BuildIOType arg = args[j];
				BuildResource rcs[] = (BuildResource[])arg.getResources();
				for(int k = 0; k < rcs.length; k++){
					BuildResource rc = rcs[k];
					composeOutputs(action, arg, rc);
				}
			}
		}
	}
	
	private void initMultiSteps(){
		ITool tools[] = fCfg.getFilteredTools();
		ITool targetTool = fCfg.calculateTargetTool();
		
		for(int i = 0; i < tools.length; i++){
			ITool tool = tools[i];
			IInputType type = tool.getPrimaryInputType();
			BuildStep action = null;
			if(type != null ? type.getMultipleOfType() : tool == targetTool){
				action = createStep(tool,type);//new BuildStep(this, tool, type);
				action.createIOType(true, true, type);
				fToolToMultiStepMap.put(tool, action);
			}
			
		}
		
		fOrderedMultiActions = new BuildStep[fToolToMultiStepMap.size()];
		ITool orderedTools[] = getOrderedTools();
		int index = 0;
		for(int i = 0; i < orderedTools.length; i++){
			BuildStep action = (BuildStep)fToolToMultiStepMap.get(orderedTools[i]);
			if(action != null)
				fOrderedMultiActions[index++] = action;
		}
	}
	
	
	
	private void completeLinking() throws CoreException{
		boolean foundUnused = false;
	
		do{
			BuildStep steps[] = (BuildStep[])getSteps();
			foundUnused = false;
			for(int k = 0; k < steps.length; k++){
				BuildStep step = steps[k];
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

		Set set = fLocationToRcMap.entrySet();
		List list = new ArrayList();
		for(Iterator iter = set.iterator();iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			
			BuildResource rc = (BuildResource)entry.getValue();
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
		
		for(Iterator iter = list.iterator(); iter.hasNext();){
			BuildIOType[][] types = removeResource((BuildResource)iter.next());
			
			BuildIOType producer = types[0][0];
			if(producer != null && producer.getResources().length == 0){
				((BuildStep)producer.getStep()).removeIOType(producer);
			}
			
			BuildIOType deps[] = types[1];
			for(int i = 0; i < deps.length; i++){
				if(deps[i].getResources().length == 0)
					((BuildStep)deps[i].getStep()).removeIOType(deps[i]);
			}
		}

	}
	
	protected void resourceRemoved(BuildResource rc){
		fLocationToRcMap.remove(rc.getLocation());
	}
	
	protected void resourceCreated(BuildResource rc){
		fLocationToRcMap.put(rc.getLocation(), rc);
	}
	
	private IManagedBuilderMakefileGenerator getMakeGenInitialized(){
		if(fMakeGen == null){
			fMakeGen = ManagedBuildManager.getBuildfileGenerator(fCfg);
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
		return fProject.getLocation().append(getTopBuildDirFullPath().removeFirstSegments(1));
	}
	
	private BuildResource[] addOutputs(IPath paths[], BuildIOType buildArg, IPath outDirPath){
		if(paths != null){
			List list = new ArrayList();
			for(int k = 0; k < paths.length; k++){
				IPath outFullPath = paths[k];
				IPath outLocation;
				
				
				if(outFullPath.isAbsolute()){
					outLocation = outFullPath;
					if(fProject.getLocation().isPrefixOf(outLocation))
						outFullPath = fProject.getFullPath().append(outLocation.removeFirstSegments(fProject.getLocation().segmentCount()));
					else
						outFullPath = null;
				} else {
					if (outFullPath.segmentCount() == 1) {
						outFullPath = outDirPath.append(outFullPath); 
						outLocation = fProject.getLocation().append(outFullPath.removeFirstSegments(1));
					} else {
						outLocation = getTopBuildDirLocation().append(outFullPath);
						outFullPath = getTopBuildDirFullPath().append(outFullPath);
					}
				}

				BuildResource outRc = createResource(outLocation, outFullPath);
				list.add(outRc);
				buildArg.addResource(outRc);
				
			}
			return (BuildResource[])list.toArray(new BuildResource[list.size()]);
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

			IPath path = new Path(artifactName);
			if(artifactExt != null && artifactExt.length() != 0)
				path = path.addFileExtension(artifactExt);
			
			IOutputType type = action.getTool().getPrimaryOutputType();
			BuildIOType ioType = action.getIOTypeForType(type, false);
			if(ioType == null)
				ioType = action.createIOType(false, true, type);
			addOutputs(new IPath[]{path}, ioType, outDirPath);
		} else if (outTypes != null && outTypes.length > 0) {
			for (int i=0; i<outTypes.length; i++) {
				IOutputType type = outTypes[i];
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
                    config = (IConfiguration) ((IToolChain) toolParent)
                            .getParent();
                }

                else if (toolParent instanceof IResourceConfiguration) {
                    config = (IConfiguration) ((IResourceConfiguration) toolParent)
                            .getParent();
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
								optType == IOption.OBJECTS) {
							List outputList = (List)option.getValue();
							// Add outputPrefix to each if necessary
							if(outputList != null && outputList.size() > 0){
//TODO
							try{
								pathStrings = ManagedBuildManager
								.getBuildMacroProvider()
								.resolveStringListValues(
										(String[])outputList.toArray(new String[outputList.size()]),
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
					if(buildArg == null)
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
				
				outLocation = fProject.getLocation().append(outFullPath.removeFirstSegments(1));
				
				BuildIOType buildArg = action.createIOType(false, true, null);

				BuildResource outRc = createResource(outLocation, outFullPath);
				buildArg.addResource(outRc);
		}
		
		if(checkFlags(BuildDescriptionManager.DEPS_DEPFILE_INFO)){
			if(tool != null && buildRc != null){
				IInputType type = action.getInputType();
				String ext = null;
				if(type != null){
					String exts[] = type.getSourceExtensions(tool);
					String location = buildRc.getLocation().toOSString();
					for(int i = 0; i < exts.length; i++){
						if(location.endsWith(exts[i])){
							ext = exts[i];
							break;
						}
					}
				}
				if(ext == null)
					ext = buildRc.getLocation().getFileExtension();
				
				if(ext != null){
					IManagedDependencyGeneratorType depGenType = tool.getDependencyGeneratorForExtension(ext);
					if(depGenType != null){
						IPath depFiles[] = null;
						if(depGenType instanceof IManagedDependencyGenerator2){
							IBuildObject context = tool.getParent();
							if(context instanceof IToolChain){
								context = ((IToolChain)context).getParent();
							}
							IPath path = buildRc.isProjectResource() ?
									buildRc.getFullPath().removeFirstSegments(1) :
										buildRc.getLocation();
							IManagedDependencyInfo info = ((IManagedDependencyGenerator2)depGenType).getDependencySourceInfo(path, context, tool, getDefaultBuildDirLocation());
							if(info instanceof IManagedDependencyCommands){
								depFiles = ((IManagedDependencyCommands)info).getDependencyFiles();
							}
						} else if (depGenType.getCalculatorType() == IManagedDependencyGeneratorType.TYPE_COMMAND
								&& depGenType instanceof IManagedDependencyGenerator) {
							depFiles = new IPath[1];
							depFiles[0] = new Path(buildRc.getLocation().segment(buildRc.getLocation().segmentCount() -1 )).removeFileExtension().addFileExtension("d");  //$NON-NLS-1$
						}
						
						if(depFiles != null){
							BuildIOType depType = action.createIOType(false, false, null);
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
		if(fProject.getLocation().isPrefixOf(location))
			return location.removeFirstSegments(fProject.getLocation().segmentCount()).setDevice(null);
		//TODO
		return location;
	}
	
	public IBuildResource getBuildResource(IPath location) {
		return (BuildResource)fLocationToRcMap.get(location);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescription#getResources()
	 */
	public IBuildResource[] getResources(){
		Collection c = fLocationToRcMap.values();
		List list = new ArrayList();
		for(Iterator iter = c.iterator();iter.hasNext();){
			Object obj = iter.next();
	
			if(obj instanceof BuildResource)
				list.add(obj);
			else if(obj instanceof List)
				list.addAll((List)obj);
		}
		return (IBuildResource[])list.toArray(new IBuildResource[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildDescription#getConfiguration()
	 */
	public IConfiguration getConfiguration() {
		return fCfg;
	}
	
	public Map getEnvironment(){
		if(fEnvironment == null)
			fEnvironment = calculateEnvironment();
		return fEnvironment;
	}
	
	protected Map calculateEnvironment(){
		IBuildEnvironmentVariable variables[] = ManagedBuildManager.getEnvironmentVariableProvider().getVariables(fCfg,true,true);
		Map map = new HashMap();
		
		for(int i = 0; i < variables.length; i++){
			IBuildEnvironmentVariable var = variables[i];
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
			for (int i=0; i<inTypes.length; i++) {
				IInputType type = inTypes[i];
				String variable = type.getBuildVariable();
				boolean primaryInput = type.getPrimaryInput();
				IOption option = tool.getOptionBySuperClassId(type.getOptionId());
				BuildIOType arg = step.getIOTypeForType(type, true);
				
				//  Option?
				if (option != null) {
					try {
						List inputs = new ArrayList();
						int optType = option.getValueType();
						if (optType == IOption.STRING) {
							inputs.add(option.getStringValue());
						} else if (
								optType == IOption.STRING_LIST ||
								optType == IOption.LIBRARIES ||
								optType == IOption.OBJECTS) {
							inputs = (List)option.getValue();
						}
						for (int j=0; j<inputs.size(); j++) {
							String inputName = ((String)inputs.get(j)).trim();
							
				
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
					for (int j=0; j<addlInputs.length; j++) {
						IAdditionalInput addlInput = addlInputs[j];
						int kind = addlInput.getKind();
						if (kind == IAdditionalInput.KIND_ADDITIONAL_INPUT ||
							kind == IAdditionalInput.KIND_ADDITIONAL_INPUT_DEPENDENCY) {
							String[] paths = addlInput.getPaths();
							if (paths != null) {
								for (int k = 0; k < paths.length; k++) {
									String strPath = paths[k];

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

													for(int o = 0; o < objs.length; o++){
														addInput(objs[o], arg);
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

												Set set = (Set)fVarToAddlInSetMap.get(var);
												if(set == null){
													set = new HashSet();
													fVarToAddlInSetMap.put(var, set);
												}
												
												if(set.add(arg)){
													for(Iterator iter = fLocationToRcMap.values().iterator(); iter.hasNext();){
														BuildResource rc = (BuildResource)iter.next();
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
	}
	
	public String[] getLibs(BuildStep step) {
		Vector libs = new Vector();
		ITool tool = step.getLibTool();
			
		if(tool != null){
				IOption[] opts = tool.getOptions();
				// Look for the lib option type
				for (int i = 0; i < opts.length; i++) {
					IOption option = opts[i];
					try {
						if (option.getValueType() == IOption.LIBRARIES) {
							
							// check to see if the option has an applicability calculator
							IOptionApplicability applicabilitytCalculator = option.getApplicabilityCalculator();
							
							if (applicabilitytCalculator == null
									|| applicabilitytCalculator.isOptionUsedInCommandLine(fCfg, tool, option)) {
								String command = option.getCommand();
								String[] allLibs = option.getLibraries();
								for (int j = 0; j < allLibs.length; j++)
								{
									try {
										String resolved[] = ManagedBuildManager.getBuildMacroProvider().resolveStringListValueToMakefileFormat(
												allLibs[j],
												"", //$NON-NLS-1$
												" ", //$NON-NLS-1$
												IBuildMacroProvider.CONTEXT_OPTION,
												new OptionContextData(option, tool));
										if(resolved != null && resolved.length > 0){
											for(int k = 0; k < resolved.length; k++){
												String string = resolved[k];
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
		return (String[])libs.toArray(new String[libs.size()]);
	}

	public String[] getUserObjs(BuildStep step) {
		Vector objs = new Vector();
		ITool tool = fCfg.calculateTargetTool();
		if(tool == null)
			tool = step.getTool();
			
		if(tool != null){
				IOption[] opts = tool.getOptions();
				// Look for the user object option type
				for (int i = 0; i < opts.length; i++) {
					IOption option = opts[i];
					try {
						if (option.getValueType() == IOption.OBJECTS) {
							String unresolved[] = option.getUserObjects();
							if(unresolved != null && unresolved.length > 0){
								for(int k = 0; k < unresolved.length; k++){
									try {
										String resolved[] = ManagedBuildManager.getBuildMacroProvider().resolveStringListValueToMakefileFormat(
												unresolved[k],
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
		return (String[])objs.toArray(new String[objs.size()]);
	}

	private BuildResource addInput(String path, BuildIOType buildArg){
		if(path.length() > 0){
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
				if(!fProject.getLocation().isPrefixOf(inLocation))
					inFullPath = null;
			} else {
				IPath projPath = inFullPath;
				inFullPath = fProject.getFullPath().append(inFullPath);

				IResource res = ResourcesPlugin.getWorkspace().getRoot().getFile(inFullPath);//.findMember(inFullPath);
				inLocation = calcResourceLocation(res);
/*				if(res != null)
					inLocation = res.getLocation();
				else 
					inLocation = fProject.getLocation().append(projPath);
*/
			}
			
			BuildResource rc = createResource(inLocation, inFullPath);
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
		return createResource(fProject.getLocation().append(projPath),fProject.getFullPath().append(projPath));
	}
	
	public BuildResource createResource(IResource rc){
		return createResource(calcResourceLocation(rc), rc.getFullPath());
	}

	public BuildResource createResource(IPath location, IPath fullPath){
		
		BuildResource rc = (BuildResource)getBuildResource(location);
		
		if(rc == null)
			rc = new BuildResource(this, location, fullPath);

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
						for(int k = 0; k < deps.length; k++){
							if(indexOf(deps[k], tools, 0, i) == -1){
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

	private int indexOf(Object obj, Object array[]){
		return indexOf(obj, array, 0, -1);
	}

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
		ToolOrderEstimation order = (ToolOrderEstimation)fToolOrderMap.get(tool.getId());
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
		
		ITool tools[] = fCfg.getFilteredTools();
		Set set = new HashSet();
		for(int i = 0; i < tools.length; i++){
			ITool t = tools[i];
			if(t == tool)
				continue;
			
			for(int j = 0; j < exts.length; j++){
				String e = exts[j];
				if(t.producesFileType(e)){
					IInputType inType = tool.getInputType(e);
					IOutputType outType = t.getOutputType(e);
					if((inType == null && outType == null) 
							|| (inType.getBuildVariable().equals(outType.getBuildVariable()))){
						
						set.add(t);
						ToolOrderEstimation est = getToolOrder(t);
						ITool deps[] = est.getDeps();
						for(int k = 0; k < deps.length; k++){
							if(deps[k] != tool)
								set.add(deps[k]);
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
		return (ITool[])set.toArray(new ITool[set.size()]);
	}
	
	private ITool[] doCalcConsumers(ITool tool){
		if(!fToolInProcesSet.add(tool)){
			//TODO throw error?
			if(DbgUtil.DEBUG)
				DbgUtil.trace("loop dependency for tool" + tool.getName());	//$NON-NLS-1$
			return new ITool[0];
		}
		
		String exts[] = tool.getAllOutputExtensions();
		
		ITool tools[] = fCfg.getFilteredTools();
		Set set = new HashSet();
		for(int i = 0; i < tools.length; i++){
			ITool t = tools[i];
			if(t == tool)
				continue;
			
			for(int j = 0; j < exts.length; j++){
				String e = exts[j];
				if(t.buildsFileType(e)){
					IOutputType inType = tool.getOutputType(e);
					IInputType outType = t.getInputType(e);
					if((inType == null && outType == null) 
							|| (inType.getBuildVariable().equals(outType.getBuildVariable()))){
						
						set.add(t);
						ToolOrderEstimation est = getToolOrder(t);
						ITool consumers[] = est.getConsumers();
						for(int k = 0; k < consumers.length; k++){
							if(consumers[k] != tool)
								set.add(consumers[k]);
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
		return (ITool[])set.toArray(new ITool[set.size()]);
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
		IPath paths[] = getGeneratedPaths();
		for(int i = 0; i < paths.length; i++){
			if(paths[i].isPrefixOf(path))
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
	
	protected void resourceAddedToType(BuildIOType type, BuildResource rc){
		if(!type.isInput()){
			String var = type.getLinkId();
			if(var == null)
				var = new String();
			
			Set set = (Set)fVarToAddlInSetMap.get(var);
			if(set != null){
				for(Iterator iter = set.iterator(); iter.hasNext();){
					BuildIOType t = (BuildIOType)iter.next();
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
		return (BuildStep[])fStepList.toArray(new BuildStep[fStepList.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription#findBuildResource(org.eclipse.core.resources.IResource)
	 */
	public IBuildResource getBuildResource(IResource resource){
		return getBuildResource(calcResourceLocation(resource));
	}
}
