/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.internal.core;

/**
 * Utilities used by C/C++ Debug Plugin's classes.
 */
public class CDebugUtils {

	/**
	 * Returns the hexadecimal presentation of the given address.
	 *  
	 * @param address an address to be converted to hex
	 * @return the hexadecimal presentation of the given address
	 */
	public static String toHexAddressString( long address ) {
		String addressString = Long.toHexString( address );
		StringBuffer sb = new StringBuffer( 10 );
		sb.append( "0x" ); //$NON-NLS-1$
		for( int i = 0; i < 8 - addressString.length(); ++i ) {
			sb.append( '0' );
		}
		sb.append( addressString );
		return sb.toString();
	}
}
