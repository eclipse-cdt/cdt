/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.text;

import java.io.IOException;

/**
 * IFileRider
 */
public interface IFileRider {

	/** Set rider to position
	 * @param pos is normalized to be in range [0, f.length()]
	 */
	public abstract void seek(int pos) throws IOException;

	/**
	 * Write a char.
	 * @param c
	 */
	public abstract void writeChar(char c) throws IOException;

	/**
	 * Write a character array.
	 * @param buf
	 * @throws IOException
	 */
	public abstract void writeChars(char[] buf) throws IOException;

	/**
	 * Write n characters of an array of characters.
	 * @param buf
	 * @param n
	 * @throws IOException
	 */
	public abstract void writeChars(char[] buf, int n) throws IOException;

	/**
	 * Write n characters of an array of characters starting at an offset.
	 * @param buf
	 * @param off
	 * @param n
	 * @throws IOException
	 */
	public abstract void writeChars(char[] buf, int off, int n) throws IOException;

	/**
	 * Write n characters of a String starting at an offset.
	 * @param buf
	 * @param off
	 * @param n
	 * @throws IOException
	 */
	public abstract void writeChars(String buf, int off, int n) throws IOException;

	/**
	 * Read next character.
	 * @return next char in buffer.
	 * @throws IOException
	 */
	public abstract char readChar() throws IOException;

	/**
	 * Read as much characters as possible into a char array.
	 * @param buf
	 * @throws IOException
	 */
	public abstract void readChars(char[] buf) throws IOException;

	/**
	 * Read n characters into character array.
	 * @param buf
	 * @param n
	 * @throws IOException
	 */
	public abstract void readChars(char[] buf, int n) throws IOException;

	/**
	 * Read n characters into char array.
	 * @param buf
	 * @param off
	 * @param n
	 * @throws IOException
	 */
	public abstract void readChars(char[] buf, int off, int n) throws IOException;

	/**
	 * Read n characters into StringBuffer.
	 * @param buf
	 * @param from
	 * @param n
	 * @throws IOException
	 */
	public abstract void readChars(StringBuffer buf, int n) throws IOException;

	/**
	 * @return length of file
	 */
	public abstract int length();

	/**
	 * @return length limit of file
	 */
	public abstract int limit();

	/**
	 * @return whether this rider is readonly or not
	 */
	public abstract boolean isReadonly();

}
