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
package org.eclipse.cdt.internal.core.parser.token;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.parser.Directives;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author jcamelon
 */
public class KeywordSets {

	public static Set getKeywords( KeywordSetKey kind, ParserLanguage language )
	{
		if( kind == KeywordSetKey.EMPTY )
			return EMPTY_TABLE;
		if( kind == KeywordSetKey.DECL_SPECIFIER_SEQUENCE )
			return (Set) DECL_SPECIFIER_SEQUENCE_TABLE.get( language );
		if( kind == KeywordSetKey.DECLARATION )
			return (Set) DECLARATION_TABLE.get( language );
		if( kind == KeywordSetKey.STATEMENT )
			return (Set) STATEMENT_TABLE.get( language );
		if( kind == KeywordSetKey.BASE_SPECIFIER )
			return BASE_SPECIFIER_CPP;
		if( kind == KeywordSetKey.MEMBER )
		{
			if( language == ParserLanguage.CPP )
				return CLASS_MEMBER;
			return EMPTY_TABLE;
		}
		if( kind == KeywordSetKey.POST_USING )
			return POST_USING_CPP;
		if( kind == KeywordSetKey.FUNCTION_MODIFIER )
			return (Set) FUNCTION_MODIFIER_TABLE.get( language );
		if( kind == KeywordSetKey.NAMESPACE_ONLY )	
			return NAMESPACE_ONLY_SET;
		if( kind == KeywordSetKey.MACRO )
			return MACRO_ONLY;
		if( kind == KeywordSetKey.PP_DIRECTIVE )
			return (Set) PP_DIRECTIVES_TABLE.get( language );
		if( kind == KeywordSetKey.EXPRESSION )
			return (Set) EXPRESSION_TABLE.get( language );
		if( kind == KeywordSetKey.ALL )
			return (Set) ALL_TABLE.get( language );
		if( kind == KeywordSetKey.KEYWORDS )
			return (Set) KEYWORDS_TABLE.get( language );
		if( kind == KeywordSetKey.TYPES )
			return (Set) TYPES_TABLE.get( language );
		//TODO finish this
		return null;
	}
	
	private static final Set EMPTY_TABLE = new HashSet(0);
	
	private static final Set NAMESPACE_ONLY_SET;
	static 
	{
		NAMESPACE_ONLY_SET = new HashSet(1);
		NAMESPACE_ONLY_SET.add(Keywords.NAMESPACE );
	}
	
