/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 * 	   Sergey Prigogin (Google)
 *     Elazar Leibovich (IDF) - Code folding of compound statements (bug 174597)
 *     Jens Elmenthaler (Verigy) - http://bugs.eclipse.org/235586
 *******************************************************************************/
package org.eclipse.cdt.ui;

import java.util.Locale;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.text.ICColorConstants;

import org.eclipse.cdt.internal.ui.preferences.formatter.FormatterProfileManager;
import org.eclipse.cdt.internal.ui.text.spelling.SpellCheckEngine;

/**
 * Preference constants used in the CDT-UI preference store. Clients should only read
 * the CDT-UI preference store using these values. Clients are not allowed to modify
 * the preference store programmatically.
 *
 * The preferences defined in this file are exported in the plugin's
 * "preferenceTransfer" extension. If adding a new preference please
 * also add it to one of the two if applicable:
 * <ol>
 * <li>"Editor Appearance" - Any preference related to how the editor presents
 *     the edited code to the user.</li>
 * <li>"Editor Behavior" - Any preference related to how the editor processes
 * 	   the edited code.</li>
 * </ol>
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 2.0
 */
public class PreferenceConstants {

	private PreferenceConstants() {
	}

	/**
	 * Preference key suffix for bold text style preference keys.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_BOLD_SUFFIX= "_bold"; //$NON-NLS-1$

	/**
	 * Preference key suffix for italic text style preference keys.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_ITALIC_SUFFIX= "_italic"; //$NON-NLS-1$

	/**
	 * Preference key suffix for strikethrough text style preference keys.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_STRIKETHROUGH_SUFFIX= "_strikethrough"; //$NON-NLS-1$

	/**
	 * Preference key suffix for underline text style preference keys.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_UNDERLINE_SUFFIX= "_underline"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render multi-line comments.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_MULTI_LINE_COMMENT_COLOR= ICColorConstants.C_MULTI_LINE_COMMENT;

	/**
	 * A named preference that controls whether multi-line comments are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}. If {@code true} multi-line comments are rendered
	 * in bold. If {@code false} the are rendered using no font style attribute.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_MULTI_LINE_COMMENT_BOLD= ICColorConstants.C_MULTI_LINE_COMMENT + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether multi-line comments are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}. If {@code true} multi-line comments are rendered
	 * in italic. If {@code false} the are rendered using no italic font style attribute.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_MULTI_LINE_COMMENT_ITALIC= ICColorConstants.C_MULTI_LINE_COMMENT + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render single line comments.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_COLOR= ICColorConstants.C_SINGLE_LINE_COMMENT;

	/**
	 * A named preference that controls whether single line comments are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}. If {@code true} single line comments are rendered
	 * in bold. If {@code false} the are rendered using no font style attribute.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_BOLD= ICColorConstants.C_SINGLE_LINE_COMMENT + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether single line comments are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}. If {@code true} single line comments are rendered
	 * in italic. If {@code false} the are rendered using no italic font style attribute.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_ITALIC= ICColorConstants.C_SINGLE_LINE_COMMENT + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render C/C++ keywords.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_KEYWORD_COLOR= ICColorConstants.C_KEYWORD;

	/**
	 * A named preference that controls whether keywords are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_KEYWORD_BOLD= ICColorConstants.C_KEYWORD + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether keywords are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_KEYWORD_ITALIC= ICColorConstants.C_KEYWORD + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render preprocessor directives.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DIRECTIVE_COLOR= ICColorConstants.PP_DIRECTIVE;

	/**
	 * A named preference that controls whether preprocessor directives are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DIRECTIVE_BOLD= ICColorConstants.PP_DIRECTIVE + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether preprocessor directives are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DIRECTIVE_ITALIC= ICColorConstants.PP_DIRECTIVE + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render headers.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_PP_HEADER_COLOR= ICColorConstants.PP_HEADER;

	/**
	 * A named preference that controls whether headers are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_PP_HEADER_BOLD= ICColorConstants.PP_HEADER + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether number are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_PP_HEADER_ITALIC= ICColorConstants.PP_HEADER + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render preprocessor text.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DEFAULT_COLOR= ICColorConstants.PP_DEFAULT;

	/**
	 * A named preference that controls whether preprocessor text is rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DEFAULT_BOLD= ICColorConstants.PP_DEFAULT + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether preprocessor text is rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DEFAULT_ITALIC= ICColorConstants.PP_DEFAULT + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render builtin types.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_BUILTIN_TYPE_COLOR= ICColorConstants.C_TYPE;

	/**
	 * A named preference that controls whether builtin types are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_BUILTIN_TYPE_BOLD= ICColorConstants.C_TYPE + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether builtin types are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_BUILTIN_TYPE_ITALIC= ICColorConstants.C_TYPE + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render string constants.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_STRING_COLOR= ICColorConstants.C_STRING;

	/**
	 * A named preference that controls whether string constants are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_STRING_BOLD= ICColorConstants.C_STRING + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether string constants are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_STRING_ITALIC= ICColorConstants.C_STRING + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render operators.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_OPERATOR_COLOR= ICColorConstants.C_OPERATOR;

	/**
	 * A named preference that controls whether operators are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_OPERATOR_BOLD= ICColorConstants.C_OPERATOR + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether operators are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_OPERATOR_ITALIC= ICColorConstants.C_OPERATOR + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render numbers.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_NUMBER_COLOR= ICColorConstants.C_NUMBER;

	/**
	 * A named preference that controls whether number are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_NUMBER_BOLD= ICColorConstants.C_NUMBER + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether number are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_NUMBER_ITALIC= ICColorConstants.C_NUMBER + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render braces.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_BRACES_COLOR= ICColorConstants.C_BRACES;

	/**
	 * A named preference that controls whether braces are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_BRACES_BOLD= ICColorConstants.C_BRACES + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether braces are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_BRACES_ITALIC= ICColorConstants.C_BRACES + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render C/C++ default text.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_DEFAULT_COLOR= ICColorConstants.C_DEFAULT;

	/**
	 * A named preference that controls whether C/C++ default text is rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_DEFAULT_BOLD= ICColorConstants.C_DEFAULT + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether C/C++ default text is rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_C_DEFAULT_ITALIC= ICColorConstants.C_DEFAULT + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render assembly labels.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 5.0
	 */
	public final static String EDITOR_ASM_LABEL_COLOR= ICColorConstants.ASM_LABEL;

	/**
	 * A named preference that controls whether assembly labels are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String EDITOR_ASM_LABEL_BOLD= ICColorConstants.ASM_LABEL + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether assembly labels are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String EDITOR_ASM_LABEL_ITALIC= ICColorConstants.ASM_LABEL + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render assembly directives.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 5.0
	 */
	public final static String EDITOR_ASM_DIRECTIVE_COLOR= ICColorConstants.ASM_DIRECTIVE;

	/**
	 * A named preference that controls whether assembly directives are rendered in bold.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String EDITOR_ASM_DIRECTIVE_BOLD= ICColorConstants.ASM_DIRECTIVE + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether assembly directives are rendered in italic.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String EDITOR_ASM_DIRECTIVE_ITALIC= ICColorConstants.ASM_DIRECTIVE + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render doxygen tags.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 5.4
	 */
	public static final String DOXYGEN_TAG_COLOR= "org.eclipse.cdt.internal.ui.text.doctools.doxygen.recognizedTag"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render doxygen single line comments.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 5.4
	 */
	public static final String DOXYGEN_SINGLE_LINE_COLOR= "org.eclipse.cdt.internal.ui.text.doctools.doxygen.single"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render doxygen multi-line comments.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 5.4
	 */
	public static final String DOXYGEN_MULTI_LINE_COLOR= "org.eclipse.cdt.internal.ui.text.doctools.doxygen.multi"; //$NON-NLS-1$

