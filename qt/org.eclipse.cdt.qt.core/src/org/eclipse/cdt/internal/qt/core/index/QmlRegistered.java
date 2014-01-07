/*
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQmlRegistration;
import org.eclipse.cdt.internal.qt.core.pdom.QtPDOMQmlUncreatableRegistration;
import org.eclipse.cdt.qt.core.index.IQObject;
import org.eclipse.cdt.qt.core.index.IQmlRegistered;
import org.eclipse.core.runtime.CoreException;

public class QmlRegistered implements IQmlRegistered {

	private final QtIndexImpl qtIndex;
	private final IQmlRegistered.Kind kind;
	private final String[] ownerName;
	private final Long version;
	private final String uri;
	private final Long major;
	private final Long minor;
	private final String qmlName;
	private final String reason;
	private IQObject qObject;

	public static QmlRegistered create(QtIndexImpl qtIndex, IBinding pdom) throws CoreException {
		if (pdom instanceof QtPDOMQmlUncreatableRegistration)
			return new QmlRegistered(qtIndex, (QtPDOMQmlUncreatableRegistration) pdom);
		if (pdom instanceof QtPDOMQmlRegistration)
			return new QmlRegistered(qtIndex, (QtPDOMQmlRegistration) pdom);
		return null;
	}

	private QmlRegistered(QtIndexImpl qtIndex, QtPDOMQmlRegistration pdom) throws CoreException {
		this.qtIndex = qtIndex;
		this.kind = IQmlRegistered.Kind.Type;

		String qobjName = pdom.getQObjectName();
		this.ownerName = qobjName == null ? null : qobjName.split("::");

		this.version = pdom.getVersion();
		this.uri = pdom.getUri();
		this.major = pdom.getMajor();
		this.minor = pdom.getMinor();
		this.qmlName = pdom.getQmlName();
		this.reason = null;
	}

	private QmlRegistered(QtIndexImpl qtIndex, QtPDOMQmlUncreatableRegistration pdom) throws CoreException {
		this.qtIndex = qtIndex;
		this.kind = IQmlRegistered.Kind.Uncreatable;

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
	public IQmlRegistered.Kind getKind() {
		return kind;
	}

	@Override
	public IQObject getQObject() {
		if (qObject == null
		 && ownerName != null)
			qObject = qtIndex.findQObject(ownerName);
		return qObject;
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
