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
import org.eclipse.cdt.internal.ui.preferences.CParserPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CodeAssistPreferencePage;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

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
		CPluginPreferencePage.initDefaults(store);
		BuildConsolePreferencePage.initDefaults(store);
		CView.initDefaults(store);
		CParserPreferencePage.initDefaults(store);
		CEditorPreferencePage.initDefaults(store);
		CodeAssistPreferencePage.initDefaults(store);

		// We need to do this remove any keys that might have been
		// in the CUIPlugin store prior to the move of the CEditor setting
		// All of those settings are now in the workbench "All TextEditor" preference Page.
		// Later we should remove this calls, after CDT-3.0
		EditorsUI.useAnnotationsPreferencePage(store);
        EditorsUI.useQuickDiffPreferencePage(store);
		useTextEditorPreferencePage(store);
	}

	/*
	 * reset to default, those constants are no longer maintain int CUIPlugin store.
	 */
	public static void useTextEditorPreferencePage(IPreferenceStore store) {
		
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR);

		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER);	
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER);

		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);

		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN);
		
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET);
		
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR);
		store.setToDefault(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND);
		store.setToDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
		store.setToDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		store.setToDefault(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT);

		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_DISABLE_OVERWRITE_MODE);
	}

}
