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
package org.eclipse.cdt.core.parser.ast2.c;

/**
 * @author Doug Schaefer
 */
public interface ICASTFundamentalTypeSpecifier extends ICASTDeclSpecifier {
	
	public int getType();
	public static final int t_void = 1;
	public static final int t_char = 2;
	public static final int t_int = 3;
	public static final int t_float = 4;
	public static final int t_double = 5;
	// C ones
	public static final int t__Bool = 6;
	public static final int t__Complex = 7;
	public static final int t__Imaginary = 8;
	// C++ ones
	public static final int t_wchar_t = 9;
	public static final int t_bool = 10;
	
	// Qualifiers
	public boolean isSigned();
	public boolean isUnsigned();
	public boolean isLong();
	public boolean isShort();
}
