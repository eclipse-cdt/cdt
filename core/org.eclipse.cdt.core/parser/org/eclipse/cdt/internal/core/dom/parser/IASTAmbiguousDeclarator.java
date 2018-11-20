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
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;

/**
 * Needed to handle the ambiguous declarator.
 * @since 5.0
 */
public interface IASTAmbiguousDeclarator extends IASTDeclarator {

	public static final ASTNodeProperty SUBDECLARATOR = new ASTNodeProperty("IASTAmbiguousDeclarator.SUBDECLARATOR"); //$NON-NLS-1$

	/**
	 * Add an alternative to this ambiguous declarator.
	 */
	public void addDeclarator(IASTDeclarator e);

	/**
	 * Return an array of all alternatives for this ambiguous declarator.
	 */
	public IASTDeclarator[] getDeclarators();
}
