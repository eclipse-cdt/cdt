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
import org.eclipse.cdt.ui.newui.UIMessages;
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
		l.setText(UIMessages.getString("PropertyMultiCfgTab.0")); //$NON-NLS-1$
		l.setForeground(RED);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		
        enable_multi = new Button(usercomp, SWT.CHECK);
        enable_multi.setText(UIMessages.getString("PropertyMultiCfgTab.1")); //$NON-NLS-1$
        enable_multi.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        enable_multi.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				setStates();
			}});
        
        dGrp = new Group(usercomp, SWT.NONE);
        dGrp.setText(UIMessages.getString("PropertyMultiCfgTab.3")); //$NON-NLS-1$
        dGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        FillLayout fl = new FillLayout(SWT.VERTICAL);
        fl.spacing = SPACING;
        fl.marginWidth = SPACING;
        dGrp.setLayout(fl);
        
        l = new Label(dGrp, SWT.WRAP | SWT.CENTER);
        l.setText(
       		UIMessages.getString("PropertyMultiCfgTab.4") //$NON-NLS-1$
        );
        l.setForeground(BLUE);
        d_0 = new Button(dGrp, SWT.RADIO);
        d_0.setText(UIMessages.getString("PropertyMultiCfgTab.5")); //$NON-NLS-1$
        d_1 = new Button(dGrp, SWT.RADIO);
        d_1.setText(UIMessages.getString("PropertyMultiCfgTab.6")); //$NON-NLS-1$
        d_2 = new Button(dGrp, SWT.RADIO);
        d_2.setText(UIMessages.getString("PropertyMultiCfgTab.7")); //$NON-NLS-1$
        
        wGrp = new Group(usercomp, SWT.NONE);
        wGrp.setText(UIMessages.getString("PropertyMultiCfgTab.8")); //$NON-NLS-1$
        wGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fl = new FillLayout(SWT.VERTICAL);
        fl.spacing = SPACING;
        fl.marginWidth = SPACING;
        wGrp.setLayout(fl);

        l = new Label(wGrp, SWT.WRAP | SWT.CENTER);
        l.setText(
           		UIMessages.getString("PropertyMultiCfgTab.9") //$NON-NLS-1$
            );
        l.setForeground(BLUE);
        w_0 = new Button(wGrp, SWT.RADIO);
        w_0.setText(UIMessages.getString("PropertyMultiCfgTab.10")); //$NON-NLS-1$
        w_1 = new Button(wGrp, SWT.RADIO);
        w_1.setText(UIMessages.getString("PropertyMultiCfgTab.11")); //$NON-NLS-1$
        
		enable_multi.setSelection(CDTPrefUtil.getBool(CDTPrefUtil.KEY_MULTI));
		
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
		d_0.setSelection(false);
		d_1.setSelection(true);
		d_2.setSelection(false);
		w_0.setSelection(true);
		w_1.setSelection(false);
	}

	private void setStates() {
		boolean b = enable_multi.getSelection();
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
