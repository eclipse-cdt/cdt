/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.ObjectMap;

/**
 * @author aniefer
 */
public class CPPDeferredClassInstance extends CPPInstance implements
		ICPPClassType, ICPPDeferredTemplateInstance, ICPPInternalDeferredClassInstance {
	
	public CPPDeferredClassInstance(ICPPClassTemplate orig,	IType[] arguments) {
		super(null, orig, null, arguments);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase[] getBases() throws DOMException {
		return ((ICPPClassType) getClassTemplate()).getBases();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(java.lang.String)
	 */
	public IField findField(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	public ICPPField[] getDeclaredFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public ICPPMethod[] getMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public ICPPMethod[] getAllDeclaredMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public ICPPMethod[] getDeclaredMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
	 */
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
	 */
	public IBinding[] getFriends() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() throws DOMException {
		return ((ICPPClassType) getClassTemplate()).getKey();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() throws DOMException {
		return ((ICPPClassType) getClassTemplate()).getCompositeScope();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	 @Override
	public Object clone() {
        return this;
    }

	/**
	 * @param argMap
	 * @return
	 */
	public IType instantiate(ObjectMap argMap) {
		IType[] arguments = getArguments();
		IType[] newArgs = new IType[arguments.length];
		int size = arguments.length;
		for (int i = 0; i < size; i++) {
			newArgs[i] = CPPTemplates.instantiateType(arguments[i], argMap);
		}
		
		ICPPClassTemplate classTemplate = getClassTemplate();
		if (argMap.containsKey(classTemplate)) {
			classTemplate = (ICPPClassTemplate) argMap.get(classTemplate);
		}
		
		return (IType) ((ICPPInternalTemplateInstantiator)classTemplate).instantiate(newArgs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
	 */
	public boolean isSameType(IType type) {
		if (type == this)
			return true;

		// allow some fuzziness here.
		ICPPClassTemplate classTemplate = getClassTemplate();
		if (type instanceof ICPPDeferredTemplateInstance && type instanceof ICPPClassType) {
			ICPPClassTemplate typeClass =
				(ICPPClassTemplate) ((ICPPDeferredTemplateInstance) type).getSpecializedBinding();
			return typeClass == classTemplate;
		} else if (type instanceof ICPPClassTemplate && classTemplate == type) {
			return true;
		} else if (type instanceof ICPPTemplateInstance &&
				((ICPPTemplateInstance)type).getTemplateDefinition() == classTemplate) {
			return true;
		}
		return false;
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	private ICPPClassTemplate getClassTemplate() {
		return (ICPPClassTemplate) getSpecializedBinding();
	}
}
