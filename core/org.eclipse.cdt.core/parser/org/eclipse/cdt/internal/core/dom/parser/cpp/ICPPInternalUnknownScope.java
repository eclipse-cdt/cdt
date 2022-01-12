/*******************************************************************************
 * Copyright (c) 2008, 2012 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Scope corresponding to an unknown binding.
 */
public interface ICPPInternalUnknownScope extends ICPPASTInternalScope {

	/**
	 * @return Returns the binding corresponding to the scope.
	 */
	public IType getScopeType();
}
