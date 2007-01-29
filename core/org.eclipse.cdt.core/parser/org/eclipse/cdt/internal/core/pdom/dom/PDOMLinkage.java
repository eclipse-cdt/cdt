/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * IBM Corporation
 * Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 * This class represents a collection of symbols that can be linked together at
 * link time. These are generally global symbols specific to a given language.
 */
public abstract class PDOMLinkage extends PDOMNamedNode implements IIndexLinkage {

	// record offsets
	private static final int ID_OFFSET   = PDOMNamedNode.RECORD_SIZE + 0;
	private static final int NEXT_OFFSET = PDOMNamedNode.RECORD_SIZE + 4;
	private static final int INDEX_OFFSET = PDOMNamedNode.RECORD_SIZE + 8;
	
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 12;
	
	// node types
	protected static final int LINKAGE = 0; // special one for myself
	static final int POINTER_TYPE = 1;
	static final int ARRAY_TYPE = 2;
	static final int QUALIFIER_TYPE = 3;
	
	protected static final int LAST_NODE_TYPE = QUALIFIER_TYPE;
	
	public PDOMLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	protected PDOMLinkage(PDOM pdom, String languageId, char[] name) throws CoreException {
		super(pdom, null, name);
		Database db = pdom.getDB();

		// id
		db.putInt(record + ID_OFFSET, db.newString(languageId).getRecord());
		
		pdom.insertLinkage(this);
	}
	
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return LINKAGE;
	}

	public static IString getId(PDOM pdom, int record) throws CoreException {
		Database db = pdom.getDB();
		int namerec = db.getInt(record + ID_OFFSET);
		return db.getString(namerec);
	}
	
	public static int getNextLinkageRecord(PDOM pdom, int record) throws CoreException {
		return pdom.getDB().getInt(record + NEXT_OFFSET);
	}
		
	public void setNext(int nextrec) throws CoreException {
		pdom.getDB().putInt(record + NEXT_OFFSET, nextrec);
	}
	
	public BTree getIndex() throws CoreException {
		return new BTree(pdom.getDB(), record + INDEX_OFFSET, getIndexComparator());
	}
	
	public void accept(final IPDOMVisitor visitor) throws CoreException {
		super.accept(visitor);
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
	
	public ILinkage getLinkage() throws CoreException {
		return this;
	}

	public final void addChild(PDOMNode child) throws CoreException {
		getIndex().insert(child.getRecord());
	}
	
	public PDOMNode getNode(int record) throws CoreException {
		switch (PDOMNode.getNodeType(pdom, record)) {
		case POINTER_TYPE:
			return new PDOMPointerType(pdom, record);
		case ARRAY_TYPE:
			return new PDOMArrayType(pdom, record);
		case QUALIFIER_TYPE:
			return new PDOMQualifierType(pdom, record);
		}
		return null;
	}
	
	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		PDOMNode node;
		
		if (type instanceof IPointerType)
			node = new PDOMPointerType(pdom, parent, (IPointerType)type);
		else if (type instanceof IArrayType) 
			node= new PDOMArrayType(pdom, parent, (IArrayType) type);
		else if (type instanceof IQualifierType)
			node = new PDOMQualifierType(pdom, parent, (IQualifierType)type);
		else
			node = null;
		
		if(node!=null) {
			parent.addChild(node);
		}
		
		return node;
	}

	public abstract IBTreeComparator getIndexComparator();
	
	public abstract PDOMBinding addBinding(IASTName name) throws CoreException;
	
	public abstract PDOMBinding adaptBinding(IBinding binding) throws CoreException;
	
	public abstract PDOMBinding resolveBinding(IASTName name) throws CoreException;
	
	/**
	 * 
	 * @param binding
	 * @return <ul><li> null - skip this binding (don't add to pdom)
	 * <li>this - for filescope
	 * <li>a PDOMBinding instance - parent adapted binding
	 * </ul>
	 * @throws CoreException
	 */
	public PDOMNode getAdaptedParent(IBinding binding) throws CoreException {
		try {
		IScope scope = binding.getScope();
		if (scope == null) {
			return binding instanceof PDOMBinding ? this : null;
		}
		
		if (scope instanceof IIndexBinding) {
			return adaptBinding((IBinding) scope);
		}
			
		// the scope is from the ast
		if (scope instanceof ICPPNamespaceScope) {
			IName name= scope.getScopeName();
			if (name != null && name.toCharArray().length == 0) {
				// skip unnamed namespaces
				return null;
			}
		}
		
		IASTNode scopeNode = ASTInternal.getPhysicalNodeOfScope(scope);
		if (scopeNode instanceof IASTCompoundStatement)
			return null;
		else if (scopeNode instanceof IASTTranslationUnit)
			return this;
		else {
			IName scopeName = scope.getScopeName();
			if (scopeName instanceof IASTName) {
				IBinding scopeBinding = ((IASTName) scopeName).resolveBinding();
				PDOMBinding scopePDOMBinding = adaptBinding(scopeBinding);
				if (scopePDOMBinding != null)
					return scopePDOMBinding;
			}
		}
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
		return null;
	}
	
	public abstract int getBindingType(IBinding binding);

	public IBinding[] findInGlobalScope(char[] name) throws CoreException {
		FindBindingsInBTree visitor= new FindBindingsInBTree(this, name);
		getIndex().accept(visitor);
		
		return visitor.getBinding();
	}

	private static class PrefixedBindingFinder implements IBTreeVisitor {
		private PDOMLinkage linkage;
		private String prefix;
		private IndexFilter filter;
		private List bindings = new ArrayList();
		
		PrefixedBindingFinder(PDOMLinkage linkage, String prefix, IndexFilter filter) {
			this.linkage = linkage;
			this.prefix = prefix;
			this.filter = filter;
		}
		
		public int compare(int record) throws CoreException {
			PDOMNamedNode node = (PDOMNamedNode) linkage.getNode(record);
			IString name = node.getDBName();
			if (name.getString().startsWith(prefix)) {
				return 0;
			}
			return name.compare(prefix);
		}

		public boolean visit(int record) throws CoreException {
			PDOMBinding binding = linkage.getPDOM().getBinding(record);
			if (filter.acceptImplicitMethods() || !(binding instanceof ICPPMethod) ||
					!((ICPPMethod)binding).isImplicit()) {
				if (filter.acceptBinding(binding)) {
					bindings.add(binding);
				}
			}
			return true;
		}
		
		public IBinding[] getBindings() {
			return (IBinding[]) bindings.toArray(new IBinding[bindings.size()]);
		}
	}
	
	public IBinding[] findBindingsForPrefix(String prefix, IndexFilter filter) throws CoreException {
		PrefixedBindingFinder visitor = new PrefixedBindingFinder(this, prefix, filter);
		getIndex().accept(visitor);
		
		return visitor.getBindings();
	}
	
	/**
	 * Callback informing the linkage that a name has been added. This is
	 * used to do addtional processing, like establishing inheritance relationships.
	 * @param pdomName the name that was inserted into the linkage
	 * @param name the name that caused the insertion
	 * @throws CoreException 
	 * @since 4.0
	 */
	public void onCreateName(PDOMName pdomName, IASTName name) throws CoreException {
		IASTNode parentNode= name.getParent();
		if (parentNode instanceof IASTDeclSpecifier) {
			IASTDeclSpecifier ds= (IASTDeclSpecifier) parentNode;
			if (ds.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				if (pdomName.getEnclosingDefinitionRecord() != 0) {
					pdomName.setIsBaseSpecifier(true);
				}
			}
		}
	}

	/**
	 * Callback informing the linkage that a name is about to be deleted. This is
	 * used to do addtional processing, like removing inheritance relationships.
	 * @param pdomName the name that was inserted into the linkage
	 * @param name the name that caused the insertion
	 * @throws CoreException 
	 * @since 4.0
	 */
	public void onDeleteName(PDOMName nextName) throws CoreException {
	}
}
