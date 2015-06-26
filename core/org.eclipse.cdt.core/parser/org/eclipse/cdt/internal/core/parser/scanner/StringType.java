/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.parser.IToken;

/**
 * Utility class that provides some simple operations for string literals.
 */
@SuppressWarnings("nls")
public enum StringType {
	// Listed in order of "wideness"
	NARROW("", IToken.tSTRING),
	WIDE("L",  IToken.tLSTRING),
	UTF16("u", IToken.tUTF16STRING),
	UTF32("U", IToken.tUTF32STRING);
	
	
	private char[] prefix;
	private int tokenVal;
	
	private StringType(String prefix, int tokenVal) {
		this.prefix = prefix.toCharArray();
		this.tokenVal = tokenVal;
	}
	
	public char[] getPrefix() {
		return prefix;
	}
	
	public int getTokenValue() {
		return tokenVal;
	}
	
	/**
	 * Returns the StringType value that represesnts the 'wider'
	 * of the two given StringTypes.
	 * @thows NullPointerException if an argument is null
	 */
	public static StringType max(StringType st1, StringType st2) {
		return values()[Math.max(st1.ordinal(), st2.ordinal())];
	}

	/**
	 * Returns the StringType value for the given string literal type.
	 * 
	 * @see IToken#tSTRING
	 * @see IToken#tLSTRING
	 * @see IToken#tUTF16STRING
	 * @see IToken#tUTF32STRING
	 * 
	 * @throws IllegalArgumentException if the tokenVal does not represent a string literal
	 */
	public static StringType fromToken(int tokenVal) {
		switch(tokenVal) {
		case IToken.tSTRING:      return NARROW;
    	case IToken.tLSTRING:     return WIDE;
        case IToken.tUTF16STRING: return UTF16;
        case IToken.tUTF32STRING: return UTF32;
        default:
        	throw new IllegalArgumentException(tokenVal + " is not a string token");
		}
	}
}
