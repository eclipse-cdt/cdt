/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Jul 15, 2004
 */
package org.eclipse.cdt.core.parser.util;

import java.util.Collections;
import java.util.List;

/**
 * @author aniefer
 */
public class ObjectSet extends ObjectTable {
    public static final ObjectSet EMPTY_SET = new ObjectSet( 0 ){
        public Object clone()               { return this; }
        public List toList()                { return Collections.EMPTY_LIST; }
        public void put( Object key )       { throw new UnsupportedOperationException(); }
        public void addAll( List list )     { throw new UnsupportedOperationException(); }
        public void addAll( ObjectSet set ) { throw new UnsupportedOperationException(); }
    };

	public ObjectSet(int initialSize) {
		super( initialSize );
	}
	
	public void put(Object key ){
		add(key);
	}
	
	public void addAll( List list ){
	    if( list == null )
	        return;
	    
	    int size = list.size();
	    for( int i = 0; i < size; i++ ){
	        add( list.get( i ) );
	    }
	}
	
	public void addAll( ObjectSet set ){
	    if( set == null )
	        return;
	    int size = set.size();
	    for( int i = 0; i < size; i++ ){
	        add( set.keyAt( i ) );
	    }
	}
	
	public void addAll( Object[] objs ){
		if( objs == null )
			return;
		
		for (int i = 0; i < objs.length; i++) {
			if( objs[i] != null ) add( objs[i] );
		}
	}

	public boolean remove( Object key ) {
		int i = lookup(key);
		if (i < 0)
			return false;

		removeEntry(i);
		return true;
	}
}
