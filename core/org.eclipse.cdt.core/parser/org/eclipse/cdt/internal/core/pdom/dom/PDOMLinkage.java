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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.index.composite.CompositeScope;
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
	protected static final int LINKAGE= 0; // special one for myself
	static final int POINTER_TYPE= 1;
	static final int ARRAY_TYPE= 2;
	static final int QUALIFIER_TYPE= 3;
	static final int FILE_LOCAL_SCOPE_TYPE= 4;
	
	protected static final int LAST_NODE_TYPE = FILE_LOCAL_SCOPE_TYPE;
	
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
		case FILE_LOCAL_SCOPE_TYPE:
			return new PDOMFileLocalScope(pdom, record);
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
	 * @param createFileLocalScope if <code>true</code> the creation of a file local
	 *        scope object is allowed.
	 * @return <ul><li> null - skip this binding (don't add to pdom)
	 * <li>this - for filescope
	 * <li>a PDOMBinding instance - parent adapted binding
	 * </ul>
	 * @throws CoreException
	 */
	protected PDOMNode getAdaptedParent(IBinding binding, boolean createFileLocalScope) throws CoreException {
		try {
		IScope scope = binding.getScope();
		if (scope == null) {
			if (binding instanceof IIndexBinding) {
				IIndexBinding ib= (IIndexBinding) binding;
				// don't adapt file local bindings from other fragments to this one.
				if (ib.isFileLocal()) {
					return null;
				}
				// in an index the null scope represents global scope.
				return this;
			}
			return null;
		}
		 		
		if(scope instanceof IIndexScope) {
			if(scope instanceof CompositeScope) { // we special case for performance
				return adaptBinding(((CompositeScope)scope).getRawScopeBinding());
			} else {
				return adaptBinding(((IIndexScope) scope).getScopeBinding());
			}
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
		else if (scopeNode instanceof IASTTranslationUnit) {
			if (isFileLocalBinding(binding)) {
				IASTTranslationUnit tu= (IASTTranslationUnit) scopeNode;
				return findFileLocalScope(tu.getFilePath(), createFileLocalScope);
			}
			return this;
		}
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
	
	protected abstract boolean isFileLocalBinding(IBinding binding) throws DOMException;
	public abstract int getBindingType(IBinding binding);
	
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
	
	/**
	 * Searches for the a file local scope object. If none is found depending
	 * on the value of the parameter 'create' such an object is created.
	 * @param fileName
	 * @param create
	 * @since 4.0
	 */
	final protected PDOMFileLocalScope findFileLocalScope(String fileName, boolean create) throws CoreException {
		char[] fname= fileName.toCharArray();
		int fnamestart= findFileNameStart(fname);
		StringBuffer buf= new StringBuffer();
		buf.append('{');
		buf.append(fname, fnamestart, fname.length-fnamestart);
		buf.append(':');
		buf.append(fileName.hashCode());
		buf.append('}');
		fname= buf.toString().toCharArray();

		final PDOMFileLocalScope[] fls= new PDOMFileLocalScope[] {null};
		NamedNodeCollector collector= new NamedNodeCollector(this, fname) {
			public boolean addNode(PDOMNamedNode node) {
				if (node instanceof PDOMFileLocalScope) {
					fls[0]= (PDOMFileLocalScope) node;
					return false;	// done
				}
				return true;
			}
		};
		getIndex().accept(collector);
		if (fls[0] == null && create) {
			fls[0]= new PDOMFileLocalScope(pdom, this, fname);
			addChild(fls[0]);
		}
		return fls[0];
	}

	private static int findFileNameStart(char[] fname) {
		for (int i= fname.length-2; i>=0; i--) {
			switch (fname[i]) {
			case '/':
			case '\\':
				return i+1;
			}
		}
		return 0;
	}
}
