/*******************************************************************************
 * Copyright (c) 2012, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Prigogin (Google) - Initial API and implementation
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * An AST node that may have attributes.
 * @since 5.4
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTAttributeOwner extends IASTNode {
	/** @since 5.7 */
	public static final ASTNodeProperty ATTRIBUTE_SPECIFIER = 
			new ASTNodeProperty("IASTAttributeOwner.ATTRIBUTE_SPECIFIER"); //$NON-NLS-1$
	/** @deprecated Not used. */
	@Deprecated
	public static final ASTNodeProperty ATTRIBUTE = 
			new ASTNodeProperty("IASTAttributeOwner.ATTRIBUTE"); //$NON-NLS-1$

	/**
	 * Returns an array of all the node's attribute specifiers.
	 * @since 5.7
	 */
	public IASTAttributeSpecifier[] getAttributeSpecifiers();

	/**
	 * Adds an attribute specifier to the node.
	 * @since 5.7
	 */
	public void addAttributeSpecifier(IASTAttributeSpecifier attributeSpecifier);

	/**
	 * Returns the array of all attributes.
	 */
	public IASTAttribute[] getAttributes();

	/**
	 * @deprecated Ignored. Attributes should not be assigned to nodes directly, but have to be
	 * wrapped by attribute specifiers.
	 */
	@Deprecated
	public void addAttribute(IASTAttribute attribute);
}
