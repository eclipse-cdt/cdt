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
 *     Doug Schaefer (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTAlignmentSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

/**
 * C extension to IASTDeclSpecifier. (restrict keyword)
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICASTDeclSpecifier extends IASTDeclSpecifier {
	/**
	 * @since 6.0
	 */
	public static final ASTNodeProperty ALIGNMENT_SPECIFIER = new ASTNodeProperty(
			"ICASTDeclSpecifier.ALIGNMENT_SPECIFIER - Alignment specifier"); //$NON-NLS-1$

	/**
	 * @since 5.1
	 */
	@Override
	public ICASTDeclSpecifier copy();

	@Override
	public IASTAlignmentSpecifier[] getAlignmentSpecifiers();

	@Override
	public void setAlignmentSpecifiers(IASTAlignmentSpecifier[] alignmentSpecifiers);
}
