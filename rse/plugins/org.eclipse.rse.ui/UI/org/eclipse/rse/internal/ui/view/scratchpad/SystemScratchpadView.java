/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Kevin Doyle (IBM) - [192278] Removed handleKeyPressed
 * Kevin Doyle (IBM) - [189150] _selectionFlagsUpdated reset after clear action performed
 * Kevin Doyle (IBM) - [195537] Use Hashlookup and ElementComparer
 * Kevin Doyle (IBM) - [189423] Scratchpad not completely updated after Delete.
 * Kevin Doyle (IBM) - [193151] Scratchpad not updated on Move
 * Kevin Doyle (IBM) - [189421] Scratchpad not updated after Rename
 * David McKnight   (IBM)        - [197860] drag and drop consistency - no text transfer
 * Kevin Doyle (IBM) - [197841] "Terminate and Remove" should remove the shell from Scratchpad
 * Xuan Chen   (IBM) - [160775] [api] rename (at least within a zip) blocks UI thread
 * David McKnight   (IBM)        - [224313] [api] Create RSE Events for MOVE and COPY holding both source and destination fields
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view.scratchpad;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvent;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.ISystemRemoteChangeListener;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;
import org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.internal.ui.actions.SystemCommonRenameAction;
import org.eclipse.rse.internal.ui.actions.SystemCommonSelectAllAction;
import org.eclipse.rse.internal.ui.actions.SystemOpenExplorerPerspectiveAction;
import org.eclipse.rse.internal.ui.actions.SystemShowInTableAction;
import org.eclipse.rse.internal.ui.actions.SystemSubMenuManager;
import org.eclipse.rse.internal.ui.view.ElementComparer;
import org.eclipse.rse.internal.ui.view.SafeTreeViewer;
import org.eclipse.rse.internal.ui.view.SystemView;
import org.eclipse.rse.internal.ui.view.SystemViewDataDragAdapter;
import org.eclipse.rse.internal.ui.view.SystemViewDataDropAdapter;
import org.eclipse.rse.internal.ui.view.SystemViewMenuListener;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemDeleteTarget;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemRenameTarget;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.internal.model.SystemRegistryUI;
import org.eclipse.rse.ui.internal.model.SystemScratchpad;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.model.ISystemShellProvider;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ContextObject;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemSelectAllTarget;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.PluginTransfer;


/**
 * This subclass of the standard JFace tabletree viewer is used to
 * show a generic tabletree view of the selected object 
 * <p>
 */
public class SystemScratchpadView
	// TODO change TreeViewer to ScratchpadViewer when Eclipse fixes SWT viewer 
