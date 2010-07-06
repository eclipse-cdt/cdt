/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Miwako Tokugawa (Intel Corporation) - Fixed-location tooltip support
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.preferences;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * @since 5.1
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PropertyPageDefsTab extends AbstractCPropertyTab {

	private static final int SPACING = 5; // for radio buttons layout
	
    private Button show_tree;
    private Button show_inc_files;
    private Button show_mng;
    private Button show_tool;
    private Button show_exp;
    private Button show_tipbox;

    private Button b_0;
    private Button b_1;
    private Button b_2;
    private Button b_3;

    private Button s_0;
    private Button s_1;
    private Button s_2;
    
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));

        show_mng = new Button(usercomp, SWT.CHECK);
        show_mng.setText(Messages.PropertyPageDefsTab_0); 
        show_mng.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        show_inc_files = new Button(usercomp, SWT.CHECK);
        show_inc_files.setText(Messages.PropertyPageDefsTab_showIncludeFileTab); 
        show_inc_files.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_tree = new Button(usercomp, SWT.CHECK);
        show_tree.setText(Messages.PropertyPageDefsTab_1); 
        show_tree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_tool = new Button(usercomp, SWT.CHECK);
        show_tool.setText(Messages.PropertyPageDefsTab_4); 
        show_tool.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		show_exp = new Button(usercomp, SWT.CHECK);
        show_exp.setText(Messages.PropertyPageDefsTab_10); 
        show_exp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        show_tipbox = new Button(usercomp, SWT.CHECK);
        show_tipbox.setText(Messages.PropertyPageDefsTab_16); 
        show_tipbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Group saveGrp = new Group(usercomp, SWT.NONE);
        saveGrp.setText(Messages.PropertyPageDefsTab_11); 
        saveGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        FillLayout fl = new FillLayout(SWT.VERTICAL);
        fl.spacing = SPACING;
        fl.marginHeight = SPACING;
        fl.marginWidth = SPACING;
        saveGrp.setLayout(fl);
        
        s_0 = new Button(saveGrp, SWT.RADIO);
        s_0.setText(Messages.PropertyPageDefsTab_13); 
        s_1 = new Button(saveGrp, SWT.RADIO);
        s_1.setText(Messages.PropertyPageDefsTab_12); 
        s_2 = new Button(saveGrp, SWT.RADIO);
        s_2.setText(Messages.PropertyPageDefsTab_14); 
        
        Group discGrp = new Group(usercomp, SWT.NONE);
        discGrp.setText(Messages.PropertyPageDefsTab_5); 
        discGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fl = new FillLayout(SWT.VERTICAL);
        fl.spacing = SPACING;
        fl.marginHeight = SPACING;
        fl.marginWidth = SPACING;
        discGrp.setLayout(fl);
        
        b_0 = new Button(discGrp, SWT.RADIO);
        b_0.setText(Messages.PropertyPageDefsTab_6); 
        b_1 = new Button(discGrp, SWT.RADIO);
        b_1.setText(Messages.PropertyPageDefsTab_7); 
        b_2 = new Button(discGrp, SWT.RADIO);
        b_2.setText(Messages.PropertyPageDefsTab_8); 
        b_3 = new Button(discGrp, SWT.RADIO);
        b_3.setText(Messages.PropertyPageDefsTab_9); 
        
        show_inc_files.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_SHOW_INC_FILES));
        show_tree.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_DTREE));
		show_mng.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOMNG));
		show_tool.setSelection(!CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOTOOLM));
		show_exp.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_EXPORT));
		show_tipbox.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_TIPBOX));
		
		switch (CDTPrefUtil.getInt(CDTPrefUtil.KEY_DISC_NAMES)) {
			case CDTPrefUtil.DISC_NAMING_UNIQUE_OR_BOTH: b_0.setSelection(true); break;
			case CDTPrefUtil.DISC_NAMING_UNIQUE_OR_IDS:  b_1.setSelection(true); break;
			case CDTPrefUtil.DISC_NAMING_ALWAYS_BOTH:    b_2.setSelection(true); break;
			case CDTPrefUtil.DISC_NAMING_ALWAYS_IDS:     b_3.setSelection(true); break;
		}

		switch (CDTPrefUtil.getInt(CDTPrefUtil.KEY_POSSAVE)) {
			case CDTPrefUtil.POSITION_SAVE_BOTH: s_1.setSelection(true); break;
			case CDTPrefUtil.POSITION_SAVE_NONE: s_2.setSelection(true); break;
			default: s_0.setSelection(true); break;
		}
	}

	@Override
	protected void performOK() {
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_SHOW_INC_FILES, show_inc_files.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_DTREE, show_tree.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOMNG, !show_mng.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOTOOLM, !show_tool.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_EXPORT, show_exp.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_TIPBOX, show_tipbox.getSelection());
		int x = 0;
		if (b_1.getSelection()) x = 1;
		else if (b_2.getSelection()) x = 2;
		else if (b_3.getSelection()) x = 3;
		CDTPrefUtil.setInt(CDTPrefUtil.KEY_DISC_NAMES, x);

		if (s_0.getSelection()) x = CDTPrefUtil.POSITION_SAVE_SIZE;
		else if (s_1.getSelection()) x = CDTPrefUtil.POSITION_SAVE_BOTH;
		else if (s_2.getSelection()) x = CDTPrefUtil.POSITION_SAVE_NONE;
		CDTPrefUtil.setInt(CDTPrefUtil.KEY_POSSAVE, x);
	}
	
	@Override
	protected void performDefaults() {
		show_tree.setSelection(false);
		show_inc_files.setSelection(false);
		show_mng.setSelection(true);
		show_tool.setSelection(true);
		show_exp.setSelection(false);
		show_tipbox.setSelection(false);
		b_0.setSelection(true);
		b_1.setSelection(false);
		b_2.setSelection(false);
		b_3.setSelection(false);
		s_0.setSelection(true);
		s_1.setSelection(false);
		s_2.setSelection(false);
	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) { performOK(); }
	@Override
	protected void updateData(ICResourceDescription cfg) {}  // Do nothing. Data is read once after creation
	@Override
	protected void updateButtons() {} // Do nothing. No buttons to update
}
