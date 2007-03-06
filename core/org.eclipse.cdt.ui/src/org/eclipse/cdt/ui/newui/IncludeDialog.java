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

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class IncludeDialog extends AbstractPropertyDialog {
	public String sdata;
	public Button b_add2all;
	public Text text;
	private Button b_work;
	private Button b_file;
	private Button b_vars;
	private Button b_ok;
	private Button b_ko;
	private int mode;
	private Button c_wsp;
	private ICConfigurationDescription cfgd;
	private boolean isWsp = false;
	
	static final int NEW_FILE = 0;
	static final int NEW_DIR  = 1;
	static final int OLD_FILE = 2;
	static final int OLD_DIR  = 3;
	
	static final int DIR_MASK = 1;	
	static final int OLD_MASK = 2;	
	
	public IncludeDialog(Shell parent, int _mode,
		String title, String _data, ICConfigurationDescription _cfgd, int flags) {
		super(parent, title);
		mode = _mode;
		sdata = _data;
		cfgd = _cfgd;
		if (flags == ICSettingEntry.VALUE_WORKSPACE_PATH)
			isWsp = true;
	}

	protected Control createDialogArea(Composite c) {
		c.setLayout(new GridLayout(5, true));
		GridData gd;
		
		Label l1 = new Label(c, SWT.NONE);
		if ((mode & DIR_MASK) == DIR_MASK)
			l1.setText("Directory :");  //$NON-NLS-1$
		else
			l1.setText("File :");  //$NON-NLS-1$
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 5;
		l1.setLayoutData(gd);
		
		text = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 5;
		gd.widthHint = 400;
		text.setLayoutData(gd);
		if ((mode & OLD_MASK) == OLD_MASK) { text.setText(sdata); }
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setButtons();
			}}); 
		b_add2all = new Button(c, SWT.CHECK);
		b_add2all.setText("Add to all configurations"); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		if ((mode & OLD_MASK) == OLD_MASK) {
			gd.heightHint = 1;
			b_add2all.setVisible(false);
		}
		b_add2all.setLayoutData(gd);
		
		b_vars = setupButton(c, AbstractCPropertyTab.VARIABLESBUTTON_NAME);
		new Label(c, 0).setLayoutData(new GridData()); // placeholder
		b_ok = setupButton(c, IDialogConstants.OK_LABEL);
		b_ko = setupButton(c, IDialogConstants.CANCEL_LABEL);
		b_work = setupButton(c, AbstractCPropertyTab.WORKSPACEBUTTON_NAME);
		b_file = setupButton(c, AbstractCPropertyTab.FILESYSTEMBUTTON_NAME);

		c_wsp = new Button(c, SWT.CHECK);
		c_wsp.setText(NewUIMessages.getResourceString("ExpDialog.4")); //$NON-NLS-1$
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 5;
		c_wsp.setLayoutData(gd);
		c_wsp.setSelection(isWsp);
		c_wsp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				c_wsp.setImage(AbstractExportTab.getWspImage(c_wsp.getSelection()));
			}});
		
		c_wsp.setImage(AbstractExportTab.getWspImage(isWsp));
		
		c.getShell().setDefaultButton(b_ok);
		c.pack();
		setButtons();
		return c;
	}	
	
	private void setButtons() {
		b_ok.setEnabled(text.getText().trim().length() > 0);
	}
	
	public void buttonPressed(SelectionEvent e) {
		String s;
		if (e.widget.equals(b_ok)) { 
			text1 = text.getText();
			check1 = b_add2all.getSelection();
			check2 = c_wsp.getSelection();
			result = true;
			shell.dispose(); 
		} else if (e.widget.equals(b_ko)) {
			shell.dispose();
		} else if (e.widget.equals(b_work)) {
			if ((mode & DIR_MASK)== DIR_MASK)
				s = AbstractCPropertyTab.getWorkspaceDirDialog(shell, text.getText());
			else 
				s = AbstractCPropertyTab.getWorkspaceFileDialog(shell, text.getText());
			if (s != null) {
				s = strip_wsp(s);
				text.setText(s);
				c_wsp.setSelection(true);
				c_wsp.setImage(AbstractExportTab.getWspImage(c_wsp.getSelection()));
			}
		} else if (e.widget.equals(b_file)) {
			if ((mode & DIR_MASK)== DIR_MASK)
				s = AbstractCPropertyTab.getFileSystemDirDialog(shell, text.getText());
			else 
				s = AbstractCPropertyTab.getFileSystemFileDialog(shell, text.getText());
			if (s != null) {
				text.setText(s);
				c_wsp.setSelection(false);
				c_wsp.setImage(AbstractExportTab.getWspImage(c_wsp.getSelection()));
			}
		} else if (e.widget.equals(b_vars)) {
			s = AbstractCPropertyTab.getVariableDialog(shell, cfgd);
			if (s != null) text.insert(s);
		}
	}
}
