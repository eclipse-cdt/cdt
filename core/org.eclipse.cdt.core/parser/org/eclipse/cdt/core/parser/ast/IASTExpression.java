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
public interface IASTExpression
{
	public class ExpressionKind extends Enum
	{
		public static final ExpressionKind PRIMARY_INTEGER_LITERAL      = new ExpressionKind( 0 ); 
		public static final ExpressionKind PRIMARY_CHAR_LITERAL         = new ExpressionKind( 1 );
		public static final ExpressionKind PRIMARY_FLOAT_LITERAL        = new ExpressionKind( 2 );
		public static final ExpressionKind PRIMARY_STRING_LITERAL       = new ExpressionKind( 3 );
		public static final ExpressionKind PRIMARY_BOOLEAN_LITERAL      = new ExpressionKind( 4 );
		public static final ExpressionKind PRIMARY_THIS                 = new ExpressionKind( 5 );
		public static final ExpressionKind PRIMARY_BRACKETED_EXPRESSION = new ExpressionKind( 6 );
		public static final ExpressionKind ID_EXPRESSION                = new ExpressionKind( 7 );
		public static final ExpressionKind POSTFIX_ARRAY                = new ExpressionKind( 8 );
		public static final ExpressionKind POSTFIX_CONSTRUCT            = new ExpressionKind( 9 );
		public static final ExpressionKind POSTFIX_SIMPLETYPE_CONSTRUCT = new ExpressionKind( 10 );
		public static final ExpressionKind POSTFIX_TYPENAME_IDENTIFIER  = new ExpressionKind( 11 );
		public static final ExpressionKind POSTFIX_TYPENAME_TEMPLATEID  = new ExpressionKind( 12 );
		public static final ExpressionKind POSTFIX_DOT_IDEXPRESSION     = new ExpressionKind( 13 );
		public static final ExpressionKind POSTFIX_ARROW_IDEXPRESSION   = new ExpressionKind( 14 );
		public static final ExpressionKind POSTFIX_DOT_DESTRUCTOR       = new ExpressionKind( 15 );
		public static final ExpressionKind POSTFIX_ARROW_DESTRUCTOR     = new ExpressionKind( 16 );
		public static final ExpressionKind POSTFIX_INCREMENT            = new ExpressionKind( 17 );
		public static final ExpressionKind POSTFIX_DECREMENT            = new ExpressionKind( 18 );
		public static final ExpressionKind POSTFIX_DYNAMIC_CAST         = new ExpressionKind( 19 );
		public static final ExpressionKind POSTFIX_REINTERPRET_CAST     = new ExpressionKind( 20 );
		public static final ExpressionKind POSTFIX_STATIC_CAST          = new ExpressionKind( 21 );
		public static final ExpressionKind POSTFIX_CONST_CAST           = new ExpressionKind( 22 );
		public static final ExpressionKind POSTFIX_TYPEID_EXPRESSION    = new ExpressionKind( 23 );
		public static final ExpressionKind POSTFIX_TYPEID_TYPEID        = new ExpressionKind( 24 );
		public static final ExpressionKind UNARY_INCREMENT              = new ExpressionKind( 25 );
		public static final ExpressionKind UNARY_DECREMENT              = new ExpressionKind( 26 );
		public static final ExpressionKind UNARY_STAR_CASTEXPRESSION    = new ExpressionKind( 27 );
		public static final ExpressionKind UNARY_AMPSND_CASTEXPRESSION  = new ExpressionKind( 28 );
		public static final ExpressionKind UNARY_PLUS_CASTEXPRESSION    = new ExpressionKind( 29 );
		public static final ExpressionKind UNARY_MINUS_CASTEXPRESSION   = new ExpressionKind( 30 );
		public static final ExpressionKind UNARY_NOT_CASTEXPRESSION     = new ExpressionKind( 31 );
		public static final ExpressionKind UNARY_TILDE_CASTEXPRESSION   = new ExpressionKind( 32 );
		public static final ExpressionKind UNARY_SIZEOF_UNARYEXPRESSION = new ExpressionKind( 33 );
		public static final ExpressionKind UNARY_SIZEOF_TYPEID          = new ExpressionKind( 34 );
		public static final ExpressionKind NEW_NEWTYPEID                = new ExpressionKind( 35 );
		public static final ExpressionKind NEW_TYPEID                   = new ExpressionKind( 36 );
		public static final ExpressionKind DELETE_CASTEXPRESSION        = new ExpressionKind( 37 );
		public static final ExpressionKind DELETE_VECTORCASTEXPRESSION  = new ExpressionKind( 38 );
		public static final ExpressionKind CASTEXPRESSION               = new ExpressionKind( 39 );
		public static final ExpressionKind PM_DOTSTAR                   = new ExpressionKind( 40 );
		public static final ExpressionKind PM_ARROWSTAR                 = new ExpressionKind( 41 );
		public static final ExpressionKind MULTIPLICATIVE_MULTIPLY      = new ExpressionKind( 42 );
		public static final ExpressionKind MULTIPLICATIVE_DIVIDE        = new ExpressionKind( 43 );
		public static final ExpressionKind MULTIPLICATIVE_MODULUS       = new ExpressionKind( 44 );
		public static final ExpressionKind ADDITIVE_PLUS                = new ExpressionKind( 45 );
		public static final ExpressionKind ADDITIVE_MINUS               = new ExpressionKind( 46 );
		public static final ExpressionKind SHIFT_LEFT                   = new ExpressionKind( 47 );
		public static final ExpressionKind SHIFT_RIGHT                  = new ExpressionKind( 48 );
		public static final ExpressionKind RELATIONAL_LESSTHAN          = new ExpressionKind( 49 );
		public static final ExpressionKind RELATIONAL_GREATERTHAN       = new ExpressionKind( 50 );
		public static final ExpressionKind RELATIONAL_LESSTHANEQUALTO   = new ExpressionKind( 51 );
		public static final ExpressionKind RELATIONAL_GREATERTHANEQUALTO= new ExpressionKind( 52 );
		public static final ExpressionKind EQUALITY_EQUALS              = new ExpressionKind( 53 );
		public static final ExpressionKind EQUALITY_NOTEQUALS           = new ExpressionKind( 54 );
		public static final ExpressionKind ANDEXPRESSION                = new ExpressionKind( 55 );
		public static final ExpressionKind EXCLUSIVEOREXPRESSION        = new ExpressionKind( 56 );
		public static final ExpressionKind INCLUSIVEOREXPRESSION        = new ExpressionKind( 57 );
		public static final ExpressionKind LOGICALANDEXPRESSION         = new ExpressionKind( 58 );
		public static final ExpressionKind LOGICALOREXPRESSION          = new ExpressionKind( 59 );
		public static final ExpressionKind CONDITIONALEXPRESSION        = new ExpressionKind( 60 );
		public static final ExpressionKind THROWEXPRESSION              = new ExpressionKind( 61 );
		public static final ExpressionKind ASSIGNMENTEXPRESSION         = new ExpressionKind( 62 );
		public static final ExpressionKind EXPRESSIONLIST               = new ExpressionKind( 63 );
		
		
        /**
         * @param enumValue
         */
        private ExpressionKind(int enumValue)
        {
            super(enumValue);
        }
		
	}
	
	public ExpressionKind getExpressionKind(); 
	public IASTExpression getLHSExpression(); 
	public IASTExpression getRHSExpression();
	public String getLiteralString(); 
	public String getTypeId(); 	
	public String getId(); 
	
	public int evaluateExpression() throws ExpressionEvaluationException;
	
}
