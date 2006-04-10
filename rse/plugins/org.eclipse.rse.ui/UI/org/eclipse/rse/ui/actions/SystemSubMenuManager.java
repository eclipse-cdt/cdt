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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.swt.widgets.Shell;



/**
 * For cascading menus, we need our own menu subclass so we can intercept
 *  the state-setting methods of our frameworks, and foreword those onto the
 *  sub-menu actions.
 * <p>
 * The state-setting methods including setShell, setSelection and setValue.
 */
public class SystemSubMenuManager  
       extends MenuManager
       //implements ISelectionChangedListener
       //implements ISystemAction
{
	protected String          toolTipText;	
    protected ImageDescriptor image = null;
    protected Shell           shell = null;
    protected Viewer          viewer = null;
    protected boolean         deferPopulation;
	protected boolean         traceSelections = false;	
	protected String          traceTarget;
    protected ISelection      selection;
    protected String          label;
    protected SystemBaseSubMenuAction parentCascadingAction;
    
	/**
	 * Constructor for SystemSubMenuManager
	 */
	public SystemSubMenuManager(SystemBaseSubMenuAction parentAction)
	{
		super();
		this.parentCascadingAction = parentAction;
	}
	/**
	 * Constructor for SystemSubMenuManager
	 */
	public SystemSubMenuManager(SystemBaseSubMenuAction parentAction, String text)
	{
		super(text);
		this.label = text;
		this.parentCascadingAction = parentAction;
	}
	/**
	 * Constructor for SystemSubMenuManager
	 */
	public SystemSubMenuManager(SystemBaseSubMenuAction parentAction, String text, String id)
	{
		super(text, id);
		this.label = text;
		this.parentCascadingAction = parentAction;
	}
	/**
	 * Constructor for SystemSubMenuManager
	 */
	public SystemSubMenuManager(SystemBaseSubMenuAction parentAction, String text, String id, ImageDescriptor image)
	{
		super(text, id);
		this.label = text;
		this.image = image;
		this.parentCascadingAction = parentAction;
	}
	
	/**
	 * Return the parent cascading menu action that created this.
	 */
	public SystemBaseSubMenuAction getParentCascadingAction()
	{
		return parentCascadingAction;
	}

	/**
	 * Set the tooltip text when this is used for in a cascading menu.
	 * @see org.eclipse.rse.ui.actions.SystemBaseSubMenuAction
	 */
	public void setToolTipText(String tip)
	{
		this.toolTipText = tip;
	}
	/**
	 * Get the tooltip text when this is used for in a cascading menu
	 */
	public String getToolTipText()
	{
		return toolTipText;
	}
	
	/**
	 * Return the label for this submenu
	 */
	public String getLabel()
	{
		return label;
	}
	
	// ------------------------
    // ISYSTEMACTION METHODS...
    // ------------------------	
    /**
     * An optimization for performance reasons that allows all inputs to be set in one call.
     * This is called by SystemView's fillContextMenu method.
     */
    public void setInputs(Shell shell, Viewer v, ISelection selection)
    {
		if (traceSelections)
		  issueTraceMessage(" INSIDE SETINPUTS FOR SUBMENUMGR FOR '"+label+"'");
    	this.shell = shell;
    	this.viewer = v;
        this.selection = selection;  
        if (parentCascadingAction != null)
          parentCascadingAction.setInputsFromSubMenuManager(shell, v, selection);
        cascadeAllInputs();
    }


	/** 
	 * Sets the parent shell for this action. Usually context dependent.
	 * We cascade this down to all of the actions added to this submenu.
	 */
	public void setShell(Shell shell)
	{
		this.shell = shell;
	    IContributionItem[] items = getItems();
	    for (int idx=0; idx < items.length; idx++)
	    {
	    	if ((items[idx] instanceof ActionContributionItem) &&
	      	    (((ActionContributionItem)items[idx]).getAction() instanceof ISystemAction))
	      	{	      	   
	      	   ISystemAction item = (ISystemAction) ( ((ActionContributionItem)items[idx]).getAction() );
	      	   item.setShell(shell);
	      	}
	      	else if (items[idx] instanceof SystemSubMenuManager)
	      	{
	      	   SystemSubMenuManager item = (SystemSubMenuManager)items[idx];
	      	   item.setShell(shell);
	      	}
	    }
    	if (traceSelections)
    	{
    	  issueTraceMessage("*** INSIDE SETSHELL FOR SUBMENUMGR "+label+". #ITEMS = "+items.length);
    	}

	}

	/**
	 * This is called by the framework to set the selection input, just prior to showing the popup menu.
	 * We cascade this down to all of the actions added to this submenu.
	 */
	public void setSelection(ISelection selection) 
	{	
        this.selection = selection;     
	    IContributionItem[] items = getItems();
	    for (int idx=0; idx < items.length; idx++)
	    {
	    	if ((items[idx] instanceof ActionContributionItem) &&
	      	    (((ActionContributionItem)items[idx]).getAction() instanceof ISystemAction))
	      	{	      	   
	      	   ISystemAction item = (ISystemAction) ( ((ActionContributionItem)items[idx]).getAction() );
	      	   item.setSelection(selection);
	      	}
	      	else if (items[idx] instanceof SystemSubMenuManager)
	      	{
	      	   SystemSubMenuManager item = (SystemSubMenuManager)items[idx];
	      	   item.setSelection(selection);
	      	}
	    }
    	if (traceSelections)
    	{
    	  issueTraceMessage("*** INSIDE SETSELECTION FOR SUBMENUMGR"+label+". #ITEMS = "+items.length);
    	}

	}	
	/**
	 * Set the Viewer that called this action. It is good practice for viewers to call this
	 *  so actions can directly access them if needed.
	 */
	public void setViewer(Viewer v)
	{
		this.viewer = v;
	    IContributionItem[] items = getItems();
	    for (int idx=0; idx < items.length; idx++)
	    {
	    	if ((items[idx] instanceof ActionContributionItem) &&
	      	    (((ActionContributionItem)items[idx]).getAction() instanceof ISystemAction))
	      	{	      	   
	      	   ISystemAction item = (ISystemAction) ( ((ActionContributionItem)items[idx]).getAction() );
	      	   item.setViewer(viewer);
	      	}
	      	else if (items[idx] instanceof SystemSubMenuManager)
	      	{
	      	   SystemSubMenuManager item = (SystemSubMenuManager)items[idx];
	      	   item.setViewer(viewer);
	      	}
	    }
	}
	
	/**
	 * Get the Viewer that called this action. Not guaranteed to be set,
	 *  depends if that viewer called setViewer or not. SystemView does.
	 */
	public Viewer getViewer()
	{
		return viewer;
	}	
	/**
	 * Get the Shell that hosts this action. Not guaranteed to be set,
	 */
	public Shell getShell()
	{
		return shell;
	}	
	/**
	 * Get the Selection
	 */
	public IStructuredSelection getSelection()
	{
		return (IStructuredSelection)selection;
	}	
		
	/**
	 * @see ContributionManager#add(IAction)
	 */

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
    	super.appendToGroup(groupName, action);
    	if (action instanceof ISystemAction)
          cascadeAllInputs((ISystemAction)action);    
	}
    /**
     * Intercept so we can cascade the selection, viewer and shell down
     */
	public void appendToGroup(String groupName, IContributionItem item) 
	{
		super.appendToGroup(groupName, item);
	    if (item instanceof SystemSubMenuManager)
	      cascadeAllInputs((SystemSubMenuManager)item);
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
		super.add(item);
		if (item instanceof ActionContributionItem) 
		{
			IAction action = ((ActionContributionItem)item).getAction();
			if (action instanceof ISystemAction)
		       cascadeAllInputs((ISystemAction)action);
		}
		else if (item instanceof SystemSubMenuManager)
		  cascadeAllInputs((SystemSubMenuManager)item);
	}

	/**
	 * Cascade in one shot all input state inputs to all actions
	 */
	protected void cascadeAllInputs()
    {
    	//super.menuAboutToShow(ourSubMenu);
	    IContributionItem[] items = getItems();
    	if (traceSelections)
    	{
    		issueTraceMessage("INSIDE CASCADEALLINPUTS FOR SUBMENUMGR FOR "+label+". NBR ITEMS = "+items.length);
    	}

	    for (int idx=0; idx < items.length; idx++)
	    {
	    	if ((items[idx] instanceof ActionContributionItem) &&
	      	    (((ActionContributionItem)items[idx]).getAction() instanceof ISystemAction))
	      	{	      	   
	      	   ISystemAction item = (ISystemAction) ( ((ActionContributionItem)items[idx]).getAction() );
	      	   if (!item.isDummy())
	      	     cascadeAllInputs(item);
	      	}
	      	else if (items[idx] instanceof SystemSubMenuManager)
	      	{
	      	   SystemSubMenuManager item = (SystemSubMenuManager)items[idx];
	      	   cascadeAllInputs(item);
	      	}
	    }

    }
	/**
	 * Cascade in one shot all input state inputs to one action
	 */
	protected void cascadeAllInputs(ISystemAction action)
    {
    	if (action.isDummy())
    	  return; // waste of time
		if (shell != null)
		  action.setShell(shell);
		if (viewer != null)
		  action.setViewer(viewer);
		if (selection != null)
		  action.setSelection(selection);
    }
	/**
	 * Cascade in one shot all input state inputs to one submenu
	 */
	protected void cascadeAllInputs(SystemSubMenuManager submenu)
    {
		if (shell != null)
		  submenu.setShell(shell);
		if (viewer != null)
		  submenu.setViewer(viewer);
		if (selection != null)
		  submenu.setSelection(selection);
    }
    // ------------------------
    // HELPER METHODS...
    // ------------------------
    /**
     * Turn on tracing for selections, shell and viewer to watch as it is set
     */
    public void setTracing(boolean tracing)
    {
    	traceSelections = tracing;
    }
    /**
     * Turn on tracing for selections, shell and viewer to watch as it is set,
     *  scoped to a particular class name (will use indexOf('xxx') to match).
     */
    public void setTracing(String tracingClassTarget)
    {
    	traceSelections = (tracingClassTarget != null);
    	traceTarget = tracingClassTarget;
    }
    /**
     * Turn on tracing for selections, shell and viewer to watch as it is set,
     *  scoped to a particular class name (will use indexOf('xxx') to match).
     */
    public void setTracing(boolean tracing, String tracingClassTarget)
    {
    	traceSelections = tracing;
    	traceTarget = tracingClassTarget;
    }

    /**
     * Issue trace message
     */
    protected void issueTraceMessage(String msg)
    {
    	if (traceSelections)
    	{
    		String className = this.getClass().getName();
    		if ((traceTarget==null) || (className.indexOf(traceTarget)>=0))
    		{
    		  className = className.substring(className.lastIndexOf('.'));
              SystemBasePlugin.logInfo(className+": "+msg);
    		}
    	}
    }

}