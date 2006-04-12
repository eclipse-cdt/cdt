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
import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.dialogs.SystemSimpleSelectDialog;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogInterface;
import org.eclipse.rse.ui.filters.SystemFilterUIHelpers;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the Select Filter Pools dialog, and which returns
 * an array of selected filter pools.
 * <p>
 * Dialog will display a root node for each manager, and then the filter pools
 * within each manager as children. User can select any pool from any of the 
 * given managers.
 * <p>
 * Uses getName() on manager for display name of root nodes.
 * <p>
 * Typically, such a dialog is used to allow the user to select a subset of pools
 * that they will access in some context. There is framework support for such
 * selections, via SystemFilterPoolReferences. Each of these are a reference to a 
 * filter pool, and the SystemFilterPoolReferenceManager class offers full support
 * for manager a list of such references, optionally even saving and restoring such
 * a list.
 * 
 * <p>
 * You call the setFilterPoolManagers method to set the array of filter pool managers
 * this dialog allows the user to select from.
 * <p>
 * If you also call the optional method setFilterPoolReferenceManager, you need not 
 * subclass this action. It will handle everything for you!!
 * <ul
 *   <li>Preselects the filter pools currently referenced by one or more reference objects
 *          in the filter pool reference manager.
 *   <li>What OK is pressed, removes the previous references from the reference manager,
 *          and adds references for the pools selected by the user.
 * </ul>
 * 
 * You can either supply the label, dialog title, dialog prompt, filter pool image,
 * input filter pool managers and filter pool reference manager by calling the 
 * appropriate setXXX methods, or by overriding the related getXXX methods.
 */
public class SystemFilterSelectFilterPoolsAction 
       extends SystemFilterAbstractFilterPoolAction
       
