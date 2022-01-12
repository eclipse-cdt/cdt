/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bryan Wilkinson (QNX) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Interface for a code completion's context. Used for context-sensitive
 * finding of bindings with a certain name or prefix.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 4.0
 */
public interface IASTCompletionContext {
	/**
	 * Returns bindings that start with the given name or prefix, only
	 * considering those that are valid for this context.
	 *
	 * @param n the name containing a prefix
	 * @return valid bindings in this context for the given prefix
	 */
	IBinding[] findBindings(IASTName n, boolean isPrefix);
}
