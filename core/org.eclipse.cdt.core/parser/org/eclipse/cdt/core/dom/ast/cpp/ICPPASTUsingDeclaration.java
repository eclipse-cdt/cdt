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
 *     John Camelon (IBM) - Initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTAttributeOwner;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This interface represents a using declaration.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTUsingDeclaration
		extends IASTDeclaration, IASTNameOwner, IASTAttributeOwner, IASTImplicitNameOwner {
	/**
	 * <code>NAME</code> is the qualified name brought into scope.
	 */
	public static final ASTNodeProperty NAME = new ASTNodeProperty(
			"ICPPASTUsingDeclaration.NAME - Qualified Name brought into scope"); //$NON-NLS-1$

	/**
	 * Was the typename keyword used?
	 *
	 * @param value
	 *            boolean
	 */
	public void setIsTypename(boolean value);

	/**
	 * Set that the typename keyword was/wasn't used.
	 *
	 * @return boolean
	 */
	public boolean isTypename();

	/**
	 * Get the name.
	 *
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Set the name.
	 *
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTUsingDeclaration copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTUsingDeclaration copy(CopyStyle style);
}
