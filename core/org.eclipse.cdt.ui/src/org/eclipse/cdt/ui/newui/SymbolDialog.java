/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

//import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SymbolDialog extends AbstractPropertyDialog {
	private String  data1;
	private String  data2;
	private Button b_add2conf;
	private Button b_add2lang;
	private Text txt1;
	private Text txt2;
	private Button b_vars;
	private Button b_ok;
	private Button b_ko;
	private boolean newAction;
	private ICResourceDescription cfgd;

	public SymbolDialog(Shell parent, boolean _newAction,
		String title, String _data1, String _data2,
		ICResourceDescription _cfgd) {
		super(parent, title);
		data1 = _data1;
		data2 = _data2;
		newAction = _newAction;
		cfgd = _cfgd;
	}

	@Override
	protected Control createDialogArea(Composite c) {
		c.setLayout(new GridLayout(4, true));
		GridData gd;
		
		Label l1 = new Label(c, SWT.NONE);
		l1.setText(Messages.SymbolDialog_0); 
		l1.setLayoutData(new GridData(GridData.BEGINNING));
		
		txt1 = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.widthHint = 300;
		txt1.setLayoutData(gd);
		txt1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setButtons();
			}}); 
		
		Label l2 = new Label(c, SWT.NONE);
		l2.setText(Messages.SymbolDialog_1); 
		l2.setLayoutData(new GridData(GridData.BEGINNING));
		
		txt2 = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint = 200;
		txt2.setLayoutData(gd);
		txt2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setButtons();
			}}); 

		b_vars = setupButton(c, AbstractCPropertyTab.VARIABLESBUTTON_NAME);
			
		b_add2conf = new Button(c, SWT.CHECK);
		b_add2conf.setText(Messages.IncludeDialog_2); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		b_add2conf.setLayoutData(gd);
		if (!newAction) {
			b_add2conf.setVisible(false);
			txt1.setEnabled(false); // don't change name
		}

		b_add2lang = new Button(c, SWT.CHECK);
		b_add2lang.setText(Messages.IncludeDialog_3); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		b_add2lang.setLayoutData(gd);
		if (!newAction) {
			b_add2lang.setVisible(false);
			txt1.setEnabled(false); // don't change name
		}
		
		// add 2 placeholders
		new Label(c, 0).setLayoutData(new GridData());
		new Label(c, 0).setLayoutData(new GridData());
		b_ok = setupButton(c, IDialogConstants.OK_LABEL);
		b_ko = setupButton(c, IDialogConstants.CANCEL_LABEL);
		
		c.getShell().setDefaultButton(b_ok);
		c.pack();

		// moved here to avoid accessing b_ok before it created.
		txt1.setText(data1);
		txt2.setText(data2); 
		
		setButtons();
		return c;
	}	
	
	private void setButtons() {
		b_ok.setEnabled(txt1.getText().trim().length() > 0);
	}
	
	@Override
	public void buttonPressed(SelectionEvent e) {
		if (e.widget.equals(b_ok)) { 
			super.text1 = txt1.getText();
			super.text2 = txt2.getText();
			check1 = b_add2conf.getSelection(); 
			check3 = b_add2lang.getSelection(); 
			result = true;
			shell.dispose(); 
		} else if (e.widget.equals(b_ko)) {
			shell.dispose();
		} else if (e.widget.equals(b_vars)) {
			String s = AbstractCPropertyTab.getVariableDialog(shell, cfgd.getConfiguration());
			if (s != null) txt2.insert(s);
		}
	}
}
