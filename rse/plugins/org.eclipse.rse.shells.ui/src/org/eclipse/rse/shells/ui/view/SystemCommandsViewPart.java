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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvent;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.ISystemResourceChangeListener;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.shells.ui.ShellResources;
import org.eclipse.rse.shells.ui.actions.SystemBaseShellAction;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemBaseDummyAction;
import org.eclipse.rse.ui.actions.SystemTablePrintAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.IRSEViewPart;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.ViewPart;



/**
 * This is the desktop view wrapper of the System View viewer.
 */
public class SystemCommandsViewPart
	extends ViewPart
	implements
		ISelectionListener,
		SelectionListener,
		ISelectionChangedListener,
		ISystemResourceChangeListener,
		IRSEViewPart,
		IMenuListener,
		ISystemMessageLine
{


	class RestoreStateRunnable implements Runnable
	{
	    public void run()
	    {
	        initDefaultCommandShells();
	    }
	}

	class BrowseAction extends Action
	{
		public BrowseAction(String label, ImageDescriptor des)
		{
			super(label, des);

			setToolTipText(label);
		}

		public void checkEnabledState()
		{
			if (_folder != null && _folder.getInput() != null)
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


	

	public class ShellAction extends BrowseAction
	{
		private IRemoteCmdSubSystem _cmdSubSystem;

		public ShellAction(String title, ImageDescriptor image, IRemoteCmdSubSystem cmdSubSystem)
		{
			super(title, image);

			setToolTipText(ShellResources.ACTION_RUN_SHELL_TOOLTIP);
			_cmdSubSystem = cmdSubSystem;
		}

		public void checkEnabledState()
		{
			setEnabled(_cmdSubSystem.canRunShell());
		}

		public void run()
		{
			try
			{
				IRemoteCommandShell cmd = _cmdSubSystem.runShell(getShell(), null);
				if (cmd != null)
				{
					showInView(cmd);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		private void showInView(IRemoteCommandShell cmd)
		{
			SystemCommandsUI commandsUI = SystemCommandsUI.getInstance();
			SystemCommandsViewPart cmdsPart = commandsUI.activateCommandsView();
			cmdsPart.updateOutput(cmd);
		}
	}

	public class ClearAction extends BrowseAction
	{
		public ClearAction()
		{

			super(SystemResources.ACTION_CLEAR_LABEL,
			RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CLEAR_ID));

			ImageDescriptor des = RSEUIPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_CLEAR_ID);

			setImageDescriptor(des);

			setEnabled(false);
			// TODO DKM - get help for this!
			//PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CLEAR_CONSOLE_ACTION);
		}

		public void checkEnabledState()
		{
			Object input = _folder.getInput();
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
			if (_folder.getInput() != null)
			{
				clear();
			}
		}

		// clear contents of the current command viewer
		private void clear()
		{
			_folder.getViewer().clearAllItems();
		}
	}

	
	public class CommandSubSystemDisconnectedRunnable implements Runnable
	{
		private IRemoteCmdSubSystem _subsystem;
		public CommandSubSystemDisconnectedRunnable(IRemoteCmdSubSystem subsystem)
		{
			_subsystem = subsystem;
		}

		public void run()
		{
			IRemoteCommandShell[] cmds = _subsystem.getShells();
			if (cmds != null)
			{
			for (int i = 0; i < cmds.length; i++)
			{
				_folder.remove(cmds[i]);
			}
			}
		}
	}

	public class CommandMenuManager extends MenuManager
	{
		public CommandMenuManager()
		{
			super(ShellResources.ACTION_LAUNCH_LABEL);
		}
	}
	
	public class CommandSubmenuManager extends MenuManager
	{
	    private IRemoteCmdSubSystem[] _subsystems;
		public CommandSubmenuManager(IHost connection, IRemoteCmdSubSystem[] subsystems)
		{
			super(connection.getAliasName());
			_subsystems = subsystems;
		}
		
		public IRemoteCmdSubSystem[] getSubSystems()
		{
		    return _subsystems;
		}
	}

	private ClearAction _clearAction = null;



	private List _shellActions = null;
	
	private SystemTablePrintAction _printTableAction = null;
	private CommandsViewWorkbook _folder = null;
	private IRemoteCommandShell _lastSelected = null;
	private CellEditorActionHandler _editorActionHandler = null;

	//  for ISystemMessageLine
	private String _message, _errorMessage;
	private SystemMessage sysErrorMessage;
	private IStatusLineManager _statusLine = null;

	// constants			
	public static final String ID = "org.eclipse.rse.shells.ui.view.commandsView";
	// matches id in plugin.xml, view tag	

	public void setFocus()
	{
		_folder.showCurrentPage();
	}

	public Shell getShell()
	{
		return _folder.getShell();
	}

	public Viewer getRSEViewer()
	{
		return _folder.getViewer();
	}
	
	public CellEditorActionHandler getEditorActionHandler()
	{
	    if (_editorActionHandler == null)
	    {
	        _editorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());
	    }
	    return _editorActionHandler;
	}
	
	public void createPartControl(Composite parent)
	{
		_folder = new CommandsViewWorkbook(parent, this);
		_folder.getFolder().addSelectionListener(this);

		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.addSelectionListener(this);
		

		SystemWidgetHelpers.setHelp(_folder, RSEUIPlugin.HELPPREFIX + "ucmd0000");

		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();


	
		registry.addSystemResourceChangeListener(this);

		RestoreStateRunnable restore = new RestoreStateRunnable();
		Display.getCurrent().asyncExec(restore);
		
		fillLocalToolBar();
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel)
	{
	}

	public void dispose()
	{
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		selectionService.removeSelectionListener(this);
		_folder.dispose();

		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		registry.removeSystemResourceChangeListener(this);
		super.dispose();
	}

	public void updateActionStates()
	{
		if (_shellActions == null)
			fillLocalToolBar();

		if (_folder != null && _folder.getInput() != null)
		{
		    IRemoteCommandShell currentSelected = (IRemoteCommandShell)_folder.getInput();
		    if (currentSelected != null)
		    {
		        if (currentSelected != _lastSelected)
		        {
		            // reset the actions
		    		IActionBars actionBars = getViewSite().getActionBars();
					IToolBarManager toolBarManager = actionBars.getToolBarManager();
					updateShellActions();
					addToolBarItems(toolBarManager);		            
		        }
		        _lastSelected = currentSelected;
		        
				_folder.updateActionStates();
				CommandsViewPage page = _folder.getCurrentTabItem();
				if (page != null)
				{
					_printTableAction.setTableView(page.getTitle(), page.getViewer());
				}
				else
				{
					_printTableAction.setTableView("", null);
				}
				
				_clearAction.checkEnabledState();
	
				StructuredSelection cmdSelection = new StructuredSelection(_folder.getInput());
				for (int i =0; i < _shellActions.size(); i++)
				{
				    Object action = _shellActions.get(i);
				    if (action instanceof SystemBaseShellAction)
				    {
				        SystemBaseShellAction shellAction = (SystemBaseShellAction)action;
				    	shellAction.setEnabled(shellAction.updateSelection(cmdSelection));
				    }
				}
				
				_printTableAction.checkEnabledState();
		    }
		}


	}

	protected void updateShellActions()
	{
	    if (_folder != null && _folder.getInput() != null)
	    {
	        IRemoteCommandShell cmdShell = (IRemoteCommandShell)_folder.getInput();
	        SystemViewRemoteOutputAdapter adapter = (SystemViewRemoteOutputAdapter)((IAdaptable)cmdShell).getAdapter(ISystemViewElementAdapter.class);
	        
	        _shellActions = adapter.getShellActions(cmdShell.getCommandSubSystem().getParentRemoteCmdSubSystemFactory());
	    }
	    else if (_shellActions != null)
	    {
	        _shellActions.clear();
	    }
	    else
	    {
	    	_shellActions = new ArrayList();
	    }
	}

	public void fillLocalToolBar()
	{
		boolean firstCall = false;
		if (_folder != null )
		{
			firstCall = (_shellActions == null);

			if (firstCall) {															
				updateShellActions();
			}
		
			updateActionStates();
	
			IActionBars actionBars = getViewSite().getActionBars();
	
			if (firstCall)
			{				
				_clearAction = new ClearAction();
				_printTableAction = new SystemTablePrintAction(getTitle(), null);
				IMenuManager menuManager = actionBars.getMenuManager();
				addMenuItems(menuManager);
				_statusLine = actionBars.getStatusLineManager();
			}
			IToolBarManager toolBarManager = actionBars.getToolBarManager();
			addToolBarItems(toolBarManager);
		}
	}

	private void addToolBarItems(IToolBarManager toolBarManager)
	{
		toolBarManager.removeAll();
		if (_clearAction != null)
		{
			toolBarManager.add(_clearAction);
		}
		if (_shellActions != null)
		{
			for (int i =0; i < _shellActions.size(); i++)
			{
			    Object shellAction = _shellActions.get(i);
			    if (shellAction instanceof IContributionItem)
			    {
			        toolBarManager.add((IContributionItem)shellAction);
			    }
			    else if (shellAction instanceof IAction)
			    {
			        toolBarManager.add((IAction)shellAction);
			    }
			}
		}
		toolBarManager.update(true);
		
	}

	private void addMenuItems(IMenuManager menuManager)
	{
		IMenuManager launchMenu = new CommandMenuManager();

		launchMenu.add(new SystemBaseDummyAction());
		launchMenu.addMenuListener(this);
		launchMenu.setRemoveAllWhenShown(true);
		menuManager.add(launchMenu);
		menuManager.addMenuListener(this);

		menuManager.add(new Separator());
		menuManager.add(_printTableAction);
	}

	public void menuAboutToShow(IMenuManager menuManager)
	{
	    if (menuManager instanceof CommandSubmenuManager)
	    {
	        IRemoteCmdSubSystem[] cmdSubSystems = ((CommandSubmenuManager)menuManager).getSubSystems();
		    for (int c = 0; c < cmdSubSystems.length; c++)
		    {
		        IRemoteCmdSubSystem cmdSubSystem = cmdSubSystems[c];
		        
				if (cmdSubSystem != null && cmdSubSystem.canRunShell())
				{
					String name = cmdSubSystem.getName();
					ISystemViewElementAdapter ssva =
							(ISystemViewElementAdapter) ((IAdaptable) cmdSubSystem).getAdapter(
								ISystemViewElementAdapter.class);
					ImageDescriptor icon = ssva.getImageDescriptor(cmdSubSystem);																
					ShellAction action =
						new ShellAction(
							name,
							icon,
							cmdSubSystem);
					menuManager.add(action);
				}
		    } 
	    }
	    else
	    {
			boolean hasItem = false;
	
			ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			IHost[] connections = registry.getHosts();
	
			for (int i = 0; i < connections.length; i++)
			{
				IHost connection = connections[i];
				if (registry.isAnySubSystemConnected(connection) || connection.getSystemType().equals("Local"))
				{
					IRemoteCmdSubSystem[] cmdSubSystems = RemoteCommandHelpers.getCmdSubSystems(connection);
					if (cmdSubSystems.length == 1)
					{
					    IRemoteCmdSubSystem cmdSubSystem = cmdSubSystems[0];
						if (cmdSubSystem != null && cmdSubSystem.canRunShell())
						{
							hasItem = true;
	
							if (menuManager instanceof CommandMenuManager)
							{
								ISystemViewElementAdapter va =
									(ISystemViewElementAdapter) ((IAdaptable) connection).getAdapter(
										ISystemViewElementAdapter.class);
								
								String name = connection.getAliasName();
								ImageDescriptor icon = va.getImageDescriptor(connection);
								
								
								ShellAction action =
									new ShellAction(
										name,
										icon,
										cmdSubSystem);
								menuManager.add(action);
							}
						}
					}
					else if (cmdSubSystems.length > 1)
					{				  
						if (menuManager instanceof CommandMenuManager)
						{
							ISystemViewElementAdapter va =
								(ISystemViewElementAdapter) ((IAdaptable) connection).getAdapter(
									ISystemViewElementAdapter.class);
							
							IMenuManager sublaunchMenu = new CommandSubmenuManager(connection, cmdSubSystems);
							sublaunchMenu.add(new SystemBaseDummyAction());
							sublaunchMenu.addMenuListener(this);
							sublaunchMenu.setRemoveAllWhenShown(true);					
							
							menuManager.add(sublaunchMenu);
							menuManager.addMenuListener(this);
						}
						
						
					}
				}
			}
	
			if (!(menuManager instanceof CommandMenuManager))
			{
				if (!hasItem)
				{
					if (menuManager.getItems().length > 0)
					{
						MenuManager lmgr = (MenuManager) menuManager.getItems()[0];
						if (lmgr.getMenu() != null)
						{
							lmgr.getMenu().getParentItem().setEnabled(false);
						}
					}
				}
				else
				{
					if (menuManager.getItems().length > 0)
					{
						MenuManager lmgr = (MenuManager) menuManager.getItems()[0];
						if (lmgr.getMenu() != null)
						{
							lmgr.getMenu().getParentItem().setEnabled(true);
						}
					}
				}
			}
	    }
	}

	public void selectionChanged(SelectionChangedEvent e)
	{
	}

	/**
	 * Update or create a view tab for the specified command
	 * If there is no tab for the command, create one.
	 * 
	 * @param root command to view
	 * 
	 */
	public void updateOutput(IRemoteCommandShell root)
	{
		updateOutput(root, true);
	}

	private void updateOutput(IRemoteCommandShell root, boolean createTab)
	{
		if (root != null)
		{
			_folder.updateOutput(root, createTab);
			if (createTab)
			    updateActionStates();
		}
	}

	public void setInput(IAdaptable object)
	{
		setInput(object, true);
	}

	public void setInput(IAdaptable object, boolean updateHistory)
	{
		_folder.setInput(object);
	}


	protected void initDefaultCommandShells()
	{
		ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		IHost[] connections = registry.getHosts();
		CommandsViewPage curpage = _folder.getCurrentTabItem();
		 
		for (int i = 0; i < connections.length; i++)
		{
			IHost connection = connections[i];
			if (registry.isAnySubSystemConnected(connection) || connection.getSystemType().equals("Local"))
			{
				IRemoteCmdSubSystem[] cmdSubSystems = RemoteCommandHelpers.getCmdSubSystems(connection);
				if (cmdSubSystems.length > 0)
				{
				    for (int c= 0; c < cmdSubSystems.length; c++)
				    {
						IRemoteCmdSubSystem cmdSubSystem = cmdSubSystems[c];
	
						if (cmdSubSystem != null && cmdSubSystem.canRunShell())
						{
							restoreCommandShells(cmdSubSystem);
						}
				    }
				}
			}
		}
		   
		 if (curpage != null)
		    {
		     updateOutput((IRemoteCommandShell)curpage.getInput());
		     //curpage.setFocus();  
		    }

	}

	protected void restoreCommandShells(IRemoteCmdSubSystem cmdSS)
	{
	   
		try
		{
			IRemoteCommandShell[] cmds = cmdSS.getShells();
			if (cmds == null || cmds.length == 0)
			{
				cmds = cmdSS.restoreShellState(getShell());
				for (int i = 0; i < cmds.length; i++)
				{
					updateOutput(cmds[i]);
				}
			}
		}
		catch (Exception e)
		{
		}
			
		
	}
	
	
	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{ 
		if (event.getType() == ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_FINISHED)
		{
			Object source = event.getSource();
			if (source instanceof IRemoteCmdSubSystem)
			{
				Shell shell = RSEUIPlugin.getTheSystemRegistry().getShell();
				shell.getDisplay().asyncExec(new CommandSubSystemDisconnectedRunnable((IRemoteCmdSubSystem) source));
			}
			else if (source instanceof IRemoteCommandShell)
			{
				// find out if we're listening to this

				updateOutput((IRemoteCommandShell) source, false);
				updateActionStates();
			}
		} 
		
		if (event.getType() == ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_REMOVED)
		{
			Object source = event.getSource();
			if (source instanceof IRemoteCommandShell)
			{
			    updateOutput((IRemoteCommandShell) source, false);
			    _folder.remove((IRemoteCommandShell)source);
				updateActionStates();
			}
		}
		else if (event.getType() == ISystemResourceChangeEvents.EVENT_REFRESH)
		{
			Object parent = event.getParent();
			if (parent instanceof IRemoteCommandShell)
			{
				updateOutput((IRemoteCommandShell) parent, false);
			}
		}
		else if (event.getType() == ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE)
		{
			IRemoteCmdSubSystem cmdSS = null;
			Object parent = event.getSource();

			if (parent instanceof IRemoteFileSubSystem)
			{
				IRemoteFileSubSystem ss = (IRemoteFileSubSystem)parent;
				cmdSS = RemoteCommandHelpers.getCmdSubSystem(ss.getHost());				
			}
			else if (parent instanceof IRemoteCmdSubSystem)
			{
				cmdSS = (IRemoteCmdSubSystem)parent;
			}
			else if (parent instanceof ISubSystem)
			{
			    // DKM - for now assuming only 1 cmd subsystem
			    //SystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			    //registry.getCmdSubSystems()
			    cmdSS = RemoteCommandHelpers.getCmdSubSystem(((ISubSystem)parent).getHost());
			}
			if (cmdSS != null)
			{
				if (cmdSS.isConnected())
					restoreCommandShells(cmdSS);		
			}
		}
		else if (event.getType() == ISystemResourceChangeEvents.EVENT_MUST_COLLAPSE)
		{
		    Object source = event.getSource();
			if (source instanceof IRemoteCmdSubSystem)
			{
				Shell shell = RSEUIPlugin.getTheSystemRegistry().getShell();
				shell.getDisplay().asyncExec(new CommandSubSystemDisconnectedRunnable((IRemoteCmdSubSystem) source));
			}		    
		}
	}

	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e)
	{
		Widget source = e.widget;

		if (source == _folder.getFolder())
		{
			updateActionStates();
		}
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