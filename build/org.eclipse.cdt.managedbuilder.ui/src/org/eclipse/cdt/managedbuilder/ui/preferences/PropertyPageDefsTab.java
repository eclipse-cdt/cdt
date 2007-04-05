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
package org.eclipse.cdt.managedbuilder.ui.preferences;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class PropertyPageDefsTab extends AbstractCPropertyTab {

	private static final int SPACING = 5; // for radio buttons layout
	
    private Button show_tree;
//    private Button show_mul;
    private Button show_mng;
    private Button show_sav;
    private Button show_tool;
    private Button b_0;
    private Button b_1;
    private Button b_2;
    private Button b_3;

	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));

        show_mng = new Button(usercomp, SWT.CHECK);
        show_mng.setText(UIMessages.getString("PropertyPageDefsTab.0")); //$NON-NLS-1$
        show_mng.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_tree = new Button(usercomp, SWT.CHECK);
        show_tree.setText(UIMessages.getString("PropertyPageDefsTab.1")); //$NON-NLS-1$
        show_tree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	//	show_mul = new Button(usercomp, SWT.CHECK);
    //    show_mul.setText(UIMessages.getString("PropertyPageDefsTab.2")); //$NON-NLS-1$
    //    show_mul.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_sav = new Button(usercomp, SWT.CHECK);
        show_sav.setText(UIMessages.getString("PropertyPageDefsTab.3")); //$NON-NLS-1$
        show_sav.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_tool = new Button(usercomp, SWT.CHECK);
        show_tool.setText(UIMessages.getString("PropertyPageDefsTab.4")); //$NON-NLS-1$
        show_tool.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Group discGrp = new Group(usercomp, SWT.NONE);
        discGrp.setText(UIMessages.getString("PropertyPageDefsTab.5")); //$NON-NLS-1$
        discGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        FillLayout fl = new FillLayout(SWT.VERTICAL);
        fl.spacing = SPACING;
        fl.marginHeight = SPACING;
        fl.marginWidth = SPACING;
        discGrp.setLayout(fl);
        
        b_0 = new Button(discGrp, SWT.RADIO);
        b_0.setText(UIMessages.getString("PropertyPageDefsTab.6")); //$NON-NLS-1$
        b_1 = new Button(discGrp, SWT.RADIO);
        b_1.setText(UIMessages.getString("PropertyPageDefsTab.7")); //$NON-NLS-1$
        b_2 = new Button(discGrp, SWT.RADIO);
        b_2.setText(UIMessages.getString("PropertyPageDefsTab.8")); //$NON-NLS-1$
        b_3 = new Button(discGrp, SWT.RADIO);
        b_3.setText(UIMessages.getString("PropertyPageDefsTab.9")); //$NON-NLS-1$
        
        show_tree.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_DTREE));
	//	show_mul.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_MULTI));
		show_mng.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOMNG));
		show_sav.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOSAVE));
		show_tool.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_TOOLM));
		
		switch (CDTPrefUtil.getInt(CDTPrefUtil.KEY_DISC_NAMES)) {
			case CDTPrefUtil.DISC_NAMING_UNIQUE_OR_BOTH: b_0.setSelection(true); break;
			case CDTPrefUtil.DISC_NAMING_UNIQUE_OR_IDS:  b_1.setSelection(true); break;
			case CDTPrefUtil.DISC_NAMING_ALWAYS_BOTH:    b_2.setSelection(true); break;
			case CDTPrefUtil.DISC_NAMING_ALWAYS_IDS:     b_3.setSelection(true); break;
		}
	}

	protected void performOK() {
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_DTREE, show_tree.getSelection());
	//	CDTPrefUtil.setBool(CDTPrefUtil.KEY_MULTI, show_mul.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOMNG, !show_mng.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOSAVE, !show_sav.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_TOOLM, show_tool.getSelection());
		int x = 0;
		if (b_1.getSelection()) x = 1;
		else if (b_2.getSelection()) x = 2;
		else if (b_3.getSelection()) x = 3;
		CDTPrefUtil.setInt(CDTPrefUtil.KEY_DISC_NAMES, x);
	}
	
	protected void performDefaults() {
		show_tree.setSelection(false);
	//	show_mul.setSelection(false);
		show_mng.setSelection(true);
		show_mng.setSelection(true);
		show_tool.setSelection(false);
		b_0.setSelection(true);
	}

	protected void performApply(ICResourceDescription src, ICResourceDescription dst) { performOK(); }
	protected void updateData(ICResourceDescription cfg) {}  // Do nothing. Data is read once after creation
	protected void updateButtons() {} // Do nothing. No buttons to update
}
