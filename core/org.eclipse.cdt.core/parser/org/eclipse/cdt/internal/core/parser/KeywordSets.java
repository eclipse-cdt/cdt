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
package org.eclipse.cdt.internal.core.parser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

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
		
		//TODO finish this
		return null;
	}
	
	private static final Set EMPTY = new HashSet();
	
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
	}
	
	private static final Hashtable STATEMENT;
	static
	{
		STATEMENT = new Hashtable(); 
		STATEMENT.put( ParserLanguage.CPP, STATEMENT_CPP);
		STATEMENT.put( ParserLanguage.C, STATEMENT_C );
	}
}
