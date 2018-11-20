/*******************************************************************************
 * Copyright (c) 2008, 2013 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial Implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * Place-holder in the AST for template arguments that are not yet understood.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTAmbiguousTemplateArgument extends IASTNode {
	/**
	 * Add an partial parse tree that could be a suitable subtree representing
	 * the template argument
	 * @param expression a non-null expression
	 * @since 5.6
	 */
	public void addExpression(IASTExpression expression);

	/**
	 * Add an partial parse tree that could be a suitable subtree representing
	 * the template argument
	 * @param typeId a non-null type-id
	 */
	public void addTypeId(IASTTypeId typeId);

	/**
	 * @deprecated Replaced by {@link #addExpression(IASTExpression)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void addIdExpression(IASTExpression idExpression);

	/**
	 * @deprecated Replaced by {@link #addIdExpression(IASTExpression)}.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public void addIdExpression(IASTIdExpression idExpression);
}
