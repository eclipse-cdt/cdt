/**********************************************************************
 * Copyright (c) 2004 IBM and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

/**
 * @author Doug Schaefer
 */
public class CharArrayIntMap extends CharArrayMap {

	private int[] valueTable;
	public final int undefined;

	public CharArrayIntMap(int initialSize, int undefined) {
		super(initialSize);
		valueTable = new int[capacity()];
		this.undefined = undefined;
	}

	protected void resize(int size) {
		int[] oldValueTable = valueTable;
		valueTable = new int[size];
		System.arraycopy(oldValueTable, 0, valueTable, 0, oldValueTable.length);
	}
	
	public int put(char[] key, int start, int length, int value) {
		int i = add(key, start, length);
		int oldvalue = valueTable[i];
		valueTable[i] = value;
		return oldvalue;
	}

	public int put(char[] key, int value) {
		return put(key, 0, key.length, value);
	}
	
	public int get(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i >= 0)
			return valueTable[i];
		else
			return -1;
	}

}
