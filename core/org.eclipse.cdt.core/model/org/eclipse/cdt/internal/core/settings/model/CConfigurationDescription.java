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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CDataObject;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

public class CConfigurationDescription extends CDataProxyContainer implements ICConfigurationDescription, IProxyFactory, IInternalCCfgInfo {
	private CfgProxyCache fCache;
//	private ProxyProvider fFileProxyProvider;
//	private ProxyProvider fFolderProxyProvider;
//	private ProxyProvider fRcProxyProvider;
	private PathSettingsContainer fPathContainer;
	private ResourceDescriptionHolder fRcHolder;
	private CConfigurationSpecSettings fCfgSpecSettings;
	private CConfigurationDescriptionCache fCfgCache;
	private boolean fIsPreference = false;

	CConfigurationDescription(CConfigurationData data, ICDataProxyContainer cr) throws CoreException {
		this(data, null, cr);
	}

	CConfigurationDescription(CConfigurationData data, String buildSystemId, ICDataProxyContainer cr) throws CoreException {
		super(data, cr, null);
		if(!(cr instanceof ICProjectDescription))
			fIsPreference = true;
		
		if(data instanceof CConfigurationDescriptionCache)
			fCfgCache = (CConfigurationDescriptionCache)data;

		setConfiguration(this);
		if(buildSystemId != null)
			getSpecSettings().setBuildSystemId(buildSystemId);

	}

	/**
	 * creating a new configuration
	 * 
	 * @param id
	 * @param name
	 * @param base
	 * @param projectDes
	 * @throws CoreException
	 */
	CConfigurationDescription(String id, String name, ICConfigurationDescription base, CProjectDescription projectDes) throws CoreException {
		super(null, projectDes, null);

		setConfiguration(this);
		fCfgSpecSettings = new CConfigurationSpecSettings(this, ((CConfigurationDescription)base).getSpecSettings());
		fCfgSpecSettings.setId(id);
		fCfgSpecSettings.setName(name);
		CConfigurationData baseData = ((IInternalCCfgInfo)base).getConfigurationData(false);
		if(baseData instanceof CConfigurationDescriptionCache){
			baseData = ((CConfigurationDescriptionCache)baseData).getConfigurationData();
		}
		setData(CProjectDescriptionManager.getInstance().createData(this, base, baseData, false, null));
	}

	/*
	 * conveter cnfig constructor
	 */
	CConfigurationDescription(String id, String name, ICStorageElement el, CProjectDescription projectDes) throws CoreException {
		super(null, projectDes, null);
		
		setConfiguration(this);
		
		ICStorageElement storage = CProjectDescriptionManager.getInstance().createStorage(projectDes, id);
		fCfgSpecSettings = new CConfigurationSpecSettings(this, storage, el);
		fCfgSpecSettings.setId(id);
		fCfgSpecSettings.setName(name);
		fCfgSpecSettings.setBuildSystemId(CCorePlugin.DEFAULT_PROVIDER_ID);
		CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
		CConfigurationData data = mngr.createDefaultConfigData(projectDes.getProject(), id, name, PathEntryConfigurationDataProvider.getDataFactory());
		setData(data);
		fCfgSpecSettings.reconsileExtensionSettings(false);
	}

	/*
	 * preference config constructor
	 */
	CConfigurationDescription(String id, String name, String bsId, ICStorageElement el, ICDataProxyContainer cr) throws CoreException {
		super(null, cr, null);
		fIsPreference = true;
		
		setConfiguration(this);
		
		fCfgSpecSettings = new CConfigurationSpecSettings(this, el);
		fCfgSpecSettings.setId(id);
		fCfgSpecSettings.setName(name);
		fCfgSpecSettings.setBuildSystemId(bsId);
		setData(CProjectDescriptionManager.getInstance().loadData(this, null));
	}

