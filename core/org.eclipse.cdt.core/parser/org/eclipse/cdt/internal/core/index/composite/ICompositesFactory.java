/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;

public interface ICompositesFactory {
	
	public IScope getCompositeScope(IIndexScope rscope) throws DOMException;

	/**
	 * Returns a composite (in the sense of potentially spanning multiple index fragments - i.e. not to be confused
	 * with ICompositeType) type for the specified type.
	 * @param index
	 * @param type
	 * @return
	 */
	public IType getCompositeType(IIndexType rtype) throws DOMException;

	/**
	 * Returns a composite (index context carrying) binding for the specified binding. It does not
	 * matter which fragment the specified binding comes from
	 * @param index the context to construct the composite binding for
	 * @param binding a binding that will be used when searching for information to return from the composite
	 * binding methods
	 * @return a composite (index context carrying) binding for the specified binding
	 */
	public IIndexBinding getCompositeBinding(IIndexFragmentBinding binding);

	/**
	 * Identifies common bindings, calls getCompositeBindings
	 * @param index
	 * @param bindings
	 * @return
	 */
	public IIndexBinding[] getCompositeBindings(IIndexFragmentBinding[][] bindings);
}
