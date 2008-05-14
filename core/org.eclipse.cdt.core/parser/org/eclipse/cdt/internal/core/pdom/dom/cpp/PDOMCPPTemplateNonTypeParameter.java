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
 *    Sergey Prigogin (Google)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPTemplateNonTypeParameter extends PDOMCPPVariable implements IPDOMMemberOwner,
		ICPPTemplateNonTypeParameter, IIndexType {

	private static final int MEMBERLIST = PDOMCPPVariable.RECORD_SIZE + 4;

	/**
	 * The size in bytes of a PDOMCPPTemplateTypeParameter record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPVariable.RECORD_SIZE + 4;
	
	public PDOMCPPTemplateNonTypeParameter(PDOM pdom, PDOMNode parent,
			ICPPTemplateNonTypeParameter param) throws CoreException {
		super(pdom, parent, param);
	}

	public PDOMCPPTemplateNonTypeParameter(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TEMPLATE_NON_TYPE_PARAMETER;
	}
	
	@Override
	public void addChild(PDOMNode member) throws CoreException {
		addMember(member);
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.addMember(member);
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + MEMBERLIST, getLinkageImpl());
		list.accept(visitor);
	}
	
	public boolean isSameType(IType type) {
		if (type instanceof ITypedef) {
			return type.isSameType(this);
		}
		
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}
		
		if (type instanceof ICPPTemplateNonTypeParameter && !(type instanceof ProblemBinding)) {
			ICPPTemplateNonTypeParameter ttp= (ICPPTemplateNonTypeParameter) type;
			try {
				char[][] ttpName= ttp.getQualifiedNameCharArray();
				return hasQualifiedName(ttpName, ttpName.length - 1);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
		return false;
	}
	
	public ICPPBinding getParameterOwner() throws CoreException {
		return (ICPPBinding) getParentBinding();
	}
	
	@Override
	public Object clone() { fail(); return null; }


	public IASTExpression getDefault() {
		return null;
	}
}
