/*******************************************************************************
 * Copyright (c) 2006-2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.util.Map;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Class used to initialize default preference values.
 */
public class TraditionalRenderingPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();

		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_TEXT, true);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_BACKGROUND, true);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_SELECTION, true);

		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED, "255,0,0");
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED_ITALIC, false);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED_BOLD, false);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED_BOX, true);

		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT, "0,255,0");
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT_ITALIC, true);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT_BOLD, false);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT_BOX, true);

		Color systemSelection = Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_SELECTION,
				systemSelection.getRed() + "," + systemSelection.getGreen() + "," + systemSelection.getBlue());

		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS, "5");

		Color systemText = Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_TEXT,
				systemText.getRed() + "," + systemText.getGreen() + "," + systemText.getBlue());

		Color systemBackground = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_BACKGROUND,
				systemBackground.getRed() + "," + systemBackground.getGreen() + "," + systemBackground.getBlue());

		// Set the default background colors, for known memory spaces
		IMemorySpacePreferencesHelper util = TraditionalMemoryRenderingFactory.getMemorySpacesPreferencesHelper();
		Map<String, String> prefKeyToColor = util.getMemorySpaceDefaultColors();

		if (prefKeyToColor.size() > 0) {
			// If there are memory spaces present, set no global background as default
			store.setDefault(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_BACKGROUND, false);
			for (String key : prefKeyToColor.keySet()) {
				store.setDefault(key, prefKeyToColor.get(key));
			}
		}

		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE,
				TraditionalRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE_ON_ENTER_ONLY);

		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_HISTORY_TRAILS_COUNT, "1");

		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_CROSS_REFERENCE_INFO, true);
	}

}
