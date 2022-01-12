/*
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.db.PDOMNodeLinkedList;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.core.runtime.CoreException;

/**
 * Qt has two types of annotation that can be applied to classes (Q_GADGET and G_OBJECT).
 * This class stores the information that is common to each.
 */
@SuppressWarnings("restriction")
public abstract class AbstractQtPDOMClass extends QtPDOMBinding {

	private static int offsetInitializer = QtPDOMBinding.Field.Last.offset;

	protected static enum Field {
		CppRecord(Database.PTR_SIZE), Children(4 /* From PDOMNodeLinkedList.RECORD_SIZE, which is protected */),
		Last(0);

		public final int offset;

		private Field(int sizeof) {
			this.offset = offsetInitializer;
			offsetInitializer += sizeof;
		}

		public long getRecord(long baseRec) {
			return baseRec + offset;
		}
	}

	private final PDOMNodeLinkedList children;

	protected AbstractQtPDOMClass(QtPDOMLinkage linkage, long record) throws CoreException {
		super(linkage, record);
		children = new PDOMNodeLinkedList(linkage, Field.Children.getRecord(record));
	}

	public AbstractQtPDOMClass(QtPDOMLinkage linkage, IASTName qtName, IASTName cppName) throws CoreException {
		super(linkage, null, qtName);

		IBinding cppBinding = getPDOM().findBinding(cppName);
		if (cppBinding != null) {
			IPDOMBinding cppPDOMBinding = cppBinding.getAdapter(IPDOMBinding.class);
			if (cppPDOMBinding != null) {
				if (cppPDOMBinding.getLinkage() != null
						&& cppPDOMBinding.getLinkage().getLinkageID() == ILinkage.CPP_LINKAGE_ID)
					getDB().putRecPtr(Field.CppRecord.getRecord(record), cppPDOMBinding.getRecord());
			}
		}

		children = new PDOMNodeLinkedList(linkage, Field.Children.getRecord(record));
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

	@Override
	protected int getRecordSize() {
		return Field.Last.offset;
	}

	// This forwarding method is to get rid of compilation warnings when clients try to call
	// #getName on the non-accessible parent.
	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public void addChild(PDOMNode child) throws CoreException {
		children.addMember(child);
	}

	public <T extends QtPDOMBinding> List<T> getChildren(Class<T> cls) throws CoreException {
		QtPDOMVisitor.All<T> collector = new QtPDOMVisitor.All<>(cls);
		try {
			children.accept(collector);
		} catch (CoreException e) {
			Activator.log(e);
			return Collections.emptyList();
		}

		return collector.list;
	}
}
