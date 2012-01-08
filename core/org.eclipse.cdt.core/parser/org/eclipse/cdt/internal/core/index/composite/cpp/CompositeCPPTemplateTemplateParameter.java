/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPTemplateTemplateParameter extends CompositeCPPBinding 
	implements ICPPTemplateTemplateParameter, ICPPUnknownBinding, ICPPUnknownType, IIndexType {

	private ICPPScope unknownScope;

	public CompositeCPPTemplateTemplateParameter(ICompositesFactory cf,	ICPPTemplateTemplateParameter binding) {
		super(cf, binding);
	}

	@Override
	public IType getDefault() throws DOMException {
		IType preresult= ((ICPPTemplateTemplateParameter)rbinding).getDefault();
		return cf.getCompositeType(preresult);
	}

	@Override
	public short getParameterPosition() {
		return ((ICPPTemplateParameter)rbinding).getParameterPosition();
	}

	@Override
	public short getTemplateNestingLevel() {
		return ((ICPPTemplateParameter)rbinding).getTemplateNestingLevel();
	}
	
	@Override
	public int getParameterID() {
		return ((ICPPTemplateParameter)rbinding).getParameterID();
	}

	@Override
	public boolean isParameterPack() {
		return ((ICPPTemplateParameter)rbinding).isParameterPack();
	}

	@Override
	public boolean isSameType(IType type) {
		return ((IType)rbinding).isSameType(type);
	}
	
	@Override
	public Object clone() {
		fail(); return null; 
	}

	@Override
	public ICPPScope asScope() {
		if (unknownScope == null) {
			unknownScope= new CompositeCPPUnknownScope(this, getUnknownName());
		}
		return unknownScope;
	}

	@Override
	public IASTName getUnknownName() {
		return new CPPASTName(getNameCharArray());
	}
	
	@Override
	public ICPPTemplateArgument getDefaultValue() {
		try {
			return TemplateInstanceUtil.convert(cf, ((ICPPTemplateTemplateParameter)rbinding).getDefaultValue());
		} catch (DOMException e) {
			return null;
		}
	}

	@Override
	public ICPPTemplateParameter[] getTemplateParameters() {
		return TemplateInstanceUtil.convert(cf, ((ICPPTemplateTemplateParameter)rbinding).getTemplateParameters());
	}

	@Override
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	@Override
	public IField findField(String name) {
		return null;
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public IField[] getFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	@Override
	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public ICPPMethod[] getMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public IScope getCompositeScope() {
		return asScope();
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
	public ICPPDeferredClassInstance asDeferredInstance() {
		return null;
	}
}
