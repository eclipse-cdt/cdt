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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * @author jcamelon
 */
public class CodeReader {

	private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	private static final String NOFILE = "<text>"; //$NON-NLS-1$
	
	public final char[] buffer;
	public final String filename;
	
	// If you already have preloaded the buffer, e.g. working copy
	public CodeReader(String filename, char[] buffer) {
		this.filename = filename;
		this.buffer = buffer;
	}

	// If you are just scanning a string
	public CodeReader(char[] buffer) {
		this(NOFILE, buffer);
	}
	
	// If you are loading up a file normally
	public CodeReader(String filename) throws IOException
	{
		this.filename = filename;
		
		FileInputStream stream = new FileInputStream(filename);
		buffer = load(stream);
	}
	
	// If you have a handle on a stream to the file, e.g. IFile.getContents()
	public CodeReader(String filename, InputStream stream) throws IOException {
		this.filename = filename;
		
		FileInputStream fstream = 
			(stream instanceof FileInputStream)
				? (FileInputStream)stream
				: new FileInputStream(filename);
		buffer = load(fstream);
	}
	
	private char[] load(FileInputStream stream) throws IOException {
		FileChannel channel = stream.getChannel();
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int)channel.size());
		channel.read(byteBuffer);
		byteBuffer.rewind();

		// TODO use the real encoding
		CharBuffer charBuffer = Charset.forName(UTF_8).decode(byteBuffer);
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
		CharBuffer charBuffer = Charset.forName(UTF_8).decode(map);
		if (charBuffer.hasArray())
			return charBuffer.array();
		
		// Got to copy it out
		char[] buff = new char[charBuffer.length()];
		charBuffer.get(buff);
		return buff;
		
	}
	
	public boolean isFile() {
		return filename != NOFILE;
	}
	
}
