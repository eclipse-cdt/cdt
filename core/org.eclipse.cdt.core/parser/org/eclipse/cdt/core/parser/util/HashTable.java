/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.parser.util;

import java.util.Comparator;

/**
 * @author ddaoust
 */
public class HashTable {

	protected static final int minHashSize = 2;
	protected int currEntry = -1;
	
    public boolean isEmpty() {
        return currEntry == -1;
    }
    
	final public int size(){
	    return currEntry + 1;
	}
    
	protected int[] hashTable;
	protected int[] nextTable;

	public HashTable(int initialSize) {
		int size = 1;
		while (size < initialSize)
			size <<= 1;
		
		if (size > minHashSize) {
			hashTable = new int[size * 2];
			nextTable = new int[size];
		}
		else {
			hashTable = null;
			nextTable = null;
		}
	}
	
	protected void resize() {
		resize(capacity() << 1);
	}

	public void clear() {
		currEntry = -1; 
		
		// clear the table
		if (hashTable == null)
			return;
		
		for( int i = 0; i < capacity(); i++ ){
	        hashTable[2*i] = 0;
	        hashTable[2*i+1] = 0;
	        nextTable[i] = 0;
        }
	}
	protected void rehash() {
		if (nextTable == null)
			return;
		
		// clear the table (don't call clear() or else the subclasses stuff will be cleared too)
		for( int i = 0; i < capacity(); i++ ){
            hashTable[2*i] = 0;
            hashTable[2*i+1] = 0;
            nextTable[i] = 0;
	        }
		// Need to rehash everything
		for (int i = 0; i <= currEntry; ++i) {
			linkIntoHashTable(i, hash(i));
		}
	}
	protected void resize(int size) {
		if (size > minHashSize) {
		
			hashTable = new int[size * 2];
			nextTable = new int[size];
			
			// Need to rehash everything
			for (int i = 0; i <= currEntry; ++i) {
				linkIntoHashTable(i, hash(i));
			}
		}
	}
	
	protected int hash(int pos) {
		// return the hash value of the element in the key table 
		throw new UnsupportedOperationException();
	}
	
	protected void linkIntoHashTable(int i, int hash) {
		if (nextTable == null)
			return;
		
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
	
	final public int capacity() {
		if (nextTable == null)
			return minHashSize;
		return nextTable.length;
	}
	
	protected void removeEntry(int i, int hash) {
		if (nextTable == null){
		    --currEntry;
			return;
		}
		
		// Remove the hash entry
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
			System.arraycopy(nextTable, i + 1, nextTable, i, currEntry - i);
			
			// adjust hash and next entries for things that moved
			for (int j = 0; j < hashTable.length; ++j)
				if (hashTable[j] > i + 1)
					--hashTable[j];

			for (int j = 0; j < nextTable.length; ++j)
				if (nextTable[j] > i + 1)
					--nextTable[j];
		}

		// last entry is now free
		nextTable[currEntry] = 0;
		--currEntry;
	}

    final public void sort( Comparator c ) {
        if( size() > 1 ){
	        quickSort( c, 0, size() - 1 );       
	        rehash();
        }
    }	
    final private void quickSort( Comparator c, int p, int r ){
        if( p < r ){
            int q = partition( c, p, r );
            if( p < q )   quickSort( c, p, q );
            if( ++q < r ) quickSort( c, q, r );
        }
    }
    
    protected int partition( Comparator c, int p, int r ) {
    	throw new UnsupportedOperationException();
    }
    
	public void dumpNexts() {
		if (nextTable == null)
			return;
		
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
