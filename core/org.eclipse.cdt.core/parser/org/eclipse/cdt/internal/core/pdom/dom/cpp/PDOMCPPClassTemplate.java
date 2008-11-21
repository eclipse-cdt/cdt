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
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 */
public class PDOMCPPClassTemplate extends PDOMCPPClassType
		implements ICPPClassTemplate, ICPPInstanceCache, IPDOMCPPTemplateParameterOwner {
	private static final int PARAMETERS = PDOMCPPClassType.RECORD_SIZE + 0;
	private static final int FIRST_PARTIAL = PDOMCPPClassType.RECORD_SIZE + 4;
	
	/**
	 * The size in bytes of a PDOMCPPClassTemplate record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPClassType.RECORD_SIZE + 8;
	
	private ICPPTemplateParameter[] params;  // Cached template parameters.
	
	public PDOMCPPClassTemplate(PDOM pdom, PDOMNode parent, ICPPClassTemplate template)	throws CoreException {
		super(pdom, parent, template);
	}

	public PDOMCPPClassTemplate(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_TEMPLATE;
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
	
	public ICPPTemplateParameter[] getTemplateParameters() {
		if (params == null) {
			try {
				PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + PARAMETERS, getLinkageImpl());
				TemplateParameterCollector visitor = new TemplateParameterCollector();
				list.accept(visitor);
				params = visitor.getTemplateParameters();
			} catch (CoreException e) {
				CCorePlugin.log(e);
				params = ICPPTemplateParameter.EMPTY_TEMPLATE_PARAMETER_ARRAY;
			}
		}
		// Copy to a new array for safety.
		ICPPTemplateParameter[] result = new ICPPTemplateParameter[params.length];
		System.arraycopy(params, 0, result, 0, params.length);
		return result;
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
			ArrayList<PDOMCPPClassTemplatePartialSpecialization> partials =
					new ArrayList<PDOMCPPClassTemplatePartialSpecialization>();
			for (PDOMCPPClassTemplatePartialSpecialization partial = getFirstPartial();
					partial != null;
					partial = partial.getNextPartial()) {
				partials.add(partial);
			}
			
			return partials.toArray(new ICPPClassTemplatePartialSpecialization[partials.size()]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new ICPPClassTemplatePartialSpecialization[0];
		}
	}

	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
		PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + PARAMETERS, getLinkageImpl());
		list.accept(visitor);
	}

	@Override
	public void addChild(PDOMNode member) throws CoreException {
		if (member instanceof ICPPTemplateParameter) {
			params= null; // clear cache
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + PARAMETERS, getLinkageImpl());
			list.addMember(member);
		} else {
			super.addChild(member);
		}
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

		// need a class template
		if (type instanceof ICPPClassTemplate == false || type instanceof ProblemBinding) 
			return false;
		
		// exclude other kinds of class templates
		if (type instanceof ICPPClassTemplatePartialSpecialization ||
				type instanceof ICPPTemplateTemplateParameter ||
				type instanceof ICPPClassSpecialization)
			return false;
				
		try {
			ICPPClassType ctype= (ICPPClassType) type;
			if (ctype.getKey() != getKey())
				return false;
				
			final char[][] qname= ctype.getQualifiedNameCharArray();
			if (!hasQualifiedName(qname, qname.length - 1)) 
				return false;
				
			ICPPTemplateParameter[] params1= getTemplateParameters();
			ICPPTemplateParameter[] params2= ((ICPPClassTemplate) type).getTemplateParameters();

			if (params1 == params2)
				return true;

			if (params1 == null || params2 == null)
				return false;

			if (params1.length != params2.length)
				return false;

			for (int i = 0; i < params1.length; i++) {
				ICPPTemplateParameter p1= params1[i];
				ICPPTemplateParameter p2= params2[i];
				if (p1 instanceof IType && p2 instanceof IType) {
					IType t1= (IType) p1;
					IType t2= (IType) p2;
					if (!t1.isSameType(t2)) {
						return false;
					}
				} else if (p1 instanceof ICPPTemplateNonTypeParameter
						&& p2 instanceof ICPPTemplateNonTypeParameter) {
					IType t1= ((ICPPTemplateNonTypeParameter)p1).getType();
					IType t2= ((ICPPTemplateNonTypeParameter)p2).getType();
					if (t1 != t2) {
						if (t1 == null || t2 == null || !t1.isSameType(t2)) {
							return false;
						}
					}
				} else {
					return false;
				}
			}
			return true;
		} catch (DOMException e) {
			return false;
		}
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
		int pos = param.getParameterPosition();
		if (params != null) {
			return pos < params.length ? params[pos] : null;
		}
		try {
			PDOMNodeLinkedList list = new PDOMNodeLinkedList(pdom, record + PARAMETERS, getLinkageImpl());
			return (ICPPTemplateParameter) list.getNodeAt(pos);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
}
