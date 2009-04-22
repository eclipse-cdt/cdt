/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Anna Dushistova  (MontaVista) - [170910] Integrate the TM Terminal View with RSE
 * Martin Oberhuber (Wind River) - [227320] Fix endless loop in TelnetTerminalShell
 * Anna Dushistova  (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 * Martin Oberhuber (Wind River) - [267402] [telnet] "launch shell" takes forever
 * Anna Dushistova  (MontaVista) - [267474] [rseterminal][telnet] Notify the remote when terminal window size changes
 *******************************************************************************/

package org.eclipse.rse.internal.services.telnet.terminal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.commons.net.io.ToNetASCIIInputStream;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetOption;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.internal.services.telnet.ITelnetSessionProvider;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.rse.services.terminals.AbstractTerminalShell;
import org.eclipse.rse.services.terminals.ITerminalService;

/**
 * A remote shell connection supporting Streams for I/O.
 * 
 * @since 2.0
 */
public class TelnetTerminalShell extends AbstractTerminalShell {

	private ITelnetSessionProvider fSessionProvider;
	private TelnetClient fTelnetClient;
	private String fEncoding;
	private EOFDetectingInputStreamWrapper fInputStream;
	private OutputStream fOutputStream;
	private Writer fOutputStreamWriter;
	private int fWidth = 0;
	private int fHeight = 0;
	private static String defaultEncoding = new java.io.InputStreamReader(
			new java.io.ByteArrayInputStream(new byte[0])).getEncoding();

	/**
	 * Construct a new Terminal connection.
	 * 
	 * The SSH channel is immediately connected in the Constructor.
	 * 
	 * @param sessionProvider
	 *            SSH session provider
	 * @param ptyType
	 *            Terminal type to set, or <code>null</code> if not relevant
	 * @param encoding
	 *            The default encoding to use for initial command.
	 * @param environment
	 *            Environment array to set, or <code>null</code> if not
	 *            relevant.
	 * @param initialWorkingDirectory
	 *            initial directory to open the Terminal in. Use
	 *            <code>null</code> or empty String ("") to start in a default
	 *            directory. Empty String will typically start in the home
	 *            directory.
	 * @param commandToRun
	 *            initial command to send.
	 * @throws SystemMessageException
	 *             in case anything goes wrong. Channels and Streams are all
	 *             cleaned up again in this case.
	 * @see ITerminalService
	 */
	public TelnetTerminalShell(ITelnetSessionProvider sessionProvider,
			String ptyType, String encoding, String[] environment,
			String initialWorkingDirectory, String commandToRun)
			throws SystemMessageException {
		try {
			fSessionProvider = sessionProvider;
			boolean onUNIX = System.getProperty("os.name").toLowerCase()//$NON-NLS-1$
					.startsWith("unix")//$NON-NLS-1$
					|| System.getProperty("os.name").toLowerCase().startsWith( //$NON-NLS-1$
							"linux");//$NON-NLS-1$
			fEncoding = encoding;
			if (ptyType == null) {
				fTelnetClient = new TelnetClient();
			} else {
				fTelnetClient = new TelnetClient(ptyType);
				fTelnetClient.addOptionHandler(new TerminalTypeOptionHandler(
						ptyType, true, true, true, true));
			}
			// request remote echo, but accept local if desired
			fTelnetClient.addOptionHandler(new EchoOptionHandler(false, true,
					true, true));
			fTelnetClient.addOptionHandler(new SuppressGAOptionHandler(true,
					true, true, true));
			fTelnetClient.addOptionHandler(new WindowSizeOptionHandler(fWidth,
					fHeight, true, true, true, true));
			fTelnetClient = fSessionProvider.loginTelnetClient(fTelnetClient,
					new NullProgressMonitor());
			fOutputStream = fTelnetClient.getOutputStream();
			if (onUNIX)
				fInputStream = new EOFDetectingInputStreamWrapper(
						new ToNetASCIIInputStream(fTelnetClient
								.getInputStream()));
			else
				fInputStream = new EOFDetectingInputStreamWrapper(fTelnetClient
						.getInputStream());
			if (fEncoding != null) {
				fOutputStreamWriter = new BufferedWriter(
						new OutputStreamWriter(fOutputStream, encoding));
			} else {
				// default encoding == System.getProperty("file.encoding")
				// TODO should try to determine remote encoding if possible
				fOutputStreamWriter = new BufferedWriter(
						new OutputStreamWriter(fOutputStream));
			}

			if (initialWorkingDirectory != null
					&& initialWorkingDirectory.length() > 0
					&& !initialWorkingDirectory.equals(".") //$NON-NLS-1$
					&& !initialWorkingDirectory.equals("Command Shell") //$NON-NLS-1$ //FIXME workaround for bug 153047
			) {
				writeToShell("cd " + PathUtility.enQuoteUnix(initialWorkingDirectory)); //$NON-NLS-1$
			}
			if (commandToRun != null && commandToRun.length() > 0) {
				writeToShell(commandToRun);
			}
		} catch (Exception e) {
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
	 * @param s
	 *            String to encode
	 * @param encoding
	 *            Encoding to use
	 * @return encoded String
	 * @throws UnsupportedEncodingException
	 *             in case the requested encoding is not supported
	 */
	protected String recode(String s, String encoding)
			throws UnsupportedEncodingException {
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
	 * 
	 * @see ITerminalHostShell#getInputStream(Object)
	 */
	public InputStream getInputStream() {
		return fInputStream;
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * @param command
	 *            Command String to send, or "#break" to send a Ctrl+C command.
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
		if (fTelnetClient != null) {
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
					ioe.printStackTrace();
				}
				try {
					// TODO disconnect should better be done via the
					// ConnectorService!!
					// Because like we do it here, the connector service is not
					// notified!
					synchronized (fTelnetClient) {
						if (fTelnetClient.isConnected())
							fTelnetClient.disconnect();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} finally {
				fTelnetClient = null;
				isActive();
			}
		}
	}

	public boolean isActive() {
		if (fTelnetClient != null && fTelnetClient.isConnected()
				&& !isDisconnected()) {
			return true;
		}
		// shell is not active: check for session lost
		exit();

		// //MOB: Telnet sessions are really independent of each other.
		// //So if one telnet session disconnects, it must not disconnect
		// //the other sessions.
		// if (fTelnetClient!=null && !fTelnetClient.isConnected()) {
		// fSessionProvider.handleSessionLost();
		// }
		return false;
	}

	private boolean isDisconnected() {
		return fInputStream.isEOF();
	}

	public boolean isLocalEcho() {
		return fTelnetClient.getLocalOptionState(TelnetOption.ECHO);
	}

	public void setTerminalSize(int newWidth, int newHeight) {
		if (fTelnetClient != null
				&& (newWidth != fWidth || newHeight != fHeight)) {
			// avoid excessive communications due to change size requests by
			// caching previous size
			synchronized (fTelnetClient) {
				try {
					fTelnetClient.deleteOptionHandler(TelnetOption.WINDOW_SIZE);
					fTelnetClient.addOptionHandler(new WindowSizeOptionHandler(
							newWidth, newHeight, true, true, true, true));
				} catch (InvalidTelnetOptionException e) {
					e.printStackTrace();
				}
			}
			fWidth = newWidth;
			fHeight = newHeight;
		}
	}

}
