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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsBroadcastingProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.CConfigBasedDescriptorManager;
import org.eclipse.cdt.internal.core.CExtensionInfo;
import org.eclipse.cdt.internal.core.COwner;
import org.eclipse.cdt.internal.core.COwnerConfiguration;
import org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsDelta;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

/**
 * CConfigurationSpecSettings impelements ICSettingsStorage
 * to provide storage for ICStorageElements related to project settings
 *
 * This corresponds to the <cconfiguration id="....> elements within
 * the org.eclipse.cdt.core.settings storageModule in the project xml file
 */
public class CConfigurationSpecSettings implements ICSettingsStorage, ILanguageSettingsProvidersKeeper {
	static final String BUILD_SYSTEM_ID = "buildSystemId";	//$NON-NLS-1$
//	private final static String ELEMENT_REFERENCES = "references";  //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTR_POINT = "point"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTR_ID = "id"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTRIBUTE = "attribute"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTRIBUTE_KEY = "key"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION_ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSION = "extension"; //$NON-NLS-1$
	private static final String PROJECT_DATA = "data"; //$NON-NLS-1$
	private static final String PROJECT_DATA_ITEM = "item"; //$NON-NLS-1$
	private static final String PROJECT_DATA_ID = "id"; //$NON-NLS-1$
	private static final String PROJECT_EXTENSIONS = "extensions"; //$NON-NLS-1$
	private static final String OWNER_ID = "owner"; //$NON-NLS-1$
	private static final String OLD_OWNER_ID = "id"; //$NON-NLS-1$

	static final String ID = "id";	//$NON-NLS-1$
	static final String NAME = "name";	//$NON-NLS-1$
	private ICConfigurationDescription fCfg;
	private ICStorageElement fRootStorageElement;
	private ICStorageElement fSettingsStorageElement;
	private ICSettingsStorage fStorage;
	private String fBuildSystemId;
	private String fName;
	private String fId;
	private StorableCdtVariables fMacros;
	private StorableEnvironment fEnvironment;
//	private HashMap fRefInfoMap;
	private Map<String, String> fRefMapCache;
	private CExternalSettingsHolder fExtSettingsProvider = new CExternalSettingsHolder();
	private boolean fIsModified;
	private HashMap<QualifiedName, Object> fSessionPropertiesMap;
	private HashMap<String, CConfigExtensionReference[]> fExtMap;
	private HashMap<CConfigExtensionReference, CExtensionInfo> fExtInfoMap = new HashMap<CConfigExtensionReference, CExtensionInfo>();
	private String fOwnerId;
	private COwner fOwner;
//	private CConfigBasedDescriptor fDescriptor;
//	private Map fExternalSettingsProviderMap;

	private List<ILanguageSettingsProvider> fLanguageSettingsProviders = new ArrayList<ILanguageSettingsProvider>(0);
	private LinkedHashMap<String /*provider*/, LanguageSettingsStorage> lspPersistedState = new LinkedHashMap<String, LanguageSettingsStorage>();
	private String[] defaultLanguageSettingsProvidersIds = null;


	private class DeltaSet {
		public Set<ICConfigExtensionReference> extSet;
		public Set<String> idSet;
		public DeltaSet(Set<ICConfigExtensionReference> extSet, Set<String> idSet) {
			this.extSet = extSet;
			this.idSet = idSet;
		}
	}

	public CConfigurationSpecSettings(ICConfigurationDescription des, ICStorageElement storage) throws CoreException{
		fCfg = des;
		fRootStorageElement = storage;
		ICStorageElement settings = getSettingsStorageElement();

		fBuildSystemId = settings.getAttribute(BUILD_SYSTEM_ID);
		fName = settings.getAttribute(NAME);
		fId = settings.getAttribute(ID);

		setCOwner(settings.getAttribute(OWNER_ID));

		for (ICStorageElement child : settings.getChildren()) {
			String name = child.getName();

			if(StorableCdtVariables.MACROS_ELEMENT_NAME.equals(name)){
				fMacros = new StorableCdtVariables(child, fCfg.isReadOnly());
			}/* else if(ELEMENT_REFERENCES.equals(name)){
				loadReferences(child);
			} */else if (CExternalSettingsHolder.ELEMENT_EXT_SETTINGS_CONTAINER.equals(name)){
				fExtSettingsProvider = new CExternalSettingsHolder(child);
			} else if(StorableEnvironment.ENVIRONMENT_ELEMENT_NAME.equals(name)){
				fEnvironment = new StorableEnvironment(child, fCfg.isReadOnly());
			} else if(PROJECT_EXTENSIONS.equals(name)){
				loadExtensionInfo(child, false);
			}
		}

//		if(fMacros == null)
//			fMacros = new StorableMacros(des.isReadOnly());


	}

