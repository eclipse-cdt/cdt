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
public abstract class CharArrayMap {

	private char[][] keyTable;
	private int[] hashTable;
	private int[] nextTable;
	protected int currEntry = -1;
	
	protected CharArrayMap(int initialSize) {
		int size = 1;
		while (size < initialSize)
			size <<= 1;
		
		keyTable = new char[size][];
		hashTable = new int[size * 2];
		nextTable = new int[size];
	}
	
	protected int capacity() {
		return keyTable.length;
	}
	
	private int hash(char[] buffer, int start, int len) {
		return CharArrayUtils.hash(buffer, start, len) & (keyTable.length - 1); 
	}
	
	private void insert(int i) {
		insert(i, hash(keyTable[i], 0, keyTable[i].length));
	}
	
	private void insert(int i, int hash) {
		
		if (hashTable[hash] == 0) {
			hashTable[hash] = i + 1;
		} else {
			// need to link
			int j = hashTable[hash] - 1;
			while (nextTable[j] != 0)
				j = nextTable[j] - 1;
			nextTable[j] = i + 1;
		}
	}
	
	protected void resize(int size) {
		char[][] oldKeyTable = keyTable;
		keyTable = new char[size][];
		System.arraycopy(oldKeyTable, 0, keyTable, 0, oldKeyTable.length);
		
		// Need to rehash everything
		hashTable = new int[size * 2];
		nextTable = new int[size];
		for (int i = 0; i < oldKeyTable.length; ++i) {
			insert(i);
		}
	}
	
	private void resize() {
		resize(keyTable.length << 1);
	}

	protected final int add(char[] buffer, int start, int len) {
		int hash = hash(buffer, start, len);
		
		if (hashTable[hash] == 0) {
			keyTable[++currEntry] = CharArrayUtils.extract(buffer, start, len);
			insert(currEntry, hash);
			return currEntry;
		} else {
			// is the key already registered?
			int i = hashTable[hash] - 1;
			if (CharArrayUtils.equals(buffer, start, len, keyTable[i]))
				// yup
				return i;
			
			// follow the next chain
			int last = i;
			for (i = nextTable[i] - 1; i >= 0; i = nextTable[i] - 1) {
				if (CharArrayUtils.equals(buffer, start, len, keyTable[i]))
					// yup this time
					return i;
				last = i;
			}
			
			// nope, add it in
			keyTable[++currEntry] = CharArrayUtils.extract(buffer, start, len);
			nextTable[last] = currEntry + 1;
			return currEntry;
		}
	}
	
	protected final int lookup(char[] buffer, int start, int len) {
		int hash = hash(buffer, start, len);
		
		if (hashTable[hash] == 0)
			return -1;
		
		int i = hashTable[hash] - 1;
		if (CharArrayUtils.equals(buffer, start, len, keyTable[i]))
			return i;
		
		// Follow the next chain
		for (i = nextTable[i] - 1; i >= 0; i = nextTable[i] - 1)
			if (CharArrayUtils.equals(buffer, start, len, keyTable[i]))
				return i;
			
		return -1;
	}

	public void dumpNexts() {
		for (int i = 0; i < nextTable.length; ++i) {
			if (nextTable[i] == 0)
				continue;
			
			System.out.print(i);
			
			for (int j = nextTable[i] - 1; j >= 0; j = nextTable[j] - 1)
				System.out.print(" -> " + j);
			
			System.out.println("");
		}
	}
	
}
