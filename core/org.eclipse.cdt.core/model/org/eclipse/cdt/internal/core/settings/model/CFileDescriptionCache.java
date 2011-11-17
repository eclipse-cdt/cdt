/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CFileData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultFileData;
import org.eclipse.core.runtime.IPath;

public class CFileDescriptionCache extends CDefaultFileData implements
		ICFileDescription, ICachedData {
	private CConfigurationDescriptionCache fCfg;
	private ResourceDescriptionHolder fRcDesHolder;

	public CFileDescriptionCache(CFileData base, CConfigurationDescriptionCache cfg) {
		super(base.getId(), base.getPath(), cfg, null);
		fCfg = cfg;
		fCfg.addResourceDescription(this);

		copyDataFrom(base, true);
	}

	@Override
	protected CLanguageData copyLanguageData(CLanguageData data, boolean clone) {
		return new CLanguageSettingCache(data, this);
	}

	@Override
	public void setExcluded(boolean excluded) throws WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public boolean canExclude(boolean exclude) {
		return exclude == isExcluded();
	}

	@Override
	public void setName(String name) throws WriteAccessException{
		throw ExceptionFactory.createIsReadOnlyException();
	}


	@Override
	public void setPath(IPath path) throws WriteAccessException{
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public ICConfigurationDescription getConfiguration() {
		return fCfg;
	}

	@Override
	public ICSettingContainer getParent() {
		return fCfg;
	}

	@Override
	public ICLanguageSetting getLanguageSetting() {
		return (ICLanguageSetting)fLanguageData;
	}

	@Override
	public ICSettingObject[] getChildSettings() {
		return new ICSettingObject[]{(ICSettingObject)fLanguageData};
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	private ResourceDescriptionHolder getRcDesHolder(){
		if(fRcDesHolder == null)
			fRcDesHolder = fCfg.createHolderForRc(getPath());
		return fRcDesHolder;
	}


	@Override
	public ICFolderDescription getParentFolderDescription() {
		return getRcDesHolder().getParentFolderDescription();
	}

	@Override
	public IPath getPath() {
		return ResourceDescriptionHolder.normalizePath(super.getPath());
	}

	@Override
	public boolean hasCustomSettings() {
		return true;
	}

	@Override
	public boolean isExcluded() {
		return fCfg.isExcluded(getPath());
	}
}