	/**
	 * The symbolic font name for the C/C++ editor text font
	 * (value {@code "org.eclipse.cdt.ui.editors.textfont"}).
	 *
	 * @since 4.0
	 */
	public final static String EDITOR_TEXT_FONT= "org.eclipse.cdt.ui.editors.textfont"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the cview's selection is linked to the active
	 * editor.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String PREF_LINK_TO_EDITOR= "org.eclipse.cdt.ui.editor.linkToEditor"; //$NON-NLS-1$

	/**
	 * A named preference that specifies whether children of a translation unit are shown in
	 * the CView.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String PREF_SHOW_CU_CHILDREN= "org.eclipse.cdt.ui.editor.CUChildren"; //$NON-NLS-1$

	/**
	 * A named preference that speficies whether to use the parser's structural mode to build
	 * the CModel.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String PREF_USE_STRUCTURAL_PARSE_MODE= "org.eclipse.cdt.ui.editor.UseStructuralMode"; //$NON-NLS-1$

	/**
	 * A named preference that controls if segmented view (show selected element only) is turned
	 * on or off.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String EDITOR_SHOW_SEGMENTS= "org.eclipse.cdt.ui.editor.showSegments"; //$NON-NLS-1$

    /**
     * A named preference that holds the color used to render task tags.
     * <p>
     * Value is of type {@code String}. A RGB color value encoded as a string
     * using class {@code PreferenceConverter}
     * </p>
     *
     * @see org.eclipse.jface.resource.StringConverter
     * @see org.eclipse.jface.preference.PreferenceConverter
     */
    public final static String EDITOR_TASK_TAG_COLOR= ICColorConstants.TASK_TAG;

    /**
     * A named preference that controls whether task tags are rendered in bold.
     * <p>
     * Value is of type {@code Boolean}.
     * </p>
     */
    public final static String EDITOR_TASK_TAG_BOLD= ICColorConstants.TASK_TAG + EDITOR_BOLD_SUFFIX;

    /**
     * A named preference that controls whether task tags are rendered in italic.
     * <p>
     * Value is of type {@code Boolean}.
     * </p>
     */
    public final static String EDITOR_TASK_TAG_ITALIC= ICColorConstants.TASK_TAG + EDITOR_ITALIC_SUFFIX;

    /**
	 * A named preference that controls if correction indicators are shown in the UI.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_CORRECTION_INDICATION= "CEditor.ShowTemporaryProblem"; //$NON-NLS-1$

	/**
	 * A named preference that controls if temporary problems are evaluated and shown in the UI.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_EVALUATE_TEMPORARY_PROBLEMS= "handleTemporaryProblems"; //$NON-NLS-1$

	/**
	 * A named preference that defines the key for the hover modifiers.
	 *
	 */
	public static final String EDITOR_TEXT_HOVER_MODIFIERS= "hoverModifiers"; //$NON-NLS-1$

	/**
	 * A named preference that defines the key for the hover modifier state masks.
	 * The value is only used if the value of {@code EDITOR_TEXT_HOVER_MODIFIERS}
	 * cannot be resolved to valid SWT modifier bits.
	 *
	 * @see #EDITOR_TEXT_HOVER_MODIFIERS
	 */
	public static final String EDITOR_TEXT_HOVER_MODIFIER_MASKS= "hoverModifierMasks"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close strings' feature
	 *  is   enabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_CLOSE_STRINGS= "closeStrings"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'wrap strings' feature is
	 * enabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_WRAP_STRINGS= "wrapStrings"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'escape strings' feature is
	 * enabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_ESCAPE_STRINGS= "escapeStrings"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close brackets' feature is
	 * enabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_CLOSE_BRACKETS= "closeBrackets"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close angular brackets' feature is
	 * enabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_CLOSE_ANGULAR_BRACKETS= "closeAngularBrackets"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close braces' feature is
	 * enabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_CLOSE_BRACES= "closeBraces"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'smart paste' feature is
	 * enabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_SMART_PASTE= "smartPaste"; //$NON-NLS-1$

	/**
	 * A named preference that controls the smart tab behavior.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String EDITOR_SMART_TAB= "smart_tab"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'auto indent' feature is
	 * enabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public final static String EDITOR_AUTO_INDENT= "autoIndent"; //$NON-NLS-1$

	/**
	 * The id of the best match hover contributed for extension point
	 * {@code org.eclipse.cdt.ui.textHovers}.
	 *
	 * @since 2.1
	 */
	public static final String ID_BESTMATCH_HOVER= "org.eclipse.cdt.ui.BestMatchHover"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the Outline view should group include directives.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String OUTLINE_GROUP_INCLUDES= "org.eclipse.cdt.ui.outline.groupincludes"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the Outline view should group namespaces.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String OUTLINE_GROUP_NAMESPACES= "org.eclipse.cdt.ui.outline.groupnamespaces"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the Outline view should group member definitions.
	 * <p>
	 * Value is of type {@code Boolean}.
	 * @since 5.1
	 */
	public static final String OUTLINE_GROUP_MEMBERS= "org.eclipse.cdt.ui.outline.groupmembers"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the Outline view should group macro definitions.
	 * <p>
	 * Value is of type {@code Boolean}.
	 * @since 5.2
	 */
	public static final String OUTLINE_GROUP_MACROS= "org.eclipse.cdt.ui.outline.groupmacros"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the Outline view
	 * selection should stay in sync with with the element at the current cursor position.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String OUTLINE_LINK_TO_EDITOR = "org.eclipse.cdt.ui.outline.linktoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether include directives should be grouped in
	 * the C/C++ Projects view and the Project Explorer view.
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String CVIEW_GROUP_INCLUDES= "org.eclipse.cdt.ui.cview.groupincludes"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether macro definitions should be grouped in
	 * the C/C++ Projects view and the Project Explorer view.
	 * <p>
	 * Value is of type {@code Boolean}.
	 * @since 5.2
	 */
	public static final String CVIEW_GROUP_MACROS= "org.eclipse.cdt.ui.cview.groupmacros"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether header and source files should be separated in
	 * the C/C++ Projects view and the Project Explorer view.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String CVIEW_SEPARATE_HEADER_AND_SOURCE= "org.eclipse.cdt.ui.cview.separateheaderandsource"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the sorting order of source files should be changed
	 * in the C/C++ Projects view and the Project Explorer view when they are excluded from build.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.3
	 */
	public static final String SORT_ORDER_OF_EXCLUDED_FILES= "org.eclipse.cdt.ui.cview.sortorderofexcludedfiles"; //$NON-NLS-1$

	/**
	 * A named preference that controls which completion proposal categories
	 * have been excluded from the default proposal list.
	 * <p>
	 * Value is of type {@code String}, a "\0"-separated list of identifiers.
	 *
	 * @since 4.0
	 */
	public static final String CODEASSIST_EXCLUDED_CATEGORIES= "content_assist_disabled_computers"; //$NON-NLS-1$

	/**
	 * A named preference that controls the order of the specific code assist commands.
	 * <p>
	 * Value is of type {@code String}, a "\0"-separated list of identifiers.
	 *
	 * @since 4.0
	 */
	public static final String CODEASSIST_CATEGORY_ORDER= "content_assist_category_order"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether folding is enabled in the C editor.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 */
	public static final String EDITOR_FOLDING_ENABLED= "editor_folding_enabled"; //$NON-NLS-1$

