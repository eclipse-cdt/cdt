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
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.managedbuilder.ui.wizards.ICBuildWizardHandler;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.PageLayout;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class PreferredToolchainsTab extends AbstractCBuildPropertyTab {

    protected CWizardHandler h_selected = null;
    // widgets
    private Tree tree;
    private Composite right;
    private Button show_sup;
    private Label right_label;

    private Button pref1;
    private Button pref0;

	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));
		
        Composite c = new Composite(usercomp, SWT.NONE);
        c.setLayoutData(new GridData(GridData.FILL_BOTH));
    	c.setLayout(new GridLayout(2, true));

        Label l1 = new Label(c, SWT.NONE);
        l1.setText(Messages.getString("CMainWizardPage.0")); //$NON-NLS-1$
        l1.setFont(parent.getFont());
        l1.setLayoutData(new GridData(GridData.BEGINNING));
        
        right_label = new Label(c, SWT.NONE);
        right_label.setFont(parent.getFont());
        right_label.setLayoutData(new GridData(GridData.BEGINNING));
    	
        tree = new Tree(c, SWT.SINGLE | SWT.BORDER);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] tis = tree.getSelection();
				if (tis == null || tis.length == 0) return;
				switchTo((CWizardHandler)tis[0].getData());
			}});
        
        right = new Composite(c, SWT.NONE);
        right.setLayoutData(new GridData(GridData.FILL_BOTH));
        right.setLayout(new PageLayout());

    	Label l = new Label(c,SWT.WRAP | SWT.CENTER);
        l.setText(Messages.getString("PreferredToolchainsTab.0")); //$NON-NLS-1$
        GridData gd = new GridData(GridData.CENTER);
        gd.horizontalSpan = 2;
        l.setLayoutData(gd);

        new Label(c,0).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pref1 = new Button(c, SWT.PUSH);
        pref1.setText(Messages.getString("PreferredToolchainsTab.1")); //$NON-NLS-1$
        pref1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pref1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) { setPref(true); }} );

        new Label(c,0).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));        
        pref0 = new Button(c, SWT.PUSH);
        pref0.setText(Messages.getString("PreferredToolchainsTab.2")); //$NON-NLS-1$
        pref0.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pref0.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) { setPref(false); }} );
        
        show_sup = new Button(c, SWT.CHECK);
        show_sup.setSelection(true);
        show_sup.setText(Messages.getString("CMainWizardPage.1")); //$NON-NLS-1$
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        show_sup.setLayoutData(gd);
        show_sup.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (h_selected != null)
					h_selected.setSupportedOnly(show_sup.getSelection());
				switchTo(CDTMainWizardPage.updateData(tree, right, show_sup, null, null));
			}} );
        CDTPrefUtil.readPreferredTCs();
        switchTo(CDTMainWizardPage.updateData(tree, right, show_sup, null, null));
    }

	private void setPref(boolean set) {
		if (h_selected == null || !h_selected.supportsPreferred()) 
			return;
		if (h_selected instanceof ICBuildWizardHandler) {
			IToolChain[] tcs = ((ICBuildWizardHandler)h_selected).getSelectedToolChains();
			for (int i=0; i<tcs.length; i++) {
				String id = (tcs[i] == null) ? CDTPrefUtil.NULL : tcs[i].getId();
				if (set) CDTPrefUtil.addPreferredTC(id);
				else CDTPrefUtil.delPreferredTC(id);
			}
		}
		h_selected.updatePreferred(CDTPrefUtil.getPreferredTCs());
	}
	
	private void switchTo(CWizardHandler h) {
		if (h == null) return;
		if (h_selected != null) h_selected.handleUnSelection();
		h_selected = h;
		right_label.setText(h_selected.getHeader());
		h_selected.handleSelection();
		h_selected.setSupportedOnly(show_sup.getSelection());
	}

	protected void performOK() {
        CDTPrefUtil.savePreferredTCs();
	}
	
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) { performOK();}

	protected void performDefaults() {
        CDTPrefUtil.cleanPreferredTCs();
		h_selected.handleSelection();
	}
	protected void updateData(ICResourceDescription cfg) {}
	protected void updateButtons() {} // Do nothing. No buttons to update.
}
