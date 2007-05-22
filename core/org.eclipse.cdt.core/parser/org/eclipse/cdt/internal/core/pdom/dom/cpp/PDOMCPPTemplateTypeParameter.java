/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexInternalTemplateParameter;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPTemplateTypeParameter extends PDOMCPPBinding implements
		ICPPTemplateTypeParameter, IIndexType, IIndexInternalTemplateParameter {

	private static final int DEFAULT_TYPE = PDOMCPPBinding.RECORD_SIZE + 0;	
	
	/**
	 * The size in bytes of a PDOMCPPTemplateTypeParameter record in the database.
	 */
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 4;
	
	public PDOMCPPTemplateTypeParameter(PDOM pdom, PDOMNode parent,
			ICPPTemplateTypeParameter param) throws CoreException {
		super(pdom, parent, param.getNameCharArray());
		
		try {
			IType dflt = param.getDefault();
			if (dflt != null) {
				PDOMNode typeNode = getLinkageImpl().addType(this, dflt);
				if (typeNode != null) {
					pdom.getDB().putInt(record + DEFAULT_TYPE, typeNode.getRecord());
				}
			}
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPTemplateTypeParameter(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_TEMPLATE_TYPE_PARAMETER;
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
		
		if (type instanceof ICPPTemplateTypeParameter && !(type instanceof ProblemBinding)) {
			ICPPTemplateTypeParameter ttp= (ICPPTemplateTypeParameter) type;
			try {
				char[][] ttpName= ttp.getQualifiedNameCharArray();
				return hasQualifiedName(ttpName, ttpName.length-1);
			} catch (DOMException e) {
				CCorePlugin.log(e);
			}
		}
		return false;
	}

	public IType getDefault() throws DOMException {
		try {
			PDOMNode node = getLinkageImpl().getNode(pdom.getDB().getInt(record + DEFAULT_TYPE));
			if (node instanceof IType) {
				return (IType) node;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	public ICPPBinding getParameterOwner() throws CoreException {
		return (ICPPBinding) getParentBinding();
	}
	
	public Object clone() { fail();return null; }
}