	void doWritable() throws CoreException{
		if(!containsWritableData()){
			CConfigurationData data = getConfigurationData(false);
			CConfigurationDescriptionCache cache = (CConfigurationDescriptionCache)data;
			data = cache.getConfigurationData();
			setData(CProjectDescriptionManager.getInstance().createData(this, cache, data, true, null));
		}
	}
	
	public CConfigurationDescriptionCache getCache(){
		return fCfgCache;
	}
	
	public String getId() {
		String id = super.getId();
		if(id == null){
			try {
				id = getSpecSettings().getId();
			} catch (CoreException e) {
				//TODO: log
			}
		}
		return id;
	}

	public String getName() {
		String name = super.getName();
		if(name == null){
			try {
				name = getSpecSettings().getName();
			} catch (CoreException e) {
				//TODO: log
			}
		}
		return name;
	}

	public String getDescription() {
		CConfigurationData data = getConfigurationData(false);
		return data.getDescription();
	}
	
	public CConfigurationData getConfigurationData(boolean write){
		CConfigurationData data = (CConfigurationData)getData(write);
		if(data == null)
			throw new IllegalStateException();
		return data;
	}

	public ICProjectDescription getProjectDescription() {
		if(fIsPreference)
			return null;
		return (ICProjectDescription)getParent();
	}

	public ICResourceDescription[] getResourceDescriptions(int kind) {
		return getRcHolder().getResourceDescriptions(kind);
	}

	public ICFolderDescription getRootFolderDescription() {
		return (ICFolderDescription)getRcHolder().getCurrentResourceDescription();
	}

	public boolean isActive() {
		if(fIsPreference)
			return false;
		return getProjectDescription().getActiveConfiguration() == this;
	}
	
	public void setActive() throws WriteAccessException{
		if(fIsPreference)
			return;
		getProjectDescription().setActiveConfiguration(this);
	}
	
	public void removeResourceDescription(ICResourceDescription des)
			throws CoreException {
		CConfigurationData data = getConfigurationData(true);
		IPath path = des.getPath();
		getRcHolder().removeResurceDescription(path);
		data.removeResourceData((CResourceData)((CDataProxy)des).getData(false));
	}

	public final int getType() {
		return ICSettingBase.SETTING_CONFIGURATION;
	}

	public CDataProxy createProxy(CDataObject data) {
		switch(data.getType()){
			case ICSettingBase.SETTING_FOLDER:
				return new CFolderDescription((CFolderData)data, this);
			case ICSettingBase.SETTING_FILE:
				return new CFileDescription((CFileData)data, this);
			case ICSettingBase.SETTING_TARGET_PLATFORM:
				return new CTargetPlatformSetting((CTargetPlatformData)data, this);
			case ICSettingBase.SETTING_BUILD:
				return new CBuildSetting((CBuildData)data, this); 
				
		}
		return null;
	}

	protected IProxyProvider createChildProxyProvider() {
		ICDataScope scope = new ICDataScope(){

			public CDataObject[] getChildren() {
				CConfigurationData data = getConfigurationData(false);
				List list = new ArrayList();
				CResourceData rcDatas[] = data.getResourceDatas();
				for(int i = 0; i < rcDatas.length; i++){
					list.add(rcDatas[i]);
				}
				CTargetPlatformData tpData = data.getTargetPlatformData();
				list.add(tpData);
				CBuildData buildData = data.getBuildData();
				list.add(buildData);
				// TODO add other data types
				return (CDataObject[])list.toArray(new CDataObject[list.size()]);
			}

			public boolean isStatic() {
				return !containsWritableData();
			}
			
		};
		IProxyCache cache = getCfgProxyCache();
	
		return new ProxyProvider(scope, cache, this);
	}
	
	protected ResourceDescriptionHolder createHolder(CFolderDescription des){
		PathSettingsContainer container = des.getPathContainer();
		if(container == null){
			container = getPathContainer().getChildContainer(des.getPath(), true, true);
			des.setPathContainer(container);
		}
		//TODO: check
		return new ProviderBasedRcDesHolder(getChildrenProxyProvider(), container, false);
	}

