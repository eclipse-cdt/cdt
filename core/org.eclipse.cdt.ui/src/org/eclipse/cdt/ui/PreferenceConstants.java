/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import org.eclipse.cdt.internal.ui.text.ICColorConstants;

/**
 * Preference constants used in the CDT-UI preference store. Clients should only read the
 * CDT-UI preference store using these values. Clients are not allowed to modify the 
 * preference store programmatically.
 * 
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
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_MULTI_LINE_COMMENT_COLOR= ICColorConstants.C_MULTI_LINE_COMMENT;

	/**
	 * A named preference that controls whether multi-line comments are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> multi-line comments are rendered
	 * in bold. If <code>false</code> the are rendered using no font style attribute.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_MULTI_LINE_COMMENT_BOLD= ICColorConstants.C_MULTI_LINE_COMMENT + EDITOR_BOLD_SUFFIX; 

	/**
	 * A named preference that controls whether multi-line comments are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> multi-line comments are rendered
	 * in italic. If <code>false</code> the are rendered using no italic font style attribute.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_MULTI_LINE_COMMENT_ITALIC= ICColorConstants.C_MULTI_LINE_COMMENT + EDITOR_ITALIC_SUFFIX;
	
	/**
	 * A named preference that holds the color used to render single line comments.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_COLOR= ICColorConstants.C_SINGLE_LINE_COMMENT;

	/**
	 * A named preference that controls whether single line comments are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single line comments are rendered
	 * in bold. If <code>false</code> the are rendered using no font style attribute.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_BOLD= ICColorConstants.C_SINGLE_LINE_COMMENT + EDITOR_BOLD_SUFFIX; 

	/**
	 * A named preference that controls whether single line comments are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single line comments are rendered
	 * in italic. If <code>false</code> the are rendered using no italic font style attribute.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_ITALIC= ICColorConstants.C_SINGLE_LINE_COMMENT + EDITOR_ITALIC_SUFFIX;
	
	/**
	 * A named preference that holds the color used to render C/C++ keywords.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_KEYWORD_COLOR= ICColorConstants.C_KEYWORD;

	/**
	 * A named preference that controls whether keywords are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_KEYWORD_BOLD= ICColorConstants.C_KEYWORD + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether keywords are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_KEYWORD_ITALIC= ICColorConstants.C_KEYWORD + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render preprocessor directives.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DIRECTIVE_COLOR= ICColorConstants.PP_DIRECTIVE;

	/**
	 * A named preference that controls whether preprocessor directives are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DIRECTIVE_BOLD= ICColorConstants.PP_DIRECTIVE + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether preprocessor directives are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DIRECTIVE_ITALIC= ICColorConstants.PP_DIRECTIVE + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render headers.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_PP_HEADER_COLOR= ICColorConstants.PP_HEADER;	

	/**
	 * A named preference that controls whether headers are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_PP_HEADER_BOLD= ICColorConstants.PP_HEADER + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether number are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_PP_HEADER_ITALIC= ICColorConstants.PP_HEADER + EDITOR_ITALIC_SUFFIX;
	
	/**
	 * A named preference that holds the color used to render preprocessor text.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DEFAULT_COLOR= ICColorConstants.PP_DEFAULT;

	/**
	 * A named preference that controls whether preprocessor text is rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DEFAULT_BOLD= ICColorConstants.PP_DEFAULT + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether preprocessor text is rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_PP_DEFAULT_ITALIC= ICColorConstants.PP_DEFAULT + EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that holds the color used to render builtin types.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_BUILTIN_TYPE_COLOR= ICColorConstants.C_TYPE;

	/**
	 * A named preference that controls whether builtin types are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_BUILTIN_TYPE_BOLD= ICColorConstants.C_TYPE + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether builtin types are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_BUILTIN_TYPE_ITALIC= ICColorConstants.C_TYPE + EDITOR_ITALIC_SUFFIX;
	
	/**
	 * A named preference that holds the color used to render string constants.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_STRING_COLOR= ICColorConstants.C_STRING;

	/**
	 * A named preference that controls whether string constants are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_STRING_BOLD= ICColorConstants.C_STRING + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether string constants are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_STRING_ITALIC= ICColorConstants.C_STRING + EDITOR_ITALIC_SUFFIX;
	
	/**
	 * A named preference that holds the color used to render operators.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_OPERATOR_COLOR= ICColorConstants.C_OPERATOR;	
 
	/**
	 * A named preference that controls whether operators are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_OPERATOR_BOLD= ICColorConstants.C_OPERATOR + EDITOR_BOLD_SUFFIX;
	
	/**
	 * A named preference that controls whether operators are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_OPERATOR_ITALIC= ICColorConstants.C_OPERATOR + EDITOR_ITALIC_SUFFIX;
	
	/**
	 * A named preference that holds the color used to render numbers.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_NUMBER_COLOR= ICColorConstants.C_NUMBER;	

	/**
	 * A named preference that controls whether number are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_NUMBER_BOLD= ICColorConstants.C_NUMBER + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether number are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_NUMBER_ITALIC= ICColorConstants.C_NUMBER + EDITOR_ITALIC_SUFFIX;
	
	/**
	 * A named preference that holds the color used to render braces.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_BRACES_COLOR= ICColorConstants.C_BRACES;	

	/**
	 * A named preference that controls whether braces are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_BRACES_BOLD= ICColorConstants.C_BRACES + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether braces are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_BRACES_ITALIC= ICColorConstants.C_BRACES + EDITOR_ITALIC_SUFFIX;
	
	/**
	 * A named preference that holds the color used to render C/C++ default text.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public final static String EDITOR_C_DEFAULT_COLOR= ICColorConstants.C_DEFAULT;

	/**
	 * A named preference that controls whether C/C++ default text is rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_DEFAULT_BOLD= ICColorConstants.C_DEFAULT + EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether C/C++ default text is rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_C_DEFAULT_ITALIC= ICColorConstants.C_DEFAULT + EDITOR_ITALIC_SUFFIX;

	/**
	 * The symbolic font name for the C/C++ editor text font 
	 * (value <code>"org.eclipse.cdt.ui.editors.textfont"</code>).
	 * 
	 * @since 4.0
	 */
	public final static String EDITOR_TEXT_FONT= "org.eclipse.cdt.ui.editors.textfont"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the cview's selection is linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String PREF_LINK_TO_EDITOR= "org.eclipse.cdt.ui.editor.linkToEditor"; //$NON-NLS-1$

	/**
	 * A named preference that speficies whether children of a translation unit are shown in the CView.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String PREF_SHOW_CU_CHILDREN= "org.eclipse.cdt.ui.editor.CUChildren"; //$NON-NLS-1$

	/**
	 * A named preference that speficies whether to use the parser's structural mode to build the CModel.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String PREF_USE_STRUCTURAL_PARSE_MODE= "org.eclipse.cdt.ui.editor.UseStructuralMode"; //$NON-NLS-1$

	/**
	 * A named preference that controls if segmented view (show selected element only) is turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String EDITOR_SHOW_SEGMENTS= "org.eclipse.cdt.ui.editor.showSegments"; //$NON-NLS-1$
    
    /**
     * A named preference that holds the color used to render task tags.
     * <p>
     * Value is of type <code>String</code>. A RGB color value encoded as a string
     * using class <code>PreferenceConverter</code>
     * </p>
     * 
     * @see org.eclipse.jface.resource.StringConverter
     * @see org.eclipse.jface.preference.PreferenceConverter
     */
    public final static String EDITOR_TASK_TAG_COLOR= ICColorConstants.TASK_TAG;

    /**
     * A named preference that controls whether task tags are rendered in bold.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public final static String EDITOR_TASK_TAG_BOLD= ICColorConstants.TASK_TAG + EDITOR_BOLD_SUFFIX;
     
    /**
     * A named preference that controls whether task tags are rendered in italic.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public final static String EDITOR_TASK_TAG_ITALIC= ICColorConstants.TASK_TAG + EDITOR_ITALIC_SUFFIX;

    /**
	 * A named preference that controls if correction indicators are shown in the UI.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CORRECTION_INDICATION= "CEditor.ShowTemporaryProblem"; //$NON-NLS-1$

	/**
	 * A named preference that controls if temporary problems are evaluated and shown in the UI.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_EVALUATE_TEMPORARY_PROBLEMS= "handleTemporaryProblems"; //$NON-NLS-1$

	/**
	 * A named preference that defines the key for the hover modifiers.
	 *
	 */
	public static final String EDITOR_TEXT_HOVER_MODIFIERS= "hoverModifiers"; //$NON-NLS-1$

	/**
	 * A named preference that defines the key for the hover modifier state masks.
	 * The value is only used if the value of <code>EDITOR_TEXT_HOVER_MODIFIERS</code>
	 * cannot be resolved to valid SWT modifier bits.
	 * 
	 * @see #EDITOR_TEXT_HOVER_MODIFIERS
	 */
	public static final String EDITOR_TEXT_HOVER_MODIFIER_MASKS= "hoverModifierMasks"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close strings' feature
	 *  is   enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CLOSE_STRINGS= "closeStrings"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'wrap strings' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_WRAP_STRINGS= "wrapStrings"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'escape strings' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_ESCAPE_STRINGS= "escapeStrings"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close brackets' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CLOSE_BRACKETS= "closeBrackets"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close angular brackets' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CLOSE_ANGULAR_BRACKETS= "closeAngularBrackets"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'close braces' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CLOSE_BRACES= "closeBraces"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the 'smart paste' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_SMART_PASTE= "smartPaste"; //$NON-NLS-1$

	/**
	 * A named preference that controls the smart tab behavior.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 */
	public static final String EDITOR_SMART_TAB= "smart_tab"; //$NON-NLS-1$

	/**
	 * The id of the best match hover contributed for extension point
	 * <code>org.eclipse.cdt.ui.textHovers</code>.
	 *
	 * @since 2.1
	 */
	public static final String ID_BESTMATCH_HOVER= "org.eclipse.cdt.ui.BestMatchHover"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the Outline view.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String OUTLINE_GROUP_INCLUDES= "org.eclipse.cdt.ui.outline.groupincludes"; //$NON-NLS-1$

	/**
	 * A named preference that controls if the Outline view.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String OUTLINE_GROUP_NAMESPACES= "org.eclipse.cdt.ui.outline.groupnamespaces"; //$NON-NLS-1$

	
	/**
	 * A named preference that controls whether the outline view 
	 * selection should stay in sync with with the element at the current cursor position.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String OUTLINE_LINK_TO_EDITOR = "org.eclipse.cdt.ui.outline.linktoeditor"; //$NON-NLS-1$
	
	/**
	 * A named preference that controls if the CView.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String CVIEW_GROUP_INCLUDES= "org.eclipse.cdt.ui.cview.groupincludes"; //$NON-NLS-1$

	/**
	 * A named preference that controls which completion proposal categories
	 * have been excluded from the default proposal list.
	 * <p>
	 * Value is of type <code>String</code>, a "\0"-separated list of identifiers.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String CODEASSIST_EXCLUDED_CATEGORIES= "content_assist_disabled_computers"; //$NON-NLS-1$

	/**
	 * A named preference that controls the order of the specific code assist commands.
	 * <p>
	 * Value is of type <code>String</code>, a "\0"-separated list of identifiers.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String CODEASSIST_CATEGORY_ORDER= "content_assist_category_order"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether folding is enabled in the C editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 */
	public static final String EDITOR_FOLDING_ENABLED= "editor_folding_enabled"; //$NON-NLS-1$
	
	/**
	 * A named preference that stores the configured folding provider.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 */
	public static final String EDITOR_FOLDING_PROVIDER= "editor_folding_provider"; //$NON-NLS-1$
	
	/**
	 * A named preference that stores the value for Structure folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_STRUCTURES= "editor_folding_default_structures"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for functions folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_FUNCTIONS= "editor_folding_default_functions"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for method folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_METHODS= "editor_folding_default_methods"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for macros folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_MACROS= "editor_folding_default_macros"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for comment folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_FOLDING_COMMENTS= "editor_folding_default_comments"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for header comment folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_FOLDING_HEADERS= "editor_folding_default_headers"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for inactive code folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_FOLDING_INACTIVE_CODE= "editor_folding_default_inactive"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether folding of preprocessor branches is enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED= "editor_folding_preprocessor_enabled"; //$NON-NLS-1$
	
	/**
	 * A named preference that controls if templates are formatted when applied.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 2.1
	 */	
	public static final String TEMPLATES_USE_CODEFORMATTER= "org.eclipse.cdt.ui.text.templates.format"; //$NON-NLS-1$

	/**
	 * A named preference that controls which profile is used by the code formatter.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 *
	 * @since 4.0
	 */	
	public static final String FORMATTER_PROFILE = "formatter_profile"; //$NON-NLS-1$

	/** 
	 * Preference key for whether to ensure a newline at the end of files when saving.
	 * 
	 * @since 4.0
	 */
	public final static String ENSURE_NEWLINE_AT_EOF = "ensureNewlineAtEOF"; //$NON-NLS-1$	

	/**
	 * A named preference that defines whether the hint to make hover sticky should be shown.
	 *
	 * @since 3.1.1
	 * @deprecated As of 4.0, replaced by {@link AbstractDecoratedTextEditorPreferenceConstants#EDITOR_SHOW_TEXT_HOVER_AFFORDANCE}
	 */
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
	 * Value is of type <code>String</code>. A RGB color value encoded as a string
	 * using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX=".color"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the text attribute bold.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if bold.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX=".bold"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the text attribute italic.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if italic.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX=".italic"; //$NON-NLS-1$
	
	/**
	 * A named preference suffix that controls if semantic highlighting has the text attribute strikethrough.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if strikethrough.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX=".strikethrough"; //$NON-NLS-1$
	
	/**
	 * A named preference suffix that controls if semantic highlighting has the text attribute underline.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if underline.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX=".underline"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting is enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if enabled.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX=".enabled"; //$NON-NLS-1$

	/**
	 * A named preference key that controls if semantic highlighting is enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if enabled.
	 * </p>
	 * 
	 * @since 4.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED= "semanticHighlighting.enabled"; //$NON-NLS-1$

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

		store.setDefault(PreferenceConstants.EDITOR_CORRECTION_INDICATION, false);
		store.setDefault(PreferenceConstants.EDITOR_SHOW_SEGMENTS, false);
		store.setDefault(PreferenceConstants.PREF_SHOW_CU_CHILDREN, true);
		
		// Turned off by default since there are too many false reports right now 
		store.setDefault(PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS, false);
		
		int sourceHoverModifier= SWT.MOD2;
		String sourceHoverModifierName= Action.findModifierString(sourceHoverModifier);	// Shift
		store.setDefault(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS, "org.eclipse.cdt.ui.BestMatchHover;0;org.eclipse.cdt.ui.CSourceHover;" + sourceHoverModifierName); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS, "org.eclipse.cdt.ui.BestMatchHover;0;org.eclipse.cdt.ui.CSourceHover;" + sourceHoverModifier); //$NON-NLS-1$
		
		// coloring
		PreferenceConverter.setDefault(store, EDITOR_MULTI_LINE_COMMENT_COLOR, new RGB(63, 127, 95));
		store.setDefault(EDITOR_MULTI_LINE_COMMENT_BOLD, false);
		store.setDefault(EDITOR_MULTI_LINE_COMMENT_ITALIC, false);

		PreferenceConverter.setDefault(store, EDITOR_SINGLE_LINE_COMMENT_COLOR, new RGB(63, 127, 95));
		store.setDefault(EDITOR_SINGLE_LINE_COMMENT_BOLD, false);
		store.setDefault(EDITOR_SINGLE_LINE_COMMENT_ITALIC, false);

        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_TASK_TAG_COLOR, new RGB(127, 159, 191));
        store.setDefault(PreferenceConstants.EDITOR_TASK_TAG_BOLD, true);
        store.setDefault(PreferenceConstants.EDITOR_TASK_TAG_ITALIC, false);

        PreferenceConverter.setDefault(store, EDITOR_C_KEYWORD_COLOR, new RGB(127, 0, 85));
		store.setDefault(EDITOR_C_KEYWORD_BOLD, true);
		store.setDefault(EDITOR_C_KEYWORD_ITALIC, false);

		PreferenceConverter.setDefault(store, EDITOR_C_BUILTIN_TYPE_COLOR, new RGB(127, 0, 85));
		store.setDefault(EDITOR_C_BUILTIN_TYPE_BOLD, true);
		store.setDefault(EDITOR_C_BUILTIN_TYPE_ITALIC, false);

		PreferenceConverter.setDefault(store, EDITOR_C_STRING_COLOR, new RGB(42, 0, 255));
		store.setDefault(EDITOR_C_STRING_BOLD, false);
		store.setDefault(EDITOR_C_STRING_ITALIC, false);

		PreferenceConverter.setDefault(store, EDITOR_C_DEFAULT_COLOR, new RGB(0, 0, 0));
		store.setDefault(EDITOR_C_DEFAULT_BOLD, false);
		store.setDefault(EDITOR_C_DEFAULT_ITALIC, false);

        PreferenceConverter.setDefault(store, EDITOR_C_OPERATOR_COLOR, new RGB(0, 0, 0));
        store.setDefault(EDITOR_C_OPERATOR_BOLD, false);
        store.setDefault(EDITOR_C_OPERATOR_ITALIC, false);

        PreferenceConverter.setDefault(store, EDITOR_C_BRACES_COLOR, new RGB(0, 0, 0));
        store.setDefault(EDITOR_C_BRACES_BOLD, false);
        store.setDefault(EDITOR_C_BRACES_ITALIC, false);

        PreferenceConverter.setDefault(store, EDITOR_C_NUMBER_COLOR, new RGB(0, 0, 0));
        store.setDefault(EDITOR_C_NUMBER_BOLD, false);
        store.setDefault(EDITOR_C_NUMBER_ITALIC, false);

        PreferenceConverter.setDefault(store, EDITOR_PP_DIRECTIVE_COLOR, new RGB(127, 0, 85));
		store.setDefault(EDITOR_PP_DIRECTIVE_BOLD, true);
		store.setDefault(EDITOR_PP_DIRECTIVE_ITALIC, false);

        PreferenceConverter.setDefault(store, EDITOR_PP_HEADER_COLOR, new RGB(42, 0, 255));
        store.setDefault(EDITOR_PP_HEADER_BOLD, false);
        store.setDefault(EDITOR_PP_HEADER_ITALIC, false);

        PreferenceConverter.setDefault(store, EDITOR_PP_DEFAULT_COLOR, new RGB(0, 0, 0));
        store.setDefault(EDITOR_PP_DEFAULT_BOLD, false);
        store.setDefault(EDITOR_PP_DEFAULT_ITALIC, false);

		// folding
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED, false);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_PROVIDER, "org.eclipse.cdt.ui.text.defaultFoldingProvider"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_FUNCTIONS, false);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_STRUCTURES, true);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_METHODS, false);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_MACROS, true);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_COMMENTS, false);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_HEADERS, true);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_INACTIVE_CODE, true);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_PREPROCESSOR_BRANCHES_ENABLED, false);

		// smart edit
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_STRINGS, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACKETS, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_ANGULAR_BRACKETS, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACES, true);
		store.setDefault(PreferenceConstants.EDITOR_SMART_PASTE, true);
		store.setDefault(PreferenceConstants.EDITOR_SMART_TAB, true);
		store.setDefault(PreferenceConstants.EDITOR_WRAP_STRINGS, true);
		store.setDefault(PreferenceConstants.EDITOR_ESCAPE_STRINGS, false);
		
		store.setDefault(PreferenceConstants.ENSURE_NEWLINE_AT_EOF, false);
		
		// content assist
		store.setDefault(PreferenceConstants.CODEASSIST_EXCLUDED_CATEGORIES, "org.eclipse.cdt.ui.textProposalCategory\0"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.CODEASSIST_CATEGORY_ORDER, "org.eclipse.cdt.ui.parserProposalCategory:65539\0org.eclipse.cdt.ui.textProposalCategory:65541\0org.eclipse.cdt.ui.templateProposalCategory:2\0"); //$NON-NLS-1$
    }
}
