/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

/**
 * This interface represents an explicit template instantiation.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTExplicitTemplateInstantiation extends IASTDeclaration {

	/**
	 * Gnu extension.
	 * @since 5.2
	 */
	public static final int STATIC = 1;
	/**
	 * Gnu extension.
	 * @since 5.2
	 */
	public static final int INLINE = 2;
	/**
	 * C++0x.
	 * @since 5.2
	 */
	public static final int EXTERN = 3;

	/**
	 * <code>OWNED_DECLARATION</code> represents the role of the inner
	 * declaration that this template refers to.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"ICPPASTExplicitTemplateInstantiation.OWNED_DECLARATION - Role of inner declaration template refers to"); //$NON-NLS-1$

	/**
	 * Get the owned declaration.
	 * 
	 * @return <code>IASTDeclaration</code>
	 */
	public IASTDeclaration getDeclaration();

	/**
	 * Set the owned declaration.
	 * 
	 * @param declaration
	 *            <code>IASTDeclaration</code>
	 */
	public void setDeclaration(IASTDeclaration declaration);

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTExplicitTemplateInstantiation copy();
	
	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTExplicitTemplateInstantiation copy(CopyStyle style);

	/**
	 * Returns {@link #STATIC}, {@link #INLINE}, {@link #EXTERN}, or <code>0</code>.
	 * 
	 * @since 5.2
	 */
	public int getModifier();

	/**
	 * Set the modifier value, not allowed on frozen ast.
	 * @since 5.2
	 */
	public void setModifier(int value);
}