{


	/**
	 * Constructor when default label desired.
	 */
	public SystemFilterSelectFilterPoolsAction(Shell parent) 
	{
		super(parent,
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SELECTFILTERPOOLS_ID),
		      SystemResources.ACTION_SELECTFILTERPOOLS_LABEL, SystemResources.ACTION_SELECTFILTERPOOLS_TOOLTIP);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CHANGE);        		
		// set default help for action and dialog
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0043");
		setDialogHelp(RSEUIPlugin.HELPPREFIX + "dsfp0000");
	}	
	/**
	 * Constructor when given the translated action label
	 */
	public SystemFilterSelectFilterPoolsAction(Shell parent, String title) 
	{
		super(parent, title);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CHANGE);        		
		// set default help for action and dialog
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0043");
		setDialogHelp(RSEUIPlugin.HELPPREFIX + "dsfp0000");
	}
	
	
	/**
	 * Constructor when given the translated action label
	 */
	public SystemFilterSelectFilterPoolsAction(Shell parent, String title, String tooltip) 
	{
		super(parent, title, tooltip);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CHANGE);        		
		// set default help for action and dialog
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0043");
		setDialogHelp(RSEUIPlugin.HELPPREFIX + "dsfp0000");
	}
	

    /**
     * Override of init in parent
     */
    protected void init()
    {
    	super.init();
        dlgInputs.prompt = SystemResources.RESID_SELECTFILTERPOOLS_PROMPT;
        dlgInputs.title = SystemResources.RESID_SELECTFILTERPOOLS_TITLE;
    }

	/**
     * Creates our select-filter-pools dialog, and populates it with the list of
     * filter pools to select from.
     * <p>
     * <i>Assumes setFilterPoolManagers has been called.</i>
     * <p>
     * Dialog will display a root node for each manager, and then the filter pools
     * within each manager as children. User can select any pool from any of the 
     * given managers.
     * <p>
     * Uses getName() on manager for display name of root nodes.
     *
 	 * @see org.eclipse.rse.ui.actions.SystemBaseDialogAction#run()
	 */
	protected Dialog createDialog(Shell parent)
	{
		SystemSimpleSelectDialog dialog = 
		   new SystemSimpleSelectDialog(parent, getDialogTitle(), getDialogPrompt());
		   
    	ISystemFilterPoolManager[] mgrs = getFilterPoolManagers();
		ISystemFilterPoolReferenceManagerProvider sprmp = getReferenceManagerProviderSelection();
		ISystemFilterPoolManager[] additionalMgrs = null;
		if (sprmp != null)
          additionalMgrs = sprmp.getSystemFilterPoolReferenceManager().getAdditionalSystemFilterPoolManagers();
        if (additionalMgrs != null)
        {
          ISystemFilterPoolManager[] allmgrs = new ISystemFilterPoolManager[mgrs.length+additionalMgrs.length];
          int allidx = 0;
          for (int idx=0; idx<mgrs.length; idx++)
             allmgrs[allidx++] = mgrs[idx];
          for (int idx=0; idx<additionalMgrs.length; idx++)
             allmgrs[allidx++] = additionalMgrs[idx];
          mgrs = allmgrs;
        }

		SystemSimpleContentElement input = 
		      SystemFilterUIHelpers.getFilterPoolModel(getFilterPoolManagerProvider(), mgrs);
		preSelect(input);
		setValue(input);

		if (sprmp != null)
		{
			ISystemFilterPoolManager initialSelection = sprmp.getSystemFilterPoolReferenceManager().getDefaultSystemFilterPoolManager();
			if (initialSelection != null)
			{
			  SystemSimpleContentElement initialElementSelection = SystemFilterUIHelpers.getDataElement(input, initialSelection);
			  if (initialElementSelection != null)
			    dialog.setRootToPreselect(initialElementSelection);
			}
		}
		return dialog;
	}
	/**
	 * We override createDialog from parent, so this is a no-op.
	 */
	public SystemFilterPoolDialogInterface createFilterPoolDialog(Shell parent)
	{
		return null;
	}
	    
    /**
     * Walk elements deciding pre-selection
     */
    protected void preSelect(SystemSimpleContentElement inputElement)
    {
    	super.preSelect(inputElement);
    }
	/**
	 * Decide per pool if it should be selected or not.
	 * Default behaviour is to select it if it is currently referenced.
	 */
	protected boolean getFilterPoolPreSelection(ISystemFilterPool pool)
	{
        ISystemFilterPoolReferenceManagerProvider refMgrProvider = getReferenceManagerProviderSelection();
        if (refMgrProvider != null)
        {
          ISystemFilterPoolReferenceManager refMgr = refMgrProvider.getSystemFilterPoolReferenceManager();
          if (refMgr != null)
          {
          	return refMgr.isSystemFilterPoolReferenced(pool);
          }
          else
            return false;
        }
        else
		  return false;
	}
	
	/**
     * Called by parent class after dialog returns. We set the internal value attribute
     * to be the array of user selected filter pools.
     * <p>
	 * After this action executes, simply call getValue() to get an 
	 * array of SystemFilterPool objects representing what pools the user selected.
	 * A result of null means the dialog was cancelled.
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		SystemSimpleSelectDialog dialog = (SystemSimpleSelectDialog)dlg;
		if (!dialog.wasCancelled())
		{
          Vector selectedFilterPools = new Vector();			
		  SystemSimpleContentElement inputElement = dialog.getUpdatedContent();		  
		  SystemSimpleContentElement[] mgrElements = inputElement.getChildren();
		  for (int idx=0; idx<mgrElements.length; idx++)
		  {
		     SystemSimpleContentElement[] poolElements = mgrElements[idx].getChildren();
		     for (int jdx=0; jdx<poolElements.length; jdx++)
		     {
          	    if (poolElements[jdx].isSelected())
		     	  selectedFilterPools.addElement(poolElements[jdx].getData());
		     }
		  }
		  
          ISystemFilterPool[] selectedPoolArray = new ISystemFilterPool[selectedFilterPools.size()];
          for (int idx=0; idx<selectedFilterPools.size(); idx++)
             selectedPoolArray[idx] = (ISystemFilterPool)selectedFilterPools.elementAt(idx);          
		  return selectedPoolArray;
		}
		else
		  return null;
	}


	/**
	 * Method called when ok pressed on dialog and after getDialogValue has set the
	 * value attribute to an array of SystemFilterPool objects for the selected pools.
	 * <p>
	 * By default, if the current selected object implements SystemFilterPoolReferenceManagerProvider,
	 * then this will call setFilterPoolReferences on that selected object.
	 * <p>
	 * @param dlgOutput The array of SystemFilterPools selected by the user, as set in getDialogValue()
	 */
	public void doOKprocessing(Object dlgOutput)
	{
		ISystemFilterPool[] selectedPools = (ISystemFilterPool[])dlgOutput;
		ISystemFilterPoolReferenceManagerProvider sfprmp = getReferenceManagerProviderSelection();
		if (sfprmp != null)
		{
		  sfprmp.getSystemFilterPoolReferenceManager().setSystemFilterPoolReferences(selectedPools,true);
		}
	}
	
}