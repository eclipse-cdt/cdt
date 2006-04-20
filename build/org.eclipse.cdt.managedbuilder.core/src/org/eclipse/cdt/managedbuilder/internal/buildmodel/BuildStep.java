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
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
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
	private boolean fAssignToCalculated;
	
	protected BuildStep(BuildDescription des, ITool tool, IInputType inputType){
		fTool = tool;
		fInputType = inputType;
		fBuildDescription = des;
		
		if(DbgUtil.DEBUG)
			DbgUtil.trace("step " + DbgUtil.stepName(this) + " created");	//$NON-NLS-1$	//$NON-NLS-2$
		
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
			DbgUtil.trace("step  " + DbgUtil.stepName(this) + " removed");	//$NON-NLS-1$	//$NON-NLS-2$

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
		if(fTool == null){
			String step = null;
			if(this == fBuildDescription.getInputStep()){
				step = fBuildDescription.getConfiguration().getPrebuildStep();
			} else if(this == fBuildDescription.getOutputStep()){
				step = fBuildDescription.getConfiguration().getPostbuildStep();
			}
			
			if(step != null && (step = step.trim()).length() > 0){
				String commands[] = step.split(";"); 	//$NON-NLS-1$
				if(cwd == null)
					cwd = calcCWD();

				List list = new ArrayList(); 
				for(int i = 0; i < commands.length; i++){
					IBuildCommand cmds[] = createCommandsFromString(commands[i], cwd, getEnvironment());
					for(int j = 0; j < cmds.length; j++){
						list.add(cmds[j]);
					}
				}
				return (IBuildCommand[])list.toArray(new BuildCommand[list.size()]);
			}
			return null;
		}
		
		if(cwd == null)
			cwd = calcCWD();
		
		performAsignToOption(cwd);
		
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
		
		return createCommandsFromString(info.getCommandLine(), cwd, getEnvironment());
	}
	
	private IPath calcCWD(){
		IPath cwd = fBuildDescription.getDefaultBuildDirLocation();
		
		if(!cwd.isAbsolute())
			cwd = fBuildDescription.getConfiguration().getOwner().getProject().getLocation().append(cwd);
 
		return cwd;
	}

	protected Map getEnvironment(){
		return fBuildDescription.getEnvironment();
	}
	
	protected IBuildCommand[] createCommandsFromString(String cmd, IPath cwd, Map env){
		char arr[] = cmd.toCharArray();
		char expect = 0;
		char prev = 0;
//		int start = 0;
		List list = new ArrayList();
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < arr.length; i++){
			char ch = arr[i]; 
			switch(ch){
			case '\'':
			case '"':
				if(expect == ch){
//					String s = cmd.substring(start, i);
//					list.add(s);
					expect = 0;
//					start = i + 1;
				} else if (expect == 0){
//					String s = cmd.substring(start, i);
//					list.add(s);
					expect = ch;
//					start = i + 1;
				} else {
					buf.append(ch);
				}
				break;
			case ' ':
				if(expect == 0){
					if(prev != ' '){
						list.add(buf.toString());
						buf.delete(0, buf.length());
					}
//					start = i + 1;
				} else {
					buf.append(ch);
				}
				break;
			default:
				buf.append(ch);
				break;
			
			}
			prev = ch;
		}
		
		if(buf.length() > 0)
			list.add(buf.toString());
		
		IPath c = new Path((String)list.remove(0));
		String[] args = (String[])list.toArray(new String[list.size()]);
		
		return new IBuildCommand[]{new BuildCommand(c, args, env, cwd, this)};
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
	
	protected void performAsignToOption(IPath cwd){
		if(fTool == null && !fAssignToCalculated)
			return;
		
		fAssignToCalculated = true;

		IConfiguration cfg = fBuildDescription.getConfiguration();
		
		for(Iterator iter = fInputTypes.iterator(); iter.hasNext();){
			BuildIOType bType = (BuildIOType)iter.next();
			IInputType type = (IInputType)bType.getIoType();
			
			if(type == null)
				continue;
			
			IOption option = fTool.getOptionBySuperClassId(type.getOptionId());
			IOption assignToOption = fTool.getOptionBySuperClassId(type.getAssignToOptionId());
			if (assignToOption != null && option == null) {
				try {
					BuildResource bRcs[] = (BuildResource[])bType.getResources();
					int optType = assignToOption.getValueType();
					if (optType == IOption.STRING) {
						String optVal = "";	   //$NON-NLS-1$
						for (int j=0; j<bRcs.length; j++) {
							if (j != 0) {
								optVal += " ";	   //$NON-NLS-1$
							}
							optVal += BuildDescriptionManager.getRelPath(cwd, bRcs[j].getLocation()).toOSString();
						}
						ManagedBuildManager.setOption(cfg, fTool, assignToOption, optVal);							
					} else if (
							optType == IOption.STRING_LIST ||
							optType == IOption.LIBRARIES ||
							optType == IOption.OBJECTS ||
							optType == IOption.INCLUDE_PATH ||
							optType == IOption.PREPROCESSOR_SYMBOLS){
						//  Mote that when using the enumerated inputs, the path(s) must be translated from project relative 
						//  to top build directory relative
						String[] paths = new String[bRcs.length];
						for (int j=0; j<bRcs.length; j++) {
							paths[j] = BuildDescriptionManager.getRelPath(cwd, bRcs[j].getLocation()).toOSString();
						}
						ManagedBuildManager.setOption(cfg, fTool, assignToOption, paths);
					} else if (optType == IOption.BOOLEAN) {
						if (bRcs.length > 0) {
							ManagedBuildManager.setOption(cfg, fTool, assignToOption, true);
						} else {
							ManagedBuildManager.setOption(cfg, fTool, assignToOption, false);
						}
					} else if (optType == IOption.ENUMERATED) {
						if (bRcs.length > 0) {
							ManagedBuildManager.setOption(cfg, fTool, assignToOption, BuildDescriptionManager.getRelPath(cwd, bRcs[0].getLocation()).toOSString());
						}
					}
				} catch( BuildException ex ) {
				}
			}
		}
	}
}
