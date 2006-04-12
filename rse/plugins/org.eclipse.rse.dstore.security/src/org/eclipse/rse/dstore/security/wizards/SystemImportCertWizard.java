/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;

import org.eclipse.dstore.core.util.ssl.DStoreKeyStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.comm.ISystemKeystoreProvider;
import org.eclipse.rse.dstore.security.ImageRegistry;
import org.eclipse.rse.dstore.security.UniversalSecurityProperties;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.wizards.AbstractSystemWizard;

public class SystemImportCertWizard 
                  extends AbstractSystemWizard 
                  implements  ISystemMessages 
{	
	
	private SystemImportCertWizardMainPage  _mainPage;
	private SystemImportCertWizardAliasPage _aliasPage;
	private ISystemKeystoreProvider _provider;
	private String _systemName;
  

    /**
     * Constructor
     */	
	public SystemImportCertWizard(ISystemKeystoreProvider provider, String systemName)
	{
	   	super(UniversalSecurityProperties.RESID_SECURITY_TRUST_IMPORT_CERTIFICATE_WIZARD,
				ImageRegistry.getImageDescriptor(ImageRegistry.IMG_WZ_IMPORT_CERTIF));	
		_provider = provider;
		_systemName = systemName;
	}
	
	/**
	 * Creates the wizard pages.
	 * This method is an override from the parent Wizard class.
	 */
	public void addPages()
	{
	   try {
	      _mainPage = createMainPage();	        
	      addPage((WizardPage)_mainPage);
		  
		  _aliasPage = createAliasPage();
		  addPage((WizardPage)_aliasPage);
	      //super.addPages();
	   } catch (Exception exc)
	   {
	   	 SystemBasePlugin.logError("New File: Error in createPages: ",exc);
	   }
	} 

	/**
	 * Creates the wizard's main page. 
	 * This method is an override from the parent class.
	 */
	protected SystemImportCertWizardMainPage createMainPage()
	{
		SystemMessage errMsg = null;
		
   	    _mainPage = new SystemImportCertWizardMainPage(this, getCertificates()); 
   	    if (errMsg != null)
   	      _mainPage.setErrorMessage(errMsg);
   	    return _mainPage;
	}     
	
	/**
	 * Creates the wizard's main page. 
	 * This method is an override from the parent class.
	 */ 
	protected SystemImportCertWizardAliasPage createAliasPage()
	{
		SystemMessage errMsg = null;
		
   	    _aliasPage = new SystemImportCertWizardAliasPage(this, getCertificates(), _systemName); 
   	    if (errMsg != null)
   	      _aliasPage.setErrorMessage(errMsg);
   	    return _aliasPage;
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
		boolean ok = false;
		if (_aliasPage.performFinish())
		{
			List certs = getCertificates();
			for (int i = 0; i < certs.size(); i++)
			{
				X509Certificate cert = (X509Certificate)certs.get(i);
				if (cert != null)
				{
					String alias = _aliasPage.getAlias();
					
					try
					{
						KeyStore ks = DStoreKeyStore.getKeyStore(_provider.getKeyStorePath(), _provider.getKeyStorePassword());
						DStoreKeyStore.addCertificateToKeyStore(ks, cert, alias);
						DStoreKeyStore.persistKeyStore(ks, _provider.getKeyStorePath(), _provider.getKeyStorePassword());
						ok = true;
					}
					catch (Exception e)
					{
						e.printStackTrace();
						ok = false;
					}
				}
			}
		}
		return ok;
	}
	
	
	public List getCertificates()
	{
		return (List)getInputObject();
	}
	
	
	
} // end class