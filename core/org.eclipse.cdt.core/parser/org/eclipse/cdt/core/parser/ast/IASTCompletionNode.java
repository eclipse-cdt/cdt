/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
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
		
		// class member declaration type reference
		public static final CompletionKind FIELD_TYPE = new CompletionKind( 2 );
		
		// stand-alone declaration type reference
		public static final CompletionKind VARIABLE_TYPE = new CompletionKind( 3 );
		
		// function/method argument type reference
		public static final CompletionKind ARGUMENT_TYPE = new CompletionKind( 4 );
		
		// inside code body - name reference || int X::[ ]
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
		
		// any place where constructor parameters are expected 
		public static final CompletionKind CONSTRUCTOR_REFERENCE = new CompletionKind( 12 );
		
		// any place where exclusively a preprocessor directive is expected
		public static final CompletionKind PREPROCESSOR_DIRECTIVE = new CompletionKind( 13 );
		
		// any place where function parameters are expected 
		public static final CompletionKind FUNCTION_REFERENCE = new CompletionKind( 15 );
		
		// after a new expression
		public static final CompletionKind NEW_TYPE_REFERENCE = new CompletionKind( 16 );
		
		// inside something that does not reach the parser - (#ifdefed out/comment)
		public static final CompletionKind UNREACHABLE_CODE = new CompletionKind( 17 );
		
		// structs only
		public static final CompletionKind STRUCT_REFERENCE = new CompletionKind( 18 );
		
		// unions only
		public static final CompletionKind UNION_REFERENCE = new CompletionKind( 19 );
		
		// enums only
		public static final CompletionKind ENUM_REFERENCE = new CompletionKind( 20 );
		
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
	 * @return		the name of the function/constructor being completed in
	 * 					CONSTRUCTOR_REFERENCE
	 * 					FUNCTION_REFERENCE
	 */
	public String			getFunctionName();
	
	/**
	 * 
	 * @return		the IASTExpression representing the number of parameters
	 * input in the CONSTRUCTOR_REFERENCE/FUNCTION_REFERENCE context.  
	 */
	public IASTExpression   getFunctionParameters();
	
	/**
	 * @return		the prefix
	 */
	public String			getCompletionPrefix(); 
	
	/**
	 * @return		iterator of string keywords
	 */
	public Iterator 		getKeywords();
	
}
