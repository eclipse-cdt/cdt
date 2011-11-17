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

import org.eclipse.cdt.core.envvar.IEnvironmentContributor;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.extension.CBuildData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultBuildData;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.utils.envvar.StorableEnvironment;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class CBuildSettingCache extends CDefaultBuildData implements
		ICBuildSetting, ICachedData {
	private CConfigurationDescriptionCache fCfgCache;
	private StorableEnvironment fEnvironment;
	private StorableEnvironment fResolvedEnvironment;
	private ICOutputEntry[] fProjOutputEntries;
	private ICOutputEntry[] fResolvedOutputEntries;

	CBuildSettingCache(CBuildData base, CConfigurationDescriptionCache cfgCache){
		super(/*base.getId(), base*/);

		fId = base.getId();

		fCfgCache = cfgCache;

		fCfgCache.addBuildSetting(this);

		copySettingsFrom(base);
	}

	void initEnvironmentCache(){
		fEnvironment = new StorableEnvironment(
				EnvironmentVariableManager.getDefault().getVariables(fCfgCache, false),
				true);
	}

	public StorableEnvironment getCachedEnvironment(){
		return fEnvironment;
	}

	public StorableEnvironment getResolvedEnvironment(){
		if(fResolvedEnvironment == null){
			fResolvedEnvironment = new StorableEnvironment(
					EnvironmentVariableManager.getDefault().getVariables(fCfgCache, true),
					true);
		}
		return fResolvedEnvironment;
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
	public void setBuilderCWD(IPath path) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public void setErrorParserIDs(String[] ids) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void setName(String name) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public void setOutputDirectories(ICOutputEntry[] entries) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public IEnvironmentContributor getBuildEnvironmentContributor() {
		return fCfgCache.getConfigurationData().getBuildData().getBuildEnvironmentContributor();
	}

	@Override
	public ICOutputEntry[] getResolvedOutputDirectories() {
		if(fResolvedOutputEntries == null){
			ICOutputEntry[] entries = getOutputDirectories();
			return CDataUtil.resolveEntries(entries, getConfiguration());
		}
		return fResolvedOutputEntries;
	}

	@Override
	public ICOutputEntry[] getOutputDirectories() {
		initOutputEntries();
		return fProjOutputEntries.clone();
	}

	private void initOutputEntries(){
		if(fProjOutputEntries == null){
			IProject project = getProject();
			fProjOutputEntries = CDataUtil.adjustEntries(fOutputEntries, true, project);
		}
	}

	private IProject getProject(){
		ICConfigurationDescription cfg = getConfiguration();
		return cfg.isPreferenceConfiguration() ? null : cfg.getProjectDescription().getProject();
	}


}
