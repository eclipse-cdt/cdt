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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCFunction extends CompositeCBinding implements IFunction {

	public CompositeCFunction(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	@Override
	public IScope getFunctionScope() {
		return null;
	}

	@Override
	public IParameter[] getParameters() {
		IParameter[] preResult = ((IFunction) rbinding).getParameters();
		IParameter[] result = new IParameter[preResult.length];
		for (int i = 0; i < preResult.length; i++) {
			result[i] = (IParameter) cf.getCompositeBinding((IIndexFragmentBinding) preResult[i]);
		}
		return result;
	}

	@Override
	public IFunctionType getType() {
		IType rtype = ((IFunction) rbinding).getType();
		return (IFunctionType) cf.getCompositeType(rtype);
	}

	@Override
	public boolean isAuto() {
		return ((IFunction) rbinding).isAuto();
	}

	@Override
	public boolean isExtern() {
		return ((IFunction) rbinding).isExtern();
	}

	@Override
	public boolean isInline() {
		return ((IFunction) rbinding).isInline();
	}

	@Override
	public boolean isRegister() {
		return ((IFunction) rbinding).isRegister();
	}

	@Override
	public boolean isStatic() {
		return ((IFunction) rbinding).isStatic();
	}

	@Override
	public boolean takesVarArgs() {
		return ((IFunction) rbinding).takesVarArgs();
	}

	@Override
	public boolean isNoReturn() {
		return ((IFunction) rbinding).isNoReturn();
	}
}
