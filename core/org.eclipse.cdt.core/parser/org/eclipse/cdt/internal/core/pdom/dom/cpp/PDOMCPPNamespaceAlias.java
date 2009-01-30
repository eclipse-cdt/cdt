/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPNamespaceAlias extends PDOMCPPBinding implements ICPPNamespaceAlias {

	private static final int NAMESPACE_BINDING = PDOMCPPBinding.RECORD_SIZE + 0;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + 4;
	
	public PDOMCPPNamespaceAlias(PDOMLinkage linkage, PDOMNode parent, ICPPNamespaceAlias alias)
	throws CoreException {
		super(linkage, parent, alias.getNameCharArray());
		setTargetBinding(parent.getLinkage(), alias.getBinding());
	}

	public PDOMCPPNamespaceAlias(PDOMLinkage linkage, int record) {
		super(linkage, record);
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPNamespaceAlias) {
			ICPPNamespaceAlias alias= (ICPPNamespaceAlias) newBinding;
			IBinding oldTarget= getBinding();
			IBinding newTarget= alias.getBinding();
			setTargetBinding(linkage, newTarget);
			if (oldTarget != null) {
				linkage.deleteBinding(oldTarget);
			}				
		}
	}
	
	private void setTargetBinding(PDOMLinkage linkage, IBinding target) throws CoreException {
		PDOMBinding namespace = getLinkage().adaptBinding(target);
		getDB().putInt(record + NAMESPACE_BINDING, 
				namespace != null ? namespace.getRecord() : 0);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPNAMESPACEALIAS;
	}
	
	public ICPPNamespaceScope getNamespaceScope() throws DOMException {
		return getNamespaceScope(this, 20);	// avoid an infinite loop.
	}
	
	private ICPPNamespaceScope getNamespaceScope(PDOMCPPNamespaceAlias alias, final int maxDepth) {
		IBinding binding= alias.getBinding();
		if (binding instanceof ICPPNamespaceScope) {
			return (ICPPNamespaceScope) binding;
		}

		if (maxDepth <= 0) {
			return null;
		}
		if (binding instanceof PDOMCPPNamespaceAlias) {
			return getNamespaceScope((PDOMCPPNamespaceAlias) binding, maxDepth-1);
		}
		return null;
	}

	public IBinding[] getMemberBindings() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IBinding getBinding() {
		try {
			return (IBinding) getLinkage().getNode(getPDOM().getDB().getInt(record + NAMESPACE_BINDING));
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		return null;
	}

}
