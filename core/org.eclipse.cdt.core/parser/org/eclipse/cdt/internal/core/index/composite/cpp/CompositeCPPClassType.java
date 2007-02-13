/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPClassType extends CompositeCPPBinding implements ICPPClassType, IIndexType {
	public CompositeCPPClassType(ICompositesFactory cf, ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	public Object clone() {
		fail(); return null;
	}

	public IField findField(String name) throws DOMException {
		IField preResult = ((ICPPClassType)rbinding).findField(name);
		return (IField) cf.getCompositeBinding(preResult);
	}

	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		ICPPMethod[] preResult = ((ICPPClassType)rbinding).getAllDeclaredMethods();
		ICPPMethod[] result = new ICPPMethod[preResult.length];
		for(int i=0; i<preResult.length; i++) {
			result[i] = (ICPPMethod) cf.getCompositeBinding(preResult[i]);
		}
		return result;
	}

	public ICPPBase[] getBases() throws DOMException {
		final ICPPBase[] preresult = ((ICPPClassType)rbinding).getBases();
		ICPPBase[] result = new ICPPBase[preresult.length];
		for(int i=0; i<preresult.length; i++) {
			final int n = i;
			result[i] = new ICPPBase() {
				public IBinding getBaseClass() throws DOMException {
					return cf.getCompositeBinding(preresult[n].getBaseClass());
				}

				public int getVisibility() throws DOMException {
					return preresult[n].getVisibility();
				}

				public boolean isVirtual() throws DOMException {
					return preresult[n].isVirtual();
				}
				
				public IName getBaseClassSpecifierName() {
					return preresult[n].getBaseClassSpecifierName();
				}
			};
		}
		return result;
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		ICPPConstructor[] preResult = ((ICPPClassType)rbinding).getConstructors();
		ICPPConstructor[] result = new ICPPConstructor[preResult.length];
		for(int i=0; i<preResult.length; i++) {
			result[i] = (ICPPConstructor) cf.getCompositeBinding(preResult[i]);
		}
		return result;
	}

	public ICPPField[] getDeclaredFields() throws DOMException {
		ICPPField[] preResult = ((ICPPClassType)rbinding).getDeclaredFields();
		ICPPField[] result = new ICPPField[preResult.length];
		for(int i=0; i<preResult.length; i++) {
			result[i] = (ICPPField) cf.getCompositeBinding(preResult[i]);
		}
		return result;
	}

	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		ICPPMethod[] preResult = ((ICPPClassType)rbinding).getDeclaredMethods();
		ICPPMethod[] result = new ICPPMethod[preResult.length];
		for(int i=0; i<preResult.length; i++) {
			result[i] = (ICPPMethod) cf.getCompositeBinding(preResult[i]);
		}
		return result;
	}

	public IField[] getFields() throws DOMException {
		IField[] preResult = ((ICPPClassType)rbinding).getFields();
		IField[] result = new IField[preResult.length];
		for(int i=0; i<preResult.length; i++) {
			result[i] = (IField) cf.getCompositeBinding(preResult[i]);
		}
		return result;
	}

	public IBinding[] getFriends() throws DOMException {
		IBinding[] preResult = ((ICPPClassType)rbinding).getFriends();
		IBinding[] result = new IBinding[preResult.length];
		for(int i=0; i<preResult.length; i++) {
			result[i] = cf.getCompositeBinding(preResult[i]);
		}
		return result;
	}

	public ICPPMethod[] getMethods() throws DOMException {
		ICPPMethod[] result = ((ICPPClassType)rbinding).getMethods();
		for(int i=0; i<result.length; i++) {
			result[i] = (ICPPMethod) cf.getCompositeBinding(result[i]);
		}
		return result;
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		ICPPClassType[] preResult = ((ICPPClassType)rbinding).getNestedClasses();
		ICPPClassType[] result = new ICPPClassType[preResult.length];
		for(int i=0; i<preResult.length; i++) {
			result[i] = (ICPPClassType) cf.getCompositeBinding(preResult[i]);
		}
		return result;
	}

	public IScope getCompositeScope() throws DOMException {
		return new CompositeCPPClassScope(cf, rbinding);
	}

	public int getKey() throws DOMException {
		return ((ICPPClassType)rbinding).getKey();
	}

	public boolean isSameType(IType type) {
		return ((ICPPClassType)rbinding).isSameType(type);
	}
}
