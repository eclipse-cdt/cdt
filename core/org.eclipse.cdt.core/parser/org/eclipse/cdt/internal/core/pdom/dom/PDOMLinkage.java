/*******************************************************************************
 * Copyright (c) 2005, 2012 QNX Software Systems and others.
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
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *     Sergey Prigogin (Google)
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
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexLinkage;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The top-level node in the PDOM storage format.  A linkage is a collection of nodes
 * that can be linked with references.  Several linkages can be created for an input AST.
 *
 * TODO Move this to a public interface and discuss the extension point (that already exists).
 */
public abstract class PDOMLinkage extends PDOMNamedNode implements IIndexLinkage, IIndexBindingConstants {
	// Record offsets.
	private static final int ID_OFFSET   = PDOMNamedNode.RECORD_SIZE + 0;
	private static final int NEXT_OFFSET = PDOMNamedNode.RECORD_SIZE + 4;
	private static final int INDEX_OFFSET = PDOMNamedNode.RECORD_SIZE + 8;
	private static final int NESTED_BINDINGS_INDEX = PDOMNamedNode.RECORD_SIZE + 12;
	private static final int MACRO_BTREE = PDOMNamedNode.RECORD_SIZE + 16;

	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNamedNode.RECORD_SIZE + 20;
	protected static final long[] FILE_LOCAL_REC_DUMMY = new long[] { 0 };

	private BTree fMacroIndex= null;  // No need for volatile, all fields of BTree are final.
	private final PDOM fPDOM;
	private final Database fDatabase;

	public PDOMLinkage(PDOM pdom, long record) {
		super(null, record);
		fPDOM= pdom;
		fDatabase= pdom.getDB();
	}

	protected PDOMLinkage(PDOM pdom, String linkageID, char[] name) throws CoreException {
		super(pdom.getDB(), name);
		final Database db= pdom.getDB();

		fPDOM= pdom;
		fDatabase= db;
		db.putRecPtr(record + ID_OFFSET, db.newString(linkageID).getRecord());
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
		return IIndexBindingConstants.LINKAGE;
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
				@Override
				public int compare(long record) throws CoreException {
					return 0;
				}
				@Override
				public boolean visit(long record) throws CoreException {
					PDOMNode node= PDOMNode.load(fPDOM, record);
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
		final PDOMNode node= PDOMNode.load(fPDOM, record);
		if (node instanceof PDOMBinding)
			return (PDOMBinding) node;
		return null;
	}

	public abstract PDOMNode getNode(long record, int nodeType) throws CoreException;

	public abstract IBTreeComparator getIndexComparator();

	public abstract PDOMGlobalScope getGlobalScope();

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
		PDOMBinding pdomBinding= binding.getAdapter(PDOMBinding.class);
		// There is no guarantee, that the binding is from the same PDOM object.
		if (pdomBinding != null && pdomBinding.getPDOM() == getPDOM()) {
			return pdomBinding;
		}
		return (PDOMBinding) fPDOM.getCachedResult(binding);
	}

	public final PDOMBinding adaptBinding(IBinding binding) throws CoreException {
		return adaptBinding(binding, true);
	}
	
