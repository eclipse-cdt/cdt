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
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class PropertyMultiCfgTab extends AbstractCPropertyTab {

	private static final int SPACING = 5; // for radio buttons layout
	private static final Color RED  = CUIPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_RED);
    private static final Color BLUE = CUIPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_BLUE);

    private Button enable_multi;
    private Button enable_3state;
    private Group dGrp;
    private Group wGrp;
    private Button d_0;
    private Button d_1;
    private Button d_2;

    private Button w_0;
    private Button w_1;
    
    
	public void createControls(Composite parent) {
		super.createControls(parent);
		GridLayout g = new GridLayout(1, false);
		g.verticalSpacing = SPACING;
		usercomp.setLayout(g);

		Label l = new Label(usercomp, SWT.CENTER | SWT.BORDER);
		l.setText("* This functionality is experimental *");
		l.setForeground(RED);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		
        enable_multi = new Button(usercomp, SWT.CHECK);
        enable_multi.setText("Enable multiple configurations setting");
        enable_multi.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        enable_multi.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				setStates();
			}});
        
		enable_3state = new Button(usercomp, SWT.CHECK);
		enable_3state.setText("Use 3-state checkboxes where possible");
		enable_3state.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        dGrp = new Group(usercomp, SWT.NONE);
        dGrp.setText("String list Display mode");
        dGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        FillLayout fl = new FillLayout(SWT.VERTICAL);
        fl.spacing = SPACING;
        fl.marginWidth = SPACING;
        dGrp.setLayout(fl);
        
        l = new Label(dGrp, SWT.WRAP | SWT.CENTER);
        l.setText(
       		"Define how string lists from different configurations\nshould be combined for display, when lists are not equal"
        );
        l.setForeground(BLUE);
        d_0 = new Button(dGrp, SWT.RADIO);
        d_0.setText("Show empty list");
        d_1 = new Button(dGrp, SWT.RADIO);
        d_1.setText("Show common elements (conjunction)");
        d_2 = new Button(dGrp, SWT.RADIO);
        d_2.setText("Show all elements except doubles (disjunction)");
        
        wGrp = new Group(usercomp, SWT.NONE);
        wGrp.setText("String list Write mode");
        wGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fl = new FillLayout(SWT.VERTICAL);
        fl.spacing = SPACING;
        fl.marginWidth = SPACING;
        wGrp.setLayout(fl);

        l = new Label(wGrp, SWT.WRAP | SWT.CENTER);
        l.setText(
           		"Define how to save changes in string lists \nfor different configurations"
            );
        l.setForeground(BLUE);
        w_0 = new Button(wGrp, SWT.RADIO);
        w_0.setText("Add/remove/change affected elements, do not touch others");
        w_1 = new Button(wGrp, SWT.RADIO);
        w_1.setText("Replace existing string lists with string list shown to user");
        
		enable_multi.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_MULTI));
		enable_3state.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_3STATE));
		
		switch (CDTPrefUtil.getInt(CDTPrefUtil.KEY_DMODE)) {
			case CDTPrefUtil.DMODE_EMPTY:       d_0.setSelection(true); break;
			case CDTPrefUtil.DMODE_CONJUNCTION: d_1.setSelection(true); break;
			case CDTPrefUtil.DMODE_DISJUNCTION: d_2.setSelection(true); break;
			default:                            d_1.setSelection(true); break;
		}

		switch (CDTPrefUtil.getInt(CDTPrefUtil.KEY_WMODE)) {
			case CDTPrefUtil.WMODE_MODIFY:  w_0.setSelection(true); break;
			case CDTPrefUtil.WMODE_REPLACE: w_1.setSelection(true); break;
			default: w_0.setSelection(true); break;
		}
		
		setStates();
	}

	protected void performOK() {
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_MULTI, enable_multi.getSelection());
		CDTPrefUtil.setBool(CDTPrefUtil.KEY_3STATE,enable_3state.getSelection());
		int x = 0;
		if (d_0.getSelection()) 
			x = CDTPrefUtil.DMODE_EMPTY;
		else if (d_1.getSelection()) 
			x = CDTPrefUtil.DMODE_CONJUNCTION;
		else if (d_2.getSelection()) 
			x = CDTPrefUtil.DMODE_DISJUNCTION;
		CDTPrefUtil.setInt(CDTPrefUtil.KEY_DMODE, x);

		if (w_0.getSelection())      
			x = CDTPrefUtil.WMODE_MODIFY;
		else if (w_1.getSelection()) 
			x = CDTPrefUtil.WMODE_REPLACE;
		CDTPrefUtil.setInt(CDTPrefUtil.KEY_WMODE, x);
	}
	
	protected void performDefaults() {
		enable_multi.setSelection(false);
		enable_3state.setSelection(false);
		d_0.setSelection(false);
		d_1.setSelection(true);
		d_2.setSelection(false);
		w_0.setSelection(true);
		w_1.setSelection(false);
	}

	private void setStates() {
		boolean b = enable_multi.getSelection();
		enable_3state.setEnabled(b);
		d_0.setEnabled(b);
		d_1.setEnabled(b);
		d_2.setEnabled(b);
		w_0.setEnabled(b);
		w_1.setEnabled(b);
		dGrp.setEnabled(b);
		wGrp.setEnabled(b);
	}
	
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) { performOK(); }
	protected void updateData(ICResourceDescription cfg) {}  // Do nothing. Data is read once after creation
	protected void updateButtons() {} // Do nothing. No buttons to update
}
