/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
	 * Get the nested template parameters.
	 */
	public ICPPASTTemplateParameter[] getTemplateParameters();

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
	public ICPPASTTemplatedTypeTemplateParameter copy();

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
	 * Set the name of this template template parameter.
	 */
	public void setName(IASTName name);

	/**
	 * Set default value for the template template parameter.
	 */
	public void setDefaultValue(IASTExpression expression);

	/**
	 * @deprecated Use {@link #addTemplateParameter(ICPPASTTemplateParameter)};
	 */
	@Deprecated
	public void addTemplateParamter(ICPPASTTemplateParameter parm);
}
