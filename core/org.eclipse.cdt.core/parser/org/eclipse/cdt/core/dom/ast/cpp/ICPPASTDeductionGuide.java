/*******************************************************************************
 * Copyright (c) 2018, Institute for Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Felix Morgner - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;

/**
 * A C++ class-template argument deduction guide
 * <p>
 * e.g:
 *
 * <pre>
 * SomeTemplateName(int) -> SomeTemplateName&lt;float&gt;;
 *
 * template&lt;typename I&gt;
 * SomeTemplateName(I, I) -> SomeTemplateName&lt;typename std::iterator_traits&lt;I&gt;::value_type&gt;;
 * </pre>
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 6.6
 */
public interface ICPPASTDeductionGuide extends IASTDeclaration, ICPPASTParameterListOwner {

	/**
	 * <code>TEMPLATE_NAME</code> represents the relationship between an
	 * <code>ICPPASTDeductionGuide</code> and it's nested
	 * <code>IASTName</code>.
	 */
	static final ASTNodeProperty TEMPLATE_NAME = new ASTNodeProperty(
			"ICPPASTDeductionGuide.TEMPLATE_NAME - IASTName for ICPPASTDeductionGuide"); //$NON-NLS-1$

	/**
	 * <code>TEMPLATE_ID</code> represents the relationship between an
	 * <code>ICPPASTDeductionGuide</code> and it's nested
	 * <code>ICPPASTTemplateId</code>.
	 */
	static final ASTNodeProperty TEMPLATE_ID = new ASTNodeProperty(
			"ICPPASTDeductionGuide.TEMPLATE_ID - ICPPASTTemplateId for ICPPASTDeductionGuide"); //$NON-NLS-1$

	/**
	 * <code>PARAMETER</code> represents the relationship between an
	 * <code>ICPPASTDeductionGuide</code> and it's nested
	 * <code>IASTParameterDeclaration</code>.
	 */
	public final static ASTNodeProperty PARAMETER = new ASTNodeProperty(
			"ICPPASTDeductionGuide.PARAMETER - IASTParameterDeclaration for ICPPASTDeductionGuide"); //$NON-NLS-1$

	/**
	 * Check if the deduction guide was declared as 'explicit'.
	 */
	boolean isExplicit();

	/**
	 * Set whether or not the deduction guide is marked explicit
	 */
	void setExplicit(boolean isExplict);

	/**
	 * Get the name of the template type the deduction guide refers to
	 */
	IASTName getTemplateName();

	/**
	 * Set the name of the template type the deduction guide refers to
	 */
	void setTemplateName(IASTName name);

	/**
	 * Get the template id of the deduction guide
	 */
	ICPPASTTemplateId getSimpleTemplateId();

	/**
	 * Set the template id of the deduction guide
	 */
	void setSimpleTemplateId(ICPPASTTemplateId id);

	@Override
	public ICPPASTDeductionGuide copy();

	@Override
	public ICPPASTDeductionGuide copy(CopyStyle style);

}
