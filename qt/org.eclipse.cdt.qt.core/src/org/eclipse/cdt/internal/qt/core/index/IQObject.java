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
import java.util.List;

/**
 * A class that inherits the Qt QObject and contains an expansion of Q_OBJECT.  This
 * provides a handle for retrieving signals, slots, and other Qt-related elements.
 * @see IQMethod
 */
public interface IQObject extends IQElement {

	/**
	 * The interface to be implemented by elements that can be returned as members of an
	 * implementation of {@link IQObject}.
	 */
	public static interface IMember {
		/**
		 * Return the QObject class that declares this member.  Does not return null.
		 */
		public IQObject getOwner();

		/**
		 * Returns true if it is <strong>*possible*</strong> for this member and the parameter
		 * to override each the other.  A true result indicates that <strong>*if*</strong> the members
		 * owner's are related by inheritance then one will override the other; the implementation
		 * does not check that there actually is such an inheritance relationship.
		 */
		public boolean isOverride(IMember member);
	}

	/**
	 * A wrapper for unmodifiable collections of members of a class.  Accessors provide filtered
	 * views of the member list.
	 *
	 * @see #all()
	 * @see #locals()
	 * @see #withoutOverrides()
	 */
	public static interface IMembers<T extends IMember> {
		/**
		 * Returns an unmodifiable collection with all locally declared, inherited, and overridden
		 * members.  Does not return null.
		 */
		public Collection<T> all();

		/**
		 * Returns an unmodifiable collection with only the members that are locally declared in the
		 * source class.  Does not return null.
		 */
		public Collection<T> locals();

		/**
		 * Returns an unmodifiable collection of all locally declared and inherited members with the
		 * overridden members filtered out.  Does not return null.
		 */
		public Collection<T> withoutOverrides();
	}

	/**
	 * Returns the fully qualified name of the class.
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

	/**
	 * Returns the methods that have been tagged as Qt slots.  Does not return null.
	 */
	public IMembers<IQMethod> getSlots();

	/**
	 * Returns the methods that have been tagged as Qt signals.  Does not return null.
	 */
	public IMembers<IQMethod> getSignals();

	/**
	 * Returns the methods that have been tagged with Q_INVOKABLE.  Does not return null.
	 */
	public IMembers<IQMethod> getInvokables();

	/**
	 * Returns the expansions of the Q_PROPERTY macro.  Does not return null.
	 */
	public IMembers<IQProperty> getProperties();

	/**
	 * Returns the methods that have been tagged with Q_INVOKABLE.  Does not return null.
	 */
	public Collection<IQmlRegistration> getQmlRegistrations();

	/**
	 * Examines the Q_CLASSINFO expansions to return the value associated with the given
	 * key.  Returns null if there isn't a Q_CLASSINFO for the given key.
	 */
	public String getClassInfo(String key);

	/**
	 * Returns an unsorted collection of all Q_ENUMS macro expansions within this QObject's class
	 * declaration.
	 * @see IQEnum
	 */
	public Collection<IQEnum> getEnums();
}
