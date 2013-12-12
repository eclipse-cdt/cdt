/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.qt.core.QtPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * The persisted form of QObjects.
 */
@SuppressWarnings("restriction")
public class QtPDOMQObject extends QtPDOMBinding {

	private static int offsetInitializer = QtPDOMBinding.Field.Last.offset;
	protected static enum Field {
		CppRecord(Database.PTR_SIZE, 3),
		Children(4 /* From PDOMNodeLinkedList.RECORD_SIZE, which is protected */, 0),
		ClassInfos(Database.PTR_SIZE, 2),
		Last(0, 0);

		private final int offset;
		private final int version;

		private Field(int sizeof, int version) {
			this.offset = offsetInitializer;
			this.version = version;
			offsetInitializer += sizeof;
		}

		public long getRecord(long baseRec) {
			return baseRec + offset;
		}

		/**
		 * Return true if this linkage in supported in the given instance of the linkage.
		 */
		public boolean isSupportedIn(QtPDOMLinkage linkage) {
			return linkage.getVersion() >= version;
		}
	}

	private final PDOMNodeLinkedList children;

	protected QtPDOMQObject(QtPDOMLinkage linkage, long record) throws CoreException {
		super(linkage, record);
		children = new PDOMNodeLinkedList(linkage, Field.Children.getRecord(record));
	}

	public QtPDOMQObject(QtPDOMLinkage linkage, IASTName qtName, IASTName cppName) throws CoreException {
		super(linkage, null, qtName);

		IBinding cppBinding = getPDOM().findBinding(cppName);
		if (cppBinding != null) {
			IPDOMBinding cppPDOMBinding = (IPDOMBinding) cppBinding.getAdapter(IPDOMBinding.class);
			if (cppPDOMBinding != null) {
				if (cppPDOMBinding.getLinkage() != null
				 && cppPDOMBinding.getLinkage().getLinkageID() == ILinkage.CPP_LINKAGE_ID)
					getDB().putRecPtr(Field.CppRecord.getRecord(record), cppPDOMBinding.getRecord());
			}
		}

		children = new PDOMNodeLinkedList(linkage, Field.Children.getRecord(record));

		if (qtName instanceof QObjectName) {
			QObjectName qobjName = (QObjectName) qtName;
			setClassInfos(qobjName.getClassInfos());
		}
	}

	public void delete() throws CoreException {
		long fieldRec = Field.ClassInfos.getRecord(record);
		new QtPDOMArray<ClassInfo>(getQtLinkage(), ClassInfo.Codec, fieldRec).delete();
		getDB().putRecPtr(Field.ClassInfos.getRecord(record), 0);
	}

	public ICPPClassType getCppClassType() throws CoreException {
		long cppRec = getDB().getRecPtr(Field.CppRecord.getRecord(record));
		if (cppRec == 0)
			return null;

		PDOMLinkage cppLinkage = getPDOM().getLinkage(ILinkage.CPP_LINKAGE_ID);
		if (cppLinkage == null)
			return null;

		PDOMBinding cppBinding = cppLinkage.getBinding(cppRec);
		return cppBinding instanceof ICPPClassType ? (ICPPClassType) cppBinding : null;
	}

	public void setClassInfos(Map<String, String> classInfos) throws CoreException {

		// Make sure the version of the linkage contains this field.
		if (!Field.ClassInfos.isSupportedIn(getQtLinkage()))
			return;

		// Create an array to be stored to the PDOM.
		ClassInfo[] array = new ClassInfo[classInfos.size()];
		Iterator<Map.Entry<String, String>> iterator = classInfos.entrySet().iterator();
		for(int i = 0; i < array.length && iterator.hasNext(); ++i) {
			Map.Entry<String, String> entry = iterator.next();
			array[i] = new ClassInfo(entry.getKey(), entry.getValue());
		}

		// Store the array into the Database.
		long arrayRec = getDB().getRecPtr(Field.ClassInfos.getRecord(record));
		QtPDOMArray<ClassInfo> pdomArray = new QtPDOMArray<QtPDOMQObject.ClassInfo>(getQtLinkage(), ClassInfo.Codec, arrayRec);
		arrayRec = pdomArray.set(array);

		// Update the record that is stored in the receiver's field.
		getDB().putRecPtr(Field.ClassInfos.getRecord(record), arrayRec);
	}