	protected ResourceDescriptionHolder createHolder(CFileDescription des){
		PathSettingsContainer container = des.getPathContainer();
		if(container == null){
			container = getPathContainer().getChildContainer(des.getPath(), true, true);
			des.setPathContainer(container);
		}
		//TODO: check
		return new ProviderBasedRcDesHolder(getChildrenProxyProvider(), container, false);
	}

	private CfgProxyCache getCfgProxyCache(){
		if(fCache == null)
			fCache = new CfgProxyCache(getPathContainer());
		return fCache;
	}
	
	private PathSettingsContainer getPathContainer(){
		if(fPathContainer == null)
			fPathContainer = PathSettingsContainer.createRootContainer();
		return fPathContainer;
	}
	
	private ResourceDescriptionHolder getRcHolder(){
		if(fRcHolder == null)
			fRcHolder = new ProviderBasedRcDesHolder(getChildrenProxyProvider(),
					getPathContainer(),
					true);
		return fRcHolder;
	}
	

	public ICResourceDescription getResourceDescription(IPath path, boolean exactPath) {
		return getRcHolder().getResourceDescription(path, exactPath);
	}

	public void setDescription(String des) throws WriteAccessException {
		getConfigurationData(true).setDescription(des);
	}

	public ICResourceDescription[] getResourceDescriptions() {
		return getResourceDescriptions(ICSettingBase.SETTING_FILE | ICSettingBase.SETTING_FOLDER);
	}

	public ICStorageElement getStorage(String id, boolean create) throws CoreException {
		return getSpecSettings().getStorage(id, create);
	}
	
	public void removeStorage(String id) throws CoreException {
		getSpecSettings().removeStorage(id);
	}

	public boolean containsStorage(String id) throws CoreException {
		return getSpecSettings().containsStorage(id);
	}

	public CConfigurationSpecSettings getSpecSettings() throws CoreException{
		if(fCfgSpecSettings == null){
			if(fCfgCache != null){
				if(fCfgCache.isInitializing())
					fCfgSpecSettings = fCfgCache.getSpecSettings();
				else {
					fCfgSpecSettings = new CConfigurationSpecSettings(this, fCfgCache.getSpecSettings());
					fCfgCache = null;
				}
			} else {
				fCfgSpecSettings = new CConfigurationSpecSettings(this, (ICStorageElement)null);
			}

//			fCfgSpecSettings = CProjectDescriptionManager.getInstance().createConfigurationSpecSettings(this);
		} else if( fCfgSpecSettings.getConfigurarion() != this){
			if(!fCfgCache.isInitializing()){
				fCfgSpecSettings = new CConfigurationSpecSettings(this, fCfgCache.getSpecSettings());
				fCfgCache = null;
			}
		}
		return fCfgSpecSettings;
	}

	public String getBuildSystemId() {
		try {
			return getSpecSettings().getBuildSystemId();
		} catch (CoreException e) {
		}
		return null;
	}

	public CConfigurationData getConfigurationData() {
		return getConfigurationData(true);
	}

	public void setConfigurationData(String buildSystemId, CConfigurationData data) throws WriteAccessException {
		String oldId = getId();
		setData(data);
		if(!fIsPreference){
			((CProjectDescription)getProjectDescription()).updateMap(this, oldId);
		}
		try {
			CConfigurationSpecSettings settings = getSpecSettings();
			settings.setId(getId());
			settings.setName(getName());
			settings.setBuildSystemId(buildSystemId);
		} catch (CoreException e) {
		}
	}

	public boolean isModified() {
		try {
			CConfigurationSpecSettings settings = getSpecSettings();
			if(settings.isModified())
				return true;
		} catch (CoreException e) {
		}
		return !(getConfigurationData(false) instanceof CConfigurationDescriptionCache);
	}
	