	public CConfigurationSpecSettings(ICConfigurationDescription des, ICStorageElement storage, ICStorageElement oldInfo) throws CoreException{
		fCfg = des;
		fRootStorageElement = storage;

		loadOldStileDescription(oldInfo);

	}
	private void loadOldStileDescription(ICStorageElement storage) throws CoreException{
		setCOwner(storage.getAttribute(OLD_OWNER_ID));

		loadExtensionInfo(storage, true);
	}
	public CConfigurationSpecSettings(ICConfigurationDescription des, CConfigurationSpecSettings base){
		this(des, base, null);
	}

	/**
	 * Create a new CConfigurationSpecSettings based on a base cfg spec settings
	 * @param des
	 * @param base
	 * @param rootEl
	 */
	public CConfigurationSpecSettings(ICConfigurationDescription des, CConfigurationSpecSettings base, ICStorageElement rootEl){
		fCfg = des;
		fRootStorageElement = rootEl;
		fBuildSystemId = base.fBuildSystemId;
		fName = base.fName;
		fId = base.fId;

//		copyRefInfos(base.fRefInfoMap);

		if(base.fMacros != null)
			fMacros = new StorableCdtVariables(base.fMacros, des.isReadOnly());

		if(base.fExtSettingsProvider != null)
			fExtSettingsProvider = new CExternalSettingsHolder(base.fExtSettingsProvider);

		if(base.fSessionPropertiesMap != null) {
			@SuppressWarnings("unchecked")
			HashMap<QualifiedName, Object> clone = (HashMap<QualifiedName, Object>)base.fSessionPropertiesMap.clone();
			fSessionPropertiesMap = clone;
		}

		if(base.fEnvironment != null)
			fEnvironment = EnvironmentVariableManager.fUserSupplier.cloneEnvironmentWithContext(fCfg, base.fEnvironment, des.isReadOnly());

		fOwnerId = base.fOwnerId;
		fOwner = base.fOwner;

		copyExtensionInfo(base);

		fLanguageSettingsProviders = LanguageSettingsProvidersSerializer.cloneProviders(base.getLanguageSettingProviders());
		for (String providerId : base.lspPersistedState.keySet()) {
			try {
				LanguageSettingsStorage clone = base.lspPersistedState.get(providerId).clone();
				lspPersistedState.put(providerId, clone);
			} catch (CloneNotSupportedException e) {
				CCorePlugin.log("Not able to clone language settings storage:" + e); //$NON-NLS-1$
			}
		}
		if (base.defaultLanguageSettingsProvidersIds != null) {
			defaultLanguageSettingsProvidersIds = base.defaultLanguageSettingsProvidersIds.clone();
		} else {
			defaultLanguageSettingsProvidersIds = null;
		}
	}

//	private void copyRefInfos(Map infosMap){
//		if(infosMap == null || infosMap.size() == 0){
//			fRefInfoMap = null;
//			return;
//		}
//
//		fRefInfoMap = new HashMap(infosMap.size());
//		for(Iterator iter = infosMap.entrySet().iterator(); iter.hasNext();){
//			Map.Entry entry = (Map.Entry)iter.next();
//			String projName = (String)entry.getKey();
//			ProjectRefInfo info = (ProjectRefInfo)entry.getValue();
//			fRefInfoMap.put(projName, new ProjectRefInfo(info));
//		}
//	}

	public void setCOwner(String ownerId) throws CoreException{
		if(ownerId == null)
			ownerId = CConfigBasedDescriptorManager.NULL_OWNER_ID;

		if(ownerId.equals(fOwnerId))
			return;

		fOwnerId = ownerId;
		CProjectDescriptionManager.getInstance().getDescriptorManager();
		COwnerConfiguration cfg = CConfigBasedDescriptorManager.getOwnerConfiguration(fOwnerId);
		fOwner = new COwner(cfg);
	}

//	private void loadReferences(ICStorageElement el){
//		fRefInfoMap = new HashMap();
//		ICStorageElement children[] = el.getChildren();
//
//		for(int i = 0; i < children.length; i++){
//			ICStorageElement child = children[i];
//			String name = child.getName();
//
//			if(ProjectRefInfo.ELEMENT_REFERENCE.equals(name)){
//				ProjectRefInfo info = new ProjectRefInfo(child);
//				fRefInfoMap.put(info.getProjectName(), info);
//			}
//		}
//
//		if(fRefInfoMap.size() == 0)
//			fRefInfoMap = null;
//	}

//	 private Map normalizeRefs(Map ref){
//			for(Iterator iter = ref.entrySet().iterator(); iter.hasNext();){
//				Map.Entry entry = (Map.Entry)iter.next();
//				Object projObj = entry.getKey();
//				Object cfgObj = entry.getValue();
//				if(projObj instanceof String && (cfgObj == null || cfgObj instanceof String)){
//					String proj = ((String)projObj).trim();
//					String cfg = (String)cfgObj;
//					if(cfg == null)
//						cfg = EMPTY_STRING;
//					else
//						cfg = cfg.trim();
//
//					if(proj.length() > 0){
//						entry.setValue(cfg);
//					} else {
//						iter.remove();
//					}
//				}
//			}
//			return ref;
//		}

//	 private String[] normalizeRef(String projName, String cfgId){
//		if(projName == null || (projName = projName.trim()).length() == 0)
//			return null;
//		if(cfgId == null)
//			cfgId = EMPTY_STRING;
//		else
//			cfgId = cfgId.trim();
//
//		return new String[] {projName, cfgId};
//	}

