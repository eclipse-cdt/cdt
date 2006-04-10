/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.dstore.security.wizards;

import java.security.cert.X509Certificate;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.dstore.security.preference.X509CertificateElement;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.validators.ISystemValidator;
import org.eclipse.rse.ui.wizards.AbstractSystemWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class SystemImportCertWizardAliasPage 
 	   extends AbstractSystemWizardPage
 	   implements  ISystemMessages        
{  


	protected SystemMessage errorMessage;
	protected ISystemValidator nameValidator;
	protected ISystemMessageLine msgLine;
	
    private Text _alias;
	/**
	 * Constructor.
	 */
	public SystemImportCertWizardAliasPage(Wizard wizard, List certs)
	{
		super(wizard, "SpecifyAlias", 
  		      UniversalSecurityProperties.RESID_SECURITY_TRUST_WIZ_ALIAS_TITLE, 
		      UniversalSecurityProperties.RESID_SECURITY_TRUST_WIZ_ALIAS_DESC);
	}

	/**
	 * CreateContents is the one method that must be overridden from the parent class.
	 * In this method, we populate an SWT container with widgets and return the container
	 *  to the caller (JFace). This is used as the contents of this page.
	 */
	public Control createContents(Composite parent)
	{
		Composite content = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		GridData data = new GridData(GridData.FILL_BOTH);
		layout.numColumns = 3;
		content.setLayout(layout);
		content.setLayoutData(data);	
		
		SystemWidgetHelpers.createLabel(content, UniversalSecurityProperties.RESID_SECURITY_CERTIFICATE_ALIAS);
		_alias = SystemWidgetHelpers.createTextField(content, null);
		_alias.addModifyListener(
				new ModifyListener() 
				{
					public void modifyText(ModifyEvent e) 
					{
						validateNameInput();
					}
				}
			);		
		return _alias;
	}
	
	
	public X509CertificateElement getElement(Object cert)
	{
		if (cert instanceof X509Certificate)
		{
			return new X509CertificateElement(null, 
					UniversalSecurityProperties.RESID_SECURITY_TRUSTED_CERTIFICATE, 
					(X509Certificate)cert);
		}
		return null;
	}
	

	
	/**
	 * Return the Control to be given initial focus.
	 * Override from parent. Return control to be given initial focus.
	 */
	protected Control getInitialFocusControl()
	{
        return _alias;
	}
	
	/**
	 * Init values using input data
	 */
	protected void initializeInput()
	{
	}
	
  	/**
	 * This hook method is called whenever the text changes in the input field.
	 * The default implementation delegates the request to an <code>ISystemValidator</code> object.
	 * If the <code>ISystemValidator</code> reports an error the error message is displayed
	 * in the Dialog's message line.
	 */	
	protected SystemMessage validateNameInput() 
	{	
		errorMessage = null;
		this.clearErrorMessage();	
	    if (nameValidator != null)
	      errorMessage= nameValidator.validate(_alias.getText());
	    if (errorMessage != null)
		  setErrorMessage(errorMessage);		
		setPageComplete(errorMessage==null);
		return errorMessage;		
	}
	
	/**
	 * Completes processing of the wizard. If this 
	 * method returns true, the wizard will close; 
	 * otherwise, it will stay active.
	 * This method is an override from the parent Wizard class. 
	 *
	 * @return whether the wizard finished successfully
	 */
	public boolean performFinish() 
	{
		
	    return true;
	}
    
	// --------------------------------- //
	// METHODS FOR EXTRACTING USER DATA ... 
	// --------------------------------- //
	/**
	 * Return user-entered new file name.
	 * Call this after finish ends successfully.
	 */
	public String getAlias()
	{
		return _alias.getText();
	}    

	/**
	 * Return true if the page is complete, so to enable Finish.
	 * Called by wizard framework.
	 */
	public boolean isPageComplete()
	{
		return (errorMessage==null) && (_alias.getText().trim().length()>0);
	}
	

}