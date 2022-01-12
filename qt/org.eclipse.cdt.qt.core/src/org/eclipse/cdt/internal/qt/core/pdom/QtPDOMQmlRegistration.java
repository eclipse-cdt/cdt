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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class QtPDOMQmlRegistration extends QtPDOMBinding {

	private static int offsetInitializer = QtPDOMBinding.Field.Last.offset;

	protected static enum Field {
		CppRecord(Database.PTR_SIZE), QObjectName(Database.PTR_SIZE), Version(8), // Database doesn't have a LONG_SIZE
		Uri(Database.PTR_SIZE), Major(8), Minor(8), QmlName(Database.PTR_SIZE), Last(0);

		public final int offset;

		private Field(int sizeof) {
			this.offset = offsetInitializer;
			offsetInitializer += sizeof;
		}
	}

	public QtPDOMQmlRegistration(QtPDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public QtPDOMQmlRegistration(QtPDOMLinkage linkage, QmlTypeRegistration qmlTypeReg, IASTName cppName)
			throws CoreException {
		super(linkage, null, qmlTypeReg);

		putStringOrNull(Field.QObjectName.offset, qmlTypeReg.getQObjectName());
		putLongOrNull(Field.Version.offset, qmlTypeReg.getVersion());
		putStringOrNull(Field.Uri.offset, qmlTypeReg.getUri());
		putLongOrNull(Field.Major.offset, qmlTypeReg.getMajor());
		putLongOrNull(Field.Minor.offset, qmlTypeReg.getMinor());
		putStringOrNull(Field.QmlName.offset, qmlTypeReg.getQmlName());
	}

	public static Collection<QtPDOMQmlRegistration> findFor(QtPDOMQObject qobj) throws CoreException {
		PDOMLinkage linkage = qobj.getLinkage();
		if (linkage == null || !(linkage instanceof QtPDOMLinkage))
			return Collections.emptyList();

		String name = qobj.getName();
		if (name == null)
			return Collections.emptyList();

		QtPDOMLinkage qtLinkage = (QtPDOMLinkage) linkage;
		return qtLinkage.getQmlRegistrations(qobj.getName());
	}

	@Override
	protected int getRecordSize() {
		return Field.Last.offset;
	}

	@Override
	public int getNodeType() {
		return QtPDOMNodeType.QmlTypeRegistration.Type;
	}

	public String getQObjectName() throws CoreException {
		return getStringOrNull(Field.QObjectName.offset);
	}

	public Long getVersion() throws CoreException {
		return getLongOrNull(Field.Version.offset);
	}

	public Long getMajor() throws CoreException {
		return getLongOrNull(Field.Major.offset);
	}

	public Long getMinor() throws CoreException {
		return getLongOrNull(Field.Minor.offset);
	}

	public String getUri() throws CoreException {
		return getStringOrNull(Field.Uri.offset);
	}

	public String getQmlName() throws CoreException {
		return getStringOrNull(Field.QmlName.offset);
	}
}
