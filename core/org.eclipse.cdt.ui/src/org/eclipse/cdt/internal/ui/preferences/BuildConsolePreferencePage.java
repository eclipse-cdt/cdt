/*******************************************************************************
 * Copyright (c) 2000, 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

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
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;

public class BuildConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String PREF_CLEAR_CONSOLE = "clearConsole"; //$NON-NLS-1$
	private static final String PREF_CONSOLE_ON_TOP = "consoleOnTop"; //$NON-NLS-1$
	private static final String PREF_AUTO_OPEN_CONSOLE = "autoOpenConsole"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_WRAP_LINES = "buildConsoleWrapLines"; //$NON-NLS-1$

	// In font registry
	public static final String PREF_BUILDCONSOLE_FONT = "org.eclipse.cdt.ui.buildconsole.ConsoleFont"; //$NON-NLS-1$

	public static final String PREF_BUILDCONSOLE_TAB_WIDTH = "buildConsoleTabWith"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_LINES = "buildConsoleLines"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_UPDATE_DELAY_MS = "buildConsoleUpdateDelayMs"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_INFO_COLOR = "buildConsoleInfoStreamColor"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_OUTPUT_COLOR = "buildConsoleOutputStreamColor"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_ERROR_COLOR = "buildConsoleErrorStreamColor"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_BACKGROUND_COLOR = "buildConsoleBackgroundColor"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR = "buildConsoleProblemBackgroundColor"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR = "buildConsoleProblemWarningBackgroundColor"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR = "buildConsoleProblemInfoBackgroundColor"; //$NON-NLS-1$
	public static final String PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR = "buildConsoleProblemHighlightedColor"; //$NON-NLS-1$

	/**
	 * Update the UI after a short delay. The reason for a short delay is to try
	 * and reduce the "frame rate" of the build console updates, this reduces
	 * the total load on the main thread. User's won't be able to tell that
	 * there is an extra delay.
	 *
	 * A too short time has little effect and a too long time starts to be
	 * visible to the user. With my experiments to get under 50% CPU utilization
	 * on the main thread requires at least 35 msec delay between updates. 250
	 * msec leads to visible delay to user and ~20% utilization. And finally the
	 * chosen value, 75 msec leads to ~35% utilization and no user visible
	 * delay.
	 */
	public static final int DEFAULT_BUILDCONSOLE_UPDATE_DELAY_MS = 75;

	
	public BuildConsolePreferencePage() {
		super(GRID);
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.BUILD_CONSOLE_PREFERENCE_PAGE);
	}

	@Override
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
		BooleanFieldEditor consoleWrapLines = new BooleanFieldEditor(PREF_BUILDCONSOLE_WRAP_LINES,
				CUIPlugin.getResourceString("ConsolePreferencePage.consoleWrapLines.label"), parent); //$NON-NLS-1$
		addField(consoleWrapLines);

		IntegerFieldEditor buildCount = new IntegerFieldEditor(PREF_BUILDCONSOLE_LINES,
				CUIPlugin.getResourceString("ConsolePreferencePage.consoleLines.label"), parent); //$NON-NLS-1$
		buildCount.getLabelControl(parent).setToolTipText(CUIPlugin.getResourceString("ConsolePreferencePage.consoleLines.tooltip")); //$NON-NLS-1$
		buildCount.getTextControl(parent).setToolTipText(CUIPlugin.getResourceString("ConsolePreferencePage.consoleLines.tooltip")); //$NON-NLS-1$
		buildCount.setErrorMessage(CUIPlugin.getResourceString("ConsolePreferencePage.consoleLines.errorMessage")); //$NON-NLS-1$
		buildCount.setValidRange(10, Integer.MAX_VALUE);
		addField(buildCount);

		IntegerFieldEditor updateDelay = new IntegerFieldEditor(PREF_BUILDCONSOLE_UPDATE_DELAY_MS,
				CUIPlugin.getResourceString("ConsolePreferencePage.consoleUpdateDelay.label"), parent); //$NON-NLS-1$
		updateDelay.getLabelControl(parent).setToolTipText(CUIPlugin.getResourceString("ConsolePreferencePage.consoleUpdateDelay.tooltip")); //$NON-NLS-1$
		updateDelay.getTextControl(parent).setToolTipText(CUIPlugin.getResourceString("ConsolePreferencePage.consoleUpdateDelay.tooltip")); //$NON-NLS-1$
		updateDelay.setErrorMessage(CUIPlugin.getResourceString("ConsolePreferencePage.consoleUpdateDelay.errorMessage")); //$NON-NLS-1$
		updateDelay.setValidRange(0, Integer.MAX_VALUE);
		addField(updateDelay);

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
		addField(createColorFieldEditor(PREF_BUILDCONSOLE_BACKGROUND_COLOR,
				CUIPlugin.getResourceString("ConsolePreferencePage.backgroundColor.label"), parent)); //$NON-NLS-1$
		addField(createColorFieldEditor(PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR,
				CUIPlugin.getResourceString("ConsolePreferencePage.problemBackgroundColor.label"), parent)); //$NON-NLS-1$
		addField(createColorFieldEditor(PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR,
				CUIPlugin.getResourceString("ConsolePreferencePage.problemWarningBackgroundColor.label"), parent)); //$NON-NLS-1$
		addField(createColorFieldEditor(PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR,
				CUIPlugin.getResourceString("ConsolePreferencePage.problemInfoBackgroundColor.label"), parent)); //$NON-NLS-1$
		addField(createColorFieldEditor(PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR,
				CUIPlugin.getResourceString("ConsolePreferencePage.problemHighlightedColor.label"), parent)); //$NON-NLS-1$
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
		editor.setPage(this);
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

	public static boolean isConsoleWrapLines() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PREF_BUILDCONSOLE_WRAP_LINES);
	}

	public static int buildConsoleLines() {
		return CUIPlugin.getDefault().getPreferenceStore().getInt(PREF_BUILDCONSOLE_LINES);
	}
	
	public static int buildConsoleUpdateDelayMs() {
		return CUIPlugin.getDefault().getPreferenceStore().getInt(PREF_BUILDCONSOLE_UPDATE_DELAY_MS);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	public static void initDefaults(IPreferenceStore prefs) {
		if(!prefs.contains(PREF_CLEAR_CONSOLE))
			prefs.setDefault(PREF_CLEAR_CONSOLE, true);
		if(!prefs.contains(PREF_AUTO_OPEN_CONSOLE))
			prefs.setDefault(PREF_AUTO_OPEN_CONSOLE, true);
		if(!prefs.contains(PREF_CONSOLE_ON_TOP))
			prefs.setDefault(PREF_CONSOLE_ON_TOP, true);
		if(!prefs.contains(PREF_BUILDCONSOLE_WRAP_LINES))
			prefs.setDefault(PREF_BUILDCONSOLE_WRAP_LINES, false);
		if(!prefs.contains(PREF_BUILDCONSOLE_LINES))
			prefs.setDefault(PREF_BUILDCONSOLE_LINES, 500);
		if(!prefs.contains(PREF_BUILDCONSOLE_UPDATE_DELAY_MS))
			prefs.setDefault(PREF_BUILDCONSOLE_UPDATE_DELAY_MS, DEFAULT_BUILDCONSOLE_UPDATE_DELAY_MS);
		if(!prefs.contains(PREF_BUILDCONSOLE_TAB_WIDTH))
			prefs.setDefault(PREF_BUILDCONSOLE_TAB_WIDTH, 4);
		if(!prefs.contains(PREF_BUILDCONSOLE_OUTPUT_COLOR))
			PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_OUTPUT_COLOR, new RGB(0, 0, 0));
		if(!prefs.contains(PREF_BUILDCONSOLE_INFO_COLOR))
			PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_INFO_COLOR, new RGB(0, 0, 255));
		if(!prefs.contains(PREF_BUILDCONSOLE_ERROR_COLOR))
			PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_ERROR_COLOR, new RGB(255, 0, 0));
		if(!prefs.contains(PREF_BUILDCONSOLE_BACKGROUND_COLOR))
			PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_BACKGROUND_COLOR, new RGB(255, 255, 255));
		if(!prefs.contains(PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR))
			PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_PROBLEM_BACKGROUND_COLOR, new RGB(254, 231, 224));
		if(!prefs.contains(PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR))
			PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_PROBLEM_WARNING_BACKGROUND_COLOR, new RGB(254, 243, 218));
		if(!prefs.contains(PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR))
			PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_PROBLEM_INFO_BACKGROUND_COLOR, new RGB(244, 247, 254));
		if(!prefs.contains(PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR))
			PreferenceConverter.setDefault(prefs, PREF_BUILDCONSOLE_PROBLEM_HIGHLIGHTED_COLOR, new RGB(255, 0, 0));
	}

}
