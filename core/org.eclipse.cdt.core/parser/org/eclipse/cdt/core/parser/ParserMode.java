/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * @author jcamelon
 *
 */
public class ParserMode extends Enum {
	
	// do not follow inclusions, do not parse function/method bodies
	public static final ParserMode QUICK_PARSE = new ParserMode( 1 );

	//follow inclusions, do not parse function/method bodies
	public static final ParserMode STRUCTURAL_PARSE = new ParserMode( 2 );
	
	// follow inclusions, parse function/method bodies
	public static final ParserMode COMPLETE_PARSE = new ParserMode( 3 );
	
	// follow inclusions, parse function/method bodies, stop at particular offset
	// provide optimized lookup capability for querying symbols
	public static final ParserMode COMPLETION_PARSE = new ParserMode( 4 );

	// follow inclusions, parse function/method bodies, stop at particular offset
	// provide specific semantic information about an offset range or selection
	public static final ParserMode SELECTION_PARSE = new ParserMode( 5 );
	
	protected ParserMode( int value )
	{
		super( value );
	}
	 
}
