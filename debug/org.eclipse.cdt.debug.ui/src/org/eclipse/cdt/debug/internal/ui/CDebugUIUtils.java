/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

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

	static public IRegion findWord( IDocument document, int offset ) 
	{
		int start = -1;
		int end = -1;
				
		try 
		{	
			int pos = offset;
			char c;
			
			while( pos >= 0 ) 
			{
				c = document.getChar( pos );
				if ( !Character.isJavaIdentifierPart( c ) )
					break;
				--pos;
			}
			
			start = pos;
			
			pos = offset;
			int length = document.getLength();
			
			while( pos < length ) 
			{
				c = document.getChar( pos );
				if ( !Character.isJavaIdentifierPart( c ) )
					break;
				++pos;
			}
			
			end = pos;
			
		} 
		catch( BadLocationException x ) 
		{
		}
		
		if ( start > -1 && end > -1 ) 
		{
			if ( start == offset && end == offset )
				return new Region( offset, 0 );
			else if ( start == offset )
				return new Region( start, end - start );
			else
				return new Region( start + 1, end - start - 1 );
		}
		
		return null;
	}
}
