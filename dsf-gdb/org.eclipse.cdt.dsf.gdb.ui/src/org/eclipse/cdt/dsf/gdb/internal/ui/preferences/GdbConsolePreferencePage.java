/*******************************************************************************
 * Copyright (c) 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * GDB CLI Console Preference Page.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 */
public class GdbConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final int MIN_BUFFER_LINES = 16; /* minimum of ~1000 chars */
	// Instead of using a maximum of Integer.MAX_VALUE (which is some obscure number),
	// let's use a well defined limit e.g. 2 billion lines, which is readable.
	private static final int MAX_BUFFER_LINES = 2000000000;

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
	}

	private void setupData() {
		setPreferenceStore(GdbUIPlugin.getDefault().getPreferenceStore());
	}

	private void setupEditors() {
		BooleanFieldEditor invertColors = new BooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS,
				MessagesForPreferences.GdbConsolePreferencePage_InvertColors, getFieldEditorParent());
		IntegerFieldEditor editorBufferSize = new IntegerFieldEditor(
				IGdbDebugPreferenceConstants.PREF_CONSOLE_BUFFERLINES,
				MessagesForPreferences.GdbConsolePreferencePage_BufferLines, getFieldEditorParent());

		editorBufferSize.setValidRange(MIN_BUFFER_LINES, MAX_BUFFER_LINES);

		addField(invertColors);
		addField(editorBufferSize);
	}
}
