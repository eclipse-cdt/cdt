/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.qt.core.index;

import java.util.List;

/**
 * A class that inherits the Qt QObject and contains an expansion of Q_OBJECT.  This
 * provides a handle for retrieving signals, slots, and other Qt-related elements.
 * @see IQMethod
 */
public interface IQObject extends IQElement {
	/**
	 * Returns the name of the class.
	 */
	public String getName();

	/**
	 * Returns a list of the QObject's that are bases of this class.
	 * E.g. in:
	 * <pre>
	 * class T {};
	 * class B1 : public QObject { Q_OBJECT };
	 * class B2 : public QObject { Q_OBJECT };
	 * class B3 : public T, public QObject { };
	 * class D : public B1, public B2, public B3, public T { Q_OBJECT };
	 * </pre>
	 * The list of bases for D will contain B1 and B2, but not B3 or T.
	 * <p>
	 * The list will be ordered as in the C++ code and will include only the directly declared
	 * base classes.
	 */
	public List<IQObject> getBases();
}
