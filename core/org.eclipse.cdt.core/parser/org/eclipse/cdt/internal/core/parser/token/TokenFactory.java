/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerContext;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerData;

/**
 * @author johnc
 */
public class TokenFactory {
	
	static final String MAX_LONG_STRING = Long.toString( Long.MAX_VALUE );
	static final int MAX_LONG_STRING_LENGTH = MAX_LONG_STRING.length();
	static final String MAX_HEX_LONG_STRING = "0x" + Long.toString( Long.MAX_VALUE, 16 ); //$NON-NLS-1$
	static final int MAX_HEX_LONG_STRING_LENGTH = MAX_HEX_LONG_STRING.length();
		
	public static IToken createIntegerToken( String value, IScannerData scannerData )
	{
		if( value.length() > MAX_LONG_STRING_LENGTH || value.compareTo( MAX_LONG_STRING ) > 0 )		
			return createUniquelyImagedToken( IToken.tINTEGER, value, scannerData );
		if( scannerData.getContextStack().getCurrentContext().getKind() == IScannerContext.ContextKind.MACROEXPANSION )
			return new IntegerExpansionToken( IToken.tINTEGER, Long.parseLong(value ), scannerData.getContextStack() );
			
		return new IntegerToken( IToken.tINTEGER, Long.parseLong( value ), scannerData.getContextStack() );		
	}
	
	public static IToken createHexadecimalIntegerToken( String value, IScannerData scannerData )
	{
		if( value.length() > MAX_HEX_LONG_STRING_LENGTH || value.compareTo( MAX_HEX_LONG_STRING ) > 0 )
			return createUniquelyImagedToken( IToken.tHEXINT, value, scannerData );
		if( scannerData.getContextStack().getCurrentContext().getKind() == IScannerContext.ContextKind.MACROEXPANSION )
			return new HexIntegerExpansionToken( IToken.tHEXINT, value, scannerData.getContextStack() );
			
		return new HexIntegerToken( IToken.tHEXINT, value, scannerData.getContextStack() );		
		
	}
	
	
	public static IToken createToken( int tokenType, IScannerData scannerData )
	{
		if( scannerData.getContextStack().getCurrentContext().getKind() == IScannerContext.ContextKind.MACROEXPANSION )
			return new SimpleExpansionToken( tokenType, scannerData.getContextStack() );
		
		return new SimpleToken(	tokenType, scannerData.getContextStack() );
	}

	/**
	 * @param type
	 * @param image
	 * @param scannerData
	 * @return
	 */
	public static IToken createUniquelyImagedToken(int type, String image, IScannerData scannerData) {
		if( scannerData.getContextStack().getCurrentContext().getKind() == IScannerContext.ContextKind.MACROEXPANSION )
			return new ImagedExpansionToken( type, scannerData.getContextStack(), image );

		return new ImagedToken(type, scannerData.getContextStack(), image );
	}
	
	public static IToken createStandAloneToken( int type, String image )
	{
		return new ImagedToken( type, image);
	}
}
