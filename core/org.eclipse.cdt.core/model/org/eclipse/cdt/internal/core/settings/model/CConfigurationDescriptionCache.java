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
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationData;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.cdt.internal.core.cdtvariables.CdtVariableManager;
import org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

public class CConfigurationDescriptionCache extends CDefaultConfigurationData
		implements ICConfigurationDescription, IInternalCCfgInfo, ICachedData {
	private CProjectDescription fParent;
	private PathSettingsContainer fPathSettingContainer = PathSettingsContainer.createRootContainer();
	private ResourceDescriptionHolder fRcHolder = new ResourceDescriptionHolder(fPathSettingContainer, true);
	private List fChildList = new ArrayList();
	private CConfigurationSpecSettings fSpecSettings;
	private CConfigurationData fData;
	private ICSourceEntry fSourceEntries[];
	private StorableCdtVariables fMacros;
	private boolean fDataLoadded;
	private boolean fInitializing;

	CConfigurationDescriptionCache(ICStorageElement storage, CProjectDescription parent) throws CoreException{
		super(null);
		fInitializing = true;
		fParent = parent;
		fSpecSettings = new CConfigurationSpecSettings(this, storage);
		
		fId = fSpecSettings.getId();
		fName = fSpecSettings.getName();
		
//		loadData();
	}
	
	void loadData() throws CoreException{
		if(fDataLoadded)
			return;
		
		fDataLoadded = true;
			
		fData = CProjectDescriptionManager.getInstance().loadData(this);
		copySettingsFrom(fData, true);
		
		CProjectDescriptionManager.getInstance().reconsileBinaryParserSettings(this, true);
		CProjectDescriptionManager.getInstance().reconsileErrorParserSettings(this, true);
		((CBuildSettingCache)fBuildData).initEnvironmentCache();
		ICdtVariable vars[] = CdtVariableManager.getDefault().getVariables(this);
		fMacros = new StorableCdtVariables(vars, true);
		fInitializing = false;
	}

	CConfigurationDescriptionCache(CConfigurationData base, CConfigurationSpecSettings settingsBase, CProjectDescription parent, ICStorageElement rootEl, boolean saving) throws CoreException {
		super(base.getId(), base.getName(), null);
		fInitializing = true;
		fParent = parent;
		fSpecSettings = new CConfigurationSpecSettings(this, settingsBase, rootEl);
		
		if(base instanceof CConfigurationDescriptionCache){
			fData = ((CConfigurationDescriptionCache)base).getConfigurationData();
			fData = CProjectDescriptionManager.getInstance().applyData(this, fData);
		} else {
			base = CProjectDescriptionManager.getInstance().applyData(this, base);
			fData = base;
		} 
		fDataLoadded = true;
		fName = fData.getName();
		fId = fData.getId();
		
		copySettingsFrom(base, true);
		
		ICdtVariable vars[] = CdtVariableManager.getDefault().getVariables(this);
		fMacros = new StorableCdtVariables(vars, true);
		if(saving)
			fSpecSettings.serialize();
		
		fInitializing = false;
	}
	
	public StorableCdtVariables getCachedVariables(){
		return fMacros;
	}

	protected void setId(String id) throws CoreException {
		throw ExceptionFactory.createIsReadOnlyException();
	}
	
	public CFileData copyFileData(IPath path, CFileData base, boolean clone) {
		return new CFileDescriptionCache(base, this);
	}

	protected CFileData copyFileData(IPath path, CFolderData base,
			CLanguageData langData) {
		//should never be here
		throw new UnsupportedOperationException();
	}

	public CFolderData copyFolderData(IPath path, CFolderData base, boolean clone) {
		return new CFolderDescriptionCache(base, this);
	}

	protected CBuildData copyBuildData(CBuildData data, boolean clone) {
		return new CBuildSettingCache(data, this);
	}

	void addResourceDescription(ICResourceDescription des){
		fRcHolder.addResourceDescription(des.getPath(), des);
		fChildList.add(des);
	}
	
	void addTargetPlatformSetting(ICTargetPlatformSetting tpS){
		fChildList.add(tpS);
	}
	
	void addBuildSetting(ICBuildSetting bs){
		fChildList.add(bs);
	}

	public ICProjectDescription getProjectDescription() {
		return fParent;
	}

	public ICResourceDescription getResourceDescription(IPath path, boolean exactPath) {
		return fRcHolder.getResourceDescription(path, exactPath);
	}

	public ICResourceDescription[] getResourceDescriptions(int kind) {
		return fRcHolder.getResourceDescriptions(kind);
	}

	public ICFolderDescription getRootFolderDescription() {
		return (ICFolderDescription)fRootFolderData;
	}

	public boolean isActive() {
		if(isPreferenceConfiguration())
			return false;
		return fParent.getActiveConfiguration() == this;
	}

	public void removeResourceDescription(ICResourceDescription des)
			throws CoreException {
		throw new CoreException(new DescriptionStatus("description is read only"));
	}

	public CFileData createFileData(IPath path, CFileData base) throws CoreException{
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public CFileData createFileData(IPath path, CFolderData base, CLanguageData baseLangData) throws CoreException{
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public CFolderData createFolderData(IPath path, CFolderData base) throws CoreException{
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void removeResourceData(CResourceData data)throws CoreException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void setDescription(String description) throws WriteAccessException{
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void setName(String name) throws WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public ICSettingObject[] getChildSettings() {
		return (ICSettingObject[])fChildList.toArray(new ICSettingObject[fChildList.size()]);
	}

	public ICConfigurationDescription getConfiguration() {
		return this;
	}

	public ICSettingContainer getParent() {
		return fParent;
	}

	public ICResourceDescription[] getResourceDescriptions() {
		return fRcHolder.getResourceDescriptions();
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

	public CConfigurationSpecSettings getSpecSettings() /*throws CoreException*/{
		return fSpecSettings;
	}

	public String getBuildSystemId() {
		return fSpecSettings.getBuildSystemId();
	}

	public CConfigurationData getConfigurationData() {
		return fData;
	}

	public void setConfigurationData(String bsId, CConfigurationData data) throws WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public boolean isModified() {
		return false;
	}

	public CConfigurationData getConfigurationData(boolean write) throws WriteAccessException {
		if(write)
			throw ExceptionFactory.createIsReadOnlyException();
		
		return this;
	}

	public void setActive() throws WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}
	
/*	public CConfigurationData getBaseData(){
		return fData;
	}
*/
	public ICFileDescription createFileDescription(IPath path, ICResourceDescription base) throws CoreException, WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public ICFolderDescription createFolderDescription(IPath path, ICFolderDescription base) throws CoreException, WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}
	
	ResourceDescriptionHolder createHolderForRc(IPath path){
		return new ResourceDescriptionHolder(fPathSettingContainer.getChildContainer(path, true, true), false);
	}

	public boolean isReadOnly() {
		return true;
	}

	public ICTargetPlatformSetting getTargetPlatformSetting() {
		return (ICTargetPlatformSetting)getTargetPlatformData();
	}

	protected CTargetPlatformData copyTargetPlatformData(CTargetPlatformData base, boolean clone) {
		return new CTargetPlatformSettingCache(base, this);
	}

	public ICFileDescription[] getFileDescriptions() {
		return (ICFileDescription[])fRcHolder.getResourceDescriptions(ICSettingBase.SETTING_FILE);
	}

	public ICFolderDescription[] getFolderDescriptions() {
		return (ICFolderDescription[])fRcHolder.getResourceDescriptions(ICSettingBase.SETTING_FOLDER);
	}

	public void setSourcePaths(IPath[] paths) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public ICSourceEntry[] getSourceEntries() {
		if(fSourceEntries == null){
			IPath[] paths = getSourcePaths();
			fSourceEntries = fRcHolder.calculateSourceEntriesFromPaths(getProjectDescription().getProject(), paths);
		}
		return fSourceEntries;
	}

	public void setSourceEntries(ICSourceEntry[] entries) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public Map getReferenceInfo() {
		return getSpecSettings().getReferenceInfo();
	}

	public void setReferenceInfo(Map refs) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public ICExternalSetting createExternalSetting(String[] languageIDs,
			String[] contentTypeIds, String[] extensions,
			ICLanguageSettingEntry[] entries) {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		
		return fSpecSettings.createExternalSetting(languageIDs, contentTypeIds, extensions, entries);
	}

	public ICExternalSetting[] getExternalSettings() {
		return fSpecSettings.getExternalSettings();
	}

	public void removeExternalSetting(ICExternalSetting setting) {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		
		fSpecSettings.removeExternalSetting(setting);
	}

	public void removeExternalSettings() {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		
		fSpecSettings.removeExternalSettings();
	}
	
	public ICBuildSetting getBuildSetting() {
		return (ICBuildSetting)getBuildData();
	}

	public void setSessionProperty(QualifiedName name, Object value) {
		fSpecSettings.setSettionProperty(name, value);
		//throw ExceptionFactory.createIsReadOnlyException();
	}

	public Object getSessionProperty(QualifiedName name) {
		return fSpecSettings.getSettionProperty(name);
	}

	public ICdtVariablesContributor getBuildVariablesContributor() {
		return fData.getBuildVariablesContributor();
	}

	public ICConfigExtensionReference create(String extensionPoint,
			String extension) throws CoreException {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		return fSpecSettings.create(extensionPoint, extension);
	}

	public ICConfigExtensionReference[] get(String extensionPointID) {
		return fSpecSettings.get(extensionPointID);
	}

	public void remove(ICConfigExtensionReference ext) throws CoreException {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		fSpecSettings.remove(ext);
	}

	public void remove(String extensionPoint) throws CoreException {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		fSpecSettings.remove(extensionPoint);
	}
	
	public boolean isPreferenceConfiguration() {
		return getProjectDescription() == null;
	}
}
