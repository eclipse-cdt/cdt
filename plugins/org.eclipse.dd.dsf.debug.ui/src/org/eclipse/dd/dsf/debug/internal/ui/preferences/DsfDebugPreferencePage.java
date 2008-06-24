/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.ui.preferences;

import org.eclipse.dd.dsf.debug.internal.ui.DsfDebugUIPlugin;
import org.eclipse.dd.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
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
		super(GRID);
		IPreferenceStore store= DsfDebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(MessagesForPreferences.DsfDebugPreferencePage_description); 
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDsfDebugUIConstants.PREFERENCE_PAGE);
	}

	@Override
	protected void createFieldEditors() {
		Group performanceGroup= new Group(getFieldEditorParent(), SWT.NONE);
		performanceGroup.setText(MessagesForPreferences.DsfDebugPreferencePage_performanceGroup_label);
		performanceGroup.setLayout(new GridLayout());
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent= 5;
		performanceGroup.setLayoutData(gd);
		
		Composite innerParent= new Composite(performanceGroup, SWT.NONE);
		innerParent.setLayout(new GridLayout());
		innerParent.setLayoutData(gd);
		
		IntegerFieldEditor limitEditor= new IntegerWithBooleanFieldEditor(
				IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT_ENABLE, 
				IDsfDebugUIConstants.PREF_STACK_FRAME_LIMIT, 
				MessagesForPreferences.DsfDebugPreferencePage_limitStackFrames_label, 
				innerParent);

		limitEditor.setValidRange(1, Integer.MAX_VALUE);
		limitEditor.fillIntoGrid(innerParent, 3);
		addField(limitEditor);
	}

}
