/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alex Collins (Broadcom Corp.) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.internal.ui.buildconsole.BuildConsoleManager;

/**
 * Preference page for build logging options, such as whether the
 * global build console should be logged and, if so, where.
 */
public class GlobalBuildLogPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public GlobalBuildLogPreferencePage() {
		 super(GRID);
		 setPreferenceStore(BuildConsoleManager.getBuildLogPreferenceStore(null));
	}

	/**
	 * A file path field with choose button that does not require the chosen file to exist.
	 */
	static private class FilePathEditor extends StringButtonFieldEditor {
		public FilePathEditor(String name, String label, Composite parent) {
			super(name, label, parent);
		}

		@Override
		protected String changePressed() {
			FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
			dialog.setText(getLabelText());
			String fileName = super.oldValue;
			IPath logFolder = new Path(fileName).removeLastSegments(1);
			dialog.setFilterPath(logFolder.toOSString());
			return dialog.open();
		}
	}

	@Override
	protected void createFieldEditors() {
		 Composite parent = getFieldEditorParent();
		 BooleanFieldEditor keepLog = new BooleanFieldEditor(BuildConsoleManager.KEY_KEEP_LOG,
				   PreferencesMessages.GlobalBuildLogPreferencePage_EnableLogging, parent);
		 addField(keepLog);
		 FilePathEditor logLocation = new FilePathEditor(BuildConsoleManager.KEY_LOG_LOCATION,
				   PreferencesMessages.GlobalBuildLogPreferencePage_LogLocation, parent);
		 addField(logLocation);
	}

	public void init(IWorkbench workbench) {
		 initDefaults(BuildConsoleManager.getBuildLogPreferenceStore(null));
	}

	public static void initDefaults(IPreferenceStore prefs) {
		 prefs.setDefault(BuildConsoleManager.KEY_KEEP_LOG, BuildConsoleManager.CONSOLE_KEEP_LOG_DEFAULT);
		 prefs.setDefault(BuildConsoleManager.KEY_LOG_LOCATION, BuildConsoleManager.getDefaultConsoleLogLocation(null));
	}
}
