/*******************************************************************************
 * Copyright (c) 2009, 2014 Wind River Systems, Inc. and others.
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

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.core.runtime.PlatformObject;

public class CPPBuiltinParameter extends PlatformObject implements ICPPParameter {
	public static ICPPParameter[] createParameterList(ICPPFunctionType ft) {
		if (ft == null) {
			return ICPPParameter.EMPTY_CPPPARAMETER_ARRAY;
		}
		IType[] ptypes = ft.getParameterTypes();
		ICPPParameter[] result = new ICPPParameter[ptypes.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = new CPPBuiltinParameter(ptypes[i]);
		}
		return result;
	}

	private IType type;

	public CPPBuiltinParameter(IType type) {
		this.type = type;
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean isExtern() {
		return false;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isAuto() {
		return false;
	}

	@Override
	public boolean isRegister() {
		return false;
	}

	@Override
	public String getName() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public char[] getNameCharArray() {
		return CharArrayUtils.EMPTY;
	}

	@Override
	public IScope getScope() {
		return null;
	}

	@Override
	public boolean hasDefaultValue() {
		return false;
	}

	@Override
	public IValue getDefaultValue() {
		return null;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public boolean isConstexpr() {
		return false;
	}

	@Override
	public String[] getQualifiedName() {
		return new String[0];
	}

	@Override
	public char[][] getQualifiedNameCharArray() {
		return new char[0][];
	}

	@Override
	public boolean isGloballyQualified() {
		return false;
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		return null;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}

	@Override
	public boolean isParameterPack() {
		return false;
	}
}