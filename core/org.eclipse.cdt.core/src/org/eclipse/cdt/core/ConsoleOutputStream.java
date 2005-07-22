/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Output stream which storing the console output
 */
public class ConsoleOutputStream extends OutputStream {
	
	protected StringBuffer fBuffer;
		
	public ConsoleOutputStream() {
		fBuffer= new StringBuffer();
	}

	public synchronized String readBuffer() {
		String buf = fBuffer.toString();
		fBuffer.setLength(0);
		return buf;
	}

	public synchronized void write(int c) throws IOException {
		byte ascii[] = new byte[1];
		ascii[0] = (byte) c;
		fBuffer.append(new String(ascii));
	}
	    
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        fBuffer.append(new String(b, off, len));
    }
}
