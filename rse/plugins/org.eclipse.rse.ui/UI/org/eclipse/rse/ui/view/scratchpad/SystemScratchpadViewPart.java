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

package org.eclipse.rse.ui.view.scratchpad;

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
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
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
public class SystemScratchpadViewPart extends ViewPart implements ISelectionListener, ISelectionChangedListener, 
 ISystemResourceChangeListener, ISystemMessageLine, IRSEViewPart
{

	
	private SystemScratchpadView _viewer;

	// common actions
	private SystemCopyToClipboardAction _copyAction;
	private SystemPasteFromClipboardAction _pasteAction;
	private SystemCommonDeleteAction _deleteAction;
	private ClearAction _clearAction;
	private ClearSelectedAction _clearSelectionAction;

	//  for ISystemMessageLine
	private String _message, _errorMessage;
	private SystemMessage sysErrorMessage;
	private IStatusLineManager _statusLine = null;

	// constants			
	public static final String ID = "org.eclipse.rse.ui.view.scratchpad.SystemScratchpadViewPart"; // matches id in plugin.xml, view tag	

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
		_viewer.setWorkbenchPart(this);
	
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
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();
		Clipboard clipboard = registry.getSystemClipboard();

		CellEditorActionHandler editorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());

		_copyAction = new SystemCopyToClipboardAction(_viewer.getShell(), clipboard);
		_pasteAction = new SystemPasteFromClipboardAction(_viewer.getShell(), clipboard);
		_deleteAction = new SystemCommonDeleteAction(_viewer.getShell(), _viewer);
		
		editorActionHandler.setCopyAction(_copyAction);
		editorActionHandler.setPasteAction(_pasteAction);
		editorActionHandler.setDeleteAction(_deleteAction);
		//editorActionHandler.setSelectAllAction(new SelectAllAction());

		registry.addSystemResourceChangeListener(this);

		SystemWidgetHelpers.setHelp(_viewer.getControl(), SystemPlugin.HELPPREFIX + "scrp0000");

		setInput(registry.getSystemScratchPad());
		
		getSite().registerContextMenu(_viewer.getContextMenuManager(), _viewer);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel)
	{
	}

	public void dispose()
	{
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.removeSelectionListener(this);
		_viewer.removeSelectionChangedListener(this);

		SystemPlugin.getTheSystemRegistry().removeSystemResourceChangeListener(this);
		
		if (_viewer != null)
		{
			_viewer.dispose();
		}

		super.dispose();
	}

	private void handleDoubleClick(DoubleClickEvent event)
	{
		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object element = s.getFirstElement();
		
		if (element == null)
			return;

		ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) element).getAdapter(ISystemViewElementAdapter.class);
		boolean alreadyHandled = false;
		
		if (adapter != null)
		{
			if (adapter.hasChildren(element))
			{
				setInput((IAdaptable) element);
			}
			else
			{
				alreadyHandled = adapter.handleDoubleClick(element);
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
		//if (_refreshAction == null)
	    if (_clearAction == null)
		{
			// refresh action
			//_refreshAction = new RefreshAction();
	        _clearAction = new ClearAction(_viewer);
	        _clearSelectionAction = new ClearSelectedAction(_viewer);

		}

		updateActionStates();

		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		IMenuManager menuMgr = actionBars.getMenuManager();

		SystemRefreshAction refreshAction = new SystemRefreshAction(getShell());
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		_statusLine = actionBars.getStatusLineManager();

		addToolBarItems(toolBarManager);
		addToolBarMenuItems(menuMgr);
	}

	private void addToolBarMenuItems(IMenuManager menuManager)
	{
		menuManager.removeAll();
		menuManager.add(_clearSelectionAction);
		menuManager.add(new Separator());
		menuManager.add(_clearAction);
	}

	private void addToolBarItems(IToolBarManager toolBarManager)
	{
		toolBarManager.removeAll();
		toolBarManager.add(_clearSelectionAction);
		toolBarManager.add(new Separator());
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
		Object child = event.getSource();
		Object parent = event.getParent();
		Object input = _viewer.getInput();
		
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