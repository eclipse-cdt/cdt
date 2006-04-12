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
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseSubMenuAction;
import org.eclipse.rse.ui.view.SystemViewMenuListener;
import org.eclipse.swt.widgets.Shell;


/**
 * A cascading menu action for "New Filter Pool Reference->"
 */
public class SystemFilterCascadingNewFilterPoolReferenceAction 
       extends SystemBaseSubMenuAction 
       implements  IMenuListener
{
	private ISystemFilterPoolReferenceManager refMgr;

	/**
	 * Constructor when reference mgr not available. Must call setSystemFilterPoolReferenceManager.
	 */
	public SystemFilterCascadingNewFilterPoolReferenceAction(Shell shell)
	{
        this(shell, null);
	}

	/**
	 * Constructor when reference mgr is available. No need to call setSystemFilterPoolReferenceManager.
	 */
	public SystemFilterCascadingNewFilterPoolReferenceAction(Shell shell, ISystemFilterPoolReferenceManager refMgr)
	{
		super(SystemResources.ACTION_CASCADING_FILTERPOOL_NEWREFERENCE_LABEL, SystemResources.ACTION_CASCADING_FILTERPOOL_NEWREFERENCE_TOOLTIP, 
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_NEWFILTERPOOLREF_ID),shell);
        setCreateMenuEachTime(false);
        setPopulateMenuEachTime(false);
		setContextMenuGroup(ISystemContextMenuConstants.GROUP_NEW);        		        
        this.refMgr = refMgr;
	}

    /**
     * Set the help context Id (infoPop) for this action. This must be fully qualified by
     *  plugin ID.
     * <p>
     * Same as {@link org.eclipse.rse.ui.actions.SystemBaseAction #setHelp(String)}
     * @see org.eclipse.rse.ui.actions.SystemBaseAction #getHelpContextId()
     */
    public void setHelpContextId(String id)
    {
    	setHelp(id);
    }

	/**
	 * @see SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager menu)
	{
		menu.addMenuListener(this);
		menu.setRemoveAllWhenShown(true);
		//menu.setEnabled(true);
		menu.add(new SystemBaseAction("dummy",null));
		return menu;
	}
	
	/**
	 * Set the master filter pool reference manager from which the filter pools are to be selectable,
	 * and into which we will add the filter pool reference
	 */
	public void setSystemFilterPoolReferenceManager(ISystemFilterPoolReferenceManager refMgr)
	{
		this.refMgr = refMgr;
	}
	
	/**
	 * Called when submenu is about to show
	 */
	public void menuAboutToShow(IMenuManager ourSubMenu)
	{
		Shell shell = getShell();
		ISystemFilterPoolManager[] mgrs = refMgr.getSystemFilterPoolManagers();
		SystemFilterCascadingNewFilterPoolReferenceFPMgrAction action = null;
		ISystemFilterPoolManager mgr = null;
	    ISystemFilterPoolManager defaultMgr = refMgr.getDefaultSystemFilterPoolManager();
	    String helpId = getHelpContextId();
	    if (defaultMgr != null)
	    {
           action = new SystemFilterCascadingNewFilterPoolReferenceFPMgrAction(shell, defaultMgr, refMgr);
           if (helpId != null)
             action.setHelp(helpId);
    	   ourSubMenu.add(action.getSubMenu());		
	    }
		for (int idx=0; idx<mgrs.length; idx++)
		{
		   mgr = mgrs[idx];
		   if (mgr != defaultMgr)
		   {
		     action = new SystemFilterCascadingNewFilterPoolReferenceFPMgrAction(shell, mgr, refMgr);
             if (helpId != null)
               action.setHelp(helpId);
		     ourSubMenu.add(action.getSubMenu());		
		   }
		}
	}
	
    /**
     * Overridable method from parent that instantiates the menu listener who job is to add mnemonics.
     * @param setMnemonicsOnlyOnce true if the menu is static and so mnemonics need only be set once. False if it is dynamic
     */
    protected SystemViewMenuListener createMnemonicsListener(boolean setMnemonicsOnlyOnce)
    {
    	return new SystemViewMenuListener(false); // our menu is re-built dynamically each time
    }
	
}