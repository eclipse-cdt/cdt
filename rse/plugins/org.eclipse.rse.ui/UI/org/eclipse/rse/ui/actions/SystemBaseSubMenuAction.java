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
import java.util.ResourceBundle;
import java.util.Vector;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.ui.view.SystemViewMenuListener;
import org.eclipse.swt.widgets.Shell;

/**
 * Our framework is designed to allow actions to be added to popup menus.
 * Sometimes, we want an expandable or cascading menu item for an action. 
 * That is what this class is designed for. It represents a populated submenu.
 */
public abstract class SystemBaseSubMenuAction
	extends SystemBaseAction

{
	
    protected SystemSubMenuManager subMenu = null;
    protected String actionLabel;
    protected String menuID;
    protected boolean createMenuEachTime = true;
    protected boolean populateMenuEachTime = true;    
    private boolean dontCascade = false;
    private boolean test;
    private static final IAction[] EMPTY_ACTION_ARRAY = new IAction[0];

	/**
	 * Constructor for SystemBaseSubMenuAction when there is an image
	 * @param label
	 * @param tooltip
	 * @param image The image to display for this action
	 * @param shell The owning shell. If you pass null now, be sure to call setShell later
	 * 
	 * @deprecated use fields from resource class directly now instead of via ResourceBundle
	 */	
	protected SystemBaseSubMenuAction(ResourceBundle rb, String label, String tooltip,ImageDescriptor image,Shell shell)
	{
		super(label, tooltip, image, shell);
		actionLabel = label;
		//setTracing(true);		
	}


	/**
	 * Constructor for SystemBaseSubMenuAction when there is just a string
	 * @param label The label to display
	 * @param parent The owning shell. If you pass null now, be sure to call setShell later
	 */
	protected SystemBaseSubMenuAction(String label, Shell shell) 
	{
		super(label, shell);
		actionLabel = label;
		//setTracing(true);
	}
	/**
	 * Constructor for SystemBaseSubMenuAction when there is just a string
	 * @param label The label to display
	 * @param tooltip The tooltip to display
	 * @param parent The owning shell. If you pass null now, be sure to call setShell later
	 */
	protected SystemBaseSubMenuAction(String label, String tooltip, Shell shell) 
	{
		super(label, tooltip, shell);
		actionLabel = label;
		//setTracing(true);
	}
	/**
	 * Constructor for SystemBaseSubMenuAction when there is just a string and image
	 * @param label The label to display
	 * @param parent The owning shell. If you pass null now, be sure to call setShell later
	 */
	protected SystemBaseSubMenuAction(String label, ImageDescriptor image, Shell shell) 
	{
		super(label, image, shell);
		actionLabel = label;
		//setTracing(true);
	}
	
	/**
	 * Constructor for SystemBaseSubMenuAction when there is just a string and image
	 * @param label The label to display
	 * @param tooltip the tooltip to display
	 * @param parent The owning shell. If you pass null now, be sure to call setShell later
	 */
	protected SystemBaseSubMenuAction(String label, String tooltip, ImageDescriptor image, Shell shell) 
	{
		super(label, tooltip, image, shell);
		actionLabel = label;
		//setTracing(true);
	}

    /**
     * Set the menu ID. This is important to allow action contributions via the popupMenus extension point.
     */
    public void setMenuID(String Id)
    {
    	this.menuID = Id;
    }

    /**
     * Call this if the submenu should be created on-the-fly every time, versus creating and populating it
     * only on the first usage.
     */
    public void setCreateMenuEachTime(boolean eachTime)
    {
    	this.createMenuEachTime = eachTime;
    }
    /**
     * Call this if the submenu should be populated on-the-fly every time, versus populating it
     * only on the first usage. This only makes sense to be true if setCreateMenuEachTime is false.
     */
    public void setPopulateMenuEachTime(boolean eachTime)
    {
    	this.populateMenuEachTime = eachTime;
    }
    
    /**
     * Set test mode on
     */
    public void setTest(boolean testMode)
    {
    	this.test = testMode;
    }

    	
	/**
	 * <i>Must be overridden</i>
	 * <p>Example of this:<p>
     * <pre><code>
	 *  menu.add(new MyAction1());
	 * </code></pre>
	 * @param menu The cascading menu, which is created for you. Add your actions to it.
	 * @return The given menu if you just populated it, or a new menu if you want to create the menu yourself.
	 */
	public abstract IMenuManager populateSubMenu(IMenuManager menu);

    /**
     * Return the MenuManager object. It is this that is added to the primary popup menu.
     */
    public IMenuManager getSubMenu()
    {
    	if ((subMenu == null) || createMenuEachTime)
    	{
    	  if (menuID == null)
    	  {
    	  	if (test)
    	      subMenu = new SystemSubMenuManagerForTesting(this,actionLabel);
    	  	else
    	      subMenu = new SystemSubMenuManager(this,actionLabel);
    	  }
    	  else
    	  {
    	  	if (test)
    	       subMenu = new SystemSubMenuManagerForTesting(this, actionLabel, menuID);
    	    else
    	       subMenu = new SystemSubMenuManager(this,actionLabel, menuID);
    	  }
    	  createStandardGroups(subMenu);
    	  subMenu.setTracing(traceSelections, traceTarget);
    	  populateSubMenu(subMenu);
    	  if (traceSelections)
    	  {
    	    issueTraceMessage("*** INSIDE GETSUBMENU for "+actionLabel+". createMenuEachTime = " + createMenuEachTime);
    	  }
    	  subMenu.setToolTipText(getToolTipText());
    	  //cascadeAllInputs(); no point in doing in now, setInputs will be called later by SV
		  subMenu.addMenuListener(createMnemonicsListener(!populateMenuEachTime));
    	}
    	else if (populateMenuEachTime)
    	{
    	  subMenu.removeAll();
    	  createStandardGroups(subMenu);
    	  populateSubMenu(subMenu);
    	  if (traceSelections)
    	  {
    	    issueTraceMessage("*** INSIDE GETSUBMENU for "+actionLabel+". populateMenuEachTime = " + populateMenuEachTime);
    	  }
    	  //cascadeAllInputs(); no point in doing in now, setInputs will be called later by SV
		  //Menu m = subMenu.getMenu();
		  //System.out.println("SubMenu's menu null? " + (m==null));
		  //if (m != null)
		    //m.addMenuListener(new SystemViewMenuListener());
    	} 
    	else if (traceSelections)
    	{
    	  issueTraceMessage("*** INSIDE GETSUBMENU for "+actionLabel+". SUBMENU ALREADY CREATED. ");
    	}
    	
    	return subMenu;
    }
	/**
	 * Creates the standard groups for the context sub-menu.
	 */
	protected void createStandardGroups(IMenuManager menu) 
	{
		if (!menu.isEmpty())
			return;			
	    // simply sets partitions in the menu, into which actions can be directed.
	    // Each partition can be delimited by a separator (new Separator) or not (new GroupMarker).
	    // Deleted groups are not used yet.
	    //... decided it is better to let this get created when needed, else will be at the top of the menu.
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS)); // user or BP/ISV additions
		
	}
	
	/**
	 * Return the actions currently in the menu.
	 * Never returns null, but may return an empty array. 
	 */
	public IAction[] getActions()
	{
		//System.out.println("in getActions. subMenu null? "+(subMenu==null));
		if (subMenu==null)
			return EMPTY_ACTION_ARRAY;
		else
		{
			IContributionItem[] items = subMenu.getItems();
			//System.out.println("in getActions. #items "+items.length);
			Vector v = new Vector();
			for (int idx=0; idx<items.length; idx++)
				if (items[idx] instanceof ActionContributionItem)
					v.add( ((ActionContributionItem)items[idx]).getAction() );
				else if (items[idx] instanceof SystemSubMenuManager)
				{
					SystemSubMenuManager menu = (SystemSubMenuManager)items[idx];
					v.add(menu.getParentCascadingAction());
				}
				//else
				    //System.out.println("...item: "+items[idx].getClass().getName());
			IAction[] actions = new IAction[v.size()];
			for (int idx=0; idx<v.size(); idx++)
				actions[idx] = (IAction)v.elementAt(idx);
			return actions;
		}
	}


    
    /**
     * Overridable method that instantiates the menu listener who job is to add mnemonics.
     * @param setMnemonicsOnlyOnce true if the menu is static and so mnemonics need only be set once. False if it is dynamic
     */
    protected SystemViewMenuListener createMnemonicsListener(boolean setMnemonicsOnlyOnce)
    {
    	return new SystemViewMenuListener(setMnemonicsOnlyOnce);
    }
        
	/** 
	 * Sets the parent shell for this action. This is an override of our parent's method so we can
	 * cascade it to each sub-action.
	 */
	public void setShell(Shell shell)
	{
		super.setShell(shell);
		if (!dontCascade)
		  cascadeShell();
	}
	
	/**
	 * This is called by the framework to set the selection input, just prior to showing the popup menu.
	 * We cascade this down to all of the actions added to this submenu.
	 */
	public void setSelection(ISelection selection) 
	{	
        super.setSelection(selection);
		if (!dontCascade)
          cascadeSelection(selection);
	}	

	/**
	 * This is called by the framework to set the selection input, just prior to showing the popup menu.
	 * We cascade this down to all of the actions added to this submenu.
	 */
	public void setViewer(Viewer v) 
	{	
        super.setViewer(v);
		if (!dontCascade)
          cascadeViewer();
	}	
	
	/**
	 * Return the shell. If not set locally, queries it from the submenu, which is where it is set by the RSE framework
	 */
	public Shell getShell()
	{
		if (super.getShell(false) != null)
		  return super.getShell(false);
		else if (subMenu != null)
		  return subMenu.getShell();
		else
		  return null;
	}
	/**
	 * Return the selection. If not set locally, queries it from the submenu, which is where it is set by the RSE framework
	 */
	public IStructuredSelection getSelection()
	{
		if (super.getSelection() != null)
		  return super.getSelection();
		else if (subMenu != null)
		  return subMenu.getSelection();
		else
		  return null;
	}
	/**
	 * Return the viewer. If not set locally, queries it from the submenu, which is where it is set by the RSE framework
	 */
	public Viewer getViewer()
	{
		if (super.getViewer() != null)
		  return super.getViewer();
		else if (subMenu != null)
		  return subMenu.getViewer();
		else
		  return null;
	}
	
	/**
	 * Special method called by our submenu manager when from its setInputs method. No need to
	 *  cascade as the menu manager will do it for us.
	 */
	public void setInputsFromSubMenuManager(Shell shell, Viewer v, ISelection selection)
	{
    	dontCascade = true; // so we don't redundantly do cascading. Phil
    	super.setInputs(shell, v, selection); // calls setSelection+Shell+Viewer
    	dontCascade = false;		
	}
	
    /**
     * An optimization for performance reasons that allows all inputs to be set in one call.
     * Intercept of parent so we can cascade to sub-actions. Note however this won't really ever
     * get called. This is because for cascading menu actions we don't put this action object into
     * the menumanager ... we put the SystemSubMenu object so that is what the SystemView will call.
     */
    public void setInputs(Shell shell, Viewer v, ISelection selection)
    {
    	dontCascade = true; // so we don't redundantly do cascading. Phil
    	super.setInputs(shell, v, selection); // calls setSelection+Shell+Viewer
    	dontCascade = false;
		if (traceSelections)
		  issueTraceMessage(" INSIDE SETINPUTS IN BASE ACTION CLASS");
		cascadeAllInputs();
    }
	
    /**
     * Cascade the current selection to all actions
     */
    private void cascadeSelection(ISelection selection)
    {
    	if (traceSelections)
    	{
    	  issueTraceMessage("*** INSIDE CASCADESELECTION ***");
    	  issueTraceMessage("  subMenu = " + subMenu);
    	  issueTraceMessage("  selection = " + selection);
    	}
        if (subMenu == null)
          return;
        subMenu.setSelection(selection);
        /*
		IAction[] actions = subMenu.getActions();
		for (int idx=0; idx<actions.length; idx++)
		{
		   if (actions[idx] instanceof ISystemAction)
		   {
		     ((ISystemAction)actions[idx]).setSelection(selection);		   	 
		   }
		}
		*/
    }
 
    /**
     * Cascade the current shell to all actions
     */
    private void cascadeShell()
    {
        if (subMenu == null)
          return;
    	Shell shell = super.getShell(false);
    	if (shell != null)
    	{
          subMenu.setShell(shell);
          /*
          IAction[] items = subMenu.getActions();
          for (int idx=0; idx<items.length; idx++)
             if (items[idx] instanceof ISystemAction)   		
               ((ISystemAction)items[idx]).setShell(shell);
          */
    	}
    }

    /**
     * Cascade the current viewer to all actions
     */
    private void cascadeViewer()
    {
        if (subMenu == null)
          return;
    	if (viewer != null)
    	{
    		subMenu.setViewer(viewer);
    		/*
          IAction[] items = subMenu.getActions();
          for (int idx=0; idx<items.length; idx++)
             if (items[idx] instanceof ISystemAction)   		
               ((ISystemAction)items[idx]).setViewer(viewer);
            */
    	}
    }

	/**
	 * Called when actions added dynamically
	 */
	protected void cascadeAllInputs()
    {
        if (subMenu == null)
          return;
    	Shell shell = super.getShell(false);
    	Viewer viewer = super.getViewer();
    	ISelection selection = super.getSelection();
    	subMenu.setInputs(shell, viewer, selection);
    	/*
		IAction[] actions = subMenu.getActions();
    	if (traceSelections)
    	{
    		issueTraceMessage("INSIDE CASCADEALLINPUTS FOR "+actionLabel+". NBR ACTIONS = "+actions.length);
    	}

		for (int idx=0; idx<actions.length; idx++)
		{
		   if (actions[idx] instanceof ISystemAction)
		   {
		   	 ISystemAction action = (ISystemAction)actions[idx];
		   	 action.setInputs(shell, viewer, selection);
		   }
	    }    	
	    */
    }
    
    /**
     * Enable/disable this menu action. This amounts to enabling/disabling the associated sub menu.
     * Unfortunately, there is no way to do this!
     */
    public void setEnabled(boolean enable)
    {
    	super.setEnabled(enable);
    	if (subMenu!=null)
    	{
    		//subMenu.setEnabled(enable);
    	}    	
    }
}