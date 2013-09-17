/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
 * David McKnight   (IBM)        - [191599] Use the remote encoding specified in the host property page
 * David McKnight   (IBM)        - [196301] Check that the remote encoding isn't null before using it
 * Martin Oberhuber (Wind River) - [204744] Honor encoding in SSH command input field
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable
 * Anna Dushistova  (MontaVista) - adapted from SshHostShell
 * Anna Dushistova  (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 * Anna Dushistova  (MontaVista) - [258720] SshHostShell fails to run command if initialWorkingDirectory supplied
 * Rob Stryker (JBoss) - [335059] TerminalServiceShellOutputReader logs error when hostShell.exit() is called
 * Martin Oberhuber (Wind River) - [356132] wait for initial output
 * Ioana Grigoropol (Intel)      - [411343] Provide access to readers in host shell
 *******************************************************************************/

package org.eclipse.rse.internal.services.shells;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.shells.AbstractHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;
import org.eclipse.rse.services.terminals.ITerminalShell;

/**
 * @since 3.1
 */
public class TerminalServiceHostShell extends AbstractHostShell {

	public static final String SHELL_INVOCATION = ">"; //$NON-NLS-1$

	ITerminalShell fTerminalShell;
	BufferedReader fBufReader;
	private TerminalServiceShellOutputReader fStdoutHandler;
	
	private TerminalServiceShellOutputReader fStderrHandler;
	
	private TerminalServiceShellWriterThread fShellWriter;

	public TerminalServiceHostShell(ITerminalShell terminalShell,
			String initialWorkingDirectory, String commandToRun,
			String[] environment) {
		try {
			fTerminalShell = terminalShell;
			String encoding = fTerminalShell.getDefaultEncoding();

			if (encoding != null) {
				fBufReader = new BufferedReader(new InputStreamReader(fTerminalShell
						.getInputStream(), encoding)); 
			} else {
				fBufReader = new BufferedReader(new InputStreamReader(fTerminalShell
								.getInputStream()));
			}
			//bug 356132: wait for initial output before sending any command
			//FIXME this should likely move into the TerminalServiceShellWriterThread, so wait can be canceled
			fBufReader.mark(1);
			fBufReader.read();
			fBufReader.reset();
			
			fStdoutHandler = new TerminalServiceShellOutputReader(this, fBufReader, false);
			fStderrHandler = new TerminalServiceShellOutputReader(this, null, true);
			OutputStream outputStream = fTerminalShell.getOutputStream();
			if (encoding != null) {
				// use specified encoding
				Charset cs = Charset.forName(encoding);
				PrintWriter outputWriter = new PrintWriter(
						new OutputStreamWriter(outputStream, cs));
				fShellWriter = new TerminalServiceShellWriterThread(
						outputWriter);
			} else {
				PrintWriter outputWriter = new PrintWriter(outputStream);
				fShellWriter = new TerminalServiceShellWriterThread(
						outputWriter);
			}

			if (initialWorkingDirectory != null
					&& initialWorkingDirectory.length() > 0
					&& !initialWorkingDirectory.equals(".") //$NON-NLS-1$
					&& !initialWorkingDirectory.equals("Command Shell") //$NON-NLS-1$ //FIXME workaround for bug 153047
			) {
				writeToShell("cd " + PathUtility.enQuoteUnix(initialWorkingDirectory)); //$NON-NLS-1$
			} 
			if (SHELL_INVOCATION.equals(commandToRun)) {
				writeToShell(getPromptCommand());
			} else if (commandToRun != null && commandToRun.length() > 0) {
				writeToShell(commandToRun);
			}

		} catch (Exception e) {
			// TODO [209043] Forward exception to RSE properly
			e.printStackTrace();
			if (fShellWriter != null) {
				fShellWriter.stopThread();
				fShellWriter = null;
			}
			if (fStderrHandler != null) {
				fStderrHandler.interrupt();
				fStderrHandler = null;
			}
			if (fStdoutHandler != null) {
				fStdoutHandler.interrupt();
				fStdoutHandler = null;
			}
		}

	}

	public void exit() {
		if (fShellWriter != null) {
			fShellWriter.stopThread();
		}
		if( fStderrHandler != null ) {
			fStderrHandler.stopThread();
		}
		if( fStdoutHandler!= null ) {
			fStdoutHandler.stopThread();
		}
		fTerminalShell.exit();
	}

	public IHostShellOutputReader getStandardErrorReader() {
		return fStderrHandler;
	}

	public IHostShellOutputReader getStandardOutputReader() {
		return fStdoutHandler;
	}

	public boolean isActive() {
		return fTerminalShell.isActive();
	}

	private static final Pattern cdCommands = Pattern
			.compile("\\A\\s*(cd|chdir|ls)\\b"); //$NON-NLS-1$

	public void writeToShell(String command) {
		if (isActive()) {
			if ("#break".equals(command)) { //$NON-NLS-1$
				command = "\u0003"; //Unicode 3 == Ctrl+C //$NON-NLS-1$
			} else if (cdCommands.matcher(command).find()) {
				command += "\r\n" + getPromptCommand(); //$NON-NLS-1$
			}
			if (!fShellWriter.sendCommand(command)) {
				// exception occurred: terminate writer thread, cancel
				// connection
				exit();
				isActive();
			}
		}
	}

	public String getPromptCommand() {
		return "echo $PWD'>'"; //$NON-NLS-1$
	}

	public BufferedReader getReader(boolean isErrorReader) {
		return fBufReader;
	}
}
