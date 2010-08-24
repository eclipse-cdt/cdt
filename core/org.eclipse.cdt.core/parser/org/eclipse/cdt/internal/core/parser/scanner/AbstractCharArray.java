/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

/**
 * Abstract class for providing input to the lexer.
 * @since 5.2
 */
public abstract class AbstractCharArray {

	/**
	 * Returns the length of this array or -1 if it is yet, unknown. This method may be called
	 * before the array has been traversed.
	 */
	public abstract int tryGetLength();
	
	/**
	 * Returns the length of the array. This method is called only after the lexer has worked its
	 * way through the array. Therefore for subclasses it is efficient enough to read through to the
	 * end of the array and provide the length. 
	 */
	public abstract int getLength();

	/** 
	 * Checks whether the given offset is valid for this array. Subclasses may assume
	 * that offset is non-negative.
	 */
	public abstract boolean isValidOffset(int offset);

	/**
	 * Computes 64-bit hash value of the character array. This method doesn't cause any I/O if called
	 * after the array has been traversed.
	 * @return The hash value of the contents of the array.
	 */
	public abstract long getContentsHash();

	/**
	 * Returns the character at the given position, subclasses do not have to do range checks.
	 */
	public abstract char get(int offset);

	/**
	 * Copy a range of characters to the given destination. Subclasses do not have to do any
	 * range checks.
	 */
	public abstract void arraycopy(int offset, char[] destination, int destinationPos, int length);

	/**
	 * This method is slow. Use only for debugging.
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for (int pos = 0; isValidOffset(pos); pos++) {
			buf.append(get(pos));
		}
		return buf.toString();
	}
}
