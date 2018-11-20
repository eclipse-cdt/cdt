/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * Interface for methods on scopes that are internal to the AST.
 */
public interface IASTInternalScope extends IScope {
	/**
	 * Returns the physical IASTNode that this scope was created for
	 */
	public IASTNode getPhysicalNode();

	/**
	 * Adds an IBinding to the scope.  It is primarily used by the parser to add
	 * implicit IBindings to the scope (such as GCC built-in functions).
	 */
	public void addBinding(IBinding binding);

	/**
	 * Adds an IASTName to be cached in this scope.
	 * @param adlOnly whether this declaration of this name only makes the name visible to
	 *                argument-dependent lookup
	 *
	 * Implementation note: only CPPNamespaceScope cares about "adlOnly".
	 */
	public void addName(IASTName name, boolean adlOnly);

	/**
	 * Can be called during ambiguity resolution to populate a scope without considering
	 * the ambiguous branches. The rest of the names has to be cached one by one after
	 * the ambiguities have been resolved.
	 */
	public void populateCache();

	/**
	 * Can be called during ambiguity resolution to remove the names within the given
	 * node from the cache.
	 */
	public void removeNestedFromCache(IASTNode container);
}
