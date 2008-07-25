/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPClassInstance extends PDOMCPPClassSpecialization implements ICPPTemplateInstance {
	
	private static final int ARGUMENTS = PDOMCPPClassSpecialization.RECORD_SIZE + 0;
	
	/**
	 * The size in bytes of a PDOMCPPClassInstance record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPClassSpecialization.RECORD_SIZE + 4;
	
	public PDOMCPPClassInstance(PDOM pdom, PDOMNode parent, ICPPClassType classType, PDOMBinding orig)
			throws CoreException {
		super(pdom, parent, classType, orig);

		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
		IType[] args = ((ICPPTemplateInstance) classType).getArguments();
		for (int i = 0; i < args.length; i++) {
			PDOMNode typeNode = getLinkageImpl().addType(this, args[i]);
			if (typeNode != null)
				list.addMember(typeNode);
		}
	}
	
	public PDOMCPPClassInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
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
		
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_INSTANCE;
	}
	
	@Override
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
		
		// require a class instance
		if (type instanceof ICPPClassSpecialization == false ||
				type instanceof ICPPTemplateInstance == false || type instanceof IProblemBinding) 
			return false;
		
		
		final ICPPClassSpecialization classSpec2 = (ICPPClassSpecialization) type;
		final ICPPClassType orig1= getSpecializedBinding();
		final ICPPClassType orig2= classSpec2.getSpecializedBinding();
		if (!orig1.isSameType(orig2))
			return false;
		
		IType[] args1= getArguments();
		IType[] args2= ((ICPPTemplateInstance) type).getArguments();
		if (args1.length != args2.length)
			return false;
		
		for (int i = 0; i < args1.length; i++) {
			if (!CPPTemplates.isSameTemplateArgument(args1[i], args2[i]))
				return false;
		}
		return true;
	}
}
