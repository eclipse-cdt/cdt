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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class CProjectDescription implements ICProjectDescription, ICDataProxyContainer {

	private ICConfigurationDescription fActiveCfg;
	private String fActiveCfgId;
	private ICConfigurationDescription fIndexCfg;
	private String fIndexCfgId;
	private IProject fProject;
	private ICSettingsStorage fStorage;
	private ICStorageElement fRootStorageElement;
	private Map fCfgMap = new HashMap();
	private boolean fIsReadOnly;
	private boolean fIsModified;
	private HashMap fPropertiesMap;
	private boolean fNeedsActiveCfgIdPersistence;
	private boolean fIsLoadding;

	CProjectDescription(IProject project, ICStorageElement element, boolean loadding) throws CoreException {
		fProject = project;
		fRootStorageElement = element;
		fIsReadOnly = loadding;
		fIsLoadding = loadding;
		
		if(loadding){
			CProjectDescriptionManager mngr = CProjectDescriptionManager.getInstance();
			Map cfgStorMap = mngr.createCfgStorages(this);
			
			for(Iterator iter = cfgStorMap.values().iterator(); iter.hasNext();){
				CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache((ICStorageElement)iter.next(), this);
				configurationCreated(cache);
			}
		}
		fPropertiesMap = new HashMap();
//		loadActiveCfgId();
	}
	
	void updateProject(IProject project){
		fProject = project;
	}
	
	void loadDatas(){
		if(!fIsReadOnly || !fIsLoadding)
			return;
		
		fIsLoadding = false;
		for(Iterator iter = fCfgMap.values().iterator(); iter.hasNext();){
			CConfigurationDescriptionCache cache = (CConfigurationDescriptionCache)iter.next();
			try {
				cache.loadData();
			} catch (CoreException e) {
				CCorePlugin.log(e);
				iter.remove();
			}
		}
	}
	
	public boolean isLoadding(){
		return fIsLoadding;
	}
	
/*	CProjectDescription(IProject project) throws CoreException {
		this(project, CProjectDescriptionManager.getInstance().createStorage(project, true, false));
	}
*/	
	public CProjectDescription(CProjectDescription base, boolean saving, ICStorageElement el) {
		fActiveCfgId = base.fActiveCfgId;
		fProject = base.fProject;
		fRootStorageElement = el;
		fIsReadOnly = saving;
		fIsLoadding = base.fIsLoadding;
		
		
		
		for(Iterator iter = base.fCfgMap.values().iterator(); iter.hasNext();){
			try {
				IInternalCCfgInfo cfgDes = (IInternalCCfgInfo)iter.next();
				if(fIsReadOnly){
					CConfigurationData baseData = cfgDes.getConfigurationData(false);
					if(baseData instanceof CConfigurationDescriptionCache){
						baseData = ((CConfigurationDescriptionCache)baseData).getConfigurationData();
					}
					CConfigurationDescriptionCache cache = new CConfigurationDescriptionCache(baseData, cfgDes.getSpecSettings(), this, null, saving);
					configurationCreated(cache);
				} else {
					CConfigurationData baseData = cfgDes.getConfigurationData(false);
					CConfigurationDescription cfg = new CConfigurationDescription(baseData, this);
					configurationCreated(cfg);
				}
			} catch (CoreException e){
				//TODO: log
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
		if(fActiveCfg == null){
			String id = getActiveConfigurationId();
			if(id != null){
				fActiveCfg = getConfigurationById(id);
				if(fActiveCfg == null){
					fActiveCfgId = getFirstCfgId();
					if(fActiveCfgId != null){
						fActiveCfg = getConfigurationById(fActiveCfgId);
						fNeedsActiveCfgIdPersistence = true;
//						storeActiveCfgId(fActiveCfgId);
					}
				}
			}
		}
		return fActiveCfg;
	}

	private String getActiveConfigurationId(){
		if(fActiveCfgId == null){
			fActiveCfgId = CProjectDescriptionManager.getInstance().loadActiveCfgId(this);
			if(fActiveCfgId == null){
				fActiveCfgId = getFirstCfgId();
				if(fActiveCfgId != null){
					fNeedsActiveCfgIdPersistence = true;
//					storeActiveCfgId(fActiveCfgId);
				}
			}
		}
		return fActiveCfgId;
	}
	
	boolean needsActiveCfgIdPersistence(){
		return fNeedsActiveCfgIdPersistence;
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
		boolean wasActive = des.isActive();
		fCfgMap.remove(des.getId());
		fIsModified = true;
		
		if(wasActive){
			fActiveCfg = null;
//			fActiveCfgId = null;
			getActiveConfiguration();
		}
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
		
		if(getActiveConfiguration() != cfg){
			fActiveCfgId = cfg.getId();
			fActiveCfg = cfg;
			fIsModified = true;
//			storeActiveCfgId(fActiveCfgId);
		}
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
			fRootStorageElement = CProjectDescriptionManager.getInstance().createStorage(fProject, true, true, fIsReadOnly);
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
		return fIsReadOnly;
	}

	public ICSettingObject getChildSettingById(String id) {
		return getConfigurationById(id);
	}
	
	public ICConfigurationDescription getIndexConfiguration(){
		if(fIndexCfg == null){
			String id = getIndexConfigurationId();
			fIndexCfg = getConfigurationById(id);
		}
		return fIndexCfg;
	}
	
	private String getIndexConfigurationId(){
		if(fIndexCfgId == null)
			fIndexCfgId = getActiveConfigurationId();
		return fIndexCfgId;
	}
	
	void setIndexConfiguration(ICConfigurationDescription cfg){
		fIndexCfg = cfg;
		fIndexCfgId = cfg.getId();
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
}