	private static final Set MACRO_ONLY;
	static 
	{
		MACRO_ONLY = new HashSet(1);
		MACRO_ONLY.add("defined()" ); //$NON-NLS-1$
	}
	
	
	private static final Set DECL_SPECIFIER_SEQUENCE_C;
	static
	{
		DECL_SPECIFIER_SEQUENCE_C = new TreeSet();
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.INLINE );
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.AUTO);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.REGISTER);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.STATIC);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.EXTERN);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.MUTABLE);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.TYPEDEF);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.CONST);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.VOLATILE);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.SIGNED);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.UNSIGNED);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.SHORT);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.LONG);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords._COMPLEX);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords._IMAGINARY);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.CHAR);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.WCHAR_T);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords._BOOL);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.INT);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.FLOAT);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.DOUBLE);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.VOID);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.STRUCT);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.UNION);
		DECL_SPECIFIER_SEQUENCE_C.add( Keywords.ENUM);
	}
	
	private static final Set DECL_SPECIFIER_SEQUENCE_CPP;
	static
	{
		DECL_SPECIFIER_SEQUENCE_CPP = new TreeSet();
		// add all of C then remove the ones we don't need
		DECL_SPECIFIER_SEQUENCE_CPP.addAll( DECL_SPECIFIER_SEQUENCE_C );
		DECL_SPECIFIER_SEQUENCE_CPP.remove( Keywords._COMPLEX);
		DECL_SPECIFIER_SEQUENCE_CPP.remove( Keywords._IMAGINARY);
		DECL_SPECIFIER_SEQUENCE_CPP.remove( Keywords._BOOL);
		// CPP specific stuff
		DECL_SPECIFIER_SEQUENCE_CPP.add( Keywords.VIRTUAL);
		DECL_SPECIFIER_SEQUENCE_CPP.add( Keywords.MUTABLE);
		DECL_SPECIFIER_SEQUENCE_CPP.add( Keywords.EXPLICIT);
		DECL_SPECIFIER_SEQUENCE_CPP.add( Keywords.FRIEND);
		DECL_SPECIFIER_SEQUENCE_CPP.add( Keywords.BOOL);
		DECL_SPECIFIER_SEQUENCE_CPP.add( Keywords.TYPENAME);
		DECL_SPECIFIER_SEQUENCE_CPP.add( Keywords.CLASS);
	}
	
	private static final Hashtable DECL_SPECIFIER_SEQUENCE_TABLE; 
	static
	{
		DECL_SPECIFIER_SEQUENCE_TABLE = new Hashtable(); 
		DECL_SPECIFIER_SEQUENCE_TABLE.put( ParserLanguage.CPP, DECL_SPECIFIER_SEQUENCE_CPP );
		DECL_SPECIFIER_SEQUENCE_TABLE.put( ParserLanguage.C, DECL_SPECIFIER_SEQUENCE_C );
	}
	
	private static final Set DECLARATION_CPP; 
	static
	{
		DECLARATION_CPP = new TreeSet();
		DECLARATION_CPP.addAll( DECL_SPECIFIER_SEQUENCE_CPP );
		DECLARATION_CPP.add( Keywords.ASM );
		DECLARATION_CPP.add( Keywords.TEMPLATE );
		DECLARATION_CPP.add( Keywords.USING );
		DECLARATION_CPP.add( Keywords.NAMESPACE );
		DECLARATION_CPP.add( Keywords.EXPORT );
	}
	
	private static final Set DECLARATION_C; 
	static
	{
		DECLARATION_C = new TreeSet();
		DECLARATION_C.addAll(DECL_SPECIFIER_SEQUENCE_C );
		DECLARATION_C.add(Keywords.ASM );
	}
	
	private static final Hashtable DECLARATION_TABLE; 
	static
	{
		DECLARATION_TABLE = new Hashtable();
		DECLARATION_TABLE.put( ParserLanguage.CPP, DECLARATION_CPP );
		DECLARATION_TABLE.put( ParserLanguage.C, DECLARATION_C );
	}
	
	private static final Set EXPRESSION_C;
	static
	{
		EXPRESSION_C = new TreeSet();
		EXPRESSION_C.add( Keywords.CHAR );
		EXPRESSION_C.add( Keywords.WCHAR_T);
		EXPRESSION_C.add( Keywords.SHORT);		
		EXPRESSION_C.add( Keywords.INT);
		EXPRESSION_C.add( Keywords.LONG);	
		EXPRESSION_C.add( Keywords.SIGNED);
		EXPRESSION_C.add( Keywords.UNSIGNED);
		EXPRESSION_C.add( Keywords.FLOAT);
		EXPRESSION_C.add( Keywords.DOUBLE);
		EXPRESSION_C.add( Keywords.SIZEOF );
		
	}
	
	private static final Set EXPRESSION_CPP;
	static
	{
		EXPRESSION_CPP = new TreeSet(EXPRESSION_C);
		EXPRESSION_CPP.add( Keywords.BOOL );
		EXPRESSION_CPP.add( Keywords.NEW );
		EXPRESSION_CPP.add( Keywords.DELETE );
		EXPRESSION_CPP.add( Keywords.TYPENAME );
		EXPRESSION_CPP.add( Keywords.DYNAMIC_CAST );
		EXPRESSION_CPP.add( Keywords.STATIC_CAST );
		EXPRESSION_CPP.add( Keywords.REINTERPRET_CAST );
		EXPRESSION_CPP.add( Keywords.CONST_CAST );
		EXPRESSION_CPP.add( Keywords.TYPEID );
		EXPRESSION_CPP.add( Keywords.TRUE );
		EXPRESSION_CPP.add( Keywords.FALSE );
		EXPRESSION_CPP.add( Keywords.THIS );
		EXPRESSION_CPP.add( Keywords.OPERATOR );
		EXPRESSION_CPP.add( Keywords.THROW );
	}
	
	private static final Hashtable EXPRESSION_TABLE;
	static
	{
		EXPRESSION_TABLE = new Hashtable();
		EXPRESSION_TABLE.put( ParserLanguage.CPP, EXPRESSION_CPP );
		EXPRESSION_TABLE.put( ParserLanguage.C, EXPRESSION_C );
	}
	
	private static final Set STATEMENT_C;
	static 
	{
		STATEMENT_C= new TreeSet(); 
		STATEMENT_C.addAll( DECLARATION_C );
		STATEMENT_C.addAll( EXPRESSION_C );
		STATEMENT_C.add( Keywords.FOR );
		STATEMENT_C.add( Keywords.BREAK );
		STATEMENT_C.add( Keywords.CASE );
		STATEMENT_C.add( Keywords.GOTO );
		STATEMENT_C.add( Keywords.SWITCH );
		STATEMENT_C.add( Keywords.WHILE );
		STATEMENT_C.add( Keywords.IF);
		STATEMENT_C.add( Keywords.CONTINUE);
		STATEMENT_C.add( Keywords.DEFAULT);
		STATEMENT_C.add( Keywords.RETURN);
		STATEMENT_C.add( Keywords.ELSE);
		STATEMENT_C.add( Keywords.DO);
	}
	
	private static final Set STATEMENT_CPP; 
	static 
	{
		STATEMENT_CPP = new TreeSet( DECLARATION_CPP );
		STATEMENT_CPP.addAll( EXPRESSION_CPP );
		STATEMENT_CPP.add( Keywords.TRY );
		STATEMENT_CPP.add( Keywords.FOR );
		STATEMENT_CPP.add( Keywords.BREAK );
		STATEMENT_CPP.add( Keywords.CASE );
		STATEMENT_CPP.add( Keywords.CATCH );
		STATEMENT_CPP.add( Keywords.GOTO );
		STATEMENT_CPP.add( Keywords.SWITCH );
		STATEMENT_CPP.add( Keywords.WHILE );
		STATEMENT_CPP.add( Keywords.IF);
		STATEMENT_CPP.add( Keywords.CONTINUE);
		STATEMENT_CPP.add( Keywords.DEFAULT);
		STATEMENT_CPP.add( Keywords.RETURN);
		STATEMENT_CPP.add( Keywords.ELSE);
		STATEMENT_CPP.add( Keywords.DO);
	}
	
	private static final Hashtable STATEMENT_TABLE;
	static
	{
		STATEMENT_TABLE = new Hashtable(); 
		STATEMENT_TABLE.put( ParserLanguage.CPP, STATEMENT_CPP);
		STATEMENT_TABLE.put( ParserLanguage.C, STATEMENT_C );
	}
	
	private static final Set BASE_SPECIFIER_CPP;
	static
	{
		BASE_SPECIFIER_CPP = new TreeSet();
		BASE_SPECIFIER_CPP.add(Keywords.PUBLIC);
		BASE_SPECIFIER_CPP.add(Keywords.PROTECTED);
		BASE_SPECIFIER_CPP.add(Keywords.PRIVATE);
		BASE_SPECIFIER_CPP.add(Keywords.VIRTUAL);
	}
	
	private static final Set CLASS_MEMBER;
	static
	{
		CLASS_MEMBER = new TreeSet(DECL_SPECIFIER_SEQUENCE_CPP);
		CLASS_MEMBER.add(Keywords.PUBLIC);
		CLASS_MEMBER.add(Keywords.PROTECTED);
		CLASS_MEMBER.add(Keywords.PRIVATE);
	}
	
	private static final Set POST_USING_CPP;
	static
	{
		POST_USING_CPP = new TreeSet();
		POST_USING_CPP.add(Keywords.NAMESPACE);
		POST_USING_CPP.add(Keywords.TYPENAME);
	}
	
	private static final Set FUNCTION_MODIFIER_C = EMPTY_TABLE; 

	private static final Set FUNCTION_MODIFIER_CPP;
	static
	{
		FUNCTION_MODIFIER_CPP = new TreeSet( FUNCTION_MODIFIER_C );
		
		FUNCTION_MODIFIER_CPP.add( Keywords.THROW);
		FUNCTION_MODIFIER_CPP.add( Keywords.TRY );
		FUNCTION_MODIFIER_CPP.add( Keywords.VOLATILE );
	}
	
	private static final Hashtable FUNCTION_MODIFIER_TABLE;
	static
	{
		FUNCTION_MODIFIER_TABLE= new Hashtable(2);
		FUNCTION_MODIFIER_TABLE.put( ParserLanguage.CPP, FUNCTION_MODIFIER_CPP );
		FUNCTION_MODIFIER_TABLE.put( ParserLanguage.C, FUNCTION_MODIFIER_C );
	}

	private static final Set PP_DIRECTIVES_C;
	static
	{
		PP_DIRECTIVES_C = new TreeSet();
		PP_DIRECTIVES_C.add(Directives.POUND_BLANK);
		PP_DIRECTIVES_C.add(Directives.POUND_DEFINE);
		PP_DIRECTIVES_C.add(Directives.POUND_UNDEF);
		PP_DIRECTIVES_C.add(Directives.POUND_IF);
		PP_DIRECTIVES_C.add(Directives.POUND_IFDEF);
		PP_DIRECTIVES_C.add(Directives.POUND_IFNDEF);
		PP_DIRECTIVES_C.add(Directives.POUND_ELSE);
		PP_DIRECTIVES_C.add(Directives.POUND_ENDIF);
		PP_DIRECTIVES_C.add(Directives.POUND_INCLUDE);
		PP_DIRECTIVES_C.add(Directives.POUND_LINE);
		PP_DIRECTIVES_C.add(Directives.POUND_ERROR);
		PP_DIRECTIVES_C.add(Directives.POUND_PRAGMA);
		PP_DIRECTIVES_C.add(Directives.POUND_ELIF);
		PP_DIRECTIVES_C.add(Directives._PRAGMA );
	}

	
	private static final Set PP_DIRECTIVES_CPP;
	static
	{
		PP_DIRECTIVES_CPP = new TreeSet();
		PP_DIRECTIVES_CPP.add(Directives.POUND_BLANK);
		PP_DIRECTIVES_CPP.add(Directives.POUND_DEFINE);
		PP_DIRECTIVES_CPP.add(Directives.POUND_UNDEF);
		PP_DIRECTIVES_CPP.add(Directives.POUND_IF);
		PP_DIRECTIVES_CPP.add(Directives.POUND_IFDEF);
		PP_DIRECTIVES_CPP.add(Directives.POUND_IFNDEF);
		PP_DIRECTIVES_CPP.add(Directives.POUND_ELSE);
		PP_DIRECTIVES_CPP.add(Directives.POUND_ENDIF);
		PP_DIRECTIVES_CPP.add(Directives.POUND_INCLUDE);
		PP_DIRECTIVES_CPP.add(Directives.POUND_LINE);
		PP_DIRECTIVES_CPP.add(Directives.POUND_ERROR);
		PP_DIRECTIVES_CPP.add(Directives.POUND_PRAGMA);
		PP_DIRECTIVES_CPP.add(Directives.POUND_ELIF);
	}
	
	private static final Set ALL_C;
	static
	{
		ALL_C = new TreeSet(PP_DIRECTIVES_CPP);
		ALL_C.add( Keywords.AUTO);
		ALL_C.add( Keywords.BREAK);
		ALL_C.add( Keywords.CASE);
		ALL_C.add( Keywords.CHAR);
		ALL_C.add( Keywords.CONST);
		ALL_C.add( Keywords.CONTINUE);
		ALL_C.add( Keywords.DEFAULT);
		ALL_C.add( Keywords.DELETE);
		ALL_C.add( Keywords.DO);
		ALL_C.add( Keywords.DOUBLE);
		ALL_C.add( Keywords.ELSE);
		ALL_C.add( Keywords.ENUM);
		ALL_C.add( Keywords.EXTERN);
		ALL_C.add( Keywords.FLOAT);
		ALL_C.add( Keywords.FOR);
		ALL_C.add( Keywords.GOTO);
		ALL_C.add( Keywords.IF);
		ALL_C.add( Keywords.INLINE);
		ALL_C.add( Keywords.INT);
		ALL_C.add( Keywords.LONG);
		ALL_C.add( Keywords.REGISTER);
		ALL_C.add( Keywords.RESTRICT);
		ALL_C.add( Keywords.RETURN);
		ALL_C.add( Keywords.SHORT);
		ALL_C.add( Keywords.SIGNED);
		ALL_C.add( Keywords.SIZEOF);
		ALL_C.add( Keywords.STATIC);
		ALL_C.add( Keywords.STRUCT);
		ALL_C.add( Keywords.SWITCH);
		ALL_C.add( Keywords.TYPEDEF);
		ALL_C.add( Keywords.UNION);
		ALL_C.add( Keywords.UNSIGNED);
		ALL_C.add( Keywords.VOID);
		ALL_C.add( Keywords.VOLATILE);
		ALL_C.add( Keywords.WHILE);
		ALL_C.add( Keywords._BOOL);
		ALL_C.add( Keywords._COMPLEX);
		ALL_C.add( Keywords._IMAGINARY);
	}
	
	private static final Set ALL_CPP;
	static
	{
		ALL_CPP = new TreeSet(PP_DIRECTIVES_CPP);
		ALL_CPP.add( Keywords.AND );
		ALL_CPP.add( Keywords.AND_EQ);
		ALL_CPP.add( Keywords.ASM);
		ALL_CPP.add( Keywords.AUTO);
		ALL_CPP.add( Keywords.BITAND);
		ALL_CPP.add( Keywords.BITOR);
		ALL_CPP.add( Keywords.BOOL);
		ALL_CPP.add( Keywords.BREAK);
		ALL_CPP.add( Keywords.CASE);
		ALL_CPP.add( Keywords.CATCH);
		ALL_CPP.add( Keywords.CHAR);
		ALL_CPP.add( Keywords.CLASS);
		ALL_CPP.add( Keywords.COMPL);
		ALL_CPP.add( Keywords.CONST);
		ALL_CPP.add( Keywords.CONST_CAST);
		ALL_CPP.add( Keywords.CONTINUE);
		ALL_CPP.add( Keywords.DEFAULT);
		ALL_CPP.add( Keywords.DELETE);
		ALL_CPP.add( Keywords.DO);
		ALL_CPP.add( Keywords.DOUBLE);
		ALL_CPP.add( Keywords.DYNAMIC_CAST);
		ALL_CPP.add( Keywords.ELSE);
		ALL_CPP.add( Keywords.ENUM);
		ALL_CPP.add( Keywords.EXPLICIT);
		ALL_CPP.add( Keywords.EXPORT);
		ALL_CPP.add( Keywords.EXTERN);
		ALL_CPP.add( Keywords.FALSE);
		ALL_CPP.add( Keywords.FLOAT);
		ALL_CPP.add( Keywords.FOR);
		ALL_CPP.add( Keywords.FRIEND);
		ALL_CPP.add( Keywords.GOTO);
		ALL_CPP.add( Keywords.IF);
		ALL_CPP.add( Keywords.INLINE);
		ALL_CPP.add( Keywords.INT);
		ALL_CPP.add( Keywords.LONG);
		ALL_CPP.add( Keywords.MUTABLE);
		ALL_CPP.add( Keywords.NAMESPACE);
		ALL_CPP.add( Keywords.NEW);
		ALL_CPP.add( Keywords.NOT);
		ALL_CPP.add( Keywords.NOT_EQ);
		ALL_CPP.add( Keywords.OPERATOR);
		ALL_CPP.add( Keywords.OR);
		ALL_CPP.add( Keywords.OR_EQ);
		ALL_CPP.add( Keywords.PRIVATE);
		ALL_CPP.add( Keywords.PROTECTED);
		ALL_CPP.add( Keywords.PUBLIC);
		ALL_CPP.add( Keywords.REGISTER);
		ALL_CPP.add( Keywords.REINTERPRET_CAST);
		ALL_CPP.add( Keywords.RETURN);
		ALL_CPP.add( Keywords.SHORT);
		ALL_CPP.add( Keywords.SIGNED);
		ALL_CPP.add( Keywords.SIZEOF);
		ALL_CPP.add( Keywords.STATIC);
		ALL_CPP.add( Keywords.STATIC_CAST);
		ALL_CPP.add( Keywords.STRUCT);
		ALL_CPP.add( Keywords.SWITCH);
		ALL_CPP.add( Keywords.TEMPLATE);
		ALL_CPP.add( Keywords.THIS);
		ALL_CPP.add( Keywords.THROW);
		ALL_CPP.add( Keywords.TRUE);
		ALL_CPP.add( Keywords.TRY);
		ALL_CPP.add( Keywords.TYPEDEF);
		ALL_CPP.add( Keywords.TYPEID);
		ALL_CPP.add( Keywords.TYPENAME);
		ALL_CPP.add( Keywords.UNION);
		ALL_CPP.add( Keywords.UNSIGNED);
		ALL_CPP.add( Keywords.USING);
		ALL_CPP.add( Keywords.VIRTUAL);
		ALL_CPP.add( Keywords.VOID);
		ALL_CPP.add( Keywords.VOLATILE);
		ALL_CPP.add( Keywords.WCHAR_T);
		ALL_CPP.add( Keywords.WHILE);
		ALL_CPP.add( Keywords.XOR);
		ALL_CPP.add( Keywords.XOR_EQ);

	}
	private static final Hashtable ALL_TABLE;
	static
	{
		ALL_TABLE = new Hashtable( 2 );
		ALL_TABLE.put( ParserLanguage.C, ALL_C );
		ALL_TABLE.put( ParserLanguage.CPP, ALL_CPP );
	}
	
	private static final Set KEYWORDS_CPP;
	static
	{
		KEYWORDS_CPP = new TreeSet();
		KEYWORDS_CPP.add( Keywords.AND );
		KEYWORDS_CPP.add( Keywords.AND_EQ );
		KEYWORDS_CPP.add( Keywords.ASM );
		KEYWORDS_CPP.add( Keywords.AUTO );
		KEYWORDS_CPP.add( Keywords.BITAND );
		KEYWORDS_CPP.add( Keywords.BITOR );
		KEYWORDS_CPP.add( Keywords.BREAK );
		KEYWORDS_CPP.add( Keywords.CASE );
		KEYWORDS_CPP.add( Keywords.CATCH );
		KEYWORDS_CPP.add( Keywords.CLASS );
		KEYWORDS_CPP.add( Keywords.COMPL );
		KEYWORDS_CPP.add( Keywords.CONST );
		KEYWORDS_CPP.add( Keywords.CONST_CAST );
		KEYWORDS_CPP.add( Keywords.CONTINUE );
		KEYWORDS_CPP.add( Keywords.DEFAULT );
		KEYWORDS_CPP.add( Keywords.DELETE );
		KEYWORDS_CPP.add( Keywords.DO );
		KEYWORDS_CPP.add( Keywords.DYNAMIC_CAST );
		KEYWORDS_CPP.add( Keywords.ELSE );
		KEYWORDS_CPP.add( Keywords.ENUM );
		KEYWORDS_CPP.add( Keywords.EXPLICIT );
		KEYWORDS_CPP.add( Keywords.EXPORT );
		KEYWORDS_CPP.add( Keywords.EXTERN );
		KEYWORDS_CPP.add( Keywords.FALSE );
		KEYWORDS_CPP.add( Keywords.FOR );
		KEYWORDS_CPP.add( Keywords.FRIEND );
		KEYWORDS_CPP.add( Keywords.GOTO ); 			
		KEYWORDS_CPP.add( Keywords.IF );
		KEYWORDS_CPP.add( Keywords.INLINE );
		KEYWORDS_CPP.add( Keywords.MUTABLE );
		KEYWORDS_CPP.add( Keywords.NAMESPACE );
		KEYWORDS_CPP.add( Keywords.NEW );
		KEYWORDS_CPP.add( Keywords.NOT );
		KEYWORDS_CPP.add( Keywords.NOT_EQ );
		KEYWORDS_CPP.add( Keywords.OPERATOR );
		KEYWORDS_CPP.add( Keywords.OR );
		KEYWORDS_CPP.add( Keywords.OR_EQ );
		KEYWORDS_CPP.add( Keywords.PRIVATE );
		KEYWORDS_CPP.add( Keywords.PROTECTED );
		KEYWORDS_CPP.add( Keywords.PUBLIC );
		KEYWORDS_CPP.add( Keywords.REGISTER );
		KEYWORDS_CPP.add( Keywords.REINTERPRET_CAST );
		KEYWORDS_CPP.add( Keywords.RESTRICT );
		KEYWORDS_CPP.add( Keywords.RETURN );
		KEYWORDS_CPP.add( Keywords.SIZEOF );
		KEYWORDS_CPP.add( Keywords.STATIC );
		KEYWORDS_CPP.add( Keywords.STATIC_CAST );
		KEYWORDS_CPP.add( Keywords.STRUCT );
		KEYWORDS_CPP.add( Keywords.SWITCH );
		KEYWORDS_CPP.add( Keywords.TEMPLATE );
		KEYWORDS_CPP.add( Keywords.THIS );
		KEYWORDS_CPP.add( Keywords.THROW );
		KEYWORDS_CPP.add( Keywords.TRUE );
		KEYWORDS_CPP.add( Keywords.TRY );
		KEYWORDS_CPP.add( Keywords.TYPEDEF );
		KEYWORDS_CPP.add( Keywords.TYPEID );
		KEYWORDS_CPP.add( Keywords.TYPENAME ); 
		KEYWORDS_CPP.add( Keywords.UNION );
		KEYWORDS_CPP.add( Keywords.USING );
		KEYWORDS_CPP.add( Keywords.VIRTUAL );
		KEYWORDS_CPP.add( Keywords.VOLATILE );
		KEYWORDS_CPP.add( Keywords.WHILE );
		KEYWORDS_CPP.add( Keywords.XOR );
		KEYWORDS_CPP.add( Keywords.XOR_EQ );

	}
	
	private static Set KEYWORDS_C;
	static
	{
		KEYWORDS_C = new TreeSet();
		KEYWORDS_C.add( Keywords.ASM );
		KEYWORDS_C.add( Keywords.AUTO );
		KEYWORDS_C.add( Keywords.BREAK );
		KEYWORDS_C.add( Keywords.CASE );
		KEYWORDS_C.add( Keywords.CONST );
		KEYWORDS_C.add( Keywords.CONTINUE );
		KEYWORDS_C.add( Keywords.DEFAULT );
		KEYWORDS_C.add( Keywords.DO );
		KEYWORDS_C.add( Keywords.ELSE );
		KEYWORDS_C.add( Keywords.ENUM );
		KEYWORDS_C.add( Keywords.EXTERN );
		KEYWORDS_C.add( Keywords.FOR );
		KEYWORDS_C.add( Keywords.GOTO );
		KEYWORDS_C.add( Keywords.IF );
		KEYWORDS_C.add( Keywords.INLINE );
		KEYWORDS_C.add( Keywords.REGISTER );
		KEYWORDS_C.add( Keywords.RETURN );
		KEYWORDS_C.add( Keywords.RESTRICT );
		KEYWORDS_C.add( Keywords.SIZEOF );
		KEYWORDS_C.add( Keywords.STATIC );
		KEYWORDS_C.add( Keywords.STRUCT );
		KEYWORDS_C.add( Keywords.SWITCH );
		KEYWORDS_C.add( Keywords.TYPEDEF );
		KEYWORDS_C.add( Keywords.UNION );
		KEYWORDS_C.add( Keywords.VOLATILE );
		KEYWORDS_C.add( Keywords.WHILE );
	}
	
	

	private static final Hashtable KEYWORDS_TABLE;
	static
	{
		KEYWORDS_TABLE = new Hashtable(2);
		KEYWORDS_TABLE.put( ParserLanguage.C, KEYWORDS_C );
		KEYWORDS_TABLE.put( ParserLanguage.CPP, KEYWORDS_CPP );
	}

	private static final Set TYPES_C;
	static
	{
		TYPES_C = new TreeSet();
		TYPES_C.add( Keywords.CHAR );
		TYPES_C.add( Keywords.DOUBLE );
		TYPES_C.add( Keywords.FLOAT );
		TYPES_C.add( Keywords.INT );
		TYPES_C.add( Keywords.LONG );
		TYPES_C.add( Keywords.SHORT );
		TYPES_C.add( Keywords.SIGNED );
		TYPES_C.add( Keywords.UNSIGNED );
		TYPES_C.add( Keywords.VOID );
		TYPES_C.add( Keywords._BOOL );
		TYPES_C.add( Keywords._COMPLEX );
		TYPES_C.add( Keywords._IMAGINARY );
	}
	private static final Set TYPES_CPP;
	static
	{
		TYPES_CPP = new TreeSet();
		TYPES_CPP.add( Keywords.BOOL );
		TYPES_CPP.add( Keywords.CHAR );
		TYPES_CPP.add( Keywords.DOUBLE );
		TYPES_CPP.add( Keywords.FLOAT );
		TYPES_CPP.add( Keywords.INT );
		TYPES_CPP.add( Keywords.LONG );
		TYPES_CPP.add( Keywords.SHORT );
		TYPES_CPP.add( Keywords.SIGNED );
		TYPES_CPP.add( Keywords.UNSIGNED );
		TYPES_CPP.add( Keywords.VOID );
		TYPES_CPP.add( Keywords.WCHAR_T );
	}	
	
	private static Hashtable TYPES_TABLE;
	static
	{
		TYPES_TABLE = new Hashtable( 2 );
		TYPES_TABLE.put( ParserLanguage.C, TYPES_C );
		TYPES_TABLE.put( ParserLanguage.CPP, TYPES_CPP );
	}
	
	private static Hashtable PP_DIRECTIVES_TABLE;
	static
	{
		PP_DIRECTIVES_TABLE = new Hashtable( 2 );
		PP_DIRECTIVES_TABLE.put( ParserLanguage.C, PP_DIRECTIVES_C );
		PP_DIRECTIVES_TABLE.put( ParserLanguage.CPP, PP_DIRECTIVES_CPP );
	}
}
