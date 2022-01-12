/*******************************************************************************
 * Copyright (c) 2010, 2012 Tomasz Wesolowski and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface for a code completion's context. Used for context-sensitive finding of bindings with a certain
 * name or prefix, including additional lookup requested namespaces.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 5.3
 */
public interface ICPPASTCompletionContext extends IASTCompletionContext {
	/**
	 * Returns bindings that start with the given name or prefix, only considering those that are valid for
	 * this context, including those in the requested set of namespaces.
	 *
	 * @param n
	 *            the name containing a prefix
	 * @param namespaces
	 *            the qualified names of additional namespaces to check for bindings, or null
	 * @return valid bindings in this context for the given prefix
	 */
	IBinding[] findBindings(IASTName n, boolean isPrefix, String[] namespaces);
}
