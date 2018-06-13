/*******************************************************************************
 * Copyright (c) 2018 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Waqas Ilyas (Mentor Graphics) - Initial implementation.
 *******************************************************************************/
package org.eclipse.cdt.serial.tests.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps an input stream to create artificial delays on read in attempt to
 * simulate a an IO device that may take arbitrary amount of time in different
 * states to respond
 */
public class DelayedInputStream extends FilterInputStream {
	private long maxDelay = 0;
	private boolean randomDelay = false;

	public DelayedInputStream(InputStream in, long maxDelay, boolean randomDelay) throws IOException {
		super(in);
		this.maxDelay = maxDelay;
		this.randomDelay = randomDelay;
	}

	@Override
	public int read() throws IOException {
		delay();
		return super.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		delay();
		return super.read(b, off, len);
	}

	protected void delay() {
		long delay;
		if (randomDelay)
			delay = (long) (Math.random() * maxDelay);
		else
			delay = maxDelay;

		try {
			// Artificial delay in read
			if (delay > 0)
				Thread.sleep(delay);
		} catch (InterruptedException e) {
			// ignore
		}
	}
}
