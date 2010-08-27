/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer (QNX) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.db.TypeMarshalBuffer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class represents a collection of symbols that can be linked together at
 * link time. These are generally global symbols specific to a given language.
 */
public abstract class PDOMLinkage extends PDOMNamedNode implements IIndexLinkage, IIndexBindingConstants {

	// record offsets
	private static final int ID_OFFSET   = PDOMNamedNode.RECORD_SIZE + 0;
	private static final int NEXT_OFFSET = PDOMNamedNode.RECORD_SIZE + 4;
	private static final int INDEX_OFFSET = PDOMNamedNode.RECORD_SIZE + 8;
	private static final int NESTED_BINDINGS_INDEX = PDOMNamedNode.RECORD_SIZE + 12;
	private static final int MACRO_BTREE = PDOMNamedNode.RECORD_SIZE + 16;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 20;
	protected static final long[] FILE_LOCAL_REC_DUMMY = new long[]{0};

	// node types
	protected static final int LINKAGE= 0; // special one for myself

	private BTree fMacroIndex= null;
	private final PDOM fPDOM;
	private final Database fDatabase;

	public PDOMLinkage(PDOM pdom, long record) {
		super(null, record);
		fPDOM= pdom;
		fDatabase= pdom.getDB();
	}

	protected PDOMLinkage(PDOM pdom, String languageId, char[] name) throws CoreException {
		super(pdom.getDB(), name);
		final Database db= pdom.getDB();

		fPDOM= pdom;
		fDatabase= db;
		db.putRecPtr(record + ID_OFFSET, db.newString(languageId).getRecord());
		pdom.insertLinkage(this);
	}
	
	@Override
	public final PDOM getPDOM() {
		return fPDOM;
	}
	
	@Override
	public final PDOMLinkage getLinkage() {
		return this;
	}
	
	@Override
	public final Database getDB()  {
		return fDatabase;
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return LINKAGE;
	}

	public static IString getLinkageID(PDOM pdom, long record) throws CoreException {
		Database db = pdom.getDB();
		long namerec = db.getRecPtr(record + ID_OFFSET);
		return db.getString(namerec);
	}

	public static long getNextLinkageRecord(PDOM pdom, long record) throws CoreException {
		return pdom.getDB().getRecPtr(record + NEXT_OFFSET);
	}

	public void setNext(long nextrec) throws CoreException {
		getDB().putRecPtr(record + NEXT_OFFSET, nextrec);
	}

	public BTree getIndex() throws CoreException {
		return new BTree(getDB(), record + INDEX_OFFSET, getIndexComparator());
	}

	/**
	 * Returns the BTree for the nested bindings.
	 * @throws CoreException
	 */
	public BTree getNestedBindingsIndex() throws CoreException {
		return new BTree(fDatabase, record + NESTED_BINDINGS_INDEX, getNestedBindingsComparator());
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


	@Override
	public void addChild(PDOMNode child) throws CoreException {
		getIndex().insert(child.getRecord());
	}
	
	public final PDOMBinding getBinding(long record) throws CoreException {
		final PDOMNode node= getNode(record);
		if (node instanceof PDOMBinding)
			return (PDOMBinding) node;
		return null;
	}

	public final PDOMNode getNode(long record) throws CoreException {
		if (record == 0) {
			return null;
		}
		final int nodeType= PDOMNode.getNodeType(fDatabase, record);
		switch (nodeType) {
		case LINKAGE:
			return null;
		}
		return getNode(record, nodeType);
	}

	abstract public PDOMNode getNode(long record, int nodeType) throws CoreException;

	public abstract IBTreeComparator getIndexComparator();

	public IBTreeComparator getNestedBindingsComparator() {
		return new FindBinding.NestedBindingsBTreeComparator(this);
	}

	protected boolean cannotAdapt(final IBinding inputBinding) throws CoreException {
		if (inputBinding == null || inputBinding instanceof IProblemBinding || inputBinding instanceof IParameter) {
			return true;
		}
		if (inputBinding instanceof PDOMBinding) {
			PDOMBinding pdomBinding = (PDOMBinding) inputBinding;
			if (pdomBinding.getPDOM() != getPDOM() && pdomBinding.isFileLocal()) {
				return true;
			}
		}
		return false;
	}
	
	protected final PDOMBinding attemptFastAdaptBinding(final IBinding binding) throws CoreException {
		if (binding instanceof PDOMBinding) {
			// there is no guarantee, that the binding is from the same PDOM object.
			PDOMBinding pdomBinding = (PDOMBinding) binding;
			if (pdomBinding.getPDOM() == getPDOM()) {
				return pdomBinding;
			}
		}
		return (PDOMBinding) fPDOM.getCachedResult(binding);
	}
	public abstract PDOMBinding adaptBinding(IBinding binding) throws CoreException;
	public abstract PDOMBinding addBinding(IASTName name) throws CoreException;

	final protected long getLocalToFileRec(PDOMNode parent, IBinding binding, PDOMBinding glob) throws CoreException {
		long rec= 0;
		if (parent instanceof PDOMBinding) {
			rec= ((PDOMBinding) parent).getLocalToFileRec();
		}
		if (rec == 0) {
			PDOMFile file= getLocalToFile(binding, glob);
			if (file != null) {
				rec= file.getRecord();
			}
		}
		return rec;
	}

	protected PDOMFile getLocalToFile(IBinding binding, PDOMBinding glob) throws CoreException {
		if (fPDOM instanceof WritablePDOM) {
			final WritablePDOM wpdom= (WritablePDOM) fPDOM;
			if (binding instanceof IField) {
				return null;
			}
			boolean checkIfInSourceOnly= false;
			boolean requireDefinition= false;
			if (binding instanceof IVariable) {
				if (!(binding instanceof IField)) {
					checkIfInSourceOnly= ((IVariable) binding).isStatic();
				}
			} else if (binding instanceof IFunction) {
				IFunction f= (IFunction) binding;
				checkIfInSourceOnly= ASTInternal.isStatic(f, false);
			} else if (binding instanceof ITypedef || binding instanceof ICompositeType || binding instanceof IEnumeration) {
				checkIfInSourceOnly= true;
				requireDefinition= true;
			}

			if (checkIfInSourceOnly) {
				String path= ASTInternal.getDeclaredInSourceFileOnly(binding, requireDefinition, glob);
				if (path != null) {
					return wpdom.getFileForASTPath(getLinkageID(), path);
				}
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
					pdomName.setIsBaseSpecifier();
				}
			}
		}
	}

