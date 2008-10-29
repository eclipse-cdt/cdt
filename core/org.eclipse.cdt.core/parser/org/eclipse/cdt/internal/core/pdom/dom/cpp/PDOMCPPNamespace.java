/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 */
class PDOMCPPNamespace extends PDOMCPPBinding
		implements ICPPNamespace, ICPPNamespaceScope, IIndexScope {

	private static final int INDEX_OFFSET = PDOMBinding.RECORD_SIZE + 0;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;

	public PDOMCPPNamespace(PDOM pdom, PDOMNode parent, ICPPNamespace namespace) throws CoreException {
		super(pdom, parent, namespace.getNameCharArray());
	}

	public PDOMCPPNamespace(PDOM pdom, int record) throws CoreException {
		super(pdom, record);
	}

	public EScopeKind getKind() {
		return EScopeKind.eNamespace;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPNAMESPACE;
	}

	public BTree getIndex() throws CoreException {
		return new BTree(pdom.getDB(), record + INDEX_OFFSET, getLinkageImpl().getIndexComparator());
	}

	@Override
	public void accept(final IPDOMVisitor visitor) throws CoreException {
		if (visitor instanceof IBTreeVisitor) {
			getIndex().accept((IBTreeVisitor) visitor);
		} else {
			getIndex().accept(new IBTreeVisitor() {
				public int compare(int record) throws CoreException {
					return 0;
				}
				public boolean visit(int record) throws CoreException {
					PDOMBinding binding = pdom.getBinding(record);
					if (binding != null) {
						if (visitor.visit(binding))
							binding.accept(visitor);
						visitor.leave(binding);
					}
					return true;
				}
			});
		}
	}

	@Override
	public void addChild(PDOMNode child) throws CoreException {
		getIndex().insert(child.getRecord());
	}

	public ICPPNamespaceScope getNamespaceScope() throws DOMException {
		return this;
	}

	public ICPPUsingDirective[] getUsingDirectives() throws DOMException {
		return new ICPPUsingDirective[0];
	}

	public IBinding[] find(String name) {
		try {
			BindingCollector visitor = new BindingCollector(getLinkageImpl(),  name.toCharArray(),
					IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, false, true);
			getIndex().accept(visitor);
			return visitor.getBindings();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) throws DOMException {
		try {
			IBinding[] bindings= getBindingsViaCache(name.toCharArray());
			if (fileSet != null) {
				bindings= fileSet.filterFileLocalBindings(bindings);
			}
			return CPPSemantics.resolveAmbiguities(name, bindings);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}
	
	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet)
			throws DOMException {
		IBinding[] result = null;
		try {
			if (!prefixLookup) {
				return getBindingsViaCache(name.toCharArray());
			}
			BindingCollector visitor= new BindingCollector(getLinkageImpl(), name.toCharArray(),
					IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, prefixLookup, !prefixLookup);
			getIndex().accept(visitor);
			IBinding[] bindings = visitor.getBindings();
			if (fileSet != null) {
				bindings= fileSet.filterFileLocalBindings(bindings);
			}
			result = (IBinding[]) ArrayUtil.addAll(IBinding.class, result, bindings);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}

	private IBinding[] getBindingsViaCache(final char[] name) throws CoreException {
		final String key= pdom.createKeyForCache(record, name);
		IBinding[] result= (IBinding[]) pdom.getCachedResult(key);
		if (result != null) {
			return result;
		}
		BindingCollector visitor = new BindingCollector(getLinkageImpl(), name,
				IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, false, true);
		getIndex().accept(visitor);
		result = visitor.getBindings();
		pdom.putCachedResult(key, result);
		return result;
	}

	public boolean isFullyCached() throws DOMException {
		return true;
	}

	@Override
	public boolean mayHaveChildren() {
		return true;
	}

	public IBinding[] getMemberBindings() throws DOMException {
		IBinding[] result = null;
		final List<PDOMNode> preresult = new ArrayList<PDOMNode>();
		try {
			getIndex().accept(new IBTreeVisitor() {
				public int compare(int record) throws CoreException {
					return 0;
				}
				public boolean visit(int record) throws CoreException {
					preresult.add(getLinkageImpl().getNode(record));
					return true;
				}
			});
			result = preresult.toArray(new IBinding[preresult.size()]);
		} catch (CoreException ce) {
			CCorePlugin.log(ce);
		}
		return result;
	}
	
	public void addUsingDirective(ICPPUsingDirective directive) throws DOMException { fail(); }
	
	public IIndexBinding getScopeBinding() {
		return this;
	}

    @Override
	public String toString() {
    	String[] names = getQualifiedName();
    	if (names.length == 0) {
    		return "<unnamed namespace>"; //$NON-NLS-1$
    	}
    	return ASTStringUtil.join(names, String.valueOf(Keywords.cpCOLONCOLON));
	}
}
