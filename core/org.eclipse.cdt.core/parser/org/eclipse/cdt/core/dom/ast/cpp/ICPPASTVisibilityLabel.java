/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

/**
 * C++ allows for visibility labels to be mixed with declarations in class specifiers.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTVisibilityLabel extends IASTDeclaration {
	/**
	 * <code>v_public</code> == public:
	 */
	public static final int v_public = 1;

	/**
	 * <code>v_protected</code> == protected:
	 */
	public static final int v_protected = 2;

	/**
	 * <code>v_private</code> == private:
	 */
	public static final int v_private = 3;

	/**
	 * Returns the visibility.
	 *
	 * @return int
	 */
	public int getVisibility();

	/**
	 * Sets visibility.
	 *
	 * @param visibility one of v_public, v_protected, v_private
	 */
	public void setVisibility(int visibility);

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTVisibilityLabel copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTVisibilityLabel copy(CopyStyle style);
}
