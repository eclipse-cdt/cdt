/*******************************************************************************
 * Copyright (c) 2007, 2015 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;

/**
 * Interface for scopes returned via the index
 * @since 4.0
 */
public interface IIndexScope extends IScope {
	IIndexScope[] EMPTY_INDEX_SCOPE_ARRAY = {};

	/**
	 * Returns the binding associated with the scope.
	 */
	IIndexBinding getScopeBinding();

	/**
	 * Returns the parent scope or {@code null} if the scope is nested in the global scope.
	 */
	@Override
	IIndexScope getParent();

	/**
	 * Returns the name of this scope.
	 */
	@Override
	IIndexName getScopeName();
}
