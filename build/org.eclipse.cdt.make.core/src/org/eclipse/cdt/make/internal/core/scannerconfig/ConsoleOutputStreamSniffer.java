/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.make.core.scannerconfig.*;

/**
 * Intercepts an output to console and forwards it to line parsers for processing
 * 
 * @author vhirsl
 */
public class ConsoleOutputStreamSniffer extends OutputStream {

	private StringBuffer currentLine = new StringBuffer();
	private OutputStream outputStream;
	private int nOpens = 0;
	private IScannerInfoConsoleParser[] parsers;
	
	public ConsoleOutputStreamSniffer(IScannerInfoConsoleParser[] parsers) {
		this.parsers = parsers;
	}
	
	public ConsoleOutputStreamSniffer(OutputStream outputStream, IScannerInfoConsoleParser[] parsers) {
		this(parsers);
		nOpens = 1;
		this.outputStream = outputStream;
	}
	
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		currentLine.append((char) b);
		checkLine(false);
		if (outputStream != null) {
			outputStream.write(b);
		}
	}
	/**
	 * @param flush
	 */
	private void checkLine(boolean flush) {
		String buffer = currentLine.toString();
		int i = 0;
		while ((i = buffer.indexOf('\n')) != -1) {
			String line = buffer.substring(0, i).trim(); // get rid of any trailing \r
			processLine(line);
			buffer = buffer.substring(i + 1); // skip the \n and advance
		}
		currentLine.setLength(0);
		if (flush) {
			if (buffer.length() > 0) {
				processLine(buffer);
			}
		} else {
			currentLine.append(buffer);
		}
	}

	/**
	 * @param line
	 */
	private void processLine(String line) {
		for (int i = 0; i < parsers.length; ++i) {
			parsers[i].processLine(line);
		}
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		if (nOpens > 0 && --nOpens == 0) {
			checkLine(true);
			if (outputStream != null)
				outputStream.close();
		}
		for (int i = 0; i < parsers.length; ++i) {
			parsers[i].shutdown();
		}
	}
	/* (non-Javadoc)
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		if (outputStream != null) {
			outputStream.flush();
		}
	}
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off != 0 || (len < 0) || (len > b.length)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		currentLine.append(new String(b, 0, len));
		checkLine(false);
		if (outputStream != null)
			outputStream.write(b, off, len);
	}
}
