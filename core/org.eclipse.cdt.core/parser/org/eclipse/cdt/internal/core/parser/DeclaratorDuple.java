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

/**
 * @author jcamelon
 */
public class DeclaratorDuple {

	public DeclaratorDuple( Object o, Declarator d )
	{
		object = o; 
		declarator = d;
	}
	
	private final Declarator declarator; 
	private final Object object; 
	/**
	 * @return
	 */
	public Declarator getDeclarator() {
		return declarator;
	}

	/**
	 * @return
	 */
	public Object getObject() {
		return object;
	}

}
