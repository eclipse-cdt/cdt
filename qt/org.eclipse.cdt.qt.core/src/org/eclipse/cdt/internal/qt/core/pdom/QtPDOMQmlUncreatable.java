/*
 * Copyright (c) 2014 QNX Software Systems and others.
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
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPDOMQmlUncreatable extends QtPDOMQmlRegistration {

	private static int offsetInitializer = QtPDOMQmlRegistration.Field.Last.offset;

	protected static enum Field {
		Reason(Database.PTR_SIZE), Last(0);

		public final int offset;

		private Field(int sizeof) {
			this.offset = offsetInitializer;
			offsetInitializer += sizeof;
		}
	}

	public QtPDOMQmlUncreatable(QtPDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public QtPDOMQmlUncreatable(QtPDOMLinkage linkage, QmlTypeRegistration qmlTypeReg, IASTName cppName)
			throws CoreException {
		super(linkage, qmlTypeReg, cppName);

		putStringOrNull(Field.Reason.offset, qmlTypeReg.getReason());
	}

	@Override
	protected int getRecordSize() {
		return Field.Last.offset;
	}

	@Override
	public int getNodeType() {
		return QtPDOMNodeType.QmlUncreatableRegistration.Type;
	}

	public String getReason() throws CoreException {
		return getStringOrNull(Field.Reason.offset);
	}
}
