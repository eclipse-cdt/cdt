/*******************************************************************************
 * Copyright (c) 2007, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateParameterMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Deferred class instances collect information about an instantiation until it can be
 * carried out.
 */
class PDOMCPPDeferredClassInstance extends PDOMCPPSpecialization implements ICPPDeferredClassInstance, IPDOMMemberOwner, IIndexType {

	private static final int MEMBERLIST = PDOMCPPSpecialization.RECORD_SIZE + 0;
	private static final int ARGUMENTS = PDOMCPPSpecialization.RECORD_SIZE + 4;	
	/**
	 * The size in bytes of a PDOMCPPDeferredClassInstance record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 8;

	private ICPPScope unknownScope;
	
	public PDOMCPPDeferredClassInstance(PDOMLinkage linkage, PDOMNode parent, ICPPDeferredClassInstance classType, PDOMBinding instantiated)
			throws CoreException {
		super(linkage, parent, classType, instantiated);

		final ICPPTemplateArgument[] args= classType.getTemplateArguments();
		final long argListRec= PDOMCPPArgumentList.putArguments(this, args);
		getDB().putRecPtr(record+ARGUMENTS, argListRec);
	}

	public PDOMCPPDeferredClassInstance(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_DEFERRED_CLASS_INSTANCE;
	}
	
	public boolean isExplicitSpecialization() {
		return false;
	}

	public IScope getCompositeScope() {
		return asScope();
	}
		
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
        if (type instanceof PDOMNode) {
			PDOMNode node = (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
        }
		
		ICPPClassTemplate classTemplate = getClassTemplate();
		
		if (type instanceof ICPPDeferredClassInstance) {
			final ICPPDeferredClassInstance rhs = (ICPPDeferredClassInstance) type;
			if (!classTemplate.isSameType((IType) rhs.getSpecializedBinding())) 
				return false;
			
			return CPPTemplates.haveSameArguments(this, rhs);
		}
		return false;
	}

	public ICPPClassTemplate getClassTemplate() {
		return (ICPPClassTemplate) getSpecializedBinding();
	}
			
	@Override
	public void addChild(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.addMember(member);
	}
	
	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(getLinkage(), record + MEMBERLIST);
		list.accept(visitor);
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

	public ICPPClassType[] getNestedClasses() {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

    public int getKey(){
        return getClassTemplate().getKey();
    }

	@Override
	public Object clone() {
		throw new UnsupportedOperationException(); 
	}

	public ICPPScope asScope() {
		if (unknownScope == null) {
			unknownScope= new PDOMCPPUnknownScope(this, getUnknownName());
		}
		return unknownScope;
	}

	public IASTName getUnknownName() {
		return new CPPASTName(getNameCharArray());
	}

	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}
	
	public ICPPTemplateArgument[] getTemplateArguments() {
		try {
			final long rec= getPDOM().getDB().getRecPtr(record+ARGUMENTS);
			return PDOMCPPArgumentList.getArguments(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return ICPPTemplateArgument.EMPTY_ARGUMENTS;
		}
	}

	public boolean isAnonymous() {
		return false;
	}
	
	@Override
	public CPPTemplateParameterMap getTemplateParameterMap() {
		try {
			ICPPTemplateParameter[] params = getClassTemplate().getTemplateParameters();
			ICPPTemplateArgument[] args = getTemplateArguments();
			int size = Math.min(args.length, params.length);
			CPPTemplateParameterMap map = new CPPTemplateParameterMap(size);
			for (int i = 0; i < size; i++) {
				map.put(params[i], args[i]);
			}
			return map;
		} catch (DOMException e) {
		}
		return CPPTemplateParameterMap.EMPTY;
	}
	
	@Deprecated
	public IType[] getArguments() {
		return CPPTemplates.getArguments(getTemplateArguments());
	}
}
