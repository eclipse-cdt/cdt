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

/**
 * @author jcamelon
 *
 */
public class ClassKind {

	public final static ClassKind CLASS = new ClassKind( 1 );
	public final static ClassKind STRUCT = new ClassKind( 2 );
	public final static ClassKind UNION = new ClassKind( 3 );
	public final static ClassKind ENUM = new ClassKind( 4 );

	protected ClassKind( int value )
	{
		this.value = value; 
	}

	private final int value; 
	 
}
