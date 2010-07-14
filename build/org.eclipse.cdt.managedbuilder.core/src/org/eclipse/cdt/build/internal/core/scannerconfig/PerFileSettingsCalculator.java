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
package org.eclipse.cdt.build.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.IPathSettingsContainerVisitor;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class PerFileSettingsCalculator {
	private static final String[] EMPTY_STRING_ARRAY  = new String[0]; 
//	private static class ListIndex {
//	int fIndex;
//	List fList;
//	
//	public ListIndex(int index, List list) {
//		fIndex = index;
//		fList = list;
//	}
//}

	public interface IRcSettingInfo {
		CResourceData getResourceData();
		
		ILangSettingInfo[] getLangInfos();
	}
	
	public interface ILangSettingInfo {
		CLanguageData getLanguageData();
		
		PathInfo getFilePathInfo();
	}

	private static class RcSettingInfo implements IRcSettingInfo{
		private ArrayList fLangInfoList;
		private CResourceData fRcData;

		RcSettingInfo(CResourceData rcData){
			fRcData = rcData;
		}

		public ILangSettingInfo[] getLangInfos() {
			if(fLangInfoList != null && fLangInfoList.size() != 0)
				return (ILangSettingInfo[])fLangInfoList.toArray(new ILangSettingInfo[fLangInfoList.size()]);
			return new ILangSettingInfo[0];
		}

		public CResourceData getResourceData() {
			return fRcData;
		}
		
		void add(ILangSettingInfo info){
			if(fLangInfoList == null)
				fLangInfoList = new ArrayList();
			fLangInfoList.add(info);
		}
	}
	
	private static class LangSettingInfo implements ILangSettingInfo {
		private CLanguageData fLangData;
		private PathInfo fPathInfo;
		
		LangSettingInfo(CLanguageData langData, PathInfo info){
			fLangData = langData;
			fPathInfo = info;
		}

		public PathInfo getFilePathInfo() {
			return fPathInfo;
		}

		public CLanguageData getLanguageData() {
			return fLangData;
		}
		
	}

	private static class ListIndexStore {
		private int fMaxIndex;
		private List[] fStore;
		
		public ListIndexStore(int size){
			if(size < 0)
				size = 0;
			
			fStore = new List[size];
		}
		
		public void add(int index, Object value){
			List list = checkResize(index) ? new ArrayList() : fStore[index];
			if(list == null){
				list = new ArrayList();
				fStore[index] = list;
			}
			
			if(fMaxIndex < index)
				fMaxIndex = index;
			
			list.add(value);
		}
		
		private boolean checkResize(int index){
			if(index >= fStore.length){
				int newSize = ++index;
				List resized[] = new List[newSize];
				if(fStore != null && fStore.length != 0){
					System.arraycopy(fStore, 0, resized, 0, fStore.length);
				}
				fStore = resized;
				return true;
			}
			return false;
		}
		
		public List[] getLists(){
			int size = fMaxIndex + 1;
			List list = new ArrayList(size);
			List l;
			for(int i = 0; i < size; i++){
				l = fStore[i];
				if(l != null)
					list.add(l);
			}
			
			return (List[])list.toArray(new List[list.size()]);
		}
	}
	
	private static class PathFilePathInfo {
		IPath fPath;
		PathInfo fInfo;
		
		PathFilePathInfo(IPath path, PathInfo info){
			fPath = path;
			fInfo = info;
		}
	}
	
	private static class ExtsSetSettings {
//		String[] fExts;
//		HashSet fExtsSet;
		private ExtsSet fExtsSet;
		Map fPathFilePathInfoMap;
		CLanguageData fBaseLangData;
		boolean fIsDerived;
		private PathInfo fMaxMatchInfo;
		private List fMaxMatchInfoList;
		private int fHash;

		public ExtsSetSettings(CLanguageData baseLangData, ExtsSet extsSet, boolean isDerived) {
			fExtsSet = extsSet;
			fBaseLangData = baseLangData;
			fIsDerived = isDerived;
		}
		
		void add(ExtsSetSettings setting){
			if(setting.fPathFilePathInfoMap != null){
				List list;
				int size;
				for(Iterator iter = setting.fPathFilePathInfoMap.values().iterator(); iter.hasNext();){
					list = (List)iter.next();
					size = list.size();
					for(int i = 0; i < size; i++){
						add((PathFilePathInfo)list.get(i));
					}
				}
			}
		}
		
		void updateLangData(CLanguageData lData, boolean isDerived){
			fBaseLangData = lData;
			fIsDerived = lData != null ? isDerived : false;
		}

		public void add(PathFilePathInfo pInfo){
			if(fPathFilePathInfoMap == null)
				fPathFilePathInfoMap = new HashMap(3);
			
			PathInfo fileInfo = pInfo.fInfo;
			List list = fileInfo == fMaxMatchInfo ? fMaxMatchInfoList : (List)fPathFilePathInfoMap.get(fileInfo);
			if(list == null){
				list = new ArrayList();
				fPathFilePathInfoMap.put(fileInfo, list);
				if(fMaxMatchInfo == null){
					fMaxMatchInfo = fileInfo;
					fMaxMatchInfoList = list;
				} 
//				else {
//					fIsMultiple = true;
//				}
			} else if(fMaxMatchInfoList != list){
//				fIsMultiple = true;
				
				if(fMaxMatchInfoList.size() == list.size()){
					fMaxMatchInfoList = list;
					fMaxMatchInfo = fileInfo;
				}
			}
			list.add(pInfo);
		}
		
		public boolean isMultiple(){
			return fPathFilePathInfoMap != null && fPathFilePathInfoMap.size() > 1;
		}
		
		public PathInfo getMaxMatchPathInfo(){
			return fMaxMatchInfo;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			
			if(isMultiple())
				return false;
			
			if(!(obj instanceof ExtsSetSettings))
				return false;
			
			ExtsSetSettings other = (ExtsSetSettings)obj;
			if(other.isMultiple())
				return false;
			
			if(!fExtsSet.equals(other.fExtsSet))
				return false;
			
			if(!CDataUtil.objectsEqual(fMaxMatchInfo, other.fMaxMatchInfo))
				return false;
			
			return true;
		}

		@Override
		public int hashCode() {
			int hash = fHash;
			if(hash == 0){
				if(isMultiple())
					hash = super.hashCode();
				else {
					hash = fExtsSet.hashCode();
					
					if(fMaxMatchInfo != null)
						hash += fMaxMatchInfo.hashCode();	
				}
				
				fHash = hash;
			}
			return hash;
		}
		
	}
	
	private static class ExtsSet {
		private String[] fExts;
		private HashSet fExtsSet;
		private int fHash;

		public ExtsSet(String[] exts){
			fExts = exts == null || exts.length == 0 ? EMPTY_STRING_ARRAY : (String[])exts.clone();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			
			if(!(obj instanceof ExtsSet))
				return false;
			
			ExtsSet other = (ExtsSet)obj;
			if(fExts.length != other.fExts.length)
				return false;
			
			if(fExts.length != 0){
				HashSet set = (HashSet)calcExtsSet().clone();
				set.removeAll(other.calcExtsSet());
				if(set.size() != 0)
					return false;
			}
			return true;
		}
		
		public String[] getExtensions(){
			return (String[])fExts.clone();
		}

		@Override
		public int hashCode() {
			int hash = fHash;
			if(hash == 0){
				hash = 47;
				for(int i = 0; i < fExts.length; i++){
					hash += fExts[i].hashCode();
				}
				fHash = hash;
			}
			return hash;
		}
		
		private HashSet calcExtsSet(){
			if(fExtsSet == null)
				fExtsSet = new HashSet(Arrays.asList(fExts));
			return fExtsSet;
		}

		@Override
		public String toString() {
			if(fExts.length == 0)
				return "<empty>"; //$NON-NLS-1$
			
			StringBuffer buf = new StringBuffer();
			
			for(int i = 0; i < fExts.length; i++){
				if(i != 0)
					buf.append(","); //$NON-NLS-1$
				buf.append(fExts[i]);
			}
			return buf.toString();
		}
	}
	
	private static class RcSetSettings {
		private CResourceData fRcData;
		private HashMap fExtToExtsSetMap;
		private HashMap fExtsSetToExtsSetSettingsMap;
		private PathSettingsContainer fContainer;
		private boolean fIsDerived;

		RcSetSettings(PathSettingsContainer cr, CResourceData rcData, boolean isDerived){
			this.fContainer = cr;
			this.fRcData = rcData;
			this.fIsDerived = isDerived;
			cr.setValue(this);
		}

		public RcSetSettings getChild(IPath path, boolean exactPath){
			PathSettingsContainer cr = fContainer.getChildContainer(path, false, exactPath);
			if(cr != null)
				return (RcSetSettings)cr.getValue();
			return null;
		}

		public RcSetSettings getChild(IPath path){
			PathSettingsContainer cr = fContainer.getChildContainer(path, false, true);
			if(cr != null)
				return (RcSetSettings)cr.getValue();
			return null;
		}

		public CResourceData getResourceData() {
			return fRcData;
		}

		public RcSetSettings createChild(IPath path, CResourceData data, boolean isDerived){
			PathSettingsContainer cr = fContainer.getChildContainer(path, true, true);
			RcSetSettings child = (RcSetSettings)cr.getValue();
			if(child == null){
				child = new RcSetSettings(cr, data, isDerived);
//				cr.setValue(child);
			}
			return child;
		}
		
		void updateRcData(CResourceData data, boolean isDerived){
			fRcData = data;
			fIsDerived = data != null ? isDerived : false;
			updateLangDatas();
		}
		
		private void updateLangDatas(){
			ExtsSetSettings extSetting;
			
			if(fRcData.getType() == ICSettingBase.SETTING_FILE){
				CLanguageData lData = ((CFileData)fRcData).getLanguageData();
				extSetting = (ExtsSetSettings)fExtToExtsSetMap.get(getFileExt(fRcData.getPath()));
				if(extSetting != null){
					extSetting.fBaseLangData = lData;
					extSetting.fIsDerived = lData != null ? fIsDerived : false;
				}
				
				if(extSetting != null ? 
						fExtsSetToExtsSetSettingsMap.size() > 1 
						: fExtsSetToExtsSetSettingsMap.size() > 0){
					ExtsSetSettings s;
					for(Iterator iter = fExtsSetToExtsSetSettingsMap.values().iterator(); iter.hasNext();){
						s = (ExtsSetSettings)iter.next();
						if(s != extSetting){
							s.fBaseLangData = null;
							s.fIsDerived = false;
						}
					}
				}
			} else {
				CLanguageData[] lDatas = ((CFolderData)fRcData).getLanguageDatas();
				Map map = (HashMap)fExtsSetToExtsSetSettingsMap.clone();
				
				CLanguageData lData;
				for(int i = 0; i < lDatas.length; i++){
					lData = lDatas[i];
					extSetting = (ExtsSetSettings)map.remove(new ExtsSet(lData.getSourceExtensions()));
					if(extSetting != null){
						extSetting.fBaseLangData = lData;
						extSetting.fIsDerived = this.fIsDerived;
					}
				}
				
				if(map.size() != 0){
					for(Iterator iter = map.values().iterator(); iter.hasNext();){
						extSetting = (ExtsSetSettings)iter.next();
						extSetting.fBaseLangData = null;
						extSetting.fIsDerived = false;
					}
				}
			}
		}
		
		public IPath getPath(){
			return fContainer.getPath();
		}
		
		public RcSetSettings getParent(){
			PathSettingsContainer cr = fContainer.getParentContainer();
			if(cr != null)
				return (RcSetSettings)cr.getValue();
			return null;
		}
		
		void internalSetSettingsMap(HashMap map){
			fExtsSetToExtsSetSettingsMap = map;
			fExtToExtsSetMap = calcExtToExtSetSettingsMap(map);
		}
		
		void internalAdd(ExtsSetSettings setting){
			if(fExtsSetToExtsSetSettingsMap == null){
				fExtsSetToExtsSetSettingsMap = new HashMap();
			}
			
			ExtsSetSettings cur = (ExtsSetSettings)fExtsSetToExtsSetSettingsMap.get(setting.fExtsSet);
			if(cur != null){
				cur.add(setting);
			} else {
				fExtsSetToExtsSetSettingsMap.put(setting.fExtsSet, setting);
				fExtToExtsSetMap = addExtsInfoToMap(setting, fExtToExtsSetMap);
			}
		}
	
		void internalAddSettingsMap(HashMap map){
			ExtsSetSettings setting;//, thisSetting;
//			ExtsSet extsSet;
			for(Iterator iter = map.values().iterator(); iter.hasNext();){
				setting = (ExtsSetSettings)iter.next();
				internalAdd(setting);
//				extsSet = setting.fExtsSet;
//				thisSetting = (ExtsSetSettings)fExtsSetToExtsSetSettingsMap.get(extsSet);
//				if(thisSetting != null){
//					thisSetting.add(setting);
//				} else {
//					fExtsSetToExtsSetSettingsMap.put(extsSet, setting);
//					fExtToExtsSetMap = addExtsInfoToMap(setting, fExtToExtsSetMap);
//				}
			}
		}
	
		public boolean settingsEqual(RcSetSettings other){
			return fExtsSetToExtsSetSettingsMap.equals(other.fExtsSetToExtsSetSettingsMap);
		}
		
		public RcSetSettings[] getChildren(final boolean includeCurrent){
			final List list = new ArrayList();
			fContainer.accept(new IPathSettingsContainerVisitor(){

				public boolean visit(PathSettingsContainer container) {
					if(includeCurrent || container != fContainer){
						list.add(container.getValue());
					}
					return true;
				}
				
			});
			
			return (RcSetSettings[])list.toArray(new RcSetSettings[list.size()]);
		}
		
		public boolean containsEqualMaxMatches(RcSetSettings other, boolean ignoreGenerated){
			if(!ignoreGenerated && fExtsSetToExtsSetSettingsMap.size() < other.fExtsSetToExtsSetSettingsMap.size())
				return false;
			
			ExtsSetSettings otherSetting, thisSetting;
			Map.Entry entry;
			
			for(Iterator iter = other.fExtsSetToExtsSetSettingsMap.entrySet().iterator(); iter.hasNext();){
				entry = (Map.Entry)iter.next();
				otherSetting = (ExtsSetSettings)entry.getValue();
				if(ignoreGenerated && otherSetting.fBaseLangData == null)
					continue;
				
				thisSetting = (ExtsSetSettings)fExtsSetToExtsSetSettingsMap.get(entry.getKey());
				if(thisSetting == null)
					return false;
				
				if(otherSetting.fMaxMatchInfo != null && !otherSetting.fMaxMatchInfo.equals(thisSetting.fMaxMatchInfo))
					return false;
			}
			
			return true;
		}
		
		void removeChild(RcSetSettings setting){
			IPath path = setting.fContainer.getPath();
			IPath thisPath = fContainer.getPath();
			if(!thisPath.isPrefixOf(path))
				throw new IllegalArgumentException();
			
			path = path.removeFirstSegments(thisPath.segmentCount());
			fContainer.removeChildContainer(path);
		}
	}
	
	private static HashMap calcExtToExtSetSettingsMap(Map extsSetMap){
		HashMap result = null;
		ExtsSetSettings setting;
		for(Iterator iter = extsSetMap.values().iterator(); iter.hasNext();){
			setting = (ExtsSetSettings)iter.next();
			result = addExtsInfoToMap(setting, result);
		}
		return result;
	}
	
	private static HashMap addExtsInfoToMap(ExtsSetSettings setting, HashMap map){
		boolean forceAdd = false;
		String[] exts = setting.fExtsSet.fExts;
		String ext;
		if(map == null){
			map = new HashMap();
			forceAdd = true;
		}

		for(int i = 0; i < exts.length; i++){
			ext = exts[i];
			if(forceAdd || !map.containsKey(ext)){
				map.put(ext, setting);
			}
		}
		return map;
	}
	
	private RcSetSettings createRcSetInfo (CConfigurationData data){
		CFolderData rootData = data.getRootFolderData();
		PathSettingsContainer container = PathSettingsContainer.createRootContainer();
		RcSetSettings rcSet = new RcSetSettings(container, rootData, false);
		rcSet.internalSetSettingsMap(createExtsSetSettingsMap(rootData));
//		rcSet.fExtToExtsSetMap = new HashMap();
//		rcSet.fExtsSetToExtsSetSettingsMap = new HashMap();
		
		CResourceData[] rcDatas = data.getResourceDatas();
		CResourceData rcData;
		RcSetSettings curRcSet;
		HashMap fileMap;
		ExtsSetSettings fileSetting;
		IPath path;
		
		for(int i = 0; i < rcDatas.length; i++){
			rcData = rcDatas[i];
			if(rcData == rootData)
				continue;
			
			if(!includeRcDataInCalculation(data, rcData))
				continue;
			
			path = rcData.getPath();
			curRcSet = rcSet.createChild(path, rcData, false);
			if(rcData.getType() == ICSettingBase.SETTING_FILE){
				fileMap = new HashMap(1);
				fileSetting = createExtsSetSettings(path, (CFileData)rcData);
				fileMap.put(fileSetting.fExtsSet, fileSetting);
				curRcSet.internalSetSettingsMap(fileMap);
			} else {
				curRcSet.internalSetSettingsMap(createExtsSetSettingsMap((CFolderData)rcData));
			}
		}
		
		return rcSet;
	}
	
	protected boolean includeRcDataInCalculation(CConfigurationData cfgData, CResourceData rcData){
		return true;
	}
	
	protected CFileData createFileData(CConfigurationData cfgData, IPath path, CFileData base) throws CoreException{
		return cfgData.createFileData(path, base);
	}

	protected CFileData createFileData(CConfigurationData cfgData, IPath path, CFolderData base, CLanguageData langBase) throws CoreException{
		return cfgData.createFileData(path, base, langBase);
	}

	protected CFolderData createFolderData(CConfigurationData cfgData, IPath path, CFolderData base) throws CoreException{
		return cfgData.createFolderData(path, base);
	}

	private RcSetSettings createRcSetSettings(CConfigurationData data, IDiscoveredPathManager.IPerFileDiscoveredPathInfo2 discoveredInfo){
		RcSetSettings rcSet = createRcSetInfo(data);
		Map map = discoveredInfo.getPathInfoMap();
		PathFilePathInfo pInfos[] = createOrderedInfo(map);
		mapDiscoveredInfo(rcSet, pInfos);
		checkRemoveDups(rcSet);
		return rcSet;
	}
	
	/*
	 * utility method for creating empty IRcSettingInfo
	 */
	public static IRcSettingInfo createEmptyRcSettingInfo(CFolderData data){
		RcSettingInfo rcInfo = new RcSettingInfo(data);
		CLanguageData[] lDatas = data.getLanguageDatas();
		addEmptyLanguageInfos(rcInfo, lDatas);
		return rcInfo;
	}
	
	private static void addEmptyLanguageInfos(RcSettingInfo rcInfo, CLanguageData[] lDatas){
		ArrayList list = rcInfo.fLangInfoList;
		if(list == null){
			list = new ArrayList(lDatas.length);
			rcInfo.fLangInfoList = list;
		} else {
			list.ensureCapacity(lDatas.length);
		}
		
		for(int i = 0; i < lDatas.length; i++){
			list.add(new LangSettingInfo(lDatas[i], PathInfo.EMPTY_INFO));
		}
	}
	
	private IRcSettingInfo[] mapFileDiscoveredInfo(IProject project, CConfigurationData data, RcSetSettings rcSet, PathFilePathInfo[] pfpis){
//		IResource rc;
		PathInfo pInfo;
		IPath projRelPath;
		CResourceData rcData;
//		RcSetSettings dataSetting;
		List list = new ArrayList(pfpis.length);
		RcSettingInfo rcInfo;
		LangSettingInfo lInfo;
		CLanguageData lData;
		ArrayList tmpList;
		PathFilePathInfo pfpi;
		
		for(int i = 0; i < pfpis.length; i++){
			pfpi = pfpis[i];
			projRelPath = pfpi.fPath;
			pInfo = pfpi.fInfo;
			if(pInfo.isEmpty())
				continue;

			if(projRelPath.segmentCount() == 0){
				CFolderData rootData = (CFolderData)rcSet.fRcData;
				CLanguageData lDatas[] = rootData.getLanguageDatas();
				IPath[] incPaths = pInfo.getIncludePaths();
				IPath[] quotedIncPaths = pInfo.getQuoteIncludePaths();
				IPath[] incFiles = pInfo.getIncludeFiles();
				IPath[] macroFiles = pInfo.getMacroFiles();
				Map symbolMap = pInfo.getSymbols();
				int kinds = 0;

				if(incPaths.length != 0 || quotedIncPaths.length != 0)
					kinds |= ICLanguageSettingEntry.INCLUDE_PATH;
				if(incFiles.length != 0)
					kinds |= ICLanguageSettingEntry.INCLUDE_FILE;
				if(macroFiles.length != 0)
					kinds |= ICLanguageSettingEntry.MACRO_FILE;
				if(symbolMap.size() != 0)
					kinds |= ICLanguageSettingEntry.MACRO;
				
				rcInfo = null;
				for(int k = 0; k < lDatas.length; k++){
					lData = lDatas[k];
					if((lData.getSupportedEntryKinds() & kinds) == 0)
						continue;
					
					if(rcInfo == null){
						rcInfo = new RcSettingInfo(rootData);
						tmpList = new ArrayList(lDatas.length - k);
						rcInfo.fLangInfoList = tmpList;
					}
					
					lInfo = new LangSettingInfo(lData, pInfo);
					rcInfo.add(lInfo);
				}

				if(rcInfo != null)
					list.add(rcInfo);

				continue;
			}
//			switch(rc.getType()){
//			case IResource.FILE:
//				projRelPath = rc.getProjectRelativePath();
//				dataSetting = rcSet.getChild(projRelPath, false); 
//				rcData = dataSetting.fRcData;
				rcData = rcSet.getChild(projRelPath, false).fRcData;
				if(!rcData.getPath().equals(projRelPath)){
					if(rcData.getType() == ICSettingBase.SETTING_FOLDER){
						CFolderData foData = (CFolderData)rcData;
						lData = CDataUtil.findLanguagDataForFile(projRelPath.lastSegment(), project, (CFolderData)rcData);
						try {
							rcData = createFileData(data, projRelPath, foData, lData);
						} catch (CoreException e) {
							rcData = null;
							ManagedBuilderCorePlugin.log(e);
						}
					} else {
						try {
							rcData = createFileData(data, projRelPath, (CFileData)rcData);
						} catch (CoreException e) {
							rcData = null;
							ManagedBuilderCorePlugin.log(e);
						} 
					}
//					if(rcData != null)
//						dataSetting = rcSet.createChild(projRelPath, rcData, false);
//					else
//						dataSetting = null;
				}
				
				if(rcData != null){
					if(rcData.getType() == ICSettingBase.SETTING_FILE){
						lData = ((CFileData)rcData).getLanguageData();
					} else {
						lData = CDataUtil.findLanguagDataForFile(projRelPath.lastSegment(), project, (CFolderData)rcData);
						
					}

					if(lData != null){
						rcInfo = new RcSettingInfo(rcData);
						lInfo = new LangSettingInfo(lData, pInfo);
						tmpList = new ArrayList(1);
						tmpList.add(lInfo);
						rcInfo.fLangInfoList = tmpList;
						list.add(rcInfo);
					}

				}

//				break;
//			}
		}
		return (RcSettingInfo[])list.toArray(new RcSettingInfo[list.size()]);
	}
	
	public IRcSettingInfo[] getSettingInfos(IProject project, CConfigurationData data, IDiscoveredPathManager.IPerFileDiscoveredPathInfo2 discoveredInfo, boolean fileDataMode){
		if(fileDataMode){
			RcSetSettings rcSettings = createRcSetInfo(data);
			PathFilePathInfo pInfos[] = createOrderedInfo(discoveredInfo.getPathInfoMap());
			return mapFileDiscoveredInfo(project, data, rcSettings, pInfos);
		}
		RcSetSettings settings = createRcSetSettings(data, discoveredInfo);
		return createInfos(data, settings);
	}
	
	private IRcSettingInfo[] createInfos(CConfigurationData data, RcSetSettings rootSetting){
		RcSetSettings settings[] = rootSetting.getChildren(true);
		RcSetSettings setting;
		CResourceData rcData;
		ExtsSetSettings extSetting;
		List resultList = new ArrayList();
		LangSettingInfo langInfo;
		RcSettingInfo rcInfo;
		PathInfo pathInfo;
		for(int i = 0; i < settings.length; i++){
			setting = settings[i];
			rcData = setting.fRcData;
			if(rcData == null)
				continue;
			if(setting.fIsDerived){
//				rcData = null;
				try {
					rcData = createFolderData(data, rcData, setting);
				} catch (CoreException e) {
					rcData = null;
					ManagedBuilderCorePlugin.log(e);
				}
				if(rcData != null){
					setting.updateRcData(rcData, false);
				} else {
					//TODO:
					continue;
				}
			}
			
			if(rcData.getType() == ICSettingBase.SETTING_FILE){
				extSetting = (ExtsSetSettings)setting.fExtToExtsSetMap.get(getFileExt(rcData.getPath()));
				if(extSetting != null){
					pathInfo = extSetting.fMaxMatchInfo;
					if(pathInfo != null){
						langInfo = new LangSettingInfo(extSetting.fBaseLangData, pathInfo);
						rcInfo = new RcSettingInfo(rcData);
						rcInfo.fLangInfoList = new ArrayList(1);
						rcInfo.fLangInfoList.add(langInfo);
						resultList.add(rcInfo);
					}
				}
			} else {
				if(setting.fExtsSetToExtsSetSettingsMap.size() != 0 ){
					rcInfo = new RcSettingInfo(rcData);
					rcInfo.fLangInfoList = new ArrayList(setting.fExtsSetToExtsSetSettingsMap.size());
					resultList.add(rcInfo);

					for(Iterator iter = setting.fExtsSetToExtsSetSettingsMap.values().iterator(); iter.hasNext();){
						extSetting = (ExtsSetSettings)iter.next();
						if(extSetting.fMaxMatchInfo == null)
							continue;
						
						if(extSetting.fBaseLangData == null)
							continue;
						
						if(extSetting.fIsDerived){
							throw new IllegalStateException();
						}
						
						
						rcInfo.add(new LangSettingInfo(extSetting.fBaseLangData, extSetting.fMaxMatchInfo));
						
						if(extSetting.isMultiple()){
							Map.Entry entry;
							List piList;
							int sz;
							PathFilePathInfo pi;
							CFileData fiData;
							RcSettingInfo fiInfo;
							CLanguageData fiLangData;
							
							for(Iterator pathInfoIter = extSetting.fPathFilePathInfoMap.entrySet().iterator(); pathInfoIter.hasNext();){
								entry = (Map.Entry)pathInfoIter.next();
								if(entry.getKey().equals(extSetting.fMaxMatchInfo))
									continue;
								piList = (List)entry.getValue();
								sz = piList.size();
								
								for(int k = 0; k < sz; k++){
									pi = (PathFilePathInfo)piList.get(k);
									try {
										fiData = createFileData(data, pi.fPath, (CFolderData)rcData, extSetting.fBaseLangData);
										fiLangData = fiData.getLanguageData();
										if(fiLangData != null){
											fiInfo = new RcSettingInfo(fiData);
											fiInfo.add(new LangSettingInfo(fiLangData, pi.fInfo));
											resultList.add(fiInfo);
										}
									} catch (CoreException e) {
										ManagedBuilderCorePlugin.log(e);
									}
								}
							}
						}
					}
				}
			}
		}
//		}
		
		return (RcSettingInfo[])resultList.toArray(new RcSettingInfo[resultList.size()]);
	}
	
	private CFolderData createFolderData(CConfigurationData cfg, CResourceData base, RcSetSettings setting) throws CoreException{
		if(base.getType() == ICSettingBase.SETTING_FOLDER)
			return createFolderData(cfg, setting.getPath(), (CFolderData)base);
		
		//should not be here
		throw new IllegalStateException();
	}
	
	private static void checkRemoveDups(RcSetSettings rcSet){
		RcSetSettings settings[] = rcSet.getChildren(true);
		RcSetSettings setting, parent;
		for(int i = 0; i < settings.length; i++){
			setting = settings[i];
			if(!setting.fIsDerived)
				continue;
			
			parent = setting.getParent();
			if(parent == null)
				continue;
			
			if(parent.containsEqualMaxMatches(setting, true))
				removeChildAddingChildSettings(parent, setting);
		}
	}
	
	private static void removeChildAddingChildSettings(RcSetSettings parent, RcSetSettings child){
		parent.internalAddSettingsMap(child.fExtsSetToExtsSetSettingsMap);
		parent.removeChild(child);
	}
	
	private static void mapDiscoveredInfo(RcSetSettings rcSet, PathFilePathInfo[] pInfos){
		PathFilePathInfo pInfo;
		RcSetSettings child, parent;
		String ext;
		ExtsSetSettings extsSet;
//		boolean isDerived;
		IPath dirPath;
		for(int i = 0; i < pInfos.length; i++){
			pInfo = pInfos[i];
			child = rcSet.getChild(pInfo.fPath);
			if(child == null) {
				dirPath = pInfo.fPath.removeLastSegments(1);
				child = rcSet.getChild(dirPath);
				if(child == null){
					child = rcSet.createChild(dirPath, null, true);
					if(child.fExtToExtsSetMap == null){
						parent = child.getParent();
						child.fRcData = parent.fRcData;
						child.internalSetSettingsMap(createEmptyExtSetMapCopy(parent.fExtsSetToExtsSetSettingsMap));
					}
				}
			} 
			
//			isDerived = child.fIsDerived;

			if(pInfo.fPath.segmentCount() == 0){
				processProjectPaths(child, pInfo);
			} else {
				ext = getFileExt(pInfo.fPath);
				extsSet = (ExtsSetSettings)child.fExtToExtsSetMap.get(ext);
				if(extsSet == null){
					extsSet = new ExtsSetSettings(null, new ExtsSet(new String[]{ext}), false);
					child.internalAdd(extsSet);
//					child.fExtToExtsSetMap.put(ext, extsSet);
				}
				extsSet.add(pInfo);
			}
		}
	}
	
	private static void processProjectPaths(RcSetSettings rcSet, PathFilePathInfo pfpi){
		ExtsSetSettings setting;
		for(Iterator iter = rcSet.fExtsSetToExtsSetSettingsMap.values().iterator(); iter.hasNext();){
			setting = (ExtsSetSettings)iter.next();
			setting.add(pfpi);
		}
	}
	
	private static String getFileExt(IPath path){
		String ext = path.getFileExtension();
		if(ext != null)
			return ext;
		return ""; //$NON-NLS-1$
	}
	
	private static HashMap createEmptyExtSetMapCopy(HashMap base){
		HashMap map = (HashMap)base.clone();
		ExtsSetSettings extsSet;
		for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			extsSet = (ExtsSetSettings)entry.getValue();
			extsSet = new ExtsSetSettings(extsSet.fBaseLangData, extsSet.fExtsSet, true);
			entry.setValue(extsSet);
		}
		return map;
	}
	
	private static ExtsSetSettings createExtsSetSettings(IPath path, CFileData data){
		CLanguageData lData = data.getLanguageData();
		if(lData != null){
			String ext = getFileExt(path);
			return createExtsSetSettings(lData, new String[]{ext});
		}
		return new ExtsSetSettings(null, new ExtsSet(EMPTY_STRING_ARRAY), false);
	}
	
	private static ExtsSetSettings createExtsSetSettings(CLanguageData lData, String exts[]){
		return new ExtsSetSettings(lData, new ExtsSet(exts), false);
	}

	private static HashMap createExtsSetSettingsMap(CFolderData data){
		CLanguageData[] lDatas = data.getLanguageDatas();
		HashMap map = new HashMap(lDatas.length);
		ExtsSetSettings settings;
		
		if(lDatas.length != 0) {
			CLanguageData lData;
			for( int i = 0; i < lDatas.length; i++){
				lData = lDatas[i];
				 settings = createExtsSetSettings(lData, lData.getSourceExtensions());
				 map.put(settings.fExtsSet, settings);
			}
		}
		
		return map;
	}

	private static PathFilePathInfo[] createOrderedInfo(Map map){
		Map.Entry entry;
		IResource rc;
		IPath path;
		PathInfo info, storedInfo;
		ListIndexStore store = new ListIndexStore(10);
		HashMap infoMap = new HashMap();
//		LinkedHashMap result;
		
		for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
			entry = (Map.Entry)iter.next();
			rc = (IResource)entry.getKey();
			path = rc.getProjectRelativePath();
			int segCount = path.segmentCount();
//			if(segCount < 1)
//				continue;

//			path = path.removeFirstSegments(1);
//			segCount--;
			
			info = (PathInfo)entry.getValue();
			storedInfo = (PathInfo)infoMap.get(info);
			if(storedInfo == null){
				storedInfo = info;
				infoMap.put(storedInfo, storedInfo);
			}
			
			store.add(segCount, new PathFilePathInfo(path, storedInfo));
		}
		
		List lists[] = store.getLists();
//		result = new LinkedHashMap(map.size());
//		List l;
//		int lSize;
//		PathFilePathInfo pfpi;
//		for(int i = 0; i < lists.length; i++){
//			l = lists[i];
//			lSize = l.size();
//			if(lSize != 0){
//				for(int k = 0; k < lSize; k++){
//					pfpi = (PathFilePathInfo)l.get(k);
//					result.put(pfpi.fPath, pfpi.fInfo);
//				}
//			}
//		}
		int size = 0;
		PathFilePathInfo infos[];
		for(int i = 0; i < lists.length; i++){
			size += lists[i].size();
		}
		
		infos = new PathFilePathInfo[size];
		int num = 0;
		int listSize;
		List list;
		for(int i = 0; i < lists.length; i++){
			list = lists[i];
			listSize = list.size();
			for(int k = 0; k < listSize; k++){
				infos[num++] = (PathFilePathInfo)list.get(k);
			}
		}
		
		return infos;
	}
}
