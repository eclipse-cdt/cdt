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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

public class EnvDialog extends Dialog {
	IEnvironmentVariable var;
	Button b_add2all;
	private Text text1;
	private Text text2;
	boolean newAction;
	Shell shell;
	ICConfigurationDescription cfgd;
	public String t1 = AbstractCPropertyTab.EMPTY_STR;
	public String t2 = AbstractCPropertyTab.EMPTY_STR;
	public boolean toAll = false;

	public EnvDialog(Shell parent, 
			IEnvironmentVariable _var, 
			String title, boolean _newAction,
			ICConfigurationDescription _cfgd) {
		super(parent);
		shell = parent;
		var = _var;
		newAction = _newAction;
		cfgd = _cfgd;
	}

	/**
	 * Method is overridden to disable "OK" button at start
	 */
	protected Control createContents(Composite parent) {
		Control out = super.createContents(parent);
		setButtons();
		return out;
	}	
	
	protected Control createDialogArea(Composite c) {
		c.setLayout(new GridLayout(3, false));
		GridData gd;
		
		Label l1 = new Label(c, SWT.NONE);
		l1.setText(NewUIMessages.getResourceString("EnvDialog.0")); //$NON-NLS-1$
		l1.setLayoutData(new GridData(GridData.BEGINNING));
		
		text1 = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint = 400;
		text1.setLayoutData(gd);
		text1.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) { setButtons(); }});
		
		Label l2 = new Label(c, SWT.NONE);
		l2.setText(NewUIMessages.getResourceString("EnvDialog.1")); //$NON-NLS-1$
		l2.setLayoutData(new GridData(GridData.BEGINNING));
		
		text2 = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 250;
		text2.setLayoutData(gd);
		text2.addModifyListener(new ModifyListener () {
			public void modifyText(ModifyEvent e) { setButtons(); }});

		final Button b = new Button(c, SWT.PUSH);
		b.setText(NewUIMessages.getResourceString("EnvDialog.2")); //$NON-NLS-1$
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String x = AbstractCPropertyTab.getVariableDialog(b.getShell(), cfgd);
				if (x != null) {
					text2.insert(x);
					setButtons();
				}
			}});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = AbstractCPropertyTab.BUTTON_WIDTH;
		b.setLayoutData(gd);
		
		b_add2all = new Button(c, SWT.CHECK);
		b_add2all.setText(NewUIMessages.getResourceString("EnvDialog.3")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if (cfgd == null)
			b_add2all.setVisible(false);
		else
			b_add2all.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					toAll = b_add2all.getSelection();
				}});
		
		if (!newAction) {
			gd.heightHint = 1;
			b_add2all.setVisible(false);

			text1.setText(var.getName());
			text1.setEnabled(false); // don't change name
			text2.setText(var.getValue()); 
		}
		
		gd.horizontalSpan = 3;
		b_add2all.setLayoutData(gd);
		setButtons();
		return c;
	}
	
	private void setButtons() {
		t1 = text1.getText(); 
		t2 = text2.getText();
		Button b = getButton(IDialogConstants.OK_ID);
		if (b != null)
			b.setEnabled(t1.trim().length() > 0);
	}
}
