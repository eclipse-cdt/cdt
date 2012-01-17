/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/


package org.eclipse.cdt.debug.ui.memory.traditional;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Class used to initialize default preference values.
 */
public class TraditionalRenderingPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault()
				.getPreferenceStore();
		
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_TEXT, true);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_BACKGROUND, true);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_SELECTION, true);
		
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED, "255,0,0");
		
		Color systemSelection = Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_SELECTION, systemSelection.getRed()
				+ "," + systemSelection.getGreen() + "," + systemSelection.getBlue());
		
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS, "5");
		
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT, "0,255,0");
		
		Color systemText = Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_TEXT, systemText.getRed()
				+ "," + systemText.getGreen() + "," + systemText.getBlue());
		
		Color systemBackground = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_COLOR_BACKGROUND, systemBackground.getRed()
				+ "," + systemBackground.getGreen() + "," + systemBackground.getBlue());
		
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE, 
				TraditionalRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE_ON_ENTER_ONLY);
		
		store.setDefault(TraditionalRenderingPreferenceConstants.MEM_HISTORY_TRAILS_COUNT, "1");
	}

}
