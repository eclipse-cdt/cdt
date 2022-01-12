/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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

package org.eclipse.cdt.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output stream which storing the console output
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ConsoleOutputStream extends OutputStream {

	protected StringBuffer fBuffer;

	public ConsoleOutputStream() {
		fBuffer = new StringBuffer();
	}

	public synchronized String readBuffer() {
		String buf = fBuffer.toString();
		fBuffer.setLength(0);
		return buf;
	}

	@Override
	public synchronized void write(int c) throws IOException {
		byte ascii[] = new byte[1];
		ascii[0] = (byte) c;
		fBuffer.append(new String(ascii));
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		fBuffer.append(new String(b, off, len));
	}

	/**
	 * @since 6.0
	 */
	public synchronized void write(String msg) throws IOException {
		fBuffer.append(msg);
	}

}
