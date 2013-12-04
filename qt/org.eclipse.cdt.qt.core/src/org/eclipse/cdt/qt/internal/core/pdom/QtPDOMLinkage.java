/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.pdom;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.dom.FindBinding;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPDOMLinkage extends PDOMLinkage {

	private static int offsetInitializer = PDOMLinkage.RECORD_SIZE;
	private static enum Field {
		Version(Database.INT_SIZE),
		Last(0);

		private final int offset;

		private Field(int sizeof) {
			this.offset = offsetInitializer;
			offsetInitializer += sizeof;
		}

		public long getRecord(long baseRec) {
			return baseRec + offset;
		}
	}

	// The version that has been read from/written to the persisted file.
	private int version;

	private final Map<IQtASTName, PDOMBinding> cache = new HashMap<IQtASTName, PDOMBinding>();

	public QtPDOMLinkage(PDOM pdom, long record) throws CoreException {
		super(pdom, record);

		version = pdom.getDB().getInt(Field.Version.getRecord(record));
	}

	protected QtPDOMLinkage(PDOM pdom) throws CoreException {
		super(pdom, ILinkage.QT_LINKAGE_NAME, ILinkage.QT_LINKAGE_NAME.toCharArray());

		// Initialize the version with whatever is current.
		version = QtPDOMNodeType.VERSION;
		pdom.getDB().putInt(Field.Version.getRecord(record), version);
	}

	public int getVersion() {
		return version;
	}

	@Override
	public String getLinkageName() {
		return ILinkage.QT_LINKAGE_NAME;
	}

	@Override
	public int getLinkageID() {
		return ILinkage.QT_LINKAGE_ID;
	}

	@Override
	public PDOMNode getNode(long record, int nodeType) throws CoreException {
		return QtPDOMNodeType.load(this, nodeType, record);
	}

	@Override
	public IBTreeComparator getIndexComparator() {
		return new FindBinding.DefaultBindingBTreeComparator(this);
	}

	// IBinding#getAdapter cannot create an instance of PDOMBinding because the Linkage is required.  This
	// utility method uses #getAdapter to see if an instance has already been create.  If not then a new
	// is created and stored in the AST binding.
	@Override
	public PDOMBinding adaptBinding(IBinding binding, boolean includeLocal) throws CoreException {
		if (binding == null)
			return null;

		// If a binding has already been persisted for this instance then return it now.
		QtPDOMBinding pdomBinding = (QtPDOMBinding) binding.getAdapter(QtPDOMBinding.class);
		if (pdomBinding != null
		 && pdomBinding.getLinkage() == this)
			return pdomBinding;

		// If a PDOMBinding was created, then add it to the linkage before returning it.
		if (pdomBinding != null) {
			addChild(pdomBinding);
			return pdomBinding;
		}

		// Otherwise fall back to looking in the C++ linkage.
		return getPDOM().getLinkage(ILinkage.CPP_LINKAGE_ID).adaptBinding(binding);
	}

	public long getCPPRecord(IASTName cppName) throws CoreException {

		if (cppName == null)
			return 0;

		IBinding binding = getPDOM().findBinding(cppName);
		if (binding == null)
			return 0;

		IPDOMBinding pdomBinding = (IPDOMBinding) binding.getAdapter(IPDOMBinding.class);
		if (pdomBinding == null)
			return 0;

		if (pdomBinding.getLinkage() == null
		 || pdomBinding.getLinkage().getLinkageID() != ILinkage.CPP_LINKAGE_ID)
			return 0;

		return pdomBinding.getRecord();
	}

	/**
	 * Return the PDOMBinding for the given Qt name creating a new binding if needed.  The
	 * implementation caches the result using the name instance as the key.  This ensures
	 * one-to-one uniqueness between AST names and PDOMBindings.
	 * <p>
	 * This method is not thread-safe.
	 */
	public PDOMBinding getBinding(IQtASTName qtAstName) throws CoreException {
		// The Qt implementation ensures uniqueness by creating only a single instance of
		// the IASTName for each thing that should create a single instance in the PDOM.
		// This will work as long as all Qt elements are updated at once, which is currently
		// the case.
		//
		// I don't think this needs to be thread-safe, because things are only added from
		// the single indexer task.
		PDOMBinding pdomBinding = null;
		pdomBinding = cache.get(qtAstName);
		if (pdomBinding != null)
			return pdomBinding;

		// The result is cached even when null is returned.
		pdomBinding = qtAstName.createPDOMBinding(this);
		cache.put(qtAstName, pdomBinding);

		// Only add children that are actually created.
		if (pdomBinding != null)
			addChild(pdomBinding);

		return pdomBinding;
	}

	@Override
	public PDOMBinding addBinding(IASTName name) throws CoreException {

		// The Qt linkage is able to reference elements in other linkages.  This implementation
		// needs to decide if the binding associated with this name is from the Qt linkage or
		// from one of those external references.

		if (name == null)
			return null;

		if (name instanceof IQtASTName)
			return getBinding((IQtASTName) name);

		IBinding binding = name.getBinding();
		if (binding == null)
			return null;

		// Use the receiving linkage by default, and override only if the binding is found to
		// have a linkage with a different id.
		PDOMLinkage pdomLinkage = this;
		ILinkage linkage = binding.getLinkage();
		if (linkage != null
		 && linkage.getLinkageID() != getLinkageID())
			pdomLinkage = getPDOM().getLinkage(linkage.getLinkageID());

		// Handle bindings in unknown linkages as though the name is to be added to this linkage.
		return (pdomLinkage == null ? this : pdomLinkage).adaptBinding(binding);
	}

	@Override
	public int getBindingType(IBinding binding) {
		return binding instanceof QtPDOMBinding ? ((QtPDOMBinding) binding).getNodeType() : 0;
	}

	@Override
	public PDOMBinding addTypeBinding(IBinding binding) throws CoreException {
		throw new CoreException(QtPlugin.error("Qt Linkage does not manage types")); //$NON-NLS-1$
	}

	@Override
	public IType unmarshalType(ITypeMarshalBuffer buffer) throws CoreException {
		throw new CoreException(QtPlugin.error("Qt Linkage does not marshal types")); //$NON-NLS-1$
	}

	@Override
	public IBinding unmarshalBinding(ITypeMarshalBuffer buffer) throws CoreException {
		throw new CoreException(QtPlugin.error("Qt Linkage does not marshal bindings")); //$NON-NLS-1$
	}

	@Override
	public ISerializableEvaluation unmarshalEvaluation(ITypeMarshalBuffer typeMarshalBuffer) throws CoreException {
		throw new CoreException(QtPlugin.error("Qt Linkage does not marshal evaluations")); //$NON-NLS-1$
	}
}
