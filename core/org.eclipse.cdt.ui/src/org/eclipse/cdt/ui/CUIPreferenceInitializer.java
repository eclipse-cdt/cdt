/*******************************************************************************
 *  Copyright (c) 2003, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     QNX Software Systems - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.cdt.internal.ui.ICThemeConstants;
import org.eclipse.cdt.internal.ui.cview.CView;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CEditorPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.CodeAssistPreferencePage;
import org.eclipse.cdt.internal.ui.preferences.WorkInProgressPreferencePage;

/**
 * This class implements the setting of the CUI initial preference store settings.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CUIPreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();

        PreferenceConstants.initializeDefaultValues(store);
		CPluginPreferencePage.initDefaults(store);
		BuildConsolePreferencePage.initDefaults(store);
		CView.initDefaults(store);
		CEditorPreferencePage.initDefaults(store);
		CodeAssistPreferencePage.initDefaults(store);
		SemanticHighlightings.initDefaults(store);
		WorkInProgressPreferencePage.initDefaults(store);

		// We need to do this remove any keys that might have been
		// in the CUIPlugin store prior to the move of the CEditor setting
		// All of those settings are now in the workbench "All TextEditor" preference Page.
		// Later we should remove this calls, after CDT-3.0
		EditorsUI.useAnnotationsPreferencePage(store);
        EditorsUI.useQuickDiffPreferencePage(store);
		useTextEditorPreferencePage(store);
	}

	/*
	 * Reset to default, those constants that are no longer maintained in CUIPlugin store.
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
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SMART_HOME_END);
	}

	static void setThemeBasedPreferences(IPreferenceStore store, boolean fireEvent) {
		ColorRegistry registry= null;
		if (PlatformUI.isWorkbenchRunning())
			registry= PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();

		setDefault(
				store,
				PreferenceConstants.EDITOR_MULTI_LINE_COMMENT_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_MULTI_LINE_COMMENT_COLOR, new RGB(63, 127, 95)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR, new RGB(63, 127, 95)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_TASK_TAG_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_TASK_TAG_COLOR, new RGB(127, 159, 191)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_C_KEYWORD_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_C_KEYWORD_COLOR, new RGB(127, 0, 85)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_C_BUILTIN_TYPE_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_C_BUILTIN_TYPE_COLOR, new RGB(127, 0, 85)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_C_STRING_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_C_STRING_COLOR, new RGB(42, 0, 255)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_C_DEFAULT_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_C_DEFAULT_COLOR, new RGB(0, 0, 0)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_C_OPERATOR_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_C_OPERATOR_COLOR, new RGB(0, 0, 0)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_C_BRACES_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_C_BRACES_COLOR, new RGB(0, 0, 0)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_C_NUMBER_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_C_NUMBER_COLOR, new RGB(0, 0, 0)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_PP_DIRECTIVE_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_PP_DIRECTIVE_COLOR, new RGB(127, 0, 85)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_PP_HEADER_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_PP_HEADER_COLOR, new RGB(42, 0, 255)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_PP_DEFAULT_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_PP_DEFAULT_COLOR, new RGB(0, 0, 0)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_ASM_LABEL_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_ASM_LABEL_COLOR, new RGB(127, 0, 85)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.EDITOR_ASM_DIRECTIVE_COLOR,
				findRGB(registry, ICThemeConstants.EDITOR_ASM_DIRECTIVE_COLOR, new RGB(127, 0, 85)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND,
				findRGB(registry, ICThemeConstants.CODEASSIST_PROPOSALS_BACKGROUND, new RGB(255, 255, 255)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND,
				findRGB(registry, ICThemeConstants.CODEASSIST_PROPOSALS_FOREGROUND, new RGB(0, 0, 0)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND,
				findRGB(registry, ICThemeConstants.CODEASSIST_PARAMETERS_BACKGROUND, new RGB(255, 255, 255)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND,
				findRGB(registry, ICThemeConstants.CODEASSIST_PARAMETERS_FOREGROUND, new RGB(0, 0, 0)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.DOXYGEN_MULTI_LINE_COLOR,
				findRGB(registry, ICThemeConstants.DOXYGEN_MULTI_LINE_COLOR, new RGB(63, 95, 191)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.DOXYGEN_SINGLE_LINE_COLOR,
				findRGB(registry, ICThemeConstants.DOXYGEN_SINGLE_LINE_COLOR, new RGB(63, 95, 191)), fireEvent);
		setDefault(
				store,
				PreferenceConstants.DOXYGEN_TAG_COLOR,
				findRGB(registry, ICThemeConstants.DOXYGEN_TAG_COLOR, new RGB(127, 159, 191)), fireEvent);
	}

	/**
	 * Sets the default value and fires a property
	 * change event if necessary.
	 *
	 * @param store	the preference store
	 * @param key the preference key
	 * @param newValue the new value
	 * @param fireEvent <code>false</code> if no event should be fired
	 * @since 5.4
	 */
	private static void setDefault(IPreferenceStore store, String key, RGB newValue, boolean fireEvent) {
		if (!fireEvent) {
			PreferenceConverter.setDefault(store, key, newValue);
			return;
		}

		RGB oldValue= null;
		if (store.isDefault(key))
			oldValue= PreferenceConverter.getDefaultColor(store, key);

		PreferenceConverter.setDefault(store, key, newValue);

		if (oldValue != null && !oldValue.equals(newValue))
			store.firePropertyChangeEvent(key, oldValue, newValue);
	}

	/**
	 * Returns the RGB for the given key in the given color registry.
	 *
	 * @param registry the color registry
	 * @param key the key for the constant in the registry
	 * @param defaultRGB the default RGB if no entry is found
	 * @return RGB the RGB
	 * @since 5.4
	 */
	private static RGB findRGB(ColorRegistry registry, String key, RGB defaultRGB) {
		if (registry == null)
			return defaultRGB;

		RGB rgb= registry.getRGB(key);
		if (rgb != null)
			return rgb;

		return defaultRGB;
	}
}
