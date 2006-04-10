/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.clientserver.java;

/**
 * This singleton class converts an array of unsigned bytes (represented by shorts) to UTF-8
 * strings as per the UTF-8 format that the JVM uses. Note that the JVM's version
 * of UTF8 is different to the standard UTF-8 format.
 */
public class ClassFileUTF8Reader {
	
	private static ClassFileUTF8Reader instance;

	/**
	 * Constructor.
	 */
	private ClassFileUTF8Reader() {
		super();
	}
	
	/**
	 * Returns the singleton instance of the reader.
	 * @return the singleton instance.
	 */
	public static final ClassFileUTF8Reader getInstance() {
		
		if (instance == null) {
			instance = new ClassFileUTF8Reader();
		}
		
		return instance;
	}
	
	/**
	 * Returns a string given an unsigned array of bytes (represented as an array of shorts). Converts to
	 * a string assuming the bytes represent the UTF8 format used by JVM.
	 * @param bytes the bytes.
	 * @return the string.
	 */
	public String getString(short[] bytes) {
		StringBuffer buf = new StringBuffer();
		
		char c;
		
		int i = 0;
		
		while (i < bytes.length) {
			
			// first bit is 0
			// char is represented by one byte
			// char is in the range '\u0001' to '\u007F'
			// format: x byte
			// x: 0xxxxxxx
			if ((bytes[i] & 0x80) == 0) {
				c = (char)(bytes[i]);
				i = i + 1;
			}
			// first three bits are 110 and first two bits of next byte are 10
			// char is represented by two bytes
			// char is either null character ('\u0000') or in the range '\u0080' to '\u07FF'
			// format: x byte followed by y byte
			// x: 110xxxxx
			// y: 10xxxxxx
			else if (((bytes[i] & 0xE0) == 0xC0) && ((bytes[i+1] & 0xC0) == 0x80)) {
				c = (char)(((bytes[i] & 0x1F) << 6) + (bytes[i+1] & 0x3F));
				i = i + 2;
			}
			// first three bits are 1110 and first two bits of next bytes are 10
			// char is represented by three bytes
			// char is in the range '\u0800' to '\uFFFF'
			// format: x byte, y byte and z byte
			// x: 1110xxxx
			// y: 10xxxxxx
			// z: 10xxxxxx
			else if (((bytes[i] & 0xF0) == 0xE0) && ((bytes[i+1] & 0xC0) == 0x80) && ((bytes[i+2] & 0xC0) == 0x80)) {
				c = (char)(((bytes[i] & 0x0F) << 12) + ((bytes[i+1] & 0x3F) << 6) + (bytes[i+2] & 0x3F));
				i = i + 3;
			}
			// we should not never be here
			else {
				continue;
			}
			
			// append character
			buf.append(c);
		}
		
		return buf.toString();
	}
}