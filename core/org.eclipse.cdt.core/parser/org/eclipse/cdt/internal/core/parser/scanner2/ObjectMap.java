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
 * cloned from CharArrayMap & CharArrayObjectMap
 * Created on Jul 14, 2004
 */
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.Comparator;

/**
 * @author aniefer
 */
public class ObjectMap extends HashTable{
    public static final ObjectMap EMPTY_MAP = new ObjectMap( 0 ){
        public Object clone()                         { return this; }
        public Object put( Object key, Object value ) { throw new UnsupportedOperationException(); }
    };
    
	private Object[] valueTable;

	public ObjectMap(int initialSize) {
	    super( initialSize );
		
		valueTable = new Object[ capacity() ];
	}

	public Object clone(){
	    ObjectMap newMap = (ObjectMap) super.clone();
	    
	    newMap.valueTable = new Object[ capacity() ];
	    
	    System.arraycopy(valueTable, 0, newMap.valueTable, 0, valueTable.length);
	    return newMap;
	}
	
	final public void clear(){
	    super.clear();
	    for( int i = 0; i < valueTable.length; i++ ){
	        valueTable[i] = null;
	    }
	}
	
	protected void resize(int size) {
		Object[] oldValueTable = valueTable;
		valueTable = new Object[size];
		System.arraycopy(oldValueTable, 0, valueTable, 0, oldValueTable.length);
			
		super.resize( size );
	}
	
	public Object put(Object key, Object value) {
		int i = add(key);
		Object oldvalue = valueTable[i];
		valueTable[i] = value;
		return oldvalue;
	}

	final public Object get(Object key) {
		int i = lookup(key);
		if (i >= 0)
			return valueTable[i];
		return null;
	}

	final public Object getAt( int i ){
	    if( i < 0 || i > currEntry )
	        return null;
	    
	    return get( keyAt( i ) );
	}

	final public boolean isEmpty(){
	    return currEntry == -1;
	}
	
	final public Object remove( Object key ) {
	    if( key == null )
	        return null;
		int i = lookup(key);
		if (i < 0)
			return null;

		Object value = valueTable[i];
		removeEntry(i);
		
		return value;
	}
	
	final protected void removeEntry(int i) {
		// Remove the entry from the valueTable, shifting everything over if necessary
		if (i < currEntry)
			System.arraycopy(valueTable, i + 1, valueTable, i, currEntry - i);
		valueTable[currEntry] = null;

		// Make sure you remove the value before calling super where currEntry will change
		super.removeEntry(i);
	}
	
    final public void sort( Comparator c ) {
        if( size() > 1 ){
	        quickSort( c, 0, size() - 1 );
	        
	        rehash( size(), false );
        }
    }	
    
    private void quickSort( Comparator c, int p, int r ){
        if( p < r ){
            int q = partition( c, p, r );
            if( p < q )   quickSort( c, p, q );
            if( ++q < r ) quickSort( c, q, r );
        }
    }
    private int partition( Comparator c, int p, int r ){
        Object x = keyTable[ p ];
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
                keyTable[i] = temp;
                
                temp = valueTable[j];
                valueTable[j] = valueTable[i];
                valueTable[i] = temp;
            } else {
                return j;
            }
        }
    }
}
