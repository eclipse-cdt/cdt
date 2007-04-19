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

package org.eclipse.rse.subsystems.shells.core.subsystems;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IProperty;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.PropertyList;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IRemoteSystemEnvVar;
import org.eclipse.rse.core.subsystems.SubSystem;
import org.eclipse.rse.internal.subsystems.shells.core.ShellStrings;
import org.eclipse.rse.internal.subsystems.shells.subsystems.RemoteSystemEnvVar;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemRegistry;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 * This is the abstraction of a subsystem specialized for remote execution of
 * commands.
 */

public abstract class RemoteCmdSubSystem extends SubSystem implements IRemoteCmdSubSystem, ICommunicationsListener
{

	private static String COMMAND_SHELLS_MEMENTO = "commandshells"; //$NON-NLS-1$
	private static String ENVIRONMENT_VARS = "EnvironmentVariables"; //$NON-NLS-1$
	
	protected ArrayList _cmdShells;

	protected IRemoteCommandShell _defaultShell;

	protected IRemoteFileSubSystem _fileSubSystem;

	public RemoteCmdSubSystem(IHost host, IConnectorService connectorService)
	{
		super(host, connectorService);
		_cmdShells = new ArrayList();
	}

	/**
	 * Return parent subsystem factory, cast to a RemoteCmdSubSystemConfiguration
	 */
	public IRemoteCmdSubSystemConfiguration getParentRemoteCmdSubSystemConfiguration()
	{
		return (IRemoteCmdSubSystemConfiguration) super.getSubSystemConfiguration();
	}

	public String getShellEncoding()
	{
		IPropertySet set = getPropertySet("Remote"); //$NON-NLS-1$
		if (set != null)
		{
			return set.getPropertyValue("shell.encoding"); //$NON-NLS-1$
		}
		return null;
	}

	public void setShellEncoding(String encoding)
	{
		IPropertySet set = getPropertySet("Remote"); //$NON-NLS-1$
		if (set == null)
		{
			set = createPropertySet("Remote", getDescription()); //$NON-NLS-1$
		}
		set.addProperty("shell.encoding", encoding); //$NON-NLS-1$
		setDirty(true);
		commit();
	}

