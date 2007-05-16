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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CSettingEntryFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class CProjectDescription implements ICProjectDescription, ICDataProxyContainer {
	private static final String ACTIVE_CFG = "activeConfiguration"; //$NON-NLS-1$
	private static final QualifiedName ACTIVE_CFG_PROPERTY = new QualifiedName(CCorePlugin.PLUGIN_ID, ACTIVE_CFG);
	private static final String SETTING_CFG = "settingConfiguration"; //$NON-NLS-1$
	private static final QualifiedName SETTING_CFG_PROPERTY = new QualifiedName(CCorePlugin.PLUGIN_ID, SETTING_CFG);
	
	private CfgIdPair fActiveCfgInfo;
	private CfgIdPair fSettingCfgInfo;
	private CProjectDescriptionPreferences fPrefs;
//	private ICConfigurationDescription fActiveCfg;
//	private String fActiveCfgId;
//	private ICConfigurationDescription fIndexCfg;
//	private String fIndexCfgId;
	private IProject fProject;
	private ICSettingsStorage fStorage;
	private ICStorageElement fRootStorageElement;
	private LinkedHashMap fCfgMap = new LinkedHashMap();
	private boolean fIsReadOnly;
	private boolean fIsModified;
	private HashMap fPropertiesMap;
//	private boolean fNeedsActiveCfgIdPersistence;
	private boolean fIsLoadding;
	private boolean fIsApplying;

	private class CfgIdPair {
		private String fId;
		private ICConfigurationDescription fCfg;
		private QualifiedName fPersistanceName;
		private boolean fNeedsPersistance;
		private boolean fIsModified;
		
		CfgIdPair(CfgIdPair base){
			fId = base.fId;
			fPersistanceName = base.fPersistanceName;
		}

		CfgIdPair(QualifiedName persistanceName){
			fPersistanceName = persistanceName;
		}
		
		public String getId(){
			if(fId == null){
				fId = load();
				if(fId == null){
					fId = getFirstCfgId();
					if(fId != null){
						fNeedsPersistance = true;
					}
				}
			}
			return fId;
		}
		
		public ICConfigurationDescription getConfiguration() {
			if(fCfg == null){
				String id = getId();
				if(id != null){
					fCfg = getConfigurationById(id);
					if(fCfg == null){
						fId = getFirstCfgId();
						if(fId != null){
							fCfg = getConfigurationById(fId);
							fNeedsPersistance = true;
						}
					}
				}
			}
			return fCfg;
		}
		
		public void setConfiguration(ICConfigurationDescription cfg){
			if(cfg.getProjectDescription() != CProjectDescription.this)
				throw new IllegalArgumentException();
			
			if(cfg.getId().equals(getId()))
				return;
			
			fCfg = cfg;
			fId = cfg.getId();
			fIsModified = true;
			fNeedsPersistance = true;
		}
		
		public void configurationRemoved(ICConfigurationDescription cfg){
			if(cfg.getProjectDescription() != CProjectDescription.this)
				throw new IllegalArgumentException();

			if(!cfg.getId().equals(getId()))
				return;

			fIsModified = true;
			fCfg = null;
			getConfiguration();
		}
		
		private String load(){
			try {
				return getProject().getPersistentProperty(fPersistanceName);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			return null;
		}

		private boolean store(String oldId, boolean force){
			if(force || fIsModified || fNeedsPersistance || oldId == null || !oldId.equals(fId)){
				try {
					getProject().setPersistentProperty(fPersistanceName, fId);
					fIsModified = false;
					fNeedsPersistance = false;
					return true;
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
			return false;
		}

	}
	CProjectDescription(IProject project, ICStorageElement element, boolean loadding) throws CoreException {
		fProject = project;
		fRootStorageElement = element;
		fIsReadOnly = loadding;
		fIsLoadding = loadding;
		fActiveCfgInfo = new CfgIdPair(ACTIVE_CFG_PROPERTY);
		fSettingCfgInfo = new CfgIdPair(SETTING_CFG_PROPERTY);
		ICStorageElement el = null;
		CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
		if(loadding){
			Map cfgStorMap = mngr.createCfgStorages(this);
			
			for(Iterator iter = cfgStorMap.values().iterator(); iter.hasNext();){
				CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache((ICStorageElement)iter.next(), this);
				configurationCreated(cache);
			}
			
			el = getStorage(CProjectDescriptionManager.MODULE_ID, false);
		}

		fPrefs = new CProjectDescriptionPreferences(el, 
				(CProjectDescriptionPreferences)mngr.getProjectDescriptionWorkspacePreferences(false), 
				false);

		fPropertiesMap = new HashMap();
	}
	
	void updateProject(IProject project){
		fProject = project;
	}
	
	void loadDatas(){
		if(!fIsReadOnly || !fIsLoadding)
			return;
		
		CSettingEntryFactory factory = new CSettingEntryFactory();
		for(Iterator iter = fCfgMap.values().iterator(); iter.hasNext();){
			CConfigurationDescriptionCache cache = (CConfigurationDescriptionCache)iter.next();
			try {
				cache.loadData(factory);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				iter.remove();
			}
		}
		
//		doneInitializing();
		
		factory.clear();
		
//		fIsLoadding = false;
	}

	void applyDatas(){
		if(!fIsReadOnly || !fIsApplying)
			return;
		
		CSettingEntryFactory factory = new CSettingEntryFactory();
		for(Iterator iter = fCfgMap.values().iterator(); iter.hasNext();){
			CConfigurationDescriptionCache cache = (CConfigurationDescriptionCache)iter.next();
			try {
				cache.applyData(factory);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				iter.remove();
			}
		}
		
//		doneInitializing();
		
		factory.clear();
		
//		fIsApplying = false;
	}
	
	

	void doneApplying(){
		doneInitializing();
		fIsApplying = false;
	}

	void doneLoadding(){
		doneInitializing();
		fIsLoadding = false;
	}
	
	void setLoadding(boolean loadding){
		fIsLoadding = loadding;
	}

	private void doneInitializing(){
		for(Iterator iter = fCfgMap.values().iterator(); iter.hasNext();){
			CConfigurationDescriptionCache cache = (CConfigurationDescriptionCache)iter.next();
			cache.doneInitialization();
		}
		
		if(fIsReadOnly)
			fPrefs.setReadOnly(true);
	}

	public boolean isLoadding(){
		return fIsLoadding;
	}

	public boolean isApplying(){
		return fIsApplying;
	}

	public CProjectDescription(CProjectDescription base, boolean saving, ICStorageElement el) {
		fActiveCfgInfo = new CfgIdPair(base.fActiveCfgInfo);
		fSettingCfgInfo = new CfgIdPair(base.fSettingCfgInfo);
		fProject = base.fProject;
		fRootStorageElement = el;
		fIsReadOnly = saving;
		fIsLoadding = base.fIsLoadding;
		fIsApplying = saving || base.fIsApplying;
		
		fPrefs = new CProjectDescriptionPreferences(base.fPrefs, (CProjectDescriptionPreferences)CProjectDescriptionManager.getInstance().getProjectDescriptionWorkspacePreferences(false), false);
		
		for(Iterator iter = base.fCfgMap.values().iterator(); iter.hasNext();){
			try {
				IInternalCCfgInfo cfgDes = (IInternalCCfgInfo)iter.next();
				if(fIsReadOnly){
					CConfigurationData baseData = cfgDes.getConfigurationData(false);
					if(baseData instanceof CConfigurationDescriptionCache){
						baseData = ((CConfigurationDescriptionCache)baseData).getConfigurationData();
					}
					CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache((ICConfigurationDescription)cfgDes, baseData, cfgDes.getSpecSettings(), this, null);
					configurationCreated(cache);
				} else {
					CConfigurationData baseData = cfgDes.getConfigurationData(false);
					CConfigurationDescription cfg = new CConfigurationDescription(baseData, this);
					configurationCreated(cfg);
				}
			} catch (CoreException e){
				CCorePlugin.log(e);
			}
		}
		
		fPropertiesMap = (HashMap)base.fPropertiesMap.clone();
	}

	void configurationCreated(ICConfigurationDescription des){
		fCfgMap.put(des.getId(), des);
	}
	
	public ICConfigurationDescription createConfiguration(String id, String name,
			ICConfigurationDescription base) throws CoreException{
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		CConfigurationDescription cfg = new CConfigurationDescription(id, name, base, this);
		configurationCreated(cfg);
		return cfg;
	}

	public ICConfigurationDescription getActiveConfiguration() {
		return fActiveCfgInfo.getConfiguration();
	}

	private String getFirstCfgId(){
		if(!fCfgMap.isEmpty()){
			return (String)fCfgMap.keySet().iterator().next(); 
		}
		return null;
	}

	public ICConfigurationDescription getConfigurationById(String id) {
		return (ICConfigurationDescription)fCfgMap.get(id);
	}

	public ICConfigurationDescription getConfigurationByName(String name) {
		for(Iterator iter = fCfgMap.values().iterator(); iter.hasNext();){
			ICConfigurationDescription cfg = (ICConfigurationDescription)iter.next();
			if(name.equals(cfg.getName()))
				return cfg;
		}
		return null;
	}

	public ICConfigurationDescription[] getConfigurations() {
		return (ICConfigurationDescription[])fCfgMap.values().toArray(new ICConfigurationDescription[fCfgMap.size()]);
	}

	public void removeConfiguration(String name) throws WriteAccessException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		
		
		CConfigurationDescription cfgDes = (CConfigurationDescription)getConfigurationByName(name);
		if(cfgDes != null){
			cfgDes.removeConfiguration();
		}
		
	}
	
	void configurationRemoved(CConfigurationDescription des){
		fCfgMap.remove(des.getId());
		fIsModified = true;

		fActiveCfgInfo.configurationRemoved(des);
		fSettingCfgInfo.configurationRemoved(des);
	}

	public void removeConfiguration(ICConfigurationDescription cfg) throws WriteAccessException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		
		((CConfigurationDescription)cfg).removeConfiguration();
	}

	public void setActiveConfiguration(
			ICConfigurationDescription cfg) throws WriteAccessException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(cfg == null)
			throw new NullPointerException();
		
		fActiveCfgInfo.setConfiguration(cfg);
		
		if(getConfigurationReltations() == CONFIGS_LINK_SETTINGS_AND_ACTIVE)
			fSettingCfgInfo.setConfiguration(cfg);

	}
	
	public IProject getProject() {
		return fProject;
	}

	public ICStorageElement getStorage(String moduleId, boolean create) throws CoreException {
		return getStorageBase().getStorage(moduleId, create);
	}
	
//	public boolean containsStorage(String id) throws CoreException {
//		return getStorageBase().containsStorage(id);
//	}

	public ICSettingObject[] getChildSettings() {
		return getConfigurations();
	}

	public ICConfigurationDescription getConfiguration() {
		return null;
	}

	public String getId() {
		//TODO:
		return null;
	}

	public final int getType() {
		return ICSettingBase.SETTING_PROJECT;
	}

	public String getName() {
		return fProject.getName();
	}

	public ICSettingContainer getParent() {
		return null;
	}

	public boolean isValid() {
		return fProject.exists() && fCfgMap.size() > 0;
	}

	public void updateChild(CDataProxy child, boolean write) {
		if(write){
			try {
				String oldId = child.getId();
				CConfigurationDescription cfgDes = ((CConfigurationDescription)child); 
				cfgDes.doWritable();
				updateMap(cfgDes, oldId);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}
	
	void updateMap(CConfigurationDescription des, String oldId){
		if(!oldId.equals(des.getId())){
			fCfgMap.remove(oldId);
			fCfgMap.put(des.getId(), des);
		}
	}

	ICStorageElement getRootStorageElement() throws CoreException{
		if(fRootStorageElement == null){
			fRootStorageElement = CProjectDescriptionManager.getInstance().createStorage(fProject, true, true, isReadOnly());
		}
		return fRootStorageElement;
	}
	
	private ICSettingsStorage getStorageBase() throws CoreException{
		if(fStorage == null)
			fStorage = new CStorage((InternalXmlStorageElement)getRootStorageElement());
		return fStorage;
	}

	public ICConfigurationDescription createConfiguration(String buildSystemId, CConfigurationData data) throws CoreException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		CConfigurationDescription cfg = new CConfigurationDescription(data, buildSystemId, this);
		configurationCreated(cfg);
		return cfg;
	}
	
	public CConfigurationDescription createConvertedConfiguration(String id, String name, ICStorageElement el) throws CoreException{
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		CConfigurationDescription cfg = new CConfigurationDescription(id, name, el, this);
		configurationCreated(cfg);
		return cfg;
	}

	public boolean isModified() {
		if(fIsModified)
			return true;
		
		if(fActiveCfgInfo.fIsModified)
			return true;

		if(fSettingCfgInfo.fIsModified)
			return true;

		if(fPrefs.isModified())
			return true;
		
		if(fRootStorageElement != null 
				&& ((InternalXmlStorageElement)fRootStorageElement).isDirty())
			return true;
		
		for(Iterator iter = fCfgMap.values().iterator(); iter.hasNext();){
			if(((ICConfigurationDescription)iter.next()).isModified())
				return true;
		}
		return false;
	}

	public boolean isReadOnly() {
		return fIsReadOnly && !(fIsLoadding || fIsApplying);
	}

	public ICSettingObject getChildSettingById(String id) {
		return getConfigurationById(id);
	}
	
	public ICConfigurationDescription getDefaultSettingConfiguration(){
		if(getConfigurationReltations() == CONFIGS_LINK_SETTINGS_AND_ACTIVE)
			return getActiveConfiguration();
		
		return fSettingCfgInfo.getConfiguration();
	}

	public void setDefaultSettingConfiguration(ICConfigurationDescription cfg) throws WriteAccessException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(cfg == null)
			throw new NullPointerException();

		fSettingCfgInfo.setConfiguration(cfg);
		
		if(getConfigurationReltations() == CONFIGS_LINK_SETTINGS_AND_ACTIVE)
			fActiveCfgInfo.setConfiguration(cfg);
	}

	public Object getSessionProperty(QualifiedName name) {
		return fPropertiesMap.get(name);
	}

	public void setSessionProperty(QualifiedName name, Object value) {
		fPropertiesMap.put(name, value);
	}

	public void removeStorage(String id) throws CoreException {
		getStorageBase().removeStorage(id);
	}
	
	void switchToCachedAppliedData(CProjectDescription appliedCache){
		if(fIsReadOnly)
			return;
		
		ICConfigurationDescription[] cfgs = appliedCache.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			CConfigurationDescriptionCache cfgCache = (CConfigurationDescriptionCache)cfgs[i];
			CConfigurationDescription des = (CConfigurationDescription)getChildSettingById(cfgCache.getId());
			if(des != null){
				des.setData(cfgCache);
//				ICResourceDescription rcDes = des.getResourceDescription(new Path("dd"), false);
//				rcDes = des.getResourceDescription(new Path("dd"), false);
//				ICBuildSetting bs = des.getBuildSetting();
//				ICLanguageSetting lss[] = ((ICFolderDescription)rcDes).getLanguageSettings();
			}
		}
	}
	
//	boolean checkPersistCfgChanges(boolean force){
//		boolean stored = false;
//		stored |= checkPersistActiveCfg(force);
//		stored |= checkPersistSettingCfg(force);
//		return stored;
//	}
	
	boolean checkPersistActiveCfg(String oldId, boolean force){
		return fActiveCfgInfo.store(oldId, force);
	}

	boolean checkPersistSettingCfg(String oldId, boolean force){
		return fSettingCfgInfo.store(oldId, force);
	}

	boolean needsActiveCfgPersistence(){
		return fActiveCfgInfo.fIsModified;
	}

	boolean needsSettingCfgPersistence(){
		return fSettingCfgInfo.fIsModified;
	}

	CProjectDescriptionPreferences getPreferences(){
		return fPrefs;
	}

	public int getConfigurationReltations() {
		return fPrefs.getConfigurationReltations();
	}

	public boolean isDefaultConfigurationRelations() {
		return fPrefs.isDefaultConfigurationRelations();
	}

	public void setConfigurationRelations(int status) {
		fPrefs.setConfigurationRelations(status);
	}

	public void useDefaultConfigurationRelations() {
		fPrefs.useDefaultConfigurationRelations();
	}
	
	
}
