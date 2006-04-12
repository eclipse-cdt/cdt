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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.rse.core.SystemAdapterHelpers;
import org.eclipse.rse.core.SystemElapsedTimer;
import org.eclipse.rse.core.SystemPopupMenuActionContributorManager;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterContainer;
import org.eclipse.rse.filters.ISystemFilterContainerReference;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterString;
import org.eclipse.rse.filters.ISystemFilterStringReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.rse.model.ISystemMessageObject;
import org.eclipse.rse.model.ISystemPromptableObject;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemRemoteChangeEvent;
import org.eclipse.rse.model.ISystemRemoteChangeEvents;
import org.eclipse.rse.model.ISystemRemoteChangeListener;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.model.ISystemResourceSet;
import org.eclipse.rse.model.SystemRemoteElementResourceSet;
import org.eclipse.rse.model.SystemRemoteResourceSet;
import org.eclipse.rse.references.ISystemBaseReferencingObject;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemDeleteTarget;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemRenameTarget;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemCascadingGoToAction;
import org.eclipse.rse.ui.actions.SystemCollapseAction;
import org.eclipse.rse.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.ui.actions.SystemCommonRenameAction;
import org.eclipse.rse.ui.actions.SystemCommonSelectAllAction;
import org.eclipse.rse.ui.actions.SystemExpandAction;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.rse.ui.actions.SystemOpenExplorerPerspectiveAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.actions.SystemRemotePropertiesAction;
import org.eclipse.rse.ui.actions.SystemShowInMonitorAction;
import org.eclipse.rse.ui.actions.SystemShowInTableAction;
import org.eclipse.rse.ui.actions.SystemSubMenuManager;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.views.framelist.GoIntoAction;


/**
 * This subclass of the standard JFace tree viewer is used to show a tree
 * view of connections to remote systems, which can be manipulated and expanded
 * to access remote objects in the remote system.
 */
