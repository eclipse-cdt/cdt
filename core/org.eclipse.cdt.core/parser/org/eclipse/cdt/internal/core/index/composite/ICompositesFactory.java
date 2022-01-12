/*******************************************************************************
 * Copyright (c) 2007, 2013 Symbian Software Systems and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;

public interface ICompositesFactory {

	public IIndexScope getCompositeScope(IIndexScope rscope);

	/**
	 * Returns a composite (in the sense of potentially spanning multiple index fragments -
	 * i.e. not to be confused with ICompositeType) type for the specified type.
	 */
	public IType getCompositeType(IType rtype);

	/**
	 * Returns a composite (index context carrying) binding for the specified binding. It does not
	 * matter which fragment the specified binding comes from.
	 *
	 * @param binding a binding that will be used when searching for information to return from
	 *     the composite binding methods
	 * @return a composite (index context carrying) binding for the specified binding
	 */
	public IIndexBinding getCompositeBinding(IIndexFragmentBinding binding);

	/**
	 * Identifies common bindings, calls getCompositeBindings
	 */
	public IIndexBinding[] getCompositeBindings(IIndexFragmentBinding[][] bindings);

	/**
	 * Selects all equivalent bindings from the available fragments
	 */
	public IIndexFragmentBinding[] findEquivalentBindings(IBinding binding);

	/**
	 * Converts values.
	 */
	public IValue getCompositeValue(IValue v);
}
