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
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.CConfigBasedDescriptorManager;
import org.eclipse.cdt.internal.core.CExtensionInfo;
import org.eclipse.cdt.internal.core.COwner;
import org.eclipse.cdt.internal.core.COwnerConfiguration;
import org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class CConfigurationSpecSettings implements ICSettingsStorage{
	static final String BUILD_SYSTEM_ID = "buildSystemId";	//$NON-NLS-1$
	private final static String ELEMENT_REFERENCES = "references";  //$NON-NLS-1$
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

	private final static String EMPTY_STRING = new String();
	static final String ID = "id";	//$NON-NLS-1$
	static final String NAME = "name";	//$NON-NLS-1$
	private ICConfigurationDescription fCfg;
	private ICStorageElement fRootStorageElement;
	private ICStorageElement fSettingsStorageElement;
	private CStorage fStorage;
	private String fBuildSystemId;
	private String fName;
	private String fId;
	private StorableCdtVariables fMacros;
	private StorableEnvironment fEnvironment;
	private HashMap fRefInfoMap;
	private CExternalSettingProvider fExtSettingsProvider = new CExternalSettingProvider();
	private boolean fIsModified;
	private HashMap fSessionPropertiesMap;
	private HashMap fExtMap;
	private HashMap fExtInfoMap = new HashMap();
	private String fOwnerId;
	private COwner fOwner;
//	private Map fExternalSettingsProviderMap;
	
	public CConfigurationSpecSettings(ICConfigurationDescription des, ICStorageElement storage) throws CoreException{
		fCfg = des;
		fRootStorageElement = storage;
		ICStorageElement settings = getSettingsStorageElement();
		
		fBuildSystemId = settings.getAttribute(BUILD_SYSTEM_ID);
		fName = settings.getAttribute(NAME);
		fId = settings.getAttribute(ID);
		
		setCOwner(settings.getAttribute(OWNER_ID));
		
		ICStorageElement children[] = settings.getChildren();
		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			String name = child.getName();
			
			if(StorableCdtVariables.MACROS_ELEMENT_NAME.equals(name)){
				fMacros = new StorableCdtVariables(child, fCfg.isReadOnly());
			} else if(ELEMENT_REFERENCES.equals(name)){
				loadReferences(child);
			} else if (CExternalSettingProvider.ELEMENT_EXT_SETTINGS_CONTAINER.equals(name)){
				fExtSettingsProvider = new CExternalSettingProvider(child);
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

	public CConfigurationSpecSettings(ICConfigurationDescription des, CConfigurationSpecSettings base, ICStorageElement rootEl){
		fCfg = des;
		fRootStorageElement = rootEl;
		fBuildSystemId = base.fBuildSystemId;
		fName = base.fName;
		fId = base.fId;
		
		copyRefInfos(base.fRefInfoMap);
		
		if(base.fMacros != null)
			fMacros = new StorableCdtVariables(base.fMacros, des.isReadOnly());
		
		if(base.fExtSettingsProvider != null)
			fExtSettingsProvider = new CExternalSettingProvider(base.fExtSettingsProvider);
		
		if(base.fSessionPropertiesMap != null)
			fSessionPropertiesMap = (HashMap)base.fSessionPropertiesMap.clone();
		
		if(base.fEnvironment != null)
			fEnvironment = new StorableEnvironment(base.fEnvironment, des.isReadOnly());
		
		fOwnerId = base.fOwnerId;
		fOwner = base.fOwner;

		copyExtensionInfo(base);
	}
	
	private void copyRefInfos(Map infosMap){
		if(infosMap == null || infosMap.size() == 0){
			fRefInfoMap = null;
			return;
		}
		
		fRefInfoMap = new HashMap(infosMap.size());
		for(Iterator iter = infosMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			String projName = (String)entry.getKey();
			ProjectRefInfo info = (ProjectRefInfo)entry.getValue();
			fRefInfoMap.put(projName, new ProjectRefInfo(info));
		}
	}
	
	public void setCOwner(String ownerId) throws CoreException{
		if(ownerId == null)
			ownerId = CConfigBasedDescriptorManager.NULL_OWNER_ID;
	
		if(ownerId.equals(fOwnerId))
			return;
		
		fOwnerId = ownerId;
		COwnerConfiguration cfg = CProjectDescriptionManager.getInstance().getDescriptorManager().getOwnerConfiguration(fOwnerId);
		fOwner = new COwner(cfg);
	}
	
	private void loadReferences(ICStorageElement el){
		fRefInfoMap = new HashMap();
		ICStorageElement children[] = el.getChildren();
		
		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			String name = child.getName();

			if(ProjectRefInfo.ELEMENT_REFERENCE.equals(name)){
				ProjectRefInfo info = new ProjectRefInfo(child);
				fRefInfoMap.put(info.getProjectName(), info);
			}
		}
		
		if(fRefInfoMap.size() == 0)
			fRefInfoMap = null;
	}
	
	 private Map normalizeRefs(Map ref){
			for(Iterator iter = ref.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				Object projObj = entry.getKey();
				Object cfgObj = entry.getValue();
				if(projObj instanceof String && (cfgObj == null || cfgObj instanceof String)){
					String proj = ((String)projObj).trim();
					String cfg = (String)cfgObj;
					if(cfg == null)
						cfg = EMPTY_STRING;
					else 
						cfg = cfg.trim();
					
					if(proj.length() > 0){
						entry.setValue(cfg);
					} else {
						iter.remove();
					}
				}
			}
			return ref;
		}

	 private String[] normalizeRef(String projName, String cfgId){
		if(projName == null || (projName = projName.trim()).length() == 0)
			return null;
		if(cfgId == null)
			cfgId = EMPTY_STRING;
		else 
			cfgId = cfgId.trim();
					
		return new String[] {projName, cfgId};
	}

	private ICStorageElement getSettingsStorageElement() throws CoreException{
		if(fSettingsStorageElement == null)
			fSettingsStorageElement =getStorage(CProjectDescriptionManager.MODULE_ID, true);
		return fSettingsStorageElement;
	}
	
	public ICStorageElement getStorage(String id,boolean create) throws CoreException {
		return getStorageBase().getStorage(id, create);
	}
	
	public void removeStorage(String id) throws CoreException {
		getStorageBase().removeStorage(id);
	}

	public boolean containsStorage(String id) throws CoreException {
		return getStorageBase().containsStorage(id);
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
	
	private CStorage getStorageBase() throws CoreException{
		if(fStorage == null)
			fStorage = new CStorage((InternalXmlStorageElement)getRootStorageElement());
		return fStorage;
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
			ICStorageElement child = settings.createChild(CExternalSettingProvider.ELEMENT_EXT_SETTINGS_CONTAINER);
			fExtSettingsProvider.serialize(child);
		}
		
		if(fRefInfoMap != null && fRefInfoMap.size() != 0){
			ICStorageElement el = settings.createChild(ELEMENT_REFERENCES);
			for(Iterator iter = fRefInfoMap.values().iterator(); iter.hasNext();){
				ProjectRefInfo info = (ProjectRefInfo)iter.next();
				ICStorageElement child = el.createChild(ProjectRefInfo.ELEMENT_REFERENCE);
				info.serialize(child);
			}
		}
		
		ICStorageElement extEl = settings.createChild(PROJECT_EXTENSIONS);
		encodeProjectExtensions(extEl);
	}
	
	public boolean isReadOnly(){
		return fCfg.isReadOnly();
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
	
	public Map getReferenceInfo(){
		if(fRefInfoMap == null || fRefInfoMap.size() == 0)
			return new HashMap(0);
		
		Map map = (HashMap)fRefInfoMap.clone();
		for(Iterator iter = map.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			ProjectRefInfo info = (ProjectRefInfo)entry.getValue();
			entry.setValue(info.getCfgId());
		}
		return map;
	}
	
	public Map getProjectRefInfoMap(){
		if(fRefInfoMap == null || fRefInfoMap.size() == 0)
			return new HashMap(0);
		
		return (Map)fRefInfoMap.clone();
	}
	
	public void setProjectRefInfoMap(Map map){
		if(map == null && map.size() == 0)
			fRefInfoMap = null;
		
		fRefInfoMap = new HashMap(map);
		fIsModified = true;
	}

/*	public void setReferenceInfo(Map ref){
		if(isReadOnly())
			throw ExceptionFactory.createIsReadOnlyException();

		List removed = null, added = null;
		if(fRefInfos != null){
			for(int i = 0; i < fRefInfos.length; i++){
				String cfgId
			}
		}
		if(ref != null && CProjectDescriptionManager.getInstance().normalizeRefs(ref).size() != 0){
			fReferenceInfo = new HashMap(ref);
		} else {
			fReferenceInfo = null;
		}
	}
*/
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
			ICLanguageSettingEntry[] entries) {
		return fExtSettingsProvider.createExternalSetting(languageIDs, contentTypeIDs, extensions, entries);
	}

	public void removeExternalSetting(ICExternalSetting setting) {
		fExtSettingsProvider.removeExternalSetting(setting);
	}

	public void removeExternalSettings() {
		fExtSettingsProvider.removeExternalSettings();
	}

	public boolean isModified(){
		if(fIsModified)
			return true;
		
		return fExtSettingsProvider.isModified();
	}
	
	void setModified(){
		if(isReadOnly())
			throw ExceptionFactory.createIsReadOnlyException();
		fIsModified = true;
	}
	
	private Map getSessionPropertiesMap(boolean create){
		if(fSessionPropertiesMap == null && create)
			fSessionPropertiesMap = new HashMap();
		return fSessionPropertiesMap;
	}
	
	public Object getSettionProperty(QualifiedName name){
		Map map = getSessionPropertiesMap(false);
		return map != null ? map.get(name) : null;
	}

	public void setSettionProperty(QualifiedName name, Object value){
//		if(isReadOnly())
//			throw ExceptionFactory.createIsReadOnlyException();
		Map map = getSessionPropertiesMap(true);
		if(value != null)
			map.put(name, value);
		else
			map.remove(name);
		fIsModified = true;
	}
	

	//extension reference info
	private HashMap getExtMap(){
		if(fExtMap == null)
			fExtMap = new HashMap();
		return fExtMap;
	}
	
	public Map getExtensionMapCopy(){
		return (HashMap)getExtMap().clone();
	}
	
	public ICConfigExtensionReference[] get(String extensionPointID) {
		CConfigExtensionReference[] refs = (CConfigExtensionReference[])getExtMap().get(extensionPointID);
		if (refs == null)
			return new ICConfigExtensionReference[0];
		return (ICConfigExtensionReference[])refs.clone();
	}

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
		CConfigExtensionReference extensions[] = (CConfigExtensionReference[])getExtMap().get(extensionPoint);
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

	public ICConfigExtensionReference create(String extensionPoint, String extension) throws CoreException {
//		boolean fireEvent = false;
		CConfigExtensionReference extRef;
//		synchronized (this) {
			extRef = createRef(extensionPoint, extension);
//			updateOnDisk();
//			if (!isInitializing) {
//				fireEvent = true;
//			}
//		}
//		if (fireEvent) {
//			fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
//		}
		return extRef;
	}

	public void remove(ICConfigExtensionReference ext) throws CoreException {
//		boolean fireEvent = false;
//		synchronized (this) {
			CConfigExtensionReference extensions[] = (CConfigExtensionReference[])getExtMap().get(ext.getExtensionPoint());
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

	public void remove(String extensionPoint) throws CoreException {
//		boolean fireEvent = false;
//		synchronized (this) {
			CConfigExtensionReference extensions[] = (CConfigExtensionReference[])getExtMap().get(extensionPoint);
			if (extensions != null) {
				getExtMap().remove(extensionPoint);
//				updateOnDisk();
//				if (!isInitializing) {
//					fireEvent = true;
//				}
			}
//		}
//		if (fireEvent) {
//			fManager.fireEvent(new CDescriptorEvent(this, CDescriptorEvent.CDTPROJECT_CHANGED, CDescriptorEvent.EXTENSION_CHANGED));
//		}
	}

	CExtensionInfo getInfo(CConfigExtensionReference cProjectExtension) {
		CExtensionInfo info = (CExtensionInfo)fExtInfoMap.get(cProjectExtension);
		if (info == null) {
			info = new CExtensionInfo();
			fExtInfoMap.put(cProjectExtension, info);
		}
		return info;
	}

	private void loadExtensionInfo(ICStorageElement node, boolean oldData) {
		ICStorageElement childNode;
		ICStorageElement list[] = node.getChildren();
		for (int i = 0; i < list.length; i++) {
			childNode = list[i];
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
		ICStorageElement extAttrib[] = element.getChildren();
		for (int j = 0; j < extAttrib.length; j++) {
			if (extAttrib[j].getName().equals(PROJECT_EXTENSION_ATTRIBUTE)) {
//				NamedNodeMap attrib = extAttrib.item(j).getAttributes();
				getInfo(ext).setAttribute(extAttrib[j].getAttribute(PROJECT_EXTENSION_ATTRIBUTE_KEY),
						extAttrib[j].getAttribute(PROJECT_EXTENSION_ATTRIBUTE_VALUE));
			}
		}
	}

	private void encodeProjectExtensions(ICStorageElement configRootElement) {
		ICStorageElement element;
		Iterator extIterator = getExtMap().values().iterator();
		while (extIterator.hasNext()) {
			CConfigExtensionReference extension[] = (CConfigExtensionReference[])extIterator.next();
			for (int i = 0; i < extension.length; i++) {
				element = configRootElement.createChild(PROJECT_EXTENSION);
				element.setAttribute(PROJECT_EXTENSION_ATTR_POINT, extension[i].getExtensionPoint());
				element.setAttribute(PROJECT_EXTENSION_ATTR_ID, extension[i].getID());
				CExtensionInfo info = (CExtensionInfo)fExtInfoMap.get(extension[i]);
				if (info != null) {
					Iterator attribIterator = info.getAttributes().entrySet().iterator();
					while (attribIterator.hasNext()) {
						Entry entry = (Entry)attribIterator.next();
						ICStorageElement extAttributes = element.createChild(PROJECT_EXTENSION_ATTRIBUTE);
						extAttributes.setAttribute(PROJECT_EXTENSION_ATTRIBUTE_KEY, (String)entry.getKey());
						extAttributes.setAttribute(PROJECT_EXTENSION_ATTRIBUTE_VALUE, (String)entry.getValue());
					}
				}
			}
		}
	}

//	private ICExtension createExtensions(ICExtensionReference ext) throws CoreException {
//		InternalCExtension cExtension = null;
//		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
//		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(ext.getExtension());
//		IExtension extension = extensionPoint.getExtension(ext.getID());
//		if (extension == null) {
//			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
//					CCorePlugin.getResourceString("CDescriptor.exception.providerNotFound") + ":" + ext.getID(), null)); //$NON-NLS-1$ //$NON-NLS-2$
//		}
//		IConfigurationElement element[] = extension.getConfigurationElements();
//		for (int i = 0; i < element.length; i++) {
//			if (element[i].getName().equalsIgnoreCase(CEXTENSION_NAME)) {
//				cExtension = (InternalCExtension)element[i].createExecutableExtension("run"); //$NON-NLS-1$
//				cExtension.setExtensionReference(ext);
//				cExtension.setProject(fProject);
//				break;
//			}
//		}
//		return (ICExtension)cExtension;
//	}
//
//	private IConfigurationElement[] getConfigurationElement(ICConfigExtensionReference ext) throws CoreException {
//		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
//		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(ext.getExtensionPoint());
//		IExtension extension = extensionPoint.getExtension(ext.getID());
//		if (extension == null) {
//			throw new CoreException(new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,
//					CCorePlugin.getResourceString("CDescriptor.exception.providerNotFound"), null)); //$NON-NLS-1$
//		}
//		IConfigurationElement element[] = extension.getConfigurationElements();
//		for (int i = 0; i < element.length; i++) {
//			if (element[i].getName().equalsIgnoreCase(CEXTENSION_NAME)) {
//				return element[i].getChildren();
//			}
//		}
//		return new IConfigurationElement[0];
//	}

	private void decodeProjectData(ICStorageElement data) throws CoreException {
		ICStorageElement[] nodes = data.getChildren();
		for (int i = 0; i < nodes.length; ++i) {
			if(PROJECT_DATA_ITEM.equals(nodes[i].getName())){
				ICStorageElement element = nodes[i];
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
	
	public void importStorage(String id, ICStorageElement el) throws CoreException{
		CStorage storage = getStorageBase();
		
		storage.importStorage(id, el);
	}
	
	private void copyExtensionInfo(CConfigurationSpecSettings other){
		if(other.fExtMap != null && other.fExtMap.size() != 0){
			fExtMap = (HashMap)other.fExtMap.clone();
			for(Iterator iter = fExtMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				CConfigExtensionReference refs[] = (CConfigExtensionReference[])entry.getValue();
				refs = (CConfigExtensionReference[])refs.clone();
				for(int i = 0; i < refs.length; i++){
					refs[i] = new CConfigExtensionReference(this, refs[i]);
				}
				entry.setValue(refs);
			}
		}
		
		if(other.fExtInfoMap != null && other.fExtInfoMap.size() != 0){
			fExtInfoMap = (HashMap)other.fExtInfoMap.clone();
			for(Iterator iter = fExtInfoMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				CExtensionInfo info = (CExtensionInfo)entry.getValue();
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
}
