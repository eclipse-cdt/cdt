/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * Provides built-in symbols to the parser.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 *
 * @since 4.0
 */
public interface IBuiltinBindingsProvider {

	/**
	 * Get additional built-in bindings for the given scope.
	 *
	 * @param scope the scope the bindings are added to
	 * @return an array of {@link IBinding}s, may not return <code>null</code>
	 */
	public IBinding[] getBuiltinBindings(IScope scope);

	/**
	 * Returns whether the given name names a known builtin.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public boolean isKnownBuiltin(char[] builtinName);
}
