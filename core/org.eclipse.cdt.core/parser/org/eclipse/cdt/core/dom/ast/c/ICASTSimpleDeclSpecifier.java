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
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;

/**
 * This interface represents a built-in type in C.
 * 
 * @author Doug Schaefer
 */
public interface ICASTSimpleDeclSpecifier extends IASTSimpleDeclSpecifier,
		ICASTDeclSpecifier {

	// Extra types in C
	/**
	 * <code>t_Bool</code> boolean. e.g. _Bool x;
	 */
	public static final int t_Bool = IASTSimpleDeclSpecifier.t_last + 1;

	/**
	 * <code>t_Complex</code> complex number. e.g. _Complex t;
	 */
	public static final int t_Complex = IASTSimpleDeclSpecifier.t_last + 2;

	/**
	 * <code>t_Imaginary</code> complex imaginary number. e.g. _Imaginr
	 */
	public static final int t_Imaginary = IASTSimpleDeclSpecifier.t_last + 3;

	/**
	 * <code>t_last</code> is defined for sub-interfaces.
	 */
	public static final int t_last = t_Imaginary;

	// allow for long long's
	/**
	 * Is long long?
	 * 
	 * @return boolean
	 */
	public boolean isLongLong();

	/**
	 * Set long long to be 'value'.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setLongLong(boolean value);

}
