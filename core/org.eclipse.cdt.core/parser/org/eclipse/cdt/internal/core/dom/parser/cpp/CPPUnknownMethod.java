/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;

/**
 * Represents a reference to a method (instance), which cannot be resolved because the owner is
 * unknown. A compiler would resolve it during instantiation.
 */
public class CPPUnknownMethod extends CPPUnknownMember implements ICPPMethod {
	private static final ICPPFunctionType FUNCTION_TYPE = new CPPFunctionType(ProblemType.UNKNOWN_FOR_EXPRESSION,
			IType.EMPTY_TYPE_ARRAY, null);

	public CPPUnknownMethod(IType owner, char[] name) {
		super(owner, name);
	}

	@Override
	public IType[] getExceptionSpecification() {
		return null;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isInline() {
		return false;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public IScope getFunctionScope() {
		return asScope();
	}

	@Override
	public ICPPParameter[] getParameters() {
		return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
	}

	@Override
	public ICPPFunctionType getDeclaredType() {
		return FUNCTION_TYPE;
	}

	@Override
	public ICPPFunctionType getType() {
		// TODO(nathanridge): We'd like to return a TypeOfUnknownMember here,
		// but that doesn't implement ICPPFuncionType. We'll probably have
		// to write an implementation of ICPPFunctionType that stores the
		// CPPUnknownMethod and resolves it upon instantiation.
		return FUNCTION_TYPE;
	}

	@Override
	public boolean isAuto() {
		return false;
	}

	@Override
	public boolean isExtern() {
		return false;
	}

	@Override
	public boolean isRegister() {
		return false;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean takesVarArgs() {
		return false;
	}

	@Override
	public boolean isNoReturn() {
		return false;
	}

	@Override
	public int getRequiredArgumentCount() {
		return 0;
	}

	@Override
	public boolean hasParameterPack() {
		return false;
	}

	@Override
	public int getVisibility() {
		return v_public;
	}

	@Override
	public ICPPClassType getClassOwner() {
		IType owner = getOwnerType();
		if (owner instanceof ICPPClassType)
			return (ICPPClassType) owner;
		return null;
	}

	@Override
	public boolean isVirtual() {
		return false;
	}

	@Override
	public boolean isDestructor() {
		return false;
	}

	@Override
	public boolean isImplicit() {
		return false;
	}

	@Override
	public boolean isExplicit() {
		return false;
	}

	@Override
	public boolean isPureVirtual() {
		return false;
	}

	@Override
	public boolean isOverride() {
		return false;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public boolean isConstexpr() {
		return false;
	}
}
