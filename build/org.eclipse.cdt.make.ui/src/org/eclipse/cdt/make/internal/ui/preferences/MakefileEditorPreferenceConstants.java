/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.make.internal.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.editors.text.TextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * MakefileEditorPreferenceConstants
 */
public class MakefileEditorPreferenceConstants {

	/**
	 * 
	 */
	private MakefileEditorPreferenceConstants() {
	}

	/**
	 * The symbolic names for colors for displaying code assist proposals
	 * @see org.eclipse.jface.resource.ColorRegistry
	 */
	public static final String CURRENT_LINE_COLOR = "org.eclipse.cdt.make.ui.currentLineHightlightColor"; //$NON-NLS-1$
	public static final String LINE_NUMBER_RULER_COLOR = "org.eclipse.cdt.make.ui.lineNumberForegroundColor"; //$NON-NLS-1$
	public static final String PRINT_MARGIN_COLOR = "org.eclipse.cdt.make.ui.printMarginColor"; //$NON-NLS-1$	
	
	/**
	 * Preference key suffix for bold text style preference keys.
	 * 
	 */
	public static final String EDITOR_BOLD_SUFFIX= "_bold"; //$NON-NLS-1$

	/**
	 * Preference key suffix for italic text style preference keys.
	 */
	public static final String EDITOR_ITALIC_SUFFIX= "_italic"; //$NON-NLS-1$


	public static final String EDITOR_FOLDING_MACRODEF = "editor_folding_default_macrodef"; //$NON-NLS-1$


	public static final String EDITOR_FOLDING_RULE = "editor_folding_default_rule"; //$NON-NLS-1$


	public static final String EDITOR_FOLDING_CONDITIONAL = "editor_folding_default_conditional"; //$NON-NLS-1$

	public static final String EDITOR_FOLDING_ENABLED = "editor_folding_enabled"; //$NON-NLS-1$;
	

	public static void initializeDefaultValues(IPreferenceStore store) {
		TextEditorPreferenceConstants.initializeDefaultValues(store);
		store.setDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, true);

		store.setDefault(MakefileEditorPreferenceConstants.EDITOR_FOLDING_ENABLED, false);
		store.setDefault(MakefileEditorPreferenceConstants.EDITOR_FOLDING_MACRODEF, false);
		store.setDefault(MakefileEditorPreferenceConstants.EDITOR_FOLDING_RULE, true);
		store.setDefault(MakefileEditorPreferenceConstants.EDITOR_FOLDING_CONDITIONAL, true);
	}

}
