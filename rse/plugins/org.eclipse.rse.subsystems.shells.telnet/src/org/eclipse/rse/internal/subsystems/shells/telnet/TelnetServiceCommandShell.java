/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Adapted from LocalServiceCommandShell
 * Sheldon D'souza  (Celunite)   - Adapted from SshServiceCommandShell
 * Martin Oberhuber (Wind River) - [225510][api] Fix OutputRefreshJob API leakage
 * Anna Dushistova  (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 * Anna Dushistova  (MontaVista) - [261478] Remove SshShellService, SshHostShell (or deprecate and schedule for removal in 3.2)
 * David McKnight   (IBM)        - [272032][ssh][telnet][local] shell output not setting line numbers when available
 *******************************************************************************/
package org.eclipse.rse.internal.subsystems.shells.telnet;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.services.shells.TerminalServiceHostShell;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.ParsedOutput;
import org.eclipse.rse.services.shells.Patterns;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.model.ISystemOutputRemoteTypes;
import org.eclipse.rse.subsystems.shells.core.model.RemoteError;
import org.eclipse.rse.subsystems.shells.core.model.RemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteOutput;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ServiceCommandShell;

public class TelnetServiceCommandShell extends ServiceCommandShell {

	private Patterns _patterns;
	private String _curCommand;
	private String _workingDir;
	private IRemoteFileSubSystem _fs;

	public TelnetServiceCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell) {
		super(cmdSS, hostShell);
		_patterns = new Patterns();
		_patterns.update("cmd"); //$NON-NLS-1$
		ISubSystem[] sses = cmdSS.getHost().getSubSystems();
		for (int i = 0; i < sses.length; i++)
		{
			if (sses[i] instanceof IRemoteFileSubSystem)
			{
				_fs = (IRemoteFileSubSystem)sses[i];
			}
		}
	}

	public Object getContext()
	{
		String workingDir = _workingDir;
		if (workingDir != null && workingDir.length() > 0 && _fs != null)
		{
			try
			{
				return _fs.getRemoteFileObject(workingDir, new NullProgressMonitor());
			}
			catch (Exception e)
			{
			}
		}
		return null;

	}

	public void shellOutputChanged(IHostShellChangeEvent event)
	{
		IHostOutput[] lines = event.getLines();
		boolean gotCommand = false;
		ArrayList outputs = new ArrayList(lines.length);
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].getString();
			if (line.endsWith(getPromptCommand())) {
				continue; //ignore our synthetic prompt command
			}

			ParsedOutput parsedMsg = null;
			if (!gotCommand && line.equals(_curCommand)) {
				gotCommand = true;
				continue; //ignore remote command echo
			} else {
				try {

					// Bug 160202: Remote shell dies.
					if ((_curCommand == null) || (!_curCommand.trim().equals("ls"))) { //$NON-NLS-1$
						parsedMsg = _patterns.matchLine(line);

						// Bug 160202: Remote shell dies.
						if (_curCommand != null) {
							String temp = _curCommand.trim();
							StringTokenizer tokenizer = new StringTokenizer(temp);

							if (tokenizer.countTokens() == 2) {
								String token1 = tokenizer.nextToken();
								String token2 = tokenizer.nextToken();

								if ((token1.equals("ls")) && (token2.indexOf('-') == 0) && (token2.indexOf('l') > 0)) { //$NON-NLS-1$
									if (line.startsWith("total")) { //$NON-NLS-1$
										parsedMsg = null;
									}
								}
							}
						}
					}
				}
				catch (Throwable e) {
					e.printStackTrace();
				}
			}

			RemoteOutput output = null;

			String type = "stdout"; //$NON-NLS-1$

			if (parsedMsg != null) {
				type = parsedMsg.type;
			}

			if (event.isError()) {
				output = new RemoteError(this, type);
			}
			else  {
				output = new RemoteOutput(this, type);
			}

			output.setText(line);
			if (parsedMsg != null)
			{
				String file = parsedMsg.file;
				if (type.equals(ISystemOutputRemoteTypes.TYPE_PROMPT))
				{
					_workingDir = file;
					output.setAbsolutePath(_workingDir);
				}
				else if(_workingDir!=null)
				{
					IPath p = new Path(_workingDir).append(file);
					output.setAbsolutePath(p.toString());
				}
				else
				{
					output.setAbsolutePath(file);
				}
				if (parsedMsg.line > 0){
					output.setLine(parsedMsg.line);
				}
			}

			addOutput(output);
			outputs.add(output);
		}
		IRemoteOutput[] remoteOutputs = (IRemoteOutput[])outputs.toArray(new IRemoteOutput[outputs.size()]);
		notifyOutputChanged(remoteOutputs, false);
	}

	/**
	 * Return the prompt command, such that lines ending with the
	 * prompt command can be removed from output.
	 * Should be overridden in case the IHostShell used for this
	 * service is not an TelnetHostShell.
	 * @return String promptCommand
	 */
	protected String getPromptCommand() {
		IHostShell shell = getHostShell();
		//assert shell instanceof TelnetHostShell;
		if (shell instanceof TerminalServiceHostShell) {
			return ((TerminalServiceHostShell)shell).getPromptCommand();
		}
		//return something impossible such that nothing is ever matched
		return "\uffff"; //$NON-NLS-1$
	}

	public void writeToShell(String cmd)
	{
		_curCommand = cmd;
		_patterns.update(cmd);
		super.writeToShell(cmd);

	}

}
