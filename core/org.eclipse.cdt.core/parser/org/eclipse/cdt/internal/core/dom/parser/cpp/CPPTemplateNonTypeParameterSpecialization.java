/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;

/**
 * A specialization of a non-type template parameter. This is needed when a nested template
 * has a non-type template parameter whose type or default value is dependent on a template
 * parameter of an enclosing template.
 *
 * This class can represent a specialization of either an AST or a PDOM template parameter.
 */
public class CPPTemplateNonTypeParameterSpecialization extends CPPTemplateParameterSpecialization
		implements ICPPTemplateNonTypeParameter {
	private final IType fType;

	public CPPTemplateNonTypeParameterSpecialization(ICPPSpecialization owner, ICPPScope scope,
			ICPPTemplateNonTypeParameter specialized, ICPPTemplateArgument defaultValue, IType type) {
		super(owner, scope, specialized, defaultValue);

		this.fType = type;
	}

	@Override
	public ICPPTemplateNonTypeParameter getSpecializedBinding() {
		return (ICPPTemplateNonTypeParameter) super.getSpecializedBinding();
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
	public boolean isExternC() {
		return false;
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
	public boolean isRegister() {
		return false;
	}

	@Override
	public boolean isAuto() {
		return false;
	}

	@Override
	public IType getType() {
		return fType;
	}

	@Override
	public IValue getInitialValue() {
		return getDefaultValue().getNonTypeValue();
	}

	@Override
	@Deprecated
	public IASTExpression getDefault() {
		return null;
	}

	@Override
	public ICPPScope asScope() throws DOMException {
		// A non-type template parameter can never appear on the left hand side
		// of a scope resolution operator.
		return null;
	}
}
