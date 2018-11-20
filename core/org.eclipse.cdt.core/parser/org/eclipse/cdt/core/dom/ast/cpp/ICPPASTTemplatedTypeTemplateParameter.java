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
 *    John Camelon (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This is a template template parameter as <code> V </code> in
 * <code>template&lttemplate&lttypename T&gt class V&gt class CT;</code>
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTemplatedTypeTemplateParameter extends ICPPASTTemplateParameter, IASTNameOwner {

	/**
	 * Relation between template template parameter and its (nested) template parameters.
	 */
	public static final ASTNodeProperty PARAMETER = new ASTNodeProperty(
			"ICPPASTTemplateTypeTemplateParameter.PARAMETER [ICPPASTTemplateParameter]"); //$NON-NLS-1$

	/**
	 * Relation between template template parameter and its name.
	 */
	public static final ASTNodeProperty PARAMETER_NAME = new ASTNodeProperty(
			"ICPPASTTemplateTypeTemplateParameter.PARAMETER_NAME [ICPPASTName]"); //$NON-NLS-1$

	/**
	 * Relation between template template parameter and its default value.
	 */
	public static final ASTNodeProperty DEFAULT_VALUE = new ASTNodeProperty(
			"ICPPASTTemplateTypeTemplateParameter.DEFAULT_VALUE [IASTExpression]"); //$NON-NLS-1$

	/**
	 * <code>tt_class</code> represents a class.
	 * @since 6.6
	 */
	public static final int tt_class = 1;

	/**
	 * <code>tt_typename</code> represents a typename.
	 * @since 6.6
	 */
	public static final int tt_typename = 2;

	/**
	 * Get the nested template parameters.
	 */
	public ICPPASTTemplateParameter[] getTemplateParameters();

	/**
	 * Get the type of the template-template parameter (either {@link #tt_class} or {@link #tt_typename}).
	 * @since 6.6
	 */
	public int getParameterType();

	/**
	 * Get the (optional) name of this template template parameter. In case there is no name an
	 * empty name is returned.
	 */
	public IASTName getName();

	/**
	 * Get default value for template template parameter or <code>null</code>.
	 */
	public IASTExpression getDefaultValue();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTemplatedTypeTemplateParameter copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTTemplatedTypeTemplateParameter copy(CopyStyle style);

	/**
	 * Add a nested template parameter.
	 * @since 5.3
	 */
	public void addTemplateParameter(ICPPASTTemplateParameter parm);

	/**
	 * Set whether this template template parameter is a parameter pack.
	 * @since 5.2
	 */
	public void setIsParameterPack(boolean val);

	/**
	 * Set the type of the template-template parameter.
	 * @param type The type of the template-template parameter (either {@link #tt_class} or {@link #tt_typename})
	 * @since 6.6
	 */
	public void setParameterType(int type);

	/**
	 * Set the name of this template template parameter.
	 */
	public void setName(IASTName name);

	/**
	 * Set default value for the template template parameter.
	 */
	public void setDefaultValue(IASTExpression expression);

	/**
	 * Returns the scope that contains the template parameters of this template-template parameter.
	 * @since 5.4
	 */
	public ICPPScope asScope();

	/**
	 * @deprecated Use {@link #addTemplateParameter(ICPPASTTemplateParameter)}
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void addTemplateParamter(ICPPASTTemplateParameter parm);
}
