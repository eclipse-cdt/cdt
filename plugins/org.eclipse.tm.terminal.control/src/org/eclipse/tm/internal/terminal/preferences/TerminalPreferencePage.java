/*******************************************************************************
 * Copyright (c) 2003, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.tm.internal.terminal.control.impl.TerminalMessages;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TerminalPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
    protected BooleanFieldEditor fInvertColors;

	protected IntegerFieldEditor fEditorBufferSize;

	public TerminalPreferencePage() {
		super(GRID);
	}
	protected void createFieldEditors() {
		setupPage();
	}
	public void init(IWorkbench workbench) {
		// do nothing
	}
	protected void setupPage() {
		setupData();
		setupEditors();
	}
	protected void setupData() {
		TerminalPlugin plugin;
		IPreferenceStore preferenceStore;

		plugin = TerminalPlugin.getDefault();
		preferenceStore = plugin.getPreferenceStore();
		setPreferenceStore(preferenceStore);
	}
	protected void setupEditors() {
		fInvertColors = new BooleanFieldEditor(
				ITerminalConstants.PREF_INVERT_COLORS, TerminalMessages.INVERT_COLORS,
				getFieldEditorParent());
		fEditorBufferSize = new IntegerFieldEditor(ITerminalConstants.PREF_BUFFERLINES,
				TerminalMessages.BUFFERLINES, getFieldEditorParent());

		fEditorBufferSize.setValidRange(0, Integer.MAX_VALUE);

		addField(fInvertColors);
		addField(fEditorBufferSize);
	}
}
