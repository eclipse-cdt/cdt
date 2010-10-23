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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ToolListMap;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ToolListMap.CollectionEntry;
import org.eclipse.core.runtime.IConfigurationElement;

public class ToolChainModificationHelper {
	
	private static ToolListMap createRealToToolMap(ITool[] tools, boolean ext){
		ToolListMap lMap = new ToolListMap();
		for(int i = 0; i < tools.length; i++){
			ITool tool = tools[i];
			ITool rt = ManagedBuildManager.getRealTool(tool);
			if(rt == null)
				rt = tool;
			ITool t = ext ? ManagedBuildManager.getExtensionTool(tool) : tool;
			if(t == null)
				t = tool;
			lMap.add(rt, t);
		}
		
		return lMap;
	}
	
	private static ToolListMap calculateDifference(ToolListMap m1, ToolListMap m2){
		m1 = (ToolListMap)m1.clone();
		Set ceSet2 = m2.collectionEntrySet();
		
		for(Iterator iter = ceSet2.iterator(); iter.hasNext(); ){
			CollectionEntry entry = (CollectionEntry)iter.next();
			Collection c1 = m2.get(entry.getKey(), false);
			if(c1 != null){
				Collection c2 = entry.getValue();
				int i = c2.size();
				for(Iterator c1Iter = c1.iterator(); i >= 0 && c1Iter.hasNext(); i--){
					c1Iter.next();
					c1Iter.remove();
				}
			}
		}
		
		return m1;
	}

	static public ToolListModificationInfo getModificationInfo(IResourceInfo rcInfo, ITool[] fromTools, ITool[] addedTools, ITool[] removedTools){
		ToolListMap addedMap = createRealToToolMap(addedTools, false);
		for(int i = 0; i < removedTools.length; i++){
			ITool removedTool = removedTools[i];
			ITool realTool = ManagedBuildManager.getRealTool(removedTool);
			if(realTool == null)
				realTool = removedTool;
			
			addedMap.remove(realTool, 0);
		}
		
		ToolListMap removedMap = createRealToToolMap(removedTools, false);
		for(int i = 0; i < addedTools.length; i++){
			ITool addedTool = addedTools[i];
			ITool realTool = ManagedBuildManager.getRealTool(addedTool);
			if(realTool == null)
				realTool = addedTool;
			
			removedMap.remove(realTool, 0);
		}
		
		addedMap.clearEmptyLists();
		removedMap.clearEmptyLists();
		
		ToolListMap curMap = createRealToToolMap(fromTools, false);
		for(Iterator iter = removedMap.collectionEntrySet().iterator(); iter.hasNext();){
			CollectionEntry entry = (CollectionEntry)iter.next();
			List cur = curMap.get(entry.getKey(), false);
			List removed = entry.getValue();
			if(cur != null){
				int numToRemove = removed.size();
				int curSize = cur.size();
				if(curSize <= numToRemove){
					curMap.removeAll(entry.getKey());
				} else {
					for(int i = 0; i < numToRemove; i++){
						cur.remove(0);
					}
				}
			}
		}

		curMap.clearEmptyLists();
		
		for(Iterator iter = addedMap.collectionEntrySet().iterator(); iter.hasNext();){
			CollectionEntry entry = (CollectionEntry)iter.next();
			List cur = curMap.get(entry.getKey(), true);
			List added = entry.getValue();
			int numToAdd = added.size();
			numToAdd -= cur.size();
			for(int i = 0; i < numToAdd; i++){
				cur.add(added.get(i));
			}
			
			if(cur.size() == 0)
				curMap.removeAll(entry.getKey());
		}
		
		curMap.clearEmptyLists();
		
		List resultingList = new ArrayList();
		curMap.putValuesToCollection(resultingList);
		
		return getModificationInfo(rcInfo, fromTools, (ITool[])resultingList.toArray(new ITool[resultingList.size()]));
	}

