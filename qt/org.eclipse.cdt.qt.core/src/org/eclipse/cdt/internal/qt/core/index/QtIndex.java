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

import org.eclipse.cdt.internal.qt.core.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * The public interface to the Qt index.  The Qt index is a small wrapper around the
 * core CDT's CIndex that adds Qt specific information.  The Qt index is designed to
 * interpret multiple versions of Qt, however only 4.8 has been implemented for now.
 *
 * @see #getIndex(IProject)
 */
public abstract class QtIndex {
	/**
	 * Return an instance of the Qt index for the argument project.  The CDT index is
	 * examined to discover appropriate version of Qt (using the value of QT_VERSION).
	 * Returns null if the Qt index cannot be created.  This could happen if the argument
	 * project does not have the qtnature, or if the value of QT_VERSION is not supported
	 * by this implementation.
	 *
	 * @param  project A Qt enabled project that should be indexed with Qt-specific information.
	 * @return The Qt index or null if the index cannot be created.
	 */
	public static QtIndex getIndex(IProject project) {

		if (project == null)
			return null;

		try {
			Object index = project.getSessionProperty(Activator.QTINDEX_PROP_NAME);
			if (index instanceof QtIndex)
				return (QtIndex) index;
		} catch (CoreException e) {
			Activator.log(e);
		}

		// create and store a new instance when needed
		QtIndex index = QtFactory.create(project);
		if (index == null)
			return null;

		try {
			project.setSessionProperty(Activator.QTINDEX_PROP_NAME, index);
		} catch (CoreException e) {
			Activator.log(e);
		}

		return index;
	}

	/**
	 * Find and return a subclass of QObject with the given qualified name.  Returns null if
	 * the index does not have a subclass of QObject with the given name.
	 */
	public abstract IQObject findQObject(String[] qualifiedName);

	/**
	 * Find and return a class that has been marked with the Q_GADGET macro.  These are
	 * normal C++ classes that are able to introduce Q_ENUMS and Q_FLAGS to the Qt
	 * meta-object system.  Returns null if the index does not have a Q_GADGET with
	 * the given name.
	 */
	public abstract IQGadget findQGadget(String[] qualifiedName);

	/**
	 * Find and return the types that have been registered with the Qt meta type system.  This
	 * is the result of the function calls like:
	 * <pre>
	 * qmlRegisterType<Q>( "uri", 1, 2, "Qv1.2" );
	 * </pre>
	 */
	public abstract Collection<IQmlRegistration> getQmlRegistrations();
}
