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
 * David McKnight   (IBM)        - [191599] Use the remote encoding specified in the host property page
 * David McKnight   (IBM)        - [196301] Check that the remote encoding isn't null before using it
 * Martin Oberhuber (Wind River) - [204744] Honor encoding in SSH command input field
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable
 * Anna Dushistova  (MontaVista) - [258720] SshHostShell fails to run command if initialWorkingDirectory supplied
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import org.eclipse.rse.internal.services.ssh.ISshSessionProvider;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.shells.AbstractHostShell;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;

/**
 * A Shell subsystem for SSH.
 */
public class SshHostShell extends AbstractHostShell implements IHostShell {

	public static final String SHELL_INVOCATION = ">"; //$NON-NLS-1$

	private ISshSessionProvider fSessionProvider;
	private Channel fChannel;
	private SshShellOutputReader fStdoutHandler;
	private SshShellOutputReader fStderrHandler;
	private SshShellWriterThread fShellWriter;

	public SshHostShell(ISshSessionProvider sessionProvider, String initialWorkingDirectory, String commandToRun, String encoding, String[] environment) {
		try {
			fSessionProvider = sessionProvider;
		    fChannel = fSessionProvider.getSession().openChannel("shell"); //$NON-NLS-1$

		    ////disable pty mode. This works in jsch-0.1.25 and later only.
		    ////By default, jsch always creates a vt100 connection sized
		    ////80x24 / 640x480 (dimensions can be changed).
		    ////I wonder whether jsch could give us a dumb terminal?
		    //if(commandToRun!=null && !commandToRun.equals(SHELL_INVOCATION) & (fChannel instanceof ChannelShell)) {
		    //	((ChannelShell)fChannel).setPty(false);
		    //}

		    //Try to set the user environment. On most sshd configurations, this will
		    //not work since in sshd_config, PermitUserEnvironment and AcceptEnv
		    //settings are disabled. Still, it's worth a try.
		    if (environment!=null && environment.length>0 && fChannel instanceof ChannelShell) {
		    	Hashtable envTable=new Hashtable();
		    	for(int i=0; i<environment.length; i++) {
		    		String curStr=environment[i];
		    		int curLen=environment[i].length();
		    		int idx = curStr.indexOf('=');
		    		if (idx>0 && idx<curLen-1) {
		    			String key=environment[i].substring(0, idx);
		    			String value=environment[i].substring(idx+1, curLen);
		    			envTable.put(key, value);
		    		}
		    	}
		    	((ChannelShell)fChannel).setEnv(envTable);
		    }

		    if (encoding != null)
		    {
		    	fStdoutHandler = new SshShellOutputReader(this, new BufferedReader(new InputStreamReader(fChannel.getInputStream(), encoding)), false);
		    }
		    else
		    {
		    	// default encoding - same as
				// System.getProperty("file.encoding")
				// TODO should try to determine remote encoding if possible
		    	fStdoutHandler = new SshShellOutputReader(this, new BufferedReader(new InputStreamReader(fChannel.getInputStream())), false);
		    }
			fStderrHandler = new SshShellOutputReader(this, null,true);
			OutputStream outputStream = fChannel.getOutputStream();
			if (encoding!=null) {
				//use specified encoding
				Charset cs = Charset.forName(encoding);
				PrintWriter outputWriter = new PrintWriter(
						new OutputStreamWriter(outputStream,cs));
				fShellWriter = new SshShellWriterThread(outputWriter);
			} else {
				PrintWriter outputWriter = new PrintWriter(outputStream);
				fShellWriter = new SshShellWriterThread(outputWriter);
			}
		    fChannel.connect();
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

	/**
	 * Connect to remote system and launch Threads for the shell as needed.
	 * 
	 * @param monitor progress monitor for long-running operation
	 */
	protected void start(IProgressMonitor monitor)
	{
		//TODO Move stuff from constructor to here
		//TODO Set up environment variables for proper prompting, e.g. like dstore
		//varTable.put("PS1","$PWD/>");
		//varTable.put("COLUMNS","256");
		//alias ls='ls -1'
	}

	public boolean isActive() {
		if (fChannel!=null && !fChannel.isEOF()) {
			return true;
		}
		// shell is not active: check for session lost
		exit();
		Session session = fSessionProvider.getSession();
		if (session!=null && !session.isConnected()) {
			fSessionProvider.handleSessionLost();
		}
		return false;
	}

	private static final Pattern cdCommands = Pattern.compile("\\A\\s*(cd|chdir|ls)\\b"); //$NON-NLS-1$

	public String getPromptCommand() {
		return "echo $PWD'>'"; //$NON-NLS-1$
	}

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

	public IHostShellOutputReader getStandardOutputReader() {
		return fStdoutHandler;
	}

	public IHostShellOutputReader getStandardErrorReader() {
		return fStderrHandler;
	}

	public void exit() {
		if (fShellWriter!=null) {
			fShellWriter.stopThread();
		}
		if (fChannel!=null) {
			fChannel.disconnect();
		}
	}

}
