/*******************************************************************************
 * Copyright (c) 2006, 2010, 2012 Wind River Systems, Inc. and others.
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
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.floatingpoint;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Class used to initialize default preference values.
 */
public class FPRenderingPreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();

		store.setDefault(FPRenderingPreferenceConstants.MEM_USE_GLOBAL_TEXT, true);
		store.setDefault(FPRenderingPreferenceConstants.MEM_USE_GLOBAL_BACKGROUND, true);
		store.setDefault(FPRenderingPreferenceConstants.MEM_USE_GLOBAL_SELECTION, true);

		store.setDefault(FPRenderingPreferenceConstants.MEM_COLOR_CHANGED, "255,0,0"); //$NON-NLS-1$

		Color systemSelection = Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);
		store.setDefault(FPRenderingPreferenceConstants.MEM_COLOR_SELECTION,
				systemSelection.getRed() + "," + systemSelection.getGreen() + "," //$NON-NLS-1$ //$NON-NLS-2$
						+ systemSelection.getBlue());

		store.setDefault(FPRenderingPreferenceConstants.MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS, "5"); //$NON-NLS-1$

		store.setDefault(FPRenderingPreferenceConstants.MEM_COLOR_EDIT, "0,255,0"); //$NON-NLS-1$

		Color systemText = Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		store.setDefault(FPRenderingPreferenceConstants.MEM_COLOR_TEXT,
				systemText.getRed() + "," + systemText.getGreen() + "," + systemText.getBlue()); //$NON-NLS-1$ //$NON-NLS-2$

		Color systemBackground = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		store.setDefault(FPRenderingPreferenceConstants.MEM_COLOR_BACKGROUND,
				systemBackground.getRed() + "," + systemBackground.getGreen() + "," //$NON-NLS-1$ //$NON-NLS-2$
						+ systemBackground.getBlue());

		store.setDefault(FPRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE,
				FPRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE_ON_ENTER_ONLY);

		store.setDefault(FPRenderingPreferenceConstants.MEM_HISTORY_TRAILS_COUNT, "1"); //$NON-NLS-1$
	}
}
