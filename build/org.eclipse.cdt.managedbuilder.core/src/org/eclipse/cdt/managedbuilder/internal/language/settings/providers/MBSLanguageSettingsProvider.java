/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsBroadcastingProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * Implementation of language settings provider for CDT Managed Build System.
 */
public class MBSLanguageSettingsProvider extends AbstractExecutableExtensionBase implements ILanguageSettingsBroadcastingProvider {
	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {

		IPath projectPath = rc.getProjectRelativePath();
		ICLanguageSetting[] languageSettings = null;

		if (rc instanceof IFile) {
			ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(projectPath, true);
			if (ls != null) {
				languageSettings = new ICLanguageSetting[] { ls };
			} else {
				return getSettingEntries(cfgDescription, rc.getParent(), languageId);
			}
		} else {
			ICResourceDescription rcDescription = cfgDescription.getResourceDescription(projectPath, false);
			languageSettings = getLanguageSettings(rcDescription);
		}

		List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();

		if (languageSettings != null) {
			for (ICLanguageSetting langSetting : languageSettings) {
				if (langSetting != null) {
					String id = langSetting.getLanguageId();
					if (id != null && id.equals(languageId)) {
						int kindsBits = langSetting.getSupportedEntryKinds();
						for (int kind=1; kind <= kindsBits; kind <<= 1) {
							if ((kindsBits & kind) != 0) {
								list.addAll(langSetting.getSettingEntriesList(kind));
							}
						}
					}
				}
			}
		}
		return LanguageSettingsStorage.getPooledList(list);
	}

	/**
	 * Get language settings for resource description.
	 */
	private ICLanguageSetting[] getLanguageSettings(ICResourceDescription rcDescription) {
		ICLanguageSetting[] array = null;
		switch (rcDescription.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription)rcDescription;
			array = foDes.getLanguageSettings();
			break;
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription)rcDescription;
			ICLanguageSetting ls = fiDes.getLanguageSetting();
			if (ls != null) {
				array = new ICLanguageSetting[] { ls };
			}
		}
		if (array == null) {
			array = new ICLanguageSetting[0];
		}
		return array;
	}

	@Override
	public LanguageSettingsStorage copyStorage() {
		class PretendStorage extends LanguageSettingsStorage {
			@Override
			public boolean isEmpty() {
				return false;
			}
			@Override
			public LanguageSettingsStorage clone() throws CloneNotSupportedException {
				return this;
			}
			@Override
			public boolean equals(Object obj) {
				// Note that this always triggers change event even if nothing changed in MBS
				return false;
			}
		}
		return new PretendStorage();
	}

}
