/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public abstract class ObjectTable<T> extends HashTable implements Iterable<T> {  
	protected T[] keyTable;

	@SuppressWarnings("unchecked")
	public ObjectTable(int initialSize) {
		super(initialSize);
		keyTable= (T[]) new Object[capacity()];
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
	    ObjectTable<T> newTable = (ObjectTable<T>) super.clone();
        
        int size = capacity();
        newTable.keyTable = (T[]) new Object[size];
        System.arraycopy(keyTable, 0, newTable.keyTable, 0, keyTable.length);
        
	    return newTable;
	}
	
	public List<T> toList() {
	    int size = size();
	    List<T> list = new ArrayList<T>(size);
	    for (int i = 0; i < size; i++) {
	        list.add(keyAt(i));
	    }
	    return list;
	}

	public T keyAt(int i) {
	    if (i < 0 || i > currEntry)
	        return null;
	    
	    return keyTable[i];
	}
	
	@Override
	public void clear() {
		super.clear();
	    for (int i = 0; i < keyTable.length; i++)
	        keyTable[i] = null;
	}
	
	@Override
	protected final int hash(int pos) {
	    return hash(keyTable[pos]);
	}
	
	private int hash(Object obj) {
	    return obj.hashCode() & ((capacity() * 2) - 1);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void resize(int size) {
		Object[] oldKeyTable = keyTable;
		keyTable = (T[]) new Object[size];
		System.arraycopy(oldKeyTable, 0, keyTable, 0, oldKeyTable.length);
		super.resize(size);
	}
	
	protected final int add(T obj) {
		int pos = lookup(obj);
		if (pos != -1)
			return pos;
		
		if ((currEntry + 1) >= capacity()) {
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
	
	protected final int lookup(Object buffer) {
		if (hashTable != null) {
			int hash = hash(buffer);
			
			if (hashTable[hash] == 0)
				return -1;
			
			int i = hashTable[hash] - 1;
			if (buffer.equals(keyTable[i]))
				return i;
			
			// Follow the next chain
			for (i = nextTable[i] - 1; i >= 0 && nextTable[i] != i + 1; i = nextTable[i] - 1) {
				if (buffer.equals(keyTable[i]))
					return i;
			}
				
			return -1;
		}
		for (int i = 0; i <= currEntry; i++) {
			if (buffer.equals(keyTable[i]))
				return i;
		}
		return -1;		
	}
	
	public boolean containsKey(T key) {
	    return lookup(key) != -1; 
	}
	
	public Object[] keyArray() {
	    Object[] keys = new Object[size()];
	    System.arraycopy(keyTable, 0, keys, 0, keys.length);
	    return keys;
	}
	
	@SuppressWarnings("unchecked")
	public <X> X[] keyArray(Class<X> c) {
		X[] keys = (X[]) Array.newInstance(c, size());
        System.arraycopy(keyTable, 0, keys, 0, keys.length);
        return keys;
	}

	public boolean isEquivalent(ObjectTable<T> other, IObjectMatcher matcher) {
		if (size() != other.size()) {
			return false;
		}
		
		for (int i = 0; i < keyTable.length; i++) {
			T key1 = keyTable[i];
			T key2 = other.keyTable[i];
			if (key1 != key2 && !matcher.isEquivalent(key1, key2)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @since 5.4
	 */
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int nextIndex;

			@Override
			public boolean hasNext() {
				return nextIndex < size();
			}

			@Override
			public T next() {
				T element = keyAt(nextIndex);
				if (element == null) {
					throw new NoSuchElementException();
				}
				nextIndex++;
				return element;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
