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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IModificationStatus;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

public class ToolListModificationInfo {
	private ToolInfo[] fResultingTools;
	private ToolInfo[] fAddedTools;
	private ToolInfo[] fRemovedTools;
	private IResourceInfo fRcInfo;
	
	ToolListModificationInfo(IResourceInfo rcInfo, ToolInfo[] resultingTools, ToolInfo[] added, ToolInfo[] removed, ToolInfo[] remaining){
		fResultingTools = resultingTools;
		fRemovedTools = removed;
		fAddedTools = added;
		fRcInfo = rcInfo;
	}
	
	public IResourceInfo getResourceInfo(){
		return fRcInfo;
	}
	
	public List getResultingToolList(List list) {
		if(list == null)
			list = new ArrayList(fResultingTools.length);
		
		for(int i = 0; i < fResultingTools.length; i++){
			list.add(fResultingTools[i].getResultingTool());
		}
		
		return list;
	}
	
	public ITool[] getResultingTools() {
		ITool[] tools = new ITool[fResultingTools.length];
		
		
		for(int i = 0; i < fResultingTools.length; i++){
			tools[i] = fResultingTools[i].getResultingTool();
		}
		
		return tools;
	}
	
	public ITool[] getRemovedTools() {
		return toToolArray(fRemovedTools, true);
	}

	public ITool[] getAddedTools(boolean resulting) {
		return toToolArray(fAddedTools, !resulting);
	}

	public ITool[] getRemainedTools() {
		return toToolArray(fAddedTools, true);
	}
	
	private static ITool[] toToolArray(ToolInfo[] infos, boolean initialTools){
		ITool[] tools = new ITool[infos.length];
		
		for(int i = 0; i < infos.length; i++){
			tools[i] = initialTools ? infos[i].getInitialTool() : infos[i].getResultingTool();
		}
		
		return tools;
	}
	
	private static ITool[][] toToolArray(ToolInfo[][] infos, boolean initialTools){
		ITool[][] tools = new ITool[infos.length][];
		
		for(int i = 0; i < infos.length; i++){
			tools[i] = toToolArray(infos[i], initialTools);
		}
		
		return tools;
	}
	


	public MultiStatus getModificationStatus(){
		List statusList = new ArrayList();

		ToolInfo[][] conflictInfos = calculateConflictingTools(fResultingTools);
		ITool[][] conflicting = toToolArray(conflictInfos, true);
		
		Map unspecifiedRequiredProps = new HashMap();
		Map unspecifiedProps = new HashMap();
		Set undefinedSet = new HashSet();
		IConfiguration cfg = fRcInfo.getParent();
		ITool[] nonManagedTools = null;
		if(cfg.isManagedBuildOn() && cfg.supportsBuild(true)){
			List list = new ArrayList();
			for(int i = 0; i < fResultingTools.length; i++){
				if(!fResultingTools[i].getInitialTool().supportsBuild(true)){
					list.add(fResultingTools[i].getInitialTool());
				}
			}
			if(list.size() != 0){
				nonManagedTools = (ITool[])list.toArray(new Tool[list.size()]);
			}
		}
		
		IModificationStatus status = new ModificationStatus(unspecifiedRequiredProps, unspecifiedProps, undefinedSet, conflicting, nonManagedTools);

		if(status.getSeverity() != IStatus.OK)
			statusList.add(status);
		
		for(int i = 0; i < fResultingTools.length; i++){
			status = fResultingTools[i].getModificationStatus();
			if(status.getSeverity() != IStatus.OK)
				statusList.add(status);
		}
		
		if(statusList.size() != 0)
			return new MultiStatus(ManagedBuilderCorePlugin.getUniqueIdentifier(), IStatus.INFO, "", null);
		return new MultiStatus(ManagedBuilderCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "", null);
	}

	private ToolInfo[][] calculateConflictingTools(ToolInfo[] infos){
		infos = filterInfos(infos);
		
		return doCalculateConflictingTools(infos);
	}
	
	private ToolInfo[] filterInfos(ToolInfo[] infos){
		if(fRcInfo instanceof FolderInfo){
			Map map = createInitialToolToToolInfoMap(infos);
			ITool[] tools = (ITool[])new ArrayList(map.keySet()).toArray(new ITool[map.size()]);
		
			tools = ((FolderInfo)fRcInfo).filterTools(tools, fRcInfo.getParent().getManagedProject());
			
			if(tools.length < infos.length){
				infos = new ToolInfo[tools.length]; 
				for(int i = 0; i < infos.length; i++){
					infos[i] = (ToolInfo)map.get(tools[i]);
				}
			}
		}
		
		return infos;
	}
	
	private static Map createInitialToolToToolInfoMap(ToolInfo[] infos){
		Map map = new LinkedHashMap();
		for(int i = 0; i < infos.length; i++){
			map.put(infos[i].getInitialTool(), infos[i]);
		}
		
		return map;
	}


	private ToolInfo[][] doCalculateConflictingTools(ToolInfo[] infos){
		HashSet set = new HashSet();
		set.addAll(Arrays.asList(infos));
		List result = new ArrayList();
		for(Iterator iter = set.iterator(); iter.hasNext();){
			ToolInfo ti = (ToolInfo)iter.next();
			ITool t = ti.getInitialTool();
			iter.remove();
			HashSet tmp = (HashSet)set.clone();
			List list = new ArrayList();
			for(Iterator tmpIt = tmp.iterator(); tmpIt.hasNext();){
				ToolInfo otherTi = (ToolInfo)tmpIt.next(); 
				ITool other = otherTi.getInitialTool();
				String conflicts[] = getConflictingInputExts(t, other);
				if(conflicts.length != 0){
					list.add(other);
					tmpIt.remove();
				}
			}
			
			if(list.size() != 0){
				list.add(t);
				result.add(list.toArray(new ToolInfo[list.size()]));
			}
			set = tmp;
			iter = set.iterator();
		}
		
		return (ToolInfo[][])result.toArray(new ToolInfo[result.size()][]);
	}
	
	private String[] getConflictingInputExts(ITool tool1, ITool tool2){
		IProject project = fRcInfo.getParent().getOwner().getProject();
		String ext1[] = ((Tool)tool1).getAllInputExtensions(project);
		String ext2[] = ((Tool)tool2).getAllInputExtensions(project);
		Set set1 = new HashSet(Arrays.asList(ext1));
		Set result = new HashSet();
		for(int i = 0; i < ext2.length; i++){
			if(set1.remove(ext2[i]))
				result.add(ext2[i]);
		}
		return (String[])result.toArray(new String[result.size()]);
	}

	public void apply(){
		((ResourceInfo)fRcInfo).doApply(this);
	}
}
