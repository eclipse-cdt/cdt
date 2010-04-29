/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CDataObject;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.IPathSettingsContainerVisitor;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;


public class CDataDiscoveredInfoCalculator {
	private static final String[] EMPTY_STRING_ARRAY  = new String[0]; 

	private static CDataDiscoveredInfoCalculator fInstance;
	
	public static class DiscoveredSettingInfo{
		private boolean fIsPerFileDiscovery;
		private IRcSettingInfo[] fInfos;
		
		public DiscoveredSettingInfo(boolean isPerFileDiscovery, IRcSettingInfo[] infos){
			fIsPerFileDiscovery = isPerFileDiscovery;
			fInfos = infos;
		}
		
		public boolean isPerFileDiscovery(){
			return fIsPerFileDiscovery;
		}
		
		public IRcSettingInfo[] getRcSettingInfos(){
			return fInfos;
		}
	}

	public interface IRcSettingInfo {
		CResourceData getResourceData();
		
		ILangSettingInfo[] getLangInfos();
	}
	
	public interface ILangSettingInfo {
		CLanguageData getLanguageData();
		
		PathInfo getFilePathInfo();
	}
	
	private static class RcSettingInfo implements IRcSettingInfo{
		private ArrayList<ILangSettingInfo> fLangInfoList;
		private CResourceData fRcData;

		RcSettingInfo(CResourceData rcData){
			fRcData = rcData;
		}

		public ILangSettingInfo[] getLangInfos() {
			if(fLangInfoList != null && fLangInfoList.size() != 0)
				return fLangInfoList.toArray(new ILangSettingInfo[fLangInfoList.size()]);
			return new ILangSettingInfo[0];
		}

		public CResourceData getResourceData() {
			return fRcData;
		}
		
