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
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

/**
 * C++ adds additional modifiers and types for decl specifier sequence.
 * 
 * @author Doug Schaefer
 */
public interface ICPPASTDeclSpecifier extends IASTDeclSpecifier {

	// Extra storage class in C++
	/**
	 * <code>sc_mutable</code> represents a mutable storage representation.
	 */
	public static final int sc_mutable = IASTDeclSpecifier.sc_last + 1;

	/**
	 * <code>sc_last</code> is overwritten to allow extensibility.
	 */
	public static final int sc_last = sc_mutable;

	// A declaration in C++ can be a friend declaration
	/**
	 * Is this a friend declaration?
	 * 
	 * @return boolean
	 */
	public boolean isFriend();

	/**
	 * Set this to be a friend declaration true/false.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setFriend(boolean value);

	/**
	 * Is this a virtual function?
	 * 
	 * @return boolean
	 */
	public boolean isVirtual();

	/**
	 * Set this declaration to be virutal.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setVirtual(boolean value);

	/**
	 * Is this an explicit constructor?
	 * 
	 * @return boolean
	 */
	public boolean isExplicit();

	/**
	 * Set this to be an explicit constructor.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setExplicit(boolean value);

}
