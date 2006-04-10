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

package org.eclipse.rse.ui.filters;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemSubMenuManager;
import org.eclipse.rse.ui.filters.dialogs.SystemFilterWorkWithFilterPoolsDialog;
import org.eclipse.rse.ui.view.SystemView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;


/**
 * This subclass of the standard JFace tree viewer is used to
 * show a tree view of filter pools within filterpool managers,
 * for a work-with experience.
 */
public class SystemFilterWorkWithFilterPoolsTreeViewer 
       extends TreeViewer
       implements IMenuListener      
{

    private SystemFilterWorkWithFilterPoolsDialog caller;
    private Shell shell;
    private IAction[] contextMenuActions;
    private MenuManager menuMgr;    

	/**
	 * Constructor for SystemFilterWorkWithFilterPoolsTreeViewer
	 */
	public SystemFilterWorkWithFilterPoolsTreeViewer(Shell shell, SystemFilterWorkWithFilterPoolsDialog caller, Composite arg0) 
	{
		super(arg0);
		init(shell, caller);
	}

	/**
	 * Constructor for SystemFilterWorkWithFilterPoolsTreeViewer
	 */
	public SystemFilterWorkWithFilterPoolsTreeViewer(Shell shell, SystemFilterWorkWithFilterPoolsDialog caller, Composite arg0, int arg1) 
	{
		super(arg0, arg1);
		init(shell, caller);
	}

	/**
	 * Constructor for SystemFilterWorkWithFilterPoolsTreeViewer
	 */
	public SystemFilterWorkWithFilterPoolsTreeViewer(Shell shell,
	                                                 SystemFilterWorkWithFilterPoolsDialog caller, 
	                                                 Tree tree) 
	{
		super(tree);
		init(shell, caller);
	}
	
	/**
	 * Initialize
	 */
	public void init(Shell shell, SystemFilterWorkWithFilterPoolsDialog caller)
	{
		this.caller = caller;
		this.shell = shell;
		// -----------------------------
		// Enable right-click popup menu
		// -----------------------------
		menuMgr = new MenuManager("#PopupMenu");
	    menuMgr.setRemoveAllWhenShown(true);
	    menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(getTree());
		getTree().setMenu(menu);		
	}
	
	/**
	 * Set the context menu actions to show in the popup
	 */
	public void setContextMenuActions(IAction[] actions)
	{
		this.contextMenuActions = actions;
	}

    /**
     * Override of refresh from parent
     */
    public void refresh()
    {
    	if (!caller.refreshTree())
    	  super.refresh();
    	//super.refresh();    	
    }
    
    /**
     * Called when the context menu is about to open.
     */
    public void menuAboutToShow(IMenuManager menu)
    {
    	ISystemAction ourAction = null;
    	if (contextMenuActions != null)
        {
            //SystemMenuManager ourMenu = createStandardGroups(menu);
            SystemView.createStandardGroups(menu);
			SystemMenuManager ourMenu = new SystemMenuManager(menu);       	
        	for (int idx=0; idx<contextMenuActions.length; idx++)
        	{
        		if (contextMenuActions[idx] instanceof ISystemAction)
        		{
        			ourAction = (ISystemAction)contextMenuActions[idx];
        			ourMenu.add(ISystemContextMenuConstants.GROUP_REORGANIZE, ourAction);

	      	        if (ourAction instanceof SystemSubMenuManager)
	      	        {
	      	          SystemSubMenuManager item = (SystemSubMenuManager)ourAction;
	      	          item.setShell(shell);
	      	          item.setSelection(getSelection());	      	 	
	      	        }
        		}
        		else
        	      menu.appendToGroup(ISystemContextMenuConstants.GROUP_REORGANIZE, ourAction);        		  
        	}
        }
    }

	/*
	 * Creates the Systems plugin standard groups in a context menu.
	 *
	public SystemMenuManager createStandardGroups(IMenuManager menu) 
	{
		if (menu.isEmpty())
		{
	      // simply sets partitions in the menu, into which actions can be directed.
	      // Each partition can be delimited by a separator (new Separator) or not (new GroupMarker).
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_NEW)); // new->
		  //menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GOTO)); // goto into, go->
		  menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_EXPANDTO)); // expand TO->
		  menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPENWITH)); // open with->
		  menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPEN)); // open xxx
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_SHOW)); // show->type hierarchy, in-navigator
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_BUILD)); // build, rebuild, refresh
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_CHANGE)); // update, change
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE)); // rename,move,copy,delete,bookmark,refactoring
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER)); // move up, move down
		  //menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GENERATE)); // getters/setters, etc. Typically in editor
		  //menu.add(new Separator(ISystemContextMenuConstants.GROUP_SEARCH)); // search
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_CONNECTION)); // user or BP/ISV additions
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_STARTSERVER));  // start/stop remote server actions
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_IMPORTEXPORT)); // get or put actions
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADAPTERS)); // actions queried from adapters
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS)); // user or BP/ISV additions
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_VIEWER_SETUP)); // ? Probably View->by xxx, yyy
		  menu.add(new Separator(ISystemContextMenuConstants.GROUP_PROPERTIES)); // Properties
		}		
		return new SystemMenuManager(menu);
	}*/

	/**
	 * Private helper method to add an Action to a given menu.
	 * To give the action the opportunity to grey out, we call selectionChanged, but
	 * only if the action implements ISelectionChangedListener
	 */
	protected void menuAdd(MenuManager menu, IAction action)
	{
		if (action instanceof ISelectionChangedListener)
		  ((ISelectionChangedListener)action).selectionChanged(new SelectionChangedEvent(this,getSelection()));
	}
    
}