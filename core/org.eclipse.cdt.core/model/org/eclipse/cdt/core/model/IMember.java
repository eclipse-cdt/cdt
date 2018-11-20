/**********************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * Common protocol for C elements that can be members of types.
 * This set consists of <code>IType</code>, <code>IMethod</code>,
 * <code>IField</code>.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IMember extends IDeclaration {

	/**
	 * Returns the member's visibility
	 * V_PRIVATE = 0 V_PROTECTED = 1 V_PUBLIC = 2
	 * @return int
	 */
	public ASTAccessVisibility getVisibility() throws CModelException;

}
