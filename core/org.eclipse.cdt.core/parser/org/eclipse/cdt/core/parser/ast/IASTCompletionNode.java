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

import java.util.Iterator;

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public interface IASTCompletionNode {

	public static class CompletionKind extends Enum
	{
		// x.[ ] x->[ ]
		public static final CompletionKind MEMBER_REFERENCE = new CompletionKind( 0 );
		// x::[ ]
		public static final CompletionKind SCOPED_REFERENCE = new CompletionKind( 1 );
		
		// class member declaration type reference
		public static final CompletionKind FIELD_TYPE = new CompletionKind( 2 );
		
		// stand-alone declaration type reference
		public static final CompletionKind VARIABLE_TYPE = new CompletionKind( 3 );
		
		// function/method argument type reference
		public static final CompletionKind ARGUMENT_TYPE = new CompletionKind( 4 );
		
		// inside code body - name reference
		public static final CompletionKind SINGLE_NAME_REFERENCE = new CompletionKind( 5 );
		
		// any place one can expect a type
		public static final CompletionKind TYPE_REFERENCE = new CompletionKind( 6 );
		
		// any place where one can expect a class name
		public static final CompletionKind CLASS_REFERENCE = new CompletionKind( 7 );
		
		// any place where a namespace name is expected
		public static final CompletionKind NAMESPACE_REFERENCE = new CompletionKind( 8 );
		
		// any place where an exception name is expected
		public static final CompletionKind EXCEPTION_REFERENCE = new CompletionKind( 9 );
		
		// any place where exclusively a preprocessor macro name would be expected  
		public static final CompletionKind MACRO_REFERENCE = new CompletionKind( 10 );
		
		// any place where function arguments are expected
		public static final CompletionKind FUNCTION_REFERENCE = new CompletionKind( 11 );
		
		// any place where constructor arguments are expected 
		public static final CompletionKind CONSTRUCTOR_REFERENCE = new CompletionKind( 12 );
		
		// any place where exclusively a keyword is expected 
		public static final CompletionKind KEYWORD = new CompletionKind( 13 );
		
		// any place where exclusively a preprocessor directive is expected
		public static final CompletionKind PREPROCESSOR_DIRECTIVE = new CompletionKind( 14 );
		
		// any place where a type or variable name is expected to be introduced
		public static final CompletionKind USER_SPECIFIED_NAME = new CompletionKind( 15 );
		
		// error condition -- a place in the grammar where there is nothing to lookup
		public static final CompletionKind NO_SUCH_KIND = new CompletionKind( 200 );
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
	
	/**
	 * @return
	 */
	public Iterator 		getKeywords();
	
}
