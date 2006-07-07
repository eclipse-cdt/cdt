/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.ui;

import org.eclipse.cdt.internal.ui.text.ICColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

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
     */
    public static final String EDITOR_BOLD_SUFFIX= "_bold"; //$NON-NLS-1$

	/**
	 * A named preference that controls if comment stubs will be added
	 * automatically to newly created types and methods.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * @since 2.1
	 */
	public static final String CODEGEN_ADD_COMMENTS= "org.eclipse.cdt.ui.javadoc"; //$NON-NLS-1$
	
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
     * A named preference that controls whether the editor shows task indicators in text (squiggly lines). 
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public final static String EDITOR_TASK_INDICATION= "taskIndication"; //$NON-NLS-1$

    /**
     * A named preference that holds the color used to render task indicators.
     * <p>
     * Value is of type <code>String</code>. A RGB color value encoded as a string
     * using class <code>PreferenceConverter</code>
     * </p>
     * 
     * @see #EDITOR_TASK_INDICATION
     * @see org.eclipse.jface.resource.StringConverter
     * @see org.eclipse.jface.preference.PreferenceConverter
     */
    public final static String EDITOR_TASK_INDICATION_COLOR= "taskIndicationColor"; //$NON-NLS-1$
    
    /**
     * A named preference that controls whether the overview ruler shows task
     * indicators.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     * @since 2.1
     */
    public final static String EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER= "taskIndicationInOverviewRuler"; //$NON-NLS-1$ 
 
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
	 * The id of the best match hover contributed for extension point
	 * <code>javaEditorTextHovers</code>.
	 *
	 * @since 2.1
	 */
	public static final String ID_BESTMATCH_HOVER= "org.eclipse.cdt.ui.BestMatchHover"; //$NON-NLS-1$


    public static final String REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD= "Refactoring.ErrorPage.severityThreshold"; //$NON-NLS-1$
    public static final String REFACTOR_FATAL_SEVERITY= "4"; //$NON-NLS-1$
    public static final String REFACTOR_ERROR_SEVERITY= "3"; //$NON-NLS-1$
    public static final String REFACTOR_WARNING_SEVERITY= "2"; //$NON-NLS-1$
    public static final String REFACTOR_INFO_SEVERITY= "1"; //$NON-NLS-1$
    public static final String REFACTOR_OK_SEVERITY= "0"; //$NON-NLS-1$
    public static final String REFACTOR_SAVE_ALL_EDITORS= "Refactoring.savealleditors"; //$NON-NLS-1$
    
    
	/**
	 * A named preference that controls if the C Browsing views are linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String BROWSING_LINK_VIEW_TO_EDITOR= "org.eclipse.cdt.ui.browsing.linktoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls the layout of the C Browsing views vertically. Boolean value.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true<code> the views are stacked vertical.
	 * If <code>false</code> they are stacked horizontal.
	 * </p>
	 */
	public static final String BROWSING_STACK_VERTICALLY= "org.eclipse.cdt.ui.browsing.stackVertically"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the hierarchy view's selection is linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String LINK_TYPEHIERARCHY_TO_EDITOR= "org.eclipse.cdt.ui.packages.linktypehierarchytoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the projects view's selection is
	 * linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * @since 2.1
	 */
	public static final String LINK_BROWSING_PROJECTS_TO_EDITOR= "org.eclipse.cdt.ui.browsing.projectstoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the namespace view's selection is
	 * linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * @since 2.1
	 */
	public static final String LINK_BROWSING_PACKAGES_TO_EDITOR= "org.eclipse.cdt.ui.browsing.packagestoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the types view's selection is
	 * linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * @since 2.1
	 */
	public static final String LINK_BROWSING_TYPES_TO_EDITOR= "org.eclipse.cdt.ui.browsing.typestoeditor"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether the members view's selection is
	 * linked to the active editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * @since 2.1
	 */
	public static final String LINK_BROWSING_MEMBERS_TO_EDITOR= "org.eclipse.cdt.ui.browsing.memberstoeditor"; //$NON-NLS-1$
    
	/**
	 * A named preference that controls if a new type hierarchy gets opened in a 
	 * new type hierarchy perspective or inside the type hierarchy view part.
	 * <p>
	 * Value is of type <code>String</code>: possible values are <code>
	 * OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE</code> or <code>
	 * OPEN_TYPE_HIERARCHY_IN_VIEW_PART</code>.
	 * </p>
	 * 
	 * @see #OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE
	 * @see #OPEN_TYPE_HIERARCHY_IN_VIEW_PART
	 */
	public static final String OPEN_TYPE_HIERARCHY= "org.eclipse.cdt.ui.openTypeHierarchy"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference <code>OPEN_TYPE_HIERARCHY</code>.
	 * 
	 * @see #OPEN_TYPE_HIERARCHY
	 */
	public static final String OPEN_TYPE_HIERARCHY_IN_PERSPECTIVE= "perspective"; //$NON-NLS-1$

	/**
	 * A string value used by the named preference <code>OPEN_TYPE_HIERARCHY</code>.
	 * 
	 * @see #OPEN_TYPE_HIERARCHY
	 */
	public static final String OPEN_TYPE_HIERARCHY_IN_VIEW_PART= "viewPart"; //$NON-NLS-1$

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
	 * A named preference that controls if the CView.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String CVIEW_GROUP_INCLUDES= "org.eclipse.cdt.ui.cview.groupincludes"; //$NON-NLS-1$

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
	 * A named preference that controls if templates are formatted when applied.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 2.1
	 */	
	public static final String TEMPLATES_USE_CODEFORMATTER= "org.eclipse.cdt.ui.text.templates.format"; //$NON-NLS-1$

	/**
	 * Returns the CDT-UI preference store.
	 * 
	 * @return the CDT-UI preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * A named preference that defines whether the hint to make hover sticky should be shown.
	 *
	 * @see JavaUI
	 * @since 3.1.1
	 */
	public static final String EDITOR_SHOW_TEXT_HOVER_AFFORDANCE= "PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE"; //$NON-NLS-1$


    /**
     * Initializes the given preference store with the default values.
     * 
     * @param store the preference store to be initialized
     */
    public static void initializeDefaultValues(IPreferenceStore store) {
        store.setDefault(PreferenceConstants.EDITOR_TASK_INDICATION, false);
        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_TASK_INDICATION_COLOR, new RGB(0, 128, 255));
        store.setDefault(PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER, true);
        
        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_TASK_TAG_COLOR, new RGB(127, 159, 191));
        store.setDefault(PreferenceConstants.EDITOR_TASK_TAG_BOLD, true);

		store.setDefault(PreferenceConstants.EDITOR_CORRECTION_INDICATION, false);
		
		// Turned off by default since there are too many false reports right now 
		store.setDefault(PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS, false);
		
		String mod1Name= Action.findModifierString(SWT.MOD1);	// SWT.COMMAND on Mac; SWT.CONTROL elsewhere
		store.setDefault(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS, "org.eclipse.cdt.ui.BestMatchHover;0;org.eclipse.cdt.ui.CSourceHover;" + mod1Name); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS, "org.eclipse.cdt.ui.BestMatchHover;0;org.eclipse.cdt.ui.CSourceHover;" + SWT.MOD1); //$NON-NLS-1$
		
		store.setDefault(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE, true);

		// folding
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED, false);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_PROVIDER, "org.eclipse.cdt.ui.text.defaultFoldingProvider"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_FUNCTIONS, false);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_STRUCTURES, true);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_METHODS, false);
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_MACROS, true);


    }
}
