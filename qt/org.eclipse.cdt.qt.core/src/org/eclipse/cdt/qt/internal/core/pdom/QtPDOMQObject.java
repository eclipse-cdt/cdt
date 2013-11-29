/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.internal.core.pdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.IString;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * The persisted form of QObjects.
 */
@SuppressWarnings("restriction")
public class QtPDOMQObject extends QtPDOMBinding implements ICompositeType {

	// The RecordSize is initialized with the size of the parent.  It is incremented during
	// loading of the Fields enum.  This value does not reliably store the size of the
	// QtPDOMQObject record because the enum will not be initialized until it is needed.
	// The record size is retrieved as the offset of the special terminal enumerator Last.
	private static int offsetInitializer = QtPDOMBinding.Field.Last.offset;
	protected static enum Field {
		Children(4 /* From PDOMNodeLinkedList.RECORD_SIZE, which is protected */),
		ClassInfos(Database.PTR_SIZE),
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

	private final PDOMNodeLinkedList children;

	protected QtPDOMQObject(QtPDOMLinkage linkage, long record) throws CoreException {
		super(linkage, record);
		children = new PDOMNodeLinkedList(linkage, Field.Children.getRecord(record));
	}

	public QtPDOMQObject(QtPDOMLinkage linkage, QtBinding binding) throws CoreException {
		super(linkage, null, binding);
		children = new PDOMNodeLinkedList(linkage, Field.Children.getRecord(record));

		IASTName qtName = binding.getQtName();
		if (qtName instanceof QObjectName)
			setClassInfos(((QObjectName) qtName).getClassInfos());
	}

	public void setClassInfos(Map<String, String> classInfos) throws CoreException {

		// ClassInfo was not supported before version 2.
		if (getQtLinkage().getVersion() < 2)
			return;

		// Delete all entries that are currently in the list.
		long block = getDB().getRecPtr(Field.ClassInfos.getRecord(record));
		if (block != 0) {
			int numEntries = getDB().getInt(block);
			for(long b = block + Database.INT_SIZE, end = block + (numEntries * 2 * Database.PTR_SIZE); b < end; b += Database.PTR_SIZE)
				getDB().getString(b).delete();
			getDB().free(block);
		}

		// Clear the pointer if the incoming map is empty.
		if (classInfos.isEmpty()) {
			getDB().putRecPtr(Field.ClassInfos.getRecord(record), 0);
			return;
		}

		// Otherwise create a block large enough to hold the incoming list and then populate it.
		block = getDB().malloc(Database.INT_SIZE + (classInfos.size() * 2 * Database.PTR_SIZE));
		getDB().putInt(block, classInfos.size());

		long b = block + Database.INT_SIZE;
		for(Map.Entry<String, String> classInfo : classInfos.entrySet()) {
			IString key = getDB().newString(classInfo.getKey());
			IString val = getDB().newString(classInfo.getValue());

			getDB().putRecPtr(b, key.getRecord()); b += Database.PTR_SIZE;
			getDB().putRecPtr(b, val.getRecord()); b += Database.PTR_SIZE;
		}

		// Put the new block into the PDOM.
		getDB().putRecPtr(Field.ClassInfos.getRecord(record), block);
	}

	public Map<String, String> getClassInfos() throws CoreException {
		Map<String, String> classInfos = new LinkedHashMap<String, String>();

		// ClassInfo was not supported before version 2.
		if (getQtLinkage().getVersion() < 2)
			return classInfos;

		long block = getDB().getRecPtr(Field.ClassInfos.getRecord(record));
		if (block != 0) {
			int numEntries = getDB().getInt(block);
			block += Database.INT_SIZE;

			for(long end = block + (numEntries * 2 * Database.PTR_SIZE); block < end; /* in loop body */) {
				long rec = getDB().getRecPtr(block);
				IString key = getDB().getString(rec);
				block += Database.PTR_SIZE;

				rec = getDB().getRecPtr(block);
				IString val = getDB().getString(rec);
				block += Database.PTR_SIZE;

				classInfos.put(key.getString(), val.getString());
			}
		}

		return classInfos;
	}

	@Override
	protected int getRecordSize() {
		return Field.Last.offset;
	}

	@Override
	public int getNodeType() {
		return QtPDOMNodeType.QObject.Type;
	}

	// This forwarding method is to get rid of compilation warnings when clients try to call
	// #getName on the non-accessible parent.
	@Override
	public String getName() {
		return super.getName();
	}

	public List<QtPDOMQObject> findBases() throws CoreException {
		IBinding cppBinding = getCppBinding();
		if (!(cppBinding instanceof ICPPClassType))
			return Collections.emptyList();

		List<QtPDOMQObject> bases = new ArrayList<QtPDOMQObject>();
		for (ICPPBase base : ((ICPPClassType) cppBinding).getBases()) {
			if (base.getVisibility() != ICPPBase.v_public)
				continue;

			IBinding baseCls = base.getBaseClass();
			if (baseCls == null)
				continue;

			IPDOMBinding pdomBase = (IPDOMBinding) baseCls.getAdapter(IPDOMBinding.class);
			if (pdomBase == null)
				continue;

			QtPDOMBinding qtPDOMBinding = getQtLinkage().findFromCppRecord(pdomBase.getRecord());
			if (qtPDOMBinding == null)
				continue;

			QtPDOMQObject pdomQObj = (QtPDOMQObject) qtPDOMBinding.getAdapter(QtPDOMQObject.class);
			if (pdomQObj != null)
				bases.add(pdomQObj);
		}
		return bases;
	}

	@Override
	public boolean isSameType(IType type) {
		if (type == this)
			return true;

		if (!(type instanceof QtPDOMQObject))
			return false;

		QtPDOMQObject other = (QtPDOMQObject) type;
		return getRecord() == other.getRecord()
			&& getLinkage().equals(other.getLinkage());
	}

	@Override
	public int getKey() {
		return ICPPClassType.k_class;
	}

	@Override
	public boolean isAnonymous() {
		return false;
	}

	@Override
	public void addChild(PDOMNode child) throws CoreException {
		children.addMember(child);
	}

	@Override
	public IField[] getFields() {
		QtPDOMVisitor.All<IField> collector = new QtPDOMVisitor.All<IField>(IField.class);
		try {
			children.accept(collector);
		} catch(CoreException e) {
			QtPlugin.log(e);
			return IField.EMPTY_FIELD_ARRAY;
		}

		return collector.list.toArray(new IField[collector.list.size()]);
	}

	@Override
	public IField findField(String name) {
		QtPDOMVisitor.IFilter filter = new QtPDOMVisitor.PDOMNamedNodeFilter(name);
		QtPDOMVisitor.Find<IField> finder = new QtPDOMVisitor.Find<IField>(IField.class, filter);
		try {
			accept(finder);
		} catch(CoreException e) {
			QtPlugin.log(e);
		}
		return finder.element;
	}

	@Override
	public IScope getCompositeScope() {
		try {
			IBinding cppBinding = getCppBinding();
			if (cppBinding instanceof ICompositeType)
				return ((ICompositeType) cppBinding).getCompositeScope();
		} catch(CoreException e) {
			QtPlugin.log(e);
		}

		return null;
	}

    @Override
	public Object clone() {
		throw new UnsupportedOperationException();
    }
}
