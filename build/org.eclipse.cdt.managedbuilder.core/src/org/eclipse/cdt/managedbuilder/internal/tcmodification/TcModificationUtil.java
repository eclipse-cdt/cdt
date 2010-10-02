/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.core.runtime.IPath;

public class TcModificationUtil {

	private static void processFolderInfoChildren(FolderInfo foInfo, PerTypeMapStorage storage, IToolChain rtc, Map tcMap, Map toolMap, boolean addSkipInfo){
		IResourceInfo rcInfos[] = foInfo.getDirectChildResourceInfos();
		for(int i = 0; i < rcInfos.length; i++){
			IResourceInfo rc = rcInfos[i];
			if(rc instanceof ResourceConfiguration){
				ResourceConfiguration fi = (ResourceConfiguration)rc;
				IPath p = fi.getPath();
				ITool[] tools = fi.getTools();
				processTools(storage.getMap(IRealBuildObjectAssociation.OBJECT_FILE_INFO, true), p, tools, toolMap, addSkipInfo);
			} else {
				FolderInfo childFoInfo = (FolderInfo)rc;
				IToolChain tc = foInfo.getToolChain();
				tc = ManagedBuildManager.getRealToolChain(tc);
				if(tc == rtc){
					if(addSkipInfo && tcMap != null){
						Set set = getPathTreeSet(tcMap, tc);
						set.add(childFoInfo.getPath());
					}
					processFolderInfoChildren(childFoInfo, storage, tc, tcMap, toolMap, addSkipInfo); 
				} else {
					processFolderInfo(storage, childFoInfo, null, false);
				}
			}
		}
	}
	
	public static PerTypeMapStorage createChildObjectsRealToolToPathSet(FolderInfo foInfo, Map toolChainMap, Map toolsMap, boolean addSkipPaths){
		PerTypeMapStorage storage = new PerTypeMapStorage();
		
		IToolChain tc = foInfo.getToolChain();
		IToolChain rTc = ManagedBuildManager.getRealToolChain(tc);
		ITool[] tools = rTc.getTools();
		toolsMap.clear();
		if(toolChainMap != null)
			toolChainMap.clear();

		for(int i = 0; i < tools.length; i++){
			toolsMap.put(ManagedBuildManager.getRealTool(tools[i]), null);
		}
		
		
		processFolderInfoChildren(foInfo, storage, rTc, toolChainMap, toolsMap, addSkipPaths);
		
		return storage;
	}
	
	public static PerTypeMapStorage createParentObjectsRealToolToPathSet(final FolderInfo foInfo){
		PerTypeMapStorage storage = new PerTypeMapStorage();
		IConfiguration cfg = foInfo.getParent();
		FolderInfo rf = (FolderInfo)cfg.getRootFolderInfo();
		IPath p = rf.getPath();

		IBuilder realBuilder = ManagedBuildManager.getRealBuilder(cfg.getBuilder());
		Map map = storage.getMap(IRealBuildObjectAssociation.OBJECT_BUILDER, true);
		TreeSet pathSet = new TreeSet(PathComparator.INSTANCE);
		pathSet.add(p);
		map.put(realBuilder, pathSet);
		
		IRealBuildObjectAssociation realCfg = ((Configuration)cfg).getRealBuildObject();
		map = storage.getMap(IRealBuildObjectAssociation.OBJECT_CONFIGURATION, true);
		pathSet = new TreeSet(PathComparator.INSTANCE);
		pathSet.add(p);
		map.put(realCfg, pathSet);
		
		if(!foInfo.isRoot()){
			Set allRcInfos = new HashSet(Arrays.asList(cfg.getResourceInfos()));
			allRcInfos.removeAll(foInfo.getChildResourceInfoList(true));
			for(Iterator iter = allRcInfos.iterator(); iter.hasNext(); ){
				Object rc = iter.next();
				if(rc instanceof ResourceConfiguration){
					processFileInfo(storage, (ResourceConfiguration)rc, null, false);
				} else {
					processFolderInfo(storage, (FolderInfo)rc, null, false);
				}
			}
		}
		
		return storage;
	}

	public static PerTypeMapStorage cloneRealToolToPathSet(PerTypeMapStorage storage){
		storage = (PerTypeMapStorage)storage.clone();
		int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
		for(int i = 0; i < types.length; i++){
			Map map = storage.getMap(types[i], false);
			if(map != null){
				for(Iterator iter = map.entrySet().iterator(); iter.hasNext(); ){
					Map.Entry entry = (Map.Entry)iter.next();
					entry.setValue(((TreeSet)entry.getValue()).clone());
				}
			}
		}

		return storage;
	}
	
