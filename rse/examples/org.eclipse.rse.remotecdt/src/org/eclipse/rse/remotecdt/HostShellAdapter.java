/*******************************************************************************
 * Copyright (c) 2006 PalmSource, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource)
 *******************************************************************************/

package org.eclipse.rse.remotecdt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellChangeEvent;
import org.eclipse.rse.services.shells.IHostShellOutputListener;

public class HostShellAdapter extends Process implements
IHostShellOutputListener {

	private IHostShell hostShell;
	private PipedInputStream inputStream = null;
	private PipedInputStream errorStream = null;
	private HostShellOutputStream outputStream = null;
	
	private PipedOutputStream hostShellInput = null;
	private PipedOutputStream hostShellError = null;
	
	
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
	
	public synchronized void destroy() {
		hostShell.exit();
		notifyAll();
		try {
			inputStream.close();
			errorStream.close();
			outputStream.close();
		} catch (IOException e) {
			// Ignore
		}	
	}

	public int exitValue() {
		if(hostShell.isActive())
			throw new IllegalThreadStateException();
		// No way to tell what the exit value was.
		return 0;
	}

	public InputStream getErrorStream() {
		return errorStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public synchronized int waitFor() throws InterruptedException {
		while(hostShell.isActive())
			wait();
		return 0;
	}
	
	public void shellOutputChanged(IHostShellChangeEvent event) {
		Object[] input = event.getLines();
		OutputStream outputStream = event.isError() ? hostShellError : hostShellInput;
		try {
		for(int i = 0; i < input.length; i++) {
			outputStream.write(input[i].toString().getBytes());
			outputStream.write('\n');
			outputStream.flush();
		}
		} catch(IOException e) {
			// Ignore
		}
	}

}
