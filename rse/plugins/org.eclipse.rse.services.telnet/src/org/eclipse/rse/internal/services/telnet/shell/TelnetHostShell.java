/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
 * Martin Oberhuber (Wind River) - Adapted from LocalHostShell.
 * Sheldon D'souza  (Celunite)   - Adapted from SshHostShell
 * Sheldon D'souza  (Celunite)   - [187301] support multiple telnet shells
 * David McKnight   (IBM)        - [191599] Use the remote encoding specified in the host property page
 * Martin Oberhuber (Wind River) - [194466] Fix shell terminated state when stream is closed
 * Anna Dushistova  (MontaVista) - [258720] SshHostShell fails to run command if initialWorkingDirectory supplied
 *******************************************************************************/
package org.eclipse.rse.internal.services.telnet.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import org.apache.commons.net.telnet.TelnetClient;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.internal.services.telnet.ITelnetSessionProvider;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.shells.AbstractHostShell;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;

public class TelnetHostShell extends AbstractHostShell implements IHostShell {

	public static final String SHELL_INVOCATION = ">"; //$NON-NLS-1$

	private ITelnetSessionProvider fSessionProvider;
	private TelnetShellOutputReader fStdoutHandler;
	private TelnetShellOutputReader fStderrHandler;
	private TelnetShellWriterThread fShellWriter;
	private TelnetClient fTelnetClient;
	
	public TelnetHostShell(ITelnetSessionProvider sessionProvider, String initialWorkingDirectory, String commandToRun, String encoding, String[] environment) {
		try {
			fSessionProvider = sessionProvider;
			
			fTelnetClient = fSessionProvider.makeNewTelnetClient(new NullProgressMonitor());

		    if (encoding != null)
		    {
		    	fStdoutHandler = new TelnetShellOutputReader(this, new BufferedReader(new InputStreamReader(fTelnetClient.getInputStream(), encoding)), false);
		    }
		    else
		    {
		    	fStdoutHandler = new TelnetShellOutputReader(this, new BufferedReader(new InputStreamReader(fTelnetClient.getInputStream())), false);
		    }

			fStderrHandler = new TelnetShellOutputReader(this, null,true);
			OutputStream outputStream = fTelnetClient.getOutputStream();
			//TODO check if encoding or command to execute needs to be considered
			//If a command is given, it might be possible to do without a Thread
			//Charset cs = Charset.forName(encoding);
			//PrintWriter outputWriter = new PrintWriter(
			//		new BufferedWriter(new OutputStreamWriter(outputStream,cs)));
			PrintWriter outputWriter = new PrintWriter(outputStream);
			fShellWriter = new TelnetShellWriterThread(outputWriter);
			
		    if (initialWorkingDirectory!=null && initialWorkingDirectory.length()>0 
		    	&& !initialWorkingDirectory.equals(".") //$NON-NLS-1$
		    	&& !initialWorkingDirectory.equals("Command Shell") //$NON-NLS-1$ //FIXME workaround for bug 153047
		    ) { 
			    writeToShell("cd "+PathUtility.enQuoteUnix(initialWorkingDirectory)); //$NON-NLS-1$
		    } 
		    if (SHELL_INVOCATION.equals(commandToRun)) {
		    	writeToShell(getPromptCommand());
		    } else if(commandToRun!=null && commandToRun.length()>0) {
		    	writeToShell(commandToRun);
		    }
		} catch(Exception e) {
			//TODO [209043] Forward exception to RSE properly
			e.printStackTrace();
			if (fShellWriter!=null) {
				fShellWriter.stopThread();
				fShellWriter = null;
			}
			if (fStderrHandler!=null) {
				fStderrHandler.interrupt();
				fStderrHandler = null;
			}
			if (fStdoutHandler!=null) {
				fStdoutHandler.interrupt();
				fStdoutHandler = null;
			}
		}
	}
	
	public String getPromptCommand() {
		return "echo $PWD'>'"; //$NON-NLS-1$
	}
	
	public void exit() {
		if (fShellWriter.isAlive()) {
			fShellWriter.stopThread();
		}
		try {
			//TODO disconnect should better be done via the ConnectorService!!
			//Because like we do it here, the connector service is not notified!
			if (fTelnetClient!=null) {
				synchronized(fTelnetClient) {
					if (fTelnetClient.isConnected())
						fTelnetClient.disconnect();
				}
			}
		} catch (IOException e) {
		}

	}

	public IHostShellOutputReader getStandardOutputReader() {
		return fStdoutHandler;
	}

	public IHostShellOutputReader getStandardErrorReader() {
		return fStderrHandler;
	}

	public boolean isActive() {
		if (fTelnetClient!=null && fTelnetClient.isConnected() && !fStdoutHandler.isFinished()) {
			return true;
		}
		// shell is not active: check for session lost
		exit();
		
		////MOB: Telnet sessions are really independent of each other.
		////So if one telnet session disconnects, it must not disconnect
		////the other sessions.
		//if (fTelnetClient!=null && !fTelnetClient.isConnected()) {
		//	fSessionProvider.handleSessionLost();
		//}
		return false;
	}

	private static final Pattern cdCommands = Pattern.compile("\\A\\s*(cd|chdir|ls)\\b"); //$NON-NLS-1$
	
	public void writeToShell(String command) {
		if (isActive()) {
			if ("#break".equals(command)) { //$NON-NLS-1$
				command = "\u0003"; //Unicode 3 == Ctrl+C //$NON-NLS-1$
			} else if (cdCommands.matcher(command).find()) {
				command += "\r\n" + getPromptCommand(); //$NON-NLS-1$
			}
			if (!fShellWriter.sendCommand(command)) {
				//exception occurred: terminate writer thread, cancel connection
				exit();
				isActive();
			}
		}

	}

}
