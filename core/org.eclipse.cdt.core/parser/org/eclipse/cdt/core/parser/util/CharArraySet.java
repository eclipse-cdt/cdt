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

public class CharArraySet extends CharTable {
	
    public static final CharArraySet EMPTY_SET = new CharArraySet( 0 ){
        @Override
		public Object clone()               { return this; }
        @Override
		public List<char[]> toList()                { return Collections.emptyList(); }
        @Override
		public void put( char[] key )       { throw new UnsupportedOperationException(); }
        @Override
		public void addAll( List<char[]> list )     { throw new UnsupportedOperationException(); }
        @Override
		public void addAll( CharArraySet set ) { throw new UnsupportedOperationException(); }
    };

	public CharArraySet(int initialSize) {
		super(initialSize);
	}
	
	public void put(char[] key ){
		addIndex(key);
	}
	
	public void addAll( List<char[]> list ){
	    if( list == null )
	        return;
	    
	    int size = list.size();
	    for( int i = 0; i < size; i++ ){
	        addIndex( list.get( i ) );
	    }
	}
	
	public void addAll( CharArraySet set ){
	    if( set == null )
	        return;
	    int size = set.size();
	    for( int i = 0; i < size; i++ ){
	        addIndex( set.keyAt( i ) );
	    }
	}
	
	final public boolean remove( char[] key ) {
		int i = lookup(key);
		if (i < 0)
			return false;

		removeEntry(i);
		return true;
	}

	@Override
	final public void clear(){
	    for( int i = 0; i < keyTable.length; i++ ){
	        keyTable[i] = null;
	        hashTable[ 2*i ] = 0;
	        hashTable[ 2*i + 1 ] = 0;
	        nextTable[i] = 0;
	    }
	    currEntry = -1;
	}
}
