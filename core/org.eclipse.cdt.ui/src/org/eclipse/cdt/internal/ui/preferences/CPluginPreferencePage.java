package org.eclipse.cdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The page for setting c plugin preferences.
 */
public class CPluginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	

	private static final String LINK_TO_EDITOR_LABEL= "CBasePreferencePage.linkToEditor.label";
	private static final String SHOW_CU_CHILDREN_LABEL= "CBasePreferencePage.CUChildren.label";
	private static final String USE_NEW_PARSER_LABEL= "CBasePreferencePage.useNewParser.label";

	public CPluginPreferencePage() {
		super(GRID);
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
	}
	
	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), ICHelpContextIds.C_PREF_PAGE);
	}	

	/**
	 * @see FieldEditorPreferencePage#createControl(Composite)
	 */	
	protected void createFieldEditors() {
		Composite parent= getFieldEditorParent();

		BooleanFieldEditor linkEditor= new BooleanFieldEditor(PreferenceConstants.PREF_LINK_TO_EDITOR, CUIPlugin.getResourceString(LINK_TO_EDITOR_LABEL), parent);
		addField(linkEditor);

		BooleanFieldEditor showCUChildrenEditor= new BooleanFieldEditor(PreferenceConstants.PREF_SHOW_CU_CHILDREN, CUIPlugin.getResourceString(SHOW_CU_CHILDREN_LABEL), parent);
		addField(showCUChildrenEditor);

		BooleanFieldEditor useNewParserEditor= new BooleanFieldEditor(CCorePlugin.PREF_USE_NEW_PARSER, CUIPlugin.getResourceString(USE_NEW_PARSER_LABEL), parent);
		addField(useNewParserEditor);
	}
	

	public static boolean isLinkToEditor() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_LINK_TO_EDITOR);
	}

	public static boolean showCompilationUnitChildren() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_SHOW_CU_CHILDREN);
	}
	
	public static boolean useNewParser() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(CCorePlugin.PREF_USE_NEW_PARSER);
	}
	
	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
		CUIPlugin.getDefault().getPreferenceStore().setValue(CCorePlugin.PREF_USE_NEW_PARSER, CCorePlugin.getDefault().useNewParser());
	}
	
	/**
	 * Initializes the default values of this page in the preference bundle.
	 */
	public static void initDefaults(IPreferenceStore prefs) {
		prefs.setDefault(PreferenceConstants.PREF_LINK_TO_EDITOR, true);
		prefs.setDefault(PreferenceConstants.PREF_SHOW_CU_CHILDREN, true);
		prefs.setDefault(CCorePlugin.PREF_USE_NEW_PARSER, CCorePlugin.getDefault().useNewParser());
		prefs.setDefault(PreferenceConstants.EDITOR_SHOW_SEGMENTS, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (!super.performOk())
			return false;
		
		CCorePlugin.getDefault().setUseNewParser(useNewParser());
		return true;
	}

}
