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

package org.eclipse.rse.ui;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemBaseSubMenuAction;
import org.eclipse.rse.ui.actions.SystemCascadingBrowseWithAction;
import org.eclipse.rse.ui.actions.SystemCascadingCompareWithAction;
import org.eclipse.rse.ui.actions.SystemCascadingExpandToAction;
import org.eclipse.rse.ui.actions.SystemCascadingNewAction;
import org.eclipse.rse.ui.actions.SystemCascadingOpenWithAction;
import org.eclipse.rse.ui.actions.SystemCascadingRemoteServersAction;
import org.eclipse.rse.ui.actions.SystemCascadingReplaceWithAction;
import org.eclipse.rse.ui.actions.SystemCascadingViewAction;
import org.eclipse.rse.ui.actions.SystemCascadingWorkWithAction;
import org.eclipse.rse.ui.actions.SystemSeparatorAction;

/**
 * Override/wrapper of JFace MenuManager so we can redirect any
 * menu additions to specific menu groups to go into 
 * pre-defined submenus.
 * <p>
 * Only exposes core set of MenuManager methods. Rest can be
 * accessed by calling getMenuManger().
 */
public class SystemMenuManager //implements IMenuManager
       implements ISystemContextMenuConstants
{
	private IMenuManager mgr;
	private IMenuManager newSubMenu, expandtoSubMenu, openwithSubMenu, browsewithSubMenu, comparewithSubMenu, replacewithSubMenu, workwithSubMenu, viewSubMenu, serverSubMenu;
	private boolean      menuCreated = false;
	
	/**
	 * Constructor for SystemMenuManager
	 * @param menuManager existing JFace menu manager
	 */
	public SystemMenuManager(IMenuManager menuManager)
	{
		super();
		mgr = menuManager;
	}
	
	/**
	 * Get the wrapped IMenuManager manager
	 */
	public IMenuManager getMenuManager()
	{
		return mgr;
	}
	
    /**
     * Method declared on IContributionManager.
     * Add an action to this menu.
     * COMMENTED OUT TO PREVENT CALLING IT. YOU SHOULD EXPLICITLY
     * DECIDE WHAT GROUP EACH ACTION SHOULD BE ADDED TO, SO WE FORCE
     * YOU TO CALL APPENDTOGROUP!
     *
    public void add(IAction action) 
    {
	    mgr.add(action);
    }*/	
    
    /**
     * Method declared on IContributionManager.
     * Append an action to the menu.
     * <p>
     * Intercepted so we can direct appends to certain groups into appropriate cascading submenus.
     * <p>
     * @param groupName group to append to. See {@link org.eclipse.rse.ui.ISystemContextMenuConstants}.
     * @param action action to append.
     */
    public void appendToGroup(String groupName, IAction action) 
    {
    	if (!checkForSpecialGroup(groupName, action, true))
          if (groupName != null)    	
	        mgr.appendToGroup(groupName, action);
	      else 
	        mgr.add(action);
    }
    /**
     * Method declared on IContributionManager.
     * Append a submenu to the menu.
     * <p>
     * Intercepted so we can direct appends to certain groups into appropriate cascading submenus.
     * <p>
     * @param groupName group to append to. See {@link org.eclipse.rse.ui.ISystemContextMenuConstants}.
     * @param submenu submenu to append.
     */
    public void appendToGroup(String groupName, IContributionItem menuOrSeparator) 
    {
    	if (!checkForSpecialGroup(groupName, menuOrSeparator, true))
    	  if (groupName != null)
	        mgr.appendToGroup(groupName, menuOrSeparator);
	      else
	        mgr.add(menuOrSeparator);
    }
    
    /**  
     * Method declared on IContributionManager.
     * Prepend an action to the menu.
     * <p>
     * Intercepted so we can direct appends to certain groups into appropriate cascading submenus.
     * <p>
     * @param groupName group to append to. See {@link org.eclipse.rse.ui.ISystemContextMenuConstants}.
     * @param action action to prepend.
     */
    public void prependToGroup(String groupName, IAction action) 
    {
    	if (!checkForSpecialGroup(groupName, action, false))
	      mgr.prependToGroup(groupName, action);
    }
    /**
     * Method declared on IContributionManager.
     * Prepend a submenu to the menu.
     * <p>
     * Intercepted so we can direct appends to certain groups into appropriate cascading submenus.
     * <p>
     * @param groupName group to append to. See {@link org.eclipse.rse.ui.ISystemContextMenuConstants}.
     * @param submenu submenu to append.
     */
    public void prependToGroup(String groupName, IContributionItem subMenu) 
    {
    	if (!checkForSpecialGroup(groupName, subMenu, true))
	      mgr.prependToGroup(groupName, subMenu);
    }

	/**
	 * Add a separator.
	 * HOPEFULLY THIS IS NEVER CALLED. RATHER, BY USING GROUPS AND DECIDING PER GROUP IF THERE
	 * SHOULD BE SEPARATORS, WE AVOID HARDCODING SEPARATORS LIKE THIS.
	 */
	public void addSeparator()
	{
		mgr.add(new Separator());
	}
   
    /**
     * Special helper that intelligently adds system framework actions
     * @param menuGroup default menuGroup to add to, if action doesn't contain an explicit location
     * @param action action to add to the menu
     */
    public void add(String menuGroup, IAction action)
    {
		if (action instanceof SystemBaseSubMenuAction)
		  appendToGroup(getMenuGroup(action,menuGroup),
		                ((SystemBaseSubMenuAction)action).getSubMenu());		   
		else if (!(action instanceof SystemSeparatorAction))
		  appendToGroup(getMenuGroup(action,menuGroup),action);
		else // hopefully we don't have these!
		  appendToGroup(menuGroup, new Separator()); // add a separator, which is an IContributionItem
    }    
    
    private String getMenuGroup(IAction action, String defaultGroup)
    {
	   if ( (action instanceof ISystemAction) &&
		  (((ISystemAction)action).getContextMenuGroup()!=null) )   
		 return ((ISystemAction)action).getContextMenuGroup();
	   else
	     return defaultGroup;		      
    }

    private boolean checkForSpecialGroup(String groupName, IAction action, boolean add)
    {
    	boolean takenCareOf = false;
    	IMenuManager subMenu = getSpecialSubMenu(groupName);
    	if (subMenu != null)
    	{
    	  takenCareOf = true;
    	  if (action instanceof SystemSeparatorAction)
    	  {
    	    subMenu.add(new Separator());
    	    if (((SystemSeparatorAction)action).isRealAction())
    	      subMenu.add(action);    	    
    	  }
    	  else
    	    subMenu.add(action);	
    	}
    	
    	return takenCareOf;
    }        
    
    private boolean checkForSpecialGroup(String groupName, IContributionItem contribution, boolean add)
    {
    	boolean takenCareOf = false;
    	IMenuManager subMenu = getSpecialSubMenu(groupName);
    	if (subMenu != null)
    	{
    	  takenCareOf = true;
    	  subMenu.add(contribution);	
    	}    	
    	return takenCareOf;
    }        

    private IMenuManager getSpecialSubMenu(String groupName)
    {
    	IMenuManager subMenu = null;
    	menuCreated = false;
    	if (groupName!=null) 
    	{
    	  if (groupName.equals(GROUP_NEW))
    	  {
    	    if (newSubMenu == null)
    	    {
    	      newSubMenu = (new SystemCascadingNewAction()).getSubMenu();
              mgr.appendToGroup(GROUP_NEW, newSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = newSubMenu;
    	  }
    	  /*
    	  else if (groupName.equals(GROUP_GOTO))
    	  {
    	    if (gotoSubMenu == null)
    	    {
    	      gotoSubMenu = (new SystemCascadingGoToAction()).getSubMenu();
              mgr.appendToGroup(GROUP_GOTO, gotoSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = gotoSubMenu;
    	  }
    	  */
    	  else if (groupName.equals(GROUP_EXPANDTO))
    	  {
    	    if (expandtoSubMenu == null)
    	    {
    	      expandtoSubMenu = (new SystemCascadingExpandToAction()).getSubMenu();
              mgr.appendToGroup(GROUP_EXPANDTO, expandtoSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = expandtoSubMenu;
    	  }
    	  else if (groupName.equals(GROUP_OPENWITH))
    	  {
    	    if (openwithSubMenu == null)
    	    {
    	      openwithSubMenu = (new SystemCascadingOpenWithAction()).getSubMenu();
              mgr.appendToGroup(GROUP_OPENWITH, openwithSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = openwithSubMenu;
    	  }
    	  else if (groupName.equals(GROUP_BROWSEWITH))
    	  {
    	    if (browsewithSubMenu == null)
    	    {
    	      browsewithSubMenu = (new SystemCascadingBrowseWithAction()).getSubMenu();
              mgr.appendToGroup(GROUP_BROWSEWITH, browsewithSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = browsewithSubMenu;
    	  }
    	  else if (groupName.equals(GROUP_COMPAREWITH))
    	  {
    	  	if (comparewithSubMenu == null)
    	  	{
    	  		comparewithSubMenu = (new SystemCascadingCompareWithAction()).getSubMenu();
    	  		mgr.appendToGroup(GROUP_COMPAREWITH, comparewithSubMenu);
    	  		menuCreated = true;
    	  	}
    	  	subMenu = comparewithSubMenu;
    	  }
		  else if (groupName.equals(GROUP_REPLACEWITH))
		  {
			if (replacewithSubMenu == null)
			{
				replacewithSubMenu = (new SystemCascadingReplaceWithAction()).getSubMenu();
				mgr.appendToGroup(GROUP_REPLACEWITH, replacewithSubMenu);
				menuCreated = true;
			}
			subMenu = replacewithSubMenu;
		  }
    	  else if (groupName.equals(GROUP_WORKWITH))
    	  {
    	    if (workwithSubMenu == null)
    	    {
    	      workwithSubMenu = (new SystemCascadingWorkWithAction()).getSubMenu();
              mgr.appendToGroup(GROUP_WORKWITH, workwithSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = workwithSubMenu;
    	  }
    	  else if (groupName.equals(GROUP_VIEWER_SETUP))
    	  {
    	    if (viewSubMenu == null)
    	    {
    	      viewSubMenu = (new SystemCascadingViewAction()).getSubMenu();
              mgr.appendToGroup(GROUP_VIEWER_SETUP, viewSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = viewSubMenu;
    	  }
		  else if (groupName.equals(GROUP_STARTSERVER))
		  {
			if (serverSubMenu == null)
			{
				serverSubMenu = (new SystemCascadingRemoteServersAction()).getSubMenu();
			  	mgr.appendToGroup(GROUP_STARTSERVER, serverSubMenu);	    
			  	menuCreated = true;
			}
			subMenu = serverSubMenu;
		  }
    	}
    	return subMenu;
    }        

    public IMenuManager getSpecialSubMenuByMenuID(String menuID)
    {
    	IMenuManager subMenu = null;
    	String groupName = null;
    	menuCreated = false;
    	if (menuID!=null) 
    	{
    	  	if (menuID.equals(MENU_NEW))
    	    	groupName = GROUP_NEW;
    	  	else if (menuID.equals(MENU_GOTO))
    	    	groupName = GROUP_GOTO;
    	  	else if (menuID.equals(MENU_EXPANDTO))
    	    	groupName = GROUP_EXPANDTO;
    	  	else if (menuID.equals(MENU_OPENWITH))
    	    	groupName = GROUP_OPENWITH;
    	  	else if (menuID.equals(MENU_BROWSEWITH))
    	  		groupName = GROUP_BROWSEWITH;
    	  	else if (menuID.equals(MENU_COMPAREWITH))
    	  		groupName = GROUP_COMPAREWITH;
		  	else if (menuID.equals(MENU_REPLACEWITH))
			  	groupName = GROUP_REPLACEWITH;	
    	  	else if (menuID.equals(MENU_WORKWITH))
    	    	groupName = GROUP_WORKWITH; 	    
		  	else if (menuID.equals(MENU_STARTSERVER))
			  	groupName = GROUP_STARTSERVER; 	    
			      	    
    	  	if (groupName != null)
    	    	subMenu = getSpecialSubMenu(groupName);
    	}
    	return subMenu;
    }        
   
    public boolean wasMenuCreated()
    {
    	return menuCreated;
    }
}