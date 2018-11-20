/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;

public class CPPVariableSpecialization extends CPPSpecialization implements ICPPVariable {
	private final IType type;
	private final IValue value;

	public CPPVariableSpecialization(IBinding orig, IBinding owner, ICPPTemplateParameterMap tpmap, IType type,
			IValue value) {
		super(orig, owner, tpmap);
		this.type = type;
		this.value = value;
	}

	private ICPPVariable getVariable() {
		return (ICPPVariable) getSpecializedBinding();
	}

	@Override
	public IType getType() {
		return type;
	}

	@Override
	public IValue getInitialValue() {
		return value;
	}

	@Override
	public boolean isStatic() {
		return getVariable().isStatic();
	}

	@Override
	public boolean isExtern() {
		return getVariable().isExtern();
	}

	@Override
	public boolean isAuto() {
		return getVariable().isAuto();
	}

	@Override
	public boolean isRegister() {
		return getVariable().isRegister();
	}

	@Override
	public boolean isMutable() {
		return getVariable().isMutable();
	}

	@Override
	public boolean isConstexpr() {
		return getVariable().isConstexpr();
	}

	@Override
	public boolean isExternC() {
		return false;
	}
}
