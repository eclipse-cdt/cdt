/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.elf;


import java.util.Comparator;

/**
 * @deprecated This class is slated for removal, it is not used by the CDT classes
 */
public class SymbolSortCompare implements Comparator {
	public int compare( Object o1, Object o2 ) {
        String s1 = o1.toString();
        String s2 = o2.toString();

        while( s1.length() > 0 && s1.charAt( 0 ) == '_' )
            s1 = s1.substring( 1 );

        while( s2.length() > 0 && s2.charAt( 0 ) == '_' )
            s2 = s2.substring( 1 );

        return s1.compareToIgnoreCase( s2 );
	}
}

