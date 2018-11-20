/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCTypedef extends CompositeCBinding implements ITypedef, IIndexType, ITypeContainer {
	public CompositeCTypedef(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	@Override
	public IType getType() {
		IType type = ((ITypedef) rbinding).getType();
		return cf.getCompositeType(type);
	}

	@Override
	public boolean isSameType(IType type) {
		return ((ITypedef) rbinding).isSameType(type);
	}

	@Override
	public Object clone() {
		fail();
		return null;
	}

	@Override
	public void setType(IType type) {
		fail();
	}
}
