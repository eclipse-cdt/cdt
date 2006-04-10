/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/


package org.eclipse.rse.dstore.security.preference;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.rse.dstore.security.UniversalSecurityPlugin;
import org.eclipse.rse.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.dstore.security.util.GridUtil;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RenameCertDialog extends SystemPromptDialog implements Listener{
	

	private Text txtName;
	String newAlias;
	private String oldAlias;
	
	public RenameCertDialog(UniversalSecurityPreferencePage page, Shell shell, String oldValue)
	{
		super(shell, UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_RENAME_CERT_DLG_TITLE));
		oldAlias = oldValue;
	}
	
	public Control getInitialFocusControl()
	{
		return txtName;
	}
	
	protected Control createInner(Composite parent)
	{

		Composite content = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		GridData data = GridUtil.createFill();
		data.widthHint = 350;
		layout.numColumns = 2;
		content.setLayout(layout);
		content.setLayoutData(data);

		Label lblName = new Label(content, SWT.NONE);
		lblName.setText(UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_ALIAS));
		
		txtName = new Text(content, SWT.BORDER);
		txtName.setText(oldAlias);
		txtName.selectAll();
		txtName.addListener(SWT.Modify, this);
		
		data = GridUtil.createHorizontalFill();
		data.widthHint = 200;
		txtName.setLayoutData(data);

		return content;
	}
	
	private void validateDialog(){
		if(txtName.getText().trim().length()==0)
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		else
			getButton(IDialogConstants.OK_ID).setEnabled(true);
	}
	
	public void handleEvent(Event e){
		if(e.widget.equals(txtName))
			validateDialog();
	}
	
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);			
		validateDialog();
		return control;
		
	}

	public String getNewAlias()
	{
		return newAlias;
	}
	
	protected boolean processOK()
	{
		newAlias = txtName.getText();
		return super.processOK();
	}

}