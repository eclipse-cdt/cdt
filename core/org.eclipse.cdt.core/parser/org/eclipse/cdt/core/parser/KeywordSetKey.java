/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;



public class KeywordSetKey extends Enum
{
	public static final KeywordSetKey EMPTY = new KeywordSetKey( 0 );
	public static final KeywordSetKey DECL_SPECIFIER_SEQUENCE = new KeywordSetKey( 1 );
	public static final KeywordSetKey DECLARATION = new KeywordSetKey( 2 );
	public static final KeywordSetKey STATEMENT = new KeywordSetKey(3);
	public static final KeywordSetKey BASE_SPECIFIER = new KeywordSetKey(4);
	public static final KeywordSetKey POST_USING = new KeywordSetKey( 5 );
	public static final KeywordSetKey FUNCTION_MODIFIER = new KeywordSetKey( 6 );
	public static final KeywordSetKey NAMESPACE_ONLY = new KeywordSetKey(6);
	public static final KeywordSetKey MACRO = new KeywordSetKey( 7 );
	public static final KeywordSetKey PP_DIRECTIVE = new KeywordSetKey( 8 );
	public static final KeywordSetKey EXPRESSION = new KeywordSetKey( 9 );
	public static final KeywordSetKey MEMBER = new KeywordSetKey(10);
	public static final KeywordSetKey ALL = new KeywordSetKey( 11 );
	/**
	 * @param enumValue
	 */
	protected KeywordSetKey(int enumValue) {
		super(enumValue);
	}
	
}