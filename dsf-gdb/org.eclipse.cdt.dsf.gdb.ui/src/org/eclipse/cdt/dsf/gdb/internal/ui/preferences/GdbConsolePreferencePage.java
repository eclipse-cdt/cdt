/*******************************************************************************
 * Copyright (c) 2016 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Initial version is based on:
 * org.eclipse.tm.terminal.control/src/org/eclipse/tm/internal/terminal/preferences/TerminalPreferencePage.java
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * GDB Full CLI Console Preference Page.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 */
public class GdbConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final int MIN_BUFFER_LINES = 16;  /* minimum of ~1000 chars */

	public GdbConsolePreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		setupPage();
	}

	@Override
	public void init(IWorkbench workbench) {
		// do nothing
	}

	private void setupPage() {
		setupData();
		setupEditors();
		setupDefaults();
	}

	private void setupData() {
		setPreferenceStore(GdbUIPlugin.getDefault().getPreferenceStore());
	}

	private void setupEditors() {
		BooleanFieldEditor invertColors = new BooleanFieldEditor(IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS,
				MessagesForPreferences.GdbConsolePreferencePage_INVERT_COLORS, getFieldEditorParent());
		IntegerFieldEditor editorBufferSize = new IntegerFieldEditor(IGdbDebugPreferenceConstants.PREF_CONSOLE_BUFFERLINES,
				MessagesForPreferences.GdbConsolePreferencePage_BUFFERLINES, getFieldEditorParent());

		editorBufferSize.setValidRange(MIN_BUFFER_LINES, Integer.MAX_VALUE);

		addField(invertColors);
		addField(editorBufferSize);
	}

	private void setupDefaults() {
		getPreferenceStore().setDefault(IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS, IGdbDebugPreferenceConstants.CONSOLE_INVERTED_COLORS);
		getPreferenceStore().setDefault(IGdbDebugPreferenceConstants.PREF_CONSOLE_BUFFERLINES, IGdbDebugPreferenceConstants.CONSOLE_BUFFERLINES);
	}
}
