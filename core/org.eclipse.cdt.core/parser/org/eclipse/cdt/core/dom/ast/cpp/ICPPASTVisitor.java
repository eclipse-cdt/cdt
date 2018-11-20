/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

/**
 * Interface for visitors to visit c-specific nodes.
 * @since 5.0
 */
public interface ICPPASTVisitor {

	/**
	 * Visits a base class specifier of a composite type.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE}, {@link ASTVisitor#PROCESS_SKIP} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int visit(ICPPASTBaseSpecifier baseSpecifier);

	/**
	 * Visits a base class specifier of a composite type.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int leave(ICPPASTBaseSpecifier baseSpecifier);

	/**
	 * Visits a namespace definition.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE}, {@link ASTVisitor#PROCESS_SKIP} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int visit(ICPPASTNamespaceDefinition namespaceDefinition);

	/**
	 * Visits a namespace definition.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int leave(ICPPASTNamespaceDefinition namespaceDefinition);

	/**
	 * Visits a template parameter.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE}, {@link ASTVisitor#PROCESS_SKIP} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int visit(ICPPASTTemplateParameter templateParameter);

	/**
	 * Visits a template parameter.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int leave(ICPPASTTemplateParameter templateParameter);
}
