/*******************************************************************************
 * Copyright (c) 2007, 2016 Intel Corporation and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.runtime.preferences.DefaultScope;

public class CProjectDescriptionPreferences implements ICProjectDescriptionPreferences {

	private static final String ATTR_CONFIG_RELATIONS = "configRelations"; //$NON-NLS-1$

	// This preference is only used at Default Scope to allow product preference customization (plugin_customization.ini)
	private static final String PREF_CPROJECTDESCRIPTION_CONFIG_RELATIONS_KEY = "cprojectdescription.configRelations"; //$NON-NLS-1$

	private boolean fIsReadOnly;
	private boolean fIsModified;

	private Integer fConfigRelations;
	private CProjectDescriptionPreferences fSuperPreference;

	CProjectDescriptionPreferences(CProjectDescriptionPreferences base, boolean isReadOnly) {
		this(base, base.fSuperPreference, isReadOnly);
	}

	CProjectDescriptionPreferences(CProjectDescriptionPreferences base, CProjectDescriptionPreferences superPreference,
			boolean isReadOnly) {
		fConfigRelations = base.fConfigRelations;
		fSuperPreference = superPreference;
		fIsReadOnly = isReadOnly;
	}

	CProjectDescriptionPreferences(ICStorageElement el, CProjectDescriptionPreferences superPreference,
			boolean isReadOnly) {
		fIsReadOnly = isReadOnly;
		if (el != null) {
			if (el.getAttribute(ATTR_CONFIG_RELATIONS) != null)
				fConfigRelations = Integer
						.valueOf(CDataUtil.getInteger(el, ATTR_CONFIG_RELATIONS, getDefaultRelations()));
		}

		this.fSuperPreference = superPreference;
	}

	protected CProjectDescriptionPreferences getSuperPreferences() {
		if (isReadOnly())
			return fSuperPreference;
		return (CProjectDescriptionPreferences) CProjectDescriptionManager.getInstance()
				.getProjectDescriptionWorkspacePreferences(false);
	}

	void serialize(ICStorageElement el) {
		if (fConfigRelations != null)
			CDataUtil.setInteger(el, ATTR_CONFIG_RELATIONS, fConfigRelations.intValue());
	}

	public static int getDefaultRelations() {
		return DefaultScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID)
				.getInt(PREF_CPROJECTDESCRIPTION_CONFIG_RELATIONS_KEY, CONFIGS_LINK_SETTINGS_AND_ACTIVE);
	}

	@Override
	public int getConfigurationRelations() {
		if (fConfigRelations != null)
			return fConfigRelations.intValue();
		CProjectDescriptionPreferences superPrefs = getSuperPreferences();
		if (superPrefs != null)
			return superPrefs.getConfigurationRelations();
		return getDefaultRelations();
	}

	@Override
	public boolean isDefaultConfigurationRelations() {
		return fConfigRelations == null;
	}

	@Override
	public void setConfigurationRelations(int status) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if (fConfigRelations != null && fConfigRelations.intValue() == status)
			return;

		fConfigRelations = Integer.valueOf(status);
		fIsModified = true;
	}

	@Override
	public void useDefaultConfigurationRelations() {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		if (fConfigRelations == null)
			return;

		fConfigRelations = null;
		fIsModified = true;
	}

	public boolean isModified() {
		return fIsModified || (fSuperPreference != null
				&& !fSuperPreference.settingsEqual((CProjectDescriptionPreferences) CProjectDescriptionManager
						.getInstance().getProjectDescriptionWorkspacePreferences(false)));
	}

	void setModified(boolean modified) {
		fIsModified = modified;
	}

	public boolean isReadOnly() {
		return fIsReadOnly;
	}

	void setReadOnly(boolean readOnly) {
		fIsReadOnly = readOnly;
	}

	public boolean settingsEqual(CProjectDescriptionPreferences other) {
		if (isDefaultConfigurationRelations() != other.isDefaultConfigurationRelations())
			return false;

		if (getConfigurationRelations() != other.getConfigurationRelations())
			return false;

		return true;
	}
}
