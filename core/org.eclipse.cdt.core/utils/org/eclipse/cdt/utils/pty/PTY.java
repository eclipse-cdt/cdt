/**********************************************************************
 * Copyright (c) 2002,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.utils.pty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.core.CCorePlugin;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PTY {

	String slave;
	InputStream in;
	OutputStream out;
	int master;

	private static boolean hasPTY;

	/**
	 * The master fd is use on two streams. We need to wrap the fd
	 * so when stream.close() is call the other stream is disable.
	 */
	public class MasterFD {

		public int getFD() {
			return master;
		}

		public void setFD(int fd) {
			master = fd;
		}
	}

	public PTY() throws IOException {
		if (hasPTY) {
			slave= forkpty();
		}

		if (slave == null) {
			throw new IOException(CCorePlugin.getResourceString("Util.exception.cannotCreatePty")); //$NON-NLS-1$
		}

		in = new PTYInputStream(new MasterFD());
		out = new PTYOutputStream(new MasterFD());
	}
	
	public String getSlaveName() {
		return slave;
	}
	
	public OutputStream getOutputStream() {
		return out;
	}
	
	public InputStream getInputStream() {
		return in;
	}
	
	native String forkpty();

	static {
		try {
			System.loadLibrary("pty"); //$NON-NLS-1$
			hasPTY = true;
		} catch (SecurityException e) {
		} catch (UnsatisfiedLinkError e) {
		}			
	}
	
}
