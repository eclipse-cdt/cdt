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

package org.eclipse.rse.ui.wizards;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * A base class for additional pages that are to be appended to the New Connection wizard.
 * @see org.eclipse.rse.core.subsystems.ISubSystemConfiguration#getNewConnectionWizardPages(IWizard)
 */
public abstract class AbstractSystemNewConnectionWizardPage extends AbstractSystemWizardPage 
       implements ISystemNewConnectionWizardPage
{	
	protected ISubSystemConfiguration parentFactory;
    
	/**
	 * Constructor that takes everything
	 */
	public AbstractSystemNewConnectionWizardPage(IWizard wizard, ISubSystemConfiguration parentFactory, String pageName, String pageTitle, String pageDescription)
	{
		super(wizard, pageName, pageTitle, pageDescription);
		this.parentFactory = parentFactory;
	}
	/**
	 * Constructor that defaults:
	 * <ul>
	 *   <li>the page name to the parent factory's id
	 *   <li>the page title to the parent factory's name
	 * </ul>
	 */
	public AbstractSystemNewConnectionWizardPage(IWizard wizard, ISubSystemConfiguration parentFactory, String pageDescription)
	{
		super(wizard, parentFactory.getId(), parentFactory.getName(), pageDescription);
		this.parentFactory = parentFactory;
	}
	/**
	 * Constructor that defaults:
	 * <ul>
	 *   <li>the page name to the parent factory's id
	 *   <li>the page title to the parent factory's name
	 *   <li>the page description to RESID_NEWCONN_SUBSYSTEMPAGE_DESCRIPTION
	 * </ul>
	 */
	public AbstractSystemNewConnectionWizardPage(IWizard wizard, ISubSystemConfiguration parentFactory)
	{
		super(wizard, parentFactory.getId(), parentFactory.getName(), SystemResources.RESID_NEWCONN_SUBSYSTEMPAGE_DESCRIPTION);
		this.parentFactory = parentFactory;
	}
	
	/**
	 * Return the subsystem factory that supplied this page
	 */
	public ISubSystemConfiguration getSubSystemConfiguration()
	{
		return parentFactory;
	}

	/**
	 * @see AbstractSystemWizardPage#getInitialFocusControl()
	 */
	protected Control getInitialFocusControl() 
	{
		return null;
	}

	/**
	 * @see AbstractSystemWizardPage#createContents(Composite)
	 */
	public abstract Control createContents(Composite parent);

	/**
	 * @see ISystemWizardPage#performFinish()
	 */
	public boolean performFinish() 
	{
		return true;
	}

    /**
     * Get the parent wizard typed as the SystemNewConnectionWizard
     */
    public RSENewConnectionWizard getNewConnectionWizard()
    {
        return (RSENewConnectionWizard)getWizard();
    }

    /**
     * Get the main page of SystemNewConnectionWizard, which contains all user enter connection attributes
     */
    public ISystemNewConnectionWizardMainPage getMainPage()
    {
    	RSENewConnectionWizard ourWizard = getNewConnectionWizard();
    	if (ourWizard != null) {
    	  String[] systemTypes = parentFactory.getSystemTypes();
    	  IRSESystemType systemType = RSECorePlugin.getDefault().getRegistry().getSystemType(systemTypes[0]);
    	  IWizardPage wizardPage = ourWizard.getDelegate().getMainPage();
    	  
    	  if (wizardPage instanceof ISystemNewConnectionWizardMainPage) {
    		  return (ISystemNewConnectionWizardMainPage)wizardPage;
    	  }
    	  else {
    		  return null;
    	  }
    	}
        else {
          return null;
        }
    }

    /**
     * Get the SystemConnectionForm of the main page of SystemNewConnectionWizard, which 
     *  contains all user enter connection attributes
     */
    public SystemConnectionForm getMainPageForm()
    {
    	RSENewConnectionWizard ourWizard = getNewConnectionWizard();
    	if (ourWizard != null) {
      	  String[] systemTypes = parentFactory.getSystemTypes();
    	  IRSESystemType systemType = RSECorePlugin.getDefault().getRegistry().getSystemType(systemTypes[0]);
    	  IWizardPage wizardPage = ourWizard.getDelegate().getMainPage();
    	  
    	  if (wizardPage instanceof RSENewConnectionWizardDefaultDelegateMainPage) {
    		  return ((RSENewConnectionWizardDefaultDelegateMainPage)wizardPage).getForm();
    	  }
    	  else {
    		  return null;
    	  }
        }
    	else {
    		return null;
    	}
    }
}