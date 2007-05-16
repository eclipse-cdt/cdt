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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.OptionStringValue;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class BuildSettingsUtil {
	private static final int[] COMMON_SETTINGS_IDS = new int[]{
		IOption.INCLUDE_PATH,
		IOption.PREPROCESSOR_SYMBOLS,
		IOption.LIBRARIES,
		IOption.OBJECTS,
		IOption.INCLUDE_FILES,
		IOption.LIBRARY_PATHS,
		IOption.LIBRARY_FILES,
		IOption.MACRO_FILES,
	};

	public static void disconnectDepentents(IConfiguration cfg, ITool[] tools){
		for(int i = 0; i < tools.length; i++){
			disconnectDepentents(cfg, tools[i]);
		}
	}

	public static void disconnectDepentents(IConfiguration cfg, ITool tool){
		ITool deps[] = getDependentTools(cfg, tool);
		for(int i = 0; i < deps.length; i++){
			disconnect(deps[i], tool);
		}
	}
	
	private static void disconnect(ITool child, ITool superClass){
		ITool directChild = child;
		for(;directChild != null; directChild = directChild.getSuperClass()){
			if(superClass.equals(directChild.getSuperClass()))
				break;
		}
		
		if(directChild == null)
			return;
		
		((Tool)directChild).copyNonoverriddenSettings((Tool)superClass);
		((Tool)directChild).setSuperClass(superClass.getSuperClass());
	}
	
	public static ITool[] getDependentTools(IConfiguration cfg, ITool tool){
		IResourceInfo rcInfos[] = cfg.getResourceInfos();
		List list = new ArrayList();
		for(int i = 0; i < rcInfos.length; i++){
			calcDependentTools(rcInfos[i], tool, list);
		}
		return (Tool[])list.toArray(new Tool[list.size()]);
	}
	
	private static List calcDependentTools(IResourceInfo info, ITool tool, List list){
		return calcDependentTools(info.getTools(), tool, list);
	}
	
	public static List calcDependentTools(ITool tools[], ITool tool, List list){
		if(list == null)
			list = new ArrayList();
		
		for(int i = 0; i < tools.length; i++){
			ITool superTool = tools[i];
			for(;superTool != null; superTool = superTool.getSuperClass()){
				if(superTool.equals(tool)){
					list.add(tools[i]);
				}
			}
		}
		
		return list;
	}

	public static void copyCommonSettings(ITool fromTool, ITool toTool){
		Tool fromT = (Tool)fromTool;
		Tool toT = (Tool)toTool;
		List values = new ArrayList();
		for(int i = 0; i < COMMON_SETTINGS_IDS.length; i++){
			int type = COMMON_SETTINGS_IDS[i];
			IOption[] toOptions = toT.getOptionsOfType(type);
			if(toOptions.length == 0)
				continue;

			IOption[] fromOptions = fromT.getOptionsOfType(type);
			values.clear();
			for(int k = 0; k < fromOptions.length; k++){
				Option fromOption = (Option)fromOptions[k];
				if(fromOption.getParent() != fromTool)
					continue;
				
				List v = (List)fromOption.getExactValue();
				values.addAll(v);
			}
			
			if(values.size() == 0)
				continue;

			IOption toOption = toOptions[0];
			
			try {
				OptionStringValue[] v = toOption.getBasicStringListValueElements();
				if(v.length != 0)
					values.addAll(Arrays.asList(v));
			} catch (BuildException e) {
				ManagedBuilderCorePlugin.log(e);
			}
			
			OptionStringValue[] v = (OptionStringValue[])values.toArray(new OptionStringValue[values.size()]);
			IResourceInfo rcInfo = toTool.getParentResourceInfo();
			
			ManagedBuildManager.setOption(rcInfo, toTool, toOption, v);

			values.clear();
		}
	}
	
	public static void checkApplyDescription(IProject project, ICProjectDescription des) throws CoreException{
		ICConfigurationDescription[] cfgs = des.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			if(!ManagedBuildManager.CFG_DATA_PROVIDER_ID.equals(cfgs[i].getBuildSystemId()))
				des.removeConfiguration(cfgs[i]);
		}
		
		CoreModel.getDefault().setProjectDescription(project, des);
	}
}
