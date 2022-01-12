/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface for AST nodes that can nest in initializer lists.
 * @since 5.2
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTInitializerClause extends IASTNode {
	@Override
	IASTInitializerClause copy();

	/**
	 * @since 5.3
	 */
	@Override
	IASTInitializerClause copy(CopyStyle style);
}
