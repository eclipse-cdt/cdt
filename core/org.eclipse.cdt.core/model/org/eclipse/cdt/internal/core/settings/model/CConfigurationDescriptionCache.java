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
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.settings.model.CConfigurationStatus;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.IModificationContext;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.CSettingEntryFactory;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.cdt.internal.core.cdtvariables.CdtVariableManager;
import org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

public class CConfigurationDescriptionCache extends CDefaultConfigurationData
		implements ICConfigurationDescription, IInternalCCfgInfo, ICachedData {
	private CProjectDescription fParent;
	private PathSettingsContainer fPathSettingContainer = PathSettingsContainer.createRootContainer();
	private ResourceDescriptionHolder fRcHolder = new ResourceDescriptionHolder(fPathSettingContainer, true);
	private List<ICSettingObject> fChildList = new ArrayList<ICSettingObject>();
	private CConfigurationSpecSettings fSpecSettings;
	private CConfigurationData fData;
	private CConfigurationDescriptionCache fBaseCache;
	private ICSourceEntry fProjSourceEntries[];
	private StorableCdtVariables fMacros;
	private boolean fDataLoadded;
	private boolean fInitializing;
	private ICConfigurationDescription fBaseDescription;
	private CSettingEntryFactory fSettingsFactory;
	private ICSourceEntry[] fResolvedSourceEntries;

	CConfigurationDescriptionCache(ICStorageElement storage, CProjectDescription parent) throws CoreException{
		super(null);
		fInitializing = true;
		fParent = parent;
		fSpecSettings = new CConfigurationSpecSettings(this, storage);

		fId = fSpecSettings.getId();
		fName = fSpecSettings.getName();

//		loadData();
	}

	public boolean isInitializing(){
		return fInitializing;
	}

	void loadData(CSettingEntryFactory factory) throws CoreException{
		if(fDataLoadded)
			return;

		fDataLoadded = true;

		fData = CProjectDescriptionManager.getInstance().loadData(this, null);

		fSettingsFactory = factory;

		copySettingsFrom(fData, true);

		fSettingsFactory = null;

		fSpecSettings.reconcileExtensionSettings(true);
		((CBuildSettingCache)fBuildData).initEnvironmentCache();
		ICdtVariable vars[] = CdtVariableManager.getDefault().getVariables(this);
		fMacros = new StorableCdtVariables(vars, true);
//		fInitializing = false;
	}

	CConfigurationDescriptionCache(ICConfigurationDescription baseDescription, CConfigurationData base, CConfigurationDescriptionCache baseCache, CConfigurationSpecSettings settingsBase, CProjectDescription parent, ICStorageElement rootEl) throws CoreException {
		super(base.getId(), base.getName(), null);
		fInitializing = true;
		fParent = parent;
		fSpecSettings = new CConfigurationSpecSettings(this, settingsBase, rootEl);
		fSpecSettings.setModified(settingsBase.isModified());
		fBaseDescription = baseDescription;
		if(base instanceof CConfigurationDescriptionCache){
			fData = ((CConfigurationDescriptionCache)base).getConfigurationData();
//			fData = CProjectDescriptionManager.getInstance().applyData(this, baseDescription, fData);
		} else {
			fData = base;
//			base = CProjectDescriptionManager.getInstance().applyData(this, baseDescription, base);
//			fData = base;
		}

		fBaseCache = baseCache;
	}

	CConfigurationDescriptionCache getBaseCache(){
		return fBaseCache;
	}

	boolean applyData(CSettingEntryFactory factory, SettingsContext context) throws CoreException{
		boolean modified = true;
		if(fBaseDescription != null){

			fData = CProjectDescriptionManager.getInstance().applyData(this, fBaseDescription, fData, context, null);
			fDataLoadded = true;
			fName = fData.getName();
			fId = fData.getId();
			fSettingsFactory = factory;

			if((context.getAllConfigurationSettingsFlags() & IModificationContext.CFG_DATA_SETTINGS_UNMODIFIED) == 0  || fBaseCache == null){
				copySettingsFrom(fData, true);
			} else {
				copySettingsFrom(fBaseCache, true);
				modified = fSpecSettings.isModified();
				if(!modified)
					modified = (context.getAllConfigurationSettingsFlags() & IModificationContext.CFG_DATA_STORAGE_UNMODIFIED) == 0;
			}

			fSettingsFactory = null;

			ICdtVariable vars[] = CdtVariableManager.getDefault().getVariables(this);
			fMacros = new StorableCdtVariables(vars, true);
			fSpecSettings.serialize();
			fSpecSettings.setModified(false);

		}

		fBaseDescription = null;
		fBaseCache = null;

		return modified;
	}

	CSettingEntryFactory getSettingsFactory(){
		return fSettingsFactory;
	}

	public StorableCdtVariables getCachedVariables(){
		return fMacros;
	}

	protected void setId(String id) throws CoreException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public CFileData copyFileData(IPath path, CFileData base, boolean clone) {
		return new CFileDescriptionCache(base, this);
	}

	@Override
	protected CFileData copyFileData(IPath path, CFolderData base,
			CLanguageData langData) {
		//should never be here
		throw new UnsupportedOperationException();
	}

	@Override
	public CFolderData copyFolderData(IPath path, CFolderData base, boolean clone) {
		return new CFolderDescriptionCache(base, this);
	}

	@Override
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
		fBuildData = (CBuildData)bs;
	}

	@Override
	public ICProjectDescription getProjectDescription() {
		return fParent;
	}

	@Override
	public ICResourceDescription getResourceDescription(IPath path, boolean exactPath) {
		return fRcHolder.getResourceDescription(path, exactPath);
	}

	public ICResourceDescription[] getResourceDescriptions(int kind) {
		return fRcHolder.getResourceDescriptions(kind);
	}

	@Override
	public ICFolderDescription getRootFolderDescription() {
		return (ICFolderDescription)fRootFolderData;
	}

	@Override
	public boolean isActive() {
		if(isPreferenceConfiguration())
			return false;
		return fParent.getActiveConfiguration() == this;
	}

	@Override
	public void removeResourceDescription(ICResourceDescription des)
			throws CoreException {
		throw new CoreException(new DescriptionStatus(SettingsModelMessages.getString("CConfigurationDescriptionCache.0"))); //$NON-NLS-1$
	}

	@Override
	public CFileData createFileData(IPath path, CFileData base) throws CoreException{
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public CFileData createFileData(IPath path, CFolderData base, CLanguageData baseLangData) throws CoreException{
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public CFolderData createFolderData(IPath path, CFolderData base) throws CoreException{
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public void removeResourceData(CResourceData data)throws CoreException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public void setDescription(String description) throws WriteAccessException{
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public void setName(String name) throws WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public ICSettingObject[] getChildSettings() {
		return fChildList.toArray(new ICSettingObject[fChildList.size()]);
	}

	@Override
	public ICConfigurationDescription getConfiguration() {
		return this;
	}

	@Override
	public ICSettingContainer getParent() {
		return fParent;
	}

	@Override
	public ICResourceDescription[] getResourceDescriptions() {
		return fRcHolder.getResourceDescriptions();
	}

	@Override
	public ICStorageElement getStorage(String id, boolean create) throws CoreException {
		return getSpecSettings().getStorage(id, create);
	}

	@Override
	public void removeStorage(String id) throws CoreException {
		getSpecSettings().removeStorage(id);
	}

	@Override
	public ICStorageElement importStorage(String id, ICStorageElement el) throws UnsupportedOperationException, CoreException {
		return getSpecSettings().importStorage(id, el);
	}

	@Override
	public CConfigurationSpecSettings getSpecSettings() /*throws CoreException*/{
		return fSpecSettings;
	}

	@Override
	public String getBuildSystemId() {
		return fSpecSettings.getBuildSystemId();
	}

	@Override
	public CConfigurationData getConfigurationData() {
		return fData;
	}

	@Override
	public void setConfigurationData(String bsId, CConfigurationData data) throws WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public CConfigurationData getConfigurationData(boolean write) throws WriteAccessException {
		if(write)
			throw ExceptionFactory.createIsReadOnlyException();

		return this;
	}

	@Override
	public void setActive() throws WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

/*	public CConfigurationData getBaseData(){
		return fData;
	}
*/
	@Override
	public ICFileDescription createFileDescription(IPath path, ICResourceDescription base) throws CoreException, WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public ICFolderDescription createFolderDescription(IPath path, ICFolderDescription base) throws CoreException, WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	ResourceDescriptionHolder createHolderForRc(IPath path){
		return new ResourceDescriptionHolder(fPathSettingContainer.getChildContainer(path, true, true), false);
	}

	@Override
	public boolean isReadOnly() {
		return !fInitializing;
	}

	@Override
	public void setReadOnly(boolean readOnly, boolean keepModify) {
		if (readOnly)
			throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public ICTargetPlatformSetting getTargetPlatformSetting() {
		return (ICTargetPlatformSetting)getTargetPlatformData();
	}

	@Override
	protected CTargetPlatformData copyTargetPlatformData(CTargetPlatformData base, boolean clone) {
		return new CTargetPlatformSettingCache(base, this);
	}

	@Override
	public ICFileDescription[] getFileDescriptions() {
		return (ICFileDescription[])fRcHolder.getResourceDescriptions(ICSettingBase.SETTING_FILE);
	}

	@Override
	public ICFolderDescription[] getFolderDescriptions() {
		return (ICFolderDescription[])fRcHolder.getResourceDescriptions(ICSettingBase.SETTING_FOLDER);
	}

	public void setSourcePaths(IPath[] paths) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public ICSourceEntry[] getSourceEntries() {
		initSourceEntries();
		return fProjSourceEntries.clone();
	}

	private void initSourceEntries(){
		if(fProjSourceEntries == null){
			IProject project = getProject();
			fProjSourceEntries = CDataUtil.adjustEntries(fSourceEntries, true, project);
		}
	}

	private IProject getProject(){
		return isPreferenceConfiguration() ? null : getProjectDescription().getProject();
	}

	@Override
	public void setSourceEntries(ICSourceEntry[] entries) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public Map<String, String> getReferenceInfo() {
		return getSpecSettings().getReferenceInfo();
	}

	@Override
	public void setReferenceInfo(Map<String, String> refs) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public ICExternalSetting createExternalSetting(String[] languageIDs,
			String[] contentTypeIds, String[] extensions,
			ICSettingEntry[] entries) {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();

		return fSpecSettings.createExternalSetting(languageIDs, contentTypeIds, extensions, entries);
	}

	@Override
	public ICExternalSetting[] getExternalSettings() {
		return fSpecSettings.getExternalSettings();
	}

	@Override
	public void removeExternalSetting(ICExternalSetting setting) {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();

		fSpecSettings.removeExternalSetting(setting);
	}

	@Override
	public void removeExternalSettings() {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();

		fSpecSettings.removeExternalSettings();
	}

	@Override
	public ICBuildSetting getBuildSetting() {
		return (ICBuildSetting)getBuildData();
	}

	@Override
	public void setSessionProperty(QualifiedName name, Object value) {
		fSpecSettings.setSettionProperty(name, value);
		//throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public Object getSessionProperty(QualifiedName name) {
		return fSpecSettings.getSettionProperty(name);
	}

	@Override
	public ICdtVariablesContributor getBuildVariablesContributor() {
		return fData.getBuildVariablesContributor();
	}

	@Override
	public ICConfigExtensionReference create(String extensionPoint,
			String extension) throws CoreException {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		return fSpecSettings.create(extensionPoint, extension);
	}

	@Override
	public ICConfigExtensionReference[] get(String extensionPointID) {
		return fSpecSettings.get(extensionPointID);
	}

	@Override
	public void remove(ICConfigExtensionReference ext) throws CoreException {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		fSpecSettings.remove(ext);
	}

	@Override
	public void remove(String extensionPoint) throws CoreException {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		fSpecSettings.remove(extensionPoint);
	}

	@Override
	public boolean isPreferenceConfiguration() {
		return getProjectDescription() == null;
	}

	void doneInitialization(){
		CProjectDescriptionManager.getInstance().notifyCached(this, fData, null);
		fInitializing = false;
		fSpecSettings.doneInitialization();
	}

	@Override
	public ICLanguageSetting getLanguageSettingForFile(IPath path, boolean ignoreExcludeStatus) {
		return CProjectDescriptionManager.getLanguageSettingForFile(this, path, ignoreExcludeStatus);
	}

	@Override
	protected CResourceData[] filterRcDatasToCopy(CConfigurationData base) {
		if(!isPreferenceConfiguration())
			CProjectDescriptionManager.removeNonCustomSettings(getProjectDescription().getProject(), base);
		return super.filterRcDatasToCopy(base);
	}

	boolean isExcluded(IPath path){
//		if(path.segmentCount() == 0)
//			return false;

		initSourceEntries();
		IProject project = getProject();
		if(project != null)
			path = project.getFullPath().append(path);

		return CDataUtil.isExcluded(path, fProjSourceEntries);
	}

	@Override
	public String[] getExternalSettingsProviderIds() {
		return fSpecSettings.getExternalSettingsProviderIds();
	}

	@Override
	public void setExternalSettingsProviderIds(String[] ids) {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		fSpecSettings.setExternalSettingsProviderIds(ids);
	}

	@Override
	public void updateExternalSettingsProviders(String[] ids) {
		if(!fInitializing)
			throw ExceptionFactory.createIsReadOnlyException();
		fSpecSettings.updateExternalSettingsProviders(ids);
	}

	@Override
	public ICSourceEntry[] getResolvedSourceEntries() {
		if(fResolvedSourceEntries == null){
			ICSourceEntry[] entries = getSourceEntries();
			fResolvedSourceEntries = CDataUtil.resolveEntries(entries, this);
		}
		return fResolvedSourceEntries;
	}

	@Override
	public CConfigurationStatus getConfigurationStatus() {
		CConfigurationStatus status = getStatus();
		return status != null ? status : CConfigurationStatus.CFG_STATUS_OK;
	}

}
