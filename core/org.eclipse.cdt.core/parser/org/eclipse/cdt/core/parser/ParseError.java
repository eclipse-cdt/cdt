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
 */
public class ParseError extends Error {

	private final ParseErrorKind errorKind;

	public static class ParseErrorKind extends Enum
	{
		// the method called is not implemented in this particular implementation
		public static final ParseErrorKind METHOD_NOT_IMPLEMENTED = new ParseErrorKind( 0 );
		
		// offset specified is within a section of code #if'd out by the preprocessor 
		// semantic context cannot be provided in this case
		public static final ParseErrorKind OFFSETDUPLE_UNREACHABLE = new ParseErrorKind( 1 );
		
		// offset range specified is not a valid identifier or qualified name
		// semantic context cannot be provided in this case
		public static final ParseErrorKind OFFSET_RANGE_NOT_NAME = new ParseErrorKind( 2 );

		public static final ParseErrorKind TIMEOUT_OR_CANCELLED = new ParseErrorKind( 3 );
		
		/**
		 * @param enumValue
		 */
		protected ParseErrorKind(int enumValue) {
			super(enumValue);
		}
	}

	public ParseErrorKind getErrorKind()
	{
		return errorKind;
	}
	
	public ParseError( ParseErrorKind kind )
	{
		errorKind = kind;
	}
}
