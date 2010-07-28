/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.buildmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildMultiStatus;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildStatus;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.DbgUtil;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.DefaultBuildDescriptionFactory;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IConfigurationBuildState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 *
 * This class represents the build description manager
 * The build description manager is the entry point 
 * for all build description-related operations
 *
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class BuildDescriptionManager {
	/**
	 * include information on removed resources into the build model
	 */
	public static final int REMOVED = 1;
	
	/**
	 * include the rebuild state information into the build model
	 */
	public static final int REBUILD = 1 << 1;
	
	/**
	 * include dependencies information into the build model.
	 * the method to be used for calculation is determined by the manager
	 */
	public static final int DEPS = 1 << 2;

	/**
	 * include the dependency file (.d) information in the build model.
	 */
	public static final int DEPFILES = 1 << 3;
	
	private Set<IBuildStep> fVisitedSteps = new HashSet<IBuildStep>();
	private boolean fUp;
	private IBuildDescription fInfo;

	private BuildDescriptionManager(boolean up, IBuildDescription info){
		fUp = up;
		fInfo = info;
	}
	
	/**
	 * creates the build description
	 * @param cfg the build configuration for which the description is to be
	 * created
	 * @param delta the resource delta or null if none
	 * @param flags specifies how the build description should be generated
	 * and what information it should contain.
	 * Can contain the following flags:
	 * BuildDescriptionManager.REBUILD, 
	 * BuildDescriptionManager.REMOVED,
	 * BuildDescriptionManager.DEPS,
	 * BuildDescriptionManager.DEPFILES 
	 * 
	 * @see BuildDescriptionManager#REBUILD 
	 * @see BuildDescriptionManager#REMOVED
	 * @see BuildDescriptionManager#DEPS
	 * @see BuildDescriptionManager#DEPFILES
	 * @return IBuildDescription
	 * @throws CoreException if the build description creation fails
	 */
	static public IBuildDescription createBuildDescription(IConfiguration cfg,
			IResourceDelta delta,
			int flags) throws CoreException {
		return DefaultBuildDescriptionFactory.getInstance().createBuildDescription(cfg, delta, flags);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	static public IBuildDescription createBuildDescription(IConfiguration cfg,
			IConfigurationBuildState bs,
			IResourceDelta delta,
			int flags) throws CoreException {
		return DefaultBuildDescriptionFactory.getInstance().createBuildDescription(cfg, bs, delta, flags);
	}
	
	/**
	 * runs though all steps in build description in the dependency order
	 * and notifies the visitor callback
	 * the order in which steps are enumerated depends on the "up" argument
	 *  
	 * @param visitor represents the visitor callback
	 * @param des represents the build description
	 * @param up specifies the order in which build steps are to be enumerated
	 * if true, enumeration will be performed starting from the input step and 
	 * ending with the output state. Otherwise enumeration will be performed 
	 * in the reversed order 
	 * 
	 * @throws CoreException if the operation fails
	 */
	static public void accept(IStepVisitor visitor, IBuildDescription des, boolean up) throws CoreException {
		BuildDescriptionManager util = new BuildDescriptionManager(up, des);
		
		util.doAccept(visitor);
	}
	
	private void doAccept(IStepVisitor visitor) throws CoreException{
		IBuildStep action = fUp ? fInfo.getInputStep() : fInfo.getOutputStep();

		doAccept(visitor, action, true);
	}

	private boolean doAccept(IStepVisitor visitor, IBuildStep action, boolean doNext) throws CoreException{

		IBuildStep[] actions = getSteps(action, fUp);
		boolean proceed = true;
		
		for(int i = 0; i < actions.length; i++){
			if(!fVisitedSteps.contains(actions[i])){
				if(!doAccept(visitor, actions[i], false)){
					proceed = false;
					break;
				}
			}
		}
		
		if(proceed && !fVisitedSteps.contains(action)){
			proceed = visitor.visit(action) == IStepVisitor.VISIT_CONTINUE;
			fVisitedSteps.add(action);
		}
		
		if(doNext && proceed){
			IBuildStep[] nextActions = getSteps(action, !fUp);
			for(int i = 0; i < nextActions.length; i++){
				if(!fVisitedSteps.contains(nextActions[i])){
					if(!doAccept(visitor, nextActions[i], true)){
						proceed = false;
						break;
					}
				}
			}
		}
		
		return proceed;
	}

	public static IBuildStep[] getSteps(IBuildStep step, boolean input){
		Set<IBuildStep> set = new HashSet<IBuildStep>();
		
		IBuildIOType args[] = input ?
				step.getInputIOTypes() :
					step.getOutputIOTypes();
		
		for(int i = 0; i < args.length; i++){
			IBuildResource rcs[] = args[i].getResources();
			for(int j = 0; j < rcs.length; j++){
				if(input){
					IBuildIOType arg = rcs[j].getProducerIOType();
					if(arg != null && arg.getStep() != null)
						set.add(arg.getStep());
				} else {
					IBuildIOType depArgs[] = rcs[j].getDependentIOTypes();
					for(int k = 0; k < depArgs.length; k++){
						IBuildIOType arg = depArgs[k];
						if(arg != null && arg.getStep() != null)
							set.add(arg.getStep());
					}
				}
			}
		}
		
		return set.toArray(new IBuildStep[set.size()]);
	}

	public static IBuildResource[] filterGeneratedBuildResources(IBuildResource rc[], int rcState){
		List<IBuildResource> list = new ArrayList<IBuildResource>();
		
		addBuildResources(rc, list, rcState);
		return list.toArray(new IBuildResource[list.size()]);
	}

	private static void addBuildResources(IBuildResource rcs[], List<IBuildResource> list, int rcState){
		if(rcs.length == 0)
			return;
		IBuildStep inputAction = rcs[0].getBuildDescription().getInputStep();

		if(DbgUtil.DEBUG)
			DbgUtil.trace(">>found resources to clean:");	//$NON-NLS-1$

		for(int i = 0; i < rcs.length; i++){
			IBuildResource buildRc = rcs[i];
			IPath path = buildRc.getFullPath();
			if(path != null
					&& ((checkFlags(rcState, REBUILD) && buildRc.needsRebuild())
							|| (checkFlags(rcState, REMOVED) && buildRc.isRemoved()))
					&& buildRc.getProducerIOType() != null
					&& buildRc.getProducerIOType().getStep() != inputAction
					&& buildRc.isProjectResource()){

				if(DbgUtil.DEBUG)
					DbgUtil.trace(path.toString());

				list.add(buildRc);
			}
		}

		if(DbgUtil.DEBUG)
			DbgUtil.trace("<<");	//$NON-NLS-1$
	}
	
	private static boolean checkFlags(int var, int flags){
		return (var & flags) == flags;
	}

	/**
	 * returns the project resource for the given build resource or null
	 * if the project does not contain the build resource
	 * 
	 * @param bRc build resource
	 * @return IResource
	 */
	public static IResource findResourceForBuildResource(IBuildResource bRc){
		IPath path = bRc.getFullPath();
		if(path != null){
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			return root.findMember(path);
		}
		
		return null;
	}
	
	/**
	 * cleans the resources to be rebuilt
	 * 
	 * @param des build description
	 */
	public static void cleanGeneratedRebuildResources(IBuildDescription des) throws CoreException{
		IBuildResource bRcs[] = filterGeneratedBuildResources(des.getResources(), REMOVED | REBUILD);
		List<Object[]> failList = new ArrayList<Object[]>();
		
		for(int i = 0; i < bRcs.length; i++){
			if(!bRcs[i].isProjectResource())
				continue;
			
			IResource rc = findResourceForBuildResource(bRcs[i]);
			if(rc != null){
				try {
					rc.delete(true, null);
				} catch (CoreException e) {
					failList.add(new Object[]{rc,e});
				}
			}
		}
		
		if(failList.size() != 0){
			BuildMultiStatus status = new BuildMultiStatus("failed to remove resources", null);	//$NON-NLS-1$
			for(Iterator<Object[]> iter = failList.iterator(); iter.hasNext();){
				Object[] err = iter.next();
				IResource rc = (IResource)err[0];
				CoreException e = (CoreException)err[1];
				status.add(new BuildStatus(rc.getFullPath().toString(), e));
			}
			throw new CoreException(status);
		}
	}
	
	public static IPath getRelPath(IPath cwd, IPath location){
		if(!location.isAbsolute())
			return location;
		
//		IPath path = null;
		IPath tmp = cwd;
		StringBuffer buf = null;
		while(tmp.segmentCount() != 0){
			if(tmp.isPrefixOf(location)){
				IPath p = location.removeFirstSegments(tmp.segmentCount()).setDevice(null);
				if(buf == null)
					return p;
				buf.append(p.makeRelative().toString());
				return new Path(buf.toString());
			}
			if(buf == null){
				buf = new StringBuffer();
				buf.append("../");	//$NON-NLS-1$
			} else {
				buf.append("../");	//$NON-NLS-1$
			}
			tmp = tmp.removeLastSegments(1);
		}
		
		return location;
	}
}
