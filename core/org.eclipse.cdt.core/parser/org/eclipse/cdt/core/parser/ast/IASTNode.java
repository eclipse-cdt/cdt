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
public interface IASTNode {
	
	 public static class LookupKind extends Enum {

		public static final LookupKind ALL = new LookupKind( 0 );
		public static final LookupKind STRUCTURES = new LookupKind( 1 );
		public static final LookupKind STRUCTS = new LookupKind( 2 );
		public static final LookupKind UNIONS = new LookupKind( 3 );
		public static final LookupKind CLASSES = new LookupKind( 4 );
		public static final LookupKind FUNCTIONS = new LookupKind( 5 );
		public static final LookupKind VARIABLES = new LookupKind( 6 );
		public static final LookupKind LOCAL_VARIABLES = new LookupKind( 7 );
		public static final LookupKind MEMBERS = new LookupKind( 8 );
		public static final LookupKind METHODS = new LookupKind( 9 );
		public static final LookupKind FIELDS = new LookupKind( 10 );
		public static final LookupKind CONSTRUCTORS = new LookupKind (11);
		public static final LookupKind NAMESPACES = new LookupKind( 12 ); 
		public static final LookupKind MACROS = new LookupKind( 13 ); 
		public static final LookupKind ENUMERATIONS = new LookupKind( 14 ); 
		public static final LookupKind ENUMERATORS = new LookupKind( 15 );
		public static final LookupKind THIS = new LookupKind(16);
		
		/**
		 * @param enumValue
		 */
		protected LookupKind(int enumValue) {
			super(enumValue);
		}
	 }
	 
	 public static class LookupError extends Error
	 {
	 }
	 
	 public static interface ILookupResult 
	 {
	 	public String getPrefix(); 
	 	public Iterator getNodes(); 
	 	public int getResultsSize();
	 }

	/**
	 * @param prefix
	 * @param kind
	 * @param context
	 * @return
	 * @throws LookupError
	 */
	public ILookupResult lookup( String prefix, LookupKind[] kind, IASTNode context) throws LookupError, ASTNotImplementedException;
}

