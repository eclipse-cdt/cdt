/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the declarator for an array.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTArrayDeclarator extends IASTDeclarator {
	/**
	 * Node property that describes the relationship between an
	 * <code>IASTArrayDeclarator</code> and an <code>IASTArrayModifier</code>.
	 */
	public static final ASTNodeProperty ARRAY_MODIFIER = new ASTNodeProperty(
			"IASTArrayDeclarator.ARRAY_MODIFIER - IASTArrayModifier for IASTArrayDeclarator"); //$NON-NLS-1$

	/**
	 * Get all <code>IASTArrayModifier</code>'s for this declarator.
	 * 
	 * @return array of <code>IASTArrayModifier</code>
	 */
	public IASTArrayModifier[] getArrayModifiers();

	/**
	 * Add an <code>IASTArrayModifier</code> to this declarator
	 * 
	 * @param arrayModifier
	 *            <code>IASTArrayModifier</code> to be added
	 */
	public void addArrayModifier(IASTArrayModifier arrayModifier);
	
	/**
	 * @since 5.1
	 */
	@Override
	public IASTArrayDeclarator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTArrayDeclarator copy(CopyStyle style);

}
