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

import java.util.ArrayList;
import java.util.List;

/**
 * @author ddaoust
 *
 */
public class CharTable extends HashTable {
	protected char[][] keyTable;

	protected CharTable(int initialSize) {
		super(initialSize);
		keyTable = new char[capacity()][];
	}
	
	protected void resize(int size) {
		char[][] oldKeyTable = keyTable;
		keyTable = new char[size][];
		System.arraycopy(oldKeyTable, 0, keyTable, 0, oldKeyTable.length);
		super.resize(size);
	}
	
	public void clear() {
		super.clear();
		for( int i = 0; i < capacity(); i++ )
			keyTable[i] = null;
	}
	public Object clone(){
	    CharTable newTable = (CharTable) super.clone();
        
        int size = capacity();
        newTable.keyTable = new char[size][];
        System.arraycopy(keyTable, 0, newTable.keyTable, 0, keyTable.length);
        
	    return newTable;
	}
	protected final int hash(char[] source, int start, int length) {
		return CharArrayUtils.hash(source, start, length) & ((keyTable.length * 2) - 1);
	}
	protected final int hash( int pos ){
	    return hash(keyTable[pos], 0, keyTable[pos].length);
	}
	
	protected final int hash( char[] obj ){
	    return hash( obj, 0, obj.length );
	}

	protected final int addIndex(char[] buffer ) {
		return addIndex(buffer, 0, buffer.length);
	}
	protected final int addIndex(char[] buffer, int start, int len) {
		if (hashTable != null)
		{
			int hash = hash(buffer, start, len);
			int pos = lookup(buffer, start, len, hash);
			if (pos != -1)
				return pos;
			
			// key is not here, add it.
			if( (currEntry + 1) >= capacity()) {
				resize();
				hash = hash(buffer, start, len);
			}
			currEntry++;
			keyTable[currEntry] = CharArrayUtils.extract(buffer, start, len);
			linkIntoHashTable(currEntry, hash);		
		}
		else 
		{
			int pos = lookup(buffer, start, len);
			if (pos != -1)
				return pos;
			// key is not here, add it.
			if( (currEntry + 1) >= capacity()) {
				resize();
				if( capacity() > minHashSize ){
					//if we grew from list to hash, then recurse and add as a hashtable
				    return addIndex( buffer, start, len );
				}
			}
			currEntry++;
			keyTable[currEntry] = CharArrayUtils.extract(buffer, start, len);
		}
		return currEntry;
		
	}
	
	protected void removeEntry(int i) {		
		// Remove the entry from the keyTable, shifting everything over if necessary
		int hash = hash(keyTable[i]);
		if (i < currEntry)
			System.arraycopy(keyTable, i + 1, keyTable, i, currEntry - i);			

		keyTable[currEntry] = null;
		
		// Make sure you remove the value before calling super where currEntry will change
		removeEntry(i, hash);
	}
	
	public List toList(){
	    List list = new ArrayList( size() );
	    int size = size();
	    for( int i = 0; i < size; i++ ){
	        list.add( keyAt( i ) );
	    }
	    return list;
	}
	
	final public char[] keyAt( int i ){
	    if( i < 0 || i > currEntry )
	        return null;
	    
	    return keyTable[ i ];
	}
	
	final public boolean containsKey( char[] key ){
	    return lookup( key ) != -1; 
	}
	final public char[] findKey( char[] buffer, int start, int len ){
	    int idx = lookup( buffer, start, len );
	    if( idx == -1 )
	        return null;
	    
	    return keyTable[ idx ];
	}
	protected int lookup(char[] buffer ){
		return lookup(buffer, 0, buffer.length);
	}
	
	protected final int lookup(char[] buffer, int start, int len) {
		if (hashTable != null)
			return lookup(buffer, start, len, hash(buffer, start, len) );
		for (int i = 0; i <= currEntry; i++) {
			if (CharArrayUtils.equals(buffer, start, len, keyTable[i]))
				return i;
		}
		return -1;
	}
	
	protected final int lookup(char[] buffer, int start, int len, int hash) {		
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
}
