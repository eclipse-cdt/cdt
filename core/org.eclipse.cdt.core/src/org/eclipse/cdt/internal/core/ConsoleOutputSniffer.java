/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.core.IConsoleParser;


/**
 * Intercepts an output to console and forwards it to console parsers for processing
 * 
 * @author vhirsl
 */
public class ConsoleOutputSniffer {

    /*
     * Private class to sniffer the output stream for this snifffer.
     */
    private class ConsoleOutputStream extends OutputStream {
        // Stream's private buffer for the stream's read contents.
    	private StringBuffer currentLine = new StringBuffer();
    	private OutputStream outputStream = null;
        
    	/**
		 * @param consoleErrorStream
		 */
		public ConsoleOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		/* (non-Javadoc)
    	 * @see java.io.OutputStream#write(int)
    	 */
    	public void write(int b) throws IOException {
    		currentLine.append((char) b);
    		checkLine(false);

    		// Continue writing the bytes to the console's output.
    		if (outputStream != null) {
    			outputStream.write(b);
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
    		
    		// Continue writing the bytes to the console's output.
    		if (outputStream != null)
    			outputStream.write(b, off, len);
    	}
    	
    	/* (non-Javadoc)
    	 * @see java.io.OutputStream#close()
    	 */
    	public void close() throws IOException {
    		checkLine(true);
    		closeConsoleOutputStream();
    	}
    	
    	/* (non-Javadoc)
    	 * @see java.io.OutputStream#flush()
    	 */
    	public void flush() throws IOException {
    		if (outputStream != null) {
    			outputStream.flush();
    		}
    	}

    	/*
    	 * Checks to see if the already read input constitutes
    	 * a complete line (e.g. does the sniffing).  If so, then
    	 * send it to processLine.
    	 * 
    	 * @param flush
    	 */
    	private void checkLine(boolean flush) {
    		String buffer = currentLine.toString();
    		int i = 0;
    		while ((i = buffer.indexOf('\n')) != -1) {
    			String line = buffer.substring(0, i).trim(); // get rid of any trailing \r
    			if (line.length() > 0)
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

    } // end ConsoleOutputStream class
    
	private int nOpens = 0;
	private OutputStream consoleOutputStream;
	private OutputStream consoleErrorStream;
	private IConsoleParser[] parsers;
	
	public ConsoleOutputSniffer(IConsoleParser[] parsers) {
		this.parsers = parsers;
	}
	
	public ConsoleOutputSniffer(OutputStream outputStream, OutputStream errorStream, IConsoleParser[] parsers) {
		this(parsers);
		this.consoleOutputStream = outputStream;
		this.consoleErrorStream = errorStream;
	}
	
	/**
	 * Returns an output stream that will be sniffed.
	 * This stream should be hooked up so the command
	 * output stream goes into here.
	 * 
	 * @return
	 */
	public OutputStream getOutputStream() {
	    incNOpens();
	    return new ConsoleOutputStream(consoleOutputStream);
	}
	
	/**
	 * Returns an error stream that will be sniffed.
	 * This stream should be hooked up so the command
	 * error stream goes into here.
	 * 
	 * @return
	 */
	public OutputStream getErrorStream() {
	    incNOpens();
	    return new ConsoleOutputStream(consoleErrorStream);
	}
	
	private synchronized void incNOpens() {
		nOpens++; 
	}
	
	/*
	 */
	public synchronized void closeConsoleOutputStream() throws IOException {
		if (nOpens > 0 && --nOpens == 0) {
			for (int i = 0; i < parsers.length; ++i) {
				parsers[i].shutdown();
			}
		}
	}
	
	/*
	 * Processes the line by passing the line to the parsers.
	 * 
	 * @param line
	 */
	private synchronized void processLine(String line) {
		for (int i = 0; i < parsers.length; ++i) {
			parsers[i].processLine(line);
		}
	}
	
}
