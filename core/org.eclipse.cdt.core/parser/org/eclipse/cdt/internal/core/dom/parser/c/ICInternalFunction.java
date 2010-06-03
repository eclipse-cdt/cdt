/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;

/**
 * Interface for ast-internal implementations of function bindings.
 */
public interface ICInternalFunction extends ICInternalBinding {
	public void setFullyResolved(boolean resolved);

	public void addDeclarator(IASTDeclarator fnDeclarator);

	/**
	 * Returns whether there is a static declaration for this function.
	 * 
	 * @param resolveAll
	 *            checks for names that are not yet resolved to this binding.
	 */
	public boolean isStatic(boolean resolveAll);
}
