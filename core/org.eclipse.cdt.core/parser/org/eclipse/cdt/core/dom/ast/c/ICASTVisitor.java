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
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;

/**
 * Interface for visitors to visit c-specific nodes.
 * @since 5.0
 */
public interface ICASTVisitor {

	/**
	 * Visits a designator.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE}, {@link ASTVisitor#PROCESS_SKIP} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int visit(ICASTDesignator designator);

	/**
	 * Visits a designator.
	 * @return {@link ASTVisitor#PROCESS_CONTINUE} or {@link ASTVisitor#PROCESS_ABORT}
	 */
	int leave(ICASTDesignator designator);
}
