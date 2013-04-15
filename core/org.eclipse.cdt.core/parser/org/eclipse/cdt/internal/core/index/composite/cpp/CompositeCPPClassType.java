/*******************************************************************************
 * Copyright (c) 2007, 2012 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getNestedType;

import java.util.Arrays;

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
	public ICPPMethod[] getAllDeclaredMethods() {
		return ClassTypeHelper.getAllDeclaredMethods(this, null);
	}

	private class CPPBaseDelegate implements ICPPBase {
		private final ICPPBase base;
		private IType baseClass;
		private final boolean writable;
		
		CPPBaseDelegate(ICPPBase b) {
			this(b, false); 
		}
		
		CPPBaseDelegate(ICPPBase b, boolean writable) {
			this.base= b;
			this.writable= writable; 
		}
		
		@Override
		public IBinding getBaseClass() {
			IType type= getBaseClassType();
			type = getNestedType(type, TDEF);
			if (type instanceof IBinding)
				return (IBinding) type;
			return null;
		}

		@Override
		public IType getBaseClassType() {
			if (baseClass == null) {
				baseClass= cf.getCompositeType(base.getBaseClassType());
			}
			return baseClass;
		}

		@Override @Deprecated
		public IName getBaseClassSpecifierName() {
			return base.getBaseClassSpecifierName();
		}
		
		@Override
		public IName getClassDefinitionName() {
			return base.getClassDefinitionName();
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
			if (writable && binding instanceof IType) {
				baseClass= (IType) binding;
			} else {
				base.setBaseClass(binding);
			}
		}

		@Override
		public void setBaseClass(IType binding) {
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
		ICPPBase[] bases = ((ICPPClassType) rbinding).getBases();
		return wrapBases(bases);
	}

	@Override
	public ICPPConstructor[] getConstructors() {
		ICPPConstructor[] result = ((ICPPClassType) rbinding).getConstructors();
		return wrapBindings(result);
	}

	@Override
	public ICPPField[] getDeclaredFields() {
		ICPPField[] result = ((ICPPClassType) rbinding).getDeclaredFields();
		return wrapBindings(result);
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() {
		ICPPMethod[] result = ((ICPPClassType) rbinding).getDeclaredMethods();
		return wrapBindings(result);
	}

	@Override
	public IField[] getFields() {
		return ClassTypeHelper.getFields(this, null);
	}

	@Override
	public IBinding[] getFriends() {
		IBinding[] preResult = ((ICPPClassType) rbinding).getFriends();
		return wrapBindings(preResult);
	}

	@Override
	public ICPPMethod[] getMethods() {
		return ClassTypeHelper.getMethods(this, null);
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		ICPPClassType[] result = ((ICPPClassType) rbinding).getNestedClasses();
		return wrapBindings(result);
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

	protected ICPPBase[] wrapBases(final ICPPBase[] bases) {
		ICPPBase[] result = new ICPPBase[bases.length];
		for (int i= 0; i < bases.length; i++) {
			result[i] = new CPPBaseDelegate(bases[i]);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected <T extends IBinding> T[] wrapBindings(T[] bindings) {
		T[] result = Arrays.copyOf(bindings, bindings.length);
		for (int i= 0; i < bindings.length; i++) {
			result[i] = (T) cf.getCompositeBinding((IIndexFragmentBinding) bindings[i]);
		}
		return result;
	}

	@Override
	public boolean isFinal() {
		return ((ICPPClassType) rbinding).isFinal();
	}
}
