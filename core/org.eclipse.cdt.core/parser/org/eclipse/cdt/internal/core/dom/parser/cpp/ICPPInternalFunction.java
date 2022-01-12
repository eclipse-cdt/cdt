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
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Interface for ast-internal implementations of function bindings.
 */
public interface ICPPInternalFunction extends ICPPInternalBinding, ICPPComputableFunction {
	/**
	 * Called to resolve the parameter in the second phase.
	 */
	public IBinding resolveParameter(CPPParameter parameter);

	/**
	 * Returns whether there is a static declaration for this function.
	 * @param resolveAll checks for names that are not yet resolved to this binding.
	 */
	public boolean isStatic(boolean resolveAll);
}
