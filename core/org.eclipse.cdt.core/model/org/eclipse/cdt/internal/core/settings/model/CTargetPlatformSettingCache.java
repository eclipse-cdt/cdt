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
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultTargetPlatformData;

public class CTargetPlatformSettingCache extends CDefaultTargetPlatformData
		implements ICTargetPlatformSetting, ICachedData {
	private CConfigurationDescriptionCache fCfgCache;
	CTargetPlatformSettingCache(CTargetPlatformData base, CConfigurationDescriptionCache cfgCache){
		fId = base.getId();

		fCfgCache = cfgCache;

		fCfgCache.addTargetPlatformSetting(this);

		copyDataFrom(base);
	}


	@Override
	public ICConfigurationDescription getConfiguration() {
		return fCfgCache;
	}

	@Override
	public ICSettingContainer getParent() {
		return fCfgCache;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public void setBinaryParserIds(String[] ids) {
		if(!fCfgCache.isInitializing())
			throw ExceptionFactory.createIsReadOnlyException();

		super.setBinaryParserIds(ids);
	}

	public void setName(String name) {
		if(!fCfgCache.isInitializing())
			throw ExceptionFactory.createIsReadOnlyException();
	}
}
