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
 * Qt provides macros for marking member functions as special.  The moc compiler
 * recognizes these annotations and generates extra code to implement the special
 * behaviour.
 *
 * This interface is used to represent these methods in the Qt index.  It is used
 * for member functions that have been marked as signals, slots, and invokables.
 */
public interface IQMethod extends IQElement, IQObject.IMember {

	/**
	 * The kind of Qt annotation that has been applied to this member function.
	 * Signals and slots are implicitly invokable, if a single member function
	 * has been tagged with both signal/slot and invokable, the kind will be
	 * Signal or Slot.
	 */
	public static enum Kind {
		Unspecified, Invokable, Signal, Slot;
	}

	/**
	 * The kind of Qt annotation that has been applied to this member function.  Signals and
	 * slots are implicitly invokable, if a single member function has been tagged with both
	 * signal and invokable, the kind will be Signal (and likewise for Slot).
	 */
	public Kind getKind();

	/**
	 * Returns the function name of the method.
	 */
	public String getName();

	/**
	 * Returns the normalized C++ function signatures of the receiver method.  There is
	 * more than one signature only when at least one parameter has a default value.
	 * E.g., for
	 * #signal1 in:
	 * <pre>
	 * class T : public QObject
	 * {
	 * Q_OBJECT
	 * Q_SIGNAL void signal1( int = 5 );
	 * };
	 * </pre>
	 * This would return "{ signal1(int), signal1() }".
	 */
	public Collection<String> getSignatures();

	/**
	 * Return the revision if this method was tagged with the Q_REVISION macro and null
	 * otherwise.  The return type is Long in order to accommodate unsigned C++ 32-bit
	 * values.
	 */
	public Long getRevision();
}
