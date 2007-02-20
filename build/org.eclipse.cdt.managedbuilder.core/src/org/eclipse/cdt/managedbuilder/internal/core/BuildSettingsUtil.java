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
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;

public class BuildSettingsUtil {
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


}
