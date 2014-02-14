/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.util;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;

public final class ResourceConfigurationUtil {
	
	/**
	 * Returns whether or not the resource is customized for this configuration. More specifically, a resource is customized if
	 * it has a resource description (non-null) or if it has different language settings than its parent.
	 * 
	 * @param cfgDescription
	 *            the configuration description
	 * @param res
	 *            the resource
	 * @return true if the resource is customized, false otherwise
	 */
	public static boolean isCustomizedResource(ICConfigurationDescription cfgDescription, IResource res) {
		if (ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(res.getProject())) {
			if (cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
				IContainer parent = res.getParent();
				List<String> languages = LanguageSettingsManager.getLanguages(res, cfgDescription);
				for (ILanguageSettingsProvider provider: ((ILanguageSettingsProvidersKeeper) cfgDescription).getLanguageSettingProviders()) {
					for (String languageId : languages) {
						List<ICLanguageSettingEntry> list = provider.getSettingEntries(cfgDescription, res, languageId);
						if (list != null) {
							List<ICLanguageSettingEntry> listDefault = provider.getSettingEntries(cfgDescription, parent, languageId);
							// != is OK here due as the equal lists will have the same reference in WeakHashSet
							if (list != listDefault)
								return true;
						}
					}
				}
			}
		}

		ICResourceDescription rcDescription = cfgDescription.getResourceDescription(res.getProjectRelativePath(), true);
		return rcDescription != null;
	}
}
