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

package org.eclipse.rse.shells.ui.view;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.rse.core.subsystems.IRemoteLineReference;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemTableViewProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;





/**
 * This is the wrapper class to SystemBuildErrorView.
 */
public class SystemBuildErrorViewPart extends ViewPart implements ISelectionListener, SelectionListener, ISystemResourceChangeListener, ISystemMessageLine
{


   class BrowseAction extends Action
	{
		public BrowseAction(String label, ImageDescriptor des)
		{
			super(label, des);

			setToolTipText(label);
		}

		public void checkEnabledState()
		{
			if (getViewer().getInput() != null)
			{
				setEnabled(true);
			}
			else
			{
				setEnabled(false);
			}
		}

		public void run()
		{
		}
	}
   
   public class ClearAction extends BrowseAction
	{
		public ClearAction()
		{
			super(SystemResources.ACTION_CLEAR_LABEL,
			//	DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_CLEAR));
			RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CLEAR_ID));

			ImageDescriptor des = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CLEAR_ID);

			/*
			setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_CLEAR));
			setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_CLEAR));
			setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_CLEAR));
			*/
			setImageDescriptor(des);

			// TODO DKM - get help for this!
			//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CLEAR_CONSOLE_ACTION);
		}

		public void checkEnabledState()
		{
			Object input = getViewer().getInput();
			if (input != null)
			{
				if (input instanceof IRemoteCommandShell)
				{
					setEnabled(((IRemoteCommandShell) input).isActive());
					return;
				}
			}

			setEnabled(false);
		}

		public void run()
		{
			clear();
		}

		// clear contents of the current command viewer
		private void clear()
		{
			getViewer().clearAllItems();
		}
	}
   
   
   // constants			
   public static final String ID = "com.ibm.etools.systems.core.ui.view.buildErrorView";
   // matches id in plugin.xml, view tag	
	
	private SystemBuildErrorView _viewer;
	private ClearAction _clearAction;
	private boolean _enableUpdates = true;
	
	//  for ISystemMessageLine
	private String _message, _errorMessage;
	private SystemMessage sysErrorMessage;
	private IStatusLineManager _statusLine = null;

	public void setFocus()
	{
		_viewer.getControl().setFocus();
	}

	public SystemBuildErrorView getViewer()
	{
	    return _viewer;
	}
	
	public Shell getShell()
	{
		return _viewer.getShell();
	}
	
	public void fillLocalToolBar()
	{
	    boolean firstCall = false;
		if (_clearAction == null)
		{
		    firstCall = true;
			_clearAction = new ClearAction();
		}
		
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		addToolBarItems(toolBarManager);

		if (firstCall)
		{
			IMenuManager menuManager = actionBars.getMenuManager();
			_statusLine = actionBars.getStatusLineManager();
			//addMenuItems(menuManager);
		}
	}
	
	private void addToolBarItems(IToolBarManager toolBarManager)
	{
		toolBarManager.removeAll();

		toolBarManager.add(_clearAction);
	}

	public void createPartControl(Composite parent)
	{
		// create table portion
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		_viewer = new SystemBuildErrorView(table, this);
		table.setLinesVisible(true);
		
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.addSelectionListener(this);

		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();

		registry.addSystemResourceChangeListener(this);
		
		_viewer.addDoubleClickListener(new IDoubleClickListener()
			{
				public void doubleClick(DoubleClickEvent event)
				{
					handleDoubleClick(event);
				}
			});
		SystemWidgetHelpers.setHelp(_viewer.getControl(), RSEUIPlugin.HELPPREFIX + "uerr0000");
		fillLocalToolBar();
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
				if (!adapter.hasChildren(element))
				{		
					alreadyHandled = adapter.handleDoubleClick(element);
				}
			}
		}
		
	public void selectionChanged(IWorkbenchPart part, ISelection sel)
	{
	}

	public void dispose()
	{
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.removeSelectionListener(this);
		_viewer.dispose();

		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		registry.removeSystemResourceChangeListener(this);
		super.dispose();
	}

	

	

	public void selectionChanged(SelectionChangedEvent e)
	{
	}

	public void resetOffset()
	{
		   SystemBuildErrorViewProvider provider = (SystemBuildErrorViewProvider)_viewer.getContentProvider();
		   int size = ((IRemoteCommandShell)_viewer.getInput()).getSize();
           provider.setOffset(size - 1);	
   }
	
	public void setInput(IAdaptable object, String cmdString)
	{
		setInput(object, true, cmdString);
	}

	public void setInput(IAdaptable object, boolean updateHistory, String cmdString)
	{
		_viewer.getTable().removeAll();
		setEnableUpdates(true);
		
		if (object instanceof IRemoteCommandShell)
		{
		    _viewer.setInput(object);
		}
		else
		{
		    if (object instanceof IRemoteOutput)
		    {
		        IRemoteOutput output = (IRemoteOutput)object;
		        if (output != null)
		        {
		            _viewer.setInput(output.getParent());
		            SystemBuildErrorViewProvider provider = (SystemBuildErrorViewProvider)_viewer.getContentProvider();
		            provider.setOffset(output.getIndex() - 1);
		        }
		    }
		    else if (object instanceof IRemoteLineReference)
		    {
		       
		        _viewer.setInput(((IRemoteLineReference)object).getParent());
		    }
		
		}
		
		SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_ERROR_LIST_TITLE);
		msg.makeSubstitution(cmdString);
		setPartName(msg.getLevelOneText());
	}
	
	public void updateOutput()
	{
		if (_viewer != null)
		{
			((SystemTableViewProvider) _viewer.getContentProvider()).flushCache();
			_viewer.updateChildren();
		}
	}
	

	private void updateOutput(IRemoteCommandShell root)
	{
		if (root != null && _enableUpdates)
		{
			if (_viewer.getInput() != root)
			{
				_viewer.clearAllItems();
				_viewer.setInput(root);
			}
			updateOutput();
		}
	}
	
	/*
	 * Lock view from updates since the compile has ended
	 */
	public void setEnableUpdates(boolean enable)
	{
	    if (!enable)
	    {
	        // do one last refresh to make sure
	        // last compile got shown
	        updateOutput();
	    }
	    _enableUpdates= enable;
	}

	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{
		if (event.getType() == ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_FINISHED)
		{
			Object source = event.getSource();
			if (source instanceof IRemoteCmdSubSystem)
			{
				Shell shell = RSEUIPlugin.getTheSystemRegistry().getShell();
				//shell.getDisplay().asyncExec(new CommandSubSystemDisconnectedRunnable((RemoteCmdSubSystem) source));
			}
			else
				if (source instanceof IRemoteCommandShell)
				{
					// find out if we're listening to this					
					updateOutput((IRemoteCommandShell) source);
				}
		}
		else
			if (event.getType() == ISystemResourceChangeEvents.EVENT_REFRESH)
			{
			    
				Object parent = event.getParent();
				if (parent instanceof IRemoteCommandShell)
				{
					updateOutput((IRemoteCommandShell) parent);
				}
			}
	}

	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e)
	{

		
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