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
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.view.SystemViewPart;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.framelist.BackAction;
import org.eclipse.ui.views.framelist.ForwardAction;
import org.eclipse.ui.views.framelist.FrameList;
import org.eclipse.ui.views.framelist.UpAction;


/**
 * A cascading menu action for "Go To->"
 */
public class SystemCascadingGoToAction extends SystemBaseSubMenuAction 
{
	//private IAdaptable pageInput;
	//private IMenuManager parentMenuManager;
	private boolean actionsMade = false;

	private SystemViewPart fSystemViewPart;
	private BackAction backAction;
	private ForwardAction forwardAction;
	private UpAction upAction;

	
	/**
	 * Constructor 
	 */
	public SystemCascadingGoToAction(Shell shell, SystemViewPart systemViewPart)
	{
		super(SystemResources.ACTION_CASCADING_GOTO_LABEL, SystemResources.ACTION_CASCADING_GOTO_TOOLTIP, shell);
		setMenuID(ISystemContextMenuConstants.MENU_GOTO);
		this.fSystemViewPart = systemViewPart;		
        setCreateMenuEachTime(false);
        setPopulateMenuEachTime(false);
	    allowOnMultipleSelection(false);
	    setContextMenuGroup(ISystemContextMenuConstants.GROUP_GOTO);        
	}

	/**
	 * @see SystemBaseSubMenuAction#getSubMenu()
	 */
	public IMenuManager populateSubMenu(IMenuManager gotoMenu)
	{
		if (!actionsMade)
		  makeActions();
		gotoMenu.add(backAction);
		gotoMenu.add(forwardAction);
		gotoMenu.add(upAction);
		return gotoMenu;
	}

	protected void makeActions() 
	{
		FrameList frameList = fSystemViewPart.getFrameList();
		backAction = new BackAction(frameList);
		forwardAction = new ForwardAction(frameList);
		upAction = new UpAction(frameList);
		
		actionsMade = true;
	}
	
}