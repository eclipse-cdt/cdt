/*******************************************************************************
 * Copyright (c) 2008 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Sergey Prigogin
 */
class PDOMCPPUnknownClassType extends PDOMCPPBinding implements ICPPClassScope, ICPPInternalUnknownClassType,
		IPDOMMemberOwner, IIndexType, IIndexScope {

	private static final int FIRSTBASE = PDOMCPPBinding.RECORD_SIZE + 0;
	private static final int KEY = PDOMCPPBinding.RECORD_SIZE + 4; // byte
	private static final int MEMBERLIST = PDOMCPPBinding.RECORD_SIZE + 8;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 12;

	public PDOMCPPUnknownClassType(PDOM pdom, PDOMNode parent, ICPPInternalUnknownClassType classType)
			throws CoreException {
		super(pdom, parent, classType.getNameCharArray());

		setKind(classType);
		// linked list is initialized by storage being zero'd by malloc
	}

	public PDOMCPPUnknownClassType(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
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
		try {
			pdom.getDB().putByte(record + KEY, (byte) ct.getKey());
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
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

	public PDOMCPPBase getFirstBase() throws CoreException {
		int rec = pdom.getDB().getInt(record + FIRSTBASE);
		return rec != 0 ? new PDOMCPPBase(pdom, rec) : null;
	}

	private void setFirstBase(PDOMCPPBase base) throws CoreException {
		int rec = base != null ? base.getRecord() : 0;
		pdom.getDB().putInt(record + FIRSTBASE, rec);
	}

	public void addBase(PDOMCPPBase base) throws CoreException {
		PDOMCPPBase firstBase = getFirstBase();
		base.setNextBase(firstBase);
		setFirstBase(base);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.accept(visitor);
	}

	public ICPPMethod[] getImplicitMethods() {
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	public IScope getCompositeScope() throws DOMException {
		return this;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown#getUnknownScope()
     */
    public ICPPScope getUnknownScope() { fail(); return null; }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.IIndexScope#getScopeBinding()
     */
	public IIndexBinding getScopeBinding() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
	 */
	@Override
	public boolean isGloballyQualified() throws DOMException {
		try {
			return getParentNode() instanceof PDOMLinkage;
		} catch (CoreException e) {
			return true;
		}
	}

	public ICPPClassType getClassType() {
		return this;
	}

	@Override
	public void addChild(PDOMNode member) throws CoreException {
		addMember(member);
	}

	public boolean isFullyCached()  {
		return true;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) throws DOMException {
		return null;
	}
	
	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet)
			throws DOMException {
		return IBinding.EMPTY_BINDING_ARRAY;
	}
	
	public IBinding[] find(String name) throws DOMException {
		return CPPSemantics.findBindings(this, name, false);
	}
	
	// Not implemented

	@Override
	public Object clone() { fail(); return null; }
	public IField findField(String name) throws DOMException { fail(); return null; }

	@Override
	public boolean mayHaveChildren() {
		return true;
	}

	public void removeBase(PDOMName pdomName) throws CoreException {
	}
	
	public void addDeclaration(IASTNode node) {
	}

	public void addDefinition(IASTNode node) {
	}

	public IASTNode[] getDeclarations() {
		return null;
	}

	public IASTNode getDefinition() {
		return null;
	}

	public void removeDeclaration(IASTNode node) {
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
     */
    public ICPPBase[] getBases() {
        return ICPPBase.EMPTY_BASE_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
     */
    public IField[] getFields() {
        return IField.EMPTY_FIELD_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
     */
    public ICPPField[] getDeclaredFields() {
        return ICPPField.EMPTY_CPPFIELD_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
     */
    public ICPPMethod[] getMethods() {
        return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
     */
    public ICPPMethod[] getAllDeclaredMethods() {
        return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
     */
    public ICPPMethod[] getDeclaredMethods() {
        return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
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
        return IBinding.EMPTY_BINDING_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
     */
    public int getKey() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType(IType type) {
        return type == this;
    }

	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown#resolveUnknown(org.eclipse.cdt.core.parser.util.ObjectMap)
     */
    public IBinding resolveUnknown(ObjectMap argMap) throws DOMException {
        IBinding result = this;
		try {
			IIndexBinding parentBinding = getParentBinding();
			IType t = null;
			if (parentBinding instanceof ICPPTemplateTypeParameter) {
				t = CPPTemplates.instantiateType((ICPPTemplateTypeParameter) parentBinding, argMap);
			} else if (parentBinding instanceof ICPPInternalUnknownClassType) {
	        	IBinding binding = ((ICPPInternalUnknownClassType) parentBinding).resolveUnknown(argMap);
	        	if (binding instanceof IType) {
	                t = (IType) binding;
	            }
	        }
	        if (t != null) {
	            t = SemanticUtil.getUltimateType(t, false);
		        if (t instanceof ICPPClassType) {
		            IScope s = ((ICPPClassType) t).getCompositeScope();
		            if (s != null && ASTInternal.isFullyCached(s)) {
		            	IBinding[] bindings = s.find(getName());
		            	if (bindings != null && bindings.length > 0) {
		            		result = bindings[0];
		            	}
		            }
		        } else if (t instanceof ICPPInternalUnknown) {
		            result = resolvePartially((ICPPInternalUnknown) t, argMap);
		        }
	        }
		} catch (CoreException e) {
		}
        return result;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassType#resolvePartially(org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown, org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	public IBinding resolvePartially(ICPPInternalUnknown parentBinding,	ObjectMap argMap) {
		try {
			if (parentBinding == getParentBinding()) {
				return this;
			}
		} catch (CoreException e) {
		}
		IASTName name = new CPPASTName(getNameCharArray());
		return new CPPUnknownClass(parentBinding, name);
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}