	private ICStorageElement getSettingsStorageElement() throws CoreException{
		if(fSettingsStorageElement == null)
			fSettingsStorageElement = getStorage(CProjectDescriptionManager.MODULE_ID, true);
		return fSettingsStorageElement;
	}

	@Override
	public ICStorageElement getStorage(String id,boolean create) throws CoreException {
		return getStorageBase().getStorage(id, create);
	}

	@Override
	public void removeStorage(String id) throws CoreException {
		getStorageBase().removeStorage(id);
	}

	ICStorageElement getRootStorageElement() throws CoreException{
		if(fRootStorageElement == null){
			if(fCfg.isPreferenceConfiguration()){
				fRootStorageElement = CProjectDescriptionManager.getInstance().getBuildSystemConfigPreferenceStorage(fBuildSystemId);
			} else {
				fRootStorageElement = CProjectDescriptionManager.getInstance().createStorage(fCfg.getProjectDescription(), fCfg.getId());
			}
		}
		return fRootStorageElement;
	}

	void removeConfiguration() throws CoreException{
		CProjectDescriptionManager.getInstance().removeStorage(fCfg.getProjectDescription(), fCfg.getId());
	}

	private ICSettingsStorage getStorageBase() throws CoreException{
		if(fStorage == null)
			if (fCfg.isPreferenceConfiguration())
				// Get a storage from the root storage element (in the case of a preferences element, getProject() will be null...
				fStorage = CProjectDescriptionManager.getInstance().getStorageForElement(null, getRootStorageElement());
			else
				fStorage = CProjectDescriptionManager.getInstance().getStorageForElement(fCfg.getProjectDescription().getProject(), getRootStorageElement());

		return fStorage;
	}

	void doneInitialization(){
		if(isReadOnly())
			if(fStorage != null)
				fStorage.setReadOnly(true, false);
	}

	public String getBuildSystemId(){
		return fBuildSystemId;
	}

	public ICConfigurationDescription getConfigurarion(){
		return fCfg;
	}

	public String getName() {
		return fName;
	}

	public void setName(String name){
		if(isReadOnly())
			throw ExceptionFactory.createIsReadOnlyException();
		if(!CDataUtil.objectsEqual(fName, name)){
			fName = name;
			fIsModified = true;
		}
	}

	public String getId() {
		return fId;
	}

	public void setId(String id){
		if(isReadOnly())
			throw ExceptionFactory.createIsReadOnlyException();
		if(!CDataUtil.objectsEqual(fId, id)){
			fId = id;
			fIsModified = true;
		}
	}

	void setBuildSystemId(String id){
		if(isReadOnly())
			throw ExceptionFactory.createIsReadOnlyException();
		if(!CDataUtil.objectsEqual(fBuildSystemId, id)){
			fBuildSystemId = id;
			fIsModified = true;
		}
	}

	void serializeId() throws CoreException {
		fId = fCfg.getId();
		ICStorageElement settings = getSettingsStorageElement();
		settings.setAttribute(ID, fId);
	}

	void serialize() throws CoreException {
		fId = fCfg.getId();
		fName = fCfg.getName();
		ICStorageElement settings = getSettingsStorageElement();
		settings.clear();

		settings.setAttribute(ID, fId);
		settings.setAttribute(NAME, fName);
		settings.setAttribute(BUILD_SYSTEM_ID, fBuildSystemId);

		if(fMacros != null && !fMacros.isEmpty()){
			ICStorageElement macrosEl = settings.createChild(StorableCdtVariables.MACROS_ELEMENT_NAME);
			fMacros.serialize(macrosEl);
		}

		if(fExtSettingsProvider != null){
			ICStorageElement child = settings.createChild(CExternalSettingsHolder.ELEMENT_EXT_SETTINGS_CONTAINER);
			fExtSettingsProvider.serialize(child);
		}

//		if(fRefInfoMap != null && fRefInfoMap.size() != 0){
//			ICStorageElement el = settings.createChild(ELEMENT_REFERENCES);
//			for(Iterator iter = fRefInfoMap.values().iterator(); iter.hasNext();){
//				ProjectRefInfo info = (ProjectRefInfo)iter.next();
//				ICStorageElement child = el.createChild(ProjectRefInfo.ELEMENT_REFERENCE);
//				info.serialize(child);
//			}
//		}

		ICStorageElement extEl = settings.createChild(PROJECT_EXTENSIONS);
		encodeProjectExtensions(extEl);
	}

