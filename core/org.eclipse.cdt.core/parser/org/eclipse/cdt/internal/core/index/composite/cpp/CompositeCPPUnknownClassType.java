/*******************************************************************************
 * Copyright (c) 2008, 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	  Sergey Prigogin (Google) - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassType;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;


class CompositeCPPUnknownClassType extends CompositeCPPUnknownBinding implements ICPPUnknownClassType, IIndexType {
	private ICPPScope unknownScope;

	public CompositeCPPUnknownClassType(ICompositesFactory cf, ICPPUnknownClassType rbinding) {
		super(cf, rbinding);
	}

	@Override
	public IField findField(String name) {
		IField preResult = ((ICPPClassType) rbinding).findField(name);
		return (IField) cf.getCompositeBinding((IIndexFragmentBinding)preResult);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	@Override
	public ICPPBase[] getBases() {
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	@Override
	public IField[] getFields() {
		return IField.EMPTY_FIELD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	@Override
	public ICPPField[] getDeclaredFields() {
		return ICPPField.EMPTY_CPPFIELD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	@Override
	public ICPPMethod[] getMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	@Override
	public ICPPMethod[] getAllDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	@Override
	public ICPPMethod[] getDeclaredMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
	 */
	@Override
	public ICPPConstructor[] getConstructors() {
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
	 */
	@Override
	public IBinding[] getFriends() {
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	@Override
	public ICPPClassType[] getNestedClasses() {
		ICPPClassType[] result = ((ICPPClassType) rbinding).getNestedClasses();
		for (int i = 0; i < result.length; i++) {
			result[i] = (ICPPClassType) cf.getCompositeBinding((IIndexFragmentBinding) result[i]);
		}
		return result;
	}

	@Override
	public IScope getCompositeScope() {
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
	public ICPPScope asScope() {
    	if (unknownScope == null) {
    		unknownScope= new CompositeCPPUnknownScope(this, getUnknownName());
    	}
    	return unknownScope;
    }

	@Override
	public boolean isAnonymous() {
		return false;
	}
}
