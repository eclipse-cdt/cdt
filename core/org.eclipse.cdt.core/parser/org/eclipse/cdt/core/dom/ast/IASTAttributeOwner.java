/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Prigogin (Google) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * An AST node that may have attributes.
 * @since 5.4
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTAttributeOwner extends IASTNode {
	public static final ASTNodeProperty ATTRIBUTE = 
			new ASTNodeProperty("IASTAttributeOwner.ATTRIBUTE"); //$NON-NLS-1$

	/**
	 * Returns the array of attributes.
	 */
	public IASTAttribute[] getAttributes();

	/**
	 * Adds an attribute to the node.
	 */
	public void addAttribute(IASTAttribute attribute);
}
