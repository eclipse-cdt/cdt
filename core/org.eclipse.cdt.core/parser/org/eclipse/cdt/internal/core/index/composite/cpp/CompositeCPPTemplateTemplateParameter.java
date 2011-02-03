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

	public IType getDefault() throws DOMException {
		IType preresult= ((ICPPTemplateTemplateParameter)rbinding).getDefault();
		return cf.getCompositeType(preresult);
	}

	public short getParameterPosition() {
		return ((ICPPTemplateParameter)rbinding).getParameterPosition();
	}

	public short getTemplateNestingLevel() {
		return ((ICPPTemplateParameter)rbinding).getTemplateNestingLevel();
	}
	
	public int getParameterID() {
		return ((ICPPTemplateParameter)rbinding).getParameterID();
	}

	public boolean isParameterPack() {
		return ((ICPPTemplateParameter)rbinding).isParameterPack();
	}

	public boolean isSameType(IType type) {
		return ((IType)rbinding).isSameType(type);
	}
	
	@Override
	public Object clone() {
		fail(); return null; 
	}

	public ICPPScope asScope() {
		if (unknownScope == null) {
			unknownScope= new CompositeCPPUnknownScope(this, getUnknownName());
		}
		return unknownScope;
	}

	public IASTName getUnknownName() {
		return new CPPASTName(getNameCharArray());
	}
	
	public ICPPTemplateArgument getDefaultValue() {
		try {
			return TemplateInstanceUtil.convert(cf, ((ICPPTemplateTemplateParameter)rbinding).getDefaultValue());
		} catch (DOMException e) {
			return null;
		}
	}

	public ICPPTemplateParameter[] getTemplateParameters() {
		return TemplateInstanceUtil.convert(cf, ((ICPPTemplateTemplateParameter)rbinding).getTemplateParameters());
	}

	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	public IField findField(String name) {
		return null;
	}

	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public IField[] getFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	public ICPPMethod[] getMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	public IScope getCompositeScope() {
		return asScope();
	}

	public int getKey() {
		return 0;
	}

	public boolean isAnonymous() {
		return false;
	}
	
	public ICPPDeferredClassInstance asDeferredInstance() {
		return null;
	}
}
