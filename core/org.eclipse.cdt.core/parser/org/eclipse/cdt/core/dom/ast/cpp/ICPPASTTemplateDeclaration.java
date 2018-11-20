/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

/**
 * Template declaration.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTemplateDeclaration extends IASTDeclaration {
	/**
	 * <code>OWNED_DECLARATION</code> is the subdeclaration that we maintain grammatically.
	 */
	public static final ASTNodeProperty OWNED_DECLARATION = new ASTNodeProperty(
			"ICPPASTTemplateDeclaration.OWNED_DECLARATION - Subdeclaration maintained grammatically"); //$NON-NLS-1$

	/**
	 * <code>PARAMETER</code> is used for template parameters.
	 */
	public static final ASTNodeProperty PARAMETER = new ASTNodeProperty(
			"ICPPASTTemplateDeclaration.PARAMETER - Template Parameter"); //$NON-NLS-1$

	/**
	 * Is the export keyword used?
	 */
	public boolean isExported();

	/**
	 * Should the export keyword be used?
	 */
	public void setExported(boolean value);

	/**
	 * Returns the template declaration.
	 */
	public IASTDeclaration getDeclaration();

	/**
	 * Sets the template declaration.
	 *
	 * @param declaration the declaration to set
	 */
	public void setDeclaration(IASTDeclaration declaration);

	/**
	 * Returns the template parameters.
	 */
	public ICPPASTTemplateParameter[] getTemplateParameters();

	/**
	 * Adds a template parameter.
	 *
	 * @param paramm the parameter to add
	 * @since 5.2
	 */
	public void addTemplateParameter(ICPPASTTemplateParameter paramm);

	/**
	 * @deprecated Use {@link #addTemplateParameter(ICPPASTTemplateParameter)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void addTemplateParamter(ICPPASTTemplateParameter paramm);

	/**
	 * Returns the template scope representing this declaration in the logical tree.
	 */
	public ICPPTemplateScope getScope();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTemplateDeclaration copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTTemplateDeclaration copy(CopyStyle style);
}
