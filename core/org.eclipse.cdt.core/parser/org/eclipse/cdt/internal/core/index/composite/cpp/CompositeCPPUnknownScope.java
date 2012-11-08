/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownTypeScope;
import org.eclipse.cdt.internal.core.index.IIndexScope;

public class CompositeCPPUnknownScope extends CPPUnknownTypeScope implements IIndexScope {

	public CompositeCPPUnknownScope(IIndexBinding binding, IASTName name) {
		super((IType) binding, name);
	}

	@Override
	public IIndexName getScopeName() {
		return null;
	}
	
	@Override
	public IIndexScope getParent() {
		try {
			return (IIndexScope) super.getParent();
		} catch (DOMException e) {
			return null;
		}
	}
	
	@Override
	public IIndexBinding getScopeBinding() {
		return (IIndexBinding) super.getScopeType();
	}
}
