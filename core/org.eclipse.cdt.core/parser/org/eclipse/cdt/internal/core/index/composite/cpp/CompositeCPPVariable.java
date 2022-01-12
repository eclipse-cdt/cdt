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
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPVariable extends CompositeCPPBinding implements ICPPVariable {

	public CompositeCPPVariable(ICompositesFactory cf, IVariable delegate) {
		super(cf, delegate);
	}

	@Override
	public boolean isMutable() {
		return ((ICPPVariable) rbinding).isMutable();
	}

	@Override
	public boolean isExternC() {
		return ((ICPPVariable) rbinding).isExternC();
	}

	@Override
	public IType getType() {
		IType rtype = ((ICPPVariable) rbinding).getType();
		return cf.getCompositeType(rtype);
	}

	@Override
	public boolean isAuto() {
		return ((ICPPVariable) rbinding).isAuto();
	}

	@Override
	public boolean isExtern() {
		return ((ICPPVariable) rbinding).isExtern();
	}

	@Override
	public boolean isRegister() {
		return ((ICPPVariable) rbinding).isRegister();
	}

	@Override
	public boolean isStatic() {
		return ((ICPPVariable) rbinding).isStatic();
	}

	@Override
	public boolean isConstexpr() {
		return ((ICPPVariable) rbinding).isConstexpr();
	}

	@Override
	public IValue getInitialValue() {
		return ((ICPPVariable) rbinding).getInitialValue();
	}
}