	public static PerTypeMapStorage createRealToolToPathSet(IConfiguration cfg, PerTypeMapStorage skipMapStorage, boolean addSkipPaths){
		PerTypeMapStorage storage = new PerTypeMapStorage();
		FolderInfo rf = (FolderInfo)cfg.getRootFolderInfo();
		IPath p = rf.getPath();

		Map skipMap = skipMapStorage != null ? skipMapStorage.getMap(IRealBuildObjectAssociation.OBJECT_BUILDER, false) : null;
		IBuilder realBuilder = ManagedBuildManager.getRealBuilder(cfg.getBuilder());
		if(skipMap != null && skipMap.containsKey(realBuilder)){
			if(addSkipPaths){
				Set set = getPathTreeSet(skipMap, realBuilder);
				set.add(p);
			}
		} else {
			Map map = storage.getMap(IRealBuildObjectAssociation.OBJECT_BUILDER, true);
			TreeSet pathSet = new TreeSet(PathComparator.INSTANCE);
			pathSet.add(p);
			map.put(realBuilder, pathSet);
		}
		
		skipMap = skipMapStorage != null ? skipMapStorage.getMap(IRealBuildObjectAssociation.OBJECT_CONFIGURATION, false) : null;
		IRealBuildObjectAssociation realCfg = ((Configuration)cfg).getRealBuildObject();
		if(skipMap != null && skipMap.containsKey(realCfg)){
			if(addSkipPaths){
				Set set = getPathTreeSet(skipMap, realCfg);
				set.add(p);
			}
		} else {
			Map map = storage.getMap(IRealBuildObjectAssociation.OBJECT_CONFIGURATION, true);
			TreeSet pathSet = new TreeSet(PathComparator.INSTANCE);
			pathSet.add(p);
			map.put(realCfg, pathSet);
		}
		
		processFolderInfo(storage, rf, skipMapStorage, addSkipPaths);
		IResourceInfo[] rcInfos = rf.getChildResourceInfos();
		for(int i = 0; i < rcInfos.length; i++){
			IResourceInfo rc = rcInfos[i];
			if(rc instanceof ResourceConfiguration){
				processFileInfo(storage, (ResourceConfiguration)rc, skipMapStorage, addSkipPaths);
			} else {
				processFolderInfo(storage, (FolderInfo)rc, skipMapStorage, addSkipPaths);
			}
		}
		
		return storage;
	}
	
