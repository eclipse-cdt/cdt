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
	
	protected StringBuffer fContent;

	protected int pos;
	
	public ConsoleOutputStream() {
		fBuffer= new StringBuffer(256);
		fContent= new StringBuffer();
		pos = 0;
	}

	/**
	 * @see OutputStream#flush
	 */
	public synchronized void flush() throws IOException {
		final String content= fBuffer.toString();
		fBuffer.setLength(0);
		fContent.append(content);
	}
	
	public String getContent(int len) {
		String s = null;
		try {
			s = fContent.substring (len);
		} catch (StringIndexOutOfBoundsException e) {
			s = "";
		}
		return s;
	}
	
	public String getContent() {
		// return fContent.toString();
		if (pos >= fContent.length())
			pos = 0;
		String s = getContent(pos);
		pos += s.length();
		return s;
	}

	public void clear() {
		fBuffer.setLength (0);
		fContent.setLength (0);
		pos = 0;
	}

	/**
	 * Implements buffered output at the lowest level
	 * @see OutputStream#write
	 */
	public synchronized void write(int c) throws IOException {
		fBuffer.append((char) c);
		if (fBuffer.length() > 250) {
			flush();
		}
	}
}
