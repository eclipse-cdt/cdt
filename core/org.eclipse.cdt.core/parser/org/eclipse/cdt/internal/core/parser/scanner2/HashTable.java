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

/*
 * For use by the Parser Symbol Table
 * Created on Jul 15, 2004
 */
package org.eclipse.cdt.internal.core.parser.scanner2;


/**
 * @author aniefer
 */
public abstract class HashTable implements Cloneable{
    
	protected Object[] keyTable;
	private int[] hashTable;
	private int[] nextTable;

	
	protected int currEntry = -1;

	public HashTable(int initialSize) {
		int size = 1;
		while (size < initialSize)
			size <<= 1;
		
		keyTable = new Object[size];
		hashTable = new int[size * 2];
		nextTable = new int[size];
	}

	public Object clone(){
	    HashTable newTable = null;
        try {
            newTable = (HashTable) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //shouldn't happen because object supports clone.
            return null;
        }
        
        int size = capacity();
        newTable.keyTable = new Object[ size ];
        newTable.hashTable = new int[ size*2 ];
        newTable.nextTable = new int[ size ];
        
        System.arraycopy(keyTable, 0, newTable.keyTable, 0, keyTable.length);
	    System.arraycopy(hashTable, 0, newTable.hashTable, 0, hashTable.length);
	    System.arraycopy(nextTable, 0, newTable.nextTable, 0, nextTable.length);
	    
	    newTable.currEntry = currEntry;
	    return newTable;
	}
	
	public int size(){
	    return currEntry + 1;
	}
	
	public Object keyAt( int i ){
	    if( i < 0 || i > currEntry )
	        return null;
	    
	    return keyTable[ i ];
	}
	
	public void clear(){
	    for( int i = 0; i < keyTable.length; i++ ){
	        keyTable[i] = null;
	        hashTable[ 2*i ] = 0;
	        hashTable[ 2*i + 1 ] = 0;
	        nextTable[i] = 0;
	    }
	    currEntry = -1;
	}
	
	public int capacity() {
		return keyTable.length;
	}
	
	private int hash(Object obj) {
	    return obj.hashCode() & (hashTable.length - 1);
	}
	
	private void insert(int i) {
		insert(i, hash(keyTable[i]));
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
		Object[] oldKeyTable = keyTable;
		keyTable = new Object[size];
		System.arraycopy(oldKeyTable, 0, keyTable, 0, oldKeyTable.length);

		// Need to rehash everything
		rehash( oldKeyTable.length, true );
	}
	
	protected void rehash( int n, boolean reallocate ){
	    if( reallocate ){
	        hashTable = new int[ keyTable.length * 2 ];
	        nextTable = new int[ keyTable.length ];
	    } else {
	        for( int i = 0; i < keyTable.length; i++ ){
	            hashTable[2*i] = 0;
	            hashTable[2*i+1] = 0;
	            nextTable[i] = 0;
	        }
	    }
	    for (int i = 0; i < n; ++i) {
			insert(i);
		}
	}
	
	private void resize() {
		resize(keyTable.length << 1);
	}

	protected final int add(Object obj) {
		int hash = hash(obj);
		
		if (hashTable[hash] == 0) {
			if ( (currEntry + 1) >= keyTable.length){
				resize();
				//hash code needs to be recomputed, just recurse.
				return add( obj );
			}
			currEntry++;
			keyTable[currEntry] = obj;
			insert(currEntry, hash);
			return currEntry;
		} 
		// is the key already registered?
		int i = hashTable[hash] - 1;
		if ( obj.equals( keyTable[i] ) )
			// yup
			return i;
		
		// follow the next chain
		int last = i;
		for (i = nextTable[i] - 1; i >= 0; i = nextTable[i] - 1) {
			if (obj.equals( keyTable[i] ) )
				// yup this time
				return i;
			last = i;
		}
		
		// nope, add it in
		if ( (currEntry + 1) >= keyTable.length){
			resize();
			//hash code needs to be recomputed, just recurse.
			return add( obj );
		}
		currEntry++;
		keyTable[currEntry] = obj;
		nextTable[last] = currEntry + 1;
		return currEntry;
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
				if (hashTable[j] > i + 1)
					--hashTable[j];

			for (int j = 0; j < nextTable.length; ++j)
				if (nextTable[j] > i + 1)
					--nextTable[j];
		}

		// last entry is now free
		keyTable[currEntry] = null;
		nextTable[currEntry] = 0;
		--currEntry;
	}
	
	protected final int lookup(Object buffer ){
		int hash = hash(buffer);
		
		if (hashTable[hash] == 0)
			return -1;
		
		int i = hashTable[hash] - 1;
		if (buffer.equals( keyTable[i] ) )
			return i;
		
		// Follow the next chain
		for (i = nextTable[i] - 1; i >= 0; i = nextTable[i] - 1)
			if ( buffer.equals( keyTable[i] ))
				return i;
			
		return -1;
	}
	
	public boolean containsKey( Object key ){
	    return lookup( key ) != -1; 
	}
	
	public Object [] keyArray(){
	    Object [] keys = new Object[ size() ];
	    System.arraycopy( keyTable, 0, keys, 0, keys.length );
	    return keys;
	}
}