	public Map<String, String> getClassInfos() throws CoreException {
		Map<String, String> classInfos = new LinkedHashMap<String, String>();

		// Make sure the version of the linkage contains this field.
		if (!Field.ClassInfos.isSupportedIn(getQtLinkage()))
			return classInfos;

		// Read the array from the Database and insert the elements into the Map that is to be returned.
		long arrayRec = getDB().getRecPtr(Field.ClassInfos.getRecord(record));
		QtPDOMArray<ClassInfo> pdomArray = new QtPDOMArray<QtPDOMQObject.ClassInfo>(getQtLinkage(), ClassInfo.Codec, arrayRec);

		ClassInfo[] array = pdomArray.get();
		if (array == null)
			return classInfos;

		for(ClassInfo classInfo : array)
			classInfos.put(classInfo.key, classInfo.value);

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
		ICPPClassType cppClassType = getCppClassType();
		if (cppClassType == null)
			return Collections.emptyList();

		List<QtPDOMQObject> bases = new ArrayList<QtPDOMQObject>();
		for (ICPPBase base : cppClassType.getBases()) {
			if (base.getVisibility() != ICPPBase.v_public)
				continue;

			IBinding baseCls = base.getBaseClass();
			if (baseCls == null)
				continue;

			PDOMBinding pdomBinding = (PDOMBinding) baseCls.getAdapter(PDOMBinding.class);
			QtPDOMQObject baseQObj = ASTNameReference.findFromBinding(QtPDOMQObject.class, pdomBinding);
			if (baseQObj != null)
				bases.add(baseQObj);
		}

		return bases;
	}

	@Override
	public void addChild(PDOMNode child) throws CoreException {
		children.addMember(child);
	}

	public <T extends QtPDOMBinding> List<T> getChildren(Class<T> cls) throws CoreException {
		QtPDOMVisitor.All<T> collector = new QtPDOMVisitor.All<T>(cls);
		try {
			children.accept(collector);
		} catch(CoreException e) {
			QtPlugin.log(e);
			return Collections.emptyList();
		}

		return collector.list;
	}

    private static class ClassInfo {
    	public final String key;
    	public final String value;
    	public ClassInfo(String key, String value) {
    		this.key = key;
    		this.value = value;
    	}

    	public static final IQtPDOMCodec<ClassInfo> Codec = new IQtPDOMCodec<ClassInfo>() {
			@Override
			public int getElementSize() {
				return 2 * Database.PTR_SIZE;
			}

			@Override
			public ClassInfo[] allocArray(int count) {
				return new ClassInfo[count];
			}

			@Override
			public ClassInfo decode(QtPDOMLinkage linkage, long record) throws CoreException {
				long keyRec = linkage.getDB().getRecPtr(record);
				long valRec = linkage.getDB().getRecPtr(record + Database.PTR_SIZE);
				return new ClassInfo(linkage.getDB().getString(keyRec).getString(), linkage.getDB().getString(valRec).getString());
			}

			@Override
			public void encode(QtPDOMLinkage linkage, long record, ClassInfo element) throws CoreException {
				// Delete the existing strings then create and store new ones.
				long rec = linkage.getDB().getRecPtr(record);
				if (rec != 0)
					linkage.getDB().getString(rec).delete();
				linkage.getDB().putRecPtr(record, element == null ? 0 : linkage.getDB().newString(element.key).getRecord());

				rec = linkage.getDB().getRecPtr(record + Database.PTR_SIZE);
				if (rec != 0)
					linkage.getDB().getString(rec).delete();
				linkage.getDB().putRecPtr(record + Database.PTR_SIZE, element == null ? 0 : linkage.getDB().newString(element.value).getRecord());
			}
    	};
    }
}