	void removeConfiguration(){
//		CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
//		CConfigurationData data = getConfigurationData(true);//fCfgCache.getBaseData(); 
//
//		try {
//			mngr.removeData(this, data);
//		} catch (CoreException e) {
//		}
		
		((CProjectDescription)getProjectDescription()).configurationRemoved(this);
		remove();
	}

	public ICFileDescription createFileDescription(IPath path, ICResourceDescription base) throws CoreException, WriteAccessException {
		CConfigurationData data = getConfigurationData(true);
		CResourceData baseRcData = (CResourceData)((CDataProxy)base).getData(true);
		CFileData createdData = null;
		if(base instanceof ICFileDescription){
			createdData = data.createFileData(path, (CFileData)baseRcData);
		} else {
			ICFolderDescription baseFo = (ICFolderDescription)base;
			String fileName = path.lastSegment();
			CLanguageSetting baseLang = (CLanguageSetting)baseFo.getLanguageSettingForFile(fileName);
//			if(baseLang == null){
//				ICLanguageSetting allSettings[] = baseFo.getLanguageSettings();
//				if(allSettings.length != 0)
//					baseLang = (CLanguageSetting)allSettings[0];
//			}
			CLanguageData baseLangData = baseLang != null ? (CLanguageData)baseLang.getData(false) : null;
			createdData = data.createFileData(path, (CFolderData)baseRcData, baseLangData);
		}
		 
		
		if(createdData == null)
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CConfigurationDescription.0")); //$NON-NLS-1$
		
		CDataProxy proxy = getChildrenProxyProvider().getProxy(createdData);
		if(!(proxy instanceof ICFileDescription))
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CConfigurationDescription.1") + proxy.getClass().getName()); //$NON-NLS-1$

		return (ICFileDescription)proxy;
	}

	public ICFolderDescription createFolderDescription(IPath path, ICFolderDescription base) throws CoreException, WriteAccessException {
		CConfigurationData data = getConfigurationData(true);
		CFolderData baseRcData = (CFolderData)((CDataProxy)base).getData(true);
		CFolderData createdData = data.createFolderData(path, baseRcData);
		
		if(createdData == null)
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CConfigurationDescription.2")); //$NON-NLS-1$
		
		CDataProxy proxy = getChildrenProxyProvider().getProxy(createdData);
		if(!(proxy instanceof ICFolderDescription))
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CConfigurationDescription.3") + proxy.getClass().getName()); //$NON-NLS-1$

		return (ICFolderDescription)proxy;
	}

	public ICTargetPlatformSetting getTargetPlatformSetting() {
		CConfigurationData data = getConfigurationData(false);
		return (ICTargetPlatformSetting)getChildrenProxyProvider().getProxy(data.getTargetPlatformData());
	}

	public ICFileDescription[] getFileDescriptions() {
		return (ICFileDescription[])getRcHolder().getResourceDescriptions(ICSettingBase.SETTING_FILE);
	}

	public ICFolderDescription[] getFolderDescriptions() {
		return (ICFolderDescription[])getRcHolder().getResourceDescriptions(ICSettingBase.SETTING_FOLDER);
	}

	public ICSourceEntry[] getSourceEntries() {
		CConfigurationData data = getConfigurationData(false);
		IPath[] srcPaths = data.getSourcePaths();
		IProject proj = fIsPreference ? null : getProjectDescription().getProject();
		return getRcHolder().calculateSourceEntriesFromPaths(proj, srcPaths);
	}

