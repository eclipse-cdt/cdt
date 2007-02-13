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

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.index.IIndexScope;

public abstract class CompositeScope implements IIndexScope {
	/**
	 * The factory used for obtaining further composite bindings
	 */
	protected final ICompositesFactory cf;
	/**
	 * The representative binding for this composite binding. Most cases are simple
	 * enough that this becomes a delegate, some need to use it as a search key over fragments,
	 * and some ignore it as a representative binding from each fragment is needed to meet interface
	 * contracts.
	 */
	protected final IBinding rbinding;

	public CompositeScope(ICompositesFactory cf, IBinding rbinding) {
		if(cf==null || rbinding==null)
			throw new NullPointerException();
		this.cf = cf;
		this.rbinding = rbinding;
	}
	
	final public IScope getParent() throws DOMException {
		IScope rscope = rbinding.getScope();
		if(rscope!=null) {
			return cf.getCompositeScope(rscope);
		}
		return null;
	}

	// Note: for c++ namespaces we are returning an arbitrary name 
	final public IName getScopeName() throws DOMException {
		if(rbinding instanceof IScope)
			return ((IScope) rbinding).getScopeName();
		return null;
	}

	protected final void fail() {
		throw new CompositingNotImplementedError();
	}
	
	
	public IBinding getRawScopeBinding() {
		return rbinding;
	}
}
