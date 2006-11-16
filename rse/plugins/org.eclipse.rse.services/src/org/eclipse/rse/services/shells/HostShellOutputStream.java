/*******************************************************************************
 * Copyright (c) 2006 PalmSource, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource)
 * Martin Oberhuber (Wind River) - moved from org.eclipse.rse.remotecdt (bug 161777)
 * Martin Oberhuber (Wind River) - improved Javadoc
 *******************************************************************************/

package org.eclipse.rse.services.shells;

import java.io.IOException;
import java.io.OutputStream;


/**
 * An adapter between the OutputStream and the IHostShell objects.
 * @author Ewa Matejska 
 * @see IHostShell
 * @see java.io.OutputStream
 */
public class HostShellOutputStream extends OutputStream {

	private IHostShell hostShell;
	
	/**
	 * Constructor.
	 * @param hostShell  An instance of the IHostShell class.  
	 * The output will be sent to this instance.
	 */
	public HostShellOutputStream(IHostShell hostShell) {
		this.hostShell = hostShell;
	}
	
	/**
	 * Writes one byte to the shell.
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] b) {
		if(hostShell != null && b != null)
			hostShell.writeToShell(new String(b)); 
	}
	
    /**
     * Writes multiple bytes to the shell.
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len)  {
		if(hostShell != null && b != null)
			hostShell.writeToShell(new String(b, off, len)); 
	}

	/**
	 * Writes one character to the shell.
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		char[] array = { (char) b };
		if(hostShell != null)
			hostShell.writeToShell(new String(array));
	}

}
