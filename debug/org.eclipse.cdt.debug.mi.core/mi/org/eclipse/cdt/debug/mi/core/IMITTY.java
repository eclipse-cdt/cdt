/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core;

import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public interface IMITTY {

	/**
	 * Returns the name of the slave to pass to gdb --tty command
	 * ex: --tty=/dev/pty/1
	 * 
	 * @return
	 */
	public String getSlaveName();

	/**
	 * Returns the OutputStream of the Master.
	 * 
	 * @return
	 */
	public OutputStream getOutputStream();

	/**
	 * Returns the InputStream of the Master
	 * 
	 * @return
	 */
	public InputStream getInputStream();	

}
