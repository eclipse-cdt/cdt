/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
