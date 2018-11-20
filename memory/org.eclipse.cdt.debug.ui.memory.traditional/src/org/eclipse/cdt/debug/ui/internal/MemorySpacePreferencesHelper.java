/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Dumais (Ericsson) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.ui.memory.traditional.IMemorySpacePreferencesHelper;
import org.eclipse.cdt.debug.ui.memory.traditional.TraditionalRenderingMessages;
import org.eclipse.cdt.debug.ui.memory.traditional.TraditionalRenderingPlugin;
import org.eclipse.cdt.debug.ui.memory.traditional.TraditionalRenderingPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This class encapsulates the messy details of dealing with preferences
 * entries that have unpredictable key names. This is necessary because the
 * preference store does not allow getting a list of the keys, just a lookup
 * by exact key. So the work-around is to use one key to save a csv string,
 * containing the information necessary to reconstruct the keys for the
 * unpredictable entries.
 *
 * @since 1.4
 */
public class MemorySpacePreferencesHelper implements IMemorySpacePreferencesHelper {
	/** Reference to the plugin's preference store */
	private final IPreferenceStore fStore;

	// List of RGB colors that we can use, by default, for memory space backgrounds
	private static final String[] fColorPool = { "238,192,192", "250,238,195", "255,179,0", "122,245,0", "184,242,255",
			"166,189,215", "206,162,98", "245,138,157", "244,200,0", "255,136,56", "244,255,128" };

	/** Constructor */
	public MemorySpacePreferencesHelper() {
		fStore = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * @return an array of the currently known memory spaces ids, for which
	 * a background color preference was created.
	 */
	private String[] getMemorySpaceIds() {
		String csv = fStore.getString(TraditionalRenderingPreferenceConstants.MEM_KNOWN_MEMORY_SPACE_ID_LIST_CSV);
		return csv.isEmpty() ? new String[0] : csv.split(",");
	}

	/* (non-Javadoc)
	* @see org.eclipse.cdt.debug.ui.internal.IMemorySpacesPreferencesUtil#updateMemorySpaces(java.lang.String[])
	*/
	@Override
	public void updateMemorySpaces(String[] ids) {
		List<String> inputIdList = new ArrayList<>(Arrays.asList(ids));
		List<String> knownIdList = new ArrayList<>(Arrays.asList(getMemorySpaceIds()));
		int nextIdIndex = knownIdList.size();
		boolean newIds;

		// Remove ids already known
		inputIdList.removeAll(knownIdList);
		newIds = inputIdList.size() > 0 ? true : false;

		// remaining ids are new
		for (String id : inputIdList) {
			knownIdList.add(id);
			// set default color for this memory space id
			setDefaultColorPreference(id, nextIdIndex);
			nextIdIndex++;
		}
		// Save set of known memory space ids, if new ones were added
		if (newIds) {
			setMemorySpaceIds(knownIdList.toArray(new String[knownIdList.size()]));
		}
	}

	/**
	 * Saves a set of memory space ids, as a CSV string, into the
	 * preferences store
	 */
	private void setMemorySpaceIds(String[] memorySpaces) {
		StringBuilder csv = new StringBuilder();
		for (int i = 0; i < memorySpaces.length; i++) {
			csv.append(memorySpaces[i]);
			if (i < memorySpaces.length - 1) {
				csv.append(",");
			}
		}

		fStore.setValue(TraditionalRenderingPreferenceConstants.MEM_KNOWN_MEMORY_SPACE_ID_LIST_CSV, csv.toString());
	}

	/* (non-Javadoc)
	* @see org.eclipse.cdt.debug.ui.internal.IMemorySpacesPreferencesUtil#getMemorySpaceKey(java.lang.String)
	*/
	@Override
	public String getMemorySpaceKey(String id) {
		return TraditionalRenderingPreferenceConstants.MEM_MEMORY_SPACE_ID_PREFIX + id;
	}

	@Override
	public Map<String, String> getMemorySpaceLabels() {
		String prefix = TraditionalRenderingPreferenceConstants.MEM_MEMORY_SPACE_ID_PREFIX;
		String labelPrefix = TraditionalRenderingMessages
				.getString("TraditionalRenderingPreferencePage_BackgroundColorMemorySpacePrefix");
		String[] ids = getMemorySpaceIds();

		Map<String, String> keysToLabels = new HashMap<>();
		String key, label;
		for (int i = 0; i < ids.length; i++) {
			key = prefix + ids[i];
			label = labelPrefix + " " + ids[i];
			keysToLabels.put(key, label);
		}
		return keysToLabels;
	}

	@Override
	public Map<String, String> getMemorySpaceDefaultColors() {
		String prefix = TraditionalRenderingPreferenceConstants.MEM_MEMORY_SPACE_ID_PREFIX;
		String[] ids = getMemorySpaceIds();
		Map<String, String> mapKeyToColor = new HashMap<>();
		String key, color;
		for (int i = 0; i < ids.length; i++) {
			key = prefix + ids[i];
			color = getColor(i);
			mapKeyToColor.put(key, color);
		}
		return mapKeyToColor;
	}

	/** Adds a preference for a memory space id and assign it a unique color */
	private void setDefaultColorPreference(String id, int index) {
		String prefix = TraditionalRenderingPreferenceConstants.MEM_MEMORY_SPACE_ID_PREFIX;
		String key = prefix + id;
		fStore.setValue(key, getColor(index));
		// Setting the default here prevents not having a default defined at first.
		fStore.setDefault(key, getColor(index));
	}

	/**
	 * @return a csv string representation of a color. A color array is defined
	 *  in this class. The entry returned corresponds to the index parameter, and
	 *  wraps around if the index is greater than the number of defined colors
	 */
	private String getColor(int index) {
		// wrap-around if we have exhausted the pool
		if (index >= fColorPool.length) {
			index = index % (fColorPool.length);
		}
		return fColorPool[index];
	}

}
