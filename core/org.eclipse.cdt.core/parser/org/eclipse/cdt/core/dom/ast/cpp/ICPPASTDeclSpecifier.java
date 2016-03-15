/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

/**
 * C++ adds additional modifiers and types for decl specifier sequence.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTDeclSpecifier extends IASTDeclSpecifier, IASTAttributeOwner {
	// A declaration in C++ can be a friend declaration
	/**
	 * Is this a friend declaration?
	 *
	 * @return boolean
	 */
	public boolean isFriend();

	/**
	 * Sets this to be a friend declaration true/false.
	 *
	 * @param value the new value
	 */
	public void setFriend(boolean value);

	/**
	 * Is this a virtual function?
	 *
	 * @return boolean
	 */
	public boolean isVirtual();

	/**
	 * Sets this declaration to be virtual.
	 *
	 * @param value the new value
	 */
	public void setVirtual(boolean value);

	/**
	 * Is this an explicit constructor?
	 *
	 * @return boolean
	 */
	public boolean isExplicit();

	/**
	 * Sets this to be an explicit constructor.
	 *
	 * @param value the new value
	 */
	public void setExplicit(boolean value);

	/**
	 * Is this a constexpr
	 *
	 * @return boolean
	 * @since 5.4
	 */
	public boolean isConstexpr();

	/**
	 * Sets this to be constexpr.
	 *
	 * @param value the new value
	 * @since 5.4
	 */
	public void setConstexpr(boolean value);

	/**
	 * Is this thread_local
	 * 
	 * @return boolean
	 * @since 5.4
	 */
	public boolean isThreadLocal();

	/**
	 * Sets this to be thread_local.
	 *
	 * @param value the new value
	 * @since 5.4
	 */
	public void setThreadLocal(boolean value);

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
}
