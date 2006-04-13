/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayUtils;

/**
 * @author jcamelon
 */
public class CodeReader {
    public static final String SYSTEM_DEFAULT_ENCODING = System.getProperty( "file.encoding" ); //$NON-NLS-1$
	//private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	private static final String NF = "<text>"; //$NON-NLS-1$
	private static final char [] NOFILE = NF.toCharArray(); 
	
	public final char[] buffer;
	public final char[] filename;
	
	// If you already have preloaded the buffer, e.g. working copy
	public CodeReader(String filename, char[] buffer) {
		this.filename = filename.toCharArray();
		this.buffer = buffer;
	}

	// If you are just scanning a string
	public CodeReader(char[] buffer) {
		this(NF, buffer);
	}
	
	// If you are loading up a file normally
	public CodeReader(String filename) throws IOException
	{
		this.filename = filename.toCharArray();
		
		FileInputStream stream = new FileInputStream(filename);
		try {
			buffer = load(SYSTEM_DEFAULT_ENCODING, stream);
		} finally {
			stream.close();
		}
	}
	public CodeReader(String filename, String charSet ) throws IOException
	{
		this.filename = filename.toCharArray();
		
		FileInputStream stream = new FileInputStream(filename);
		try {
			buffer = load(charSet, stream);
		} finally {
			stream.close();
		}
	}
	
	public CodeReader( String filename, InputStream stream ) throws IOException
	{
		this( filename, SYSTEM_DEFAULT_ENCODING, stream );
	}
	
	public CodeReader( String fileName, String charSet, InputStream stream ) throws IOException {
	    filename = fileName.toCharArray();
	    
		FileInputStream fstream = 
			(stream instanceof FileInputStream)
				? (FileInputStream)stream
				: new FileInputStream(fileName);
		try {
			buffer = load(charSet, fstream);
		} finally {
			// If we create the FileInputStream we need close to it when done,
			// if not we figure the above layer will do it.
			if (!(stream instanceof FileInputStream)) {
				fstream.close();
			}
		}
	}
	
	private char[] load( String charSet, FileInputStream stream ) throws IOException {
	    String encoding = Charset.isSupported( charSet ) ? charSet : SYSTEM_DEFAULT_ENCODING; 

        BufferedInputStream bufferedStream = new BufferedInputStream(stream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final int blocksize = 2048;
        byte [] b = new byte[blocksize];
        int count = 0;
        while( true )
        {
            int size = bufferedStream.read(b);
            if( size != blocksize )
            {
                outputStream.write(b, 0, size );
                count += size;
                break;
            }
            // if we get this far, the full buffer was read in
            outputStream.write(b);
            count += blocksize;
        }
        
        
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(count);
        byteBuffer.put(outputStream.toByteArray());
        byteBuffer.rewind();
		
		CharBuffer charBuffer = Charset.forName(encoding).decode(byteBuffer);
		if (charBuffer.hasArray())
			return charBuffer.array();
		// Got to copy it out
		char[] buff = new char[charBuffer.length()];
		charBuffer.get(buff);
		return buff;
	}
	
	public boolean isFile() {
		return !CharArrayUtils.equals( filename, NOFILE );
	}
	
}