	/**
	 * A named preference that stores the configured folding provider.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 */
	public static final String EDITOR_FOLDING_PROVIDER= "editor_folding_provider"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for Structure folding for the default folding provider.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_STRUCTURES= "editor_folding_default_structures"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for statements folding (if/else, do/while, for, switch statements)
	 * <p>
	 * Value is of type {@code Boolean}.
	 */
	public static final String EDITOR_FOLDING_STATEMENTS = "editor_folding_statements"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for functions folding for the default folding provider.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_FUNCTIONS= "editor_folding_default_functions"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for method folding for the default folding provider.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_METHODS= "editor_folding_default_methods"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for macros folding for the default folding provider.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_MACROS= "editor_folding_default_macros"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for comment folding for the default folding provider.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_FOLDING_COMMENTS= "editor_folding_default_comments"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for header comment folding for the default folding provider.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_FOLDING_HEADERS= "editor_folding_default_headers"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for inactive code folding for the default folding provider.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_FOLDING_INACTIVE_CODE= "editor_folding_default_inactive"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether folding of preprocessor branches is enabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED= "editor_folding_preprocessor_enabled"; //$NON-NLS-1$

	/**
	 * A named preference that controls if templates are formatted when applied.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 2.1
	 */
	public static final String TEMPLATES_USE_CODEFORMATTER= "org.eclipse.cdt.ui.text.templates.format"; //$NON-NLS-1$

	/**
	 * A named preference that controls which profile is used by the code formatter.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 4.0
	 */
	public static final String FORMATTER_PROFILE = "formatter_profile"; //$NON-NLS-1$

	/**
	 * Preference key determining the scope of formatting when the selection is empty.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.9
	 */
	public final static String FORMATTING_SCOPE_FOR_EMPTY_SELECTION = "formattingScopeForEmptySelection"; //$NON-NLS-1$
	
	/**
	 * The value of {@link #FORMATTING_SCOPE_FOR_EMPTY_SELECTION} specifying that the formatting
	 * applies to the whole document.
	 *
	 * @since 5.9
	 */
	public static final String FORMATTING_SCOPE_DOCUMENT = "document"; //$NON-NLS-1$

	/**
	 * The value of {@link #FORMATTING_SCOPE_FOR_EMPTY_SELECTION} specifying that the formatting
	 * applies to the containing statement.
	 *
	 * @since 5.9
	 */
	public static final String FORMATTING_SCOPE_STATEMENT = "statement"; //$NON-NLS-1$

	/**
	 * Preference key for whether to ask user for how formatting of an empty selection
	 * should be interpreted.
	 * <p>
	 * Value is of type {@code Boolean}. The default is {@code true}.
	 *
	 * @since 5.9
	 */
	public final static String FORMATTING_CONFIRM_SCOPE_FOR_EMPTY_SELECTION = "formattingConfirmScopeForEmptySelection"; //$NON-NLS-1$

	/**
	 * Preference key for whether to ensure a newline at the end of files when saving.
	 *
	 * @since 4.0
	 */
	public final static String ENSURE_NEWLINE_AT_EOF = "ensureNewlineAtEOF"; //$NON-NLS-1$

	/**
	 * Preference key for whether to remove trailing whitespace when saving.
	 *
	 * @since 5.0
	 */
	public final static String REMOVE_TRAILING_WHITESPACE = "removeTrailingWhitespace"; //$NON-NLS-1$

	/**
	 * Preference key controlling how REMOVE_TRAILING_WHITESPACE option is applied.
	 * If REMOVE_TRAILING_WHITESPACE is enabled, this option limits the scope of
	 * the removal to edited lines only. This option has no effect if
	 * REMOVE_TRAILING_WHITESPACE is disabled.
	 *
	 * @since 5.1
	 */
	public final static String REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES = "removeTrailingWhitespaceEditedLines"; //$NON-NLS-1$

	/**
	 * Style format code on save
	 *
	 * @since 5.9
	 */
	public final static String FORMAT_SOURCE_CODE = "formatSourceCode"; //$NON-NLS-1$

	/**
	 * A named preference that defines whether the hint to make hover sticky should be shown.
	 *
	 * @since 3.1.1
	 * @deprecated As of 4.0, replaced by {@link AbstractDecoratedTextEditorPreferenceConstants#EDITOR_SHOW_TEXT_HOVER_AFFORDANCE}
	 */
	@Deprecated
	public static final String EDITOR_SHOW_TEXT_HOVER_AFFORDANCE= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE;

	/**
	 * A named preference prefix for semantic highlighting preferences.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX="semanticHighlighting."; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls a semantic highlighting's color.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX=".color"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the text attribute bold.
	 * <p>
	 * Value is of type {@code Boolean}: {@code true} if bold.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX=".bold"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the text attribute italic.
	 * <p>
	 * Value is of type {@code Boolean}: {@code true} if italic.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX=".italic"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the text attribute strikethrough.
	 * <p>
	 * Value is of type {@code Boolean}: {@code true} if strikethrough.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX=".strikethrough"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the text attribute underline.
	 * <p>
	 * Value is of type {@code Boolean}: {@code true} if underline.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX=".underline"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting is enabled.
	 * <p>
	 * Value is of type {@code Boolean}: {@code true} if enabled.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX=".enabled"; //$NON-NLS-1$

	/**
	 * A named preference key that controls if semantic highlighting is enabled.
	 * <p>
	 * Value is of type {@code Boolean}: {@code true} if enabled.
	 *
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED= "semanticHighlighting.enabled"; //$NON-NLS-1$

	/**
	 * A named preference that controls if quick assist light bulbs are shown.
	 * <p>
	 * Value is of type {@code Boolean}: if {@code true} light bulbs are shown
	 * for quick assists.
	 *
	 * @since 5.0
	 */
	public static final String EDITOR_QUICKASSIST_LIGHTBULB="org.eclipse.cdt.quickassist.lightbulb"; //$NON-NLS-1$

	/**
	 * A named preference that holds the background color used in the code assist selection dialog.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 *
	 * @since 5.0
	 */
	public final static String CODEASSIST_PROPOSALS_BACKGROUND= "content_assist_proposals_background"; //$NON-NLS-1$
	/**
	 * A named preference that holds the foreground color used in the code assist selection dialog.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 *
	 * @since 5.0
	 */
	public final static String CODEASSIST_PROPOSALS_FOREGROUND= "content_assist_proposals_foreground"; //$NON-NLS-1$

	/**
     * A named preference that holds the duration (in milli seconds) a content proposal may compute
     * until it is assumed that the proposal computer has a problem and should be disabled.
     * <p>
     * Value is of type {@code long}.
     *
     * @since 5.4
     */
    public final static String CODEASSIST_PROPOSALS_TIMEOUT= "content_assist_proposals_timeout"; //$NON-NLS-1$

    /**
	 * A named preference that holds the background color used for parameter hints.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 *
	 * @since 5.0
	 */
	public final static String CODEASSIST_PARAMETERS_BACKGROUND= "content_assist_parameters_background"; //$NON-NLS-1$

