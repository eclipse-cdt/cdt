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

import java.util.Comparator;

/**
 * @author Doug Schaefer
 */
public class CharArrayObjectMap {//extends CharArrayMap {
    public static final CharArrayObjectMap EMPTY_MAP = new CharArrayObjectMap( 0 ){
        public Object clone()                         { return this; }
        public Object put( char[] key, int start, int length, Object value ) 
        { throw new UnsupportedOperationException(); }
    };

	private char[][] keyTable;
	private int[] hashTable;
	private int[] nextTable;
	private Object[] valueTable;
	
	private int currEntry = -1;

	public CharArrayObjectMap(int initialSize) {
		int size = 1;
		while (size < initialSize)
			size <<= 1;
		
		keyTable = new char[size][];
		hashTable = new int[size * 2];
		nextTable = new int[size];
		valueTable = new Object[size];
	}
	
	public Object put(char[] key, int start, int length, Object value) {
		int i = add(key, start, length);
		Object oldvalue = valueTable[i];
		valueTable[i] = value;
		return oldvalue;
	}

	final public Object put(char[] key, Object value) {
		return put(key, 0, key.length, value);
	}
	
	final public Object get(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i >= 0)
			return valueTable[i];
		return null;
	}
	
	final public Object get(char[] key) {
		return get(key, 0, key.length);
	}
	
	final public Object getAt( int i ){
	    if( i < 0 || i > currEntry )
	        return null;
	  
	    char [] key = keyAt( i );
	    if( key == null ) return null;
	    
	    return get( key, 0, key.length );
	}
	
	final public char[] keyAt( int i ){
	    if( i < 0 || i > currEntry )
	        return null;
	    
	    return keyTable[ i ];
	}
	
	final public Object remove(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i < 0)
			return null;

		Object value = valueTable[i];

		removeEntry(i);
		
		return value;
	}
	
	final public Object remove(char[] key) {
		return remove(key, 0, key.length);
	}
	
	public Object clone(){
        int size = capacity();

	    CharArrayObjectMap newTable = new CharArrayObjectMap( size );
        newTable.keyTable = new char[ size ][];
        newTable.hashTable = new int[ size*2 ];
        newTable.nextTable = new int[ size ];
	    newTable.valueTable = new Object[ capacity() ];
	    
	    System.arraycopy(valueTable, 0, newTable.valueTable, 0, valueTable.length);
        System.arraycopy(keyTable, 0, newTable.keyTable, 0, keyTable.length);
	    System.arraycopy(hashTable, 0, newTable.hashTable, 0, hashTable.length);
	    System.arraycopy(nextTable, 0, newTable.nextTable, 0, nextTable.length);
	    
	    newTable.currEntry = currEntry;
	    return newTable;
	}
	
	final public int size(){
	    return currEntry + 1;
	}
	
	final public void clear(){
	    for( int i = 0; i < keyTable.length; i++ ){
	        keyTable[i] = null;
	        hashTable[ 2*i ] = 0;
	        hashTable[ 2*i + 1 ] = 0;
	        nextTable[i] = 0;
	        valueTable[i] = null;
	    }
	    currEntry = -1;
	}
	
	final public int capacity() {
		return keyTable.length;
	}
	
	final public boolean containsKey( char[] key ){
	    return lookup( key, 0, key.length ) != -1; 
	}
	
	final public char [][] keyArray(){
	    char [][] keys = new char[ size() ][];
	    System.arraycopy( keyTable, 0, keys, 0, keys.length );
	    return keys;
	}

	final public boolean isEmpty(){
	    return currEntry == -1;
	}
	
    final public void sort( Comparator c ) {
        if( size() > 1 ){
	        quickSort( c, 0, size() - 1 );
	        
	        rehash( size(), false );
        }
    }	

	private void resize() {
		resize(keyTable.length << 1);
	}
	
	private void resize(int size) {
		Object[] oldValueTable = valueTable;
		valueTable = new Object[size];
		System.arraycopy(oldValueTable, 0, valueTable, 0, oldValueTable.length);
		
		Object[] oldKeyTable = keyTable;
		keyTable = new char[size][];
		System.arraycopy(oldKeyTable, 0, keyTable, 0, oldKeyTable.length);

		// Need to rehash everything
		rehash( oldKeyTable.length, true );
	}
	
	private int hash(char[] buffer, int start, int len) {
		return CharArrayUtils.hash(buffer, start, len) & (hashTable.length - 1); 
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

	private void rehash( int n, boolean reallocate ){
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

	private int add(char[] buffer, int start, int len) {
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
		if (currEntry + 1 >= keyTable.length){
		    //need to recompute hash for this add, recurse
			resize();
			return add( buffer, start, len );
		}
		currEntry++;
		keyTable[currEntry] = CharArrayUtils.extract(buffer, start, len);
		nextTable[last] = currEntry + 1;
		return currEntry;
	}
	
	private void removeEntry(int i) {
	    if (i < currEntry)
			System.arraycopy(valueTable, i + 1, valueTable, i, currEntry - i);
		valueTable[currEntry] = null;
		
		// Remove the hash entry
		int hash = hash(keyTable[i], 0, keyTable[i].length);
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
	
	private int lookup(char[] buffer, int start, int len) {
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
	    
    private void quickSort( Comparator c, int p, int r ){
        if( p < r ){
            int q = partition( c, p, r );
            if( p < q )   quickSort( c, p, q );
            if( ++q < r ) quickSort( c, q, r );
        }
    }
    private int partition( Comparator c, int p, int r ){
        char[] x = keyTable[ p ];
        Object temp = null;
        int i = p;
        int j = r;
        
        while( true ){
            while( c.compare( keyTable[ j ], x ) > 0 ){ j--; }
            if( i < j ) 
                while( c.compare( keyTable[ i ], x ) < 0 ){ i++; }
            
            if( i < j ){
                temp = keyTable[j];
                keyTable[j] = keyTable[i];
                keyTable[i] = (char[]) temp;
                
                temp = valueTable[j];
                valueTable[j] = valueTable[i];
                valueTable[i] = temp;
            } else {
                return j;
            }
        }
    }

}
