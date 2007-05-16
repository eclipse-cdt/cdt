/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Tobias Schwarz (Wind River) - [187312] Fix duplicate submenus contributed through plugin.xml
 ********************************************************************************/

package org.eclipse.rse.ui;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.rse.internal.ui.actions.SystemCascadingBrowseWithAction;
import org.eclipse.rse.internal.ui.actions.SystemCascadingCompareWithAction;
import org.eclipse.rse.internal.ui.actions.SystemCascadingExpandToAction;
import org.eclipse.rse.internal.ui.actions.SystemCascadingNewAction;
import org.eclipse.rse.internal.ui.actions.SystemCascadingOpenWithAction;
import org.eclipse.rse.internal.ui.actions.SystemCascadingRemoteServersAction;
import org.eclipse.rse.internal.ui.actions.SystemCascadingReplaceWithAction;
import org.eclipse.rse.internal.ui.actions.SystemCascadingViewAction;
import org.eclipse.rse.internal.ui.actions.SystemCascadingWorkWithAction;
import org.eclipse.rse.internal.ui.actions.SystemSeparatorAction;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemBaseSubMenuAction;

/**
 * Override/wrapper of JFace MenuManager so we can redirect any
 * menu additions to specific menu groups to go into 
 * predefined submenus.
 * <p>
 * Only exposes core set of MenuManager methods. Rest can be
 * accessed by calling getMenuManger().
 */
