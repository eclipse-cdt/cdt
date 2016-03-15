/*******************************************************************************
 * Copyright (c) 2013, 2013 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;

/**
 * Language settings provider to provide entries exported from referenced
 * projects.
 */
public class ReferencedProjectsLanguageSettingsProvider extends LanguageSettingsBaseProvider {
	/** ID of the provider used in extension point from plugin.xml */
	public static final String ID = "org.eclipse.cdt.core.ReferencedProjectsLanguageSettingsProvider"; //$NON-NLS-1$

	final private ThreadLocal<Boolean> recursiveCallIndicator = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	@Override
	public List<ICLanguageSettingEntry> getSettingEntries(IBuildConfiguration config, IResource rc,
			String languageId) {
		if (recursiveCallIndicator.get()) {
			// Recursive call indicates that the provider of a referenced
			// project is called.
			// Only exported entries of the original configuration should be
			// considered,
			// entries of referenced projects are not re-exported.
			return null;
		}

		if (config == null) {
			return null;
		}

		ICConfigurationDescription cfgDescription = config.getAdapter(ICConfigurationDescription.class);
		if (cfgDescription == null) {
			return null;
		}
		ICProjectDescription prjDescription = cfgDescription.getProjectDescription();
		if (prjDescription == null) {
			return null;
		}

		try {
			recursiveCallIndicator.set(true);
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			ICConfigurationDescription[] refCfgDescriptions = CoreModelUtil
					.getReferencedConfigurationDescriptions(cfgDescription, false);
			for (ICConfigurationDescription refCfgDescription : refCfgDescriptions) {
				IBuildConfiguration refConfig = Adapters.adapt(refCfgDescription, IBuildConfiguration.class);
				List<ICLanguageSettingEntry> refEntries = LanguageSettingsManager
						.getSettingEntriesByKind(refConfig, rc, languageId, ICSettingEntry.ALL);
				for (ICLanguageSettingEntry refEntry : refEntries) {
					int flags = refEntry.getFlags();
					if ((flags & ICSettingEntry.EXPORTED) == ICSettingEntry.EXPORTED) {
						// create a new entry with EXPORTED flag cleared
						ICLanguageSettingEntry entry = CDataUtil.createEntry(refEntry,
								flags & ~ICSettingEntry.EXPORTED);
						entries.add(entry);
					}
				}
			}

			return LanguageSettingsStorage.getPooledList(new ArrayList<ICLanguageSettingEntry>(entries));
		} finally {
			recursiveCallIndicator.set(false);
		}
	}
}
