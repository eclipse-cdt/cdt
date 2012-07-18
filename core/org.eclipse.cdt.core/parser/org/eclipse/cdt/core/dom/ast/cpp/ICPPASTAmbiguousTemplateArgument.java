/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * @param idExpression a non-null id-expression or a pack expansion of an id-expression
	 * @since 5.2
	 */
	public void addIdExpression(IASTExpression idExpression);

	/**
	 * Add an partial parse tree that could be a suitable subtree representing
	 * the template argument
	 * @param typeId a non-null type-id
	 */
	public void addTypeId(IASTTypeId typeId);
	
	/**
	 * @deprecated Replaced by {@link #addIdExpression(IASTExpression)}.
	 */
	@Deprecated
	public void addIdExpression(IASTIdExpression idExpression);
}