public class SystemView extends TreeViewer implements  ISystemTree,
                                                      ISystemResourceChangeListener,
                                                      ISystemRemoteChangeListener,
					                                  IMenuListener,
					                                  //MenuListener,
					                                  //IDoubleClickListener,
					                                  //ArmListener,
					                                  ISelectionChangedListener,
					                                  ISelectionProvider,
					                                  ITreeViewerListener,
					                                  ISystemResourceChangeEvents,
													  ISystemDeleteTarget,
													  ISystemRenameTarget,
													  ISystemSelectAllTarget
													  //, IWireEventTarget
{
	
	protected Shell                     shell; // shell hosting this viewer
	protected ISystemViewInputProvider  inputProvider; // who is supplying our tree root elements?
	protected ISystemViewInputProvider  previousInputProvider; // who is supplying our tree root elements?
	protected Object                    previousInput;
	protected IHost          previousInputConnection;
	// protected actions
	protected SystemNewConnectionAction newConnectionAction;
	protected SystemRefreshAction       refreshAction;
	protected PropertyDialogAction      propertyDialogAction;
	protected SystemRemotePropertiesAction  remotePropertyDialogAction;
	protected SystemCollapseAction      collapseAction; // defect 41203
	protected SystemExpandAction        expandAction;   // defect 41203
	protected SystemOpenExplorerPerspectiveAction openToPerspectiveAction;
	
	protected SystemShowInTableAction   showInTableAction;
	protected SystemShowInMonitorAction showInMonitorAction;
	protected GoIntoAction              goIntoAction;
    protected SystemCascadingGoToAction gotoActions;
	// global actions
	// Note the Edit menu actions are set in SystemViewPart. Here we use these
	//   actions from our own popup menu actions.
	protected SystemCommonDeleteAction    deleteAction;        // for global delete menu item	
	protected SystemCommonRenameAction    renameAction;        // for common rename menu item	
	protected SystemCommonSelectAllAction selectAllAction;     // for common Ctrl+A select-all
	// special flags needed when building popup menu, set after examining selections
	protected boolean selectionShowRefreshAction;
	protected boolean selectionShowOpenViewActions;
	protected boolean selectionShowGenericShowInTableAction;
	protected boolean selectionShowDeleteAction;
	protected boolean selectionShowRenameAction;
	protected boolean selectionEnableDeleteAction;
	protected boolean selectionEnableRenameAction;
	protected boolean selectionIsRemoteObject;
	protected boolean selectionHasAncestorRelation;
	protected boolean selectionFlagsUpdated = false;
	// misc
    protected MenuManager menuMgr;
	protected boolean     showActions = true;
	protected boolean     hardCodedConnectionSelected = false;
	protected boolean     mixedSelection = false;
	protected boolean     specialMode = false;
	protected boolean     menuListenerAdded = false;
	protected boolean     fromSystemViewPart = false;
	protected boolean     areAnyRemote = false;
	protected boolean     enabledMode = true;
	protected Widget      previousItem = null;
	protected int         searchDepth = 0;
	//protected Vector      remoteItemsToSkip = null;
	protected Cursor      busyCursor;
	protected TreeItem    inputTreeItem = null;
	protected static final int SEARCH_INFINITE = 10; // that's far enough down to search!
	public boolean debug = false;
	public boolean debugRemote = false;
	public boolean debugProperties = debug && false;
	public boolean doTimings = false;
	public SystemElapsedTimer elapsedTime = new SystemElapsedTimer();
	// for support of Expand To actions ... transient filters really.
	// we need to record these per tree node they are applied to.
	protected Hashtable expandToFiltersByObject; // most efficient way to find these is by binary object
	protected Hashtable expandToFiltersByTreePath; // however, we lose that after a refresh so we also record by tree path

	// message line
	protected ISystemMessageLine messageLine = null;
	// button pressed
    protected static final int LEFT_BUTTON = 1;
    protected int mouseButtonPressed = LEFT_BUTTON;    //d40615
    protected boolean expandingTreeOnly = false;      //d40615
    protected ViewerFilter[] initViewerFilters = null;
    
	protected List _setList;
        
	/**
	 * Constructor
	 * @param shell The shell hosting this tree viewer widget
	 * @param parent The composite widget into which to place this widget
	 * @param inputProvider The input object which will supply the initial root objects in the tree.
	 *                      Can be null initially, but be sure to call #setInputProvider(ISystemViewInputProvider) later.
	 * @param msgLine Where to display messages and tooltip text
	 */
	public SystemView(Shell shell, Composite parent, ISystemViewInputProvider inputProvider, ISystemMessageLine msgLine)
	{
		super(parent);
		this.shell = shell;
		this.inputProvider = inputProvider;
		this.inputProvider.setShell(shell); 	// DY:  defect 44544
		this.messageLine = msgLine;
		init();
	}	
	/**
	 * Constructor to use when you want to specify styles for the tree widget
	 * @param shell The shell hosting this tree viewer widget
	 * @param parent The composite widget into which to place this widget
	 * @param style The style to give the tree widget
	 * @param inputProvider The input object which will supply the initial root objects in the tree.
	 *                      Can be null initially, but be sure to call #setInputProvider(ISystemViewInputProvider) later.
	 * @param msgLine Where to display messages and tooltip text
	 */
	public SystemView(Shell shell, Composite parent, int style, ISystemViewInputProvider inputProvider, ISystemMessageLine msgLine)
	{
		super(parent, style);
		this.shell = shell;
		this.inputProvider = inputProvider;
		this.inputProvider.setShell(shell); 	// DY:  defect 44544
		this.messageLine = msgLine;
		init();
	}
	
	/**
	 * Constructor to use when you want to specify styles for the tree widget
	 * @param shell The shell hosting this tree viewer widget
	 * @param parent The composite widget into which to place this widget
	 * @param style The style to give the tree widget
	 * @param inputProvider The input object which will supply the initial root objects in the tree.
	 *                      Can be null initially, but be sure to call #setInputProvider(ISystemViewInputProvider) later.
	 * @param msgLine Where to display messages and tooltip text
	 * @param initViewerFilters the initial viewer filters to apply.
	 */
	public SystemView(Shell shell, Composite parent, int style, ISystemViewInputProvider inputProvider,
						ISystemMessageLine msgLine, ViewerFilter[] initViewerFilters)
	{
		super(parent, style);
		this.shell = shell;
		this.inputProvider = inputProvider;
		this.inputProvider.setShell(shell); 	// DY:  defect 44544
		this.messageLine = msgLine;
		this.initViewerFilters = initViewerFilters;
		init();
	}
	
	/**
	 * Constructor to use when you create your own tree widget. 
	 * @param shell The shell hosting this tree viewer widget
	 * @param tree The Tree widget you created.
	 * @param inputProvider The input object which will supply the initial root objects in the tree.
	 *                      Can be null initially, but be sure to call #setInputProvider(ISystemViewInputProvider) later.
	 * @param msgLine Where to display messages and tooltip text
	 */
	public SystemView(Shell shell, Tree tree, ISystemViewInputProvider inputProvider, ISystemMessageLine msgLine)
	{
		super(tree);
		this.shell = shell;
		this.inputProvider = inputProvider;
		this.inputProvider.setShell(shell); 	// DY:  defect 44544
		this.messageLine = msgLine;
		init();
	}	
	
	/**
	 * Set the input provider. Sometimes this is delayed, or can change.
	 */
	public void setInputProvider(ISystemViewInputProvider inputProvider)
	{
		this.inputProvider = inputProvider;		
		inputProvider.setViewer(this);
		inputProvider.setShell(getShell()); // DY:  Defect 44544, shell was not being set for Test dialogs, when they
											// tried to connect there was not shell for the password prompt
											// and an error message (expand failed) occured.
		setInput(inputProvider);
	}
	
	/**
	 * Get the SystemViewPart that encapsulates us.
	 * Will be null unless fromSystemViewPart is true.
	 */
	public SystemViewPart getSystemViewPart()
	{
		if (fromSystemViewPart)
		  return ((SystemViewPart)messageLine);
		else
		  return null;
	}
	
	/**
	 * Get the workbench window containing this view part. Will only be non-null for the explorer view part,
	 * not when used within, say, a dialog
	 */
	protected IWorkbenchWindow getWorkbenchWindow()
	{
		if (fromSystemViewPart)
		  return getSystemViewPart().getSite().getWorkbenchWindow();
		else
		  return null;
	}
	/**
	 * Get the workbench part containing this view. Will only be non-null for the explorer view part,
	 * not when used within, say, a dialog
	 */
	protected IWorkbenchPart getWorkbenchPart()
	{
		return getSystemViewPart();
	}
	
	/**
	 * Disable/Enable the viewer. We do this by blocking keystrokes without visually greying out
	 */
	public void setEnabled(boolean enabled)
	{
        enabledMode = enabled;
	}

	/**
	 * Sets the label and content provider for the system view.
	 * This can be called externally if a custom RSE label and content provider is desired
	 * @param lcProvider the provider
	 */
	public void setLabelAndContentProvider(SystemViewLabelAndContentProvider lcProvider)
	{
		setLabelProvider(new DecoratingLabelProvider(lcProvider, RSEUIPlugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));
		setContentProvider(lcProvider);
	}
	
	protected void init()
	{
		_setList = new ArrayList();
	    busyCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        setUseHashlookup(true); // new for our 2nd release. Attempt to fix 38 minutes to refresh for 15K elements
		
        // set content provider
        SystemViewLabelAndContentProvider lcProvider = new SystemViewLabelAndContentProvider();
        setLabelAndContentProvider(lcProvider);        

		// set initial viewer filters
		if (initViewerFilters != null) {
			
			for (int i = 0; i < initViewerFilters.length; i++) {
				addFilter(initViewerFilters[i]);
			}
		}
		
   		fromSystemViewPart = ((messageLine != null) && (messageLine instanceof SystemViewPart));
		
   		// set the tree's input. Provides initial roots.	
		if (inputProvider != null)
		{
		  inputProvider.setViewer(this);
		  setInput(inputProvider);
		  if (fromSystemViewPart)
		  {
    	    previousInputConnection = getInputConnection(getWorkbenchPart().getSite().getPage().getInput());
		  }
		}
		//addDoubleClickListener(this);
		addSelectionChangedListener(this);
		addTreeListener(this);
        // ----------------------------------------
		// register with system registry for events
        // ----------------------------------------
		RSEUIPlugin.getTheSystemRegistry().addSystemResourceChangeListener(this);
		RSEUIPlugin.getTheSystemRegistry().addSystemRemoteChangeListener(this);
		// -----------------------------
		// Enable right-click popup menu
		// -----------------------------
		menuMgr = new MenuManager("#PopupMenu");
	    menuMgr.setRemoveAllWhenShown(true);
	    menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(getTree());
		getTree().setMenu(menu);		
		// -------------------------------------------
		// Enable specific keys: dbl-click, Delete, F5
		// -------------------------------------------		
		addDoubleClickListener(new IDoubleClickListener() 
		{
			public void doubleClick(DoubleClickEvent event) 
			{
				handleDoubleClick(event);
			}
		});
		getControl().addKeyListener(new KeyAdapter() 
		{
			public void keyPressed(KeyEvent e) 
			{
				handleKeyPressed(e);
			}
		});		
		getControl().addMouseListener(new MouseAdapter() 
		{
		   public void mouseDown(MouseEvent e) 
		   {
		   	    mouseButtonPressed =  e.button;   		  //d40615 	    
				if (!enabledMode)
				{
				   //e.doit = false;
				   return;
				}
		   }
		});		

		initRefreshKey();
		
		// initialize drag and drop
		initDragAndDrop();
	}
	
	/**
	 * Create the KeyListener for doing the refresh on the viewer.
	 */
	protected void initRefreshKey() 
	{
		/* DKM - no need for explicit key listener since we
		 * have global action
		getControl().addKeyListener(new KeyAdapter() 
		{
			public void keyReleased(KeyEvent event) 
			{
				if (!enabledMode)
				  return;
				if (event.keyCode == SWT.F5) 
				{
					//if (debug)
					//  System.out.println("F5 pressed");
					refreshAll();
				}
			}
		});
		*/
	}

	/**
	 * Handles double clicks in viewer.
	 * Opens editor if file double-clicked.
	 */
	protected void handleDoubleClick(DoubleClickEvent event) 
	{
	    if (!enabledMode)
	    {
	      //event.doit = false;
	      return;
	    }
		IStructuredSelection s= (IStructuredSelection) event.getSelection();
		Object element= s.getFirstElement();
		if (element == null)
		  return;
		ISystemViewElementAdapter adapter = getAdapter(element);
		boolean alreadyHandled = false;
		if (adapter != null) 
		  alreadyHandled = adapter.handleDoubleClick(element);
		if (!alreadyHandled && isExpandable(element)) 
		{
			boolean expandedState = getExpandedState(element);
		    setExpandedState(element, !expandedState);
			// DY:  fire collapse / expand event
			if (expandedState) {
				fireTreeCollapsed(new TreeExpansionEvent(this, element));
			} else {
				fireTreeExpanded(new TreeExpansionEvent(this, element));
			}
			return;
		}
	}
	/**
 	 * Handles key events in viewer.
 	 */
	void handleKeyPressed(KeyEvent event) 
	{
		if ((event.character == SWT.DEL) && (event.stateMask == 0) && (((IStructuredSelection)getSelection()).size()>0) )
        {	
          scanSelections("handleKeyPressed");    
      	/* DKM - 53694
		  if (showDelete() && canDelete())
		  {
		  
            SystemCommonDeleteAction dltAction = (SystemCommonDeleteAction)getDeleteAction();
		    dltAction.setShell(getShell());
	        dltAction.setSelection(getSelection());
	        dltAction.setViewer(this);
		    dltAction.run();
		    
		  }
		  */
		}
		else if ((event.character == '-') && (event.stateMask == SWT.CTRL) )
		{
			collapseAll();
		}
		else if ((event.character == 1) && // for some reason Ctrl+A comes in as Ctrl plus the number 1!
		         (event.stateMask == SWT.CTRL) && !fromSystemViewPart)
		{
			//System.out.println("Inside Ctrl+A processing");
			if (enableSelectAll(null))
			  doSelectAll(null);
		}
		else if ((event.character == '-') && (((IStructuredSelection)getSelection()).size()>0) )
		{
			//System.out.println("Inside Ctrl+- processing");
			collapseSelected();
		}
		else if ((event.character == '+') && (((IStructuredSelection)getSelection()).size()>0) )
		{
			//System.out.println("Inside Ctrl++ processing");
			expandSelected();
		}

	}
	
	/**
	 * Handles a collapse-selected request
	 */
	public void collapseSelected()
	{		
        TreeItem[] selectedItems = ((Tree)getControl()).getSelection();		
        if ((selectedItems != null) && (selectedItems.length>0))
        {
        	for (int idx=0; idx<selectedItems.length; idx++)
        	   selectedItems[idx].setExpanded(false);
        }
	}
	/**
	 * Handles an expand-selected request
	 */
	public void expandSelected()
	{		
        TreeItem[] selectedItems = ((Tree)getControl()).getSelection();		
        if ((selectedItems != null) && (selectedItems.length>0))
        {
        	for (int idx=0; idx<selectedItems.length; idx++)
        	{
        	   if (!selectedItems[idx].getExpanded())
        	   {
        	   	  createChildren(selectedItems[idx]);        	   	  
        	   }
        	   selectedItems[idx].setExpanded(true);
        	}
        }
	}
	
	/**
	 * Display a message/status on the message/status line
	 */
	public void displayMessage(String msg)
	{
		if (messageLine != null)
		  messageLine.setMessage(msg);
	}
	/**
	 * Clear message/status shown on the message/status line
	 */
	public void clearMessage()
	{
		if (messageLine != null)
		  messageLine.clearMessage();
	}
	
	/**
	 * Turn off right-click actions
	 */
	public void setShowActions(boolean show)
	{
		this.showActions = show;
	}
	
	/**
	 * Return the input provider
	 */
	public ISystemViewInputProvider getInputProvider()
	{
		inputProvider.setViewer(this); // just in case. Added by Phil in V5.0
		inputProvider.setShell(getShell()); // just in case. Added by Phil
		return inputProvider;
	}
	/**
	 * Return the popup menu for the tree
	 */
	public Menu getContextMenu()
	{
		return getTree().getMenu();
	}
	/**
	 * Return the popup menu for the tree
	 */
	public MenuManager getContextMenuManager()
	{
		return menuMgr;
	}

    /**
     * Rather than pre-defining this common action we wait until it is first needed,
     *  for performance reasons.
     */    
    public IAction getNewConnectionAction()
    {
    	if (newConnectionAction == null)
    	  newConnectionAction = new SystemNewConnectionAction(getShell(), true, this); // true=>from popup menu
    	return newConnectionAction;
    }
    /**
     * Return the refresh action
     */    
    public IAction getRefreshAction()
    {
    	if (refreshAction == null)
    	  refreshAction = new SystemRefreshAction(getShell());
    	return refreshAction;
    }
    /**
     * Return the collapse action
     */    
    public IAction getCollapseAction()
    {
    	if (collapseAction == null)
    	  collapseAction = new SystemCollapseAction(getShell());
    	return collapseAction;
    }
    /**
     * Return the expand action
     */    
    public IAction getExpandAction()
    {
    	if (expandAction == null)
    	  expandAction = new SystemExpandAction(getShell());
    	return expandAction;
    }

    /**
     * Rather than pre-defining this common action we wait until it is first needed,
     *  for performance reasons.
     */    
    public PropertyDialogAction getPropertyDialogAction()
    {
    	if (propertyDialogAction == null)
    	{
    	  propertyDialogAction = new PropertyDialogAction(new SameShellProvider(getShell()),this);
    	  //propertyDialogAction.setToolTipText(" "); 
    	}
    	propertyDialogAction.selectionChanged(getSelection());
    	return propertyDialogAction;
    }
    /**
     * Rather than pre-defining this common action we wait until it is first needed,
     *  for performance reasons.
     */    
    public SystemRemotePropertiesAction getRemotePropertyDialogAction()
    {
    	if (remotePropertyDialogAction == null)
    	{
    	  remotePropertyDialogAction = new SystemRemotePropertiesAction(getShell());
    	}
    	remotePropertyDialogAction.setSelection(getSelection());
    	return remotePropertyDialogAction;
    }
    /**
     * Return the select All action
     */    
    public IAction getSelectAllAction()
    {
    	if (selectAllAction == null)
    	  selectAllAction = new SystemCommonSelectAllAction(getShell(),this,this);
    	return selectAllAction;
    }
    
    
    /**
     * Rather than pre-defined this common action we wait until it is first needed,
     *  for performance reasons.
     */    
    public IAction getRenameAction()
    {
    	if (renameAction == null)
    	  renameAction = new SystemCommonRenameAction(getShell(),this);
    	return renameAction;
    }    
    /**
     * Rather than pre-defined this common action we wait until it is first needed,
     *  for performance reasons.
     */    
    public IAction getDeleteAction()
    {
    	if (deleteAction == null)
    	  deleteAction = new SystemCommonDeleteAction(getShell(),this);
    	return deleteAction;
    }    
    /*
     * Get the common "Open to->" action for opening a new Remote Systems Explorer view,
     *  scoped to the currently selected object.
     *
    protected SystemCascadingOpenToAction getOpenToAction()
    {
    	if (openToAction == null)
    	  openToAction = new SystemCascadingOpenToAction(getShell(),getWorkbenchWindow());
    	return openToAction;
    } NOT USED YET */     

    /**
     * Get the common "Open to->" action for opening a new Remote Systems Explorer view,
     *  scoped to the currently selected object.
     */    
    public SystemOpenExplorerPerspectiveAction getOpenToPerspectiveAction()
    {
    	if (openToPerspectiveAction == null)
    	  openToPerspectiveAction = new SystemOpenExplorerPerspectiveAction(getShell(),getWorkbenchWindow());
    	return openToPerspectiveAction;
    }    
    
    public SystemShowInTableAction getShowInTableAction()
    {
    	if (showInTableAction == null)
    		showInTableAction = new SystemShowInTableAction(getShell());
    	return showInTableAction;
    }
    
  public SystemShowInMonitorAction getShowInMonitorAction()
    {
    	if (showInMonitorAction == null)
    		showInMonitorAction = new SystemShowInMonitorAction(getShell());
    	return showInMonitorAction;
    }
    
    /**
     * Get the common "Go Into" action for drilling down in the Remote Systems Explorer view,
     *  scoped to the currently selected object.
     */    
    public GoIntoAction getGoIntoAction()
    {
    	if (goIntoAction == null)
    	{
    	  goIntoAction = new GoIntoAction(getSystemViewPart().getFrameList());
    	  goIntoAction.setText(SystemResources.ACTION_CASCADING_GOINTO_LABEL);
    	  goIntoAction.setToolTipText(SystemResources.ACTION_CASCADING_GOINTO_TOOLTIP);
    	}
    	return goIntoAction;
    }    
    /**
     * Get the common "Go To->" cascading menu action for navigating the frame list.
     */    
    public SystemCascadingGoToAction getGoToActions()
    {
    	if (gotoActions == null)
    	  gotoActions = new SystemCascadingGoToAction(getShell(), getSystemViewPart());
    	return gotoActions;
    }    
  		
	/*
	 * Helper method to collapse a node in the tree, and then re-expand it one element deep.
	 * Called by com.ibm.etools.systems.SystemBaseElement.
	 */
	//public void refreshElementChildren(ISystemBaseElement element)
	//{
	//	boolean expanded = isElementExpanded(element);
    //  collapseElement(element, true); // collapse and delete children
    //  if (expanded)
    //    expandToLevel(element, 1); // re-expand
    //}
	/**
	 * Helper method to collapse a node in the tree.
	 * Called when a currently expanded subsystem is disconnected.
	 * @param true if children should be deleted from memory so re-expand forces refresh.
	 */
	public void collapseNode(Object element, boolean forceRefresh)
	{
		// First, collapse this element and all its children.
		collapseToLevel(element,ALL_LEVELS);

		// Collapsed just changes expanded state but leaves existing child
		//  widgets in memory so they are re-shown on next expansion.
		// To force the next expand to re-get the children, we have to delete the
		//  children.
		if (forceRefresh)
		{
		  refresh(element); // look at AbstractTreeViewer.updateChildren which this
		                    // will call. If the element is collapsed (which we just
		                    // did) then its children are simply disposed of, and
		                    // not re-queried. Just what we want!
		}
	}

	/**
	 * This is method is called to populate the popup menu
	 */
	public void fillContextMenu(IMenuManager menu)
	{
		if (!showActions)
		  return;
		//SystemViewPlugin.getDefault().logMessage("inside fillContextMenu");
		IStructuredSelection selection= (IStructuredSelection)getSelection();
		boolean allSelectionsFromSameParent = true;
		int selectionCount = selection.size();
		
		if (selectionCount == 0) // nothing selected
		{
		  menu.add(getNewConnectionAction());
		}
		else
		{
		   if (selectionCount > 1)
		   {
		   	 allSelectionsFromSameParent = sameParent();
		     if (!allSelectionsFromSameParent)
		     {
		     	if (selectionHasAncestryRelationship())
		     	{
		     		// don't show the menu because actions with
		     		//  multiple select on objects that are ancestors 
		     		//  of each other is problematic
					// still create the standard groups
					createStandardGroups(menu);
		     		return;
		     	}
		     }		       
		   }
		  // Partition into groups...
          createStandardGroups(menu);

          // PRESET INSTANCE VARIABLES ABOUT WHAT COMMON ACTIONS ARE TO BE SHOWN...
          // PERFORMANCE TWEAK: OUR GLOBAL DELETE ACTION LISTENS FOR SELECTION CHANGES, AND
          //  WHEN THAT CHANGES, WILL CALL CANDELETE() HERE. THAT IN TURN WILL CALL SCANSELECTIONS.
          //  THIS MEANS SCAN SELECTIONS GETS CALL TWICE ON MOST RIGHT CLICK ACTIONS.
          if (!selectionFlagsUpdated) // might already be called by the global delete action wh
            scanSelections("fillContextMenu");
          
		  // ADD COMMON ACTIONS...

		  // COMMON REFRESH ACTION...
   	      if (showRefresh())
   	      {
		    menu.appendToGroup(ISystemContextMenuConstants.GROUP_BUILD, getRefreshAction());
		    menu.appendToGroup(ISystemContextMenuConstants.GROUP_BUILD, getExpandAction());  // defect 41203
		    menu.appendToGroup(ISystemContextMenuConstants.GROUP_BUILD, getCollapseAction());  // defect 41203
   	      }
		  
   	      // COMMON RENAME ACTION...
   	      if (showRename())
   	      {
   	        menu.appendToGroup(ISystemContextMenuConstants.GROUP_REORGANIZE, getRenameAction());
   	     ((ISystemAction)getRenameAction()).setInputs(getShell(), this, selection);
   	      }

          // ADAPTER SPECIFIC ACTIONS   	      
          SystemMenuManager ourMenu = new SystemMenuManager(menu);

		  // yantzi:artemis 6.0 (defect 53970), do not show adapter specific actions when 
		  // there is not a common adapter for all selected elements (i.e. there are 2 or 
		  // more selected elements that have different adapters
		  Iterator elements= selection.iterator();
	      //Hashtable adapters = new Hashtable();
		  ISystemViewElementAdapter adapter = null;
		  boolean skipAdapterActions = false;
	      		    		  
		  while (elements.hasNext() && !skipAdapterActions)
		  {
			Object element= elements.next();
			if (adapter == null)
			{
				adapter = getAdapter(element);
			}
			else if (adapter != getAdapter(element))
			{
				// selected elements have different adapters
				skipAdapterActions = true; 
			}		    
		    //if (adapter != null)
		    //	adapters.put(adapter,element); // want only unique adapters
		  }
		  
		  //Enumeration uniqueAdapters = adapters.keys();
		  if (!skipAdapterActions && adapter != null)
		  {
			  Shell shell = getShell();

			  //while (uniqueAdapters.hasMoreElements())
			  //{
			  //	 ISystemViewElementAdapter nextAdapter = (ISystemViewElementAdapter)uniqueAdapters.nextElement();
			     adapter.addActions(ourMenu,selection,shell,ISystemContextMenuConstants.GROUP_ADAPTERS);
			     if (adapter instanceof AbstractSystemViewAdapter)
			     {
		
						AbstractSystemViewAdapter aVA = (AbstractSystemViewAdapter)adapter;
						// add remote actions
						aVA.addCommonRemoteActions(ourMenu, selection, shell, ISystemContextMenuConstants.GROUP_ADAPTERS);
						
						// add dynamic menu popups
						aVA.addDynamicPopupMenuActions(ourMenu, selection, shell,  ISystemContextMenuConstants.GROUP_ADDITIONS);
			     }
			  //}
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
	      	     item.setInputs(getShell(), this, selection);
	      	   } catch (Exception e)
	      	   {
	      	   	 RSEUIPlugin.logError("Error configuring action " + item.getClass().getName(),e);
	      	   }
	      	 }
	      	 else if (items[idx] instanceof SystemSubMenuManager)
	      	 {
	      	   SystemSubMenuManager item = (SystemSubMenuManager)items[idx];	
	      	   item.setInputs(getShell(), this, selection);
	      	 }
	      }

		  // COMMON DELETE ACTION...
   	      if (showDelete())
   	      {
   	        //menu.add(getDeleteAction());
   	        menu.appendToGroup(ISystemContextMenuConstants.GROUP_REORGANIZE, getDeleteAction());
   	        ((ISystemAction)getDeleteAction()).setInputs(getShell(), this, selection);
   	        menu.add(new Separator());
   	      }
   	      
   	      
  	     
	      // PROPERTIES ACTION...
		  // This is supplied by the system, so we pretty much get it for free. It finds the
		  // registered propertyPages extension points registered for the selected object's class type.
          //propertyDialogAction.selectionChanged(selection);		  
       
          
          if (!selectionIsRemoteObject) // is not a remote object
          {
            PropertyDialogAction pdAction = getPropertyDialogAction();           
            if (pdAction.isApplicableForSelection())
		      menu.appendToGroup(ISystemContextMenuConstants.GROUP_PROPERTIES, pdAction);
		    // GO INTO ACTION...
		    // OPEN IN NEW WINDOW ACTION...
		    if (fromSystemViewPart && showOpenViewActions())
		    {
		      GoIntoAction goIntoAction = getGoIntoAction();
		      goIntoAction.setEnabled(selection.size()==1);
		      menu.appendToGroup(ISystemContextMenuConstants.GROUP_GOTO, goIntoAction);

		      SystemOpenExplorerPerspectiveAction openToPerspectiveAction = getOpenToPerspectiveAction();
		      openToPerspectiveAction.setSelection(selection);
		      menu.appendToGroup(openToPerspectiveAction.getContextMenuGroup(), openToPerspectiveAction);
		      
		      if (showGenericShowInTableAction())
		      {
			      SystemShowInTableAction showInTableAction = getShowInTableAction();
			      showInTableAction.setSelection(selection);
			      menu.appendToGroup(openToPerspectiveAction.getContextMenuGroup(), showInTableAction);			      		      

			      SystemShowInMonitorAction showInMonitorAction = getShowInMonitorAction();
			      showInMonitorAction.setSelection(selection);
			      menu.appendToGroup(openToPerspectiveAction.getContextMenuGroup(), showInMonitorAction);			      		      

		      }
		    }
          }
          else // is a remote object
          {
            //Object firstSelection = selection.getFirstElement();
            //ISystemRemoteElementAdapter remoteAdapter = getRemoteAdapter(firstSelection);
          	//logMyDebugMessage(this.getClass().getName(), ": there is a remote adapter");
          	SystemRemotePropertiesAction pdAction = getRemotePropertyDialogAction();
          	if (pdAction.isApplicableForSelection())
          	  menu.appendToGroup(ISystemContextMenuConstants.GROUP_PROPERTIES, pdAction);
          	//else
          	  //logMyDebugMessage(this.getClass().getName(), ": but it is not applicable for selection");          	
          	// --------------------------------------------------------------------------------------------------------------------
          	// look for and add any popup menu actions registered via our org.eclipse.rse.core.popupMenus extension point...
          	// --------------------------------------------------------------------------------------------------------------------
		    if (fromSystemViewPart) // these require an IWorkbenchPart as a parameter, so we can't support them from within dialogs
		      addObjectActions(ourMenu);         	
          }
		  // GO TO CASCADING ACTIONS...
		  if (fromSystemViewPart && (selectionIsRemoteObject || showOpenViewActions()))
		  {		  	  
		      SystemCascadingGoToAction gotoActions = getGoToActions();
		      gotoActions.setSelection(selection);
		      menu.appendToGroup(gotoActions.getContextMenuGroup(), gotoActions.getSubMenu());
          }
		}
	}
	/**
	 * Contributes popup menu actions and submenus registered for the object type(s) in the current selection.
	 * Patterned after addObjectActions in PopupMenuExtender class supplied by Eclipse.
	 */
	protected void addObjectActions(SystemMenuManager menu) 
	{
	     if (SystemPopupMenuActionContributorManager.getManager().contributeObjectActions(getWorkbenchPart(), menu, this, null))
		 {
	       //menu.add(new Separator());
		 }
	}
    /**
     * Called when the context menu is about to open.
     * Calls {@link #fillContextMenu(IMenuManager)}
     */
    public void menuAboutToShow(IMenuManager menu)
    {
    	if (!enabledMode)
    	  return;
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
   	      		if (messageLine != null)
   	      		  ml.setShowToolTipText(true, messageLine);
   	      		m.addMenuListener(ml);
   	      	}
   	      }
   	    }
   	    //System.out.println("Inside menuAboutToShow: menu null? "+( ((MenuManager)menu).getMenu()==null));
    }

	/**
	 * Creates the Systems plugin standard groups in a context menu.
	 */
	public static IMenuManager createStandardGroups(IMenuManager menu) 
	{
		if (!menu.isEmpty())
			return menu;			
	    // simply sets partitions in the menu, into which actions can be directed.
	    // Each partition can be delimited by a separator (new Separator) or not (new GroupMarker).
	    // Deleted groups are not used yet.
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_NEW));          // new->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GOTO));       // goto into, go->
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_EXPANDTO));   // expand to->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_EXPAND));       // expand, collapse
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPEN));       // open xxx
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPENWITH));   // open with->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_BROWSEWITH));   // open with->
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_WORKWITH));     // work with->
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_SHOW));         // show->type hierarchy, in-navigator
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_BUILD));        // build, rebuild, refresh
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CHANGE));       // update, change
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE));   // rename,move,copy,delete,bookmark,refactoring
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER));      // move up, move down		
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GENERATE)); // getters/setters, etc. Typically in editor
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_SEARCH));     // search
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CONNECTION));   // connection-related actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_STARTSERVER));  // start/stop remote server actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_IMPORTEXPORT)); // get or put actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADAPTERS));     // actions queried from adapters
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS));    // user or BP/ISV additions
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_VIEWER_SETUP)); // ? Probably View->by xxx, yyy
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_TEAM));         // Team
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_COMPAREWITH));
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_REPLACEWITH));
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_PROPERTIES));   // Properties
		
		return menu;
	}

	/**
	 * protected helper method to add an Action to a given menu.
	 * To give the action the opportunity to grey out, we call selectionChanged, but
	 * only if the action implements ISelectionChangedListener
	 */
	protected void menuAdd(MenuManager menu, IAction action)
	{
		if (action instanceof ISelectionChangedListener)
		  ((ISelectionChangedListener)action).selectionChanged(new SelectionChangedEvent(this,getSelection()));
	}

   /**
	 * Determines whether the view has an ancestor relation selection so
	 * that actions can be enable/disabled appropriately.
	 * For example, delete needs to be disabled when a parent and it's child
	 * are both selected. 
	 * @return true if the selection has one or more ancestor relations
	 */
	protected boolean hasAncestorRelationSelection()
	{
		
		Item[] elements = getSelection(getControl());
		for (int i = 0; i < elements.length; i++)
		{
			TreeItem parentItem = (TreeItem)elements[i];
			for (int j =0; j < elements.length; j++)
			{
				if (j != i)
				{
					if (isAncestorOf(parentItem, (TreeItem)elements[j]))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Handles selection changed in viewer.
	 * Updates global actions.
	 * Links to editor (if option enabled)
	 */
	public void selectionChanged(SelectionChangedEvent event)
	{
        selectionFlagsUpdated = false;
        _setList = new ArrayList();
		IStructuredSelection sel = (IStructuredSelection)event.getSelection();		
		Object firstSelection = sel.getFirstElement();
		if (firstSelection == null)
		  return;

		//	added by Phil. Noticed Edit->Delete not enabled when it should be
        boolean enableDelete = true;
		IStructuredSelection selection= (IStructuredSelection)getSelection();
		Iterator elements= selection.iterator();
		while (enableDelete && elements.hasNext())
		{
			Object element= elements.next();
			ISystemViewElementAdapter adapter = getAdapter(element);
			if (adapter == null)
			  continue;
			if (enableDelete)
				enableDelete = adapter.showDelete(element) && adapter.canDelete(element);
		}
		//System.out.println("Enabling delete action: "+enableDelete);                
		//System.out.println("Enabling selectAll action: "+enableSelectAll(sel));
		((SystemCommonDeleteAction)getDeleteAction()).setEnabled(enableDelete);
		((SystemCommonSelectAllAction)getSelectAllAction()).setEnabled(enableSelectAll(sel)); // added by Phil. Noticed Edit->Select All not enabled when it should be

		ISystemViewElementAdapter adapter = getAdapter(firstSelection);
		if (adapter != null)
		{
		   displayMessage(adapter.getStatusLineText(firstSelection));
		   if ((mouseButtonPressed == LEFT_BUTTON) && (!expandingTreeOnly))   //d40615
		      adapter.selectionChanged(firstSelection);		//d40615  
		}  
		else
		  clearMessage();
        //System.out.println("Inside selectionChanged in SystemView");
        expandingTreeOnly = false;            //d40615
		if (debugProperties)
		{
            ISystemRemoteElementAdapter element = getRemoteAdapter(firstSelection);
            if (element == null)
              return;
            else
			{
				logMyDebugMessage(this.getClass().getName(),": -----------------------------------------------------------");
				logMyDebugMessage(this.getClass().getName(),": REMOTE SSFID.......: " + element.getSubSystemFactoryId(firstSelection));
				logMyDebugMessage(this.getClass().getName(),": REMOTE NAME........: " + element.getName(firstSelection));
				logMyDebugMessage(this.getClass().getName(),": REMOTE TYPECATEGORY: " + element.getRemoteTypeCategory(firstSelection));
				logMyDebugMessage(this.getClass().getName(),": REMOTE TYPE........: " + element.getRemoteType(firstSelection));
				logMyDebugMessage(this.getClass().getName(),": REMOTE SUBTYPE.....: " + element.getRemoteSubType(firstSelection));
				logMyDebugMessage(this.getClass().getName(),": REMOTE SUBSUBTYPE..: " + element.getRemoteSubSubType(firstSelection));
			}
		}
	}
	protected void logMyDebugMessage(String prefix, String msg)
	{
		if (!debugProperties)
		  return;
		//RSEUIPlugin.logDebugMessage(prefix, msg);		
		System.out.println(prefix+" "+msg);
	}

	/**
	 * Convenience method for returning the shell of this viewer.
	 */
	public Shell getShell()
	{
		//return shell;
		return getTree().getShell();
	}	
	
	/**
	 * Helper method to determine if a given object is currently selected.
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelected(Object element)
	{
        return isSelected(element, (IStructuredSelection)getSelection());		
	}
	/**
	 * Helper method to determine if a given tree item is currently selected.
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isTreeItemSelected(Widget w)
	{
		boolean match = false;
        TreeItem[] items = getTree().getSelection();
        if ((items!=null) && (items.length>0))
        {
        	for (int idx=0; !match && (idx<items.length); idx++)
        	  if (items[idx] == w)
        	    match = true;
        }		
        return match;
	}

	/**
	 * Helper method to determine if any of a given array of objects is currently selected
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelected(Object[] elementArray)
	{
        return isSelected(elementArray, (IStructuredSelection)getSelection());		
	}	
	/**
	 * Helper method to determine if a given object is in given selection
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelected(Object element, IStructuredSelection selection)
	{
        boolean isSelected = false;
		Iterator elements = selection.iterator();
		while (!isSelected && elements.hasNext())
		{
			if (element.equals(elements.next()))
			  isSelected = true;
		}        
        return isSelected;		
	}
	/**
	 * Helper method to determine if any of a given array of objects is in given selection
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelected(Object[] elementArray, IStructuredSelection selection)
	{
        boolean isSelected = false;        
		Iterator elements = selection.iterator();
		while (!isSelected && elements.hasNext())
		{
			Object nextSelection = elements.next();
			for (int idx=0; !isSelected && (idx<elementArray.length); idx++)
			{			  
			  if (elementArray[idx].equals(nextSelection))
			    isSelected = true;
			}
		}        
        return isSelected;		
	}	

	/**
	 * Helper method to determine if a given object is currently selected.
	 * Does consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelectedOrChildSelected(Object[] parentElements)
	{
        boolean isSelected = false;
        if ((parentElements==null) || (parentElements.length==0))
          return false;
        for (int idx=0; !isSelected && (idx<parentElements.length); idx++)
           isSelected = isSelectedOrChildSelected(parentElements[idx]);
        return isSelected;
	}


	protected boolean searchToRoot(TreeItem selectedItem, TreeItem searchItem)
	{
		boolean found = false;
		boolean done = false;
		while (!found && !done)
		{
			if (selectedItem == searchItem)
			  found = true;
			else
			{
				selectedItem = selectedItem.getParentItem();
				if (selectedItem == null)
				  done = true;
			}
		}
		return found;
	}

    /**
     *	Called after tree item collapsed
     */
    public void treeCollapsed(TreeExpansionEvent event)
    {
    	//if (true)
    	  //return;
   	    final Object element = event.getElement(); // get parent node being collapsed
   	    Widget widget = findItem(element); // find GUI widget for this node		
   	    if ((widget!=null) && (widget instanceof Item))
   	    {
   		  Item ti= (Item) widget;
   	      Item[] items= getItems(ti);
   	      
   	        //D51021 if ((items!=null) && (items.length==1))
		  if (items!=null) {
   		     for (int i= 0; i < items.length; i++)
	   	     {
			  Object data = items[i].getData();	   	          	
	   	        if ((data != null) && (data instanceof ISystemMessageObject)) {
        		        if (((ISystemMessageObject) data).isTransient()) {	
				  	disassociate(items[i]);
	   	          	      items[i].dispose();
				  }  
	   	        }
	   	     }   
	   	     // append a dummy so there is a plus
	   	     if (getItemCount(ti)==0)
	   	       newItem(ti, SWT.NULL, -1);			
		 }
   	    }
   	    // we always allow adapters opportunity to show a different icon depending on collapsed state
   	    getShell().getDisplay().asyncExec
   	    (
   	        new Runnable()
   	        {
   	        	public void run()
   	        	{
    	            String[] allProps = {IBasicPropertyConstants.P_TEXT,IBasicPropertyConstants.P_IMAGE};    	
	                update(element, allProps); // for refreshing non-structural properties in viewer when model changes   	   	          	    
   	        	}
   	        }
   	    );
    }

    /**
     *	Called after tree item expanded.
     *  We need this hook to potentially undo user expand request.
     */
    public void treeExpanded(TreeExpansionEvent event)
    {
    	expandingTreeOnly=true;
    	//System.out.println("tree expanded");
        final Object element = event.getElement();
   	    // we always allow adapters opportunity to show a different icon depending on expanded state
   	    getShell().getDisplay().asyncExec
   	    (
   	        new Runnable()
   	        {
   	        	public void run()
   	        	{
    	            updatePropertySheet();
    	            String[] allProps = {IBasicPropertyConstants.P_TEXT,IBasicPropertyConstants.P_IMAGE};    	
	                update(element, allProps); // for refreshing non-structural properties in viewer when model changes   	   	          	    
   	        	}
   	        }
   	    );
    }

    /**
     * Handles a tree expand event from the SWT widget.
     * An interception of parent method to set the cursor to busy if the user is expanding a connection.
     *
     * @param event the SWT tree event
     */
    protected void handleTreeExpand(TreeEvent event) 
    {
    	Widget item = event.item;
    	boolean cursorSet = false;
    	Shell shell = getShell();
    	if ((item instanceof TreeItem) && (((TreeItem)item).getData() != null))
    	{
    	    if (doTimings)
    	      elapsedTime.setStartTime();
    		if (item.getData() instanceof IHost)
    		{    		  
    		  //getShell().setCursor(busyCursor);
    		  IHost con = (IHost)item.getData();
    		  if (con.isOffline())
    		  {
    			  org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(shell, busyCursor);
    		  	  cursorSet = true;
    		  }
    		}
    	}
    	super.handleTreeExpand(event);
    	if (cursorSet)
    	{
    	  //getShell().setCursor(null);
    	    org.eclipse.rse.ui.dialogs.SystemPromptDialog.setDisplayCursor(shell, null);
    	}
    	if (doTimings && (item instanceof TreeItem) && (((TreeItem)item).getData() != null))
    	{
    		elapsedTime.setEndTime();
    		System.out.println("Time to expand for " + ((TreeItem)item).getItemCount() + " items: " + elapsedTime);
    	}
    }
       
    /**
     * Clear current selection. Ignore widget disposed message.
     */
    protected void clearSelection()
    {
    	try {
    	  setSelection((ISelection)null);
    	}
    	catch (Exception exc) {}
    }


    /**
     * Returns the implementation of ISystemViewElement for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     */
    protected ISystemViewElementAdapter getAdapter(Object o) 
    {
        
        ISystemViewInputProvider provider = getInputProvider();
        
        // should never be null, but we check just to be safe
        // the input provider should be set because for things like connections, the select
        // dialogs may set a different input provider for the connection adapter which is subsequently
        // not updated when selecting a connection in the Remote Systems view.
        // This ensures that the input provider for the Remote Systems view is set for the adapter.
        if (provider != null) {
            return SystemAdapterHelpers.getAdapter(o, this, provider);
        }
        else {
            return SystemAdapterHelpers.getAdapter(o, this);         
        }
    }
    
    /**
     * Returns the implementation of ISystemRemoteElement for the given
     * object.  Returns null if this object does not adaptable to this.
     */
    protected ISystemRemoteElementAdapter getRemoteAdapter(Object o) 
    {
    	return SystemAdapterHelpers.getRemoteAdapter(o, this);
    }
 
 	/**
	 *
	 */
	public void handleDispose(DisposeEvent event)
	{
		//if (debug)
		  //RSEUIPlugin.logDebugMessage(this.getClass().getName(),"Inside handleDispose for SystemView");
		RSEUIPlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);				
		RSEUIPlugin.getTheSystemRegistry().removeSystemRemoteChangeListener(this);				
		busyCursor.dispose();
		super.handleDispose(event);		
	}  
	
	/**
	 * Return the connection of the selected object, whatever it is. 
	 */
	public IHost getSelectedConnection()
	{
		Object firstSelection = ((StructuredSelection)getSelection()).getFirstElement();
		if (firstSelection == null)
		   return null;
		else if (firstSelection instanceof IHost)		
		   return (IHost)firstSelection;
		else if (firstSelection instanceof ISubSystem)
		   return ((ISubSystem)firstSelection).getHost();
		else if (firstSelection instanceof ISystemFilterPoolReference)
		   return ((ISubSystem)(((ISystemFilterPoolReference)firstSelection).getProvider())).getHost();
		else if (firstSelection instanceof ISystemFilterReference)
		   return ((ISubSystem)(((ISystemFilterReference)firstSelection).getProvider())).getHost();
		else if (getRemoteAdapter(firstSelection) != null)
		{
			ISubSystem ss = getRemoteAdapter(firstSelection).getSubSystem(firstSelection);
			if (ss!=null)
			   return ss.getHost();
			else
			   return null;
		}
	    else
	       return null;
	}

	/**
	 * We override getSelection(Control) so that a list of items
	 * under the same parent always gets returned in the order in which
	 * they appear in the tree view.  Otherwise, after a "move up" or
	 * "move down", the order of selection can come back wrong.
	 */
	protected Item[] getSelection(Control widget) 
	{
		Tree tree = (Tree)widget;
		Item[] oldResult = tree.getSelection();	
		if (oldResult != null && oldResult.length > 0)
		{
			if (oldResult[0] instanceof TreeItem)
			{			
				Widget parentItem = ((TreeItem)oldResult[0]).getParentItem();
				if (parentItem == null)
				{
					parentItem = tree;
				}
				if (itemsShareParent(parentItem, oldResult))
				{
					Item[] newResult = sortSelection(parentItem, oldResult);
					return newResult;
				}
			}
		}
		return oldResult;	
	}
	
	protected boolean itemsShareParent(Widget parentItem, Item[] items)
	{
		for (int i = 0; i < items.length; i++)
		{
			Widget itemParent = ((TreeItem)items[i]).getParentItem();
			if (parentItem instanceof TreeItem)
			{
				if (itemParent != parentItem)
				{
					return false;
				}
			}
			else if (itemParent != null)
			{
				return false;
			}
		}
		
		return true;
	}
	
	protected Item[] sortSelection(Widget parentItem, Item[] oldResult)
	{
		Item[] newResult = new Item[oldResult.length];
		for (int i = 0; i < oldResult.length; i++)
		{
			Item first = removeFirstItem(parentItem, oldResult);
			newResult[i] = first;
		}
			
		return newResult;
	}
	
	protected Item removeFirstItem(Widget parentItem, Item[] items)
	{
		int firstIndex = 0;
		Item firstItem = null;
		int firstItemPosition = 0;
		for (int i = 0; i < items.length; i++)	
		{
			if (items[i] != null)
			{
				Item current = items[i];
				int position = getTreeItemPosition(parentItem, current, items);
				
				if (firstItem == null || position < firstItemPosition)
				{
					firstItem = current;
					firstItemPosition = position;
					firstIndex = i;
				}
			}		
		}
		
		items[firstIndex] = null;
		return firstItem;
	}
	
	
    /**
     * Move one tree item to a new location
     */
    protected void moveTreeItem(Widget parentItem, Item item, Object src, int newPosition)
    { 
    	if (getExpanded(item))
    	{
    	  setExpanded(item, false);
    	  refresh(src); // flush items from memory  	  
    	}

    	createTreeItem(parentItem, src, newPosition);
    	
    	//createTreeItem(parentItem, (new String("New")), newPosition);
        //remove(src);    
        	
        disassociate(item);
	    item.dispose();		
		// TODO: make this work so the selection order doesn't get screwed up!
    }
    
    /**
     * Move existing items a given number of positions within the same node.
     * If the delta is negative, they are all moved up by the given amount. If 
     * positive, they are all moved down by the given amount.<p>
     */
    protected void moveTreeItems(Widget parentItem, Object[] src, int delta)
    {
    	int[] oldPositions = new int[src.length];
    	Item[] oldItems = new Item[src.length];

    	for (int idx=0; idx<src.length; idx++)
    	   oldItems[idx] = (Item)internalFindRelativeItem(parentItem, src[idx], 1);    	
    	for (int idx=0; idx<src.length; idx++) 
    	{
    	   oldPositions[idx] = getTreeItemPosition(parentItem, oldItems[idx])+1;
    	}

    	if (delta > 0) // moving down, process backwards
    	{
          for (int idx=src.length-1; idx>=0; idx--)
          {
             moveTreeItem(parentItem, oldItems[idx], src[idx], oldPositions[idx]+delta);
          }
    	}
        else // moving up, process forewards
        {
          for (int idx=0; idx<src.length; idx++)
          {
             moveTreeItem(parentItem, oldItems[idx], src[idx], oldPositions[idx]+delta-1);	        
          }
        }
    }
    
    protected int getTreeItemPosition(Widget parentItem, Item childItem)
    {
        return getTreeItemPosition(parentItem, childItem, null);
    }
  
    /**
     * Get the position of a tree item within its parent
     */
    protected int getTreeItemPosition(Widget parentItem, Item childItem, Item[] items)
    {
    	int pos = -1;
    	Item[] children = null;
    	if (parentItem instanceof Item)
    	{
    	  if (items == null)
    	     children = getItems((Item)parentItem);
    	  else
    	  children = items;
    	}
    	else 
    	  children = getChildren(parentItem);
    	for (int idx=0; (pos==-1) && (idx<children.length); idx++)
    	{
    	   if (children[idx] == childItem)
    	     pos = idx;
    	}
    	return pos;
    }
    
    /**
     * Expand a given filter, given a subsystem that contains a reference to the filter's pool.
     * This will expand down to the filter if needed
     * @param parentSubSystem - the subsystem containing a reference to the filter's parent pool
     * @param filter - the filter to find, reveal, and expand within the subsystem context
     * @return the filter reference to the filter if found and expanded. This is a unique binary address 
     *   within the object's in this tree, so can be used in the viewer methods to affect this particular
     *   node.
     */
    public ISystemFilterReference revealAndExpand(ISubSystem parentSubSystem, ISystemFilter filter)    
    {
        setExpandedState(parentSubSystem.getHost(), true); // expand the connection
        setExpandedState(parentSubSystem, true); // expand the subsystem
	    Object filterParentInTree = parentSubSystem; // will be case unless in show filter pool mode
	    // if showing filter pools, expand parent filter pool reference...
	    if (SystemPreferencesManager.getPreferencesManager().getShowFilterPools())	    
	    {    	 
	    	ISystemFilterPoolReference poolRef = parentSubSystem.getFilterPoolReferenceManager().getReferenceToSystemFilterPool(filter.getParentFilterPool());
    	    setExpandedState(poolRef, true);
    	    filterParentInTree = poolRef;
	    }
	    // now, find the filter reference, and expand it...
	    Widget parentItem = findItem(filterParentInTree); // find tree widget of parent
	    if ((parentItem == null) || !(parentItem instanceof Item))
	      return null;
        TreeItem child = (TreeItem)internalFindReferencedItem((Item)parentItem, filter, 1);	    
        if (child == null)
          return null;
        // found it! Now expand it...
    	setExpandedState(child.getData(), true);        
    	return (ISystemFilterReference)child.getData();
    }
	
    // ------------------------------------
    // ISYSTEMRESOURCEChangeListener METHOD
    // ------------------------------------
	
    /**
     * Called when something changes in the model
     */
    public void systemResourceChanged(ISystemResourceChangeEvent event)
    {
    	   int type = event.getType();    	   
    	   Object src = event.getSource();
    	   Object parent = event.getParent();
           String[] properties = new String[1];
    	   if (parent == RSEUIPlugin.getTheSystemRegistry())
    	     parent = inputProvider;
    	   ISubSystem ss = null;
    	   Widget item = null;
    	   Widget parentItem = null;
           Object[] multiSource = null;
           Object previous = null;
           if (event.getViewerItem() instanceof TreeItem)
             inputTreeItem = (TreeItem)event.getViewerItem();
           else
             inputTreeItem = null;
    	   boolean wasSelected = false;
    	   boolean originatedHere = (event.getOriginatingViewer() == null) || (event.getOriginatingViewer() == this);
    	   
    	   //logDebugMsg("INSIDE SYSRESCHGD: " + type + ", " + src + ", " + parent);
    	   switch(type)
    	   {
   	    	  // SPECIAL CASES: ANYTHING TO DO WITH FILTERS!!
    	   	  case EVENT_RENAME_FILTER_REFERENCE:
    	   	  case EVENT_CHANGE_FILTER_REFERENCE:
    	   	      findAndUpdateFilter(event, type);
    	   	      break;
    	   	  case EVENT_CHANGE_FILTERSTRING_REFERENCE:
    	   	      findAndUpdateFilterString(event, type);
    	   	      break;

    	   	  case EVENT_ADD_FILTERSTRING_REFERENCE:
    	   	  case EVENT_DELETE_FILTERSTRING_REFERENCE:
    	   	  case EVENT_MOVE_FILTERSTRING_REFERENCES:
    	   	      //findAndUpdateFilterStringParent(event, type);
    	   	      //break;
    	   	  case EVENT_ADD_FILTER_REFERENCE:
    	   	  case EVENT_DELETE_FILTER_REFERENCE:
    	   	  case EVENT_MOVE_FILTER_REFERENCES:
   	    	      // are we a secondary perspective, and our input or parent of our input was deleted?
    	   	      if (((type == EVENT_DELETE_FILTERSTRING_REFERENCE) || 
    	   	           (type == EVENT_DELETE_FILTER_REFERENCE)) &&
    	   	          affectsInput(src))
    	   	      {
    	   	        close();
    	   	        return; 
    	   	      }

    	   	      findAndUpdateFilterParent(event, type);
    	   	      break;

    	   	  case EVENT_ADD:
    	   	  case EVENT_ADD_RELATIVE:
    	   	      if (debug)
    	   	      {
    	   	        logDebugMsg("SV event: EVENT_ADD ");
    	   	      }
    	   	      clearSelection();
    	   	      //refresh(parent);
    	   	      parentItem = findItem(parent);
    	   	      if (parentItem == null)
    	   	        return;
    	   	      if ((parentItem instanceof Item) && !getExpanded((Item)parentItem))
    	   	      {
    	   	        refresh(parent); // flush cached stuff so next call will show new item
    	   	      }
    	   	      else if ((parentItem instanceof Item) || // regular node
    	   	               (parent == inputProvider))  // root node. Hmm, hope this is going to work in all cases
    	   	      {
                  	boolean addingConnection = (src instanceof IHost);
                  	  //System.out.println("ADDING CONNECTIONS.........................: " + addingConnection);
                  	  //System.out.println("event.getParent() instanceof SystemRegistry: " + (event.getParent() instanceof SystemRegistry));
                  	  //System.out.println("inputProvider.showingConnections().........: " + (inputProvider.showingConnections()));
                    if ((parent == inputProvider) && addingConnection && 
                  	    (event.getParent() instanceof ISystemRegistry) && 
                  	    !inputProvider.showingConnections())
                      return; // only reflect new connections in main perspective. pc42742
                  	int pos = -1;
                  	if (type == EVENT_ADD_RELATIVE)
                  	{
                  		previous = event.getRelativePrevious();
                  		if (previous != null)
                  		  pos = getItemIndex(parentItem, previous);
                  		if (pos >= 0)
                  		  pos++; // want to add after previous
                  	}
                  	else
                  	  pos = event.getPosition();
                  	//logDebugMsg("ADDING CONN? "+ addingConnection + ", position="+pos);
    	   	        createTreeItem(parentItem, src, pos);
    	   	        setSelection(new StructuredSelection(src),true);
    	   	      }
   	    	      break;
    	   	  case EVENT_ADD_MANY:
    	   	      if (debug)
    	   	      {
    	   	        logDebugMsg("SV event: EVENT_ADD_MANY");
    	   	      }
   	    	      multiSource = event.getMultiSource();
   	    	      clearSelection();
   	    	      parentItem = findItem(parent);
    	   	      if (parentItem == null)
    	   	        return;
    	   	      if ((parentItem instanceof Item) && !getExpanded((Item)parentItem))
    	   	      {
                    refresh(parent); // flush cached stuff so next call will show new items
    	   	      }
                  else if (multiSource.length > 0)
                  {
                  	  boolean addingConnections = (multiSource[0] instanceof IHost);
                      // are we restoring connections previously removed due to making a profile inactive,
                      // and is one of these connections the one we were opened with?
                      if (addingConnections && (event.getParent() instanceof ISystemRegistry) &&
                               (inputProvider instanceof SystemEmptyListAPIProviderImpl))
                      {
                      	 boolean done = false;
   	    	             for (int idx=0; !done && (idx<multiSource.length); idx++)                      	
   	    	             {
   	    	                if (multiSource[idx] == previousInputConnection)
   	    	                {
   	    	                	done = true;
   	    	                	setInputProvider(previousInputProvider);
   	    	                	previousInput = null;
   	    	                	previousInputProvider = null;
   	    	                }
   	    	             }
   	    	             if (done)
   	    	               return;
                      }
                      // are we adding connections and yet we are not a secondary perspective?
                      // If so, this event does not apply to us.                      
                  	  else if (addingConnections && (event.getParent() instanceof ISystemRegistry) && 
                  	                                !inputProvider.showingConnections())
                        return;

   	    	          for (int idx=0; idx<multiSource.length; idx++)
   	    	          {
    	   	            if (debug && addingConnections)
    	   	              logDebugMsg("... new connection " + ((IHost)multiSource[idx]).getAliasName());
   	    	            createTreeItem(parentItem, multiSource[idx], -1);
   	    	          }
    	   	          setSelection(new StructuredSelection(multiSource),true);
   	    	      }   	    	        
   	    	      break;   	    	      
    	   	  case EVENT_REPLACE_CHILDREN:
    	   	      if (debug)
    	   	      {
    	   	        logDebugMsg("SV event: EVENT_REPLACE_CHILDREN");
    	   	      }
   	    	      multiSource = event.getMultiSource();
   	    	        //logDebugMsg("MULTI-SRC LENGTH : " + multiSource.length);
   	    	      clearSelection();
   	    	      parentItem = findItem(parent);
    	   	      if (parentItem == null)
    	   	        return;
                  if (multiSource.length > 0 && parentItem != null && parentItem instanceof Item )
                  {
		              getControl().setRedraw(false);    	   	        
   	    	          collapseNode(parent, true); // collapse and flush gui widgets from memory    	   	        
   	    	          //setExpandedState(parent, true); // expand the parent
   	    	          setExpanded( (Item) parentItem, true);  // expand the parent without calling resolveFilterString
   	    	          TreeItem[] kids = ((TreeItem)parentItem).getItems(); // any kids? Like a dummy node?
   	    	          if (kids != null)
   	    	          	for (int idx=0; idx<kids.length; idx++)
   	    	          	   kids[idx].dispose();
                  	  //boolean addingConnections = (multiSource[0] instanceof SystemConnection);
   	    	          for (int idx=0; idx<multiSource.length; idx++)
   	    	          {
    	   	            //if (debug && addingConnections)
    	   	            //  logDebugMsg("... new connection " + ((SystemConnection)multiSource[idx]).getAliasName());
   	    	            createTreeItem(parentItem, multiSource[idx], -1);
   	    	          }
		              getControl().setRedraw(true);    	   	        	   	             	    	        
    	   	          //setSelection(new StructuredSelection(multiSource),true);
   	    	      }   	    	        
   	    	      break;   	    	      
    	   	  case EVENT_CHANGE_CHILDREN:
    	   	      if (debug)
    	   	      {
    	   	        logDebugMsg("SV event: EVENT_CHANGE_CHILDREN. src="+src+", parent="+parent);
    	   	        //Exception e = new Exception();
    	   	        //e.fillInStackTrace();
    	   	        //e.printStackTrace();
    	   	      }
    	   	      // I HAVE DECIDED TO CHANGE THE SELECTION ALGO TO ONLY RESELECT IF THE CURRENT
    	   	      // SELECTION IS A CHILD OF THE PARENT... PHIL
                  boolean wasSrcSelected = false;
    	   	      if (src != null) 
    	   	      {
                    wasSrcSelected = isSelectedOrChildSelected(src);
                    //System.out.println("WAS SELECTED? " + wasSrcSelected);
    	   	      }
    	          item = findItem(parent);
    	          //logDebugMsg("  parent = " + parent);  
    	          //logDebugMsg("  item = " + item);
    	          // INTERESTING BUG HERE. GETEXPANDED WILL RETURN TRUE IF THE TREE ITEM HAS EVER BEEN
    	          // EXPANDED BUT IS NOW COLLAPSED! I CANNOT FIND ANY API IN TreeItem or TreeViewer THAT
    	          // WILL TELL ME IF A TREE ITEM IS SHOWING OR NOT!
    	          if ((item != null) && (item instanceof TreeItem) && ((TreeItem)item).getExpanded())
    	          {
                    if (wasSrcSelected)
                    {
                      //System.out.println("...Clearing selection");
    	   	          clearSelection();
                    }
    	   	        //refresh(parent);
    	   	        if (debug)
    	   	          System.out.println("Found item and it was expanded for "+parent);
		            getControl().setRedraw(false);    	   	        
   	    	        collapseNode(parent, true); // collapse and flush gui widgets from memory    	   	        
   	    	        setExpandedState(parent, true); // expand the parent
		            getControl().setRedraw(true);    	   	        	   	             	    	        
    	   	        if (wasSrcSelected)
    	   	        {
    	   	          //System.out.println("Setting selection to " + src);
    	   	          setSelection(new StructuredSelection(src),true);
    	   	        }
    	          }
    	          else
    	            collapseNode(parent,true);
   	    	      break;   	    	      
   	    	  case EVENT_DELETE:   	    	  
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_DELETE ");
   	    	      // are we a secondary perspective, and our input or parent of our input was deleted?
    	   	      if (affectsInput(src))
    	   	      {
    	   	        close();
    	   	        return; 
    	   	      }
   	    	      parentItem = findItem(parent);
    	   	      if (parentItem == null)
    	   	        return;
                  if ((parentItem instanceof Item) && !getExpanded((Item)parentItem))    	   	        
                    refresh(parent); // flush memory
                  else
                  {
   	    	        wasSelected = isSelectedOrChildSelected(src);
   	    	        if (wasSelected)
   	    	          clearSelection();
   	    	        remove(src);
   	    	        if (wasSelected)
    	   	          setSelection(new StructuredSelection(parent),true);
                  }
   	    	      break;

   	    	  case EVENT_DELETE_MANY:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_DELETE_MANY ");  
   	    	      multiSource = event.getMultiSource(); 
   	    	      // are we a secondary perspective, and our input or parent of our input was deleted?
    	   	      if (affectsInput(multiSource))
    	   	      {
    	   	        close();
    	   	        return; 
    	   	      }
   	    	      parentItem = findItem(parent);
    	   	      if (parentItem == null)
    	   	        return;
                  if ((parentItem instanceof Item) && !getExpanded((Item)parentItem))    	   	        
                    refresh(parent); // flush memory
                  else
                  {  
   	    	        wasSelected = isSelectedOrChildSelected(multiSource);
   	    	        if (wasSelected)
   	    	          clearSelection();
    	   	        remove(multiSource);   	    	        
   	    	        if (wasSelected)
    	   	          setSelection(new StructuredSelection(parent),true);
                  }
   	    	      break;   	    	      
   	    	  /* Now done below in systemRemoteResourceChanged
   	    	  case EVENT_DELETE_REMOTE:   	    	  
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_DELETE_REMOTE ");
                  deleteRemoteObject(src);
   	    	      break;

   	    	  case EVENT_DELETE_REMOTE_MANY:
   	    	  // multi-source: array of objects to delete
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_DELETE_REMOTE_MANY ");  
    	   	      multiSource = event.getMultiSource();
                  //remoteItemsToSkip = null; // reset
    	   	      if ((multiSource == null) || (multiSource.length==0))
    	   	        return;
                  for (int idx=0; idx<multiSource.length; idx++)
                    deleteRemoteObject(multiSource[idx]);
   	    	      break;   	    	      
   	    	  */
   	    	  case EVENT_RENAME:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_RENAME ");
	              properties[0] = IBasicPropertyConstants.P_TEXT;
	              update(src, properties); // for refreshing non-structural properties in viewer when model changes   	   	      
   	    	      updatePropertySheet();
   	    	      break;
   	    	  /* Now done below in systemRemoteResourceChanged
   	    	  case EVENT_RENAME_REMOTE:
   	    	  // SRC: the updated remote object, after the rename
   	    	  // PARENT: the String from calling getAbsoluteName() on the remote adapter BEFORE updating the remote object's name
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_RENAME_REMOTE ");
    	   	        
   	   	          renameRemoteObject(src, (String)parent);
   	    	      break;
   	    	  */
   	    	  case EVENT_ICON_CHANGE:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_ICON_CHANGE ");
    	   	      
    	   	      if (initViewerFilters != null && initViewerFilters.length > 0) 
    	   	      {    	   	 
    	   	    	  Widget w = findItem(src);
    	   	    	  if (w == null)
    	   	    	  {
    	   	    		  refresh(parent);
    	   	    	  }
    	   	    	  else
    	   	    	  {
    	   	         	properties[0] = IBasicPropertyConstants.P_IMAGE;
        	   	      	update(src, properties); // for refreshing non-structural properties in viewer when model changes
        	  
    	   	    	  }
    	   	      }
    	   	      else {
    	   	      	properties[0] = IBasicPropertyConstants.P_IMAGE;
    	   	      	update(src, properties); // for refreshing non-structural properties in viewer when model changes
    	   	      }
    	   	      
   	    	      //updatePropertySheet();
   	    	      break;
   	    	  //case EVENT_CHANGE:
    	   	      //if (debug)
    	   	        //logDebugMsg("SV event: EVENT_CHANGE ");
   	    	      //refresh(src); THIS IS AN EVIL OPERATION: CAUSES ALL EXPANDED NODES TO RE-REQUEST THEIR CHILDREN. OUCH!
   	    	      //updatePropertySheet();
   	    	      //break;
   	    	  case EVENT_REFRESH:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_REFRESH ");
    	   	      //if (src != null)
   	    	      //  refresh(src); // ONLY VALID WHEN USER TRULY WANTS TO REQUERY CHILDREN FROM HOST
   	    	      //else
   	    	      //  refresh(); // refresh entire tree
   	    	      if ((src == null) || (src == RSEUIPlugin.getTheSystemRegistry()))
   	    	        refreshAll();
   	    	      else
   	    	      {
   	    	        //smartRefresh(src, false);
   	    	        smartRefresh(src, true);
   	    	      }
   	    	      updatePropertySheet();
   	    	      break;
   	    	  // refresh the parent of the currently selected items.
   	    	  // todo: intelligently re-select previous selections
   	    	  case EVENT_REFRESH_SELECTED_PARENT:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_REFRESH_SELECTED_PARENT ");
    	   	      TreeItem[] items = getTree().getSelection();    	   	      
    	   	      if ((items != null) && (items.length > 0) && (items[0] instanceof Item))
    	   	      {
    	   	      	//System.out.println("Selection not empty");
    	   	      	parentItem = getParentItem(items[0]); // get parent of first selection. Only allowed to select items of same parent.
    	   	      	if ((parentItem != null) && (parentItem instanceof Item))
    	   	      	{
    	   	      	  //System.out.println("parent of selection not empty: "+parentItem.getData());
    	   	          smartRefresh(new TreeItem[] {(TreeItem)parentItem});
    	   	      	}
    	   	      	//else
    	   	      	//System.out.println("parent of selection is empty");    	   	      	
    	   	      }
    	   	      //else
    	   	      	//System.out.println("Selection is empty");    	   	      
    	   	      break;
   	    	  case EVENT_REFRESH_SELECTED:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_REFRESH_SELECTED ");
    	   	      IStructuredSelection selected = (IStructuredSelection)getSelection();
    	   	      Iterator i = selected.iterator();
    	   	      // the following is a tweak. Refresh only re-queries the children. If the selected item has no
    	   	      //  childen, then refresh does nothing. Instead of that outcome, we re-define it to mean refresh
    	   	      //  the parent. The tricky part is to prevent multiple refreshes on multiple selections so we have
    	   	      //  to pre-scan for this scenario.
    	   	      // We also want to re-select any remote objects currently selected. They lose their selection as their
    	   	      //  memory address changes.
    	   	      Item  parentElementItem = null;
    	   	      Vector  selectedRemoteObjects = new Vector();
    	   	      ss = null;
    	   	      items = getTree().getSelection();
    	   	      int itemIdx = 0;
    	   	      SystemElapsedTimer timer = null;
    	   	      if (doTimings)
    	   	         timer = new SystemElapsedTimer();
                  //System.out.println("Inside EVENT_REFRESH_SELECTED. FIRST SELECTED OBJECT = " + items[0].handle);
    	   	      while (i.hasNext())
    	   	      {

    	   	      	  Object element = i.next();
    	   	      	  ISystemViewElementAdapter adapter = getAdapter(element);
    	   	      	  if ((parentElementItem==null) && (adapter != null) && (!adapter.hasChildren(element)))
    	   	      	  {    	   	      	  	
    	   	      	    //parentItem = getParentItem((Item)findItem(element));
    	   	      	    parentItem = getParentItem(items[itemIdx]);
    	   	      	    if ((parentItem != null) && (parentItem instanceof Item))
    	   	      	      parentElementItem = (Item)parentItem; //.getData();
    	   	      	  }
    	   	      	  if (getRemoteAdapter(element) != null)
    	   	      	  {
    	   	      	    selectedRemoteObjects.addElement(element);
    	   	      	    if (ss==null)
    	   	      	      ss=getRemoteAdapter(element).getSubSystem(element);
    	   	      	  }
    	   	      	  itemIdx++;
    	   	      }
    	   	      if (parentElementItem != null)
    	   	      {
    	   	        //refresh(parentElement);
    	   	        smartRefresh(new TreeItem[] {(TreeItem)parentElementItem});
    	   	        if (selectedRemoteObjects.size() > 0)
    	   	        {
    	   	            selectRemoteObjects(selectedRemoteObjects, ss, parentElementItem);
    	   	        }
    	   	      }
    	   	      // the following is another tweak. If an expanded object is selected for refresh, which has remote children,
    	   	      // and any of those children are expanded, then on refresh the resulting list may be in a different 
    	   	      // order and the silly algorithm inside tree viewer will simply re-expand the children at the previous
    	   	      // relative position. If that position has changed, the wrong children are re-expanded!
    	   	      // How to fix this? Ugly code to get the query the list of expanded child elements prior to refresh, 
    	   	      // collapse them, do the refresh, then re-expand them based on absolute name versus tree position.
    	   	      // Actually, to do this right we need to test if the children of the selected item are remote objects
    	   	      // versus just the selected items because they may have selected a filter!
    	   	      // We go straight the TreeItem level for performance and ease of programming.
                  else
                  {                  	
                     smartRefresh(getTree().getSelection());
                  }
    	   	      if (doTimings)
    	   	      {
    	   	         timer.setEndTime();
    	   	         System.out.println("Time to refresh selected: " + timer);
    	   	      }
                  //else
                  //{                  	
    	   	        //i = selected.iterator();
    	   	        //while (i.hasNext())
    	   	          //refresh(i.next());
                  //}
                  
   	    	      updatePropertySheet();
   	    	      break;
   	    	  case EVENT_REFRESH_SELECTED_FILTER:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_REFRESH_SELECTED_FILTER ");
    	   	      IStructuredSelection selectedItems= (IStructuredSelection)getSelection();
    	   	      Iterator j = selectedItems.iterator();
    	   	      // We climb up the tree here until we find a SystemFilterReference data member in the tree.
    	   	      // If we do find a reference of SystemFilterReference we refresh on it.
    	   	      // If we do not find a reference of SystemFilterReference we.....TODO: WHAT DO WE DO???
    	   	      // We also want to re-select any remote objects currently selected. They lose their selection as their
    	   	      //  memory address changes.
    	   	      Item  parentElemItem = null;
    	   	      Vector  selRemoteObjects = new Vector();
                  ss = null;
    	   	      if (j.hasNext())
    	   	      {
    	   	      	  Object element = j.next();
    	   	      	  ISystemViewElementAdapter adapter = getAdapter(element);
    	   	      	  if ((parentElemItem==null) && (adapter != null))
    	   	      	  {
    	   	      	    Item parItem = getParentItem((Item)findItem(element));

    	   	      	    if ((parItem != null) && (parItem instanceof Item))
    	   	      	    	parentElemItem = (Item)parItem; //.getData();    	   	      	    
	   	   	      	    	
 	  	   	      	    while (parItem!= null && !(parItem.getData() instanceof ISystemFilterReference))
    	   	      	    {
	    	   	      	    parItem = getParentItem((Item)parItem);	    	

	    	   	      	    if ((parItem != null) && (parItem instanceof Item))
	    	   	      	    	parentElemItem = (Item)parItem; //.getData();
    	   	      	    }
    	   	      	  }
    	   	      	  if (getRemoteAdapter(element) != null)
    	   	      	  {
    	   	      	    selRemoteObjects.addElement(element);
    	   	      	    if (ss==null)
    	   	      	      ss=getRemoteAdapter(element).getSubSystem(element);
    	   	      	  }
    	   	      }
    	   	      
				  if (parentElemItem != null && (parentElemItem.getData() instanceof ISystemFilterReference))
    	   	      {
    	   	        smartRefresh(new TreeItem[] {(TreeItem)parentElemItem});
    	   	        if (selRemoteObjects.size() > 0)
    	   	        {
    	   	          	selectRemoteObjects(selRemoteObjects, ss, parentElemItem);
    	   	        }
    	   	        
	   	    	    updatePropertySheet();    	   	        
    	   	      }
    	   	      else
    	   	      {
    	   	      	// if we cannot find a parent element that has a system filter reference then we refresh
    	   	      	// everything since the explorer must be within a filter
    	   	      	event.setType(ISystemResourceChangeEvents.EVENT_REFRESH);
    	   	      	systemResourceChanged(event);
    	   	      }
   	    	      break;   	    	      
   	    	  case EVENT_REFRESH_REMOTE:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_REFRESH_REMOTE: src = "+src);
    	   	      refreshRemoteObject(src, parent, originatedHere);
   	    	      break;
   	    	  case EVENT_SELECT_REMOTE:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_SELECT_REMOTE: src = "+src);
                  //remoteItemsToSkip = null; // reset
                  selectRemoteObjects(src, (ISubSystem)null, parent);
   	    	      break;

   	    	  case EVENT_MOVE_MANY:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_MOVE_MANY ");   	    	  
   	    	      multiSource = event.getMultiSource();
   	    	      if ((multiSource == null) || (multiSource.length == 0))
   	    	        return;
   	    	      parentItem = findItem(parent);
    	   	      if (parentItem == null)
    	   	        return;
                  if ((parentItem instanceof Item) && !getExpanded((Item)parentItem))    	   	        
                    refresh(parent); // flush memory
                  else
                  {
                  	clearSelection();
                  	moveTreeItems(parentItem, multiSource, event.getPosition());
   	    	        setSelection(new StructuredSelection(multiSource),true);                  	  
                  }
   	    	      break;   	    	      
   	    	  case EVENT_PROPERTY_CHANGE:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_PROPERTY_CHANGE ");   	    	  
    	   	      String[] allProps = {IBasicPropertyConstants.P_TEXT,IBasicPropertyConstants.P_IMAGE};
                  ISystemRemoteElementAdapter ra = getRemoteAdapter(src);
                  if (ra != null) 
                  {
                  	 updateRemoteObjectProperties(src);
                  } 
                  else  	   	      
	                update(src, allProps); // for refreshing non-structural properties in viewer when model changes   	   	      
   	    	      updatePropertySheet();
   	    	      break;
   	    	  case EVENT_PROPERTYSHEET_UPDATE:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_PROPERTYSHEET_UPDATE ");   	    	  
   	    	      updatePropertySheet();
   	    	      break;
   	    	  case EVENT_MUST_COLLAPSE:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_MUST_COLLAPSE ");   	    	  
   	    	      collapseNode(src, true); // collapse and flush gui widgets from memory
   	    	      break;   	    	      
   	    	  case EVENT_COLLAPSE_ALL:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_COLLAPSE_ALL ");   	    	  
   	    	      collapseAll(); // collapse all
   	    	      if ((src!=null) && (src instanceof String) && ((String)src).equals("false")) // defect 41203
   	    	        {}
   	    	      else
   	    	        refresh(); // flush gui widgets from memory
   	    	      break;   	    	      
   	    	  case EVENT_COLLAPSE_SELECTED: // defect 41203
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_COLLAPSE_SELECTED ");   	    	  
   	    	      collapseSelected(); 
   	    	      break;   	    	      
   	    	  case EVENT_EXPAND_SELECTED: // defect 41203
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_EXPAND_SELECTED ");   	    	  
   	    	      expandSelected(); 
   	    	      break;   	    	      


              case EVENT_REVEAL_AND_SELECT:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_REVEAL_AND_SELECT "); 	  
    	   	      parentItem = findItem(parent);
    	   	      if (parentItem == null)
    	   	        return;
    	   	      if ((parentItem instanceof Item) && !getExpanded((Item)parentItem))
    	   	      {
    	   	        setExpandedState(parent, true);
    	   	        Object toSelect = src;
    	   	        //if (event.getMultiSource() != null)
    	   	          //toSelect = event.getMultiSource();
    	   	        //clearSelection();
    	   	        if (toSelect != null)    	   	        
    	   	        {
    	   	          if (parent instanceof ISystemBaseReferencingObject) 
    	   	          {
                        TreeItem child = (TreeItem)internalFindReferencedItem((Item)parentItem, toSelect, 1);
                        if (child != null)
                          toSelect = child.getData();
    	   	          }
    	   	          else if ( (parent instanceof ISystemFilterPoolReferenceManagerProvider) &&
    	   	                    !(src instanceof ISystemBaseReferencingObject) )
    	   	          {
    	   	          	// we are in "don't show filter pools" mode and a new filter was created
    	   	          	//  (we get the actual filter, vs on pool ref creation when we get the pool ref)
                        TreeItem child = (TreeItem)internalFindReferencedItem((Item)parentItem, toSelect, 1);
                        if (child != null)
                          toSelect = child.getData();    	   	          	
    	   	          }
    	   	          setSelection(new StructuredSelection(toSelect),true);
    	   	        }
    	   	      }
   	    	      break;   	
              case EVENT_SELECT:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_SELECT "); 	  
    	   	      item = findItem(src);
    	   	      if (item == null) // if not showing item, this is a no-op
    	   	        return;
    	   	      setSelection(new StructuredSelection(src), true);
   	    	      break;   	
              case EVENT_SELECT_EXPAND:
    	   	      if (debug)
    	   	        logDebugMsg("SV event: EVENT_SELECT_EXPAND "); 	  
    	   	      item = findItem(src);
    	   	      if (item == null) // if not showing item, this is a no-op
    	   	        return;
    	   	      if (!getExpanded((Item)item))
    	   	        setExpandedState(src, true);
    	   	      setSelection(new StructuredSelection(src), true);    	   	        
   	    	      break;   	

   	    }
    }	
    // ------------------------------------
    // ISYSTEMREMOTEChangeListener METHOD
    // ------------------------------------
   
	/**
	 * This is the method in your class that will be called when a remote resource
	 *  changes. You will be called after the resource is changed.
	 * @see org.eclipse.rse.model.ISystemRemoteChangeEvent
	 */
    public void systemRemoteResourceChanged(ISystemRemoteChangeEvent event)
    {
    	int eventType = event.getEventType();
    	Object remoteResourceParent = event.getResourceParent();    	
    	Object remoteResource = event.getResource();
    	boolean originatedHere = (event.getOriginatingViewer() == this);
    	Vector remoteResourceNames = null;
    	if (remoteResource instanceof Vector)
    	{
    		remoteResourceNames = (Vector)remoteResource;
    		remoteResource = remoteResourceNames.elementAt(0);
    	}
    	String remoteResourceParentName = getRemoteResourceAbsoluteName(remoteResourceParent);
    	String remoteResourceName = getRemoteResourceAbsoluteName(remoteResource);
    	if (remoteResourceName == null)
    	  return;
    	  
    	ISubSystem ss = getSubSystem(event, remoteResource, remoteResourceParent);
    	
    	Vector filterMatches = null;

    	switch (eventType)
    	{
    		// --------------------------
    		// REMOTE RESOURCE CHANGED...
    		// --------------------------
    		case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CHANGED:
    		     updatePropertySheet(); // just in case
    		     break;
    		// --------------------------
    		// REMOTE RESOURCE CREATED...
    		// --------------------------
    		case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED:
    		     // we can easily lose our original selection so we need save and restore it if needed
    		     Vector prevSelection = null;
    		     TreeItem parentSelectionItem = null;
    		     if (originatedHere)
    		     {
    		     	prevSelection = getRemoteSelection();
    		        parentSelectionItem = getSelectedParentItem();
    		     }

    		     // when a new remote resource is created, we need to interrogate all filters
    		     //  within connections to the same hostname, to see if the filter results are
    		     //  affected by this change. If so, we refresh the filter.
    		     filterMatches = findAllRemoteItemFilterReferences(remoteResourceName, ss, null);
    		     ArrayList selectedFilters = null;
    		     if (filterMatches != null)
    		     {
    		     	for (int idx=0; idx<filterMatches.size(); idx++)
    		     	{
    		     		FilterMatch match = (FilterMatch)filterMatches.elementAt(idx);
    		     		TreeItem filterItem = match.getTreeItem();
    		     		if (isTreeItemSelected(filterItem)) // if this filter is currently selected, we will lose that selection!
    		     		{
    		     			if (selectedFilters == null)
    		     			  selectedFilters = new ArrayList();
    		     			selectedFilters.add(filterItem);
    		     		}
                        smartRefresh(new TreeItem[] {filterItem}, null, true);
    		     	}
    		     }
    		     // now, refresh all occurrences of the remote parent object. 
    	   	     refreshRemoteObject(remoteResourceParent, null, false);
    	   	     // restore selected filters...
    	   	     if (selectedFilters != null)
    	   	       setSelection(selectedFilters);
    	   	     // if the create event originated here, then expand the selected node and 
    	   	     //  select the new resource under it.
    	   	     if (originatedHere)
    	   	     {
    	   	     	// first, restore previous selection...
    	   	     	if (prevSelection != null)
    		     	  selectRemoteObjects(prevSelection, ss, parentSelectionItem);
    	   	     	TreeItem selectedItem = getFirstSelectedTreeItem();
    	   	     	if (selectedItem != null)
    	   	     	{
    	   	     	    if (!selectedItem.getExpanded()) // if the filter is expanded, then we already refreshed it... 
    	   	     	    {
        	   	            createChildren(selectedItem);        	   	  
        	                selectedItem.setExpanded(true);
    	   	     	    }
    	   	     	    if (remoteResourceNames != null)
                           selectRemoteObjects(remoteResourceNames, ss, selectedItem);
    	   	     	    else
                           selectRemoteObjects(remoteResourceName,  ss, selectedItem);
    	   	     	}
    	   	     	//else 
    	   	     	  //System.out.println("Hmm, nothing selected");
    	   	     }
    		     break;
    		// --------------------------
    		// REMOTE RESOURCE DELETED...
    		// --------------------------
    		case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED:
    		     // delete all existing references to the remote object(s)...
    		     if (remoteResourceNames != null)
    		     {
    		     	for (int idx=0; idx<remoteResourceNames.size(); idx++)
    		     	   deleteRemoteObject(remoteResourceNames.elementAt(idx), ss);
    		     }
    		     else
    		        deleteRemoteObject(remoteResourceName, ss);
    		        
    		     // now, find all filters that either list this remote resource or list the contents of it,
    		     // if it is a container... for expediency we only test for the first resource, even if given
    		     // a list of them...
    		     filterMatches = findAllRemoteItemFilterReferences(remoteResourceName, ss, null);
    		     if (filterMatches != null)
    		     {
    		     	for (int idx=0; idx<filterMatches.size(); idx++)
    		     	{
    		     		FilterMatch match = (FilterMatch)filterMatches.elementAt(idx);
    		     		TreeItem filterItem = match.getTreeItem();
    		     		if (match.listsElement())
    		     		{
    		     		    // if the filter is expanded, we are ok. If not, we need to flush its memory...
		                    if (!getExpanded(filterItem))
			                   refresh(filterItem.getData());
    		     		}
    		     		else // else this filter lists the contents of the deleted container element, so refresh it:
    		     		{
    		     		    // if the filter is not expanded, we need to flush its memory...
		                    if (!getExpanded(filterItem))
			                   refresh(filterItem.getData());
			                else // if the filter is expanded, we need to refresh it
                               smartRefresh(new TreeItem[] {filterItem}, null, true);			                       		     			
    		     		}
    		     	}
    		     }
    		     
    		     break;

    		// --------------------------
    		// REMOTE RESOURCE RENAMED...
    		// --------------------------
    		case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED:
    		     // we can easily lose our original selection so we need save and restore it if needed
    		     prevSelection = null;
    		     parentSelectionItem = null;
    		     if (originatedHere)
    		     {
    		     	prevSelection = getRemoteSelection();
    		        parentSelectionItem = getSelectedParentItem();
    		     }

    		     // rename all existing references to the remote object...
    		     renameRemoteObject(remoteResource, event.getOldName(), ss);

    		     // now, find all filters that list the contents of the OLD name container.
    		     filterMatches = findAllRemoteItemFilterReferences(event.getOldName(), ss, null);
    		     if (filterMatches != null)
    		     {
    		     	for (int idx=0; idx<filterMatches.size(); idx++)
    		     	{
    		     		FilterMatch match = (FilterMatch)filterMatches.elementAt(idx);
    		     		TreeItem filterItem = match.getTreeItem();
    		     		if (match.listsElementContents()) // this filter lists the contents of the renamed container element, so refresh it:
    		     		{
    		     		    // if the filter is not expanded, we need only flush its memory...
		                    if (!getExpanded(filterItem))
			                   refresh(filterItem.getData());
			                else // the filter is expanded, so refresh its contents. This will likely result in an empty list
                               smartRefresh(new TreeItem[] {filterItem}, null, true);			                       		     			
    		     		}
    		     	}
    		     }
    		     // now, find all filters that list the contents of the NEW name container.
    		     filterMatches = findAllRemoteItemFilterReferences(remoteResourceName, ss, null);
    		     if (filterMatches != null)
    		     {
    		     	for (int idx=0; idx<filterMatches.size(); idx++)
    		     	{
    		     		FilterMatch match = (FilterMatch)filterMatches.elementAt(idx);
    		     		TreeItem filterItem = match.getTreeItem();
    		     		if (match.listsElementContents()) // this filter lists the contents of the renamed container element, so refresh it:
    		     		{
    		     		    // if the filter is not expanded, we need only flush its memory...
		                    if (!getExpanded(filterItem))
			                   refresh(filterItem.getData());
			                else // the filter is expanded, so refresh its contents. This will likely result in an empty list
                               smartRefresh(new TreeItem[] {filterItem}, null, true);			                       		     			
    		     		}
    		     	}
    		     }

    		     // restore selection
    		     if (originatedHere && (prevSelection != null))
    		     {
    		     	selectRemoteObjects(prevSelection, ss, parentSelectionItem);
    		       updatePropertySheet(); // just in case
    		     }
    		     break;    		     
    	}
    }
    
    /**
     * Turn selection into an array of remote object names
     */
    protected Vector getRemoteSelection()
    {
    	Vector prevSelection = null;
        IStructuredSelection selection = (IStructuredSelection)getSelection();
    	Iterator i = selection.iterator();
    	while (i.hasNext())
    	{
    		Object element = i.next();
    		ISystemRemoteElementAdapter ra = getRemoteAdapter(element);
    		if (ra != null)
    		{
    			if (prevSelection == null)
    			  prevSelection = new Vector();
    		    prevSelection.addElement(ra.getAbsoluteName(element));    		     		
    		}
        }
    	return prevSelection;
    }

    /**
     * Turn a given remote object reference into a fully qualified absolute name
     */
    protected String getRemoteResourceAbsoluteName(Object remoteResource)
    {
    	if (remoteResource == null)
    	  return null;
    	String remoteResourceName = null;
        if (remoteResource instanceof String)
    	  remoteResourceName = (String)remoteResource;    	  
    	else
    	{
    		ISystemRemoteElementAdapter ra = getRemoteAdapter(remoteResource);
    		if (ra == null)
    		  return null;
    		remoteResourceName = ra.getAbsoluteName(remoteResource);
    	}
    	return remoteResourceName;    	
    }
    /**
     * Deduce the subsystem from the event or remote object
     */
    protected ISubSystem getSubSystem(ISystemRemoteChangeEvent event, Object remoteResource, Object remoteParent)
    {
    	if (event.getSubSystem() != null)
    	  return event.getSubSystem();
    	ISubSystem ss = null;
    	if ((remoteResource != null) && !(remoteResource instanceof String))
    	{
    		if (remoteResource instanceof Vector)
    		{
    			Vector v = (Vector)remoteResource;
    			if (v.size() > 0)    			
    		      ss = getSubSystem(event, v.elementAt(0), null);
    		}
    		else
    		{
    		   ISystemRemoteElementAdapter ra = getRemoteAdapter(remoteResource);
    		   if (ra != null)
    		     ss = ra.getSubSystem(remoteResource);
    		}
    	}
    	if ((ss==null) && (remoteParent != null) && !(remoteParent instanceof String))
    	{
    		if (remoteParent instanceof Vector)
    		{
    			Vector v = (Vector)remoteParent;
    			if (v.size() > 0)    			
    		      ss = getSubSystem(event, null, v.elementAt(0));    			
    		}
    		else
    		{
    		   ISystemRemoteElementAdapter ra = getRemoteAdapter(remoteParent);
    		   if (ra != null)
    		     ss = ra.getSubSystem(remoteParent);
    		}
    	}
    	return ss;
    }

    // ------------------------------------
    // MISCELLANEOUS METHODS...
    // ------------------------------------
        
    /**
     * Close us!
     */
    protected void close()
    {
    	previousInputProvider = inputProvider;
    	previousInput = getWorkbenchPart().getSite().getPage().getInput();
        ISystemViewInputProvider ip = new SystemEmptyListAPIProviderImpl();
        setInputProvider(ip);
    }

    /**
     * Test if the given input is our view's input object. This is designed to only
     * consider instances of ourself in non-primary perspectives.
     */
    protected boolean affectsInput(Object[] elements)
    {
    	boolean affected = false;
    	IWorkbenchPart viewPart = getWorkbenchPart();
    	if ((viewPart != null) && (getInput() != RSEUIPlugin.getTheSystemRegistry())
    	    && !(getInput() instanceof SystemEmptyListAPIProviderImpl))
    	{
    		for (int idx=0; !affected && (idx<elements.length); idx++)
    		   affected = affectsInput(elements[idx]);
    	}
        return affected;
    }
    
    /**
     * Test if the given input is our view's input object. This is designed to only
     * consider instances of ourself in non-primary perspectives.
     */
    protected boolean affectsInput(Object element)
    {
    	boolean affected = false;
    	IWorkbenchPart viewPart = getWorkbenchPart();
    	if ((viewPart != null) && (getInput() != RSEUIPlugin.getTheSystemRegistry())
    	    && !(getInput() instanceof SystemEmptyListAPIProviderImpl))
    	{

    		Object input = viewPart.getSite().getPage().getInput();
    		if (input != null)
    		{
    			Object referencedElement = getReferencedObject(element);
    		    Object referencedInput   = getReferencedObject(input);
    			//System.out.println("TESTING1 " + input + " vs " + element);
    			//System.out.println("TESTING2 " + referencedInput + " vs " + referencedElement);
    			if ((input == element) || (referencedInput == referencedElement))
    			  affected = true;
    			else 
    			{
    				while (!affected && (input != null))
    				{
    					input = getElementParent(input);
    					if (input != null)
    					{
    					  referencedInput = getReferencedObject(input);
    			          //System.out.println("...TESTING1 " + input + " vs " + element);
    			          //System.out.println("...TESTING2 " + referencedInput + " vs " + referencedElement);
    					  affected = ((input == element) || (referencedInput == referencedElement));
    					}
    				}
    			}
    		}
    	}
    	return affected;
    }
    protected Object getReferencedObject(Object inputObj)
    {
		if (inputObj instanceof ISystemFilterPoolReference)
		  return ((ISystemFilterPoolReference)inputObj).getReferencedFilterPool();
		else if (inputObj instanceof ISystemFilterReference)
		  return ((ISystemFilterReference)inputObj).getReferencedFilter();
		else if (inputObj instanceof ISystemFilterStringReference)
		  return ((ISystemFilterStringReference)inputObj).getReferencedFilterString();
        else
          return inputObj;
    }
    protected Object getElementParent(Object inputObj)
    {
		if (inputObj instanceof IHost)		  
		  return ((IHost)inputObj).getSystemProfile();
		else if (inputObj instanceof ISubSystem)
		  return ((ISubSystem)inputObj).getHost();
		else if (inputObj instanceof ISystemFilterPoolReference)
		  return ((ISystemFilterPoolReference)inputObj).getProvider(); // will be a subsystem
		else if (inputObj instanceof ISystemFilterPool)
		  return ((ISystemFilterPool)inputObj).getProvider(); // will be a subsystem factory. Hmm!
		else if (inputObj instanceof ISystemFilterReference)
		  return ((ISystemFilterReference)inputObj).getParent(); // will be filter reference or filter pool reference
		else if (inputObj instanceof ISystemFilter)
		{
		  ISystemFilter filter = (ISystemFilter)inputObj;
		  if (filter == null) return null;
		  if (filter.getParentFilter() != null)
		    return filter.getParentFilter();
		  else
		    return filter.getParentFilterPool();
		}
		else if (inputObj instanceof ISystemFilterStringReference)
		  return ((ISystemFilterStringReference)inputObj).getParent(); // will be a SystemFilterReference
		else if (inputObj instanceof ISystemFilterString)
		  return ((ISystemFilterString)inputObj).getParentSystemFilter();
        else
          return null;
    }
    protected IHost getInputConnection(Object inputObj)
    {
		if (inputObj instanceof IHost)		  
		  return (IHost)inputObj;
		else if (inputObj instanceof ISubSystem)
		  return ((ISubSystem)inputObj).getHost();
		else if (inputObj instanceof ISystemFilterPoolReference)
		{
		  ISubSystem ss = (ISubSystem)((ISystemFilterPoolReference)inputObj).getProvider(); // will be a subsystem
		  if (ss == null) return null;
		  return ss.getHost();
		}
		else if (inputObj instanceof ISystemFilterReference)
		{
		  ISubSystem ss = (ISubSystem)((ISystemFilterReference)inputObj).getProvider(); // will be a subsystem
		  if (ss == null) return null;
		  return ss.getHost();
		}
		else if (inputObj instanceof ISystemFilterStringReference)
		{
		  ISubSystem ss = (ISubSystem)((ISystemFilterStringReference)inputObj).getProvider(); // will be a subsystem
		  if (ss == null) return null;
		  return ss.getHost();
		}
        else
          return null;
    }

    /**
     * Handy debug method to print a tree item
     */
    protected String printTreeItem(Item item)
    {
    	if (item == null)
    	  return "";
    	else if (item instanceof TreeItem)
    	{
    		TreeItem ti = (TreeItem)item;
    		return printTreeItem(ti.getParentItem()) + "/" + ti.getText();
    	}
    	else
    	  return item.toString();
    }
    
    /**
     * Delete all occurrences of a given remote object
     */
    protected void deleteRemoteObject(Object deleteObject, ISubSystem subsystem)
    {	    
	    Vector matches = null;
	    String oldElementName = null;
    	   	        
    	// STEP 1: get the object's remote adapter and subsystem
    	if (deleteObject instanceof String)
    	  oldElementName = (String)deleteObject;
    	else
    	{	
	       ISystemRemoteElementAdapter rmtAdapter = getRemoteAdapter(deleteObject);
	       if (rmtAdapter == null)
	           return;
    	   oldElementName = rmtAdapter.getAbsoluteName(deleteObject);
    	   subsystem = rmtAdapter.getSubSystem(deleteObject);
    	}
    	// STEP 2: find all references to the object
        matches = findAllRemoteItemReferences(oldElementName, deleteObject, subsystem, matches);        
        if (matches == null)
        {
        	//System.out.println("matches is null");
            return;
        }

    	boolean wasSelected = false;
    	Item parentItem = null;    	
    	
    	boolean dupes = false;
    	Object prevData = null;
    	for (int idx=0; !dupes && (idx<matches.size()); idx++)
    	{
    		Item match = (Item)matches.elementAt(idx);
    		if ((match instanceof TreeItem) && !((TreeItem)match).isDisposed())
    		{
    		   if (match.getData() == prevData)
    		     dupes = true;
    		   else
    		     prevData = match.getData();
    		}
    	}
        //System.out.println("matches size = " + matches.size() + ", any binary duplicates? " + dupes);
            	
    	// STEP 3: process all references to the object
    	for (int idx=0; idx<matches.size(); idx++)
    	{
    		Item match = (Item)matches.elementAt(idx);
    		//System.out.println("...match " + idx + ": TreeItem? " + (match instanceof TreeItem) + ", disposed? " + ((TreeItem)match).isDisposed());
    		// a reference to this remote object
    		if ((match instanceof TreeItem) && !((TreeItem)match).isDisposed())
    		{    	
    		  TreeItem pItem = ((TreeItem)match).getParentItem();
    		  Object data = match.getData();
    	      if (!wasSelected)
    	      {
    	        //wasSelected = isSelectedOrChildSelected(data);
    	        wasSelected = isTreeItemSelectedOrChildSelected(match);
    	        if (wasSelected)
    	        {
    	          clearSelection();
    	          parentItem = ((TreeItem)match).getParentItem();
    	          //System.out.println("...current item was selected");
    	        }
    	      }
    	      if (dupes) // defect 46818
    	      { // if there are multiple references to the same binary object, ...    	      
    	         //System.out.println(".....calling refresh(data) on this match");
    	         /*
    	         if (pItem!=null)
                     smartRefresh(new TreeItem[] {pItem}); // just refresh the parent node
                 else
                     refreshAll();
                 */
                 disassociate(match);
                 match.dispose();
    	      }
    	      else
    	      {
    	        //System.out.println(".....calling remove(data) on this match");
    	        remove(data); // remove this item from the tree
    	      }
    		}
    	}

        // STEP 4: if we removed a selected item, select its parent
        if (wasSelected && (parentItem != null) && (parentItem instanceof TreeItem) && (parentItem.getData() != null))
        {
    	  //System.out.println("Resetting selection to parent");
          setSelection(new StructuredSelection(parentItem.getData()),true);
        }
    	return;
    }
    
    /**
     * Rename a remote object. Renames all references to it currently displayed in this tree.
     */
    protected void renameRemoteObject(Object renameObject, String oldElementName, ISubSystem subsystem)
    {
	    String[] properties = new String[1];
	    properties[0] = IBasicPropertyConstants.P_TEXT;    	   	        

    	// STEP 0: do we have the physical remote object that has been renamed? If so, update it directly
    	/*
    	Item item = (Item)findItem(renameObject);
    	if (item != null)
    	{
	      update(renameObject, properties); // for refreshing non-structural properties in viewer when model changes
	      if (item instanceof TreeItem)
            smartRefresh(new TreeItem[] {(TreeItem)item}); // we update the kids because they typically store references to their parent
    	}
    	*/
	    
	    Vector matches = null;
    	   	        
    	// STEP 1: get the object's remote adapter and subsystem
    	String newElementName = null;
    	ISystemRemoteElementAdapter rmtAdapter = null;
    	if (renameObject instanceof String)
    	{
    	   newElementName = (String)renameObject;
    	}
    	else
    	{
	       rmtAdapter = getRemoteAdapter(renameObject);
    	   subsystem = rmtAdapter.getSubSystem(renameObject);
    	}
    	          	              
    	// STEP 2: find all references to the old name object
        matches = findAllRemoteItemReferences(oldElementName, renameObject, subsystem, matches);
        if (matches == null)
          return;

        boolean binaryRefreshed = false;
    	// STEP 3: process all references to the old name object
    	for (int idx=0; idx<matches.size(); idx++)
    	{
    		Item match = (Item)matches.elementAt(idx);
    		// a reference to this remote object
    		if ((match instanceof TreeItem) && !((TreeItem)match).isDisposed())
    		{    			
    		  Object data = match.getData();
              boolean refresh = false;              
              if (data != renameObject) // not a binary match
              {
              	if (rmtAdapter == null)
              	  rmtAdapter = getRemoteAdapter(data);
                refresh = rmtAdapter.refreshRemoteObject(data, renameObject); // old, new
              }
              else //if (!binaryRefreshed) 
              {
              	refresh = true;
              	binaryRefreshed = true; // presumably we should only have to refresh the first occurrence. Turns out not to be true!
              }
	          update(data, properties); // for refreshing non-structural properties in viewer when model changes
              //System.out.println("Match found. refresh required? " + refresh);
              if (refresh)
                //refreshRemoteObject(data,null,false);
                smartRefresh(new TreeItem[] {(TreeItem)match});
    		}
    	}

        // STEP 4: update property sheet, just in case.    	
   	    updatePropertySheet();
   	    
    	return;
    }
    
    /**
     * Update properties of remote object. Update all references to this object
     */
    protected void updateRemoteObjectProperties(Object remoteObject)
    {
	    Vector matches = new Vector();
    	   	        
    	// STEP 1: get the object's remote adapter and subsystem
	    ISystemRemoteElementAdapter rmtAdapter = getRemoteAdapter(remoteObject);
    	ISubSystem subsystem = rmtAdapter.getSubSystem(remoteObject);
    	          	              
    	// STEP 2: find all references to the object
    	String  oldElementName = rmtAdapter.getAbsoluteName(remoteObject);
        findAllRemoteItemReferences(oldElementName, remoteObject, subsystem, matches);

    	// STEP 3: process all references to the object
    	String[] allProps = {IBasicPropertyConstants.P_TEXT,IBasicPropertyConstants.P_IMAGE};
    	for (int idx=0; idx<matches.size(); idx++)
    	{
    		Item match = (Item)matches.elementAt(idx);
    		// a reference to this remote object
    		if ((match instanceof TreeItem) && !((TreeItem)match).isDisposed())
    		{    			
    		   Object data = match.getData();
               if (data == remoteObject) // same binary object as given?
	             update(data, allProps); // for refreshing non-structural properties in viewer when model changes
	           else // match by name
	           {
                 rmtAdapter.refreshRemoteObject(data, remoteObject); // old, new
                 update(data, allProps);
	           }
    		}
    	}
    	
    	// STEP 4: update the property sheet in case we changed properties of first selected item
        updatePropertySheet();
    	return;
    }
    
    /**
     * Refresh contents of remote container. Refreshes all references to this container including filters that 
     *  display the contents of this container.
     * @param remoteObject - either an actual remote object, or the absolute name of a remote object
     * @param toSelect - the child object to select after refreshing the given object. This will force the
     *   object to be expanded, and then select this object which can be a remote object or absolute name of a 
     *   remote object. To simply force an expand of the remote object, without bothering to select a child,
     *   pass an instance of SystemViewDummyObject.
     * @return true if a refresh done, false if given a non-remote object.
     */
    protected boolean refreshRemoteObject(Object remoteObject, Object toSelect, boolean originatedHere)
    {
    	if (remoteObject == null)
    	  return false;
    	   	        
    	// STEP 1: get the object's remote adapter and subsystem, or use its name if only given that
	    ISystemRemoteElementAdapter rmtAdapter = null;
    	ISubSystem subsystem = null;
    	String  oldElementName = null;
    	if (!(remoteObject instanceof String))
    	{
	      rmtAdapter = getRemoteAdapter(remoteObject);
	      if (rmtAdapter == null)
	        return false;
    	  subsystem = rmtAdapter.getSubSystem(remoteObject);
    	  oldElementName = rmtAdapter.getAbsoluteName(remoteObject);
    	}
    	else 
    	  oldElementName = (String)remoteObject;

	    Vector matches = new Vector();    	          	              
    	// STEP 2: find all references to the object
        findAllRemoteItemReferences(oldElementName, remoteObject, subsystem, matches);
        if (remoteObject instanceof String) 
          remoteObject = getFirstRemoteObject(matches);

		if (remoteObject instanceof ISystemContainer)
		{
			((ISystemContainer)remoteObject).markStale(true);
		}

    	// STEP 3: process all references to the object
        boolean firstSelection = true;
    	for (int idx=0; idx<matches.size(); idx++)
    	{
    		Item match = (Item)matches.elementAt(idx);
    		// a reference to this remote object
    		if ((match instanceof TreeItem) && !((TreeItem)match).isDisposed())
    		{    			
    		   Object data = match.getData();
               smartRefresh(new TreeItem[] {(TreeItem)match}); // refresh the remote object
               if (firstSelection && // for now, we just select the first binary occurrence we find
                   (data == remoteObject)) // same binary object as given?
               {
    	           firstSelection = false;
    	           if ((toSelect!=null) && originatedHere)
    	           {
    	              if (!getExpanded(match)) // assume if callers wants to select kids that they want to expand parent
    	              {
    	                createChildren(match);
    	                setExpanded(match,true);
    	              }
    	              // todo: handle cumulative selections. 
    	              // STEP 4: If requested, select the kids in the newly refreshed object. 
    	              // If the same binary object appears multiple times, select the kids in the first occurrence.
    	              //  ... what else to do?
    	              if (!(toSelect instanceof SystemViewDummyObject))
    	                selectRemoteObjects(toSelect, null, match); // select the given kids in this parent
    	           }
               }
    		}
    	}
    	return true;
    }
    /**
     * Given the result of findAllRemoteItemReferences, scan for first non-filter object
     */
    protected Object getFirstRemoteObject(Vector matches)
    {
    	if ((matches==null) || (matches.size()==0))
    	  return null;
    	Object firstRemote = matches.elementAt(0);
    	if (firstRemote != null)
    	  firstRemote = ((Item)firstRemote).getData();      	
        return firstRemote;
    }
    /**
     * Refreshes the tree starting at the given widget.
     *
     * @param widget the widget
     * @param element the element
     * @param doStruct <code>true</code> if structural changes are to be picked up,
     *   and <code>false</code> if only label provider changes are of interest
     */
    protected void ourInternalRefresh(Widget widget, Object element, boolean doStruct, boolean forceRemote, boolean doTimings) 
    {	
    	final Widget fWidget = widget;
    	final Object fElement = element;
    	final boolean fDoStruct = doStruct;
    	    	
    	// we have to take special care if one of our kids are selected and it is a remote object...
    	if (forceRemote || (isSelectionRemote() && isTreeItemSelectedOrChildSelected(widget)))
    	{
    	    if (!isTreeItemSelected(widget)) // it is one of our kids that is selected
    	    {
    		  clearSelection(); // there is nothing much else we can do. Calling code will restore it anyway hopefully
    	      doOurInternalRefresh(fWidget, fElement, fDoStruct, doTimings);
    	    }
    	    else // it is us that is selected. This might be a refresh selected operation. TreeItem address won't change
    	    {
    	      doOurInternalRefresh(fWidget, fElement, fDoStruct, doTimings);
    	    }
    	}
        else
    	{
    		final boolean finalDoTimings = doTimings;
    	    preservingSelection(new Runnable() 
    	    {     	      
    		    public void run() 
    		    {
    			    doOurInternalRefresh(fWidget, fElement, fDoStruct, finalDoTimings);
    		    }
    	    });
    	}
    }
    protected boolean isSelectionRemote()
    {
    	ISelection s = getSelection();
    	if ((s!=null)&&(s instanceof IStructuredSelection))
    	{
    		IStructuredSelection ss = (IStructuredSelection)s;
    		Object firstSel = ss.getFirstElement();
    		if ((firstSel != null) && (getRemoteAdapter(firstSel) != null))
    		  return true;
    	}
    	return false;
    }
    protected void doOurInternalRefresh(Widget widget, Object element, boolean doStruct, boolean doTimings) 
    {
    	if (debug)
    	{
    	  logDebugMsg("in doOurInternalRefresh on " + getAdapter(element).getName(element));
    	  logDebugMsg("...current selection is " + getFirstSelectionName(getSelection())); 
        }
        SystemElapsedTimer timer = null;
        if (doTimings)
          timer = new SystemElapsedTimer();
    	if (widget instanceof Item) 
    	{
            //System.out.println("Inside doOurInternalRefresh. widget = " + ((TreeItem)widget).handle);    		
    		if (doStruct) {
    			updatePlus((Item)widget, element);
    		}	
    		updateItem((Item)widget, element);
    		if (doTimings)
    		{
    		  System.out.println("doOurInternalRefresh timer 1: time to updatePlus and updateItem:" + timer.setEndTime());
    		  timer.setStartTime();
    		}
    	}

    	if (doStruct) {
    		// pass null for children, to allow updateChildren to get them only if needed
    		Object[] newChildren = null;
	        if ((widget instanceof Item) && getExpanded((Item)widget))
	        {
	    // DKM - get raw children does a query but so does internalRefresh()
		//    		  newChildren = getRawChildren(widget);
    		  if (doTimings)
    		  {
    		    System.out.println("doOurInternalRefresh timer 2: time to getRawChildren:" + timer.setEndTime());
    		    timer.setStartTime();
    		  }
	        }
		// DKM - without the else we get duplicate queries on expanded folder
		// uncommented - seems new results after query aren't showing up
		//else
		{
    		internalRefresh(element);
		}
		
    		if (doTimings)
    		{
    		    System.out.println("doOurInternalRefresh timer 3: time to updateChildren:" + timer.setEndTime());
    		    timer.setStartTime();
    		}
    	}
    	// recurse
    	Item[] children= getChildren(widget);
    	if (children != null) 
    	{
    		//SystemElapsedTimer timer2 = null;
    		//int intervalCount = 0;
    		//if (doTimings)
    		  //timer2 = new SystemElapsedTimer();
    		for (int i= 0; i < children.length; i++) 
    		{
    			Widget item= children[i];
    			Object data= item.getData();
    			if (data != null)
    				doOurInternalRefresh(item, data, doStruct, false);
    			/*
    			if (doTimings)
    			{
    				++intervalCount;
    				if (intervalCount == 1000)
    				{
    					System.out.println("...time to recurse next 1000 children: " + timer2.setEndTime());
    					intervalCount = 0;
    					timer2.setStartTime();
    				}
    			}*/
    		}
    	}
        if (doTimings)
    	{
    	   System.out.println("doOurInternalRefresh timer 4: time to recurse children:" + timer.setEndTime());
    	   timer.setStartTime();
    	}
    }
    protected Object[] getRawChildren(Widget w) 
    {
    	Object parent = w.getData();
    	if (w != null) 
    	{
    		if (parent.equals(getRoot()))
    			return super.getRawChildren(parent);
    		Object[] result = ((ITreeContentProvider) getContentProvider()).getChildren(parent);
    		if (result != null)
    			return result;
    	}
    	return new Object[0];
    }

    /*
    protected void preservingSelection(Runnable updateCode) 
    {
       super.preservingSelection(updateCode);
       System.out.println("After preservingSelection: new selection = "+getFirstSelectionName(getSelection()));       		
    }
    protected void handleInvalidSelection(ISelection invalidSelection, ISelection newSelection) 
    {
       System.out.println("Inside handleInvalidSelection: old = "+getFirstSelectionName(invalidSelection)+", new = "+getFirstSelectionName(newSelection));       
	   updateSelection(newSelection);
    }
    */
    protected String getFirstSelectionName(ISelection s)
    {
    	if ((s!=null) && (s instanceof IStructuredSelection))
    	{
    	   IStructuredSelection ss = (IStructuredSelection)s;
    	   Object firstSel = ss.getFirstElement();
    	   String name = null;    	   
    	   if (firstSel != null)
    	   {
    	     ISystemRemoteElementAdapter ra = getRemoteAdapter(firstSel);
    	     if (ra != null) 
    	       name = ra.getAbsoluteName(firstSel);
    	     else
    	       name = getAdapter(firstSel).getName(firstSel);
    	   }
           return name;
    	}    	
    	else
    	  return null;
    }

    /**
     * Expand a remote object within the tree. Must be given its parent element within the tree,
     *   in order to uniquely find it. If not given this, we expand the first occurrence we find!
     * @param remoteObject - either a remote object or a remote object absolute name
     * @param subsystem - the subsystem that owns the remote objects, to optimize searches. 
     * @param parentobject - the parent that owns the remote objects, to optimize searches. Can 
     *          be an object or the absolute name of a remote object.
     * @return the tree item of the remote object if found and expanded, else null
     */
    public Item expandRemoteObject(Object remoteObject, ISubSystem subsystem, Object parentObject)
    {
    	// given the parent? Should be easy
    	Item remoteItem = null;
    	if (parentObject != null)
    	{
    		Item parentItem = null;
    		if (parentObject instanceof Item)
    		   parentItem = (Item)parentObject;
    		else if (parentObject instanceof String) // given absolute name of remote object
    	       parentItem = findFirstRemoteItemReference((String)parentObject, subsystem, (Item)null); // search all roots for the parent
    		else // given actual remote object
    		{
   	           ISystemRemoteElementAdapter ra = getRemoteAdapter(parentObject);
   	           if (ra != null)
   	           {
    	          if (subsystem == null)
                     subsystem = ra.getSubSystem(parentObject);
    	          parentItem = findFirstRemoteItemReference(ra.getAbsoluteName(parentObject), subsystem, (Item)null); // search all roots for the parent
   	           }
   	           else // else parent is not a remote object. Probably its a filter
   	           {
   	              Widget parentWidget = findItem(parentObject);
   	              if (parentWidget instanceof Item)
   	                 parentItem = (Item)parentWidget;
   	           }
   	        }
   	        // ok, we have the parent item! Hopefully!
   	        if (remoteObject instanceof String)
   	          remoteItem = findFirstRemoteItemReference((String)remoteObject, subsystem, parentItem);
   	        else
   	          remoteItem = findFirstRemoteItemReference(remoteObject, parentItem);
   	        if (remoteItem == null)
   	          return null;
   	        setExpandedState(remoteItem.getData(), true);
    	}
    	else // not given a parent to refine search with. Better have a subsystem!!
    	{
            remoteItem = null;
    		if (remoteObject instanceof String)
    	      remoteItem = findFirstRemoteItemReference((String)remoteObject, subsystem, (Item)null); 
    	    else 
    	    {
   	           ISystemRemoteElementAdapter ra = getRemoteAdapter(remoteObject);
   	           if (ra != null)
   	           {
    	          if (subsystem == null)
                     subsystem = ra.getSubSystem(remoteObject);
    	          remoteItem = findFirstRemoteItemReference(ra.getAbsoluteName(remoteObject), subsystem, (Item)null);                    
   	           }
    	    }
    	    if (remoteItem == null)
    	      return null;
    	    setExpandedState(remoteItem.getData(), true);
    	}
    	return remoteItem;
    }

    /**
     * Select a remote object or objects given the parent remote object (can be null) and subsystem (can be null)
     * @param src - either a remote object, a remote object absolute name, or a vector of remote objects or remote object absolute names
     * @param subsystem - the subsystem that owns the remote objects, to optimize searches.
     * @param parentobject - the parent that owns the remote objects, to optimize searches.
     * @return true if found and selected
     */
    public boolean selectRemoteObjects(Object src, ISubSystem subsystem, Object parentObject)
    {
    	//String parentName = null;
    	// given a parent object? That makes it easy...
    	if (parentObject != null)
    	{
   	      ISystemRemoteElementAdapter ra = getRemoteAdapter(parentObject);
   	      if (ra != null)
   	      {
   	         //parentName = ra.getAbsoluteName(parentObject);
    	     if (subsystem == null)
               subsystem = ra.getSubSystem(parentObject);
    	     Item parentItem = (Item)findFirstRemoteItemReference(parentObject, (Item)null); // search all roots for the parent
    	     return selectRemoteObjects(src, subsystem, parentItem);
   	      }
   	      else // else parent is not a remote object. Probably its a filter
   	      {
   	      	 Item parentItem = null;
   	      	 if (parentObject instanceof Item)
   	      	    parentItem = (Item)parentObject;
   	      	 else
   	      	 {
   	            Widget parentWidget = findItem(parentObject);
   	            if (parentWidget instanceof Item)
   	              parentItem = (Item)parentWidget;
   	      	 }
   	         if (parentItem != null)
   	           return selectRemoteObjects(src, (ISubSystem)null, parentItem);
   	         else
   	           return false;
   	      }
    	}
    	else
    	  //return selectRemoteObjects(src, (SubSystem)null, (Item)null); // Phil test
    	  return selectRemoteObjects(src, subsystem, (Item)null);
    }
    /**
     * Select a remote object or objects given the parent remote object (can be null) and subsystem (can be null) and parent TreeItem to 
     *  start the search at (can be null)
     * @param src - either a remote object, a remote object absolute name, or a vector of remote objects or remote object absolute names
     * @param subsystem - the subsystem that owns the remote objects, to optimize searches.
     * @param parentItem - the parent at which to start the search to find the remote objects. Else, starts at the roots.
     * @return true if found and selected
     */
    protected boolean selectRemoteObjects(Object src, ISubSystem subsystem, Item parentItem)
    {
    	clearSelection();
        Item selItem = null;
		
		 if (parentItem != null && parentItem.isDisposed()) {
	         return false;
	        }
                
        if ((parentItem!=null) && !getExpanded(parentItem))
    	  //setExpanded(parentItem, true);
    	  setExpandedState(parentItem.getData(), true);

    	//System.out.println("SELECT_REMOTE: PARENT = " + parent + ", PARENTITEM = " + parentItem);
    	if (src instanceof Vector)
    	{
    	    String elementName = null;
   	    	Vector selVector = (Vector)src;
   	    	ArrayList selItems = new ArrayList();
   	    	// our goal here is to turn the vector of names or remote objects into a collection of
   	    	// actual TreeItems we matched them on...
   	    	for (int idx=0; idx<selVector.size(); idx++)
   	    	{
   	    	    Object o = selVector.elementAt(idx);
   	    	    elementName = null;
   	    	    if (o instanceof String)
                  selItem = (Item)findFirstRemoteItemReference((String)o, subsystem, parentItem);
                else
                  selItem = (Item)findFirstRemoteItemReference(o, parentItem);                      

                if (selItem != null)
                {
                   selItems.add(selItem);
                   // when selecting multiple items, we optimize by assuming they have the same parent...
                   if ((parentItem == null) && (selItem instanceof TreeItem))
                     parentItem = ((TreeItem)selItem).getParentItem();
                }
   	    	}
   	    	if (selItems.size() > 0)
   	    	{
   	    	   setSelection(selItems);
   	    	   updatePropertySheet();
   	    	   return true;
   	    	}
    	}
    	else
    	{
   	    	if (src instanceof String)
              //selItem = (Item)findFirstRemoteItemReference((String)src, (SubSystem)null, parentItem); Phil test
              selItem = (Item)findFirstRemoteItemReference((String)src, subsystem, parentItem);
            else
              selItem = (Item)findFirstRemoteItemReference(src, parentItem);                      

    	   	if (selItem != null)
    	   	{
   	    	  ArrayList selItems = new ArrayList();
   	    	  selItems.add(selItem); 	    	  
   	    	  setSelection(selItems);
   	    	  updatePropertySheet();
   	    	  return true;
    	   	}
    	}
    	return false;
    }


    /**
     * Refresh the whole tree. We have special code to reselect remote objects after the refresh
     */
    public void refreshAll()
    {
    	IStructuredSelection selected = (IStructuredSelection)getSelection();
    	Iterator i = selected.iterator();
    	Object  parentElement = null;
    	Vector  selectedRemoteObjects = new Vector();
    	Widget  parentItem = null;
    	ISubSystem ss = null;
    	while (i.hasNext())
    	{
    	   	  Object element = i.next();
    	   	  if (parentElement == null)
    	   	  {
    	   	      parentItem = getParentItem((Item)findItem(element));
    	   	      if ((parentItem != null) && (parentItem instanceof Item))
    	   	        parentElement = ((Item)parentItem).getData();
    	   	  }
    	   	  if (getRemoteAdapter(element) != null)
    	   	  {
    	   	    selectedRemoteObjects.addElement(element);
    	   	    if (ss == null)
    	   	      ss = getRemoteAdapter(element).getSubSystem(element);
    	   	  }
    	}
    	
    	//super.refresh();
    	smartRefresh((Object)null, (selectedRemoteObjects.size() > 0));
    	
    	if (selectedRemoteObjects.size() > 0)
    	{
    	    selectRemoteObjects(selectedRemoteObjects, ss, parentElement);
    	}
    }           

    /**
     * Do an intelligent refresh of an expanded item. The inherited algorithm for refresh is stupid,
     * in that it reexpands children based on their original ordinal position which can change after a
     * refresh, resulting in the wrong children being expanded. Currently this only truly comes to light
     * for remote objects, where refresh really can change the resulting list and hence each child's
     * ordinal position. So, to be safe we only override the inherited algorithm if any nested child
     * is a remote object
     */
    protected void smartRefresh(TreeItem[] itemsToRefresh)
    {
    	smartRefresh(itemsToRefresh, null, false);
    }
    
    protected void smartRefresh(TreeItem[] itemsToRefresh, ArrayList expandedChildren, boolean forceRemote)
    {
    	SystemElapsedTimer timer = null;
    	if (doTimings)
    	  timer = new SystemElapsedTimer();
        areAnyRemote = false; // set in ExpandedItem constructor
        boolean fullRefresh = false;
        // for each selected tree item gather a list of expanded child nodes...
        if (expandedChildren == null)
          expandedChildren = new ArrayList();
        else  
          fullRefresh = true;
        boolean[] wasExpanded = new boolean[itemsToRefresh.length];
        boolean anyGivenItemsRemote = false;
        for (int idx=0; idx<itemsToRefresh.length; idx++)
        {
        	TreeItem currItem = itemsToRefresh[idx];
            // ...if this selected item is expanded, recursively gather up all its expanded descendents
           	Object data = currItem.getData();
           	ISystemViewElementAdapter adapter = null;
           	if (data != null)
           	  adapter = getAdapter(data);
            if (adapter instanceof ISystemRemoteElementAdapter)
              anyGivenItemsRemote = true;
           	if (currItem.getExpanded() && (adapter!=null) && adapter.isPromptable(data))
              setExpandedState(data, false); // collapse temp expansion of prompts
           	else if (currItem.getExpanded())
            {
               //expandedChildren.add(new ExpandedItem(currItem)); we don't need special processing for given items themselves as they will not be refreshed, only their kids
     	       if (doTimings)
     	         timer.setStartTime();
               gatherExpandedChildren((fullRefresh ? null : currItem), currItem, expandedChildren);
               wasExpanded[idx] = true;
               if (doTimings)
                 System.out.println("Refresh Timer 1: time to gatherExpandedChildren: " + timer.setEndTime());
            }
            else
              wasExpanded[idx] = false;
        }    
        // ok, we have found all expanded descendents of all selected items. 

        // If none of the expanded sub-nodes are remote simply use the inherited algorithm for refresh
        if (!areAnyRemote)
        {
        	for (int idx=0; idx<itemsToRefresh.length; idx++)
               //ourInternalRefresh(itemsToRefresh[idx], itemsToRefresh[idx].getData(), wasExpanded[idx]);
               ourInternalRefresh(itemsToRefresh[idx], itemsToRefresh[idx].getData(), true, forceRemote, doTimings); // defect 42321
            return;
        }
		getControl().setRedraw(false);  
        // If any selected nodes are remote use our own algorithm:
        // 1. collapse each given node and refresh it to remove the children from memory, then
        //    expand it again. It doesn't matter if it is remote or not since its own memory
        //    address won't change, only that of its children.
        for (int idx=0; idx<itemsToRefresh.length; idx++)
        {
        	TreeItem currItem = itemsToRefresh[idx];
     	    if (doTimings)
     	      timer.setStartTime();
            setExpanded(currItem, false); // collapse node
            if (doTimings)
            {
              System.out.println("Refresh Timer 2: time to setExpanded(false): " + timer.setEndTime());
              timer.setStartTime();
            }
        	ourInternalRefresh(currItem, currItem.getData(), true, true, doTimings); // dispose of children, update plus
            if (doTimings)
            {
              System.out.println("Refresh Timer 3: time to do ourInternalRefresh(...): " + timer.setEndTime());
              timer.setStartTime();
            }
            if (wasExpanded[idx])
            {
              createChildren(currItem); // re-expand
              if (doTimings)
              {
                 System.out.println("Refresh Timer 4: time to createChildren(...): " + timer.setEndTime());
                 timer.setStartTime();
              }
              currItem.setExpanded(true);
              if (doTimings)
              {
                 System.out.println("Refresh Timer 5: time to setExpanded(true): " + timer.setEndTime());
                 timer.setStartTime();
              }
            }
            else // hmm, item was not expanded so just flush its memory
            {
            	
            	
            }
        }        
        // 2. expand each previously expanded sub-node, recursively        
        for (int idx=0; idx<expandedChildren.size(); idx++)
        {
        	ExpandedItem itemToExpand = (ExpandedItem)expandedChildren.get(idx);
        	if (itemToExpand.isRemote())
        	{
              // find remote item based on its original name and unchanged root parent 
     	      Item item = null;
     	      //if (itemToExpand.parentItem != null)
     	        //item = (Item)recursiveFindRemoteItem(itemToExpand.parentItem, itemToExpand.remoteName, itemToExpand.subsystem);
     	      //else
     	        //item = (Item)findRemoteItem(itemToExpand.remoteName, itemToExpand.subsystem);
     	      item = findFirstRemoteItemReference(itemToExpand.remoteName, itemToExpand.subsystem, itemToExpand.parentItem);
     	      // if found, re-expand it
     	      if (item != null)
     	      {
     	        //setExpanded(item, true);
     	        createChildren(item);
     	        ((TreeItem)item).setExpanded(true);
        	    if (debug)
    		      System.out.println("Re-Expanded RemoteItem: " + itemToExpand.remoteName);
     	      }
     	      else if (debug)
    		    System.out.println("Re-Expand of RemoteItem '" + itemToExpand.remoteName + "' failed. Not found");
        	}
        	else
        	{
              setExpandedState(itemToExpand.data, true);
              if (debug)
    		    System.out.println("Re-Expanded non-remote Item: " + itemToExpand.data);
        	}
        }        
        if (doTimings)
        {
           System.out.println("Refresh Timer 6: time to reExpanded expanded subnodes: " + timer.setEndTime());
           timer.setStartTime();
        }
		getControl().setRedraw(true);  
        if (doTimings)
        {
           System.out.println("Refresh Timer 7: time to setRedraw(true): " + timer.setEndTime());
           timer.setStartTime();
        }
    }
    /**
     * Do an intelligent refresh of the given element. Can be null for full refresh
     */
    protected void smartRefresh(Object element, boolean forceRemote)
    {
    	if ((element == null) || (element == getInput()))
    	{
    	  // fullRefresh
    	  Tree tree = getTree();
    	  TreeItem[] roots = tree.getItems();
    	  boolean anyExpanded = false;
    	  areAnyRemote = false; // set in ExpandedItem constructor
          ArrayList expandedChildren = new ArrayList();
    	  if (roots != null)
    	  {
    	    for (int idx=0; idx<roots.length; idx++)
    	    {
    	       TreeItem currItem = roots[idx];
           	   Object data = currItem.getData();
           	   ISystemViewElementAdapter adapter = null;
           	   if (data != null)
           	     adapter = getAdapter(data);
           	   if (currItem.getExpanded() && (adapter!=null) && adapter.isPromptable(data))
                 setExpandedState(data, false);
    	       else if (currItem.getExpanded())
    	       {
    	         //setExpanded(roots[idx], false);	
                 expandedChildren.add(new ExpandedItem(null, currItem));
    	         anyExpanded = true;
                 //gatherExpandedChildren(currItem, currItem, expandedChildren);
    	       }
    	    }
    	  }
    	  if (!anyExpanded)
    	    super.refresh();
    	  else
    	  {
    	  internalRefresh(getInput());
    	    roots = tree.getItems(); // re-query roots 	
    	    smartRefresh(roots, expandedChildren, forceRemote);
    	  }
    	}
    	else if (getRemoteAdapter(element) != null)
    	{
    	
    		Item item = null;
        	  if (element instanceof String)
        	  {
        	    item = findFirstRemoteItemReference((String)element, (ISubSystem)null, (Item)null);
        	    if (item != null)
        	    {
        	    	smartRefresh(new TreeItem[]{(TreeItem)item});
        	    }
        	  }
        	  else
        	  {
        		  ISystemRemoteElementAdapter adapter = getRemoteAdapter(element);
        		  String elementName = adapter.getName(element);
        		  ISubSystem subSystem = adapter.getSubSystem(element);
        		  
        		  Vector matches = new Vector();
        		  findAllRemoteItemReferences(elementName, element, subSystem, matches);
        		  if (matches.size() > 0)
        		  {
        			  for (int i = 0; i < matches.size(); i++)
        			  {
        				  Item match = (Item)matches.get(i);
        				  if ((match instanceof TreeItem) && !((TreeItem)match).isDisposed())
        				  {
        					  smartRefresh(new TreeItem[]{(TreeItem)match});
        				  }
        			  }
        		  }        		  
        	  }
        	  
   
    		/*
    	  Item item = null; 
    	  if (element instanceof String)
    	    item = findFirstRemoteItemReference((String)element, (SubSystem)null, (Item)null);
    	  else
    	    item = findFirstRemoteItemReference(element, (Item)null);
    	  if (item != null)
    	    smartRefresh(new TreeItem[] {(TreeItem)item});
    	    
    	    */
    	}
    	else
    	{
    	  Item item = (Item)findItem(element);
    	  //System.out.println("Inside SV smartRefresh for "+element+". Item found? " + (item!=null));
    	  if (item != null)
    	    smartRefresh(new TreeItem[] {(TreeItem)item});
    	}
    }
    
    class ExpandedItem
    {
    	TreeItem item, parentItem;
    	Object   data;
    	String   remoteName;
    	ISystemRemoteElementAdapter remoteAdapter;
    	ISubSystem subsystem;
    	ExpandedItem(TreeItem parentItem, TreeItem item)
    	{
    		this.parentItem = parentItem;
    		this.item = item;
    		this.data = item.getData();
    		if (data != null)
    		{
    			remoteAdapter = getRemoteAdapter(data);
    			if (remoteAdapter != null)
    			{
    			  remoteName = remoteAdapter.getAbsoluteName(data);
    			  subsystem = remoteAdapter.getSubSystem(data);
                  areAnyRemote = true;
                  if (debug)
    		        System.out.println("ExpandedRemoteItem added. remoteName = " + remoteName);
    			}
    		    else if (debug)
    		      System.out.println("ExpandedItem added. Data = " + data);
    		}
    		else if (debug)
    		  System.out.println("ExpandedItem added. Data = null");
    	}
    	boolean isRemote()
    	{
    		return (remoteAdapter != null);
    	}
    }
    
    /**
     * Gather up all expanded children of the given tree item into a list that can be used later to
     * reexpand.
     * @param parentItem The root parent which will not be refreshed itself (only its kids) and hence will remain valid after refresh.
     *                    In a full refresh this will be null.
     * @param startingItem The starting item for this search. Usually same as parentItem, but changes via recursion
     * @param listToPopulate An array list that will be populated with instances of our inner class ExpandedItem
     */
    protected void gatherExpandedChildren(TreeItem parentItem, TreeItem startingItem, ArrayList listToPopulate)
    {
         TreeItem[] itemChildren = startingItem.getItems();
         if (itemChildren != null)
         {
           for (int idx=0; idx<itemChildren.length; idx++)
           {
           	  TreeItem currChild = itemChildren[idx];
           	  Object data = currChild.getData();
           	  ISystemViewElementAdapter adapter = null;
           	  if (data != null)
           	    adapter = getAdapter(data);
           	  if (currChild.getExpanded() && (adapter!=null) && adapter.isPromptable(data))
                 setExpandedState(data, false);
              else if (currChild.getExpanded())
           	  {
                listToPopulate.add(new ExpandedItem(parentItem, currChild));
                gatherExpandedChildren(parentItem, currChild, listToPopulate);
           	  }
           }
         }           
    }
    
    /**
     * Get index of item given its data element
     */
    protected int getItemIndex(Widget parent, Object element)
    {
    	int index = -1;
    	Item[] kids = getChildren(parent);
    	if (kids!=null)
    	  for (int idx=0; idx<kids.length; idx++)
    	     if (kids[idx].getData() == element)
    	       index = idx;
    	return index;
    }

    /**
     * We don't show actual filters, only filter references that are unique generated
     *  for each subtree of each subsystem. Yet, each event is relative to the filter,
     *  not our special filter references. Hence, all this code!!
     * <p>
     * Special case handling for updates to filters which affect the filter
     *  but not the filter parent:
     *   1. Existing filter renamed (RENAME)
     *   2. Existing filter's filter strings changed (CHANGE)
     * <p>
     * Assumption:
     *   1. event.getGrandParent() == subsystem (one event fired per affected subsystem)
     *   2. event.getSource() == filter or filter string (not the reference, the real filter or string)
     *   3. event.getParent() == parent of filter or filter string. One of:
     *      a. filterPool reference or filter reference (nested)
     *      b. filterPool for non-nested filters when showing filter pools
     *      c. subsystem for non-nested filters when not showing filter pools
     *      d. filter for nested filters
     * <p>
     * Our job here:
     *   1. Determine if we are even showing the given subsystem
     *   2. Find the reference to the updated filter in that subsystem's subtree
     *   3. Ask that parent to either update its name or collapse and refresh its children
     *   4. Forget selecting something ... the original item remains selected!
     */
    protected void findAndUpdateFilter(ISystemResourceChangeEvent event, int type)
    {
    	ISystemFilter filter = (ISystemFilter)event.getSource();
    	//Object parent = event.getParent();
    	if (debug)
    	{
    	  String eventType = null;
    	  switch(type)
    	  {
    	  	case EVENT_RENAME_FILTER_REFERENCE:
    	  	   eventType = "EVENT_RENAME_FILTER_REFERENCE";
    	  	   break;
    	  	case EVENT_CHANGE_FILTER_REFERENCE:
    	  	   eventType = "EVENT_CHANGE_FILTER_REFERENCE";
    	  	   break;
    	  }
    	  logDebugMsg("SV event: "+eventType);
    	}
    	  
    	// STEP 1. ARE WE EVEN SHOWING THE GIVEN SUBSYSTEM?
    	ISubSystem ss = (ISubSystem)event.getGrandParent();
    	Widget widget = findItem(ss);
    	    	   	      
    	if (widget != null) 
    	{
    	  
		  // STEP 2: ARE WE SHOWING A REFERENCE TO RENAMED OR UPDATED FILTER?
		  Widget item = null;
		  
		  Control c = getControl();
		  
    	  // KM: defect 53008.
    	  // Yes we are showing the subsystem, so widget is the subsystem item
    	  if (widget != c && widget instanceof Item) {

			if (debug)
			  logDebugMsg("...Found ss " + ss);
			  
			item = internalFindReferencedItem(widget, filter, SEARCH_INFINITE);
    	  }
    	  // No, we are not showing the subsystem, so widget is the control
    	  else if (widget == c) {

			if (debug)
			  logDebugMsg("...Din not find ss " + ss);
			  
    	  	item = internalFindReferencedItem(widget, filter, SEARCH_INFINITE);
    	  }
    	  
    	  if (item == null)
    	    logDebugMsg("......didn't find renamed/updated filter's reference!");
    	  else
    	  {
    	    // STEP 3: UPDATE THAT FILTER...
    	    if (type == EVENT_RENAME_FILTER_REFERENCE)
    	    {
	          String[] rproperties = {IBasicPropertyConstants.P_TEXT};
	          update(item.getData(), rproperties); // for refreshing non-structural properties in viewer when model changes   	   	      
    	    }
    	    else if (type == EVENT_CHANGE_FILTER_REFERENCE)
    	    {
    	      //if (((TreeItem)item).getExpanded())    	      
                //refresh(item.getData()); 
              smartRefresh(new TreeItem[] {(TreeItem)item});
              /*
              Object data = item.getData();
              boolean wasExpanded = getExpanded((Item)item);                                
              setExpandedState(data, false); // collapse node
              refresh(data); // clear all cached widgets
              if (wasExpanded)
                setExpandedState(data, true); // by doing this all subnodes that were expanded are now collapsed
              */
    	    }
   	    	updatePropertySheet();
    	  }    	          	  
    	}    	
    }
    protected void findAndUpdateFilterString(ISystemResourceChangeEvent event, int type)
    {
    	ISystemFilterString filterString = (ISystemFilterString)event.getSource();
    	// STEP 1. ARE WE EVEN SHOWING THE GIVEN SUBSYSTEM?
    	ISubSystem ss = (ISubSystem)event.getGrandParent();
    	Widget item = findItem(ss);
    	if (item != null && item != getControl()) 
    	{
    	  Item ssItem = (Item)item;
    	  if (debug)
    	    logDebugMsg("...Found ss "+ss);
    	  // STEP 2: ARE WE SHOWING A REFERENCE TO THE UPDATED FILTER STRING?
    	  item = internalFindReferencedItem(ssItem, filterString, SEARCH_INFINITE);
    	  if (item == null)
    	    logDebugMsg("......didn't find updated filter string's reference!");
    	  else
    	  {
    	    // STEP 3: UPDATE THAT FILTER STRING...
    	    if (type == EVENT_CHANGE_FILTERSTRING_REFERENCE) // HAD BETTER!
    	    {
    	      //if (((TreeItem)item).getExpanded())
                //refresh(item.getData()); 
              // boolean wasExpanded = getExpanded((Item)item);                  
              Object data = item.getData();
              setExpandedState(data, false); // collapse node
              refresh(data); // clear all cached widgets
              //if (wasExpanded)
                //setExpandedState(data, true); // hmm, should we?
	          String properties[] = {IBasicPropertyConstants.P_TEXT};
	          update(item.getData(), properties); // for refreshing non-structural properties in viewer when model changes   	   	      
   	    	  updatePropertySheet();
    	    }
    	  }    	          	  
    	}     	
    }

    /**
     * We don't show actual filters, only filter references that are unique generated
     *  for each subtree of each subsystem. Yet, each event is relative to the filter,
     *  not our special filter references. Hence, all this code!!
     * <p>
     * Special case handling for updates to filters which affect the parent of the 
     *  filter, such that the parent's children must be re-generated:
     *   1. New filter created (ADD)
     *   2. Existing filter deleted (DELETE)
     *   3. Existing filters reordered (MOVE)
     * <p>
     * Assumption:
     *   1. event.getGrandParent() == subsystem (one event fired per affected subsystem)
     *   2. event.getSource() == filter (not the reference, the real filter)
     *   3. event.getParent() == parent of filter. One of:
     *      a. filterPool reference or filter reference (nested)
     *      b. filterPool for non-nested filters when showing filter pools
     *      c. subsystem for non-nested filters when not showing filter pools
     *      d. filter for nested filters
     * <p>
     * Our job here:
     *   1. Determine if we are even showing the given subsystem
     *   2. Find the parent to the given filter: filterPool or subsystem
     *   3. Ask that parent to refresh its children (causes re-gen of filter references)
     *   4. Select something: QUESTION: is this subsystem the origin of this action??
     *      a. For ADD, select the newly created filter reference for the new filter
     *           ANSWER: IF PARENT OF NEW FILTER IS WITHIN THIS SUBSYSTEM, AND WAS SELECTED PREVIOUSLY
     *      b. For DELETE, select the parent of the filter?
     *           ANSWER: IF DELETED FILTER IS WITHING THIS SUBSYSTEM AND WAS SELECTED PREVIOUSLY
     *      c. For MOVE, select the moved filters
     *           ANSWER: IF MOVED FILTERS ARE WITHIN THIS SUBSYSTEM, AND WERE SELECTED PREVIOUSLY
     */
    protected void findAndUpdateFilterParent(ISystemResourceChangeEvent event, int type)
    {
    	ISubSystem ss = (ISubSystem)event.getGrandParent();
    	boolean add = false, move = false, delete = false;
    	boolean afilterstring = false;
        //if (debug)
        //{
    	  String eventType = null;
    	  switch(type)
    	  {
    	  	case EVENT_ADD_FILTER_REFERENCE:
    	  	   add = true;
    	  	   if (debug)
    	  	     eventType = "EVENT_ADD_FILTER_REFERENCE";
    	  	   break;
    	  	case EVENT_DELETE_FILTER_REFERENCE:
    	  	   delete = true;
    	  	   if (debug)
    	  	     eventType = "EVENT_DELETE_FILTER_REFERENCE";
    	  	   break;
    	  	case EVENT_MOVE_FILTER_REFERENCES:
    	  	   move = true;    	  	   
    	  	   if (debug)
    	  	     eventType = "EVENT_MOVE_FILTER_REFERENCES";
    	  	   break;
    	  	case EVENT_ADD_FILTERSTRING_REFERENCE:
    	  	   add = true;
    	  	   afilterstring = true;
    	  	   if (debug)
    	  	     eventType = "EVENT_ADD_FILTERSTRING_REFERENCE";
    	  	   break;
    	  	case EVENT_DELETE_FILTERSTRING_REFERENCE:
    	  	   delete = true;
    	  	   afilterstring = true;
    	  	   if (debug)
    	  	     eventType = "EVENT_DELETE_FILTERSTRING_REFERENCE";
    	  	   break;
    	  	case EVENT_MOVE_FILTERSTRING_REFERENCES:
    	  	   move = true;
    	  	   afilterstring = true;
    	  	   if (debug)
    	  	     eventType = "EVENT_MOVE_FILTERSTRING_REFERENCES";
    	  	   break;

    	  }
    	  if (debug)
    	    logDebugMsg("SV event: "+eventType);
        //}
    	//clearSelection();
    	  
    	ISystemFilter filter = null;
      	ISystemFilterString filterstring = null; 
      	if (!afilterstring)
      	  filter = (ISystemFilter)event.getSource(); // for multi-source move, gets first filter
      	else
      	  filterstring = (ISystemFilterString)event.getSource(); 
    	  
    	boolean multiSource = move;
    	// STEP 1: ARE WE SHOWING THE SUBSYSTEM GRANDPARENT OF CURRENT REFRESH?
    	Widget item = findItem(ss);    	   	      
  
    	if (item == null)
    	{
    		refresh();
    	
    	  if (debug)
    	    logDebugMsg("...Did not find ss "+ss.getName());
    	  return;
    	}
    	Item ssItem = (Item)item;
        boolean wasSelected = false;
   	    IStructuredSelection oldSelections = (IStructuredSelection)getSelection();
 
    	
    	
    	Object parent = event.getParent();
    	if (debug)
    	  logDebugMsg("...Found ss "+ss);

    	// STEP 2: ARE WE SHOWING A REFERENCE TO THE FILTER's PARENT POOL?
    	Item parentRefItem = null;    	    	
    	ISystemFilterContainer refdParent = null;
    	// 3a (reference to filter pool or filter)
    	if (parent instanceof ISystemFilterContainerReference) // given a reference to parent?
    	{
    	  refdParent = ((ISystemFilterContainerReference)parent).getReferencedSystemFilterContainer();
    	  parentRefItem = (Item)internalFindReferencedItem(ssItem, refdParent, SEARCH_INFINITE);
    	}
    	// 3b and 3d. (filter pool or filter)
    	else if (parent instanceof ISystemFilterContainer)
    	{
    	  refdParent = (ISystemFilterContainer)parent;
    	  parentRefItem = (Item)internalFindReferencedItem(ssItem, refdParent, SEARCH_INFINITE);
    	}
    	// 3c (subsystem)
    	else 
    	{
          parentRefItem = ssItem;
    	}
    	if (parentRefItem != null)
    	{
            if (debug)
              logDebugMsg("......We are showing reference to parent");
            // STEP 3... YES, SO REFRESH PARENT... IT WILL RE-GEN THE FILTER REFERENCES FOR EACH CHILD FILTER
            //  ... actually, call off the whole show if that parent is currently not expanded!!
            // HMMM... WE NEED TO REFRESH EVEN IF NOT EXPANDED IF ADDING FIRST CHILD
            if (!add) // move or delete
            {
              if ( !(((TreeItem)parentRefItem).getExpanded()))
              {
                refresh(parentRefItem.getData()); // flush cached widgets so next expand is fresh
                return;
              }
              // move or delete and parent is expanded...
              Item oldItem = (Item)internalFindReferencedItem(parentRefItem, afilterstring?(Object)filterstring:(Object)filter, 1);
              //if (debug)
                //logDebugMsg("oldItem null? " + (oldItem==null));
              if (oldItem != null) // found moved or deleted filter in our subtree
              {
                wasSelected = isSelected(oldItem.getData(), oldSelections); // was it selected before?
                //if (debug)
                  //logDebugMsg("was selected? " + wasSelected);
              }
              else
              {
                // else interesting case ... we are showing the parent, but can't find the child!
              }
              if (move)
              {
              	Object[] srcObjects = null;
              	if (multiSource)
              	  srcObjects = event.getMultiSource();
              	else
              	{
              	   srcObjects = new Object[1];
              	   srcObjects[0] = event.getSource();
              	}
                moveReferencedTreeItems(parentRefItem, srcObjects, event.getPosition());
                //refresh(parentRefItem.getData());
              }
              else // remove
              {
              	remove(oldItem.getData());
              }
            }
            else // add operation
            {               
               if ( !(((TreeItem)parentRefItem).getExpanded()))
               {
               	  refresh(parentRefItem.getData()); // delete cached GUIs
               	  //setExpandedState(parentRefItem,true); // not our job to expand here.
               } 
               else if (afilterstring)
               {
               	  ISystemFilterReference fr = (ISystemFilterReference)parentRefItem.getData();
               	  ISystemFilterStringReference fsr = fr.getSystemFilterStringReference(filterstring);
    	   	      createTreeItem(parentRefItem, fsr, event.getPosition());
    	   	      //setSelection(new StructuredSelection(fsr),true);
               }
               else
               {
               	  Object data = parentRefItem.getData();
               	  if (data instanceof ISystemFilterContainerReference)
               	  {
               	    ISystemFilterContainerReference sfcr = (ISystemFilterContainerReference)data;
               	    ISystemFilterReference sfr = sfcr.getSystemFilterReference(ss, filter);               	   
    	   	        createTreeItem(parentRefItem, sfr, event.getPosition());
               	  }
               	  else // hmm, could be parent is a subsystem, child is a filter in no-show-filter-pools mode
               	  {
               	  	if (data instanceof ISystemFilterPoolReferenceManagerProvider) // that's a subsystem!
               	  	{
               	  	  ISystemFilterPoolReferenceManagerProvider sfprmp = (ISystemFilterPoolReferenceManagerProvider)data;
               	  	  ISystemFilterPoolReferenceManager sfprm = sfprmp.getSystemFilterPoolReferenceManager();
               	  	  ISystemFilterReference sfr = sfprm.getSystemFilterReference(ss, filter);               	  	  
    	   	          createTreeItem(parentRefItem, sfr, sfprm.getSystemFilterReferencePosition(sfr));
               	  	}
               	  }
               }               
               //refresh(parentRefItem.getData());            	
            }

    	    // STEP 4: DECIDE WHAT TO SELECT:
    	    
    	    // 4a. ADD ... only select if parent of new filter was previously selected...
    	    if (add && isSelected(parentRefItem.getData(),oldSelections))
    	    {
    	   	    if (debug)
    	   	      logDebugMsg(".........that parent was previously selected");
    	        // .... YES, SO SELECT NEW FILTER'S REFERENCE
    	   	    Item filterItem = (Item)internalFindReferencedItem(parentRefItem, afilterstring?(Object)filterstring:(Object)filter, 1); // start at filter's parent, search for filter
    	   	    if (filterItem == null)
    	   	    {
    	   	      if (debug)
    	   	        logDebugMsg("Hmm, didn't find new filter's reference!");
    	   	    }
    	   	    else
    	   	    {
    	   	      if (debug)
    	   	        logDebugMsg(".........Trying to set selection to " + filterItem.getData());
    	   	      setSelection(new StructuredSelection(filterItem.getData()),true);
    	   	    }
    	    }
    	    // 4b. DELETE ... select parent if deleted filter was previously selected
    	    else if (delete && wasSelected)
    	    {     	      	    	  
   	    	  setSelection(new StructuredSelection(parentRefItem.getData())); // select parent
    	    }
    	    // 4c. MOVE ... only select if any of moved references were previously selected...
    	    else if (move && wasSelected && !afilterstring)
    	    {
   	    	  ISystemFilter[] filters = (ISystemFilter[])event.getMultiSource();   	    	  
   	    	  if (filters != null)
   	    	  {
   	    	  	ISystemFilterReference[] newRefs = new ISystemFilterReference[filters.length];
   	    	  	for (int idx=0; idx<newRefs.length; idx++)
   	    	  	{
   	               Widget w = internalFindReferencedItem((Item)parentRefItem,filters[idx],1);
   	               newRefs[idx] = (ISystemFilterReference)((Item)w).getData();
   	    	  	}   	    	  	
   	    	    setSelection(new StructuredSelection(newRefs),true);
   	    	  }
    	    }
    	    else if (move && wasSelected && afilterstring)
    	    {
   	    	  ISystemFilterString[] filterStrings = (ISystemFilterString[])event.getMultiSource();   	    	  
   	    	  if (filterStrings != null)
   	    	  {
   	    	  	ISystemFilterStringReference[] newRefs = new ISystemFilterStringReference[filterStrings.length];
   	    	  	for (int idx=0; idx<newRefs.length; idx++)
   	    	  	{
   	               Widget w = internalFindReferencedItem((Item)parentRefItem,filterStrings[idx],1);
   	               newRefs[idx] = (ISystemFilterStringReference)((Item)w).getData();
   	    	  	}   	    	  	
   	    	    setSelection(new StructuredSelection(newRefs),true);
   	    	  }
    	    }

        }
    	else if (debug)
    	  logDebugMsg("Did not find parent ref " + parent);
    }
    /**
     * Move existing items a given number of positions within the same node.
     * If the delta is negative, they are all moved up by the given amount. If 
     * positive, they are all moved down by the given amount.<p>
     */
    protected void moveReferencedTreeItems(Widget parentItem, Object[] masterSrc, int delta)
    {
    	int[] oldPositions = new int[masterSrc.length];
    	Item[] oldItems = new Item[masterSrc.length];
    	Object[] src = new Object[masterSrc.length];

    	for (int idx=0; idx<src.length; idx++)
    	{
    	   oldItems[idx] = (Item)internalFindReferencedItem((Item)parentItem, masterSrc[idx], 1);    	
    	   src[idx] = oldItems[idx].getData();
    	}
    	for (int idx=0; idx<src.length; idx++) 
    	{
    	   oldPositions[idx] = getTreeItemPosition(parentItem, oldItems[idx])+1;
    	   //logDebugMsg("::: Old position : " + oldPositions[idx]);
    	}

    	if (delta > 0) // moving down, process backwards
    	{
          for (int idx=src.length-1; idx>=0; idx--)
          {
    	     //logDebugMsg("DN: Old position : " + oldPositions[idx] + ", new position : " + (oldPositions[idx]+delta));
             moveTreeItem(parentItem, oldItems[idx], src[idx], oldPositions[idx]+delta);
          }
    	}
        else // moving up, process forewards
        {
          for (int idx=0; idx<src.length; idx++)
          {
    	     //logDebugMsg("UP: Old position : " + oldPositions[idx] + ", new position : " + (oldPositions[idx]+delta-1));
             moveTreeItem(parentItem, oldItems[idx], src[idx], oldPositions[idx]+delta-1);	        
          }
        }
    }
    
    /**
     * Recursively tries to find a reference to the given referenced item
     *
     * @param parent the parent item at which to start the search.
     * @param element the master element to which we want to find a tree item which references it
     * @param searchLimit how deep to search
     */
    protected Widget internalFindReferencedItem(Widget parent, Object element, int searchLimit) 
    {
    	previousItem = null;
    	searchDepth = 0;
    	return recursiveInternalFindReferencedItem(parent, element, searchLimit);
    }    
    /**
     * Recursively tries to find a reference the given filtercontainer
     * Limits search depth to when we find an item that is not a connection, 
     *    subsystem, filter pool, filter or filter string.
     * @param parent the parent item at which to start the search.
     * @param element the master element to which we want to find a tree item which references it
     * @param searchLimit how deep to search
     */
    protected Widget recursiveInternalFindReferencedItem(Widget parent, Object element, int searchLimit) 
    {
	    // compare with node
	    Object data= parent.getData();
	    if ((data != null) && (data instanceof ISystemBaseReferencingObject))
	    {
	      ISystemBaseReferencingObject refingData = (ISystemBaseReferencingObject)data;
	      Object refedData = refingData.getReferencedObject();
	      //logDebugMsg("data is a refing obj to " + refingData);	      
		  if (refedData == element)
		  //if (refedData.equals(element))		  
			return parent;
	      else
	        previousItem = parent;
	    }
	    // recurse over children if we are listing a subsystem or connection or
	    // filter framework reference object, and nesting limit not reached.
	    if ( ((data instanceof ISubSystem) ||
	          (data instanceof IHost) ||
	          (data instanceof ISystemFilterContainer) ||
	          (data instanceof ISystemFilterContainerReference) ||
	          (data instanceof ISystemFilterStringReference) ) &&
	         (searchDepth < searchLimit) )
	    {
	      ++searchDepth;
	      int oldDepth = searchDepth;
	      Item[] items= getChildren(parent);
	      for (int i= 0; (i < items.length); i++) 
	      {	    	
		    Widget o = recursiveInternalFindReferencedItem(items[i], element, searchLimit);
		    if (o != null)
			  return o;
			searchDepth = oldDepth;
	      }
	    }
	    return null;
    }    

    /**
     * Recursively tries to find an item starting at the given item.
     * (base viewer classes do not offer a relative search!)
     *
     * @param parent the parent item at which to start the search.
     * @param element the element to match on. Matches on "==" versus equals()
     */
    protected Widget internalFindRelativeItem(Widget parent, Object element, int searchLimit) 
    {
    	searchDepth = 0;
	    return recursiveInternalFindRelativeItem(parent,element,searchLimit);
    }    
    /**
     * Recursively tries to find an item starting at the given item.
     * (base viewer classes do not offer a relative search!)
     *
     * @param parent the parent item at which to start the search.
     * @param element the element to match on. Matches on "==" versus equals()
     */
    protected Widget recursiveInternalFindRelativeItem(Widget parent, Object element, int searchLimit) 
    {
	    // compare with node
	    Object data= parent.getData();
	    if ((data != null) && (data == element))
	      return parent;
	    // recurse over children
	    if (searchDepth < searchLimit)
	    {
	      ++searchDepth;
	      int oldDepth = searchDepth;
	      Item[] items= getChildren(parent);
	      for (int i= 0; i < items.length; i++) 
	      {
		    Widget o= recursiveInternalFindRelativeItem(items[i], element, searchLimit);
		    if (o != null)
			    return o;
		    searchDepth = oldDepth;
	      }
	    }
	    return null;
    }    

    protected ISystemRemoteElementAdapter getRemoteData(Item item, Object rawData)
    {
    	if (rawData != null)
    	  return getRemoteAdapter(rawData);
    	else 
    	  return null;
    }

    /**
     * Find the first binary-match or name-match of remote object, given its absolute name.
     * @param remoteObjectName The absolute name of the remote object to find.
     * @param subsystem The subsystem of the remote object to find. Optional.
     * @param parentItem The parent item at which to start the search. Optional.
     * @return TreeItem hit if found
     */
    public Item findFirstRemoteItemReference(String remoteObjectName, ISubSystem subsystem, Item parentItem)
    {
    	//Vector matches = new Vector();
    	Item match = null;
    	if (parentItem == null)
    	   //findAllRemoteItemReferences(remoteObjectName, null, subsystem, matches);
    	   match = internalFindFirstRemoteItemReference(remoteObjectName, null, subsystem);
    	else
    	{
           //recursiveFindAllRemoteItemReferences(parentItem, remoteObjectName, null, subsystem, matches);    	  
           match = recursiveFindFirstRemoteItemReference(parentItem, remoteObjectName, null, subsystem);
           if (debugRemote)
             System.out.println("Returning " + match + " from findFirstRemoteItemReference(1,2,3)");
    	}
    	//if (matches.size() > 0)
    	//  return (Item)matches.elementAt(0);
    	//else
    	//  return null;
    	return match;
    }

    /**
     * Find the first binary-match or name-match of a remote object, given its binary object.
     * @param remoteObject - The remote object to find.
     * @param parentItem - Optionally, the parent item to start the search at
     * @return TreeItem hit if found
     */
    public Item findFirstRemoteItemReference(Object remoteObject, Item parentItem)
    {
    	//Vector matches = new Vector();
    	ISystemRemoteElementAdapter adapter = getRemoteAdapter(remoteObject);
    	if (adapter == null)
    	  return null;
    	Item match = null;
    	ISubSystem subsystem = adapter.getSubSystem(remoteObject);
    	String remoteObjectName = adapter.getAbsoluteName(remoteObject);
    	if (parentItem == null)
    	   //findAllRemoteItemReferences(remoteObjectName, remoteObject, subsystem, matches);
    	   match = internalFindFirstRemoteItemReference(remoteObjectName, remoteObject, subsystem);
    	else
    	{
           //recursiveFindAllRemoteItemReferences(parentItem, remoteObjectName, remoteObject, subsystem, matches); 
           match = recursiveFindFirstRemoteItemReference(parentItem, remoteObjectName, remoteObject, subsystem);
           if (debugRemote)
             System.out.println("Returning " + match + " from findFirstRemoteItemReference(1,2)");
    	}

    	//if (matches.size() > 0)
    	//  return (Item)matches.elementAt(0);
    	//else
    	//  return null;
    	return match;
    }

    /**
     * Recursively tries to find a given remote object. Since the object memory object 
     *  for a remote object is not dependable we call getAbsoluteName() on the adapter to
     *  do the comparisons. Note this does not take into account the parent connection or 
     *  subsystem or filter, hence you must know where to start the search, else you risk
     *  finding the wrong one.
     *
     * @param element the remote object to which we want to find a tree item which references it. Can be a string or an object
     * @param elementObject the actual remote element to find, for binary matching, optionally for cases when element is a string
     * @param matches the vector to populate with hits
     */
    protected Vector findAllRemoteItemReferences(Object element, Object elementObject, Vector matches) 
    {    	
    	String searchString = null;
    	ISubSystem subsystem = null;
    	if (element instanceof String)
    	  searchString = (String)element;
    	else
    	{
    	  if (elementObject == null)
    	    elementObject = element;
    	  ISystemRemoteElementAdapter adapter = getRemoteAdapter(element);
    	  if (adapter == null)
    	    return matches;
    	  subsystem = adapter.getSubSystem(element);
    	  searchString = adapter.getAbsoluteName(element);
    	}
    	Tree tree = getTree();
    	Item[] roots = tree.getItems();
    	if (roots == null)
    	  return matches;
    	if (matches == null)
    	  matches = new Vector();
    	for (int idx=0; idx<roots.length; idx++)
    	{
           matches = recursiveFindAllRemoteItemReferences(roots[idx], searchString, elementObject, subsystem, matches);
    	}
        return matches;
    }    
    /**
     * Recursively tries to find all occurrences of a given remote object, starting at the tree root. 
     * Since the object memory object for a remote object is not dependable we call getAbsoluteName() 
     * on the adapter to do the comparisons. 
     * <p>
     * This overload takes a string and a subsystem.
     * 
     * @param searchString the absolute name of the remote object to which we want to find a tree item which references it.
     * @param elementObject the actual remote element to find, for binary matching
     * @param subsystem optional subsystem to search within
     * @param matches the vector to populate with hits
     */
    protected Vector findAllRemoteItemReferences(String searchString, Object elementObject, ISubSystem subsystem, Vector matches)
    {
    	Tree tree = getTree();
    	Item[] roots = tree.getItems();
    	if (roots == null)
    	  return matches;
    	if (matches == null)
    	  matches = new Vector();
    	for (int idx=0; idx<roots.length; idx++)
           matches = recursiveFindAllRemoteItemReferences(roots[idx], searchString, elementObject, subsystem, matches);
        return matches;
    }    
    /**
     * Recursively tries to find the first occurrence of a given remote object, starting at the tree root. 
     * Optionally scoped to a specific subsystem.
     * Since the object memory object for a remote object is not dependable we call getAbsoluteName() 
     * on the adapter to do the comparisons. 
     * <p>
     * This overload takes a string and a subsystem.
     * 
     * @param searchString the absolute name of the remote object to which we want to find a tree item which references it.
     * @param elementObject the actual remote element to find, for binary matching
     * @param subsystem optional subsystem to search within
     * @param matches the vector to populate with hits
     * @return TreeItem hit if found
     */
    protected Item internalFindFirstRemoteItemReference(String searchString, Object elementObject, ISubSystem subsystem)
    {
    	Item[] roots = getTree().getItems();
    	if ((roots == null) || (roots.length==0))
    	  return null;
    	Item match = null;
    	if (debugRemote)
    	{
    		System.out.println("Inside internalFindFirstRemoteItemReference for searchString: "+searchString+", subsystem null? " + (subsystem==null) + ", nbr roots = " + roots.length);
    	}
    	for (int idx=0; (match==null) && (idx<roots.length); idx++)
    	{
           match = recursiveFindFirstRemoteItemReference(roots[idx], searchString, elementObject, subsystem);
           if (debugRemote)
    		   System.out.println("...Inside internalFindFirstRemoteItemReference. Result of searching root "+idx+": "+roots[idx].getText()+": " + match);
    	}
        if (debugRemote)
        {
    	  System.out.println("...Inside internalFindFirstRemoteItemReference. Returning " + match);
    	  if (match != null)
    	    System.out.println("......set bp here");
        }
        return match;
    }    
    /**
     * Recursively tries to find all references to a remote object.
     * @param parent the parent item at which to start the search.
     * @param elementName the absolute name of the remote element to find
     * @param elementObject the actual remote element to find, for binary matching
     * @param subsystem optional subsystem to search within
     * @param occurrences the vector to populate with hits
     */
    protected Vector recursiveFindAllRemoteItemReferences(Item parent, String elementName, Object elementObject, ISubSystem subsystem, Vector occurrences) 
    {
        Object rawData = parent.getData();
        ISystemRemoteElementAdapter remoteAdapter = null;
        // ----------------------------
        // what are we looking at here?
        // ----------------------------
        if (rawData != null)
            remoteAdapter = getRemoteAdapter(rawData);
        // -----------------------------------------------------------------------
        // if this is a remote object, test if it is the one we are looking for...
        // -----------------------------------------------------------------------
        if (remoteAdapter != null)
        {
            // first test for binary match
            if (elementObject == rawData)
            {
		  	    occurrences.addElement(parent); // found a match!
                if (debugRemote)
                   System.out.println("Find All: Remote item binary match found");
			    return occurrences; // no point in checking the kids
            }
            // now test for absolute name match
	        String fqn = remoteAdapter.getAbsoluteName(rawData);
            if (debugRemote)
               System.out.println("TESTING FINDALL: '" + fqn + "' vs '" + elementName + "'");
		    if ((fqn != null) && fqn.equals(elementName))
		    {
		  	   occurrences.addElement(parent); // found a match!
               if (debugRemote)
                  System.out.println("...and remote item name match found");
			   return occurrences; // no point in checking the kids
		    }
        }
        // -------------------------------------------------------------------------
        // if we have been given a subsystem to restrict to, that is a hint to us...
        // -------------------------------------------------------------------------
	    else if ((rawData!=null) && (subsystem!=null)) // test for hints we are in the wrong place
	    {
	       // if we are currently visiting a subsystem, and that subsystem is not from the same
	       //  factory, then we can assume the remote object occurrences we are looking for are
	       //  not to be found within this branch...
	       if ((rawData instanceof ISubSystem) && (((ISubSystem)rawData).getSubSystemConfiguration() != subsystem.getSubSystemConfiguration()))
	       {	       	
	       	    return occurrences; // they don't match, so don't bother checking the kids
	       }
	       // if we are currently visiting a connection, and that connection's hostname is not the same
	       //  as that of our given subsystem, then we can assume the remote object occurrences we are 
	       //  looking for are not to be found within this branch...
	       else if ((rawData instanceof IHost) && (!((IHost)rawData).getHostName().equals(subsystem.getHost().getHostName())))
	       {
	            return occurrences; // they don't match, so don't bother checking the kids
	       }
	    }
	    // recurse over children	    
	    Item[] items= getChildren(parent);
	    for (int i= 0; (i < items.length); i++) 
	    {	    	
	    	if (!items[i].isDisposed())
		      occurrences = recursiveFindAllRemoteItemReferences(items[i], elementName, elementObject, subsystem, occurrences);
	    }
	    return occurrences;    
    }    
    /**
     * Recursively tries to find the first references to a remote object.
     * This search is restricted to the given subsystem, if given.
     * @param parent the parent item at which to start the search.
     * @param elementName the absolute name of the remote element to find
     * @param elementObject the actual remote element to find, for binary matching
     * @param subsystem optional subsystem to search within
     * @return TreeItem match if found, null if not found. 
     */
    protected Item recursiveFindFirstRemoteItemReference(Item parent, String elementName, Object elementObject, ISubSystem subsystem) 
    {
        Object rawData = parent.getData();
        ISystemRemoteElementAdapter remoteAdapter = null;
        // ----------------------------
        // what are we looking at here?
        // ----------------------------
        if (rawData != null)
            remoteAdapter = getRemoteAdapter(rawData);
        // -----------------------------------------------------------------------
        // if this is a remote object, test if it is the one we are looking for...
        // -----------------------------------------------------------------------
        if (remoteAdapter != null)
        {
            // first test for binary match
            if (elementObject == rawData)
            {
                if (debugRemote)
                   System.out.println("Remote item binary match found");
			    return parent; // return the match
            }
            // now test for absolute name match
	        String fqn = remoteAdapter.getAbsoluteName(rawData);
            if (debugRemote)
               System.out.println("TESTING FINDFIRST: '" + fqn + "' vs '" + elementName + "'");
		    if ((fqn != null) && fqn.equals(elementName))
		    {
		       if ((subsystem != null) && (subsystem == remoteAdapter.getSubSystem(rawData)))
		       {
                  if (debugRemote)
                     System.out.println("Remote item name match found and subsystems matched");
			      return parent; // return the match
		       }
		       else if (subsystem == null)
		       {
                  if (debugRemote)
                     System.out.println("Remote item name match found and subsystem null");
		          return parent;
		       }		          
		       else if (debugRemote)
                  System.out.println("Remote item name match found but subsystem mismatch");		       
		    }
        }
        // -------------------------------------------------------------------------
        // if we have been given a subsystem to restrict to, that is a hint to us...
        // -------------------------------------------------------------------------
	    else if ((rawData!=null) && (subsystem!=null)) // test for hints we are in the wrong place
	    {
	       // if we are currently visiting a subsystem, and that subsystem is not from the same
	       //  factory, then we can assume the remote object occurrences we are looking for are
	       //  not to be found within this branch...
	       if ((rawData instanceof ISubSystem) && (rawData != subsystem))
	       {
	             return null; // they don't match, so don't bother checking the kids
	       }
	       // if we are currently visiting a connection, and that connection's hostname is not the same
	       //  as that of our given subsystem, then we can assume the remote object occurrences we are 
	       //  looking for are not to be found within this branch...
	       else if ((rawData instanceof IHost)  &&
	                 !((IHost)rawData).getHostName().equals(subsystem.getHost().getHostName()))
	       {
	              return null; // they don't match, so don't bother checking the kids
	       }
	    }
	    // recurse over children	    
	    Item[] items= getChildren(parent);
        Item   match = null;
	    for (int i= 0; (match==null) && (i < items.length); i++) 
	    {	    	
	    	if (!items[i].isDisposed())
		      match = recursiveFindFirstRemoteItemReference(items[i], elementName, elementObject, subsystem);
	    }
	    return match;    
    }    


    /**
     * Recursively tries to find all filters affected by a given remote object. 
     * 
     * @param elementName the absolute name of the remote object to which we want to find a filters which result in it.
     * @param subsystem. The subsystem which owns the remote resource. Necessary to scope the search for impacted filters.
     * @param matches the vector to populate with hits. Can be null, in which case a new vector is created.
     * 
     * @return Vector of FilterMatch objects for each affected filter
     */
    protected Vector findAllRemoteItemFilterReferences(String elementName, ISubSystem subsystem, Vector matches)
    {
    	Tree tree = getTree();
    	Item[] roots = tree.getItems();
    	if (roots == null)
    	  return matches;
    	if (matches == null)
    	  matches = new Vector();
    	for (int idx=0; idx<roots.length; idx++)
           matches = recursiveFindAllRemoteItemFilterReferences(roots[idx], elementName, subsystem, matches);
        return matches;

    }    

    /**
     * Recursively tries to find all filters which are affected by a given remote object, such that we can subsequently refresh that filter
     *  after a remote resource change.
     * @param parent the parent item at which to start the search.
     * @param elementName the absolute name of the remote element that has been created, changed, deleted or renamed.
     * @param subsystem. The subsystem which owns the remote resource. Necessary to scope the search for impacted filters.
     * @param occurrences the vector to populate with hits
     * 
     * @return Vector of FilterMatch objects for each affected filter
     */
    protected Vector recursiveFindAllRemoteItemFilterReferences(Item parent, String elementName, ISubSystem subsystem, Vector occurrences) 
    {
        Object rawData = parent.getData();

        // ----------------------------
        // what are we looking at here?
        // ----------------------------

        // ---------------------------------------------------------------------
        // if this is a filter object, test for two things:
        //  #1. does this filter list this remote object if expanded/refreshed?
        //  #2. does this filter list the contents of this remote object?
        // ---------------------------------------------------------------------
        if (rawData instanceof ISystemFilterReference)
        {
            ISystemFilterReference filterRef = (ISystemFilterReference)rawData;
            if (filterRef.getReferencedFilter().isPromptable())
              return occurrences;
            if (debugRemote)
              System.out.println("Testing filter: " + filterRef.getReferencedFilter().getName());
            ISubSystem fss = (ISubSystem)filterRef.getProvider();
		    if (fss != null) // should never happen!!
		    {
		    	// #1
		    	if (fss.doesFilterMatch(filterRef.getReferencedFilter(), elementName))
		    	{
		  	       occurrences.addElement(new FilterMatch((TreeItem)parent, true)); // found a match!
                   if (debugRemote)
                      System.out.println("...Filter match found for "+elementName+": " + filterRef.getReferencedFilter().getName());
			       return occurrences; // no point in checking the kids
		    	}
		    	// #2
		    	else if (fss.doesFilterListContentsOf(filterRef.getReferencedFilter(),elementName))
		    	{
		  	       occurrences.addElement(new FilterMatch((TreeItem)parent, false)); // found a match!
                   if (debugRemote)
                      System.out.println("...Filter content match found for "+elementName+": " + filterRef.getReferencedFilter().getName());
			       return occurrences; // no point in checking the kids
		    	}
		    	else if (debugRemote)
		    	  System.out.println("... no match on the filter for element name " + elementName);
		    }
        }
        // ----------------------------------------------------------------------
        // if this is not a filter, then before recursing on its kids, check for
        //  hints that such recursion is a waste of time, for performance reasons
        // ----------------------------------------------------------------------
        else if (rawData != null)
        {
            // ---------------------------------------------------------------------------------
	        // if we are currently visiting a subsystem, and that subsystem is not from the same
	        //  factory, then we can assume the remote object occurrences we are looking for are
	        //  not to be found within this branch...
            // ---------------------------------------------------------------------------------
	        if ((rawData instanceof ISubSystem) && (subsystem != null))
	        {
	            ISubSystem currSS = (ISubSystem)rawData;
	       	    if (currSS.getSubSystemConfiguration() != subsystem.getSubSystemConfiguration())
	               return occurrences; // they don't match, so don't bother checking the kids
	        }
            // -----------------------------------------------------------------------------------------
	        // if we are currently visiting a connection, and that connection's hostname is not the same
	        //  as that of our given subsystem, then we can assume the remote object occurrences we are 
	        //  looking for are not to be found within this branch...
            // -----------------------------------------------------------------------------------------
	        else if (rawData instanceof IHost) 
	        {
	       	    IHost currConn = (IHost)rawData;	       	 
	            if (!currConn.getHostName().equals(subsystem.getHost().getHostName()))
	               return occurrences; // they don't match, so don't bother checking the kids
	        }
	        // skip the new connection prompts...
	        else if (rawData instanceof ISystemPromptableObject)
	           return occurrences;
            // ------------------------------------------------------------------------
            // if this is a remote object, we are too deep into this branch of the tree
            //  for filters, so stop here
            // ------------------------------------------------------------------------
            else if (getRemoteAdapter(rawData) != null)
              return occurrences;
        }
	    // recurse over children	    
	    Item[] items= getChildren(parent);
	    for (int i= 0; (i < items.length); i++) 
	    {	    	
		    occurrences = recursiveFindAllRemoteItemFilterReferences(items[i], elementName, subsystem, occurrences);
	    }
	    return occurrences;    
    }    
    
    /**
     * Inner class to ensapsulate what is put in the vector for the recursiveFindAllRemoteItemFilterReferences() method.
     */
    protected class FilterMatch
    {
    	protected boolean filterListsElement;
    	protected boolean filterListsElementContents;
    	protected TreeItem match;
    	
    	FilterMatch(TreeItem match, boolean filterListsElement)
    	{
    		this.match = match;
    		this.filterListsElement = filterListsElement;
    		this.filterListsElementContents = !filterListsElement;
    	}
    	
    	boolean listsElement()
    	{
    		return filterListsElement;
    	}
    	
    	boolean listsElementContents()
    	{
    		return filterListsElementContents;
    	}
        
        TreeItem getTreeItem()
        {
        	return match;
        }
    }
    

    /**
     * --------------------------------------------------------------------------------
     * For many actions we have to walk the selection list and examine each selected
     *  object to decide if a given common action is supported or not.
     * <p>
     * Walking this list multiple times while building the popup menu is a performance
     *  hit, so we have this common method that does it only once, setting instance
     *  variables for all of the decisions we are in interested in.
     * --------------------------------------------------------------------------------
     */
    protected void scanSelections(String whereFrom)
    {
    	//System.out.println("inside scanSelections. Called from " + whereFrom);
        // here are the instance variables we set...
	    // protected boolean selectionShowRefreshAction;
	    // protected boolean selectionShowOpenViewActions;
	    // protected boolean selectionShowDeleteAction;
	    // protected boolean selectionShowRenameAction;
	    // protected boolean selectionIsRemoteObject;
	    // protected boolean selectionEnableDeleteAction;
	    // protected boolean selectionEnableRenameAction;

        // initial these variables to true. Then if set to false even once, leave as false always...
        selectionShowRefreshAction  = true;
        selectionShowOpenViewActions= true;
        selectionShowGenericShowInTableAction = true;
        selectionShowDeleteAction   = true;
        selectionShowRenameAction   = true;
        selectionEnableDeleteAction = true;
        selectionEnableRenameAction = true;
        selectionIsRemoteObject     = true;
		selectionHasAncestorRelation = hasAncestorRelationSelection();
        
		IStructuredSelection selection= (IStructuredSelection)getSelection();
		Iterator elements= selection.iterator();
		while (elements.hasNext())
		{
			Object element= elements.next();
			ISystemViewElementAdapter adapter = getAdapter(element);
			if (adapter == null)
			  continue;

			if (selectionShowRefreshAction)
			  selectionShowRefreshAction = adapter.showRefresh(element);

			if (selectionShowOpenViewActions)
			  selectionShowOpenViewActions   = adapter.showOpenViewActions(element);
			  
		    if (selectionShowGenericShowInTableAction)
		      selectionShowGenericShowInTableAction = adapter.showGenericShowInTableAction(element);

			if (selectionShowDeleteAction)
			  selectionShowDeleteAction = adapter.showDelete(element);

			if (selectionShowRenameAction)
			  selectionShowRenameAction = adapter.showRename(element);

			if (selectionEnableDeleteAction)
			  selectionEnableDeleteAction = selectionShowDeleteAction && adapter.canDelete(element)  && !selectionHasAncestorRelation;
			//System.out.println("ENABLE DELETE SET TO " + selectionEnableDeleteAction);

			if (selectionEnableRenameAction)
			  selectionEnableRenameAction = selectionShowRenameAction && adapter.canRename(element);

			if (selectionIsRemoteObject)
              selectionIsRemoteObject = (getRemoteAdapter(element) != null);
			
			if (selectionIsRemoteObject && !selectionFlagsUpdated)
			{
				ISubSystem srcSubSystem = adapter.getSubSystem(element);
		    	if (srcSubSystem.isConnected() || 
				        element instanceof ISystemFilterReference ||
				        element instanceof ISubSystem)
				{
		    		SystemRemoteElementResourceSet set = getSetFor(srcSubSystem, adapter);
					set.addResource(element);
				}
			}
		}
		selectionFlagsUpdated = true;
        //System.out.println("Inside scan selections: selectionShowOpenViewActions = " + selectionShowOpenViewActions);		
    }

    /**
     * Decides whether to even show the refresh menu item.
     * Assumes scanSelections() has already been called
     */
    protected boolean showRefresh()
    {
    	return selectionShowRefreshAction;
    	/*
   	    boolean ok = true;
		IStructuredSelection selection= (IStructuredSelection)getSelection();
		Iterator elements= selection.iterator();
		int count = 0;
		while (ok && elements.hasNext())
		{
			Object element= elements.next();
			ISystemViewElementAdapter adapter = getAdapter(element);
			if (!adapter.showRefresh(element))
			  ok = false;
		}
   	    return ok;
   	    */
    }
    /**
     * Decides whether to even show the "open in new perspective" menu item.
     * Assumes scanSelections() has already been called
     */
    protected boolean showOpenViewActions()
    {
    	return selectionShowOpenViewActions;
    }
    
	/**
	   * Decides whether to even show the generic "show in table" menu item.
	   * Assumes scanSelections() has already been called
	   */
	  protected boolean showGenericShowInTableAction()
	  {
		  return selectionShowGenericShowInTableAction;
	  } 
    
    /**
     * Decides whether all the selected objects are remote objects or not
     * Assumes scanSelections() has already been called
     */
    protected boolean areSelectionsRemote()
    {
         return selectionIsRemoteObject;
    }


    // ---------------------------
    // ISYSTEMDELETETARGET METHODS
    // ---------------------------
           
    /**
     * Required method from ISystemDeleteTarget.
     * Decides whether to even show the delete menu item.
     * Assumes scanSelections() has already been called
     */
    public boolean showDelete()
    {
    	if (!selectionFlagsUpdated)
    	{
    	  //System.out.println("Inside showDelete. selectFlagsUpdated = false");
    	  scanSelections("showDelete");
    	}
    	return selectionShowDeleteAction;
    }
    /**
     * Required method from ISystemDeleteTarget
     * Decides whether to enable the delete menu item. 
     * Assumes scanSelections() has already been called
     */
    public boolean canDelete()
    {
    	if (!selectionFlagsUpdated)
    	{
    	  //System.out.println("Inside canDelete. selectFlagsUpdated = false");
    	  scanSelections("canDelete");
    	}
    	return selectionEnableDeleteAction;
    }    
   
    /**
     * Required method from ISystemDeleteTarget
     */
    public boolean doDelete(IProgressMonitor monitor)
    {   	  
    	ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry(); 
		IStructuredSelection selection= (IStructuredSelection)getSelection();		
		Iterator elements= selection.iterator();
		//int selectedCount = selection.size();
		//Object multiSource[] = new Object[selectedCount];
		//int idx = 0;
		Object element = null;
		//Object parentElement = getSelectedParent();
		ISystemViewElementAdapter adapter = null;
		boolean ok = true;
		boolean anyOk = false;
		Vector deletedVector = new Vector();
		try
		{
		  while (ok && elements.hasNext())
		  {
			element= elements.next();
			//multiSource[idx++] = element;
			adapter = getAdapter(element);
			if (getRemoteAdapter(element) != null) continue;
			ok = adapter.doDelete(getShell(), element, monitor); 
			if (ok)
			{
			  anyOk = true;
			  deletedVector.addElement(element);
			}
		  }
			// now we have things divided into sets
			// delete 1 set at a time
			for (int s = 0; s < _setList.size() && ok; s++)
			{
				SystemRemoteElementResourceSet set = (SystemRemoteElementResourceSet)_setList.get(s);
				ISubSystem srcSubSystem = set.getSubSystem();
				ISystemViewElementAdapter srcAdapter = set.getAdapter();
								
				if (srcSubSystem != null)
				{
					ok = srcAdapter.doDeleteBatch(getShell(), set.getResourceSet(), monitor); 
					if (ok)
					{
					  anyOk = true;
					  deletedVector.addAll(set.getResourceSet());
					}	
				}
			}
		}
		catch (SystemMessageException exc)
		{
			SystemMessageDialog.displayErrorMessage(getShell(), exc.getSystemMessage());
		    ok = false;
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
    	    String msg = exc.getMessage();
    	    if ((msg == null) || (exc instanceof ClassCastException))
    	      msg = exc.getClass().getName();
			SystemMessageDialog.displayErrorMessage(getShell(), RSEUIPlugin.getPluginMessage(
			                                     ISystemMessages.MSG_EXCEPTION_DELETING).makeSubstitution(element,msg));
		    ok = false;
		}
		//System.out.println("in doDelete. Any ok? " + anyOk + ", selectionIsRemoteObject? " + selectionIsRemoteObject);
		if (anyOk)
		{
            if (selectionIsRemoteObject)          
              //sr.fireEvent(
              //  new com.ibm.etools.systems.model.impl.SystemResourceChangeEvent(
              //    deleted,ISystemResourceChangeEvent.EVENT_DELETE_REMOTE_MANY,null));
              sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, deletedVector, null, null, null, this);
          else
          {
		      Object[] deleted = new Object[deletedVector.size()];
		      for (int idx=0; idx<deleted.length; idx++)
		          deleted[idx] = deletedVector.elementAt(idx);
              sr.fireEvent(new org.eclipse.rse.model.SystemResourceChangeEvent(
                  deleted,ISystemResourceChangeEvent.EVENT_DELETE_MANY,getSelectedParent()));
          }
		}
        return ok;
    }   
    
    // ---------------------------
    // ISYSTEMRENAMETARGET METHODS
    // ---------------------------
           
    /**
     * Required method from ISystemRenameTarget.
     * Decides whether to even show the rename menu item.
     * Assumes scanSelections() has already been called
     */
    public boolean showRename()
    {
    	return selectionShowRenameAction;
    }
    /**
     * Required method from ISystemRenameTarget
     * Decides whether to enable the rename menu item. 
     * Assumes scanSelections() has already been called
     */
    public boolean canRename()
    {
    	if (!selectionFlagsUpdated)
    	  scanSelections("canRename");
    	return selectionEnableRenameAction;
    }    
   
    /**
     * Required method from ISystemRenameTarget
     */
    public boolean doRename(String[] newNames)
    {   	  
    	ISystemRegistry sr = RSEUIPlugin.getDefault().getSystemRegistry(); 
		IStructuredSelection selection= (IStructuredSelection)getSelection();		
		Iterator elements= selection.iterator();
		int selectedCount = selection.size();
		int idx = 0;
		Object element = null;
		Object parentElement = getSelectedParent();
		ISystemViewElementAdapter adapter = null;
		ISystemRemoteElementAdapter remoteAdapter = null;
		String oldFullName = null;
		boolean ok = true;
		try
		{
		  int nameIdx = 0;
		  while (ok && elements.hasNext())
		  {
			element= elements.next();
			adapter = getAdapter(element);
			remoteAdapter = getRemoteAdapter(element);
			if (remoteAdapter != null)
			  oldFullName = remoteAdapter.getAbsoluteName(element); // pre-rename
			ok = adapter.doRename(getShell(), element,newNames[nameIdx++]);
			if (ok)
			{
			  if (remoteAdapter != null)
                //sr.fireEvent(
                //  new com.ibm.etools.systems.model.impl.SystemResourceChangeEvent(
                //    element,ISystemResourceChangeEvent.EVENT_RENAME_REMOTE, oldFullName));
                sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED,
                  element, parentElement, null, oldFullName, this);

			  else
                sr.fireEvent(
                  new org.eclipse.rse.model.SystemResourceChangeEvent(
                    element,ISystemResourceChangeEvent.EVENT_RENAME, parentElement));
			}
		  }
		}
		catch (SystemMessageException exc)
		{
			SystemMessageDialog.displayErrorMessage(getShell(), exc.getSystemMessage());
		    ok = false;
		}
		catch (Exception exc)
		{
    	    //String msg = exc.getMessage();
    	    //if ((msg == null) || (exc instanceof ClassCastException))
    	    //  msg = exc.getClass().getName();
			SystemMessageDialog.displayErrorMessage(getShell(), RSEUIPlugin.getPluginMessage(
			                                     ISystemMessages.MSG_EXCEPTION_RENAMING).makeSubstitution(element,exc), //msg),
			                                  exc);
			ok = false;
		}	
		return ok;	
    }   
 
    protected void logDebugMsg(String msg)
    {
    	//RSEUIPlugin.logDebugMessage(this.getClass().getName(),msg);
    	msg = this.getClass().getName()+": "+msg;
    	RSEUIPlugin.logInfo(msg);
    	System.out.println(msg);
    }

	// -----------------------------------------------------------------
	// ISystemSelectAllTarget methods to facilitate the global action...
	// -----------------------------------------------------------------
	/**
	 * Return true if select all should be enabled for the given object.
	 * For a tree view, you should return true if and only if the selected object has children.
	 * You can use the passed in selection or ignore it and query your own selection.
	 */
    public boolean enableSelectAll(IStructuredSelection selection)
    {
		Tree tree = getTree();
        TreeItem[] items = tree.getSelection();
        if ((items==null) || (items.length!=1)) // only allow for single selections
          return false;

	    TreeItem ti = items[0];
	    int count = getItemCount(ti);
        if (count == 1) // is it a dummy?
		{
		  	if ((getItems(ti)[0]).getData() == null)
		  	  count = 0; // assume a dummy
		}
		return (count > 0);
    }
    /**
     * When this action is run via Edit->Select All or via Ctrl+A, perform the
     * select all action. For a tree view, this should select all the children 
     * of the given selected object. You can use the passed in selected object
     * or ignore it and query the selected object yourself. 
     */
    public void doSelectAll(IStructuredSelection selection)
    {
    	Tree tree = getTree();
    	TreeItem[] currSel = tree.getSelection();
    	TreeItem[] childItems = currSel[0].getItems();
    	if (childItems.length == 0)
    	  return;
    	tree.setSelection(childItems);
    	Object[] childObjects = new Object[childItems.length];
    	for (int idx=0; idx<childObjects.length; idx++)
    	   childObjects[idx] = childItems[idx].getData();
    	fireSelectionChanged(
    	   new SelectionChangedEvent(this,
    	         new StructuredSelection(childObjects)));
    }
	
	// --------------------------------------------	
	// ISystemTree methods to facilitate our GUI...
	// --------------------------------------------
	/**
	 * This is called to ensure all elements in a multiple-selection have the same parent in the
	 *  tree viewer. If they don't we automatically disable all actions. 
	 * <p>
	 * Designed to be as fast as possible by going directly to the SWT widgets
	 */
	public boolean sameParent()
	{
		boolean same = true;
		Tree tree = getTree();
        TreeItem[] items = tree.getSelection();
        if ((items == null) || (items.length ==0))
          return true;
        TreeItem prevParent = null;
        TreeItem currParent = null;
        for (int idx=0; same && (idx<items.length); idx++)
        {
           currParent = items[idx].getParentItem();           
           if ((idx>0) && (currParent != prevParent))
             same = false;
           else
           {
           	 prevParent = currParent;  
           }
        }
		return same;
	}

	protected boolean selectionHasAncestryRelationship()
	{
		if (selectionFlagsUpdated) return selectionHasAncestorRelation;
		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();

		for (int idx=0; idx<items.length; idx++)
		{
			TreeItem item = items[idx];
			for (int c=0; c < items.length; c++)
			{
				if (item != items[c])
				{					
					if (isAncestorOf(item, items[c]))
					{
						return true;
					}
				}
			}
		}
		return false;		
	}
	
	protected boolean isAncestorOf(TreeItem container, TreeItem item)
	{
		TreeItem[] children = container.getItems();
		for (int i = 0; i < children.length; i++)
		{
			TreeItem child = children[i];
			if (child == item)
			{
				return true;
			}
			else if (child.getItemCount() > 0)
			{
				if (isAncestorOf(child, item))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This is called to accurately get the parent object for the current selection
	 *  for this viewer. 
	 * <p>
	 * The getParent() method in the adapter is very unreliable... adapters can't be sure
	 * of the context which can change via filtering and view options.
	 */
	public Object getSelectedParent()
	{
		Tree tree = getTree();
        TreeItem[] items = tree.getSelection();
        if ((items==null) || (items.length==0))
        {
        	return tree.getData();
        }
        else
        {
        	TreeItem parentItem = items[0].getParentItem();
        	if (parentItem != null)    	
              return parentItem.getData();
            else
              return tree.getData();
        }
	}
	/**
	 * Return the TreeItem of the parent of the selected node. Or null if a root is selected.
	 */
	public TreeItem getSelectedParentItem()
	{
		Tree tree = getTree();
        TreeItem[] items = tree.getSelection();
        if ((items==null) || (items.length==0))
        {
        	return null;
        }
        else
        {
        	return items[0].getParentItem();
        }
	}
	/**
	 * This returns the element immediately before the first selected element in this tree level.
	 * Often needed for enablement decisions for move up actions.
	 */
	public Object getPreviousElement()
	{
		 Object prevElement = null;
		 Tree tree = getTree();
		 TreeItem[] items = tree.getSelection();
		 if ((items != null) && (items.length>0))
		 {
		 	TreeItem item1 = items[0];
		 	TreeItem[] parentItems = null;
		 	TreeItem parentItem = item1.getParentItem();		 	
		 	if (parentItem != null)
		 	  parentItems = parentItem.getItems();
		    else
		      parentItems = item1.getParent().getItems();
		    if (parentItems != null)
		    {
		    	TreeItem prevItem = null;
		    	for (int idx=0; (prevItem==null) && (idx<parentItems.length); idx++)
		    	   if ((parentItems[idx] == item1) && (idx > 0))
		    	     prevItem = parentItems[idx-1];
		    	if (prevItem != null)
		    	  prevElement = prevItem.getData();
		    }
		 }
		 return prevElement;
	}
	/**
	 * This returns the element immediately after the last selected element in this tree level
	 * Often needed for enablement decisions for move down actions.
	 */
	public Object getNextElement()
	{
		 Object nextElement = null;
		 Tree tree = getTree();
		 TreeItem[] items = tree.getSelection();
		 if ((items != null) && (items.length>0))
		 {
		 	TreeItem itemN = items[items.length-1];
		 	TreeItem[] parentItems = null;
		 	TreeItem parentItem = itemN.getParentItem();		 	
		 	if (parentItem != null)
		 	  parentItems = parentItem.getItems();
		    else
		      parentItems = itemN.getParent().getItems();
		    if (parentItems != null)
		    {
		    	TreeItem nextItem = null;
		    	for (int idx=0; (nextItem==null) && (idx<parentItems.length); idx++)
		    	   if ((parentItems[idx] == itemN) && (idx < (parentItems.length-1)))
		    	     nextItem = parentItems[idx+1];
		    	if (nextItem != null)
		    	  nextElement = nextItem.getData();
		    }
		 }
		 return nextElement;		
	}
	
	/**
	 * This is called to walk the tree back up to the roots and return the visible root
	 *  node for the first selected object.
	 */
	public Object getRootParent()
	{
		Tree tree = getTree();
        TreeItem[] selectedItems = tree.getSelection();
        Object rootElement = null;
        if ((selectedItems != null) && (selectedItems.length>0))
        {
          TreeItem item = selectedItems[0];
          TreeItem parentItem = item.getParentItem();
          if (parentItem == null) // item is a root element
            rootElement = item.getData();
          else
            while (rootElement == null)
            {
            	item = parentItem;
            	parentItem = item.getParentItem();
            	if (parentItem == null) // item is a root element
            	  rootElement = item.getData();
            }
        } 
        //logDebugMsg("getRootParent returned: "+rootElement);
        return rootElement;       
	}
	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 */
	public Object[] getElementNodes(Object element)
	{
		Widget w = findItem(element);
		if ((w != null) && (w instanceof TreeItem))
	        return getElementNodes((TreeItem)w);
		return null;
	}
	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 * This flavour is optimized for the case when you have the tree item directly.
	 */
	public Object[] getElementNodes(TreeItem item)
	{
		Vector v = new Vector();
		v.addElement(item.getData());
		while (item != null)
		{
           item = item.getParentItem();
           if (item != null)    	
                v.addElement(item.getData());
		}
        Object[] nodes = new Object[v.size()];
        for (int idx=0; idx<nodes.length; idx++)
           nodes[idx] = v.elementAt(idx);
        return nodes;			
	}
	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 * This flavour returns a vector of TreeItem objects versus element objects.
	 */
	public TreeItem[] getItemNodes(TreeItem item)
	{
		Vector v = new Vector();
		v.addElement(item);
		while (item != null)
		{
           item = item.getParentItem();
           if (item != null)    	
                v.addElement(item);
		}
        TreeItem[] nodes = new TreeItem[v.size()];
        for (int idx=0; idx<nodes.length; idx++)
           nodes[idx] = (TreeItem)v.elementAt(idx);
        return nodes;			
	}

	/**
	 * Helper method to determine if a given object is currently selected.
	 * Does consider if a child node of the given object is currently selected.
	 */
	public boolean isSelectedOrChildSelected(Object parentElement)
	{
		boolean isSelected = false;
        Item[] selectedItems = ((Tree)getControl()).getSelection();		
        if ((selectedItems != null) && (selectedItems.length>0))
        {
          Widget w = findItem(parentElement);
          if (!(w instanceof TreeItem))
            return true; // assume we have been given the root, which means any selection is a child
          TreeItem item = (TreeItem)w;  
          // for every selected tree item, scan upwards to the root to see if
          // it or any of its parents are the given element.
          for (int idx=0; !isSelected && (idx<selectedItems.length); idx++)
          {
          	 if (selectedItems[idx] instanceof TreeItem)
          	 {
          	   if (selectedItems[idx] == item)
          	     isSelected = true;
          	   else
          	     isSelected = searchToRoot((TreeItem)selectedItems[idx], item);
          	 }
          }
        }
        return isSelected;
        //return isSelected(element, (IStructuredSelection)getSelection());		
	}
	/**
	 * Override that takes a widget.
	 */
	public boolean isTreeItemSelectedOrChildSelected(Widget w)
	{
		boolean isSelected = false;
        Item[] selectedItems = ((Tree)getControl()).getSelection();		
        if ((selectedItems != null) && (selectedItems.length>0))
        {
          if (!(w instanceof TreeItem))
            return true; // assume we have been given the root, which means any selection is a child
          TreeItem item = (TreeItem)w;  
          // for every selected tree item, scan upwards to the root to see if
          // it or any of its parents are the given element.
          for (int idx=0; !isSelected && (idx<selectedItems.length); idx++)
          {
          	 if (selectedItems[idx] instanceof TreeItem)
          	 {
          	   if (selectedItems[idx] == item)
          	     isSelected = true;
          	   else
          	     isSelected = searchToRoot((TreeItem)selectedItems[idx], item);
          	 }
          }
        }
        return isSelected;
        //return isSelected(element, (IStructuredSelection)getSelection());		
	}

	/**
	 * Return the number of immediate children in the tree, for the given tree node
	 */
    public int getChildCount(Object element)
	{
		if (getTree().isDisposed())
		  return 0;
		Widget w = findItem(element);
		if (w == null)
		  return 0;
		else		
		{
			if (w instanceof TreeItem)
			{
			  TreeItem ti = (TreeItem)w;
			  int count = getItemCount((Item)w);
			  if (count == 1) // is it a dummy?
			  {
			  	Item[] items = getItems(ti);
			  	if (items[0].getData() == null)
			  	  count = 0; // assume a dummy
			  }
			  return count;
			}
			else
			  return getItemCount((Control)w);
		}
	}
	
	/** 
	 * Return the tree item of the first selected object
	 */
	protected TreeItem getFirstSelectedTreeItem()
	{
    	// find the selected tree item...
        Item[] selectedItems = ((Tree)getControl()).getSelection();		
        if ((selectedItems == null) || (selectedItems.length==0) || !(selectedItems[0] instanceof TreeItem))
          return null;
        return (TreeItem)selectedItems[0];        
	}
	/**
	 * Refresh the given tree item node
	 */
	protected void refreshTreeItem(TreeItem item)
	{
		// if we are already expanded, collapse and refresh to clear memory
		if (getExpanded(item))
		{
			collapseNode(item.getData(), true);
		    //setExpanded(selectedItem, false);
		    //refreshItem(selectedItem, selectedItem.getData());
		}
		// ok, now time to force an expand...
		createChildren(item); // re-expand. this calls the content provider, which calls the getChildren() method in the adapter. That will call us back.
		item.setExpanded(true);
	}

    /**
     * Called when a property is updated and we need to inform the Property Sheet viewer.
     * There is no formal mechanism for this so we simulate a selection changed event as
     *  this is the only event the property sheet listens for.
     */
    public void updatePropertySheet()
    {    	
    	ISelection selection = getSelection();
    	if (selection == null)
    	  return;
	    // create an event
	    SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
        // fire the event
        fireSelectionChanged(event);
    }
	
    /**
     * Called to select an object within the tree, and optionally expand it
     */   
    public void select(Object element, boolean expand)
    {
    	setSelection(new StructuredSelection(element), true); // true => reveal
    	if (expand)
    	  setExpandedState(element, true);
    }

    /**
     * Returns the tree item of the first selected object. Used for setViewerItem in a resource
     *  change event.
     */
    public Item getViewerItem()
    {
        TreeItem[] selectedItems = getTree().getSelection();
        if ((selectedItems != null) && (selectedItems.length>0))
          return selectedItems[0];
        else
          return null;
    }
    
    /**
     * Returns true if any of the selected items are currently expanded
     */
    public boolean areAnySelectedItemsExpanded()
    {
    	boolean expanded = false;
        Item[] selectedItems = ((Tree)getControl()).getSelection();		
        if ((selectedItems != null) && (selectedItems.length>0))
        {
          // for every selected tree item, see if it is currently expanded...
          for (int idx=0; !expanded && (idx<selectedItems.length); idx++)
          {
          	 if (selectedItems[idx] instanceof TreeItem)
          	 {
          	   if (((TreeItem)selectedItems[idx]).getExpanded())
          	     expanded = true;
          	 }
          }
        }    	 
    	return expanded;
    }
    /**
     * Returns true if any of the selected items are expandable but not yet expanded
     */
    public boolean areAnySelectedItemsExpandable()
    {
    	boolean expandable = false;
        Item[] selectedItems = ((Tree)getControl()).getSelection();		
        if ((selectedItems != null) && (selectedItems.length>0))
        {
          // for every selected tree item, see if needs expanding...
          for (int idx=0; !expandable && (idx<selectedItems.length); idx++)
          {
          	 if (selectedItems[idx] instanceof TreeItem)
          	 {
          	   if ((((TreeItem)selectedItems[idx]).getItemCount() > 0) &&
          	       !((TreeItem)selectedItems[idx]).getExpanded())
          	     expandable = true;
          	 }
          }
        }    	 
    	return expandable;
    }
    
    /**
     * Initialize drag and drop support for this view.
     * 
     */  
    protected void initDragAndDrop() 
    {
        int ops = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] transfers = new Transfer[]   
            { PluginTransfer.getInstance(), 
            /*ResourceTransfer.getInstance(),*/ 
            FileTransfer.getInstance(),
            EditorInputTransfer.getInstance()
            };    
        addDragSupport(ops | DND.DROP_DEFAULT, transfers, new SystemViewDataDragAdapter((ISelectionProvider)this));
        addDropSupport(ops | DND.DROP_DEFAULT, transfers, new SystemViewDataDropAdapter(this));
    }
    
    // ----------------------------------
    // Support for EXPAND TO-> ACTIONS...
    // ----------------------------------
    /**
     * Called when user selects an Expand To action to expand the selected remote object with a quick filter
     */
    public void expandTo(String filterString)
    {
    	SystemViewPart svp = getSystemViewPart();
    	if (svp == null)
    	  return;
    	// find the selected tree item...
        TreeItem selectedItem = getFirstSelectedTreeItem();
        if (selectedItem == null)
          return;
        Object element = selectedItem.getData();
		ISystemRemoteElementAdapter remoteAdapter = getRemoteAdapter(element);
		if (remoteAdapter == null)
		  return;		
		// update our hashtables, keyed by object address and tree path...
		if (expandToFiltersByObject == null)
		  expandToFiltersByObject = new Hashtable();
		if (expandToFiltersByTreePath == null)
		  expandToFiltersByTreePath = new Hashtable();
		if (filterString != null)
		  expandToFiltersByObject.put(selectedItem.getData(), filterString);
		else
		  expandToFiltersByObject.remove(selectedItem.getData());
		if (filterString != null)
		  expandToFiltersByTreePath.put(getItemPath(selectedItem), filterString);
		else
		  expandToFiltersByTreePath.remove(getItemPath(selectedItem));
		  
		// now refresh this tree item node...
		refreshTreeItem(selectedItem);
    }
    
    /**
     * Return the fully-qualified path up to the given item, expressible as a string
     */	
	protected String getItemPath(TreeItem item)
	{
		StringBuffer idBuffer = new StringBuffer(getItemNodeID(item));
		TreeItem[] elementNodes = getItemNodes(item);
		if (elementNodes != null)
		{
		   for (int idx=elementNodes.length-1; idx>=0; idx--)
		   {
		      item = elementNodes[idx];
		      idBuffer.append(SystemViewPart.MEMENTO_DELIM+getItemNodeID(item));
	       }
		}
		//System.out.println("MEMENTO HANDLE: " + idBuffer.toString());
		return idBuffer.toString();
	}
	/**
	 * Return the string identifying this node in the tree
	 */
	protected String getItemNodeID(TreeItem item)
	{
         //ISystemViewElementAdapter adapter = getAdapter(item.getData());
         //return adapter.getMementoHandle(item.getData());
         return item.getText();
	}
	
	/**
	 * Callback from the input provider to test if the given node has expand-to filtering criteria
	 */
	public String getExpandToFilter(Object element)
	{
		String filter = null;
		// for performance reasons, we first test for a binary match...
		if (expandToFiltersByObject != null)
		{
			filter = (String)expandToFiltersByObject.get(element);
		}
		// if binary match fails, look for tree path match...
		if ((filter==null) && (expandToFiltersByTreePath != null))
		{
			Widget item = findItem(element);
			if ((item != null) && (item instanceof TreeItem))
			{
			  filter = (String)expandToFiltersByTreePath.get(getItemPath((TreeItem)item));			
			  if (filter != null)
			  {
			  	if (expandToFiltersByObject == null)
			  	  expandToFiltersByObject = new Hashtable();
			  	expandToFiltersByObject.put(element, filter); // so next time it will be faster
			  }
			}
		}
		return filter;
	}
  
    /**
     * To support restoring state we need to write out to disk out current table that maps
     *   tree items to their current expand-to filter. That means we need access to the table.
     */
    public Hashtable getExpandToFilterTable()
    {
    	return expandToFiltersByTreePath;
    }   
    /**
     * To support restoring state we need to write out to disk out current table that maps
     *   tree items to their current expand-to filter. That means we need to be able to set the table.
     */
    public void setExpandToFilterTable(Hashtable ht)
    {
    	expandToFiltersByTreePath = ht;
    }     
    
	protected SystemRemoteElementResourceSet getSetFor(ISubSystem subSystem, ISystemViewElementAdapter adapter)
	{
		for (int i = 0; i < _setList.size(); i++)
		{
			SystemRemoteElementResourceSet set = (SystemRemoteElementResourceSet)_setList.get(i);
			if (set.getAdapter() == adapter && set.getSubSystem() == subSystem)
			{
				return set;
			}
		}
		
		// no existing set - create one
		SystemRemoteElementResourceSet newSet = new SystemRemoteElementResourceSet(subSystem, adapter);
		_setList.add(newSet);
		return newSet;
	}
}