/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

// Note: this class should be removed from public API.  See bug 246004
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		
		IPreferenceStore prefs = DsfUIPlugin.getDefault().getPreferenceStore();
		
		/*
		 *  Common to all views.
		 */
		PreferenceConverter.setDefault(prefs, IDebugUIConstants.PREF_CHANGED_DEBUG_ELEMENT_COLOR, new RGB(255, 0, 0));
		prefs.setDefault(IDsfDebugUIConstants.PREF_DETAIL_PANE_WORD_WRAP, false);
		prefs.setDefault(IDsfDebugUIConstants.PREF_MAX_DETAIL_LENGTH, 10000);
		
		/*
		 * Variables view
		 */
		prefs.setDefault(IDsfDebugUIConstants.VARIABLES_DETAIL_PANE_ORIENTATION, IDsfDebugUIConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		
		/*
		 * Registers View
		 */
		prefs.setDefault(IDsfDebugUIConstants.REGISTERS_DETAIL_PANE_ORIENTATION, IDsfDebugUIConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		
		/*
		 * Expressions View
		 */
		prefs.setDefault(IDsfDebugUIConstants.EXPRESSIONS_DETAIL_PANE_ORIENTATION, IDsfDebugUIConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);

		/* 
		 * Debug View
		 */
		prefs.setDefault(IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT, 10);
		prefs.setDefault(IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT_ENABLE, true);
		prefs.setDefault(IDsfDebugUIConstants.PREF_WAIT_FOR_VIEW_UPDATE_AFTER_STEP_ENABLE, false);
		prefs.setDefault(IDsfDebugUIConstants.PREF_MIN_STEP_INTERVAL, 100);
	}
}
