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
import java.io.OutputStream;

import org.eclipse.rse.services.shells.IHostShell;

/**
 * An adapter between the OutputStream and the IHostShell objects.
 * @author Ewa Matejska 
 * @see IHostShell
 * @see java.io.OutputStream
 */
public class HostShellOutputStream extends OutputStream {

	private IHostShell hostShell;
	
	/**
	 * @param hostShell  An instance of the IHostShell class.  
	 * The output will be sent to this instance.
	 */
	public HostShellOutputStream(IHostShell hostShell) {
		this.hostShell = hostShell;
	}
	
	/**
	 * Writes one byte to the shell.
	 */
	public void write(byte[] b) {
		if(hostShell != null && b != null)
			hostShell.writeToShell(new String(b)); 
	}
	
    /**
     * Writes multiple bytes to the shell.
     */
	public void write(byte[] b, int off, int len)  {
		if(hostShell != null && b != null)
			hostShell.writeToShell(new String(b, off, len)); 
	}

	/**
	 * Writes one character to the shell.
	 */
	public void write(int b) throws IOException {
		char[] array = { (char) b };
		if(hostShell != null)
			hostShell.writeToShell(new String(array));
	}

}
