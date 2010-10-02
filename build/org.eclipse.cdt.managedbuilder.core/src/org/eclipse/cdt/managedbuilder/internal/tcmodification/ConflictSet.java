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
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ToolChainModificationManager.ConflictMatch;
import org.eclipse.cdt.managedbuilder.tcmodification.IConflict;
import org.eclipse.core.runtime.IPath;

public class ConflictSet {
	public static final IConflict[] EMPTY_CONFLICT_ARRAY = new IConflict[0];
	public static final IBuildObject[] EMPTY_BO_ARRAY = new IBuildObject[0];
	
	private PerTypeMapStorage fConflictStorage;
	private List fConflictMatchList;
	private Set fExtConflictSet;
	private IRealBuildObjectAssociation fRealObj;
	
	public ConflictSet(IRealBuildObjectAssociation realObj, List conflictMatchList, Set extConflictSet){
		fConflictMatchList = conflictMatchList;
		fExtConflictSet = extConflictSet;
		fRealObj = realObj; 
	}
	
	private void init(){
		if(fConflictStorage == null){
			fConflictStorage = new PerTypeMapStorage();
			if(fConflictMatchList != null && fConflictMatchList.size() != 0){
				int size = fConflictMatchList.size();
				PerTypeMapStorage result = new PerTypeMapStorage();
				for(int i = 0; i < size; i++){
					ConflictMatch match = (ConflictMatch)fConflictMatchList.get(i);
					int objType = match.fMatchType;
					Map cm = match.fRObjToPathMap;
					Map cur = result.getMap(objType, true);

					for(Iterator iter = cm.entrySet().iterator(); iter.hasNext(); ){
						Map.Entry entry = (Map.Entry)iter.next();
						Object obj = entry.getKey();
						if(DbgTcmUtil.DEBUG){
							if(((IRealBuildObjectAssociation)obj).getType() != objType)
								DbgTcmUtil.fail();
						}
						
						TreeSet set = (TreeSet)cur.get(obj);
						if(set == null){
							set = new TreeSet(PathComparator.INSTANCE);
							cur.put(obj, set);
						}
						
						set.addAll((Set)entry.getValue());
					}
				}

				int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
				for(int i = 0; i < types.length; i++){
					int type = types[i];
					Map map = result.getMap(type, false);
					if(map == null)
						continue;
					
					for(Iterator iter = map.entrySet().iterator(); iter.hasNext(); ){
						Map.Entry entry = (Map.Entry)iter.next();
						
						IRealBuildObjectAssociation obj = (IRealBuildObjectAssociation)entry.getKey();
						TreeSet set = (TreeSet)entry.getValue();
						
						Map cMap = fConflictStorage.getMap(type, true);
						cMap.put(obj, new Conflict(IConflict.INCOMPATIBLE, obj, set));
					}
				}

			}
		}
	}
	
	private static class Conflict implements IConflict {
		private IRealBuildObjectAssociation fObj;
		private SortedSet fPathSet;
		private int fType;
		
		Conflict(int type, IRealBuildObjectAssociation obj, SortedSet paths){
			fType = type;
			fObj = obj;
			fPathSet = paths;
		}
		
		public IBuildObject getBuildObject() {
			return fObj;
		}

		public int getConflictType() {
			return fType;
		}

		public int getObjectType() {
			return fObj.getType();
		}

		public IPath[] getPaths() {
			return (IPath[])fPathSet.toArray(new IPath[fPathSet.size()]);
		}
	}
	
	public IConflict[] getConflicts(){
		init();
		int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
		List list = new ArrayList();
		for(int i = 0; i < types.length; i++){
			Map map = fConflictStorage.getMap(types[i], false);
			if(map == null)
				continue;
			
			getConflicts(map, list);
		}
		
		return conflictArray(list);
	}
	
	private static List getConflicts(Map map, List list){
		if(list == null)
			list = new ArrayList();
		
		for(Iterator iter = map.values().iterator(); iter.hasNext(); ){
			list.add(iter.next());
		}
		
		return list;
	}
	
	private static Conflict[] conflictArray(Collection list){
		return (Conflict[])list.toArray(new Conflict[list.size()]);
	}

	private static IRealBuildObjectAssociation[] objArray(Collection list){
		return (IRealBuildObjectAssociation[])list.toArray(new IRealBuildObjectAssociation[list.size()]);
	}

	public IConflict[] getConflictsWith(int objectType){
		init();
		Map map = fConflictStorage.getMap(objectType, false);
		if(map == null)
			return EMPTY_CONFLICT_ARRAY;


		return conflictArray(map.values());
	}
	
	public IBuildObject[] getConflictingObjects(int objectType){
		init();
		Map map = fConflictStorage.getMap(objectType, false);
		if(map == null)
			return EMPTY_BO_ARRAY;

		return objArray(map.keySet());
	}
	
	public IConflict getConflictWith(IBuildObject bo){
		init();
		if(!(bo instanceof IRealBuildObjectAssociation))
			return null;
		
		IRealBuildObjectAssociation obj = (IRealBuildObjectAssociation)bo;
		Map map = fConflictStorage.getMap(obj.getType(), false);
		if(map == null)
			return null;
		
		return (IConflict)map.get(obj);
	}
}
