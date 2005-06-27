/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

/**
 * @author Doug Schaefer
 */
public class CharArrayIntMap extends CharTable {

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
		super.resize(size);
	}
	
	public void clear() {
		super.clear();
		for( int i = 0; i < capacity(); i++ )
			valueTable[i] = undefined;
	}
	public Object clone(){
	    CharArrayIntMap newMap = (CharArrayIntMap) super.clone();
	    newMap.valueTable = new int[ capacity() ];
	    System.arraycopy(valueTable, 0, newMap.valueTable, 0, valueTable.length);
	    return newMap;
	}
	
	public int put(char[] key, int start, int length, int value) {
		int i = addIndex(key, start, length);
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
		return undefined;
	}

	/**
	 * @param image
	 * @return
	 */
	public int get(char[] image) {
		return get( image, 0, image.length );
	}
}
