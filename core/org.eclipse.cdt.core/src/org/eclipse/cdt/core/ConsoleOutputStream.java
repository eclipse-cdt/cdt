package org.eclipse.cdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
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

		
	public String readBuffer() {
		String buf = fBuffer.toString();
		fBuffer.setLength(0);
		return buf;
	}

	public void clear() {
		fBuffer.setLength (0);
	}

	/**
	 * Implements buffered output at the lowest level
	 * @see OutputStream#write
	 */
	public synchronized void write(int c) throws IOException {
		fBuffer.append((char) c);
	}
}
