package org.eclipse.cdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The page for setting c plugin preferences.
 */
public class CPluginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String PREF_CONSOLE_FONT= "consoleFont";
	
//	private static final String PREF_BUILD_LOCATION= "buildLocation";
//	private static final String PREF_STOP_ON_ERROR= "stopOnError";
	private static final String PREF_CLEAR_CONSOLE= "clearConsole";
	private static final String PREF_CONSOLE_ON_TOP= "consoleOnTop";
	private static final String PREF_AUTO_OPEN_CONSOLE = "autoOpenConsole";
	private static final String PREF_LINK_TO_EDITOR= "linkToEditor";
	public static final String SHOW_CU_CHILDREN="CUChildren"; //$NON-NLS-1$
		
	private static final String PAGE_DESC= "CBasePreferencePage.description";
//	private static final String BUILD_LOC_LABEL= "CBasePreferencePage.buildLocation.label";
	private static final String CLEAR_CONSOLE_LABEL= "CBasePreferencePage.clearConsole.label";
	private static final String CONSOLE_ON_TOP_LABEL= "CBasePreferencePage.consoleOnTop.label";
	private static final String AUTO_OPEN_CONSOLE_LABEL= "CBasePreferencePage.autoOpenConsole.label";
	private static final String LINK_TO_EDITOR_LABEL= "CBasePreferencePage.linkToEditor.label";
	private static final String SHOW_CU_CHILDREN_LABEL= "CBasePreferencePage.CUChildren.label";
	//private static final String EDITOR_FONT_LABEL= "CBasePreferencePage.editorFont.label";
	private static final String CONSOLE_FONT_LABEL= "CBasePreferencePage.consoleFont.label";

	public CPluginPreferencePage() {
		super(GRID);
		setPreferenceStore(CPlugin.getDefault().getPreferenceStore());
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
/*
		Label buildText= new Label(parent, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan= 3;
		buildText.setLayoutData(gd);
		buildText.setText(CPlugin.getResourceString(PAGE_DESC));
		FileFieldEditor editor= new FileFieldEditor(PREF_BUILD_LOCATION, CPlugin.getResourceString(BUILD_LOC_LABEL), true, parent) {
			protected boolean checkState() {
				return true;
			}
		};
		addField(editor);
*/		
		BooleanFieldEditor clearConsole= new BooleanFieldEditor(PREF_CLEAR_CONSOLE, CPlugin.getResourceString(CLEAR_CONSOLE_LABEL), parent);
		addField(clearConsole);

		BooleanFieldEditor autoOpenConsole = new BooleanFieldEditor(PREF_AUTO_OPEN_CONSOLE, CPlugin.getResourceString(AUTO_OPEN_CONSOLE_LABEL), parent);
		addField(autoOpenConsole);
		BooleanFieldEditor consoleOnTop= new BooleanFieldEditor(PREF_CONSOLE_ON_TOP, CPlugin.getResourceString(CONSOLE_ON_TOP_LABEL), parent);
		addField(consoleOnTop);

		BooleanFieldEditor linkEditor= new BooleanFieldEditor(PREF_LINK_TO_EDITOR, CPlugin.getResourceString(LINK_TO_EDITOR_LABEL), parent);
		addField(linkEditor);

		BooleanFieldEditor showCUChildrenEditor= new BooleanFieldEditor(SHOW_CU_CHILDREN, CPlugin.getResourceString(SHOW_CU_CHILDREN_LABEL), parent);
		addField(showCUChildrenEditor);

		addField(new FontFieldEditor(PREF_CONSOLE_FONT, CPlugin.getResourceString(CONSOLE_FONT_LABEL), parent));
		
		//addField(new FontFieldEditor(AbstractTextEditor.PREFERENCE_FONT, CPlugin.getResourceString(EDITOR_FONT_LABEL), parent));
		
	}
	
	/**
	 * Returns the current preference setting if the build console should
	 * be cleared before each build.
	 */
	public static boolean isClearBuildConsole() {
		return CPlugin.getDefault().getPreferenceStore().getBoolean(PREF_CLEAR_CONSOLE);
	}
	public static boolean isAutoOpenConsole() {
		return CPlugin.getDefault().getPreferenceStore().getBoolean(PREF_AUTO_OPEN_CONSOLE);
	}

	public static boolean isConsoleOnTop() {
		return CPlugin.getDefault().getPreferenceStore().getBoolean(PREF_CONSOLE_ON_TOP);
	}

	public static boolean isLinkToEditor() {
		return CPlugin.getDefault().getPreferenceStore().getBoolean(PREF_LINK_TO_EDITOR);
	}

	public static boolean showCompilationUnitChildren() {
		return CPlugin.getDefault().getPreferenceStore().getBoolean(SHOW_CU_CHILDREN);
	}
	
	/**
	 * Returns the current preference setting of the build command location.
	 */	
//	public static String getBuildLocation() {
//		return CPlugin.getDefault().getPreferenceStore().getString(PREF_BUILD_LOCATION);
//	}
	
//	public static boolean isStopOnError() {
//		return CPlugin.getDefault().getPreferenceStore().getBoolean(PREF_STOP_ON_ERROR);
//	}
	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Initializes the default values of this page in the preference bundle.
	 */
	public static void initDefaults(IPreferenceStore prefs) {
//		prefs.setDefault(PREF_BUILD_LOCATION, "make");
//		prefs.setDefault(PREF_STOP_ON_ERROR, false);
		prefs.setDefault(PREF_CLEAR_CONSOLE, true);
		prefs.setDefault(PREF_AUTO_OPEN_CONSOLE, false);
		prefs.setDefault(PREF_CONSOLE_ON_TOP, true);
		prefs.setDefault(PREF_LINK_TO_EDITOR, true);
		prefs.setDefault(SHOW_CU_CHILDREN, true);
		Font font= JFaceResources.getTextFont();
		if (font != null) {
			FontData[] data= font.getFontData();
			if (data != null && data.length > 0) {
				//PreferenceConverter.setDefault(prefs, AbstractTextEditor.PREFERENCE_FONT, data[0]);
				PreferenceConverter.setDefault(prefs, PREF_CONSOLE_FONT, data[0]);
			}
		}		
	}	
	
}
