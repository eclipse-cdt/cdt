/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
abstract class PDOMCPPInstance extends PDOMCPPSpecialization implements
		ICPPTemplateInstance {
	
	private static final int ARGUMENTS = PDOMCPPSpecialization.RECORD_SIZE + 0;
	
	/**
	 * The size in bytes of a PDOMCPPInstance record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 4;
	
	public PDOMCPPInstance(PDOM pdom, PDOMNode parent, ICPPTemplateInstance inst, PDOMBinding instantiated)
			throws CoreException {
		super(pdom, parent, inst, instantiated);
		
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + ARGUMENTS, getLinkageImpl());
		IType[] args = inst.getArguments();
		for (int i = 0; i < args.length; i++) {
			PDOMNode typeNode = getLinkageImpl().addType(this, args[i]);
			list.addMember(typeNode);
		}
	}

	public PDOMCPPInstance(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	public ICPPTemplateDefinition getTemplateDefinition() {
		return (ICPPTemplateDefinition) getSpecializedBinding();
	}
	
	private static class TemplateArgumentCollector implements IPDOMVisitor {
		private List args = new ArrayList();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof IType)
				args.add(node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public IType[] getTemplateArguments() {
			return (IType[])args.toArray(new IType[args.size()]);
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
			return new IType[0];
		}
	}
	
	public boolean matchesArguments(IType[] arguments) {
		IType [] args = getArguments();
		if( args.length == arguments.length ){
			int i = 0;
			for(; i < args.length; i++) {
				if( !( args[i].isSameType( arguments[i] ) ) )
					break;
			}
			return i == args.length;
		}
		return false;
	}
}
