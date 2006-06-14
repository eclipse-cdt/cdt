/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on May 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cdt.core.parser.util;

/**
 * @author dschaefe
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CharArrayPool extends CharTable{
	
	public CharArrayPool(int tableSize) {
		super(tableSize);
	}

	// Removes the current entry
	private final void remove() {
		int hash = hash(keyTable[currEntry], 0, keyTable[currEntry].length);
		int i = hashTable[hash] - 1;
		if (i == currEntry)
			// make my next the hash entry
			hashTable[hash] = nextTable[currEntry];
		else {
			// remove me from the next list
			int last;
			do {
				last = i;
				i = nextTable[i] - 1;
			} while (i != currEntry);
			
			nextTable[last] = nextTable[currEntry];
		}
		
		keyTable[currEntry] = null;
		nextTable[currEntry] = 0;
	}
	
	private final void addHashed(char[] str, int hash) {
		// First remove the existing string if there is one
		if (++currEntry == keyTable.length)
			currEntry = 0;
		
		if (keyTable[currEntry] != null)
			remove();
		
		keyTable[currEntry] = str;

		// Now add it to the hash table, insert into next entries as necessary
		if (hashTable[hash] != 0)
			nextTable[currEntry] = hashTable[hash];
		hashTable[hash] = currEntry + 1;
	}
	
	public final char[] add(char[] source, int start, int length) {
		// do we have it already?
		int hash = hash(source, start, length);
		int result = lookup(source, start, length, hash);
		
		if( result >= 0)
			return keyTable[result];
		
		char [] res = new char[length];
		System.arraycopy(source, 0, res, 0, length);
		addHashed(res, hash);
		
		return res;
	}
	
	public final char[] add(char[] source) {
		return add(source, 0, source.length);
	}
}
