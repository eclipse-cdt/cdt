/*******************************************************************************
 * Copyright (c) 2007, 2018 Intel Corporation and others.
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
 * Christian Walther (Indel AG) - [335344] changing language IDs
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.EntryContentsKey;
import org.eclipse.cdt.core.settings.model.util.KindBasedStore;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettinsDeltaCalculator.ExtSettingsDelta;
import org.eclipse.core.runtime.CoreException;

/**
 * Responsible for applying external settings delta to a given ICConfigurationDescrptions
 */
public class CExternalSettingsDeltaProcessor {

	/**
	 * Main entrance point for applying a full array of external settings delta
	 * @param des ICConfigurationDescription
	 * @param deltas ExtSettingsDelta array
	 * @return boolean indicating whether there was change
	 */
	static boolean applyDelta(ICConfigurationDescription des, ExtSettingsDelta deltas[]) {
		return applyDelta(des, deltas, KindBasedStore.ORED_ALL_ENTRY_KINDS);
	}

	/**
	 * Applies the deltas to all resource description (overriden resource configs)
	 * in the configuration description
	 * @param des The configuration description to be updated
	 * @param deltas deltas to be applied
	 * @param kindMask
	 * @return
	 */
	static boolean applyDelta(ICConfigurationDescription des, ExtSettingsDelta deltas[], int kindMask) {
		ICResourceDescription rcDess[] = des.getResourceDescriptions();
		boolean changed = false;
		for (ICResourceDescription rcDes : rcDess) {
			if (applyDelta(rcDes, deltas, kindMask))
				changed = true;
		}

		if ((kindMask & ICSettingEntry.SOURCE_PATH) != 0) {
			if (applySourceEntriesChange(des, deltas))
				changed = true;
		}
		if ((kindMask & ICSettingEntry.OUTPUT_PATH) != 0) {
			if (applyOutputEntriesChange(des, deltas))
				changed = true;
		}

		return changed;
	}

