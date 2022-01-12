/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPDOMQMethod extends QtPDOMBinding {

	private static int offsetInitializer = QtPDOMBinding.Field.Last.offset;

	protected static enum Field {
		Signature(Database.PTR_SIZE), Revision(8), Flags(1), Last(0);

		public final int offset;

		private Field(int sizeof) {
			this.offset = offsetInitializer;
			offsetInitializer += sizeof;
		}

		public long getRecord(long baseRec) {
			return baseRec + offset;
		}
	}

	private static final int KIND_IS_INVOKABLE = 1;
	private static final int KIND_IS_SIGNAL = 2;
	private static final int KIND_IS_SLOT = 3;
	private static final int KIND_MASK = 3;
	private static final int HAS_REVISION = 4;

	public QtPDOMQMethod(QtPDOMLinkage linkage, long record) throws CoreException {
		super(linkage, record);
	}

	public QtPDOMQMethod(QtPDOMLinkage linkage, PDOMBinding parent, IASTName qtName, IASTName cppName,
			IQMethod.Kind kind, String qtEncSignatures, Long revision) throws CoreException {
		super(linkage, parent, qtName);

		byte flag = 0;
		switch (kind) {
		case Invokable:
			flag |= KIND_IS_INVOKABLE;
			break;
		case Signal:
			flag |= KIND_IS_SIGNAL;
			break;
		case Slot:
			flag |= KIND_IS_SLOT;
			break;
		case Unspecified:
			break;
		}

		if (revision != null) {
			flag |= HAS_REVISION;
			getDB().putLong(Field.Revision.getRecord(record), revision.longValue());
		}

		getDB().putByte(Field.Flags.getRecord(record), flag);

		long rec = qtEncSignatures == null ? 0 : getDB().newString(qtEncSignatures).getRecord();
		getDB().putRecPtr(Field.Signature.getRecord(record), rec);

		if (parent instanceof QtPDOMQObject)
			((QtPDOMQObject) parent).addChild(this);
	}

	@Override
	protected int getRecordSize() {
		return Field.Last.offset;
	}

	public IQMethod.Kind getKind() throws CoreException {
		switch (getDB().getByte(Field.Flags.getRecord(record)) & KIND_MASK) {
		case KIND_IS_INVOKABLE:
			return IQMethod.Kind.Invokable;
		case KIND_IS_SIGNAL:
			return IQMethod.Kind.Signal;
		case KIND_IS_SLOT:
			return IQMethod.Kind.Slot;
		default:
			return IQMethod.Kind.Unspecified;
		}
	}

	public String getQtEncodedSignatures() throws CoreException {
		long rec = getDB().getRecPtr(Field.Signature.getRecord(record));
		return rec == 0 ? null : getDB().getString(rec).getString();
	}

	public Long getRevision() throws CoreException {
		byte flag = getDB().getByte(Field.Flags.getRecord(record));
		if ((flag & HAS_REVISION) == 0)
			return null;

		return Long.valueOf(getDB().getLong(Field.Revision.getRecord(record)));
	}

	@Override
	public int getNodeType() {
		return QtPDOMNodeType.QMethod.Type;
	}
}
