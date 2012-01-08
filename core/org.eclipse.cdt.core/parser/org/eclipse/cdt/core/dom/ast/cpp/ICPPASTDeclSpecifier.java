/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

/**
 * C++ adds additional modifiers and types for decl specifier sequence.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTDeclSpecifier extends IASTDeclSpecifier {

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
	
	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTDeclSpecifier copy();
	
	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTDeclSpecifier copy(CopyStyle style);

	/**
	 * @deprecated All constants must be defined in {@link IASTDeclSpecifier}
	 */
	@Deprecated
	public static final int sc_last = sc_mutable;
}
