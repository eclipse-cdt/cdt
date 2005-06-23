/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.utils.pty.PTY;

/**
 * Adapt the PTY code to IMITTY
 * 
 */
public class MITTYAdapter implements IMITTY {

	PTY fPty;

	public MITTYAdapter(PTY pty) {
		fPty = pty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.IMITTY#getSlaveName()
	 */
	public String getSlaveName() {
		return fPty.getSlaveName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.IMITTY#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return fPty.getOutputStream();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.IMITTY#getInputStream()
	 */
	public InputStream getInputStream() {
		return fPty.getInputStream();
	}

}
