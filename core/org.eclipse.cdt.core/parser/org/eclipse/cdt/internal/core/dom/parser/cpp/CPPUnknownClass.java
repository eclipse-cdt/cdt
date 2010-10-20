/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Niefer (IBM Corporation) - initial API and implementation
 *    Sergey Prigogin (Google)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * Represents a C++ class, declaration of which is not yet available.
 */
public class CPPUnknownClass extends CPPUnknownBinding implements ICPPUnknownClassType {
	public static CPPUnknownClass createUnnamedInstance() {
    	return new CPPUnknownClass(null, CharArrayUtils.EMPTY);
    }
    
    public CPPUnknownClass(IBinding binding, char[] name) {
        super(binding, name);
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

    public int getKey(){
        return 0;
    }

    public final IScope getCompositeScope() {
        return asScope();
    }

    public boolean isSameType(IType type) {
    	if (this == type) 
    		return true;
    	
		if (type instanceof ITypedef) 
			return type.isSameType(this);
		
		if (type instanceof ICPPUnknownClassType 
				&& !(type instanceof ICPPUnknownClassInstance)
				&& !(type instanceof ICPPDeferredClassInstance)) {
			ICPPUnknownClassType rhs= (ICPPUnknownClassType) type;
			if (CharArrayUtils.equals(getNameCharArray(), rhs.getNameCharArray())) {
				final IBinding lhsContainer = getOwner();
				final IBinding rhsContainer = rhs.getOwner();
				if (lhsContainer instanceof IType && rhsContainer instanceof IType) {
					return ((IType) lhsContainer).isSameType((IType) rhsContainer);
				}
			}
		}
		return false;
    }

	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}
	
	public boolean isAnonymous() {
		return false;
	}
	
	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
