/*
 * Copyright (c) 2013, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPDOMQEnum extends QtPDOMBinding {

	private static int offsetInitializer = QtPDOMBinding.Field.Last.offset;

	protected static enum Field {
		Flags(1), CppRecord(Database.PTR_SIZE), Last(0);

		public final int offset;

		private Field(int sizeof) {
			this.offset = offsetInitializer;
			offsetInitializer += sizeof;
		}

		public long getRecord(long baseRec) {
			return baseRec + offset;
		}
	}

	private static final int IS_FLAG_MASK = 1;

	protected QtPDOMQEnum(QtPDOMLinkage linkage, long record) throws CoreException {
		super(linkage, record);
	}

	public QtPDOMQEnum(QtPDOMLinkage linkage, PDOMBinding parent, IASTName qtName, IASTName cppName)
			throws CoreException {
		super(linkage, parent, qtName);

		getDB().putRecPtr(Field.CppRecord.getRecord(record), linkage.getCPPRecord(cppName));

		// The flags are set in several sections, and then written to the Database in one operation.
		byte flags = 0;

		if (qtName instanceof QtEnumName && ((QtEnumName) qtName).isFlag())
			flags |= IS_FLAG_MASK;

		// Write the flags to the database.
		getDB().putByte(Field.Flags.getRecord(record), flags);

		if (parent instanceof AbstractQtPDOMClass)
			((AbstractQtPDOMClass) parent).addChild(this);
	}

	@Override
	public int getRecordSize() {
		return Field.Last.offset;
	}

	public IEnumeration getCppEnumeration() throws CoreException {
		long cppRec = getDB().getRecPtr(Field.CppRecord.getRecord(record));
		if (cppRec == 0)
			return null;

		PDOMLinkage cppLinkage = getPDOM().getLinkage(ILinkage.CPP_LINKAGE_ID);
		if (cppLinkage == null)
			return null;

		PDOMBinding cppBinding = cppLinkage.getBinding(cppRec);

		// TODO
		if (cppBinding == null)
			return null;
		cppBinding.getAdapter(IEnumeration.class);

		return cppBinding instanceof IEnumeration ? (IEnumeration) cppBinding : null;
	}

	public boolean isFlag() throws CoreException {
		byte flags = getDB().getByte(Field.Flags.getRecord(record));
		return (flags & IS_FLAG_MASK) == IS_FLAG_MASK;
	}

	@Override
	public int getNodeType() {
		return QtPDOMNodeType.QEnum.Type;
	}

	public List<IEnumerator> getEnumerators() throws CoreException {
		IEnumeration cppEnum = getCppEnumeration();
		return cppEnum == null ? Collections.<IEnumerator>emptyList() : Arrays.asList(cppEnum.getEnumerators());
	}
}
