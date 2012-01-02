/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Doug Schaefer
 */
public class CharArrayObjectMap <T> extends CharTable {
    public static final CharArrayObjectMap<?> EMPTY_MAP = new CharArrayObjectMap<Object>(0) {
        @Override
		public Object clone() { return this; }
        @Override
		public List<char[]> toList() { return Collections.emptyList(); }
        @Override
		public Object put(char[] key, int start, int length, Object value) {
        	throw new UnsupportedOperationException();
        }
    };
	/**
	 * @since 5.4
	 */
	@SuppressWarnings("unchecked")
	public static <T> CharArrayObjectMap<T> emptyMap() {
		return (CharArrayObjectMap<T>) EMPTY_MAP;
	}


	private Object[] valueTable;

	public CharArrayObjectMap(int initialSize) {
		super(initialSize);
		valueTable = new Object[capacity()];
	}
	
	public T put(char[] key, int start, int length, T value) {
		int i = addIndex(key, start, length);
		@SuppressWarnings("unchecked")
		T oldvalue = (T) valueTable[i];
		valueTable[i] = value;
		return oldvalue;
	}

	final public T put(char[] key, T value) {
		return put(key, 0, key.length, value);
	}
	
	@SuppressWarnings("unchecked")
	final public T get(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i >= 0)
			return (T) valueTable[i];
		return null;
	}
	
	final public T get(char[] key) {
		return get(key, 0, key.length);
	}
	
	@SuppressWarnings("unchecked")
	final public T getAt(int i) {
	    if (i < 0 || i > currEntry)
	        return null;
	    return (T) valueTable[i];
	}
	
	final public T remove(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i < 0)
			return null;

		@SuppressWarnings("unchecked")
		T value = (T) valueTable[i];

	    if (i < currEntry)
			System.arraycopy(valueTable, i + 1, valueTable, i, currEntry - i);
		
	    valueTable[currEntry] = null;
		
		removeEntry(i);
		
		return value;
	}
	
	@Override
	public Object clone() {
        @SuppressWarnings("unchecked")
		CharArrayObjectMap<T> newTable = (CharArrayObjectMap<T>) super.clone();
        newTable.valueTable = new Object[capacity()];
	    System.arraycopy(valueTable, 0, newTable.valueTable, 0, valueTable.length);

	    return newTable;
	}
	
	@Override
	protected void resize(int size) {
		Object[] oldValueTable = valueTable;
		valueTable = new Object[size];
		System.arraycopy(oldValueTable, 0, valueTable, 0, oldValueTable.length);
		super.resize(size);
	}
    
	@Override
	public void clear() {
		super.clear();
		for (int i = 0; i < capacity(); i++)
			valueTable[i] = null;
	}

    @Override
	protected int partition(Comparator<Object> c, int p, int r) {
        char[] x = keyTable[p];
        Object temp = null;
        int i = p;
        int j = r;
        
        while (true) {
            while (c.compare(keyTable[j], x) > 0) {
            	j--;
            }
            if (i < j) {
                while (c.compare(keyTable[i], x) < 0) {
                	i++;
                }
            }
            
            if (i < j) {
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
    
    public Object[] valueArray() {
	    Object[] values = new Object[size()];
	    System.arraycopy(valueTable, 0, values, 0, values.length);
	    return values;
	}

    public Object[] valueArray(Class<?> clazz) {
	    Object[] values= (Object[]) Array.newInstance(clazz, size());
	    System.arraycopy(valueTable, 0, values, 0, values.length);
	    return values;
	}

}
