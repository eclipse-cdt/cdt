/*******************************************************************************
 * Copyright (c) 2013, 2015 Nathan Ridge.
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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;

/**
 * A specialization of a template template parameter. This is needed when a nested template
 * has a template template parameter whose default value is dependent on a template
 * parameter of an enclosing template.
 *
 * This class can represent a specialization of either an AST or a PDOM template parameter.
 */
public class CPPTemplateTemplateParameterSpecialization extends CPPTemplateParameterSpecialization
		implements ICPPTemplateTemplateParameter {
	// The scope of the type named by the parameter specialization.
	// Not to be confused with CPPTemplateParameterSpecialization.fScope,
	// which is the enclosing scope in which the parameter specialization lives.
	private ICPPScope fScope;

	public CPPTemplateTemplateParameterSpecialization(ICPPSpecialization owner, ICPPScope scope,
			ICPPTemplateTemplateParameter specialized, ICPPTemplateArgument defaultValue) {
		super(owner, scope, specialized, defaultValue);
	}

	@Override
	public ICPPTemplateTemplateParameter getSpecializedBinding() {
		return (ICPPTemplateTemplateParameter) super.getSpecializedBinding();
	}

	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		return ICPPClassTemplatePartialSpecialization.EMPTY_ARRAY;
	}

	@Override
	public ICPPTemplateInstance asDeferredInstance() {
		return null;
	}

	@Override
	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	@Override
	public IField[] getFields() {
		return IField.EMPTY_FIELD_ARRAY;
	}

	@Override
	public IField findField(String name) {
		return null;
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	@Override
	public ICPPMethod[] getMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	@Override
	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public ICPPUsingDeclaration[] getUsingDeclarations() {
		return ICPPUsingDeclaration.EMPTY_USING_DECL_ARRAY;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public int getVisibility(IBinding member) {
		return getSpecializedBinding().getVisibility(member);
	}

	@Override
	public int getKey() {
		return 0;
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public IScope getCompositeScope() {
		return null;
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return getSpecializedBinding().getTemplateParameters();
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
		if (!(type instanceof ICPPTemplateTemplateParameter))
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
