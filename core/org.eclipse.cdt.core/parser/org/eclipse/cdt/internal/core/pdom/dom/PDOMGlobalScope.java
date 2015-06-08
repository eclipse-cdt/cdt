/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * Base class for C and C++ global index scopes. The global index scope contain any bindings.
 * Bindings in the global scope have to be retrieved from the global scope of a particular
 * translation unit.
 */
public abstract class PDOMGlobalScope implements IIndexScope {
	@Override
	public EScopeKind getKind() {
		return EScopeKind.eGlobal;
	}

	@Override
	public IBinding[] find(String name) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve) {
		return null;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) {
		return null;
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,
			IIndexFileSet acceptLocalBindings) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IIndexBinding getScopeBinding() {
		return null;
	}

	@Override
	public IIndexScope getParent() {
		return null;
	}

	@Override
	public IIndexName getScopeName() {
		return null;
	}

	@Override
	public String toString() {
		return "<global scope>"; //$NON-NLS-1$
	}
}
