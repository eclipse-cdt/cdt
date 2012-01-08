/*******************************************************************************
 * Copyright (c) 2008, 2011 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergey Prigogin (Google) - initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Sergey Prigogin
 */
class PDOMCPPUnknownClassType extends PDOMCPPUnknownBinding implements ICPPClassScope, ICPPUnknownClassType,
		IPDOMMemberOwner, IIndexType, IIndexScope {

	private static final int KEY = PDOMCPPBinding.RECORD_SIZE + 0; // byte
	private static final int MEMBERLIST = PDOMCPPBinding.RECORD_SIZE + 4;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPUnknownBinding.RECORD_SIZE + 8;
	
	private PDOMCPPUnknownScope unknownScope; // No need for volatile, PDOMCPPUnknownScope protects its fields

	public PDOMCPPUnknownClassType(PDOMLinkage linkage, PDOMNode parent, ICPPUnknownClassType classType) throws CoreException {
		super(linkage, parent, classType);

		setKind(classType);
		// linked list is initialized by storage being zero'd by malloc
	}

	public PDOMCPPUnknownClassType(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPClassType) {
			ICPPClassType ct= (ICPPClassType) newBinding;
			setKind(ct);
			super.update(linkage, newBinding);
		}
	}

	private void setKind(ICPPClassType ct) throws CoreException {
		getDB().putByte(record + KEY, (byte) ct.getKey());
	}
	
	@Override
	public void addChild(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.addMember(member);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_UNKNOWN_CLASS_TYPE;
	}
	
	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.accept(visitor);
	}

	@Override
	public ICPPMethod[] getImplicitMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	@Override
	public IScope getCompositeScope() {
		return this;
	}

    @Override
	public ICPPScope asScope() {
    	if (unknownScope == null) {
    		unknownScope= new PDOMCPPUnknownScope(this, getUnknownName());
    	}
    	return unknownScope;
    }
    
	@Override
	public IIndexBinding getScopeBinding() {
		return this;
	}

	@Override
	public ICPPClassType getClassType() {
		return this;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		return null;
	}
	
	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return IBinding.EMPTY_BINDING_ARRAY;
	}
	
	@Override
	public IBinding[] find(String name) {
		return CPPSemantics.findBindings(this, name, false);
	}
	
	// Not implemented

	@Override
	public Object clone() { 
		throw new UnsupportedOperationException(); 
	}
	
	@Override
	public IField findField(String name) {
		return null; 
	}

	@Override
	public boolean mayHaveChildren() {
		return true;
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
     */
    @Override
	public int getKey() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    @Override
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			// Different PDOM bindings may result in equal types if a parent
			// turns out to be a template parameter.
			if (node.getPDOM() == getPDOM() && node.getRecord() == getRecord()) {
				return true;
			}
		}
		
		if (type instanceof ICPPUnknownClassType 
				&& type instanceof ICPPUnknownClassInstance == false
				&& type instanceof ICPPDeferredClassInstance == false) {
			ICPPUnknownClassType rhs= (ICPPUnknownClassType) type;
			if (CharArrayUtils.equals(getNameCharArray(), rhs.getNameCharArray())) {
				final IBinding lhsContainer = getOwner();
				final IBinding rhsContainer = rhs.getOwner();
				if (lhsContainer instanceof IType && rhsContainer instanceof IType) {
					return ((IType)lhsContainer).isSameType((IType) rhsContainer);
				}
			}
		}
		return false;
    }

	@Override
	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}
	
	@Override
	public boolean isAnonymous() {
		return false;
	}
}
