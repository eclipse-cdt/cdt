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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredFunctionInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPFunctionTemplateSpecialization extends
		PDOMCPPFunctionSpecialization implements ICPPFunctionTemplate,
		ICPPInternalTemplateInstantiator, IPDOMMemberOwner {

	private static final int INSTANCES = PDOMCPPFunctionSpecialization.RECORD_SIZE + 0;
	private static final int SPECIALIZATIONS = PDOMCPPFunctionSpecialization.RECORD_SIZE + 4;
	
	/**
	 * The size in bytes of a PDOMCPPFunctionTemplateSpecialization record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPFunctionSpecialization.RECORD_SIZE + 8;
	
	public PDOMCPPFunctionTemplateSpecialization(PDOM pdom, PDOMNode parent, ICPPFunctionTemplate template, PDOMBinding specialized)
			throws CoreException {
		super(pdom, parent, (ICPPFunction) template, specialized);
	}

	public PDOMCPPFunctionTemplateSpecialization(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_FUNCTION_TEMPLATE_SPECIALIZATION;
	}
	
	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPFunctionTemplate template = (ICPPFunctionTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
	}

	public ICPPSpecialization deferredInstance(IType[] arguments) {
		ICPPSpecialization instance = getInstance(arguments);
		if( instance == null ){
			instance = new CPPDeferredFunctionInstance( this, arguments );
		}
		return instance;
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
		return CPPTemplates.instantiateTemplate(this, arguments, getArgumentMap());
	}

	public void addChild(PDOMNode child) throws CoreException {
		addMember(child);
	}

	public void addMember(PDOMNode member) throws CoreException {
		if (member instanceof ICPPTemplateInstance) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + INSTANCES, getLinkageImpl());
			list.addMember(member);
		} else if (member instanceof ICPPSpecialization) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + SPECIALIZATIONS, getLinkageImpl());
			list.addMember(member);
		}
	}
	
	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + INSTANCES, getLinkageImpl());
		list.accept(visitor);
	}
}
