/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPNamespaceAlias extends PDOMCPPBinding implements
		ICPPNamespaceAlias {

	private static final int NAMESPACE_BINDING = PDOMCPPBinding.RECORD_SIZE + 0;
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 4;
	
	public PDOMCPPNamespaceAlias(PDOM pdom, PDOMNode parent, ICPPNamespaceAlias alias)
	throws CoreException {
		super(pdom, parent, alias.getNameCharArray());
		PDOMBinding namespace = getLinkageImpl().adaptBinding(alias.getBinding());
		pdom.getDB().putInt(record + NAMESPACE_BINDING, 
				namespace != null ? namespace.getRecord() : 0);
	}

	public PDOMCPPNamespaceAlias(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPPNAMESPACEALIAS;
	}
	
	public ICPPNamespaceScope getNamespaceScope() throws DOMException {
		return (ICPPNamespaceScope) getBinding();
	}

	public IBinding[] getMemberBindings() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public int getDelegateType() {
		throw new PDOMNotImplementedError();
	}

	public IBinding getBinding() {
		try {
			return (IBinding) getLinkageImpl().getNode(getPDOM().getDB().getInt(record + NAMESPACE_BINDING));
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		return null;
	}

}
