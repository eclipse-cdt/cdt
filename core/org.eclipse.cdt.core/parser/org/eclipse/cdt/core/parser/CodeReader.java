/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author jcamelon
 */
public class CodeReader {
    public static final String SYSTEM_DEFAULT_ENCODING = System.getProperty( "file.encoding" ); //$NON-NLS-1$
	private static final String NF = "<text>"; //$NON-NLS-1$
	private static final char [] NOFILE = NF.toCharArray(); //$NON-NLS-1$
	
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

        FileChannel channel = stream.getChannel();
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int)channel.size());
		channel.read(byteBuffer);
		byteBuffer.rewind();

		
		CharBuffer charBuffer = Charset.forName(encoding).decode(byteBuffer);
		if (charBuffer.hasArray())
			return charBuffer.array();
		// Got to copy it out
		char[] buff = new char[charBuffer.length()];
		charBuffer.get(buff);
		return buff;
	}
	
	protected char[] xload(FileInputStream stream) throws IOException {
		FileChannel channel = stream.getChannel();
		MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

		// TODO use the real encoding
		CharBuffer charBuffer = Charset.forName(SYSTEM_DEFAULT_ENCODING).decode(map);
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
    
    public String toString() {
		return getPath();
    }
	
	public String getPath()
	{
		return new String( filename );
	}
	
}
