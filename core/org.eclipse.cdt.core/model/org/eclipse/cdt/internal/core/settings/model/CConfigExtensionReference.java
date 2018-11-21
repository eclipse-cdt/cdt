/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.CExtensionInfo;
import org.eclipse.core.runtime.CoreException;

public class CConfigExtensionReference implements ICConfigExtensionReference {

	private CConfigurationSpecSettings fCfgSettings;
	private String fExtPoint;
	private String fId;

	public CConfigExtensionReference(CConfigurationSpecSettings cfg, String extPoint, String id) {
		fCfgSettings = cfg;
		fExtPoint = extPoint;
		fId = id;
	}

	public CConfigExtensionReference(CConfigurationSpecSettings cfg, CConfigExtensionReference base) {
		fCfgSettings = cfg;
		fExtPoint = base.fExtPoint;
		fId = base.fId;
	}

	@Override
	public String getExtensionPoint() {
		return fExtPoint;
	}

	@Override
	public String getID() {
		return fId;
	}

	private CExtensionInfo getInfo() {
		return fCfgSettings.getInfo(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof CConfigExtensionReference) {
			CConfigExtensionReference ext = (CConfigExtensionReference) obj;
			if (ext.fExtPoint.equals(fExtPoint) && ext.fId.equals(fId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fExtPoint.hashCode() + fId.hashCode();
	}

	@Override
	public void setExtensionData(String key, String value) throws CoreException {
		getInfo().setAttribute(key, value);
		fCfgSettings.setModified();
	}

	@Override
	public String getExtensionData(String key) {
		return getInfo().getAttribute(key);
	}

	@Override
	public ICConfigurationDescription getConfiguration() {
		return fCfgSettings.getConfigurarion();
	}
}