	/**
	 * A named preference that holds the foreground color used in the code assist selection dialog.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 *
	 * @since 5.0
	 */
	public final static String CODEASSIST_PARAMETERS_FOREGROUND= "content_assist_parameters_foreground"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether all dirty editors are automatically saved before a refactoring is
	 * executed.
	 * <p>
	 * Value is of type {@code Boolean}.
	 * @since 5.3
	 */
	public static final String REFACTOR_SAVE_ALL_EDITORS= "Refactoring.savealleditors"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether certain refactorings use a lightweight UI when
	 * started from a C/C++ editor.
	 * <p>
	 * Value is of type {@code Boolean}.
	 * @since 5.3
	 */
	public static final String REFACTOR_LIGHTWEIGHT= "Refactor.lightweight"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether words containing digits should
	 * be skipped during spell checking.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_IGNORE_DIGITS= "spelling_ignore_digits"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether mixed case words should be
	 * skipped during spell checking.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_IGNORE_MIXED= "spelling_ignore_mixed"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether sentence capitalization should
	 * be ignored during spell checking.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_IGNORE_SENTENCE= "spelling_ignore_sentence"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether upper case words should be
	 * skipped during spell checking.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_IGNORE_UPPER= "spelling_ignore_upper"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether URLs should be ignored during
	 * spell checking.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_IGNORE_URLS= "spelling_ignore_urls"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether single letters
	 * should be ignored during spell checking.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_IGNORE_SINGLE_LETTERS= "spelling_ignore_single_letters"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether string literals
	 * should be ignored during spell checking.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_IGNORE_STRING_LITERALS= "spelling_ignore_string_literals"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether non-letters at word boundaries
	 * should be ignored during spell checking.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_IGNORE_NON_LETTERS= "spelling_ignore_non_letters"; //$NON-NLS-1$

	/**
	 * A named preference that controls the locale used for spell checking.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_LOCALE= "spelling_locale"; //$NON-NLS-1$

	/**
	 * A named preference that controls the number of proposals offered during
	 * spell checking.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_PROPOSAL_THRESHOLD= "spelling_proposal_threshold"; //$NON-NLS-1$

	/**
	 * A named preference that controls the maximum number of problems reported
	 * during spell checking.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_PROBLEMS_THRESHOLD= "spelling_problems_threshold"; //$NON-NLS-1$

	/**
	 * A named preference that specifies the workspace user dictionary.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_USER_DICTIONARY= "spelling_user_dictionary"; //$NON-NLS-1$

	/**
	 * A named preference that specifies encoding of the workspace user dictionary.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_USER_DICTIONARY_ENCODING= "spelling_user_dictionary_encoding"; //$NON-NLS-1$

	/**
	 * A named preference that specifies whether spelling dictionaries are available to content assist.
	 *
	 * <strong>Note:</strong> This is currently not supported because the spelling engine
	 * cannot return word proposals but only correction proposals.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public final static String SPELLING_ENABLE_CONTENTASSIST= "spelling_enable_contentassist"; //$NON-NLS-1$

	/**
	 * A named preference that controls if documentation comment stubs will be added
	 * automatically to newly created types and methods.
	 * <p>
	 * Value is of type {@code Boolean}.
	 * @since 5.0
	 */
	public static final String CODEGEN_ADD_COMMENTS= "org.eclipse.cdt.ui.add_comments"; //$NON-NLS-1$

	/**
	 * A named preference that holds the source hover background color.
	 * <p>
	 * Value is of type {@code String}. A RGB color value encoded as a string
	 * using class {@code PreferenceConverter}
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 5.0
	 */
	public final static String EDITOR_SOURCE_HOVER_BACKGROUND_COLOR= "sourceHoverBackgroundColor"; //$NON-NLS-1$

	/**
	 * A named preference that tells whether to use the system
	 * default color ({@link SWT#COLOR_INFO_BACKGROUND}) for
	 * the source hover background color.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 5.0
	 */
	public final static String EDITOR_SOURCE_HOVER_BACKGROUND_COLOR_SYSTEM_DEFAULT= "sourceHoverBackgroundColor.SystemDefault"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether occurrences are marked in the editor.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String EDITOR_MARK_OCCURRENCES= "markOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether occurrences are sticky in the editor.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is {@code true}.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String EDITOR_STICKY_OCCURRENCES= "stickyOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether occurrences of overloaded operators are marked in the editor.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is {@code true}.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.3
	 */
	public static final String EDITOR_MARK_OVERLOADED_OPERATOR_OCCURRENCES= "markOverloadedOperatorsOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether all scalability mode options should be turned on.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String SCALABILITY_ENABLE_ALL = "scalability.enableAll"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the editor's reconciler is disabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String SCALABILITY_RECONCILER = "scalability.reconciler"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether syntax coloring is disabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String SCALABILITY_SYNTAX_COLOR = "scalability.syntaxColor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether parser-based content assist proposals are disabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String SCALABILITY_PARSER_BASED_CONTENT_ASSIST = "scalability.parserBasedContentAssist"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether users should be notified if scalability mode should be turned on.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String SCALABILITY_ALERT = "scalability.detect"; //$NON-NLS-1$

	/**
	 * The size of the file that will trigger scalability mode
	 * <p>
	 * Value is of type {@code int}.
	 *
	 * @since 5.0
	 */
	public static final String SCALABILITY_NUMBER_OF_LINES = "scalability.numberOfLines"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether syntax coloring is disabled.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String SCALABILITY_SEMANTIC_HIGHLIGHT = "scalability.semanticHighlight"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the content assist auto activation is disabled in scalability mode.
	 * <p>
	 * Value is of type {@code Boolean}.
	 *
	 * @since 5.0
	 */
	public static final String SCALABILITY_CONTENT_ASSIST_AUTO_ACTIVATION = "scalability.contentAssistAutoActivation"; //$NON-NLS-1$

	/**
	 * A named preference that controls how an include guard symbol is created.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.1
	 */
	public static final String CODE_TEMPLATES_INCLUDE_GUARD_SCHEME = "codetemplates.includeGuardGenerationScheme"; //$NON-NLS-1$

	/**
	 * The value of {@code CODE_TEMPLATES_INCLUDE_GUARD_GENERATION_SCHEME}
	 * specifying that the include guard symbol is to be derived from
	 * the include file's name.
	 *
	 * @since 5.1
	 */
	public static final int CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME = 0;

	/**
	 * The value of {@code CODE_TEMPLATES_INCLUDE_GUARD_GENERATION_SCHEME}
	 * specifying that the include guard symbol is to be derived from a UUID.
	 *
	 * @since 5.1
	 */
	public static final int CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_UUID = 1;

	/**
	 * The value of {@code CODE_TEMPLATES_INCLUDE_GUARD_GENERATION_SCHEME}
	 * specifying that the include guard symbol is to be derived from
	 * the include file's path relative to the source folder.
	 *
	 * @since 5.2
	 */
	public static final int CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_PATH = 2;

	/**
	 * A named preference that controls how capitalization of a constant name.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CONSTANT_CAPITALIZATION = "nameStyle.constant.capitalization"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of a constant name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CONSTANT_PREFIX = "nameStyle.constant.prefix"; //$NON-NLS-1$
	/**
	 * A named preference that controls suffix of a constant name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CONSTANT_SUFFIX = "nameStyle.constant.suffix"; //$NON-NLS-1$
	/**
	 * A named preference that controls delimiter that is inserted between words
	 * of a constant name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CONSTANT_WORD_DELIMITER = "nameStyle.constant.wordDelimiter"; //$NON-NLS-1$

	/**
	 * A named preference that controls how capitalization of a variable name.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_VARIABLE_CAPITALIZATION = "nameStyle.variable.capitalization"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of a variable name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_VARIABLE_PREFIX = "nameStyle.variable.prefix"; //$NON-NLS-1$
	/**
	 * A named preference that controls suffix of a variable name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_VARIABLE_SUFFIX = "nameStyle.variable.suffix"; //$NON-NLS-1$
	/**
	 * A named preference that controls delimiter that is inserted between words
	 * of a variable name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_VARIABLE_WORD_DELIMITER = "nameStyle.variable.wordDelimiter"; //$NON-NLS-1$

	/**
	 * A named preference that controls how capitalization of a field name.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_FIELD_CAPITALIZATION = "nameStyle.field.capitalization"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of a field name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_FIELD_PREFIX = "nameStyle.field.prefix"; //$NON-NLS-1$
	/**
	 * A named preference that controls suffix of a field name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_FIELD_SUFFIX = "nameStyle.field.suffix"; //$NON-NLS-1$
	/**
	 * A named preference that controls delimiter that is inserted between words
	 * of a field name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_FIELD_WORD_DELIMITER = "nameStyle.field.wordDelimiter"; //$NON-NLS-1$

	/**
	 * A named preference that controls how capitalization of a method name.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.4
	 */
	public static final String NAME_STYLE_METHOD_CAPITALIZATION = "nameStyle.method.capitalization"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of a method name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.4
	 */
	public static final String NAME_STYLE_METHOD_PREFIX = "nameStyle.method.prefix"; //$NON-NLS-1$
	/**
	 * A named preference that controls suffix of a method name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.4
	 */
	public static final String NAME_STYLE_METHOD_SUFFIX = "nameStyle.method.suffix"; //$NON-NLS-1$
	/**
	 * A named preference that controls delimiter that is inserted between words
	 * of a method name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.4
	 */
	public static final String NAME_STYLE_METHOD_WORD_DELIMITER = "nameStyle.method.wordDelimiter"; //$NON-NLS-1$

