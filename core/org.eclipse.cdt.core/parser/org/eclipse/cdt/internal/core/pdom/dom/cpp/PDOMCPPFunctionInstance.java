/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPFunctionInstance extends PDOMCPPFunctionSpecialization implements ICPPTemplateInstance {
	private static final int ARGUMENTS = PDOMCPPFunctionSpecialization.RECORD_SIZE + 0;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunctionSpecialization.RECORD_SIZE + 4;
	
	public PDOMCPPFunctionInstance(PDOM pdom, PDOMNode parent, ICPPFunction function, PDOMBinding orig)
			throws CoreException {
		super(pdom, parent, function, orig);

		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
		IType[] args = ((ICPPTemplateInstance) function).getArguments();
		for (int i = 0; i < args.length; i++) {
			PDOMNode typeNode = getLinkageImpl().addType(this, args[i]);
			if (typeNode != null)
				list.addMember(typeNode);
		}
	}

	public PDOMCPPFunctionInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FUNCTION_INSTANCE;
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
	
	/*
	 * For debug purposes only
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(getName());
		result.append("(){"); 
		try {
			result.append(ASTTypeUtil.getType(getType()));
		} catch (DOMException e) {
			e.printStackTrace();
		}
		result.append("} "); 
		try {
			result.append(getConstantNameForValue(getLinkageImpl(), getNodeType()));
		} catch (CoreException ce) {
			result.append(getNodeType());
		}
		return result.toString();
	}
}
