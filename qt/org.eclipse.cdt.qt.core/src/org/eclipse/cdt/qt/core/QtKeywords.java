/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.qt.core;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;

/**
 * Declares constants related to tokens that are special in Qt applications.
 */
public class QtKeywords {
	public static final String CONNECT = "connect";
	public static final String DISCONNECT = "disconnect";
	public static final String Q_CLASSINFO = "Q_CLASSINFO";
	public static final String Q_DECLARE_FLAGS = "Q_DECLARE_FLAGS";
	public static final String Q_ENUMS = "Q_ENUMS";
	public static final String Q_FLAGS = "Q_FLAGS";
	public static final String Q_GADGET = "Q_GADGET";
	public static final String Q_INVOKABLE = "Q_INVOKABLE";
	public static final String Q_OBJECT = "Q_OBJECT";
	public static final String Q_PROPERTY = "Q_PROPERTY";
	public static final String Q_REVISION = "Q_REVISION";
	public static final String Q_SIGNAL = "Q_SIGNAL";
	public static final String Q_SIGNALS = "Q_SIGNALS";
	public static final String Q_SLOT = "Q_SLOT";
	public static final String Q_SLOTS = "Q_SLOTS";
	public static final String QMETAMETHOD = "QMetaMethod";
	public static final String QML_ATTACHED_PROPERTIES = "qmlAttachedProperties";
	public static final String QML_REGISTER_TYPE = "qmlRegisterType";
	public static final String QML_REGISTER_UNCREATABLE_TYPE = "qmlRegisterUncreatableType";
	public static final String QOBJECT = "QObject";
	public static final String SIGNAL = "SIGNAL";
	public static final String SIGNALS = "signals";
	public static final String SLOT = "SLOT";
	public static final String SLOTS = "slots";

	/**
	 * Returns true if the argument binding is for the QObject::connect function
	 * and false otherwise.
	 */
	public static boolean is_QObject_connect(IBinding binding) {
		if (binding == null)
			return false;

		// IBinding#getAdapter returns null when binding is an instance of
		// PDOMCPPMethod.
		if (!(binding instanceof ICPPFunction))
			return false;

		try {
			String[] qualName = ((ICPPFunction) binding).getQualifiedName();
			return qualName.length == 2
				&& QOBJECT.equals(qualName[0])
				&& CONNECT.equals(qualName[1]);
		} catch (DOMException e) {
			return false;
		}
	}

	/**
	 * Returns true if the argument binding is for the QObject::disconnect function
	 * and false otherwise.
	 */
	public static boolean is_QObject_disconnect(IBinding binding) {
		if (binding == null)
			return false;

		// IBinding#getAdapter returns null when binding is an instance of
		// PDOMCPPMethod.
		if (!(binding instanceof ICPPFunction))
			return false;

		try {
			String[] qualName = ((ICPPFunction) binding).getQualifiedName();
			return qualName.length == 2
				&& QOBJECT.equals(qualName[0])
				&& DISCONNECT.equals(qualName[1]);
		} catch (DOMException e) {
			return false;
		}
	}
}
