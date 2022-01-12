/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
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
package org.eclipse.cdt.core.settings.model;

import org.eclipse.cdt.core.settings.model.extension.impl.UserAndDiscoveredEntryConfigurationDataProvider;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.core.testplugin.CTestPlugin;

public class TestUserAndDiscoveredEntriesCfgDataProvider extends UserAndDiscoveredEntryConfigurationDataProvider {
	public static final String PROVIDER_ID = CTestPlugin.PLUGIN_ID + ".testUserAndDiscoveredCfgDataProvider";

	private static final KindBasedStore[] ENTRIES_STORES = new KindBasedStore[] { new KindBasedStore(false) };

	static {
		ICLanguageSettingEntry[] entries = new ICLanguageSettingEntry[4];
		entries[0] = new CIncludePathEntry("a/b/c", 0);
		entries[1] = new CIncludePathEntry("/d/e/f", 0);
		entries[2] = new CIncludePathEntry("g/h/i", ICSettingEntry.VALUE_WORKSPACE_PATH);
		entries[3] = new CIncludePathEntry("/j/k/l", ICSettingEntry.VALUE_WORKSPACE_PATH);

		ENTRIES_STORES[0].put(ICSettingEntry.INCLUDE_PATH, entries);

		entries = new ICLanguageSettingEntry[2];
		entries[0] = new CMacroEntry("a", "b", 0);
		entries[1] = new CMacroEntry("c", null, 0);
		ENTRIES_STORES[0].put(ICSettingEntry.MACRO, entries);
	}

	@Override
	protected ICLanguageSettingEntry[] getAllDiscoveredEntries(LanguageData data, int kind) {
		ICLanguageSettingEntry entries[] = (ICLanguageSettingEntry[]) ENTRIES_STORES[0].get(kind);
		return entries != null ? (ICLanguageSettingEntry[]) entries.clone() : new ICLanguageSettingEntry[0];
	}

}
