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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.core.settings.model.util.ListComparator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ExternalSettingsManager {
	static private ExternalSettingsManager fInstance;
	
	private ExternalSettingsManager(){
	}
	
	public static ExternalSettingsManager getInstance(){
		if(fInstance == null)
			fInstance = new ExternalSettingsManager();
		return fInstance;
	}

/*	private static class EntryComparator extends Comparator {

		public boolean equal(Object o1, Object o2) {
			ICLanguageSettingEntry entry1 = (ICLanguageSettingEntry)o1;
			ICLanguageSettingEntry entry2 = (ICLanguageSettingEntry)o2;
/*			int kind = entry1.getKind();
			
			if(entry2.getKind() != kind)
				return false;
			
			if(!entry1.getName().equals(entry2.getName()))
				return false;
			
			return kind  == ICLanguageSettingEntry.MACRO ?
					entry1.getValue().equals(entry2.getValue()) : true;
*/
/*			return entry1.equalsByContents(entry2);
		}
		
	}
*/	
	public void updateReferenceInfo(CConfigurationDescription des, Map newRefMap) throws CoreException{
		CConfigurationSpecSettings settings = des.getSpecSettings();
		
		if(settings == null)
			return;
		
		Map projRefInfoMap = settings.getProjectRefInfoMap();
		
		Map[] delta = calculateReferenceDelta(newRefMap, new HashMap(projRefInfoMap));
		if(delta != null){
			Map removed = delta[1];
			Map added = delta[0];
			
			if(removed != null){
				List removedInfos = handleProjReferenceAddRemove(des, removed, false);
				for(Iterator iter = removedInfos.iterator(); iter.hasNext();){
					ProjectRefInfo info = (ProjectRefInfo)iter.next();
					projRefInfoMap.remove(info.getProjectName());
				}
			}
			
			if(added != null){
				List addedInfos = handleProjReferenceAddRemove(des, added, true);
				for(Iterator iter = addedInfos.iterator(); iter.hasNext();){
					ProjectRefInfo info = (ProjectRefInfo)iter.next();
					projRefInfoMap.put(info.getProjectName(), info);
				}
			}

			settings.setProjectRefInfoMap(projRefInfoMap);
		}
		
	}
	
	private static void calculateDeltas(ICProjectDescription des, boolean added, Map deltaMap){
		ICConfigurationDescription configs[] = des.getConfigurations();
		for(int i = 0; i < configs.length; i++){
			ExtSettingsDelta deltas[] = getSettingChange(configs[i], null);
			if(deltas != null){
				deltaMap.put(configs[i].getId(), deltas);
				if(configs[i].isActive())
					deltaMap.put("", deltas);
			}
		}
	}

/*	private static ExtSettingsDelta[] calculateDeltas(ICDescriptionDelta projectDelta, String cfgId, ICExternalSetting oldSettings[]){
		ICProjectDescription newDes = (ICProjectDescription)projectDelta.getNewSetting();
//		ICProjectDescription oldDes = (ICProjectDescription)projectDelta.getOldSetting();

		switch(projectDelta.getDeltaKind()){
		case ICDescriptionDelta.ADDED:{
			ICConfigurationDescription newCfg = getConfigurationById(newDes, cfgId);
			if(newCfg == null)
				return null;
			return getSettingChange(newCfg.getExternalSettings(), oldSettings);
		}
		case ICDescriptionDelta.REMOVED:{
//			ICConfigurationDescription oldCfg = getConfigurationById(oldDes, cfgId);
//			if(oldCfg == null)
//				return null;
			return getSettingChange(null, oldSettings);
		}
		case ICDescriptionDelta.CHANGED:{
//			boolean activeCfgChanged = (projectDelta.getChangeFlags() | ICDescriptionDelta.ACTIVE_CFG) != 0;
			ICConfigurationDescription newCfg = getConfigurationById(newDes, cfgId);
//			ICConfigurationDescription oldCfg = getConfigurationById(oldDes, cfgId);;
			return getSettingChange(newCfg.getExternalSettings(), oldSettings);
		}
		}
		return null;
	}
*/
	private ICExternalSetting[][] getNewOldSettings(ICDescriptionDelta projectDelta, String cfgId){
		ICExternalSetting[][] settings = new ICExternalSetting[2][];
		ICProjectDescription newDes = (ICProjectDescription)projectDelta.getNewSetting();
		ICProjectDescription oldDes = (ICProjectDescription)projectDelta.getOldSetting();

		switch(projectDelta.getDeltaKind()){
		case ICDescriptionDelta.ADDED:{
			ICConfigurationDescription newCfg = getConfigurationById(newDes, cfgId);
			if(newCfg != null){
				settings[0] = newCfg.getExternalSettings();
			}
			break;
		}
		case ICDescriptionDelta.REMOVED:{
			ICConfigurationDescription oldCfg = getConfigurationById(oldDes, cfgId);
			if(oldCfg != null){
				settings[1] = oldCfg.getExternalSettings();
			}
			break;
		}
		case ICDescriptionDelta.CHANGED:{
//			boolean activeCfgChanged = (projectDelta.getChangeFlags() | ICDescriptionDelta.ACTIVE_CFG) != 0;
			ICConfigurationDescription newCfg = getConfigurationById(newDes, cfgId);
			ICConfigurationDescription oldCfg = getConfigurationById(oldDes, cfgId);;
			
			settings[0] = newCfg.getExternalSettings();
			settings[1] = oldCfg.getExternalSettings();
			break;
		}
		}
		return settings;
	}

/*	private static ExtSettingsDelta[] calculateDeltas(ICDescriptionDelta projectDelta, String cfgId){
		ICProjectDescription newDes = (ICProjectDescription)projectDelta.getNewSetting();
		ICProjectDescription oldDes = (ICProjectDescription)projectDelta.getOldSetting();

		switch(projectDelta.getDeltaKind()){
		case ICDescriptionDelta.ADDED:{
			ICConfigurationDescription newCfg = getConfigurationById(newDes, cfgId);
			if(newCfg == null)
				return null;
			return getSettingChange(newCfg, null);
		}
		case ICDescriptionDelta.REMOVED:{
			ICConfigurationDescription oldCfg = getConfigurationById(oldDes, cfgId);
			if(oldCfg == null)
				return null;
			return getSettingChange(null, oldCfg);
		}
		case ICDescriptionDelta.CHANGED:{
//			boolean activeCfgChanged = (projectDelta.getChangeFlags() | ICDescriptionDelta.ACTIVE_CFG) != 0;
			ICConfigurationDescription newCfg = getConfigurationById(newDes, cfgId);
			ICConfigurationDescription oldCfg = getConfigurationById(oldDes, cfgId);;
			return getSettingChange(newCfg, oldCfg);
		}
		}
		return null;
	}
*/
	private static ICConfigurationDescription getConfigurationById(ICProjectDescription des, String id){
		return id.length() != 0 ?
				des.getConfigurationById(id) :
				des.getActiveConfiguration();
	}

	public ICProjectDescription updateReferencedSettings(ICProjectDescription des){
		CfgDesIterator cfgIter = new CfgDesIterator(des);
		while(cfgIter.hasNext()){
			CConfigurationSpecSettings specSettings;
			try {
				specSettings = ((IInternalCCfgInfo)cfgIter.next(false)).getSpecSettings();
			} catch (CoreException e) {
				continue;
			}
			Map map = specSettings.getProjectRefInfoMap();
			for(Iterator iter = map.values().iterator(); iter.hasNext();){
				ProjectRefInfo info = (ProjectRefInfo)iter.next();
				ICConfigurationDescription refDes = getConfiguration(info.getProjectName(), info.getCfgId(), false);
				if(refDes != null){
					ICExternalSetting extSettings[] = refDes.getExternalSettings();
					updateReferencedSettings(cfgIter, info, (ICExternalSetting[])extSettings, null);
				}
			}
			
		}
		return cfgIter.getProjectDescription();
	}
	
	private static ICConfigurationDescription getConfiguration(String projName, String cfgId, boolean write){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		CoreModel model = CoreModel.getDefault();

		IProject project = root.getProject(projName);
		if(!project.exists())
			return null;
		
		ICProjectDescription des = model.getProjectDescription(project, write);
		if(des == null)
			return null;
		
		return des.getConfigurationById(cfgId);
}
	
	private static void updateReferencedSettings(ICfgContainer cr, ProjectRefInfo refInfo, ICExternalSetting newSettings[], ExtSettingsDelta[] deltas){
		if(!refInfo.isSynchronized()){
			CExternalSettingProvider provider = refInfo.getProvider();
			ICExternalSetting oldSettings[] = provider.getExternalSettings();
			deltas = getSettingChange(newSettings, oldSettings);
		}
		
		String refProjName = refInfo.getProjectName();
		String refCfgId = refInfo.getCfgId();
//		provider.setExternallSetting((CExternalSetting[])newOldSettings[0]);
		
		refInfo.setSynchronized(true);
		if(deltas != null && deltas.length != 0){
		
			CConfigurationDescription writableCfg = (CConfigurationDescription)cr.getConfguration(true);
			if(writableCfg != null){
				try {
					CConfigurationSpecSettings settings = writableCfg.getSpecSettings();
					
					refInfo = (ProjectRefInfo)settings.getProjectRefInfoMap().get(refProjName);
					if(refInfo != null && refInfo.getCfgId().equals(refCfgId)){
						applyDelta(writableCfg, deltas);
						refInfo.getProvider().setExternallSetting(newSettings);
						refInfo.setSynchronized(true);
					}
				} catch (CoreException e) {
				}
			}
		}

	}
	
/*	private static ICProjectDescription updateReferencedSettings(ICConfigurationDescription des){
		Map map = des.getReferenceInfo();
		ICConfigurationDescription refs[] = getReferencedConfigs(map, false);
		if(refs.length != 0){
			for(int i = 0; i < refs.length; i++){
				ICConfigurationDescription ref = refs[i];
			}
		}
	}
	
	private static ICConfigurationDescription[] getReferencedConfigs(Map map, boolean write){
		if(map.size() != 0){
			List list = new ArrayList(map.size());
			for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				String projName = (String)entry.getKey();
				String cfgId = (String)entry.getValue();
				
				ICConfigurationDescription refCfgDes = getConfiguration(projName, cfgId, write);
				if(refCfgDes == null)
					continue;
				
				list.add(refCfgDes);
			}
			
			if(list.size() != 0){
				return (ICConfigurationDescription[])list.toArray(new ICConfigurationDescription[list.size()]);
			}
		}
		
		return new ICConfigurationDescription[0];
	}
*/	
	private interface ICfgContainer {
		ICConfigurationDescription getConfguration(boolean write);
	}

	private static class CfgDesIterator implements ICfgContainer{
		private ICProjectDescription fProjDes;
		private ICConfigurationDescription fCurrentDes;
		private List fCfgList;
		private Set fProcessedSet = new HashSet();
		private boolean fWriteStatusChanged;
		
		
		public CfgDesIterator(ICProjectDescription des){
			fProjDes = des;
			fCfgList = new ArrayList(Arrays.asList(fProjDes.getConfigurations()));
		}
		
		public boolean isReadOnly(){
			return fProjDes.isReadOnly();
		}
		
		public ICConfigurationDescription next(boolean write){
			if(fCfgList.isEmpty())
				throw new NoSuchElementException();
			
			fCurrentDes = (ICConfigurationDescription)fCfgList.remove(fCfgList.size() - 1);
			fProcessedSet.add(fCurrentDes.getId());
			return current(write);
		}

		public ICConfigurationDescription current(boolean write){
			if(fCurrentDes == null)
				return next(write);
			
			ICConfigurationDescription writableCfg = null;
			if(write && fCurrentDes.isReadOnly()){
				fWriteStatusChanged = true;
				ICProjectDescription wDes = CoreModel.getDefault().getProjectDescription(fProjDes.getProject());
				if(wDes != null){
					writableCfg = wDes.getConfigurationById(fCurrentDes.getId());
					if(writableCfg != null){
						fCurrentDes = writableCfg;
						fProjDes = wDes;
						ICConfigurationDescription dess[] = fProjDes.getConfigurations();
						fCfgList.clear();
						for(int i = 0; i < dess.length; i++){
							if(!fProcessedSet.contains(dess[i].getId())){
								fCfgList.add(dess[i]);
							}
						}
					}
				}
				return writableCfg;
			}
			return fCurrentDes;
		}
		
		public ICProjectDescription getProjectDescription(){
			return fProjDes;
		}

		public boolean hasNext(){
			return !fCfgList.isEmpty();
		}
		
		public boolean isWriteStatusChanged(){
			return fWriteStatusChanged;
		}

		public ICConfigurationDescription getConfguration(boolean write) {
			return current(write);
		}
	}
	
	public void updateDepentents(ICDescriptionDelta projectDelta){
		if(projectDelta == null)
			return;
		
		Map deltaMap = new HashMap(); 
		ICProjectDescription newDes = (ICProjectDescription)projectDelta.getNewSetting();
		ICProjectDescription oldDes = (ICProjectDescription)projectDelta.getOldSetting();

		switch(projectDelta.getDeltaKind()){
		case ICDescriptionDelta.ADDED:
			calculateDeltas(newDes, true, deltaMap);
			break;
		case ICDescriptionDelta.REMOVED:
			calculateDeltas(oldDes, false, deltaMap);
			break;
		case ICDescriptionDelta.CHANGED:
			ICDescriptionDelta[] children = projectDelta.getChildren();
			ICDescriptionDelta child;
			boolean activeCfgChanged = (projectDelta.getChangeFlags() & ICDescriptionDelta.ACTIVE_CFG) != 0;
			for(int i = 0; i < children.length; i++){
				child = children[i];
				ICConfigurationDescription newCfg = (ICConfigurationDescription)child.getNewSetting();
//				ICConfigurationDescription oldCfg = (ICConfigurationDescription)child.getOldSetting();
				ExtSettingsDelta deltas[] = ((CProjectDescriptionDelta)child).getExtSettingsDeltas();//getSettingChange(newCfg, oldCfg);
				if(deltas != null){
					deltaMap.put(newCfg.getId(), deltas);
					if(!activeCfgChanged && newCfg.isActive()){
						deltaMap.put("", deltas);
					}
				}
			}
			
			if(activeCfgChanged){
				ICConfigurationDescription newCfg = newDes.getActiveConfiguration();
				ICConfigurationDescription oldCfg = oldDes.getActiveConfiguration();
				ExtSettingsDelta deltas[] = getSettingChange(newCfg, oldCfg);
				if(deltas != null)
					deltaMap.put("", deltas);
			}
		}
		
		if(deltaMap.size() == 0)
			return;
		
		IProject currentProject = ((ICProjectDescription)projectDelta.getSetting()).getProject();
		String currentProjectName = currentProject.getName();
		IProject projects[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
		for(int i = 0; i < projects.length; i++){
			IProject proj = projects[i];
			if(proj.equals(currentProject))
				continue;
			
			ICProjectDescription projDes = mngr.getProjectDescription(proj, false);
			if(projDes == null)
				continue;
			
			CfgDesIterator cfgIter = new CfgDesIterator(projDes);
			while(cfgIter.hasNext()){
				ICConfigurationDescription cfg = cfgIter.next(false);
				CConfigurationSpecSettings settings = null;
				try {
					settings = ((IInternalCCfgInfo)cfg).getSpecSettings();
				} catch (CoreException e) {
				}
				
				if(settings == null)
					continue;
				
				Map map = settings.getProjectRefInfoMap();
				
				ProjectRefInfo refInfo = (ProjectRefInfo)map.get(currentProjectName);
				if(refInfo == null)
					continue;
				
				String cfgId = refInfo.getCfgId();
				ExtSettingsDelta deltas[] = null;
				ICExternalSetting[][] newOldSettings = getNewOldSettings(projectDelta, cfgId);
				deltas = (ExtSettingsDelta[])deltaMap.get(cfgId);
				
				updateReferencedSettings(cfgIter, refInfo, (CExternalSetting[])newOldSettings[0], deltas);
			}
		
			if(cfgIter.isWriteStatusChanged()){
				try {
					mngr.setProjectDescription(proj, cfgIter.getProjectDescription());
				} catch (CoreException e) {
				}
			}
		}
	}
	
	
	
	static class ExtSettingsDelta {
		ICExternalSetting fSetting;
		boolean fAdded;
		KindBasedStore fEntryChangeStore;

		ExtSettingsDelta(ICExternalSetting setting){
			fSetting = setting;
		}

		ExtSettingsDelta(ICExternalSetting setting, boolean added){
			fSetting = setting;
			fAdded = added;
		}
		
		boolean isChange(){
			return fEntryChangeStore != null;
		}
		
		boolean isAdded(){
			return fAdded;
		}
		
		ICExternalSetting getSetting(){
			return fSetting;
		}
		
		ICLanguageSettingEntry[][] getEntriesDelta(int kind){
			if(fEntryChangeStore != null)
				return (ICLanguageSettingEntry[][])fEntryChangeStore.get(kind);
			ICLanguageSettingEntry [] entries = fSetting.getEntries(kind);
			if(entries == null || entries.length == 0)
				return null;
			
			ICLanguageSettingEntry[][] delta = new ICLanguageSettingEntry[2][];
			if(fAdded)
				delta[0] = entries;
			else 
				delta[1] = entries;
			
			return delta;
		}
		
		ICLanguageSettingEntry[][] getEntriesDelta(){
			int kinds[] = KindBasedStore.getLanguageEntryKinds();
			List added = new ArrayList();
			List removed = new ArrayList();
			for(int i = 0; i < kinds.length; i++){
				ICLanguageSettingEntry[][] d = getEntriesDelta(kinds[i]);
				if(d == null)
					continue;
				
				if(d[0] != null){
					added.addAll(Arrays.asList(d[0]));
				}
				if(d[1] != null){
					removed.addAll(Arrays.asList(d[1]));
				}
			}

			ICLanguageSettingEntry[][] delta = new ICLanguageSettingEntry[2][];
			
			if(added.size() != 0){
				delta[0] = (ICLanguageSettingEntry[])added.toArray(new ICLanguageSettingEntry[added.size()]);
			}
			if(removed.size() != 0){
				delta[1] = (ICLanguageSettingEntry[])removed.toArray(new ICLanguageSettingEntry[removed.size()]);
			}
		
			return delta;
		}
		


	}
	
	 static class ExtSettingMapKey {
		private ICExternalSetting fSetting;
		public ExtSettingMapKey(ICExternalSetting setting){
			fSetting = setting;
		}
		
		public boolean equals(Object obj) {
			if(obj == this)
				return true;

			if(!(obj instanceof ExtSettingMapKey))
				return false;
			
			ExtSettingMapKey other = (ExtSettingMapKey)obj;
			return settingsMatch(fSetting, other.fSetting);
		}
		public int hashCode() {
			return code(fSetting.getCompatibleLanguageIds())
				+ code(fSetting.getCompatibleContentTypeIds())
				+ code(fSetting.getCompatibleExtensions());
		}
		
		private int code(String[] arr){
			if(arr == null || arr.length == 0)
				return 0;
			
			int code = 0;
			
			for(int i = 0; i < arr.length; i++){
				code += arr[i].hashCode();
			}
			return code;
		}
		
		public ICExternalSetting getSetting(){
			return fSetting;
		}
		
	}
/*	
	private static class ExtSettingsComparator extends Comparator{
		private List fDeltaList = null;
		private int fKinds[] = KindBasedStore.getSupportedKinds();
		private Comparator fEntryComparator = new Comparator();

		public boolean equal(Object o1, Object o2) {
			ICExternalSetting setting1 = (ICExternalSetting)o1;
			ICExternalSetting setting2 = (ICExternalSetting)o2;
			if(settingsMatch(setting1, setting2)){
				int kind;
				ExtSettingsDelta extDelta = null;
				for(int i = 0; i < fKinds.length; i++){
					kind = fKinds[i];
					ICLanguageSettingEntry entries1[] = setting1.getEntries(kind);
					ICLanguageSettingEntry entries2[] = setting2.getEntries(kind);
					List delta[] = ListComparator.compare(entries1, entries2, fEntryComparator);
					if(delta == null)
						continue;
					ICLanguageSettingEntry entries[][] = new ICLanguageSettingEntry[2][];
					if(delta[0] != null)
						entries[0] = (ICLanguageSettingEntry[])delta[0].toArray(new ICLanguageSettingEntry[delta[0].size()]);
					if(delta[1] != null)
						entries[1] = (ICLanguageSettingEntry[])delta[1].toArray(new ICLanguageSettingEntry[delta[1].size()]);
					
					if(extDelta == null){
						extDelta = new ExtSettingsDelta(setting1);
						extDelta.fEntryChangeStore = new KindBasedStore();
						if(fDeltaList == null)
							fDeltaList = new ArrayList();
						fDeltaList.add(extDelta);
					}
					
					extDelta.fEntryChangeStore.put(kind, entries);
				}
				
				return true;
			}
			return false;
		}
	}
*/
	private static ExtSettingsDelta createDelta(ICExternalSetting setting1, ICExternalSetting setting2){

		int kind;
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		ExtSettingsDelta extDelta = null;
		for(int i = 0; i < kinds.length; i++){
			kind = kinds[i];
			ICLanguageSettingEntry entries1[] = setting1.getEntries(kind);
			ICLanguageSettingEntry entries2[] = setting2.getEntries(kind);
			List delta[] = ListComparator.compare(entries1, entries2);
			if(delta == null)
				continue;
			ICLanguageSettingEntry entries[][] = new ICLanguageSettingEntry[2][];
			if(delta[0] != null)
				entries[0] = (ICLanguageSettingEntry[])delta[0].toArray(new ICLanguageSettingEntry[delta[0].size()]);
			if(delta[1] != null)
				entries[1] = (ICLanguageSettingEntry[])delta[1].toArray(new ICLanguageSettingEntry[delta[1].size()]);
			
			if(extDelta == null){
				extDelta = new ExtSettingsDelta(setting1);
				extDelta.fEntryChangeStore = new KindBasedStore();
			}
			
			extDelta.fEntryChangeStore.put(kind, entries);
		}
		
		return extDelta;
	
	}
	
	static boolean settingsMatch(ICExternalSetting setting1, ICExternalSetting setting2) {
		if(setting1.equals(setting2))
			return true;
		
		return settingsMatch(setting1, 
				setting2.getCompatibleLanguageIds(),
				setting2.getCompatibleContentTypeIds(),
				setting2.getCompatibleExtensions());
	}

	static boolean settingsMatch(ICExternalSetting setting, 
			String languageIDs[], String contentTypeIDs[], String extensions[]){
		if(!Arrays.equals(setting.getCompatibleLanguageIds(), languageIDs))
			return false;
		if(!Arrays.equals(setting.getCompatibleContentTypeIds(), contentTypeIDs))
			return false;
		if(!Arrays.equals(setting.getCompatibleExtensions(), extensions))
			return false;

		return true;
	}
	
	private static ExtSettingsDelta[] getSettingChange(ICConfigurationDescription newDes, ICConfigurationDescription oldDes){
		return getSettingChange(newDes != null ? newDes.getExternalSettings() : null,
				oldDes != null ? oldDes.getExternalSettings() : null);
	}
	
	private static Map toSettingsKeyMap(ICExternalSetting[] settings){
		Map map = new HashMap();
		for(int i = 0; i < settings.length; i++){
			if(map.put(new ExtSettingMapKey(settings[i]), settings[i]) != null)
				throw new IllegalArgumentException();
		}
		return map;
	}

	private static ExtSettingsDelta[] getSettingChange(ICExternalSetting newSettings[],
			ICExternalSetting oldSettings[]){


		if(newSettings == null || newSettings.length == 0)
			return createDeltas(oldSettings, false);
		if(oldSettings == null || oldSettings.length == 0)
			return createDeltas(newSettings, true);

		List deltaList = new ArrayList();
		
		Map newMap= toSettingsKeyMap(newSettings);
		Map oldMap = toSettingsKeyMap(oldSettings);
		for(Iterator iter = newMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			ICExternalSetting newSetting = (ICExternalSetting)entry.getValue();
			ICExternalSetting oldSetting = (ICExternalSetting)oldMap.remove(entry.getKey());
			if(oldSetting == null){
				deltaList.add(new ExtSettingsDelta(newSetting, true));
			} else {
				ExtSettingsDelta delta = createDelta(newSetting, oldSetting);
				if(delta != null)
					deltaList.add(delta);
			}
		}
		
		for(Iterator iter = oldMap.values().iterator(); iter.hasNext();){
			ICExternalSetting oldSettng = (ICExternalSetting)iter.next();
			deltaList.add(new ExtSettingsDelta(oldSettng, false));
		}
		
		if(deltaList.size() == 0)
			return null;
		return (ExtSettingsDelta[])deltaList.toArray(new ExtSettingsDelta[deltaList.size()]);
	}
	
	private static ExtSettingsDelta[] createDeltas(ICExternalSetting settings[], boolean added){
		if(settings == null || settings.length == 0)
			return null;
		
		ExtSettingsDelta deltas[] = new ExtSettingsDelta[settings.length];
		for(int i = 0; i < settings.length; i++){
			deltas[i] = new ExtSettingsDelta(settings[i], added);
		}
		
		return deltas;
	}

	private static List handleProjReferenceAddRemove(CConfigurationDescription des, Map delta, boolean added){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List list = new ArrayList();
		for(Iterator iter = delta.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			String projName = (String)entry.getKey();
			IProject project = (IProject)root.findMember(projName);
			if(project != null){
				ICExternalSetting settings[];
				Object cfgInfo = entry.getValue();
				ProjectRefInfo info;
				if(cfgInfo instanceof String){
					String cfgId = (String)cfgInfo;
					settings = getExternalSettingsForProjCfg(project, cfgId);
					info = new ProjectRefInfo(projName, cfgId);
					info.getProvider().setExternallSetting(settings);
				} else {
					info = (ProjectRefInfo)cfgInfo;
					settings = (info).getProvider().getExternalSettings();
				}
				list.add(info);
				if(settings.length != 0){
					ExtSettingsDelta deltas[] = new ExtSettingsDelta[settings.length];
					for(int i = 0; i < deltas.length; i++){
						deltas[i] = new ExtSettingsDelta(settings[i], added);
					}
					
					applyDelta(des, deltas);
				}
			}
		}
		return list;
	}
	
	private static void applyDelta(ICConfigurationDescription des, ExtSettingsDelta deltas[]){
		ICResourceDescription rcDess[] = des.getResourceDescriptions();
		for(int i = 0; i < rcDess.length; i++){
			ICResourceDescription rcDes = rcDess[i];
			if(rcDes.getType() == ICSettingBase.SETTING_FOLDER){
				applyDelta((ICFolderDescription)rcDes, deltas);
			} else {
				applyDelta((ICFileDescription)rcDes, deltas);
			}
		}
	}
	
	private static void applyDelta(ICFileDescription des, ExtSettingsDelta deltas[]){
		ICLanguageSetting setting = des.getLanguageSetting();
		if(setting == null)
			return;
		for(int i = 0; i < deltas.length; i++){
			if(isSettingCompatible(setting, deltas[i].fSetting)){
				applyDelta(setting, deltas[i]);
			}
		}
	}

	private static void applyDelta(ICFolderDescription des, ExtSettingsDelta deltas[]){
		ICLanguageSetting settings[] = des.getLanguageSettings();
		if(settings == null || settings.length == 0)
			return;
		
		ICLanguageSetting setting;
		for(int k = 0; k < settings.length; k++){
			setting = settings[k];
			for(int i = 0; i < deltas.length; i++){
				if(isSettingCompatible(setting, deltas[i].fSetting)){
					applyDelta(setting, deltas[i]);
				}
			}
		}
	}

	private static void applyDelta(ICLanguageSetting setting, ExtSettingsDelta delta){
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		int kind;
		ICLanguageSettingEntry entries[];
		ICLanguageSettingEntry diff[][];
		for(int i = 0; i < kinds.length; i++){
			kind = kinds[i];
			diff = delta.getEntriesDelta(kind);
			if(diff == null)
				continue;
			
			entries = setting.getSettingEntries(kind);
			List list = calculateUpdatedEntries(entries, diff[0], diff[1]);
			
			if(list != null)
				setting.setSettingEntries(kind, list);
		}
	}
	
	private static List calculateUpdatedEntries(ICLanguageSettingEntry current[], ICLanguageSettingEntry added[], ICLanguageSettingEntry removed[]){
	//	EntryComparator comparator = new EntryComparator();
		List toAdd = ListComparator.getAdded(added, current);
		List toLeave = ListComparator.getAdded(current, removed);
		
		List result;
		if(toAdd != null){
			result = toAdd;
			if(toLeave != null){
				toAdd.addAll(toLeave);
			}
		} else {
			result = toLeave;
		}
		
		if(result == null)
			result = new ArrayList(0);
		return result;
	}
	
/*	private static ICExternalSetting[] getExternalSettingsForRefMap(Map map){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List list = null;
		for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			String projName = (String)entry.getKey();
			IProject project = (IProject)root.findMember(projName);
			if(project != null){
				ICExternalSetting settings[] = getExternalSettingsForProjCfg(project, (String)entry.getValue());
				if(settings.length != 0){
					if(list == null)
						list = new ArrayList();
					for(int i = 0; i < settings.length; i++){
						list.add(settings[i]);
					}
				}
			}
		}
		
		if(list != null)
			return (ICExternalSetting[])list.toArray(new ICExternalSetting[list.size()]);
		return CConfigurationSpecSettings.EMPTY_EXT_SETTINGS_ARRAY;
	}
*/	
	private static ICExternalSetting[] getExternalSettingsForProjCfg(IProject project, String cfgId){
		ICProjectDescription projDes = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		if(projDes != null){
			ICConfigurationDescription refCfg = cfgId.length() != 0 ?
					projDes.getConfigurationById(cfgId)
					: projDes.getActiveConfiguration();

			if(refCfg != null)
				return refCfg.getExternalSettings();
		}
		
		return CExternalSettingProvider.EMPTY_EXT_SETTINGS_ARRAY;
	}
	
	private static Map[] calculateReferenceDelta(Map newRef, Map oldRef){
		Map delta[] = new Map[2];
		if(newRef != null && newRef.size() != 0){
			if(oldRef != null && oldRef.size() != 0){
				Map added = new HashMap();
				Map removed = new HashMap();
				for(Iterator iter = newRef.entrySet().iterator(); iter.hasNext();){
					Map.Entry entry = (Map.Entry)iter.next();
					String proj = (String)entry.getKey();
					String cfg = (String)entry.getValue();
					iter.remove();
						
					ProjectRefInfo oldRefInfo = (ProjectRefInfo)oldRef.remove(proj);
					if(oldRefInfo != null){
						String oldCfgId = oldRefInfo.getCfgId();
						if(!cfg.equals(oldCfgId)){
							added.put(proj, cfg);
							removed.put(proj, oldRefInfo);
						}
					} else {
						added.put(proj, cfg);
					}
				}
				
				if(oldRef.size() > 0){
					for(Iterator iter = oldRef.entrySet().iterator(); iter.hasNext();){
						Map.Entry entry = (Map.Entry)iter.next();
						String proj = (String)entry.getKey();
						ProjectRefInfo info = (ProjectRefInfo)entry.getValue();
						removed.put(proj, info);
					}
				}
				
				delta[0] = added;
				delta[1] = removed;
				
			} else {
				delta[0] = new HashMap(newRef);
				delta[1] = null;//new HashMap(0);
			}
		} else {
			delta[0] = null;//new HashMap(0);
			delta[1] = oldRef != null ? new HashMap(oldRef) : null;//new HashMap(0);
		}
		
		if(delta[0] != null && delta[0].size() == 0)
			delta[0] = null;
		if(delta[1] != null && delta[1].size() == 0)
			delta[1] = null;
		
		if(delta[0] == null && delta[1] == null)
			delta = null;
		
		return delta;
	}

	private static boolean isSettingCompatible(ICLanguageSetting setting, ICExternalSetting provider){
		String ids[] = provider.getCompatibleLanguageIds();
		String id;
		if(ids != null && ids.length > 0){
			id = setting.getLanguageId();
			if(id != null){
				if(contains(ids, id))
					return true;
				return false;
			}
		}
		
		ids = provider.getCompatibleContentTypeIds();
		if(ids != null && ids.length > 0){
			String[] cTypeIds = setting.getSourceContentTypeIds();
			if(cTypeIds.length != 0){
				for(int i = 0; i < cTypeIds.length; i++){
					id = cTypeIds[i];
					if(contains(ids, id))
						return true;
				}
			}
			return false;
		}
		
		ids = provider.getCompatibleExtensions();
		if(ids != null && ids.length > 0){
			String [] srcIds = setting.getSourceExtensions();
			if(srcIds.length != 0){
				for(int i = 0; i < srcIds.length; i++){
					id = srcIds[i];
					if(contains(ids, id))
						return true;
				}
				return false;
			}
		}
		return true;
	}
	
	private static boolean contains(Object array[], Object value){
		for(int i = 0; i < array.length; i++){
			if(array[i].equals(value))
				return true;
		}
		return false;
	}
	
	private static int calcRefChangeFlags(ICConfigurationDescription newDes, ICConfigurationDescription oldDes){
		Map newMap = newDes != null ? newDes.getReferenceInfo() : null;  
		Map oldMap = oldDes != null ? oldDes.getReferenceInfo() : null;
		
		int flags = 0;
		if(newMap == null || newMap.size() == 0){
			if(oldMap != null && oldMap.size() != 0){
				flags = ICDescriptionDelta.CFG_REF_REMOVED;
			}
		} else {
			if(oldMap == null || oldMap.size() == 0){
				flags = ICDescriptionDelta.CFG_REF_ADDED;
			} else {
				boolean stop = false;
				for(Iterator iter = newMap.entrySet().iterator(); iter.hasNext();){
					Map.Entry newEntry = (Map.Entry)iter.next();
					Object newProj = newEntry.getKey();
					Object newCfg = newEntry.getValue();
					Object oldCfg = oldMap.remove(newProj);
					if(!newCfg.equals(oldCfg)){
						flags |= ICDescriptionDelta.CFG_REF_ADDED;
						if(oldCfg != null){
							flags |= ICDescriptionDelta.CFG_REF_REMOVED;
							stop = true;
						}
						if(stop)
							break;
					}
				}
				
				if(!oldMap.isEmpty())
					flags |= ICDescriptionDelta.CFG_REF_REMOVED;
			}
		}
		
		return flags;
	}
	
	void calculateCfgExtSettingsDelta(CProjectDescriptionDelta delta){
		ICConfigurationDescription newDes = (ICConfigurationDescription)delta.getNewSetting();
		ICConfigurationDescription oldDes = (ICConfigurationDescription)delta.getOldSetting();
		ExtSettingsDelta[] deltas = getSettingChange(newDes, oldDes);
		int flags = 0;
		int addedRemoved = ICDescriptionDelta.EXTERNAL_SETTINGS_ADDED | ICDescriptionDelta.EXTERNAL_SETTINGS_REMOVED; 
		if(deltas != null ){
			for(int i = 0; i < deltas.length; i++){
				ICLanguageSettingEntry[][] d = deltas[i].getEntriesDelta();
				if(d[0] != null)
					flags |= ICDescriptionDelta.EXTERNAL_SETTINGS_ADDED;
				if(d[1] != null)
					flags |= ICDescriptionDelta.EXTERNAL_SETTINGS_REMOVED;
				
				if((flags & (addedRemoved)) == addedRemoved)
					break;
			}
			delta.setExtSettingsDeltas(deltas);
			if(flags != 0)
				delta.addChangeFlags(flags);
		}
		
		int cfgRefFlags = calcRefChangeFlags(newDes, oldDes);
		if(cfgRefFlags != 0)
			delta.addChangeFlags(cfgRefFlags);
	}
}
