/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.ui;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

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
	 * A named preference that speficies whether children of a translation unit are shown in the package explorer.
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


    public static final String REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD= "Refactoring.ErrorPage.severityThreshold"; //$NON-NLS-1$
    public static final String REFACTOR_FATAL_SEVERITY= "4"; //$NON-NLS-1$
    public static final String REFACTOR_ERROR_SEVERITY= "3"; //$NON-NLS-1$
    public static final String REFACTOR_WARNING_SEVERITY= "2"; //$NON-NLS-1$
    public static final String REFACTOR_INFO_SEVERITY= "1"; //$NON-NLS-1$
    public static final String REFACTOR_OK_SEVERITY= "0"; //$NON-NLS-1$
    public static final String REFACTOR_SAVE_ALL_EDITORS= "Refactoring.savealleditors"; //$NON-NLS-1$
    
	/**
	 * Returns the JDT-UI preference store.
	 * 
	 * @return the JDT-UI preference store
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
        store.setDefault(PreferenceConstants.EDITOR_TASK_INDICATION, false);
        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_TASK_INDICATION_COLOR, new RGB(0, 128, 255));
        store.setDefault(PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER, true);
        
        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_TASK_TAG_COLOR, new RGB(127, 159, 191));
        store.setDefault(PreferenceConstants.EDITOR_TASK_TAG_BOLD, true);

		store.setDefault(PreferenceConstants.EDITOR_CORRECTION_INDICATION, false);
		store.setDefault(PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS, false);

    }
}