/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * A facade for a Map that allows char[] slices to be used as keys.
 * 
 * @see ICharArrayMap for API docs
 * @author Mike Kucera
 */
public final class CharArrayMap<V> implements ICharArrayMap<V> {

	/**
	 * Wrapper class used as keys in the map. The purpose
	 * of this class is to provide implementations of
	 * equals() and hashCode() that operate on array slices.
	 * 
	 * This class is private so it is assumed that the arguments
	 * passed to the constructor are legal.
	 */
    private static final class Key {
        private final char[] buffer;
        private final int start;
        private final int length;

        public Key(char[] buffer, int start, int length) {
            this.buffer = buffer;
            this.length = length;
            this.start = start;
        }
        
        /**
         * @throws NullPointerException if buffer is null
         */
        public Key(char[] buffer) {
        	this.buffer = buffer;
        	this.length = buffer.length; // throws NPE
        	this.start = 0;
        }
        
        @Override 
        public boolean equals(Object x) {
        	if(this == x) 
        		return true;
        	if(!(x instanceof Key))  
        		return false;
        	
            Key k = (Key) x;
            if(length != k.length)
            	return false;
            
            for(int i = start, j = k.start; i < length; i++, j++) {
            	if(buffer[i] != k.buffer[j]) {
            		return false;
            	}
            }
            return true;
        }
        
        @Override 
        public int hashCode() {
            int result = 17;
            for(int i = start; i < start+length; i++) {
            	result = 37 * result + buffer[i];
            }
            return result;
        }
        
        @Override 
        public String toString() {
        	String slice = new String(buffer, start, length);
        	return "'" + slice + "'@(" + start + "," + length + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        
    }
    

    /**
     * Used to enforce preconditions. 
     * Note that the NPE thrown by mutator methods is thrown from the Key constructor.
     * 
     * @throws IndexOutOfBoundsException if boundaries are wrong in any way
     */
    private static void checkBoundaries(char[] chars, int start, int length) {
    	if(start < 0 || length < 0 || start >= chars.length || start + length > chars.length)
    		throw new IndexOutOfBoundsException("Buffer length: " + chars.length + //$NON-NLS-1$
    				                          ", Start index: " + start + //$NON-NLS-1$
    				                          ", Length: " + length); //$NON-NLS-1$
    }
    
    
    private final Map<Key,V> map;

    
    /**
     * Constructs an empty CharArrayMap with default initial capacity.
     */
    public CharArrayMap() {
    	map = new HashMap<Key,V>();
    }
    
    /**
     * Constructs an empty CharArrayMap with the given initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public CharArrayMap(int initialCapacity) {
    	map = new HashMap<Key,V>(initialCapacity);
    }
    
    
    public void put(char[] chars, int start, int length, V value) {
    	checkBoundaries(chars, start, length);
        map.put(new Key(chars, start, length), value);
    }

    
    public void put(char[] chars, V value) {
        map.put(new Key(chars), value);
    }

    
    public V get(char[] chars, int start, int length) {
    	checkBoundaries(chars, start, length);
        return map.get(new Key(chars, start, length));
    }

    
    public V get(char[] chars) {
        return map.get(new Key(chars));
    }

    
    public V remove(char[] chars, int start, int length) {
    	checkBoundaries(chars, start, length);
    	return map.remove(new Key(chars, start, length));
    }
    
    
    public V remove(char[] chars) {
    	return map.remove(new Key(chars));
    }

    
    public boolean containsKey(char[] chars, int start, int length) {
    	checkBoundaries(chars, start, length);
    	return map.containsKey(new Key(chars, start, length));
    }

    
    public boolean containsKey(char[] chars) {
    	return map.containsKey(new Key(chars));
    }
    
    
    public boolean containsValue(V value) {
    	return map.containsValue(value);
    }

    
    public Collection<V> values() {
        return map.values();
    }

    
    public void clear() {
    	map.clear();
    }

    
    public int size() {
    	return map.size();
    }


    public boolean isEmpty() {
    	return map.isEmpty();
    }
    
    
    /**
     * Returns a String representation of the map.
     */
    @Override 
    public String toString() {
    	return map.toString();
    }
   
}