	public static TreeMap<IPath, PerTypeSetStorage> createResultingChangesMap(TreeMap<IPath, PerTypeSetStorage> resultingMap, TreeMap<IPath, PerTypeSetStorage> initialMap){
		int[] types = new int []{
				IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, 
				IRealBuildObjectAssociation.OBJECT_BUILDER,
				IRealBuildObjectAssociation.OBJECT_TOOL,
				};
		
		TreeMap<IPath, PerTypeSetStorage> result = new TreeMap<IPath, PerTypeSetStorage>(PathComparator.INSTANCE);
		@SuppressWarnings("unchecked")
		TreeMap<IPath, PerTypeSetStorage> clone = (TreeMap<IPath, PerTypeSetStorage>)initialMap.clone();
		initialMap = clone;
		
		for (Entry<IPath, PerTypeSetStorage> entry : resultingMap.entrySet()) {
			IPath oPath = entry.getKey();
			
			PerTypeSetStorage resStorage = entry.getValue();
			PerTypeSetStorage initStorage = initialMap.remove(oPath);
			PerTypeSetStorage storage;
			
			if(initStorage == null || initStorage.isEmpty(true)){
				if(resStorage != null && !resStorage.isEmpty(true)){
					storage = (PerTypeSetStorage)resStorage.clone();
				} else {
					storage = new PerTypeSetStorage();
				}
			} else if(resStorage == null || resStorage.isEmpty(true)){
				storage = new PerTypeSetStorage();
				for(int i = 0; i < types.length; i++){
					Set set = initStorage.getSet(types[i], false);
					if(set != null && set.size() != 0){
						storage.getSet(types[i], true);
					}
				}
			} else {
				Set tcInitSet, resSet, setToStore;
				Set bInitSet = null, tInitSet = null;
				storage = new PerTypeSetStorage();
				
				tcInitSet = initStorage.getSet(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, false);
				resSet = resStorage.getSet(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, false);
				setToStore = compareSets(resSet, tcInitSet);
				if(setToStore != null) {
					storage.getSet(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, true).addAll(setToStore);
					
					//need to change the initial storage to contain 
					//tools and a builder from the newly assigned tool-chain
					//for the correct change calculation
							
					ToolChain tc = setToStore.size() != 0 ?
							(ToolChain)setToStore.iterator().next() : null;
					
					IPath path = oPath;
					if(tc != null){
						tInitSet = new LinkedHashSet();
						TcModificationUtil.getRealObjectsSet((Tool[])tc.getTools(), tInitSet);
						if(path.segmentCount() == 0){
							bInitSet = new LinkedHashSet();
							IBuilder builder = tc.getBuilder();
							if(builder != null){
								bInitSet.add(ManagedBuildManager.getRealBuilder(builder));
							}
						}
					} else {
						tcInitSet = Collections.EMPTY_SET;
						if(path.segmentCount() == 0){
							bInitSet = Collections.EMPTY_SET;
						}
					}
						}

				if(bInitSet == null)
					bInitSet = initStorage.getSet(IRealBuildObjectAssociation.OBJECT_BUILDER, false);
				
				resSet = resStorage.getSet(IRealBuildObjectAssociation.OBJECT_BUILDER, false);
				setToStore = compareSets(resSet, bInitSet);
				if(setToStore != null) {
					storage.getSet(IRealBuildObjectAssociation.OBJECT_BUILDER, true).addAll(setToStore);
					}

				if(tInitSet == null)
					tInitSet = initStorage.getSet(IRealBuildObjectAssociation.OBJECT_TOOL, false);
				
				resSet = resStorage.getSet(IRealBuildObjectAssociation.OBJECT_TOOL, false);
				setToStore = compareSets(resSet, tInitSet);
				if(setToStore != null) {
					storage.getSet(IRealBuildObjectAssociation.OBJECT_TOOL, true).addAll(setToStore);
				}
			}
			
			if(!storage.isEmpty(false)){
				result.put(oPath, storage);
			}
		}
		
		if(initialMap.size() != 0){
			for(Iterator iter = initialMap.entrySet().iterator(); iter.hasNext(); ){
				Map.Entry entry = (Map.Entry)iter.next();
				IPath oPath = (IPath) entry.getKey();
				
				PerTypeSetStorage initStorage = (PerTypeSetStorage)entry.getValue();

				if(!initStorage.isEmpty(true)){
					PerTypeSetStorage storage = new PerTypeSetStorage();

					for(int i = 0; i < types.length; i++){
						Set set = initStorage.getSet(types[i], false);
						if(set != null && set.size() != 0){
							storage.getSet(types[i], true).addAll(set);
						}
					}
					
					if(!storage.isEmpty(false)){
						result.put(oPath, storage);
					}

				}
				
			}
		}

		
		return result;
	}
	
	private static Set compareSets(Set resSet, Set initSet){
		Set result = null;
		if(initSet == null || initSet.isEmpty()){
			if(resSet != null && !resSet.isEmpty()){
				result = resSet;
			}
		} else if (resSet == null || resSet.isEmpty()){
			result = Collections.EMPTY_SET;
		} else {
			if(!initSet.equals(resSet)){
				result = resSet;
			}
		}
		
		return result;
	}

	private static void processFolderInfo(PerTypeMapStorage storage, FolderInfo info, PerTypeMapStorage skipMapStorage, boolean addSkipPaths){
		IPath p = info.getPath();
		IToolChain rtc = ManagedBuildManager.getRealToolChain(info.getToolChain());
		Map skipMap = skipMapStorage != null ? skipMapStorage.getMap(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, false) : null;
		if(skipMap != null && skipMap.containsKey(rtc)){
			if(addSkipPaths){
				TreeSet set = getPathTreeSet(skipMap, rtc);
				set.add(p);
			}
		} else {
			Map map = storage.getMap(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, true);
			TreeSet set = getPathTreeSet(map, rtc);
			set.add(p);
		}
		
		ITool[] tools = info.getTools();
		processTools(storage, p, tools, skipMapStorage, addSkipPaths);
	}
	
