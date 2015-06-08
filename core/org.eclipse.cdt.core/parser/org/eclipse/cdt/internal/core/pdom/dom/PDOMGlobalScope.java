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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexScope;

/**
 * Base class for C and C++ global index scopes.
 */
public abstract class PDOMGlobalScope implements IIndexScope {
	@Override
	public EScopeKind getKind() {
		return EScopeKind.eGlobal;
	}

	@Override
	public IBinding[] find(String name, IASTTranslationUnit tu) {
		logInvalidCallError();
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IBinding[] find(String name) {
		logInvalidCallError();
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve) {
		logInvalidCallError();
		return null;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet acceptLocalBindings) {
		logInvalidCallError();
		return null;
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup) {
		logInvalidCallError();
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,
			IIndexFileSet acceptLocalBindings) {
		logInvalidCallError();
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public IBinding[] getBindings(ScopeLookupData lookup) {
		logInvalidCallError();
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

	private void logInvalidCallError() {
		CCorePlugin.log(new UnsupportedOperationException(
				"Global index scope has to be mapped to the global scope of a particular translation unit.")); //$NON-NLS-1$
	}
}
