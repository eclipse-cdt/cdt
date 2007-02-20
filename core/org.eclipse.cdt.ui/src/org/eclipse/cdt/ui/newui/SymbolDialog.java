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

//import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;

public class SymbolDialog extends AbstractPropertyDialog {
	String  data1;
	String  data2;
	Button b_add2all;
	Text txt1;
	Text txt2;
	Button b_vars;
	Button b_ok;
	Button b_ko;
	boolean newAction;
	ICResourceDescription cfgd;

	public SymbolDialog(Shell parent, boolean _newAction,
		String title, String _data1, String _data2,
		ICResourceDescription _cfgd) {
		super(parent, title);
		data1 = _data1;
		data2 = _data2;
		newAction = _newAction;
		cfgd = _cfgd;
	}

	protected Control createDialogArea(Composite c) {
		c.setLayout(new GridLayout(4, true));
		GridData gd;
		
		Label l1 = new Label(c, SWT.NONE);
		l1.setText("Name:");  //$NON-NLS-1$
		l1.setLayoutData(new GridData(GridData.BEGINNING));
		
		txt1 = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		txt1.setLayoutData(gd);
		txt1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setButtons();
			}}); 
		
		Label l2 = new Label(c, SWT.NONE);
		l2.setText("Value:");  //$NON-NLS-1$
		l2.setLayoutData(new GridData(GridData.BEGINNING));
		
		txt2 = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		txt2.setLayoutData(gd);
		txt2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setButtons();
			}}); 

		b_vars = setupButton(c, "Variables..."); //$NON-NLS-1$
		
		b_add2all = new Button(c, SWT.CHECK);
		b_add2all.setText("Add to all configurations"); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		if (!newAction) {
			gd.heightHint = 1;
			b_add2all.setVisible(false);
			txt1.setText(data1);
			txt1.setEnabled(false); // don't change name
			txt2.setText(data2); 
		}
		b_add2all.setLayoutData(gd);
		
		// add 2 placeholders
		new Label(c, 0).setLayoutData(new GridData());
		new Label(c, 0).setLayoutData(new GridData());
		b_ok = setupButton(c, IDialogConstants.OK_LABEL);
		b_ko = setupButton(c, IDialogConstants.CANCEL_LABEL);

		c.getShell().setDefaultButton(b_ok);
		c.pack();
		Rectangle r = shell.getBounds();
		r.width = 400;
		shell.setBounds(r);
		
		setButtons();
		return c;
	}	
	
	private void setButtons() {
		b_ok.setEnabled(
				(txt1.getText().trim().length() > 0) &&
				(txt2.getText().trim().length() > 0));
	}

	
	public void buttonPressed(SelectionEvent e) {
		if (e.widget.equals(b_ok)) { 
			super.text1 = txt1.getText();
			super.text2 = txt2.getText();
			check1 = b_add2all.getSelection(); 
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
