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
public class AccessVisibility {

	public static final AccessVisibility PUBLIC = new AccessVisibility( 1 );
	public static final AccessVisibility PROTECTED = new AccessVisibility( 2 );
	public static final AccessVisibility PRIVATE = new AccessVisibility( 3 );

	private AccessVisibility( int constant)
	{
		value = constant; 
	}
	
	private final int value; 
}
