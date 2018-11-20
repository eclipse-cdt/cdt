/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents a mechanism for a name to discover more information about it's parent.
 * All interfaces that claim ownership/residence of a name should extend this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTNameOwner {
	/**
	 * Role of name in this context is a declaration.
	 */
	public static final int r_declaration = 0;
	/**
	 * Role of name in this construct is a reference.
	 */
	public static final int r_reference = 1;

	/**
	 * Role of name in this construct is a definition.
	 */
	public static final int r_definition = 2;
	/**
	 * Role is unclear.
	 */
	public static final int r_unclear = 3;

	/**
	 * Get the role for the name.
	 *
	 * @param name the name to determine the role for.
	 * @return r_definition, r_declaration, r_reference or r_unclear.
	 */
	public int getRoleForName(IASTName name);
}
