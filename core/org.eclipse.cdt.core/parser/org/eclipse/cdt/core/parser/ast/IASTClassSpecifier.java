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
public interface IASTClassSpecifier extends IASTTypeSpecifier, IASTScope, IASTOffsetableNamedElement, IASTScopedTypeSpecifier, ISourceElementCallbackDelegate {

	public class ClassNameType extends Enum {

		public static final ClassNameType IDENTIFIER = new ClassNameType( 1 );
		public static final ClassNameType TEMPLATE   = new ClassNameType( 2 ); 
 
		private ClassNameType( int t )
		{
			super( t );
		}
	}
	
	public ClassNameType getClassNameType(); 

	public ASTClassKind getClassKind();

	public Iterator getBaseClauses();
	
	public ASTAccessVisibility getCurrentVisibilityMode(); 
	public void setCurrentVisibility( ASTAccessVisibility visibility );
	 
}