	public void setSourceEntries(ICSourceEntry[] entries) throws CoreException {
		ICSourceEntry entry;
		IPath entryPath;
		IPath paths[];
		PathSettingsContainer cr = PathSettingsContainer.createRootContainer();
		cr.setValue(Boolean.TRUE);
		Set srcPathSet = new HashSet();
		IProject project = fIsPreference ? null : getProjectDescription().getProject();
		IPath projPath = project != null ? project.getFullPath() : null;
//		Map exclusionMap = new HashMap();
		
//		HashSet pathSet = new HashSet();
		
		if(entries == null){
			IPath pasePath = projPath != null ? projPath : Path.EMPTY;
			entries = new ICSourceEntry[]{new CSourceEntry(pasePath, null, ICLanguageSettingEntry.RESOLVED | ICLanguageSettingEntry.VALUE_WORKSPACE_PATH)};
		}

		for(int i = 0 ; i < entries.length; i++){
			entry = entries[i];
			entryPath = entry.getFullPath();
			if(projPath != null){
				if(projPath.isPrefixOf(entryPath)){
					entryPath = entryPath.removeFirstSegments(projPath.segmentCount());
				} else {
					continue;
				}
			} 
//			else {
//				if(entryPath.segmentCount() > 0)
//					entryPath = entryPath.removeFirstSegments(1);
//				else
//					continue;
//			}
			if(srcPathSet.add(entryPath)){
	//			exclusionMap.put(entryPath, Boolean.TRUE);
				PathSettingsContainer entryCr = cr.getChildContainer(entryPath, true, true);
				entryCr.setValue(Boolean.TRUE);
	
				
				paths = entry.getExclusionPatterns();
				
				
				for(int j = 0; j < paths.length; j++){
					IPath path = paths[j];
					PathSettingsContainer exclusion = entryCr.getChildContainer(path, true, true);
					if(exclusion.getValue() == null)
						exclusion.setValue(Boolean.FALSE);
	//				if(null == exclusionMap.get(path))
	//					exclusionMap.put(path, Boolean.FALSE);
				}
			}
		}

		CConfigurationData data = getConfigurationData(true);
		data.setSourcePaths((IPath[])srcPathSet.toArray(new IPath[srcPathSet.size()]));
		ICResourceDescription rcDess[] = getResourceDescriptions();
		ICResourceDescription rcDes;
		Set pathSet = new HashSet();
		
		for(int i = 0; i < rcDess.length; i++){
			rcDes = rcDess[i];
			IPath path  = rcDes.getPath();
			pathSet.add(path);
//			Boolean b = (Boolean)exclusionMap.remove(path);
			Boolean b = (Boolean)cr.getChildContainer(path, false, false).getValue();
			assert (b != null);
			if(Boolean.TRUE == b) {
				if(rcDes.isExcluded())
					rcDes.setExcluded(false);
			} else {
				if((rcDes.getType() == ICSettingBase.SETTING_FILE
						|| !((ICFolderDescription)rcDes).isRoot())
						   && !rcDes.isExcluded())
					rcDes.setExcluded(true);
			}
		}
		
		PathSettingsContainer crs[] = cr.getChildren(true);
		for(int i= 0; i < crs.length; i++){
			PathSettingsContainer c = crs[i];
			IPath path = c.getPath();
			if(!pathSet.remove(path)){
				Boolean b = (Boolean)c.getValue();
				assert (b != null);
				ICResourceDescription base = getResourceDescription(path, false);
				if(b == Boolean.TRUE){
					if(base.isExcluded()){
						ICResourceDescription newDes = createResourceDescription(path, base);
						if(newDes == null){
							ICResourceDescription fo = getResourceDescription(path, false);
							if(fo.getType() == ICSettingBase.SETTING_FILE){
								fo = getResourceDescription(path.removeLastSegments(1), false);
							}
							newDes = createFolderDescription(path, (ICFolderDescription)fo);
						}
						newDes.setExcluded(false);
					}
				} else {
					if(!base.isExcluded()){
						ICResourceDescription newDes = createResourceDescription(path, base);
						if(newDes == null){
							ICResourceDescription fo = getResourceDescription(path, false);
							if(fo.getType() == ICSettingBase.SETTING_FILE){
								fo = getResourceDescription(path.removeLastSegments(1), false);
							}
							newDes = createFolderDescription(path, (ICFolderDescription)fo);
						}
						newDes.setExcluded(true);
					}
				}
			}
		}
		
	}
	
