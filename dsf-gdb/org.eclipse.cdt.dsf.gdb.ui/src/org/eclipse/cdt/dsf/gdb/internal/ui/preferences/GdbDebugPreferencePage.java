/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IGdbDebugPreferenceConstants.PREFERENCE_PAGE);
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent= getFieldEditorParent();
		final GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		parent.setLayout(layout);
		
		Group tracesGroup= new Group(parent, SWT.NONE);
		tracesGroup.setText(MessagesForPreferences.GdbDebugPreferencePage_traces_label);
		GridLayout groupLayout= new GridLayout(3, false);
		tracesGroup.setLayout(groupLayout);
		tracesGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		BooleanFieldEditor traces= new BooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE,
				MessagesForPreferences.GdbDebugPreferencePage_enableTraces_label,
				tracesGroup);

		traces.fillIntoGrid(tracesGroup, 3);
		addField(traces);

		// need to set layout again
		tracesGroup.setLayout(groupLayout);
	}

	@Override
	protected void adjustGridLayout() {
		// do nothing
	}
}
