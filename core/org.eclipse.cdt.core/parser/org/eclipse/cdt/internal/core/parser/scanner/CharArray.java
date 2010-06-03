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
 * Wrapper around char[] to implement {@link AbstractCharArray}.
 */
public final class CharArray extends AbstractCharArray {

	private final char[] fArray;
	private long hash64;

	public CharArray(char[] array) {
		fArray= array;
	}

	public CharArray(String str) {
		fArray= str.toCharArray();
	}
	
	public char[] getArray() {
		return fArray;
	}

	@Override
	public int getLength() {
		return fArray.length;
	}

	@Override
	public int tryGetLength() {
		return fArray.length;
	}

	@Override
	public char get(int pos) {
		return fArray[pos];
	}

	@Override
	public void arraycopy(int offset, char[] destination, int destPos, int length) {
		System.arraycopy(fArray, offset, destination, destPos, length);
	}

	@Override
	public boolean isValidOffset(int offset) {
		return offset < fArray.length;
	}

	@Override
	public long getContentsHash() {
		if (hash64 == 0 && fArray.length != 0) {
			StreamHasher hasher = new StreamHasher();
			hasher.addChunk(fArray);
			hash64 = hasher.computeHash();
		}
		return hash64;
	}
}