	/**
	 * Callback informing the linkage that a name is about to be deleted. This is
	 * used to do additional processing, like removing inheritance relationships.
	 * @param name the name that is about to be deleted
	 * @throws CoreException 
	 * @since 4.0
	 */
	public void onDeleteName(PDOMName name) throws CoreException {
	}

	/**
	 * Callback informing the linkage that a binding has been added. Used to index nested bindings.
	 * @param pdomBinding
	 * @throws CoreException
	 * @since 4.0.1
	 */
	protected final void insertIntoNestedBindingsIndex(PDOMBinding pdomBinding) throws CoreException {
		if (pdomBinding.getParentNodeRec() != record) {
			getNestedBindingsIndex().insert(pdomBinding.getRecord());
		}
	}

	/**
	 * Call-back informing the linkage that a binding is about to be removed. Used to index nested bindings.
	 * @param pdomBinding
	 * @throws CoreException
	 * @since 4.0.1
	 */
	public void beforeRemoveBinding(PDOMBinding pdomBinding) throws CoreException {
		if (pdomBinding.getParentNodeRec() != record) {
			getNestedBindingsIndex().delete(pdomBinding.getRecord());
		}
	}

	public ICPPUsingDirective[] getUsingDirectives(PDOMFile file) throws CoreException {
		return ICPPUsingDirective.EMPTY_ARRAY;
	}
	
	public BTree getMacroIndex() {
		if (fMacroIndex == null) {
			fMacroIndex= new BTree(getDB(), record + MACRO_BTREE, new FindBinding.MacroBTreeComparator(fDatabase));
		}
		return fMacroIndex;
	}

	public PDOMMacroContainer findMacroContainer(final char[] name) throws CoreException {
		return findMacroContainer(name, fPDOM.createKeyForCache(record, name));
	}

	private PDOMMacroContainer findMacroContainer(final char[] name, final String key) throws CoreException {
		Object result= fPDOM.getCachedResult(key);
		if (result instanceof PDOMMacroContainer) {
			return ((PDOMMacroContainer) result);
		}
		assert result==null;
		
		MacroContainerFinder visitor = new MacroContainerFinder(this, name);
		getMacroIndex().accept(visitor);
		PDOMMacroContainer container= visitor.getMacroContainer();
		if (container != null) {
			fPDOM.putCachedResult(key, container);
		}
		return container;
	}

	public PDOMMacroContainer getMacroContainer(char[] name) throws CoreException {
		String key= fPDOM.createKeyForCache(record, name);
		PDOMMacroContainer result= findMacroContainer(name, key);
		if (result == null) {
			result= new PDOMMacroContainer(this, name);
			getMacroIndex().insert(result.getRecord());
			fPDOM.putCachedResult(key, result);
		}
		return result;
	}

