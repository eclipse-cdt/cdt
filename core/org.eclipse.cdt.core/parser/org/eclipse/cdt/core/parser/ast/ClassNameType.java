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
public class ClassNameType {

	public static final ClassNameType IDENTIFIER = new ClassNameType( 1 );
	public static final ClassNameType TEMPLATE   = new ClassNameType( 2 ); 

	private final int type; 
	private ClassNameType( int t )
	{
		type = t;
	}
}
