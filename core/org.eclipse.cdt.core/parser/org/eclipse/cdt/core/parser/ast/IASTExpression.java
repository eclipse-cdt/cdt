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
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;

/**
 * @author jcamelon
 *
 */
public interface IASTExpression extends ISourceElementCallbackDelegate
{
	public class Kind extends Enum
	{
		public static final Kind PRIMARY_EMPTY 				= new Kind( -1 );
		public static final Kind PRIMARY_INTEGER_LITERAL      = new Kind( 0 ); 
		public static final Kind PRIMARY_CHAR_LITERAL         = new Kind( 1 );
		public static final Kind PRIMARY_FLOAT_LITERAL        = new Kind( 2 );
		public static final Kind PRIMARY_STRING_LITERAL       = new Kind( 3 );
		public static final Kind PRIMARY_BOOLEAN_LITERAL      = new Kind( 4 );
		public static final Kind PRIMARY_THIS                 = new Kind( 5 );
		public static final Kind PRIMARY_BRACKETED_EXPRESSION = new Kind( 6 );
		public static final Kind ID_EXPRESSION                = new Kind( 7 );
		public static final Kind POSTFIX_SUBSCRIPT            = new Kind( 8 );
		public static final Kind POSTFIX_FUNCTIONCALL         = new Kind( 9 );
		public static final Kind POSTFIX_SIMPLETYPE_INT       = new Kind( 10 );
		public static final Kind POSTFIX_SIMPLETYPE_SHORT     = new Kind( 11 );
		public static final Kind POSTFIX_SIMPLETYPE_DOUBLE    = new Kind( 12 );
		public static final Kind POSTFIX_SIMPLETYPE_FLOAT     = new Kind( 13 );
		public static final Kind POSTFIX_SIMPLETYPE_CHAR      = new Kind( 14 );
		public static final Kind POSTFIX_SIMPLETYPE_WCHART    = new Kind( 15 );
		public static final Kind POSTFIX_SIMPLETYPE_SIGNED    = new Kind( 16 );
		public static final Kind POSTFIX_SIMPLETYPE_UNSIGNED  = new Kind( 17 );
		public static final Kind POSTFIX_SIMPLETYPE_BOOL      = new Kind( 18 );
		public static final Kind POSTFIX_SIMPLETYPE_LONG      = new Kind( 19 );
		public static final Kind POSTFIX_TYPENAME_IDENTIFIER  = new Kind( 20 );
		public static final Kind POSTFIX_TYPENAME_TEMPLATEID  = new Kind( 21 );
		public static final Kind POSTFIX_DOT_IDEXPRESSION     = new Kind( 22 );
		public static final Kind POSTFIX_ARROW_IDEXPRESSION   = new Kind( 23 );
		public static final Kind POSTFIX_DOT_TEMPL_IDEXPRESS  = new Kind( 24 ); 
		public static final Kind POSTFIX_ARROW_TEMPL_IDEXP    = new Kind( 25 );
		public static final Kind POSTFIX_DOT_DESTRUCTOR       = new Kind( 26 );
		public static final Kind POSTFIX_ARROW_DESTRUCTOR     = new Kind( 27 );
		public static final Kind POSTFIX_INCREMENT            = new Kind( 28 );
		public static final Kind POSTFIX_DECREMENT            = new Kind( 29 );
		public static final Kind POSTFIX_DYNAMIC_CAST         = new Kind( 30 );
		public static final Kind POSTFIX_REINTERPRET_CAST     = new Kind( 31 );
		public static final Kind POSTFIX_STATIC_CAST          = new Kind( 32 );
		public static final Kind POSTFIX_CONST_CAST           = new Kind( 33 );
		public static final Kind POSTFIX_TYPEID_EXPRESSION    = new Kind( 34 );
		public static final Kind POSTFIX_TYPEID_TYPEID        = new Kind( 35 );
		public static final Kind UNARY_INCREMENT              = new Kind( 36 );
		public static final Kind UNARY_DECREMENT              = new Kind( 37 );
		public static final Kind UNARY_STAR_CASTEXPRESSION    = new Kind( 38 );
		public static final Kind UNARY_AMPSND_CASTEXPRESSION  = new Kind( 39 );
		public static final Kind UNARY_PLUS_CASTEXPRESSION    = new Kind( 40 );
		public static final Kind UNARY_MINUS_CASTEXPRESSION   = new Kind( 41 );
		public static final Kind UNARY_NOT_CASTEXPRESSION     = new Kind( 42 );
		public static final Kind UNARY_TILDE_CASTEXPRESSION   = new Kind( 43 );
		public static final Kind UNARY_SIZEOF_UNARYEXPRESSION = new Kind( 44 );
		public static final Kind UNARY_SIZEOF_TYPEID          = new Kind( 45 );
		public static final Kind NEW_NEWTYPEID                = new Kind( 46 );
		public static final Kind NEW_TYPEID                   = new Kind( 47 );
		public static final Kind DELETE_CASTEXPRESSION        = new Kind( 48 );
		public static final Kind DELETE_VECTORCASTEXPRESSION  = new Kind( 49 );
		public static final Kind CASTEXPRESSION               = new Kind( 50 );
		public static final Kind PM_DOTSTAR                   = new Kind( 51 );
		public static final Kind PM_ARROWSTAR                 = new Kind( 52 );
		public static final Kind MULTIPLICATIVE_MULTIPLY      = new Kind( 53 );
		public static final Kind MULTIPLICATIVE_DIVIDE        = new Kind( 54 );
		public static final Kind MULTIPLICATIVE_MODULUS       = new Kind( 55 );
		public static final Kind ADDITIVE_PLUS                = new Kind( 56 );
		public static final Kind ADDITIVE_MINUS               = new Kind( 57 );
		public static final Kind SHIFT_LEFT                   = new Kind( 58 );
		public static final Kind SHIFT_RIGHT                  = new Kind( 59 );
		public static final Kind RELATIONAL_LESSTHAN          = new Kind( 60 );
		public static final Kind RELATIONAL_GREATERTHAN       = new Kind( 61 );
		public static final Kind RELATIONAL_LESSTHANEQUALTO   = new Kind( 62 );
		public static final Kind RELATIONAL_GREATERTHANEQUALTO= new Kind( 63 );
		public static final Kind EQUALITY_EQUALS              = new Kind( 64 );
		public static final Kind EQUALITY_NOTEQUALS           = new Kind( 65 );
		public static final Kind ANDEXPRESSION                = new Kind( 66 );
		public static final Kind EXCLUSIVEOREXPRESSION        = new Kind( 67 );
		public static final Kind INCLUSIVEOREXPRESSION        = new Kind( 68 );
		public static final Kind LOGICALANDEXPRESSION         = new Kind( 69 );
		public static final Kind LOGICALOREXPRESSION          = new Kind( 70 );
		public static final Kind CONDITIONALEXPRESSION_SIMPLE = new Kind( 71 );
		public static final Kind CONDITIONALEXPRESSION_HARD   = new Kind( 72 );
		public static final Kind THROWEXPRESSION              = new Kind( 72 );
		public static final Kind ASSIGNMENTEXPRESSION_NORMAL  = new Kind( 73 );
		public static final Kind ASSIGNMENTEXPRESSION_PLUS    = new Kind( 74 );
		public static final Kind ASSIGNMENTEXPRESSION_MINUS   = new Kind( 75 );
		public static final Kind ASSIGNMENTEXPRESSION_MULT    = new Kind( 76 );
		public static final Kind ASSIGNMENTEXPRESSION_DIV     = new Kind( 77 );
		public static final Kind ASSIGNMENTEXPRESSION_MOD     = new Kind( 78 );
		public static final Kind ASSIGNMENTEXPRESSION_LSHIFT  = new Kind( 79 );
		public static final Kind ASSIGNMENTEXPRESSION_RSHIFT  = new Kind( 80 );
		public static final Kind ASSIGNMENTEXPRESSION_AND     = new Kind( 81 );
		public static final Kind ASSIGNMENTEXPRESSION_OR      = new Kind( 82 );
		public static final Kind ASSIGNMENTEXPRESSION_XOR     = new Kind( 83 );
		public static final Kind EXPRESSIONLIST               = new Kind( 84 );
		
		
        /**
         * @param enumValue
         */
        private Kind(int enumValue)
        {
            super(enumValue);
        }
        		
	}
	
	public interface IASTNewExpressionDescriptor
	{
		public Iterator getNewPlacementExpressions();
		public Iterator getNewTypeIdExpressions();
		public Iterator getNewInitializerExpressions();
	}
	
	
	
	public Kind getExpressionKind(); 
	public IASTExpression getLHSExpression(); 
	public IASTExpression getRHSExpression();
	public IASTExpression getThirdExpression();
	public String getLiteralString(); 
	public String getTypeIdString(); 	
	public IASTNewExpressionDescriptor getNewExpressionDescriptor(); 
	
	public int evaluateExpression() throws ExpressionEvaluationException;
	
}
