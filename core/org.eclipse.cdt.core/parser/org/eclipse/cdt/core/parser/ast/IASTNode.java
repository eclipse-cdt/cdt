/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
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
public interface IASTNode {
	
	 public static class LookupKind extends Enum {

		public static final LookupKind ALL = new LookupKind( 0 ); // includes everything
		public static final LookupKind STRUCTURES = new LookupKind( 1 ); // includes STRUCTS + UNIONS + CLASSES
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
		public static final LookupKind ENUMERATIONS = new LookupKind( 13 ); 
		public static final LookupKind ENUMERATORS = new LookupKind( 14 );
		public static final LookupKind THIS = new LookupKind(15);
		public static final LookupKind TYPEDEFS = new LookupKind(16);
		public static final LookupKind TYPES = new LookupKind(17); // includes STRUCTURES + ENUMERATIONS + TYPEDEFS
		
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
	 	public int getIndexOfNextParameter();
	 }

	/**
	 * @param prefix
	 * @param kind
	 * @param context
	 * @param functionParameters
	 * @return
	 * @throws LookupError
	 */
	public ILookupResult lookup( String prefix, LookupKind[] kind, IASTNode context, IASTExpression functionParameters) throws LookupError, ASTNotImplementedException;
}