	private ICResourceDescription createResourceDescription(IPath path, ICResourceDescription base){
		if(fIsPreference)
			return null;
		IProject project = getProjectDescription().getProject();
		IResource rc = project.findMember(path);
		ICResourceDescription des = null;
		if(rc != null){
			if(rc.getType() == IResource.FILE) {
				try {
					des = createFileDescription(path, base);
				} catch (WriteAccessException e) {
				} catch (CoreException e) {
				}
			} else if (rc.getType() == IResource.FOLDER) {
				try {
					des = createFolderDescription(path, (ICFolderDescription)base);
				} catch (WriteAccessException e) {
				} catch (CoreException e) {
				}
			}
		}
		
		return des;
	}

	public Map getReferenceInfo() {
		try {
			CConfigurationSpecSettings specs = getSpecSettings();
			return specs.getReferenceInfo();
		} catch (CoreException e) {
		}
		return new HashMap(0);
	}

	public void setReferenceInfo(Map refs) {
		try {
			ExternalSettingsManager.getInstance().updateReferenceInfo(this, refs);
		} catch (CoreException e) {
		}
	}

	public ICExternalSetting createExternalSetting(String[] languageIDs,
			String[] contentTypeIDs, String[] extensions,
			ICLanguageSettingEntry[] entries) {
		try {
			return getSpecSettings().createExternalSetting(languageIDs, contentTypeIDs, extensions, entries);
		} catch (CoreException e) {
		}
		return null;
	}

	public ICExternalSetting[] getExternalSettings() {
		try {
			return getSpecSettings().getExternalSettings();
		} catch (CoreException e) {
		}
		return null;
	}

	public void removeExternalSetting(ICExternalSetting setting) {
		try {
			getSpecSettings().removeExternalSetting(setting);
		} catch (CoreException e) {
		}
	}

	public void removeExternalSettings() {
		try {
			getSpecSettings().removeExternalSettings();
		} catch (CoreException e) {
		}
	}

	public ICBuildSetting getBuildSetting() {
		CConfigurationData data = getConfigurationData(false);
		return (ICBuildSetting)getChildrenProxyProvider().getProxy(data.getBuildData());
	}

	public void setSessionProperty(QualifiedName name, Object value){
		try {
			getSpecSettings().setSettionProperty(name, value);
		} catch (CoreException e) {
		}
	}

	public Object getSessionProperty(QualifiedName name) {
		try {
			return getSpecSettings().getSettionProperty(name);
		} catch (CoreException e) {
		}
		return null;
	}

	public ICdtVariablesContributor getBuildVariablesContributor() {
		CConfigurationData data = getConfigurationData(false);
		return data.getBuildVariablesContributor();
	}

	public void setName(String name) {
		if(name.equals(getName()))
			return;
		
		getConfigurationData(true).setName(name);
		try {
			getSpecSettings().setName(name);
		} catch (CoreException e) {
		}
	}

	public ICConfigExtensionReference create(String extensionPoint,
			String extension) throws CoreException {
		return getSpecSettings().create(extensionPoint, extension);
	}

	public ICConfigExtensionReference[] get(String extensionPointID) {
		try {
			return getSpecSettings().get(extensionPointID);
		} catch (CoreException e) {
		}
		return new ICConfigExtensionReference[0];
	}

	public void remove(ICConfigExtensionReference ext) throws CoreException {
		getSpecSettings().remove(ext);
	}

	public void remove(String extensionPoint) throws CoreException {
		getSpecSettings().remove(extensionPoint);
	}

	public boolean isPreferenceConfiguration() {
		return fIsPreference;
	}
	
	protected boolean containsWritableData(){
		if(super.containsWritableData())
			return true;
		
		CConfigurationDescriptionCache data = (CConfigurationDescriptionCache)doGetData();
		return data.isInitializing();
	}

	public ICLanguageSetting getLanguageSettingForFile(IPath path) {
		return CProjectDescriptionManager.getLanguageSettingForFile(this, path);
	}
}
