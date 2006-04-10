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

package org.eclipse.rse.ui.view.team;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemModelChangeEvent;
import org.eclipse.rse.model.ISystemModelChangeEvents;
import org.eclipse.rse.model.ISystemModelChangeListener;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemProfileManager;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.SystemStartHere;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemDeleteTarget;
import org.eclipse.rse.ui.ISystemRenameTarget;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemCollapseAllAction;
import org.eclipse.rse.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.ui.actions.SystemCommonSelectAllAction;
import org.eclipse.rse.ui.actions.SystemNewProfileAction;
import org.eclipse.rse.ui.actions.SystemSubMenuManager;
import org.eclipse.rse.ui.actions.SystemTeamReloadAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.view.IRSEViewPart;
import org.eclipse.rse.ui.view.ISystemMementoConstants;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemViewMenuListener;
import org.eclipse.rse.ui.view.SystemViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;


/**
 * The viewer and view part for the Team view
 */
public class SystemTeamViewPart
	extends ViewPart
	implements ISetSelectionTarget, ISelectionProvider, ISystemModelChangeListener, 
	           ISystemMessageLine, ISelectionChangedListener,
				ISystemDeleteTarget, ISystemRenameTarget, IMenuListener, IRSEViewPart
{

	private boolean menuListenerAdded;
	public static final String ID = "org.eclipse.rse.ui.view.teamView";
	
	private SystemTeamViewInputProvider input = null;
	private SystemTeamView treeViewer = null;
	//private FrameList frameList = null;
	private IStatusLineManager statusLine = null;
	private String             message, errorMessage;
	private SystemMessage      sysErrorMessage;

	// selectionChangedListeners 
	private ListenerList selectionChangedListeners = new ListenerList(6);
	
	private boolean privateProfileStillExists = false;
	
	// context menu actions for project...
	protected SystemTeamReloadAction reloadRSEAction;
	protected SystemNewProfileAction newProfileAction;
	// common context menu actions...
	protected SystemCommonDeleteAction deleteAction;
	protected PropertyDialogAction propertyDialogAction;	
	protected SystemTeamViewRefreshAllAction toolBarRefreshAllAction, menuRefreshAllAction;
	protected SystemCollapseAllAction collapseAllAction;
	
	protected ISystemViewElementAdapter profileAdapter = SystemPlugin.getDefault().getSystemViewAdapterFactory().getProfileAdapter();

	// remember-state variables...	
	private IMemento                 fMemento;
	// state...
	static final String TAG_RELEASE= "release"; 
	static final String TAG_SELECTION= "selection"; 
	static final String TAG_EXPANDED_TO= "expandedTo"; 
	static final String TAG_EXPANDED= "expanded"; 
	static final String TAG_ELEMENT= "element"; 
	static final String TAG_PATH= "path";  
	static final String TAG_INPUT= "svInput"; 
	static final String TAG_VERTICAL_POSITION= "verticalPosition"; 
	static final String TAG_HORIZONTAL_POSITION= "horizontalPosition";	
	static final String MEMENTO_DELIM = "///";
		
	/**
	 * Remove a selection change listener
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) 
	{
		selectionChangedListeners.remove(listener);
	}
	/**
	 * Add a selection change listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) 
	{
		selectionChangedListeners.add(listener);
	}

	/**
	 * Returns selection for the tree view
	 */
	public ISelection getSelection() 
	{
		return treeViewer.getSelection(); 
	}

	public void setSelection(ISelection selection) 
	{
		treeViewer.setSelection(selection);
	}

	/**
	 * Returns the tree viewer selection as a structured selection
	 */
	public IStructuredSelection getStructuredSelection() 
	{
		// we know we have a ss.
		return (IStructuredSelection) (treeViewer.getSelection());
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
	public Viewer getRSEViewer()
	{
		return treeViewer;
	}

	/**
	 * We are getting focus
	 */
	public void setFocus() 
	{
		if (treeViewer == null)
		 	return;
		Tree tree = treeViewer.getTree();
		if (tree != null)
			treeViewer.getTree().setFocus();
	}

	/**
	 * Create the viewer to go in this view part.
	 */
	public void createPartControl(Composite parent) 
	{
		treeViewer =
			//new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			new SystemTeamView(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, this);
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(new SystemTeamViewContentProvider());
		treeViewer.setLabelProvider(new SystemTeamViewLabelProvider(treeViewer));

		treeViewer.setInput(getInput());

		addTreeViewerListeners();

		// create the frame list.
		//frameList = createFrameList();

		// now update title of the view part.
		updateTitle();

		// Handle menus:
		// think about menu manager id later.
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		Menu menu = menuMgr.createContextMenu(treeViewer.getTree());
		treeViewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
		// important to add our listener after registering, so we are called second!
		// This gives us the opportunity to scrub the contributions added by others, to screen out
		//  non-team additions.
		menuMgr.addMenuListener(this);
		/*
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SystemTeamViewPart.this.fillContextMenu(manager);
			}
		});*/

		// Fill the action bars and update the global action handlers'
		// enabled state to match the current selection. We pass the selection
		// based on the iSeries object model. The action group will handle 
		// delegating the correct object model to the actions.
		fillActionBars(getViewSite().getActionBars());
		//updateActionBars(getStructuredSelection());

		// this is a must here to get Properties Pages to work.
		getSite().setSelectionProvider(treeViewer);
		//getSite().setSelectionProvider(this);

		// Update status line.
		statusLine =	getViewSite().getActionBars().getStatusLineManager();
		//updateStatusLine(getStructuredSelection());

		// we need to refresh viewer when page gets activated for Marker updates
		//pageListener = new CurrentPageListener(getSite().getPage());
		//getSite().getWorkbenchWindow().addPageListener(pageListener);

		// update F1 help
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IF1HelpContextID.NAV01);

		SystemPlugin.getTheSystemRegistry().addSystemModelChangeListener(this);
		
		treeViewer.setAutoExpandLevel(2); // dang, it doesn't work!

		// ----------------------
		// Restore previous state
		// ----------------------		
		if (fMemento != null)
			restoreState(fMemento);
		fMemento= null;
	}

	/**
	 * Called when the context menu is about to open.
	 * From IMenuListener interface
	 * Calls {@link #fillContextMenu(IMenuManager)}
	 */
	public void menuAboutToShow(IMenuManager menu)
	{
		fillContextMenu(menu);
		if (!menuListenerAdded)
		{
		  if (menu instanceof MenuManager)
		  {
			Menu m = ((MenuManager)menu).getMenu();
			if (m != null)
			{
				menuListenerAdded = true;
				SystemViewMenuListener ml = new SystemViewMenuListener();
				ml.setShowToolTipText(true, (ISystemMessageLine)this);
				m.addMenuListener(ml);
			}
		  }
		}
		//System.out.println("Inside menuAboutToShow: menu null? "+( ((MenuManager)menu).getMenu()==null));
	}
	
	// -------------------------------------------
	// MEMENTO SUPPORT (SAVING/RESTORING STATE)...
	// -------------------------------------------
	/**
	 * Initializes this view with the given view site.  A memento is passed to
	 * the view which contains a snapshot of the views state from a previous
	 * session.  Where possible, the view should try to recreate that state
	 * within the part controls.
	 * <p>
	 * The parent's default implementation will ignore the memento and initialize
	 * the view in a fresh state.  Subclasses may override the implementation to 
	 * perform any state restoration as needed.
	 */
	public void init(IViewSite site,IMemento memento) throws PartInitException 
	{
		super.init(site,memento);
		fMemento = memento;
		//System.out.println("INSIDE INIT");
	}

	/**
	 * Adds the listeners to the tree viewer.
	 */
	protected void addTreeViewerListeners() 
	{
		treeViewer.addDoubleClickListener(new IDoubleClickListener() 
		{
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		
		//System.out.println("Add key listener");
		
		treeViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {				
				handleKeyReleased(e);
			} });
		treeViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {				
				handleKeyPressed(e);
			} });
			
		treeViewer.addSelectionChangedListener(this);

		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				handleOpen(event);
			}
		});
	}


	/**
	 * Returns the shell to use for opening dialogs.
	 * Used in this class, and in the actions.
	 */
	public Shell getShell() 
	{
		return getViewSite().getShell();
	}

	/**
	 * Handles double clicks in viewer. It is responsible for expanding
	 * and collapsing of folders.
	 */
	private void handleDoubleClick(DoubleClickEvent event) 
	{
		/*
		IStructuredSelection rseSSel =
			(IStructuredSelection) event.getSelection();
		Object rseObject = rseSSel.getFirstElement();
		if (treeViewer.isExpandable(rseObject)) 
		{
			treeViewer.setExpandedState(
				rseObject,
				!treeViewer.getExpandedState(rseObject));
		}
		*/
	}

	/**
	 * Handles an open event from the viewer.
	 * Opens an editor on the selected file.
	 */
	protected void handleOpen(OpenEvent event) 
	{
	}

	/**
	 * Handles key events in viewer.<br>
	 * This is needed for various keys (eg: delete key) and for model dump.
	 */
	private void handleKeyReleased(KeyEvent event) 
	{
		//System.out.println("in handleKeyPressed. keyCode == SWT.F5? " + (event.keyCode==SWT.F5) + ", keyCode: "+event.keyCode);
		if (event.keyCode == SWT.F5) 
		{
			getRefreshAllToolbarAction(getStructuredSelection()).run();
		}
	}
	/**
	 * Handles key events in viewer.<br>
	 * This is needed for various keys (eg: delete key) and for model dump.
	 */
	private void handleKeyPressed(KeyEvent event) 
	{
		//System.out.println("in handleKeyPressed. keyCode == SWT.F5? " + (event.keyCode==SWT.F5) + ", keyCode: "+event.keyCode);
		IStructuredSelection selection = (IStructuredSelection)getSelection();
		if ((event.character == SWT.DEL) && (event.stateMask == 0) && (selection.size()>0) )
		{	      
			if (showDelete() && canDelete())
			{
				SystemCommonDeleteAction dltAction = (SystemCommonDeleteAction)getDeleteAction(selection);
				dltAction.setShell(getShell());
				dltAction.setSelection(getSelection());
				dltAction.setViewer(getViewer());
				dltAction.run();
			}
		}
		else if ((event.character == '-') && (event.stateMask == SWT.CTRL) )
		{
			SystemCollapseAllAction collapseAllAction = getCollapseAllAction();
			collapseAllAction.setShell(getShell());
			collapseAllAction.run();
		}
		else if ((event.character == '-') && (selection.size()>0) )
		{
			//System.out.println("Inside Ctrl+- processing");
			treeViewer.collapseSelected();
		}
		else if ((event.character == '+') && (selection.size()>0) )
		{
			//System.out.println("Inside Ctrl++ processing");
			treeViewer.expandSelected();
		}

	}

	/**
	 * Reveal and selects the passed selection in viewer.<br>
	 */
	public void selectReveal(ISelection selection) 
	{
		if (!(selection instanceof StructuredSelection))
		  return;
		StructuredSelection ssel = (StructuredSelection)selection;
		java.util.List test = ssel.toList();
		if (!ssel.isEmpty()) {
			// select and reveal the item
			treeViewer.setSelection(ssel, true);
		}
	}

	/**
	 * Called when the context menu is about to open.
	 */
	private void fillContextMenu(IMenuManager menu) 
	{
		SystemMenuManager ourMenu = new SystemMenuManager(menu);
		
	    privateProfileStillExists = (SystemStartHere.getSystemProfileManager().getDefaultPrivateSystemProfile() != null);

		// Populate with our stuff...
		IStructuredSelection selection = getStructuredSelection();
		Object firstSelection = selection.getFirstElement();
		if (firstSelection instanceof IProject)
		{
			// Scrub unrelated menu items
			scrubOtherContributions(menu);
			createStandardGroups(menu);			
			if (selection.size() == 1)
		      fillProjectContextMenu(ourMenu, selection);
		}
		else
		{
			createStandardGroups(menu);
			ISystemViewElementAdapter adapter = SystemAdapterHelpers.getAdapter(firstSelection, treeViewer);
			if (adapter != null)
			{
				if ((firstSelection instanceof SystemTeamViewSubSystemFactoryNode) ||
		// FIXME - compile actions separate now			(firstSelection instanceof SystemTeamViewCompileTypeNode) ||
					(firstSelection instanceof ISystemProfile))
				{ 
					addActions(ourMenu, selection);
				}
				else if (firstSelection instanceof ISystemFilterPool)
				{
					//SystemTestingAction testAction = new SystemTestingAction(getShell(), this);
					//testAction.setSelection(getSelection());
					//ourMenu.add(ISystemContextMenuConstants.GROUP_CHANGE, testAction);					
				}
			}			
		}
		// wail through all actions, updating shell and selection
		IContributionItem[] items = menu.getItems();
		for (int idx=0; idx < items.length; idx++)
		{
		   if ((items[idx] instanceof ActionContributionItem) &&
			   (((ActionContributionItem)items[idx]).getAction() instanceof ISystemAction))
		   {	      	   
			 ISystemAction item = (ISystemAction) ( ((ActionContributionItem)items[idx]).getAction() );
			 try{
			   item.setInputs(getShell(), getViewer(), selection);
			 } catch (Exception e)
			 {
			   SystemBasePlugin.logError("Error configuring action " + item.getClass().getName(),e);	      	   	      	   	
			   System.err.println("Error configuring action " + item.getClass().getName());
			 }
		   }
		   else if (items[idx] instanceof SystemSubMenuManager)
		   {
			 SystemSubMenuManager item = (SystemSubMenuManager)items[idx];
			 item.setInputs(getShell(), getViewer(), selection);
		   }
		}		
		PropertyDialogAction pdAction = getPropertyDialogAction(selection);           
		if (pdAction.isApplicableForSelection())
		  menu.appendToGroup(ISystemContextMenuConstants.GROUP_PROPERTIES, pdAction);
	}
	
	/**
	 * Let each object add their own actions...
	 * @param menu
	 */
	protected void addActions(SystemMenuManager ourMenu, IStructuredSelection selection)
	{
		// ADAPTER SPECIFIC ACTIONS   	                
		Iterator elements= selection.iterator();
		Hashtable adapters = new Hashtable();		    		  
		while (elements.hasNext())
		{
		  Object element= elements.next();
		  ISystemViewElementAdapter adapter = SystemAdapterHelpers.getAdapter(element, treeViewer);
		  if (adapter != null)
		  	adapters.put(adapter,element); // want only unique adapters
		}
		Enumeration uniqueAdapters = adapters.keys();
		Shell shell = getShell();
		while (uniqueAdapters.hasMoreElements())
		{
		   ISystemViewElementAdapter nextAdapter = (ISystemViewElementAdapter)uniqueAdapters.nextElement();
		   nextAdapter.addActions(ourMenu,selection,shell,ISystemContextMenuConstants.GROUP_ADAPTERS);
		   //if (nextAdapter instanceof AbstractSystemViewAdapter)
			// ((AbstractSystemViewAdapter)nextAdapter).addCommonRemoteActions(ourMenu,selection,shell,ISystemContextMenuConstants.GROUP_ADAPTERS);
		}

		// wail through all actions, updating shell and selection
		IContributionItem[] items = ourMenu.getMenuManager().getItems();
		for (int idx=0; idx < items.length; idx++)
		{
		   	if ((items[idx] instanceof ActionContributionItem) &&
				   (((ActionContributionItem)items[idx]).getAction() instanceof ISystemAction))
		   	{	      	   
			 	ISystemAction item = (ISystemAction) ( ((ActionContributionItem)items[idx]).getAction() );
			 	try{
			   		item.setInputs(getShell(), treeViewer, selection);
			 	} catch (Exception e)
			 	{
			   		SystemBasePlugin.logError("Error configuring action " + item.getClass().getName(),e);	      	   	      	   	
			   		System.out.println("Error configuring action " + item.getClass().getName());
			 	}
		   }
		   else if (items[idx] instanceof SystemSubMenuManager)
		   {
			 	SystemSubMenuManager item = (SystemSubMenuManager)items[idx];
			 	item.setInputs(getShell(), treeViewer, selection);
		   }
		}		
	}
	
	/**
	 * Creates the Systems plugin standard groups in a context menu.
	 */
	public void createStandardGroups(IMenuManager menu) 
	{
		//if (!menu.isEmpty())
		//	return;			
	    // simply sets partitions in the menu, into which actions can be directed.
	    // Each partition can be delimited by a separator (new Separator) or not (new GroupMarker).
	    // Deleted groups are not used yet.
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_NEW));          // new->
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_NEW_NONCASCADING)); // new stuff
		/*
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GOTO));       // goto into, go->
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_EXPANDTO));   // expand to->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_EXPAND));       // expand, collapse
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPEN));       // open xxx
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPENWITH));   // open with->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_BROWSEWITH));   // open with->
		*/
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_WORKWITH));     // work with->		
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_BUILD));        // build, rebuild, refresh
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CHANGE));       // update, change
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE));   // rename,move,copy,delete,bookmark,refactoring
		/*
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER));      // move up, move down		
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GENERATE)); // getters/setters, etc. Typically in editor
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_SEARCH));     // search
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CONNECTION));   // connection-related actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_STARTSERVER));  // Start Remote Server cascading menu
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_IMPORTEXPORT)); // get or put actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADAPTERS));     // actions queried from adapters
		*/
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS));    // user or BP/ISV additions
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_VIEWER_SETUP)); // ? Probably View->by xxx, yyy
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_TEAM));         // Team
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_PROPERTIES));   // Properties
	}
	
	
	/**
	 * Fill context menu for IProjects
	 */
	private void fillProjectContextMenu(SystemMenuManager menu, IStructuredSelection selection)
	{
		menu.add(ISystemContextMenuConstants.GROUP_BUILD,getRefreshAllMenuAction(selection));
		menu.add(ISystemContextMenuConstants.GROUP_BUILD,getReloadRSEAction(selection));
		menu.add(ISystemContextMenuConstants.GROUP_NEW,getNewProfileAction(selection));		
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_PROPERTIES));
	    //menu.appendToGroup(ISystemContextMenuConstants.GROUP_PROPERTIES, getPropertyDialogAction(selection));
	}

	/**
	 * Get the properties dialog action
	 */
	private PropertyDialogAction getPropertyDialogAction(IStructuredSelection selection)
	{
		 if (propertyDialogAction == null)
           propertyDialogAction = new PropertyDialogAction(new SameShellProvider(getShell()), treeViewer);
         propertyDialogAction.selectionChanged(selection);
         return propertyDialogAction;
	}
	/**
	 * Get the reload RSE action for the context menu
	 */
	private SystemTeamReloadAction getReloadRSEAction(IStructuredSelection selection)
	{
		if (reloadRSEAction == null)
		  reloadRSEAction = new SystemTeamReloadAction(getShell());
        reloadRSEAction.setSelection(selection);
        if (privateProfileStillExists)
          reloadRSEAction.setEnabled(false);
		return reloadRSEAction;
	}
	/**
	 * Get the refresh All action for the context menu
	 */
	private SystemTeamViewRefreshAllAction getRefreshAllMenuAction(IStructuredSelection selection)
	{
		if (menuRefreshAllAction == null)
		  menuRefreshAllAction = new SystemTeamViewRefreshAllAction(getShell(), this);
        menuRefreshAllAction.setSelection(selection);
		return menuRefreshAllAction;
	}
	/**
	 * Get the refresh All action for the toolbar
	 */
	private SystemTeamViewRefreshAllAction getRefreshAllToolbarAction(IStructuredSelection selection)
	{
		if (toolBarRefreshAllAction == null)
		  toolBarRefreshAllAction = new SystemTeamViewRefreshAllAction(getShell(), this); 
        toolBarRefreshAllAction.setSelection(selection);
		return toolBarRefreshAllAction;
	}
	/**
	 * Get the New Profile actoin
	 */
	private SystemNewProfileAction getNewProfileAction(IStructuredSelection selection)
	{
		if (newProfileAction == null)
		{
			newProfileAction = new SystemNewProfileAction(getShell(), false);
			newProfileAction.setViewer(getViewer());
		}
		newProfileAction.setSelection(selection);
		return newProfileAction;
	}
	/**
	 * Rather than pre-defined this common action we wait until it is first needed,
	 *  for performance reasons.
	 */    
	protected IAction getDeleteAction(IStructuredSelection selection)
	{
		if (deleteAction == null)
		{
		  	deleteAction = new SystemCommonDeleteAction(getShell(),this);
		  	deleteAction.setViewer(getViewer());
		  	deleteAction.setHelp(SystemPlugin.HELPPREFIX+"actndlpr");
		  	deleteAction.setDialogHelp(SystemPlugin.HELPPREFIX+"ddltprfl");
		  	deleteAction.setPromptLabel(SystemResources.RESID_DELETE_PROFILES_PROMPT);
		}
		deleteAction.setSelection(selection);
		return deleteAction;
	}    
		
	/**
	 * Scrub the popup menu to remove everything but team-related stuff...
	 */
	private void scrubOtherContributions(IMenuManager menuMgr)
	{
	     IContributionItem items[] = menuMgr.getItems();		

	     if (items != null)
	     {
	        //System.out.println("# existing menu items: "+items.length);
	     	for (int idx=0; idx<items.length; idx++)
	     	{
	     		IContributionItem item = items[idx];
	     		//System.out.println("menu item id: " + item.getId());
	     		if (item.getId()!=null)
	     		{
	     			if (!item.getId().equals("team.main") || privateProfileStillExists)
	     			  menuMgr.remove(item);
	     			/*
	     			if (item.getId().startsWith("com.ibm.etools") || 
	     			    item.getId().startsWith("com_ibm_etools") || 
	     			    item.getId().equals("ValidationAction") ||
	     			    item.getId().equals("addJETNature") ||
	     			    item.getId().equals("addFromHistoryAction"))
	     			{
	     			   menuMgr.remove(item);	
	     		    }*/
	     		}
	     	}
	     }
	     //else
	       //System.out.println("existing menu items null");
	}

	public void dispose() 
	{
		SystemPlugin.getTheSystemRegistry().removeSystemModelChangeListener(this);		
		super.dispose();
	}

	/**
	 * Return our viewer.
	 */
	public TreeViewer getViewer() 
	{
		return treeViewer;
	}

	/**
	 * Updates the title text and title tool tip.
	 * Called whenever the input of the viewer changes. 
	 */
	public void updateTitle() 
	{
		Object input = getTreeViewer().getInput();
		String viewName = getConfigurationElement().getAttribute("name");
	    setPartName(getTitle());
		setTitleToolTip("");
	}

	/** 
	 * Determines the input for the viewer. This is needed for the "Open
	 * in New Window" action that sets the input to the workbench page,
	 * and expects the viewers to use it. 
	 */
	protected IAdaptable getInput() 
	{
		if (input == null)
			input = new SystemTeamViewInputProvider();
		return input;
	}

	/**
	 * Adds the actions in this group and its subgroups to the action bars.
	 */
	public void fillActionBars(IActionBars actionBars) 
	{
		IStructuredSelection selection = getStructuredSelection();
		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(),	getPropertyDialogAction(selection));
	    SystemTeamViewRefreshAllAction refreshAllAction = getRefreshAllToolbarAction(selection);
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAllAction);
		
		actionBars.getToolBarManager().add(getNewProfileAction(selection));
		// now add the global Refresh action in the view tool bar.	
		actionBars.getToolBarManager().add(refreshAllAction);	    

		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), getDeleteAction(selection));
		SystemCommonSelectAllAction selAllAction = new SystemCommonSelectAllAction(getShell(), treeViewer, treeViewer);
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selAllAction);

		actionBars.getToolBarManager().add(new Separator());	
		actionBars.getToolBarManager().add(getCollapseAllAction());		

		statusLine = actionBars.getStatusLineManager();
	}
	
	/**
	 * Return the collapseAll action
	 */
	protected SystemCollapseAllAction getCollapseAllAction()
	{
		if (collapseAllAction == null)
		{
			collapseAllAction = new SystemCollapseAllAction(getShell()); 
			collapseAllAction.setSelectionProvider(treeViewer);
			collapseAllAction.setViewer(treeViewer);
			collapseAllAction.setImageDescriptor(getNavigatorImageDescriptor("elcl16/collapseall.gif")); //$NON-NLS-1$
			// DKM - changed this icon from clcl16 since navigator no longer has it
			collapseAllAction.setHoverImageDescriptor(getNavigatorImageDescriptor("elcl16/collapseall.gif")); //$NON-NLS-1$
		}
		return collapseAllAction;
	}

	/**
	 * Updates the actions which were added to the action bars.
	 */
	public void updateActionBars(IStructuredSelection selection) 
	{
		getPropertyDialogAction(selection);
		getRefreshAllToolbarAction(selection);
		getDeleteAction(selection);
	}

	/**
	 * This is the method in your class that will be called when a resource in the 
	 *  RSE model changes. You will be called after the resource is changed.
	 * @see ISystemModelChangeEvent
	 */
    public void systemModelResourceChanged(ISystemModelChangeEvent event)
    {
    	int resourceType = event.getResourceType();
    	boolean testMode = false;
    	if ((event.getEventType() == ISystemModelChangeEvents.SYSTEM_RESOURCE_ALL_RELOADED) || 
    		(resourceType == ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_PROFILE))
    	{
 		  	getTreeViewer().refresh();
 		  	treeViewer.updatePropertySheet();
 		  	if (testMode && (resourceType == ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_PROFILE))
				System.out.println("Profile change event of type: " + event.getEventType());
		}
 		else if (resourceType == ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTER)
 		{
			if (testMode)
				System.out.println("Filter change event of type: " + event.getEventType());
			ISystemFilter filter = (ISystemFilter)event.getResource();
			ISystemFilterPool pool = filter.getParentFilterPool();
			if (pool == null) // maybe for transient filters? 
				return;
			if (isModelObjectExpanded(pool)) // if parent is expanded...
				treeViewer.refresh(pool); // refresh the parent.
 		}
		else if (resourceType == ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_FILTERPOOL)
		{
			if (testMode)
				System.out.println("Filter Pool change event of type: " + event.getEventType());
			ISystemFilterPool pool = (ISystemFilterPool)event.getResource();
			ISystemProfile profile = ((ISubSystemConfiguration)pool.getProvider()).getSystemProfile(pool);
			TreeItem filterCategoryItem = getCategoryNodeTreeItem(profile, SystemTeamViewCategoryNode.MEMENTO_FILTERPOOLS);
			if ((filterCategoryItem!=null) && filterCategoryItem.getExpanded())
				treeViewer.refresh(filterCategoryItem.getData());			
		} 
		else if (resourceType == ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_CONNECTION)
		{
			if (testMode)
				System.out.println("Connection change event of type: " + event.getEventType());
			IHost conn = (IHost)event.getResource();
			ISystemProfile profile = conn.getSystemProfile();
			
			TreeItem connCategoryItem = getCategoryNodeTreeItem(profile, SystemTeamViewCategoryNode.MEMENTO_CONNECTIONS);
			if ((connCategoryItem != null) && connCategoryItem.getExpanded())
			{
				treeViewer.refresh(connCategoryItem.getData());
			}
		}
		else if (resourceType == ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_SUBSYSTEM)
		{
			if (testMode)
				System.out.println("SubSystem change event of type: " + event.getEventType());
		}
    	
// FIXME - user actions separate now
//		else if (resourceType == ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_USERACTION)
//		{
//			if (testMode)
//				System.out.println("User Action change event of type: " + event.getEventType());
//			SystemUDActionElement action = (SystemUDActionElement)event.getResource();
//			ISystemProfile profile = action.getProfile();
//			TreeItem actionCategoryItem = getCategoryNodeTreeItem(profile, SystemTeamViewCategoryNode.MEMENTO_USERACTIONS);
//			if ((actionCategoryItem!=null) && actionCategoryItem.getExpanded())
//			{	
//				// note if the updated action is currently selected, we lose that selection because 
//				//  when we save actions, their wrapper objects are recreated (yes, its bad but who
//				//  has time to fix it?) and so the binary address of the old no longer exists.
//				// To circumvent we need to detect the situation and reselect the new one. But how to tell
//				//  if one action is equal to another? Compare their xml element nodes.
//				ISelection s = getSelection();
//				boolean reselect = false;
//				if (s instanceof StructuredSelection)
//				{
//					Object firstSel = ((StructuredSelection)s).getFirstElement();
//					if (firstSel instanceof SystemUDActionElement)
//						if ( ((SystemUDActionElement)firstSel).getElement() == 
//						 	 action.getElement() )
//						 	 reselect = true;
//				}
//				// the problem we have here is refresh will cause yet another fresh set of SystemUDActionElement objects
//				//  to be created! We really should fix that.
//				
//				//if (event.getEventType() != ISystemModelChangeEvents.SYSTEM_RESOURCE_CHANGED)
//					treeViewer.refresh(actionCategoryItem.getData());
//				// I tried this and update didn't update the label, damn.
//				//else
//				//{
//					//String[] allProps = {IBasicPropertyConstants.P_TEXT,IBasicPropertyConstants.P_IMAGE};
//					//treeViewer.update(actionCategoryItem.getData(), allProps);
//				//}
//				if (reselect)
//					treeViewer.setSelection(new StructuredSelection(action));
//			}			
//		}
		else if (resourceType == ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_NAMEDTYPE)
		{
			if (testMode)
				System.out.println("Named Type change event of type: " + event.getEventType());
		}
    	
// compile actions separate now
//		else if (resourceType == ISystemModelChangeEvents.SYSTEM_RESOURCETYPE_COMPILECMD)
//		{
//			if (testMode)
//				System.out.println("Compile Command change event of type: " + event.getEventType());
//			SystemCompileCommand cmd = (SystemCompileCommand)event.getResource();
//			ISystemProfile profile = cmd.getParentType().getParentProfile().getProfile();
//			TreeItem actionCategoryItem = getCategoryNodeTreeItem(profile, SystemTeamViewCategoryNode.MEMENTO_COMPILECMDS);
//			if ((actionCategoryItem!=null) && actionCategoryItem.getExpanded())
//				treeViewer.refresh(actionCategoryItem.getData());
//		}
    }
    /**
     * Return true if the given profile is expanded
     */
    private boolean isModelObjectExpanded(Object object)
    {
		TreeItem item = treeViewer.findTreeItem(object);
		return ((item != null) && item.getExpanded());
    }	
    /**
     * Find a category node of a particular type, under the node of a given profile
     */
    private TreeItem getCategoryNodeTreeItem(ISystemProfile profile, String mementoKey)
    {
    	TreeItem item = treeViewer.findTreeItem(profile);
    	if ((item==null) || !item.getExpanded() )
    		return null;
    	
		TreeItem[] childItems = item.getItems();
		item = null;
		if (childItems != null)
		{
			boolean found = false;
			for (int idx=0; !found && (idx<childItems.length); idx++)
			{
				if (childItems[idx].getData() instanceof SystemTeamViewCategoryNode)
				{
					SystemTeamViewCategoryNode node = (SystemTeamViewCategoryNode)childItems[idx].getData();
					if ((node!=null) && node.getMementoHandle().equals(mementoKey))
					{
						found = true;
						item = childItems[idx];
					}
				}
			}
		}    	    	
    	return item;    	
    }

    /* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.ISystemDeleteTarget#showDelete()
	 */
	public boolean showDelete()
	{
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.ISystemDeleteTarget#canDelete()
	 */
	public boolean canDelete()
	{
		boolean ok = true;
		IStructuredSelection selection= (IStructuredSelection)getStructuredSelection();		
		Iterator elements= selection.iterator();
		ISystemProfileManager mgr = SystemPlugin.getTheSystemRegistry().getSystemProfileManager();
		int nbrActiveProfiles = mgr.getActiveSystemProfiles().length;
		int activeCount = 0;
		while (ok && elements.hasNext())
		{
			Object currObj = elements.next();
			if (!(currObj instanceof ISystemProfile))
			{
				ok = false;
				//System.out.println("selection: "+currObj.getClass().getName());
			}
			else if (!mgr.isSystemProfileActive(((ISystemProfile)currObj).getName()))
				activeCount++;
		}
		if (ok && (activeCount == nbrActiveProfiles)) // attempting to delete all active profiles?
			ok = false; // don't allow that!
		//System.out.println("Inside canDelete: "+ok);
		return ok;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.ISystemDeleteTarget#doDelete()
	 */
	public boolean doDelete(IProgressMonitor monitor)
	{
		boolean ok = true;
		IStructuredSelection selection= (IStructuredSelection)getStructuredSelection();		
		Iterator elements= selection.iterator();
		Object currObj = null;
		while (ok && elements.hasNext())
		{
			currObj = elements.next();
			try {
			  ok = profileAdapter.doDelete(getShell(), currObj, monitor);
			} catch (Exception exc)
			{
				String msg = "Exception deleting profile "+((ISystemProfile)currObj).getName();
				SystemBasePlugin.logError(msg, exc);
				SystemMessageDialog.displayExceptionMessage(getShell(),exc);
				ok = false;				
			}
		}
		return ok;
	}

	// ------------------------------
	// ISystemRenameTarget methods...
	// ------------------------------
   
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.ISystemRenameTarget#showRename()
	 */
	public boolean showRename()
	{
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.ISystemRenameTarget#canRename()
	 */
	public boolean canRename()
	{
		boolean ok = true;
		IStructuredSelection selection= (IStructuredSelection)getStructuredSelection();		
		Iterator elements= selection.iterator();
		while (ok && elements.hasNext())
		{
			Object currObj = elements.next();
			if (!(currObj instanceof ISystemProfile))
				ok = false;
		}
		//System.out.println("Inside canRename: "+ok);
		return ok;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.ui.ISystemRenameTarget#doRename(java.lang.String[])
	 */
	public boolean doRename(String[] newNames)
	{
		boolean ok = true;
		IStructuredSelection selection= (IStructuredSelection)getStructuredSelection();		
		Iterator elements= selection.iterator();
		Object currObj = null;
		int idx = 0;
		while (ok && elements.hasNext())
		{
			currObj = elements.next();
			try {
		 		profileAdapter.doRename(getShell(), (ISystemProfile)currObj, newNames[idx++]);
			} 
			catch (SystemMessageException exc)
			{
				SystemMessageDialog.displayMessage(getShell(), exc);	 
			  	ok = false;  	     
			}
			catch (Exception exc)
			{
			  	String msg = "Exception renaming profile ";
			  	SystemBasePlugin.logError(msg, exc);
			  	//System.out.println(msg + exc.getMessage() + ": " + exc.getClass().getName());
			  	SystemMessageDialog.displayExceptionMessage(getShell(),exc);
			  	ok = false;
			}
		}
		return ok;
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getNavigatorImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$
		try {
			Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
			URL installURL = bundle.getEntry("");
			URL url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}	
	
	// ----------------	
	// MEMENTO STUFF...
	// ----------------
	
	/**
	 * Method declared on IViewPart.
	 */
	public void saveState(IMemento memento)
	{
		if (treeViewer == null) 
		{
			// part has not been created
			if (fMemento != null) //Keep the old state;
				memento.putMemento(fMemento);
			return;
		}

		// We record the current release for future in case anything significant changes from release to release
		memento.putString(TAG_RELEASE, SystemResources.CURRENT_RELEASE_NAME);
		
		/*
		String inputMemento = memento.getString("factoryID"); // see IWorkbenchWindow ... this is only clue I can figure out!
		if (inputMemento != null)
		{
			saveInputState(memento);
			return;
		}*/
                
		Tree tree = treeViewer.getTree();
		
		// SAVE EXPANSION STATE
		Object expandedElements[]= treeViewer.getVisibleExpandedElements();
		if ( (expandedElements!=null) && (expandedElements.length > 0) )
		{
			IMemento expandedMem= memento.createChild(TAG_EXPANDED);
			for (int i= 0; i < expandedElements.length; i++) 
			{
				Object o = expandedElements[i];
				String mementoHandle = getMementoHandle(o);
				if (mementoHandle != null)
				{
					IMemento elementMem= expandedMem.createChild(TAG_ELEMENT);
					elementMem.putString(TAG_PATH, getMementoHandle(o));
					//System.out.println("Added to saved expansion list: " + getMementoHandle(o));
				}
			}
		}
		
		// SAVE SELECTION STATE
		Object elements[]= ((IStructuredSelection) treeViewer.getSelection()).toArray();
		if ( (elements!=null) && (elements.length > 0) )
		{
			IMemento selectionMem= memento.createChild(TAG_SELECTION);
			for (int i= 0; i < elements.length; i++) 
			{
				Object o= elements[i];
				String mementoHandle = getMementoHandle(o);
				if (mementoHandle != null)
				{
					IMemento elementMem= selectionMem.createChild(TAG_ELEMENT);
					elementMem.putString(TAG_PATH, getMementoHandle(o));
				}
			}
		}

		//save vertical position
		ScrollBar bar= tree.getVerticalBar();
		int position= bar != null ? bar.getSelection() : 0;
		memento.putString(TAG_VERTICAL_POSITION, String.valueOf(position));
		//save horizontal position
		bar= tree.getHorizontalBar();
		position= bar != null ? bar.getSelection() : 0;
		memento.putString(TAG_HORIZONTAL_POSITION, String.valueOf(position));

	}
	/**
	 * Return the memento handle key plus the memento handle for
	 *  each part leading up to the current object.
	 */	
	protected String getMementoHandle(Object o)
	{
		String mementoKey = internalGetMementoHandleKey(o);
		if (mementoKey == null)
			return null;
		StringBuffer idBuffer = new StringBuffer(mementoKey);
		Object[] elementNodes = treeViewer.getElementNodes(o);
		if (elementNodes != null)
		{
		   for (int idx=elementNodes.length-1; idx>=0; idx--)
		   {
		   		o = elementNodes[idx];
		   		idBuffer.append(MEMENTO_DELIM+internalGetMementoHandle(o));
		   }
		}
		//System.out.println("MEMENTO HANDLE: " + idBuffer.toString());
		return idBuffer.toString();
	}
	/**
	 * Encapsulate code to look at object class type and determine what to return for 
	 *  a memento handle key
	 */	
	protected String internalGetMementoHandleKey(Object o)
	{
		ISystemViewElementAdapter adapter = SystemAdapterHelpers.getAdapter(o, treeViewer);
		String handle = null;
		if (adapter != null)
			handle = adapter.getMementoHandleKey(o);
		else if (o instanceof IResource)
		{
			if (o instanceof IProject)
				handle = "Project";
			else if (o instanceof IFolder)
				handle = "Folder";
			else 
				handle = "File";
		}
		else if (o instanceof ISystemProfile)
			handle = "Profile";
		else if (o instanceof SystemTeamViewCategoryNode)
			//handle = "Category";
			handle = null; // decided not to re-expand past profiles 

		return handle;
	}
	/**
	 * Encapsulate code to look at object class type and determine what to return for 
	 *  a memento handle 
	 */	
	protected String internalGetMementoHandle(Object o)
	{
		ISystemViewElementAdapter adapter = SystemAdapterHelpers.getAdapter(o, treeViewer);
		String handle = null;
		if (adapter != null)
			handle = adapter.getMementoHandle(o);
		else if (o instanceof IResource)
			handle = ((IResource)o).getName();
		else if (o instanceof ISystemProfile)
			handle = ((ISystemProfile)o).getName();
		else if (o instanceof SystemTeamViewCategoryNode)
			handle = ((SystemTeamViewCategoryNode)o).getMementoHandle();
		return handle;
	}

	/**
	 * Our own method for restoring state
	 */
	void restoreState(IMemento memento) 
	{
		RestoreStateRunnable restoreAction = new RestoreStateRunnable(memento);
		Display.getDefault().syncExec(restoreAction); 
	}
	
	/**
	 * Inner class for running restore-state in a thread
	 */
	public class RestoreStateRunnable implements Runnable
	{
		private IMemento _memento;
		public RestoreStateRunnable(IMemento memento)
		{
			_memento = memento;
		}

		public void run()
		{
			IMemento memento = _memento;			
			IMemento childMem = null;

			// restore expansion state
			childMem = memento.getChild(TAG_EXPANDED);
			if (childMem != null)
			{
				ArrayList elements = new ArrayList();
				IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
				for (int i = 0; i < elementMem.length; i++)
				{
					Object element = getObjectFromMemento(elementMem[i].getString(TAG_PATH));
					if (element != null)
						elements.add(element);
				}
				// expand objects...
				treeViewer.setExpandedElements(elements.toArray());
			}

			// restoreSelection
			childMem = memento.getChild(TAG_SELECTION);
			if (childMem != null)
			{
				ArrayList list = new ArrayList();
				IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
				for (int i = 0; i < elementMem.length; i++)
				{
					Object element = getObjectFromMemento(elementMem[i].getString(TAG_PATH));
					if (element != null)
						list.add(element);
				}
				treeViewer.setSelection(new StructuredSelection(list));
			}

			Tree tree = treeViewer.getTree();
			//restore vertical position
			ScrollBar bar = tree.getVerticalBar();
			if (bar != null)
			{
				try
				{
					String posStr = memento.getString(TAG_VERTICAL_POSITION);
					if (posStr != null)
					{
						int position;
						position = new Integer(posStr).intValue();
						bar.setSelection(position);
					}
				}
				catch (NumberFormatException e)
				{
				}
			}
			//restore vertical position
			bar = tree.getHorizontalBar();
			if (bar != null)
			{
				try
				{
					String posStr = memento.getString(TAG_HORIZONTAL_POSITION);
					if (posStr != null)
					{
						int position;
						position = new Integer(posStr).intValue();
						bar.setSelection(position);
					}
				}
				catch (NumberFormatException e)
				{
				}
			}
		}
	}		
	/**
	 * Private method to deconstruct an expanded/selected memento into an actual object
	 */
	protected Object getObjectFromMemento(String memento)
	{
		if (memento == null)
		  return null;

		ISystemRegistry sr = SystemPlugin.getTheSystemRegistry();
		ISystemProfile  profile = null;
		IProject       project = null; 
		SystemTeamViewCategoryNode category = null;
		String elementType = "";

		//System.out.println("PARSING MEMENTO: " + memento);
        	
		Vector v = SystemViewPart.tokenize(memento, MEMENTO_DELIM);    	

		// 0. element type
		// 1. RSE Project
		//    2. Profiles
		//       3. Categories

		for (int idx=0; idx<v.size(); idx++)
		{
			String token = (String)v.elementAt(idx);
			switch (idx)
			{
				// type of element
				case 0: elementType = token; break;
				// profile
				case 1: 
					project = SystemResourceManager.getRemoteSystemsProject();
					break;
				case 2: 					
					profile = sr.getSystemProfile(token);
					break;
				case 3: 					
					SystemTeamViewProfileAdapter profileAdapter = SystemPlugin.getDefault().getSystemViewAdapterFactory().getProfileAdapter();
				    category = profileAdapter.restoreCategory(profile, token);
				    //System.out.println("Restored category: "+(category==null?"null":category.getLabel()));					
					break;
			}
		}
    	
		if (elementType.equals("Project"))
		{
			//System.out.println("...PARSED INTO A PROJECT: " + project.getName());
		  	return project;
		}
		else if (elementType.equals(ISystemMementoConstants.MEMENTO_KEY_PROFILE))
		{
			//System.out.println("...PARSED INTO A PROFILE: " + profile.getName());
			return profile;
		}
		if (elementType.equals("Category"))
		{
			//System.out.println("...PARSED INTO A CATEGORY: " + category.getLabel());
			return category;
		}
		else
		{
			//System.out.println("...UNKNOWN MEMENTO ");			
		}
		return null;
	}

	// -----------------------------
    // ISelectionListener methods...
    // -----------------------------
    	
	/**
	 * Handles selection changed in viewer.
	 * Updates global actions.
	 * Links to editor (if option enabled)
	 */
	public void selectionChanged(SelectionChangedEvent event)
	{
		IStructuredSelection sel = (IStructuredSelection)event.getSelection();		
		updateActionBars(sel);
		Object firstSelection = sel.getFirstElement();
		if (firstSelection == null)
		  return;
		ISystemViewElementAdapter adapter = SystemAdapterHelpers.getAdapter(firstSelection);
		if (adapter != null)
		{
		   setMessage(adapter.getStatusLineText(firstSelection));
		   //if ((mouseButtonPressed == LEFT_BUTTON) && (!expandingTreeOnly))   //d40615
			  //adapter.selectionChanged(firstSelection);		//d40615  
		}  
		else
		   setMessage(internalGetMementoHandle(firstSelection));

		if (newProfileAction != null)
		  newProfileAction.refreshEnablement(); // not selection related, but we have no other trigger	
	}

	// -------------------------------
	// ISystemMessageLine interface...
	// -------------------------------
	/**
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage()
	{
		errorMessage = null;
		sysErrorMessage = null;
		if (statusLine != null)
		  statusLine.setErrorMessage(errorMessage);
	}
	/**
	 * Clears the currently displayed message.
	 */
	public void clearMessage()
	{
		message = null;
		if (statusLine != null)
		  statusLine.setMessage(message);
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}
	/**
	 * Get the currently displayed message.
	 * @return The message. If no message is displayed <code>null<code> is returned.
	 */
	public String getMessage()
	{
		return message;
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message)
	{
		this.errorMessage = message;
		if (statusLine != null)
		  statusLine.setErrorMessage(message);
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public SystemMessage getSystemErrorMessage() 
	{
		return sysErrorMessage;
	}
	
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */	
	public void setErrorMessage(SystemMessage message)
	{
		sysErrorMessage = message;
		setErrorMessage(message.getLevelOneText());
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */	
	public void setErrorMessage(Throwable exc)
	{
		setErrorMessage(exc.getMessage());
	}
		
	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String message)
	{
		this.message = message;
		if (statusLine != null)
		  statusLine.setMessage(message);
	}
	/** 
	 *If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage message)
	{
		setMessage(message.getLevelOneText());
	}
}