	/**
	 * A named preference that controls how capitalization of the getter name
	 * depends on capitalization of the method name.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_GETTER_CAPITALIZATION = "nameStyle.getter.capitalization"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of the getter name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_GETTER_PREFIX = "nameStyle.getter.prefix"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of the getter name for a boolean field.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_GETTER_PREFIX_FOR_BOOLEAN = "nameStyle.getter.prefixForBoolean"; //$NON-NLS-1$
	/**
	 * A named preference that controls suffix of the getter name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_GETTER_SUFFIX = "nameStyle.getter.suffix"; //$NON-NLS-1$
	/**
	 * A named preference that controls delimiter that is inserted between words
	 * when composing the getter name from the field name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_GETTER_WORD_DELIMITER = "nameStyle.getter.wordDelimiter"; //$NON-NLS-1$

	/**
	 * A named preference that controls how capitalization of the setter name
	 * depends on capitalization of the field name.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_SETTER_CAPITALIZATION = "nameStyle.setter.capitalization"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of the setter name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_SETTER_PREFIX = "nameStyle.setter.prefix"; //$NON-NLS-1$
	/**
	 * A named preference that controls suffix of the setter name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_SETTER_SUFFIX = "nameStyle.setter.suffix"; //$NON-NLS-1$
	/**
	 * A named preference that controls delimiter that is inserted between words
	 * when composing the setter name from the field name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_SETTER_WORD_DELIMITER = "nameStyle.setter.wordDelimiter"; //$NON-NLS-1$

	/**
	 * A named preference that controls how capitalization of the C++ source file name
	 * depends on capitalization of the class name.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_SOURCE_CAPITALIZATION = "nameStyle.cpp.source.capitalization"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of the C++ source file name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_SOURCE_PREFIX = "nameStyle.cpp.source.prefix"; //$NON-NLS-1$
	/**
	 * A named preference that controls suffix of the C++ source file name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_SOURCE_SUFFIX = "nameStyle.cpp.source.suffix"; //$NON-NLS-1$
	/**
	 * A named preference that controls delimiter that is inserted between words
	 * when composing the C++ source file name from the class name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_SOURCE_WORD_DELIMITER = "nameStyle.cpp.source.wordDelimiter"; //$NON-NLS-1$

	/**
	 * A named preference that controls how capitalization of the C++ header file name
	 * depends on capitalization of the class name.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_HEADER_CAPITALIZATION = "nameStyle.cpp.header.capitalization"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of the C++ header file name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_HEADER_PREFIX = "nameStyle.cpp.header.prefix"; //$NON-NLS-1$
	/**
	 * A named preference that controls suffix of the C++ header file name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_HEADER_SUFFIX = "nameStyle.cpp.header.suffix"; //$NON-NLS-1$
	/**
	 * A named preference that controls delimiter that is inserted between words
	 * when composing the C++ header file name from the class name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_HEADER_WORD_DELIMITER = "nameStyle.cpp.header.wordDelimiter"; //$NON-NLS-1$

	/**
	 * A named preference that controls how capitalization of the C++ test file name
	 * depends on capitalization of the class name.
	 * <p>
	 * Value is of type {@code Integer}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_TEST_CAPITALIZATION = "nameStyle.cpp.test.capitalization"; //$NON-NLS-1$
	/**
	 * A named preference that controls prefix of the C++ test file name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_TEST_PREFIX = "nameStyle.cpp.test.prefix"; //$NON-NLS-1$
	/**
	 * A named preference that controls suffix of the C++ test file name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_TEST_SUFFIX = "nameStyle.cpp.test.suffix"; //$NON-NLS-1$
	/**
	 * A named preference that controls delimiter that is inserted between words
	 * when composing the C++ test file name from the class name.
	 * <p>
	 * Value is of type {@code String}.
	 *
	 * @since 5.3
	 */
	public static final String NAME_STYLE_CPP_TEST_WORD_DELIMITER = "nameStyle.cpp.test.wordDelimiter"; //$NON-NLS-1$

	/**
	 * The value of {@code NAME_STYLE_*_CAPITALIZATION} specifying that the name
	 * is to be derived from the class or the variable name without changing
	 * capitalization.
	 *
	 * @since 5.3
	 */
	public static final int NAME_STYLE_CAPITALIZATION_ORIGINAL = 0;
	/**
	 * The value of {@code NAME_STYLE_*_CAPITALIZATION} specifying that the name
	 * is to be derived from the class or the variable name by converting it to upper
	 * case.
	 *
	 * @since 5.3
	 */
	public static final int NAME_STYLE_CAPITALIZATION_UPPER_CASE = 1;
	/**
	 * The value of {@code NAME_STYLE_*_CAPITALIZATION} specifying that the name
	 * is to be derived from the class or the variable name by converting it to lower
	 * case.
	 *
	 * @since 5.3
	 */
	public static final int NAME_STYLE_CAPITALIZATION_LOWER_CASE = 2;
	/**
	 * The value of {@code NAME_STYLE_*_CAPITALIZATION} specifying that the name
	 * is to be derived from the class or the variable name by capitalizing first
	 * letter of every word.
	 *
	 * @since 5.3
	 */
	public static final int NAME_STYLE_CAPITALIZATION_CAMEL_CASE = 3;
	/**
	 * The value of {@code NAME_STYLE_*_CAPITALIZATION} specifying that the name
	 * is to be derived from the class or the variable name by capitalizing first
	 * letter of every word except the first one.
	 *
	 * @since 5.3
	 */
	public static final int NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE = 4;

	/**
	 * A named preference that controls the order of private/protected/public class members in
	 * generated code.
	 * <p>
	 * Value is of type {@code Boolean}. The {@code true} value means that private members
	 * are before public ones. The default is to put public members before private ones.
	 *
	 * @since 5.4
	 */
	public static final String CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER = "class_member_ascending_visibility_order"; //$NON-NLS-1$

	/**
	 * A named preference that controls the order of parameters of generated functions.
	 * <p>
	 * Value is of type {@code Boolean}. The {@code true} value means that output
	 * parameters are before the input ones. The default is to put outputparameters after the input
	 * ones.
	 *
	 * @since 5.4
	 */
	public static final String FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT = "function_output_parameters_before_input"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether output parameters of generated functions are passed
	 * by pointer or by reference.
	 * <p>
	 * Value is of type {@code Boolean}. The {@code true} value means that output
	 * parameters are passed by pointer. The default is to pass output parameters by reference.
	 *
	 * @since 5.4
	 */
	public static final String FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER = "function_pass_output_parameters_by_pointer"; //$NON-NLS-1$

