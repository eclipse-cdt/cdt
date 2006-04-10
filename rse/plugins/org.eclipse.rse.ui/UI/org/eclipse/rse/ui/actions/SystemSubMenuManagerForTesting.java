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
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;


/**
 * For cascading menus, we need our own menu subclass so we can intercept
 *  the state-setting methods our frameworks, and foreword those onto the
 *  sub-menu actions.
 * <p>
 * The state-setting methods including setShell, setSelection and setValue.
 * <p>
 * We often have trouble tracking down when the shell, selection and viewer is
 *  not properly set for cascading actions. For these cases, we can use this
 *  override of the SystemSubMenuManager to trace what happens.
 */
public class SystemSubMenuManagerForTesting  
       extends SystemSubMenuManager
       //implements ISelectionChangedListener
       //implements ISystemAction
{
	private String prefix = "";
    
	/**
	 * Constructor
	 */
	public SystemSubMenuManagerForTesting(SystemBaseSubMenuAction parentAction)
	{
		super(parentAction);
	}
	/**
	 * Constructor
	 */
	public SystemSubMenuManagerForTesting(SystemBaseSubMenuAction parentAction, String text)
	{
		super(parentAction, text);
		System.out.println("SUBMENUMGR CTOR " + text);
	}
	/**
	 * Constructor 
	 */
	public SystemSubMenuManagerForTesting(SystemBaseSubMenuAction parentAction, String text, String id)
	{
		super(parentAction, text, id);
		System.out.println("SUBMENUMGR CTOR " + text);
	}
	/**
	 * Constructor
	 */
	public SystemSubMenuManagerForTesting(SystemBaseSubMenuAction parentAction, String text, String id, ImageDescriptor image)
	{
		super(parentAction, text, id, image);
	}

	
    /**
     * Override of parent so we can trace it....
     */
    public void setInputs(Shell shell, Viewer v, ISelection selection)
    {
		System.out.println(" INSIDE SETINPUTS FOR SUBMENUMGR '"+label+"': selection = "+selection);
		super.setInputs(shell, v, selection);
    }
       


	
    // add(): solve problem that cascaded menu items were not receiving their
    // setSelection() call, due to them only being constructed on the 
    // cascade's MenuAboutToShow(), after the setSelections have run.
    
    // THE QUESTION IS, IF WE DO THIS HERE WHEN ITEMS ARE ADDED TO THIS SUBMENU,
    // IS IT REDUNDANT TO ALSO DO IT WHEN SETINPUTS IS CALLED?

    /**
     * Intercept so we can cascade the selection, viewer and shell down
     */
    public void appendToGroup(String groupName, IAction action)     
	{
	 	  System.out.println("INSIDE APPENDTOGROUP OF ISYSTEMACTION FOR SUBMENUMGR FOR '"+label+"'");
		  prefix = "  ";
    	  super.appendToGroup(groupName, action);
		  prefix = "";
	}
    /**
     * Intercept so we can cascade the selection, viewer and shell down
     */
	public void appendToGroup(String groupName, IContributionItem item) 
	{
		  System.out.println("INSIDE APPENDTOGROUP OF SYSTEMSUBMENUMGR FOR SUBMENUMGR FOR '"+label+"'");
		  prefix = "  ";
		  super.appendToGroup(groupName, item);
	      prefix = "";	      
	}
    /**
     * Intercept so we can cascade the selection, viewer and shell down
     * THIS WAS ONLY CATCHING ACTIONS, NOT NESTED SUBMENUS. THE SUPER OF THIS
     * METHOD CALLS ADD(new ActionContributionItem(action)) SO WE NOW INTERCEPT
     * THERE INSTEAD, AS THAT IS WHAT IS CALLED FOR MULTI-CASCADING MENUS
    public void add(IAction action) 
	{
    	super.add(action);
    	if (action instanceof ISystemAction)
          cascadeAllInputs((ISystemAction)action);    
	}*/
    /**
     * Intercept so we can cascade the selection, viewer and shell down
     */
	public void add(IContributionItem item) 
	{
		prefix = "  ";
		if (item instanceof ActionContributionItem) 
		{
			IAction action = ((ActionContributionItem)item).getAction();
			if (action instanceof ISystemAction)
               System.out.println("INSIDE ADD OF ISYSTEMACTION(action="+action.getText()+") FOR THIS MNUMGR: "+label);
		}
		else if (item instanceof SystemSubMenuManager)
		{
			SystemSubMenuManager submenu = (SystemSubMenuManager)item;
            System.out.println("INSIDE ADD OF SUBMENUMGR(submenu="+submenu.getLabel()+") FOR THIS MNUMGR: "+label);
		}
		super.add(item);
	    prefix = "";	      
	}

	/**
	 * Cascade in one shot all input state inputs to all actions
	 */
	protected void cascadeAllInputs()
    {
    	//super.menuAboutToShow(ourSubMenu);
	    IContributionItem[] items = getItems();
        System.out.println(prefix+"INSIDE CASCADEALLINPUTS TO ALL ITEMS FOR SUBMENUMGR FOR "+label+". NBR ITEMS = "+items.length);
        System.out.println(prefix+"...shell = "+shell+", viewer = "+viewer+", selection = "+selection);
        String oldPrefix = prefix;
        prefix += "  ";
        super.cascadeAllInputs();
	    prefix = oldPrefix;
    }
	/**
	 * Cascade in one shot all input state inputs to one action
	 */
	protected void cascadeAllInputs(ISystemAction action)
    {
        System.out.println(prefix+"INSIDE CASCADEALLINPUTS TO ISYSTEMACTION(action="+action.getText()+") FOR THIS MNUMGR: "+label);
        System.out.println(prefix+"...shell = "+shell+", viewer = "+viewer+", selection = "+selection);
        super.cascadeAllInputs(action);
    }
	/**
	 * Cascade in one shot all input state inputs to one submenu
	 */
	protected void cascadeAllInputs(SystemSubMenuManager submenu)
    {
        System.out.println("INSIDE CASCADEALLINPUTS TO SUBMENUMGR(submenu="+submenu.getLabel()+") FOR THIS MNUMGR: "+label);
        System.out.println("...shell = "+shell+", viewer = "+viewer+", selection = "+selection);
        super.cascadeAllInputs(submenu);        
    }
}