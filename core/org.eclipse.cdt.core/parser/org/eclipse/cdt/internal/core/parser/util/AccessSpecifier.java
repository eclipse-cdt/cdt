/**********************************************************************
 * Created on Mar 26, 2003
 *
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.util;

/**
 * @author jcamelon
 *
 */
public class AccessSpecifier {

	public static final int v_private = 0;
	public static final int v_protected = 1;
	public static final int v_public = 2;
	public static final int v_unknown = 3; 
	
	private int access;
	public void setAccess(int access) { this.access = access; }
	public int getAccess() { return access; }
	
	public AccessSpecifier( int value )
	{
		setAccess( value );
	}
}