	/**
	 * Whether composite types should be forward declared if possible.
	 *
	 * Examples:
	 *  class X;
	 *  struct Y;
	 *  union Z;
	 *
	 * @since 5.6
	 */
	public static final String FORWARD_DECLARE_COMPOSITE_TYPES = "forwardDeclare.compositeTypes"; //$NON-NLS-1$

	/**
	 * Whether C++11-style enums should be forward declared if possible.
	 *
	 * Example:
	 *  enum class X;
	 *
	 * @since 5.6
	 */
	public static final String FORWARD_DECLARE_ENUMS = "forwardDeclare.enums"; //$NON-NLS-1$

	/**
	 * Whether C-style functions should be forward declared if possible.
	 *
	 * Example:
	 *  void foo();
	 *
	 * @since 5.6
	 */
	public static final String FORWARD_DECLARE_FUNCTIONS = "forwardDeclare.functions"; //$NON-NLS-1$

	/**
	 * Whether external variables should be forward declared if possible.
	 *
	 * Example:
	 *  extern int errno;
	 *
	 * @since 5.7
	 */
	public static final String FORWARD_DECLARE_EXTERNAL_VARIABLES = "forwardDeclare.externalVariables"; //$NON-NLS-1$

	/**
	 * Whether C++ templates should be forward declared if possible.
	 *
	 * Examples:
	 *  template<typename T> class foo;
	 *  template<typename T> void bar(T p);
	 *
	 * @since 5.6
	 */
	public static final String FORWARD_DECLARE_TEMPLATES = "forwardDeclare.templates"; //$NON-NLS-1$

	/**
	 * Whether elements nested within namespaces should be forward declared if possible.
	 *
	 * Examples:
	 *  namespace N { class X; }
	 *
	 * @since 5.6
	 */
	public static final String FORWARD_DECLARE_NAMESPACE_ELEMENTS = "forwardDeclare.namespaceElements"; //$NON-NLS-1$

	/**
	 * Defines a list of file name suffixes. A header file and the including file are considered
	 * partners if their file names without extensions are either identical or differ by one of
	 * these suffixes.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDES_PARTNER_FILE_SUFFIXES = "includes.partnerFileSuffixes"; //$NON-NLS-1$

	/**
	 * Defines a list of file name extensions. An included file is considered exported by default if it has
	 * one of the specified name extensions.
	 *
	 * @since 5.10
	 */
	public static final String INCLUDES_EXTENSIONS_OF_AUTO_EXPORTED_FILES = "includes.extensionsOfAutoExportedFiles"; //$NON-NLS-1$

	/**
	 * Whether a heuristic approach should be used to decide which C++ header files to include.
	 * The heuristic prefers headers which have no file extension and / or are named like the symbol
	 * which should be defined. This often works out nicely since it's a commonly used naming
	 * convention for C++ library headers.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDES_HEURISTIC_HEADER_SUBSTITUTION = "organizeIncludes.heuristicHeader_substitution"; //$NON-NLS-1$

	/**
	 * Whether it's allowed to reorder existing include directives. If this preference is set to
	 * false, the original order is kept as far as possible. This may be necessary to avoid breaking
	 * code which makes assumptions about the order of the include directives. If this is set to
	 * true, a different sort order can be applied. Groups of includes are ordered according to
	 * the values returned by
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle#getOrder()} method.
	 * Includes within each group are ordered alphabetically.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDES_ALLOW_REORDERING = "organizeIncludes.allowReordering"; //$NON-NLS-1$

	/**
	 * Whether indirect inclusion through a partner header file is allowed.
	 *
	 * @since 5.7
	 */
	public static final String INCLUDES_ALLOW_PARTNER_INDIRECT_INCLUSION = "organizeIncludes.allowPartnerIndirectInclusion"; //$NON-NLS-1$

	/**
	 * Determines what should be done with any unused include directives and forward declarations.
	 * This preference may have one of the three values defined by
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludePreferences.UnusedStatementDisposition}
	 * enumeration ("REMOVE", "COMMENT_OUT", "KEEP").
	 *
	 * @since 5.6
	 */
	public static final String INCLUDES_UNUSED_STATEMENTS_DISPOSITION = "organizeIncludes.unusedStatements"; //$NON-NLS-1$

	/**
	 * Header file substitution rules.
	 * The value of the preference is an XML representation of one or more
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.HeaderSubstitutionMap}s.
	 *
	 * @since 5.7
	 */
	public static final String INCLUDES_HEADER_SUBSTITUTION = "organizeIncludes.headerSubstitution"; //$NON-NLS-1$

	/**
	 * Symbol exporting rules.
	 * The value of the preference is an XML representation of one or more
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.SymbolExportMap}s.
	 *
	 * @since 5.7
	 */
	public static final String INCLUDES_SYMBOL_EXPORTING_HEADERS = "organizeIncludes.symbolExportingHeaders"; //$NON-NLS-1$

	/**
	 * Include style for headers closely related to the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_RELATED = "includeStyle.related"; //$NON-NLS-1$
	/**
	 * Include style for the header with the same name as the including file. 
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_PARTNER = "includeStyle.partner"; //$NON-NLS-1$
	/**
	 * Include style for headers in the same folder as the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_SAME_FOLDER = "includeStyle.sameFolder"; //$NON-NLS-1$
	/**
	 * Include style for headers in subfolders of the folder containing the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_SUBFOLDER = "includeStyle.subfolder"; //$NON-NLS-1$
	/**
	 * Include style for system headers.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_SYSTEM = "includeStyle.system"; //$NON-NLS-1$
	/**
	 * Include style for C-style system headers with a file name extension.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_SYSTEM_WITH_EXTENSION = "includeStyle.systemWithExtension"; //$NON-NLS-1$
	/**
	 * Include style for C++-style system headers without a file name extension.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_SYSTEM_WITHOUT_EXTENSION = "includeStyle.systemWithoutExtension"; //$NON-NLS-1$
	/**
	 * Include style for headers not closely related to the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_OTHER = "includeStyle.other"; //$NON-NLS-1$
	/**
	 * Include style for headers in the same project as the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_SAME_PROJECT = "includeStyle.inSameProject"; //$NON-NLS-1$
	/**
	 * Include style for headers in a different project than the including file.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_OTHER_PROJECT = "includeStyle.inOtherProject"; //$NON-NLS-1$
	/**
	 * Include style for headers outside Eclipse workspace.
	 * The value of the preference is an XML representation of
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_EXTERNAL = "includeStyle.external"; //$NON-NLS-1$
	/**
	 * Include styles for headers matching user-defined patterns.
	 * The value of the preference is an XML representation of one or more
	 * {@link org.eclipse.cdt.internal.ui.refactoring.includes.IncludeGroupStyle}s.
	 *
	 * @since 5.6
	 */
	public static final String INCLUDE_STYLE_MATCHING_PATTERN = "includeStyle.matchingPattern"; //$NON-NLS-1$

