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

package org.eclipse.rse.ui.wizards;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.messages.SystemMessageDialog;

/**
 * Wizard for creating a new remote system profile.
 */
public class      SystemNewProfileWizard
  	   extends    AbstractSystemWizard
	   
{	
	
	private SystemNewProfileWizardMainPage mainPage;

    /**
     * Constructor
     */	
	public SystemNewProfileWizard()
	{
		super(SystemResources.RESID_NEWPROFILE_TITLE,
	  	      SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWPROFILEWIZARD_ID));		      
	}
	
	/**
	 * Creates the wizard pages.
	 * This method is an override from the parent Wizard class.
	 */
	public void addPages()
	{
	   try {
	      mainPage = createMainPage();	        
	      addPage((WizardPage)mainPage);
	      //super.addPages();
	   } catch (Exception exc)
	   {
	   	 SystemBasePlugin.logError("New connection: Error in createPages: ",exc);
	   }
	}

	/**
	 * Creates the wizard's main page. 
	 * This method is an override from the parent class.
	 */
	protected SystemNewProfileWizardMainPage createMainPage()
	{
	    mainPage = new SystemNewProfileWizardMainPage(this);
	    return mainPage;
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
		boolean ok = true;
		if (mainPage.performFinish())
		{
            //SystemMessage.showInformationMessage(getShell(),"Finish pressed.");				  	
            ISystemRegistry sr = SystemPlugin.getTheSystemRegistry();
            String name = mainPage.getProfileName();
            boolean makeActive = mainPage.getMakeActive();
            try 
            {
                 sr.createSystemProfile(name,makeActive);
            } catch (Exception exc)
            {
               	 String msg = "Exception creating profile ";
               	 SystemBasePlugin.logError(msg,exc);
               	 //System.out.println(msg + exc.getMessage() + ": " + exc.getClass().getName());
               	 SystemMessageDialog.displayExceptionMessage(getShell(),exc);
            }
		    return ok;
		}
		else
		  ok = false;
	    return ok;
	}

} // end class