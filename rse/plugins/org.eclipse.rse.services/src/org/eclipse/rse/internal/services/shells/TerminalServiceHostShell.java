/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 * Teodor Madan (Freescale)      - [467833] Wait shell initialization
 *******************************************************************************/

package org.eclipse.rse.internal.services.shells;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.rse.internal.services.Activator;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.shells.AbstractHostShell;
import org.eclipse.rse.services.shells.IHostOutput;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;
import org.eclipse.rse.services.shells.IHostShellOutputReader;
import org.eclipse.rse.services.terminals.ITerminalShell;

/**
 * @since 3.1
 */
public class TerminalServiceHostShell extends AbstractHostShell {
	private static final String RSE_SHELL_READY_PING = "RSE_SHELL_READY_PING"; //$NON-NLS-1$
	private static final String ECLIPSE_TEST_KEY = "_ping"; //$NON-NLS-1$

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
			// if stdout stream is closed, pass the shell writer to be closed as well
			fStdoutHandler = new TerminalServiceShellOutputReader(this, fBufReader, false, fShellWriter);
			fStderrHandler = new TerminalServiceShellOutputReader(this, null, true);

			int pingMsec = getReadyPingMsec();
			if (SHELL_INVOCATION.equals(commandToRun) && pingMsec > 0) {
				doReadyPing("echo " + ECLIPSE_TEST_KEY + "'>'", ECLIPSE_TEST_KEY, pingMsec, 10); //$NON-NLS-1$ //$NON-NLS-2$
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

	/**
	 * Perform a test if remote shell can execute commands. Will send <code>pingCmd</code> command to remote shell for execution
	 * until a line starting with <code>expectedResponse</code> is received. Ping command will be sent up to <code>maxPing</code>
	 * times waiting <code>ttywait</code> milliseconds between pings.
	 *
	 * @param pingCmd - ping command. e.g. <code>"echo test"</code>
	 * @param expectedResponse - exepcted result, e.g. <code>"test"</code>
	 * @param msecPerPing milliseconds to wait between each ping command
	 * @param maxPing maximum number of attempts
	 *
	 * @return <code>true</code> if expected response has been received or <code>false</code> if timeout was raised before receiving
	 * expected response.
	 *  <br>When method returns <code>false</code>, ping commands still might have been executed and the response can arrive later
	 */
	public boolean doReadyPing(String pingCmd, final String expectedResponse, int msecPerPing, int maxPing) {
		// wait for handshake:
		//		send repeatable commands
		//			 --> echo <eclipse_key>
		//		until receiving a line that starts with the key,
		//			<-- <eclipse_key>
		// this differentiates from a plan echo that will contain "echo" command as well.

		final boolean[] received_handshake = new boolean[1];
		received_handshake[0] = false;
		final Object lock = new Object();
		IHostShellOutputListener echoListener = new IHostShellOutputListener() {
			public void shellOutputChanged(IHostShellChangeEvent event) {
				IHostOutput[] lines = event.getLines();
				for (int i = lines.length-1; i>=0; i--) {
					String line = lines[i].getString();
					if (line.startsWith(expectedResponse)) {
						synchronized (lock) {
							// we are done waiting;
							received_handshake[0] = true;
							lock.notifyAll();
							break;
						}
					}
				}
			}
		};
		fStdoutHandler.addOutputListener(echoListener);
		int ping = 1;
		do {
			// send periodically the handshake:
			writeToShell(pingCmd);
			synchronized (lock) {
				try {
					lock.wait(msecPerPing);
				} catch (InterruptedException ex) {
					break;
				}
				if (received_handshake[0]) {
					break;
				}
			}
			// limit number of pings in case of fundamental issue
		} while (!fStdoutHandler.isFinished() && ping++ <= maxPing);

		// remove echo listener from output handler
		fStdoutHandler.removeOutputListener(echoListener);
		return received_handshake[0];
	}

	/**
	 * @return msec to wait between shell ready ping commands; value 0 is for no ping
	 */
	protected int getReadyPingMsec() {
		int ttyWait = 0; //default is to not wait after receiving characters
		// See bug 467899:
		//  Until an API is created to read RSE service properties use system properties to enable the behavior
		String waitVal = System.getProperty(RSE_SHELL_READY_PING);
		if (waitVal != null) {
			try {
				ttyWait = Integer.parseInt(waitVal);
				// limit the lower limit of the ping to avoid spamming target ssh server.
				if (ttyWait < 200) {
					ttyWait = 200;
				}
			} catch (NumberFormatException e) {
				// ignore invalid value
				IStatus status = new Status(IStatus.WARNING, Activator.PLUGIN_ID,
						RSE_SHELL_READY_PING + " property should be an integer. Actually is '" + waitVal + "'", null); //$NON-NLS-1$ //$NON-NLS-2$
				Activator.getDefault().getLog().log(status);
			}
		}
		return ttyWait;
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
