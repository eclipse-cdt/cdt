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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.rse.dstore.security.UniversalSecurityPlugin;
import org.eclipse.rse.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.dstore.security.util.GridUtil;
import org.eclipse.rse.dstore.security.util.StringModifier;
import org.eclipse.rse.dstore.security.widgets.CertificateForm;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class NewCertDialog extends SystemPromptDialog implements Listener
{

    /**
	 * 
	 */
	private final UniversalSecurityPreferencePage	page;
	private CertificateForm _certForm;
    private Certificate _certificate;
	private Shell _shell;
	
	public NewCertDialog(UniversalSecurityPreferencePage page, Shell shell){
		super(shell,UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_ADD_CERT_DLG_TITLE) );
		this.page = page;
		_shell = shell;
	}
	
	public Control getInitialFocusControl()
	{
		return _certForm.getInitialFocusControl();
	}
	
	protected Control createInner(Composite parent)
	{ 
		Composite content = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		GridData data = GridUtil.createFill();
		layout.numColumns = 1;
		content.setLayout(layout);
		content.setLayoutData(data);

		_certForm = new CertificateForm(_shell, getMessageLine());
		_certForm.createContents(content);
		_certForm.registerListener(this);
		return content;
	}

	protected Control createButtonBar(Composite parent) 
	{
		Control control = super.createButtonBar(parent);

		getOkButton().setEnabled(false);		
		return control;	
	}
	
	protected boolean processOK(){

		try{	
			_certificate = _certForm.loadCertificate(this.page._keyStore);
		}
		catch(FileNotFoundException e){

			String text = UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_KEY_IO_ERROR_);
			text = StringModifier.change(text, "%1", _certForm.getPath());
			String msg = UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_LOAD_EXC_);
			
			Status err = new Status(IStatus.ERROR,ResourcesPlugin.PI_RESOURCES,IStatus.ERROR,text,e);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(),UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_SEC_MSG), msg,err);
			return false;
			
		}
		catch(IOException e){

			String text = UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_LOAD_IO_EXC_);
			text = StringModifier.change(text, "%1", _certForm.getPath());

			text = StringModifier.change(text, "%1", UniversalSecurityPlugin.getKeyStoreLocation());
			String msg = UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_LOAD_EXC_);
			
			Status err = new Status(IStatus.ERROR,ResourcesPlugin.PI_RESOURCES,IStatus.ERROR,text,e);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(),UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_SEC_MSG), msg,err);
			return false;
										
		}
		catch(CertificateException exc){

			String text = UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_EXC_);
			text = StringModifier.change(text, "%1", _certForm.getPath());

			String msg = UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_LOAD_EXC_);
			
			Status err = new Status(IStatus.ERROR,ResourcesPlugin.PI_RESOURCES,IStatus.ERROR,text,exc);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(),UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_SEC_MSG), msg,err);
			return false;

		}
		catch(KeyStoreException exc){
			String text = UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_KEY_STORE_ERROR_);
			text = StringModifier.change(text, "%1", UniversalSecurityPlugin.getKeyStoreLocation());
			String msg = UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_LOAD_EXC_);
			
			Status err = new Status(IStatus.ERROR,ResourcesPlugin.PI_RESOURCES,IStatus.ERROR,text,exc);
			ErrorDialog.openError(UniversalSecurityPlugin.getActiveWorkbenchShell(),UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_SEC_MSG), msg,err);
			return false;
		}
		
		if (_certificate instanceof X509Certificate)
		{
			X509CertificateElement elem = new X509CertificateElement(_certForm.getAliasName(), UniversalSecurityPlugin.getString(UniversalSecurityProperties.RESID_SECURITY_TRUSTED_CERTIFICATE), (X509Certificate)_certificate);
			this.page._tableItems.add(elem);
		}
					
		return true;
	}
	
	public void handleEvent(Event e){
		getButton(IDialogConstants.OK_ID).setEnabled(_certForm.validateDialog());
	}
	
	
	public Certificate getCertificate(){
		return _certificate;
	}
	
}