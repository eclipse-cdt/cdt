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

package org.eclipse.rse.ui.filters.actions;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManagerProvider;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseDialogAction;
import org.eclipse.rse.ui.filters.SystemFilterStringEditPane;
import org.eclipse.rse.ui.filters.dialogs.SystemChangeFilterDialog;
import org.eclipse.swt.widgets.Shell;


/**
 * The action that displays the Change Filter dialog
 */
public class SystemChangeFilterAction extends SystemBaseDialogAction 
                                 
{

    private SystemChangeFilterDialog   dlg = null;
    private String                     dlgTitle = null;
    private SystemFilterStringEditPane editPane;    
        
	/**
	 * Constructor for default action label and image
	 */
	public SystemChangeFilterAction(Shell parent) 
	{
		this( parent, SystemResources.ACTION_UPDATEFILTER_LABEL, SystemResources.ACTION_UPDATEFILTER_TOOLTIP,
		      SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CHANGEFILTER_ID));
	}
	
	public SystemChangeFilterAction(Shell parent, String label, String tooltip)
	{
		this(parent, label, tooltip, SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CHANGEFILTER_ID));
	}
	
	public SystemChangeFilterAction(Shell parent, String label, String tooltip, ImageDescriptor image)
	{
		super(label, tooltip, image, parent);
        allowOnMultipleSelection(false);        
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_CHANGE);
		setHelp(SystemPlugin.HELPPREFIX+"acfr0000");
	}


    /**
     * Set the help context Id (infoPop) for this action. This must be fully qualified by
     *  plugin ID.
     * <p>
     * Same as {@link org.eclipse.rse.ui.actions.SystemBaseAction #setHelp(String)}
     * @see org.eclipse.rse.ui.actions.SystemBaseDialogAction #setDialogHelp(String)
     * @see org.eclipse.rse.ui.actions.SystemBaseAction #getHelpContextId()
     */
    public void setHelpContextId(String id)
    {
    	setHelp(id);
    }
    /**
     * Set the title for the dialog that displays
     */
    public void setDialogTitle(String title)
    {
    	this.dlgTitle = title;
    }
    /**
     * Set the help id for the dialog that displays
     */
    public void setDialogHelpContextId(String id)
    {
    	setDialogHelp(id);
    }
	/**
	 * Specify an edit pane that prompts the user for the contents of a filter string.
	 */
	public void setFilterStringEditPane(SystemFilterStringEditPane editPane)
	{
		this.editPane = editPane;
	}
	/**
	 * Return the edit pane specified via {@link #setFilterStringEditPane(SystemFilterStringEditPane)}
	 */
	public SystemFilterStringEditPane getFilterStringEditPane()
	{
		return editPane;
	}

    /**
     * Called by SystemBaseAction when selection is set.
     * Our opportunity to verify we are allowed for this selected type.
     */
	public boolean checkObjectType(Object selectedObject)
	{
		//System.out.println("checkObjectType: " + (selectedObject instanceof SystemFilterReference));
		if (selectedObject instanceof ISystemFilter) 
		{
			return !((ISystemFilter)selectedObject).isNonChangable();
		}
		else if (selectedObject instanceof ISystemFilterReference) 
		{
			return !((ISystemFilterReference)selectedObject).getReferencedFilter().isNonChangable();			
		}
        else
          return false;
	}
	
	/**
	 * This method creates and configures the filter dialog. It defers to
	 *  {@link #getFilterDialog(Shell)} to create it, and then configures it here.
	 *  So, do not override this, but do feel free to override getFilterDialog.
	 */
	public Dialog createDialog(Shell shell)
	{
		dlg = getFilterDialog(shell);		
		dlg.setSystemFilterPoolReferenceManagerProvider(getSystemFilterPoolReferenceManagerProvider());
		dlg.setSystemFilterPoolManagerProvider(getSystemFilterPoolManagerProvider());
		if (editPane != null)
		  dlg.setFilterStringEditPane(editPane);		 
		configureFilterDialog(dlg);
		ISystemFilter filter = getSelectedFilter();
		if (filter != null)
			if (filter.isSingleFilterStringOnly())
				dlg.setSupportsMultipleStrings(false);
		return (Dialog)dlg;
	}
	
	/**
	 * Overridable extension point to get our filter dialog. Only override this if you 
	 *  subclass SystemChangeFilterDialog. Else, override configureFilterDialog.
	 */
	protected SystemChangeFilterDialog getFilterDialog(Shell shell)
	{
		if (dlgTitle == null)
		  return new SystemChangeFilterDialog(shell);
		else
		  return new SystemChangeFilterDialog(shell, dlgTitle);
	}
	
	/**
	 * This method is called internally, but had to be made public. You can ignore it.
	 */
	public void callConfigureFilterDialog(SystemChangeFilterDialog dlg)
	{
		configureFilterDialog(dlg);
	}
	
	/**
	 * Overridable extension point to configure the filter dialog. Typically you don't need
	 *  to subclass our default dialog.
	 * <p>
	 * Note since the dialog has not been opened yet, you cannot assume its shell is ready,
	 * so call getParentShell() versus getShell().
	 */
	protected void configureFilterDialog(SystemChangeFilterDialog dlg)
	{
		Shell shell = dlg.getShell();
		if (shell == null)
			shell = dlg.getParentShell();
		// code goes here...
	}
		
	/**
	 * Required by parent but we do not use it so return null;
	 */
	protected Object getDialogValue(Dialog dlg)
	{
		return null;
	}
	
    /**
     * Get the contextual system filter pool reference manager provider. Will return non-null if the
     * current selection is not a reference to a filter pool or filter, or a reference manager
     * provider.
     */
    public ISystemFilterPoolReferenceManagerProvider getSystemFilterPoolReferenceManagerProvider()
    {
    	Object firstSelection = getFirstSelection();
    	if (firstSelection != null)
    	{
    		if (firstSelection instanceof ISystemFilterReference)
    		  return ((ISystemFilterReference)firstSelection).getProvider();
    		else if (firstSelection instanceof ISystemFilterPoolReference)
    		  return ((ISystemFilterPoolReference)firstSelection).getProvider();
            else if (firstSelection instanceof ISystemFilterPoolReferenceManagerProvider)
              return (ISystemFilterPoolReferenceManagerProvider)firstSelection;
    	}
        return null;
    }
	/**
	 * Get the contextual system filter pool manager provider. Will return non-null if the
	 * current selection is not a reference to a filter pool or filter, or a reference manager
	 * provider, or a manager provider.
	 */
	public ISystemFilterPoolManagerProvider getSystemFilterPoolManagerProvider()
	{
		Object firstSelection = getFirstSelection();
		if (firstSelection != null)
		{
			if (firstSelection instanceof ISystemFilterReference)
			  return ((ISystemFilterReference)firstSelection).getReferencedFilter().getProvider();
			else if (firstSelection instanceof ISystemFilter)
			  return ((ISystemFilter)firstSelection).getProvider();
			else if (firstSelection instanceof ISystemFilterPoolReference)
			  return ((ISystemFilterPoolReference)firstSelection).getReferencedFilterPool().getProvider();
			else if (firstSelection instanceof ISystemFilterPool)
			  return ((ISystemFilterPool)firstSelection).getProvider();
			else if (firstSelection instanceof ISystemFilterPoolManagerProvider)
			  return (ISystemFilterPoolManagerProvider)firstSelection;
		}
		return null;
	}
		
	/**
	 * Get the selected filter
	 */
	public ISystemFilter getSelectedFilter()
	{
		Object firstSelection = getFirstSelection();
		if (firstSelection != null)
		{
			if (firstSelection instanceof ISystemFilterReference)
			  return ((ISystemFilterReference)firstSelection).getReferencedFilter();
			else if (firstSelection instanceof ISystemFilter)
			  return ((ISystemFilter)firstSelection);
		}
		return null;
	}
}