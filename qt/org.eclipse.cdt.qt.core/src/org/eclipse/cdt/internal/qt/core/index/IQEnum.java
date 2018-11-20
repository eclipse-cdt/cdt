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
package org.eclipse.cdt.internal.qt.core.index;

import java.util.Collection;

/**
 * Represents expansions of the Q_ENUMS macro within a class declaration.
 * <pre>
 * class B : public QObject
 * {
 * Q_OBJECT
 * enum E { enumerator };
 * Q_ENUMS( E )
 * };
 *
 * class Q : public QObject
 * {
 * Q_OBJECT
 * Q_ENUMS( B::E E0 )
 * Q_ENUMS( E1 )
 * enum E0 { e0a, e0b = 2 };
 * enum E1 { e1 };
 * }
 * </pre>
 * NOTE: http://qt-project.org/doc/qt-4.8/qobject.html#Q_ENUMS
 * <blockquote>
 * If you want to register an enum that is declared in another class, the enum must be fully qualified
 * with the name of the class defining it. In addition, the class defining the enum has to inherit
 * QObject as well as declare the enum using Q_ENUMS().
 * </blockquote>
 * So, the lookup for the C++ enum only needs to look in the same class spec when the name is not
 * qualified.  When it is qualified, then it needs to find the QObject and then look at its Q_ENUMS.
 */
public interface IQEnum {
	/**
	 * Returns the name of the enumerator as referenced in parameter in the Q_ENUMS
	 * macro expansion.  In the sample code in the class declaration, this would return
	 * "B::E", "E0", or "E1".
	 */
	public String getName();

	/**
	 * Returns true if this enumeration was introduced to the Qt meta-object system with
	 * a Q_FLAGS expansion and false if it was introduced with Q_ENUMS.
	 */
	public boolean isFlag();

	/**
	 * Returns an unsorted collection of the enumerators contained in the enum references
	 * in the Q_ENUMS macro expansion.
	 * <p>
	 * NOTE: It would be nice if the textual order of the enumerators was preserved by the
	 * underlying CDT index, but it is not.  The {@link Enumerator#getOrdinal()} method can
	 * be used to recover some ordering information.
	 */
	public Collection<Enumerator> getEnumerators();

	/**
	 * A small wrapper class for the enumerators that are declared within the enum that is
	 * referenced by the parameter of the Q_ENUMS macro expansion.
	 */
	public interface Enumerator {
		/**
		 * Returns the name of the enumerator.
		 */
		public String getName();

		/**
		 * Returns the ordinal (either explicitly or implicitly) assigned to the enumerator.
		 */
		public Long getOrdinal();
	}
}
