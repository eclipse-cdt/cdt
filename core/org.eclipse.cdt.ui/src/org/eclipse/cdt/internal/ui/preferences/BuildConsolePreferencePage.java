/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BuildConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String PREF_CONSOLE_FONT = "consoleFont";
	private static final String PREF_CLEAR_CONSOLE = "clearConsole";
	private static final String PREF_CONSOLE_ON_TOP = "consoleOnTop";
	private static final String PREF_AUTO_OPEN_CONSOLE = "autoOpenConsole";
	public static final String PREF_BUILDCONSOLE_LINES = "buildConsoleLines";

	private static final String CLEAR_CONSOLE_LABEL= "CBasePreferencePage.clearConsole.label";
	private static final String CONSOLE_ON_TOP_LABEL= "CBasePreferencePage.consoleOnTop.label";
	private static final String AUTO_OPEN_CONSOLE_LABEL= "CBasePreferencePage.autoOpenConsole.label";
	private static final String CONSOLE_FONT_LABEL= "CBasePreferencePage.consoleFont.label";

	public BuildConsolePreferencePage() {
		super(GRID);
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
	}

	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		BooleanFieldEditor clearConsole =
			new BooleanFieldEditor(PREF_CLEAR_CONSOLE, CUIPlugin.getResourceString(CLEAR_CONSOLE_LABEL), parent);
		addField(clearConsole);

		BooleanFieldEditor autoOpenConsole =
			new BooleanFieldEditor(PREF_AUTO_OPEN_CONSOLE, CUIPlugin.getResourceString(AUTO_OPEN_CONSOLE_LABEL), parent);
		addField(autoOpenConsole);
		BooleanFieldEditor consoleOnTop =
			new BooleanFieldEditor(PREF_CONSOLE_ON_TOP, CUIPlugin.getResourceString(CONSOLE_ON_TOP_LABEL), parent);
		addField(consoleOnTop);

		IntegerFieldEditor buildCount = new IntegerFieldEditor( PREF_BUILDCONSOLE_LINES, "&Build console lines: ", parent );
		buildCount.setValidRange( 10, Integer.MAX_VALUE );
		addField( buildCount );

		addField(new FontFieldEditor(PREF_CONSOLE_FONT, CUIPlugin.getResourceString(CONSOLE_FONT_LABEL), parent));
	}

	/**
	 * Returns the current preference setting if the build console should
	 * be cleared before each build.
	 */
	public static boolean isClearBuildConsole() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PREF_CLEAR_CONSOLE);
	}
	public static boolean isAutoOpenConsole() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PREF_AUTO_OPEN_CONSOLE);
	}

	public static boolean isConsoleOnTop() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PREF_CONSOLE_ON_TOP);
	}

	public static int buildConsoleLines() {
		return CUIPlugin.getDefault().getPreferenceStore().getInt(PREF_BUILDCONSOLE_LINES);
	}

	public void init(IWorkbench workbench) {
	}

	public static void initDefaults(IPreferenceStore prefs) {
		prefs.setDefault(PREF_CLEAR_CONSOLE, true);
		prefs.setDefault(PREF_AUTO_OPEN_CONSOLE, false);
		prefs.setDefault(PREF_CONSOLE_ON_TOP, true);
		prefs.setDefault(PREF_BUILDCONSOLE_LINES, 100);
		Font font = JFaceResources.getTextFont();
		if (font != null) {
			FontData[] data = font.getFontData();
			if (data != null && data.length > 0) {
				PreferenceConverter.setDefault(prefs, PREF_CONSOLE_FONT, data[0]);
			}
		}

	}

}
