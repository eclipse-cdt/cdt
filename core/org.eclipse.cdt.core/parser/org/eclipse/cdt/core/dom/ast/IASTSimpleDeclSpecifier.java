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
	 * This returns the built-in type for the declaration. The type is then
	 * refined by qualifiers for signed/unsigned and short/long. The type could
	 * also be unspecified which usually means int.
	 * 
	 * @return
	 */
	public int getType();

	/**
	 * <code>t_unspecified</code> implies an unspecified type. .e.g x = 5; //
	 * declaration w/t_unspecified type logically defaults to integer.
	 */
	public static final int t_unspecified = 0;

	/**
	 * <code>t_void</code> implies void type e.g. void x();
	 */
	public static final int t_void = 1;

	/**
	 * <code>t_char</code> implies char type e.g. char y;
	 */
	public static final int t_char = 2;

	/**
	 * <code>t_int</code> implies int type e.g. int x;
	 */
	public static final int t_int = 3;

	/**
	 * <code>t_float</code> implies floating point type. e.g. float yy;
	 */
	public static final int t_float = 4;

	/**
	 * <code>t_double</code> implies double floating point type. e.g. double
	 * d;
	 */
	public static final int t_double = 5;

	/**
	 * <code>t_last</code> specified for subinterface definition.
	 */
	public static final int t_last = t_double; // used only in subclasses

	/**
	 * Set this decl specifier type to <code>type</code>.
	 * 
	 * @param type
	 *            (int)
	 */
	public void setType(int type);

	/**
	 * Is the type modified by the signed keyword?
	 * 
	 * @return boolean
	 */
	public boolean isSigned();

	/**
	 * Is the type modified by the unsigned keyword?
	 * 
	 * @return boolean
	 */
	public boolean isUnsigned();

	/**
	 * Is the type modified by the short keyword?
	 * 
	 * @return boolean
	 */
	public boolean isShort();

	/**
	 * Is the type modified by the long keyword?
	 * 
	 * @return boolean
	 */
	public boolean isLong();

	/**
	 * Change as to if the type is modified by the keyword signed.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setSigned(boolean value);

	/**
	 * Change as to if the type is modified by the keyword unsigned.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setUnsigned(boolean value);

	/**
	 * Change as to if the type is modified by the keyword long.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setLong(boolean value);

	/**
	 * Change as to if the type is modified by the keyword short.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setShort(boolean value);

}
