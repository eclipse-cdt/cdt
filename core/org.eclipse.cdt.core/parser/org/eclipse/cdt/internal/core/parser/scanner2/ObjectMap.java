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
	
	public void clear(){
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

	public Object get(Object key) {
		int i = lookup(key);
		if (i >= 0)
			return valueTable[i];
		return null;
	}

	public Object getAt( int i ){
	    if( i < 0 || i > currEntry )
	        return null;
	    
	    return get( keyAt( i ) );
	}	
}
