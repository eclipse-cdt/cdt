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

import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.Enum;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;

/**
 * @author jcamelon
 *
 */
public interface IASTExpression extends ISourceElementCallbackDelegate, IASTNode, IASTOffsetableElement
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
		
		protected static final int LAST_KIND = 84;
		
		
        /**
         * @param enumValue
         */
        protected Kind(int enumValue)
        {
            super(enumValue);
        }

        public boolean isExtensionKind(){
        	return getEnumValue() > LAST_KIND;
        }
        
        private static final Hashtable names;
        static
		{
        	names = new Hashtable();
        	names.put( PRIMARY_EMPTY, "PRIMARY_EMPTY" ); //$NON-NLS-1$
        	names.put( PRIMARY_INTEGER_LITERAL  , "PRIMARY_INTEGER_LITERAL" ); //$NON-NLS-1$
        	names.put( PRIMARY_CHAR_LITERAL         , "PRIMARY_CHAR_LITERAL" ); //$NON-NLS-1$
        	names.put( PRIMARY_FLOAT_LITERAL        , "PRIMARY_FLOAT_LITERAL" ); //$NON-NLS-1$
        	names.put( PRIMARY_STRING_LITERAL       , "PRIMARY_STRING_LITERAL" ); //$NON-NLS-1$
        	names.put( PRIMARY_BOOLEAN_LITERAL      , "PRIMARY_BOOLEAN_LITERAL" ); //$NON-NLS-1$
        	names.put( PRIMARY_THIS                 , "PRIMARY_THIS"); //$NON-NLS-1$
        	names.put( PRIMARY_BRACKETED_EXPRESSION , "PRIMARY_BRACKETED_EXPRESSION"); //$NON-NLS-1$
        	names.put( ID_EXPRESSION                , "ID_EXPRESSION"); //$NON-NLS-1$
        	names.put( POSTFIX_SUBSCRIPT            , "POSTFIX_SUBSCRIPT"); //$NON-NLS-1$
        	names.put( POSTFIX_FUNCTIONCALL         , "POSTFIX_FUNCTIONCALL"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_INT       , "POSTFIX_SIMPLETYPE_INT"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_SHORT     , "POSTFIX_SIMPLETYPE_SHORT"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_DOUBLE    , "POSTFIX_SIMPLETYPE_DOUBLE"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_FLOAT     , "POSTFIX_SIMPLETYPE_FLOAT"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_CHAR      , "POSTFIX_SIMPLETYPE_CHAR"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_WCHART    , "POSTFIX_SIMPLETYPE_WCHART"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_SIGNED    , "POSTFIX_SIMPLETYPE_SIGNED"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_UNSIGNED  , "POSTFIX_SIMPLETYPE_UNSIGNED"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_BOOL      , "POSTFIX_SIMPLETYPE_BOOL"); //$NON-NLS-1$
        	names.put( POSTFIX_SIMPLETYPE_LONG      , "POSTFIX_SIMPLETYPE_LONG"); //$NON-NLS-1$
        	names.put( POSTFIX_TYPENAME_IDENTIFIER  , "POSTFIX_TYPENAME_IDENTIFIER"); //$NON-NLS-1$
        	names.put( POSTFIX_TYPENAME_TEMPLATEID, "POSTFIX_TYPENAME_TEMPLATEID" ); //$NON-NLS-1$
        	names.put( POSTFIX_DOT_IDEXPRESSION   , "POSTFIX_DOT_IDEXPRESSION"); //$NON-NLS-1$
        	names.put( POSTFIX_ARROW_IDEXPRESSION , "POSTFIX_ARROW_IDEXPRESSION"); //$NON-NLS-1$
        	names.put( POSTFIX_DOT_TEMPL_IDEXPRESS , "POSTFIX_DOT_TEMPL_IDEXPRESS"); //$NON-NLS-1$
        	names.put( POSTFIX_ARROW_TEMPL_IDEXP  , "POSTFIX_ARROW_TEMPL_IDEXP"); //$NON-NLS-1$
        	names.put( POSTFIX_DOT_DESTRUCTOR     , "POSTFIX_DOT_DESTRUCTOR"); //$NON-NLS-1$
        	names.put( POSTFIX_ARROW_DESTRUCTOR   , "POSTFIX_ARROW_DESTRUCTOR"); //$NON-NLS-1$
        	names.put( POSTFIX_INCREMENT          , "POSTFIX_INCREMENT"); //$NON-NLS-1$
        	names.put( POSTFIX_DECREMENT          , "POSTFIX_DECREMENT"); //$NON-NLS-1$
        	names.put( POSTFIX_DYNAMIC_CAST       , "POSTFIX_DYNAMIC_CAST"); //$NON-NLS-1$
        	names.put( POSTFIX_REINTERPRET_CAST   , "POSTFIX_REINTERPRET_CAST"); //$NON-NLS-1$
        	names.put( POSTFIX_STATIC_CAST        , "POSTFIX_STATIC_CAST"); //$NON-NLS-1$
        	names.put( POSTFIX_CONST_CAST         , "POSTFIX_CONST_CAST"); //$NON-NLS-1$
        	names.put( POSTFIX_TYPEID_EXPRESSION  , "POSTFIX_TYPEID_EXPRESSION"); //$NON-NLS-1$
        	names.put( POSTFIX_TYPEID_TYPEID        , "POSTFIX_TYPEID_TYPEID"); //$NON-NLS-1$
        	names.put( UNARY_INCREMENT              , "UNARY_INCREMENT"); //$NON-NLS-1$
        	names.put( UNARY_DECREMENT              , "UNARY_DECREMENT"); //$NON-NLS-1$
        	names.put( UNARY_STAR_CASTEXPRESSION    , "UNARY_STAR_CASTEXPRESSION"); //$NON-NLS-1$
        	names.put( UNARY_AMPSND_CASTEXPRESSION  , "UNARY_AMPSND_CASTEXPRESSION"); //$NON-NLS-1$
        	names.put( UNARY_PLUS_CASTEXPRESSION    , "UNARY_PLUS_CASTEXPRESSION"); //$NON-NLS-1$
        	names.put( UNARY_MINUS_CASTEXPRESSION   , "UNARY_MINUS_CASTEXPRESSION"); //$NON-NLS-1$
        	names.put( UNARY_NOT_CASTEXPRESSION     , "UNARY_NOT_CASTEXPRESSION"); //$NON-NLS-1$
        	names.put( UNARY_TILDE_CASTEXPRESSION   , "UNARY_TILDE_CASTEXPRESSION"); //$NON-NLS-1$
        	names.put( UNARY_SIZEOF_UNARYEXPRESSION , "UNARY_SIZEOF_UNARYEXPRESSION"); //$NON-NLS-1$
        	names.put( UNARY_SIZEOF_TYPEID          , "UNARY_SIZEOF_TYPEID"); //$NON-NLS-1$
        	names.put( NEW_NEWTYPEID                , "NEW_NEWTYPEID"); //$NON-NLS-1$
        	names.put( NEW_TYPEID                   , "NEW_TYPEID"); //$NON-NLS-1$
        	names.put( DELETE_CASTEXPRESSION        , "DELETE_CASTEXPRESSION"); //$NON-NLS-1$
        	names.put( DELETE_VECTORCASTEXPRESSION  , "DELETE_VECTORCASTEXPRESSION"); //$NON-NLS-1$
        	names.put( CASTEXPRESSION               , "CASTEXPRESSION"); //$NON-NLS-1$
        	names.put( PM_DOTSTAR                   , "PM_DOTSTAR"); //$NON-NLS-1$
        	names.put( PM_ARROWSTAR                 , "PM_ARROWSTAR"); //$NON-NLS-1$
        	names.put( MULTIPLICATIVE_MULTIPLY      , "MULTIPLICATIVE_MULTIPLY"); //$NON-NLS-1$
        	names.put( MULTIPLICATIVE_DIVIDE        , "MULTIPLICATIVE_DIVIDE"); //$NON-NLS-1$
        	names.put( MULTIPLICATIVE_MODULUS       , "MULTIPLICATIVE_MODULUS"); //$NON-NLS-1$
        	names.put( ADDITIVE_PLUS                , "ADDITIVE_PLUS"); //$NON-NLS-1$
        	names.put( ADDITIVE_MINUS               , "ADDITIVE_MINUS"); //$NON-NLS-1$
        	names.put( SHIFT_LEFT                   , "SHIFT_LEFT"); //$NON-NLS-1$
        	names.put( SHIFT_RIGHT                  , "SHIFT_RIGHT"); //$NON-NLS-1$
        	names.put( RELATIONAL_LESSTHAN          , "RELATIONAL_LESSTHAN"); //$NON-NLS-1$
        	names.put( RELATIONAL_GREATERTHAN       , "RELATIONAL_GREATERTHAN"); //$NON-NLS-1$
        	names.put( RELATIONAL_LESSTHANEQUALTO   , "RELATIONAL_LESSTHANEQUALTO"); //$NON-NLS-1$
        	names.put( RELATIONAL_GREATERTHANEQUALTO, "RELATIONAL_GREATERTHANEQUALTO" ); //$NON-NLS-1$
        	names.put( EQUALITY_EQUALS              , "EQUALITY_EQUALS"); //$NON-NLS-1$
        	names.put( EQUALITY_NOTEQUALS           , "EQUALITY_NOTEQUALS"); //$NON-NLS-1$
        	names.put( ANDEXPRESSION                , "ANDEXPRESSION"); //$NON-NLS-1$
        	names.put( EXCLUSIVEOREXPRESSION        , "EXCLUSIVEOREXPRESSION"); //$NON-NLS-1$
        	names.put( INCLUSIVEOREXPRESSION        , "INCLUSIVEOREXPRESSION"); //$NON-NLS-1$
        	names.put( LOGICALANDEXPRESSION         , "LOGICALANDEXPRESSION"); //$NON-NLS-1$
        	names.put( LOGICALOREXPRESSION          , "LOGICALOREXPRESSION"); //$NON-NLS-1$
        	names.put( CONDITIONALEXPRESSION        , "CONDITIONALEXPRESSION"); //$NON-NLS-1$
        	names.put( THROWEXPRESSION              , "THROWEXPRESSION"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_NORMAL  , "ASSIGNMENTEXPRESSION_NORMAL"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_PLUS    , "ASSIGNMENTEXPRESSION_PLUS"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_MINUS   , "ASSIGNMENTEXPRESSION_MINUS"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_MULT    , "ASSIGNMENTEXPRESSION_MULT"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_DIV     , "ASSIGNMENTEXPRESSION_DIV"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_MOD     , "ASSIGNMENTEXPRESSION_MOD"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_LSHIFT  , "ASSIGNMENTEXPRESSION_LSHIFT"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_RSHIFT  , "ASSIGNMENTEXPRESSION_RSHIFT"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_AND     , "ASSIGNMENTEXPRESSION_AND"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_OR      , "ASSIGNMENTEXPRESSION_OR"); //$NON-NLS-1$
        	names.put( ASSIGNMENTEXPRESSION_XOR     , "ASSIGNMENTEXPRESSION_XOR"); //$NON-NLS-1$
        	names.put( EXPRESSIONLIST               , "EXPRESSIONLIST"); //$NON-NLS-1$
        	
        }
		/**
		 * @return
		 */
		public String getKindName() {
			
			Object check = names.get(this);
			if( check != null )
				return (String) check;
			return "EXTENSION SPECIFIED"; //$NON-NLS-1$
		}
        
        public boolean isPostfixMemberReference()
		{
        	if( this == IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION ||  
        		this ==	IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION ||
        		this ==	IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS ||
        		this ==	IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP ||
				this == IASTExpression.Kind.POSTFIX_ARROW_DESTRUCTOR || 
				this == IASTExpression.Kind.POSTFIX_DOT_DESTRUCTOR ||
        		this ==	IASTExpression.Kind.PM_DOTSTAR ||
        		this ==	IASTExpression.Kind.PM_ARROWSTAR )
        		return true;
        	return false;

        }

		/**
		 * @return
		 */
		public boolean isLiteral() {
			if( this == PRIMARY_INTEGER_LITERAL || 
					this == PRIMARY_CHAR_LITERAL || 
					this == PRIMARY_FLOAT_LITERAL || 
					this == PRIMARY_STRING_LITERAL || 
					this == PRIMARY_BOOLEAN_LITERAL )
				return true;
			return false;
		}
        
		public boolean isBasicType(){
		    if(this == PRIMARY_EMPTY
    		|| this == THROWEXPRESSION
    		|| this == POSTFIX_DOT_DESTRUCTOR
    		|| this == POSTFIX_ARROW_DESTRUCTOR
    		|| this == DELETE_CASTEXPRESSION
    		|| this == DELETE_VECTORCASTEXPRESSION 
    		|| this == PRIMARY_INTEGER_LITERAL
    		|| this == POSTFIX_SIMPLETYPE_INT
    		|| this == UNARY_SIZEOF_TYPEID
    		|| this == UNARY_SIZEOF_UNARYEXPRESSION
    		|| this == PRIMARY_CHAR_LITERAL
    		|| this == POSTFIX_SIMPLETYPE_CHAR
    		|| this == PRIMARY_STRING_LITERAL
    		|| this == PRIMARY_FLOAT_LITERAL
    		|| this == POSTFIX_SIMPLETYPE_FLOAT
    		|| this == POSTFIX_SIMPLETYPE_DOUBLE
    		|| this == POSTFIX_SIMPLETYPE_WCHART
    		|| this == PRIMARY_BOOLEAN_LITERAL
    		|| this == POSTFIX_SIMPLETYPE_BOOL
    		|| this == RELATIONAL_GREATERTHAN
    		|| this == RELATIONAL_GREATERTHANEQUALTO
    		|| this == RELATIONAL_LESSTHAN
    		|| this == RELATIONAL_LESSTHANEQUALTO 
    		|| this == EQUALITY_EQUALS
    		|| this == EQUALITY_NOTEQUALS 
    		|| this == LOGICALANDEXPRESSION 
    		|| this == LOGICALOREXPRESSION
    		)
		        return true;
		    return false;
		}
        public boolean isPostfixSimpleType(){
            if((this == POSTFIX_SIMPLETYPE_INT)
            || (this == POSTFIX_SIMPLETYPE_SHORT)
			|| (this == POSTFIX_SIMPLETYPE_DOUBLE)
			|| (this == POSTFIX_SIMPLETYPE_FLOAT)
			|| (this == POSTFIX_SIMPLETYPE_CHAR)
			|| (this == POSTFIX_SIMPLETYPE_WCHART)
			|| (this == POSTFIX_SIMPLETYPE_SIGNED)
			|| (this == POSTFIX_SIMPLETYPE_UNSIGNED)
			|| (this == POSTFIX_SIMPLETYPE_BOOL)
			|| (this == POSTFIX_SIMPLETYPE_LONG) )
       		{
                return true;
       		}
            return false;
        }
	}
	
	public interface IASTNewExpressionDescriptor extends ISourceElementCallbackDelegate
	{
		public Iterator getNewPlacementExpressions();
		public Iterator getNewTypeIdExpressions();
		public Iterator getNewInitializerExpressions();
		public void freeReferences( );
	}
	
	
	
	public Kind getExpressionKind(); 
	public IASTExpression getLHSExpression(); 
	public IASTExpression getRHSExpression();
	public IASTExpression getThirdExpression();
	public String getLiteralString(); 
	public String getIdExpression();
	public char[] getIdExpressionCharArray();
	public IASTTypeId getTypeId(); 	
	public IASTNewExpressionDescriptor getNewExpressionDescriptor(); 
	
	public long evaluateExpression() throws ASTExpressionEvaluationException;
	public void reconcileReferences() throws ASTNotImplementedException;
	public void purgeReferences() throws ASTNotImplementedException;
	/**
	 * @param manager TODO
	 * 
	 */
	public void freeReferences();
	
}
