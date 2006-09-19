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

public class HostShellOutputStream extends OutputStream {

	private IHostShell hostShell;
	
	public HostShellOutputStream(IHostShell hostShell) {
		this.hostShell = hostShell;
	}
	
	public void write(byte[] b) {
		if(hostShell != null && b != null)
			hostShell.writeToShell(new String(b)); 
	}
	
	public void write(byte[] b, int off, int len)  {
		if(hostShell != null && b != null)
			hostShell.writeToShell(new String(b, off, len)); 
	}
 		
	public void write(int b) throws IOException {
		char[] array = { (char) b };
		if(hostShell != null)
			hostShell.writeToShell(new String(array));
	}

}
