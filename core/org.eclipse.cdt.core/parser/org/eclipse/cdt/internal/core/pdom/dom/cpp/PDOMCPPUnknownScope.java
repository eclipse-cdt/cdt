/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownTypeScope;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;

public class PDOMCPPUnknownScope extends CPPUnknownTypeScope implements IIndexScope {

	public PDOMCPPUnknownScope(IIndexFragmentBinding binding, IASTName name) {
		super((IType) binding, name);
	}
		
	@Override
	public IIndexName getScopeName() {
		return null;
	}
	
	@Override
	public IIndexScope getParent() {
		return getScopeBinding().getScope();
	}
	
	@Override
	public IIndexFragmentBinding getScopeBinding() {
		return (IIndexFragmentBinding) super.getScopeType();
	}
	
	@Override
	// Needs to be thread-safe.
	protected synchronized IBinding getOrCreateBinding(char[] name, int idx) {
		return super.getOrCreateBinding(name, idx);
	}
}
