/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BuildConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String PREF_CLEAR_CONSOLE = "clearConsole"; //$NON-NLS-1$
	private static final String PREF_CONSOLE_ON_TOP = "consoleOnTop"; //$NON-NLS-1$
	private static final String PREF_AUTO_OPEN_CONSOLE = "autoOpenConsole"; //$NON-NLS-1$

	// In font registry
	public static final String PREF_BUILDCONSOLE_FONT = "org.eclipse.cdt.ui.buildconsole.ConsoleFont"; //$NON-NLS-1$

	public static final String PREF_BUILDCONSOLE_TAB_WIDTH = "buildConsoleTabWith"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_LINES = "buildConsoleLines"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_INFO_COLOR = "buildConsoleInfoStreamColor"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_OUTPUT_COLOR = "buildConsoleOutputStreamColor"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_ERROR_COLOR = "buildConsoleErrorStreamColor"; //$NON-NLS-1$

	public BuildConsolePreferencePage() {
		super(GRID);
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
	}

	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		BooleanFieldEditor clearConsole = new BooleanFieldEditor(PREF_CLEAR_CONSOLE,
				CUIPlugin.getResourceString("ConsolePreferencePage.clearConsole.label"), parent); //$NON-NLS-1$
		addField(clearConsole);

		BooleanFieldEditor autoOpenConsole = new BooleanFieldEditor(PREF_AUTO_OPEN_CONSOLE,
				CUIPlugin.getResourceString("ConsolePreferencePage.autoOpenConsole.label"), parent); //$NON-NLS-1$
		addField(autoOpenConsole);
		BooleanFieldEditor consoleOnTop = new BooleanFieldEditor(PREF_CONSOLE_ON_TOP,
				CUIPlugin.getResourceString("ConsolePreferencePage.consoleOnTop.label"), parent); //$NON-NLS-1$
		addField(consoleOnTop);

		IntegerFieldEditor buildCount = new IntegerFieldEditor(PREF_BUILDCONSOLE_LINES,
				CUIPlugin.getResourceString("ConsolePreferencePage.consoleLines.label"), parent); //$NON-NLS-1$
		buildCount.setErrorMessage(CUIPlugin.getResourceString("ConsolePreferencePage.consoleLines.errorMessage")); //$NON-NLS-1$
		buildCount.setValidRange(10, Integer.MAX_VALUE);
		addField(buildCount);

		IntegerFieldEditor tabSize = new IntegerFieldEditor(PREF_BUILDCONSOLE_TAB_WIDTH,
				CUIPlugin.getResourceString("ConsolePreferencePage.tabWidth.label"), parent); //$NON-NLS-1$
		addField(tabSize);
		tabSize.setValidRange(1, 100);
		tabSize.setErrorMessage(CUIPlugin.getResourceString("ConsolePreferencePage.tabWidth.errorMessage")); //$NON-NLS-1$

		createLabel(parent, CUIPlugin.getResourceString("ConsolePreferencePage.colorSettings.label")); //$NON-NLS-1$

		addField(createColorFieldEditor(PREF_BUILDCONSOLE_OUTPUT_COLOR,
				CUIPlugin.getResourceString("ConsolePreferencePage.outputColor.label"), parent)); //$NON-NLS-1$
		addField(createColorFieldEditor(PREF_BUILDCONSOLE_INFO_COLOR,
				CUIPlugin.getResourceString("ConsolePreferencePage.infoColor.label"), parent)); //$NON-NLS-1$
		addField(createColorFieldEditor(PREF_BUILDCONSOLE_ERROR_COLOR,
				CUIPlugin.getResourceString("ConsolePreferencePage.errorColor.label"), parent)); //$NON-NLS-1$
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	/**
	 * Creates a new color field editor.
	 */
	private ColorFieldEditor createColorFieldEditor(String preferenceName, String label, Composite parent) {
		ColorFieldEditor editor = new ColorFieldEditor(preferenceName, label, parent);
		editor.setPreferencePage(this);
		editor.setPreferenceStore(getPreferenceStore());
		return editor;
	}

	/**
	 * Returns the current preference setting if the build console should be
	 * cleared before each build.
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
		prefs.setDefault(PREF_AUTO_OPEN_CONSOLE, true);
		prefs.setDefault(PREF_CONSOLE_ON_TOP, false);
		prefs.setDefault(PREF_BUILDCONSOLE_LINES, 500);
		prefs.setDefault(PREF_BUILDCONSOLE_TAB_WIDTH, 4);
		PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_OUTPUT_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_INFO_COLOR, new RGB(0, 0, 255));
		PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_ERROR_COLOR, new RGB(255, 0, 0));
	}

}
