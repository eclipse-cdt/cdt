/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui;

/**
 * 
 * Enter type comment.
 * 
 * @since Jul 30, 2002
 */
public class CDebugUIUtils
{
	static public String toHexAddressString( long address )
	{
		String tmp = Long.toHexString( address );
		char[] prefix = new char[10 - tmp.length()];
		prefix[0] = '0';
		prefix[1] = 'x';
		for ( int i = 2; i < prefix.length; ++i )
			prefix[i] = '0';
		return new String( prefix ) + tmp;
	} 
}
