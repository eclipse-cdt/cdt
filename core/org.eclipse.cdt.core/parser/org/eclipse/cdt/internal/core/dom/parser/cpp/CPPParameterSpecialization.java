/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Niefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameterMap;

/**
 * Binding for a specialization of a parameter.
 */
public class CPPParameterSpecialization extends CPPSpecialization implements ICPPParameter {
	private final IType fType;
	private final IValue fDefaultValue;

	public CPPParameterSpecialization(ICPPParameter orig, IBinding owner, IType type, IValue defaultValue,
			ICPPTemplateParameterMap tpmap) {
		super(orig, owner, tpmap);
		fType = type;
		fDefaultValue = defaultValue;
	}

	private ICPPParameter getParameter() {
		return (ICPPParameter) getSpecializedBinding();
	}

	@Override
	public IType getType() {
		return fType;
	}

	@Override
	public boolean isParameterPack() {
		return fType instanceof ICPPParameterPackType;
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
	public boolean isAuto() {
		return getParameter().isAuto();
	}

	@Override
	public boolean isRegister() {
		return getParameter().isRegister();
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
	public boolean hasDefaultValue() {
		return fDefaultValue != null;
	}

	@Override
	public IValue getDefaultValue() {
		return fDefaultValue;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public IValue getInitialValue() {
		return null;
	}
}
