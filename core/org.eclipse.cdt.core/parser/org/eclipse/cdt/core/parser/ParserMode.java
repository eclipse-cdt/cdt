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
	
	// follow inclusions, parse function/method bodies
	public static final ParserMode COMPLETE_PARSE = new ParserMode( 1 );
	
	// do not follow inclusions, do not parse function/method bodies
	public static final ParserMode QUICK_PARSE = new ParserMode( 2 );
	
	protected ParserMode( int value )
	{
		super( value );
	}
	 
}
