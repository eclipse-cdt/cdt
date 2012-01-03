/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import java.io.File;

import org.eclipse.cdt.dsf.debug.internal.ui.preferences.StringWithBooleanFieldEditor;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.LaunchUIMessages;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * A preference page for settings that are currently only supported in GDB.
 */
@SuppressWarnings("restriction")
public class GdbDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	/**
	 * A vehicle in order to be able to register a selection listener with
	 * a {@link BooleanFieldEditor}.
	 */
	private class ListenableBooleanFieldEditor extends BooleanFieldEditor {

		public ListenableBooleanFieldEditor(String name, String labelText, int style,
				Composite parent) {
			super(name, labelText, style, parent);
		}

		@Override
		public Button getChangeControl(Composite parent) {
			return super.getChangeControl(parent);
		}
	}

	public GdbDebugPreferencePage() {
		super(FLAT);
		IPreferenceStore store= GdbUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(MessagesForPreferences.GdbDebugPreferencePage_description);
	}

    @Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				GdbUIPlugin.PLUGIN_ID + ".dsfgdb_preference_page"); //$NON-NLS-1$
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent= getFieldEditorParent();
		final GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		parent.setLayout(layout);

		final Group group1 = new Group(parent, SWT.NONE);
		group1.setText(MessagesForPreferences.GdbDebugPreferencePage_defaults_label);
		GridLayout groupLayout = new GridLayout(3, false);
		group1.setLayout(groupLayout);
		group1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final StringFieldEditor stringFieldEditorCommand = new StringFieldEditor(
				IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND,
				LaunchUIMessages.getString("GDBDebuggerPage.gdb_debugger"), //$NON-NLS-1$
				group1);

		stringFieldEditorCommand.fillIntoGrid(group1, 2);
		addField(stringFieldEditorCommand);
		Button browsebutton = new Button(group1, SWT.PUSH);
		browsebutton.setText(LaunchUIMessages.getString("GDBDebuggerPage.gdb_browse")); //$NON-NLS-1$
		browsebutton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowseButtonSelected(LaunchUIMessages.getString("GDBDebuggerPage.gdb_browse_dlg_title"),  //$NON-NLS-1$
						stringFieldEditorCommand);
			}
		});

		final StringFieldEditor stringFieldEditorGdbInit = new StringFieldEditor(
				IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_INIT,
				LaunchUIMessages.getString("GDBDebuggerPage.gdb_command_file"), //$NON-NLS-1$
				group1);

		stringFieldEditorGdbInit.fillIntoGrid(group1, 2);
		addField(stringFieldEditorGdbInit);
		browsebutton = new Button(group1, SWT.PUSH);
		browsebutton.setText(LaunchUIMessages.getString("GDBDebuggerPage.gdb_browse")); //$NON-NLS-1$
		browsebutton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowseButtonSelected(LaunchUIMessages.getString("GDBDebuggerPage.gdb_cmdfile_dlg_title"), //$NON-NLS-1$
						stringFieldEditorGdbInit);
			}
		});

		final StringWithBooleanFieldEditor enableStopAtMain = new StringWithBooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN,
				IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL,
				LaunchUIMessages.getString("CDebuggerTab.Stop_at_main_on_startup"), //$NON-NLS-1$
				group1);
		enableStopAtMain.fillIntoGrid(group1, 2);
		addField(enableStopAtMain);

