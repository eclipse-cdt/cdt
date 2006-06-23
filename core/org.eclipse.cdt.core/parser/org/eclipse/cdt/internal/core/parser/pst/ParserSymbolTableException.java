/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.pst;

import org.eclipse.cdt.core.parser.IProblem;

/**
 * @author aniefer
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

	public static final int r_InternalError			  = -1;
	public static final int r_Ambiguous 			  =  0;
	public static final int r_BadTypeInfo   		  =  1;
	public static final int r_CircularInheritance	  =  2;
	public static final int r_InvalidOverload		  =  3;
	public static final int r_BadTemplate			  =  4;
	public static final int r_InvalidUsing			  =  5;
	public static final int r_BadVisibility			  =  6;
	public static final int r_UnableToResolveFunction =  7;
	public static final int r_BadTemplateArgument     =  8;
	public static final int r_BadTemplateParameter    =  9;
	public static final int r_RedeclaredTemplateParam = 10;
	public static final int r_RecursiveTemplate       = 11;
	public int reason = -1;
	
	/**
	 * @return
	 */
	public int createProblemID() {
		switch( reason )
		{
			case r_Ambiguous:
				return IProblem.SEMANTIC_AMBIGUOUS_LOOKUP;
			case r_BadTypeInfo:
				return IProblem.SEMANTIC_INVALID_TYPE;
			case r_CircularInheritance:
				return IProblem.SEMANTIC_CIRCULAR_INHERITANCE;
			case r_InvalidOverload:
				return IProblem.SEMANTIC_INVALID_OVERLOAD;
			case r_BadTemplate:
				return IProblem.SEMANTIC_INVALID_TEMPLATE;
			case r_InvalidUsing:
				return IProblem.SEMANTIC_INVALID_USING;
			case r_BadVisibility:
				return IProblem.SEMANTIC_BAD_VISIBILITY;
			case r_UnableToResolveFunction:
				return IProblem.SEMANTIC_UNABLE_TO_RESOLVE_FUNCTION;
			case r_BadTemplateArgument:
				return IProblem.SEMANTIC_INVALID_TEMPLATE_ARGUMENT;
			case r_BadTemplateParameter:
				return IProblem.SEMANTIC_INVALID_TEMPLATE_PARAMETER;
			case r_RedeclaredTemplateParam:
				return IProblem.SEMANTIC_REDECLARED_TEMPLATE_PARAMETER;
			case r_RecursiveTemplate:
				return IProblem.SEMANTIC_RECURSIVE_TEMPLATE_INSTANTIATION;
			default:
//				assert false : this;
				return -1;
		}
	}
	
}
