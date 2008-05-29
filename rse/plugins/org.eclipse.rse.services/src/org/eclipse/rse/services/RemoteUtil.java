/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - added createSocket() from team.cvs.core/Util
 *******************************************************************************/
package org.eclipse.rse.services;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.osgi.util.NLS;

import org.eclipse.rse.internal.services.RSEServicesMessages;

/**
 * Static helper-methods for network access. 
 */
public class RemoteUtil {

	//----------------------------------------------------------------------
	// <copied 
	//    plugin:    org.eclipse.team.cvs.core
	//    class:     org.eclipse.team.internal.ccvs.core.Policy  
	//    copyright: (c) 2000, 2006 IBM Corporation and others />
	//----------------------------------------------------------------------

	/**
	 * Progress Monitor Helper: Checks the passed progress monitor
	 * and throws an {@link OperationCanceledException} when it is
	 * cancelled.
	 * 
	 * @param monitor The ProgressMonitor to be checked
	 */
	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}

	//</copied>
	
	//----------------------------------------------------------------------
	// <copied 
	//    plugin:  org.eclipse.team.cvs
	//    class:   org.eclipse.team.internal.ccvs.core.util.Util
	//    copyright: (c) 2000, 2006 IBM Corporation and others />
	//----------------------------------------------------------------------

	/**
	 * Helper method that will time out when making a socket connection.
	 * This is required because there is no way to provide a timeout value
	 * when creating a socket and in some instances, they don't seem to
	 * timeout at all.
	 * @param host inetaddress to connect to
	 * @param port port to connect to
	 * @param timeout number of seconds for timeout (default=60)
	 * @param monitor progress monitor
	 */
	public static Socket createSocket(final String host, final int port, int timeout, IProgressMonitor monitor) throws UnknownHostException, IOException {
		
		// Start a thread to open a socket
		final Socket[] socket = new Socket[] { null };
		final Exception[] exception = new Exception[] {null };
		final Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					Socket newSocket = new Socket(host, port);
					synchronized (socket) {
						if (Thread.interrupted()) {
							// we we're either cancelled or timed out so just close the socket
							newSocket.close();
						} else {
							socket[0] = newSocket;
						}
					}
				} catch (UnknownHostException e) {
					exception[0] = e;
				} catch (IOException e) {
					exception[0] = e;
				}
			}
		});
		thread.start();
		
		// Wait the appropriate number of seconds
		if (timeout <= 0) timeout = 60;
		for (int i = 0; i < timeout; i++) {
			try {
				// wait for the thread to complete or 1 second, which ever comes first
				thread.join(1000);
			} catch (InterruptedException e) {
				// I think this means the thread was interupted but not necessarily timed out
				// so we don't need to do anything
			}
			synchronized (socket) {
				// if the user cancelled, clean up before preempting the operation
				if (monitor.isCanceled()) {
					if (thread.isAlive()) {
						thread.interrupt();
					}
					if (socket[0] != null) {
						socket[0].close();
					}
					// this method will throw the proper exception
					checkCanceled(monitor);
				}
			}
		}
		// If the thread is still running (i.e. we timed out) signal that it is too late
		synchronized (socket) {
			if (thread.isAlive()) {
				thread.interrupt();
			}
		}
		if (exception[0] != null) {
			if (exception[0] instanceof UnknownHostException)
				throw (UnknownHostException)exception[0];
			else
				throw (IOException)exception[0];
		}
		if (socket[0] == null) {
			throw new InterruptedIOException(NLS.bind(RSEServicesMessages.Socket_timeout, new String[] { host })); 
		}
		return socket[0];
	}

	//</copied>
	
}