//		final StringFieldEditor stopAtMainSymbol = new StringFieldEditor(
//				IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL,
//				"",	group1); //$NON-NLS-1$
//		stopAtMainSymbol.fillIntoGrid(group1, 2);
//		addField(stopAtMainSymbol);
//
//		enableStopAtMain.getChangeControl(group1).addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				boolean enabled = enableStopAtMain.getBooleanValue();
//				stopAtMainSymbol.setEnabled(enabled, group1);
//			}
//		});

		final ListenableBooleanFieldEditor enableNonStop= new ListenableBooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_DEFAULT_NON_STOP,
				LaunchUIMessages.getString("GDBDebuggerPage.nonstop_mode"), //$NON-NLS-1$
				SWT.NONE, group1);
		enableNonStop.fillIntoGrid(group1, 3);
		addField(enableNonStop);

		group1.setLayout(groupLayout);

		final Group group2= new Group(parent, SWT.NONE);
		group2.setText(MessagesForPreferences.GdbDebugPreferencePage_traces_label);
		groupLayout= new GridLayout(3, false);
		group2.setLayout(groupLayout);
		group2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final ListenableBooleanFieldEditor enableGdbTracesField = new ListenableBooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE,
				MessagesForPreferences.GdbDebugPreferencePage_enableTraces_label,
				SWT.NONE, group2);

		enableGdbTracesField.fillIntoGrid(group2, 3);
		addField(enableGdbTracesField);

		final IntegerFieldEditor maxCharactersField = new IntegerFieldEditor(
				IGdbDebugPreferenceConstants.PREF_MAX_GDB_TRACES,
				MessagesForPreferences.GdbDebugPreferencePage_maxGdbTraces_label,
				group2);
		// Instead of using Integer.MAX_VALUE which is some obscure number,
		// using 2 billion is nice and readable.
		maxCharactersField.setValidRange(10000, 2000000000);

		maxCharactersField.fillIntoGrid(group2, 3);
		addField(maxCharactersField);

		enableGdbTracesField.getChangeControl(group2).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = enableGdbTracesField.getBooleanValue();
				maxCharactersField.setEnabled(enabled, group2);
			}
		});

		// Need to set layout again.
		group2.setLayout(groupLayout);

		Group group= new Group(parent, SWT.NONE);
		group.setText(MessagesForPreferences.GdbDebugPreferencePage_termination_label);
		groupLayout= new GridLayout(3, false);
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		BooleanFieldEditor boolField= new BooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB,
				MessagesForPreferences.GdbDebugPreferencePage_autoTerminateGdb_label,
				group);

		boolField.fillIntoGrid(group, 3);
		addField(boolField);
		// Need to set layout again.
		group.setLayout(groupLayout);

		group= new Group(parent, SWT.NONE);
		group.setText(MessagesForPreferences.GdbDebugPreferencePage_hover_label);
		groupLayout= new GridLayout(3, false);
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		boolField= new BooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_USE_INSPECTOR_HOVER,
				MessagesForPreferences.GdbDebugPreferencePage_useInspectorHover_label,
				group);

		boolField.fillIntoGrid(group, 3);
		addField(boolField);
		// need to set layout again
		group.setLayout(groupLayout);

		group = new Group(parent, SWT.NONE);
		group.setText(MessagesForPreferences.GdbDebugPreferencePage_prettyPrinting_label);
		groupLayout = new GridLayout(3, false);
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final ListenableBooleanFieldEditor enablePrettyPrintingField = new ListenableBooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_ENABLE_PRETTY_PRINTING,
				MessagesForPreferences.GdbDebugPreferencePage_enablePrettyPrinting_label1 + "\n" //$NON-NLS-1$
				+ MessagesForPreferences.GdbDebugPreferencePage_enablePrettyPrinting_label2,
				SWT.NONE, group);

		enablePrettyPrintingField.fillIntoGrid(group, 3);
		addField(enablePrettyPrintingField);

		final Composite indentHelper = new Composite(group, SWT.NONE);
		GridLayout helperLayout = new GridLayout(3, false);
		indentHelper.setLayout(helperLayout);
		GridData helperData = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		helperData.horizontalIndent = 20;
		indentHelper.setLayoutData(helperData);

		final IntegerFieldEditor childCountLimitField = new IntegerFieldEditor(
				IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS,
				MessagesForPreferences.GdbDebugPreferencePage_initialChildCountLimitForCollections_label,
				indentHelper);

		childCountLimitField.setValidRange(1, 10000);
		childCountLimitField.fillIntoGrid(indentHelper, 3);

		boolean prettyPrintingEnabled =
				getPreferenceStore().getBoolean(IGdbDebugPreferenceConstants.PREF_ENABLE_PRETTY_PRINTING);
		childCountLimitField.setEnabled(prettyPrintingEnabled, indentHelper);

		addField(childCountLimitField);

		enablePrettyPrintingField.getChangeControl(group).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = enablePrettyPrintingField.getBooleanValue();
				childCountLimitField.setEnabled(enabled, indentHelper);
			}
		});

		// need to set layouts again
		indentHelper.setLayout(helperLayout);
		group.setLayout(groupLayout);
	}

	private void handleBrowseButtonSelected(final String dialogTitle, final StringFieldEditor stringFieldEditor) {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(dialogTitle);
		String gdbCommand = stringFieldEditor.getStringValue().trim();
		int lastSeparatorIndex = gdbCommand.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(gdbCommand.substring(0, lastSeparatorIndex));
		}
		String res = dialog.open();
		if (res == null) {
			return;
		}
		stringFieldEditor.setStringValue(res);
	}

	@Override
	protected void adjustGridLayout() {
		// do nothing
	}
}
