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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredFunctionInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPFunctionTemplate extends PDOMCPPFunction implements
		ICPPFunctionTemplate, ICPPInternalTemplateInstantiator,
		IPDOMMemberOwner, ICPPTemplateScope, IIndexScope {

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
		return CPPTemplates.instantiateTemplate(this, arguments, null);
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

	public ICPPTemplateDefinition getTemplateDefinition() throws DOMException {
		return this;
	}

	public IBinding[] find(String name) throws DOMException {
		return CPPSemantics.findBindings( this, name, false );
	}

	public IBinding getBinding(IASTName name, boolean resolve)
			throws DOMException {
		try {
			BindingCollector visitor = new BindingCollector(getLinkageImpl(), name.toCharArray());
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + TEMPLATE_PARAMS, getLinkageImpl());
			list.accept(visitor);
			
			return CPPSemantics.resolveAmbiguities(name, visitor.getBindings());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup)
			throws DOMException {
		IBinding[] result = null;
		try {
			BindingCollector visitor = new BindingCollector(getLinkageImpl(), name.toCharArray(), null, prefixLookup, !prefixLookup);
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + TEMPLATE_PARAMS, getLinkageImpl());
			list.accept(visitor);
			result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, visitor.getBindings());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}

	public IIndexBinding getScopeBinding() {
		return this;
	}
}
