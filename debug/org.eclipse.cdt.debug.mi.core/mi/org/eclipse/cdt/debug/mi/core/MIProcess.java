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
 * Check if we can interrupt the inferior.
 * 
 */
public interface MIProcess  {


	public abstract boolean canInterrupt(MIInferior inferior);

	public abstract void interrupt(MIInferior inferior);

	/* (non-Javadoc)
	 * @see java.lang.Process#destroy()
	 */
	public void destroy();

	/* (non-Javadoc)
	 * @see java.lang.Process#exitValue()
	 */
	public int exitValue();

	/* (non-Javadoc)
	 * @see java.lang.Process#getErrorStream()
	 */
	public InputStream getErrorStream();

	/* (non-Javadoc)
	 * @see java.lang.Process#getInputStream()
	 */
	public InputStream getInputStream();

	/* (non-Javadoc)
	 * @see java.lang.Process#getOutputStream()
	 */
	public OutputStream getOutputStream();

	/* (non-Javadoc)
	 * @see java.lang.Process#waitFor()
	 */
	public int waitFor() throws InterruptedException;

}
