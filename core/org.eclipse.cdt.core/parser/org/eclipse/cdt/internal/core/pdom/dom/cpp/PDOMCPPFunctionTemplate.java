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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
class PDOMCPPFunctionTemplate extends PDOMCPPFunction 
		implements ICPPFunctionTemplate, ICPPInstanceCache, IPDOMMemberOwner, IPDOMCPPTemplateParameterOwner {

	private static final int TEMPLATE_PARAMS = PDOMCPPFunction.RECORD_SIZE + 0;
	
	/**
	 * The size in bytes of a PDOMCPPFunctionTemplate record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunction.RECORD_SIZE + 4;
	
	public PDOMCPPFunctionTemplate(PDOM pdom, PDOMNode parent, ICPPFunctionTemplate template)
			throws CoreException {
		super(pdom, parent, (ICPPFunction) template, false);
	}

	public PDOMCPPFunctionTemplate(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	@Override
	public void update(PDOMLinkage linkage, IBinding name) {
		// no support for updating templates, yet.
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FUNCTION_TEMPLATE;
	}

	private static class TemplateParameterCollector implements IPDOMVisitor {
		private List<IPDOMNode> params = new ArrayList<IPDOMNode>();
		public boolean visit(IPDOMNode node) throws CoreException {
			if (node instanceof ICPPTemplateParameter)
				params.add(node);
			return false;
		}
		public void leave(IPDOMNode node) throws CoreException {
		}
		public ICPPTemplateParameter[] getTemplateParameters() {
			return params.toArray(new ICPPTemplateParameter[params.size()]);
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
	
	@Override
	public void addChild(PDOMNode member) throws CoreException {
		if (member instanceof ICPPTemplateParameter) {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + TEMPLATE_PARAMS, getLinkageImpl());
			list.addMember(member);
		} 
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + TEMPLATE_PARAMS, getLinkageImpl());
		list.accept(visitor);
	}

	public ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		return PDOMInstanceCache.getCache(this).getInstance(arguments);	
	}

	public void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		PDOMInstanceCache.getCache(this).addInstance(arguments, instance);	
	}
	
	public ICPPTemplateInstance[] getAllInstances() {
		return PDOMInstanceCache.getCache(this).getAllInstances();	
	}

	public ICPPTemplateParameter adaptTemplateParameter(ICPPTemplateParameter param) {
		// Template parameters are identified by their position in the parameter list.
		int pos = param.getParameterPosition() & 0xFFFF;
		try {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + TEMPLATE_PARAMS, getLinkageImpl());
			return (ICPPTemplateParameter) list.getNodeAt(pos);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
}
