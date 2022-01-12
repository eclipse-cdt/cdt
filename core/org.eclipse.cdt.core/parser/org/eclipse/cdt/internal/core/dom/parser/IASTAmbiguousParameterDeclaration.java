/*******************************************************************************
 * Copyright (c) 2008 IBM Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;

/**
 * Needed to handle the ambiguity for parameter declarations in plain C
 * @since 5.0
 */
public interface IASTAmbiguousParameterDeclaration extends IASTParameterDeclaration {

	public static final ASTNodeProperty SUBDECLARATION = new ASTNodeProperty(
			"IASTAmbiguousParameterDeclaration.SUBDECLARATION"); //$NON-NLS-1$

	/**
	 * Add an alternative to this ambiguous parameter declaration.
	 */
	public void addParameterDeclaration(IASTParameterDeclaration e);

	/**
	 * Return an array of all alternatives for this ambiguous parameter declaration.
	 */
	public IASTParameterDeclaration[] getParameterDeclarations();
}
