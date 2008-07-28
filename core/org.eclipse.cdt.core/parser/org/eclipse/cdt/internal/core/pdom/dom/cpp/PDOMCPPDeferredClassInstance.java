/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
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
	
	public PDOMCPPDeferredClassInstance(PDOM pdom, PDOMNode parent, ICPPDeferredClassInstance classType, PDOMBinding instantiated)
			throws CoreException {
		super(pdom, parent, classType, instantiated);

		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
		IType[] args = ((ICPPTemplateInstance) classType).getArguments();
		for (int i = 0; i < args.length; i++) {
			PDOMNode typeNode = getLinkageImpl().addType(this, args[i]);
			if (typeNode != null)
				list.addMember(typeNode);
		}
	}

	public PDOMCPPDeferredClassInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_DEFERRED_CLASS_INSTANCE;
	}

	public IScope getCompositeScope() throws DOMException {
		return ((ICPPClassType) getSpecializedBinding()).getCompositeScope();
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
			
			IType[] lhsArgs= getArguments();
			IType[] rhsArgs= rhs.getArguments();
			if (lhsArgs != rhsArgs) {
				if (lhsArgs == null || rhsArgs == null)
					return false;

				if (lhsArgs.length != rhsArgs.length)
					return false;

				for (int i= 0; i < lhsArgs.length; i++) {
					if (!CPPTemplates.isSameTemplateArgument(lhsArgs[i], rhsArgs[i])) 
						return false;
				}
			}
			return true;
		} 
		return false;
	}

	public ICPPClassTemplate getClassTemplate() {
		return (ICPPClassTemplate) getTemplateDefinition();
	}
			
	@Override
	public void addChild(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.addMember(member);
	}
	
	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
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

    public int getKey() throws DOMException{
        return 0;
    }

	@Override
	public Object clone() {fail();return null;}

	public ICPPScope getUnknownScope() throws DOMException {
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
	
	private static class TemplateArgumentCollector implements IPDOMVisitor {
		private List<IType> args = new ArrayList<IType>();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof IType)
				args.add((IType) node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public IType[] getTemplateArguments() {
			return args.toArray(new IType[args.size()]);
		}
	}
	
	public IType[] getArguments() {
		try {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
			TemplateArgumentCollector visitor = new TemplateArgumentCollector();
			list.accept(visitor);
			
			return visitor.getTemplateArguments();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return IType.EMPTY_TYPE_ARRAY;
		}
	}

	public boolean isAnonymous() {
		return false;
	}
}
