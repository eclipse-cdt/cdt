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
		return CharArrayUtils.hash(buffer, start, len) & (hashTable.length - 1); 
	}
	
	private int hash(char[] buffer) {
		return hash(buffer, 0, buffer.length);
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
			if( (currEntry + 1) >= keyTable.length){
			    //need to recompute hash for this add, recurse.
				resize();
				return add( buffer, start, len );
			}
			currEntry++;
			keyTable[currEntry] = CharArrayUtils.extract(buffer, start, len);
			insert(currEntry, hash);
			return currEntry;
		} 
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
		if (++currEntry >= keyTable.length){
		    //need to recompute hash for this add, recurse
			resize();
			return add( buffer, start, len );
		}
		currEntry++;
		keyTable[currEntry] = CharArrayUtils.extract(buffer, start, len);
		nextTable[last] = currEntry + 1;
		return currEntry;
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

	protected void removeEntry(int i) {
		// Remove the hash entry
		int hash = hash(keyTable[i]);
		if (hashTable[hash] == i + 1)
			hashTable[hash] = nextTable[i];
		else { 
			// find entry pointing to me
			int j = hashTable[hash] - 1;
			while (nextTable[j] != 0 && nextTable[j] != i + 1)
				j = nextTable[j] - 1;
			nextTable[j] = nextTable[i];
		}
		
		if (i < currEntry) {
			// shift everything over
			System.arraycopy(keyTable, i + 1, keyTable, i, currEntry - i);
			System.arraycopy(nextTable, i + 1, nextTable, i, currEntry - i);
			
			// adjust hash and next entries for things that moved
			for (int j = 0; j < hashTable.length; ++j)
				if (hashTable[j] > i)
					--hashTable[j];

			for (int j = 0; j < nextTable.length; ++j)
				if (nextTable[j] > i)
					--nextTable[j];
		}

		// last entry is now free
		keyTable[currEntry] = null;
		nextTable[currEntry] = 0;
		--currEntry;
	}
	
	public void dumpNexts() {
		for (int i = 0; i < nextTable.length; ++i) {
			if (nextTable[i] == 0)
				continue;
			
			System.out.print(i);
			
			for (int j = nextTable[i] - 1; j >= 0; j = nextTable[j] - 1)
				System.out.print(" -> " + j); //$NON-NLS-1$
			
			System.out.println(""); //$NON-NLS-1$
		}
	}
	
}
