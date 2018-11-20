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

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultFolderData;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;

public class CFolderDescriptionCache extends CDefaultFolderData implements ICFolderDescription, ICachedData {
	private CConfigurationDescriptionCache fCfg;
	private ResourceDescriptionHolder fRcDesHolder;

	public CFolderDescriptionCache(CFolderData base, CConfigurationDescriptionCache cfg) {
		super(base.getId(), base.getPath(), cfg, null);
		fCfg = cfg;
		fCfg.addResourceDescription(this);

		copyDataFrom(base, true);
	}

	@Override
	public ICLanguageSetting getLanguageSettingForFile(String fileName) {
		IProject project = getConfiguration().getProjectDescription().getProject();
		return CProjectDescriptionManager.getInstance().findLanguagSettingForFile(fileName, project,
				getLanguageSettings());
	}

	@Override
	public ICLanguageSetting[] getLanguageSettings() {
		return fLanguageDatas.toArray(new CLanguageSettingCache[fLanguageDatas.size()]);
	}

	@Override
	public ICResourceDescription getNestedResourceDescription(IPath relPath, boolean exactPath) {
		return getRcDesHolder().getResourceDescription(relPath, exactPath);
	}

	@Override
	public ICResourceDescription[] getNestedResourceDescriptions(int kind) {
		return getRcDesHolder().getResourceDescriptions(kind);
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
	public ICSettingObject[] getChildSettings() {
		return getLanguageSettings();
	}

	@Override
	protected CLanguageData copyLanguageData(CLanguageData base, boolean clone) {
		return new CLanguageSettingCache(base, this);
	}

	@Override
	public void setExcluded(boolean excluded) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public boolean canExclude(boolean exclude) {
		return exclude == isExcluded();
	}

	@Override
	public void setPath(IPath path) throws WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	public void setName(String name) throws WriteAccessException {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public ICResourceDescription[] getNestedResourceDescriptions() {
		return getNestedResourceDescriptions(ICSettingBase.SETTING_FILE | ICSettingBase.SETTING_FOLDER);
	}

	private ResourceDescriptionHolder getRcDesHolder() {
		if (fRcDesHolder == null)
			fRcDesHolder = fCfg.createHolderForRc(getPath());
		return fRcDesHolder;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public ICFolderDescription getParentFolderDescription() {
		return getRcDesHolder().getParentFolderDescription();
	}

	public ICLanguageSetting createLanguageSetting(IContentType srcType) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public ICLanguageSetting createLanguageSettingForContentTypes(String languageId, String[] typeIds) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public ICLanguageSetting createLanguageSettingForExtensions(String languageId, String[] extensions) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public CLanguageData createLanguageDataForContentTypes(String languageId, String[] typesIds) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public CLanguageData createLanguageDataForExtensions(String languageId, String[] extensions) {
		throw ExceptionFactory.createIsReadOnlyException();
	}

	@Override
	public boolean isRoot() {
		return getPath().segmentCount() == 0;
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
