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

import org.eclipse.cdt.internal.core.index.IIndexBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public enum QtPDOMNodeType {

	QObject, QEnum, QProperty, QMethod, QGadget, QmlTypeRegistration, QmlUncreatableRegistration;

	public final int Type = IIndexBindingConstants.LAST_CONSTANT + 1 + ordinal();

	/**
	 * The current version of the QtPDOMLinkage.  This can be used to make sure the persisted
	 * data matches what is expected by the implementation.  Care should be taken to make changes
	 * backward compatible when possible.
	 * <p>
	 * The version is needed because ordinals for these enumerators are written to the file.
	 * <p>
	 * This version can be reset when the PDOM's version changes because older Qt linkages will
	 * be dropped (along with everything else in that PDOM).
	 */
	public static final int VERSION = 3;

	public static QtPDOMNodeType forType(int version, int type) {
		// Nothing has been deleted or replaced yet, so the version is ignored.

		for (QtPDOMNodeType node : values())
			if (node.Type == type)
				return node;
		return null;
	}

	// This needs to return PDOMNode so that it can be either QtPDOMNode or QtPDOMBinding.
	public static PDOMNode load(QtPDOMLinkage linkage, int nodeType, long record) throws CoreException {
		QtPDOMNodeType node = QtPDOMNodeType.forType(linkage.getVersion(), nodeType);
		if (node == null)
			return null;

		switch (node) {
		case QObject:
			return new QtPDOMQObject(linkage, record);
		case QEnum:
			return new QtPDOMQEnum(linkage, record);
		case QProperty:
			return new QtPDOMProperty(linkage, record);
		case QMethod:
			return new QtPDOMQMethod(linkage, record);
		case QGadget:
			return new QtPDOMQGadget(linkage, record);
		case QmlTypeRegistration:
			return new QtPDOMQmlRegistration(linkage, record);
		case QmlUncreatableRegistration:
			return new QtPDOMQmlUncreatable(linkage, record);
		}

		return null;
	}
}
