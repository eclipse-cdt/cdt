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
public class CharArrayObjectMap extends CharArrayMap {

	private Object[] valueTable;

	public CharArrayObjectMap(int initialSize) {
		super(initialSize);
		valueTable = new Object[capacity()];
	}
	
	protected void resize(int size) {
		super.resize(size);
		Object[] oldValueTable = valueTable;
		valueTable = new Object[size];
		System.arraycopy(oldValueTable, 0, valueTable, 0, oldValueTable.length);
	}
	
	public Object put(char[] key, int start, int length, Object value) {
		int i = add(key, start, length);
		Object oldvalue = valueTable[i];
		valueTable[i] = value;
		return oldvalue;
	}

	public Object put(char[] key, Object value) {
		return put(key, 0, key.length, value);
	}
	
	public Object get(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i >= 0)
			return valueTable[i];
		else
			return null;
	}
	
	public Object get(char[] key) {
		return get(key, 0, key.length);
	}
	
}
