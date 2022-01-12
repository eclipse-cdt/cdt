/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICFunctionScope extends ICScope {

	/**
	 * Get the scope representing the function body . returns null if there is
	 * no function definition
	 *
	 * @throws DOMException
	 */
	public IScope getBodyScope() throws DOMException;

	/**
	 * return the ILabel binding in this scope that matches the given name
	 *
	 * @param name
	 * @throws DOMException
	 */
	public IBinding getBinding(char[] name) throws DOMException;

}
