/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for namespace alias
 */
class PDOMCPPNamespaceAlias extends PDOMCPPBinding implements ICPPNamespaceAlias {

	private static final int NAMESPACE_BINDING = PDOMCPPBinding.RECORD_SIZE;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPBinding.RECORD_SIZE + Database.PTR_SIZE;
	
	public PDOMCPPNamespaceAlias(PDOMLinkage linkage, PDOMNode parent, ICPPNamespaceAlias alias)
	throws CoreException {
		super(linkage, parent, alias.getNameCharArray());
		setTargetBinding(parent.getLinkage(), alias.getBinding());
	}

	public PDOMCPPNamespaceAlias(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPNamespaceAlias) {
			ICPPNamespaceAlias alias= (ICPPNamespaceAlias) newBinding;
			IBinding newTarget= alias.getBinding();
			setTargetBinding(linkage, newTarget);
		}
	}
	
	private void setTargetBinding(PDOMLinkage linkage, IBinding target) throws CoreException {
		PDOMBinding namespace = getLinkage().adaptBinding(target);
		getDB().putRecPtr(record + NAMESPACE_BINDING, 
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
	
	@Override
	public ICPPNamespaceScope getNamespaceScope() {
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

	@Override
	public IBinding[] getMemberBindings() {
		ICPPNamespace ns= this;
		for (int i = 0; i < 20; i++) {
			if (ns instanceof ICPPNamespaceAlias) {
				IBinding b= ((ICPPNamespaceAlias) ns).getBinding();
				if (b instanceof ICPPNamespace) {
					ns= (ICPPNamespace) b;
				} else {
					return IBinding.EMPTY_BINDING_ARRAY;
				}
			} else {
				break;
			}
		}
		return ns.getMemberBindings();
	}

	@Override
	public IBinding getBinding() {
		try {
			return (IBinding) getLinkage().getNode(getPDOM().getDB().getRecPtr(record + NAMESPACE_BINDING));
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		return null;
	}

	@Override
	public boolean isInline() {
		return false;
	}
}
