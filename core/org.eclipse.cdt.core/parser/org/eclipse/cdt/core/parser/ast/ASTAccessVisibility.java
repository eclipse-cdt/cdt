/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
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
public class ASTAccessVisibility extends Enum {

	public static final ASTAccessVisibility PUBLIC = new ASTAccessVisibility( 1 );
	public static final ASTAccessVisibility PROTECTED = new ASTAccessVisibility( 2 );
	public static final ASTAccessVisibility PRIVATE = new ASTAccessVisibility( 3 );

	private ASTAccessVisibility( int constant)
	{
		super( constant ); 
	}
	
	public boolean isLessThan( ASTAccessVisibility other )
	{
		return getEnumValue() < other.getEnumValue();
	}

	public boolean isGreaterThan( ASTAccessVisibility other )
	{
		return getEnumValue() > other.getEnumValue();
	}
	
	
	 
}