	static public ToolListModificationInfo getModificationInfo(IResourceInfo rcInfo, ITool[] fromTools, ITool[] toTools){
		
		ToolListMap curMap = createRealToToolMap(fromTools, false);
		List resultingList = new ArrayList();
		List addedList = new ArrayList(7);
		List remainedList = new ArrayList(7);
		List removedList = new ArrayList(7);
		
		for(int i = 0; i < toTools.length; i++){
			ITool tool = toTools[i];
			ITool realTool = ManagedBuildManager.getRealTool(tool);
			if(realTool == null)
				realTool = tool;
			
			ITool remaining = (ITool)curMap.remove(realTool, 0);
			ToolInfo tInfo;
			if(remaining != null){
				tInfo = new ToolInfo(rcInfo, remaining, ToolInfo.REMAINED);
				remainedList.add(tInfo);
			} else {
				tInfo = new ToolInfo(rcInfo, tool, ToolInfo.ADDED);
				addedList.add(tInfo);
			}
			
			resultingList.add(tInfo);
		}
		
		curMap.valuesToCollection(removedList);
		for(ListIterator iter = removedList.listIterator(); iter.hasNext(); ){
			ITool t = (ITool)iter.next();
			iter.set(new ToolInfo(rcInfo, t, ToolInfo.REMOVED));
		}
		
		ToolInfo[] added = listToArray(addedList);
		ToolInfo[] removed = listToArray(removedList);
		
		adjustAddedList(added, removed);

		calculateConverterTools(rcInfo, removed, added, null, null);

		return new ToolListModificationInfo(rcInfo,
				listToArray(resultingList),
				added, 
				removed,
				listToArray(remainedList));
	}
	
	private static ITool getCommonSuperClass(ITool tool1, ITool tool2){
		for(int i = 0; tool2 != null; tool2 = tool2.getSuperClass(), i++){
			if(getSuperClassLevel(tool1, tool2) != -1)
				return tool2;
		}
		
		return null;
	}
	
	private static int getSuperClassLevel(ITool tool, ITool superClass){
		for(int i = 0; tool != null; tool = tool.getSuperClass(), i++){
			if(superClass == tool)
				return i;
		}
		
		return -1;
	}
	
	private static int getLevel(ITool tool){
		int i= 0;
		for(; tool != null; tool = tool.getSuperClass(), i++);
		return i;
	}
	
	private static ITool getBestMatchTool(ITool realTool, ToolInfo[] tools){
		int num = -1;
		ITool bestMatch = null;
		ITool[] identicTools = ManagedBuildManager.findIdenticalTools(realTool);
		
		for(int i = 0; i < tools.length; i++){
			ITool extTool = ManagedBuildManager.getExtensionTool(tools[i].getInitialTool());
			
			for(int k = 0; k < identicTools.length; k++){
				ITool identic = identicTools[k];
				ITool commonSuper = getCommonSuperClass(extTool, identic);
				
				if(commonSuper != null){
					int level = getLevel(commonSuper);
					if(level > num){
						bestMatch = identic;
						num = level;
					}
				}
			}
		}

		return bestMatch;
	}
	
	private static void adjustAddedList(ToolInfo[] adds, ToolInfo[] removes){
		for(int i = 0; i < adds.length; i++){
			ToolInfo add = adds[i];
			
			ITool bestMatch = getBestMatchTool(add.getRealTool(), removes);
			if(bestMatch != null){
				add.updateInitialTool(bestMatch);
			}
		}
	}
	
	private static ToolInfo[] listToArray(List list){
		return (ToolInfo[])list.toArray(new ToolInfo[list.size()]);
	}
	
	private static Map calculateConverterTools(IResourceInfo rcInfo, ToolInfo[] removed, ToolInfo[] added, List remainingRemoved, List remainingAdded){
		if(remainingAdded == null)
			remainingAdded = new ArrayList(added.length);
		if(remainingRemoved == null)
			remainingRemoved = new ArrayList(removed.length);
		
		remainingAdded.clear();
		remainingRemoved.clear();
		
		remainingAdded.addAll(Arrays.asList(added));
		remainingRemoved.addAll(Arrays.asList(removed));
		
		Map resultMap = new HashMap();
		
		for(Iterator rIter = remainingRemoved.iterator(); rIter.hasNext();){
			ToolInfo rti = (ToolInfo)rIter.next();
			ITool r = rti.getInitialTool();
			
			if(r == null || r.getParentResourceInfo() != rcInfo)
				continue;
			

			Map map = ManagedBuildManager.getConversionElements(r);
			if(map.size() == 0)
				continue;

			for(Iterator aIter = remainingAdded.iterator(); aIter.hasNext();){
				ToolInfo ati = (ToolInfo)aIter.next();
				ITool a = ati.getBaseTool();
				
				if(a == null || a.getParentResourceInfo() == rcInfo)
					continue;
				
				a = ati.getBaseExtensionTool();
				if(a == null)
					continue;
				
				IConfigurationElement el = getToolConverterElement(r, a);
				if(el != null){
					ConverterInfo ci = new ConverterInfo(rcInfo, r, a, el); 
					resultMap.put(r, ci);
					rIter.remove();
					aIter.remove();
					ati.setConversionInfo(rti, ci);
					rti.setConversionInfo(ati, ci);
					break;
				}
			}
		}
		
		return resultMap;
	}
	
	private static IConfigurationElement getToolConverterElement(ITool fromTool, ITool toTool){
		return ((Tool)fromTool).getConverterModificationElement(toTool);
	}
}
