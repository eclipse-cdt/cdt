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
package org.eclipse.cdt.internal.qt.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.qt.core.index.IQObject.IMember;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQmlRegistration;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQmlUncreatable;
import org.eclipse.core.runtime.CoreException;

public class QmlRegistration implements IQmlRegistration {

	private final QtIndexImpl qtIndex;
	private final IQmlRegistration.Kind kind;
	private final String[] ownerName;
	private final Long version;
	private final String uri;
	private final Long major;
	private final Long minor;
	private final String qmlName;
	private final String reason;
	private IQObject qObject;

	public static QmlRegistration create(QtIndexImpl qtIndex, IBinding pdom) throws CoreException {
		if (pdom instanceof QtPDOMQmlUncreatable)
			return new QmlRegistration(qtIndex, (QtPDOMQmlUncreatable) pdom);
		if (pdom instanceof QtPDOMQmlRegistration)
			return new QmlRegistration(qtIndex, (QtPDOMQmlRegistration) pdom);
		return null;
	}

	private QmlRegistration(QtIndexImpl qtIndex, QtPDOMQmlRegistration pdom) throws CoreException {
		this.qtIndex = qtIndex;
		this.kind = IQmlRegistration.Kind.Type;

		String qobjName = pdom.getQObjectName();
		this.ownerName = qobjName == null ? null : qobjName.split("::");

		this.version = pdom.getVersion();
		this.uri = pdom.getUri();
		this.major = pdom.getMajor();
		this.minor = pdom.getMinor();
		this.qmlName = pdom.getQmlName();
		this.reason = null;
	}

	private QmlRegistration(QtIndexImpl qtIndex, QtPDOMQmlUncreatable pdom) throws CoreException {
		this.qtIndex = qtIndex;
		this.kind = IQmlRegistration.Kind.Uncreatable;

		String qobjName = pdom.getQObjectName();
		this.ownerName = qobjName == null ? null : qobjName.split("::");

		this.version = pdom.getVersion();
		this.uri = pdom.getUri();
		this.major = pdom.getMajor();
		this.minor = pdom.getMinor();
		this.qmlName = pdom.getQmlName();
		this.reason = pdom.getReason();
	}

	@Override
	public IQmlRegistration.Kind getKind() {
		return kind;
	}

	@Override
	public IQObject getQObject() {
		if (qObject == null && ownerName != null)
			qObject = qtIndex.findQObject(ownerName);
		return qObject;
	}

	// TODO remove getQObject from the API
	@Override
	public IQObject getOwner() {
		return getQObject();
	}

	@Override
	public boolean isOverride(IMember member) {
		// TODO I think that qmlRegistrations are never overridden
		return false;
	}

	@Override
	public Long getVersion() {
		return version;
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public Long getMajor() {
		return major;
	}

	@Override
	public Long getMinor() {
		return minor;
	}

	@Override
	public String getQmlName() {
		return qmlName;
	}

	@Override
	public String getReason() {
		return reason;
	}
}