//	extends ScratchpadViewer
	extends SafeTreeViewer
	implements IMenuListener, ISystemDeleteTarget, 
			   ISystemRenameTarget, ISystemSelectAllTarget, ISystemShellProvider,
			   ISystemResourceChangeListener, ISystemRemoteChangeListener,
			   ISelectionChangedListener, ISelectionProvider
{
	private Object _objectInput;
	//private ArrayList _attributeColumns;
	private TableLayout _layout;
	private SystemScratchpadViewProvider _provider;
	private MenuManager _menuManager;

	// these variables were copied from SystemView to allow for limited support
	// of actions.  I say limited because somethings don't yet work properly.
	protected SystemRefreshAction _refreshAction;
	protected PropertyDialogAction _propertyDialogAction;
	protected SystemOpenExplorerPerspectiveAction _openToPerspectiveAction;
	protected SystemShowInTableAction _showInTableAction;

	// global actions
	// Note the Edit menu actions are set in SystemViewPart. Here we use these
	//   actions from our own popup menu actions.
	protected SystemCommonDeleteAction _deleteAction;
	// for global delete menu item	
	protected SystemCommonRenameAction _renameAction;
	// for common rename menu item	
	protected SystemCommonSelectAllAction _selectAllAction;
	// for common Ctrl+A select-all
	protected ClearSelectedAction _clearSelectedAction;
	protected ClearAction 	_clearAllAction;

	protected boolean _selectionShowRefreshAction;
	protected boolean _selectionShowOpenViewActions;
	protected boolean _selectionShowDeleteAction;
	protected boolean _selectionShowRenameAction;
	protected boolean _selectionEnableDeleteAction;
	protected boolean _selectionEnableRenameAction;

	protected boolean _selectionIsRemoteObject = true;
	protected boolean _selectionFlagsUpdated = false;


	private ISystemMessageLine _messageLine;
	protected boolean     menuListenerAdded = false;
	
    private static final int LEFT_BUTTON = 1;
    private int mouseButtonPressed = LEFT_BUTTON;   
    
    public boolean debugRemote = false;
    
	/**
		 * Constructor for the table view
		 * 
		 */
		public SystemScratchpadView(Tree tableTree, ISystemMessageLine msgLine)
		{
			super(tableTree);
			_messageLine = msgLine;
			//_attributeColumns = new ArrayList();
			_layout = new TableLayout();

			_provider = new SystemScratchpadViewProvider(this);

			setUseHashlookup(true);
			setComparer(new ElementComparer()); 
			
			setContentProvider(_provider);
			
			IWorkbench wb = PlatformUI.getWorkbench();
			setLabelProvider(new DecoratingLabelProvider(_provider, wb.getDecoratorManager().getLabelDecorator()));	

			//setLabelProvider(_provider);

			_menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
			_menuManager.setRemoveAllWhenShown(true);
			_menuManager.addMenuListener(this);
			Menu menu = _menuManager.createContextMenu(tableTree);
			tableTree.setMenu(menu);

			addSelectionChangedListener(this);

			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			sr.addSystemResourceChangeListener(this);
			sr.addSystemRemoteChangeListener(this);

			initDragAndDrop();

			tableTree.setVisible(false);
			getControl().addMouseListener(new MouseAdapter() 
					{
					   public void mouseDown(MouseEvent e) 
					   {
					   	    mouseButtonPressed =  e.button;   		  //d40615 	    
					   }
					});		
		}

	public Layout getLayout()
	{
		return _layout;
	}


	/**
	 * Return the popup menu for the table
	 */
	public Menu getContextMenu()
	{
		return getTree().getMenu();
	}
	/**
	 * Return the popup menu for the table
	 */
	public MenuManager getContextMenuManager()
	{
		return _menuManager;
	}

	/**
	 * Called whenever the input for the view changes
	 */
	public void inputChanged(Object newObject, Object oldObject)
	{
		if (newObject instanceof IAdaptable)
		{
			getTree().setVisible(true);
			_objectInput = newObject;

	
			super.inputChanged(newObject, oldObject);
		}
		else if (newObject == null)
		{
			getTree().setVisible(false);
			_objectInput = null;
		}
	}

	public Object getInput()
	{
		return _objectInput;
	}

	/**
	 * Convenience method for retrieving the view adapter for an object 
	 */
	protected ISystemViewElementAdapter getViewAdapter(Object obj)
	{
		return SystemAdapterHelpers.getViewAdapter(obj);
	}



    /**
     * Initialize drag and drop support for this view.
     * 
     */  
    protected void initDragAndDrop() 
    {
        int ops = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] dragtransfers = new Transfer[]   
            { PluginTransfer.getInstance(), 
        		EditorInputTransfer.getInstance()
            };  
   
        Transfer[] droptransfers = new Transfer[]   
            { PluginTransfer.getInstance(), 
        		FileTransfer.getInstance(),
                EditorInputTransfer.getInstance()
             };  
        
        addDragSupport(ops | DND.DROP_DEFAULT, dragtransfers, new SystemViewDataDragAdapter(this));
        addDropSupport(ops | DND.DROP_DEFAULT, droptransfers, new SystemViewDataDropAdapter(this));
    }


	/**
	 * Used to asynchronously update the view whenever properties change.
	 */
	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{
		Object parent = event.getParent();
		Object child = event.getSource();
		int eventType = event.getType();
		switch (eventType)
		{
	 	case ISystemResourceChangeEvents.EVENT_RENAME_FILTER_REFERENCE:
	   	case ISystemResourceChangeEvents.EVENT_CHANGE_FILTER_REFERENCE:
	   	case ISystemResourceChangeEvents.EVENT_CHANGE_FILTERSTRING_REFERENCE:
	    	{
	   	    if (child instanceof ISystemFilter)
		    {
		        IRSEBaseReferencingObject[] references = ((ISystemFilter)child).getReferencingObjects();
		        for (int i = 0; i < references.length; i++)
		        {
		            IRSEBaseReferencingObject ref = references[i];
		            Widget w = findItem(ref);
		            if (w != null)
		            {
		                internalRefresh(ref);
		            }
		        }
	   	    }
	    	}
	   	    break;
			case ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE :
			case ISystemResourceChangeEvents.EVENT_PROPERTYSHEET_UPDATE :
			case ISystemResourceChangeEvents.EVENT_ICON_CHANGE:
				{
					Widget w = findItem(child);

					if (w != null)
					{
						updateItem(w, child);
					}
				}
				break;
			case ISystemResourceChangeEvents.EVENT_ADD :
			case ISystemResourceChangeEvents.EVENT_ADD_RELATIVE :
				{
					boolean addingConnection = (child instanceof IHost);
					if (_objectInput instanceof ISystemRegistry && addingConnection)
					{
						SystemScratchpadViewProvider provider = (SystemScratchpadViewProvider) getContentProvider();

						if (provider != null)
						{

							internalRefresh(_objectInput);
						}
					}
				}
				break;
			case ISystemResourceChangeEvents.EVENT_REFRESH:
			case ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE:
				{
			    	internalRefresh(child);
				}
				break;
			case ISystemResourceChangeEvents.EVENT_REFRESH_SELECTED:
				{
			    	IStructuredSelection sel = (IStructuredSelection)getSelection();
			    	Iterator iter = sel.iterator();
			    	while (iter.hasNext())
			    	{
			    	    Object obj = iter.next();
			    	    internalRefresh(obj);			    	
			    	}
				}
				break;
			case ISystemResourceChangeEvents.EVENT_RENAME:
				{
					Widget w = findItem(child);
					if (w != null)
					{
					    updateItem(w, child);
					}
				}
				break;
			case ISystemResourceChangeEvents.EVENT_DELETE:   	    	  
	    	case ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_REMOVED:
	    	  {
	    		  deleteObject(child);
	    	  }
	    	    break;
	    	case ISystemResourceChangeEvents.EVENT_DELETE_MANY:
	    	  {
	    		  Object[] multiSrc = event.getMultiSource();
	    		  if (multiSrc != null) {
		    		  for (int i = 0; i < multiSrc.length; i++) {
		    			  deleteObject(multiSrc[i]);
		    		  }
	    		  }
	    	  }
 	    	    break;    	 
			default :
				break;

		}

		if (child == _objectInput || parent == _objectInput)
		{
			SystemScratchpadViewProvider provider = (SystemScratchpadViewProvider) getContentProvider();

			if (provider != null)
			{


				try
				{
					internalRefresh(_objectInput);
				}
				catch (Exception e)
				{
					SystemBasePlugin.logError(e.getMessage());
				}
			}
		}
	}

	/**
	 * This is the method in your class that will be called when a remote resource
	 *  changes. You will be called after the resource is changed.
	 * @see org.eclipse.rse.core.events.ISystemRemoteChangeEvent
	 */
	public void systemRemoteResourceChanged(ISystemRemoteChangeEvent event)
	{
		int eventType = event.getEventType();
		Object remoteResourceParent = event.getResourceParent();
		Object remoteResource = event.getResource();
		//boolean originatedHere = (event.getOriginatingViewer() == this);
		List remoteResourceNames = null;
		if (remoteResource instanceof List)
		{
			remoteResourceNames = (List) remoteResource;
			remoteResource = remoteResourceNames.get(0);
		}
		String remoteResourceName = getRemoteResourceAbsoluteName(remoteResource);
		if (remoteResourceName == null)
			return;

		switch (eventType)
		{
			// --------------------------
			// REMOTE RESOURCE CHANGED...
			// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CHANGED :
				{
					if (remoteResourceParent == getInput())
					{
						Widget w = findItem(remoteResource);
						if (w != null)
						{
							updateItem(w, remoteResource);
						}

					}
				}
				break;

				// --------------------------
				// REMOTE RESOURCE CREATED...
				// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED :
				{
					// Since all creation operations can only be done on the same parent
					// we only need to handle refreshing of the parent
					// Must handle String's as Move operation create event passes us Strings
					if (remoteResourceParent instanceof String)
					{
						Item item = findFirstRemoteItemReference((String) remoteResourceParent, null, null);
						if (item != null)
							internalRefresh(item.getData());
					}
					else
					{
						internalRefresh(remoteResourceParent);
					}
				}
				break;

				// --------------------------
				// REMOTE RESOURCE DELETED...
				// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED :
				{
					if (remoteResourceNames != null) 
					{
						for (int i = 0; i < remoteResourceNames.size(); i++)
						{
							deleteObject(remoteResourceNames.get(i));
						}
					} 
					else
					{
						deleteObject(remoteResource);
					}
	        
			    /*
					{
						Object dchild = remoteResource;
						

						ISystemViewElementAdapter dadapt = getAdapter(dchild);
						if (dadapt != null)
						{
							SubSystem dSubSystem = dadapt.getSubSystem(dchild);
							String dkey = dadapt.getAbsoluteName(dchild);

							if (provider != null)
							{
								Object[] children = provider.getChildren(_objectInput);
								for (int i = 0; i < children.length; i++)
								{
									Object existingChild = children[i];
									if (existingChild != null)
									{
										ISystemViewElementAdapter eadapt = getAdapter(existingChild);
										SubSystem eSubSystem = eadapt.getSubSystem(existingChild);

										if (dSubSystem == eSubSystem)
										{
											String ekey = eadapt.getAbsoluteName(existingChild);
											if (ekey.equals(dkey))
											{
													// do a full refresh
													refresh();
											}
										}

									}
								}
							}
						}
					}
					*/

				}
				break;

				// --------------------------
				// REMOTE RESOURCE RENAMED...
				// --------------------------
			case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED :
				{
					renameRemoteObject(remoteResource, event.getOldNames()[0]);
				}

				break;
		}
	}

	/**
	 * Turn a given remote object reference into a fully qualified absolute name
	 */
	private String getRemoteResourceAbsoluteName(Object remoteResource)
	{
		if (remoteResource == null)
			return null;
		String remoteResourceName = null;
		if (remoteResource instanceof String)
			remoteResourceName = (String) remoteResource;
		else
		{
			ISystemViewElementAdapter ra = getViewAdapter(remoteResource);
			if (ra == null)
				return null;
			remoteResourceName = ra.getAbsoluteName(remoteResource);
		}
		return remoteResourceName;
	}

	/**
	 * Display a message/status on the message/status line
	 */
	public void displayMessage(String msg)
	{
		if (_messageLine != null)
		  _messageLine.setMessage(msg);
	}
	/**
	 * Clear message/status shown on the message/status line
	 */
	public void clearMessage()
	{
		if (_messageLine != null)
		  _messageLine.clearMessage();
	}
	
	public void selectionChanged(SelectionChangedEvent event)
	{

			// Set _selectionFlagsUpdate to false, so the next time the context menu
			// is requested we rescan the selections to determine which actions
			// to show and enable/disable.
			// Setting this to false will force a rescan because in fillContextMenu()
			// if this is false it will call scanSelections();
			_selectionFlagsUpdated = false;
		    IStructuredSelection sel = (IStructuredSelection)event.getSelection();		
			Object firstSelection = sel.getFirstElement();
			if (firstSelection == null)
			  return;
			
			ISystemViewElementAdapter adapter = getViewAdapter(firstSelection);
			if (adapter != null)
			{
			   displayMessage(adapter.getStatusLineText(firstSelection));
			   if ((mouseButtonPressed == LEFT_BUTTON))   
			      adapter.selectionChanged(firstSelection);	
			}  
			else
			  clearMessage();
	}

	public void dispose()
	{
		removeAsListener();

		Composite tree = getTree();
		
		boolean isDisposed = tree.isDisposed();
		
		// dispose control if not disposed
		if (!isDisposed) {
			tree.dispose();
		}
	}
	
	/**
	 * Remove as listener.
	 */
	public void removeAsListener() {
		
		// remove listeners
		removeSelectionChangedListener(this);
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		sr.removeSystemResourceChangeListener(this);
		sr.removeSystemRemoteChangeListener(this);

		//Composite tree = getTree();
		//boolean isDisposed = tree.isDisposed();
	}

	/*
	 * Everything below is basically stuff copied and pasted from SystemsView
	 * -There needs to be cleaning up of the below code as some of this stuff
	 * is broken for the table view
	 * 
	 *
	public void createStandardGroups(IMenuManager menu)
	{
		if (!menu.isEmpty())
			return;
		// simply sets partitions in the menu, into which actions can be directed.
		// Each partition can be delimited by a separator (new Separator) or not (new GroupMarker).
		// Deleted groups are not used yet.
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_NEW));
		// new->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GOTO));
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_EXPANDTO));
		// expand to->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_EXPAND));
		// expand, collapse
		// goto into, go->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPENWITH));
		// open with->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_BROWSEWITH));
		// browse with ->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPEN));
		// open xxx
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_WORKWITH));
		// work with->
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_SHOW));         // show->type hierarchy, in-navigator
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_BUILD));
		// build, rebuild, refresh
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CHANGE));
		// update, change
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE));
		// rename,move,copy,delete,bookmark,refactoring
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER));
		// move up, move down		
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GENERATE));
		// getters/setters, etc. Typically in editor
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_SEARCH));
		// search
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CONNECTION));
		// connection-related actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_IMPORTEXPORT));
		// get or put actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADAPTERS));
		// actions queried from adapters
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS));
		// user or BP/ISV additions
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_VIEWER_SETUP)); // ? Probably View->by xxx, yyy
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_TEAM));
		// Team
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_COMPAREWITH));
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_REPLACEWITH));
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_PROPERTIES));
		// Properties
	}*/

	/**
	  * Rather than pre-defining this common action we wait until it is first needed,
	  *  for performance reasons.
	  */
	protected PropertyDialogAction getPropertyDialogAction()
	{
		if (_propertyDialogAction == null)
		{
			_propertyDialogAction = new PropertyDialogAction(new SameShellProvider(getShell()), this);
			//propertyDialogAction.setToolTipText(" "); 
		}
		_propertyDialogAction.selectionChanged(getSelection());
		return _propertyDialogAction;
	}

	
	/**
	 * Return the select All action
	 */
	protected IAction getSelectAllAction()
	{
		if (_selectAllAction == null)
			_selectAllAction = new SystemCommonSelectAllAction(getShell(), this, this);
		return _selectAllAction;
	}

	/**
	 * Rather than pre-defined this common action we wait until it is first needed,
	 *  for performance reasons.
	 */
	protected IAction getRenameAction()
	{
		if (_renameAction == null)
			_renameAction = new SystemCommonRenameAction(getShell(), this);
		return _renameAction;
	}
	/**
	 * Rather than pre-defined this common action we wait until it is first needed,
	 *  for performance reasons.
	 */
	protected IAction getDeleteAction()
	{
		if (_deleteAction == null)
			_deleteAction = new SystemCommonDeleteAction(getShell(), this);
		return _deleteAction;
	}

	protected IAction getClearSelectedAction()
	{
	    if (_clearSelectedAction == null)
	        _clearSelectedAction = new ClearSelectedAction(this);
	    _clearSelectedAction.checkEnabledState();
	    return _clearSelectedAction;
	}
	
	protected IAction getClearAllAction()
	{
	    if (_clearAllAction == null)
	        _clearAllAction = new ClearAction(this);
	    _clearAllAction.checkEnabledState();
	    return _clearAllAction;
	}
	
	/**
	* Return the refresh action
	*/
	protected IAction getRefreshAction()
	{
		if (_refreshAction == null)
			_refreshAction = new SystemRefreshAction(getShell());
		return _refreshAction;
	}
	/*
	 * Get the common "Open to->" action for opening a new Remote System Explorer view,
	 *  scoped to the currently selected object.
	 *
	protected SystemCascadingOpenToAction getOpenToAction()
	{
		if (openToAction == null)
		  openToAction = new SystemCascadingOpenToAction(getShell(),getWorkbenchWindow());
		return openToAction;
	} NOT USED YET */
	/**
	 * Get the common "Open to->" action for opening a new Remote System Explorer view,
	 *  scoped to the currently selected object.
	 */
	protected SystemOpenExplorerPerspectiveAction getOpenToPerspectiveAction()
	{
		if (_openToPerspectiveAction == null)
		{
			IWorkbench desktop = PlatformUI.getWorkbench();
			IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();

			_openToPerspectiveAction = new SystemOpenExplorerPerspectiveAction(getShell(), win);
		}
		//getWorkbenchWindow());
		return _openToPerspectiveAction;
	}

	protected SystemShowInTableAction getShowInTableAction()
	{
		if (_showInTableAction == null)
		{
			_showInTableAction = new SystemShowInTableAction(getShell());
		}
		//getWorkbenchWindow());
		return _showInTableAction;
	}

	public Shell getShell()
	{
		return getTree().getShell();
	}

	/**
	 * Required method from ISystemDeleteTarget.
	 * Decides whether to even show the delete menu item.
	 * Assumes scanSelections() has already been called
	 */
	public boolean showDelete()
	{
		if (!_selectionFlagsUpdated)
			scanSelections();
		return _selectionShowDeleteAction;
	}
	/**
	 * Required method from ISystemDeleteTarget
	 * Decides whether to enable the delete menu item. 
	 * Assumes scanSelections() has already been called
	 */
	public boolean canDelete()
	{
		if (!_selectionFlagsUpdated)
			scanSelections();
		return _selectionEnableDeleteAction;
	}

	/*
	 * Required method from ISystemDeleteTarget
	 */
	public boolean doDelete(IProgressMonitor monitor)
	{
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();
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
				element = elements.next();
				//multiSource[idx++] = element;
				adapter = getViewAdapter(element);
				ok = adapter.doDelete(getShell(), element, monitor);
				if (ok)
				{
					anyOk = true;
					deletedVector.addElement(element);
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
			String msg = exc.getMessage();
			if ((msg == null) || (exc instanceof ClassCastException))
				msg = exc.getClass().getName();
			SystemMessageDialog.displayErrorMessage(getShell(), RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_DELETING).makeSubstitution(element, msg));
			ok = false;
		}
		if (anyOk)
		{
			Object[] deleted = new Object[deletedVector.size()];
			for (int idx = 0; idx < deleted.length; idx++)
				deleted[idx] = deletedVector.elementAt(idx);
			if (_selectionIsRemoteObject)
				sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, deletedVector, null, null, null, this);
			else
				sr.fireEvent(new org.eclipse.rse.core.events.SystemResourceChangeEvent(deleted, ISystemResourceChangeEvents.EVENT_DELETE_MANY, getInput()));
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
		if (!_selectionFlagsUpdated)
			scanSelections();
		return _selectionShowRenameAction;
	}
	/**
	 * Required method from ISystemRenameTarget
	 * Decides whether to enable the rename menu item. 
	 * Assumes scanSelections() has already been called
	 */
	public boolean canRename()
	{
		if (!_selectionFlagsUpdated)
			scanSelections();
		return _selectionEnableRenameAction;
	}

	// default implementation
	// in default table, parent is input 
	protected Object getParentForContent(Object element)
	{
		return _objectInput;
	}

	/**
	* Required method from ISystemRenameTarget
	*/
	public boolean doRename(String[] newNames)
	{
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();
		//int selectedCount = selection.size();
		Object element = null;

		ISystemViewElementAdapter adapter = null;
		ISystemRemoteElementAdapter remoteAdapter = null;
		String oldFullName = null;
		boolean ok = true;
		try
		{
			int nameIdx = 0;
			while (ok && elements.hasNext())
			{
				element = elements.next();
				adapter = getViewAdapter(element);
				Object parentElement = getParentForContent(element);

				remoteAdapter = getRemoteAdapter(element);
				if (remoteAdapter != null)
					oldFullName = remoteAdapter.getAbsoluteName(element);
				// pre-rename
				ok = adapter.doRename(getShell(), element, newNames[nameIdx++], null);
				if (ok)
				{
					if (remoteAdapter != null) {
						sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED, element, parentElement, remoteAdapter.getSubSystem(element), new String[]{oldFullName}, this);
					}
					else {
						sr.fireEvent(new org.eclipse.rse.core.events.SystemResourceChangeEvent(element, ISystemResourceChangeEvents.EVENT_RENAME, parentElement));
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
			//String msg = exc.getMessage();
			//if ((msg == null) || (exc instanceof ClassCastException))
			//  msg = exc.getClass().getName();
			SystemMessageDialog.displayErrorMessage(getShell(), RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_RENAMING).makeSubstitution(element, exc),
			//msg),
			exc);
			ok = false;
		}
		return ok;
	}

	/**
	 * Returns the implementation of ISystemRemoteElement for the given
	 * object.  Returns null if this object does not adaptable to this.
	 */
	protected ISystemRemoteElementAdapter getRemoteAdapter(Object o)
	{
		ISystemRemoteElementAdapter adapter = null;
		if (!(o instanceof IAdaptable))
			adapter = (ISystemRemoteElementAdapter) Platform.getAdapterManager().getAdapter(o, ISystemRemoteElementAdapter.class);
		else
			adapter = (ISystemRemoteElementAdapter) ((IAdaptable) o).getAdapter(ISystemRemoteElementAdapter.class);
		if ((adapter != null) && (adapter instanceof ISystemViewElementAdapter))
			 ((ISystemViewElementAdapter) adapter).setViewer(this);
		return adapter;
	}

	/**
	* Return true if select all should be enabled for the given object.
	* For a tree view, you should return true if and only if the selected object has children.
	* You can use the passed in selection or ignore it and query your own selection.
	*/
	public boolean enableSelectAll(IStructuredSelection selection)
	{
		return true;
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

			Tree theTree = tree;
			theTree.setSelection(theTree.getItems());
			TreeItem[] items = theTree.getItems();
			Object[] objects = new Object[items.length];
			for (int idx = 0; idx < items.length; idx++)
				objects[idx] = items[idx].getData();
			fireSelectionChanged(new SelectionChangedEvent(this, new StructuredSelection(objects)));

	
	}

	public void menuAboutToShow(IMenuManager manager)
	{
		SystemView.createStandardGroups(manager);
		 if (!menuListenerAdded)
	   	    {
	   	      if (manager instanceof MenuManager)
	   	      {
	   	      	Menu m = ((MenuManager)manager).getMenu();
	   	      	if (m != null)
	   	      	{
	   	      		menuListenerAdded = true;
	   	      		SystemViewMenuListener ml = new SystemViewMenuListener();
	   	      		if (_messageLine != null)
	   	      		  ml.setShowToolTipText(true, _messageLine);
	   	      		m.addMenuListener(ml);
	   	      	}
	   	      }
	   	    }
		fillContextMenu(manager);
	}

	public ISelection getSelection()
	{
		ISelection selection = super.getSelection();
		if (selection == null || selection.isEmpty())
		{
			// make the selection the parent
			ArrayList list = new ArrayList();
			if (_objectInput != null)
			{
				list.add(_objectInput);
				selection = new StructuredSelection(list);
			}
		}

		return selection;
	}

	public void fillContextMenu(IMenuManager menu) {
		
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		
		boolean allSelectionsFromSameParent = true;
		int selectionCount = selection.size();
		
		
		
		if (selectionCount == 0) // nothing selected
		{
			return;		   		    
		}
		else
		{
			
			if (selectionCount == 1) {
				
				if (selection.getFirstElement() == getInput()) {
					//return;
				}
			}
			
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
					SystemView.createStandardGroups(menu);
					return;
				}
			 }
		   }
			 
			// Partition into groups...
			SystemView.createStandardGroups(menu);

			// ADD COMMON ACTIONS...
			 // COMMON REFRESH ACTION...
	   	      if (showRefresh())
	   	      {
			    menu.appendToGroup(ISystemContextMenuConstants.GROUP_BUILD, getRefreshAction());
	   	      }
	   	      
			// COMMON RENAME ACTION...
			if (canRename())
			{
				if (showRename())
					menu.appendToGroup(ISystemContextMenuConstants.GROUP_REORGANIZE, getRenameAction());
			}

			// ADAPTER SPECIFIC ACTIONS   	      
			SystemMenuManager ourMenu = new SystemMenuManager(menu);

			Iterator elements = selection.iterator();
			Hashtable adapters = new Hashtable();
			while (elements.hasNext())
			{
				Object element = elements.next();
				ISystemViewElementAdapter adapter = getViewAdapter(element);
				if (adapter != null)
				{
					adapters.put(adapter, element); // want only unique adapters
				}
			}
			Enumeration uniqueAdapters = adapters.keys();
			Shell shell = getShell();
			while (uniqueAdapters.hasMoreElements())
			{
				ISystemViewElementAdapter nextAdapter = (ISystemViewElementAdapter) uniqueAdapters.nextElement();
				nextAdapter.addActions(ourMenu, selection, shell, ISystemContextMenuConstants.GROUP_ADAPTERS);

				if (nextAdapter instanceof AbstractSystemViewAdapter)
				{
					AbstractSystemViewAdapter aVA = (AbstractSystemViewAdapter)nextAdapter;
					// add remote actions
					aVA.addCommonRemoteActions(ourMenu, selection, shell, ISystemContextMenuConstants.GROUP_ADAPTERS);
					
					// add dynamic menu popups
					aVA.addDynamicPopupMenuActions(ourMenu, selection, shell,  ISystemContextMenuConstants.GROUP_ADDITIONS);
				}
			}

		
			
			
			// wail through all actions, updating shell and selection
			IContributionItem[] items = menu.getItems();
			for (int idx = 0; idx < items.length; idx++)
			{
				if ((items[idx] instanceof ActionContributionItem) && (((ActionContributionItem) items[idx]).getAction() instanceof ISystemAction))
				{
					ISystemAction item = (ISystemAction) (((ActionContributionItem) items[idx]).getAction());
					item.setInputs(getShell(), this, selection);
				}
				else if (items[idx] instanceof SystemSubMenuManager)
				{
					SystemSubMenuManager item = (SystemSubMenuManager) items[idx]; 	
					item.setInputs(getShell(), this, selection);
				}
			}

			// COMMON DELETE ACTION...
			if (canDelete() && showDelete())
			{
				//menu.add(getDeleteAction());
				menu.appendToGroup(ISystemContextMenuConstants.GROUP_REORGANIZE, getDeleteAction());
				((ISystemAction) getDeleteAction()).setInputs(getShell(), this, selection);
				menu.add(new Separator());
			}
			
			// REMOVE FROM VIEW ACTION...
			
			menu.appendToGroup(ISystemContextMenuConstants.GROUP_ADDITIONS, getClearSelectedAction());
			menu.appendToGroup(ISystemContextMenuConstants.GROUP_ADDITIONS, getClearAllAction());

			// PROPERTIES ACTION...
			// This is supplied by the system, so we pretty much get it for free. It finds the
			// registered propertyPages extension points registered for the selected object's class type.
			//propertyDialogAction.selectionChanged(selection);		  

	
			PropertyDialogAction pdAction = getPropertyDialogAction();
			if (pdAction.isApplicableForSelection())
			{

				menu.appendToGroup(ISystemContextMenuConstants.GROUP_PROPERTIES, pdAction);
			}
			// OPEN IN NEW PERSPECTIVE ACTION... if (fromSystemViewPart && showOpenViewActions())
			if (!_selectionIsRemoteObject)
			{
				//SystemCascadingOpenToAction openToAction = getOpenToAction();
				SystemOpenExplorerPerspectiveAction openToPerspectiveAction = getOpenToPerspectiveAction();
				SystemShowInTableAction showInTableAction = getShowInTableAction();
				openToPerspectiveAction.setSelection(selection);
				showInTableAction.setSelection(selection);
				//menu.appendToGroup(ISystemContextMenuConstants.GROUP_OPEN, openToAction.getSubMenu());
				menu.appendToGroup(ISystemContextMenuConstants.GROUP_OPEN, openToPerspectiveAction);
				menu.appendToGroup(ISystemContextMenuConstants.GROUP_OPEN, showInTableAction);

			}
		
	

		}
	}
	
	
    /**
     * Decides whether to even show the refresh menu item.
     * Assumes scanSelections() has already been called
     */
    protected boolean showRefresh()
    {
    	if (!_selectionFlagsUpdated)
    		scanSelections();
    	return _selectionShowRefreshAction;
    }
	
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
		
		if ((items == null) || (items.length ==0)) {
		  return true;
		}
		
		TreeItem prevParent = null;
		TreeItem currParent = null;
		
		for (int idx = 0; idx < items.length; idx++)
		{
		   currParent = items[idx].getParentItem();
		             
		   if ((idx>0) && (currParent != prevParent)) {
			 same = false;
			 break;
		   }
		   else
		   {
			 prevParent = currParent;  
		   }
		}
		return same;
	}
	
	private boolean selectionHasAncestryRelationship() {
		Tree tree = getTree();
		
		TreeItem[] items = tree.getSelection();

		for (int idx=0; idx<items.length; idx++)
		{
			TreeItem item = items[idx];
			
			for (int c=0; c < items.length; c++)
			{
				if (item != items[c])
				{					
					if (isAncestorOf(item, items[c], false))
					{
						return true;
					}
				}
			}
		}
		return false;		
	}
	
	/**
	 * Returns whether an item is an ancestor of another item. The ancestor can be direct or indirect.
	 * @param container the item which might be an ancestor.
	 * @param item the child.
	 * @param direct <code>true</code> if the container must be a direct ancestor of the child item,
	 * 				 <code>false</code> otherwise.
	 * @return <code>true</code> if there is an ancestry relationship, <code>false</code> otherwise.
	 */
	private boolean isAncestorOf(TreeItem container, TreeItem item, boolean direct)
	{
		TreeItem[] children = null;
		
		// does not have to be a direct ancestor
		if (!direct) {
			// get the children of the container's parent, i.e. the container's siblings
			// as well as itself
			TreeItem parent = container.getParentItem();
			
			// check if parent is null
			// parent is null if the container is a root item
			if (parent != null) {
				children = parent.getItems();
			}
			else {
				children = getTree().getItems();
			}
		}
		// must be a direct ancestor
		else {
			// get the children of the container
			children = container.getItems();
		}
			
		// go through all the children
		for (int i = 0; i < children.length; i++) {

			TreeItem child = children[i];

			// if one of the children matches the child item, return true
			if (child == item && direct) {
				return true;
			}
			// otherwise, go through children, and see if any of those are ancestors of
			// the child item 
			else if (child.getItemCount() > 0) {
				
				// we check for direct ancestry
				if (isAncestorOf(child, item, true)) {
					return true;
				}
			}
		}
		
		return false;
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
	protected void scanSelections()
	{
		// initial these variables to true. Then if set to false even once, leave as false always...
		_selectionShowRefreshAction = true;
		_selectionShowOpenViewActions = true;
		_selectionShowDeleteAction = true;
		_selectionShowRenameAction = true;
		_selectionEnableDeleteAction = true;
		_selectionEnableRenameAction = true;
		_selectionIsRemoteObject = true;
		_selectionFlagsUpdated = true;

		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();
		while (elements.hasNext())
		{
			Object element = elements.next();
			ISystemViewElementAdapter adapter = getViewAdapter(element);
			if (adapter != null)
			{
				if (_selectionShowRefreshAction)
					_selectionShowRefreshAction = adapter.showRefresh(element);
	
				if (_selectionShowOpenViewActions)
					_selectionShowOpenViewActions = adapter.showOpenViewActions(element);
	
				if (_selectionShowDeleteAction)
					_selectionShowDeleteAction = adapter.showDelete(element);
	
				if (_selectionShowRenameAction)
					_selectionShowRenameAction = adapter.showRename(element);
	
				if (_selectionEnableDeleteAction)
					_selectionEnableDeleteAction = _selectionShowDeleteAction && adapter.canDelete(element);
				//System.out.println("ENABLE DELETE SET TO " + selectionEnableDeleteAction);
	
				if (_selectionEnableRenameAction)
					_selectionEnableRenameAction = _selectionShowRenameAction && adapter.canRename(element);
	
				if (_selectionIsRemoteObject)
					_selectionIsRemoteObject = (getRemoteAdapter(element) != null);
			}
		}

	}

	/**
	 * Overridden so that we can pass a wrapper IContextObject into the provider to get children instead 
	 * of the model object, itself
	 */
	protected void createChildren(final Widget widget) 
	{
		if (widget instanceof TreeItem)
		{
		final Item[] tis = getChildren(widget);
		if (tis != null && tis.length > 0) {
			Object data = tis[0].getData();
			if (data != null) {
				return; // children already there!
			}
		}

		BusyIndicator.showWhile(widget.getDisplay(), new Runnable() {
			public void run() {
				// fix for PR 1FW89L7:
				// don't complain and remove all "dummies" ...
				if (tis != null) {
					for (int i = 0; i < tis.length; i++) {
						if (tis[i].getData() != null) {
							disassociate(tis[i]);
							Assert.isTrue(tis[i].getData() == null,
									"Second or later child is non -null");//$NON-NLS-1$

						}
						tis[i].dispose();
					}
				}
				Object d = widget.getData();
				if (d != null) 
				{
					Object parentElement = getContextObject((TreeItem)widget);
					Object[] children = getSortedChildren(parentElement);
					if (children != null)
					{
						for (int i = 0; i < children.length; i++) 
						{	
							createTreeItem(widget, children[i], -1);
						}
					}
				}
			}

		});
		}
		else
		{
			super.createChildren(widget);
		}
	}
	
	
	/**
	 * Get the containing filter reference for an item
	 * @param item the item to get the filter reference for
	 * @return the filter reference
	 */
	public ISystemFilterReference getContainingFilterReference(TreeItem item)
	{
		Object data = item.getData();
		if (data instanceof ISystemFilterReference)
		{
			return (ISystemFilterReference)data;
		}
		else
		{
			TreeItem parent = item.getParentItem();
			if (parent != null)
			{
				return getContainingFilterReference(parent);
			}
			else				
			{
				Object input = getInput();
				if (input instanceof ISystemFilterReference)
				{
					return (ISystemFilterReference)input;
				}
				else
				{
					return null;
				}
			}
		}
	}
	
	/**
	 * Get the containing subsystem from an item
	 * @param item the item to get the subsystem for
	 * @return the subsystem
	 */
	public ISubSystem getContainingSubSystem(TreeItem item)
	{
		Object data = item.getData();
		if (data instanceof ISubSystem)
		{
			return (ISubSystem)data;
		}
		else
		{
			TreeItem parent = item.getParentItem();
			if (parent != null)
			{
				return getContainingSubSystem(parent);
			}
			else				
			{
				Object input = getInput();
				if (input instanceof ISubSystem)
				{
					return (ISubSystem)input;
				}
				else
				{
					return null;
				}
			}
		}
	}
	
	/**
	 * Get the context object from a tree item
	 * @param item the item to get the context for
	 * @return the context object
	 */
	public IContextObject getContextObject(TreeItem item)
	{
		Object data = item.getData();
		ISystemFilterReference filterReference = getContainingFilterReference(item);
		if (filterReference != null)
		{
			return new ContextObject(data, filterReference.getSubSystem(), filterReference);
		}
		else
		{
			ISubSystem subSystem = getContainingSubSystem(item);
			if (subSystem != null)
			{
				return new ContextObject(data, subSystem);
			}
			else
			{				
				return new ContextObject(data);
			}
		}
	}
	
	/**
	 * Overrides the standard viewer method to get the model object from the context object
	 */
	public void add(Object parentElementOrTreePath, Object[] childElements) {
		Assert.isNotNull(parentElementOrTreePath);
		assertElementsNotNull(childElements);
		
		if (parentElementOrTreePath instanceof IContextObject)
		{
			parentElementOrTreePath = ((IContextObject)parentElementOrTreePath).getModelObject();
		}
		super.add(parentElementOrTreePath, childElements);
	}
	
	/**
	 * Delete all occurrences of a given remote object
	 */
	protected void deleteObject(Object deleteObject) 
	{
		// If deleteObject is a string find it's tree item and get it's data as thats what's
		// stored inside the SystemScratchpad
		if (deleteObject instanceof String)
		{
			Item item = findFirstRemoteItemReference((String) deleteObject,null,null);
			if (item == null)
				return;
			else
				deleteObject = item.getData();
		}
		
		// check if remoteResource is a root in Scratchpad and if so remove it
		SystemScratchpad scratchpad = SystemRegistryUI.getInstance().getSystemScratchPad();
		if (scratchpad.contains(deleteObject))
			scratchpad.removeChild(deleteObject);
		
		// find all references to the remote resource and remove them
		Widget[] widgets = findItems(deleteObject);
		for (int i = 0; i < widgets.length; i++)
		{
			if (widgets[i] instanceof TreeItem)
			{
				remove(getTreePathFromItem((TreeItem) widgets[i]));
			}
		}
		
	}

	/**
	 * Rename a remote object. 
	 */
	protected void renameRemoteObject(Object renameObject, String oldElementName) {
		String[] properties = new String[1];
		properties[0] = IBasicPropertyConstants.P_TEXT;
		List matches = new Vector();
		ISubSystem subsystem = null;
		String newElementName = ""; //$NON-NLS-1$
		Object refreshObject = null;
		
		// Try to determine the new element name/subsystem to help improve performance when searching for objects
		if (renameObject instanceof String) {
			newElementName = (String) renameObject;
		} else {
			ISystemViewElementAdapter rmtAdapter = getViewAdapter(renameObject);
			if (rmtAdapter != null) {
				subsystem = rmtAdapter.getSubSystem(renameObject);
				newElementName = rmtAdapter.getName(renameObject);
			}
		}
		
		findAllRemoteItemReferences(oldElementName, renameObject, subsystem, matches);
		
		// Go through all the matches and update the properties
		for (int i = 0; i < matches.size(); i++) {
			Item item = (Item) matches.get(i);
			if (item != null && !item.isDisposed()) {
				Object data = item.getData();
				
				if (data != null) {
					ISystemRemoteElementAdapter	remoteAdapter = (ISystemRemoteElementAdapter)((IAdaptable)data).getAdapter(ISystemRemoteElementAdapter.class);			
				
					if (remoteAdapter != null && data != renameObject)
					{
						remoteAdapter.refreshRemoteObject(data, renameObject);
					}
					item.setText(newElementName);
					internalUpdate(item, data, properties);
					
					if (refreshObject == null) {
						refreshObject = data;
					}
					
				}
			}
		}

		if (refreshObject != null) {
			// Update the Scratchpad Model
			SystemRegistryUI.getInstance().getSystemScratchPad().replace(oldElementName, refreshObject);
			
			// FTP/SSH require a new listing of files otherwise they will
			// display the old file names.
			// We can't depend on SystemView to do the refresh as it does it in a
			// deferred job and we don't know when that is done or what
			// order the listeners are in.
			if (refreshObject instanceof ISystemContainer) {
				((ISystemContainer) refreshObject).markStale(true);
			}
			internalRefresh(refreshObject);
		}
	}
	
	//TODO:
	// ----------------------------------------------------------------
	// Functions Below are pure copies of the ones from SystemView
	// and as such should be moved to a common location
	// or with 3.0 remove all need for Recursive methods - Preferred
	// ----------------------------------------------------------------
	
	/**
	 * Find the first binary-match or name-match of remote object, given its absolute name.
	 * @param remoteObjectName The absolute name of the remote object to find.
	 * @param subsystem The subsystem of the remote object to find. Optional.
	 * @param parentItem The parent item at which to start the search. Optional.
	 * @return TreeItem hit if found
	 */
	public Item findFirstRemoteItemReference(String remoteObjectName, ISubSystem subsystem, Item parentItem) {
		//List matches = new Vector();
		Item match = null;
		if (parentItem == null)
			//findAllRemoteItemReferences(remoteObjectName, null, subsystem, matches);
			match = internalFindFirstRemoteItemReference(remoteObjectName, null, subsystem);
		else {

			//recursiveFindAllRemoteItemReferences(parentItem, remoteObjectName, null, subsystem, matches);    	  
			match = recursiveFindFirstRemoteItemReference(parentItem, remoteObjectName, null, subsystem);
		}
		//if (matches.size() > 0)
		//  return (Item)matches.elementAt(0);
		//else
		//  return null;
		return match;
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
	 * @return TreeItem hit if found
	 */
	protected Item internalFindFirstRemoteItemReference(String searchString, Object elementObject, ISubSystem subsystem) {
		Item[] roots = getTree().getItems();
		if ((roots == null) || (roots.length == 0)) return null;
		
		// use map first
		Item match = mappedFindFirstRemoteItemReference(elementObject);
		
		for (int idx = 0; (match == null) && (idx < roots.length); idx++) {
			//System.out.println("recursiveFindFirstRemoteItemReference(parentItem, remoteObjectName, remoteObject, subsystem)");
			match = recursiveFindFirstRemoteItemReference(roots[idx], searchString, elementObject, subsystem);
		}

		return match;
	}
	
	protected Item mappedFindFirstRemoteItemReference(Object elementObject)
	{
		return (Item)findItem(elementObject);
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
	protected Item recursiveFindFirstRemoteItemReference(Item parent, String elementName, Object elementObject, ISubSystem subsystem) {
		Object rawData = parent.getData();
		ISystemViewElementAdapter remoteAdapter = null;
		// ----------------------------
		// what are we looking at here?
		// ----------------------------
		if (rawData != null) remoteAdapter = getViewAdapter(rawData);
		// -----------------------------------------------------------------------
		// if this is a remote object, test if it is the one we are looking for...
		// -----------------------------------------------------------------------
		if (remoteAdapter != null) {
			// first test for binary match
			if (elementObject == rawData) {
				if (debugRemote) System.out.println("Remote item binary match found"); //$NON-NLS-1$
				return parent; // return the match
			}
			// now test for absolute name match
			String fqn = remoteAdapter.getAbsoluteName(rawData);
			if (debugRemote) System.out.println("TESTING FINDFIRST: '" + fqn + "' vs '" + elementName + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if ((fqn != null) && fqn.equals(elementName)) {
				if ((subsystem != null) && (subsystem == remoteAdapter.getSubSystem(rawData))) {
					if (debugRemote) System.out.println("Remote item name match found and subsystems matched"); //$NON-NLS-1$
					return parent; // return the match
				} else if (subsystem == null) {
					if (debugRemote) System.out.println("Remote item name match found and subsystem null"); //$NON-NLS-1$
					return parent;
				} else if (debugRemote) System.out.println("Remote item name match found but subsystem mismatch"); //$NON-NLS-1$
			}
		}
		// -------------------------------------------------------------------------
		// if we have been given a subsystem to restrict to, that is a hint to us...
		// -------------------------------------------------------------------------
		else if ((rawData != null) && (subsystem != null)) // test for hints we are in the wrong place
		{
			// if we are currently visiting a subsystem, and that subsystem is not from the same
			//  factory, then we can assume the remote object occurrences we are looking for are
			//  not to be found within this branch...
			if ((rawData instanceof ISubSystem) && (rawData != subsystem)) {
				return null; // they don't match, so don't bother checking the kids
			}
			// if we are currently visiting a connection, and that connection's hostname is not the same
			//  as that of our given subsystem, then we can assume the remote object occurrences we are 
			//  looking for are not to be found within this branch...
			else if ((rawData instanceof IHost) && !((IHost) rawData).getHostName().equals(subsystem.getHost().getHostName())) {
				return null; // they don't match, so don't bother checking the kids
			}
		}
		// recurse over children	    
		Item[] items = getChildren(parent);
		Item match = null;
		for (int i = 0; (match == null) && (i < items.length); i++) {
			if (!items[i].isDisposed()) match = recursiveFindFirstRemoteItemReference(items[i], elementName, elementObject, subsystem);
		}
		return match;
	}
	
	/**
	 * Recursively tries to find all occurrences of a given remote object, starting at the tree root. 
	 * Since the object memory object for a remote object is not dependable we call getAbsoluteName() 
	 * on the adapter to do the comparisons.
	 * <p>
	 * TODO: This method should not return any invalid matches, i.e. remote objects
	 * that do match the String identifier but have been deleted already. Because the
	 * same remote object can appear in multiple contexts in the RSE Tree, a single
	 * remote object identifier String may evaluate to multiple different matches
	 * to fill into the matches argument. All those context object matches, however,
	 * reference the same real-world model objects due to the constraint that 
	 * {@link IRemoteObjectIdentifier} uniquely identifies a remote object. 
	 * <p>
	 * This overload takes a string and a subsystem.
	 * 
	 * @param searchString the absolute name of the remote object to which
	 *    we want to find a tree item which references it.
	 * @param elementObject the actual remote element to find, for binary matching
	 * @param subsystem optional subsystem to search within
	 * @param matches the List to populate with hits (TreeItem objects), 
	 *     or <code>null</code> to get a new List created and returned
	 * @return the List populated with hits, or <code>null</code> if 
	 *     <code>null</code> was passed as matches to populate and no matches
	 *     were found.
	 */
	protected List findAllRemoteItemReferences(String searchString, Object elementObject, ISubSystem subsystem, List matches) {
		Tree tree = getTree();
		Item[] roots = tree.getItems();
		if (roots == null) return matches;
		if (matches == null) 
			matches = new Vector();

		boolean foundExact = false;
		for (int idx = 0; idx < roots.length; idx++){
			if (recursiveFindExactMatches((TreeItem)roots[idx], elementObject, subsystem, matches)){
				foundExact = true;
			}
		}

		if (!foundExact)
		{
			for (int idx = 0; idx < roots.length; idx++){
				matches = recursiveFindAllRemoteItemReferences(roots[idx], searchString, elementObject, subsystem, matches);
			}
		}
		
		return matches;
	}
	
	private boolean recursiveFindExactMatches(TreeItem root, Object elementObject, ISubSystem subsystem, List matches)
	{
		boolean foundSomething = false;
		Object data = root.getData();
		if (data == elementObject)
		{
			matches.add(root);
			foundSomething = true;
		}
		if (subsystem != null){
			if (data instanceof ISubSystem){
				if (data != subsystem)
					return false;
			}
			else if (data instanceof IHost){
				if (subsystem.getHost() != data)
					return false;
			}
		}
		
		TreeItem[] children = root.getItems();
		for (int i = 0; i < children.length; i++)
		{
			if (recursiveFindExactMatches(children[i], elementObject, subsystem,  matches))
			{
				foundSomething = true;
			}
		}
		return foundSomething;
	}
	
	protected boolean mappedFindAllRemoteItemReferences(Object elementObject, List occurrences)
	{				
		Widget[] items = findItems(elementObject);
		if (items.length > 0)
		{
			for (int i = 0; i < items.length; i++)
			{
				occurrences.add(items[i]);
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Recursively tries to find all references to a remote object.
	 * @param parent the parent item at which to start the search.
	 * @param elementName the absolute name of the remote element to find
	 * @param elementObject the actual remote element to find, for binary matching
	 * @param subsystem optional subsystem to search within
	 * @param occurrences the List to populate with hits. Must not be <code>null</code>
	 * @return the given List populated with hits
	 */
	protected List recursiveFindAllRemoteItemReferences(Item parent, String elementName, Object elementObject, ISubSystem subsystem, List occurrences) {
		Object rawData = parent.getData();
		ISystemViewElementAdapter remoteAdapter = null;
		// ----------------------------
		// what are we looking at here?
		// ----------------------------
		if (rawData != null) remoteAdapter = getViewAdapter(rawData);
		// -----------------------------------------------------------------------
		// if this is a remote object, test if it is the one we are looking for...
		// -----------------------------------------------------------------------
		if (remoteAdapter != null) {
			// first test for binary match
			if (elementObject == rawData) {
				occurrences.add(parent); // found a match!
				if (debugRemote) System.out.println("Find All: Remote item binary match found"); //$NON-NLS-1$
				return occurrences; // no point in checking the kids
			}
			// now test for absolute name match
			String fqn = remoteAdapter.getAbsoluteName(rawData);
			if (debugRemote) System.out.println("TESTING FINDALL: '" + fqn + "' vs '" + elementName + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if ((fqn != null) && fqn.equals(elementName)) {
				occurrences.add(parent); // found a match!
				if (debugRemote) System.out.println("...and remote item name match found"); //$NON-NLS-1$
				return occurrences; // no point in checking the kids
			}
		}
		// -------------------------------------------------------------------------
		// if we have been given a subsystem to restrict to, that is a hint to us...
		// -------------------------------------------------------------------------
		else if ((rawData != null) && (subsystem != null)) // test for hints we are in the wrong place
		{
			// if we are currently visiting a subsystem, and that subsystem is not from the same
			//  factory, then we can assume the remote object occurrences we are looking for are
			//  not to be found within this branch...
			if ((rawData instanceof ISubSystem) && (((ISubSystem) rawData).getSubSystemConfiguration() != subsystem.getSubSystemConfiguration())) {
				return occurrences; // they don't match, so don't bother checking the kids
			}
			// if we are currently visiting a connection, and that connection's hostname is not the same
			//  as that of our given subsystem, then we can assume the remote object occurrences we are 
			//  looking for are not to be found within this branch...
			else if ((rawData instanceof IHost) && (!((IHost) rawData).getHostName().equals(subsystem.getHost().getHostName()))) {
				return occurrences; // they don't match, so don't bother checking the kids
			}
		}
		// recurse over children	    
		Item[] items = getChildren(parent);
		for (int i = 0; (i < items.length); i++) {
					
			if (!items[i].isDisposed()) occurrences = recursiveFindAllRemoteItemReferences(items[i], elementName, elementObject, subsystem, occurrences);
		}
		return occurrences;
	}
}