	/**
	 * Long running list processing calls this method to check for a user-cancel
	 * event. If user did cancel, an exception is thrown.
	 * 
	 * @return true if caller wants to cancel
	 */
	public boolean checkForCancel(IProgressMonitor monitor)
	{
		if ((monitor != null) && monitor.isCanceled())
			throw new OperationCanceledException(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_OPERATION_CANCELLED)
					.getLevelOneText());
		return false;
	}

	// ---------------------------------
	// ENVIRONMENT VARIABLE METHODS ...
	// ---------------------------------

	/**
	 * Get the initial environment variable list as a string of
	 * RemoteSystemEnvVar objects. Array returned may be size zero but will not
	 * be null.
	 */
	public IRemoteSystemEnvVar[] getEnvironmentVariableList() {
		IPropertySet environmentVariables = getEnvironmentVariables();
		String[] names = environmentVariables.getPropertyKeys();
		IRemoteSystemEnvVar[] result = new IRemoteSystemEnvVar[names.length];
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			String value = environmentVariables.getPropertyValue(name);
			IRemoteSystemEnvVar v = new RemoteSystemEnvVar();
			v.setName(name);
			v.setValue(value);
			result[i] = v;
		}
		return result;
	}

	/**
	 * Set the initial environment variable list entries, all in one shot, using
	 * a pair of String arrays: the first is the environment variable names, the
	 * second is the corresponding environment variable values.
	 * @param names the array of names
	 * @param values the array of string values
	 */
	public void setEnvironmentVariableList(String[] names, String[] values) {
		removePropertySet(ENVIRONMENT_VARS);
		IPropertySet environmentVariables = getEnvironmentVariables();
		if (names != null) {
			for (int i = 0; i < names.length; i++) {
				environmentVariables.addProperty(names[i], values[i]);
			}
		}
		try {
			if (getSubSystemConfiguration() != null) getSubSystemConfiguration().saveSubSystem(this);
		} catch (Exception exc) {
			RSECorePlugin.getDefault().getLogger().logError("Error saving command subsystem after setting env var entries", exc); //$NON-NLS-1$
		}
	}

	/**
	 * Add environment variable entry, given a name and value
	 */
	public void addEnvironmentVariable(String name, String value) {
		IPropertySet environmentVariables = getEnvironmentVariables();
		environmentVariables.addProperty(name, value);
		commit();
	}

	/**
	 * Add environment variable entry, given a RemoteSystemEnvVar object
	 */
	public void addEnvironmentVariable(IRemoteSystemEnvVar rsev) {
		addEnvironmentVariable(rsev.getName(), rsev.getValue());
	}

	/**
	 * Remove environment variable entry given its RemoteSystemEnvVar object
	 * @param rsev the remote system environment variable to remove
	 */
	public void removeEnvironmentVariable(IRemoteSystemEnvVar rsev) {
		removeEnvironmentVariable(rsev.getName());
	}

	/**
	 * Remove environment variable entry given only its environment variable
	 * name
	 */
	public void removeEnvironmentVariable(String name) {
		IPropertySet environmentVariables = getEnvironmentVariables();
		environmentVariables.removeProperty(name);
		commit();
	}

	/**
	 * Given an environment variable name, find its RemoteSystemEnvVar object.
	 * Returns null if not found.
	 */
	public IRemoteSystemEnvVar getEnvironmentVariable(String name) {
		IRemoteSystemEnvVar result = null;
		String value = getEnvironmentVariableValue(name);
		if (value != null) {
			result = new RemoteSystemEnvVar();
			result.setName(name);
			result.setValue(value);
		}
		return result;
	}

	/**
	 * Given an environment variable name, find its value. Returns null if not
	 * found.
	 */
	public String getEnvironmentVariableValue(String name) {
		IPropertySet environmentVariables = getEnvironmentVariables();
		String value = environmentVariables.getPropertyValue(name);
		return value;
	}

	/**
	 * 
	 */
	protected String[] getEnvVarsAsStringArray() {
		IPropertySet environmentVariables = getEnvironmentVariables();
		String[] names = environmentVariables.getPropertyKeys();
		String[] result = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			String value = environmentVariables.getPropertyValue(name);
			result[i] = name + "=" + value; //$NON-NLS-1$
		}
		return result;
	}

	protected boolean isUniqueVariable(List variables, String varName)
	{
		for (int i = 0; i < variables.size(); i++)
		{
			String variableStr = (String) variables.get(i);
			if (variableStr.startsWith(varName))
			{
				return false;
			}
		}
		return true;
	}

	protected String[] getUserAndHostEnvVarsAsStringArray()
	{
		String[] userVars = getEnvVarsAsStringArray();
		List systemVars = getHostEnvironmentVariables();

		List combinedVars = new ArrayList();

		// add user vars first
		// going in reverse order so that last set takes precedence over
		// previous
		// note that we currently don't support variable substituation via
		// multiple user
		// definitions of same variable
		if (userVars != null)
		{
			for (int i = userVars.length - 1; i >= 0; i--)
			{
				String userVar = userVars[i];
				String varName = null;
				int assignIndex = userVar.indexOf('=');
				if (assignIndex > 0)
				{
					varName = userVar.substring(0, assignIndex + 1);
					if (isUniqueVariable(combinedVars, varName))
					{
						combinedVars.add(userVar);
					}
				}
			}
		}

		// add system vars that are unique
		// any system var currently defined as user var is considered
		// overridden
		// note that we currently don't support variable sbustitution via user
		// variable extension over system vars
		for (int s = 0; s < systemVars.size(); s++)
		{
			String systemVar = (String) systemVars.get(s);
			String varName = null;
			int assignIndex = systemVar.indexOf('=');
			if (assignIndex > 0)
			{
				varName = systemVar.substring(0, assignIndex + 1);

				if (isUniqueVariable(combinedVars, varName))
				{
					combinedVars.add(systemVar);
				}
			}
		}

		// convert to string array
		String[] result = new String[combinedVars.size()];
		for (int a = 0; a < combinedVars.size(); a++)
		{
			result[a] = (String) combinedVars.get(a);
		}

		return result;
	}

	public boolean isWindows()
	{
		String type = getSystemType();
		return (type.equals("Windows") || type.equals("Local") //$NON-NLS-1$ //$NON-NLS-2$
				&& System.getProperty("os.name").toLowerCase().startsWith("win")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Lists the possible commands for the given context
	 * 
	 * @param context
	 *            the context for a command
	 * @return the list of possible commands
	 */
	public ICandidateCommand[] getCandidateCommands(Object context)
	{
		if (context instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell command = (IRemoteCommandShell) context;
			return command.getCandidateCommands();
		}
		return null;
	}

	protected List parsePathEnvironmentVariable(String path)
	{
		ArrayList addedPaths = new ArrayList();
		ArrayList addedFolders = new ArrayList();

		char separator = isWindows() ? ';' : ':';
		StringTokenizer tokenizer = new StringTokenizer(path, separator + ""); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			if (!addedPaths.contains(token))
			{
				addedPaths.add(token);

				// resolve folder
				IRemoteFileSubSystem fs = getFileSubSystem();
				try
				{
					IRemoteFile file = fs.getRemoteFileObject(token);
					addedFolders.add(file);
				}
				catch (Exception e)
				{
				}
			}
		}
		return addedFolders;
	}

	public IRemoteFileSubSystem getFileSubSystem()
	{
		if (_fileSubSystem == null)
		{
			_fileSubSystem = RemoteFileUtility.getFileSubSystem(getHost());
		}
		return _fileSubSystem;
	}

	/**
	 * Default implementation of getInvalidEnvironmentVariableNameCharacters.
	 * This default implementation just returns "=" (the only invalid character
	 * is the = sign.) Subclasses can override this to provide a more
	 * comprehensive list.
	 * 
	 * @see org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem#getInvalidEnvironmentVariableNameCharacters()
	 */
	public String getInvalidEnvironmentVariableNameCharacters()
	{
		return "="; //$NON-NLS-1$
	}

	// -------------------------------
	// SubSystem METHODS ...
	// -------------------------------

	/**
	 * Return the associated command subsystem. By default, returns the first
	 * command subsystem found for this connection, but can be refined by each
	 * subsystem implementation. For command subsystems, returns "this".
	 */
	public IRemoteCmdSubSystem getCommandSubSystem()
	{
		return this;
	}

	/**
	 * Actually resolve an absolute filter string. This is called by the
	 * run(IProgressMonitor monitor) method, which in turn is called by
	 * resolveFilterString.
	 * 
	 * @see org.eclipse.rse.core.subsystems.SubSystem#internalResolveFilterString(IProgressMonitor,String)
	 */
	protected Object[] internalResolveFilterString(IProgressMonitor monitor, String filterString)
			throws InvocationTargetException, InterruptedException
	{
		return null;
	}

	public Object[] getChildren()
	{
		return getShells();
		// return null;
	}

	public boolean hasChildren()
	{
		if (_cmdShells == null)
			return false;
		return _cmdShells.size() > 0;
	}

	/**
	 * Get the default running command shell for this command subsystem. If no
	 * such shell exists or is running, a new one is launched.
	 * 
	 * @return the default running command shell
	 */
	public IRemoteCommandShell getDefaultShell() throws Exception
	{
		IRemoteCommandShell[] shells = getShells();
		if (shells == null || shells.length == 0)
		{
			return runShell( null);
		}
		else
		{
			return shells[0];
		}
	}

	/**
	 * Get all command shells and transient commands that have been run or are
	 * running for this command subsystem.
	 * 
	 * @return the list of running command shells and commands
	 */
	public IRemoteCommandShell[] getShells()
	{
		IRemoteCommandShell[] shells = new IRemoteCommandShell[_cmdShells.size()];
		for (int i = 0; i < _cmdShells.size(); i++)
		{
			shells[i] = (IRemoteCommandShell) _cmdShells.get(i);
		}
		return shells;
	}

	/**
	 * Determine whether the command subsystem can run a shell
	 * 
	 * @return whether a shell can be run or not
	 */
	public boolean canRunShell()
	{
		return true;
	}

	/**
	 * Determine whether the command subsystem can run a command
	 * 
	 * @return whether a command can be run or not
	 */
	public boolean canRunCommand()
	{
		return true;
	}

	/**
	 * Return the object within the subsystem that corresponds to the specified
	 * unique ID. For remote command, the key is determined by the command ID
	 * and the ouput ID
	 */
	public Object getObjectWithAbsoluteName(String key) throws Exception
	{
		String cmdKey = key;
		String outKey = null;
		int indexOfColon = key.indexOf(':');
		if (indexOfColon > 0)
		{
			// get an output
			cmdKey = key.substring(0, indexOfColon);
			outKey = key.substring(indexOfColon + 1, key.length());
		}

		IRemoteCommandShell theCmd = null;
		// get a command
		IRemoteCommandShell[] cmds = getShells();
		for (int i = 0; i < cmds.length && theCmd == null; i++)
		{
			IRemoteCommandShell cmd = cmds[i];
			if (cmd != null && cmd.getId().equals(cmdKey))
			{
				theCmd = cmd;
			}
		}
		if (theCmd != null && outKey != null)
		{
			int outIndex = Integer.parseInt(outKey);
			return theCmd.getOutputAt(outIndex);
		}
		else
		{
			return theCmd;
		}
	}

	// called by subsystem on disconnect
	protected void saveShellState(List cmdShells)
	{
		// DKM: changing this so that only first active shell is saved
		StringBuffer shellBuffer = new StringBuffer();
		boolean gotShell = false;
		for (int i = 0; i < cmdShells.size() && !gotShell; i++)
		{
			/*
			 * if (i != 0) { shellBuffer.append('|'); }
			 */
			IRemoteCommandShell cmd = (IRemoteCommandShell) cmdShells.get(i);
			if (cmd.isActive())
			{
				Object context = cmd.getContextString();
				if (context instanceof String)
				{
					shellBuffer.append(context);
					gotShell = true;
				}
				else
				{
					shellBuffer.append(cmd.getType());
					gotShell = true;
				}
			}
		}

		IPropertySet set = getPropertySet("Remote"); //$NON-NLS-1$
		if (set != null)
		{
			IProperty property = set.getProperty(COMMAND_SHELLS_MEMENTO);
			if (property != null)
			{
				property.setValue(shellBuffer.toString());
			}
		}
	}

	protected void internalRemoveShell(Object command) throws InvocationTargetException,
			InterruptedException
	{
		if (command instanceof IRemoteCommandShell)
		{
			IRemoteCommandShell cmdShell = (IRemoteCommandShell) command;
			if (cmdShell.isActive())
			{
				internalCancelShell(null, command);
			}
			if (_defaultShell == command)
			{
				_defaultShell = null;
			}
			_cmdShells.remove(command);
			Display.getDefault().asyncExec(new RefreshRemovedShell(this, cmdShell));
		}



	}

	// called to restore running shells - behaviour determined by UI
	public IRemoteCommandShell[] restoreShellState(Shell shellWindow)
	{
		this.shell = shellWindow;
		IRemoteCommandShell[] results = null;
		
		String shellStr = null;
		IPropertySet set = getPropertySet("Remote"); //$NON-NLS-1$
		if (set != null)
		{
			shellStr =  set.getPropertyValue(COMMAND_SHELLS_MEMENTO);
		}

		int numShells = 0;
		if (shellStr != null && shellStr.length() > 0)
		{
			StringTokenizer tok = new StringTokenizer(shellStr, "|"); //$NON-NLS-1$
			results = new IRemoteCommandShell[tok.countTokens()];

			while (tok.hasMoreTokens())
			{
				String context = tok.nextToken();
				if (context != null && context.length() > 0)
				{
					try
					{
						IRemoteCommandShell rmtCmd = internalRunShell(null, context);
						results[numShells] = rmtCmd;
						numShells++;
					}
					catch (Exception e)
					{
					}
				}
			}
		}

		//ISystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
//		registry.fireEvent(new SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_REFRESH, this));

		Display.getDefault().asyncExec(new Refresh(this));
		return results;
	}

	public void cancelAllShells()
	{
	
		for (int i = _cmdShells.size() - 1; i >= 0; i--)
		{
			IRemoteCommandShell cmdShell = (IRemoteCommandShell) _cmdShells.get(i);

			try
			{
				internalRemoveShell(cmdShell);
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
			}

		}

		_cmdShells.clear();
		_defaultShell = null;
		// registry.fireEvent(new
		// org.eclipse.rse.model.SystemResourceChangeEvent(this,
		// ISystemResourceChangeEvent.EVENT_COMMAND_SHELL_FINISHED, null));
		Display.getDefault().asyncExec(new Refresh(this));

	}
	
	public class Refresh implements Runnable
	{
		private RemoteCmdSubSystem _ss;
		public Refresh(RemoteCmdSubSystem ss)
		{
			_ss = ss;
		}

		public void run() 
		{
			SystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			registry.fireEvent(new SystemResourceChangeEvent(_ss, ISystemResourceChangeEvents.EVENT_REFRESH, _ss));
		}
	}


	public class RefreshRemovedShell implements Runnable
	{
		private RemoteCmdSubSystem _ss;
		private IRemoteCommandShell _cmdShell;
		public RefreshRemovedShell(RemoteCmdSubSystem ss, IRemoteCommandShell cmdShell)
		{
			_ss = ss;
			_cmdShell = cmdShell;
		}

		public void run() 
		{
			SystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
			registry.fireEvent(new SystemResourceChangeEvent(_cmdShell, ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_REMOVED, null));
			registry.fireEvent(new SystemResourceChangeEvent(_ss, ISystemResourceChangeEvents.EVENT_REFRESH, _ss));
		}
	}
	

	/**
	 * @see ICommunicationsListener#isPassiveCommunicationsListener()
	 */
	public boolean isPassiveCommunicationsListener()
	{
		return true;
	}

	private class CancelAllShells implements Runnable
	{
		public void run()
		{
			cancelAllShells();
		}
	}

	public class RefreshSubSystem implements Runnable
	{
		RemoteCmdSubSystem _ss;

		public RefreshSubSystem(RemoteCmdSubSystem ss)
		{
			_ss = ss;
		}

		public void run()
		{
			RSEUIPlugin.getTheSystemRegistry().fireEvent(
					new SystemResourceChangeEvent(_ss, ISystemResourceChangeEvents.EVENT_COMMAND_SHELL_REMOVED, _ss));
		}

	}

	public void communicationsStateChange(CommunicationsEvent e)
	{
		switch (e.getState())
		{
		case CommunicationsEvent.AFTER_DISCONNECT:
			// no longer listen
			getConnectorService().removeCommunicationsListener(this);
			// if (_cmdShells != null) _cmdShells.clear();
			// if (_envVars != null) _envVars.clear();
			// _defaultShell = null;

			break;

		case CommunicationsEvent.BEFORE_DISCONNECT:
		case CommunicationsEvent.CONNECTION_ERROR:
			// remove all shells
			saveShellState(_cmdShells);
			if (getShells().length > 0)
			{
				Display.getDefault().asyncExec(new CancelAllShells());
				// cancelAllShells();
			}
			break;
		default:
			break;
		}
	}

	private IPropertySet getEnvironmentVariables() {
		IPropertySet environmentVariables = getPropertySet(ENVIRONMENT_VARS);
		if (environmentVariables == null) {
			environmentVariables = createPropertySet(ENVIRONMENT_VARS);
		}
		return environmentVariables;
	}
	
	public IPropertySet createPropertySet(String name) {
		IPropertySet result = null;
		if (name.equals(ENVIRONMENT_VARS)) {
			result = new PropertyList(ENVIRONMENT_VARS);
			addPropertySet(result);
		} else {
			result = super.createPropertySet(name);
		}
		return result;
	}

	/**
	 * overridden so that for universal we don't need to do in modal thread
	 * @deprecated 
	 */
	public Object[] runCommand(String command, Object context, boolean interpretOutput) throws Exception
	{
		return internalRunCommand(null, command, context, interpretOutput);
	}
	
	/**
	 * overridden so that for universal we don't need to do in modal thread
	 */
	public Object[] runCommand(IProgressMonitor monitor, String command, Object context, boolean interpretOutput) throws Exception
	{
		return internalRunCommand(monitor, command, context, interpretOutput);
	}

	/**
	 * overridden so that for universal we don't need to do in modal thread
	 * 
	 * @deprecated
	 */
	public IRemoteCommandShell runShell(Object context) throws Exception
	{
		IRemoteCommandShell cmdShell = null;
		if (isConnected())
		{
			cmdShell = internalRunShell(null, context);
		}
		else
		{
			return null;
		}

		SystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		registry.fireEvent(new SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_REFRESH, this));

		return cmdShell;
	}
	
	/**
	 * overridden so that for universal we don't need to do in modal thread
	 */
	public IRemoteCommandShell runShell(IProgressMonitor monitor, Object context) throws Exception
	{
		IRemoteCommandShell cmdShell = internalRunShell(monitor, context);
		SystemRegistry registry = RSEUIPlugin.getTheSystemRegistry();
		registry.fireEvent(new SystemResourceChangeEvent(this, ISystemResourceChangeEvents.EVENT_REFRESH, this));

		return cmdShell;
	}

	/**
	 * Execute a remote command. This is only applicable if the subsystem
	 * factory reports true for supportsCommands().
	 * 
	 * @param command Command to be executed remotely.
	 * @param context context of a command (i.e. working directory). <code>null</code> is
	 *            valid and means to use the default context.
	 * @return Array of objects that are the result of running this command.
	 *         Typically, these are messages logged by the command.
	 *         
	 * @deprecated
	 */
	public Object[] runCommand(String command, Object context) throws Exception
	{
		return runCommand(command,  context, true);
	}
	
	/**
	 * Execute a remote command. This is only applicable if the subsystem
	 * factory reports true for supportsCommands().
	 * 
	 * @param command Command to be executed remotely.
	 * @param context context of a command (i.e. working directory). <code>null</code> is
	 *            valid and means to use the default context.
	 * @return Array of objects that are the result of running this command.
	 *         Typically, these are messages logged by the command.
	 */
	public Object[] runCommand(IProgressMonitor monitor, String command, Object context) throws Exception
	{
		return runCommand(monitor, command,  context, true);
	}

	/**
	 * Send a command as input to a running command shell.
	 * 
	 * @param input the command to invoke in the shell.
	 * @param commandObject the shell or command to send the invocation to.
	 * 
	 * @deprecated
	 */
	public void sendCommandToShell(String input,  Object commandObject) throws Exception
	{
		boolean ok = true;
		if (!isConnected())
			ok = promptForPassword();
		if (ok)
		{
			Display display = Display.getCurrent();
			if (display != null)
			{
				internalSendCommandToShell(new NullProgressMonitor(), input, commandObject);
			}
			else
			{
			try
			{
				SendCommandToShellJob job = new SendCommandToShellJob(input, commandObject);

				scheduleJob(job, null);
			}
			catch (InterruptedException exc)
			{
				if (shell == null)
					throw exc;
				else
					showOperationCancelledMessage(shell);
			}
			}
		}
		else
			SystemBasePlugin.logDebugMessage(this.getClass().getName(),
					"in SubSystemImpl.sendCommandToShell: isConnected() returning false!"); //$NON-NLS-1$

	}
	
	/**
	 * Send a command as input to a running command shell.
	 * 
	 * @param monitor the progress monitor
	 * @param input the command to invoke in the shell.
	 * @param commandObject the shell or command to send the invocation to.
	 */
	public void sendCommandToShell(IProgressMonitor monitor, String input,  Object commandObject) throws Exception
	{
		boolean ok = true;
		if (!isConnected())
			ok = promptForPassword();
		if (ok)
		{
			internalSendCommandToShell(monitor, input, commandObject);
		}		
		else
			SystemBasePlugin.logDebugMessage(this.getClass().getName(),
					"in SubSystemImpl.sendCommandToShell: isConnected() returning false!"); //$NON-NLS-1$
	}

	/**
	 * Cancel a shell or running command.
	 * 
	 * @param commandObject the shell or command to cancel.
	 */
	public void cancelShell(Object commandObject) throws Exception
	{
		if (isConnected())
		{
			internalCancelShell(null, commandObject);
		}
		else
		{
			boolean ok = true;
			if (!isConnected())
				ok = promptForPassword();
			if (ok)
			{
				try
				{
					CancelShellJob job = new CancelShellJob(commandObject);
					scheduleJob(job, null);
				}
				catch (InterruptedException exc)
				{
					if (shell == null)
						throw exc;
					else
						showOperationCancelledMessage(shell);
				}
			}
			else
				SystemBasePlugin.logDebugMessage(this.getClass().getName(),
						"in SubSystemImpl.cancelShell: isConnected() returning false!"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Cancel a shell or running command.
	 * 
	 * @param monitor the progress monitor
	 * @param commandObject the shell or command to cancel.
	 */
	public void cancelShell(IProgressMonitor monitor, Object commandObject) throws Exception
	{
		boolean ok = true;
		if (!isConnected())
			ok = promptForPassword();
		
		if (ok)
		{
			internalCancelShell(monitor, commandObject);
		}
		else
		{
			SystemBasePlugin.logDebugMessage(this.getClass().getName(),
					"in SubSystemImpl.cancelShell: isConnected() returning false!"); //$NON-NLS-1$
		}
	}


	/**
	 * Remove and Cancel a shell or running command.
	 * 
	 * @param commandObject the shell or command to cancel.
	 */
	public void removeShell(Object commandObject) throws Exception
	{
		if (isConnected())
		{
			internalRemoveShell(commandObject);
		}
		else
		{
			boolean ok = true;
			if (!isConnected())
				ok = promptForPassword();
			if (ok)
			{
				try
				{
					RemoveShellJob job = new RemoveShellJob(commandObject);
					scheduleJob(job, null);
				}
				catch (InterruptedException exc)
				{
					if (shell == null)
						throw exc;
					else
						showOperationCancelledMessage(shell);
				}
			}
			else
				SystemBasePlugin.logDebugMessage(this.getClass().getName(),
						"in SubSystemImpl.removeShell: isConnected() returning false!"); //$NON-NLS-1$
		}
	}

	/**
	 * Represents the subsystem operation of running a remote command.
	 */
	protected class RunCommandJob extends SubSystemOperationJob
	{
		protected String _cmd;

		protected Object _runContext;

		protected boolean _runInterpret;

		/**
		 * Creates a new RunCommandJob
		 * 
		 * @param cmd
		 *            The remote command to run
		 * @param runContext
		 *            The context in which to run the command
		 * @param runInterpret
		 *            Whether or not to interpret results of the command as RSE
		 *            objects
		 */
		public RunCommandJob(String cmd, Object runContext, boolean runInterpret)
		{
			super(ShellStrings.RSESubSystemOperation_Run_command_message);
			_cmd = cmd;
			_runContext = runContext;
			_runInterpret = runInterpret;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException,
				Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			msg = getRunningMessage(_cmd);

			if (!implicitConnect(false, mon, msg, totalWorkUnits))
				throw new Exception(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_FAILED).makeSubstitution(
						getHostName()).getLevelOneText());
			runOutputs = internalRunCommand(mon, _cmd, _runContext, _runInterpret);
		}
	}

	/**
	 * Represents the subsystem operation of running a remote shell.
	 */
	protected class RunShellJob extends SubSystemOperationJob
	{
		protected Object _runContext;

		/**
		 * Creates a new RunShellJob
		 * 
		 * @param runContext
		 *            the context within which the shell will be ran
		 */
		public RunShellJob(Object runContext)
		{
			super(ShellStrings.RSESubSystemOperation_Run_Shell_message);
			_runContext = runContext;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException,
				Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			if (!implicitConnect(false, mon, msg, totalWorkUnits))
				throw new Exception(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_FAILED).makeSubstitution(
						getHostName()).getLevelOneText());
			runOutputs = new Object[]
			{
				internalRunShell(mon, _runContext)
			};
		}
	}

	/**
	 * Represents the subsystem operation of cancelling a remote shell.
	 */
	protected class CancelShellJob extends SubSystemOperationJob
	{
		protected Object _runContext;

		/**
		 * Constructs a new CancelShellJob
		 * 
		 * @param runContext
		 *            The context for the cancelled shell
		 */
		public CancelShellJob(Object runContext)
		{
			super(ShellStrings.RSESubSystemOperation_Cancel_Shell_message);
			_runContext = runContext;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException,
				Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			if (!implicitConnect(false, mon, msg, totalWorkUnits))
				throw new Exception(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_FAILED).makeSubstitution(
						getHostName()).getLevelOneText());
			internalCancelShell(mon, _runContext);
		}
	}

	/**
	 * Represents the subsystem operation of sending a command to a remote
	 * shell.
	 */
	protected class SendCommandToShellJob extends SubSystemOperationJob
	{
		protected Object _runContext;

		protected String _cmd;

		/**
		 * Constructs a new SendCommandToShellJob
		 * 
		 * @param cmd
		 *            The command to send to the shell
		 * @param runContext
		 *            The context in which the command is to be run
		 */
		public SendCommandToShellJob(String cmd, Object runContext)
		{
			super(ShellStrings.RSESubSystemOperation_Send_command_to_Shell_message);
			_cmd = cmd;
			_runContext = runContext;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException,
				Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			msg = getRunningMessage(_cmd);

			if (!implicitConnect(false, mon, msg, totalWorkUnits))
				throw new Exception(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_FAILED).makeSubstitution(
						getHostName()).getLevelOneText());
			internalSendCommandToShell(mon, _cmd, _runContext);
		}
	}

	/**
	 * Represents the subsystem operation of cancelling and removing a remote
	 * shell from the view.
	 */
	protected class RemoveShellJob extends SubSystemOperationJob
	{
		protected Object _runContext;

		/**
		 * Constructs a new RemoveShellJob
		 * 
		 * @param runContext
		 *            the context for the removed shell
		 */
		public RemoveShellJob(Object runContext)
		{
			super(ShellStrings.RSESubSystemOperation_Remove_Shell_message);
			_runContext = runContext;
		}

		public void performOperation(IProgressMonitor mon) throws InterruptedException, InvocationTargetException,
				Exception
		{
			String msg = null;
			int totalWorkUnits = IProgressMonitor.UNKNOWN;

			if (!implicitConnect(false, mon, msg, totalWorkUnits))
				throw new Exception(RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_CONNECT_FAILED).makeSubstitution(
						getHostName()).getLevelOneText());
			internalRemoveShell(_runContext);
		}
	}

	/**
	 * Actually run a remote command. This is called by the run(IProgressMonitor
	 * monitor) method, which in turn is called by runCommand(...).
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 * <li>if the user cancels (monitor.isCanceled()), throw new
	 * InterruptedException()
	 * <li>if something else bad happens, throw new
	 * InvocationTargetException(exc);
	 * <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT COMMANDS!
	 */
	protected Object[] internalRunCommand(IProgressMonitor monitor, String cmd, Object context)
			throws java.lang.reflect.InvocationTargetException, java.lang.InterruptedException, SystemMessageException
	{
		return null;
	}

	/**
	 * Actually run a remote command. This is called by the run(IProgressMonitor
	 * monitor) method, which in turn is called by runCommand(...).
	 * <p>
	 * As per IRunnableWithProgress rules:
	 * <ul>
	 * <li>if the user cancels (monitor.isCanceled()), throw new
	 * InterruptedException()
	 * <li>if something else bad happens, throw new
	 * InvocationTargetException(exc);
	 * <li>do not worry about calling monitor.done() ... caller will do that!
	 * </ul>
	 * YOU MUST OVERRIDE THIS IF YOU SUPPORT COMMANDS!
	 */
	protected Object[] internalRunCommand(IProgressMonitor monitor, String cmd, Object context, boolean interpretOutput)
			throws InvocationTargetException, InterruptedException, SystemMessageException
	{
		return null;
	}

	protected IRemoteCommandShell internalRunShell(IProgressMonitor monitor, Object context)
			throws InvocationTargetException, InterruptedException, SystemMessageException
	{
		return null;
	}

	protected void internalCancelShell(IProgressMonitor monitor, Object command)
			throws InvocationTargetException, InterruptedException
	{
	}

	protected void internalSendCommandToShell(IProgressMonitor monitor, String cmd, Object command)
			throws InvocationTargetException, InterruptedException
	{
	}
}