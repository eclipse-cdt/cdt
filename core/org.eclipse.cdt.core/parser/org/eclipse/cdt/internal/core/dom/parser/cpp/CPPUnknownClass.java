/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Sergey Prigogin (Google)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * Represents a C++ class, declaration of which is not yet available.
 *
 * @author aniefer
 */
public class CPPUnknownClass extends CPPUnknownBinding implements ICPPUnknownClassType {

    public CPPUnknownClass(ICPPUnknownBinding scopeBinding, IASTName name) {
        super(scopeBinding, name);
    }

    protected CPPUnknownClass(ICPPClassTemplate template) {
        super(template);
    }

    public ICPPBase[] getBases() {
        return ICPPBase.EMPTY_BASE_ARRAY;
    }

    public IField[] getFields() {
        return IField.EMPTY_FIELD_ARRAY;
    }

    public IField findField(String name) {
        return null;
    }

    public ICPPField[] getDeclaredFields() {
        return ICPPField.EMPTY_CPPFIELD_ARRAY;
    }

    public ICPPMethod[] getMethods() {
        return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
    }

    public ICPPMethod[] getAllDeclaredMethods() {
        return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
    }

    public ICPPMethod[] getDeclaredMethods() {
        return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
    }

    public ICPPConstructor[] getConstructors() {
        return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
    }

    public IBinding[] getFriends() {
        return IBinding.EMPTY_BINDING_ARRAY;
    }

    public int getKey() throws DOMException{
        return 0;
    }

    public final IScope getCompositeScope() throws DOMException {
        return getUnknownScope();
    }

    public boolean isSameType(IType type) {
        return type == this;
    }

	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	public IBinding resolvePartially(ICPPUnknownBinding parentBinding, ObjectMap argMap, ICPPScope instantiationScope) {
		if (parentBinding == this.unknownContainerBinding) {
			return this;
		}
		return new CPPUnknownClass(parentBinding, name);
	}
}
