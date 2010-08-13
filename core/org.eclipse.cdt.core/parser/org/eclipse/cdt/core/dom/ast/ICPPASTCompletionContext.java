/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tomasz Wesolowski - initial API and implementation
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
