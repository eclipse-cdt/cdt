/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River)      - initial API and implementation
 * Anna Dushistova  (MontaVista)      - [170910] Integrate the TM Terminal View with RSE
 * Martin Oberhuber (Wind River)      - [227320] Fix endless loop in SshTerminalShell
 * Yufen Kuo        (MontaVista)      - [274153] Fix pipe closed with RSE
 * Anna Dushistova  (Mentor Graphics) - Returned "session lost" handling to isActive()
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh.terminal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Hashtable;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import org.eclipse.rse.internal.services.ssh.ISshSessionProvider;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.rse.services.terminals.AbstractTerminalShell;
import org.eclipse.rse.services.terminals.ITerminalService;

/**
 * A remote shell connection supporting Streams for I/O.
 */
public class SshTerminalShell extends AbstractTerminalShell {

	private ISshSessionProvider fSessionProvider;
	private Channel fChannel;
	private String fEncoding;
	private InputStream fInputStream;
	private OutputStream fOutputStream;
	private Writer fOutputStreamWriter;
	private int fWidth = 0;
	private int fHeight = 0;
	private static String defaultEncoding = new java.io.InputStreamReader(new java.io.ByteArrayInputStream(new byte[0])).getEncoding();

	/**
	 * Construct a new Terminal connection.
	 *
	 * The SSH channel is immediately connected in the Constructor.
	 *
	 * @param sessionProvider SSH session provider
	 * @param ptyType Terminal type to set, or <code>null</code> if not
	 *            relevant
	 * @param encoding The default encoding to use for initial command.
	 * @param environment Environment array to set, or <code>null</code> if
	 *            not relevant.
	 * @param initialWorkingDirectory initial directory to open the Terminal in.
	 *            Use <code>null</code> or empty String ("") to start in a
	 *            default directory. Empty String will typically start in the
	 *            home directory.
	 * @param commandToRun initial command to send.
	 * @throws SystemMessageException in case anything goes wrong. Channels and
	 *             Streams are all cleaned up again in this case.
	 * @see ITerminalService
	 */
	public SshTerminalShell(ISshSessionProvider sessionProvider, String ptyType, String encoding, String[] environment, String initialWorkingDirectory,
			String commandToRun) throws SystemMessageException {
		try {
			fSessionProvider = sessionProvider;
			fEncoding = encoding;
		    fChannel = fSessionProvider.getSession().openChannel("shell"); //$NON-NLS-1$
			if (ptyType != null && (fChannel instanceof ChannelShell)) {
			    //By default, jsch always creates a vt100 connection sized
			    //80x24 / 640x480 (dimensions can be changed).
		    	((ChannelShell) fChannel).setPtyType(ptyType);
		    }

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
		    			if (fEncoding != null) {
		    				key = recode(key, fEncoding);
							value = recode(value, fEncoding);
		    			}
		    			envTable.put(key, value);
		    		}
		    	}
		    	((ChannelShell) fChannel).setEnv(envTable);
		    }

			fOutputStream = fChannel.getOutputStream();
			fInputStream = fChannel.getInputStream();
		    fChannel.connect();

		    if (fEncoding != null) {
		    	fOutputStreamWriter = new BufferedWriter(new OutputStreamWriter(fOutputStream, encoding));
			} else {
		    	// default encoding == System.getProperty("file.encoding")
				// TODO should try to determine remote encoding if possible
		    	fOutputStreamWriter = new BufferedWriter(new OutputStreamWriter(fOutputStream));
		    }

		    if (initialWorkingDirectory!=null && initialWorkingDirectory.length()>0
		    	&& !initialWorkingDirectory.equals(".") //$NON-NLS-1$
		    	&& !initialWorkingDirectory.equals("Command Shell") //$NON-NLS-1$ //FIXME workaround for bug 153047
		    ) {
			    writeToShell("cd " + PathUtility.enQuoteUnix(initialWorkingDirectory)); //$NON-NLS-1$
		    }
			if (commandToRun != null && commandToRun.length() > 0) {
		    	writeToShell(commandToRun);
		    }
		} catch(Exception e) {
			throw new RemoteFileException("Error creating Terminal", e); //$NON-NLS-1$
		} finally {
			isActive();
		}
	}

	public String getDefaultEncoding() {
		return fEncoding;
	}

	/**
	 * Encode String with requested user encoding, in case it differs from
	 * Platform default encoding.
	 *
	 * @param s String to encode
	 * @param encoding Encoding to use
	 * @return encoded String
	 * @throws UnsupportedEncodingException in case the requested encoding is
	 *             not supported
	 */
	protected String recode(String s, String encoding) throws UnsupportedEncodingException {
		if (encoding == null) {
			return s;
		} else if (encoding.equals(defaultEncoding)) {
			return s;
		}
		// what we want on the wire:
		byte[] bytes = s.getBytes(encoding);
		// what we need to tell Jsch to get this on the wire:
		return new String(bytes, defaultEncoding);
	}

	/*
	 * (non-Javadoc)
	 * @see ITerminalHostShell#getInputStream(Object)
	 */
	public InputStream getInputStream() {
		return fInputStream;
	}

	/*
	 * (non-Javadoc)
	 * @see ITerminalHostShell#getOutputStream(Object)
	 */
	public OutputStream getOutputStream() {
		return fOutputStream;
	}

	/**
	 * Write a command to the shell, honoring specified Encoding. Can only be
	 * done before an outputStream is obtained, since these commands would
	 * interfere with the outputStream.
	 *
	 * @param command Command String to send, or "#break" to send a Ctrl+C
	 *            command.
	 */
	public void writeToShell(String command) throws IOException {
		if (isActive()) {
			if ("#break".equals(command)) { //$NON-NLS-1$
				command = "\u0003"; // Unicode 3 == Ctrl+C //$NON-NLS-1$
			} else {
				command += "\r\n"; //$NON-NLS-1$
			}
			fOutputStreamWriter.write(command);
			fOutputStreamWriter.flush();
		}
	}

	 public void exit() {
		if (fChannel != null) {
			try {
				try {
					getInputStream().close();
				} catch (IOException ioe) {
					/* ignore */
				}
				try {
					getOutputStream().close();
				} catch (IOException ioe) {
					/* ignore */
				}
				fChannel.disconnect();
			} finally {
				fChannel = null;
				isActive();
			}
		}
	}

	 public boolean isActive() {
		if (fChannel != null && !fChannel.isEOF()) {
			return true;
		}
		// shell is not active: check for session lost
		//AD: comment out exit call until we find better solution,
		//see https://bugs.eclipse.org/bugs/show_bug.cgi?id=274153
		//exit();
		Session session = fSessionProvider.getSession();
		if (session != null && !session.isConnected()) {
			fSessionProvider.handleSessionLost();
		}
		return false;
	}

	public boolean isLocalEcho() {
		return false;
	}

	public void setTerminalSize(int newWidth, int newHeight) {
		if (fChannel != null && fChannel instanceof ChannelShell && (newWidth != fWidth || newHeight != fHeight)) {
			// avoid excessive communications due to change size requests by
			// caching previous size
			ChannelShell channelShell = (ChannelShell) fChannel;
			channelShell.setPtySize(newWidth, newHeight, 8 * newWidth, 8 * newHeight);
			fWidth = newWidth;
			fHeight = newHeight;
		}
	}

}
