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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Sergey Prigogin
 */
class PDOMCPPUnknownClassInstance extends PDOMCPPUnknownClassType
		implements ICPPInternalUnknownClassInstance {

	private static final int ARGUMENTS = PDOMCPPUnknownClassType.RECORD_SIZE + 0;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPUnknownClassType.RECORD_SIZE + 4;
	
	// Cached values.
	IType[] arguments;

	public PDOMCPPUnknownClassInstance(PDOM pdom, PDOMNode parent,
			ICPPInternalUnknownClassInstance classInstance)	throws CoreException {
		super(pdom, parent, classInstance);
		
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
		IType[] args = classInstance.getArguments();
		for (int i = 0; i < args.length; i++) {
			PDOMNode typeNode = getLinkageImpl().addType(this, args[i]);
			if (typeNode != null)
				list.addMember(typeNode);
		}
	}

	public PDOMCPPUnknownClassInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_UNKNOWN_CLASS_INSTANCE;
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
		if (arguments == null) {
			try {
				PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
				TemplateArgumentCollector visitor = new TemplateArgumentCollector();
				list.accept(visitor);
				arguments = visitor.getTemplateArguments();
			} catch (CoreException e) {
				CCorePlugin.log(e);
				return IType.EMPTY_TYPE_ARRAY;
			}
		}
		return arguments;
	}

	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations()
			throws DOMException {
		return ICPPClassTemplatePartialSpecialization.EMPTY_PARTIAL_SPECIALIZATION_ARRAY;
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		return ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
	}

	public void addPartialSpecialization(ICPPClassTemplatePartialSpecialization spec) {
	}

	public void addSpecialization(IType[] arguments, ICPPSpecialization specialization) {
	}

	public ICPPSpecialization deferredInstance(ObjectMap argMap, IType[] arguments) {
		ICPPSpecialization instance = getInstance(arguments);
		if (instance == null) {
			instance = new CPPDeferredClassInstance(this, argMap, arguments);
			addSpecialization(arguments, instance);
		}
		return instance;
	}

	public ICPPSpecialization getInstance(IType[] arguments) {
		return null;
	}

	public IBinding instantiate(IType[] arguments) {
		return deferredInstance(null, arguments);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown#resolveUnknown(org.eclipse.cdt.core.parser.util.ObjectMap)
     */
	@Override
	public IBinding resolveUnknown(ObjectMap argMap) throws DOMException {
		IBinding result = super.resolveUnknown(argMap);
		if (result instanceof ICPPSpecialization && result instanceof ICPPTemplateDefinition) {
			IType[] newArgs = CPPTemplates.instantiateTypes(getArguments(), argMap);
			IBinding instance = CPPTemplates.instantiateTemplate((ICPPTemplateDefinition) result, newArgs, null);
			if (instance != null) {
				result = instance;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownClassType#resolvePartially(org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknown, org.eclipse.cdt.core.parser.util.ObjectMap)
	 */
	@Override
	public IBinding resolvePartially(ICPPInternalUnknown parentBinding,	ObjectMap argMap) {
		IType[] arguments = getArguments();
		IType[] newArgs = CPPTemplates.instantiateTypes(arguments, argMap);
		if (parentBinding instanceof PDOMNode && isChildOf((PDOMNode) parentBinding) &&
				Arrays.equals(newArgs, arguments)) {
			return this;
		}
		IASTName name = new CPPASTName(getNameCharArray());
		return new CPPUnknownClassInstance(parentBinding, name, newArgs);
	}

	@Override
	public String toString() {
		return getName() + " <" + ASTTypeUtil.getTypeListString(getArguments()) + ">"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
