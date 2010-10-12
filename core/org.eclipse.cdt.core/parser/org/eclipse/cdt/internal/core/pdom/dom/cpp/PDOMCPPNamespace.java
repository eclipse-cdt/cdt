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
 *    Andrew Ferguson (Symbian)
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
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
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents a namespace scope for a namespace stored in the index.
 */
class PDOMCPPNamespace extends PDOMCPPBinding
		implements ICPPNamespace, ICPPNamespaceScope, IIndexScope {

	private static final int INDEX_OFFSET = PDOMCPPBinding.RECORD_SIZE;
	private static final int FIRST_NAMESPACE_CHILD_OFFSET = INDEX_OFFSET + Database.PTR_SIZE;
	private static final int NEXT_NAMESPACE_SIBBLING_OFFSET = FIRST_NAMESPACE_CHILD_OFFSET + Database.PTR_SIZE;
	private static final int FLAG_OFFSET = NEXT_NAMESPACE_SIBBLING_OFFSET + Database.PTR_SIZE;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FLAG_OFFSET + 1;
	
	private static int INLINE_FLAG= 0x1;
	
	private int fFlag= -1;
	private ICPPNamespaceScope[] fInlineNamespaces;

	public PDOMCPPNamespace(PDOMLinkage linkage, PDOMNode parent, ICPPNamespace namespace) throws CoreException {
		super(linkage, parent, namespace.getNameCharArray());
		updateFlag(namespace);
	}

	public PDOMCPPNamespace(PDOMLinkage linkage, long record) throws CoreException {
		super(linkage, record);
	}
	
	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		updateFlag((ICPPNamespace) newBinding);
	}

	private void updateFlag(ICPPNamespace namespace) throws CoreException {
		int flag= 0;
		if (namespace.isInline())
			flag |= INLINE_FLAG;
		
		getDB().putByte(record + FLAG_OFFSET, (byte) flag);
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
		return new BTree(getDB(), record + INDEX_OFFSET, getLinkage().getIndexComparator());
	}

	@Override
	public void accept(final IPDOMVisitor visitor) throws CoreException {
		if (visitor instanceof IBTreeVisitor) {
			getIndex().accept((IBTreeVisitor) visitor);
		} else {
			getIndex().accept(new IBTreeVisitor() {
				public int compare(long record) throws CoreException {
					return 0;
				}
				public boolean visit(long record) throws CoreException {
					PDOMBinding binding = getLinkage().getBinding(record);
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
		final long childRec = child.getRecord();
		getIndex().insert(childRec);
		if (child instanceof PDOMCPPNamespace) {
			((PDOMCPPNamespace) child).addToList(record + FIRST_NAMESPACE_CHILD_OFFSET);
		}
	}

	public void addToList(final long listRecord) throws CoreException {
		final Database db= getDB();
		final long nextRec= db.getRecPtr(listRecord);
		db.putRecPtr(record + NEXT_NAMESPACE_SIBBLING_OFFSET, nextRec);
		db.putRecPtr(listRecord, record);
	}

	public ICPPNamespaceScope getNamespaceScope() {
		return this;
	}

	public ICPPUsingDirective[] getUsingDirectives() {
		return new ICPPUsingDirective[0];
	}

	public IBinding[] find(String name) {
		try {
			BindingCollector visitor = new BindingCollector(getLinkage(),  name.toCharArray(),
					IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, false, true);
			getIndex().accept(visitor);
			return visitor.getBindings();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return IIndexBinding.EMPTY_INDEX_BINDING_ARRAY;
	}

	@Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
		try {
			IBinding[] bindings= getBindingsViaCache(name.getLookupKey());
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
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		IBinding[] result = null;
		try {
			if (!prefixLookup) {
				result= getBindingsViaCache(name.getLookupKey());
			} else {
				BindingCollector visitor= new BindingCollector(getLinkage(), name.getLookupKey(),
						IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, prefixLookup, !prefixLookup);
				getIndex().accept(visitor);
				result = visitor.getBindings();
			}
			if (fileSet != null) {
				result= fileSet.filterFileLocalBindings(result);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	}

	private IBinding[] getBindingsViaCache(final char[] name) throws CoreException {
		final PDOM pdom = getPDOM();
		final String key= pdom.createKeyForCache(record, name);
		IBinding[] result= (IBinding[]) pdom.getCachedResult(key);
		if (result != null) {
			return result;
		}
		BindingCollector visitor = new BindingCollector(getLinkage(), name,
				IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, false, true);
		getIndex().accept(visitor);
		result = visitor.getBindings();
		pdom.putCachedResult(key, result);
		return result;
	}

	@Override
	public boolean mayHaveChildren() {
		return true;
	}

	public IBinding[] getMemberBindings() {
		IBinding[] result = null;
		final List<PDOMNode> preresult = new ArrayList<PDOMNode>();
		try {
			getIndex().accept(new IBTreeVisitor() {
				public int compare(long record) throws CoreException {
					return 0;
				}
				public boolean visit(long record) throws CoreException {
					preresult.add(getLinkage().getNode(record));
					return true;
				}
			});
			result = preresult.toArray(new IBinding[preresult.size()]);
		} catch (CoreException ce) {
			CCorePlugin.log(ce);
		}
		return result;
	}
	
	public void addUsingDirective(ICPPUsingDirective directive) { 
		throw new UnsupportedOperationException();
	}
	
	public IIndexBinding getScopeBinding() {
		return this;
	}

    @Override
	protected String toStringBase() {
    	String[] names = getQualifiedName();
    	if (names.length == 0) {
    		return "<unnamed namespace>"; //$NON-NLS-1$
    	}
    	return ASTStringUtil.join(names, String.valueOf(Keywords.cpCOLONCOLON));
	}

	public ICPPNamespaceScope[] getInlineNamespaces() {
		if (fInlineNamespaces == null) {
			List<PDOMCPPNamespace> nslist = collectInlineNamespaces(getDB(), getLinkage(), record+FIRST_NAMESPACE_CHILD_OFFSET);
			if (nslist == null) {
				fInlineNamespaces= new PDOMCPPNamespace[0];
			} else {
				fInlineNamespaces= nslist.toArray(new PDOMCPPNamespace[nslist.size()]);
			}
		}
		return fInlineNamespaces;
	}

	public static List<PDOMCPPNamespace> collectInlineNamespaces(Database db,
			PDOMLinkage linkage, long listRecord) {
		List<PDOMCPPNamespace> nslist= null;
		try {
			long rec= db.getRecPtr(listRecord);
			while (rec != 0) {
				PDOMCPPNamespace ns= new PDOMCPPNamespace(linkage, rec);
				if (ns.isInline()) {
					if (nslist == null) {
						nslist= new ArrayList<PDOMCPPNamespace>();
					}
					nslist.add(ns);
				}
				rec= db.getRecPtr(rec + NEXT_NAMESPACE_SIBBLING_OFFSET);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return nslist;
	}

	public boolean isInline() {
		if (fFlag == -1) {
			try {
				fFlag= getDB().getByte(record + FLAG_OFFSET);
			} catch (CoreException e) {
				CCorePlugin.log(e);
				fFlag= 0;
			}
		}
		return (fFlag & INLINE_FLAG) != 0;
	}
}
