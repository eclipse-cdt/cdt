package org.eclipse.cdt.core.parser.util;

import java.util.Collection;

public interface ICharArrayMap<V> {

	/**
	 * Creates a new mapping in this map, uses the given array slice as the key.
	 * If the map previously contained a mapping for this key, the old value is replaced.
	 * @throws NullPointerException if chars is null
	 * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
	 */
	void put(char[] chars, int start, int length, V value);

	/**
	 * Creates a new mapping in this map, uses all of the given array as the key.
	 * If the map previously contained a mapping for this key, the old value is replaced.
	 * @throws NullPointerException if chars is null
	 */
	void put(char[] chars, V value);

	/**
	 * Returns the value to which the specified array slice is mapped in this map, 
	 * or null if the map contains no mapping for this key. 
	 * @throws NullPointerException if chars is null
	 * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
	 */
	V get(char[] chars, int start, int length);

	/**
	 * Returns the value to which the specified array is mapped in this map, 
	 * or null if the map contains no mapping for this key. 
	 * @throws NullPointerException if chars is null
	 */
	V get(char[] chars);

	/**
	 * Removes the mapping for the given array slice if present.
	 * Returns the value object that corresponded to the key
	 * or null if the key was not in the map.
	 * @throws NullPointerException if chars is null
	 * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
	 */
	V remove(char[] chars, int start, int length);

	/**
	 * Removes the mapping for the given array if present.
	 * Returns the value object that corresponded to the key
	 * or null if the key was not in the map.
	 * @throws NullPointerException if chars is null
	 * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
	 */
	V remove(char[] chars);

	/**
	 * Returns true if the given key has a value associated with it in the map.
	 * @throws NullPointerException if chars is null
	 * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
	 */
	boolean containsKey(char[] chars, int start, int length);

	/**
	 * Returns true if the given key has a value associated with it in the map.
	 * @throws NullPointerException if chars is null
	 * @throws IllegalArgumentException if the boundaries specified by start and length are out of range
	 */
	boolean containsKey(char[] chars);

	/**
	 * Returns true if the given value is contained in the map.
	 */
	boolean containsValue(V value);

	/** 
	 * Use this in a foreach loop.
	 */
	Collection<V> values();

	/**
	 * Removes all mappings from the map.
	 */
	void clear();

	/**
	 * Returns the number of mappings.
	 */
	int size();

	/**
	 * Returns true if the map is empty.
	 */
	boolean isEmpty();

}