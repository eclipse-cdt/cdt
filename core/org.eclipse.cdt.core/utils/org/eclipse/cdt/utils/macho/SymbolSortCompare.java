package org.eclipse.cdt.utils.macho;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.Comparator;

public class SymbolSortCompare implements Comparator {
	public int compare( Object o1, Object o2 ) {
        String s1 = o1.toString().toLowerCase();
        String s2 = o2.toString().toLowerCase();

        while( s1.length() > 0 && s1.charAt( 0 ) == '_' )
            s1 = s1.substring( 1 );

        while( s2.length() > 0 && s2.charAt( 0 ) == '_' )
            s2 = s2.substring( 1 );

        return s1.compareTo( s2 );
	}
}

