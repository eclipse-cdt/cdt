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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
public class PDOMCPPFunctionTemplate extends PDOMCPPFunction implements
		ICPPFunctionTemplate, ICPPInternalTemplateInstantiator,
		IPDOMMemberOwner {

	private static final int TEMPLATE_PARAMS = PDOMCPPFunction.RECORD_SIZE + 0;
	private static final int INSTANCES = PDOMCPPFunction.RECORD_SIZE + 4;
	private static final int SPECIALIZATIONS = PDOMCPPFunction.RECORD_SIZE + 8;
	
	/**
	 * The size in bytes of a PDOMCPPFunctionTemplate record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPFunction.RECORD_SIZE + 12;
	
	public PDOMCPPFunctionTemplate(PDOM pdom, PDOMNode parent,
			ICPPFunctionTemplate template) throws CoreException {
		super(pdom, parent, (ICPPFunction) template, false);
	}

	public PDOMCPPFunctionTemplate(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_FUNCTION_TEMPLATE;
	}

	private static class TemplateParameterCollector implements IPDOMVisitor {
		private List params = new ArrayList();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPTemplateParameter)
				params.add(node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPTemplateParameter[] getTemplateParameters() {
			return (ICPPTemplateParameter[])params.toArray(new ICPPTemplateParameter[params.size()]);
		}
	}
	
	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		try {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + TEMPLATE_PARAMS, getLinkageImpl());
			TemplateParameterCollector visitor = new TemplateParameterCollector();
			list.accept(visitor);
			
			return visitor.getTemplateParameters();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ICPPTemplateParameter[0];
		}
	}

	public ICPPSpecialization deferredInstance(IType[] arguments) {
		return getInstance(arguments);
	}

	private static class InstanceFinder implements IPDOMVisitor {
		private ICPPSpecialization instance = null;
		private IType[] arguments;
		
		public InstanceFinder(IType[] arguments) {
			this.arguments = arguments;
		}
		
		public boolean visit(IPDOMNode node) throws CoreException {
			if (instance == null && node instanceof PDOMCPPSpecialization) {
				PDOMCPPSpecialization spec = (PDOMCPPSpecialization) node;
				if (spec.matchesArguments(arguments)) {
					instance = spec;
				}
			}
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPSpecialization getInstance() {
			return instance;
		}
	}
	
	public ICPPSpecialization getInstance(IType[] arguments) {
		try {
			InstanceFinder visitor = new InstanceFinder(arguments);
			
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + INSTANCES, getLinkageImpl());
			list.accept(visitor);
			
			if (visitor.getInstance() == null) {
				list = new PDOMNodeLinkedList(pdom, record + SPECIALIZATIONS, getLinkageImpl());
				list.accept(visitor);
			}

			return visitor.getInstance();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public IBinding instantiate(IType[] arguments) {
		return getInstance(arguments);
	}

	public void addChild(PDOMNode child) throws CoreException {
		addMember(child);
	}

	public void addMember(PDOMNode member) throws CoreException {
		if (member instanceof ICPPTemplateParameter) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + TEMPLATE_PARAMS, getLinkageImpl());
			list.addMember(member);
		} else if (member instanceof ICPPTemplateInstance) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + INSTANCES, getLinkageImpl());
			list.addMember(member);
		} else if (member instanceof ICPPSpecialization) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + SPECIALIZATIONS, getLinkageImpl());
			list.addMember(member);
		}
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + TEMPLATE_PARAMS, getLinkageImpl());
		list.accept(visitor);
		list = new PDOMNodeLinkedList(pdom, record + INSTANCES, getLinkageImpl());
		list.accept(visitor);
	}
}
