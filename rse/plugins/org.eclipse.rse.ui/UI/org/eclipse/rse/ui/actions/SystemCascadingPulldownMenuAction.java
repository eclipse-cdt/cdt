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

package org.eclipse.rse.ui.actions;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.SystemViewPart;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;


/**
 * A cascading menu action for a toolbar, emulating the pulldown menu view parts have.
 */
public class SystemCascadingPulldownMenuAction 
       extends SystemBaseSubMenuAction 
       implements  IMenuListener, IMenuCreator
{
	
	private SystemSubMenuManager dropDownMenuMgr;
	private ISelectionProvider sp = null;
		
	/**
	 * Constructor 
	 */
	public SystemCascadingPulldownMenuAction(Shell shell, ISelectionProvider selectionProviderForToolbarActions)
	{
		super(SystemResources.ACTION_CASCADING_PULLDOWN_LABEL, SystemResources.ACTION_CASCADING_PULLDOWN_TOOLTIP, 
		      RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_PROFILE_ID),shell);
 	    sp = selectionProviderForToolbarActions;
        setCreateMenuEachTime(false);
        setPopulateMenuEachTime(false);
 	    setMenuCreator(this); // this IAction method tells JFace this is a dropdown menu
	}

	/**
	 * @see SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager menu)
	{
		menu.addMenuListener(this);
		menu.setRemoveAllWhenShown(false);
		boolean showConnectionActions = true;
        SystemViewPart.populateSystemViewPulldownMenu(menu, getShell(), showConnectionActions, null, sp);		
        return menu;
	}

	/**
	 * Called when submenu is about to show
	 */
	public void menuAboutToShow(IMenuManager ourSubMenu)
	{
	}

    //------------------------
    // IMenuCreator methods...
    //------------------------
    /**
     * dispose method comment.
     */
    public void dispose() 
    {
    	if (dropDownMenuMgr != null) 
    	{
    		dropDownMenuMgr.dispose();
    		dropDownMenuMgr = null;
    	}
    }
    /**
     * getMenu method comment.
     */
    public Menu getMenu(Control parent) 
    {
    	if (dropDownMenuMgr == null) 
    	{
    		//dropDownMenuMgr = new MenuManager();
    		dropDownMenuMgr = (SystemSubMenuManager)getSubMenu();
    	}
    	return dropDownMenuMgr.createContextMenu(parent);
    }
    public Menu getMenu(Menu parent) 
    {
        //System.out.println("In SystemCascadingPulldownMenuAction#getMenu(Menu)");	
    	return null;
    }    
}