	public static IRealBuildObjectAssociation[] getRealObjects(IRealBuildObjectAssociation[] objs, Class clazz){
		LinkedHashSet set = new LinkedHashSet();
		for(int i = 0; i < objs.length; i++){
			set.add(objs[i].getRealBuildObject());
		}
		Object[] array = (Object[])Array.newInstance(clazz, set.size());
		return (IRealBuildObjectAssociation[])set.toArray(array);
	}

	public static Set getRealObjectsSet(IRealBuildObjectAssociation[] objs, Set set){
		if(set == null)
			set = new LinkedHashSet();
		for(int i = 0; i < objs.length; i++){
			set.add(objs[i].getRealBuildObject());
		}
		return set;
	}

	public static Map<? extends IRealBuildObjectAssociation, ? extends IRealBuildObjectAssociation> getRealToObjectsMap(IRealBuildObjectAssociation[] objs, Map<IRealBuildObjectAssociation, IRealBuildObjectAssociation> map){
		if(map == null)
			map = new LinkedHashMap<IRealBuildObjectAssociation, IRealBuildObjectAssociation>();
		for(int i = 0; i < objs.length; i++){
			map.put(objs[i].getRealBuildObject(), objs[i]);
		}
		return map;
	}

	private static void processFileInfo(PerTypeMapStorage storage, ResourceConfiguration info, PerTypeMapStorage skipMapStorage, boolean addSkipPaths){
		IPath p = info.getPath();
		ITool[] tools = info.getTools();
		processTools(storage, p, tools, skipMapStorage, addSkipPaths);
	}

	private static void processTools(PerTypeMapStorage storage, IPath path, ITool[] tools, PerTypeMapStorage skipMapStorage, boolean addSkipPaths){
		Map map = storage.getMap(IRealBuildObjectAssociation.OBJECT_TOOL, true);
		Map skipMap = skipMapStorage != null ? skipMapStorage.getMap(IRealBuildObjectAssociation.OBJECT_TOOL, false) : null;
		
		processTools(map, path, tools, skipMap, addSkipPaths);
	}

	private static void processTools(Map storageMap, IPath path, ITool[] tools, Map skipMap, boolean addSkipPaths){
		for(int i = 0; i < tools.length; i++){
			ITool rt = ManagedBuildManager.getRealTool(tools[i]);
			if(skipMap != null && skipMap.containsKey(rt)){
				if(addSkipPaths){
					TreeSet set = getPathTreeSet(skipMap, rt);
					set.add(path);
				}
			} else {
				TreeSet set = getPathTreeSet(storageMap, rt);
				set.add(path);
			}
		}
	}
		
	
	public static TreeSet getPathTreeSet(Map map, Object obj){
		TreeSet set = (TreeSet)map.get(obj);
		if(set == null){
			set = new TreeSet(PathComparator.INSTANCE);
			map.put(obj, set);
		}
		return set;
	}

	public static ArrayList getArrayList(Map map, Object obj){
		ArrayList list = (ArrayList)map.get(obj);
		if(list == null){
			list = new ArrayList();
			map.put(obj, list);
		}
		return list;
	}

	public static Object removeBuilderInfo(PerTypeMapStorage storage, IBuilder builder){
		return storage.getMap(IRealBuildObjectAssociation.OBJECT_BUILDER, true).remove(builder);
	}

	public static void restoreBuilderInfo(PerTypeMapStorage storage, IBuilder builder, Object obj){
		storage.getMap(IRealBuildObjectAssociation.OBJECT_BUILDER, true).put((Builder) builder, obj);
	}

//	public static boolean removeToolInfo(PerTypeMapStorage storage, IPath path, ITool tool){
//		Map map = storage.getMap(IRealBuildObjectAssociation.OBJECT_TOOL, false);
//		if(map == null)
//			return false;
//		Set set = (Set)map.get(tool);
//		if(set == null)
//			return false;
//		boolean result = set.remove(path);
//		if(set.isEmpty())
//			map.remove(tool);
//		return result;
//	}
//	
//	public static void restoreToolInfo(PerTypeMapStorage storage, IPath path, ITool tool){
//		Map map = storage.getMap(IRealBuildObjectAssociation.OBJECT_TOOL, true);
//		Set set = TcModificationUtil.getPathTreeSet(map, tool);
//		set.add(path);
//	}
//	
//	public static Set removeToolInfoWithChildren(PerTypeMapStorage storage, IPath path, ITool tool){
//		Map map = storage.getMap(IRealBuildObjectAssociation.OBJECT_TOOL, false);
//		if(map == null)
//			return null;
//		SortedSet set = (SortedSet)map.get(tool);
//		if(set == null)
//			return null;
//		
//		Set rmSet = PathComparator.getChildPathSet(set, path, true, true);
//		if(rmSet.size() == 0)
//			return null;
//		
//		set.removeAll(rmSet);
//		return rmSet;
//	}
//	
//	public static void restoreToolInfoWithChildren(PerTypeMapStorage storage, Set restoreSet, ITool tool){
//		Map map = storage.getMap(IRealBuildObjectAssociation.OBJECT_TOOL, true);
//		Set set = TcModificationUtil.getPathTreeSet(map, tool);
//		set.addAll(restoreSet);
//	}
	
