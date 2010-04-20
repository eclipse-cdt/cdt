/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * A preference page for settings that are currently only supported in GDB.
 */
public class GdbDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public GdbDebugPreferencePage() {
		super(FLAT);
		IPreferenceStore store= GdbUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(MessagesForPreferences.GdbDebugPreferencePage_description); 
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), GdbUIPlugin.PLUGIN_ID + ".dsfgdb_preference_page"); //$NON-NLS-1$
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent= getFieldEditorParent();
		final GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		parent.setLayout(layout);
		
		Group group= new Group(parent, SWT.NONE);
		group.setText(MessagesForPreferences.GdbDebugPreferencePage_traces_label);
		GridLayout groupLayout= new GridLayout(3, false);
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		BooleanFieldEditor boolField= new BooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE,
				MessagesForPreferences.GdbDebugPreferencePage_enableTraces_label,
				group);

		boolField.fillIntoGrid(group, 3);
		addField(boolField);
		// need to set layout again
		group.setLayout(groupLayout);
		
		group= new Group(parent, SWT.NONE);
		group.setText(MessagesForPreferences.GdbDebugPreferencePage_termination_label);
		groupLayout= new GridLayout(3, false);
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		boolField= new BooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB,
				MessagesForPreferences.GdbDebugPreferencePage_autoTerminateGdb_label,
				group);

		boolField.fillIntoGrid(group, 3);
		addField(boolField);
		// need to set layout again
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

	}

	@Override
	protected void adjustGridLayout() {
		// do nothing
	}
}
