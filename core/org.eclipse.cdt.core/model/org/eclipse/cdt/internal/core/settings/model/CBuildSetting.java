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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class CBuildSetting extends CDataProxy implements ICBuildSetting {

	CBuildSetting(CBuildData data, CConfigurationDescription cfg) {
		super(data, cfg, cfg);
	}

	@Override
	public IPath getBuilderCWD() {
		CBuildData data = getBuildData(false);
		return data.getBuilderCWD();
	}

	private CBuildData getBuildData(boolean write){
		return (CBuildData)getData(write);
	}

	@Override
	public String[] getErrorParserIDs() {
		CBuildData data = getBuildData(false);
		return data.getErrorParserIDs();
	}

	@Override
	public ICOutputEntry[] getOutputDirectories() {
		CBuildData data = getBuildData(false);
		ICOutputEntry[] entries = data.getOutputDirectories();
		IProject project = getProject();
		entries = CDataUtil.adjustEntries(entries, true, project);
		return entries;
	}

	@Override
	public void setBuilderCWD(IPath path) {
		CBuildData data = getBuildData(true);
		data.setBuilderCWD(path);
	}

	@Override
	public void setErrorParserIDs(String[] ids) {
		CBuildData data = getBuildData(true);
		data.setErrorParserIDs(ids);
	}

	@Override
	public void setOutputDirectories(ICOutputEntry[] entries) {
		CBuildData data = getBuildData(true);
		IProject project = getProject();
		if(entries != null){
			entries = CDataUtil.adjustEntries(entries, false, project);
		}

		data.setOutputDirectories(entries);
		if(entries == null){
			CExternalSettingsManager.getInstance().restoreOutputEntryDefaults(getConfiguration());
		}
	}

	@Override
	public final int getType() {
		return ICSettingBase.SETTING_BUILD;
	}

	@Override
	public IEnvironmentContributor getBuildEnvironmentContributor() {
		CBuildData data = getBuildData(false);
		return data.getBuildEnvironmentContributor();
	}

	@Override
	public ICOutputEntry[] getResolvedOutputDirectories() {
		ICOutputEntry[] entries = getOutputDirectories();
		return CDataUtil.resolveEntries(entries, getConfiguration());
	}
}
