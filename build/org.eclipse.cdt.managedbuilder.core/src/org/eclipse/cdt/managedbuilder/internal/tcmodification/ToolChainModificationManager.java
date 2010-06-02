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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFileInfo;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceConfiguration;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.extension.ObjectSetListBasedDefinition;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.extension.RulesManager;
import org.eclipse.cdt.managedbuilder.tcmodification.IFileInfoModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IFolderInfoModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolChainModificationManager;
import org.eclipse.core.runtime.IPath;

public class ToolChainModificationManager implements
		IToolChainModificationManager {
	private static ToolChainModificationManager fInstance;
	
	private ToolChainModificationManager(){
	}
	
	public static ToolChainModificationManager getInstance(){
		if(fInstance == null)
			fInstance = getInstanceSynch();
		return fInstance;
	}
	
	private static synchronized ToolChainModificationManager getInstanceSynch() {
		if(fInstance == null)
			fInstance = new ToolChainModificationManager();
		return fInstance;
	}
	
	public void start(){
		RulesManager.getInstance().start();
	}
	
	public IFileInfoModification createModification(IFileInfo rcInfo) {
		return new FileInfoModification((ResourceConfiguration)rcInfo);
	}

	public IFolderInfoModification createModification(IFolderInfo rcInfo) {
		FolderInfo foInfo = (FolderInfo)rcInfo;
		if(foInfo.isRoot())
			return new ConfigurationModification(foInfo);
		return new FolderInfoModification(foInfo);
	}

	public IFolderInfoModification createModification(IConfiguration cfg,
			IFolderInfoModification base) throws IllegalArgumentException {
		IResourceInfo baseRcInfo = base.getResourceInfo();
		IPath path = baseRcInfo.getPath();
		IResourceInfo rcInfo = cfg.getResourceInfo(path, true);
		FolderInfo folderInfo;
		if(rcInfo != null){
			if(rcInfo instanceof FolderInfo){
				folderInfo = (FolderInfo)rcInfo;
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			folderInfo = (FolderInfo)cfg.createFolderInfo(path);
		}
		
		return folderInfo.isRoot() ?
				new ConfigurationModification(folderInfo, (ConfigurationModification)base)
				: new FolderInfoModification(folderInfo, (FolderInfoModification)base);
	}

	public IFileInfoModification createModification(IConfiguration cfg,
			IFileInfoModification base) throws IllegalArgumentException {
		IResourceInfo baseRcInfo = base.getResourceInfo();
		IPath path = baseRcInfo.getPath();
		IResourceInfo rcInfo = cfg.getResourceInfo(path, true);
		ResourceConfiguration fileInfo;
		if(rcInfo != null){
			if(rcInfo instanceof ResourceConfiguration){
				fileInfo = (ResourceConfiguration)rcInfo;
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			fileInfo = (ResourceConfiguration)cfg.createFileInfo(path);
		}
		
		return new FileInfoModification(fileInfo, (FileInfoModification)base);
	}

	public static boolean checkFlags(int flags, int value){
		return (flags & value) == value;
	}

	public static int addFlags(int flags, int value){
		return flags |= value;
	}

	public static int clearFlags(int flags, int value){
		return flags &= (~value);
	}
	
	private boolean getMatchingObjects(int type, IObjectSet[] oSets, Set skipSet, IRealBuildObjectAssociation additionalSkip, Set result){
		Set tmp = null;
		boolean added = false;
		for(int i = 0; i < oSets.length; i++){
			IObjectSet os = oSets[i];
			int setType = os.getObjectType();
			if(setType != type)
				continue;

			if(tmp == null)
				tmp = new HashSet();
			else
				tmp.clear();
			
			os.getRealBuildObjects(tmp);
			
			if(skipSet != null)
				tmp.removeAll(skipSet);
			
			if(additionalSkip != null)
				tmp.remove(additionalSkip);
			
			if(result.addAll(tmp)){
				added = true;
			}
		}
		return added;
	}

	public ConflictMatchSet getConflictInfo(int objType, PerTypeMapStorage parent){
		//parent should be passed - it is constant no need to recalculate every time
		//PerTypeMapStorage parent = TcModificationUtil.createParentObjectsRealToolToPathSet(foInfo);
		
		ConflictMatchSet conflicts = getConflictMatches(objType, parent, null);
		
		return conflicts;
	}

	public static class ConflictMatch {
		final int fMatchType;
		final Map fRObjToPathMap;
		final int fConflictType;
		final Set fConflicts;
		
		ConflictMatch(int matchType, Map robjToPathMap,
				int conflictType, Set conflicts){
			fMatchType = matchType;
			fRObjToPathMap = Collections.unmodifiableMap(robjToPathMap);
			fConflictType = conflictType;
			fConflicts = Collections.unmodifiableSet(conflicts);
		}
	}
	
	public static class ConflictMatchSet {
		ConflictMatch[] fConflicts;
		Map fObjToConflictListMap;
		
		ConflictMatchSet(ConflictMatch[] coflicts, Map objToConflictMap){
			fConflicts = coflicts;
			fObjToConflictListMap = objToConflictMap;
		}
		
		
	}
	
	private ConflictMatchSet getConflictMatches(int type, PerTypeMapStorage rtToPath, PerTypeSetStorage skip){
		
		//conversion:
		//1.first filter applicable to not-this
		//2. get variants for applicable ones
		
		//1.first filter applicable to not-this
		List conflictList = new ArrayList();
		Map objToConflictMatchMap = new HashMap();
		
		ObjectSetListBasedDefinition[] defs = RulesManager.getInstance().getRules(ObjectSetListBasedDefinition.CONFLICT);
		for(int i = 0; i < defs.length; i++){
			ObjectSetListBasedDefinition def= defs[i];
			ObjectSetList sl = def.getObjectSetList();
			IObjectSet oss[] = sl.getObjectSets();
			for(int k = 0; k < oss.length; k++){
				IObjectSet os = oss[k];
				int objType = os.getObjectType();
				Map rtToPathMap = rtToPath.getMap(objType, false);
				if(rtToPathMap == null)
					continue;
				
				rtToPathMap = (Map)((HashMap)rtToPathMap).clone();
				Set skipSet = skip != null ? skip.getSet(objType, false) : null;
				Set objSet = rtToPathMap.keySet();

				if(skipSet != null)
					objSet.removeAll(skipSet);

				os.retainMatches(objSet);
				
				if(objSet.size() != 0){
					List remainingList = new ArrayList(Arrays.asList(oss));
					remainingList.remove(os);

					IObjectSet[] remaining = (IObjectSet[])remainingList.toArray(new IObjectSet[remainingList.size()]);
					//get objects matching remaining
					skipSet = skip != null ? skip.getSet(type, false) : null;
					Set matchingObjects = new HashSet();
					getMatchingObjects(type, remaining, skipSet, 
							null,
							matchingObjects);
					if(matchingObjects.size() != 0){
						ConflictMatch conflict = new ConflictMatch(
								objType,
								rtToPathMap,
								type,
								matchingObjects);
						
						for(Iterator iter = matchingObjects.iterator(); iter.hasNext(); ){
							TcModificationUtil.getArrayList(objToConflictMatchMap, iter.next()).add(conflict);
						}
						
						conflictList.add(conflict);
					}
//					break;
				}
			}
		}
		
		return new ConflictMatchSet(
				(ConflictMatch[])conflictList.toArray(new ConflictMatch[conflictList.size()]),
				objToConflictMatchMap);
	}
}
