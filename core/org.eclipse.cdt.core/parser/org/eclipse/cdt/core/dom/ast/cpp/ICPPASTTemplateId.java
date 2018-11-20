/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
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
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTemplateId extends ICPPASTName, IASTNameOwner {
	/**
	 * The template name in the template ID.
	 */
	public static final ASTNodeProperty TEMPLATE_NAME = new ASTNodeProperty(
			"ICPPASTTemplateId.TEMPLATE_NAME - TemplateId Name"); //$NON-NLS-1$

	/**
	 * TEMPLATE_ID_ARGUMENT = template id argument.
	 */
	public static final ASTNodeProperty TEMPLATE_ID_ARGUMENT = new ASTNodeProperty(
			"ICPPASTTemplateId.TEMPLATE_ID_ARGUMENT - TemplateId Argument"); //$NON-NLS-1$

	/**
	 * @deprecated Use IASTNode.EMPTY_NODE_ARRAY instead.
	 * @noreference This field is not intended to be referenced by clients.
	 */
	@Deprecated
	public static final IASTNode[] EMPTY_ARG_ARRAY = IASTNode.EMPTY_NODE_ARRAY;

	/**
	 * Returns the name of the template.
	 *
	 * @return {@code IASTName}
	 */
	public IASTName getTemplateName();

	/**
	 * Sets the name of the template.
	 *
	 * @param name {@code IASTName}
	 */
	public void setTemplateName(IASTName name);

	/**
	 * Adds template argument.
	 *
	 * @param typeId {@code IASTTypeId}
	 */
	public void addTemplateArgument(IASTTypeId typeId);

	/**
	 * Adds a template argument.
	 *
	 * @param expression {@code IASTExpression}
	 */
	public void addTemplateArgument(IASTExpression expression);

	/**
	 * Adds an ambiguity node for later resolution.
	 *
	 * @param ambiguity
	 */
	public void addTemplateArgument(ICPPASTAmbiguousTemplateArgument ambiguity);

	/**
	 * Returns all template arguments as nodes.
	 *
	 * @return nodes representing the template arguments
	 */
	public IASTNode[] getTemplateArguments();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTTemplateId copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTTemplateId copy(CopyStyle style);
}
