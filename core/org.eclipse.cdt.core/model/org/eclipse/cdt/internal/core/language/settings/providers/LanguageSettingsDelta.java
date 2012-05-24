/*******************************************************************************
 * Copyright (c) 2011, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsStorage;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;


/**
 * Contains the delta of changes that occurred as a result of modifying
 * language settings entries {@link ICLanguageSettingEntry}. The delta is
 * associated with a configuration description.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class interface is not stable yet as
 * it is not currently clear how it may need to be used in future. Only bare
 * minimum is provided here at this point (CDT 8.1, Juno).
 * There is no guarantee that this API will work or that it will remain the same.
 * Please do not use this API without consulting with the CDT team.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class LanguageSettingsDelta {
	// maps are ordered by providers
	private LinkedHashMap<String/*providerId*/, LanguageSettingsStorage> oldLanguageSettingsState;
	private LinkedHashMap<String/*providerId*/, LanguageSettingsStorage> newLanguageSettingsState;

	private Set<String> paths = null;

	/**
	 * Constructor.
	 *
	 * @param oldState - old language settings storage state.
	 * @param newState - new language settings storage state.
	 */
	public LanguageSettingsDelta(LinkedHashMap<String, LanguageSettingsStorage> oldState, LinkedHashMap<String, LanguageSettingsStorage> newState) {
		oldLanguageSettingsState = oldState;
		newLanguageSettingsState = newState;
	}

	/**
	 * @return resource paths affected by changes represented by this delta.
	 */
	public Set<String> getAffectedResourcePaths() {
		if (paths != null) {
			return paths;
		}

		paths = new TreeSet<String>();

		LanguageSettingsStorage oldCombinedStorage = combineStorage(oldLanguageSettingsState);
		LanguageSettingsStorage newCombinedStorage = combineStorage(newLanguageSettingsState);

		for (String lang : oldCombinedStorage.getLanguages()) {
			for (String path : oldCombinedStorage.getResourcePaths(lang)) {
				if (oldCombinedStorage.getSettingEntries(path, lang) != newCombinedStorage.getSettingEntries(path, lang)) {
					if (path == null) {
						// add path of the project
						path = ""; //$NON-NLS-1$
					}
					paths.add(path);
				}
			}
		}

		for (String lang : newCombinedStorage.getLanguages()) {
			for (String path : newCombinedStorage.getResourcePaths(lang)) {
				if (newCombinedStorage.getSettingEntries(path, lang) != oldCombinedStorage.getSettingEntries(path, lang)) {
					if (path == null) {
						// add path of the project
						path = ""; //$NON-NLS-1$
					}
					paths.add(path);
				}
			}
		}

		return paths;
	}

	/**
	 * Language settings entries from different providers can overlap. This method resolves all overlapping
	 * ones combining entries into one aggregate storage.
	 */
	private LanguageSettingsStorage combineStorage(LinkedHashMap<String, LanguageSettingsStorage> state) {
		LanguageSettingsStorage combinedStore = new LanguageSettingsStorage();
		for (LanguageSettingsStorage providerStore : state.values()) {
			for (String lang : providerStore.getLanguages()) {
				for (String path : providerStore.getResourcePaths(lang)) {
					// provider (store) higher on the list overrides others below
					if (combinedStore.getSettingEntries(path, lang) == null) {
						List<ICLanguageSettingEntry> entries = providerStore.getSettingEntries(path, lang);
						combinedStore.setSettingEntries(path, lang, entries);
					}
				}
			}
		}

		return combinedStore;
	}
}
