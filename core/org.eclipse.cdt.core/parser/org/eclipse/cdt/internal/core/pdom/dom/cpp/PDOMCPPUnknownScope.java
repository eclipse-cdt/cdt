/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Sergey Prigogin (Google)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;

public class PDOMCPPUnknownScope extends CPPUnknownScope implements IIndexScope {

	public PDOMCPPUnknownScope(PDOMCPPBinding binding, IASTName name) {
		super((ICPPUnknownBinding) binding, name);
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
	public PDOMCPPBinding getScopeBinding() {
		return (PDOMCPPBinding) super.getScopeBinding();
	}
	
	@Override
	// Needs to be thread-safe.
	protected synchronized IBinding getOrCreateBinding(IASTName name, int idx) {
		return super.getOrCreateBinding(name, idx);
	}
}
