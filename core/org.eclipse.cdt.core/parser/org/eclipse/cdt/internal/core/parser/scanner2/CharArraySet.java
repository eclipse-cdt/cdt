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
 * Created on Jul 21, 2004
 */
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.util.Collections;
import java.util.List;

/**
 * @author aniefer
 */
public class CharArraySet extends CharTable {
	
    public static final CharArraySet EMPTY_SET = new CharArraySet( 0 ){
        public Object clone()               { return this; }
        public List toList()                { return Collections.EMPTY_LIST; }
        public void put( char[] key )       { throw new UnsupportedOperationException(); }
        public void addAll( List list )     { throw new UnsupportedOperationException(); }
        public void addAll( CharArraySet set ) { throw new UnsupportedOperationException(); }
    };

	public CharArraySet(int initialSize) {
		super(initialSize);
	}
	
	public void put(char[] key ){
		addIndex(key);
	}
	
	public void addAll( List list ){
	    if( list == null )
	        return;
	    
	    int size = list.size();
	    for( int i = 0; i < size; i++ ){
	        addIndex( (char[]) list.get( i ) );
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
