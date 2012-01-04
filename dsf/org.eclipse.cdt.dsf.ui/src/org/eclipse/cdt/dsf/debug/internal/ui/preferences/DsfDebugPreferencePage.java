/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.preferences;

import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * DSF debug preference page.
 */
public class DsfDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Mandatory default constructor (executable extension).
	 */
	public DsfDebugPreferencePage() {
		super(FLAT);
		IPreferenceStore store= DsfUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(MessagesForPreferences.DsfDebugPreferencePage_description); 
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDsfDebugUIConstants.PREFERENCE_PAGE);
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent= getFieldEditorParent();
		final GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		parent.setLayout(layout);
		
		Group performanceGroup= new Group(parent, SWT.NONE);
		performanceGroup.setText(MessagesForPreferences.DsfDebugPreferencePage_performanceGroup_label);
		GridLayout groupLayout= new GridLayout(3, false);
		performanceGroup.setLayout(groupLayout);
		performanceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// stack frame limit
		IntegerFieldEditor limitEditor= new IntegerWithBooleanFieldEditor(
				IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT_ENABLE, 
				IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT, 
				MessagesForPreferences.DsfDebugPreferencePage_limitStackFrames_label, 
				performanceGroup);

		limitEditor.setValidRange(1, Integer.MAX_VALUE);
		limitEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
		limitEditor.fillIntoGrid(performanceGroup, 3);
		addField(limitEditor);

		// sync stepping speed
		BooleanFieldEditor syncSteppingEditor= new BooleanFieldEditor(
				IDsfDebugUIConstants.PREF_WAIT_FOR_VIEW_UPDATE_AFTER_STEP_ENABLE,
				MessagesForPreferences.DsfDebugPreferencePage_waitForViewUpdate_label,
				performanceGroup);

		syncSteppingEditor.fillIntoGrid(performanceGroup, 3);
		addField(syncSteppingEditor);

		// minimum step interval
		IntegerFieldEditor minIntervalEditor= new DecoratingIntegerFieldEditor(
				IDsfDebugUIConstants.PREF_MIN_STEP_INTERVAL,
				MessagesForPreferences.DsfDebugPreferencePage_minStepInterval_label,
				performanceGroup);

		minIntervalEditor.setValidRange(0, 10000);
		minIntervalEditor.fillIntoGrid(performanceGroup, 3);
		addField(minIntervalEditor);
		
		// need to set layout again
		performanceGroup.setLayout(groupLayout);
}

	@Override
	protected void adjustGridLayout() {
		// do nothing
	}

}
