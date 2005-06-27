/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Doug Schaefer
 */
public class CharArrayObjectMap extends CharTable {
    public static final CharArrayObjectMap EMPTY_MAP = new CharArrayObjectMap( 0 ){
        public Object clone()                         { return this; }
        public List toList()                		  { return Collections.EMPTY_LIST; }
        public Object put( char[] key, int start, int length, Object value ) 
        { throw new UnsupportedOperationException(); }
    };

	private Object[] valueTable;

	public CharArrayObjectMap(int initialSize) {
		super(initialSize);
		valueTable = new Object[capacity()];
	}
	
	public Object put(char[] key, int start, int length, Object value) {
		int i = addIndex(key, start, length);
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
	    return valueTable[i];
	}
	
	final public Object remove(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i < 0)
			return null;

		Object value = valueTable[i];

	    if (i < currEntry)
			System.arraycopy(valueTable, i + 1, valueTable, i, currEntry - i);
		
	    valueTable[currEntry] = null;
		
		removeEntry(i);
		
		return value;
	}
	
	public Object clone(){
        CharArrayObjectMap newTable = (CharArrayObjectMap) super.clone();
        newTable.valueTable = new Object[ capacity() ];
	    System.arraycopy(valueTable, 0, newTable.valueTable, 0, valueTable.length);

	    return newTable;
	}
	
	protected void resize(int size) {
		Object[] oldValueTable = valueTable;
		valueTable = new Object[size];
		System.arraycopy(oldValueTable, 0, valueTable, 0, oldValueTable.length);
		super.resize(size);
	}
    
	public void clear() {
		super.clear();
		for( int i = 0; i < capacity(); i++ )
			valueTable[i] = null;
	}

    protected int partition( Comparator c, int p, int r ){
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
    
    public Object [] valueArray(){
	    Object [] values = new Object[ size() ];
	    System.arraycopy( valueTable, 0, values, 0, values.length );
	    return values;
	}
}
