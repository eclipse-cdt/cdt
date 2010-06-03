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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDataFactory;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultFileData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultFolderData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultLanguageData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.EntryStore;
import org.eclipse.cdt.core.settings.model.util.PathEntryResolveInfo;
import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator;
import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator.ReferenceSettingsInfo;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.PathEntryManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class PathEntryConfigurationDataProvider extends
		CConfigurationDataProvider {
	private static PathEntryDataFactory fFactory;
	
	
	public static CDataFactory getDataFactory(){
		if(fFactory == null){
			fFactory = new PathEntryDataFactory();
		}
		return fFactory;
	}

	public PathEntryConfigurationDataProvider(){
		getDataFactory();
	}

	private static class PathEntryFolderData extends CDefaultFolderData {
		private EntryStore fStore;
		
//		public PathEntryFolderData(CConfigurationData cfg, CDataFacroty factory) {
//			super(cfg, factory);
//		}

//		public PathEntryFolderData(String id, IPath path,
//				CConfigurationData cfg, CDataFacroty factory) {
//			super(id, path, cfg, factory);
//		}

		public PathEntryFolderData(String id, IPath path, PathEntryFolderData base,
				CConfigurationData cfg, CDataFactory factory, boolean clone) {
			super(id, path, cfg, factory);
			
			if(base != null)
				fStore = new EntryStore(base.fStore, true);
			else
				fStore = new EntryStore(true);
			
			copyDataFrom(base, clone);
		}
	}

	private static class PathEntryFileData extends CDefaultFileData {
		private EntryStore fStore;

//		public PathEntryFileData(CConfigurationData cfg, CDataFacroty factory) {
//			super(cfg, factory);
//		}

//		public PathEntryFileData(String id, IPath path, CConfigurationData cfg,
//				CDataFacroty factory) {
//			super(id, path, cfg, factory);
//		}

		public PathEntryFileData(String id, IPath path, PathEntryFileData base,
				CConfigurationData cfg, CDataFactory factory, boolean clone) {
			super(id, path, cfg, factory);

			fStore = new EntryStore(base.fStore, true);
			
			copyDataFrom(base, clone);
		}

		public PathEntryFileData(String id, IPath path, PathEntryFolderData base,
				CLanguageData baseLangData, CConfigurationData cfg,
				CDataFactory factory) {
			super(id, path, cfg, factory);

			fStore = new EntryStore(base.fStore, true);
			
			copyDataFrom(base, baseLangData);
		}
		
	}
	
	private static class PathEntryLanguageData extends CDefaultLanguageData {

		public PathEntryLanguageData(String id, CLanguageData base, EntryStore store) {
			fId = id;
			fStore = store;
			copySettingsFrom(base);
		}

		public PathEntryLanguageData(String id, String name, String languageId, int kinds, 
				String[] ids, boolean isContentTypes, EntryStore store) {
			super(id, languageId, ids, isContentTypes);
			fStore = store;
			fName = name;
			fSupportedKinds = kinds;
		}

		@Override
		protected EntryStore createStore() {
			return fStore;
		}

		@Override
		protected EntryStore createStore(CLanguageData data) {
			return fStore;
		}
		
	}
	
	private static class PathEntryDataFactory extends CDataFactory {

		@Override
		public CConfigurationData createConfigurationdata(String id,
				String name, CConfigurationData base, boolean clone) {
			if(clone){
				id = base.getId();
			} else if(id == null){
				id = CDataUtil.genId(null);
			}
			
			return new CfgData(id, name, base, clone);
		}

		@Override
		public CFileData createFileData(CConfigurationData cfg,
				CResourceData base, CLanguageData base2, 
				String id, boolean clone,
				IPath path) {
			if(id == null)
				id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
			if(base.getType() == ICSettingBase.SETTING_FILE)
				return new PathEntryFileData(id, path, (PathEntryFileData)base, cfg, this, clone);
			return new PathEntryFileData(id, path, (PathEntryFolderData)base,
					base2, cfg,	this);
		}

		@Override
		public CFolderData createFolderData(CConfigurationData cfg,
				CFolderData base, String id, boolean clone, IPath path) {
			if(id == null)
				id = clone ? base.getId() : CDataUtil.genId(cfg.getId());
			return new PathEntryFolderData(id, path, (PathEntryFolderData)base, cfg, this, clone);
		}

		@Override
		public CLanguageData createLanguageData(CConfigurationData cfg,
				CResourceData rcBase, CLanguageData base, String id, boolean clone) {
			if(id == null)
				id = clone ? base.getId() : CDataUtil.genId(rcBase.getId());
			EntryStore store;
			if(rcBase.getType() == ICSettingBase.SETTING_FOLDER)
				store = ((PathEntryFolderData)rcBase).fStore;
			else
				store = ((PathEntryFileData)rcBase).fStore;
			return new PathEntryLanguageData(id, base, store);
		}

		@Override
		public CLanguageData createLanguageData(CConfigurationData cfg,
				CResourceData rcBase, String id, String name, String languageId, int supportedEntryKinds, String[] rcTypes,
				boolean isContentTypes) {
			if(id == null)
				id = CDataUtil.genId(rcBase.getId());
			EntryStore store;
			if(rcBase.getType() == ICSettingBase.SETTING_FOLDER)
				store = ((PathEntryFolderData)rcBase).fStore;
			else
				store = ((PathEntryFileData)rcBase).fStore;
			return new PathEntryLanguageData(id, name, languageId, supportedEntryKinds, rcTypes, isContentTypes, store);
		}
		
	}

	private static class CfgData extends CDefaultConfigurationData {
//		private PathEntryResolveInfo fResolveInfo;

		public CfgData(String id, String name, CConfigurationData base, boolean clone) {
			super(id, name, base, fFactory, clone);
		}

		public CfgData(String id, String name) {
			super(id, name, fFactory);
		}
		
//		public PathEntryResolveInfo getResolveInfo(){
//			return fResolveInfo;
//		}
//		
//		public void setResolveInfo(PathEntryResolveInfo info){
//			fResolveInfo = info;
//		}
	}
	
	public static boolean isPathEntryData(CConfigurationData data){
		return data instanceof CfgData;
	}
	
	@Override
	public CConfigurationData applyConfiguration(
			ICConfigurationDescription des,
			ICConfigurationDescription baseDescription,
			CConfigurationData base,
			IProgressMonitor monitor)
			throws CoreException {
		//TODO: check external/reference info here as well.
		if(!fFactory.isModified(base)){
			return createData(des, base, false, true);
		}
		
		
		IProject project = des.getProjectDescription().getProject();
//		ReferenceSettingsInfo refInfo = new ReferenceSettingsInfo(des);
		IPathEntry entries[] = PathEntryTranslator.getPathEntries(project, baseDescription, PathEntryTranslator.INCLUDE_USER);
		CModelManager manager = CModelManager.getDefault();
		ICProject cproject = manager.create(project);
		IPathEntry[] curRawEntries = PathEntryManager.getDefault().getRawPathEntries(cproject);
		
		List<IPathEntry> list = new ArrayList<IPathEntry>();
		list.addAll(Arrays.asList(entries));
		for(int i = 0; i < curRawEntries.length; i++){
			if(curRawEntries[i].getEntryKind() == IPathEntry.CDT_CONTAINER){
				list.add(curRawEntries[i]);
			}
		}
		
		IPathEntry[] newEntries = list.toArray(new IPathEntry[list.size()]);
		PathEntryManager.getDefault().setRawPathEntries(cproject, newEntries, new NullProgressMonitor());
		return createData(des, base, false, false);
	}
	
	private CConfigurationData createData(ICConfigurationDescription des, CConfigurationData fallbackData, boolean modifiedFlag, boolean useCache) throws CoreException {
		CConfigurationData dataToReturn;
		try {
			dataToReturn = createData(des, useCache);
		} catch (Exception e){
			if(fallbackData != null)
				dataToReturn = fallbackData;
			else if (e instanceof CoreException)
				throw (CoreException)e;
			else
				throw ExceptionFactory.createCoreException(e);
		}
		fFactory.setModified(dataToReturn, modifiedFlag);
		return dataToReturn;
		
	}

	@Override
	public CConfigurationData createConfiguration(
			ICConfigurationDescription des, 
			ICConfigurationDescription baseDescription,
			CConfigurationData base,
			boolean clone,
			IProgressMonitor monitor) throws CoreException {
		CfgData copy = new CfgData(des.getId(), des.getName(), base, clone);
		copy.setModified(false);
		return copy;
	}
	
	private CfgData createData(ICConfigurationDescription des, boolean useCache) throws CoreException{
		IProject project = des.getProjectDescription().getProject();
		CModelManager manager = CModelManager.getDefault();
		ICProject cproject = manager.create(project);
		PathEntryResolveInfo rInfo = PathEntryManager.getDefault().getResolveInfo(cproject, useCache);
		
		CfgData data = new CfgData(des.getId(), des.getName());
		data.initEmptyData();
		CDataUtil.adjustConfig(data, getDataFactory());
		//CProjectDescriptionManager.getInstance().adjustDefaultConfig(data);
	
//		data.setResolveInfo(rInfo);
		PathEntryTranslator tr = new PathEntryTranslator(project, data);
		ReferenceSettingsInfo refInfo = tr.applyPathEntries(rInfo, PathEntryTranslator.OP_REPLACE);
		ICExternalSetting extSettings[] = refInfo.getExternalSettings();
		des.removeExternalSettings();
		if(extSettings.length != 0){
			ICExternalSetting setting;
			for(int i = 0; i < extSettings.length; i++){
				setting = extSettings[i];
				des.createExternalSetting(setting.getCompatibleLanguageIds(), 
						setting.getCompatibleContentTypeIds(), 
						setting.getCompatibleExtensions(), 
						setting.getEntries());
			}
		}

//		IPath projPaths[] = refInfo.getReferencedProjectsPaths();
//		if(projPaths.length != 0){
//			Map map = new HashMap(projPaths.length);
//			for(int i = 0; i < projPaths.length; i++){
//				map.put(projPaths[i].segment(0), "");	//$NON-NLS-1$
//			}
//			des.setReferenceInfo(map);
//		}

		cproject.close();
		
		String[] ids = getIds(des.get(CCorePlugin.BINARY_PARSER_UNIQ_ID));
		data.getTargetPlatformData().setBinaryParserIds(ids);
		
		ids = getIds(des.get(CCorePlugin.ERROR_PARSER_UNIQ_ID));
		data.getBuildData().setErrorParserIDs(ids);
		
		data.setModified(false);
		return data;
	}
	
	private String[] getIds(ICConfigExtensionReference refs[]){
		if(refs == null || refs.length == 0)
			return new String[0];
		
		String[] ids = new String[refs.length];
		for(int i = 0; i < refs.length; i++){
			ids[i] = refs[i].getID();
		}
		return ids;
	}

	@Override
	public CConfigurationData loadConfiguration(ICConfigurationDescription des,
			IProgressMonitor monitor)
			throws CoreException {
		return createData(des, null, false, true);
	}

	@Override
	public void removeConfiguration(ICConfigurationDescription des,
			CConfigurationData data,
			IProgressMonitor monitor) {
		//do nothing for now
	}

	@Override
	public void dataCached(ICConfigurationDescription cfgDes,
			CConfigurationData data, IProgressMonitor monitor) {
		fFactory.setModified(data, false);
	}

}
