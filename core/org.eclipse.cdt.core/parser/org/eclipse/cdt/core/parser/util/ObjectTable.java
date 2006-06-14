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
 * For use by the Parser Symbol Table
 * Created on Jul 15, 2004
 */
package org.eclipse.cdt.core.parser.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


/**
 * @author aniefer
 */
public abstract class ObjectTable extends HashTable implements Cloneable{  
	protected Object[] keyTable;

	public ObjectTable(int initialSize) {
		super(initialSize);
		keyTable = new Object[capacity()];
	}

	public Object clone(){
	    ObjectTable newTable = (ObjectTable) super.clone();
        
        int size = capacity();
        newTable.keyTable = new Object[ size ];
        System.arraycopy(keyTable, 0, newTable.keyTable, 0, keyTable.length);
        
	    return newTable;
	}
	
	public List toList(){
	    List list = new ArrayList( size() );
	    int size = size();
	    for( int i = 0; i < size; i++ ){
	        list.add( keyAt( i ) );
	    }
	    return list;
	}

	public Object keyAt( int i ){
	    if( i < 0 || i > currEntry )
	        return null;
	    
	    return keyTable[ i ];
	}
	
	public void clear(){
		super.clear();
	    for( int i = 0; i < keyTable.length; i++ )
	        keyTable[i] = null;
	}
	
	protected final int hash( int pos ){
	    return hash(keyTable[pos]);
	}
	
	private int hash(Object obj) {
	    return obj.hashCode() & ((capacity() * 2) - 1);
	}
	
	protected void resize(int size) {
		Object[] oldKeyTable = keyTable;
		keyTable = new Object[size];
		System.arraycopy(oldKeyTable, 0, keyTable, 0, oldKeyTable.length);

		super.resize(size);
	}
	
	protected final int add(Object obj) {
		int pos = lookup(obj);
		if (pos != -1)
			return pos;
		
		if ( (currEntry + 1) >= capacity()) {
			resize();
		}
		currEntry++;
		keyTable[currEntry] = obj;
		linkIntoHashTable(currEntry, hash(obj));
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
	
	protected final int lookup(Object buffer ){
		
		if (hashTable != null) {
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
		for (int i = 0; i <= currEntry; i++) {
			if (buffer.equals( keyTable[i] ) )
				return i;
		}
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
	
	public Object [] keyArray( Class c ){
		Object [] keys = (Object[]) Array.newInstance( c, size() );
        System.arraycopy( keyTable, 0, keys, 0, keys.length );
        return keys;
	}
}
