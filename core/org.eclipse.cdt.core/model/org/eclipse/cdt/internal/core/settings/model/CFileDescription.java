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

import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CDataObject;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.CResourceData;
import org.eclipse.cdt.core.settings.model.util.PathSettingsContainer;
import org.eclipse.core.runtime.IPath;

public class CFileDescription extends CDataProxyContainer implements
		ICFileDescription, IProxyFactory, IInternalResourceDescription {
	private PathSettingsContainer fCache;
	private ResourceDescriptionHolder fRcHolder;


	CFileDescription(CFileData data, CConfigurationDescription cfg) {
		super(data, cfg, cfg);
	}

	@Override
	public IPath getPath() {
		CResourceData data = (CResourceData)getData(false);
		return ResourceDescriptionHolder.normalizePath(data.getPath());
	}

	@Override
	public boolean isExcluded() {
		CConfigurationDescription cfg = (CConfigurationDescription)getConfiguration();
		return cfg.isExcluded(getPath());
	}

	@Override
	public void setExcluded(boolean excluded) {
		CConfigurationDescription cfg = (CConfigurationDescription)getConfiguration();
		cfg.setExcluded(getPath(), false, excluded);
	}

	@Override
	public void setPath(IPath path) {
		path = ResourceDescriptionHolder.normalizePath(path);
		if(getPath().equals(path))
			return;
		CResourceData data = (CResourceData)getData(true);
		data.setPath(path);
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

	@Override
	public IPath getCachedPath() {
		if(fCache != null)
			return fCache.getPath();
		return null;
	}

	@Override
	public final int getType() {
		return ICSettingBase.SETTING_FILE;
	}

	@Override
	public void setPathContainer(PathSettingsContainer cr) {
		fCache = cr;
	}

	@Override
	public ICLanguageSetting getLanguageSetting() {
		CFileData data = getFileData(false);
		IProxyProvider provider = getChildrenProxyProvider();
		CLanguageData lData = data.getLanguageData();
		if(lData != null)
			return (ICLanguageSetting)provider.getProxy(lData);
		return null;
	}

	protected CFileData getFileData(boolean write){
		return (CFileData)getData(write);
	}

	@Override
	protected IProxyProvider createChildProxyProvider() {
		ICDataScope scope = new ICDataScope(){

			@Override
			public CDataObject[] getChildren() {
				return new CLanguageData[]{getFileData(false).getLanguageData()};
			}

			@Override
			public boolean isStatic() {
				return !containsWritableData();
			}

		};
		IProxyCache cache = new MapProxyCache();

		return new ProxyProvider(scope, cache, this);
	}

	@Override
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
	public ICFolderDescription getParentFolderDescription() {
		return getRcHolder().getParentFolderDescription();
	}

	@Override
	public PathSettingsContainer getPathContainer() {
		return fCache;
	}

	@Override
	public boolean canExclude(boolean exclude) {
		CConfigurationDescription cfg = (CConfigurationDescription)getConfiguration();
		return cfg.canExclude(getPath(), false, exclude);
	}

	/**
	 * For debugging purpose only
	 */
	@Override
	public String toString() {
		return getPath().toString();
	}
}
