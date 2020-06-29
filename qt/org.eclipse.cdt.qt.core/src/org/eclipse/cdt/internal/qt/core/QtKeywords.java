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

package org.eclipse.cdt.internal.qt.core;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;

/**
 * Declares constants related to tokens that are special in Qt applications.
 */
public class QtKeywords {
	public static final String CONNECT = "connect"; //$NON-NLS-1$
	public static final String DISCONNECT = "disconnect"; //$NON-NLS-1$
	public static final String Q_CLASSINFO = "Q_CLASSINFO"; //$NON-NLS-1$
	public static final String Q_DECLARE_FLAGS = "Q_DECLARE_FLAGS"; //$NON-NLS-1$
	public static final String Q_ENUMS = "Q_ENUMS"; //$NON-NLS-1$
	public static final String Q_FLAGS = "Q_FLAGS"; //$NON-NLS-1$
	public static final String Q_GADGET = "Q_GADGET"; //$NON-NLS-1$
	public static final String Q_INVOKABLE = "Q_INVOKABLE"; //$NON-NLS-1$
	public static final String Q_OBJECT = "Q_OBJECT"; //$NON-NLS-1$
	public static final String Q_PROPERTY = "Q_PROPERTY"; //$NON-NLS-1$
	public static final String Q_REVISION = "Q_REVISION"; //$NON-NLS-1$
	public static final String Q_SIGNAL = "Q_SIGNAL"; //$NON-NLS-1$
	public static final String Q_SIGNALS = "Q_SIGNALS"; //$NON-NLS-1$
	public static final String Q_SLOT = "Q_SLOT"; //$NON-NLS-1$
	public static final String Q_SLOTS = "Q_SLOTS"; //$NON-NLS-1$
	public static final String QMETAMETHOD = "QMetaMethod"; //$NON-NLS-1$
	public static final String QML_ATTACHED_PROPERTIES = "qmlAttachedProperties"; //$NON-NLS-1$
	public static final String QML_REGISTER_TYPE = "qmlRegisterType"; //$NON-NLS-1$
	public static final String QML_REGISTER_UNCREATABLE_TYPE = "qmlRegisterUncreatableType"; //$NON-NLS-1$
	public static final String QOBJECT = "QObject"; //$NON-NLS-1$
	public static final String SIGNAL = "SIGNAL"; //$NON-NLS-1$
	public static final String SIGNALS = "signals"; //$NON-NLS-1$
	public static final String SLOT = "SLOT"; //$NON-NLS-1$
	public static final String SLOTS = "slots"; //$NON-NLS-1$

	/**
	 * Returns true if the argument type is for Qt's QObject class and false otherwise.
	 */
	public static boolean isQObject(IType type) {
		if (!(type instanceof ICPPClassType))
			return false;

		ICPPClassType clsType = (ICPPClassType) type;
		return QtKeywords.QOBJECT.equals(clsType.getName());
	}

	/**
	 * Returns true if the argument type is for Qt's QMetaMethod class and false otherwise.
	 */
	public static boolean isQMetaMethod(IType type) {
		if (!(type instanceof ICPPClassType))
			return false;

		ICPPClassType clsType = (ICPPClassType) type;
		return QMETAMETHOD.equals(clsType.getName());
	}

	/**
	 * Returns true if the argument binding is for the QObject::connect function
	 * and false otherwise.
	 */
	public static boolean is_QObject_connect(IBinding binding) {
		String[] qualName = getFunctionQualifiedName(binding);
		return qualName != null && qualName.length == 2 && QOBJECT.equals(qualName[0]) && CONNECT.equals(qualName[1]);
	}

	/**
	 * Returns true if the argument binding is for the QObject::disconnect function
	 * and false otherwise.
	 */
	public static boolean is_QObject_disconnect(IBinding binding) {
		String[] qualName = getFunctionQualifiedName(binding);
		return qualName != null && qualName.length == 2 && QOBJECT.equals(qualName[0])
				&& DISCONNECT.equals(qualName[1]);
	}

	/**
	 * Returns true if the given binding will register a type with the QML type system and false
	 * otherwise.
	 */
	public static boolean is_QmlType(IBinding binding) {
		String[] qualName = getFunctionQualifiedName(binding);
		return qualName != null && qualName.length == 1
				&& (QML_REGISTER_TYPE.equals(qualName[0]) || QML_REGISTER_UNCREATABLE_TYPE.equals(qualName[0]));
	}

	private static String[] getFunctionQualifiedName(IBinding binding) {
		// IBinding#getAdapter returns null when binding is an instance of
		// PDOMCPPMethod.
		if (binding instanceof ICPPFunction)
			try {
				return ((ICPPFunction) binding).getQualifiedName();
			} catch (DOMException e) {
				Activator.log(e);
			}
		return null;
	}
}
