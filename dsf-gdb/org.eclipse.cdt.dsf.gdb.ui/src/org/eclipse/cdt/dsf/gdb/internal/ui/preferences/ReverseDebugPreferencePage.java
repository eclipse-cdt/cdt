/*******************************************************************************
 * Copyright (c) 2015 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ReverseDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ReverseDebugPreferencePage() {
		super(GRID);
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	@Override
	protected void createFieldEditors() {
		FieldEditor edit = new RadioGroupFieldEditor(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
				MessagesForPreferences.ReverseDebugPreferencePage_SelectHardwareTracingMethod, 1,
				new String[][] {
						{ MessagesForPreferences.ReverseDebugPreferencePage_GDBPreference,
								IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE },
						{ MessagesForPreferences.ReverseDebugPreferencePage_BranchTrace,
								IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE },
						{ MessagesForPreferences.ReverseDebugPreferencePage_ProcessorTrace,
								IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE } },
				getFieldEditorParent());
		edit.fillIntoGrid(getFieldEditorParent(), 1);
		getPreferenceStore().setDefault(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
				IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE);
		addField(edit);
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}
