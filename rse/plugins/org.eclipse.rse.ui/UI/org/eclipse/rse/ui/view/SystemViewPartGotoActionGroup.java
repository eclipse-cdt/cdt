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

package org.eclipse.rse.ui.view;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.rse.ui.GenericMessages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.views.framelist.BackAction;
import org.eclipse.ui.views.framelist.ForwardAction;
import org.eclipse.ui.views.framelist.FrameList;
import org.eclipse.ui.views.framelist.UpAction;



/**
 * Enables typical frameset actions for Remote Systems Explorer view part
 */
public class SystemViewPartGotoActionGroup extends ActionGroup 
{

	protected SystemViewPart fSystemViewPart;
	protected BackAction backAction;
	protected ForwardAction forwardAction;
	//private GoIntoAction goIntoAction;
	protected UpAction upAction;

	
	/**
	 * Constructor
	 */
	public SystemViewPartGotoActionGroup(SystemViewPart viewPart) 
	{
		fSystemViewPart = viewPart;
	    makeActions();
	}

	/**
	 * Returns the RSE view part associated with this action group
	 */
	public SystemViewPart getSystemViewPart() 
	{
		return fSystemViewPart;
	}
	
	protected void makeActions() 
	{
		FrameList frameList = fSystemViewPart.getFrameList();
		//goIntoAction = new GoIntoAction(frameList);
		backAction = new BackAction(frameList);
		forwardAction = new ForwardAction(frameList);
		upAction = new UpAction(frameList);
	}
	
	public void fillContextMenu(IMenuManager menu) 
	{
		// we actually don't call this! See instead SystemCascadingGoToAction
	    //menu.add(goIntoAction); // done in SystemView
		MenuManager gotoMenu =
			new MenuManager(GenericMessages.ResourceNavigator_goto);
		menu.add(gotoMenu);
		gotoMenu.add(backAction);
		gotoMenu.add(forwardAction);
		gotoMenu.add(upAction);
	}
	
	public void fillActionBars(IActionBars actionBars) 
	{
		//actionBars.setGlobalActionHandler(
		//	ActionFactory.GO_INTO,
		//	goIntoAction);
		actionBars.setGlobalActionHandler(
			ActionFactory.BACK.getId(),
			backAction);
		actionBars.setGlobalActionHandler(
			ActionFactory.FORWARD.getId(),
			forwardAction);
		actionBars.setGlobalActionHandler(ActionFactory.UP.getId(),upAction);
			
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		toolBar.add(upAction);
	}
	
	public void updateActionBars() 
	{
		getContext().getSelection();
	}
}