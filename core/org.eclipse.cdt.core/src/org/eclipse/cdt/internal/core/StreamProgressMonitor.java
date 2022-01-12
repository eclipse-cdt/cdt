/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Helper class to report progress of the build via {@link IProgressMonitor}
 */
public class StreamProgressMonitor extends OutputStream {

	private IProgressMonitor monitor;
	private OutputStream console;
	private final int fTotalWork;
	private int halfWay;
	private int currentIncrement = 2;
	private int nextProgress = currentIncrement;
	private int worked = 0;

	public StreamProgressMonitor(IProgressMonitor mon, OutputStream cos, int totalWork) {
		monitor = mon;
		console = cos;
		fTotalWork = totalWork;
		halfWay = fTotalWork / 2;
		monitor.beginTask("", fTotalWork); //$NON-NLS-1$
	}

	private void progressUpdate() {
		if (--nextProgress <= 0) {
			//we have exhausted the current increment, so report progress
			if (fTotalWork > worked) {
				monitor.worked(1);
			}
			worked++;
			if (worked >= halfWay) {
				//we have passed the current halfway point, so double the
				//increment and reset the halfway point.
				currentIncrement *= 2;
				halfWay += (fTotalWork - halfWay) / 2;
			}
			//reset the progress counter to another full increment
			nextProgress = currentIncrement;
		}
	}

	/**
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		if (console != null) {
			console.close();
		}
		monitor.done();
	}

	/**
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		if (console != null) {
			console.flush();
		}
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public synchronized void write(int b) throws IOException {
		if (console != null) {
			console.write(b);
		}
		progressUpdate();
	}

	/**
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
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

	public int getWorkDone() {
		return worked;
	}
}
