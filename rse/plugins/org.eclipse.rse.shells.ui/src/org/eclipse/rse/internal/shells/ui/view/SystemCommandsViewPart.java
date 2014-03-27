/********************************************************************************
 * Copyright (c) 2002, 2014 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [175262] IHost.getSystemType() should return IRSESystemType 
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [174945] Remove obsolete icons from rse.shells.ui
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty() 
 * David McKnight (IBM) - [165680] "Show in Remote Shell View" does not work
 * Kevin Doyle (IBM) - [198534] Shell Menu Enablement Issue's
 * Radoslav Gerganov (ProSyst) - [181563] Fix hardcoded Ctrl+Space for remote shell content assist
 * David McKnight   (IBM)        - [294398] [shells] SystemCommandsViewPart always assumes systemResourceChanged() called on Display thread
 * David McKnight   (IBM)        - [351750] [shells] need to check for disposed widget when handling events
 * David McKnight (IBM)  -[431378] [shells] Remote shells not always restored properly on reconnect
 ********************************************************************************/

package org.eclipse.rse.internal.shells.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.shells.ui.ShellResources;
import org.eclipse.rse.internal.shells.ui.actions.SystemBaseShellAction;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.shells.ui.view.SystemCommandEditor;
import org.eclipse.rse.shells.ui.view.SystemViewRemoteOutputAdapter;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemBaseDummyAction;
import org.eclipse.rse.ui.actions.SystemTablePrintAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.model.ISystemShellProvider;
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
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

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
		ISystemShellProvider,
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

	/**
	 * Content assist action for all remote shells. It is activated with
	 * the default key binding for content assistance used in the workbench.
	 */
	class ContentAssistAction extends Action {
	  
	  public void run() {
	    if (_folder != null) {
	      CommandsViewPage currentTabItem = _folder.getCurrentTabItem();
	      if (currentTabItem != null) {
	        SystemCommandEditor editor = currentTabItem.getEditor();
	        editor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
	      }
	    }
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
				IRemoteCommandShell cmd = _cmdSubSystem.runShell(null, new NullProgressMonitor());
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
	public static final String ID = "org.eclipse.rse.shells.ui.view.commandsView"; //$NON-NLS-1$
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
		
		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		ContentAssistAction caAction = new ContentAssistAction();
		caAction.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		// the handler is automatically deactivated in the dispose() method of this view
		handlerService.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new ActionHandler(caAction));

		SystemWidgetHelpers.setHelp(_folder, RSEUIPlugin.HELPPREFIX + "ucmd0000"); //$NON-NLS-1$

		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();

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

		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		registry.removeSystemResourceChangeListener(this);
		super.dispose();
	}

	public void updateActionStates()
	{
		if (_shellActions == null || (_shellActions.size() == 0 && _folder != null && _folder.getInput() != null))
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
					_printTableAction.setTableView("", null); //$NON-NLS-1$
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
		} else if (_folder != null && _folder.getInput() == null) { 
			// The shell contains no input, update all action states			
			_folder.updateActionStates();
			_printTableAction.setTableView("", null); //$NON-NLS-1$
			_printTableAction.checkEnabledState();
			_clearAction.checkEnabledState();
		
			// Since no shell is open go through all the shell actions and disable them
			for (int i =0; i < _shellActions.size(); i++) {
			    Object action = _shellActions.get(i);
			    if (action instanceof SystemBaseShellAction) {
			        SystemBaseShellAction shellAction = (SystemBaseShellAction)action;
			    	shellAction.setEnabled(false);
			    }
			}
		}
		


	}

	protected void updateShellActions()
	{
	    if (_folder != null && _folder.getInput() != null)
	    {
	        IRemoteCommandShell cmdShell = (IRemoteCommandShell)_folder.getInput();
	        SystemViewRemoteOutputAdapter adapter = (SystemViewRemoteOutputAdapter)((IAdaptable)cmdShell).getAdapter(ISystemViewElementAdapter.class);
	        
	        _shellActions = adapter.getShellActions(cmdShell.getCommandSubSystem().getParentRemoteCmdSubSystemConfiguration());
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
		if (_folder != null )
		{
			IActionBars actionBars = getViewSite().getActionBars();
	
			if (_shellActions == null || _shellActions.size() == 0)
			{				
				updateShellActions();
				_clearAction = new ClearAction();
				_printTableAction = new SystemTablePrintAction(getTitle(), null);
				IMenuManager menuManager = actionBars.getMenuManager();
				addMenuItems(menuManager);
				_statusLine = actionBars.getStatusLineManager();
			}
			IToolBarManager toolBarManager = actionBars.getToolBarManager();
			addToolBarItems(toolBarManager);
			
			updateActionStates();
		}
	}

	private void addToolBarItems(IToolBarManager toolBarManager)
	{
		toolBarManager.removeAll();
		if (_clearAction != null)
		{
			toolBarManager.add(_clearAction);
		}
		if (_shellActions != null && _shellActions.size() > 0)
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
		menuManager.removeAll();
		
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
	
			ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
			IHost[] connections = registry.getHosts();
	
			for (int i = 0; i < connections.length; i++)
			{
				IHost connection = connections[i];
				if (registry.isAnySubSystemConnected(connection) || connection.getSystemType().isLocal())
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
							/*
							ISystemViewElementAdapter va =
								(ISystemViewElementAdapter) ((IAdaptable) connection).getAdapter(
									ISystemViewElementAdapter.class);
							*/
							
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
	
	/**
	 * For defect 165680, needed to change the active tab
	 * @param root the shell to show
	 */
	public void showPageFor(IRemoteCommandShell root)
	{
		if (root != null && _folder != null)
		{
			_folder.showPageFor(root);
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
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		IHost[] connections = registry.getHosts();
		CommandsViewPage curpage = _folder.getCurrentTabItem();
		 
		for (int i = 0; i < connections.length; i++)
		{
			IHost connection = connections[i];
			if (registry.isAnySubSystemConnected(connection) || connection.getSystemType().isLocal())
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
		try {
			IRemoteCommandShell[] cmds = cmdSS.getShells();
			if (cmds == null || cmds.length == 0){
				cmds = cmdSS.restoreShellState(getShell());
				if (cmds!=null)
				{
					for (int i = 0; i < cmds.length; i++)
					{
						updateOutput(cmds[i]);
					}
				}
			}
		}
		catch (Exception e){
		}
	}
	
	
	
	public void systemResourceChanged(ISystemResourceChangeEvent event)
	{ 
		if (event.getType() == ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_FINISHED)
		{
			Object source = event.getSource();
			if (source instanceof IRemoteCmdSubSystem)
			{
				Shell shell = SystemBasePlugin.getActiveWorkbenchShell();
				shell.getDisplay().asyncExec(new CommandSubSystemDisconnectedRunnable((IRemoteCmdSubSystem) source));
			}
			else if (source instanceof IRemoteCommandShell)
			{
				// find out if we're listening to this
				if (Display.getCurrent() != null){			
					updateOutput((IRemoteCommandShell) source, false);
					updateActionStates();
				}
				else {
					final IRemoteCommandShell fsource = (IRemoteCommandShell)source;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (!_folder.isDisposed()){//make sure view isn't disposed
								updateOutput(fsource, false);
								updateActionStates();
							}
						}					
					});	
				}
			}
		} 
		
		if (event.getType() == ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_REMOVED)
		{
			Object source = event.getSource();
			if (source instanceof IRemoteCommandShell)
			{
				if (Display.getCurrent() != null){			
					updateOutput((IRemoteCommandShell) source, false);
					 _folder.remove(source);
					updateActionStates();
				}
				else {
					final IRemoteCommandShell fsource = (IRemoteCommandShell)source;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (!_folder.isDisposed()){//make sure view isn't disposed
								updateOutput(fsource, false);
								_folder.remove(fsource);
								updateActionStates();
							}
						}					
					});
				}
			}
		}
		else if (event.getType() == ISystemResourceChangeEvents.EVENT_REFRESH)
		{
			Object parent = event.getParent();
			if (parent instanceof IRemoteCommandShell)
			{
				if (Display.getCurrent() != null){			
					updateOutput((IRemoteCommandShell) parent, false);
				}
				else {
					final IRemoteCommandShell fsource = (IRemoteCommandShell)parent;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							if (!_folder.isDisposed()){//make sure view isn't disposed
								updateOutput(fsource, false);
							}
						}					
					});
				}
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
			    //SystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
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
				Shell shell = SystemBasePlugin.getActiveWorkbenchShell();
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
			if (_folder.getInput() != null)
			{
				updateActionStates();
			}
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