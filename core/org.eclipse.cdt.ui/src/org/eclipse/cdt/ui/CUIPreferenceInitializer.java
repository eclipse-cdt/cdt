/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.internal.ui.cview.CView;
import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CEditorPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CSearchPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.WorkInProgressPreferencePage;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

/**
 * This class implements the setting of the CUI initial preference store settings.
 */
public class CUIPreferenceInitializer extends AbstractPreferenceInitializer {

	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();

        PreferenceConstants.initializeDefaultValues(store);
        EditorsUI.useAnnotationsPreferencePage(store);
        EditorsUI.useAnnotationsPreferencePage(store);
		AbstractDecoratedTextEditorPreferenceConstants.initializeDefaultValues(store);	
		CPluginPreferencePage.initDefaults(store);
		BuildConsolePreferencePage.initDefaults(store);
		WorkInProgressPreferencePage.initDefaults(store);
		CSearchPreferencePage.initDefaults(store);
		CView.initDefaults(store);
		CEditorPreferencePage.initDefaults(store);
	}

}
