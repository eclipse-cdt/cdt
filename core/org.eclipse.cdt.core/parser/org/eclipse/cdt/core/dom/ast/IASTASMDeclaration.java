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
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * ASM Statement as a Declaration.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTASMDeclaration extends IASTDeclaration {

	/**
	 * Get the assembly value.
	 *
	 */
	public String getAssembly();

	/**
	 * Set the assembly value.
	 *
	 * @param assembly
	 */
	public void setAssembly(String assembly);

	/**
	 * @since 5.1
	 */
	@Override
	public IASTASMDeclaration copy();

	/**
	 * @since 5.3
	 */
	@Override
	public IASTASMDeclaration copy(CopyStyle style);
}
