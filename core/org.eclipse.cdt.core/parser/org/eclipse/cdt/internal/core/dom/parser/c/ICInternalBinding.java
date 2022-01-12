/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Niefer (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * Methods needed by CVisitor but not meant for public interface
 */
public interface ICInternalBinding {
	public IASTNode getPhysicalNode();

	/**
	 * Returns the declarations for this binding.
	 * @since 5.0
	 */
	public IASTNode[] getDeclarations();

	/**
	 * Returns the definitions for this binding.
	 * @since 5.0
	 */
	public IASTNode getDefinition();
}
