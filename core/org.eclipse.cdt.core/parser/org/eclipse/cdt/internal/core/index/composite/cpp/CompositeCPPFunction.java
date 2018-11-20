/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Systems and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPComputableFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPExecution;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPFunction extends CompositeCPPBinding implements ICPPFunction, ICPPComputableFunction {

	public CompositeCPPFunction(ICompositesFactory cf, ICPPFunction rbinding) {
		super(cf, rbinding);
	}

	@Override
	public boolean isExternC() {
		return ((ICPPFunction) rbinding).isExternC();
	}

	@Override
	public boolean isInline() {
		return ((ICPPFunction) rbinding).isInline();
	}

	@Override
	public boolean isMutable() {
		return ((ICPPFunction) rbinding).isMutable();
	}

	@Override
	public boolean isConstexpr() {
		return ((ICPPFunction) rbinding).isConstexpr();
	}

	@Override
	public IScope getFunctionScope() {
		return null;
	}

	@Override
	public ICPPParameter[] getParameters() {
		ICPPParameter[] result = ((ICPPFunction) rbinding).getParameters();
		for (int i = 0; i < result.length; i++) {
			result[i] = (ICPPParameter) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	@Override
	public ICPPFunctionType getType() {
		IType rtype = ((ICPPFunction) rbinding).getType();
		return (ICPPFunctionType) cf.getCompositeType(rtype);
	}

	@Override
	public ICPPFunctionType getDeclaredType() {
		IType rtype = ((ICPPFunction) rbinding).getDeclaredType();
		return (ICPPFunctionType) cf.getCompositeType(rtype);
	}

	@Override
	public boolean isDeleted() {
		return ((ICPPFunction) rbinding).isDeleted();
	}

	@Override
	public boolean isAuto() {
		return ((ICPPFunction) rbinding).isAuto();
	}

	@Override
	public boolean isExtern() {
		return ((ICPPFunction) rbinding).isExtern();
	}

	@Override
	public boolean isRegister() {
		return ((ICPPFunction) rbinding).isRegister();
	}

	@Override
	public boolean isStatic() {
		return ((ICPPFunction) rbinding).isStatic();
	}

	@Override
	public boolean takesVarArgs() {
		return ((ICPPFunction) rbinding).takesVarArgs();
	}

	@Override
	public boolean isNoReturn() {
		return ((ICPPFunction) rbinding).isNoReturn();
	}

	@Override
	public int getRequiredArgumentCount() {
		return ((ICPPFunction) rbinding).getRequiredArgumentCount();
	}

	@Override
	public boolean hasParameterPack() {
		return ((ICPPFunction) rbinding).hasParameterPack();
	}

	@Override
	public Object clone() {
		fail();
		return null;
	}

	@Override
	public String toString() {
		return getName() + " " + ASTTypeUtil.getParameterTypeString(getType()); //$NON-NLS-1$
	}

	@Override
	public IType[] getExceptionSpecification() {
		IType[] es = ((ICPPFunction) rbinding).getExceptionSpecification();
		if (es == null || es.length == 0)
			return es;

		IType[] result = new IType[es.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = cf.getCompositeType(es[i]);
		}
		return result;
	}

	@Override
	public ICPPExecution getFunctionBodyExecution() {
		return CPPFunction.getFunctionBodyExecution((ICPPFunction) rbinding);
	}
}
