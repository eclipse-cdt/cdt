/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * Place holder of the inherited class from struct or class(IStructure).
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IInheritance {
	/**
	 * Return the inherited structures names.
	 */
	public String[] getSuperClassesNames();

	/**
	 * Returns the super class access : ASTAccessVisibility
	 */
	public ASTAccessVisibility getSuperClassAccess(String name);
}
