/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @author Doug Schaefer
 */
public interface IASTDeclSpecifier extends IASTNode {

	// Storage class
	public int getStorageClass();
	public static final int sc_unspecified = 0;
	public static final int sc_typedef = 1;
	public static final int sc_extern = 2;
	public static final int sc_static = 3;
	public static final int sc_auto = 4;
	public static final int sc_register = 5;
	public static final int sc_last = sc_register;

	public void setStorageClass( int storageClass );
	
	// Type qualifier
	public boolean isConst();
	public void setConst( boolean value );
	public boolean isVolatile();
	public void setVolatile( boolean value );
	
	// Function specifier
	public boolean isInline();
	public void setInline( boolean value );
	
}