	public void removeMacroContainer (PDOMMacroContainer container) throws CoreException {
		String key= fPDOM.createKeyForCache(record, container.getNameCharArray());
		fPDOM.putCachedResult(key, null);
		getMacroIndex().delete(container.getRecord());
	}

	/**
	 * For debugging purposes, only.
	 */
	@Override
	public String toString() {
		return getLinkageName();
	}

	/**
	 * Usually bindings are added on behalf of a name, only. For unknown values we need to 
	 * add further bindings.
	 * @throws CoreException 
	 */
	public PDOMBinding addUnknownValue(IBinding binding) throws CoreException {
		return null;
	}

	/**
	 * Returns the list of global bindings for the given name.
	 * @throws CoreException 
	 */
	public PDOMBinding[] getBindingsViaCache(char[] name, IProgressMonitor monitor) throws CoreException {
		CharArrayMap<PDOMBinding[]> map = getBindingMap();
		synchronized(map) {
			PDOMBinding[] result= map.get(name);
			if (result != null)
				return result;
		}
		
		BindingCollector visitor = new BindingCollector(this, name, null, false, true);
		visitor.setMonitor(monitor);
		getIndex().accept(visitor);
		PDOMBinding[] result= visitor.getBindings();
		synchronized(map) {
			map.put(name, result);
		}
		return result;
	}
	
	private CharArrayMap<PDOMBinding[]> getBindingMap() {
		final Long key= getRecord();
		final PDOM pdom = getPDOM();
		@SuppressWarnings("unchecked")
		Reference<CharArrayMap<PDOMBinding[]>> cached= (Reference<CharArrayMap<PDOMBinding[]>>) pdom.getCachedResult(key);
		CharArrayMap<PDOMBinding[]> map= cached == null ? null : cached.get();
		if (map == null) {
			map= new CharArrayMap<PDOMBinding[]>();
			pdom.putCachedResult(key, new SoftReference<CharArrayMap<?>>(map));
		}
		return map;
	}

	public abstract PDOMBinding addTypeBinding(IBinding type) throws CoreException;
	public abstract IType unmarshalType(ITypeMarshalBuffer buffer) throws CoreException;

	public void storeType(long offset, IType type) throws CoreException {
		final Database db= getDB();
		deleteType(db, offset);
		storeType(db, offset, type);
	}

	private void storeType(Database db, long offset, IType type) throws CoreException {
		if (type != null) {
			TypeMarshalBuffer bc= new TypeMarshalBuffer(this);
			bc.marshalType(type);
			int len= bc.getPosition();
			if (len > 0) {
				if (len <= Database.TYPE_SIZE) {
					db.putBytes(offset, bc.getBuffer(), len);
				} else if (len <= Database.MAX_MALLOC_SIZE-2){
					long ptr= db.malloc(len+2);
					db.putShort(ptr, (short) len);
					db.putBytes(ptr+2, bc.getBuffer(), len);
					db.putByte(offset, TypeMarshalBuffer.INDIRECT_TYPE);
					db.putRecPtr(offset+2, ptr);
				}
			}
		}
	}

	private void deleteType(Database db, long offset) throws CoreException {
		byte firstByte= db.getByte(offset);
		if (firstByte == TypeMarshalBuffer.INDIRECT_TYPE) {
			long ptr= db.getRecPtr(offset+2);
			clearType(db, offset);
			db.free(ptr);
		} else {
			clearType(db, offset);
		}
	}

	private void clearType(Database db, long offset) throws CoreException {
		db.clearBytes(offset, Database.TYPE_SIZE);
	}

	public IType loadType(long offset) throws CoreException {
		final Database db= getDB();
		final byte firstByte= db.getByte(offset);
		byte[] data= null;
		switch(firstByte) {
		case TypeMarshalBuffer.INDIRECT_TYPE:
			long ptr= db.getRecPtr(offset+2);
			int len= db.getShort(ptr) & 0xffff;
			data= new byte[len];
			db.getBytes(ptr+2, data);
			break;
		case TypeMarshalBuffer.NULL_TYPE:
			break;
		default:
			data= new byte[Database.TYPE_SIZE];
			db.getBytes(offset, data);
			break;
		}
			
		if (data != null) {
			return new TypeMarshalBuffer(this, data).unmarshalType();
		}
		return null;
	}

	public IIndexScope[] getInlineNamespaces() {
		return IIndexScope.EMPTY_INDEX_SCOPE_ARRAY;
	}
}
