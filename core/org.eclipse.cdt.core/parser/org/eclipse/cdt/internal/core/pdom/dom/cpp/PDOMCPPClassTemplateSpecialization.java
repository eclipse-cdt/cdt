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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
public class PDOMCPPClassTemplateSpecialization extends
		PDOMCPPClassSpecialization implements ICPPClassTemplate, ICPPInternalTemplateInstantiator {

	private static final int INSTANCES = PDOMCPPClassSpecialization.RECORD_SIZE + 0;
	private static final int SPECIALIZATIONS = PDOMCPPClassSpecialization.RECORD_SIZE + 4;
	private static final int FIRST_PARTIAL = PDOMCPPClassSpecialization.RECORD_SIZE + 8;
	
	/**
	 * The size in bytes of a PDOMCPPClassTemplateSpecialization record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPClassSpecialization.RECORD_SIZE + 12;
	
	public PDOMCPPClassTemplateSpecialization(PDOM pdom, PDOMNode parent, ICPPClassTemplate template, PDOMBinding specialized)
			throws CoreException {
		super(pdom, parent, (ICPPClassType) template, specialized);
	}

	public PDOMCPPClassTemplateSpecialization(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPP_CLASS_TEMPLATE_SPECIALIZATION;
	}
	
	private PDOMCPPClassTemplatePartialSpecialization getFirstPartial() throws CoreException {
		int value = pdom.getDB().getInt(record + FIRST_PARTIAL);
		return value != 0 ? new PDOMCPPClassTemplatePartialSpecialization(pdom, value) : null;
	}
	
	public void addPartial(PDOMCPPClassTemplatePartialSpecialization partial) throws CoreException {
		PDOMCPPClassTemplatePartialSpecialization first = getFirstPartial();
		partial.setNextPartial(first);
		pdom.getDB().putInt(record + FIRST_PARTIAL, partial.getRecord());
	}
		
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() throws DOMException {
		try {
			ArrayList partials = new ArrayList();
			for (PDOMCPPClassTemplatePartialSpecialization partial = getFirstPartial();
					partial != null;
					partial = partial.getNextPartial()) {
				partials.add(partial);
			}
			
			return (ICPPClassTemplatePartialSpecialization[]) partials
					.toArray(new ICPPClassTemplatePartialSpecialization[partials
							.size()]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ICPPClassTemplatePartialSpecialization[0];
		}
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPClassTemplate template = (ICPPClassTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
	}

	public ICPPSpecialization deferredInstance(IType[] arguments) {
		return getInstance( arguments );
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
		ICPPTemplateDefinition template = null;
		try {
			template = CPPTemplates.matchTemplatePartialSpecialization(this, arguments);
		} catch (DOMException e) {
			return e.getProblem();
		}
		
		if( template instanceof IProblemBinding )
			return template;
		if( template != null && template instanceof ICPPClassTemplatePartialSpecialization ){
			return ((PDOMCPPClassTemplate)template).instantiate( arguments );	
		}
		
		return getInstance(arguments);
	}
	
	public void addMember(PDOMNode member) throws CoreException {
		if (member instanceof ICPPTemplateInstance) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + INSTANCES, getLinkageImpl());
			list.addMember(member);
		} else if (member instanceof ICPPSpecialization
				&& !(member instanceof ICPPClassTemplatePartialSpecialization)) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + SPECIALIZATIONS, getLinkageImpl());
			list.addMember(member);
		} else {
			super.addMember(member);
		}
	}

	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + INSTANCES, getLinkageImpl());
		list.accept(visitor);
	}
}
