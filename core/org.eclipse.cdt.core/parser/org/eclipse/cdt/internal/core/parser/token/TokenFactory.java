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

import java.util.List;

import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
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

	public static ITokenDuple createTokenDuple( IToken first, IToken last )
	{
		if( (first == last) && ( first instanceof ITokenDuple )) return (ITokenDuple) first;
		return new TokenDuple( first, last );
	}

	public static ITokenDuple createTokenDuple( IToken first, IToken last, List templateArgLists )
	{
		if( (first == last) && ( templateArgLists == null ) && ( first instanceof ITokenDuple )) return (ITokenDuple) first;
		return new TokenDuple( first, last, templateArgLists );
	}

	public static ITokenDuple createTokenDuple( ITokenDuple firstDuple, ITokenDuple secondDuple ){
		return new TokenDuple( firstDuple, secondDuple );
	}
}
