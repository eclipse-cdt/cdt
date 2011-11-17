/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
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
	private volatile IProject fProject;
	private final ICSettingsStorage fStorage;
	private final ICStorageElement fRootStorageElement;
	private final HashMap<String, ICConfigurationDescription> fCfgMap = new LinkedHashMap<String, ICConfigurationDescription>();
	private boolean fIsReadOnly;
	private boolean fIsModified;
	private HashMap<QualifiedName, Object> fPropertiesMap;
//	private boolean fNeedsActiveCfgIdPersistence;
	private boolean fIsLoading;
	private boolean fIsApplying;
	private boolean fIsCreating;

	private class CfgIdPair {
		private String fId;
		private ICConfigurationDescription fCfg;
		private QualifiedName fPersistanceName;
		private boolean fNeedsPersistance;
		private boolean fIsCfgModified;

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
			fIsCfgModified = true;
			fNeedsPersistance = true;
		}

		public void configurationRemoved(ICConfigurationDescription cfg){
			if(cfg.getProjectDescription() != CProjectDescription.this)
				throw new IllegalArgumentException();

			if(!cfg.getId().equals(getId()))
				return;

			fIsCfgModified = true;
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
			if(force || fIsCfgModified || fNeedsPersistance || oldId == null || !oldId.equals(fId)){
				try {
					getProject().setPersistentProperty(fPersistanceName, fId);
					fIsCfgModified = false;
					fNeedsPersistance = false;
					return true;
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
			}
			return false;
		}

	}

	public CProjectDescription(IProject project, ICSettingsStorage storage, ICStorageElement element, boolean loading, boolean isCreating) throws CoreException {
		fProject = project;
		fStorage = storage;
		fRootStorageElement = element;
		fIsReadOnly = loading;
		fIsLoading = loading;
		fActiveCfgInfo = new CfgIdPair(ACTIVE_CFG_PROPERTY);
		fSettingCfgInfo = new CfgIdPair(SETTING_CFG_PROPERTY);
		fIsCreating = isCreating;
		ICStorageElement el = null;
		CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
		if(loading){
			Map<String, ICStorageElement> cfgStorMap = mngr.createCfgStorages(this);

			for (ICStorageElement sel : cfgStorMap.values()) {
				CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache(sel, this);
				configurationCreated(cache);
			}

			el = getStorage(CProjectDescriptionManager.MODULE_ID, false);
		}

		fPrefs = new CProjectDescriptionPreferences(el,
				(CProjectDescriptionPreferences)mngr.getProjectDescriptionWorkspacePreferences(false),
				false);

		fPropertiesMap = new HashMap<QualifiedName, Object>();
	}

	public void updateProject(IProject project){
		fProject = project;
	}

	public void loadDatas(){
		if(!fIsReadOnly || !fIsLoading)
			return;

		CSettingEntryFactory factory = new CSettingEntryFactory();
		for(Iterator<ICConfigurationDescription> iter = fCfgMap.values().iterator(); iter.hasNext();){
			CConfigurationDescriptionCache cache = (CConfigurationDescriptionCache)iter.next();
			try {
				cache.loadData(factory);
				factory.clear();
			} catch (CoreException e) {
				CCorePlugin.log(e);
				iter.remove();
			}
		}

//		doneInitializing();

//		fIsLoading = false;
	}

	public boolean applyDatas(SettingsContext context){
		if(!fIsReadOnly || !fIsApplying)
			return false;

		CSettingEntryFactory factory = new CSettingEntryFactory();
		boolean modified = false;
		for (Iterator<ICConfigurationDescription> iter = fCfgMap.values().iterator(); iter.hasNext();) {
			CConfigurationDescriptionCache cache = (CConfigurationDescriptionCache)iter.next();
			try {
				if(cache.applyData(factory, context))
					modified = true;
				factory.clear();
			} catch (CoreException e) {
				CCorePlugin.log(e);
				e.printStackTrace();
				iter.remove();
			}
		}

//		doneInitializing();

//		fIsApplying = false;

		return modified;
	}

	/**
	 * Called when the read-only project description has / is being set
	 * fIsApplying => false
	 * setModified (false)
	 * set the ICSettingsStorage to readonly
	 */
	public void doneApplying(){
		doneInitializing();
		fIsApplying = false;

		try {
			getStorageBase().setReadOnly(true, false);
		} catch (CoreException e1) {
			CCorePlugin.log(e1);
		}

		setModified(false);
	}

	public void doneLoading(){
		doneInitializing();
		fIsLoading = false;
	}

	public void setLoading(boolean loading){
		fIsLoading = loading;
	}

	private void doneInitializing(){
		for (ICConfigurationDescription cfg : fCfgMap.values()) {
			// FIXME How and why are we down casting to a CConfigurationDescriptionCache. Comments, please!
			CConfigurationDescriptionCache cache = (CConfigurationDescriptionCache)cfg;
			cache.doneInitialization();
		}

		if(fIsReadOnly)
			fPrefs.setReadOnly(true);
	}

	public boolean isLoading(){
		return fIsLoading;
	}

	public boolean isApplying(){
		return fIsApplying;
	}

	/**
	 * Create a project description based on another project description
	 *
	 * @param base
	 * @param saving
	 * @param storage
	 * @param el
	 * @param isCreating
	 */
	public CProjectDescription(CProjectDescription base, boolean saving, ICSettingsStorage storage, ICStorageElement el, boolean isCreating) {
		fActiveCfgInfo = new CfgIdPair(base.fActiveCfgInfo);
		fSettingCfgInfo = new CfgIdPair(base.fSettingCfgInfo);
		fProject = base.fProject;
		fStorage = storage;
		fRootStorageElement = el;
		fIsReadOnly = saving;
		fIsLoading = base.fIsLoading;
		fIsApplying = saving || base.fIsApplying;
		fIsCreating = isCreating;

		fPrefs = new CProjectDescriptionPreferences(base.fPrefs, (CProjectDescriptionPreferences)CProjectDescriptionManager.getInstance().getProjectDescriptionWorkspacePreferences(false), false);

		for(Iterator<ICConfigurationDescription> iter = base.fCfgMap.values().iterator(); iter.hasNext();){
			try {
				IInternalCCfgInfo cfgDes = (IInternalCCfgInfo)iter.next();
				if(fIsReadOnly){
					CConfigurationData baseData = cfgDes.getConfigurationData(false);
					CConfigurationDescriptionCache baseCache = null;
					if(baseData instanceof CConfigurationDescriptionCache){
						baseCache = (CConfigurationDescriptionCache)baseData;
						baseData = baseCache.getConfigurationData();
					}
					CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache((ICConfigurationDescription)cfgDes, baseData, baseCache, cfgDes.getSpecSettings(), this, null);
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

		@SuppressWarnings("unchecked")
		HashMap<QualifiedName, Object> cloneMap = (HashMap<QualifiedName, Object>)base.fPropertiesMap.clone();
		fPropertiesMap = cloneMap;
	}

	/**
	 * Convert the current CConfigurationDescriptions to cached versions
	 * This occurs during the SetCProjectDescription Operation
	 */
	void switchToCachedConfigurationDescriptions() throws CoreException {

		for (Map.Entry<String, ICConfigurationDescription> e : fCfgMap.entrySet()) {
			if (e.getValue() instanceof CConfigurationDescription) {
				CConfigurationDescription cfgDes = (CConfigurationDescription)e.getValue();
				CConfigurationData baseData = ((IInternalCCfgInfo)cfgDes).getConfigurationData(false);
				CConfigurationDescriptionCache baseCache = null;
				if(baseData instanceof CConfigurationDescriptionCache){
					baseCache = (CConfigurationDescriptionCache)baseData;
					baseData = baseCache.getConfigurationData();
				}
				CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache(cfgDes, baseData, baseCache,
						cfgDes.getSpecSettings(), this, null);
				e.setValue(cache);
			}
		}
	}

	void configurationCreated(ICConfigurationDescription des){
		fCfgMap.put(des.getId(), des);
	}

	@Override
	public ICConfigurationDescription createConfiguration(String id, String name,
			ICConfigurationDescription base) throws CoreException{
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		CConfigurationDescription cfg = new CConfigurationDescription(id, name, base, this);
		configurationCreated(cfg);
		return cfg;
	}

	@Override
	public ICConfigurationDescription getActiveConfiguration() {
		return fActiveCfgInfo.getConfiguration();
	}

	private String getFirstCfgId(){
		if(!fCfgMap.isEmpty()){
			return fCfgMap.keySet().iterator().next();
		}
		return null;
	}

	@Override
	public ICConfigurationDescription getConfigurationById(String id) {
		return fCfgMap.get(id);
	}

	@Override
	public ICConfigurationDescription getConfigurationByName(String name) {
		for(Iterator<ICConfigurationDescription> iter = fCfgMap.values().iterator(); iter.hasNext();){
			ICConfigurationDescription cfg = iter.next();
			if(name.equals(cfg.getName()))
				return cfg;
		}
		return null;
	}

	@Override
	public ICConfigurationDescription[] getConfigurations() {
		return fCfgMap.values().toArray(new ICConfigurationDescription[fCfgMap.size()]);
	}

	@Override
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

	@Override
	public void removeConfiguration(ICConfigurationDescription cfg) throws WriteAccessException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		((CConfigurationDescription)cfg).removeConfiguration();
	}

	@Override
	public void setActiveConfiguration(
			ICConfigurationDescription cfg) throws WriteAccessException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(cfg == null)
			throw new NullPointerException();

		fActiveCfgInfo.setConfiguration(cfg);

		if(getConfigurationRelations() == CONFIGS_LINK_SETTINGS_AND_ACTIVE)
			fSettingCfgInfo.setConfiguration(cfg);

	}

	@Override
	public IProject getProject() {
		return fProject;
	}

	@Override
	public ICStorageElement getStorage(String moduleId, boolean create) throws CoreException {
		return getStorageBase().getStorage(moduleId, create);
	}

	@Override
	public ICStorageElement importStorage(String id, ICStorageElement el) throws UnsupportedOperationException, CoreException {
		return getStorageBase().importStorage(id, el);
	}

//	public boolean containsStorage(String id) throws CoreException {
//		return getStorageBase().containsStorage(id);
//	}

	@Override
	public ICSettingObject[] getChildSettings() {
		return getConfigurations();
	}

	@Override
	public ICConfigurationDescription getConfiguration() {
		return null;
	}

	@Override
	public String getId() {
		//TODO:
		return null;
	}

	@Override
	public final int getType() {
		return ICSettingBase.SETTING_PROJECT;
	}

	@Override
	public String getName() {
		return fProject.getName();
	}

	@Override
	public ICSettingContainer getParent() {
		return null;
	}

	@Override
	public boolean isValid() {
		return /*fProject.exists() &&*/ fCfgMap.size() > 0;
	}

	@Override
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

	public ICStorageElement getRootStorageElement() throws CoreException {
		if (fRootStorageElement == null)
			throw ExceptionFactory.createCoreException("CProjectDescription ICStorageElement == null"); //$NON-NLS-1$

//		if(fRootStorageElement == null){
//			fRootStorageElement = CProjectDescriptionManager.getInstance().createStorage(fProject, true, true, isReadOnly());
//		}
		return fRootStorageElement;
	}

//	ICStorageElement doGetCachedRootStorageElement(){
//		return fRootStorageElement;
//	}

	ICSettingsStorage getStorageBase() throws CoreException{
		if(fStorage == null)
//			fStorage = new CStorage((InternalXmlStorageElement)getRootStorageElement());
			throw ExceptionFactory.createCoreException("CProjectDescription ICSettingsStorage == null"); //$NON-NLS-1$
		return fStorage;
	}

	@Override
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

	@Override
	public boolean isModified() {
		if(fIsModified)
			return true;

		if(fActiveCfgInfo.fIsCfgModified)
			return true;

		if(fSettingCfgInfo.fIsCfgModified)
			return true;

		if(fPrefs.isModified())
			return true;

		if(fStorage.isModified())
			return true;

		for(ICConfigurationDescription cfgDes : fCfgMap.values())
			if(cfgDes.isModified())
				return true;

		return false;
	}

	private void setModified(boolean modified){
		fIsModified = modified;

		if(!modified){
			fActiveCfgInfo.fIsCfgModified = false;

			fSettingCfgInfo.fIsCfgModified = false;

			fPrefs.setModified(false);

			//no need to do that for config cache since they always maintain the "isModified == false"
		}
	}

	@Override
	public boolean isReadOnly() {
		return fIsReadOnly && !(fIsLoading || fIsApplying);
	}

	@Override
	public void setReadOnly(boolean readOnly, boolean keepModify) {
		fIsReadOnly = readOnly;
		try {
			getStorageBase().setReadOnly(readOnly, keepModify);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	public ICSettingObject getChildSettingById(String id) {
		return getConfigurationById(id);
	}

	@Override
	public ICConfigurationDescription getDefaultSettingConfiguration(){
		if(getConfigurationRelations() == CONFIGS_LINK_SETTINGS_AND_ACTIVE)
			return getActiveConfiguration();

		return fSettingCfgInfo.getConfiguration();
	}

	@Override
	public void setDefaultSettingConfiguration(ICConfigurationDescription cfg) throws WriteAccessException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(cfg == null)
			throw new NullPointerException();

		fSettingCfgInfo.setConfiguration(cfg);

		if(getConfigurationRelations() == CONFIGS_LINK_SETTINGS_AND_ACTIVE)
			fActiveCfgInfo.setConfiguration(cfg);
	}

	@Override
	public Object getSessionProperty(QualifiedName name) {
		return fPropertiesMap.get(name);
	}

	@Override
	public void setSessionProperty(QualifiedName name, Object value) {
		if(value != null)
			fPropertiesMap.put(name, value);
		else
			fPropertiesMap.remove(name);

		fIsModified = true;
	}

	@Override
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
		return fActiveCfgInfo.fIsCfgModified;
	}

	boolean needsSettingCfgPersistence(){
		return fSettingCfgInfo.fIsCfgModified;
	}

	CProjectDescriptionPreferences getPreferences(){
		return fPrefs;
	}

	@Override
	public int getConfigurationRelations() {
		return fPrefs.getConfigurationRelations();
	}

	@Override
	public boolean isDefaultConfigurationRelations() {
		return fPrefs.isDefaultConfigurationRelations();
	}

	@Override
	public void setConfigurationRelations(int status) {
		fPrefs.setConfigurationRelations(status);
	}

	@Override
	public void useDefaultConfigurationRelations() {
		fPrefs.useDefaultConfigurationRelations();
	}

	@Override
	public boolean isCdtProjectCreating() {
		return fIsCreating;
	}

	@Override
	public void setCdtProjectCreated() {
		if(!fIsCreating)
			return;

		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		fIsCreating = false;
		fIsModified = true;
	}

	public void touch() throws WriteAccessException {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		fIsModified = true;
	}


}
