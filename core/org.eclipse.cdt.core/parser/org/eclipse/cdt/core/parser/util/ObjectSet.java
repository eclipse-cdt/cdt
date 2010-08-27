/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM Corporation) - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.util.Collections;
import java.util.List;

public class ObjectSet<T> extends ObjectTable<T> {
	/**
	 * Represents the empty ObjectSet
	 */
	@SuppressWarnings("rawtypes")
    public static final ObjectSet EMPTY_SET = new ObjectSet( 0 ){
    	@Override public Object clone()               { return this; }
    	@Override public List toList()                { return Collections.EMPTY_LIST; }
    	@Override public void put( Object key )       { throw new UnsupportedOperationException(); }
    	@Override public void addAll( List list )     { throw new UnsupportedOperationException(); }
    	@Override public void addAll( ObjectSet set ) { throw new UnsupportedOperationException(); }
    };

    /**
	 * @since 5.1
	 */
    @SuppressWarnings("unchecked")
	public static <T> ObjectSet<T> emptySet() {
    	return EMPTY_SET;
    }
    /**
     * Construct an empty ObjectSet, allocating an initial storage for the specified
     * number of elements
     * @param initialSize
     */
	public ObjectSet(int initialSize) {
		super(initialSize);
	}
	
	/**
	 * Construct an ObjectSet populated with the specified items, or an empty ObjectSet
	 * if the parameter is null
	 * @param items
	 */
	public ObjectSet(T[] items) {
		super( items == null ? 2 : items.length );
		addAll( items );
	}
	
	/**
	 * Adds the specified item to the set, or no-ops if the key is null
	 * @param key the item to add (may be null)
	 */
	public void checkPut(T key) {
		if(key!=null)
			add(key);
	}
	
	/**
	 * Adds the specified item to the set
	 * @param key the (non-null) object to store
	 */
	public void put(T key){
		add(key);
	}
	
	/**
	 * Adds each item in the list to this ObjectSet, or no-ops if list is null
	 * @param list a list (may be null)
	 */
	public void addAll(List<T> list ) {
	    if( list == null )
	        return;
	    
	    int size = list.size();
	    for( int i = 0; i < size; i++ ){
	        add( list.get( i ) );
	    }
	}
	
	/**
	 * Adds each item in the specified ObjectSet, or no-ops if the set is null
	 * @param set a set (may be null)
	 */
	public void addAll(ObjectSet<? extends T> set ){
	    if( set == null )
	        return;
	    int size = set.size();
	    for( int i = 0; i < size; i++ ){
	        add( set.keyAt( i ) );
	    }
	}
	
	/**
	 * Adds each of the items in the specified array, or no-ops if the array is null
	 * @param objs an array (may be null)
	 */
	public void addAll(T[] objs ){
		if( objs == null )
			return;
		
		for (T obj : objs) {
			if( obj != null ) add( obj );
		}
	}

	/**
	 * Remove the specified object from this ObjectSet
	 * @param key the (non-null) object to remove
	 * @return whether an object was removed
	 */
	public boolean remove(T key ) {
		int i = lookup(key);
		if (i < 0)
			return false;

		removeEntry(i);
		return true;
	}
}