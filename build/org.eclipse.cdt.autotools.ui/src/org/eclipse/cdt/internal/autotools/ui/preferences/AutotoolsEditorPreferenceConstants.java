/*******************************************************************************
 * Copyright (c) 2002, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Red Hat Inc. - rename to use with Autotools editors
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * MakefileEditorPreferenceConstants
 */
public class AutotoolsEditorPreferenceConstants {

	/**
	 *
	 */
	private AutotoolsEditorPreferenceConstants() {
	}

	/**
	 * The symbolic names for colors for displaying code assist proposals
	 * @see org.eclipse.jface.resource.ColorRegistry
	 */
	public static final String CURRENT_LINE_COLOR = "org.eclipse.cdt.autotools.ui.automake.currentLineHightlightColor"; //$NON-NLS-1$
	public static final String LINE_NUMBER_RULER_COLOR = "org.eclipse.cdt.autotools.ui.automake.lineNumberForegroundColor"; //$NON-NLS-1$
	public static final String PRINT_MARGIN_COLOR = "org.eclipse.cdt.autotools.ui.automake.printMarginColor"; //$NON-NLS-1$

	/**
	 * Preference key suffix for bold text style preference keys.
	 *
	 */
	public static final String EDITOR_BOLD_SUFFIX = "_bold"; //$NON-NLS-1$

	/**
	 * Preference key suffix for italic text style preference keys.
	 */
	public static final String EDITOR_ITALIC_SUFFIX = "_italic"; //$NON-NLS-1$

	public static final String EDITOR_FOLDING_MACRODEF = "editor_folding_default_macrodef"; //$NON-NLS-1$

	public static final String EDITOR_FOLDING_RULE = "editor_folding_default_rule"; //$NON-NLS-1$

	public static final String EDITOR_FOLDING_CASE = "editor_folding_default_case"; //$NON-NLS-1$

	public static final String EDITOR_FOLDING_CONDITIONAL = "editor_folding_default_conditional"; //$NON-NLS-1$

	public static final String EDITOR_FOLDING_LOOP = "editor_folding_default_loop"; //$NON-NLS-1$

	public static final String EDITOR_FOLDING_ENABLED = "editor_folding_enabled"; //$NON-NLS-1$

	public static final String AUTOCONF_VERSION = "autoconf_version";

	public static final String AUTOMAKE_VERSION = "automake_version";

	public static void initializeDefaultValues(IPreferenceStore store) {

		store.setDefault(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_ENABLED, false);
		store.setDefault(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_MACRODEF, false);
		store.setDefault(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_RULE, true);
		store.setDefault(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_CASE, true);
		store.setDefault(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_CONDITIONAL, true);
		store.setDefault(AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_LOOP, true);
		store.setDefault(AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION,
				AutoconfEditorPreferencePage.LATEST_AC_VERSION);
		store.setDefault(AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION,
				AutoconfEditorPreferencePage.LATEST_AM_VERSION);
	}

}
