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

package org.eclipse.rse.shells.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.shells.ui.RemoteCommandHelpers;
import org.eclipse.rse.shells.ui.ShellResources;
import org.eclipse.rse.shells.ui.view.SystemCommandsUI;
import org.eclipse.rse.shells.ui.view.SystemCommandsViewPart;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCommandShell;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.actions.SystemBaseAction;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * Launches a shell and/or runs a shell command, displaying the output
 * in the Remote Systems Commands view. 
 */
public class SystemCommandAction extends SystemBaseAction
{
	private IRemoteFile _selected;
	private ISystemFilterReference _selectedFilterRef;
	

	private boolean _isShell;
	private IRemoteCmdSubSystem _cmdSubSystem;


	/**
	 * The command dialog used when running a command. 
	 */
	public class CommandDialog extends SystemPromptDialog implements ISystemMessages
	{
		private Combo _cmdText;
		private Button _launchNewShellButton;
		private SystemMessage _errorMessage;

		private String _cmdStr;
		private boolean _launchNewShell;

		/**
		 * Constructor for the CommandDialog
		 * @param shell the parent of the dialog
		 */
		public CommandDialog(Shell shell)
		{
			super(shell, ShellResources.RESID_SHELLS_RUN_COMMAND_LABEL);
			//pack();
		}

		/**
		 * @return the user-specified command invocation
		 */
		public String getCmdStr()
		{
			return _cmdStr;
		}

		/**
		 * @return whether the specified command is to be launched in a new shell or not
		 */
		public boolean launchNewShell()
		{
			return _launchNewShell;
		}

		protected void buttonPressed(int buttonId)
		{
			setReturnCode(buttonId);
			_cmdStr = _cmdText.getText();
			_launchNewShell = _launchNewShellButton.getSelection();
			close();
		}

