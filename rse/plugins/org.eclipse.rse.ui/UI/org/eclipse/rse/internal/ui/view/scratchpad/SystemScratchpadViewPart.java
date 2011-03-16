/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
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
 * Kevin Doyle (IBM) - [182403] Double Click on an object that can be expanded
 * Kevin Doyle (IBM) - [195543] Double Clicking expands wrong folder when duplicate elements shown
 * Kevin Doyle (IBM) - [193155] Double Clicking on a String in Scratchpad Errors
 * Kevin Doyle (IBM) - [194867] Remote Scratchpad should have Refresh Action on toolbar
 * Kevin Doyle 		(IBM)		 - [242431] Register a new unique context menu id, so contributions can be made to all our views
 * David McKnight   (IBM)        - [330398] RSE leaks SWT resources
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view.scratchpad;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.internal.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.internal.ui.actions.SystemCommonRenameAction;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.internal.model.SystemRegistryUI;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.model.ISystemShellProvider;
import org.eclipse.rse.ui.view.IRSEViewPart;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.ViewPart;


/**
 * This class is the Remote Scratchpad view.
 */
public class SystemScratchpadViewPart extends ViewPart
	implements ISelectionListener, ISelectionChangedListener, 
	ISystemResourceChangeListener, ISystemShellProvider,
	ISystemMessageLine, IRSEViewPart
{

	
	private SystemScratchpadView _viewer;

	// common actions
	private SystemCopyToClipboardAction _copyAction;
	private SystemPasteFromClipboardAction _pasteAction;
	private SystemCommonDeleteAction _deleteAction;
	private SystemCommonRenameAction _renameAction;
	private ClearAction _clearAction;
	private ClearSelectedAction _clearSelectionAction;
	private SystemRefreshAction _refreshAction;

	//  for ISystemMessageLine
	private String _message, _errorMessage;
	private SystemMessage sysErrorMessage;
	private IStatusLineManager _statusLine = null;

	// constants			
	public static final String ID = "org.eclipse.rse.ui.view.scratchpad.SystemScratchpadViewPart"; // matches id in plugin.xml, view tag	 //$NON-NLS-1$

	public void setFocus()
	{
		_viewer.getControl().setFocus();
	}

	public SystemScratchpadView getViewer()
	{ 
		return _viewer;
	}
	
	public Viewer getRSEViewer()
	{
		return _viewer;
	}

	public void createPartControl(Composite parent)
	{
		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
        
		_viewer = new SystemScratchpadView(tree, this);
	
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.addSelectionListener(this);
		_viewer.addSelectionChangedListener(this);
		getSite().setSelectionProvider(_viewer);

		_viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				handleDoubleClick(event);
			}
		});

		fillLocalToolBar();

		// register global edit actions 		
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();

		CellEditorActionHandler editorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());

		_copyAction = new SystemCopyToClipboardAction(_viewer.getShell(), null);
		_pasteAction = new SystemPasteFromClipboardAction(_viewer.getShell(), null);
		_deleteAction = new SystemCommonDeleteAction(_viewer.getShell(), _viewer);
		_renameAction = new SystemCommonRenameAction(_viewer.getShell(), _viewer);
		
		// register rename action as a global handler
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.RENAME.getId(), _renameAction);
		
		editorActionHandler.setCopyAction(_copyAction);
		editorActionHandler.setPasteAction(_pasteAction);
		editorActionHandler.setDeleteAction(_deleteAction);
		//editorActionHandler.setSelectAllAction(new SelectAllAction());

		registry.addSystemResourceChangeListener(this);

		SystemWidgetHelpers.setHelp(_viewer.getControl(), RSEUIPlugin.HELPPREFIX + "scrp0000"); //$NON-NLS-1$

		setInput(SystemRegistryUI.getInstance().getSystemScratchPad());
		
		getSite().registerContextMenu(_viewer.getContextMenuManager(), _viewer);
		getSite().registerContextMenu(ISystemContextMenuConstants.RSE_CONTEXT_MENU, _viewer.getContextMenuManager(), _viewer);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel)
	{
	}

	public void dispose()
	{
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.removeSelectionListener(this);
		_viewer.removeSelectionChangedListener(this);

		RSECorePlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);
		
		if (_viewer != null)
		{
			_viewer.dispose();
		}

		super.dispose();
	}

	private void handleDoubleClick(DoubleClickEvent event)
	{
		ITreeSelection s = (ITreeSelection) event.getSelection();
		Object element = s.getFirstElement();
		
		if (element == null || !(element instanceof IAdaptable))
			return;

		ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) element).getAdapter(ISystemViewElementAdapter.class);
		
		if (adapter != null)
		{
			if (adapter.hasChildren((IAdaptable)element))
			{
				// Get the path for the element and use it for setting expanded state,
				// so the proper TreeItem is expanded/collapsed
				TreePath[] paths = s.getPathsFor(element);
				if (paths == null || paths.length == 0 || paths[0] == null) return;
				TreePath elementPath = paths[0];
				if (_viewer.getExpandedState(elementPath))
				{
					_viewer.collapseToLevel(elementPath, 1);
				}
				else
				{
					_viewer.expandToLevel(elementPath, 1);
				}
			}
			else
			{
				adapter.handleDoubleClick(element);
			}
		}
	}

	public void updateActionStates()
	{
	    if (_clearAction == null)
	        fillLocalToolBar();
	    
	    _clearAction.checkEnabledState();
	    _clearSelectionAction.checkEnabledState();
	}

	public void fillLocalToolBar()
	{
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		IMenuManager menuMgr = actionBars.getMenuManager();
		
	    if (_clearAction == null) {
	        _clearAction = new ClearAction(_viewer);
	        _clearSelectionAction = new ClearSelectedAction(_viewer);
		}

	    if (_refreshAction == null) {
	    	_refreshAction = new SystemRefreshAction(getShell());
			_refreshAction.setId(ActionFactory.REFRESH.getId());
			_refreshAction.setActionDefinitionId("org.eclipse.ui.file.refresh"); //$NON-NLS-1$
			_refreshAction.setSelectionProvider(_viewer);
	    }
	    actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), _refreshAction);
	    
		updateActionStates();

		_statusLine = actionBars.getStatusLineManager();

		addToolBarItems(toolBarManager);
		addToolBarMenuItems(menuMgr);
	}

	private void addToolBarMenuItems(IMenuManager menuManager)
	{
		menuManager.removeAll();
		menuManager.add(_refreshAction);
		menuManager.add(new Separator());
		menuManager.add(_clearSelectionAction);
		menuManager.add(_clearAction);
	}

	private void addToolBarItems(IToolBarManager toolBarManager)
	{
		toolBarManager.removeAll();
		toolBarManager.add(_refreshAction);
		toolBarManager.add(new Separator());
		toolBarManager.add(_clearSelectionAction);
		toolBarManager.add(_clearAction);
	}

	public void selectionChanged(SelectionChangedEvent e)
	{
		// listener for this view
		updateActionStates();

		IStructuredSelection sel = (IStructuredSelection) e.getSelection();
		_copyAction.setEnabled(_copyAction.updateSelection(sel));
		_pasteAction.setEnabled(_pasteAction.updateSelection(sel));
		_deleteAction.setEnabled(_deleteAction.updateSelection(sel));
	}

	public void setInput(IAdaptable object)
	{
		setInput(object, null);

	}

	public void setInput(IAdaptable object, String[] filters)
	{
		if (_viewer != null && object != null)
		{
			_viewer.setInput(object);
	
			updateActionStates();

		}
	}

	/**
	   * Used to asynchronously update the view whenever properties change.
	   */
	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{
		Object parent = event.getParent();
		
		if (event.getType() == ISystemResourceChangeEvents.EVENT_RENAME)
		{
		}
		
		if (parent == _viewer.getInput())
		{
		    updateActionStates();
		}
	}

	public Shell getShell()
	{
		return _viewer.getShell();
	}
	

//	 -------------------------------
	// ISystemMessageLine interface...
	// -------------------------------
	/**
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage()
	{
		_errorMessage = null;
		sysErrorMessage = null;
		if (_statusLine != null)
			_statusLine.setErrorMessage(_errorMessage);
	}
	/**
	 * Clears the currently displayed message.
	 */
	public void clearMessage()
	{
		_message = null;
		if (_statusLine != null)
			_statusLine.setMessage(_message);
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public String getErrorMessage()
	{
		return _errorMessage;
	}
	/**
	 * Get the currently displayed message.
	 * @return The message. If no message is displayed <code>null<code> is returned.
	 */
	public String getMessage()
	{
		return _message;
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message)
	{
		this._errorMessage = message;
		if (_statusLine != null)
			_statusLine.setErrorMessage(message);
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
		this._message = message;
		if (_statusLine != null)
			_statusLine.setMessage(message);
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