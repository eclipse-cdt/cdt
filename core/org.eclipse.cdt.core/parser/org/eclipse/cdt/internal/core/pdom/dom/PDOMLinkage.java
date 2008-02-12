/*******************************************************************************
 * Copyright (c) 2005, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
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
public abstract class PDOMLinkage extends PDOMNamedNode implements IIndexLinkage, IIndexBindingConstants {

	// record offsets
	private static final int ID_OFFSET   = PDOMNamedNode.RECORD_SIZE + 0;
	private static final int NEXT_OFFSET = PDOMNamedNode.RECORD_SIZE + 4;
	private static final int INDEX_OFFSET = PDOMNamedNode.RECORD_SIZE + 8;
	private static final int NESTED_BINDINGS_INDEX = PDOMNamedNode.RECORD_SIZE + 12;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 16;

	// node types
	protected static final int LINKAGE= 0; // special one for myself

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

	/**
	 * Returns the BTree for the nested bindings.
	 * @return
	 * @throws CoreException
	 */
	public BTree getNestedBindingsIndex() throws CoreException {
		return new BTree(getPDOM().getDB(), record + NESTED_BINDINGS_INDEX, getNestedBindingsComparator());
	}

	public void accept(final IPDOMVisitor visitor) throws CoreException {
		if (visitor instanceof IBTreeVisitor) {
			getIndex().accept((IBTreeVisitor) visitor);
		} else {
			getIndex().accept(new IBTreeVisitor() {
				public int compare(int record) throws CoreException {
					return 0;
				}
				public boolean visit(int record) throws CoreException {
					PDOMNode node= getNode(record);
					if (node != null) {
						if (visitor.visit(node))
							node.accept(visitor);
						visitor.leave(node);
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
		}
		return null;
	}

	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if (type instanceof IPointerType)
			return new PDOMPointerType(pdom, parent, (IPointerType)type);
		else if (type instanceof IArrayType) 
			return new PDOMArrayType(pdom, parent, (IArrayType) type);
		else if (type instanceof IQualifierType)
			return new PDOMQualifierType(pdom, parent, (IQualifierType)type);
		else
			return null;
	}

	public abstract IBTreeComparator getIndexComparator();

	public IBTreeComparator getNestedBindingsComparator() {
		return new FindBinding.NestedBindingsBTreeComparator(this);
	}

	public abstract PDOMBinding addBinding(IASTName name) throws CoreException;

	public abstract PDOMBinding addBinding(IBinding binding, IASTName fromName) throws CoreException;

	public final PDOMBinding adaptBinding(final IBinding inputBinding) throws CoreException {
		if (inputBinding == null || inputBinding instanceof IProblemBinding) {
			return null;
		}

		IBinding binding= inputBinding;
		if (binding instanceof PDOMBinding) {
			// there is no guarantee, that the binding is from the same PDOM object.
			PDOMBinding pdomBinding = (PDOMBinding) binding;
			if (pdomBinding.getPDOM() == getPDOM()) {
				return pdomBinding;
			}
			// if the binding is from another pdom it has to be adapted. However don't adapt file-local bindings
			if (pdomBinding.isFileLocal()) {
				return null;
			}
		}

		PDOMBinding result= (PDOMBinding) pdom.getCachedResult(inputBinding);
		if (result != null) {
			return result;
		}

		// assign names to anonymous types.
		binding= PDOMASTAdapter.getAdapterForAnonymousASTBinding(binding);
		if (binding == null) {
			return null;
		}

		result= doAdaptBinding(binding);
		if (result != null) {
			pdom.putCachedResult(inputBinding, result);
		}
		return result;
	}

	protected abstract PDOMBinding doAdaptBinding(IBinding binding) throws CoreException;

	public final PDOMBinding resolveBinding(IASTName name) throws CoreException {
		IBinding binding= name.resolveBinding();
		if (binding != null) {
			return adaptBinding(binding);
		}
		return null;
	}
	
	final protected int getLocalToFileRec(PDOMNode parent, IBinding binding) throws CoreException {
		int rec= 0;
		if (parent instanceof PDOMBinding) {
			rec= ((PDOMBinding) parent).getLocalToFileRec();
		}
		if (rec == 0) {
			PDOMFile file= getLocalToFile(binding);
			if (file != null) {
				rec= file.getRecord();
			}
		}
		return rec;
	}

	protected PDOMFile getLocalToFile(IBinding binding) throws CoreException {
		if (pdom instanceof WritablePDOM) {
			final WritablePDOM wpdom= (WritablePDOM) pdom;
			try {
				if (binding instanceof IField) {
					return null;
				}
				boolean checkInSourceOnly= false;
				boolean requireDefinition= false;
				if (binding instanceof IVariable) {
					if (!(binding instanceof IField)) {
						checkInSourceOnly= ASTInternal.isStatic((IVariable) binding);
					}
				} else if (binding instanceof IFunction) {
					IFunction f= (IFunction) binding;
					checkInSourceOnly= ASTInternal.isStatic(f, false);
				} else if (binding instanceof ITypedef || binding instanceof ICompositeType || binding instanceof IEnumeration) {
					checkInSourceOnly= true;
					requireDefinition= true;
				}

				if (checkInSourceOnly) {
					String path= ASTInternal.getDeclaredInSourceFileOnly(binding, requireDefinition);
					if (path != null) {
						return wpdom.getFileForASTPath(getLinkageID(), path);
					}
				}
			} catch (DOMException e) {
			}
		}
		return null;
	}

	public abstract int getBindingType(IBinding binding);

	/**
	 * Call-back informing the linkage that a name has been added. This is
	 * used to do additional processing, like establishing inheritance relationships.
	 * @param file the file that has triggered the creation of the name
	 * @param name the name that caused the insertion
	 * @param pdomName the name that was inserted into the linkage
	 * @throws CoreException 
	 * @since 4.0
	 */
	public void onCreateName(PDOMFile file, IASTName name, PDOMName pdomName) throws CoreException {
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
	 * used to do additional processing, like removing inheritance relationships.
	 * @param pdomName the name that was inserted into the linkage
	 * @param name the name that caused the insertion
	 * @throws CoreException 
	 * @since 4.0
	 */
	public void onDeleteName(PDOMName nextName) throws CoreException {
	}

	/**
	 * Callback informing the linkage that a binding has been added. Used to index nested bindings.
	 * @param pdomBinding
	 * @throws CoreException
	 * @since 4.0.1
	 */
	public void afterAddBinding(PDOMBinding pdomBinding) throws CoreException {
		if (pdomBinding.getParentNodeRec() != record) {
			getNestedBindingsIndex().insert(pdomBinding.getRecord());
		}
	}

	/**
	 * Callback informing the linkage that a binding is about to be removed. Used to index nested bindings.
	 * @param pdomBinding
	 * @throws CoreException
	 * @since 4.0.1
	 */
	public void beforeRemoveBinding(PDOMBinding pdomBinding) throws CoreException {
		if (pdomBinding.getParentNodeRec() != record) {
			getNestedBindingsIndex().delete(pdomBinding.getRecord());
		}
	}

	public void deleteType(IType type, int ownerRec) throws CoreException {
		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			// at this point only delete types that are actually owned by the requesting party.
			if (node.getParentNodeRec() == ownerRec) {
				assert ! (node instanceof IBinding);
				node.delete(this);
			}
		}
	}

	public void deleteBinding(IBinding binding) throws CoreException {
		// no implementation, yet.
	}

	public void delete(PDOMLinkage linkage) throws CoreException {
		assert false; // no need to delete linkages.
	}

	public ICPPUsingDirective[] getUsingDirectives(PDOMFile file) throws CoreException {
		return ICPPUsingDirective.EMPTY_ARRAY;
	}
}
