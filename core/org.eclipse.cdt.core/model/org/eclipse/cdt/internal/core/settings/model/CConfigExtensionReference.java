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

	public String getExtensionPoint() {
		return fExtPoint;
	}

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
			CConfigExtensionReference ext = (CConfigExtensionReference)obj;
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

	public void setExtensionData(String key, String value) throws CoreException {
		getInfo().setAttribute(key, value);
		fCfgSettings.setModified();
	}

	public String getExtensionData(String key) {
		return getInfo().getAttribute(key);
	}

	public ICConfigurationDescription getConfiguration() {
		return fCfgSettings.getConfigurarion();
	}
}
