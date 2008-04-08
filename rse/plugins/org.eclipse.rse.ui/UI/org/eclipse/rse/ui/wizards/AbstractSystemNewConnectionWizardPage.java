/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
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
 * Uwe Stieber (Wind River) - Reworked new connection wizard extension point.
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186748] Move ISubSystemConfigurationAdapter from UI/rse.core.subsystems.util
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 ********************************************************************************/

package org.eclipse.rse.ui.wizards;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.ui.SystemConnectionForm;
import org.eclipse.rse.ui.wizards.newconnection.ISystemNewConnectionWizardPage;
import org.eclipse.rse.ui.wizards.newconnection.RSEAbstractNewConnectionWizard;
import org.eclipse.rse.ui.wizards.newconnection.RSEDefaultNewConnectionWizardMainPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * A base class for additional pages that are to be appended to the New Connection wizard.
 * @see org.eclipse.rse.ui.subsystems.ISubSystemConfigurationAdapter#getNewConnectionWizardPages(ISubSystemConfiguration, org.eclipse.jface.wizard.IWizard)
 */
public abstract class AbstractSystemNewConnectionWizardPage extends AbstractSystemWizardPage 
       implements ISystemNewConnectionWizardPage, IWizardPage
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
     * Get the parent wizard typed as the RSEAbstractNewConnectionWizard
     */
    public RSEAbstractNewConnectionWizard getNewConnectionWizard()
    {
    	if (getWizard() instanceof RSEAbstractNewConnectionWizard)
        return (RSEAbstractNewConnectionWizard)getWizard();
    	
    	return null;
    }

    /**
     * Get the main page of RSEDefaultNewConnectionWizard, which contains all user enter connection attributes
     */
    public RSEDefaultNewConnectionWizardMainPage getMainPage() {
    	RSEAbstractNewConnectionWizard ourWizard = getNewConnectionWizard();
    	if (ourWizard != null) {
    		IWizardPage wizardPage = ourWizard.getStartingPage();
    		if (wizardPage instanceof RSEDefaultNewConnectionWizardMainPage) {
    			return (RSEDefaultNewConnectionWizardMainPage)wizardPage;
    		}
    	}
  		return null;
    }

    /**
		 * Get the SystemConnectionForm of the main page of SystemNewConnectionWizard, which contains all user enter
		 * connection attributes
		 */
    public SystemConnectionForm getMainPageForm()
    {
    	RSEAbstractNewConnectionWizard ourWizard = getNewConnectionWizard();
    	if (ourWizard != null) {
      	  //String[] systemTypes = parentFactory.getSystemTypes();
    	  //IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemType(systemTypes[0]);
    	  IWizardPage wizardPage = ourWizard.getStartingPage();
    	  
    	  if (wizardPage instanceof RSEDefaultNewConnectionWizardMainPage) {
    		  return ((RSEDefaultNewConnectionWizardMainPage)wizardPage).getSystemConnectionForm();
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