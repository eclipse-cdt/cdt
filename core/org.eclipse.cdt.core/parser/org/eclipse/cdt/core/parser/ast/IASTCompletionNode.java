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
package org.eclipse.cdt.core.parser.ast;

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public interface IASTCompletionNode {

	public static class CompletionKind extends Enum
	{
		public static final CompletionKind MEMBER_REFERENCE = new CompletionKind( 0 );
		public static final CompletionKind SCOPED_REFERENCE = new CompletionKind( 1 );
		public static final CompletionKind FIELD_TYPE = new CompletionKind( 2 );
		public static final CompletionKind VARIABLE_TYPE = new CompletionKind( 3 );
		public static final CompletionKind ARGUMENT_TYPE = new CompletionKind( 4 );
		public static final CompletionKind SINGLE_NAME_REFERENCE = new CompletionKind( 5 );
		public static final CompletionKind TYPE_REFERENCE = new CompletionKind( 6 );
		public static final CompletionKind CLASS_REFERENCE = new CompletionKind( 7 );
		public static final CompletionKind NAMESPACE_REFERENCE = new CompletionKind( 8 );
		public static final CompletionKind EXCEPTION_REFERENCE = new CompletionKind( 9 );
		public static final CompletionKind MACRO_REFERENCE = new CompletionKind( 10 );
		public static final CompletionKind FUNCTION_REFERENCE = new CompletionKind( 11 );
		public static final CompletionKind CONSTRUCTOR_REFERENCE = new CompletionKind( 12 );
		public static final CompletionKind KEYWORD = new CompletionKind( 13 );
		
		//TODO MORE TO COME
		/**
		 * @param enumValue
		 */
		protected CompletionKind(int enumValue) {
			super(enumValue);
		}
		
	}


	/**
	 * @return		kind of completion expected
	 */
	public CompletionKind	getCompletionKind();
	
	/**
	 * @return		the scope the code completion is within
	 * 				should never be null 
	 */
	public IASTScope		getCompletionScope(); 
	
	/**
	 * @return		the context (inter-statement) 
	 * 		e.g.  LHS of postfix expression a->b, a.b or qualified name a::b is 'a'	
	 * 		this can be null
	 */
	public IASTNode			getCompletionContext(); 

	/**
	 * @return		the prefix
	 */
	public String			getCompletionPrefix(); 
}
