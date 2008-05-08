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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;

public class CompositeCPPUnknownScope extends CPPUnknownScope implements IIndexScope {
	private CompositeCPPBinding fBinding;

	public CompositeCPPUnknownScope(CompositeCPPBinding binding, IASTName name) {
		super((ICPPUnknownBinding) binding, name);
		fBinding= binding;
	}

	@Override
	public IIndexName getScopeName() {
		return null;
	}
	
	@Override
	public IIndexScope getParent() {
		return fBinding.getScope();
	}
	
	@Override
	public CompositeCPPBinding getScopeBinding() {
		return fBinding;
	}
}