	public static void removePaths(Map map, IRealBuildObjectAssociation obj, Set paths){
		Set objPaths = (Set)map.get(obj);
		if(objPaths == null)
			return;
		
		objPaths.removeAll(paths);
		if(objPaths.size() == 0)
			map.remove(obj);
	}
	
	public static void addPaths(Map map, IRealBuildObjectAssociation obj, Set paths){
		if(paths.size() == 0)
			return;
		
		Set objPaths = (Set)map.get(obj);
		if(objPaths == null){
			objPaths = new TreeSet(PathComparator.INSTANCE);
			map.put(obj, objPaths);
		}
		
		objPaths.addAll(paths);
	}
	
	public static void addPath(Map map, IRealBuildObjectAssociation obj, IPath path){
		Set objPaths = (Set)map.get(obj);
		if(objPaths == null){
			objPaths = new TreeSet(PathComparator.INSTANCE);
			map.put(obj, objPaths);
		}
		
		objPaths.add(path);
	}

	public static void applyBuilder(PerTypeMapStorage storage, IPath path, IBuilder builder){
		Map bMap = storage.getMap(IRealBuildObjectAssociation.OBJECT_BUILDER, true);
		bMap.clear();
		IBuilder realBuilder = ManagedBuildManager.getRealBuilder(builder);
		TreeSet set = getPathTreeSet(bMap, realBuilder);
		set.add(path);
	}

	public static void applyBuilder(TreeMap pathMap, IPath path, IBuilder builder){
		IBuilder realBuilder = ManagedBuildManager.getRealBuilder(builder);
		PerTypeSetStorage storage = (PerTypeSetStorage)pathMap.get(path);
		Set set = storage.getSet(IRealBuildObjectAssociation.OBJECT_BUILDER, true);
		set.clear();
		set.add(realBuilder);
	}
	
	public static TreeMap<IPath, PerTypeSetStorage> createPathMap(IConfiguration cfg){
		//TODO: optimize to calculate the map directly
		PerTypeMapStorage storage = createRealToolToPathSet(cfg, null, false);
		return createPathMap(storage);
	}

	public static TreeMap<IPath,PerTypeSetStorage> createPathMap(PerTypeMapStorage storage){
		int[] types = ObjectTypeBasedStorage.getSupportedObjectTypes();
		TreeMap<IPath,PerTypeSetStorage> result = new TreeMap<IPath,PerTypeSetStorage>(PathComparator.INSTANCE);
		for(int i = 0; i < types.length; i++){
			int type = types[i];
			Map<IRealBuildObjectAssociation, Set<IPath>> map = storage.getMap(type, false);
			if(map == null)
				continue;
			
			Set<Entry<IRealBuildObjectAssociation, Set<IPath>>> entrySet = map.entrySet();
			for (Entry<IRealBuildObjectAssociation, Set<IPath>> entry : entrySet) {
				IRealBuildObjectAssociation pathKey = entry.getKey();
				Set<IPath> pathSet = entry.getValue();
				for (IPath path : pathSet) {
					PerTypeSetStorage oset = result.get(path);
					if(oset == null){
						oset = new PerTypeSetStorage();
						result.put(path, oset);
					}
					
					@SuppressWarnings("unchecked")
					Set<IRealBuildObjectAssociation> set = (Set<IRealBuildObjectAssociation>) oset.getSet(type, true);
					set.add(pathKey);
				}
			}
		}
		
		return result;
	}

	public static TreeMap clonePathMap(TreeMap map){
		map = (TreeMap)map.clone();
		
		for(Iterator iter = map.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry)iter.next();
			entry.setValue(((PerTypeSetStorage)entry.getValue()).clone());
		}
		
