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
public interface ICASTSimpleTypeSpecifier {

	/**
	 * Which simple type is this?
	 * 
	 * @return an enum value for the type
	 */
	public int getType();
	public static final int t_void = 1;
	public static final int t_char = 2;
	public static final int t_short = 3;
	public static final int t_int = 4;
	public static final int t_long = 5;
	public static final int t_float = 6;
	public static final int t_double = 7;
	public static final int t_signed = 8;
	public static final int t_unsigned = 9;
	// C ones
	public static final int t__Bool = 10;
	public static final int t__Complex = 11;
	public static final int t__Imaginary = 12;
	// C++ ones
	public static final int t_wchar_t = 13;
	public static final int t_bool = 14;
}
