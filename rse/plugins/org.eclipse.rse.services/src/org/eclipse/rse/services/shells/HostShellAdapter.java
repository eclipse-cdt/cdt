/*******************************************************************************
 * Copyright (c) 2006 PalmSource, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource) - initial version
 * Martin Oberhuber (Wind River) - adapt to IHostOutput API (bug 161773, 158312)
 * Martin Oberhuber (Wind River) - moved from org.eclipse.rse.remotecdt (bug 161777)
 *******************************************************************************/

package org.eclipse.rse.services.shells;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


/**
 * This class represents a host shell process. It does not 
 * represent one process running in the shell.  This means 
 * that the output of multiple shell commands will be returned
 * until the shell exits. 
 * @author Ewa Matejska
 *
 */
public class HostShellAdapter extends Process implements
IHostShellOutputListener {

	private IHostShell hostShell;
	private PipedInputStream inputStream = null;
	private PipedInputStream errorStream = null;
	private HostShellOutputStream outputStream = null;
	
	private PipedOutputStream hostShellInput = null;
	private PipedOutputStream hostShellError = null;
	
	/**
	 * 
	 * @param hostShell  An instance of the IHostShell class.
	 * @throws java.io.IOException
	 */
	public HostShellAdapter(IHostShell hostShell) throws java.io.IOException {
		this.hostShell = hostShell;
		hostShellInput = new PipedOutputStream();
		hostShellError = new PipedOutputStream();
		inputStream = new PipedInputStream(hostShellInput);
		errorStream = new PipedInputStream(hostShellError);
		outputStream = new HostShellOutputStream(hostShell);
		this.hostShell.getStandardOutputReader().addOutputListener(this);
		this.hostShell.getStandardErrorReader().addOutputListener(this);
	}
	
	/**
	 *  Exits the shell.
	 */
	public synchronized void destroy() {
		hostShell.exit();
		notifyAll();
		try {
			hostShellInput.close();
			hostShellError.close();
			inputStream.close();
			errorStream.close();
			outputStream.close();
		} catch (IOException e) {
			// Ignore
		}	
	}

	/**
	 *  There is no relevant exit value to return when the shell exits.
	 *  This always returns 0.
	 */
	public synchronized int exitValue() {
		if(hostShell.isActive())
			throw new IllegalThreadStateException();
		// No way to tell what the exit value was.
		return 0;
	}

	/**
	 *  Returns the error stream of the shell.
	 */
	public InputStream getErrorStream() {
		return errorStream;
	}

	/**
	 * Returns the input stream for the shell.
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Returns the output stream for the shell.
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 *  Waits for the shell to exit.
	 */
	public synchronized int waitFor() throws InterruptedException {
		
		while(hostShell.isActive()) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				// ignore because we're polling to see if shell is still active.
			}
		}
		
		try {
			// Wait a second to try to get some more output from the target shell before closing.
			wait(1000);
			// Allow for the data from the stream to be read if it's available
			if (inputStream.available() != 0 || errorStream.available() != 0)
				throw new InterruptedException();
	
			hostShellInput.close();
			hostShellError.close();
			inputStream.close();
			errorStream.close();
			outputStream.close();
		} catch (IOException e) {
			// Ignore
		}
		return 0;
	}
	
	public void shellOutputChanged(IHostShellChangeEvent event) {
		IHostOutput[] input = event.getLines();
		OutputStream outputStream = event.isError() ? hostShellError : hostShellInput;
		try {
		for(int i = 0; i < input.length; i++) {
			outputStream.write(input[i].getString().getBytes());
			outputStream.write('\n');
			outputStream.flush();
		}
		} catch(IOException e) {
			// Ignore
		}
	}

}
