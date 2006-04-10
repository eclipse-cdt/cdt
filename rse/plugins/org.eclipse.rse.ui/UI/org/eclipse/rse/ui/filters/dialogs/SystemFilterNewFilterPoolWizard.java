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

package org.eclipse.rse.ui.filters.dialogs;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogOutputs;
import org.eclipse.rse.ui.filters.actions.SystemFilterAbstractFilterPoolAction;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.validators.ValidatorFolderName;
import org.eclipse.rse.ui.wizards.AbstractSystemWizard;


/**
 * Wizard for creating a new system filter pool.
 */
public class      SystemFilterNewFilterPoolWizard
  	   extends    AbstractSystemWizard
	   implements SystemFilterPoolWizardInterface
{	
	protected SystemFilterNewFilterPoolWizardMainPageInterface mainPage;
	protected ValidatorFolderName usv;
    protected SystemFilterPoolDialogOutputs output;
    protected SystemFilterAbstractFilterPoolAction caller;
	protected ISystemFilterPoolManager[] mgrs;

    /**
     * Constructor that uses a default title and image
     */	
	public SystemFilterNewFilterPoolWizard()
	{
		this(SystemResources.RESID_NEWFILTERPOOL_TITLE,
		     SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILTERPOOLWIZARD_ID));
	}
    /**
     * Constructor
     * @param label The title for this wizard
     * @param image The image for this wizard
     */	
	public SystemFilterNewFilterPoolWizard(String title, ImageDescriptor image)
	{
		super(title, image);
	}	

    /**
     * Set the help context Id (infoPop) for this wizard. This must be fully qualified by
     *  plugin ID.
     */
    public void setHelpContextId(String id)
    {
    	super.setHelp(id);
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
	   	 System.out.println("Unexpected error in addPages of NewFilterPoolWizard: "+exc.getMessage() + ", " + exc.getClass().getName());
	   }
	}


	/**
	 * Creates the wizard's main page. 
	 */
	protected SystemFilterNewFilterPoolWizardMainPageInterface createMainPage()
	{
	    mainPage = new SystemFilterNewFilterPoolWizardDefaultMainPage(this,
	            caller.getDialogTitle(), caller.getDialogPrompt());
	    mgrs = caller.getFilterPoolManagers();
	    if (mgrs != null)
	    {
	      mainPage.setFilterPoolManagers(mgrs);
	      mainPage.setFilterPoolManagerNameSelectionIndex(caller.getFilterPoolManagerNameSelectionIndex());	      
	    }
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
		if (mainPage.performFinish())
		{
		  	output = mainPage.getFilterPoolDialogOutputs();
		  	String mgrName = output.filterPoolManagerName;
		  	ISystemFilterPoolManager mgr = null;
		    try
		    {
		  	  if (mgrName != null)
		  	  {		  	   
		  	     for (int idx=0; (mgr==null)&&(idx<mgrs.length); idx++)
		  	       if (mgrs[idx].getName().equalsIgnoreCase(mgrName))
		  	         mgr = mgrs[idx];
		   	  }
		   	  if (mgr == null)
		   	  {
		   	  	System.out.println("Unexpected problem in performFinish of filter pool wizard: no match for selected profile name " + mgrName);
		   	  	return false;
		   	  }
		  	  String poolName = mainPage.getPoolName();
		  	  output.newPool = createFilterPool(mgr, poolName);
		    }
		    catch (Exception exc)
		    {
		    	//SystemPlugin.logError("Error in performFinish of filter pool wizard!", exc);
		    	//System.out.println("Error in performFinish of filter pool wizard!");
		    	//exc.printStackTrace();
		    	SystemMessage msg = SystemPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_OCCURRED);
		    	msg.makeSubstitution(exc);
		    	SystemMessageDialog msgDlg = new SystemMessageDialog(getShell(), msg);
		    	msgDlg.openWithDetails();
		    	return false;
		    }		  	
		    return (output.newPool != null);
		}
	    return false;
	}
	
	
	/**
	 * Process the create new filter pool request, after user presses OK on the dialog.
	 * By default, asks the selected manager to create the new pool of the given name.
     */
	protected ISystemFilterPool createFilterPool(ISystemFilterPoolManager selectedManager, String poolName)
	          throws Exception
	{
		ISystemFilterPool newFilterPool = null;
        if (selectedManager != null)
          //try {
            newFilterPool = selectedManager.createSystemFilterPool(poolName, true);
          //} catch (Exception exc) 
          //{
	   	    //System.out.println("Unexpected error in createFilterPool of NewFilterPoolWizard: "+exc.getMessage() + ", " + exc.getClass().getName());
          //}
        return newFilterPool;		
	}
	
    /**
     * Return an object containing user-specified information pertinent to filter pool actions
     */
    public SystemFilterPoolDialogOutputs getFilterPoolDialogOutputs()
    {
    	return output;
    }

	/**
	 * Allow base action to pass instance of itself for callback to get info
	 */
    public void setFilterPoolDialogActionCaller(SystemFilterAbstractFilterPoolAction caller)
    {
    	this.caller = caller;
    }

	/**
	 * Allow wizard pages to get this.
	 */
    public SystemFilterAbstractFilterPoolAction getFilterPoolDialogActionCaller()
    {
    	return caller;
    }
	
} // end class