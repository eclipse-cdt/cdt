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
 * cloned from CharArrayMap & CharArrayObjectMap
 * Created on Jul 14, 2004
 */
package org.eclipse.cdt.core.parser.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author aniefer
 */
public class ObjectMap extends ObjectTable {
    public static final ObjectMap EMPTY_MAP = new ObjectMap( 0 ){
        public Object clone()                         { return this; }
        public List toList()                		  { return Collections.EMPTY_LIST; }
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
	    
	    return valueTable[i];
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
	
    protected int partition( Comparator c, int p, int r ){
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

	public Object [] valueArray(){
		Object [] vals = new Object[ size() ];
		System.arraycopy( valueTable, 0, vals, 0, vals.length );
	    return vals;
	}
}