		void add(ILangSettingInfo info){
			if(fLangInfoList == null)
				fLangInfoList = new ArrayList<ILangSettingInfo>();
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
		private List<PathFilePathInfo>[] fStore;
		
		public ListIndexStore(int size){
			if(size < 0)
				size = 0;
			
			@SuppressWarnings("unchecked")
			List<PathFilePathInfo>[] lists = new List[size];
			fStore = lists;
		}
		
		public void add(int index, PathFilePathInfo value){
			List<PathFilePathInfo> list = checkResize(index) ? new ArrayList<PathFilePathInfo>() : fStore[index];
			if(list == null){
				list = new ArrayList<PathFilePathInfo>();
				fStore[index] = list;
			}
			
			if(fMaxIndex < index)
				fMaxIndex = index;
			
			list.add(value);
		}
		
		private boolean checkResize(int index){
			if(index >= fStore.length){
				int newSize = ++index;
				@SuppressWarnings("unchecked")
				List<PathFilePathInfo> resized[] = new List[newSize];
				if(fStore != null && fStore.length != 0){
					System.arraycopy(fStore, 0, resized, 0, fStore.length);
				}
				fStore = resized;
				return true;
			}
			return false;
		}
		
		public List<PathFilePathInfo>[] getLists(){
			int size = fMaxIndex + 1;
			List<List<PathFilePathInfo>> list = new ArrayList<List<PathFilePathInfo>>(size);
			List<PathFilePathInfo> l;
			for(int i = 0; i < size; i++){
				l = fStore[i];
				if(l != null)
					list.add(l);
			}
			
			@SuppressWarnings("unchecked")
			List<PathFilePathInfo>[] lists = list.toArray(new List[list.size()]);
			return lists;
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
		Map<PathInfo, List<PathFilePathInfo>> fPathFilePathInfoMap;
		CLanguageData fBaseLangData;
		boolean fIsDerived;
		private PathInfo fMaxMatchInfo;
		private List<PathFilePathInfo> fMaxMatchInfoList;
		private int fHash;

		public ExtsSetSettings(CLanguageData baseLangData, ExtsSet extsSet, boolean isDerived) {
			fExtsSet = extsSet;
			fBaseLangData = baseLangData;
			fIsDerived = isDerived;
		}
		
		void add(ExtsSetSettings setting){
			if(setting.fPathFilePathInfoMap != null){
				Collection<List<PathFilePathInfo>> infoLists = setting.fPathFilePathInfoMap.values();
				for (List<PathFilePathInfo> infoList : infoLists) {
					for (PathFilePathInfo info : infoList) {
						add(info);
					}
				}
			}
		}
		
		public void add(PathFilePathInfo pInfo){
			if(fPathFilePathInfoMap == null)
				fPathFilePathInfoMap = new HashMap<PathInfo, List<PathFilePathInfo>>(3);
			
			PathInfo fileInfo = pInfo.fInfo;
			List<PathFilePathInfo> list = fileInfo == fMaxMatchInfo ? fMaxMatchInfoList : fPathFilePathInfoMap.get(fileInfo);
			if(list == null){
				list = new ArrayList<PathFilePathInfo>();
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
		private HashSet<String> fExtsSet;
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
				@SuppressWarnings("unchecked")
				HashSet<String> set = (HashSet<String>)calcExtsSet().clone();
				set.removeAll(other.calcExtsSet());
				if(set.size() != 0)
					return false;
			}
			return true;
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
		
		private HashSet<String> calcExtsSet(){
			if(fExtsSet == null)
				fExtsSet = new HashSet<String>(Arrays.asList(fExts));
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
		private HashMap<String, ExtsSetSettings> fExtToExtsSetMap;
		private HashMap<ExtsSet, ExtsSetSettings> fExtsSetToExtsSetSettingsMap;
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
			if(fRcData.getType() == ICSettingBase.SETTING_FILE){
				CLanguageData lData = ((CFileData)fRcData).getLanguageData();
				ExtsSetSettings extSetting = fExtToExtsSetMap.get(getFileExt(fRcData.getPath()));
				if(extSetting != null){
					extSetting.fBaseLangData = lData;
					extSetting.fIsDerived = lData != null ? fIsDerived : false;
				}
				
				if(extSetting != null ? 
						fExtsSetToExtsSetSettingsMap.size() > 1 
						: fExtsSetToExtsSetSettingsMap.size() > 0){
					for (ExtsSetSettings s : fExtsSetToExtsSetSettingsMap.values()) {
						if(s != extSetting){
							s.fBaseLangData = null;
							s.fIsDerived = false;
						}
					}
				}
			} else {
				CLanguageData[] lDatas = ((CFolderData)fRcData).getLanguageDatas();
				@SuppressWarnings("unchecked")
				Map<ExtsSet, ExtsSetSettings> map = (HashMap<ExtsSet, ExtsSetSettings>)fExtsSetToExtsSetSettingsMap.clone();
				
				CLanguageData lData;
				for(int i = 0; i < lDatas.length; i++){
					lData = lDatas[i];
					ExtsSetSettings extSetting = map.remove(new ExtsSet(lData.getSourceExtensions()));
					if(extSetting != null){
						extSetting.fBaseLangData = lData;
						extSetting.fIsDerived = this.fIsDerived;
					}
				}
				
				if(map.size() != 0){
					Collection<ExtsSetSettings> extSettings = map.values();
					for (ExtsSetSettings extSetting : extSettings) {
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
		
		void internalSetSettingsMap(HashMap<ExtsSet, ExtsSetSettings> map){
			fExtsSetToExtsSetSettingsMap = map;
			fExtToExtsSetMap = calcExtToExtSetSettingsMap(map);
		}
		
		void internalAdd(ExtsSetSettings setting){
			if(fExtsSetToExtsSetSettingsMap == null){
				fExtsSetToExtsSetSettingsMap = new HashMap<ExtsSet, ExtsSetSettings>();
			}
			
			ExtsSetSettings cur = fExtsSetToExtsSetSettingsMap.get(setting.fExtsSet);
			if(cur != null){
				cur.add(setting);
			} else {
				fExtsSetToExtsSetSettingsMap.put(setting.fExtsSet, setting);
				fExtToExtsSetMap = addExtsInfoToMap(setting, fExtToExtsSetMap);
			}
		}
	
		void internalAddSettingsMap(HashMap<ExtsSet, ExtsSetSettings> map){
			Collection<ExtsSetSettings> settings = map.values();
			for (ExtsSetSettings setting : settings) {
				internalAdd(setting);
//				ExtsSet extsSet;
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
	
		public RcSetSettings[] getChildren(final boolean includeCurrent){
			final List<Object> list = new ArrayList<Object>();
			fContainer.accept(new IPathSettingsContainerVisitor(){

				public boolean visit(PathSettingsContainer container) {
					if(includeCurrent || container != fContainer){
						list.add(container.getValue());
					}
					return true;
				}
				
			});
			
			return list.toArray(new RcSetSettings[list.size()]);
		}
		
		public boolean containsEqualMaxMatches(RcSetSettings other, boolean ignoreGenerated){
			if(!ignoreGenerated && fExtsSetToExtsSetSettingsMap.size() < other.fExtsSetToExtsSetSettingsMap.size())
				return false;
			
			Set<Entry<ExtsSet, ExtsSetSettings>> entrySet = other.fExtsSetToExtsSetSettingsMap.entrySet();
			for (Entry<ExtsSet, ExtsSetSettings> entry : entrySet) {
				ExtsSetSettings otherSetting = entry.getValue();
				if(ignoreGenerated && otherSetting.fBaseLangData == null)
					continue;
				
				ExtsSetSettings thisSetting = fExtsSetToExtsSetSettingsMap.get(entry.getKey());
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
	
	private static HashMap<String, ExtsSetSettings> calcExtToExtSetSettingsMap(Map<ExtsSet, ExtsSetSettings> extsSetMap){
		HashMap<String, ExtsSetSettings> result = null;
		Collection<ExtsSetSettings> settings = extsSetMap.values();
		for (ExtsSetSettings setting : settings) {
			result = addExtsInfoToMap(setting, result);
		}
		return result;
	}
	
	private static HashMap<String, ExtsSetSettings> addExtsInfoToMap(ExtsSetSettings setting, HashMap<String, ExtsSetSettings> map){
		boolean forceAdd = false;
		String[] exts = setting.fExtsSet.fExts;
		String ext;
		if(map == null){
			map = new HashMap<String, ExtsSetSettings>();
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
		HashMap<ExtsSet, ExtsSetSettings> fileMap;
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
				fileMap = new HashMap<ExtsSet, ExtsSetSettings>(1);
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
		Map<IResource, PathInfo> map = discoveredInfo.getPathInfoMap();
		PathFilePathInfo pInfos[] = createOrderedInfo(map);
		mapDiscoveredInfo(rcSet, pInfos);
		checkRemoveDups(rcSet);
		return rcSet;
	}
	
	/*
	 * utility method for creating empty IRcSettingInfo
	 */
	public static IRcSettingInfo createEmptyRcSettingInfo(CFolderData data){
		return createRcSettingInfo(data, PathInfo.EMPTY_INFO);
	}

	public static IRcSettingInfo createRcSettingInfo(CFolderData data, PathInfo info){
		RcSettingInfo rcInfo = new RcSettingInfo(data);
		CLanguageData[] lDatas = data.getLanguageDatas();
		addLanguageInfos(rcInfo, lDatas, info);
		return rcInfo;
	}

	/*
	 * utility method for creating empty IRcSettingInfo
	 */
	public static IRcSettingInfo createEmptyRcSettingInfo(CFileData data){
		return createRcSettingInfo(data, PathInfo.EMPTY_INFO);
	}

	public static IRcSettingInfo createRcSettingInfo(CFileData data, PathInfo info){
		RcSettingInfo rcInfo = new RcSettingInfo(data);
		CLanguageData lData = data.getLanguageData();
		if(lData != null)
			addLanguageInfos(rcInfo, new CLanguageData[]{lData}, info);
		return rcInfo;
	}

	private static void addLanguageInfos(RcSettingInfo rcInfo, CLanguageData[] lDatas, PathInfo info){
		ArrayList<ILangSettingInfo> list = rcInfo.fLangInfoList;
		if(list == null){
			list = new ArrayList<ILangSettingInfo>(lDatas.length);
			rcInfo.fLangInfoList = list;
		} else {
			list.ensureCapacity(lDatas.length);
		}
		
		for(int i = 0; i < lDatas.length; i++){
			list.add(new LangSettingInfo(lDatas[i], info));
		}
	}
	
	private IRcSettingInfo[] mapFileDiscoveredInfo(IProject project, CConfigurationData data, RcSetSettings rcSet, PathFilePathInfo[] pfpis){
//		IResource rc;
		PathInfo pInfo;
		IPath projRelPath;
		CResourceData rcData;
//		RcSetSettings dataSetting;
		List<RcSettingInfo> list = new ArrayList<RcSettingInfo>(pfpis.length);
		RcSettingInfo rcInfo;
		ILangSettingInfo lInfo;
		CLanguageData lData;
		ArrayList<ILangSettingInfo> tmpList;
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
				Map<String, String> symbolMap = pInfo.getSymbols();
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
						tmpList = new ArrayList<ILangSettingInfo>(lDatas.length - k);
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
							MakeCorePlugin.log(e);
						}
					} else {
						try {
							rcData = createFileData(data, projRelPath, (CFileData)rcData);
						} catch (CoreException e) {
							rcData = null;
							MakeCorePlugin.log(e);
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
						tmpList = new ArrayList<ILangSettingInfo>(1);
						tmpList.add(lInfo);
						rcInfo.fLangInfoList = tmpList;
						list.add(rcInfo);
					}

				}

//				break;
//			}
		}
		return list.toArray(new RcSettingInfo[list.size()]);
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
		List<IRcSettingInfo> resultList = new ArrayList<IRcSettingInfo>();
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
					MakeCorePlugin.log(e);
				}
				if(rcData != null){
					setting.updateRcData(rcData, false);
				} else {
					//TODO:
					continue;
				}
			}
			
			if(rcData.getType() == ICSettingBase.SETTING_FILE){
				ExtsSetSettings extSetting = setting.fExtToExtsSetMap.get(getFileExt(rcData.getPath()));
				if(extSetting != null){
					pathInfo = extSetting.fMaxMatchInfo;
					if(pathInfo != null){
						langInfo = new LangSettingInfo(extSetting.fBaseLangData, pathInfo);
						rcInfo = new RcSettingInfo(rcData);
						rcInfo.fLangInfoList = new ArrayList<ILangSettingInfo> (1);
						rcInfo.fLangInfoList.add(langInfo);
						resultList.add(rcInfo);
					}
				}
			} else {
				if(setting.fExtsSetToExtsSetSettingsMap.size() != 0 ){
					rcInfo = new RcSettingInfo(rcData);
					rcInfo.fLangInfoList = new ArrayList<ILangSettingInfo>(setting.fExtsSetToExtsSetSettingsMap.size());
					resultList.add(rcInfo);

					Collection<ExtsSetSettings> extSettings = setting.fExtsSetToExtsSetSettingsMap.values();
					for (ExtsSetSettings extSetting : extSettings) {
						if(extSetting.fMaxMatchInfo == null)
							continue;
						
						if(extSetting.fBaseLangData == null)
							continue;
						
						if(extSetting.fIsDerived){
							throw new IllegalStateException();
						}
						
						
						rcInfo.add(new LangSettingInfo(extSetting.fBaseLangData, extSetting.fMaxMatchInfo));
						
						if(extSetting.isMultiple()){
							Set<Entry<PathInfo, List<PathFilePathInfo>>> entries = extSetting.fPathFilePathInfoMap.entrySet();
							for (Entry<PathInfo, List<PathFilePathInfo>> entry : entries) {
								if(entry.getKey().equals(extSetting.fMaxMatchInfo))
									continue;
								List<PathFilePathInfo> piList = entry.getValue();
								for (PathFilePathInfo pi : piList) {
									try {
										CFileData fiData = createFileData(data, pi.fPath, (CFolderData)rcData, extSetting.fBaseLangData);
										CLanguageData fiLangData = fiData.getLanguageData();
										if(fiLangData != null){
											RcSettingInfo fiInfo = new RcSettingInfo(fiData);
											fiInfo.add(new LangSettingInfo(fiLangData, pi.fInfo));
											resultList.add(fiInfo);
										}
									} catch (CoreException e) {
										MakeCorePlugin.log(e);
									}
								}
							}
						}
					}
				}
			}
		}
//		}
		
		return resultList.toArray(new RcSettingInfo[resultList.size()]);
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
				extsSet = child.fExtToExtsSetMap.get(ext);
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
		Collection<ExtsSetSettings> settings = rcSet.fExtsSetToExtsSetSettingsMap.values();
		for (ExtsSetSettings setting : settings) {
			setting.add(pfpi);
		}
	}
	
	private static String getFileExt(IPath path){
		String ext = path.getFileExtension();
		if(ext != null)
			return ext;
		return ""; //$NON-NLS-1$
	}
	
	private static HashMap<ExtsSet, ExtsSetSettings> createEmptyExtSetMapCopy(HashMap<ExtsSet, ExtsSetSettings> base){
		@SuppressWarnings("unchecked")
		HashMap<ExtsSet, ExtsSetSettings> map = (HashMap<ExtsSet, ExtsSetSettings>)base.clone();
		Set<Entry<ExtsSet, ExtsSetSettings>> entries = map.entrySet();
		for (Entry<ExtsSet, ExtsSetSettings> entry : entries) {
			ExtsSetSettings extsSet = entry.getValue();
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

	private static HashMap<ExtsSet, ExtsSetSettings> createExtsSetSettingsMap(CFolderData data){
		CLanguageData[] lDatas = data.getLanguageDatas();
		HashMap<ExtsSet, ExtsSetSettings> map = new HashMap<ExtsSet, ExtsSetSettings>(lDatas.length);
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

	private static PathFilePathInfo[] createOrderedInfo(Map<IResource, PathInfo> map){
		ListIndexStore store = new ListIndexStore(10);
		HashMap<PathInfo, PathInfo> infoMap = new HashMap<PathInfo, PathInfo>();
//		LinkedHashMap result;
		
		Set<Entry<IResource, PathInfo>> entries = map.entrySet();
		for (Entry<IResource, PathInfo> entry : entries) {
			IResource rc = entry.getKey();
			IPath path = rc.getProjectRelativePath();
			int segCount = path.segmentCount();
//			if(segCount < 1)
//				continue;

//			path = path.removeFirstSegments(1);
//			segCount--;
			
			PathInfo info = entry.getValue();
			PathInfo storedInfo = infoMap.get(info);
			if(storedInfo == null){
				storedInfo = info;
				infoMap.put(storedInfo, storedInfo);
			}
			
			store.add(segCount, new PathFilePathInfo(path, storedInfo));
		}
		
		List<PathFilePathInfo> lists[] = store.getLists();
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
		List<PathFilePathInfo> list;
		for(int i = 0; i < lists.length; i++){
			list = lists[i];
			listSize = list.size();
			for(int k = 0; k < listSize; k++){
				infos[num++] = list.get(k);
			}
		}
		
		return infos;
	}
	
	public static CDataDiscoveredInfoCalculator getDefault(){
		if(fInstance == null)
			fInstance = new CDataDiscoveredInfoCalculator();
		return fInstance;
	}
	
	public DiscoveredSettingInfo getSettingInfos(IProject project, 
			CConfigurationData cfgData){
		InfoContext context = createContext(project, cfgData);
		try {
			IDiscoveredPathManager.IDiscoveredPathInfo info = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project, context);
			if(info instanceof IDiscoveredPathManager.IPerFileDiscoveredPathInfo2){
				IDiscoveredPathManager.IPerFileDiscoveredPathInfo2 perFileInfo = (IDiscoveredPathManager.IPerFileDiscoveredPathInfo2)info;
				DiscoveredSettingInfo dsInfo = new DiscoveredSettingInfo(true, getSettingInfos(project, cfgData, perFileInfo, true));
				return dsInfo;
			}
			IPath[] includes = info.getIncludePaths();
			Map<String, String> symbols = info.getSymbols();
			
			PathInfo pathInfo = new PathInfo(includes, null, symbols, null, null);
			CFolderData rootData = cfgData.getRootFolderData();
			IRcSettingInfo rcInfo = createRcSettingInfo(rootData, pathInfo);
			return new DiscoveredSettingInfo(false, new IRcSettingInfo[]{rcInfo});
		} catch (CoreException e) {
			MakeCorePlugin.log(e);
		}
		return new DiscoveredSettingInfo(false, new IRcSettingInfo[0]);
	}
	
	protected InfoContext createContext(IProject project, CConfigurationData data){
		return new InfoContext(project, idForData(data));
	}
	
	protected String idForData(CDataObject data){
		return data.getId();
	}
}
