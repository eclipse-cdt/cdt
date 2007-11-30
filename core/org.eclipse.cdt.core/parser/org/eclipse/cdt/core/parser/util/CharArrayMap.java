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
 * Most methods are overloaded with two versions, one that uses a
 * section of a char[] as the key (a slice), and one that uses
 * the entire char[] as the key.
 * 
 * ex)
 * char[] key = "one two three".toCharArray();
 * map.put(key, 4, 3, new Integer(99));
 * map.get(key, 4, 3); // returns 99
 * map.get("two".toCharArray()); // returns 99
 * 
 * 
 * @author Mike Kucera
 */
public final class CharArrayMap/*<V>*/ {

	/**
	 * Wrapper class used as keys in the map. The purpose
	 * of this class is to provide implementations of
	 * equals() and hashCode() that operate on array slices.
	 * 
	 * This class is private so it is assumed that the arguments
	 * passed to the constructor are legal.
	 * 
	 * TODO: implement compareTo() so that the map may be sorted
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
        
        public int hashCode() {
            int result = 17;
            for(int i = start; i < start+length; i++) {
            	result = 37 * result + (int)buffer[i];
            }
            return result;
        }
        
        public String toString() {
        	return "'" + new String(buffer, start, length) + "'@(" + start + "," + length + ")";
        }
        
    }
    

    /**
     * Used to enforce preconditions. Note that the NPE thats thrown by
     * mutator methods is thrown from the Key constructor.
     */
    private static void checkBoundaries(char[] chars, int start, int length) {
    	if(start < 0)
    		throw new IndexOutOfBoundsException("start must be non-negative, got: " + start);//$NON-NLS-1$
    	if(length < 0)
    		throw new IndexOutOfBoundsException("length must be non-negative, got: " + length);//$NON-NLS-1$
    	if(start >= chars.length)
    		throw new IndexOutOfBoundsException("start is out of bounds, got: " + start);//$NON-NLS-1$
    	if(start + length > chars.length)
    		throw new IndexOutOfBoundsException("end is out of bounds, got: " + (start+length));//$NON-NLS-1$
    }
    
    
    private final Map/*<Key,V>*/ map;

    
    /**
     * Constructs an empty CharArrayMap with default initial capacity.
     */
    public CharArrayMap() {
    	map = new HashMap/*<Key,V>*/();
    }
    
    /**
     * Constructs an empty CharArrayMap with the given initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public CharArrayMap(int initialCapacity) {
    	map = new HashMap/*<Key,V>*/(initialCapacity);
    }
    
    
    /**
     * Creates a new mapping in this map, uses the given array slice as the key.
     * If the map previously contained a mapping for this key, the old value is replaced.
     * @throws NullPointerException if chars is null
     * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
     */
    public void put(char[] chars, int start, int length, /*V*/Object value) {
    	checkBoundaries(chars, start, length);
        map.put(new Key(chars, start, length), value);
    }

    /**
     * Creates a new mapping in this map, uses all of the given array as the key.
     * If the map previously contained a mapping for this key, the old value is replaced.
     * @throws NullPointerException if chars is null
     */
    public void put(char[] chars, /*V*/Object value) {
        map.put(new Key(chars), value);
    }

    /**
     * Returns the value to which the specified array slice is mapped in this map, 
     * or null if the map contains no mapping for this key. 
     * @throws NullPointerException if chars is null
     * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
     */
    public /*V*/Object get(char[] chars, int start, int length) {
    	checkBoundaries(chars, start, length);
        return map.get(new Key(chars, start, length));
    }

    /**
     * Returns the value to which the specified array is mapped in this map, 
     * or null if the map contains no mapping for this key. 
     * @throws NullPointerException if chars is null
     */
    public /*V*/Object get(char[] chars) {
        return map.get(new Key(chars));
    }

    
    /**
     * Removes the mapping for the given array slice if present.
     * Returns the value object that corresponded to the key
     * or null if the key was not in the map.
     * @throws NullPointerException if chars is null
     * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
     */
    public /*V*/Object remove(char[] chars, int start, int length) {
    	checkBoundaries(chars, start, length);
    	return map.remove(new Key(chars, start, length));
    }
    
    
    /**
     * Removes the mapping for the given array if present.
     * Returns the value object that corresponded to the key
     * or null if the key was not in the map.
     * @throws NullPointerException if chars is null
     * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
     */
    public /*V*/Object remove(char[] chars) {
    	return map.remove(new Key(chars));
    }
    
    /**
     * Returns true if the given key has a value associated with it in the map.
     * @throws NullPointerException if chars is null
     * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
     */
    public boolean containsKey(char[] chars, int start, int length) {
    	checkBoundaries(chars, start, length);
    	return map.containsKey(new Key(chars, start, length));
    }
    
    /**
     * Returns true if the given key has a value associated with it in the map.
     * @throws NullPointerException if chars is null
     * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
     */
    public boolean containsKey(char[] chars) {
    	return map.containsKey(new Key(chars));
    }
    
    /**
     * Returns true if the given value is contained in the map.
     */
    public boolean containsValue(/*V*/Object value) {
    	return map.containsValue(value);
    }
    
    /** 
     * Use this in a foreach loop.
     */
    public Collection/*<V>*/ values() {
        return map.values();
    }
    
    /**
     * Removes all mappings from the map.
     */
    public void clear() {
    	map.clear();
    }
    
    
    /**
     * Returns the number of mappings.
     */
    public int size() {
    	return map.size();
    }

    
    /**
     * Returns true if the map is empty.
     */
    public boolean isEmpty() {
    	return map.isEmpty();
    }
    
    
    /**
     * Returns a String representation of the map.
     */
    public String toString() {
    	return map.toString();
    }
   
}

