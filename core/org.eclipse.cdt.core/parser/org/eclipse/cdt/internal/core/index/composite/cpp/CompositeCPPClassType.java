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
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBase;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
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
		return (IField) cf.getCompositeBinding((IIndexFragmentBinding)preResult);
	}

	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		ICPPMethod[] result = ((ICPPClassType)rbinding).getAllDeclaredMethods();
		for(int i=0; i<result.length; i++) {
			result[i] = (ICPPMethod) cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
		}
		return result;
	}

	private class CPPBaseDelegate implements ICPPBase, ICPPInternalBase {
		private ICPPBase base;
		
		CPPBaseDelegate(ICPPBase b) {
			this.base = b;
		}
		
		public IBinding getBaseClass() throws DOMException {
			return cf.getCompositeBinding((IIndexFragmentBinding)base.getBaseClass());
		}

		public IName getBaseClassSpecifierName() {
			return base.getBaseClassSpecifierName();
		}

		public int getVisibility() throws DOMException {
			return base.getVisibility();
		}

		public boolean isVirtual() throws DOMException {
			return base.isVirtual();
		}

		public void setBaseClass(IBinding binding) throws DOMException {
			((ICPPInternalBase)base).setBaseClass(binding);
		}
		
	    public Object clone(){
	    	return ((ICPPInternalBase)base).clone();
	    }
	}
	
	public ICPPBase[] getBases() throws DOMException {
		final ICPPBase[] preresult = ((ICPPClassType)rbinding).getBases();
		ICPPBase[] result = new ICPPBase[preresult.length];
		for(int i=0; i<preresult.length; i++) {
			result[i] = new CPPBaseDelegate(preresult[i]);
		}
		return result;
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		ICPPConstructor[] result = ((ICPPClassType)rbinding).getConstructors();
		for(int i=0; i<result.length; i++) {
			result[i] = (ICPPConstructor) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	public ICPPField[] getDeclaredFields() throws DOMException {
		ICPPField[] result = ((ICPPClassType)rbinding).getDeclaredFields();
		for(int i=0; i<result.length; i++) {
			result[i] = (ICPPField) cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
		}
		return result;
	}

	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		ICPPMethod[] result = ((ICPPClassType)rbinding).getDeclaredMethods();
		for(int i=0; i<result.length; i++) {
			result[i]= (ICPPMethod) cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
		}
		return result;
	}

	public IField[] getFields() throws DOMException {
		IField[] result = ((ICPPClassType)rbinding).getFields();
		for(int i=0; i<result.length; i++) {
			result[i]= (IField) cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
		}
		return result;
	}

	public IBinding[] getFriends() throws DOMException {
		IBinding[] preResult = ((ICPPClassType)rbinding).getFriends();
		IBinding[] result = new IBinding[preResult.length];
		for(int i=0; i<preResult.length; i++) {
			result[i] = cf.getCompositeBinding((IIndexFragmentBinding) preResult[i]);
		}
		return result;
	}

	public ICPPMethod[] getMethods() throws DOMException {
		ICPPMethod[] result = ((ICPPClassType)rbinding).getMethods();
		for(int i=0; i<result.length; i++) {
			result[i] = (ICPPMethod) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		ICPPClassType[] result = ((ICPPClassType)rbinding).getNestedClasses();
		for(int i=0; i<result.length; i++) {
			result[i] = (ICPPClassType) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
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
