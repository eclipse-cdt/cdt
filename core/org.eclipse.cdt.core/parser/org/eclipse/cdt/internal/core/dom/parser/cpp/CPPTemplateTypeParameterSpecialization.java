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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;

/**
 * A specialization of a type template parameter. This is needed when a nested template
 * has a type template parameter whose default value is dependent on a template
 * parameter of an enclosing template.
 *
 * This class can represent a specialization of either an AST or a PDOM template parameter.
 */
public class CPPTemplateTypeParameterSpecialization extends CPPTemplateParameterSpecialization
		implements ICPPTemplateTypeParameter {
	// The scope of the type named by the parameter specialization.
	// Not to be confused with CPPTemplateParameterSpecialization.fScope,
	// which is the enclosing scope in which the parameter specialization lives.
	private ICPPScope fScope;

	public CPPTemplateTypeParameterSpecialization(ICPPSpecialization owner, ICPPScope scope,
			ICPPTemplateTypeParameter specialized, ICPPTemplateArgument defaultValue) {
		super(owner, scope, specialized, defaultValue);
	}

	@Override
	public ICPPTemplateTypeParameter getSpecializedBinding() {
		return (ICPPTemplateTypeParameter) super.getSpecializedBinding();
	}

	@Override
	public IType getDefault() throws DOMException {
		return getDefaultValue().getTypeValue();
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef)
			return type.isSameType(this);
		if (!(type instanceof ICPPTemplateTypeParameter))
			return false;

		return getParameterID() == ((ICPPTemplateParameter) type).getParameterID();
	}

	@Override
	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException e) {
			//not going to happen
		}
		return o;
	}

	@Override
	public ICPPScope asScope() throws DOMException {
		if (fScope == null) {
			return new CPPUnknownTypeScope(this, new CPPASTName(getNameCharArray()));
		}
		return fScope;
	}
}