		return map;
	}

	private static boolean pathContainsObjects(PerTypeMapStorage storage, IPath path, int type){
		Map map = storage.getMap(type, false);
		if(map == null)
			return false;
		
		for(Iterator iter = map.values().iterator(); iter.hasNext(); ){
			SortedSet set = (SortedSet)iter.next();
			if(set.contains(path))
				return true;
		}
		
		return false;
	}
	
	public static SortedSet getDirectChildUsagePaths(PerTypeMapStorage storage, IPath path, IRealBuildObjectAssociation obj, SortedSet inclusionPaths){
		Map objMap = storage.getMap(obj.getType(), false);

		SortedSet objPaths = (SortedSet)objMap.get(obj);
		if(DbgTcmUtil.DEBUG){
			if(!objPaths.contains(path)){
				DbgTcmUtil.fail();
			}
		}

		SortedSet objChildPaths = PathComparator.getChildPathSet(objPaths, path, false, true);
		if(inclusionPaths != null) {
			objChildPaths.retainAll(inclusionPaths);
		}
		
		for(Iterator iter = objMap.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry)iter.next();
			Object cur = entry.getKey();
			if(obj == cur)
				continue;
			
			SortedSet curPaths = (SortedSet)entry.getValue();
			
			curPaths = PathComparator.getChildPathSet(curPaths, path, false, false);
			
			for(Iterator pathIter = objChildPaths.iterator(); pathIter.hasNext(); ){
				SortedSet sub = curPaths.tailSet(pathIter.next());
				if(sub.size() != 0)
					iter.remove();
			}
		}
		
		objChildPaths.add(path);
		
		return objChildPaths;
	}

	
	public static boolean containCommonEntries(Set set, Object[] objs){
		for(int i = 0; i < objs.length; i++){
			if(set.contains(objs[i]))
				return true;
		}
		return false;
	}
	
	public static IRealBuildObjectAssociation[] getRealObjects(int type){
		switch (type) {
		case IRealBuildObjectAssociation.OBJECT_TOOL:
			return (IRealBuildObjectAssociation[])ManagedBuildManager.getRealTools();
		case IRealBuildObjectAssociation.OBJECT_TOOLCHAIN:
			return (IRealBuildObjectAssociation[])ManagedBuildManager.getRealToolChains();
		case IRealBuildObjectAssociation.OBJECT_BUILDER:
			return (IRealBuildObjectAssociation[])ManagedBuildManager.getRealBuilders();
		case IRealBuildObjectAssociation.OBJECT_CONFIGURATION:
			return (IRealBuildObjectAssociation[])ManagedBuildManager.getExtensionConfigurations();
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public static IRealBuildObjectAssociation[] getExtensionObjects(int type){
		switch (type) {
		case IRealBuildObjectAssociation.OBJECT_TOOL:
			return (IRealBuildObjectAssociation[])ManagedBuildManager.getExtensionTools();
		case IRealBuildObjectAssociation.OBJECT_TOOLCHAIN:
			return (IRealBuildObjectAssociation[])ManagedBuildManager.getExtensionToolChains();
		case IRealBuildObjectAssociation.OBJECT_BUILDER:
			return (IRealBuildObjectAssociation[])ManagedBuildManager.getExtensionBuilders();
		case IRealBuildObjectAssociation.OBJECT_CONFIGURATION:
			return (IRealBuildObjectAssociation[])ManagedBuildManager.getExtensionConfigurations();
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public static IRealBuildObjectAssociation getObjectById(int type, String id){
		switch (type) {
		case IRealBuildObjectAssociation.OBJECT_TOOL:
			return (IRealBuildObjectAssociation)ManagedBuildManager.getExtensionTool(id);
		case IRealBuildObjectAssociation.OBJECT_TOOLCHAIN:
			return (IRealBuildObjectAssociation)ManagedBuildManager.getExtensionToolChain(id);
		case IRealBuildObjectAssociation.OBJECT_BUILDER:
			return (IRealBuildObjectAssociation)ManagedBuildManager.getExtensionBuilder(id);
		case IRealBuildObjectAssociation.OBJECT_CONFIGURATION:
			return (IRealBuildObjectAssociation)ManagedBuildManager.getExtensionConfiguration(id);
		default:
			throw new IllegalArgumentException();
		}
	}
	
}
