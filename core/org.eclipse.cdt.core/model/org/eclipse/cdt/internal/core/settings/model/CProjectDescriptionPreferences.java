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

import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;

public class CProjectDescriptionPreferences implements ICProjectDescriptionPreferences {
	private static final String ATTR_CONFIG_RELATIONS = "configRelations"; //$NON-NLS-1$

	private static final int DEFAULT_RELATIONS = CONFIGS_INDEPENDENT;
	private boolean fIsReadOnly;
	private boolean fIsModified;

	private Integer fConfigRelations;
	private CProjectDescriptionPreferences fSuperPreference;

	CProjectDescriptionPreferences(CProjectDescriptionPreferences base, boolean isReadOnly){
		this(base, base.fSuperPreference, isReadOnly);
	}

	CProjectDescriptionPreferences(CProjectDescriptionPreferences base, CProjectDescriptionPreferences superPreference, boolean isReadOnly){
		fConfigRelations = base.fConfigRelations;
		fSuperPreference = superPreference;
		fIsReadOnly = isReadOnly;
	}

	CProjectDescriptionPreferences(ICStorageElement el, CProjectDescriptionPreferences superPreference, boolean isReadOnly){
		fIsReadOnly = isReadOnly;
		if(el != null){
			if(el.getAttribute(ATTR_CONFIG_RELATIONS) != null)
				fConfigRelations = new Integer(CDataUtil.getInteger(el, ATTR_CONFIG_RELATIONS, DEFAULT_RELATIONS));
		}

		this.fSuperPreference = superPreference;
	}

	protected CProjectDescriptionPreferences getSuperPreferences(){
		if(isReadOnly())
			return fSuperPreference;
		return (CProjectDescriptionPreferences)CProjectDescriptionManager.getInstance().getProjectDescriptionWorkspacePreferences(false);
	}

	void serialize(ICStorageElement el){
		if(fConfigRelations != null)
			CDataUtil.setInteger(el, ATTR_CONFIG_RELATIONS, fConfigRelations.intValue());
	}

	@Override
	public int getConfigurationRelations() {
		if(fConfigRelations != null)
			return fConfigRelations.intValue();
		CProjectDescriptionPreferences superPrefs = getSuperPreferences();
		if(superPrefs != null)
			return superPrefs.getConfigurationRelations();
		return DEFAULT_RELATIONS;
	}

	@Override
	public boolean isDefaultConfigurationRelations() {
		return fConfigRelations == null;
	}

	@Override
	public void setConfigurationRelations(int status) {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(fConfigRelations != null && fConfigRelations.intValue() == status)
			return;

		fConfigRelations = new Integer(status);
		fIsModified = true;
	}

	@Override
	public void useDefaultConfigurationRelations() {
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		if(fConfigRelations == null)
			return;

		fConfigRelations = null;
		fIsModified = true;
	}

	public boolean isModified(){
		return fIsModified
			|| (fSuperPreference != null
					&& !fSuperPreference.settingsEqual((CProjectDescriptionPreferences)CProjectDescriptionManager.getInstance().getProjectDescriptionWorkspacePreferences(false)));
	}

	void setModified(boolean modified){
		fIsModified = modified;
	}

	public boolean isReadOnly(){
		return fIsReadOnly;
	}

	void setReadOnly(boolean readOnly){
		fIsReadOnly = readOnly;
	}

	public boolean settingsEqual(CProjectDescriptionPreferences other){
		if(isDefaultConfigurationRelations() != other.isDefaultConfigurationRelations())
			return false;

		if(getConfigurationRelations() != other.getConfigurationRelations())
			return false;

		return true;
	}
}
