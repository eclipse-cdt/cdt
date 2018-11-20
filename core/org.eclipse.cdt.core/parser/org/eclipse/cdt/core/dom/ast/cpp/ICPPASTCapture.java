/*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Capture for a lambda expression, introduced in C++0x.
 *
 * @since 5.3
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTCapture extends IASTNode, ICPPASTPackExpandable, IASTNameOwner {
	ASTNodeProperty IDENTIFIER = new ASTNodeProperty("ICPPASTCapture - IDENTIFIER [IASTName]"); //$NON-NLS-1$

	@Override
	ICPPASTCapture copy();

	@Override
	ICPPASTCapture copy(CopyStyle style);

	/**
	 * Returns whether the capture uses a leading ampersand.
	 */
	boolean isByReference();

	/**
	 * Returns whether this capture is for the this pointer.
	 */
	boolean capturesThisPointer();

	/**
	 * Returns the identifier for this capture or <code>null</code>, when
	 * <code>this<code> is captured.
	 */
	IASTName getIdentifier();

	/**
	 * Not allowed on frozen AST.
	 * @see #isByReference()
	 */
	void setIsByReference(boolean value);

	/**
	 * Not allowed on frozen AST. Providing a <code>null</code> identifier indicates
	 * that this capture is for the this pointer.
	 * @see #getIdentifier()
	 */
	void setIdentifier(IASTName identifier);
}
