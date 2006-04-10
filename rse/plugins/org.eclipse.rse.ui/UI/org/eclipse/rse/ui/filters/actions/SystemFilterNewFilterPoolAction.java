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

package org.eclipse.rse.ui.filters.actions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.ISystemWizardAction;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogOutputs;
import org.eclipse.rse.ui.filters.dialogs.SystemFilterNewFilterPoolWizard;
import org.eclipse.rse.ui.filters.dialogs.SystemFilterPoolWizardDialog;
import org.eclipse.rse.ui.filters.dialogs.SystemFilterPoolWizardInterface;
import org.eclipse.rse.ui.filters.dialogs.SystemFilterWorkWithFilterPoolsDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the New Filter Pool wizard
 * @see #setHelpContextId(String)
 */
public class SystemFilterNewFilterPoolAction 
       extends SystemFilterAbstractFilterPoolWizardAction 
       implements  ISystemWizardAction
{
	
    private SystemFilterWorkWithFilterPoolsDialog wwdialog = null;
    //private SystemFilterNewFilterPoolWizard wizard = null;
    	
	/**
	 * Constructor for SystemNewFilterPoolAction when not called from work-with dialog.
	 */
	public SystemFilterNewFilterPoolAction(Shell parent) 
	{
	    this(parent, null);
	}	

	/**
	 * Constructor for SystemNewFilterPoolAction when called from work-with dialog.
	 */
	public SystemFilterNewFilterPoolAction(Shell parent,
	                                       SystemFilterWorkWithFilterPoolsDialog wwdialog) 
	{
		super(parent, 
		      SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILTERPOOL_ID),
		      SystemResources.ACTION_NEWFILTERPOOL_LABEL, SystemResources.ACTION_NEWFILTERPOOL_TOOLTIP);
		this.wwdialog = wwdialog;
		allowOnMultipleSelection(false);		
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);        		
	}
    
    /**
     * Override of init in parent
     */
    protected void init()
    {
    	super.init();
        dlgInputs.prompt = SystemResources.RESID_NEWFILTERPOOL_PAGE1_DESCRIPTION;
        dlgInputs.title = SystemResources.RESID_NEWFILTERPOOL_PAGE1_TITLE;
		dlgInputs.poolNamePrompt = SystemResources.RESID_FILTERPOOLNAME_LABEL;
		dlgInputs.poolNameTip = SystemResources.RESID_FILTERPOOLNAME_TIP;        
		dlgInputs.poolMgrNamePrompt = SystemResources.RESID_FILTERPOOLMANAGERNAME_LABEL;
		dlgInputs.poolMgrNameTip = SystemResources.RESID_FILTERPOOLMANAGERNAME_TIP;        
    }

    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean checkObjectType(Object selectedObject)
	{
		if (selectedObject instanceof SystemSimpleContentElement)
		  selectedObject = ((SystemSimpleContentElement)selectedObject).getData();
		boolean enable = 
		  (selectedObject instanceof ISystemFilterPoolReferenceManagerProvider) ||
		  (selectedObject instanceof ISystemFilterPoolManager) ||
		  (selectedObject instanceof ISystemFilterPool);
		return enable;
	}

	
	/**
	 * Return the wizard so we can customize it prior to showing it.
	 * Returns new SystemFilterNewFilterPoolWizard(). Override to replace with your own.
	 */
	public SystemFilterPoolWizardInterface getFilterPoolWizard()
	{
		//if (wizard == null)
		//  wizard = new SystemFilterNewFilterPoolWizard();
		//return wizard;
		return new SystemFilterNewFilterPoolWizard();
	}

	/**
     * Overrides parent. Called after dialog dismissed.
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		SystemFilterPoolWizardDialog wizardDlg = (SystemFilterPoolWizardDialog)dlg;
        SystemFilterPoolDialogOutputs dlgOutput = wizardDlg.getFilterPoolDialogOutputs();
        return dlgOutput;
	}

	/**
     * Overrides parent. Called after dialog dismissed and getDialogValue called.
     * The output of getDialogValue passed as input here.
	 */
	public void doOKprocessing(Object dlgValue)
	{
	  //System.out.println("In SystemFilterNewFIlterPoolAction.doOKProcessing");
        SystemFilterPoolDialogOutputs dlgOutput = (SystemFilterPoolDialogOutputs)dlgValue;
        // called from WorkWith dialog... we do not offer to create a reference...
        if ((dlgOutput.newPool != null) && (wwdialog != null))
          wwdialog.addNewFilterPool(getShell(), dlgOutput.newPool);
        else if (dlgOutput.newPool != null)
        {
          ISystemFilterPoolReferenceManagerProvider sfprmp = getReferenceManagerProviderSelection();
          // Action selected by user when a reference manager provider was selected.
          // Seems obvious then that the user wishes to see the newly created pool, so
          //  we take the liberty of creating a reference object...
          if (sfprmp != null)
          {
          	ISystemFilterPoolReferenceManager sfprm = sfprmp.getSystemFilterPoolReferenceManager();
	  //System.out.println("...calling addREferenceToSystemFilterPool...");
          	sfprm.addReferenceToSystemFilterPool(dlgOutput.newPool);
	  //System.out.println("...back from addREferenceToSystemFilterPool");
          }
        }
	}

	/**
	 * Returns array of managers to show in combo box.
	 * Overrides parent to call back to wwdialog if not null.
	 */
    public ISystemFilterPoolManager[] getFilterPoolManagers()
	{
	   if (wwdialog != null)
	     return wwdialog.getFilterPoolManagers();
       else 
         return super.getFilterPoolManagers();
	}
	
	/**
	 * Returns the zero-based index of the manager name to preselect.
	 * Overrides parent to call back to wwdialog if not null.
	 */
	public int getFilterPoolManagerNameSelectionIndex()
	{
	   if (wwdialog != null)
	     return wwdialog.getFilterPoolManagerSelection();
	   else
         return super.getFilterPoolManagerNameSelectionIndex();
	}

}