		/**
		 * Constructs the dialog
		 * @param parent the parent of the dialog
		 */
		public Control createInner(Composite parent)
		{
			Composite c = new Composite(parent, SWT.NONE);

			GridLayout layout = new GridLayout();
			c.setLayout(layout);
			c.setLayoutData(new GridData(GridData.FILL_BOTH));

			Label aLabel = new Label(c, SWT.NONE);
			aLabel.setText(ShellResources.RESID_SHELLS_COMMAND_LABEL);

			_cmdText = new Combo(c, SWT.SINGLE | SWT.BORDER);
			GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
			_cmdText.setLayoutData(textData);
			_cmdText.setText("");
			_cmdText.setToolTipText(ShellResources.RESID_SHELLS_COMMAND_TOOLTIP);

			// add keystroke listeners...
			_cmdText.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					validateInvocation();
				}
			});

			IRemoteCmdSubSystem cmdSubSystem = getCommandSubSystem();
            if (cmdSubSystem != null)
            {
                String[] cmds = cmdSubSystem.getExecutedCommands();
                if (cmds != null)
                    _cmdText.setItems(cmds);
            }


			_launchNewShellButton = new Button(c, SWT.CHECK);
			_launchNewShellButton.setText(ShellResources.RESID_SHELLS_RUN_IN_NEW_SHELL_LABEL);
			_launchNewShellButton.setToolTipText(ShellResources.RESID_SHELLS_RUN_IN_NEW_SHELL_TOOLTIP);

			setHelp();

			enableOkButton(false);
			return c;
		}


		/**
		 * returns the initial focus control
		 */
		protected Control getInitialFocusControl()
		{
			enableOkButton(false);
			return _cmdText;
		}

		private void setHelp()
		{
			setHelp(SystemPlugin.HELPPREFIX + "cmdi0000");
		}

		/**
		 * validate the invocation
		 * @return a SystemMessage if the invocation is invalid.	
		 */
		protected SystemMessage validateInvocation()
		{
			_errorMessage = null;
			String theNewName = _cmdText.getText();

			if (theNewName.length() == 0)
			{
				_errorMessage = SystemPlugin.getPluginMessage(MSG_UCMD_INVOCATION_EMPTY);
			}

			if (_errorMessage != null)
			{
				setErrorMessage(_errorMessage);
				enableOkButton(false);
			}
			else
			{
				clearErrorMessage();
				enableOkButton(true);
			}

			return _errorMessage;
		}
	}

	/**
	 * Constructor for SystemCommandAction
	 * @param parent
	 */
	public SystemCommandAction(Shell parent)
	{
		this(parent, false);
	}

	/**
	 * Constructor for SystemCommandAction
	 * @param parent
	 * @param isShell indication of whether this action launches a shell or runs a command
	 */
	public SystemCommandAction(Shell parent, boolean isShell)
	{
		this(parent, isShell, null);
	}

	/**
	 * Constructor for SystemCommandAction
	 * @param parent
	 * @param isShell indication of whether this action launches a shell or runs a command
	 * @param cmdSubSystem the command subsystem to use if launching a shell
	 */
	public SystemCommandAction(Shell parent, boolean isShell, IRemoteCmdSubSystem cmdSubSystem)
	{
		this(
			isShell ? ShellResources.ACTION_RUN_SHELL_LABEL : ShellResources.ACTION_RUN_COMMAND_LABEL,
			isShell ? ShellResources.ACTION_RUN_SHELL_TOOLTIP : ShellResources.ACTION_RUN_COMMAND_TOOLTIP,
			parent,
			isShell,
			cmdSubSystem);
	}

	/**
	 * Constructor for SystemCommandAction
	 * @param title the title of the action
	 * @param parent
	 * @param isShell indication of whether this action launches a shell or runs a command
	 * @param cmdSubSystem the command subsystem to use if launching a shell
	 */
	public SystemCommandAction(String title, Shell parent, boolean isShell, IRemoteCmdSubSystem cmdSubSystem)
	{
		this(title, null, parent, isShell, cmdSubSystem);
	}


	/**
	 * Constructor for SystemCommandAction
	 * @param title the title of the action
	 * @param tooltip the tooltip for the action
	 * @param parent
	 * @param isShell indication of whether this action launches a shell or runs a command
	 * @param cmdSubSystem the command subsystem to use if launching a shell
	 */	
	public SystemCommandAction(String title, String tooltip, Shell parent, boolean isShell, IRemoteCmdSubSystem cmdSubSystem)
	{
		this(
			title,
			tooltip,
			isShell ? SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_SHELL_ID) : SystemPlugin.getDefault().getImageDescriptor(ISystemIconConstants.ICON_SYSTEM_RUN_ID),
			parent,
			isShell,
			cmdSubSystem);
	}


	/**
	 * Constructor for SystemCommandAction
	 * @param title the title of the action
	 * @param tooltip the tooltip for the action
	 * @param descriptor the image descriptor for the action
	 * @param parent
	 * @param isShell indication of whether this action launches a shell or runs a command
	 * @param cmdSubSystem the command subsystem to use if launching a shell
	 */	
	public SystemCommandAction(String title, String tooltip, ImageDescriptor descriptor, Shell parent, boolean isShell, IRemoteCmdSubSystem cmdSubSystem)
	{
		super(title, tooltip, descriptor, parent);
		_isShell = isShell;
		_cmdSubSystem = cmdSubSystem;
		if (_isShell)
			setHelp(SystemPlugin.HELPPREFIX+"actn0113");
		else
			setHelp(SystemPlugin.HELPPREFIX+"actn0114");		
	}

	public void setSubSystem(IRemoteCmdSubSystem ss)
	{
		_cmdSubSystem = ss;
	}
	
	/**
	 * Runs the command action.  If the action is for launching a shell, the shell is launched
	 * and the commands view shows it's output.  If the action is for running a command, a
	 * dialog pops up prompting for the invocation.
	 */
	public void run()
	{
		if (_isShell)
		{
			runShell();
		}
		else
		{
			CommandDialog cmdDialog = new CommandDialog(getShell());
			if (cmdDialog.open() == Window.OK)
			{
				String cmdStr = cmdDialog.getCmdStr();
				if (cmdStr != null)
				{
					// run the command
					runCommand(cmdStr, cmdDialog.launchNewShell());
				}
			}
		}
	}

	private IRemoteCmdSubSystem getCommandSubSystem()
	{
		if (_selectedFilterRef != null)
		{
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)_selectedFilterRef).getAdapter(ISystemViewElementAdapter.class);
			if (adapter != null)
			{
				ISubSystem ss = adapter.getSubSystem(_selectedFilterRef);
				if (ss != null)
				{
					return RemoteCommandHelpers.getCmdSubSystem(ss.getHost());
				}
			}
		}
		
		IRemoteFile selectedFile = _selected;
		if (_selected == null)
		{
			return _cmdSubSystem;	
		}

		IHost sysConn = selectedFile.getSystemConnection();
		if (sysConn != null)
		{
			ISubSystem currSubSystem = selectedFile.getParentRemoteFileSubSystem();
			if (currSubSystem != null)
			{
				try
				{
					Shell shell = getShell();
					IRemoteCmdSubSystem cmdSubSystem = RemoteCommandHelpers.getCmdSubSystem(currSubSystem.getHost());
					return cmdSubSystem;
				}
				catch (Exception e)
				{
				}
			}
		}

		return null;
	}

	/**
	 * runs the command
	 */
	private void runCommand(String cmd, boolean launchNewShell)
	{
		if (cmd.length() > 0)
		{

			try
			{
				Shell shell = getShell();
				
				if (_selectedFilterRef != null)
				{
					ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)_selectedFilterRef).getAdapter(ISystemViewElementAdapter.class);
					if (adapter != null)
					{
						ISubSystem ss = adapter.getSubSystem(_selectedFilterRef);
						if (ss != null)
						{
							Object target = ss.getTargetForFilter(_selectedFilterRef);
							if (target != null && target instanceof IRemoteFile)
							{
								_selected = (IRemoteFile)target;
							}
						}
					}
				}
					
				if (_selected != null)
				{
					IRemoteCmdSubSystem cmdSubSystem = getCommandSubSystem();
					String path = _selected.getAbsolutePath();

					if (launchNewShell)
					{
						Object[] results = cmdSubSystem.runCommand(cmd, shell, _selected);
						Object cmdObject = results[0];
						if (cmdObject instanceof IRemoteCommandShell)
						{
							showInView((IRemoteCommandShell) cmdObject);
						}
					}
					else
					{
						// run command in shell
						IRemoteCommandShell defaultShell = cmdSubSystem.getDefaultShell(shell);
						if (defaultShell != null)
						{
							showInView(defaultShell);
						}

						String cdCmd = "cd " + path;
						if ((cmdSubSystem.getHost().getSystemType().equals("Local") && System.getProperty("os.name").toLowerCase().startsWith("win"))
							|| cmdSubSystem.getHost().getSystemType().equals("Windows"))
						{
							cdCmd = "cd /d " + path;
						}
						cmdSubSystem.sendCommandToShell(cdCmd, shell, defaultShell);
						cmdSubSystem.sendCommandToShell(cmd, shell, defaultShell);
					}
				}
				else
				{
					IRemoteCmdSubSystem cmdSubSystem = getCommandSubSystem();
					if (cmdSubSystem != null)
					{
						Object[] results = cmdSubSystem.runCommand(cmd, shell, _selected);
						Object cmdObject = results[0];
						if (cmdObject instanceof IRemoteCommandShell)
						{
							showInView((IRemoteCommandShell) cmdObject);
						}
					}
				}

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	/**
	 * launches a new shell
	 */
	private void runShell()
	{
		try
		{
			Shell shell = getShell();
			if (_selectedFilterRef != null)
			{
				ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)_selectedFilterRef).getAdapter(ISystemViewElementAdapter.class);
				if (adapter != null)
				{
					ISubSystem ss = adapter.getSubSystem(_selectedFilterRef);
					if (ss != null)
					{
						Object target = ss.getTargetForFilter(_selectedFilterRef);
						if (target != null && target instanceof IRemoteFile)
						{
							_selected = (IRemoteFile)target;
						}
					}
				}
			}

			IRemoteCmdSubSystem cmdSubSystem = getCommandSubSystem();
			if (cmdSubSystem != null)
			{
				if (cmdSubSystem.isConnected())
				{
					SystemCommandsUI commandsUI = SystemCommandsUI.getInstance();
					SystemCommandsViewPart cmdsPart = commandsUI.activateCommandsView();
					IRemoteCommandShell cmd = cmdSubSystem.runShell(shell, _selected);
					cmdsPart.updateOutput(cmd);
				}
				else
				{
					cmdSubSystem.connect(shell);
					SystemCommandsUI commandsUI = SystemCommandsUI.getInstance();
					SystemCommandsViewPart cmdsPart = commandsUI.activateCommandsView();
					IRemoteCommandShell cmd = cmdSubSystem.runShell(shell, _selected);
					cmdsPart.updateOutput(cmd);
					//showInView(cmd);
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			//	SystemPlugin.getDefault().logInfo("Exception invoking command " + cmd + " on " + sysConn.getAliasName());
		}

	}

	/**
	 * shows the command in the commands view
	 */
	private void showInView(IRemoteCommandShell cmd)
	{
		SystemCommandsUI commandsUI = SystemCommandsUI.getInstance();
		SystemCommandsViewPart cmdsPart = commandsUI.activateCommandsView();
		cmdsPart.updateOutput(cmd);
	}

	/**
	 * Called when the selection changes in the systems view.  This determines
	 * the input object for the command and whether to enable or disable
	 * the action.
	 * 
	 * @param selection the current seleciton
	 * @return whether to enable or disable the action
	 */
	public boolean updateSelection(IStructuredSelection selection)
	{
		boolean enable = false;

		Iterator e = ((IStructuredSelection) selection).iterator();
		Object selected = e.next();

		if (selected != null)
		{
			if (selected instanceof ISystemFilterReference)
			{
				_selectedFilterRef = (ISystemFilterReference)selected;
				_selected = null;
				enable = true;
			}
			if (selected instanceof IRemoteFile)
			{
				_selected = (IRemoteFile) selected;
				_selectedFilterRef = null;
				if (!_selected.isFile())
				{
					enable = checkObjectType(_selected);
				}
			}
		}

		if (enable)
		{
			IRemoteCmdSubSystem cmdSubSystem = getCommandSubSystem();
			if (_cmdSubSystem != cmdSubSystem)
			{
				_cmdSubSystem = cmdSubSystem;
				enable = _cmdSubSystem.canRunCommand();	
			}
		}
		else if (_cmdSubSystem != null)
		{
			enable = _cmdSubSystem.canRunShell();
		}

		return enable;
	}
}