	@Override
	public boolean isReadOnly(){
		return fCfg.isReadOnly();
	}

	@Override
	public void setReadOnly(boolean readOnly, boolean keepModify) {
		fCfg.setReadOnly(readOnly, keepModify);
	}

	public StorableCdtVariables getMacros(){
		if(fMacros == null)
			fMacros = new StorableCdtVariables(isReadOnly());
		return fMacros;
	}

	public StorableEnvironment getEnvironment(){
		return fEnvironment;
	}

	public void setEnvironment(StorableEnvironment environment){
		fEnvironment = environment;
	}

	public Map<String, String> getReferenceInfo(){
		if(!fCfg.isReadOnly())
			return CfgExportSettingContainerFactory.getReferenceMap(fCfg);
		if(fRefMapCache == null)
			fRefMapCache = CfgExportSettingContainerFactory.getReferenceMap(fCfg);
		return new LinkedHashMap<String, String>(fRefMapCache);
//		if(fRefInfoMap == null || fRefInfoMap.size() == 0)
//			return new HashMap(0);
//
//		Map map = (HashMap)fRefInfoMap.clone();
//		for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
//			Map.Entry entry = (Map.Entry)iter.next();
//			ProjectRefInfo info = (ProjectRefInfo)entry.getValue();
//			entry.setValue(info.getCfgId());
//		}
//		return map;
	}

//	public Map getProjectRefInfoMap(){
//		if(fRefInfoMap == null || fRefInfoMap.size() == 0)
//			return new HashMap(0);
//
//		return (Map)fRefInfoMap.clone();
//	}

//	public void setProjectRefInfoMap(Map map){
//		if(map == null && map.size() == 0)
//			fRefInfoMap = null;
//
//		fRefInfoMap = new HashMap(map);
//		fIsModified = true;
//	}

	public void setReferenceInfo(Map<String, String> ref){
		fRefMapCache = null;
		CfgExportSettingContainerFactory.setReferenceMap(fCfg, ref);
//		if(isReadOnly())
//			throw ExceptionFactory.createIsReadOnlyException();
//
//		List removed = null, added = null;
//		if(fRefInfos != null){
//			for(int i = 0; i < fRefInfos.length; i++){
//				String cfgId
//			}
//		}
//		if(ref != null && CProjectDescriptionManager.getInstance().normalizeRefs(ref).size() != 0){
//			fReferenceInfo = new HashMap(ref);
//		} else {
//			fReferenceInfo = null;
//		}
	}

/*
	private Map getExternalSettingsProviderMap(boolean create){
		if(fExternalSettingsProviderMap == null && create)
			fExternalSettingsProviderMap = new HashMap();
		return fExternalSettingsProviderMap;
	}

	public ExternalSettingInfo getExternalSettingsProviderInfo(String id){
		Map map = getExternalSettingsProviderMap(false);
		if(map != null)
			return (ExternalSettingInfo)map.get(id);
		return null;
	}

	public void setExternalSettingsProviderInfo(ExternalSettingInfo info){
		getExternalSettingsProviderMap(true).put(info.fProvider.getId(), info.fProvider);
	}
*/
	public ICExternalSetting[] getExternalSettings(){
		return fExtSettingsProvider.getExternalSettings();
	}

	public ICExternalSetting createExternalSetting(String[] languageIDs,
			String[] contentTypeIDs, String[] extensions,
			ICSettingEntry[] entries) {
		return fExtSettingsProvider.createExternalSetting(languageIDs, contentTypeIDs, extensions, entries);
	}

	public void removeExternalSetting(ICExternalSetting setting) {
		fExtSettingsProvider.removeExternalSetting((CExternalSetting)setting);
	}

	public void removeExternalSettings() {
		fExtSettingsProvider.removeExternalSettings();
	}

	@Override
	public boolean isModified(){
		if(fIsModified)
			return true;

		if(fMacros != null && fMacros.isDirty())
			return true;

		if(fEnvironment != null && fEnvironment.isDirty())
			return true;

		return fExtSettingsProvider.isModified();
	}

	void setModified(boolean modified){
		if(isReadOnly())
			throw ExceptionFactory.createIsReadOnlyException();
		fIsModified = modified;

		if(!modified){
			if(fMacros != null)
				fMacros.setDirty(false);

			if(fEnvironment != null)
				fEnvironment.setDirty(false);
		}
	}

