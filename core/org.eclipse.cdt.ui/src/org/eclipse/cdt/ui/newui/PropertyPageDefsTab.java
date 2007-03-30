/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;

public class PropertyPageDefsTab extends AbstractCPropertyTab {

    private Button show_tree;
    private Button show_mul;
    private Button show_mng;
    private Button show_sav;
    private Button show_tool;

	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));

        show_mng = new Button(usercomp, SWT.CHECK);
        show_mng.setText(UIMessages.getString("PropertyPageDefsTab.0")); //$NON-NLS-1$
        show_mng.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_tree = new Button(usercomp, SWT.CHECK);
        show_tree.setText(UIMessages.getString("PropertyPageDefsTab.1")); //$NON-NLS-1$
        show_tree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_mul = new Button(usercomp, SWT.CHECK);
        show_mul.setText(UIMessages.getString("PropertyPageDefsTab.2")); //$NON-NLS-1$
        show_mul.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_sav = new Button(usercomp, SWT.CHECK);
        show_sav.setText(UIMessages.getString("PropertyPageDefsTab.3")); //$NON-NLS-1$
        show_sav.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_tool = new Button(usercomp, SWT.CHECK);
        show_tool.setText(UIMessages.getString("PropertyPageDefsTab.4")); //$NON-NLS-1$
        show_tool.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        show_tree.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_DTREE));
		show_mul.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_MULTI));
		show_mng.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOMNG));
		show_sav.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOSAVE));
		show_tool.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_TOOLM));
		
		show_mul.setVisible(false); // temporary
	}

	protected void performOK() {
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_DTREE, show_tree.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_MULTI, show_mul.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOMNG, !show_mng.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOSAVE, !show_sav.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_TOOLM, show_tool.getSelection());
	}
	protected void performDefaults() {
		show_tree.setSelection(false);
		show_mul.setSelection(false);
		show_mng.setSelection(true);
		show_mng.setSelection(true);
		show_tool.setSelection(false);
	}

	protected void performApply(ICResourceDescription src, ICResourceDescription dst) { performOK(); }
	protected void updateData(ICResourceDescription cfg) {}  // Do nothing. Data is read once after creation
	protected void updateButtons() {} // Do nothing. No buttons to update
}
