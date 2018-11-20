/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;

/**
 * This is C++'s reference operator, i.e. &, used in a declarator.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTReferenceOperator extends IASTPointerOperator {
	/**
	 * Returns whether the operator denotes a rvalue reference (e.g. <code>int &&</code>).
	 * @since 5.2
	 */
	public boolean isRValueReference();

	/**
	 * @since 5.1
	 */
	@Override
	public ICPPASTReferenceOperator copy();

	/**
	 * @since 5.3
	 */
	@Override
	public ICPPASTReferenceOperator copy(CopyStyle style);
}