	void setModified(){
		setModified(true);
	}

	private Map<QualifiedName, Object> getSessionPropertiesMap(boolean create){
		if(fSessionPropertiesMap == null && create)
			fSessionPropertiesMap = new HashMap<QualifiedName, Object>();
		return fSessionPropertiesMap;
	}

	public Object getSettionProperty(QualifiedName name){
		Map<QualifiedName, Object> map = getSessionPropertiesMap(false);
		return map != null ? map.get(name) : null;
	}

	public void setSettionProperty(QualifiedName name, Object value){
//		if(isReadOnly())
//			throw ExceptionFactory.createIsReadOnlyException();
		Map<QualifiedName, Object> map = getSessionPropertiesMap(true);
		if(value != null)
			map.put(name, value);
		else
			map.remove(name);
		fIsModified = true;
	}


	//extension reference info
	private HashMap<String, CConfigExtensionReference[]> getExtMap(){
		if(fExtMap == null)
			fExtMap = new HashMap<String, CConfigExtensionReference[]>();
		return fExtMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, CConfigExtensionReference[]> getExtensionMapCopy(){
		return (HashMap<String, CConfigExtensionReference[]>)getExtMap().clone();
	}

	private ICConfigExtensionReference[] doGet(String extensionPointID){
		return getExtMap().get(extensionPointID);
	}


	public ICConfigExtensionReference[] get(String extensionPointID) {
		checkReconsile(extensionPointID, true);

		ICConfigExtensionReference refs[] = doGet(extensionPointID);
		if (refs == null)
			return new ICConfigExtensionReference[0];
		return refs.clone();
	}

	private void checkReconsile(String extensionPointID, boolean toExt){
		if(toExt){
			DeltaSet delta = getReferenceDelta(extensionPointID);
			if(delta != null){
				if(delta.extSet != null){
					for (ICConfigExtensionReference ref : delta.extSet) {
						try {
							doRemove(ref);
						} catch (CoreException e) {
							CCorePlugin.log(e);
						}
					}
				}
				if(delta.idSet != null){
					for (String id : delta.idSet) {
						try {
							doCreate(extensionPointID, id);
						} catch (CoreException e) {
							CCorePlugin.log(e);
						}
					}
				}
			}
		} else {
			if(CCorePlugin.BINARY_PARSER_UNIQ_ID.equals(extensionPointID)){
				ICTargetPlatformSetting tp = fCfg.getTargetPlatformSetting();
				if(tp != null){
					tp.setBinaryParserIds(getIds(doGet(extensionPointID)));
				}
			} else if(CCorePlugin.ERROR_PARSER_UNIQ_ID.equals(extensionPointID)){
				ICBuildSetting bs = fCfg.getBuildSetting();
				if(bs != null){
					bs.setErrorParserIDs(getIds(doGet(extensionPointID)));
				}
			}
		}
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

	void reconcileExtensionSettings(boolean toExts){
		checkReconsile(CCorePlugin.BINARY_PARSER_UNIQ_ID, toExts);
		checkReconsile(CCorePlugin.ERROR_PARSER_UNIQ_ID, toExts);
	}

//	private boolean checkReconsile(String extPointId, ICConfigExtensionReference refs[], boolean get){
//		if(!get || !(((IInternalCCfgInfo)fCfg).getConfigurationData(false) instanceof CConfigurationDescriptionCache)){
//			if(CCorePlugin.BINARY_PARSER_UNIQ_ID.equals(extPointId))
//				return CProjectDescriptionManager.getInstance().reconcileBinaryParserExtRefs(fCfg, refs, get);
//			else if(CCorePlugin.ERROR_PARSER_UNIQ_ID.equals(extPointId))
//				return CProjectDescriptionManager.getInstance().reconcileErrorParserExtRefs(fCfg, refs, get);
//		}
//		return false;
//	}

//	synchronized private ICConfigExtensionReference[] get(String extensionID, boolean update) throws CoreException {
//		ICConfigExtensionReference[] refs = get(extensionID);
//		if (refs.length == 0 && update) {
//			boolean oldIsInitializing = isInitializing;
//			isInitializing = true;
//			fOwner.update(fProject, this, extensionID);
//			isInitializing = oldIsInitializing;
//			updateIfDirty();
//			refs = get(extensionID);
//		}
//		return refs;
//	}

	private CConfigExtensionReference createRef(String extensionPoint, String extension) {
		CConfigExtensionReference extensions[] = getExtMap().get(extensionPoint);
		if (extensions == null) {
			extensions = new CConfigExtensionReference[1];
			getExtMap().put(extensionPoint, extensions);
		} else {
			CConfigExtensionReference[] newExtensions = new CConfigExtensionReference[extensions.length + 1];
			System.arraycopy(extensions, 0, newExtensions, 0, extensions.length);
			extensions = newExtensions;
			getExtMap().put(extensionPoint, extensions);
		}
		extensions[extensions.length - 1] = new CConfigExtensionReference(this, extensionPoint, extension);
		return extensions[extensions.length - 1];
	}

	private ICConfigExtensionReference doCreate(String extensionPoint, String extension) throws CoreException {
		ICConfigExtensionReference extRef = createRef(extensionPoint, extension);
		return extRef;
	}

	public ICConfigExtensionReference create(String extensionPoint, String extension) throws CoreException {
//		boolean fireEvent = false;
		checkReconsile(extensionPoint, true);

		ICConfigExtensionReference[] refs = doGet(extensionPoint);
		ICConfigExtensionReference extRef = null;

		if (refs != null) {
			for (ICConfigExtensionReference ref : refs) {
				if(ref.getID().equals(extension)){
					extRef = ref;
					break;
				}
			}
		}

		if(extRef == null){
			extRef = createRef(extensionPoint, extension);
			checkReconsile(extensionPoint, false);
			fIsModified = true;
		}
		return extRef;
	}

	public void doRemove(ICConfigExtensionReference ext) throws CoreException {
//		boolean fireEvent = false;
//		synchronized (this) {
			CConfigExtensionReference extensions[] = getExtMap().get(ext.getExtensionPoint());
			for (int i = 0; i < extensions.length; i++) {
				if (extensions[i] == ext) {
//					System.arraycopy(extensions, i, extensions, i + 1, extensions.length - 1 - i);
					System.arraycopy(extensions, i + 1, extensions, i, extensions.length - 1 - i);
					if (extensions.length > 1) {
						CConfigExtensionReference[] newExtensions = new CConfigExtensionReference[extensions.length - 1];
						System.arraycopy(extensions, 0, newExtensions, 0, newExtensions.length);
						getExtMap().put(ext.getExtensionPoint(), newExtensions);
					} else {
						getExtMap().remove(ext.getExtensionPoint());
					}
//					updateOnDisk();
//					if (!isInitializing) {
//						fireEvent = true;
//					}
				}
			}
//		}
//		if (fireEvent) {
//			fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
//		}
	}

	public void remove(ICConfigExtensionReference ext) throws CoreException {
		doRemove(ext);

		fIsModified = true;
	}

	private boolean doRemove(String extensionPoint) throws CoreException {
//		boolean fireEvent = false;
//		synchronized (this) {
			CConfigExtensionReference extensions[] = getExtMap().get(extensionPoint);
			if (extensions != null) {
				getExtMap().remove(extensionPoint);
				return true;
//				updateOnDisk();
//				if (!isInitializing) {
//					fireEvent = true;
//				}
			}
			return false;
//		}
//		if (fireEvent) {
//			fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
//		}
	}

	public void remove(String extensionPoint) throws CoreException {

		boolean changed = doRemove(extensionPoint);

		checkReconsile(extensionPoint, false);

		if(changed)
			fIsModified = true;
	}

	CExtensionInfo getInfo(CConfigExtensionReference cProjectExtension) {
		CExtensionInfo info = fExtInfoMap.get(cProjectExtension);
		if (info == null) {
			info = new CExtensionInfo();
			fExtInfoMap.put(cProjectExtension, info);
		}
		return info;
	}

	private void loadExtensionInfo(ICStorageElement node, boolean oldData) {
		for (ICStorageElement childNode : node.getChildren()) {
//			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				if (childNode.getName().equals(PROJECT_EXTENSION)) {
					try {
						decodeProjectExtension(childNode);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				} else if (oldData && childNode.getName().equals(PROJECT_DATA)) {
					try {
						decodeProjectData(childNode);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
//			}
		}
	}

	private void decodeProjectExtension(ICStorageElement element) throws CoreException {
		String point = element.getAttribute(PROJECT_EXTENSION_ATTR_POINT);
		String id = element.getAttribute(PROJECT_EXTENSION_ATTR_ID);
		CConfigExtensionReference ext = createRef(point, id);
		for (ICStorageElement extAttr : element.getChildren()) {
			if (extAttr.getName().equals(PROJECT_EXTENSION_ATTRIBUTE)) {
//				NamedNodeMap attrib = extAttrib.item(j).getAttributes();
				getInfo(ext).setAttribute(extAttr.getAttribute(PROJECT_EXTENSION_ATTRIBUTE_KEY),
						extAttr.getAttribute(PROJECT_EXTENSION_ATTRIBUTE_VALUE));
			}
		}
	}

	private void encodeProjectExtensions(ICStorageElement configRootElement) {
		ICStorageElement element;
		for (CConfigExtensionReference[] extensions : getExtMap().values()) {
			for (CConfigExtensionReference extension : extensions) {
				element = configRootElement.createChild(PROJECT_EXTENSION);
				element.setAttribute(PROJECT_EXTENSION_ATTR_POINT, extension.getExtensionPoint());
				element.setAttribute(PROJECT_EXTENSION_ATTR_ID, extension.getID());
				CExtensionInfo info = fExtInfoMap.get(extension);
				if (info != null) {
					for (Map.Entry<String, String> entry : info.getAttributes().entrySet()) {
						ICStorageElement extAttributes = element.createChild(PROJECT_EXTENSION_ATTRIBUTE);
						extAttributes.setAttribute(PROJECT_EXTENSION_ATTRIBUTE_KEY, entry.getKey());
						extAttributes.setAttribute(PROJECT_EXTENSION_ATTRIBUTE_VALUE, entry.getValue());
					}
				}
			}
		}
	}

	private void decodeProjectData(ICStorageElement data) throws CoreException {
		for (ICStorageElement element : data.getChildren()) {
			if(PROJECT_DATA_ITEM.equals(element.getName())){
				String dataId = element.getAttribute(PROJECT_DATA_ID);
				if (dataId != null){
					element.removeAttribute(PROJECT_DATA_ID);
					try {
						importStorage(dataId, element);
					} catch(CoreException e){
					}
				}
			}
		}
	}

	@Override
	public ICStorageElement importStorage(String id, ICStorageElement el) throws CoreException {
		return getStorageBase().importStorage(id, el);
	}

	private void copyExtensionInfo(CConfigurationSpecSettings other){
		other.reconcileExtensionSettings(true);
		if(other.fExtMap != null && other.fExtMap.size() != 0){
			@SuppressWarnings("unchecked")
			HashMap<String, CConfigExtensionReference[]> clone = (HashMap<String, CConfigExtensionReference[]>)other.fExtMap.clone();
			fExtMap = clone;
			for (Map.Entry<String, CConfigExtensionReference[]> entry : fExtMap.entrySet()) {
				CConfigExtensionReference refs[] = entry.getValue();
				refs = refs.clone();
				for(int i = 0; i < refs.length; i++){
					refs[i] = new CConfigExtensionReference(this, refs[i]);
				}
				entry.setValue(refs);
			}
		}

		if(other.fExtInfoMap != null && other.fExtInfoMap.size() != 0){
			for (Map.Entry<CConfigExtensionReference, CExtensionInfo> entry : fExtInfoMap.entrySet()) {
				CExtensionInfo info = entry.getValue();
				info = new CExtensionInfo(info);
				entry.setValue(info);
			}
		}
	}

	public COwner getCOwner(){
		return fOwner;
	}

	public String getCOwnerId(){
		return fOwnerId;
	}

	private static boolean usesCache(ICConfigurationDescription cfg){
		CConfigurationData data = ((IInternalCCfgInfo)cfg).getConfigurationData(false);
		if(data instanceof CConfigurationDescriptionCache){
			return ((CConfigurationDescriptionCache)data).isReadOnly();
		}
		return false;
	}
	private DeltaSet getReferenceDelta(String extPointId){
		if(!usesCache(fCfg)){
			if(CCorePlugin.BINARY_PARSER_UNIQ_ID.equals(extPointId)){
				ICTargetPlatformSetting tp = fCfg.getTargetPlatformSetting();
				if(tp != null){
					String ids[] = tp.getBinaryParserIds();
					ICConfigExtensionReference[] refs = doGet(extPointId);
					return getReferenceDelta(refs, ids);
				}
			} else if(CCorePlugin.ERROR_PARSER_UNIQ_ID.equals(extPointId)){
				ICBuildSetting bs = fCfg.getBuildSetting();
				if(bs != null){
					String ids[] = bs.getErrorParserIDs();
					ICConfigExtensionReference[] refs = doGet(extPointId);
					return getReferenceDelta(refs, ids);
				}
			}
		}
		return null;
	}


	private DeltaSet getReferenceDelta(ICConfigExtensionReference refs[], String[] extIds){
		if(refs == null || refs.length == 0){
			if(extIds == null || extIds.length == 0)
				return null;
			return new DeltaSet(null, new HashSet<String>(Arrays.asList(extIds)));
		} else if(extIds == null || extIds.length == 0){
			Map<String, ICConfigExtensionReference> map = createRefMap(refs);
			return new DeltaSet(new HashSet<ICConfigExtensionReference>(map.values()), null);
		}

		Set<String> idSet = new HashSet<String>(Arrays.asList(extIds));
		Set<String> idSetCopy = new HashSet<String>(idSet);
		Map<String, ICConfigExtensionReference> refsMap = createRefMap(refs);

		idSet.removeAll(refsMap.keySet());
		refsMap.keySet().removeAll(idSetCopy);

		Set<ICConfigExtensionReference> extSet = new HashSet<ICConfigExtensionReference>(refsMap.values());

		return new DeltaSet(extSet, idSet);
	}

	private Map<String, ICConfigExtensionReference> createRefMap(ICConfigExtensionReference refs[]){
		Map<String, ICConfigExtensionReference> refsMap = new HashMap<String, ICConfigExtensionReference>(refs.length);
		for (ICConfigExtensionReference ref : refs) {
			refsMap.put(ref.getID(), ref);
		}
		return refsMap;
	}

	boolean extRefSettingsEqual(CConfigurationSpecSettings other){
		if(fExtMap == null || fExtMap.size() == 0)
			return other.fExtMap == null || other.fExtMap.size() == 0;
		if(other.fExtMap == null || other.fExtMap.size() == 0)
			return false;

		if(fExtMap.size() != other.fExtMap.size())
			return false;

		for (Entry<String, CConfigExtensionReference[]> entry : fExtMap.entrySet()) {
			ICConfigExtensionReference[] thisRefs = entry.getValue();
			ICConfigExtensionReference[] otherRefs = other.fExtMap.get(entry.getKey());
			if(otherRefs == null)
				return thisRefs.length == 0;
			if(thisRefs.length != otherRefs.length)
				return false;

			Map<String, ICConfigExtensionReference> map = createRefMap(thisRefs);
			map.entrySet().removeAll(createRefMap(otherRefs).entrySet());
			if(map.size() != 0)
				return false;
		}

		return true;
	}

	public String[] getExternalSettingsProviderIds(){
		return ExtensionContainerFactory.getReferencedProviderIds(fCfg);
	}

	public void setExternalSettingsProviderIds(String ids[]){
		ExtensionContainerFactory.setReferencedProviderIds(fCfg, ids);
	}

	public void updateExternalSettingsProviders(String[] ids){
		ExtensionContainerFactory.updateReferencedProviderIds(fCfg, ids);
	}

	/**
	 * Adds list of {@link ILanguageSettingsProvider} to the specs.
	 * Note that only unique IDs are accepted.
	 *
	 * @param providers - list of providers to keep in the specs.
	 */
	@Override
	public void setLanguageSettingProviders(List<ILanguageSettingsProvider> providers) {
		fLanguageSettingsProviders = new ArrayList<ILanguageSettingsProvider>(0);
		Set<String> ids = new HashSet<String>();
		for (ILanguageSettingsProvider provider : providers) {
			String id = provider.getId();
			if (provider==LanguageSettingsProvidersSerializer.getRawWorkspaceProvider(id)) {
				throw new IllegalArgumentException("Error: Attempt to add to the configuration raw global provider " + id); //$NON-NLS-1$
			}
			if (!ids.contains(id)) {
				fLanguageSettingsProviders.add(provider);
				ids.add(id);
			} else {
				throw new IllegalArgumentException("Language Settings Providers must have unique ID. Duplicate ID=" + id); //$NON-NLS-1$
			}
		}
		fIsModified = true;
	}

	@Override
	public List<ILanguageSettingsProvider> getLanguageSettingProviders() {
		return Collections.unmodifiableList(fLanguageSettingsProviders);
	}

	@Override
	public void setDefaultLanguageSettingsProvidersIds(String[] ids) {
		defaultLanguageSettingsProvidersIds = ids;
	}

	@Override
	public String[] getDefaultLanguageSettingsProvidersIds() {
		return defaultLanguageSettingsProvidersIds;
	}

	/**
	 * Returns delta and updates last persisted state to the new state.
	 * That implies that the delta needs to be used to fire an event of it will
	 * be lost.
	 */
	public LanguageSettingsDelta dropDelta() {
		LanguageSettingsDelta languageSettingsDelta = null;
		LinkedHashMap<String, LanguageSettingsStorage> newState = new LinkedHashMap<String, LanguageSettingsStorage>();
		for (ILanguageSettingsProvider provider : fLanguageSettingsProviders) {
			if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
				provider = LanguageSettingsManager.getRawProvider(provider);
			}
			if (provider instanceof ILanguageSettingsBroadcastingProvider) {
				LanguageSettingsStorage store = ((ILanguageSettingsBroadcastingProvider) provider).copyStorage();
				// avoid triggering event if empty provider was added
				if (store != null && !store.isEmpty()) {
					newState.put(provider.getId(), store);
				}
			}
		}
		if (!newState.equals(lspPersistedState)) {
			languageSettingsDelta = new LanguageSettingsDelta(lspPersistedState, newState);
			lspPersistedState = newState;
		}

		return languageSettingsDelta;
	}

}
