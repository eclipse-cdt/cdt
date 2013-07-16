/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Martin Oberhuber (Wind River Systems) - bug 155096
 *******************************************************************************/
package org.eclipse.cdt.thirdParty.tests.languageSettingsProviders;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuildCommandParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

public class ThirdPartyBuildOutputParser extends GCCBuildCommandParser {

	@Override
	public ThirdPartyBuildOutputParser cloneShallow() throws CloneNotSupportedException {
		return (ThirdPartyBuildOutputParser) super.cloneShallow();
	}

	@Override
	public ThirdPartyBuildOutputParser clone() throws CloneNotSupportedException {
		return (ThirdPartyBuildOutputParser) super.clone();
	}
	
	// Overridden to provide cumulated entries on folder and project level
	@Override
	protected void setSettingEntries(List<ICLanguageSettingEntry> entries) {
		IResource rc = null;
		switch (getResourceScope()) {
		case FILE:
			rc = currentResource;
			break;
		case FOLDER:
			if (currentResource instanceof IFile) {
				rc = currentResource.getParent();
			}
			break;
		case PROJECT:
			if (currentResource != null) {
				rc = currentResource.getProject();
			}
			break;
		default:
			break;
		}

		List<ICLanguageSettingEntry> settingEntries = getSettingEntries(currentCfgDescription, rc, currentLanguageId);
		ArrayList<ICLanguageSettingEntry> cumulatedEntries = new ArrayList<ICLanguageSettingEntry>();
		if(settingEntries != null) {
			cumulatedEntries.addAll(settingEntries);
		}
		if(entries != null) {
			for (ICLanguageSettingEntry entry : entries) {
				if(!cumulatedEntries.contains(entry)) {
					cumulatedEntries.add(entry);
				}
			}
		}
		setSettingEntries(currentCfgDescription, rc, currentLanguageId, cumulatedEntries);
	}
}