public class SystemMenuManager 
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
     * DECIDE WHAT ISystemContextMenuConstants.GROUP EACH ACTION SHOULD BE ADDED TO, SO WE FORCE
     * YOU TO CALL APPENDTOISystemContextMenuConstants.GROUP!
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
     * @param menuOrSeparator menu or separator to append.
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
     * @param subMenu submenu to append.
     */
    public void prependToGroup(String groupName, IContributionItem subMenu) 
    {
    	if (!checkForSpecialGroup(groupName, subMenu, true))
	      mgr.prependToGroup(groupName, subMenu);
    }

	/**
	 * Add a separator.
	 * HOPEFULLY THIS IS NEVER CALLED. RATHER, BY USING ISystemContextMenuConstants.GROUPS AND DECIDING PER ISystemContextMenuConstants.GROUP IF THERE
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

    /**
     * Return the given subMenu if already instantiated locally, or
     * find it with the given subMenuId in the nested IMenuManager
     * if not yet instantiated locally.
     *
     * This is necessary in order to make sure the given submenu
     * is not created twice when it (or an item of it) has been 
     * contributed by a client through plugin.xml already.
     *
     * @param subMenu existing local submenu instance, or
     *     <code>null</code> if not yet instantiated locally.
     * @param subMenuId submenu ID by which to find it in IMenuManager.
     * @return existing instantiated, or newly found submenu instance,
     *      or <code>null</code> if the subMenu does not exist yet.
     */
    private IMenuManager findSpecialSubMenu(IMenuManager subMenu, String subMenuId) {
    	if (subMenu == null) {
	    	IContributionItem item = mgr.find(subMenuId);
	    	if (item instanceof IMenuManager) {
	    		subMenu = (IMenuManager)item;
	    	}
    	}
    	return subMenu;
    }
    
    private IMenuManager getSpecialSubMenu(String groupName)
    {
    	IMenuManager subMenu = null;
    	menuCreated = false;
    	if (groupName!=null) 
    	{
    	  if (groupName.equals(ISystemContextMenuConstants.GROUP_NEW))
    	  {
    		// first of all try to find the subMenu.
    		// the submenu can already exist, when any adapter created it to allow the 
    		// contribution of actions via plugin.xml 
    		// RSE creates the submenus only, when they are needed within the code, 
    		// so it is possible that submenus doesn'texist for plugin.xml contributions 
    		// and so an error log entry is generated.
    		newSubMenu = findSpecialSubMenu(newSubMenu, ISystemContextMenuConstants.MENU_NEW); 
    	    if (newSubMenu == null)
    	    {
    	    	newSubMenu = (new SystemCascadingNewAction()).getSubMenu();
    	    	mgr.appendToGroup(ISystemContextMenuConstants.GROUP_NEW, newSubMenu);	    
    	    	menuCreated = true;
    	    }
    	    subMenu = newSubMenu;
    	  }
    	  /*
    	  else if (groupName.equals(ISystemContextMenuConstants.GROUP_GOTO))
    	  {
    	    if (gotoSubMenu == null)
    	    {
    	      gotoSubMenu = (new SystemCascadingGoToAction()).getSubMenu();
              mgr.appendToGroup(ISystemContextMenuConstants.GROUP_GOTO, gotoSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = gotoSubMenu;
    	  }
    	  */
    	  else if (groupName.equals(ISystemContextMenuConstants.GROUP_EXPANDTO))
    	  {
    		  expandtoSubMenu = findSpecialSubMenu(expandtoSubMenu, ISystemContextMenuConstants.MENU_EXPANDTO); 
    	    if (expandtoSubMenu == null)
    	    {
    	      expandtoSubMenu = (new SystemCascadingExpandToAction()).getSubMenu();
              mgr.appendToGroup(ISystemContextMenuConstants.GROUP_EXPANDTO, expandtoSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = expandtoSubMenu;
    	  }
    	  else if (groupName.equals(ISystemContextMenuConstants.GROUP_OPENWITH))
    	  {
    		  openwithSubMenu = findSpecialSubMenu(openwithSubMenu, ISystemContextMenuConstants.MENU_OPENWITH); 
    	    if (openwithSubMenu == null)
    	    {
    	      openwithSubMenu = (new SystemCascadingOpenWithAction()).getSubMenu();
              mgr.appendToGroup(ISystemContextMenuConstants.GROUP_OPENWITH, openwithSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = openwithSubMenu;
    	  }
    	  else if (groupName.equals(ISystemContextMenuConstants.GROUP_BROWSEWITH))
    	  {
    		  browsewithSubMenu = findSpecialSubMenu(browsewithSubMenu, ISystemContextMenuConstants.MENU_BROWSEWITH); 
    	    if (browsewithSubMenu == null)
    	    {
    	      browsewithSubMenu = (new SystemCascadingBrowseWithAction()).getSubMenu();
              mgr.appendToGroup(ISystemContextMenuConstants.GROUP_BROWSEWITH, browsewithSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = browsewithSubMenu;
    	  }
    	  else if (groupName.equals(ISystemContextMenuConstants.GROUP_COMPAREWITH))
    	  {
    		  comparewithSubMenu = findSpecialSubMenu(comparewithSubMenu, ISystemContextMenuConstants.MENU_COMPAREWITH); 
    	  	if (comparewithSubMenu == null)
    	  	{
    	  		comparewithSubMenu = (new SystemCascadingCompareWithAction()).getSubMenu();
    	  		mgr.appendToGroup(ISystemContextMenuConstants.GROUP_COMPAREWITH, comparewithSubMenu);
    	  		menuCreated = true;
    	  	}
    	  	subMenu = comparewithSubMenu;
    	  }
		  else if (groupName.equals(ISystemContextMenuConstants.GROUP_REPLACEWITH))
		  {
			  replacewithSubMenu = findSpecialSubMenu(replacewithSubMenu, ISystemContextMenuConstants.MENU_REPLACEWITH); 
			if (replacewithSubMenu == null)
			{
				replacewithSubMenu = (new SystemCascadingReplaceWithAction()).getSubMenu();
				mgr.appendToGroup(ISystemContextMenuConstants.GROUP_REPLACEWITH, replacewithSubMenu);
				menuCreated = true;
			}
			subMenu = replacewithSubMenu;
		  }
    	  else if (groupName.equals(ISystemContextMenuConstants.GROUP_WORKWITH))
    	  {
    		  workwithSubMenu = findSpecialSubMenu(workwithSubMenu, ISystemContextMenuConstants.MENU_WORKWITH); 
    	    if (workwithSubMenu == null)
    	    {
    	      workwithSubMenu = (new SystemCascadingWorkWithAction()).getSubMenu();
              mgr.appendToGroup(ISystemContextMenuConstants.GROUP_WORKWITH, workwithSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = workwithSubMenu;
    	  }
    	  else if (groupName.equals(ISystemContextMenuConstants.GROUP_VIEWER_SETUP))
    	  {
    	    if (viewSubMenu == null)
    	    {
    	      viewSubMenu = (new SystemCascadingViewAction()).getSubMenu();
              mgr.appendToGroup(ISystemContextMenuConstants.GROUP_VIEWER_SETUP, viewSubMenu);	    
              menuCreated = true;
    	    }
    	    subMenu = viewSubMenu;
    	  }
		  else if (groupName.equals(ISystemContextMenuConstants.GROUP_STARTSERVER))
		  {
			serverSubMenu = findSpecialSubMenu(serverSubMenu, ISystemContextMenuConstants.MENU_STARTSERVER); 
			if (serverSubMenu == null)
			{
				serverSubMenu = (new SystemCascadingRemoteServersAction()).getSubMenu();
			  	mgr.appendToGroup(ISystemContextMenuConstants.GROUP_STARTSERVER, serverSubMenu);	    
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
    	  	if (menuID.equals(ISystemContextMenuConstants.MENU_NEW))
    	    	groupName = ISystemContextMenuConstants.GROUP_NEW;
    	  	else if (menuID.equals(ISystemContextMenuConstants.MENU_GOTO))
    	    	groupName = ISystemContextMenuConstants.GROUP_GOTO;
    	  	else if (menuID.equals(ISystemContextMenuConstants.MENU_EXPANDTO))
    	    	groupName = ISystemContextMenuConstants.GROUP_EXPANDTO;
    	  	else if (menuID.equals(ISystemContextMenuConstants.MENU_OPENWITH))
    	    	groupName = ISystemContextMenuConstants.GROUP_OPENWITH;
    	  	else if (menuID.equals(ISystemContextMenuConstants.MENU_BROWSEWITH))
    	  		groupName = ISystemContextMenuConstants.GROUP_BROWSEWITH;
    	  	else if (menuID.equals(ISystemContextMenuConstants.MENU_COMPAREWITH))
    	  		groupName = ISystemContextMenuConstants.GROUP_COMPAREWITH;
		  	else if (menuID.equals(ISystemContextMenuConstants.MENU_REPLACEWITH))
			  	groupName = ISystemContextMenuConstants.GROUP_REPLACEWITH;	
    	  	else if (menuID.equals(ISystemContextMenuConstants.MENU_WORKWITH))
    	    	groupName = ISystemContextMenuConstants.GROUP_WORKWITH; 	    
		  	else if (menuID.equals(ISystemContextMenuConstants.MENU_STARTSERVER))
			  	groupName = ISystemContextMenuConstants.GROUP_STARTSERVER; 	    
			      	    
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