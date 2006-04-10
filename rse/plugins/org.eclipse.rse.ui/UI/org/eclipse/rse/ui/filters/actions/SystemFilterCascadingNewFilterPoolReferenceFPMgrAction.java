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
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.actions.SystemBaseSubMenuAction;
import org.eclipse.rse.ui.view.SystemViewMenuListener;
import org.eclipse.swt.widgets.Shell;


/**
 * A cascading submenu action for "New Filter Pool Reference->".
 * This is after the first cascade, where we list filter pool managers.
 */
public class SystemFilterCascadingNewFilterPoolReferenceFPMgrAction 
       extends SystemBaseSubMenuAction 
       implements  IMenuListener
{
	private ISystemFilterPoolManager mgr;
	private ISystemFilterPoolReferenceManager refMgr;

	/**
	 * Constructor.
	 */
	public SystemFilterCascadingNewFilterPoolReferenceFPMgrAction(Shell shell, 
	                                                                    ISystemFilterPoolManager mgr,
	                                                                    ISystemFilterPoolReferenceManager refMgr)
	{
		super(mgr.getName(),shell);
        setCreateMenuEachTime(false);
        setPopulateMenuEachTime(false);
        this.mgr = mgr;
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
	 * Called when submenu is about to show
	 */
	public void menuAboutToShow(IMenuManager ourSubMenu)
	{
		//System.out.println("inside menu about to show");
		ISystemFilterPool[] pools = mgr.getSystemFilterPools();
		SystemFilterPoolReferenceSelectAction action = null;
		ISystemFilterPool pool = null;
		Shell shell = getShell();
		String helpId = getHelpContextId();
		for (int idx=0; idx<pools.length; idx++)
		{
			pool = pools[idx];
			if (!isPoolAlreadyReferenced(pool) && !isPoolConnectionUnique(pool))
			{
		      action = new SystemFilterPoolReferenceSelectAction(shell,pool,refMgr);
		      if (helpId != null)
		        action.setHelp(helpId);
		      ourSubMenu.add(action);		
			}
		}
	}
	
	/**
	 * Determine if the given filter pool is already referenced by this reference manager
	 */
	private boolean isPoolAlreadyReferenced(ISystemFilterPool pool)
	{
		return refMgr.isSystemFilterPoolReferenced(pool);
	}
	/**
	 * Determine if the given filter pool is the special unique pool for a connection.
	 * If so, we don't want to let anyone else reference it.
	 */
	private boolean isPoolConnectionUnique(ISystemFilterPool pool)
	{
		return (pool.getOwningParentName() != null);
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