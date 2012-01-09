/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
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

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCPPClassType extends CompositeCPPBinding implements ICPPClassType, IIndexType {
	public CompositeCPPClassType(ICompositesFactory cf, ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	@Override
	public Object clone() {
		fail(); return null;
	}

	@Override
	public final IField findField(String name) {
		return ClassTypeHelper.findField(this, name);
	}

	@Override
	public final ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this);
	}

	private class CPPBaseDelegate implements ICPPBase {
		private ICPPBase base;
		private IBinding baseClass;
		private boolean writable;
		
		CPPBaseDelegate(ICPPBase b) {
			this(b, false); 
		}
		
		CPPBaseDelegate(ICPPBase b, boolean writable) {
			this.base= b;
			this.writable= writable; 
		}
		
		@Override
		public IBinding getBaseClass() {
			if (baseClass != null) {
				return baseClass;
			} else {
				return cf.getCompositeBinding((IIndexFragmentBinding)base.getBaseClass());
			}
		}

		@Override
		public IName getBaseClassSpecifierName() {
			return base.getBaseClassSpecifierName();
		}

		@Override
		public int getVisibility() {
			return base.getVisibility();
		}

		@Override
		public boolean isVirtual() {
			return base.isVirtual();
		}

		@Override
		public void setBaseClass(IBinding binding) {
			if (writable) {
				baseClass= binding;
			} else {
				base.setBaseClass(binding);
			}
		}
		
	    @Override
		public ICPPBase clone(){
	    	return new CPPBaseDelegate(base, true);
	    }
	}
	
	@Override
	public ICPPBase[] getBases() {
		final ICPPBase[] preresult = ((ICPPClassType) rbinding).getBases();
		ICPPBase[] result = new ICPPBase[preresult.length];
		for (int i= 0; i < preresult.length; i++) {
			result[i] = new CPPBaseDelegate(preresult[i]);
		}
		return result;
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		ICPPConstructor[] result = ((ICPPClassType) rbinding).getConstructors();
		for (int i= 0; i < result.length; i++) {
			result[i] = (ICPPConstructor) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		ICPPField[] result = ((ICPPClassType) rbinding).getDeclaredFields();
		for (int i= 0; i < result.length; i++) {
			result[i] = (ICPPField) cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
		}
		return result;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		ICPPMethod[] result = ((ICPPClassType) rbinding).getDeclaredMethods();
		for (int i= 0; i < result.length; i++) {
			result[i]= (ICPPMethod) cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
		}
		return result;
	}

	@Override
	public final IField[] getFields() {
		return ClassTypeHelper.getFields(this);
	}

	@Override
	public IBinding[] getFriends() {
		IBinding[] preResult = ((ICPPClassType) rbinding).getFriends();
		IBinding[] result = new IBinding[preResult.length];
		for (int i= 0; i < preResult.length; i++) {
			result[i] = cf.getCompositeBinding((IIndexFragmentBinding) preResult[i]);
		}
		return result;
	}

	@Override
	public final ICPPMethod[] getMethods() {
		return ClassTypeHelper.getMethods(this);
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		ICPPClassType[] result = ((ICPPClassType) rbinding).getNestedClasses();
		for (int i= 0; i < result.length; i++) {
			result[i] = (ICPPClassType) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	@Override
	public ICPPScope getCompositeScope() {
		return new CompositeCPPClassScope(cf, rbinding);
	}

	@Override
	public int getKey() {
		return ((ICPPClassType) rbinding).getKey();
	}

	@Override
	public boolean isSameType(IType type) {
		return ((ICPPClassType) rbinding).isSameType(type);
	}

	@Override
	public boolean isAnonymous() {
		return ((ICPPClassType) rbinding).isAnonymous();
	}
}
