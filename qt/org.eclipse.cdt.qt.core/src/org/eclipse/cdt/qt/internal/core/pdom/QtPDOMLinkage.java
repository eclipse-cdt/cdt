/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.pdom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
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
		CppIndex(Database.PTR_SIZE),
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

	// An index of C++ -> Qt Bindings.  This is used for fast lookup of things like the QObject
	// for a C++ class in the base specifier list.  Each entry in the index is a pair like
	// { CPP_Record, Qt_Record }, the CPP_Record is used for comparison when searching the index.
	private final BTree cppIndex;

	public QtPDOMLinkage(PDOM pdom, long record) throws CoreException {
		super(pdom, record);

		version = pdom.getDB().getInt(Field.Version.getRecord(record));
		cppIndex = new BTree(pdom.getDB(), Field.CppIndex.getRecord(record), new CppRecordIndexComparator());
	}

	protected QtPDOMLinkage(PDOM pdom) throws CoreException {
		super(pdom, ILinkage.QT_LINKAGE_NAME, ILinkage.QT_LINKAGE_NAME.toCharArray());

		// Initialize the version with whatever is current.
		version = QtPDOMNodeType.VERSION;
		pdom.getDB().putInt(Field.Version.getRecord(record), version);

		long cppIndexRec = Field.CppIndex.getRecord(record);
		Database db = pdom.getDB();
		db.putRecPtr(cppIndexRec, 0);
		cppIndex = new BTree(db, cppIndexRec, new CppRecordIndexComparator());
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

	public QtPDOMBinding findFromCppRecord(long cppRec) throws CoreException {
		CppRecordIndexFinder finder = new CppRecordIndexFinder(cppRec);
		cppIndex.accept(finder);
		if (finder.foundQtRec == null)
			return null;

		PDOMNode node = getNode(finder.foundQtRec.longValue());
		return node instanceof QtPDOMBinding ? (QtPDOMBinding) node : null;
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

		// Otherwise try to create a new PDOMBinding.
		QtBinding qtBinding = (QtBinding) binding.getAdapter(QtBinding.class);
		if (qtBinding != null)
			switch(qtBinding.getType()) {
			case QObject:
				pdomBinding = new QtPDOMQObject(this, qtBinding);
				break;
			case QEnum:
				pdomBinding = new QtPDOMQEnum(this, adaptBinding(qtBinding.getOwner()), qtBinding);
				break;
			}

		// If a PDOMBinding was created, then add it to the linkage before returning it.
		if (pdomBinding != null) {
			addChild(pdomBinding);
			return pdomBinding;
		}

		// Otherwise fall back to looking in the C++ linkage.
		return getPDOM().getLinkage(ILinkage.CPP_LINKAGE_ID).adaptBinding(binding);
	}

	public void addChild(QtPDOMBinding child) throws CoreException {
		super.addChild(child);

		Database db = getDB();
		long pair = db.malloc(Database.PTR_SIZE * 2);
		db.putRecPtr(pair, child.getCppRecord());
		db.putRecPtr(pair + Database.PTR_SIZE, child.getRecord());
		cppIndex.insert(pair);
	}

	public long getCPPRecord(QtBinding qtBinding) throws CoreException {

		IASTName cppName = qtBinding.getCppName();
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

	@Override
	public PDOMBinding addBinding(IASTName name) throws CoreException {
		return name == null ? null : adaptBinding(name.getBinding());
	}

	@Override
	public int getBindingType(IBinding binding) {
		return binding instanceof QtBinding ? ((QtBinding) binding).getType().Type : 0;
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

	private class CppRecordIndexComparator implements IBTreeComparator {

		@Override
		public int compare(long record1, long record2) throws CoreException {
			Database db = getDB();

			Long cppRec1 = Long.valueOf(db.getRecPtr(record1));
			long cppRec2 = Long.valueOf(db.getRecPtr(record2));
			return cppRec1.compareTo(cppRec2);
		}
	}

	private class CppRecordIndexFinder extends CppRecordIndexComparator implements IBTreeVisitor {

		private final Long targetCppRec;
		public Long foundQtRec;

		public CppRecordIndexFinder(long targetCppRec) {
			this.targetCppRec = Long.valueOf(targetCppRec);
		}

		@Override
		public int compare(long record) throws CoreException {
			Long cppRec = Long.valueOf(getDB().getRecPtr(record));
			return cppRec.compareTo(targetCppRec);
		}

		@Override
		public boolean visit(long record) throws CoreException {
			// Stop searching after the record is found.
			if (foundQtRec != null)
				return false;

			// The record is the pair, so the Qt rec is the second element.
			long qtRec = getDB().getRecPtr(record + Database.PTR_SIZE);
			foundQtRec = Long.valueOf(qtRec);
			return false;
		}
	}
}
