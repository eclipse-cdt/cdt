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

package org.eclipse.cdt.core.language.settings.providers;

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * This temporary class keeps the utility methods being looking for better home
 */
public class LanguageSettingsManager_TBD {
	public static boolean isCustomizedResource(ICConfigurationDescription cfgDescription, IResource rc) {
		if (rc instanceof IProject)
			return false;

		for (ILanguageSettingsProvider provider: cfgDescription.getLanguageSettingProviders()) {
			if (provider instanceof ILanguageSettingsBroadcastingProvider) {
				for (String languageId : LanguageSettingsManager.getLanguages(rc, cfgDescription)) {
					List<ICLanguageSettingEntry> list = provider.getSettingEntries(cfgDescription, rc, languageId);
					if (list!=null) {
						// TODO - check default or check parent?
						List<ICLanguageSettingEntry> listDefault = provider.getSettingEntries(null, null, languageId);
						// != is OK here due as the equal lists will have the same reference in WeakHashSet
						if (list != listDefault)
							return true;
					}
				}
			}
		}

		return false;
	}

	public static boolean isReconfigured(ILanguageSettingsProvider provider) {
		if (provider instanceof ILanguageSettingsEditableProvider) {
			try {
				return ! LanguageSettingsExtensionManager.equalsExtensionProviderShallow((ILanguageSettingsEditableProvider) provider);
			} catch (Exception e) {
				CCorePlugin.log("Internal Error: cannot clone provider "+provider.getId(), e);
			}
		}
		return false;
	}

	public static boolean isEqualExtensionProvider(ILanguageSettingsProvider provider) {
		return LanguageSettingsExtensionManager.equalsExtensionProvider(provider);
	}
}
