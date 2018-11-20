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
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCVariable extends CompositeCBinding implements IVariable {

	public CompositeCVariable(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	@Override
	public IType getType() {
		IType rtype = ((IVariable) rbinding).getType();
		return cf.getCompositeType(rtype);
	}

	@Override
	public boolean isAuto() {
		return ((IVariable) rbinding).isAuto();
	}

	@Override
	public boolean isExtern() {
		return ((IVariable) rbinding).isExtern();
	}

	@Override
	public boolean isRegister() {
		return ((IVariable) rbinding).isRegister();
	}

	@Override
	public boolean isStatic() {
		return ((IVariable) rbinding).isStatic();
	}

	@Override
	public IValue getInitialValue() {
		return ((IVariable) rbinding).getInitialValue();
	}
}
