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
package org.eclipse.cdt.internal.core.parser.token;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.parser.Directives;
import org.eclipse.cdt.core.parser.Enum;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author jcamelon
 */
public class KeywordSets {

	
	public static class Key extends Enum
	{
		public static final Key EMPTY = new Key( 0 );
		public static final Key DECL_SPECIFIER_SEQUENCE = new Key( 1 );
		public static final Key DECLARATION = new Key( 2 );
		public static final Key STATEMENT = new Key(3);
		public static final Key BASE_SPECIFIER = new Key(4);
		public static final Key POST_USING = new Key( 5 );
		public static final Key FUNCTION_MODIFIER = new Key( 6 );
		public static final Key NAMESPACE_ONLY = new Key(6);
		public static final Key MACRO = new Key( 7 );
		public static final Key PP_DIRECTIVE = new Key( 8 );
		public static final Key EXPRESSION = new Key( 9 );
		/**
		 * @param enumValue
		 */
		protected Key(int enumValue) {
			super(enumValue);
		}
		
	}
	
	public static Set getKeywords( Key kind, ParserLanguage language )
	{
		if( kind == Key.EMPTY )
			return EMPTY;
		if( kind == Key.DECL_SPECIFIER_SEQUENCE )
			return (Set) DECL_SPECIFIER_SEQUENCE.get( language );
		if( kind == Key.DECLARATION )
			return (Set) DECLARATION.get( language );
		if( kind == Key.STATEMENT )
			return (Set) STATEMENT.get( language );
		if( kind == Key.BASE_SPECIFIER )
			return BASE_SPECIFIER_CPP;
		if( kind == Key.POST_USING )
			return POST_USING_CPP;
		if( kind == Key.FUNCTION_MODIFIER )
			return (Set) FUNCTION_MODIFIER.get( language );
		if( kind == Key.NAMESPACE_ONLY )	
			return NAMESPACE_ONLY;
		if( kind == Key.MACRO )
			return MACRO_ONLY;
		if( kind == Key.PP_DIRECTIVE )
			return PP_DIRECTIVES;
		if( kind == Key.EXPRESSION )
			return (Set) EXPRESSION.get( language );
		//TODO finish this
		return null;
	}
	
	private static final Set EMPTY = new HashSet();
	
	private static final Set NAMESPACE_ONLY;
	static 
	{
		NAMESPACE_ONLY = new HashSet();
		NAMESPACE_ONLY.add(Keywords.NAMESPACE );
	}
	
	private static final Set MACRO_ONLY;
	static 
	{
		MACRO_ONLY = new HashSet();
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
	
	private static final Hashtable DECL_SPECIFIER_SEQUENCE; 
	static
	{
		DECL_SPECIFIER_SEQUENCE = new Hashtable(); 
		DECL_SPECIFIER_SEQUENCE.put( ParserLanguage.CPP, DECL_SPECIFIER_SEQUENCE_CPP );
		DECL_SPECIFIER_SEQUENCE.put( ParserLanguage.C, DECL_SPECIFIER_SEQUENCE_C );
	}
	
	private static final Set DECLARATION_CPP; 
	static
	{
		DECLARATION_CPP = new TreeSet();
		DECLARATION_CPP.addAll( DECL_SPECIFIER_SEQUENCE_CPP );
		DECLARATION_CPP.add( Keywords.ASM );
		// more to come
	}
	
	private static final Set DECLARATION_C; 
	static
	{
		DECLARATION_C = new TreeSet();
		DECLARATION_C.addAll(DECL_SPECIFIER_SEQUENCE_C );
		DECLARATION_C.add(Keywords.ASM );
		// more to come
	}
	
	private static final Hashtable DECLARATION; 
	static
	{
		DECLARATION = new Hashtable();
		DECLARATION.put( ParserLanguage.CPP, DECLARATION_CPP );
		DECLARATION.put( ParserLanguage.C, DECLARATION_C );
	}
	
	private static final Set STATEMENT_C;
	static 
	{
		STATEMENT_C= new TreeSet(); 
		STATEMENT_C.addAll( DECLARATION_C );
		STATEMENT_C.add( Keywords.FOR );
		// more to come
	}
	
	private static final Set STATEMENT_CPP; 
	static 
	{
		STATEMENT_CPP = new TreeSet( STATEMENT_C );
		STATEMENT_CPP.add( Keywords.TRY );
		//TODO finish this
	}
	
	private static final Hashtable STATEMENT;
	static
	{
		STATEMENT = new Hashtable(); 
		STATEMENT.put( ParserLanguage.CPP, STATEMENT_CPP);
		STATEMENT.put( ParserLanguage.C, STATEMENT_C );
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
	
	private static final Set POST_USING_CPP;
	static
	{
		POST_USING_CPP = new TreeSet();
		POST_USING_CPP.add(Keywords.NAMESPACE);
		POST_USING_CPP.add(Keywords.TYPENAME);
	}
	
	private static final Set FUNCTION_MODIFIER_C; 
	static
	{
		FUNCTION_MODIFIER_C = new TreeSet();
	}
	private static final Set FUNCTION_MODIFIER_CPP;
	static
	{
		FUNCTION_MODIFIER_CPP = new TreeSet( FUNCTION_MODIFIER_C );
		FUNCTION_MODIFIER_CPP.add( Keywords.CONST );
		FUNCTION_MODIFIER_CPP.add( Keywords.THROW);
		FUNCTION_MODIFIER_CPP.add( Keywords.TRY );
		FUNCTION_MODIFIER_CPP.add( Keywords.VOLATILE );
	}
	
	private static final Hashtable FUNCTION_MODIFIER;
	static
	{
		FUNCTION_MODIFIER= new Hashtable();
		FUNCTION_MODIFIER.put( ParserLanguage.CPP, FUNCTION_MODIFIER_CPP );
		FUNCTION_MODIFIER.put( ParserLanguage.C, FUNCTION_MODIFIER_C );
	}
	
	private static final Set PP_DIRECTIVES;
	static
	{
		PP_DIRECTIVES = new TreeSet();
		PP_DIRECTIVES.add(Directives.POUND_BLANK);
		PP_DIRECTIVES.add(Directives.POUND_DEFINE);
		PP_DIRECTIVES.add(Directives.POUND_UNDEF);
		PP_DIRECTIVES.add(Directives.POUND_IF);
		PP_DIRECTIVES.add(Directives.POUND_IFDEF);
		PP_DIRECTIVES.add(Directives.POUND_IFNDEF);
		PP_DIRECTIVES.add(Directives.POUND_ELSE);
		PP_DIRECTIVES.add(Directives.POUND_ENDIF);
		PP_DIRECTIVES.add(Directives.POUND_INCLUDE);
		PP_DIRECTIVES.add(Directives.POUND_LINE);
		PP_DIRECTIVES.add(Directives.POUND_ERROR);
		PP_DIRECTIVES.add(Directives.POUND_PRAGMA);
		PP_DIRECTIVES.add(Directives.POUND_ELIF); 
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
	
	private static final Hashtable EXPRESSION;
	static
	{
		EXPRESSION = new Hashtable();
		EXPRESSION.put( ParserLanguage.CPP, EXPRESSION_CPP );
		EXPRESSION.put( ParserLanguage.C, EXPRESSION_C );
	}
}