	static boolean applySourceEntriesChange(ICConfigurationDescription cfgDes, ExtSettingsDelta[] deltas) {
		ICSettingEntry[][] diff = CExternalSettinsDeltaCalculator.getAllEntries(deltas, ICSettingEntry.SOURCE_PATH);
		if (diff == null)
			return false;

		ICSourceEntry[] current = cfgDes.getSourceEntries();
		if (current.length == 1) {
			ICSourceEntry cur = current[0];
			if (cur.getFullPath().segmentCount() == 1 && cur.getExclusionPatterns().length == 0) {
				current = new ICSourceEntry[0];
			}
		}
		List<ICSourceEntry> newEntries = calculateUpdatedEntries(current, diff[0], diff[1]);
		if (newEntries != null) {
			try {
				cfgDes.setSourceEntries(newEntries.toArray(new ICSourceEntry[newEntries.size()]));
			} catch (WriteAccessException e) {
				CCorePlugin.log(e);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			return true;
		}
		return false;
	}

	static boolean applyOutputEntriesChange(ICConfigurationDescription cfgDes, ExtSettingsDelta[] deltas) {
		ICSettingEntry[][] diff = CExternalSettinsDeltaCalculator.getAllEntries(deltas, ICSettingEntry.OUTPUT_PATH);
		if (diff == null)
			return false;

		ICBuildSetting bs = cfgDes.getBuildSetting();
		if (bs == null)
			return false;

		ICOutputEntry[] current = bs.getOutputDirectories();
		if (current.length == 1) {
			ICOutputEntry cur = current[0];
			if (cur.getFullPath().segmentCount() == 1 && cur.getExclusionPatterns().length == 0) {
				current = new ICOutputEntry[0];
			}
		}

		List<ICOutputEntry> newEntries = calculateUpdatedEntries(current, diff[0], diff[1]);
		if (newEntries != null) {
			try {
				bs.setOutputDirectories(newEntries.toArray(new ICOutputEntry[newEntries.size()]));
			} catch (WriteAccessException e) {
				CCorePlugin.log(e);
			}
			return true;
		}
		return false;
	}

	static boolean applyDelta(ICResourceDescription rcDes, ExtSettingsDelta deltas[], int kindMask) {
		if (rcDes.getType() == ICSettingBase.SETTING_FOLDER) {
			return applyDelta((ICFolderDescription) rcDes, deltas, kindMask);
		}
		return applyDelta((ICFileDescription) rcDes, deltas, kindMask);
	}

	static boolean applyDelta(ICFileDescription des, ExtSettingsDelta deltas[], int kindMask) {
		ICLanguageSetting setting = des.getLanguageSetting();
		if (setting == null)
			return false;
		return applyDelta(setting, deltas, kindMask);
	}

	static boolean applyDelta(ICFolderDescription des, ExtSettingsDelta deltas[], int kindMask) {
		ICLanguageSetting settings[] = des.getLanguageSettings();
		if (settings == null || settings.length == 0)
			return false;

		boolean changed = false;
		for (ICLanguageSetting setting : settings) {
			if (applyDelta(setting, deltas, kindMask))
				changed = true;
		}
		return changed;
	}

	static boolean applyDelta(ICLanguageSetting setting, ExtSettingsDelta[] deltas, int kindMask) {
		boolean changed = false;
		// apply removals before additions in case several deltas apply to the same setting
		for (ExtSettingsDelta delta : deltas) {
			if (isSettingCompatible(setting, delta.fSetting)) {
				if (applyDelta(setting, delta, kindMask, false, true))
					changed = true;
			}
		}
		for (ExtSettingsDelta delta : deltas) {
			if (isSettingCompatible(setting, delta.fSetting)) {
				if (applyDelta(setting, delta, kindMask, true, false))
					changed = true;
			}
		}
		return changed;
	}

	static boolean applyDelta(ICLanguageSetting setting, ExtSettingsDelta delta, int kindMask, boolean additions,
			boolean removals) {
		int kinds[] = KindBasedStore.getLanguageEntryKinds();
		ICLanguageSettingEntry entries[];
		ICSettingEntry diff[][];
		boolean changed = false;
		for (int kind : kinds) {
			if ((kind & kindMask) == 0)
				continue;

			diff = delta.getEntriesDelta(kind);
			if (diff == null)
				continue;

			entries = setting.getSettingEntries(kind);
			List<ICLanguageSettingEntry> list = calculateUpdatedEntries(entries, additions ? diff[0] : null,
					removals ? diff[1] : null);

			if (list != null) {
				setting.setSettingEntries(kind, list);
				changed = true;
			}
		}
		return changed;
	}

	private static <T extends ICSettingEntry> List<T> calculateUpdatedEntries(T current[], ICSettingEntry added[],
			ICSettingEntry removed[]) {
		LinkedHashMap<EntryContentsKey, ICSettingEntry> map = new LinkedHashMap<>();
		boolean changed = false;
		if (added != null) {
			CDataUtil.fillEntriesMapByContentsKey(map, added);
		}
		if (current != null) {
			CDataUtil.fillEntriesMapByContentsKey(map, current);
			if (current.length != map.size()) {
				changed = true;
			}
		} else {
			if (map.size() != 0) {
				changed = true;
			}
		}
		if (removed != null) {
			for (ICSettingEntry entry : removed) {
				EntryContentsKey cKey = new EntryContentsKey(entry);
				ICSettingEntry cur = map.get(cKey);
				if (cur != null && !cur.isBuiltIn()) {
					map.remove(cKey);
					changed = true;
				}
			}
		}
		@SuppressWarnings("unchecked")
		Collection<T> values = (Collection<T>) map.values();
		return changed ? new ArrayList<>(values) : null;
	}

	private static boolean isSettingCompatible(ICLanguageSetting setting, CExternalSetting provider) {
		String ids[] = provider.getCompatibleLanguageIds();
		if (ids != null && ids.length > 0) {
			String id = setting.getLanguageId();
			if (id != null) {
				if (contains(ids, id))
					return true;
				return false;
			}
			return false;
		}

		ids = provider.getCompatibleContentTypeIds();
		if (ids != null && ids.length > 0) {
			String[] cTypeIds = setting.getSourceContentTypeIds();
			if (cTypeIds.length != 0) {
				for (String id : cTypeIds) {
					if (contains(ids, id))
						return true;
				}
				return false;
			}
			return false;
		}

		ids = provider.getCompatibleExtensions();
		if (ids != null && ids.length > 0) {
			String[] srcIds = setting.getSourceExtensions();
			if (srcIds.length != 0) {
				for (String id : srcIds) {
					if (contains(ids, id))
						return true;
				}
				return false;
			}
			return false;
		}
		return true;
	}

	private static boolean contains(Object array[], Object value) {
		for (Object element : array) {
			if (element.equals(value))
				return true;
		}
		return false;
	}
}
