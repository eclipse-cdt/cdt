/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTPointer;

/**
 * This is a pointer to member pointer operator for declarators.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTPointerToMember extends IASTPointer, IASTNameOwner {

	/**
	 * This property refers to the nested name.
	 */
	public static final ASTNodeProperty NAME = new ASTNodeProperty("ICPPASTPointerToMember.NAME - The nested Name"); //$NON-NLS-1$

	/**
	 * Set the name.
	 *
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * Get the name.
	 *
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTPointerToMember copy();

}
