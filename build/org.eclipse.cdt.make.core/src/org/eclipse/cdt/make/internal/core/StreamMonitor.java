/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

public class StreamMonitor extends OutputStream {

	IProgressMonitor monitor;
	OutputStream console;
	public static final int TOTAL_WORK = 10;
	private int halfWay = TOTAL_WORK / 2;
	private int currentIncrement = 1;
	private int nextProgress = currentIncrement;
	private int worked = 0;

	public StreamMonitor(IProgressMonitor mon, OutputStream cos) {
		monitor = mon;
		console = cos;
		monitor.beginTask("", TOTAL_WORK);
	}

	private void progressUpdate() {
		if (--nextProgress <= 0) {
			//we have exhausted the current increment, so report progress
			monitor.worked(1);
			worked++;
			if (worked >= halfWay) {
				//we have passed the current halfway point, so double the
				//increment and reset the halfway point.
				currentIncrement *= 2;
				halfWay += (TOTAL_WORK - halfWay) / 2;
			}
			//reset the progress counter to another full increment
			nextProgress = currentIncrement;
		}
	}
	/**
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		if (console != null) {
			console.close();
		}
		monitor.done();
	}

	/**
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		if (console != null) {
			console.flush();
		}
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	public synchronized void write(int b) throws IOException {
		if (console != null) {
			console.write(b);
		}
		progressUpdate();
	}

	/**
	 * @see java.io.OutputStream#write(...)
	 */
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off != 0 || (len < 0) || (len > b.length)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		if (console != null) {
			console.write(b, off, len);
		}
		progressUpdate();
	}
}
