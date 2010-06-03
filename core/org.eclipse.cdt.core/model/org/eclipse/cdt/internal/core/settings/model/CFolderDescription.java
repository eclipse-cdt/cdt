/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CDataObject;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class CFolderDescription extends CDataProxyContainer implements
		ICFolderDescription, IProxyFactory, IInternalResourceDescription {
	private ResourceDescriptionHolder fRcHolder;
	private PathSettingsContainer fCache;
	
	CFolderDescription(CFolderData data, CConfigurationDescription cfg) {
		super(data, cfg, cfg);
	}


	public IPath getPath() {
		CResourceData data = (CResourceData)getData(false);
		return ResourceDescriptionHolder.normalizePath(data.getPath());
	}

	public boolean isExcluded() {
		CConfigurationDescription cfg = (CConfigurationDescription)getConfiguration();
		return cfg.isExcluded(getPath());
	}

	public void setExcluded(boolean excluded) {
		CConfigurationDescription cfg = (CConfigurationDescription)getConfiguration();
		cfg.setExcluded(getPath(), true, excluded);
	}

	public void setPath(IPath path) {
		path = ResourceDescriptionHolder.normalizePath(path);
		if(getPath().equals(path))
			return;
		
		CResourceData data = (CResourceData)getData(true);
		data.setPath(path);
	}

	protected CFolderData getFolderData(boolean write){
		return (CFolderData)getData(write);
	}

	@Override
	protected IProxyProvider createChildProxyProvider() {
		ICDataScope scope = new ICDataScope(){

			public CDataObject[] getChildren() {
				return getFolderData(false).getLanguageDatas();
			}

			public boolean isStatic() {
				return !containsWritableData();
			}
			
		};
		IProxyCache cache = new MapProxyCache();
	
		return new ProxyProvider(scope, cache, this);
	}


	public ICResourceDescription getNestedResourceDescription(IPath relPath, boolean exactPath) {
		return getRcHolder().getResourceDescription(relPath, exactPath);
	}


	public ICResourceDescription[] getNestedResourceDescriptions(int kind) {
		return getRcHolder().getResourceDescriptions(kind);
	}


	public ICLanguageSetting getLanguageSettingForFile(String fileName) {
		IProject project = getConfiguration().getProjectDescription().getProject();
		return CProjectDescriptionManager.getInstance().findLanguagSettingForFile(fileName, project, getLanguageSettings());
	}

	public ICLanguageSetting[] getLanguageSettings() {
		IProxyProvider provider = getChildrenProxyProvider();
		CFolderData data = (CFolderData)getData(false);
		CLanguageData lDatas[] = data.getLanguageDatas();
		ICLanguageSetting settings[] = new ICLanguageSetting[lDatas.length];
		for(int i = 0; i < lDatas.length; i++){
			settings[i] = (ICLanguageSetting)provider.getProxy(lDatas[i]);
		}
		return settings;
	}


	public CDataProxy createProxy(CDataObject data) {
		if(data instanceof CLanguageData)
			return new CLanguageSetting((CLanguageData)data, this, (CConfigurationDescription)getConfiguration());
		return null;
	}
	
	private ResourceDescriptionHolder getRcHolder(){
		if(fRcHolder == null){
			fRcHolder = ((CConfigurationDescription)getConfiguration()).createHolder(this);
		}
		return fRcHolder;
	}

	@Override
	void setData(CDataObject data) {
		super.setData(data);
		IPath cachedPath = getCachedPath();
		IPath newPath = ((CResourceData)data).getPath();
		if(cachedPath != null && !cachedPath.equals(newPath)){
			fCache.setPath(newPath, true);
		}
	}

	public IPath getCachedPath() {
		if(fCache != null)
			return fCache.getPath();
		return null;
	}

	public final int getType() {
		return ICSettingBase.SETTING_FOLDER;
	}

	public void setPathContainer(PathSettingsContainer cr) {
		fCache = cr;
	}
	
	public PathSettingsContainer getPathContainer(){
		return fCache;
	}


	public ICResourceDescription[] getNestedResourceDescriptions() {
		return getNestedResourceDescriptions(ICSettingBase.SETTING_FILE | ICSettingBase.SETTING_FOLDER);
	}


	public ICFolderDescription getParentFolderDescription() {
		return getRcHolder().getParentFolderDescription();
	}


	public ICLanguageSetting createLanguageSettingForContentTypes(
			String languageId, String[] typeIds) throws CoreException {
		CFolderData data = getFolderData(true);
		
		CLanguageData lData = data.createLanguageDataForContentTypes(languageId, typeIds);
		if(lData == null)
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CFolderDescription.0")); //$NON-NLS-1$
		
		CDataProxy proxy = getChildrenProxyProvider().getProxy(lData);
		if(!(proxy instanceof ICLanguageSetting))
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CFolderDescription.1") + proxy.getClass().getName()); //$NON-NLS-1$

		return (ICLanguageSetting)proxy;
	}


	public ICLanguageSetting createLanguageSettingForExtensions(
			String languageId, String[] extensions) throws CoreException {
		CFolderData data = getFolderData(true);
		
		CLanguageData lData = data.createLanguageDataForExtensions(languageId, extensions);
		if(lData == null)
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CFolderDescription.2")); //$NON-NLS-1$
		
		CDataProxy proxy = getChildrenProxyProvider().getProxy(lData);
		if(!(proxy instanceof ICLanguageSetting))
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CFolderDescription.3") + proxy.getClass().getName()); //$NON-NLS-1$

		return (ICLanguageSetting)proxy;
	}

	public boolean isRoot() {
		return getPath().segmentCount() == 0;
	}
	
	public boolean canExclude(boolean exclude) {
		CConfigurationDescription cfg = (CConfigurationDescription)getConfiguration();
		return cfg.canExclude(getPath(), true, exclude);
	}

	/**
	 * For debugging purpose only
	 */
	@Override
	public String toString() {
		String str = getPath().toString();
		return str.length()==0 ? "/" : str; //$NON-NLS-1$
	}
}
