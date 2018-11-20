/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.llvm.dsf.lldb.ui.internal;

import java.io.File;

import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBDebugPreferenceConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A preference page for settings that are currently supported in LLDB. Based on
 * the GDB equivalent.
 */
public class LLDBDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor fStringFieldEditorCommand;
	@SuppressWarnings("restriction")
	private org.eclipse.cdt.dsf.debug.internal.ui.preferences.StringWithBooleanFieldEditor fEnableStopAtMain;

	/**
	 * Constructs the preference page.
	 */
	public LLDBDebugPreferencePage() {
		super(FLAT);
		IPreferenceStore store = LLDBUIPlugin.getDefault().getCorePreferenceStore();
		// Note that if we don't set it here, it actually never gets flushed. If
		// this page was to use two preference stores, make sure that both are
		// flushed.
		setPreferenceStore(store);
		setDescription(Messages.LLDBDebugPreferencePage_description);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		final GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		parent.setLayout(layout);

		final Group group1 = new Group(parent, SWT.NONE);
		group1.setText(Messages.LLDBDebugPreferencePage_defaults_label);
		GridLayout groupLayout = new GridLayout(3, false);
		group1.setLayout(groupLayout);
		group1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fStringFieldEditorCommand = new StringFieldEditor(ILLDBDebugPreferenceConstants.PREF_DEFAULT_LLDB_COMMAND,
				Messages.LLDBCDebuggerPage_debugger_command, group1);

		fStringFieldEditorCommand.fillIntoGrid(group1, 2);
		GridData stringFieldLayoutData = (GridData) fStringFieldEditorCommand.getTextControl(group1).getLayoutData();
		stringFieldLayoutData.widthHint = 300;

		addField(fStringFieldEditorCommand);
		Button browsebutton = new Button(group1, SWT.PUSH);
		browsebutton.setText(Messages.LLDBCDebuggerPage_browse);
		browsebutton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowseButtonSelected(Messages.LLDBCDebuggerPage_browse_dialog_title, fStringFieldEditorCommand);
			}
		});
		setButtonLayoutData(browsebutton);

		fEnableStopAtMain = createStopAtMainEditor(group1);
		fEnableStopAtMain.fillIntoGrid(group1, 3);
		addField(fEnableStopAtMain);

		group1.setLayout(groupLayout);

		createLinkToGdb(parent);
	}

	/*
	 * Using full qualified name for return value here so that we don't have to
	 * suppress warning on the whole class.
	 */
	@SuppressWarnings("restriction")
	private static org.eclipse.cdt.dsf.debug.internal.ui.preferences.StringWithBooleanFieldEditor createStopAtMainEditor(
			final Group group1) {
		return new org.eclipse.cdt.dsf.debug.internal.ui.preferences.StringWithBooleanFieldEditor(
				ILLDBDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN,
				ILLDBDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL,
				Messages.LLDBDebugPreferencePage_Stop_on_startup_at, group1);
	}

	private Control createLinkToGdb(Composite parent) {
		String text = Messages.LLDBDebugPreferencePage_see_gdb_preferences;
		Link link = new Link(parent, SWT.NONE);
		link.setText(text);
		link.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), event.text, null, null);
			}
		});

		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.widthHint = 150;
		link.setLayoutData(gridData);
		return link;
	}

	private void handleBrowseButtonSelected(final String dialogTitle, final StringFieldEditor stringFieldEditor) {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(dialogTitle);
		String lldbCommand = stringFieldEditor.getStringValue().trim();
		int lastSeparatorIndex = lldbCommand.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(lldbCommand.substring(0, lastSeparatorIndex));
		}
		String res = dialog.open();
		if (res == null) {
			return;
		}
		stringFieldEditor.setStringValue(res);
	}

	@Override
	protected void adjustGridLayout() {
		// Do nothing, already handled during creation of controls.
	}
}
