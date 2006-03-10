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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildIOType;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.macros.DefaultMacroSubstitutor;
import org.eclipse.cdt.managedbuilder.internal.macros.FileContextData;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IFileContextData;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class BuildStep implements IBuildStep {
	private List fInputTypes = new ArrayList();
	private List fOutputTypes = new ArrayList();
	private ITool fTool;
	private BuildGroup fBuildGroup;
	private boolean fNeedsRebuild;
	private boolean fIsRemoved;
	private BuildDescription fBuildDescription;
	private IInputType fInputType;
	private ITool fLibTool;
	
	protected BuildStep(BuildDescription des, ITool tool, IInputType inputType){
		fTool = tool;
		fInputType = inputType;
		fBuildDescription = des;
		
		if(DbgUtil.DEBUG)
			DbgUtil.traceln("step " + DbgUtil.stepName(this) + " created");	//$NON-NLS-1$	//$NON-NLS-2$
		
		des.stepCreated(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildStep#getInputIOTypes()
	 */
	public IBuildIOType[] getInputIOTypes() {
		return (BuildIOType[])fInputTypes.toArray(new BuildIOType[fInputTypes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildStep#getOutputIOTypes()
	 */
	public IBuildIOType[] getOutputIOTypes() {
		return (BuildIOType[])fOutputTypes.toArray(new BuildIOType[fOutputTypes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildStep#needsRebuild()
	 */
	public boolean needsRebuild() {
		if(fNeedsRebuild 
				|| (fTool != null && fTool.needsRebuild())
				|| (fLibTool != null && fLibTool.needsRebuild()))
			return true;
		
		if(fBuildGroup != null && fBuildGroup.needsRebuild())
			return true;
		
		return false;
	}
	
	public void setRebuildState(boolean rebuild){
		fNeedsRebuild = rebuild;
	}

	public BuildResource[] removeIOType(BuildIOType type) {
		
		BuildResource rcs[] = type.remove();
		
		if(type.isInput())
			fInputTypes.remove(type);
		else
			fOutputTypes.remove(type);
		
		return rcs;
	}
	
	BuildResource[][] remove(){
		BuildResource[][] rcs = clear();
		
		if(DbgUtil.DEBUG)
			DbgUtil.traceln("step  " + DbgUtil.stepName(this) + " removed");	//$NON-NLS-1$	//$NON-NLS-2$

		fBuildDescription.stepRemoved(this);
		fBuildDescription = null;
		
		return rcs;
	}
	
	BuildResource[][] clear(){
		BuildResource[][] rcs = new BuildResource[2][];

		rcs[0] = (BuildResource[])getInputResources();
		rcs[1] = (BuildResource[])getOutputResources();
		
		BuildIOType types[] = (BuildIOType[])getInputIOTypes();
		for(int i = 0; i < types.length; i++){
			removeIOType(types[i]);
		}
		
		types = (BuildIOType[])getOutputIOTypes();
		for(int i = 0; i < types.length; i++){
			removeIOType(types[i]);
		}
		
		return rcs;
	}

	public BuildIOType createIOType(boolean input, boolean primary, /*String ext,*/ IBuildObject ioType) {
		if(input){
			if(fBuildDescription.getInputStep() == this)
				throw new IllegalArgumentException("input step can not have inputs");	//$NON-NLS-1$
		} else {
			if(fBuildDescription.getOutputStep() == this)
				throw new IllegalArgumentException("input step can not have outputs");	//$NON-NLS-1$
		}
		
		BuildIOType arg = new BuildIOType(this, input, primary, /*ext,*/ ioType);
		if(input)
			fInputTypes.add(arg);
		else
			fOutputTypes.add(arg);
		
		return arg;
	}

	public void setTool(ITool tool){
		fTool = tool;
	}
	
	public ITool getTool(){
		return fTool;
	}
	
	public BuildIOType[] getPrimaryTypes(boolean input){
		Iterator iter = input ?
				fInputTypes.iterator() :
				fOutputTypes.iterator();
		
		List list = new ArrayList();
		while(iter.hasNext()){
			BuildIOType arg = (BuildIOType)iter.next();
			if(arg.isPrimary())
				list.add(arg);
		}
		return (BuildIOType[])list.toArray(new BuildIOType[list.size()]);
	}
	
	public BuildIOType getIOTypeForType(IBuildObject ioType, boolean input){
		List list;
		if(input)
			list = fInputTypes;
		else 
			list = fOutputTypes;
		
		if(ioType != null){ 
			for(Iterator iter = list.iterator();iter.hasNext();){
				BuildIOType arg = (BuildIOType)iter.next();
				if(arg.getIoType() == ioType)
					return arg;
			}
		} else {
			if(list.size() > 0)
				return (BuildIOType)list.get(0);
		}
		return null;
	}

	protected void setGroup(BuildGroup group){
		fBuildGroup = group;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildStep#getInputResources()
	 */
	public IBuildResource[] getInputResources() {
		return getResources(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildStep#getOutputResources()
	 */
	public IBuildResource[] getOutputResources() {
		return getResources(false);
	}
	
	public IBuildResource[] getResources(boolean input){
		Iterator iter = input ?
				fInputTypes.iterator() :
					fOutputTypes.iterator();
		
		Set set = new HashSet();

		while(iter.hasNext()){
			IBuildResource rcs[] = ((BuildIOType)iter.next()).getResources();
			for(int j = 0; j < rcs.length; j++){
				set.add(rcs[j]);
			}
		}
		return (BuildResource[])set.toArray(new BuildResource[set.size()]);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildStep#getCommands(org.eclipse.core.runtime.IPath, java.util.Map, java.util.Map, boolean)
	 */
	public IBuildCommand[] getCommands(IPath cwd, Map inputArgValues, Map outputArgValues, boolean resolveAll) {
		if(fTool == null)
			return null;
		
		if(!cwd.isAbsolute())
			cwd = fBuildDescription.getConfiguration().getOwner().getProject().getLocation().append(cwd);
		
		BuildResource inRc = getRcForMacros(true);
		BuildResource outRc = getRcForMacros(false);
		IPath inRcPath = inRc != null ? BuildDescriptionManager.getRelPath(cwd, inRc.getLocation()) : null;
		IPath outRcPath = outRc != null ? BuildDescriptionManager.getRelPath(cwd, outRc.getLocation()) : null;
		IManagedCommandLineGenerator gen = fTool.getCommandLineGenerator();
		FileContextData data = new FileContextData(inRcPath, outRcPath, null, fTool);
		IManagedCommandLineInfo info = gen.generateCommandLineInfo(fTool, 
				resolveMacros(fTool.getToolCommand(), data, true),
				getCommandFlags(inRcPath, outRcPath, resolveAll), 
				resolveMacros(fTool.getOutputFlag(), data, true), 
				resolveMacros(fTool.getOutputPrefix(), data, true),
				listToString(resourcesToStrings(cwd, getPrimaryResources(false)), " "), 	//$NON-NLS-1$
				resourcesToStrings(cwd, getPrimaryResources(true)), 
				fTool.getCommandLinePattern());
		
		return createCommandsFromString(info.getCommandLine(), cwd);
	}
	
	protected IBuildCommand[] createCommandsFromString(String cmd, IPath cwd){
		String[] cmds = cmd.split(" ");	//$NON-NLS-1$
		IPath c = new Path(cmds[0]);
		String[] args = new String[cmds.length - 1];
		System.arraycopy(cmds, 1, args, 0, args.length);
		
		return new IBuildCommand[]{new BuildCommand(c, args, null, cwd, this)};
	}
	
	private BuildResource[] getPrimaryResources(boolean input){
		BuildIOType[] types = getPrimaryTypes(input);
		if(types.length == 0)
			types = input ? (BuildIOType[])getInputIOTypes() : (BuildIOType[])getOutputIOTypes();
		List list = new ArrayList();
		
		for(int i = 0; i < types.length; i++){
			BuildResource [] rcs = (BuildResource[])types[i].getResources();
			
			for(int j = 0; j < rcs.length; j++){
				list.add(rcs[j]);
			}
		}
		
		return (BuildResource[])list.toArray(new BuildResource[list.size()]);
	}
	
	private String[] resourcesToStrings(IPath cwd, BuildResource rcs[]){
		List list = new ArrayList(rcs.length);
		
		for(int i = 0; i < rcs.length; i++){
			list.add(BuildDescriptionManager.getRelPath(cwd, rcs[i].getLocation()).toOSString());
		}
		return (String[])list.toArray(new String[list.size()]);
	}
	
	private String resolveMacros(String str, IFileContextData fileData, boolean resolveAll){
		try {
			String tmp = resolveAll ? ManagedBuildManager.getBuildMacroProvider().resolveValue(str, "", " ", IBuildMacroProvider.CONTEXT_FILE, fileData)	//$NON-NLS-1$	//$NON-NLS-2$
					:
						ManagedBuildManager.getBuildMacroProvider().resolveValueToMakefileFormat(str, "", " ", IBuildMacroProvider.CONTEXT_FILE, fileData);	//$NON-NLS-1$	//$NON-NLS-2$
			if((tmp = tmp.trim()).length() != 0)
				str = tmp;
		} catch (BuildMacroException e) {
		}

		return str;
	}
	
	private String[] getCommandFlags(IPath inRcPath, IPath outRcPath, boolean resolveAll){
		try {
			return resolveAll ? 
					((Tool)fTool).getToolCommandFlags(inRcPath, outRcPath, 
							new DefaultMacroSubstitutor(IBuildMacroProvider.CONTEXT_FILE, new FileContextData(inRcPath, outRcPath, null, fTool), "", " "))	//$NON-NLS-1$	//$NON-NLS-2$
					:
						fTool.getToolCommandFlags(inRcPath, outRcPath);
		} catch (BuildException e) {
		}
		return new String[0];
	}
	
	private String listToString(String[] list, String delimiter){
		if(list == null || list.length == 0)
			return new String();

		StringBuffer buf = new StringBuffer(list[0]);
		
		for(int i = 1; i < list.length; i++){
			buf.append(delimiter).append(list[i]);
		}
		
		return buf.toString();
	}
	
	private BuildResource getRcForMacros(boolean input){
		IBuildIOType types[] = getPrimaryTypes(input);
		if(types.length != 0){
			for(int i = 0; i < types.length; i++){
				IBuildResource rcs[] = types[i].getResources();
				if(rcs.length != 0)
					return (BuildResource)rcs[0];
			}
		}
		
		types = input ? getInputIOTypes() : getOutputIOTypes();
		if(types.length != 0){
			for(int i = 0; i < types.length; i++){
				IBuildResource rcs[] = types[i].getResources();
				if(rcs.length != 0)
					return (BuildResource)rcs[0];
			}
		}

		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildStep#isRemoved()
	 */
	public boolean isRemoved(){
		return fIsRemoved;
	}
	
	public void setRemoved() {
		fIsRemoved = true;
		fNeedsRebuild = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.builddescription.IBuildStep#getBuildDescription()
	 */
	public IBuildDescription getBuildDescription(){
		return fBuildDescription;
	}
	
	boolean isMultiAction(){
		BuildIOType args[] = getPrimaryTypes(true);
		BuildIOType arg = args.length > 0 ? args[0] : null;
		
		if(arg != null){
			if(arg.getIoType() != null)
				return ((IInputType)arg.getIoType()).getMultipleOfType();
			return fTool != null && fTool == ((Configuration)fBuildDescription.getConfiguration()).calculateTargetTool();
		}
		return false;
	}
	
	public IInputType getInputType(){
		return fInputType;
	}
	
	public void setLibTool(ITool libTool){
		fLibTool = libTool;
	}
	
	public ITool getLibTool(){
		return fLibTool;
	}
}
