/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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
 * Martin Oberhuber (Wind River) - Adapted from LocalHostShell.
 ********************************************************************************/

package org.eclipse.rse.services.ssh.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;

import org.eclipse.rse.services.shells.AbstractHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;
import org.eclipse.rse.services.ssh.ISshSessionProvider;

/**
 * A Shell subsystem for SSH.
 */
public class SshHostShell extends AbstractHostShell {

	private ISshSessionProvider fSessionProvider;
	private Channel fChannel;
	private SshShellOutputReader fStdoutHandler;
	private SshShellOutputReader fStderrHandler;
	private PrintWriter fStdinHandler;
	
	public SshHostShell(ISshSessionProvider sessionProvider, String initialWorkingDirectory, String commandToRun, String encoding, String[] environment) {
		try {
			fSessionProvider = sessionProvider;
		    fChannel = fSessionProvider.getSession().openChannel("shell"); //$NON-NLS-1$

		    ////disable pty mode. This works in jsch-0.1.25 and later only.
		    ////By default, jsch always creates a vt100 connection sized
		    ////80x24 / 640x480 (dimensions can be changed).
		    ////I wonder whether jsch could give us a dumb terminal?
		    //if(fChannel instanceof ChannelShell) {
		    //	((ChannelShell)fChannel).setPty(false);
		    //}

			fStdoutHandler = new SshShellOutputReader(this, new BufferedReader(new InputStreamReader(fChannel.getInputStream())), false);
			fStderrHandler = new SshShellOutputReader(this, null,true);
			fStdinHandler = new PrintWriter(fChannel.getOutputStream());

		    fChannel.connect();
		    writeToShell("cd "+initialWorkingDirectory); //$NON-NLS-1$
		} catch(Exception e) {
			//TODO Forward exception to RSE properly
			e.printStackTrace();
		}
	}

	public boolean isActive() {
		if (fChannel!=null && !fChannel.isEOF()) {
			return true;
		}
		// shell is not active: check for session lost
		Session session = fSessionProvider.getSession();
		if (session!=null && !session.isConnected()) {
			fSessionProvider.handleSessionLost();
		}
		return false;
	}

	public void writeToShell(String command) {
		if (isActive()) {
			fStdinHandler.println(command);
			fStdinHandler.flush();
		}
	}

	public IHostShellOutputReader getStandardOutputReader() {
		return fStdoutHandler;
	}

	public IHostShellOutputReader getStandardErrorReader() {
		return fStderrHandler;
	}

	public void exit() {
		fChannel.disconnect();
	}

}
