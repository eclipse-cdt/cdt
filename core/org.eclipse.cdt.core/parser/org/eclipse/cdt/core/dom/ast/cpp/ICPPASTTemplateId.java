/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * @author jcamelon
 */
public interface ICPPASTTemplateId extends IASTName {

	/**
	 * TEMPLATE_NAME is the IASTName.
	 */
	public static final ASTNodeProperty TEMPLATE_NAME = new ASTNodeProperty(
			"TemplateId Name"); //$NON-NLS-1$

	/**
	 * Get the name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getTemplateName();

	/**
	 * Set the name.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setTemplateName(IASTName name);

	/**
	 * TEMPLATE_ID_ARGUMENT = template id argument.
	 */
	public static final ASTNodeProperty TEMPLATE_ID_ARGUMENT = new ASTNodeProperty(
			"TemplateId Arg"); //$NON-NLS-1$

	/**
	 * Constant.
	 */
	public static final IASTNode[] EMPTY_ARG_ARRAY = new IASTNode[0];

	/**
	 * Add template argument.
	 * 
	 * @param typeId
	 *            <code>IASTTypeId</code>
	 */
	public void addTemplateArgument(IASTTypeId typeId);

	/**
	 * Add template argument.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void addTemplateArgument(IASTExpression expression);

	/**
	 * Get all template arguments. (as nodes)
	 * 
	 * @return <code>IASTNode []</code>
	 */
	public IASTNode[] getTemplateArguments();

}
