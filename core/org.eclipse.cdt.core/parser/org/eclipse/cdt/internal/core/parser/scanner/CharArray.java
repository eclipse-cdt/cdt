/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

/**
 * Wrapper around char[] to implement {@link AbstractCharArray}.
 */
public final class CharArray extends AbstractCharArray {

	private final char[] fArray;

	public CharArray(char[] array) {
		fArray= array;
	}

	@Override
	public int getLimit() {
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
}
