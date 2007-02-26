/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Andrew Ferguson (Symbian)
 * Bryan Wilkinson (QNX)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPNamespace extends PDOMCPPBinding implements ICPPNamespace, ICPPNamespaceScope, IIndexScope {

	private static final int INDEX_OFFSET = PDOMBinding.RECORD_SIZE + 0;

	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 4;

	public PDOMCPPNamespace(PDOM pdom, PDOMNode parent, ICPPNamespace namespace) throws CoreException {
		super(pdom, parent, namespace.getNameCharArray());
	}

	public PDOMCPPNamespace(PDOM pdom, int record) throws CoreException {
		super(pdom, record);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return PDOMCPPLinkage.CPPNAMESPACE;
	}

	public BTree getIndex() throws CoreException {
		return new BTree(pdom.getDB(), record + INDEX_OFFSET, getLinkageImpl().getIndexComparator());
	}

	public void accept(final IPDOMVisitor visitor) throws CoreException {
		if (visitor instanceof IBTreeVisitor) {
			getIndex().accept((IBTreeVisitor) visitor);
		}
		else {
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

	public void addChild(PDOMNode child) throws CoreException {
		getIndex().insert(child.getRecord());
	}

	public ICPPNamespaceScope getNamespaceScope() throws DOMException {
		return this;
	}

	public IASTNode[] getUsingDirectives() throws DOMException {
		// TODO
		return new IASTNode[0];
	}

	public IBinding[] find(String name) {
		return find(name, false);
	}
	
	public IBinding[] find(String name, boolean prefixLookup) {
		try {
			BindingCollector visitor = new BindingCollector(getLinkageImpl(), name.toCharArray(), null, prefixLookup, !prefixLookup);
			getIndex().accept(visitor);
			return visitor.getBindings();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		try {
			BindingCollector visitor= new BindingCollector(getLinkageImpl(), name.toCharArray());
			getIndex().accept(visitor);
			
			IBinding[] bindings= visitor.getBindings();
			return CPPSemantics.resolveAmbiguities(name, bindings);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public boolean isFullyCached() throws DOMException {
		return true;
	}

	public boolean mayHaveChildren() {
		return true;
	}

	public IBinding[] getMemberBindings() throws DOMException {
		IBinding[] result = null;
		final List preresult = new ArrayList();
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
			result = (IBinding[]) preresult.toArray(new IBinding[preresult.size()]);
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		return result;
	}
	
	public void addUsingDirective(IASTNode directive) throws DOMException {fail();}
	
	public IIndexBinding getScopeBinding() {
		return this;
	}
}
