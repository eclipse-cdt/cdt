/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.pty;

import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;

/**
 * PTY
 * pseudo terminal code.
 */
public class PTY {

	String slave;
	PTYInputStream in;
	PTYOutputStream out;
	/**
	 * NOTE: Field access by the native layer do not refactor.
	 */
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

		void setFD(int fd) {
			master = fd;
		}
	}

	public PTY() throws IOException {
		if (hasPTY) {
			slave= openMaster();
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

	public MasterFD getMasterFD() {
		return new MasterFD();
	}

	public PTYOutputStream getOutputStream() {
		return out;
	}
	
	public PTYInputStream getInputStream() {
		return in;
	}

	public static boolean isSupported() {
		return hasPTY;
	}

	native String openMaster();

	static {
		try {
			System.loadLibrary("pty"); //$NON-NLS-1$
			hasPTY = true;
		} catch (SecurityException e) {
			// Comment out it worries the users too much
			//CCorePlugin.log(e);
		} catch (UnsatisfiedLinkError e) {
			// Comment out it worries the users too much
			//CCorePlugin.log(e);
		}			
	}
	
}
