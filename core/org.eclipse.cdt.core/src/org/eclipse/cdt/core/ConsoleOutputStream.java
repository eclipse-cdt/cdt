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

		
	public synchronized String readBuffer() {
		String buf = fBuffer.toString();
		fBuffer.setLength(0);
		return buf;
	}

	public void clear() {
		fBuffer.setLength (0);
	}

	public synchronized void write(int c) throws IOException {
		byte ascii[] = new byte[1];
		ascii[0] = (byte) c;
		fBuffer.append(new String(ascii));
	}
	
    public synchronized void write(byte[] b) throws IOException {
        fBuffer.append(new String(b));
    }
    
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        fBuffer.append(new String(b, off, len));
    }
}
