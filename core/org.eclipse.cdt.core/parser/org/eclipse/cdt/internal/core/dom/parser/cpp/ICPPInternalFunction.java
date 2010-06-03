/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * Interface for ast-internal implementations of function bindings.
 */
public interface ICPPInternalFunction extends ICPPInternalBinding {

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
