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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ToolChainModificationManager.ConflictMatch;
import org.eclipse.cdt.managedbuilder.tcmodification.IConflict;
import org.eclipse.core.runtime.IPath;

public class ConflictSet {
	public static final IConflict[] EMPTY_CONFLICT_ARRAY = new IConflict[0];
	public static final IBuildObject[] EMPTY_BO_ARRAY = new IBuildObject[0];
	
	private PerTypeMapStorage<IRealBuildObjectAssociation, Conflict> fConflictStorage;
	private List<ConflictMatch> fConflictMatchList;
	private Set<? extends IRealBuildObjectAssociation> fExtConflictSet;
	private IRealBuildObjectAssociation fRealObj;
	
	public ConflictSet(IRealBuildObjectAssociation realObj, List<ConflictMatch> conflictMatchList, Set<? extends IRealBuildObjectAssociation> extConflictSet){
		fConflictMatchList = conflictMatchList;
		fExtConflictSet = extConflictSet;
		fRealObj = realObj; 
	}
	
	private void init(){
		if(fConflictStorage == null){
			fConflictStorage = new PerTypeMapStorage<IRealBuildObjectAssociation, Conflict>();
			if(fConflictMatchList != null && fConflictMatchList.size() != 0){
				int size = fConflictMatchList.size();
				PerTypeMapStorage<IRealBuildObjectAssociation, Set<IPath>> result = new PerTypeMapStorage<IRealBuildObjectAssociation, Set<IPath>>();
				for(int i = 0; i < size; i++){
					ConflictMatch match = fConflictMatchList.get(i);
					int objType = match.fMatchType;
					Map<IRealBuildObjectAssociation, Set<IPath>> cm = match.fRObjToPathMap;
					Map<IRealBuildObjectAssociation, Set<IPath>> cur = result.getMap(objType, true);

					Set<Entry<IRealBuildObjectAssociation, Set<IPath>>> entrySet = cm.entrySet();
					for (Entry<IRealBuildObjectAssociation, Set<IPath>> entry : entrySet) {
						IRealBuildObjectAssociation bo = entry.getKey();
						if(DbgTcmUtil.DEBUG){
							if((bo).getType() != objType)
								DbgTcmUtil.fail();
						}
						
						Set<IPath> set = cur.get(bo);
						if(set == null){
							set = new TreeSet<IPath>(PathComparator.INSTANCE);
							cur.put(bo, set);
						}
						
						set.addAll(entry.getValue());
					}
				}

				int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
				for(int i = 0; i < types.length; i++){
					int type = types[i];
					Map<IRealBuildObjectAssociation, Set<IPath>> map = result.getMap(type, false);
					if(map == null)
						continue;
					
					Set<Entry<IRealBuildObjectAssociation, Set<IPath>>> entrySet = map.entrySet();
					for (Entry<IRealBuildObjectAssociation, Set<IPath>> entry : entrySet) {
						IRealBuildObjectAssociation obj = entry.getKey();
						Set<IPath> set = entry.getValue();
						
						Map<IRealBuildObjectAssociation, Conflict> cMap = fConflictStorage.getMap(type, true);
						cMap.put(obj, new Conflict(IConflict.INCOMPATIBLE, obj, set));
					}
				}

			}
		}
	}
	
	private static class Conflict implements IConflict {
		private IRealBuildObjectAssociation fObj;
		private Set<IPath> fPathSet;
		private int fType;
		
		Conflict(int type, IRealBuildObjectAssociation obj, Set<IPath> paths){
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
			return fPathSet.toArray(new IPath[fPathSet.size()]);
		}
	}
	
	public IConflict[] getConflicts(){
		init();
		int types[] = ObjectTypeBasedStorage.getSupportedObjectTypes();
		List<Conflict> list = new ArrayList<Conflict>();
		for(int i = 0; i < types.length; i++){
			Map<IRealBuildObjectAssociation, Conflict> map = fConflictStorage.getMap(types[i], false);
			if(map == null)
				continue;
			
			getConflicts(map, list);
		}
		
		return conflictArray(list);
	}
	
	private static List<Conflict> getConflicts(Map<IRealBuildObjectAssociation, Conflict> map, List<Conflict> list){
		if(list == null)
			list = new ArrayList<Conflict>();
		
		Collection<Conflict> conflicts = map.values();
		for (Conflict conflict : conflicts) {
			list.add(conflict);
		}
		
		return list;
	}
	
	private static Conflict[] conflictArray(Collection<Conflict> list){
		return list.toArray(new Conflict[list.size()]);
	}

	private static IRealBuildObjectAssociation[] objArray(Collection<IRealBuildObjectAssociation> list){
		return list.toArray(new IRealBuildObjectAssociation[list.size()]);
	}

	public IConflict[] getConflictsWith(int objectType){
		init();
		Map<IRealBuildObjectAssociation, Conflict> map = fConflictStorage.getMap(objectType, false);
		if(map == null)
			return EMPTY_CONFLICT_ARRAY;


		return conflictArray(map.values());
	}
	
	public IBuildObject[] getConflictingObjects(int objectType){
		init();
		Map<IRealBuildObjectAssociation, Conflict> map = fConflictStorage.getMap(objectType, false);
		if(map == null)
			return EMPTY_BO_ARRAY;

		return objArray(map.keySet());
	}
	
	public IConflict getConflictWith(IBuildObject bo){
		init();
		if(!(bo instanceof IRealBuildObjectAssociation))
			return null;
		
		IRealBuildObjectAssociation obj = (IRealBuildObjectAssociation)bo;
		Map<IRealBuildObjectAssociation, Conflict> map = fConflictStorage.getMap(obj.getType(), false);
		if(map == null)
			return null;
		
		return map.get(obj);
	}
}
