/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.parser.pst;

/**
 * @author aniefer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ParserSymbolTableException extends Exception {

	/**
	 * Constructor for ParserSymbolTableException.
	 */
	public ParserSymbolTableException() {
		super();
	}

	/**
	 * Constructor for ParserSymbolTableException.
	 * @param int r: reason
	 */
	public ParserSymbolTableException( int r ) {
		reason = r;
	}

	public static final int r_InternalError			= -1;
	public static final int r_Ambiguous 			=  0;
	public static final int r_BadTypeInfo   		=  1;
	public static final int r_CircularInheritance	=  2;
	public static final int r_InvalidOverload		=  3;
	public static final int r_BadTemplate			=  4;
	public static final int r_InvalidUsing			=  5;
	public static final int r_BadVisibility			=  6;
	
	public int reason = -1;
}