	public abstract PDOMBinding adaptBinding(IBinding binding, boolean includeLocal) throws CoreException;

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
				IASTNode node= ASTInternal.getDeclaredInSourceFileOnly(getPDOM(), binding, requireDefinition, glob);
				if (node != null) {
					return wpdom.getFileForASTNode(getLinkageID(), node);
				}
			}
		}
		return null;
	}

	/**
	 * Return an identifier that uniquely identifies the given binding within this linkage.  The
	 * value cannot be used for global comparison because it does not include enough information
	 * to globally identify the binding (across all linkages).
	 */
	public abstract int getBindingType(IBinding binding);

	/**
	 * Return an identifier that would globally identifies the given binding if it were to be
	 * added to this linkage.  This value can be used for comparison with the result of
	 * {@link PDOMNode#getNodeId(Database, long)}.
	 */
	public int getBindingId(IBinding binding) {
		return PDOMNode.getNodeId(getLinkageID(), getBindingType(binding));
	}

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

	public void removeMacroContainer(PDOMMacroContainer container) throws CoreException {
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
	 * Returns the list of global bindings for the given name.
	 * @throws CoreException 
	 */
	public PDOMBinding[] getBindingsViaCache(char[] name, IProgressMonitor monitor) throws CoreException {
		CharArrayMap<PDOMBinding[]> map = getBindingMap();
		synchronized (map) {
			PDOMBinding[] result= map.get(name);
			if (result != null)
				return result;
		}
		
		BindingCollector visitor = new BindingCollector(this, name, null, false, false, true);
		visitor.setMonitor(monitor);
		getIndex().accept(visitor);
		PDOMBinding[] result= visitor.getBindings();
		synchronized (map) {
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

	public abstract PDOMBinding addTypeBinding(IBinding binding) throws CoreException;
	public abstract IType unmarshalType(ITypeMarshalBuffer buffer) throws CoreException;
	public abstract IBinding unmarshalBinding(ITypeMarshalBuffer buffer) throws CoreException;
	public abstract ISerializableEvaluation unmarshalEvaluation(ITypeMarshalBuffer typeMarshalBuffer) throws CoreException;

	public void storeType(long offset, IType type) throws CoreException {
		final Database db= getDB();
		deleteType(db, offset);
		storeType(db, offset, type);
	}

	private void storeType(Database db, long offset, IType type) throws CoreException {
		if (type != null) {
			TypeMarshalBuffer bc= new TypeMarshalBuffer(this);
			bc.marshalType(type);
			storeBuffer(db, offset, bc, Database.TYPE_SIZE);
		}
	}

	private void storeBuffer(Database db, long offset, TypeMarshalBuffer buf, int maxInlineSize) throws CoreException {
		int len= buf.getPosition();
		if (len > 0) {
			if (len <= maxInlineSize) {
				db.putBytes(offset, buf.getBuffer(), len);
			} else {
				db.putByte(offset, TypeMarshalBuffer.INDIRECT_TYPE);
				long chainOffset = offset + 1;
				buf.putInt(len);
				int lenSize = buf.getPosition() - len;
				int bufferPos = 0;
				while (bufferPos < len) {
					int chunkLength = bufferPos == 0 ? len + lenSize : len - bufferPos;
					boolean chainingRequired = false;
					if (chunkLength > Database.MAX_MALLOC_SIZE) {
						chunkLength = Database.MAX_MALLOC_SIZE;
						chainingRequired = true;
					}
					long ptr = db.malloc(chunkLength);
					db.putRecPtr(chainOffset, ptr);
					if (bufferPos == 0) {
						// Write length.
						db.putBytes(ptr, buf.getBuffer(), len, lenSize);
						ptr += lenSize;
						chunkLength -= lenSize;
					}
					if (chainingRequired) {
						// Reserve space for the chaining pointer.
						chainOffset = ptr;
						ptr += Database.PTR_SIZE;
						chunkLength -= Database.PTR_SIZE;
					}
					db.putBytes(ptr, buf.getBuffer(), bufferPos, chunkLength);
					bufferPos += chunkLength;
				}
				buf.setPosition(len); // Restore buffer position.
			}
		}
	}

	private byte[] loadLinkedSerializedData(final Database db, long offset) throws CoreException {
		long ptr= db.getRecPtr(offset);
		// Read the length in variable-length base-128 encoding, see ITypeMarshalBuffer.putInt(int).
		int pos = 0;
		int b = db.getByte(ptr + pos++);
		int len = b & 0x7F;
		for (int shift = 7; (b & 0x80) != 0; shift += 7) {
			b = db.getByte(ptr + pos++);
			len |= (b & 0x7F) << shift;
		}

		byte[] data= new byte[len];
		int bufferPos = 0;
		while (bufferPos < len) {
			int chunkLength = len + pos - bufferPos;
			long chunkPtr = ptr + pos;
			if (chunkLength > Database.MAX_MALLOC_SIZE) {
				chunkLength = Database.MAX_MALLOC_SIZE;
				ptr= db.getRecPtr(chunkPtr);
				chunkPtr += Database.PTR_SIZE;
				chunkLength -= Database.PTR_SIZE;
			}
			chunkLength -= pos; 
			db.getBytes(chunkPtr, data, bufferPos, chunkLength);
			bufferPos += chunkLength;
			pos = 0;
		}
		return data;
	}

	private void deleteSerializedData(Database db, long offset, int maxInlineSize) throws CoreException {
		byte firstByte= db.getByte(offset);
		if (firstByte == TypeMarshalBuffer.INDIRECT_TYPE) {
			long chunkPtr= db.getRecPtr(offset + 1);
			long ptr = chunkPtr;
			// Read the length in variable-length base-128 encoding, see ITypeMarshalBuffer.putInt(int).
			int b = db.getByte(ptr++);
			int len = b & 0x7F;
			for (int shift = 7; (b & 0x80) != 0; shift += 7) {
				b = db.getByte(ptr++);
				len |= (b & 0x7F) << shift;
			}
			
			len += ptr - chunkPtr;
			while (len > 0) {
				int chunkLength = len;
				if (chunkLength > Database.MAX_MALLOC_SIZE) {
					chunkLength = Database.MAX_MALLOC_SIZE;
					ptr= db.getRecPtr(ptr);
					chunkLength -= Database.PTR_SIZE;
				}
				db.free(chunkPtr);
				chunkPtr = ptr;
				len -= chunkLength;
			}
		}
		db.clearBytes(offset, maxInlineSize);
	}

	private void deleteType(Database db, long offset) throws CoreException {
		deleteSerializedData(db, offset, Database.TYPE_SIZE);
	}

	public IType loadType(long offset) throws CoreException {
		final Database db= getDB();
		final byte firstByte= db.getByte(offset);
		byte[] data= null;
		switch (firstByte) {
		case TypeMarshalBuffer.INDIRECT_TYPE:
			data = loadLinkedSerializedData(db, offset + 1);			
			break;
		case TypeMarshalBuffer.UNSTORABLE_TYPE:
			return TypeMarshalBuffer.UNSTORABLE_TYPE_PROBLEM;
		case TypeMarshalBuffer.NULL_TYPE:
			return null;
		default:
			data= new byte[Database.TYPE_SIZE];
			db.getBytes(offset, data);
			break;
		}
		return new TypeMarshalBuffer(this, data).unmarshalType();
	}

	public void storeBinding(long offset, IBinding binding) throws CoreException {
		final Database db= getDB();
		deleteBinding(db, offset);
		storeBinding(db, offset, binding);
	}

	private void storeBinding(Database db, long offset, IBinding binding) throws CoreException {
		if (binding != null) {
			TypeMarshalBuffer bc= new TypeMarshalBuffer(this);
			bc.marshalBinding(binding);
			storeBuffer(db, offset, bc, Database.TYPE_SIZE);
		}
	}

	private void deleteBinding(Database db, long offset) throws CoreException {
		deleteSerializedData(db, offset, Database.TYPE_SIZE);
	}

	public IBinding loadBinding(long offset) throws CoreException {
		final Database db= getDB();
		final byte firstByte= db.getByte(offset);
		byte[] data= null;
		switch (firstByte) {
		case TypeMarshalBuffer.INDIRECT_TYPE:
			data = loadLinkedSerializedData(db, offset + 1);			
			break;
		case TypeMarshalBuffer.UNSTORABLE_TYPE:
			return new ProblemBinding(null, ISemanticProblem.TYPE_NOT_PERSISTED);
		case TypeMarshalBuffer.NULL_TYPE:
			return null;
		default:
			data= new byte[Database.TYPE_SIZE];
			db.getBytes(offset, data);
			break;
		}
		return new TypeMarshalBuffer(this, data).unmarshalBinding();
	}

	public void storeTemplateArgument(long offset, ICPPTemplateArgument arg) throws CoreException {
		final Database db= getDB();
		deleteArgument(db, offset);
		storeArgument(db, offset, arg);
	}

	private void storeArgument(Database db, long offset, ICPPTemplateArgument arg) throws CoreException {
		if (arg != null) {
			TypeMarshalBuffer bc= new TypeMarshalBuffer(this);
			bc.marshalTemplateArgument(arg);
			storeBuffer(db, offset, bc, Database.ARGUMENT_SIZE);
		}
	}

	private void deleteArgument(Database db, long offset) throws CoreException {
		deleteSerializedData(db, offset, Database.ARGUMENT_SIZE);
	}

	public ICPPTemplateArgument loadTemplateArgument(long offset) throws CoreException {
		final Database db= getDB();
		final byte firstByte= db.getByte(offset);
		byte[] data= null;
		switch (firstByte) {
		case TypeMarshalBuffer.INDIRECT_TYPE:
			data = loadLinkedSerializedData(db, offset + 1);			
			break;
		case TypeMarshalBuffer.UNSTORABLE_TYPE:
		case TypeMarshalBuffer.NULL_TYPE:
			return null;
		default:
			data= new byte[Database.ARGUMENT_SIZE];
			db.getBytes(offset, data);
			break;
		}
		return new TypeMarshalBuffer(this, data).unmarshalTemplateArgument();
	}

	public void storeValue(long offset, IValue value) throws CoreException {
		final Database db= getDB();
		deleteValue(db, offset);
		storeValue(db, offset, value);
	}
	
	private void storeValue(Database db, long offset, IValue value) throws CoreException {
		if (value != null) {
			TypeMarshalBuffer bc= new TypeMarshalBuffer(this);
			bc.marshalValue(value);
			storeBuffer(db, offset, bc, Database.VALUE_SIZE);
		}
	}

	private void deleteValue(Database db, long offset) throws CoreException {
		deleteSerializedData(db, offset, Database.VALUE_SIZE);
	}

	public IValue loadValue(long offset) throws CoreException {
		TypeMarshalBuffer buffer = loadBuffer(offset, Database.VALUE_SIZE);
		if (buffer == null)
			return null;
		return buffer.unmarshalValue();
	}

	public void storeEvaluation(long offset, ISerializableEvaluation eval) throws CoreException {
		final Database db= getDB();
		deleteEvaluation(db, offset);
		storeEvaluation(db, offset, eval);
	}
	
	private void storeEvaluation(Database db, long offset, ISerializableEvaluation eval) throws CoreException {
		if (eval != null) {
			TypeMarshalBuffer bc= new TypeMarshalBuffer(this);
			bc.marshalEvaluation(eval, true);
			storeBuffer(db, offset, bc, Database.EVALUATION_SIZE);
		}
	}

	private void deleteEvaluation(Database db, long offset) throws CoreException {
		deleteSerializedData(db, offset, Database.EVALUATION_SIZE);
	}

	public ISerializableEvaluation loadEvaluation(long offset) throws CoreException {
		TypeMarshalBuffer buffer = loadBuffer(offset, Database.EVALUATION_SIZE);
		if (buffer == null)
			return null;
		return buffer.unmarshalEvaluation();
	}

	private TypeMarshalBuffer loadBuffer(long offset, int size) throws CoreException {
		final Database db= getDB();
		final byte firstByte= db.getByte(offset);
		byte[] data;
		switch (firstByte) {
		case TypeMarshalBuffer.INDIRECT_TYPE:
			data = loadLinkedSerializedData(db, offset + 1);			
			break;
		case TypeMarshalBuffer.NULL_TYPE:
			return null;
		default:
			data= new byte[size];
			db.getBytes(offset, data);
			break;
		}
		return new TypeMarshalBuffer(this, data);
	}

	public IIndexScope[] getInlineNamespaces() {
		return IIndexScope.EMPTY_INDEX_SCOPE_ARRAY;
	}
}
