/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David Dykstal (IBM) - [194268] action now assumes the first filter pool manager is selected if there was no selection
 *                       provided by the caller.
 * David McKnight   (IBM)        - [225506] [api][breaking] RSE UI leaks non-API types                    
 *******************************************************************************/

package org.eclipse.rse.internal.ui.actions;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rse.core.filters.ISystemFilterPoolManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.filters.SystemFilterPoolManagerUIProvider;
import org.eclipse.rse.internal.ui.filters.dialogs.SystemFilterWorkWithFilterPoolsDialog;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.dialogs.SystemSimpleContentElement;
import org.eclipse.rse.ui.filters.SystemFilterPoolDialogInterface;
import org.eclipse.rse.ui.filters.SystemFilterUIHelpers;
import org.eclipse.rse.ui.filters.actions.SystemFilterAbstractFilterPoolAction;
import org.eclipse.rse.ui.validators.ValidatorFilterPoolName;
import org.eclipse.swt.widgets.Shell;



/**
 * The action that displays the Work With Filter Pools dialog
 */
public class SystemFilterWorkWithFilterPoolsAction 
       extends SystemFilterAbstractFilterPoolAction 
       implements  SystemFilterPoolManagerUIProvider
{

	private ValidatorFilterPoolName poolNameValidator = null;

	/**
	 * Constructor when default label desired.
	 */
	public SystemFilterWorkWithFilterPoolsAction(Shell parent) 
	{
		super(parent,
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_WORKWITHFILTERPOOLS_ID),		
		      SystemResources.ACTION_WORKWITH_FILTERPOOLS_LABEL, SystemResources.ACTION_WORKWITH_FILTERPOOLS_TOOLTIP);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_WORKWITH);
		allowOnMultipleSelection(false);
		// set default action and dialog help 
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0044"); //$NON-NLS-1$
		setDialogHelp(RSEUIPlugin.HELPPREFIX + "dwfp0000");		 //$NON-NLS-1$
	}	
	/**
	 * Constructor when default label desired, and you want to choose between
	 *  Work With -> Filter Pools and Work With Filter Pools. 
	 */
	public SystemFilterWorkWithFilterPoolsAction(Shell parent, boolean cascadingAction) 
	{
		super(parent,
			  RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_WORKWITHFILTERPOOLS_ID),		
			  cascadingAction ? SystemResources.ACTION_WORKWITH_FILTERPOOLS_LABEL : SystemResources.ACTION_WORKWITH_WWFILTERPOOLS_LABEL,
			  cascadingAction ? SystemResources.ACTION_WORKWITH_FILTERPOOLS_TOOLTIP : SystemResources.ACTION_WORKWITH_WWFILTERPOOLS_TOOLTIP 
		);
		if (cascadingAction)
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_WORKWITH);
		else
			setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE);
		allowOnMultipleSelection(false);
		// set default action and dialog help 
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0044"); //$NON-NLS-1$
		setDialogHelp(RSEUIPlugin.HELPPREFIX + "dwfp0000");		 //$NON-NLS-1$
	}		
	/**
	 * Constructor when given the translated action label
	 */
	public SystemFilterWorkWithFilterPoolsAction(Shell parent, String title) 
	{
		super(parent, title);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_REORGANIZE); 
		allowOnMultipleSelection(false);
		// set default action and dialog help 
		setHelp(RSEUIPlugin.HELPPREFIX + "actn0044"); //$NON-NLS-1$
		setDialogHelp(RSEUIPlugin.HELPPREFIX + "dwfp0000");		 //$NON-NLS-1$
	}


    /**
     * Override of init in parent
     */
    protected void init()
    {
    	super.init();
        dlgInputs.prompt = SystemResources.RESID_WORKWITHFILTERPOOLS_PROMPT;
        dlgInputs.title = SystemResources.RESID_WORKWITHFILTERPOOLS_TITLE;
    }

	/**
	 * Reset between runs
	 */
	public void reset()
	{
	}
	
	/**
	 * Set the pool name validator for the rename action.
	 * The work-with dialog automatically calls setExistingNamesList on it for each selection.
	 */
	public void setFilterPoolNameValidator(ValidatorFilterPoolName pnv)
	{
		this.poolNameValidator = pnv;
	}
	
    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean checkObjectType(Object selectedObject)
	{
		//return (selectedObject instanceof SystemFilterPoolReferenceManagerProvider); // override as appropriate
		return true; // override as appropriate
	}
	
	
	/**
	 * Override of parent to create and return our specific filter pool dialog.
	 */
	public SystemFilterPoolDialogInterface createFilterPoolDialog(Shell parent)
	{
    	//SystemFilterPoolManager[] mgrs = getFilterPoolManagers();
		//SystemSimpleContentElement input = getTreeModel();
		      //SystemFilterUIHelpers.getFilterPoolModel(getFilterPoolImageDescriptor(),mgrs);		
		      
		SystemFilterWorkWithFilterPoolsDialog dialog = 
		   	new SystemFilterWorkWithFilterPoolsDialog(parent, getDialogTitle(), getDialogPrompt(), this);

        if (poolNameValidator != null)
          	dialog.setFilterPoolNameValidator(poolNameValidator);
          
	    //SystemSimpleContentElement initialElementSelection = getTreeModelPreSelection(input);
	    //if (initialElementSelection != null)
	      //dialog.setRootToPreselect(initialElementSelection);
	      
		return dialog;		
	}
	
	/**
	 * Callback for dialog to refresh its contents
	 */
	public SystemSimpleContentElement getTreeModel()
	{
    	ISystemFilterPoolManager[] mgrs = getFilterPoolManagers();
		SystemSimpleContentElement input = 
		      SystemFilterUIHelpers.getFilterPoolModel(getFilterPoolManagerProvider(), mgrs);		
        return input;		
	}

	/**
	 * Callback for dialog to refresh its contents
	 */
	public SystemSimpleContentElement getTreeModelPreSelection(SystemSimpleContentElement input) {
		SystemSimpleContentElement initialElementSelection = null;
		ISystemFilterPoolReferenceManagerProvider sprmp = getReferenceManagerProviderSelection();
		if (sprmp != null) {
			ISystemFilterPoolManager initialSelection = sprmp.getSystemFilterPoolReferenceManager().getDefaultSystemFilterPoolManager();
			if (initialSelection != null) {
				initialElementSelection = SystemFilterUIHelpers.getDataElement(input, initialSelection);
			}
		} else {
			SystemSimpleContentElement[] children = input.getChildren();
			if (children.length > 0) {
				initialElementSelection = children[0];
			}
		}
		return initialElementSelection;
	}
	
	/**
	 * We are a special case of dialog, where we do not need to do anything
	 * upon return from the dialog, as the dialog itself does it all.
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		return null;
	}
	
	/**
	 * Because we return null from getDialogValue(Dialog dlg), this
	 * method will never be called.
	 */
	public void doOKprocessing(Object dlgValue)
	{
		
	}
}
