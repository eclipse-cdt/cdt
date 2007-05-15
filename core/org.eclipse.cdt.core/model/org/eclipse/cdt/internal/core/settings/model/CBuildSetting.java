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

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.runtime.IPath;

public class CBuildSetting extends CDataProxy implements ICBuildSetting {

	CBuildSetting(CBuildData data, CConfigurationDescription cfg) {
		super(data, cfg, cfg);
	}

	public IPath getBuilderCWD() {
		CBuildData data = getBuildData(false);
		return data.getBuilderCWD();
	}

	private CBuildData getBuildData(boolean write){
		return (CBuildData)getData(write);
	}

	public String[] getErrorParserIDs() {
		CBuildData data = getBuildData(false);
		return data.getErrorParserIDs();
	}

	public ICOutputEntry[] getOutputDirectories() {
		CBuildData data = getBuildData(false);
		return data.getOutputDirectories();
	}

	public void setBuilderCWD(IPath path) {
		CBuildData data = getBuildData(true);
		data.setBuilderCWD(path);
	}

	public void setErrorParserIDs(String[] ids) {
		CBuildData data = getBuildData(true);
		data.setErrorParserIDs(ids);
	}

	public void setOutputDirectories(ICOutputEntry[] entries) {
		CBuildData data = getBuildData(true);
		data.setOutputDirectories(entries);
		if(entries == null){
			CExternalSettingsManager.getInstance().restoreOutputEntryDefaults(getConfiguration());
		}
	}

	public final int getType() {
		return ICSettingBase.SETTING_BUILD;
	}

	public IEnvironmentContributor getBuildEnvironmentContributor() {
		CBuildData data = getBuildData(false);
		return data.getBuildEnvironmentContributor();
	}

	public ICOutputEntry[] getResolvedOutputDirectories() {
		ICOutputEntry[] entries = getOutputDirectories();
		return CDataUtil.resolveEntries(entries, getConfiguration());
	}
}
