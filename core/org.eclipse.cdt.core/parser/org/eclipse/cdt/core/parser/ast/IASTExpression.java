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

import java.util.Hashtable;
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
		public static final Kind CONDITIONALEXPRESSION        = new Kind( 71 );
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

        private static final Hashtable names;
        static
		{
        	names = new Hashtable();
        	names.put( PRIMARY_EMPTY, "PRIMARY_EMPTY" );
        	names.put( PRIMARY_INTEGER_LITERAL  , "PRIMARY_INTEGER_LITERAL" );
        	names.put( PRIMARY_CHAR_LITERAL         , "PRIMARY_CHAR_LITERAL" );
        	names.put( PRIMARY_FLOAT_LITERAL        , "PRIMARY_FLOAT_LITERAL" );
        	names.put( PRIMARY_STRING_LITERAL       , "PRIMARY_STRING_LITERAL" );
        	names.put( PRIMARY_BOOLEAN_LITERAL      , "PRIMARY_BOOLEAN_LITERAL" );
        	names.put( PRIMARY_THIS                 , "PRIMARY_THIS");
        	names.put( PRIMARY_BRACKETED_EXPRESSION , "PRIMARY_BRACKETED_EXPRESSION");
        	names.put( ID_EXPRESSION                , "ID_EXPRESSION");
        	names.put( POSTFIX_SUBSCRIPT            , "POSTFIX_SUBSCRIPT");
        	names.put( POSTFIX_FUNCTIONCALL         , "POSTFIX_FUNCTIONCALL");
        	names.put( POSTFIX_SIMPLETYPE_INT       , "POSTFIX_SIMPLETYPE_INT");
        	names.put( POSTFIX_SIMPLETYPE_SHORT     , "POSTFIX_SIMPLETYPE_SHORT");
        	names.put( POSTFIX_SIMPLETYPE_DOUBLE    , "POSTFIX_SIMPLETYPE_DOUBLE");
        	names.put( POSTFIX_SIMPLETYPE_FLOAT     , "POSTFIX_SIMPLETYPE_FLOAT");
        	names.put( POSTFIX_SIMPLETYPE_CHAR      , "POSTFIX_SIMPLETYPE_CHAR");
        	names.put( POSTFIX_SIMPLETYPE_WCHART    , "POSTFIX_SIMPLETYPE_WCHART");
        	names.put( POSTFIX_SIMPLETYPE_SIGNED    , "POSTFIX_SIMPLETYPE_SIGNED");
        	names.put( POSTFIX_SIMPLETYPE_UNSIGNED  , "POSTFIX_SIMPLETYPE_UNSIGNED");
        	names.put( POSTFIX_SIMPLETYPE_BOOL      , "POSTFIX_SIMPLETYPE_BOOL");
        	names.put( POSTFIX_SIMPLETYPE_LONG      , "POSTFIX_SIMPLETYPE_LONG");
        	names.put( POSTFIX_TYPENAME_IDENTIFIER  , "POSTFIX_TYPENAME_IDENTIFIER");
        	names.put( POSTFIX_TYPENAME_TEMPLATEID, "POSTFIX_TYPENAME_TEMPLATEID" );
        	names.put( POSTFIX_DOT_IDEXPRESSION   , "POSTFIX_DOT_IDEXPRESSION");
        	names.put( POSTFIX_ARROW_IDEXPRESSION , "POSTFIX_ARROW_IDEXPRESSION");
        	names.put( POSTFIX_DOT_TEMPL_IDEXPRESS , "POSTFIX_DOT_TEMPL_IDEXPRESS");
        	names.put( POSTFIX_ARROW_TEMPL_IDEXP  , "POSTFIX_ARROW_TEMPL_IDEXP");
        	names.put( POSTFIX_DOT_DESTRUCTOR     , "POSTFIX_DOT_DESTRUCTOR");
        	names.put( POSTFIX_ARROW_DESTRUCTOR   , "POSTFIX_ARROW_DESTRUCTOR");
        	names.put( POSTFIX_INCREMENT          , "POSTFIX_INCREMENT");
        	names.put( POSTFIX_DECREMENT          , "POSTFIX_DECREMENT");
        	names.put( POSTFIX_DYNAMIC_CAST       , "POSTFIX_DYNAMIC_CAST");
        	names.put( POSTFIX_REINTERPRET_CAST   , "POSTFIX_REINTERPRET_CAST");
        	names.put( POSTFIX_STATIC_CAST        , "POSTFIX_STATIC_CAST");
        	names.put( POSTFIX_CONST_CAST         , "POSTFIX_CONST_CAST");
        	names.put( POSTFIX_TYPEID_EXPRESSION  , "POSTFIX_TYPEID_EXPRESSION");
        	names.put( POSTFIX_TYPEID_TYPEID        , "POSTFIX_TYPEID_TYPEID");
        	names.put( UNARY_INCREMENT              , "UNARY_INCREMENT");
        	names.put( UNARY_DECREMENT              , "UNARY_DECREMENT");
        	names.put( UNARY_STAR_CASTEXPRESSION    , "UNARY_STAR_CASTEXPRESSION");
        	names.put( UNARY_AMPSND_CASTEXPRESSION  , "UNARY_AMPSND_CASTEXPRESSION");
        	names.put( UNARY_PLUS_CASTEXPRESSION    , "UNARY_PLUS_CASTEXPRESSION");
        	names.put( UNARY_MINUS_CASTEXPRESSION   , "UNARY_MINUS_CASTEXPRESSION");
        	names.put( UNARY_NOT_CASTEXPRESSION     , "UNARY_NOT_CASTEXPRESSION");
        	names.put( UNARY_TILDE_CASTEXPRESSION   , "UNARY_TILDE_CASTEXPRESSION");
        	names.put( UNARY_SIZEOF_UNARYEXPRESSION , "UNARY_SIZEOF_UNARYEXPRESSION");
        	names.put( UNARY_SIZEOF_TYPEID          , "UNARY_SIZEOF_TYPEID");
        	names.put( NEW_NEWTYPEID                , "NEW_NEWTYPEID");
        	names.put( NEW_TYPEID                   , "NEW_TYPEID");
        	names.put( DELETE_CASTEXPRESSION        , "DELETE_CASTEXPRESSION");
        	names.put( DELETE_VECTORCASTEXPRESSION  , "DELETE_VECTORCASTEXPRESSION");
        	names.put( CASTEXPRESSION               , "CASTEXPRESSION");
        	names.put( PM_DOTSTAR                   , "PM_DOTSTAR");
        	names.put( PM_ARROWSTAR                 , "PM_ARROWSTAR");
        	names.put( MULTIPLICATIVE_MULTIPLY      , "MULTIPLICATIVE_MULTIPLY");
        	names.put( MULTIPLICATIVE_DIVIDE        , "MULTIPLICATIVE_DIVIDE");
        	names.put( MULTIPLICATIVE_MODULUS       , "MULTIPLICATIVE_MODULUS");
        	names.put( ADDITIVE_PLUS                , "ADDITIVE_PLUS");
        	names.put( ADDITIVE_MINUS               , "ADDITIVE_MINUS");
        	names.put( SHIFT_LEFT                   , "SHIFT_LEFT");
        	names.put( SHIFT_RIGHT                  , "SHIFT_RIGHT");
        	names.put( RELATIONAL_LESSTHAN          , "RELATIONAL_LESSTHAN");
        	names.put( RELATIONAL_GREATERTHAN       , "RELATIONAL_GREATERTHAN");
        	names.put( RELATIONAL_LESSTHANEQUALTO   , "RELATIONAL_LESSTHANEQUALTO");
        	names.put( RELATIONAL_GREATERTHANEQUALTO, "RELATIONAL_GREATERTHANEQUALTO" );
        	names.put( EQUALITY_EQUALS              , "EQUALITY_EQUALS");
        	names.put( EQUALITY_NOTEQUALS           , "EQUALITY_NOTEQUALS");
        	names.put( ANDEXPRESSION                , "ANDEXPRESSION");
        	names.put( EXCLUSIVEOREXPRESSION        , "EXCLUSIVEOREXPRESSION");
        	names.put( INCLUSIVEOREXPRESSION        , "INCLUSIVEOREXPRESSION");
        	names.put( LOGICALANDEXPRESSION         , "LOGICALANDEXPRESSION");
        	names.put( LOGICALOREXPRESSION          , "LOGICALOREXPRESSION");
        	names.put( CONDITIONALEXPRESSION        , "CONDITIONALEXPRESSION");
        	names.put( THROWEXPRESSION              , "THROWEXPRESSION");
        	names.put( ASSIGNMENTEXPRESSION_NORMAL  , "ASSIGNMENTEXPRESSION_NORMAL");
        	names.put( ASSIGNMENTEXPRESSION_PLUS    , "ASSIGNMENTEXPRESSION_PLUS");
        	names.put( ASSIGNMENTEXPRESSION_MINUS   , "ASSIGNMENTEXPRESSION_MINUS");
        	names.put( ASSIGNMENTEXPRESSION_MULT    , "ASSIGNMENTEXPRESSION_MULT");
        	names.put( ASSIGNMENTEXPRESSION_DIV     , "ASSIGNMENTEXPRESSION_DIV");
        	names.put( ASSIGNMENTEXPRESSION_MOD     , "ASSIGNMENTEXPRESSION_MOD");
        	names.put( ASSIGNMENTEXPRESSION_LSHIFT  , "ASSIGNMENTEXPRESSION_LSHIFT");
        	names.put( ASSIGNMENTEXPRESSION_RSHIFT  , "ASSIGNMENTEXPRESSION_RSHIFT");
        	names.put( ASSIGNMENTEXPRESSION_AND     , "ASSIGNMENTEXPRESSION_AND");
        	names.put( ASSIGNMENTEXPRESSION_OR      , "ASSIGNMENTEXPRESSION_OR");
        	names.put( ASSIGNMENTEXPRESSION_XOR     , "ASSIGNMENTEXPRESSION_XOR");
        	names.put( EXPRESSIONLIST               , "EXPRESSIONLIST");
        	
        }
		/**
		 * @return
		 */
		public String getKindName() {
			return (String) names.get(this);
		}
        
        public boolean isPostfixMemberReference()
		{
        	if( this == IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION ||  
        		this ==	IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION ||
        		this ==	IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS ||
        		this ==	IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP ||	
        		this ==	IASTExpression.Kind.PM_DOTSTAR ||
        		this ==	IASTExpression.Kind.PM_ARROWSTAR )
        		return true;
        	return false;

        }
        
        		
	}
	
	public interface IASTNewExpressionDescriptor extends ISourceElementCallbackDelegate
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
	public String     getIdExpression();
	public IASTTypeId getTypeId(); 	
	public IASTNewExpressionDescriptor getNewExpressionDescriptor(); 
	
	public int evaluateExpression() throws ASTExpressionEvaluationException;
	public void reconcileReferences() throws ASTNotImplementedException;
	public void purgeReferences() throws ASTNotImplementedException;
	
}
