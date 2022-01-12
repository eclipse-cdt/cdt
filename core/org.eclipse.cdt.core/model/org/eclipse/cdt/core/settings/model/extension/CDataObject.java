/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.settings.model.ICSettingBase;

public abstract class CDataObject implements ICSettingBase {
	CDataObject() {
	}

	public abstract String getId();

	public abstract String getName();

	public abstract boolean isValid();

	/*
		public CConfigurationData getConfiguration(){
			return fConfiguration;
		}

		void setConfiguration(CConfigurationData cfg){
			fConfiguration = cfg;
		}
	*/
	public abstract int getType();

	/**
	 * Intended for debugging purpose only.
	 */
	@Override
	@SuppressWarnings("nls")
	public String toString() {
		int type = getType();
		String strType = "";
		switch (type) {
		case SETTING_PROJECT:
			strType = "SETTING_PROJECT";
			break;
		case SETTING_CONFIGURATION:
			strType = "SETTING_CONFIGURATION";
			break;
		case SETTING_FOLDER:
			strType = "SETTING_FOLDER";
			break;
		case SETTING_FILE:
			strType = "SETTING_FILE";
			break;
		case SETTING_LANGUAGE:
			strType = "SETTING_LANGUAGE";
			break;
		case SETTING_TARGET_PLATFORM:
			strType = "SETTING_TARGET_PLATFORM";
			break;
		case SETTING_BUILD:
			strType = "SETTING_BUILD";
			break;
		}
		return "type=0x" + Integer.toHexString(type) + ":" + strType + ", name=[" + getName() + "]" + ", id=[" + getId()
				+ "]";
	}
}
