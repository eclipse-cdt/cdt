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

import org.eclipse.cdt.core.parser.Enum;

/**
 * @author jcamelon
 *
 */
public class ASTClassKind extends Enum {

	public final static ASTClassKind CLASS = new ASTClassKind( 1 );
	public final static ASTClassKind STRUCT = new ASTClassKind( 2 );
	public final static ASTClassKind UNION = new ASTClassKind( 3 );
	public final static ASTClassKind ENUM = new ASTClassKind( 4 );

	private ASTClassKind( int value )
	{
		super( value ); 
	}

}
