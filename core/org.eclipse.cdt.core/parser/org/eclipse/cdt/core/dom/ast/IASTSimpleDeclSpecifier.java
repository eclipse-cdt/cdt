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
 * This represents a decl specifier for a built-in type.
 * 
 * @author Doug Schaefer
 */
public interface IASTSimpleDeclSpecifier extends IASTDeclSpecifier {

	/**
	 * This returns the built-in type for the declaration. The type is
	 * then refined by qualifiers for signed/unsigned and short/long.
	 * The type could also be unspecified which usually means int.
	 * 
	 * @return
	 */
	public int getType();
	public static final int t_unspecified = 0;
	public static final int t_void = 1;
	public static final int t_char = 2;
	public static final int t_int = 3;
	public static final int t_float = 4;
	public static final int t_double = 5;
	public static final int t_last = t_double; // used only in subclasses
	
	public boolean isSigned();
	public boolean isUnsigned();
	public boolean isShort();
	public boolean isLong();
	
}
