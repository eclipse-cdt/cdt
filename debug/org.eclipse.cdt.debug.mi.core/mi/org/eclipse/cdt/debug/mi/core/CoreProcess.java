/*******************************************************************************
 * Copyright (c) 2002, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * CoreProcess 
 */
public class CoreProcess extends Process {

	/* (non-Javadoc)
	 * @see java.lang.Process#exitValue()
	 */
	public int exitValue() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#waitFor()
	 */
	public int waitFor() throws InterruptedException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#destroy()
	 */
	public void destroy() {
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getErrorStream()
	 */
	public InputStream getErrorStream() {
		return new InputStream() {
			public int read() throws IOException {
				return -1;
			}
		};
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getInputStream()
	 */
	public InputStream getInputStream() {
		return new InputStream() {
			public int read() throws IOException {
				return -1;
			}
		};
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return new OutputStream() {
			public void write(int b) throws IOException {				
			}
		};
	}

}