	/**
	 * Returns the CDT-UI preference store.
	 *
	 * @return the CDT-UI preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}

    /**
     * Initializes the given preference store with the default values.
     *
     * @param store the preference store to be initialized
     */
    public static void initializeDefaultValues(IPreferenceStore store) {
		store.setDefault(EDITOR_CORRECTION_INDICATION, false);
		store.setDefault(EDITOR_SHOW_SEGMENTS, false);
		store.setDefault(PREF_SHOW_CU_CHILDREN, true);

		// This option has to be turned on for the spelling checker too work.
		store.setDefault(EDITOR_EVALUATE_TEMPORARY_PROBLEMS, true);

		int sourceHoverModifier= SWT.MOD2;
		String sourceHoverModifierName= Action.findModifierString(sourceHoverModifier);	// Shift
		store.setDefault(EDITOR_TEXT_HOVER_MODIFIERS, "org.eclipse.cdt.ui.BestMatchHover;0;org.eclipse.cdt.ui.CSourceHover;" + sourceHoverModifierName); //$NON-NLS-1$
		store.setDefault(EDITOR_TEXT_HOVER_MODIFIER_MASKS, "org.eclipse.cdt.ui.BestMatchHover;0;org.eclipse.cdt.ui.CSourceHover;" + sourceHoverModifier); //$NON-NLS-1$

		store.setDefault(EDITOR_SOURCE_HOVER_BACKGROUND_COLOR_SYSTEM_DEFAULT, true);

		// Syntax highlighting
		store.setDefault(EDITOR_MULTI_LINE_COMMENT_BOLD, false);
		store.setDefault(EDITOR_MULTI_LINE_COMMENT_ITALIC, false);

		store.setDefault(EDITOR_SINGLE_LINE_COMMENT_BOLD, false);
		store.setDefault(EDITOR_SINGLE_LINE_COMMENT_ITALIC, false);

		store.setDefault(EDITOR_TASK_TAG_BOLD, true);
        store.setDefault(EDITOR_TASK_TAG_ITALIC, false);

		store.setDefault(EDITOR_C_KEYWORD_BOLD, true);
		store.setDefault(EDITOR_C_KEYWORD_ITALIC, false);

		store.setDefault(EDITOR_C_BUILTIN_TYPE_BOLD, true);
		store.setDefault(EDITOR_C_BUILTIN_TYPE_ITALIC, false);

		store.setDefault(EDITOR_C_STRING_BOLD, false);
		store.setDefault(EDITOR_C_STRING_ITALIC, false);

		store.setDefault(EDITOR_C_DEFAULT_BOLD, false);
		store.setDefault(EDITOR_C_DEFAULT_ITALIC, false);

        store.setDefault(EDITOR_C_OPERATOR_BOLD, false);
        store.setDefault(EDITOR_C_OPERATOR_ITALIC, false);

        store.setDefault(EDITOR_C_BRACES_BOLD, false);
        store.setDefault(EDITOR_C_BRACES_ITALIC, false);

        store.setDefault(EDITOR_C_NUMBER_BOLD, false);
        store.setDefault(EDITOR_C_NUMBER_ITALIC, false);

		store.setDefault(EDITOR_PP_DIRECTIVE_BOLD, true);
		store.setDefault(EDITOR_PP_DIRECTIVE_ITALIC, false);

        store.setDefault(EDITOR_PP_HEADER_BOLD, false);
        store.setDefault(EDITOR_PP_HEADER_ITALIC, false);

        store.setDefault(EDITOR_PP_DEFAULT_BOLD, false);
        store.setDefault(EDITOR_PP_DEFAULT_ITALIC, false);

		store.setDefault(EDITOR_ASM_LABEL_BOLD, true);
		store.setDefault(EDITOR_ASM_LABEL_ITALIC, false);

		store.setDefault(EDITOR_ASM_DIRECTIVE_BOLD, true);
		store.setDefault(EDITOR_ASM_DIRECTIVE_ITALIC, false);

		// Folding
		store.setDefault(EDITOR_FOLDING_ENABLED, true);
		store.setDefault(EDITOR_FOLDING_PROVIDER, "org.eclipse.cdt.ui.text.defaultFoldingProvider"); //$NON-NLS-1$
		store.setDefault(EDITOR_FOLDING_FUNCTIONS, false);
		store.setDefault(EDITOR_FOLDING_STRUCTURES, false);
		store.setDefault(EDITOR_FOLDING_METHODS, false);
		store.setDefault(EDITOR_FOLDING_MACROS, false);
		store.setDefault(EDITOR_FOLDING_COMMENTS, false);
		store.setDefault(EDITOR_FOLDING_HEADERS, true);
		store.setDefault(EDITOR_FOLDING_INACTIVE_CODE, false);
		store.setDefault(EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, false);

		// Smart edit
		store.setDefault(EDITOR_CLOSE_STRINGS, true);
		store.setDefault(EDITOR_CLOSE_BRACKETS, true);
		store.setDefault(EDITOR_CLOSE_ANGULAR_BRACKETS, true);
		store.setDefault(EDITOR_CLOSE_BRACES, true);
		store.setDefault(EDITOR_SMART_PASTE, true);
		store.setDefault(EDITOR_SMART_TAB, true);
		store.setDefault(EDITOR_WRAP_STRINGS, true);
		store.setDefault(EDITOR_ESCAPE_STRINGS, false);
		store.setDefault(EDITOR_AUTO_INDENT, true);

		store.setDefault(REMOVE_TRAILING_WHITESPACE, true);
		store.setDefault(REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES, true);
		store.setDefault(ENSURE_NEWLINE_AT_EOF, true);
		store.setDefault(PreferenceConstants.FORMAT_SOURCE_CODE, false);

		// Formatter profile
		store.setDefault(FORMATTER_PROFILE, FormatterProfileManager.DEFAULT_PROFILE);

		// Formatting behavior.
		store.setDefault(FORMATTING_SCOPE_FOR_EMPTY_SELECTION, FORMATTING_SCOPE_DOCUMENT);
		store.setDefault(FORMATTING_CONFIRM_SCOPE_FOR_EMPTY_SELECTION, true);

		// Content assist
		store.setDefault(CODEASSIST_EXCLUDED_CATEGORIES, "org.eclipse.cdt.ui.textProposalCategory\0"); //$NON-NLS-1$
		store.setDefault(CODEASSIST_CATEGORY_ORDER, "org.eclipse.cdt.ui.parserProposalCategory:65539\0org.eclipse.cdt.ui.textProposalCategory:65541\0org.eclipse.cdt.ui.templateProposalCategory:2\0org.eclipse.cdt.ui.helpProposalCategory:5\0"); //$NON-NLS-1$

		store.setDefault(CODEASSIST_PROPOSALS_TIMEOUT, 5000);

		// Refactoring.
		store.setDefault(REFACTOR_SAVE_ALL_EDITORS, false);
		store.setDefault(REFACTOR_LIGHTWEIGHT, true);

		// Spell checking
		store.setDefault(SPELLING_LOCALE, "en_US"); //$NON-NLS-1$
		String isInitializedKey= "spelling_locale_initialized"; //$NON-NLS-1$
		if (!store.getBoolean(isInitializedKey)) {
			store.setValue(isInitializedKey, true);
			Locale locale= SpellCheckEngine.getDefaultLocale();
			locale= SpellCheckEngine.findClosestLocale(locale);
			if (locale != null)
				store.setValue(SPELLING_LOCALE, locale.toString());
		}
		store.setDefault(SPELLING_IGNORE_DIGITS, true);
		store.setDefault(SPELLING_IGNORE_MIXED, true);
		store.setDefault(SPELLING_IGNORE_SENTENCE, true);
		store.setDefault(SPELLING_IGNORE_UPPER, true);
		store.setDefault(SPELLING_IGNORE_URLS, true);
		store.setDefault(SPELLING_IGNORE_SINGLE_LETTERS, true);
		store.setDefault(SPELLING_IGNORE_STRING_LITERALS, false);
		store.setDefault(SPELLING_IGNORE_NON_LETTERS, true);
		store.setDefault(SPELLING_USER_DICTIONARY, ""); //$NON-NLS-1$

		// Note: For backwards compatibility we must use the property and not the workspace default
		store.setDefault(SPELLING_USER_DICTIONARY_ENCODING,
				System.getProperty("file.encoding")); //$NON-NLS-1$

		store.setDefault(SPELLING_PROPOSAL_THRESHOLD, 20);
		store.setDefault(SPELLING_PROBLEMS_THRESHOLD, 100);
		/*
		 * TODO: This is currently disabled because the spelling engine
		 * cannot return word proposals but only correction proposals.
		 */
		store.setToDefault(SPELLING_ENABLE_CONTENTASSIST);

		// codegen
		store.setDefault(CODEGEN_ADD_COMMENTS, false);

		// mark occurrences
		store.setDefault(EDITOR_MARK_OCCURRENCES, true);
		store.setDefault(EDITOR_MARK_OVERLOADED_OPERATOR_OCCURRENCES, false);
		store.setDefault(EDITOR_STICKY_OCCURRENCES, true);

		// Scalability
		store.setDefault(SCALABILITY_ALERT, true);
		store.setDefault(SCALABILITY_NUMBER_OF_LINES, 5000);
		store.setDefault(SCALABILITY_ENABLE_ALL, false);
		store.setDefault(SCALABILITY_RECONCILER, true);
		store.setDefault(SCALABILITY_SYNTAX_COLOR, false);
		store.setDefault(SCALABILITY_SEMANTIC_HIGHLIGHT, false);
		store.setDefault(SCALABILITY_PARSER_BASED_CONTENT_ASSIST, false);
		store.setDefault(SCALABILITY_CONTENT_ASSIST_AUTO_ACTIVATION, false);

		// Code Templates
		store.setDefault(CODE_TEMPLATES_INCLUDE_GUARD_SCHEME,
				CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_PATH);

		// Name Style
		store.setDefault(NAME_STYLE_CONSTANT_CAPITALIZATION, NAME_STYLE_CAPITALIZATION_UPPER_CASE);
		store.setDefault(NAME_STYLE_CONSTANT_PREFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CONSTANT_SUFFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CONSTANT_WORD_DELIMITER, "_"); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_VARIABLE_CAPITALIZATION, NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE);
		store.setDefault(NAME_STYLE_VARIABLE_PREFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_VARIABLE_SUFFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_VARIABLE_WORD_DELIMITER, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_FIELD_CAPITALIZATION, NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE);
		store.setDefault(NAME_STYLE_FIELD_PREFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_FIELD_SUFFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_FIELD_WORD_DELIMITER, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_METHOD_CAPITALIZATION, NAME_STYLE_CAPITALIZATION_LOWER_CAMEL_CASE);
		store.setDefault(NAME_STYLE_METHOD_PREFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_METHOD_SUFFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_METHOD_WORD_DELIMITER, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_GETTER_CAPITALIZATION, NAME_STYLE_CAPITALIZATION_CAMEL_CASE);
		store.setDefault(NAME_STYLE_GETTER_PREFIX, "get"); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_GETTER_PREFIX_FOR_BOOLEAN, "is"); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_GETTER_SUFFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_GETTER_WORD_DELIMITER, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_SETTER_CAPITALIZATION, NAME_STYLE_CAPITALIZATION_CAMEL_CASE);
		store.setDefault(NAME_STYLE_SETTER_PREFIX, "set"); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_SETTER_SUFFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_SETTER_WORD_DELIMITER, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CPP_HEADER_CAPITALIZATION, NAME_STYLE_CAPITALIZATION_ORIGINAL);
		store.setDefault(NAME_STYLE_CPP_HEADER_PREFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CPP_HEADER_SUFFIX, ".h"); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CPP_HEADER_WORD_DELIMITER, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CPP_SOURCE_CAPITALIZATION, NAME_STYLE_CAPITALIZATION_ORIGINAL);
		store.setDefault(NAME_STYLE_CPP_SOURCE_PREFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CPP_SOURCE_SUFFIX, ".cpp"); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CPP_SOURCE_WORD_DELIMITER, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CPP_TEST_CAPITALIZATION, NAME_STYLE_CAPITALIZATION_ORIGINAL);
		store.setDefault(NAME_STYLE_CPP_TEST_PREFIX, ""); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CPP_TEST_SUFFIX, "_test.cpp"); //$NON-NLS-1$
		store.setDefault(NAME_STYLE_CPP_TEST_WORD_DELIMITER, ""); //$NON-NLS-1$

		// Code style
		store.setDefault(CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER, false);
		store.setDefault(FUNCTION_OUTPUT_PARAMETERS_BEFORE_INPUT, false);
		store.setDefault(FUNCTION_PASS_OUTPUT_PARAMETERS_BY_POINTER, false);

		// Colors that are set by the current theme
		CUIPreferenceInitializer.setThemeBasedPreferences(store, false);
    }

    /**
     * Returns the node in the preference in the given context.
     *
     * @param key The preference key.
     * @param project The current context or {@code null} if no context is available and
     *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
     * @return Returns the node matching the given context.
     */
	private static IEclipsePreferences getPreferenceNode(String key, IProject project) {
		IEclipsePreferences node = null;

		if (project != null) {
			node = new ProjectScope(project).getNode(CUIPlugin.PLUGIN_ID);
			if (node.get(key, null) != null) {
				return node;
			}
		}
		node = InstanceScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID);
		if (node.get(key, null) != null) {
			return node;
		}

		node = ConfigurationScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID);
		if (node.get(key, null) != null) {
			return node;
		}

		return DefaultScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID);
	}

	/**
	 * Returns the string value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @return Returns the current value for the string.
	 * @since 5.9
	 */
	public static String getPreference(String key, IProject project) {
		return getPreference(key, project, null);
	}

	/**
	 * Returns the string value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @return Returns the current value for the string.
	 * @since 5.0
	 */
	public static String getPreference(String key, ICProject project) {
		return getPreference(key, project.getProject());
	}

	/**
	 * Returns the string value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.9
	 */
	public static String getPreference(String key, IProject project, String defaultValue) {
		return getPreferenceNode(key, project).get(key, defaultValue);
	}

	/**
	 * Returns the string value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.6
	 */
	public static String getPreference(String key, ICProject project, String defaultValue) {
		return getPreference(key, project.getProject(), defaultValue);
	}

	/**
	 * Returns the integer value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should
	 *     be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.9
	 */
	public static int getPreference(String key, IProject project, int defaultValue) {
		return getPreferenceNode(key, project).getInt(key, defaultValue);
	}

	/**
	 * Returns the integer value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should
	 *     be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.1
	 */
	public static int getPreference(String key, ICProject project, int defaultValue) {
		return getPreference(key, project.getProject(), defaultValue);
	}

	/**
	 * Returns the boolean value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.9
	 */
	public static boolean getPreference(String key, IProject project, boolean defaultValue) {
		return getPreferenceNode(key, project).getBoolean(key, defaultValue);
	}

	/**
	 * Returns the boolean value for the given key in the given context.
	 *
	 * @param key The preference key
	 * @param project The current context or {@code null} if no context is available and
	 *     the workspace setting should be taken. Note that passing {@code null} should be avoided.
	 * @param defaultValue The default value if not specified in the preferences.
	 * @return Returns the current value of the preference.
	 * @since 5.1
	 */
	public static boolean getPreference(String key, ICProject project, boolean defaultValue) {
		return getPreference(key, project.getProject(), defaultValue);
	}

	/**
	 * Returns the scopes for preference lookup.
	 *
	 * @param project a project or {@code null}
	 * @return the scopes for preference lookup.
	 * @since 5.4
	 */
	public static IScopeContext[] getPreferenceScopes(IProject project) {
		return CCorePreferenceConstants.getPreferenceScopes(project);
	}
}
