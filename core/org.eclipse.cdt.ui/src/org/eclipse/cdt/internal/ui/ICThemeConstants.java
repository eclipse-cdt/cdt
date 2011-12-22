/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

/**
 * Defines the constants used in the <code>org.eclipse.ui.themes</code>
 * extension contributed by this plug-in.
 */
public interface ICThemeConstants {
	String ID_PREFIX= CUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	/**
	 * A theme constant that holds the background color used in the code assist selection dialog.
	 */
	public final String CODEASSIST_PROPOSALS_BACKGROUND= ID_PREFIX + PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND;

	/**
	 * A theme constant that holds the foreground color used in the code assist selection dialog.
	 */
	public final String CODEASSIST_PROPOSALS_FOREGROUND= ID_PREFIX + PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND;

	/**
	 * A theme constant that holds the background color used for parameter hints.
	 */
	public final String CODEASSIST_PARAMETERS_BACKGROUND= ID_PREFIX + PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND;

	/**
	 * A theme constant that holds the foreground color used for parameter hints.
	 */
	public final String CODEASSIST_PARAMETERS_FOREGROUND= ID_PREFIX + PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND;

	/**
	 * A theme constant that holds the foreground color used for multi-line comments.
	 */
	public final String EDITOR_MULTI_LINE_COMMENT_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_MULTI_LINE_COMMENT_COLOR;

	/**
	 * A theme constant that holds the foreground color used for single-line comments.
	 */
	public final String EDITOR_SINGLE_LINE_COMMENT_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR;

	/**
	 * A theme constant that holds the foreground color used for C/C++ keywords.
	 */
	public final String EDITOR_C_KEYWORD_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_C_KEYWORD_COLOR;

	/**
	 * A theme constant that holds the foreground color used for preprocessor directives.
	 */
	public final String EDITOR_PP_DIRECTIVE_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_PP_DIRECTIVE_COLOR;

	/**
	 * A theme constant that holds the foreground color used for preprocessor headers (includes).
	 */
	public final String EDITOR_PP_HEADER_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_PP_HEADER_COLOR;

	/**
	 * A theme constant that holds the foreground color used for preprocessor text.
	 */
	public final String EDITOR_PP_DEFAULT_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_PP_DEFAULT_COLOR;

	/**
	 * A theme constant that holds the foreground color used for C/C++ built-in types.
	 */
	public final String EDITOR_C_BUILTIN_TYPE_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_C_BUILTIN_TYPE_COLOR;

	/**
	 * A theme constant that holds the foreground color used for string constants.
	 */
	public final String EDITOR_C_STRING_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_C_STRING_COLOR;

	/**
	 * A theme constant that holds the foreground color used for operators.
	 */
	public final String EDITOR_C_OPERATOR_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_C_OPERATOR_COLOR;

	/**
	 * A theme constant that holds the foreground color used for numbers.
	 */
	public final String EDITOR_C_NUMBER_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_C_NUMBER_COLOR;

	/**
	 * A theme constant that holds the foreground color used for braces.
	 */
	public final String EDITOR_C_BRACES_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_C_BRACES_COLOR;

	/**
	 * A theme constant that holds the foreground color used for C/C++ code.
	 */
	public final String EDITOR_C_DEFAULT_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_C_DEFAULT_COLOR;

	/**
	 * A theme constant that holds the foreground color used for assembly labels.
	 */
	public final String EDITOR_ASM_LABEL_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_ASM_LABEL_COLOR;

	/**
	 * A theme constant that holds the foreground color used for assembly directives.
	 */
	public final String EDITOR_ASM_DIRECTIVE_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_ASM_DIRECTIVE_COLOR;

	/**
	 * A theme constant that holds the foreground color used for task tags.
	 */
	public final String EDITOR_TASK_TAG_COLOR= ID_PREFIX + PreferenceConstants.EDITOR_TASK_TAG_COLOR;

	/**
	 * A theme constant that holds the foreground color used for doxygen multi-line comments.
	 */
	public final String DOXYGEN_MULTI_LINE_COLOR= PreferenceConstants.DOXYGEN_MULTI_LINE_COLOR;

	/**
	 * A theme constant that holds the foreground color used for doxygen single line comments.
	 */
	public final String DOXYGEN_SINGLE_LINE_COLOR= PreferenceConstants.DOXYGEN_SINGLE_LINE_COLOR;
	
	/**
	 * A theme constant that holds the foreground color used for doxygen tags.
	 */
	public final String DOXYGEN_TAG_COLOR= PreferenceConstants.DOXYGEN_TAG_COLOR;
	
}
