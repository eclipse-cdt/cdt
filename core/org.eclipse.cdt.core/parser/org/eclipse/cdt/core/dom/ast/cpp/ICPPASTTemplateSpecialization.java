/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

/**
 * This interface represents a template specialization.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTemplateSpecialization extends IASTDeclaration {

	/**
	 * The declaration that the specialization affects.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"ICPPASTTemplateSpecialization.OWNED_DECLARATION - Declaration that the specialization affects"); //$NON-NLS-1$

	/**
	 * Get the declaration.
	 * 
	 * @return <code>IASTDeclaration</code>
	 */
	public IASTDeclaration getDeclaration();

	/**
	 * Set the declaration.
	 * 
	 * @param declaration
	 *            <code>IASTDeclaration</code>
	 */
	public void setDeclaration(IASTDeclaration declaration);

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTemplateSpecialization copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTTemplateSpecialization copy(CopyStyle style);
}
